package com.atlassian.jira.issue.customfields.impl.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.rest.AbstractVersionsRestFieldOperationsHandler;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;

/**
 * @since v5.0
 */
public class MultiVersionCustomFieldOperationsHandler extends AbstractVersionsRestFieldOperationsHandler
{
    private final CustomField field;

    public MultiVersionCustomFieldOperationsHandler(CustomField field, VersionManager versionManager, I18nHelper i18nHelper)
    {
       super(versionManager, i18nHelper);
        this.field = field;
    }

    @Override
    protected String getFieldName()
    {
        return field.getId();
    }

    @Override
    protected Collection<String> getInitialValue(Issue issue, ErrorCollection errors)
    {
        Object fieldValue = field.getValue(issue);
        if (fieldValue == null)
        {
            return Collections.emptyList();
        }
        Iterable<String> versions = Iterables.transform((Collection<Version>) fieldValue, new Function<Version, String>()
        {
            @Override
            public String apply(Version from)
            {
                return from.getId().toString();
            }
        });
        return Lists.newArrayList(versions);
    }

    @Override
    protected void finaliseOperation(Collection<String> finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.addCustomFieldValue(field.getId(), finalValue.toArray(new String[finalValue.size()]));
    }
}
