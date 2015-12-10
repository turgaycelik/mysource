package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * @since v5.0
 */
public class FixForVersionsRestFieldOperationsHandler extends AbstractVersionsRestFieldOperationsHandler
{
    public FixForVersionsRestFieldOperationsHandler(VersionManager versionManager, I18nHelper i18nHelper)
    {
        super(versionManager, i18nHelper);
    }

    @Override
    protected String getFieldName()
    {
        return IssueFieldConstants.FIX_FOR_VERSIONS;
    }

    @Override
     protected List<String> getInitialValue(Issue issue, ErrorCollection errors)
    {
        Iterable<String> versions = Iterables.transform(issue.getFixVersions(), new Function<Version, String>()
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
        Long[] ids = toVersionIds(finalValue, errors);
        if (ids != null)
        {
            parameters.setFixVersionIds(ids);
        }
    }

}
