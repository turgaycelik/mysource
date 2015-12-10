package com.atlassian.jira.rest.v2.issue.project;

import java.util.regex.Pattern;

import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.user.ApplicationUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @since v6.1
 */
@Component
public class ProjectFinder
{
    private static final Pattern PROJECT_ID_PATTERN = Pattern.compile("^[1-9]\\d{0,17}$");

    private final ProjectService projectService;

    @Autowired
    public ProjectFinder(final ProjectService projectService)
    {
        this.projectService = projectService;
    }

    public ProjectService.GetProjectResult getGetProjectForActionByIdOrKey(final ApplicationUser user, final String projectIdOrKey, final ProjectAction action)
    {
        return PROJECT_ID_PATTERN.matcher(projectIdOrKey).matches()
                ? projectService.getProjectByIdForAction(user, Long.parseLong(projectIdOrKey), action)
                : projectService.getProjectByKeyForAction(user, projectIdOrKey, action);
    }
}
