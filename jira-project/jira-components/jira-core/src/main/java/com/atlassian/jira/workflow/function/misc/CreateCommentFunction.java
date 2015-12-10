/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.misc;

import java.util.Map;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowUtil;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;

import org.apache.log4j.Logger;

import static com.atlassian.jira.bc.issue.comment.CommentService.CommentParameters;

/**
 * Creates a comment based on incoming parameters.
 *
 * @author <a href="mailto:plightbo@atlassian.com">Pat Lightbody</a>
 */
public class CreateCommentFunction implements FunctionProvider
{
    private static final Logger log = Logger.getLogger(CreateCommentFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        try
        {
            String commentBody = (String) transientVars.get(WorkflowTransitionUtil.FIELD_COMMENT);
            String groupLevel = (String) transientVars.get(WorkflowTransitionUtil.FIELD_COMMENT_LEVEL);
            String roleLevel = (String) transientVars.get(CommentSystemField.PARAM_ROLE_LEVEL);
            ImmutableMap<String,JSONObject> commentProperties = CommentSystemField.getCommentPropertiesFromParameters(transientVars);
            Long roleLevelId = null;
            if (TextUtils.stringSet(roleLevel))
            {
                roleLevelId = Long.valueOf(roleLevel);
            }

            Issue issue = (Issue) transientVars.get("issue");

            // Just a simple check to make sure we actually have a comment.
            if (TextUtils.stringSet(commentBody))
            {
                ApplicationUser author = WorkflowUtil.getCallerUser(transientVars);

                CommentService commentService = ComponentAccessor.getComponent(CommentService.class);

                CommentParameters commentParameters = CommentParameters.builder()
                        .author(author)
                        .body(commentBody)
                        .groupLevel(groupLevel)
                        .roleLevelId(roleLevelId)
                        .issue(issue)
                        .commentProperties(commentProperties)
                        .build();

                CommentService.CommentCreateValidationResult commentCreateValidationResult = commentService.validateCommentCreate(author, commentParameters);

                if (commentCreateValidationResult.isValid())
                {
                    // put a comment in transientVars map
                    transientVars.put("commentValue", commentService.create(author, commentCreateValidationResult, false));
                }
            }
        }
        catch (Exception e)
        {
            log.error("Exception: " + e, e);
        }
    }

    public static FunctionDescriptor makeDescriptor()
    {
        FunctionDescriptor descriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        descriptor.setType("class");
        descriptor.getArgs().put("class.name", CreateCommentFunction.class.getName());
        return descriptor;
    }
}
