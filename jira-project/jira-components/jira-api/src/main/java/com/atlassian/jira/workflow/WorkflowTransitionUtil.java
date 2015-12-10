package com.atlassian.jira.workflow;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.util.ErrorCollection;
import com.opensymphony.workflow.loader.ActionDescriptor;

import java.util.Map;

@PublicApi
public interface WorkflowTransitionUtil
{
    // Available workflow views
    public static final String VIEW_RESOLVE = "resolveissue";
    public static final String VIEW_COMMENTASSIGN = "commentassign";
    public static final String VIEW_SCREEN = "fieldscreen";

    public static final String FIELD_COMMENT = "comment";
    // TODO make this less confusing, it should probably be groupLevel not commentLevel
    public static final String FIELD_COMMENT_LEVEL = "commentLevel";
    public static final String FIELD_COMMENT_ROLE_LEVEL = "roleLevel";
    public static final String FIELD_COMMENT_GROUP_LEVEL = "groupLevel";
    public static final Long VIEW_COMMENTASSIGN_ID = new Long(2);
    public static final Long VIEW_RESOLVE_ID = new Long(3);

    void setIssue(MutableIssue issue);

    void setAction(int actionId);

    void setParams(Map params);

    /**
     * Pass a parameter into the action's "transientVars" parameter.
     * @param key
     * @param val
     */
    void addAdditionalInput(Object key, Object val);

    ErrorCollection validate();

    ErrorCollection progress();

    ActionDescriptor getActionDescriptor();

    /**
     * Gets username of user who the workflow transition will be executed as
     * @return username
     * @deprecated Use {@link #getUserKey()} instead. Since v6.0.
     */
    String getUsername();

    /**
     * Sets username of user who the workflow transition will be executed as
     * @param username desired username
     * @deprecated Use {@link #setUserkey(String)} instead. Since v6.0.
     */
    void setUsername(String username);

    /**
     * Gets key of user who the workflow transition will be executed as
     * @return username
     */
    String getUserKey();

    /**
     * Sets key of user who the workflow transition will be executed as
     * @param userkey desired user's key
     */
    void setUserkey(String userkey);


    FieldScreenRenderer getFieldScreenRenderer();

    /**
     * Checks if there's a screen associated with the workflow action for this transition.  Returns false
     * if no screen is associated with the action.
     *
     * @return true if a screen is associated with the current workflow action.
     */
    boolean hasScreen();
}
