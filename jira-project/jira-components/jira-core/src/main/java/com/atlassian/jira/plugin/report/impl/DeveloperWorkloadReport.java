/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 2, 2004
 * Time: 8:18:19 PM
 */
package com.atlassian.jira.plugin.report.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.report.ReportSubTaskFetcher;
import com.atlassian.jira.plugin.report.SubTaskInclusionOption;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;

import com.opensymphony.util.TextUtils;

import org.apache.log4j.Logger;

import static com.atlassian.jira.plugin.report.SubTaskInclusionOption.ONLY_ASSIGNED;
import static com.atlassian.jira.plugin.report.SubTaskInclusionOption.fromKey;

public class DeveloperWorkloadReport extends AbstractReport
{
    private static final Logger LOGGER = Logger.getLogger(DeveloperWorkloadReport.class);

    private final ProjectManager projectManager;
    private final ApplicationProperties applicationProperties;
    private final UserManager userManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final SearchProvider searchProvider;
    private final ReportSubTaskFetcher reportSubTaskFetcher;

    public DeveloperWorkloadReport(ProjectManager projectManager, ApplicationProperties applicationProperties,
            UserManager userManager, JiraDurationUtils jiraDurationUtils, SearchProvider searchProvider,
            ReportSubTaskFetcher reportSubTaskFetcher)
    {
        this.projectManager = projectManager;
        this.applicationProperties = applicationProperties;
        this.jiraDurationUtils = jiraDurationUtils;
        this.userManager = userManager;
        this.searchProvider = searchProvider;
        this.reportSubTaskFetcher = reportSubTaskFetcher;
    }

    public boolean showReport()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
    }

    public void validate(ProjectActionSupport action, Map params)
    {
        super.validate(action, params);
        final String developer = (String) params.get("developer");
        final boolean canPickUsers = action.hasPermission(Permissions.USER_PICKER);

        if (!TextUtils.stringSet(developer))
        {
            action.addError("developer", action.getText("report.developerworkload.developer.is.required"));
        }
        else if (!canPickUsers)
        {
            action.addError("developer", action.getText("report.developerworkload.developer.does.not.exist"));
        }
        else if (userManager.getUserByName(developer) == null)
        {
            action.addError("developer", action.getText("report.developerworkload.developer.does.not.exist"));
        }
    }

    public String generateReportHtml(ProjectActionSupport action, Map reqParams) throws Exception
    {
        final ApplicationUser remoteUser = action.getLoggedInApplicationUser();
        final ApplicationUser developer = userManager.getUserByName((String) reqParams.get("developer"));

        String subtaskInclusionKey = (String) reqParams.get("subtaskInclusion");
        SubTaskInclusionOption subtaskInclusion;
        if (subtaskInclusionKey == null)
        {
            // default to behaviour as before this option was added to support legacy URLs
            subtaskInclusion = ONLY_ASSIGNED;
        }
        else
        {
            subtaskInclusion = fromKey(subtaskInclusionKey);
        }

        List<Issue> result = new ArrayList<Issue>();
        {
            List<Issue> assignedIssues = initAssignedIssues(remoteUser, developer);
            result.addAll(assignedIssues);
        }
        List<Issue> subTasks = reportSubTaskFetcher.getSubTasksForUser(remoteUser, result, subtaskInclusion, true);
        result.addAll(subTasks);

        Map<Long,MutableLong> countMap = initCountMap(result);
        Map<Long,MutableLong> workloadMap = initWorkloadMap(result);

        Map<String,Object> velocityParams = new HashMap<String,Object>();
        velocityParams.put("report", this);
        velocityParams.put("action", action);
        velocityParams.put("developer", developer);
        velocityParams.put("assignedIssues", result);
        velocityParams.put("countMap", countMap);
        velocityParams.put("workloadMap", workloadMap);
        velocityParams.put("totalCount", getTotalIssuesCount(countMap));
        velocityParams.put("totalWorkload", getTotalWorkload(workloadMap));
        return descriptor.getHtml("view", velocityParams);
    }

    /**
     * this formatting function is shared by the full view for navigator as well as view issue.
     *
     * @param v duration in seconds
     *
     * @return formatted duration
     */
    public String formatPrettyDuration(Number v)
    {
        return jiraDurationUtils.getFormattedDuration(v.longValue(), descriptor.getI18nBean().getLocale());
    }

    /**
     * Retrieves the list of the issues that are currently assigned to the specified developer issue must be unresolved.
     * Never returns null.
     *
     * @param remoteUser current user
     * @param developer  user to find the assigned issues for
     *
     * @return list of issues
     */
    List<Issue> initAssignedIssues(ApplicationUser remoteUser, ApplicationUser developer)
    {
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        builder.where().assigneeUser(developer.getUsername()).and().unresolved();
        try
        {
            SearchResults searchResults = searchProvider.search(builder.buildQuery(), remoteUser, PagerFilter.getUnlimitedFilter());
            return searchResults.getIssues();
        }
        catch (SearchException e)
        {
            LOGGER.error("Error executing Search Request in DeveloperWorkloadReport (remoteUser=" + remoteUser + ", developer=" + developer + "): " + e, e);
        }
        return Collections.emptyList();
    }

    Map<Long,MutableLong> initCountMap(List<Issue> assignedIssues)
    {
        Map<Long,MutableLong> countMap = new HashMap<Long,MutableLong>();

        for (final Issue assignedIssue : assignedIssues)
        {
            Long estimate = assignedIssue.getEstimate();

            if (estimate == null)
            {
                continue; // drop issues that have not been assigned an estimate
            }

            //TODO do we have to convert to a string here?
            Long projectId = assignedIssue.getProjectObject().getId();
            MutableLong counter = countMap.get(projectId);
            if (counter != null)
            {
                counter.incrementAndGet();
            }
            else
            {
                countMap.put(projectId, new MutableLong(1L));
            }
        }

        return countMap;
    }

    Map<Long,MutableLong> initWorkloadMap(List<Issue> assignedIssues)
    {
        Map<Long,MutableLong> workloadMap = new HashMap<Long,MutableLong>();

        for (final Issue assignedIssue : assignedIssues)
        {
            Long projectId = assignedIssue.getProjectObject().getId();
            Long estimate = assignedIssue.getEstimate();

            if (estimate == null)
            {
                continue; // drop issues that have not been assigned an estimate
            }

            MutableLong total = workloadMap.get(projectId);
            if (total != null)
            {
                total.add(estimate);
            }
            else
            {
                workloadMap.put(projectId, new MutableLong(estimate));
            }
        }
        return workloadMap;
    }

    public Long getTotalIssuesCount(Map<Long,MutableLong> countMap)
    {
        long total = 0;
        for (MutableLong projectIssueCount : countMap.values())
        {
            total += projectIssueCount.longValue();
        }
        return total;
    }

    public Long getTotalWorkload(Map<Long,MutableLong> workloadMap)
    {
        long totalWorkload = 0;
        for (MutableLong projectWorkload : workloadMap.values())
        {
            totalWorkload += projectWorkload.longValue();
        }
        return totalWorkload;
    }

    public String getProjectName(Long projectId)
    {
        return projectManager.getProjectObj(projectId).getName();
    }
}
