package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.customfield.UserCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.ApplicationUserBestNameComparator;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.converters.MultiUserConverter;
import com.atlassian.jira.issue.customfields.impl.rest.MultiUserCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.UserField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareCustomFieldType;
import com.atlassian.jira.issue.fields.rest.RestCustomFieldTypeOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.notification.type.UserCFNotificationTypeAware;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import javax.annotation.Nonnull;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * <p>Multiple User Type allows selection of multiple users.  For single User select use {@link UserCFType}</p>
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link java.util.Collection}</dd>
 * <dt><strong>Singular Object Type</strong></dt>
 * <dd>{@link ApplicationUser}</dd>
 * <dt><Strong>Database Storage Type</Strong></dt>
 * <dd>{@link String} of user name</dd>
 * </dl>
 */
public class MultiUserCFType extends AbstractMultiCFType<ApplicationUser> implements UserCFNotificationTypeAware, ProjectImportableCustomField, UserField, SortableCustomField<String>, RestAwareCustomFieldType, RestCustomFieldTypeOperations
{
    protected final MultiUserConverter multiUserConverter;
    private ApplicationProperties applicationProperties;
    private JiraAuthenticationContext authenticationContext;
    private UserPickerSearchService searchService;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final UserCustomFieldImporter userCustomFieldImporter;
    private final JiraBaseUrls jiraBaseUrls;
    private final EmailFormatter emailFormatter;

    public MultiUserCFType(CustomFieldValuePersister customFieldValuePersister,
            GenericConfigManager genericConfigManager, MultiUserConverter multiUserConverter, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, UserPickerSearchService searchService,
            final FieldVisibilityManager fieldVisibilityManager, JiraBaseUrls jiraBaseUrls, final EmailFormatter emailFormatter)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.multiUserConverter = multiUserConverter;
        this.applicationProperties = applicationProperties;
        this.authenticationContext = authenticationContext;
        this.searchService = searchService;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.emailFormatter = emailFormatter;
        this.userCustomFieldImporter = new UserCustomFieldImporter();
    }

    protected Comparator<ApplicationUser> getTypeComparator()
    {
        return new ApplicationUserBestNameComparator(authenticationContext.getLocale());
    }

    @Override
    public void updateValue(CustomField customField, Issue issue, Collection<ApplicationUser> value)
    {
        super.updateValue(customField, issue, value);
    }

    @Override
    public String getChangelogValue(final CustomField field, final Collection<ApplicationUser> values)
    {
        List<String> applicationUserKeys = new ArrayList<String>();
        if (values == null)
        {
            return "";
        }
        for (ApplicationUser applicationUser : values)
        {
            applicationUserKeys.add(applicationUser.getKey());
        }
        return applicationUserKeys.toString();
    }

    @Override
    public String getChangelogString(final CustomField field, final Collection<ApplicationUser> value)
    {
        final String glue = ", ";
        if (value == null)
        {
           return "";
        }
        StringBuilder changelogStringEntry = new StringBuilder();
        Iterator<ApplicationUser> usersIterator = value.iterator();
        while (usersIterator.hasNext())
        {
            final ApplicationUser nextUser = usersIterator.next();
            if (nextUser == null) {
                continue;
            }
            changelogStringEntry.append(nextUser.getDisplayName());
            if (usersIterator.hasNext())
            {
                changelogStringEntry.append(glue);
            }
        }
        return changelogStringEntry.toString();
    }

    public Collection<ApplicationUser> getValueFromCustomFieldParams(CustomFieldParams parameters) throws FieldValidationException
    {
        Collection<ApplicationUser> userSet = new HashSet<ApplicationUser>();
        Collection values = parameters.getValuesForNullKey();
        if (values == null || values.isEmpty())
        {
            return null;
        }
        for (Object value : values)
        {
            userSet.addAll(convertPresentationStringsToUsers(new ArrayList<String>(multiUserConverter.extractUserStringsFromString((String) value))));
        }
        List<ApplicationUser> l = new ArrayList<ApplicationUser>(userSet);
        Collections.sort(l, new ApplicationUserBestNameComparator(authenticationContext.getLocale()));
        return l;
    }

    private Collection<ApplicationUser> convertPresentationStringsToUsers(final List<String> presentationStrings)
    {
        if (presentationStrings == null)
        {
            return null;
        }

        final Set<ApplicationUser> result = new HashSet<ApplicationUser>();
        for (final String presentationString : presentationStrings)
        {
            try
            {
                final ApplicationUser user = multiUserConverter.getUserFromHttpParameterWithValidation(presentationString);
                if (user != null)
                {
                    result.add(user);
                }
            }
            catch (final FieldValidationException ignore)
            {}
        }

        return result;
    }

    public Object getStringValueFromCustomFieldParams(CustomFieldParams parameters)
    {
        Collection users = parameters.getValuesForNullKey();
        if (users == null || users.isEmpty())
        {
            return null;
        }

        return putInvalidUsersAtFront(users);
    }

    public void validateFromParams(CustomFieldParams relevantParams, ErrorCollection errorCollectionToAddTo, FieldConfig config)
    {
        StringBuilder errors = null;
        Collection<String> userStrings;
        String singleParam;
        for (final Object o : relevantParams.getValuesForNullKey())
        {
            singleParam = (String) o;

            userStrings = multiUserConverter.extractUserStringsFromString(singleParam);

            if (userStrings == null)
            {
                return;
            }
            for (final String user : userStrings)
            {
                try
                {
                    multiUserConverter.getUserFromHttpParameterWithValidation(user);
                }
                catch (FieldValidationException e)
                {
                    if (errors == null)
                    {
                        errors = new StringBuilder(user);
                    }
                    else
                    {
                        errors.append(", ").append(user);
                    }
                }
                if (errors != null)
                {
                    errorCollectionToAddTo.addError(config.getCustomField().getId(), getI18nBean().getText("admin.errors.could.not.find.usernames", errors), Reason.VALIDATION_FAILED);
                }
            }
        }
    }

    @Override
    public String getStringFromSingularObject(ApplicationUser o)
    {
        return multiUserConverter.getHttpParameterValue(o);
    }

    @Override
    public ApplicationUser getSingularObjectFromString(String s) throws FieldValidationException
    {
        return multiUserConverter.getUserFromHttpParameterWithValidation(s);
    }

    @Nonnull
    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_LIMITED_TEXT;
    }

    public List getRelatedIndexers(final CustomField customField)
    {
        return EasyList.build(new MultiUserCustomFieldIndexer(fieldVisibilityManager, customField, multiUserConverter));
    }

    @Override
    protected Object convertTypeToDbValue(ApplicationUser value)
    {
        return multiUserConverter.getDbString(value);
    }

    @Override
    protected ApplicationUser convertDbValueToType(Object string)
    {
        return multiUserConverter.getUserFromDbString((String) string);
    }

    private Collection putInvalidUsersAtFront(Collection users)
    {
        final HashSet<String> retSet = new HashSet<String>();
        StringBuilder errorString = null;
        String userString;
        if (users != null)
        {
            for (final Object user : users)
            {
                String userList = (String) user;
                for (final String s : multiUserConverter.extractUserStringsFromString(userList))
                {
                    userString = s;
                    try
                    {
                        multiUserConverter.getUserFromHttpParameterWithValidation(userString);
                        retSet.add(userString);
                    }
                    catch (FieldValidationException e)
                    {
                        if (errorString == null)
                        {
                            errorString = new StringBuilder(userString);
                        }
                        else
                        {
                            errorString.append(", ").append(userString);
                        }
                    }
                }
            }
        }

        List<String> l = new ArrayList<String>(retSet);
        Collections.sort(l); //, NAME_COMPARATOR);
        if (errorString != null)
        {
            l.add(0, errorString.toString());
        }

        return l;
    }

    @Nonnull
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem)
    {
        Map<String, Object> velocityParams = super.getVelocityParameters(issue, field, fieldLayoutItem);

        JiraServiceContext ctx = new JiraServiceContextImpl(authenticationContext.getUser());

        boolean canPerformAjaxSearch = searchService.canPerformAjaxSearch(ctx);
        if (canPerformAjaxSearch)
        {
            velocityParams.put("canPerformAjaxSearch", "true");
            velocityParams.put("ajaxLimit", applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
        }
        WebResourceManager webResourceManager = ComponentAccessor.getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:autocomplete");
        return velocityParams;
    }

    //------------------------------------------------------------------------------------------------------------------
    //  Implementation of ProjectImportableCustomField
    //------------------------------------------------------------------------------------------------------------------
    public ProjectCustomFieldImporter getProjectImporter()
    {
        return this.userCustomFieldImporter;
    }
    //------------------------------------------------------------------------------------------------------------------

    static class MultiUserCustomFieldIndexer extends AbstractCustomFieldIndexer
    {
        private final CustomField customField;
        private final MultiUserConverter multiUserConverter;

        public MultiUserCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField, final MultiUserConverter multiUserConverter)
        {
            super(fieldVisibilityManager, customField);
            this.customField = customField;
            this.multiUserConverter = multiUserConverter;
        }

        public void addDocumentFieldsSearchable(final Document doc, final Issue issue)
        {
            addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED_NO_NORMS);
        }

        public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue)
        {
            addDocumentFields(doc, issue, Field.Index.NO);
        }

        public void addDocumentFields(Document doc, Issue issue, final Field.Index indexType)
        {
            List o = (List) customField.getValue(issue);
            if (o != null)
            {
                for (final Object anO : o)
                {
                    ApplicationUser user = (ApplicationUser) anO;
                    String userId = multiUserConverter.getDbString(user);
                    doc.add(new Field(getDocumentFieldId(), userId, Field.Store.YES, indexType));
                }
            }
        }
    }

    public int compare(@Nonnull final String customFieldObjectValue1, @Nonnull final String customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        return customFieldObjectValue1.compareTo(customFieldObjectValue2);
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitMultiUser(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitMultiUser(MultiUserCFType multiUserCustomFieldType);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        final String userPickerAutoCompleteUrl = String.format("%s/rest/api/1.0/users/picker?fieldName=%s&query=", jiraBaseUrls.baseUrl(), fieldTypeInfoContext.getOderableField().getId());
        return new FieldTypeInfo(null, userPickerAutoCompleteUrl);
    }

    @Override
    public JsonType getJsonSchema(CustomField customField)
    {
        return JsonTypeBuilder.customArray(JsonType.USER_TYPE, getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        return new FieldJsonRepresentation(new JsonData(UserJsonBean.shortBeanCollection(getValueFromIssue(field, issue), jiraBaseUrls, authenticationContext.getUser(), emailFormatter)));
    }
    @Override
    public RestFieldOperationsHandler getRestFieldOperation(CustomField field)
    {
        return new MultiUserCustomFieldOperationsHandler(field, getI18nBean());
    }

    public JsonData getJsonDefaultValue(IssueContext issueCtx, CustomField field)
    {
        FieldConfig config = field.getRelevantConfig(issueCtx);
        return new JsonData(UserJsonBean.shortBeanCollection(getDefaultValue(config), jiraBaseUrls, authenticationContext.getUser(), emailFormatter));
    }
}
