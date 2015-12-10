package com.atlassian.jira.issue.fields;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.event.AffectedVersionCreatedInlineEvent;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.AffectedVersionsRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.util.VersionHelperBean;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.AffectedVersionSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.RaisedInVersionStatisticsMapper;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class AffectedVersionsSystemField extends AbstractVersionsSystemField implements AffectedVersionsField, RestFieldOperations
{
    private static final String AFFECTED_VERSIONS_NAME_KEY = "issue.field.affectsversions";
    private final RaisedInVersionStatisticsMapper raisedInVersionStatsMapper;
    private final EventPublisher eventPublisher;

    public AffectedVersionsSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties,
            VersionManager versionManager, PermissionManager permissionManager,
            JiraAuthenticationContext authenticationContext,
            RaisedInVersionStatisticsMapper raisedInVersionStatsMapper,
            VersionHelperBean versionHelperBean,
            AffectedVersionSearchHandlerFactory searchHandlerFactory,
            JiraBaseUrls jiraBaseUrls,
            EventPublisher eventPublisher)
    {
        super(IssueFieldConstants.AFFECTED_VERSIONS, AFFECTED_VERSIONS_NAME_KEY, templatingEngine, applicationProperties, versionManager, permissionManager, authenticationContext, versionHelperBean, searchHandlerFactory, jiraBaseUrls);
        this.raisedInVersionStatsMapper = raisedInVersionStatsMapper;
        this.eventPublisher = eventPublisher;
    }

    public boolean isShown(Issue issue)
    {
        // Affected Versions field is not protected by any permission
        return true;
    }

    protected Collection<Version> getCurrentVersions(Issue issue)
    {
        return issue.getAffectedVersions();
    }

    protected String getArchivedVersionsFieldTitle()
    {
        return "issue.field.archived.affectsversions";
    }

    protected String getArchivedVersionsFieldSearchParam()
    {
        return "version";
    }

    protected boolean getUnreleasedVersionsFirst()
    {
        return false;
    }

    protected void addFieldRequiredErrorMessage(Issue issue, ErrorCollection errorCollection, I18nHelper i18n)
    {
        if (getPossibleVersions(issue.getProjectObject(), false).isEmpty())
        {
            errorCollection.addErrorMessage(i18n.getText("createissue.error.versions.required", i18n.getText(getNameKey()), issue.getProjectObject().getName()), Reason.VALIDATION_FAILED);
        }
        else
        {
            errorCollection.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())), Reason.VALIDATION_FAILED);
        }
    }

    public String getColumnHeadingKey()
    {
        return "issue.column.heading.affectsversions";
    }

    public LuceneFieldSorter getSorter()
    {
        return raisedInVersionStatsMapper;
    }

    protected String getIssueRelationName()
    {
        return IssueRelationConstants.VERSION;
    }

    protected String getChangeItemFieldName()
    {
        return "Version";
    }

    protected String getModifiedWithoutPermissionErrorMessage(I18nHelper i18n)
    {
        throw new UnsupportedOperationException("Affected Versions field is not protected by permission.");
    }

    public Object getDefaultValue(Issue issue)
    {
        return Collections.EMPTY_LIST;
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        final LongIdsValueHolder versions = LongIdsValueHolder.fromFieldValuesHolder(getId(), fieldValueHolder);
        if (versions != null)
        {
            final List<Version> affectedVersions = versionHelperBean.createNewVersions(issue.getProjectId(), versions.getValuesToAdd());
            for (final Version affectedVersion : affectedVersions)
            {
                eventPublisher.publish(new AffectedVersionCreatedInlineEvent(affectedVersion));
            }
            affectedVersions.addAll(getValueFromParams(fieldValueHolder));

            if(!versions.getValuesToAdd().isEmpty())
            {
                final Iterable<Long> ids = transform(affectedVersions, new com.google.common.base.Function<Version, Long>()
                {
                    @Override
                    public Long apply(final Version input)
                    {
                        return input.getId();
                    }
                });
                fieldValueHolder.put(getId(), new LongIdsValueHolder(newArrayList(ids)));
            }
            issue.setAffectedVersions(affectedVersions);
        }
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setAffectedVersions(Collections.EMPTY_LIST);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        return new AffectedVersionsRestFieldOperationsHandler(versionManager, authenticationContext.getI18nHelper());
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        return null;
    }
}
