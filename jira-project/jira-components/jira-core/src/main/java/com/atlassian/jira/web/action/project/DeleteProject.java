
package com.atlassian.jira.web.action.project;

import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.web.action.IssueActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

@WebSudoRequired
public class DeleteProject extends IssueActionSupport
{
    private boolean confirm;
    private Long pid;
    private final ProjectService projectService;
    private final ProjectFactory projectFactory;
    private GenericValue project;

    public DeleteProject(final IssueManager issueManager, final CustomFieldManager customFieldManager,
            final AttachmentManager attachmentManager, final ProjectManager projectManager,
            final PermissionManager permissionManager, final VersionManager versionManager,
            final ProjectService projectService, final ProjectFactory projectFactory,
            final UserIssueHistoryManager userHistoryManager, final TimeTrackingConfiguration timeTrackingConfiguration)
    {
        super(issueManager, customFieldManager, attachmentManager, projectManager, permissionManager, versionManager, userHistoryManager, timeTrackingConfiguration);
        this.projectService = projectService;
        this.projectFactory = projectFactory;
    }

    protected void doValidation()
    {
        final Project projectObject = getProjectObject();
        if(projectObject == null)
        {
            addErrorMessage(getText("admin.deleteproject.error.no.project", pid));
            return;
        }

        final ProjectService.DeleteProjectValidationResult result =
                projectService.validateDeleteProject(getLoggedInUser(), projectObject.getKey());
        if(!result.isValid())
        {
            addErrorCollection(result.getErrorCollection());
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (confirm)
        {
            final ProjectService.DeleteProjectValidationResult result =
                    projectService.validateDeleteProject(getLoggedInUser(), getProjectObject().getKey());
            final ProjectService.DeleteProjectResult projectResult =
                    projectService.deleteProject(getLoggedInUser(), result);

            if(!projectResult.isValid())
            {
                addErrorCollection(projectResult.getErrorCollection());
                return ERROR;
            }
        }

        return getResult();
    }

    public GenericValue getProject()
    {
        if (project == null)
        {
            project = getProjectManager().getProject(pid);
        }
        return project;
    }

    public Project getProjectObject()
    {
        return projectFactory.getProject(getProject());
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public Long getPid()
    {
        return pid;
    }

    public void setPid(Long pid)
    {
        this.pid = pid;
    }
}
