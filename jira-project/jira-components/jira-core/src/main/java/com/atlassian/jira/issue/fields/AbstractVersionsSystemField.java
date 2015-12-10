package com.atlassian.jira.issue.fields;

import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.HackyRendererType;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.VersionJsonBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.fields.util.VersionHelperBean;
import com.atlassian.jira.issue.search.handlers.SearchHandlerFactory;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.VersionProxy;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraEntityUtils;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkMoveHelper;
import com.atlassian.jira.web.bean.DefaultBulkMoveHelper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notEmpty;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public abstract class AbstractVersionsSystemField extends AbstractOrderableNavigableFieldImpl implements HideableField, RequirableField, RestAwareField
{
    private static final Logger log = Logger.getLogger(AbstractVersionsSystemField.class);

    public static final Long UNKNOWN_VERSION_ID = -1L;
    public static final Long UNRELEASED_VERSION_ID = -2L;
    public static final Long RELEASED_VERSION_ID = -3L;

    protected final VersionManager versionManager;
    protected final VersionHelperBean versionHelperBean;
    private final JiraBaseUrls jiraBaseUrls;
    private Predicate<Long> validVersionId = new Predicate<Long>()
    {
        @Override
        public boolean apply(Long versionId)
        {
            if ((UNKNOWN_VERSION_ID.equals(versionId)) || (UNRELEASED_VERSION_ID.equals(versionId)) || (RELEASED_VERSION_ID.equals(versionId))) {
                return true;
            }
            final Version version = versionManager.getVersion(versionId);
            return version != null;
        }
    };


    public AbstractVersionsSystemField(String id, String name, VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties,
            VersionManager versionManager, PermissionManager permissionManager, JiraAuthenticationContext authenticationContext,
            VersionHelperBean versionHelperBean, SearchHandlerFactory searchHandlerFactory, JiraBaseUrls jiraBaseUrls)
    {
        super(id, name, templatingEngine, applicationProperties, authenticationContext, permissionManager, searchHandlerFactory);
        this.versionManager = versionManager;
        this.versionHelperBean = versionHelperBean;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put("versions", getPossibleVersions(issue.getProjectObject(), getUnreleasedVersionsFirst()));
        velocityParams.put("unknownVersionId", UNKNOWN_VERSION_ID);
        velocityParams.put("unreleasedVersionId", UNRELEASED_VERSION_ID);
        velocityParams.put("releasedVersionId", RELEASED_VERSION_ID);
        velocityParams.put("currentVersions", LongIdsValueHolder.fromFieldValuesHolder(getId(), operationContext.getFieldValuesHolder()));
        if (fieldLayoutItem != null)
        {
            velocityParams.put("isFrotherControl", HackyRendererType.fromKey(fieldLayoutItem.getRendererType()) == HackyRendererType.FROTHER_CONTROL);
        }
        velocityParams.put("createPermission", getPermissionManager().hasPermission(Permissions.PROJECT_ADMIN, issue.getProjectObject(), authenticationContext.getUser()));

        return renderTemplate("versions-edit.vm", velocityParams);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Project project = issue.getProjectObject();
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        LongIdsValueHolder currentVersions =  LongIdsValueHolder.fromFieldValuesHolder(getId(), operationContext.getFieldValuesHolder());
        if (currentVersions != null) {
            currentVersions.validateIds(validVersionId); // remove any remaining invalid ids from the list
        }

        velocityParams.put("currentVersions", currentVersions);
        velocityParams.put("frotherInputText", currentVersions == null ? null : currentVersions.getInputText());
        velocityParams.put("unknownVersionId", UNKNOWN_VERSION_ID);
        velocityParams.put("unreleasedVersionId", UNRELEASED_VERSION_ID);
        velocityParams.put("releasedVersionId", RELEASED_VERSION_ID);
        velocityParams.put("archivedVersions", getArchivedVersionsThatAreSelected(issue, getCurrentVersions(issue)));
        velocityParams.put("archivedVersionsTitle", getArchivedVersionsFieldTitle());
        velocityParams.put("archivedVersionsSearchParam", getArchivedVersionsFieldSearchParam());
        velocityParams.put("project", issue.getProject());
        velocityParams.put("versions", getPossibleVersions(project, getUnreleasedVersionsFirst()));
        if (fieldLayoutItem != null)
        {
            velocityParams.put("isFrotherControl", HackyRendererType.fromKey(fieldLayoutItem.getRendererType()) == HackyRendererType.FROTHER_CONTROL);
        }
        velocityParams.put("createPermission", getPermissionManager().hasPermission(Permissions.PROJECT_ADMIN, issue.getProjectObject(), authenticationContext.getUser()));

        return renderTemplate("versions-edit.vm", velocityParams);
    }

    /**
     * Returns HTML that should be shown when a version field is being bulk edited.
     *
     * The HTML displayed for Bulk Move of Versions needs to allow the user to specify mappings for each old version
     * present in the currently selected issues.
     */
    @Override
    public String getBulkEditHtml(OperationContext operationContext, Action action, BulkEditBean bulkEditBean, Map displayParameters)
    {
        notNull("bulkEditBean", bulkEditBean);
        notEmpty("selectedIssues", bulkEditBean.getSelectedIssues());

        if (BulkMoveOperation.NAME.equals(bulkEditBean.getOperationName()))
        {
            final FieldLayoutItem fieldLayoutItem = bulkEditBean.getTargetFieldLayout().getFieldLayoutItem(this);
            final BulkMoveHelper bulkMoveHelper = new DefaultBulkMoveHelper();

            final Function<Object, String> versionNameResolver = new Function<Object, String>()
            {
                public String get(final Object input)
                {
                    return versionManager.getVersion((Long) input).getName();
                }
            };
            final Function<Issue, Collection<Object>> issueValueResolver = new Function<Issue, Collection<Object>>()
            {
                public Collection<Object> get(final Issue input)
                {
                    final Map fieldValuesHolder = new LinkedHashMap();
                    populateFromIssue(fieldValuesHolder, input);
                    return (Collection<Object>) fieldValuesHolder.get(getId());
                }
            };

            final Map<Long, BulkMoveHelper.DistinctValueResult> distinctVersionValues = bulkMoveHelper.getDistinctValuesForMove(bulkEditBean, this, issueValueResolver, versionNameResolver);

            final Issue issue = bulkEditBean.getFirstTargetIssueObject();
            final Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);

            // the distinct values which need to be mapped
            velocityParams.put("valuesToMap", distinctVersionValues);
            velocityParams.put("bulkMoveHelper", bulkMoveHelper);

            // List of possible Versions for the project
            velocityParams.put("unknownVersionId", UNKNOWN_VERSION_ID);
            velocityParams.put("unreleasedVersionId", UNRELEASED_VERSION_ID);
            velocityParams.put("releasedVersionId", RELEASED_VERSION_ID);
            velocityParams.put("versions", getPossibleVersions(issue.getProjectObject(), getUnreleasedVersionsFirst()));
            if (fieldLayoutItem != null)
            {
                velocityParams.put("isFrotherControl", HackyRendererType.fromKey(fieldLayoutItem.getRendererType()) == HackyRendererType.FROTHER_CONTROL);
            }
            return renderTemplate("versions-bulkmove.vm", velocityParams);
        }
        else
        {
            return super.getBulkEditHtml(operationContext, action, bulkEditBean, displayParameters);
        }
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put("versions", getCurrentVersions(issue));
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        velocityParams.put("versions", value);
        return getViewHtml(velocityParams);
    }

    protected Map<String, Object> addViewVelocityParams()
    {
        return FieldMap.build("linkToBrowseFixFor", Boolean.FALSE);
    }

    private String getViewHtml(Map<String, Object> velocityParams)
    {
        return renderTemplate("versions-view.vm", velocityParams);
    }

    protected Object getRelevantParams(Map<String, String[]> params)
    {
        String[] value = params.get(getId());
        LongIdsValueHolder vh = new LongIdsValueHolder(value);
        vh.validateIds(validVersionId);
        return vh;
    }

    public Collection<Version> getValueFromParams(Map params)
    {
        LongIdsValueHolder versionIds = LongIdsValueHolder.fromFieldValuesHolder(getId(), params);
        if (versionIds == null || versionIds.contains(UNKNOWN_VERSION_ID))
        {
            return Collections.emptyList();
        }
        else
        {
            return versionManager.getVersions(new LinkedList<Long>(versionIds));
        }
    }

    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
        fieldValuesHolder.put(getId(), new LongIdsValueHolder(getVersionIds(issue, stringValue)));
    }

    private List<Long> getVersionIds(Issue issue, String stringValue) throws FieldValidationException
    {
        // Use a set to ensure that there are no duplicate version ids.
        Set<Long> versions = new HashSet<Long>();

        // Check if the versions were provided
        if (TextUtils.stringSet(stringValue))
        {
            // If so set the values
            String[] versionParams = StringUtils.split(stringValue, ",");
            for (String versionParam : versionParams)
            {
                try
                {
                    versions.add(Long.valueOf(versionParam));
                }
                catch (NumberFormatException e)
                {
                    // Try getting the version by name
                    Version version = versionManager.getVersion(issue.getProjectObject().getId(), versionParam);
                    if (version != null)
                    {
                        versions.add(version.getId());
                    }
                    else
                    {
                        throw new FieldValidationException("Invalid version name '" + versionParam + "'.");
                    }
                }
            }
        }

        return new ArrayList<Long>(versions);
    }

    public void populateFromIssue(Map<String, Object> params, Issue issue)
    {
        params.put(getId(), getCurrentVersionIds(issue));
    }

    protected LongIdsValueHolder getCurrentVersionIds(Issue issue)
    {
        List<Long> currentVersionIds = new LinkedList<Long>();
        for (Version version : getCurrentVersions(issue))
        {
            currentVersionIds.add(version.getId());
        }

        return new LongIdsValueHolder(currentVersionIds);
    }

    protected abstract Collection<Version> getCurrentVersions(Issue issue);

    protected abstract String getArchivedVersionsFieldTitle();

    protected abstract String getArchivedVersionsFieldSearchParam();

    protected abstract boolean getUnreleasedVersionsFirst();

    protected List<VersionProxy> getPossibleVersions(Project project, boolean unreleasedFirst)
    {
        List<VersionProxy> unreleased = new ArrayList<VersionProxy>();

        Iterator<Version> unreleasedIter = versionManager.getVersionsUnreleased(project.getId(), false).iterator();
        if (unreleasedIter.hasNext())
        {
            unreleased.add(new VersionProxy(UNRELEASED_VERSION_ID.intValue(), getAuthenticationContext().getI18nHelper().getText("common.filters.unreleasedversions")));
            while (unreleasedIter.hasNext())
            {
                Version version = unreleasedIter.next();
                unreleased.add(new VersionProxy(version));
            }
        }

        //reverse the order of the releasedIter versions.
        List<VersionProxy> released = new ArrayList<VersionProxy>();
        ArrayList<Version> releasedIter = new ArrayList<Version>(versionManager.getVersionsReleased(project.getId(), false));
        if (!releasedIter.isEmpty())
        {
            released.add(new VersionProxy(RELEASED_VERSION_ID.intValue(), getAuthenticationContext().getI18nHelper().getText("common.filters.releasedversions")));
            Collections.reverse(releasedIter);
            for (final Object aReleasedIter : releasedIter)
            {
                released.add(new VersionProxy((Version) aReleasedIter));
            }
        }

        List<VersionProxy> versions = new ArrayList<VersionProxy>();
        if (unreleasedFirst)
        {
            versions.addAll(unreleased);
            versions.addAll(released);
        }
        else
        {
            versions.addAll(released);
            versions.addAll(unreleased);
        }

        return versions;
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollection, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        LongIdsValueHolder versionIds = LongIdsValueHolder.fromFieldValuesHolder(getId(), operationContext.getFieldValuesHolder());
        validateForRequiredField(errorCollection, i18n, issue, fieldScreenRenderLayoutItem, versionIds);

        boolean validIds = versionHelperBean.validateVersionIds(versionIds, errorCollection, i18n, getId());
        if (validIds)
        {
            // only do this validation if they are valid Ids
            versionHelperBean.validateVersionForProject(versionIds, issue.getProjectObject(), errorCollection, i18n, getId());
        }

        if (versionIds != null)
        {
            final String bad = versionIds.getInputText();
            if (StringUtils.isNotBlank(bad))
            {
                errorCollection.addError(getId(), i18n.getText("issue.field.versions.invalid.version.id", bad), ErrorCollection.Reason.VALIDATION_FAILED);
            }
            else if (!versionIds.getValuesToAdd().isEmpty())
            {
                versionHelperBean.validateVersionsToCreate(authenticationContext.getUser(), i18n, issue.getProjectObject(), getId(), versionIds.getValuesToAdd(), errorCollection);
            }
        }
    }

    @VisibleForTesting
    void validateForRequiredField(final ErrorCollection errorCollection, final I18nHelper i18n, final Issue issue, final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem, final LongIdsValueHolder versionIds)
    {
        // The check for 'unknown' is needed for bulk-edit
        boolean noExistingVersionsSpecified = versionIds == null || versionIds.isEmpty() || versionIds.contains(UNKNOWN_VERSION_ID);
        boolean noNewVersionsSpecified = versionIds != null && versionIds.getValuesToAdd().isEmpty();
        boolean noArchivedVersionsSpecified = getArchivedVersionsThatAreSelected(issue, getCurrentVersions(issue)).isEmpty();

        if (fieldScreenRenderLayoutItem.isRequired() && noExistingVersionsSpecified && noNewVersionsSpecified && noArchivedVersionsSpecified)
        {
            addFieldRequiredErrorMessage(issue, errorCollection, i18n);
        }
    }


    protected abstract void addFieldRequiredErrorMessage(Issue issue, ErrorCollection errorCollectionToAddTo, I18nHelper i18n);

    protected abstract String getModifiedWithoutPermissionErrorMessage(I18nHelper i18n);

    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        Collection currentVersions = (Collection) modifiedValue.getOldValue();

        Collection value = getUpdatedVersions(issue, (Collection) modifiedValue.getNewValue(), currentVersions);

        if (currentVersions == null || currentVersions.isEmpty())
        {
            if (value != null)
            {
                issueChangeHolder.addChangeItems(updateIssueValue(issue, value));
            }
        }
        else
        {
            if (!valuesEqual(value, currentVersions))
            {
                issueChangeHolder.addChangeItems(updateIssueValue(issue, value));
            }
        }
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        // Iterate over the collection of issues - if one issue requires a change - return early
        for (final Object originalIssue1 : originalIssues)
        {
            Issue originalIssue = (Issue) originalIssue1;
            // If the projects are different then may need to ask user to specify new version
            if (!originalIssue.getProjectObject().equals(targetIssue.getProjectObject()))
            {
                if (!hasValue(originalIssue))
                {
                    // If the versions field is empty and the target field layout does not require versions then no need to
                    // ask the user for input
                    if (targetFieldLayoutItem.isRequired())
                    {
                        return new MessagedResult(true);
                    }
                }
                else
                {
                    // If we have versions then we need to change them as projects are different.
                    return new MessagedResult(true);
                }
            }
            else
            {
                // Same project (different issue type) - need to see if the field is required in the target field layout
                if (getCurrentVersions(originalIssue).isEmpty() && targetFieldLayoutItem.isRequired())
                {
                    return new MessagedResult(true);
                }
            }
        }
        return new MessagedResult(false);
    }

    public void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // Preselect versions with the same name
        Collection<String>  currentVersionNames = getVersionNames(getCurrentVersions(originalIssue));
        Collection<Version> possibleVersions = getVersionManager().getVersions(targetIssue.getProjectObject());
        Collection<Long> versionIds = new LinkedList<Long>();

        for (final Version version : possibleVersions)
        {
            // Ensure we do not have a version proxy
            if (currentVersionNames.contains(version.getName()))
            {
                versionIds.add(version.getId());
            }
        }

        fieldValuesHolder.put(getId(), versionIds);
    }

    public boolean hasValue(Issue issue)
    {
        Collection currentVersions = getCurrentVersions(issue);
        return (currentVersions != null && !currentVersions.isEmpty());
    }

    private Collection<String> getVersionNames(Collection<Version> versions)
    {
        Set<String> versionNames = new HashSet<String>();
        for (final Version version : versions)
        {
            versionNames.add(version.getName());
        }

        return versionNames;
    }

    private Collection getUpdatedVersions(Issue issue, Collection selectedVersions, Collection currentVersions)
    {
        Collection affectedVersionNumbers = getArchivedVersionsThatAreSelected(issue, currentVersions);

        if (affectedVersionNumbers != null && !affectedVersionNumbers.isEmpty())
        {
            //avoid null pointer exception below or an unmodifiable empty collection
            if (selectedVersions == null || selectedVersions.isEmpty())
                selectedVersions = new LinkedList();

            selectedVersions.addAll(affectedVersionNumbers);
        }

        return selectedVersions;
    }

    private Collection getArchivedVersionsThatAreSelected(Issue issue, Collection selectedVersions)
    {
        Collection archivedVersions = versionManager.getVersionsArchived(issue.getProjectObject());
        archivedVersions.retainAll(selectedVersions);

        return archivedVersions;
    }

    private List updateIssueValue(Issue issue, Object value)
    {
        try
        {
            Collection versionGVs = new LinkedList();
            for (final Object o : ((Collection) value))
            {
                Version version = (Version) o;
                versionGVs.add(version.getGenericValue());
            }

            // TODO rewrite using a version manager
            return JiraEntityUtils.updateDependentEntities(issue.getGenericValue(), versionGVs, getIssueRelationName(), getChangeItemFieldName());
        }
        catch (GenericEntityException e)
        {
            log.error("Error while saving versions '" + value + "' for issue with id '" + issue.getLong("id") + "'.");
        }

        return null;
    }

    public void createValue(Issue issue, Object value)
    {
        updateIssueValue(issue, value);
    }

    public void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        //by default select no versions.  NOTE: This is also important for bulk/single move code
        //as it will be called if the target project has the version field hidden or no permission to edit. (JRA-16007)
        fieldValuesHolder.put(getId(), new LongIdsValueHolder(Collections.<Long>emptyList()));
    }

    public Object getDefaultValue(Issue issue)
    {
        return Collections.EMPTY_LIST;
    }

    protected abstract String getChangeItemFieldName();

    protected abstract String getIssueRelationName();

    protected VersionManager getVersionManager()
    {
        return versionManager;
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map<String, Object> velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("versions", getCurrentVersions(issue));
        velocityParams.putAll(addViewVelocityParams());
        return renderTemplate("versions-columnview.vm", velocityParams);
    }

    /////////////////////////////////////////// Bulk Edit //////////////////////////////////////////////////////////
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Can bulk-edit this field only if all selected issue belong to one project
        if (bulkEditBean.isMultipleProjects())
        {
            // Let the user know that selected issues belong to more than one project so the action is not available
            return "bulk.edit.unavailable.multipleprojects";
        }

        // Ensure that the project has versions
        if (getVersionManager().getVersions(bulkEditBean.getSingleProject()).isEmpty())
        {
            return "bulk.edit.unavailable.noversions";
        }

        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        for (FieldLayout fieldLayout : bulkEditBean.getFieldLayouts())
        {
            if (fieldLayout.isFieldHidden(getId()))
            {
                return "bulk.edit.unavailable.hidden";
            }
        }

        // If we got here then the field is visible in all field layouts
        // So check for permissions
        // Need to check for EDIT permission here rather than in the BulkEdit itself, as a user does not need the EDIT permission to edit the ASSIGNEE field,
        // just the ASSIGNEE permission, so the permissions to check depend on the field
        // We need to look through each issue as permissions can be given to current assignee / reporter
        for (Issue issue : bulkEditBean.getSelectedIssues())
        {
            if (!(hasBulkUpdatePermission(bulkEditBean, issue) && isShown(issue)))
            {
                return "bulk.edit.unavailable.permission";
            }

        }

        // This field is available for bulk-editing, return null (i.e no unavailble message)
        return null;
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        // Get all options for the config
        Collection<Version>versions = versionManager.getVersions(fieldTypeInfoContext.getIssueContext().getProjectObject().getId());

        return new FieldTypeInfo(versions, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.systemArray(JsonType.VERSION_TYPE, getId());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        Collection<Version> versions = getCurrentVersions(issue);
        Collection<VersionJsonBean> beans = VersionJsonBean.shortBeans(versions, jiraBaseUrls);
        return new FieldJsonRepresentation(new JsonData(beans));
    }
}
