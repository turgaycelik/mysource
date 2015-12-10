package com.atlassian.jira.web.servlet;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Serves avatar images for projects.
 *
 * @since v4.0
 */
public class ViewProjectAvatarServlet extends AbstractAvatarServlet
{
    @Override
    protected Long validateInput(String projectId, Long avatarId, final HttpServletResponse response) throws IOException
    {
        if (StringUtils.isBlank(projectId))
        {
            // no project id - send default avatar for project
            return getAvatarManager().getDefaultAvatarId(Avatar.Type.PROJECT);
        }
        else
        {
            final Project project = getProjectManager().getProjectObj(Long.parseLong(projectId));
            if (project == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown project");
                return null;
            }
            if (!getAvatarManager().hasPermissionToView(getAuthenticationContext().getUser(), project))
            {
                // no permission to see any avatar for this project
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unknown project");
                return null;
            }

            if (avatarId == null)
            {
                return project.getAvatar().getId();
            }
        }
        return avatarId;
    }


    @Override
    protected String getOwnerIdParamName()
    {
        return "pid";
    }

    JiraAuthenticationContext getAuthenticationContext()
    {
        return ComponentAccessor.getJiraAuthenticationContext();
    }

    ProjectManager getProjectManager()
    {
        return ComponentAccessor.getProjectManager();
    }
}
