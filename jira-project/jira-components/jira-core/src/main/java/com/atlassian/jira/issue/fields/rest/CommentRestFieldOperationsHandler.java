package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.CommentVisibility;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.VisibilityJsonBean;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.issue.IssueFieldConstants.ISSUE_LINKS;


/**
 * @since v5.0
 */
public class CommentRestFieldOperationsHandler implements RestFieldOperationsHandler
{
    private final CommentManager commentManager;
    private final ProjectRoleManager projectRoleManager;
    private final I18nHelper i18nHelper;

    public CommentRestFieldOperationsHandler(CommentManager commentManager, ProjectRoleManager projectRoleManager, I18nHelper i18nHelper)
    {
        this.commentManager = commentManager;
        this.projectRoleManager = projectRoleManager;
        this.i18nHelper = i18nHelper;
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.ADD.getName(), StandardOperation.EDIT.getName(), StandardOperation.REMOVE.getName());
    }

    @Override
    public ErrorCollection updateIssueInputParameters(IssueContext issueCtx, Issue issue, String fieldId, IssueInputParameters inputParameters, List<FieldOperationHolder> operations)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        if (operations.isEmpty())
        {
            return errors;
        }

        if (operations.size() > 1)
        {
            errors.addError(IssueFieldConstants.COMMENT, i18nHelper.getText("rest.operations.morethanone", String.valueOf(operations.size()), fieldId));
            return errors;
        }
        FieldOperationHolder fieldOperationHolder = operations.get(0);
        StandardOperation standardOperation = StandardOperation.valueOf(fieldOperationHolder.getOperation().toUpperCase());
        CommentJsonBean commentJsonBean = fieldOperationHolder.getData().convertValue(IssueFieldConstants.COMMENT, CommentJsonBean.class, errors);
        if (errors.hasAnyErrors())
        {
            return errors;
        }
        switch (standardOperation)
        {
             case ADD:
                performAddOperation(commentJsonBean, inputParameters, errors);
                break;
            case EDIT:
                performEditOperation(commentJsonBean, inputParameters, errors);
                break;
            case REMOVE:
                performRemoveOperation(commentJsonBean, inputParameters, errors);
                break;
        }

        if (standardOperation.equals(StandardOperation.REMOVE) || !commentJsonBean.isVisibilitySet())
        {
            return errors;
        }

        VisibilityJsonBean commentVisibility = commentJsonBean.getVisibility();
        String securityLevel = null;
        if (commentVisibility == null)
        {
            //AN EMPTY ROLE LEVEL WILL DELETE ANY EXISTING ISSUE SECURITY LEVEL
             securityLevel = CommentVisibility.getRoleLevelWithPrefix("");
        }
        else if (commentVisibility.getType().equals(VisibilityJsonBean.VisibilityType.group))
        {
            String group = commentVisibility.getValue();
            if(StringUtils.isNotEmpty(group))
            {
                securityLevel = CommentVisibility.getCommentLevelFromLevels(group, null);
            }
            else
            {
                errors.addError(IssueFieldConstants.COMMENT, i18nHelper.getText("rest.comment.visibility.group.no.value"));
            }
        }
        else
        {
            String roleLevel = commentVisibility.getValue();
            if (StringUtils.isNotEmpty(roleLevel))
            {
                ProjectRole projectRole = projectRoleManager.getProjectRole(roleLevel);
                Long roleLevelId;
                if (projectRole != null)
                {
                    roleLevelId = projectRole.getId();
                }
                else
                {
                    try
                    {
                        roleLevelId = Long.valueOf(roleLevel);
                    }
                    catch (NumberFormatException ex)
                    {
                        errors.addError(IssueFieldConstants.COMMENT, i18nHelper.getText("rest.comment.visibility.role.level.invalid"));
                        return errors;
                    }
                }
                securityLevel = CommentVisibility.getCommentLevelFromLevels(null, roleLevelId);
            }
            else
            {
                errors.addError(IssueFieldConstants.COMMENT, i18nHelper.getText("rest.comment.visibility.role.level.no.value"));
            }
        }

        final List<String> commentSecurity = new ArrayList<String>();
        commentSecurity.add(securityLevel);
        inputParameters.getActionParameters().put(CommentSystemField.PARAM_COMMENT_LEVEL, commentSecurity.toArray(new String[commentSecurity.size()]));
        return errors;
    }



    private void performEditOperation(CommentJsonBean commentJson, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.getActionParameters().put(CommentSystemField.EDIT_COMMENT, new String[] { "true" });
        if (StringUtils.isNotEmpty(commentJson.getId()))
        {
            parameters.getActionParameters().put(CommentSystemField.PARAM_COMMENT_ID, new String[] { commentJson.getId() });
        }
        final List<String> commentParams = new ArrayList<String>();
        if (commentJson.getBody() == null)
        {
            try
            {
                Comment comment = commentManager.getCommentById(Long.valueOf(commentJson.getId()));
                if (comment != null)
                {
                    commentParams.add(comment.getBody());
                }
                else
                {
                    //NB: We have to supply a body, otherwise the commentJson field will not be validated.
                    commentParams.add("DUMMY BODY");
                }
            }
            catch (NumberFormatException ex)
            {
                //We ignore any exception at this stage, because the validation in the CommentSystemField will report any issues with the supplied commentJson id.
                //NB: We have to supply a body, otherwise the commentJson field will not be validated.
                commentParams.add("DUMMY BODY");
            }
        }
        else
        {
            commentParams.add(commentJson.getBody());
        }
        parameters.getActionParameters().put(IssueFieldConstants.COMMENT, commentParams.toArray(new String[commentParams.size()]));
    }

    private void performAddOperation(CommentJsonBean commentJson, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.getActionParameters().put(CommentSystemField.CREATE_COMMENT, new String[] { "true" });
        final List<String> commentParams = new ArrayList<String>();
        commentParams.add(commentJson.getBody());
        parameters.getActionParameters().put(IssueFieldConstants.COMMENT, commentParams.toArray(new String[commentParams.size()]));
    }

    private void performRemoveOperation(CommentJsonBean commentJson, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.getActionParameters().put(CommentSystemField.REMOVE_COMMENT, new String[] { "true" });
        final List<String> commentParams = new ArrayList<String>();
        if (StringUtils.isNotEmpty(commentJson.getId()))
        {
            parameters.getActionParameters().put(CommentSystemField.PARAM_COMMENT_ID, new String[] { commentJson.getId() });
        }
        //The field validation for the comment field only works if there is a body in the action params.
        commentParams.add("DUMMY BODY");
        parameters.getActionParameters().put(IssueFieldConstants.COMMENT, commentParams.toArray(new String[commentParams.size()]));
    }
}
