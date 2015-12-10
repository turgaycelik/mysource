package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.project.Project;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.opensymphony.util.TextUtils.htmlEncode;

/**
 * Adds search context parameters to velocityParams. Used by searchContextDescriptionTitle in macros.vm
 * @since v5.2
 */
public class SearchContextRenderHelper
{
    public static void addSearchContextParams(SearchContext searchContext, Map<String, Object> velocityParams)
    {
        List<String> projectNames;
        if (null != searchContext.getProjects())
        {
            projectNames = Lists.transform(searchContext.getProjects(), new Function<Project, String>()
            {
                @Override
                public String apply(@Nullable Project project)
                {
                    return htmlEncode(project.getName());
                }
            });
        }
        else
        {
            projectNames = Collections.emptyList();
        }
        velocityParams.put("contextProjectNames", StringUtils.join(projectNames, ", "));

        List<String> issueTypeNames;
        if (null != searchContext.getIssueTypes())
        {
            issueTypeNames = Lists.transform(searchContext.getIssueTypes(), new Function<IssueType, String>()
            {
                @Override
                public String apply(@Nullable IssueType issueType)
                {
                    return htmlEncode(issueType.getName());
                }
            });
        }
        else
        {
            issueTypeNames = Collections.emptyList();
        }
        velocityParams.put("contextIssueTypeNames", StringUtils.join(issueTypeNames, ", "));
    }
}
