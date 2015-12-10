package com.atlassian.jira.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;
import com.atlassian.plugin.web.api.provider.WebItemProvider;

/**
 * A SimpleLinkFactory that produces links to Browse Project for recently viewed project except for the current Project
 *
 * @since v4.0
 */
public class ProjectHistoryLinkFactory implements WebItemProvider
{
    public static final int MAX_RECENT_PROJECTS_TO_SHOW = 5;

    private final UserProjectHistoryManager userHistoryManager;
    private final I18nBean.BeanFactory beanFactory;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public ProjectHistoryLinkFactory(VelocityRequestContextFactory velocityRequestContextFactory, UserProjectHistoryManager userHistoryManager,
            I18nBean.BeanFactory beanFactory)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.userHistoryManager = userHistoryManager;

        this.beanFactory = beanFactory;
    }

    @Override
    public Iterable<WebItem> getItems(final Map<String, Object> context)
    {
        final ProjectAction projectAction = ProjectAction.VIEW_ISSUES;
        final User user = (User) context.get("user");

        final List<Project> history = userHistoryManager.getProjectHistoryWithPermissionChecks(projectAction, user);
        final List<WebItem> links = new ArrayList<WebItem>();

        if (!history.isEmpty())
        {
            final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
            // Need ot ensure they contain the baseurl in case they are loaded via ajax/rest
            final String baseUrl = requestContext.getBaseUrl();

            final Project currentProject = userHistoryManager.getCurrentProject(Permissions.BROWSE, user);
            final I18nHelper i18n = beanFactory.getInstance(user);


            int weight = 10;
            for (Project project : history)
            {
                if (!project.equals(currentProject))
                {
                    final Long projectId = project.getId();
                    final String name = project.getName();
                    final String key = project.getKey();

                    String iconUrl = null;
                    if (project.getGenericValue().getLong("avatar") != null)
                    {
                        final Avatar avatar = project.getAvatar();
                        iconUrl = baseUrl + "/secure/projectavatar?pid=" + projectId + "&avatarId=" + avatar.getId() + "&size=small";
                    }
                    links.add(new WebFragmentBuilder(weight += 10).
                            id("proj_lnk_" + projectId).
                            label(name + " (" + key + ")").
                            title(i18n.getText("tooltip.browseproject.specified", name)).
                            addParam("iconUrl", iconUrl).
                            webItem("browse_link/project_history_main").
                            url(baseUrl + "/browse/" + key).
                            build());
                }
            }
        }

        return links.subList(0, Math.min(MAX_RECENT_PROJECTS_TO_SHOW, links.size()));
    }
}
