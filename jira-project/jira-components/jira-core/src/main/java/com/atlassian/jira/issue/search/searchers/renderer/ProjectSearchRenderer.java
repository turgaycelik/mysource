package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.ofbiz.OfBizStringFieldComparator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.query.Query;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A search renderer for the project system field searcher.
 *
 * @since v4.0
 */
public class ProjectSearchRenderer extends AbstractSearchRenderer implements SearchRenderer
{
    // if the number of projects a user can see is <= this amount, we don't show recently used
    public static final int MAX_PROJECTS_BEFORE_RECENT = 10;

    // max number of recently used projects to show
    public static final int MAX_RECENT_PROJECTS_TO_SHOW = 5;

    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final UserProjectHistoryManager projectHistoryManager;

    public ProjectSearchRenderer(ProjectManager projectManager, PermissionManager permissionManager,
            VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties,
            VelocityTemplatingEngine templatingEngine, String searcherNameKey, UserProjectHistoryManager projectHistoryManager)
    {
        super(velocityRequestContextFactory, applicationProperties, templatingEngine, SystemSearchConstants.forProject(), searcherNameKey);
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.projectHistoryManager = projectHistoryManager;
    }

    public String getEditHtml(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        Map<String, Object> velocityParams = getVelocityParams(user, searchContext, null, fieldValuesHolder, displayParameters, action);
        addParameters(user, fieldValuesHolder, false, velocityParams);
        return renderEditTemplate("project-searcher" + EDIT_TEMPLATE_SUFFIX, velocityParams);
    }

    public void addParameters(final User searcher, final FieldValuesHolder fieldValuesHolder, boolean noCurrentSearchRequest, Map<String, Object> velocityParams)
    {
        Collection<GenericValue> allProjects = getVisibleProjects(searcher);
        velocityParams.put("visibleProjects", allProjects);
        //if there is no search request in session and no project has been specified in the params, add the single
        // visible project to the list
        if (noCurrentSearchRequest &&
                allProjects.size() == 1 &&
                !fieldValuesHolder.containsKey(SystemSearchConstants.forProject().getUrlParameter()))
        {
            String singlePid = allProjects.iterator().next().getString("id");
            velocityParams.put("selectedProjects", Collections.singleton(singlePid));
        }
        else
        {
            List projects = (List) fieldValuesHolder.get(SystemSearchConstants.forProject().getUrlParameter());
            if (projects != null && projects.size() == 1 && projects.get(0).equals("-1"))
            {
                velocityParams.put("selectedProjects", Collections.EMPTY_LIST);
            }
            else
            {
                velocityParams.put("selectedProjects", projects != null ? projects : Collections.EMPTY_LIST);
            }
        }
        if (allProjects.size() > MAX_PROJECTS_BEFORE_RECENT)
        {
            velocityParams.put("recentProjects", getRecentProjects(searcher));
        }
    }

    public boolean isShown(final User user, final SearchContext searchContext)
    {
        return true;
    }

    public String getViewHtml(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action)
    {
        Map<String, Object> velocityParams = getVelocityParams(user, searchContext, null, fieldValuesHolder, displayParameters, action);

        final Collection projectIds = ParameterUtils.makeListLong((List) fieldValuesHolder.get(SystemSearchConstants.forProject().getUrlParameter()));
        final List<GenericValue> projects = projectManager.convertToProjects(projectIds);
        if (projects != null)
        {
            // Only need to create 'filteredOutProjects' if projects is not null.
            // This is used in context-searcher-view.vm template.
            final List<GenericValue> filteredOutProjects = new ArrayList<GenericValue>();
            for (Iterator iterator = projects.iterator(); iterator.hasNext();)
            {
                GenericValue project = (GenericValue) iterator.next();
                if (!permissionManager.hasPermission(Permissions.BROWSE, project, user))
                {
                    filteredOutProjects.add(project);
                    iterator.remove();
                }
            }
            // only ID of these projects is displayed, let's sort them by ID
            Collections.sort(filteredOutProjects, new OfBizStringFieldComparator("id"));
            velocityParams.put("filteredOutProjects", filteredOutProjects);
        }
        velocityParams.put("selectedProjects", projects);

        return renderViewTemplate("project-searcher" + VIEW_TEMPLATE_SUFFIX, velocityParams);
    }

    public boolean isRelevantForQuery(final User user, final Query query)
    {
        return isRelevantForQuery(SystemSearchConstants.forProject().getJqlClauseNames(), query);
    }

    public Collection<Project> getRecentProjects(final User searcher)
    {
        List<Project> projects = projectHistoryManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, searcher);
        return null == projects ? null : projects.subList(0, Math.min(MAX_RECENT_PROJECTS_TO_SHOW, projects.size()));
    }

    public Collection<GenericValue> getVisibleProjects(final User searcher)
    {
        return permissionManager.getProjects(Permissions.BROWSE, searcher);
    }
}
