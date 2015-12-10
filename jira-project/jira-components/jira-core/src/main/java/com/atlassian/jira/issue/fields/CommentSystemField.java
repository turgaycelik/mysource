package com.atlassian.jira.issue.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.comment.property.DefaultCommentPropertyService;
import com.atlassian.jira.bc.issue.visibility.Visibilities;
import com.atlassian.jira.bc.issue.visibility.Visibility;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.comments.MutableComment;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.RenderableField;
import com.atlassian.jira.issue.fields.renderer.comment.CommentFieldRenderer;
import com.atlassian.jira.issue.fields.rest.CommentRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentsWithPaginationJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.search.handlers.CommentSearchHandlerFactory;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.mention.MentionService;
import com.atlassian.jira.plugin.webfragment.model.CommentHelper;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.web.action.issue.bulkedit.BulkWorkflowTransition;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;

import static com.atlassian.jira.bc.issue.comment.CommentService.CommentParameters;
import static com.atlassian.jira.bc.issue.comment.CommentService.CommentParameters.CommentParametersBuilder;

/**
 * Defines a comment in Jira.
 */
public class CommentSystemField extends AbstractOrderableField implements RenderableField, UnscreenableField, CommentField, RestAwareField, RestFieldOperations
{
    public static final String CREATE_COMMENT = "comment.create.param";
    public static final String EDIT_COMMENT = "comment.edit.param";
    public static final String REMOVE_COMMENT = "comment.remove.param";

    private static final Logger log = Logger.getLogger(CommentSystemField.class);

    private static final String COMMENT_NAME_KEY = "issue.field.comment";

    /**
     * The parameter name of the user-chosen group-type "level" for restricting the comment visibility
     */
    public static final String PARAM_GROUP_LEVEL = "groupLevel";

    /**
     * The parameter name of the user-chosen group or role-type "level" for restricting the comment visibility
     */
    public static final String PARAM_COMMENT_LEVEL = "commentLevel";

    public static final String PARAM_COMMENT_OBJECT = "commentObject";

    public static final String PARAM_COMMENT_PROPERTY = "commentProperty";

    /**
     * The parameter name of the user-chosen role-type "level" for restricting the comment visibility
     */
    public static final String PARAM_ROLE_LEVEL = "roleLevel";
    public static final String PARAM_COMMENT_ID = IssueFieldConstants.COMMENT + ":id";

    private final RendererManager rendererManager;
    private final JiraAuthenticationContext authenticationContext;
    private final CommentService commentService;
    private final ProjectRoleManager projectRoleManager;
    private final ProjectFactory projectFactory;
    private final GroupManager groupManager;
    private final JiraBaseUrls jiraBaseUrls;
    private final CommentManager commentManager;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final MentionService mentionService;
    private final CommentFieldRenderer commentFieldRenderer;
    private final DefaultCommentPropertyService commentPropertyService;
    private final I18nHelper.BeanFactory i18nFactory;
    private final EmailFormatter emailFormatter;

    public CommentSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, RendererManager rendererManager,
            PermissionManager permissionManager, CommentService commentService, ProjectRoleManager projectRoleManager,
            ProjectFactory projectFactory, CommentSearchHandlerFactory searchHandlerFactory, GroupManager groupManager,
            JiraBaseUrls jiraBaseUrls, CommentManager commentManager, DateTimeFormatterFactory dateTimeFormatterFactory,
            MentionService mentionService, CommentFieldRenderer commentFieldRenderer, DefaultCommentPropertyService commentPropertyService, I18nHelper.BeanFactory i18nFactory, final EmailFormatter emailFormatter)
    {
        super(IssueFieldConstants.COMMENT, COMMENT_NAME_KEY, templatingEngine, applicationProperties, authenticationContext, permissionManager, searchHandlerFactory);
        this.rendererManager = rendererManager;
        this.authenticationContext = authenticationContext;
        this.commentService = commentService;
        this.projectRoleManager = projectRoleManager;
        this.projectFactory = projectFactory;
        this.groupManager = groupManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.commentManager = commentManager;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.mentionService = mentionService;
        this.commentFieldRenderer = commentFieldRenderer;
        this.commentPropertyService = commentPropertyService;
        this.i18nFactory = i18nFactory;
        this.emailFormatter = emailFormatter;
    }

    /**
     * Defines the object that will be passed through to the create method
     *
     * @param params is a representation of the request params that are available
     * @return an object that holds the params we need for this Field.
     */
    @Override
    protected Object getRelevantParams(Map<String, String[]> params)
    {
        Map<String, Object> commentParams = new HashMap<String, Object>();
        String[] value = params.get(getId());
        if (value != null && value.length > 0)
        {
            commentParams.put(getId(), value[0]);
        }

        CommentVisibility commentVisibility = new CommentVisibility(params, PARAM_COMMENT_LEVEL);

        commentParams.put(PARAM_GROUP_LEVEL, commentVisibility.getGroupLevel());
        commentParams.put(PARAM_ROLE_LEVEL, commentVisibility.getRoleLevel());
        if (params.containsKey(CREATE_COMMENT))
        {
            commentParams.put(CREATE_COMMENT, params.get(CREATE_COMMENT));
        }
        else if (params.containsKey(EDIT_COMMENT))
        {
            commentParams.put(EDIT_COMMENT, params.get(EDIT_COMMENT));
        }
        else if (params.containsKey(REMOVE_COMMENT))
        {
            commentParams.put(REMOVE_COMMENT, params.get(REMOVE_COMMENT));
        }

        if (params.containsKey(PARAM_COMMENT_ID))
        {
            String[] commentId = params.get(PARAM_COMMENT_ID);
            if (commentId != null && commentId.length > 0)
            {
                commentParams.put(PARAM_COMMENT_ID, commentId[0]);
            }
        }
        if (params.containsKey(PARAM_COMMENT_OBJECT))
        {
            commentParams.put(PARAM_COMMENT_OBJECT, params.get(PARAM_COMMENT_OBJECT));
        }
        if (params.containsKey(PARAM_COMMENT_PROPERTY))
        {
            commentParams.put(PARAM_COMMENT_PROPERTY, params.get(PARAM_COMMENT_PROPERTY));
        }

        return commentParams;
    }

    @Override
    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        populateVelocityParams(fieldLayoutItem, null, velocityParams);

        if (operationContext != null && operationContext.getFieldValuesHolder() != null &&
                operationContext.getFieldValuesHolder().containsKey(getId()))
        {
            Map commentParams = (Map) operationContext.getFieldValuesHolder().get(getId());
            if (commentParams != null)
            {
                velocityParams.put(getId(), commentParams.get(getId()));
                // put the selected value into the params if it exists so we can handle errors
                populateParamsWithSelectedValue(commentParams, velocityParams);
            }
        }

        return commentFieldRenderer.getIssuePageEditHtml(velocityParams, CommentHelper.builder().issue(issue).build());
    }

    @Override
    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        populateVelocityParams(fieldLayoutItem, (operationContext != null) ? operationContext.getFieldValuesHolder() : null, velocityParams);

        CommentHelper.CommentHelperBuilder helperBuilder = CommentHelper.builder().issue(issue);

        if (operationContext != null && operationContext.getFieldValuesHolder() != null &&
                operationContext.getFieldValuesHolder().containsKey(getId()))
        {
            Map commentParams = (Map) operationContext.getFieldValuesHolder().get(getId());
            if (commentParams != null)
            {
                velocityParams.put(getId(), commentParams.get(getId()));
                // put the selected value into the params if it exists so we can handle errors
                populateParamsWithSelectedValue(commentParams, velocityParams);
                helperBuilder.comment((Comment) commentParams.get(PARAM_COMMENT_OBJECT));
            }
        }

        return commentFieldRenderer.getFieldEditHtml(velocityParams, helperBuilder.build());
    }

    @Override
    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        throw new UnsupportedOperationException("Comment system field does not know how to obtain a comment value given an Issue.");
    }

    @Override
    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);

        // get the rendered value without specifying an issue for context
        IssueRenderContext context;
        if (issue != null)
        {
            context = issue.getIssueRenderContext();
        }
        else
        {
            context = new IssueRenderContext(null);
        }
        String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;

        Map<String, Object> valueMap = (Map<String, Object>) value;

        velocityParams.put("value", rendererManager.getRenderedContent(rendererType, (String) valueMap.get(getId()), context));
        if (valueMap.containsKey(PARAM_GROUP_LEVEL))
        {
            velocityParams.put(PARAM_GROUP_LEVEL, valueMap.get(PARAM_GROUP_LEVEL));
        }
        if (valueMap.containsKey(PARAM_ROLE_LEVEL))
        {
            String roleId = (String) valueMap.get(PARAM_ROLE_LEVEL);
            // We need the display name of the role
            if (roleId != null)
            {
                ProjectRole projectRole = projectRoleManager.getProjectRole(new Long(roleId));
                if (projectRole != null)
                {
                    velocityParams.put("selectedRoleName", projectRole.getName());
                }
            }
            velocityParams.put(PARAM_ROLE_LEVEL, roleId);
        }
        if (valueMap.containsKey(PARAM_COMMENT_PROPERTY))
        {
            velocityParams.put(PARAM_COMMENT_PROPERTY, valueMap.get(PARAM_COMMENT_PROPERTY));
        }
        return commentFieldRenderer.getFieldViewHtml(velocityParams, CommentHelper.builder().issue(issue).build());
    }

    @Override
    public boolean isShown(Issue issue)
    {
        return hasPermission(issue, Permissions.COMMENT_ISSUE);
    }

    @Override
    public void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        Map<String, Object> commentParams = new HashMap<String, Object>();
        commentParams.put(getId(), "");
        commentParams.put(PARAM_GROUP_LEVEL, null);
        fieldValuesHolder.put(getId(), commentParams);
    }

    /**
     * Extracts comment values from the fieldValuesHolder and places them in another map to be used by the WorkflowManager.
     * These additional inputs are required by the CreateCommentFunction to successfully create a comment.
     *
     * @param fieldValuesHolder a map containing comment values from a BulkEdit. Obtained from BulkEditBean.
     * @param additionalInputs  a map to be passed onto a WorkflowManager.
     */
    public void populateAdditionalInputs(Map fieldValuesHolder, Map<String, Object> additionalInputs)
    {
        final Map<String, Object> commentParams = (Map<String, Object>) fieldValuesHolder.get(getId());
        if (commentParams != null)
        {
            final String comment = (String) commentParams.get(getId());
            if (StringUtils.isNotBlank(comment))
            {
                additionalInputs.put(WorkflowTransitionUtil.FIELD_COMMENT, commentParams.get(getId()));
                additionalInputs.put(WorkflowTransitionUtil.FIELD_COMMENT_LEVEL, commentParams.get(PARAM_GROUP_LEVEL));
                additionalInputs.put(WorkflowTransitionUtil.FIELD_COMMENT_ROLE_LEVEL, commentParams.get(PARAM_ROLE_LEVEL));
                additionalInputs.put(WorkflowTransitionUtil.FIELD_COMMENT_ROLE_LEVEL, commentParams.get(PARAM_ROLE_LEVEL));
                additionalInputs.put(CommentSystemField.PARAM_COMMENT_PROPERTY, commentParams.get(PARAM_COMMENT_PROPERTY));
            }
        }
    }

    @Override
    public void populateFromIssue(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        // since we don't edit the comment value and we can't resolve a single comment value from an issue,
        // just populate with defaults.
        populateDefaults(fieldValuesHolder, issue);
    }

    @Override
    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        Map<String, Object> commentParams = (Map<String, Object>) fieldValuesHolder.get(getId());
        String body = (String) commentParams.get(getId());

        String groupLevel = (String) commentParams.get(PARAM_GROUP_LEVEL);
        String roleLevel = (String) commentParams.get(PARAM_ROLE_LEVEL);
        Visibility visibility = Visibilities.fromGroupAndStrRoleId(groupLevel, roleLevel);

        ApplicationUser user = authenticationContext.getUser();

        if (commentParams.containsKey(EDIT_COMMENT))
        {
            validateEditComment(errorCollectionToAddTo, issue, commentParams, body, visibility, user);
        }
        else if (commentParams.containsKey(REMOVE_COMMENT))
        {
            validateRemoveComment(errorCollectionToAddTo, commentParams, user);
        }
        else
        {
            validateCreateComment(errorCollectionToAddTo, issue, commentParams, body, visibility, user);
        }
    }

    private void validateRemoveComment(ErrorCollection errorCollectionToAddTo, Map<String, Object> commentParams, ApplicationUser user)
    {
        Object commentIdObj = commentParams.get(PARAM_COMMENT_ID);
        if (commentIdObj != null)
        {
            try
            {
                Long commentId = Long.valueOf((String) commentIdObj);
                commentService.hasPermissionToDelete(new JiraServiceContextImpl(user, errorCollectionToAddTo), commentId);
            }
            catch (NumberFormatException ex)
            {
                errorCollectionToAddTo.addError(IssueFieldConstants.COMMENT, "invalid comment id specified.");
            }
        }
        else
        {
            errorCollectionToAddTo.addError(IssueFieldConstants.COMMENT, "no comment id specified.");
        }
    }

    private void validateCreateComment(ErrorCollection errorCollection, Issue issue, Map<String, Object> commentParams, String body,
            Visibility visibility, ApplicationUser user)
    {
        // Validate user has the correct permissions IF we are actually adding a comment
        if (StringUtils.isNotBlank(body))
        {
            commentService.hasPermissionToCreate(user, issue, errorCollection);
        }
        boolean allowEmptyComments = true;
        if (commentParams.get(CREATE_COMMENT) != null)
        {
            allowEmptyComments = false;
        }

        commentService.isValidCommentBody(body, errorCollection, allowEmptyComments);

        // Validate the group and role level settings
        commentService.isValidCommentVisibility(user, issue, visibility, errorCollection);
        validateCommentProperties(commentParams, errorCollection);
    }

    private void validateEditComment(ErrorCollection errorCollection, Issue issue, Map<String, Object> commentParams,
            String body, Visibility visibility, ApplicationUser user)
    {
        if (commentParams.get(PARAM_COMMENT_ID) != null)
        {
            try
            {
                commentService.isValidCommentBody(body, errorCollection);
                final Long commentIdAsLong = Long.valueOf((String) commentParams.get(PARAM_COMMENT_ID));
                commentService.hasPermissionToEdit(new JiraServiceContextImpl(user, errorCollection), commentIdAsLong);
                commentService.isValidCommentVisibility(user, issue, visibility, errorCollection);
                validateCommentProperties(commentParams, errorCollection);
            }
            catch (NumberFormatException ex)
            {
                errorCollection.addError(IssueFieldConstants.COMMENT, "invalid comment id specified.");
            }
        }
        else
        {
            errorCollection.addError(IssueFieldConstants.COMMENT, "no comment id specified.");
        }
    }

    @Override
    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    @Override
    public void createValue(Issue issue, Object value)
    {
        throw new UnsupportedOperationException("CreateValue on the comment system field is unsupported.");
    }

    @Override
    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        // all comment creations are seen as an update
        Map<String, Object> commentParams = (Map<String, Object>) modifiedValue.getNewValue();
        String body = (String) commentParams.get(getId());

        // allow the renderer for this field a change to transform the value
        String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
        body = (String) rendererManager.getRendererForType(rendererType).transformFromEdit(body);

        if (commentParams.containsKey(EDIT_COMMENT))
        {
            editComment(issueChangeHolder, commentParams, body);
        }
        else if(commentParams.containsKey(REMOVE_COMMENT))
        {
            removeComment(issueChangeHolder, commentParams);
        }
        else
        {
            if (StringUtils.isNotBlank(body))
            {
                createComment(issue, issueChangeHolder, commentParams, body);
            }
        }

    }

    private void removeComment(IssueChangeHolder issueChangeHolder, Map commentParams)
    {
        final ApplicationUser user = authenticationContext.getUser();
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final long commentId = Long.valueOf((String) commentParams.get(PARAM_COMMENT_ID));
        Comment comment = commentService.getCommentById(user, commentId, errorCollection);
        commentService.delete(new JiraServiceContextImpl(user, errorCollection), comment, true);
        if (errorCollection.hasAnyErrors())
        {
            log.error("Error updating comment id '" + commentId + "' Error(s): '" + errorCollection.toString() + "'");
        }
        else
        {
            issueChangeHolder.setComment(comment);
        }
    }

    private void editComment(IssueChangeHolder issueChangeHolder, Map<String, Object> commentParams, String body)
    {
        final ApplicationUser user = authenticationContext.getUser();
        final long commentId = Long.valueOf((String) commentParams.get(PARAM_COMMENT_ID));
        final ErrorCollection errorCollection = new SimpleErrorCollection();

        MutableComment mutableComment = commentService.getMutableComment(user, commentId, errorCollection);
        CommentParametersBuilder builder = CommentParameters.builder(mutableComment);

        if (StringUtils.isNotBlank(body))
        {
            builder.body(body);
        }
        String groupLevel = (String) commentParams.get(PARAM_GROUP_LEVEL);
        String roleLevelIdStr = (String) commentParams.get(PARAM_ROLE_LEVEL);
        builder.visibility(Visibilities.fromGroupAndStrRoleId(groupLevel, roleLevelIdStr));
        builder.commentProperties(getCommentPropertiesQuietly(commentParams));

        final CommentService.CommentUpdateValidationResult validationResult = commentService.validateCommentUpdate(user, commentId, builder.build());
        if (validationResult.isValid())
        {
            commentService.update(user, validationResult, true);
            issueChangeHolder.setComment(mutableComment);
        }
        else
        {
            log.error("Error updating comment id '" + commentId + "' Error(s): '" + errorCollection.toString()+"'");
        }
    }

    private void createComment(Issue issue, IssueChangeHolder issueChangeHolder, Map<String, Object> commentParams, String body)
    {
        String groupLevel = (String) commentParams.get(PARAM_GROUP_LEVEL);
        String roleLevelIdStr = (String) commentParams.get(PARAM_ROLE_LEVEL);
        final Visibility visibility = Visibilities.fromGroupAndStrRoleId(groupLevel, roleLevelIdStr);

        final ApplicationUser user = authenticationContext.getUser();

        final CommentParameters commentParameters = CommentParameters.builder()
                .author(user)
                .body(body)
                .commentProperties(getCommentPropertiesQuietly(commentParams))
                .visibility(visibility)
                .issue(issue)
                .build();

        final CommentService.CommentCreateValidationResult validationResult = commentService.validateCommentCreate(user, commentParameters);

        if (validationResult.isValid())
        {
            Comment comment = commentService.create(user, validationResult, false);
            issueChangeHolder.setComment(comment);
        }
        else
        {
            log.error("There was an error creating a comment value: " + validationResult.getErrorCollection().toString());
        }
    }

    /**
     * Sets the value as a modified external field in the issue so that this
     * field will be updated along with all the other modified issue values.
     */
    @Override
    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            Map commentParams = (Map) fieldValueHolder.get(getId());
            if (StringUtils.isNotBlank((String) commentParams.get(getId())))
            {
                issue.setExternalFieldValue(getId(), fieldValueHolder.get(getId()));
            }
        }
    }

    @Override
    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        // Warn the users if we are bulk moving and the renderer types are different in one of the fieldLayoutItems
        if (originalIssues.size() > 1)
        {
            for (final Object originalIssue1 : originalIssues)
            {
                Issue originalIssue = (Issue) originalIssue1;

                // Also if the field is renderable and the render types differ prompt with an edit
                FieldLayoutItem fieldLayoutItem = null;
                try
                {
                    fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(originalIssue.getProjectObject(), originalIssue.getIssueTypeObject().getId()).getFieldLayoutItem(getId());
                }
                catch (DataAccessException e)
                {
                    log.warn(getName() + " field was unable to resolve the field layout item for issue " + originalIssue.getId(), e);
                }

                String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
                String targetRendererType = (targetFieldLayoutItem != null) ? targetFieldLayoutItem.getRendererType() : null;
                if (!rendererTypesEqual(rendererType, targetRendererType))
                {
                    return new MessagedResult(false, getAuthenticationContext().getI18nHelper().getText("renderer.bulk.move.warning"), MessagedResult.WARNING);
                }
            }
        }

        return new MessagedResult(false);
    }

    @Override
    public void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // don't have the system field do anything for move at the moment
    }

    @Override
    public void removeValueFromIssueObject(MutableIssue issue)
    {
        throw new UnsupportedOperationException("Remove is not done through the system field for comment.");
    }

    @Override
    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return false;
    }

    @Override
    public boolean hasValue(Issue issue)
    {
        // return false so that move does not get the wrong idea
        return false;
    }

    @Override
    public Object getValueFromParams(Map params) throws FieldValidationException
    {
        if (params.containsKey(getId()))
        {
            return params.get(getId());
        }

        return null;
    }

    @Override
    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
        // no conversion is needed
    }

    @Override
    public String getValueFromIssue(Issue issue)
    {
        throw new UnsupportedOperationException("Comment system field does not know how to obtain a comment value given an Issue.");
    }

    @Override
    public boolean isRenderable()
    {
        return true;
    }

    /**
     * Adds to the given velocity parameters using the given fieldValuesHolder and
     * fieldLayoutItem (to determine the renderer).
     *
     * @param fieldLayoutItem the FieldLayoutItem in play
     * @param fieldValuesHolder the fields values holder in play
     * @param velocityParams    the velocity parameters to which values will be added
     */
    private void populateVelocityParams(FieldLayoutItem fieldLayoutItem, Map fieldValuesHolder, Map<String, Object> velocityParams)
    {
        if (fieldValuesHolder != null)
        {
            Map commentParams = (Map) fieldValuesHolder.get(getId());
            if (commentParams != null)
            {
                velocityParams.put(getId(), commentParams.get(getId()));
            }
        }

        velocityParams.put("rendererParams", new HashMap());
        String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
        velocityParams.put("rendererDescriptor", rendererManager.getRendererForType(rendererType).getDescriptor());
        velocityParams.put("groupLevels", getGroupLevels());
        velocityParams.put("mentionable", mentionService.isUserAbleToMention(authenticationContext.getLoggedInUser()));

        Issue issue = (Issue) velocityParams.get("issue");
        if (issue != null)
        {
            velocityParams.put("roleLevels", getRoleLevels(issue));
        }
        else
        {
            // We are possibly in a bulk screen
            Object action = velocityParams.get("action");
            if (action != null && action instanceof BulkWorkflowTransition)
            {
                BulkWorkflowTransition bulkWorkflowTransition = (BulkWorkflowTransition) action;
                BulkEditBean bulkEditBean = bulkWorkflowTransition.getBulkEditBean();
                if (bulkEditBean != null)
                {
                    // TODO: what if there are multiple projects? We should get the intersection of all roles.
                    GenericValue project = bulkEditBean.getProject();
                    if (project != null)
                    {
                        velocityParams.put("roleLevels", getRoleLevels(project));
                    }
                }
            }
        }

    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        String rendererType = null;

        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        for (FieldLayout fieldLayout : bulkEditBean.getFieldLayouts())
        {
            if (fieldLayout.isFieldHidden(getId()))
            {
                return "bulk.edit.unavailable.hidden";
            }

            // Check for different renderer type
            if (StringUtils.isBlank(rendererType))
            {
                rendererType = fieldLayout.getRendererTypeForField(IssueFieldConstants.COMMENT);
            }
            else if (!rendererType.equals(fieldLayout.getRendererTypeForField(IssueFieldConstants.COMMENT)))
            {
                return "bulk.edit.unavailable.different.renderers";
            }
        }

        // If we got here then the field is visible in all field layouts
        // So check for permissions
        // Have to loop through all the issues incase the permission has been granted to current assignee/reporter (i.e. assigned to a role)
        for (Issue issue : bulkEditBean.getSelectedIssues())
        {
            if (!isShown(issue))
            {
                return "bulk.edit.unavailable.permission";
            }
        }

        // This field is available for bulk-editing, return null (i.e no unavailble message)
        return null;
    }

    /**
     * Returns the list of group names that the current user is in.
     *
     * @return the possibly empty Collection of group names (Strings)
     */
    private Collection getGroupLevels()
    {
        Collection groups;
        if (authenticationContext.getLoggedInUser() == null || !commentService.isGroupVisibilityEnabled())
        {
            groups = Collections.EMPTY_LIST;
        }
        else
        {
            Collection<String> groupNames = groupManager.getGroupNamesForUser(authenticationContext.getLoggedInUser().getName());
            List<String> userGroups = new ArrayList<String>(groupNames);
            Collections.sort(userGroups);
            groups = userGroups;
        }
        return groups;
    }

    private Collection getRoleLevels(GenericValue project)
    {
        if (project == null) {
            throw new NullPointerException("project GenericValue was null");
        }
        Collection roles;
        if (commentService.isProjectRoleVisibilityEnabled())
        {
            ApplicationUser user = authenticationContext.getUser();
            roles = projectRoleManager.getProjectRoles(user, projectFactory.getProject(project));
        }
        else
        {
            roles = Collections.EMPTY_LIST;
        }
        return roles;
    }

    private Collection getRoleLevels(Issue issue)
    {
        Collection roles;
        if (commentService.isProjectRoleVisibilityEnabled())
        {
            ApplicationUser user = authenticationContext.getUser();
            roles = projectRoleManager.getProjectRoles(user, issue.getProjectObject());
        }
        else
        {
            roles = Collections.EMPTY_LIST;
        }
        return roles;
    }

    private void populateParamsWithSelectedValue(Map commentParams, Map<String, Object> velocityParams)
    {
        if (commentParams.get(PARAM_ROLE_LEVEL) != null)
        {
            velocityParams.put(PARAM_COMMENT_LEVEL, "role:" + commentParams.get(PARAM_ROLE_LEVEL));
        }
        else if (commentParams.get(PARAM_GROUP_LEVEL) != null)
        {
            velocityParams.put(PARAM_COMMENT_LEVEL, "group:" + commentParams.get(PARAM_GROUP_LEVEL));
        }
        if (commentParams.get(PARAM_COMMENT_PROPERTY) != null)
        {
            velocityParams.put(PARAM_COMMENT_PROPERTY, commentParams.get(PARAM_COMMENT_PROPERTY));
        }
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

     @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.systemArray(JsonType.COMMENT_TYPE, getId());
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        return new CommentRestFieldOperationsHandler(commentManager, projectRoleManager, authenticationContext.getI18nHelper());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(final Issue issue, boolean renderedVersionRequired, @Nullable final FieldLayoutItem fieldLayoutItem)
    {
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final List<Comment> comments = commentService.getCommentsForUser(authenticationContext.getUser(), issue);
        CommentsWithPaginationJsonBean commentsWithPaginationJsonBean = new CommentsWithPaginationJsonBean();
        commentsWithPaginationJsonBean.setMaxResults(comments.size());
        commentsWithPaginationJsonBean.setTotal(comments.size());
        commentsWithPaginationJsonBean.setStartAt(0);
        commentsWithPaginationJsonBean.setComments(CommentJsonBean.shortBeans(comments, jiraBaseUrls, projectRoleManager, authenticationContext.getUser(), emailFormatter));
        FieldJsonRepresentation fieldJsonRepresentation = new FieldJsonRepresentation(new JsonData(commentsWithPaginationJsonBean));

        if (renderedVersionRequired)
        {
            CommentsWithPaginationJsonBean renderedBean = new CommentsWithPaginationJsonBean();
            renderedBean.setMaxResults(comments.size());
            renderedBean.setTotal(comments.size());
            renderedBean.setStartAt(0);
            renderedBean.setComments(CommentJsonBean.renderedShortBeans(comments, jiraBaseUrls, projectRoleManager, dateTimeFormatterFactory, rendererManager,
                    fieldLayoutItem == null ? null : fieldLayoutItem.getRendererType(), issue.getIssueRenderContext(), authenticationContext.getUser(), emailFormatter));
            fieldJsonRepresentation.setRenderedData(new JsonData(renderedBean));
        }

        if (!errorCollection.hasAnyErrors())
        {
            return fieldJsonRepresentation;
        }
        else
        {
            log.warn("Failed to include comments in REST response" + errorCollection.toString());
            return null;
        }

    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        return null;
    }

    public void validateCommentProperties(Map<String, Object> properties, ErrorCollection errorCollection)
    {
        try
        {
            final ImmutableMap<String, JSONObject> commentProperties = getCommentPropertiesFromParameters(properties);
            for (Map.Entry<String, JSONObject> property : commentProperties.entrySet())
            {
                EntityPropertyService.PropertyInput propertyInput = new EntityPropertyService.PropertyInput(property.getValue().toString(), property.getKey());
                errorCollection.addErrorCollection(commentPropertyService.validatePropertyInput(propertyInput));
            }
        }
        catch (JSONException e)
        {
            errorCollection.addErrorMessage(i18nFactory.getInstance(authenticationContext.getUser()).getText("jira.properties.service.invalid.json", properties.get(PARAM_COMMENT_PROPERTY)));
        }
    }

    private ImmutableMap<String, JSONObject> getCommentPropertiesQuietly(final Map<String, Object> commentParams)
    {
        try
        {
            return getCommentPropertiesFromParameters(commentParams);
        }
        catch (JSONException e)
        {
            // This should never get here, as properties are validated before being set.
            log.error("Comment properties are not valid JSON. The properties should be validated before comment is created.");
            return ImmutableMap.of();
        }
    }

    public static ImmutableMap<String, JSONObject> getCommentPropertiesFromParameters(final Map<String, Object> commentParams)
            throws JSONException
    {
        if (commentParams.containsKey(PARAM_COMMENT_PROPERTY))
        {
            final String[] array = (String[]) commentParams.get(PARAM_COMMENT_PROPERTY);
            if (array != null && array.length == 1)
            {
                final JSONArray jsonArray = new JSONArray(array[0]);
                final ImmutableMap.Builder<String, JSONObject> builder = ImmutableMap.builder();
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject object = jsonArray.getJSONObject(i);
                    builder.put(object.getString("key"), object.getJSONObject("value"));
                }
                return builder.build();
            }
        }
        return ImmutableMap.of();
    }
}
