package com.atlassian.jira.auditing.handlers;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.auditing.AssociatedItem;
import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.event.fields.layout.AbstractFieldLayoutSchemeEntityEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeEntityCreatedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeEntityRemovedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeEntityUpdatedEvent;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.util.I18nHelper;

/**
 *
 * @since v6.2
 */
public class FieldLayoutSchemeChangeHandlerImpl implements FieldLayoutSchemeChangeHandler
{
    private final IssueTypeManager issueTypeManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final I18nHelper.BeanFactory i18n;

    public FieldLayoutSchemeChangeHandlerImpl(IssueTypeManager issueTypeManager, FieldLayoutManager fieldLayoutManager,
            final I18nHelper.BeanFactory i18n)
    {
        this.issueTypeManager = issueTypeManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.i18n = i18n;
    }

    @Override
    public RecordRequest onFieldLayoutSchemeEntityEvent(final AbstractFieldLayoutSchemeEntityEvent event)
    {
        return new RecordRequest(AuditingCategory.FIELDS, "jira.auditing.field.layout.scheme.updated")
                .forObject(AssociatedItem.Type.SCHEME, event.getScheme().getName(), event.getScheme().getId())
                .withChangedValues(computeChangedValues(event));
    }

    private List<ChangedValue> computeChangedValues(final AbstractFieldLayoutSchemeEntityEvent event)
    {
        final String issueTypeName = getIssueTypeName(event.getEntityDetails().getIssueTypeId());
        final String fieldConfigurationName = getFieldLayoutName(event.getEntityDetails().getFieldLayoutId());

        final ChangedValuesBuilder builder = new ChangedValuesBuilder();

        if (event instanceof FieldLayoutSchemeEntityCreatedEvent)
        {
            // set to
            builder.addIfDifferent("admin.issue.constant.issuetype", "", issueTypeName)
                    .addIfDifferent("admin.issuefields.fieldconfigschemes.field.configuration", "", fieldConfigurationName);
        }
        else if (event instanceof FieldLayoutSchemeEntityRemovedEvent)
        {
            // this is PermissionDeletedEvent, so set from
            builder.add("admin.issue.constant.issuetype", issueTypeName, "")
                    .addIfDifferent("admin.issuefields.fieldconfigschemes.field.configuration", fieldConfigurationName, "");
        } else {
            throw new UnsupportedOperationException("Missing handler for " + event.getClass().getSimpleName());
        }

        return builder.build();
    }

    private String getIssueTypeName(final String issueTypeId)
    {
        return issueTypeId == null ?  getI18n().getText("common.words.default") : issueTypeManager.getIssueType(issueTypeId).getName();
    }

    @Nonnull
    private String getFieldLayoutName(@Nullable final Long fieldLayoutId)
    {
        final FieldLayout fieldLayout = fieldLayoutManager.getEditableFieldLayout(fieldLayoutId);
        return fieldLayout.getName();
    }

    @Override
    public RecordRequest onFieldLayoutSchemeEntityUpdatedEvent(final FieldLayoutSchemeEntityUpdatedEvent event)
    {
        final String issueTypeName = getIssueTypeName(event.getEntityDetails().getIssueTypeId());
        final String fieldConfigurationName = getFieldLayoutName(event.getEntityDetails().getFieldLayoutId());

        final String originalIssueTypeName = getIssueTypeName(event.getOriginalEntityDetails().getIssueTypeId());
        final String originalFieldConfigurationName = getFieldLayoutName(event.getOriginalEntityDetails().getFieldLayoutId());

        final ChangedValuesBuilder builder = new ChangedValuesBuilder();

        builder.add("admin.issue.constant.issuetype", originalIssueTypeName, issueTypeName)
                .addIfDifferent("admin.issuefields.fieldconfigschemes.field.configuration", originalFieldConfigurationName, fieldConfigurationName);

        return new RecordRequest(AuditingCategory.FIELDS, "jira.auditing.field.layout.scheme.updated")
                .forObject(AssociatedItem.Type.SCHEME, event.getScheme().getName(), event.getScheme().getId())
                .withChangedValues(builder.build());
    }

    protected I18nHelper getI18n()
    {
        // You must not cache I18nHelper
        return i18n.getInstance(Locale.ENGLISH);
    }
}
