package com.atlassian.jira.workflow;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;

/**
 * @since v4.4
 */
public class WorkflowTransitionUtilFactoryImpl implements WorkflowTransitionUtilFactory
{
    private final JiraAuthenticationContext authenticationContext;
    private final WorkflowManager workflowManager;
    private final PermissionManager permissionManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final CommentService commentService;
    private final I18nHelper.BeanFactory i18nFactory;

    public WorkflowTransitionUtilFactoryImpl(final JiraAuthenticationContext authenticationContext,
            final WorkflowManager workflowManager, final PermissionManager permissionManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory, final CommentService commentService,
            final I18nHelper.BeanFactory i18nFactory)
    {
        this.authenticationContext = authenticationContext;
        this.workflowManager = workflowManager;
        this.permissionManager = permissionManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.commentService = commentService;
        this.i18nFactory = i18nFactory;
    }


    @Override
    public WorkflowTransitionUtil create()
    {
        return new WorkflowTransitionUtilImpl(authenticationContext, workflowManager, permissionManager,
                fieldScreenRendererFactory, commentService, i18nFactory);
    }
}
