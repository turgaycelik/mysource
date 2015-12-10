package com.atlassian.jira.workflow.condition;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * A workflow condition that requires the user to be in a role.
 */
public class InProjectRoleCondition extends AbstractJiraCondition
{
    public static final String KEY_PROJECT_ROLE_ID = "jira.projectrole.id";

    private static final Logger log = Logger.getLogger(InProjectRoleCondition.class);

    /**
     * If the project role doesn't exist (i.e. has been deleted) , shouuld the
     * condition fail (true) or pass (false)?
     */
    private static final boolean CONDITION_RESULT_ON_MISSING_PROJECT_ROLE = false;

    public boolean passesCondition(Map transientVars, Map args, PropertySet propertySet) throws WorkflowException
    {
        String rawprojectRoleId = (String) args.get(KEY_PROJECT_ROLE_ID);
        Long projectRoleId = null;
        if (StringUtils.isBlank(rawprojectRoleId))
        {
            log.warn("InProjectRoleCondition not configured with a valid projectroleid");
            return false;
        }
        else
        {
            try
            {
                projectRoleId = new Long(Long.parseLong(rawprojectRoleId));
            }
            catch (NumberFormatException e)
            {
                log.warn("InProjectRoleCondition not configured with a valid projectroleid, the project role id: "+ projectRoleId + " can not be parsed");
                return false;
            }
        }

        ApplicationUser user = getCallerUser(transientVars, args);
        Issue issue = getIssue(transientVars);

        Project project = issue.getProjectObject();

        ProjectRoleManager projectRoleManager = ComponentAccessor.getComponentOfType(ProjectRoleManager.class);
        ProjectRole projectRole = projectRoleManager.getProjectRole(projectRoleId);
        if (projectRole == null)
        {
            log.warn("Workflow condition is configured to check user membership in project role that doesn't exist: id is "
                    + projectRoleId + " (workflow condition will fail for everyone!)");
            return CONDITION_RESULT_ON_MISSING_PROJECT_ROLE;
        }
        return projectRoleManager.isUserInProjectRole(user, projectRole, project);
    }
}
