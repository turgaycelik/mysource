/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Apr 15, 2004
 * Time: 1:43:25 PM
 */
package com.atlassian.jira.workflow;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WorkflowActionsBean
{
    private static final Map<String, Long> screensForViews;

    private Map<String, String> availableViews;

    private FieldScreenManager fieldScreenManager;
    public static final long VIEW_COMMENTASSIGN_ID = 2;
    public static final long VIEW_RESOLVE_ID = 3;

    static
    {
        Map<String, Long> tmp = new HashMap<String, Long>();
        tmp.put(WorkflowTransitionUtil.VIEW_COMMENTASSIGN, VIEW_COMMENTASSIGN_ID);
        tmp.put(WorkflowTransitionUtil.VIEW_RESOLVE, VIEW_RESOLVE_ID);

        screensForViews = Collections.unmodifiableMap(tmp);
    }

    public Map<String, String> getAvailableViews()
    {
        if (availableViews == null)
        {
            availableViews = new LinkedHashMap<String, String>();
            availableViews.put("", "No view for transition");
            availableViews.put(WorkflowTransitionUtil.VIEW_COMMENTASSIGN, "Add comment and assign");
            availableViews.put(WorkflowTransitionUtil.VIEW_RESOLVE, "Add comment, assign and set resolution");
        }

        return availableViews;
    }

    public String getSelectedView()
    {
        return WorkflowTransitionUtil.VIEW_COMMENTASSIGN;
    }

    public FieldScreen getFieldScreenForView(ActionDescriptor actionDescriptor)
    {
        String view = actionDescriptor.getView();
        if (TextUtils.stringSet(view))
        {
            Long fieldScreenId;
            if (screensForViews.containsKey(view))
            {
                fieldScreenId = screensForViews.get(view);
            }
            else
            {
                if (actionDescriptor.getMetaAttributes().containsKey("jira.fieldscreen.id"))
                {
                    fieldScreenId = new Long((String) actionDescriptor.getMetaAttributes().get("jira.fieldscreen.id"));
                }
                else
                {
                    throw new IllegalArgumentException("Unknown workflow view '"+view+"', or cannot find attribute 'jira.fieldscreen.id' for workflow action '" + actionDescriptor.getId() + "'.");
                }
            }

            FieldScreen fieldScreen = getFieldScreenManager().getFieldScreen(fieldScreenId);
            if (fieldScreen != null)
            {
                return fieldScreen;
            }
            else
            {
                throw new IllegalArgumentException("Cannot find Screen with id '" + fieldScreenId + "'.");
            }
        }
        else
        {
            return null;
        }
    }

    private FieldScreenManager getFieldScreenManager()
    {
        if (fieldScreenManager == null)
        {
            fieldScreenManager = ComponentAccessor.getFieldScreenManager();
        }

        return fieldScreenManager;
    }
}
