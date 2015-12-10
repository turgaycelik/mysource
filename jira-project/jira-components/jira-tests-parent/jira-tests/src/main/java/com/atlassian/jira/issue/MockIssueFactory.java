package com.atlassian.jira.issue;

import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.MockSubTaskManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import org.ofbiz.core.entity.GenericValue;

/**
 * Factory method for producing mock issues.
 *
 * @since v3.13
 */
public class MockIssueFactory
{
    private static IssueManager issueManager;
    private static ProjectManager projectManager;
    private static VersionManager versionManager;
    private static IssueSecurityLevelManager issueSecurityLevelManager;
    private static ConstantsManager constantsManager;
    private static SubTaskManager subTaskManager = new MockSubTaskManager();
    private static AttachmentManager attachmentManager;
    private static LabelManager labelManager;
    private static ProjectComponentManager projectComponentManager;
    private static UserManager userManager;
    private static JiraAuthenticationContext jiraAuthenticationContext;

    public static MutableIssue createIssue(long id)
    {
        return createIssue(new Long(id));
    }

    public static MutableIssue createIssue(Long id)
    {
        GenericValue gv = new MockGenericValue("Issue");
        gv.set("id", id);
        gv.set("key", "HSP-" + id);
        gv.set(IssueFieldConstants.ISSUE_NUMBER, id);
        return new IssueImpl(gv, issueManager, projectManager, versionManager, issueSecurityLevelManager,
                constantsManager, subTaskManager, attachmentManager, labelManager, projectComponentManager,
                userManager, jiraAuthenticationContext);
    }

    public static MutableIssue createIssue(long id, String key, long projectId)
    {
        GenericValue gv = new MockGenericValue("Issue");
        gv.set("id", id);
        gv.set("key", key);
        gv.set(IssueFieldConstants.ISSUE_NUMBER, id);
        gv.set(IssueFieldConstants.PROJECT, projectId);

        return new IssueImpl(gv, issueManager, projectManager, versionManager, issueSecurityLevelManager,
                constantsManager, subTaskManager, attachmentManager, labelManager, projectComponentManager,
                userManager, jiraAuthenticationContext);
    }

    public static IssueManager getIssueManager()
    {
        return issueManager;
    }

    public static void setIssueManager(IssueManager issueManager)
    {
        MockIssueFactory.issueManager = issueManager;
    }

    public static ProjectManager getProjectManager()
    {
        return projectManager;
    }

    public static void setProjectManager(ProjectManager projectManager)
    {
        MockIssueFactory.projectManager = projectManager;
    }

    public static VersionManager getVersionManager()
    {
        return versionManager;
    }

    public static void setVersionManager(VersionManager versionManager)
    {
        MockIssueFactory.versionManager = versionManager;
    }

    public static IssueSecurityLevelManager getIssueSecurityLevelManager()
    {
        return issueSecurityLevelManager;
    }

    public static void setIssueSecurityLevelManager(IssueSecurityLevelManager issueSecurityLevelManager)
    {
        MockIssueFactory.issueSecurityLevelManager = issueSecurityLevelManager;
    }

    public static ConstantsManager getConstantsManager()
    {
        return constantsManager;
    }

    public static void setConstantsManager(ConstantsManager constantsManager)
    {
        MockIssueFactory.constantsManager = constantsManager;
    }

    public static SubTaskManager getSubTaskManager()
    {
        return subTaskManager;
    }

    public static void setSubTaskManager(SubTaskManager subTaskManager)
    {
        MockIssueFactory.subTaskManager = subTaskManager;
    }

    public static AttachmentManager getAttachmentManager()
    {
        return attachmentManager;
    }

    public static void setAttachmentManager(AttachmentManager attachmentManager)
    {
        MockIssueFactory.attachmentManager = attachmentManager;
    }

    public static LabelManager getLabelManager()
    {
        return labelManager;
    }

    public static void setLabelManager(final LabelManager labelManager)
    {
        MockIssueFactory.labelManager = labelManager;
    }

    public static void setProjectComponentManager(final ProjectComponentManager projectComponentManager)
    {
        MockIssueFactory.projectComponentManager = projectComponentManager;
    }

    public static ProjectComponentManager getProjectComponentManager()
    {
        return projectComponentManager;
    }

    public static UserManager getUserManager()
    {
        return userManager;
    }

    public static void setUserManager(final UserManager userManager)
    {
        MockIssueFactory.userManager = userManager;
    }

    public static JiraAuthenticationContext getJiraAuthenticationContext()
    {
        return jiraAuthenticationContext;
    }

    public static void setJiraAuthenticationContext(final JiraAuthenticationContext jiraAuthenticationContext)
    {
        MockIssueFactory.jiraAuthenticationContext = jiraAuthenticationContext;
    }
}
