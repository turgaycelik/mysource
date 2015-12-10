package com.atlassian.jira.issue.fields.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.IssueTypeSystemField;
import com.atlassian.jira.issue.fields.ProjectSystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.json.beans.FieldHtmlBean;
import com.atlassian.jira.issue.fields.rest.json.beans.FieldTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;

import org.apache.commons.lang.StringUtils;

import webwork.action.Action;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.issue.operation.IssueOperations.CREATE_ISSUE_OPERATION;
import static com.atlassian.jira.issue.operation.IssueOperations.EDIT_ISSUE_OPERATION;

/**
 * Note: There's no unit tests in JIRA source but this is already tested quite heavily via func tests in the quick-edit
 * plugin as well as webdriver tests for quick edit in JIRA source.
 *
 * @since 5.0.3
 */
public class FieldHtmlFactoryImpl implements FieldHtmlFactory
{
    private static final Map<String, Object> DISPLAY_PARAMS =
            MapBuilder.<String, Object>newBuilder("noHeader", "true", "theme", "aui",
                    "isFirstField", true, "isLastField", true).toMutableMap();

    private final I18nHelper.BeanFactory beanFactory;
    private final FieldManager fieldManager;
    private FieldScreenRendererFactory fieldScreenRendererFactory;
    private final PermissionManager permissionManager;

    public FieldHtmlFactoryImpl(final I18nHelper.BeanFactory beanFactory, final FieldManager fieldManager,
            final FieldScreenRendererFactory fieldScreenRendererFactory, final PermissionManager permissionManager)
    {
        this.beanFactory = beanFactory;
        this.fieldManager = fieldManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.permissionManager = permissionManager;
    }

    @Override
    public List<FieldHtmlBean> getCreateFields(final User user, final OperationContext operationContext,
            final Action action, final MutableIssue newIssueObject, boolean retainValues, final List<String> fieldsToRetain)
    {
        final List<FieldHtmlBean> ret = createProjectAndIssueTypeFields(operationContext, action, newIssueObject);

        final List<FieldRenderItemWithTab> items = getRenderableItems(newIssueObject, CREATE_ISSUE_OPERATION);
        //First we need to populate the fieldValues holder with the correct stuff.
        for (FieldRenderItemWithTab item : items)
        {
            final FieldScreenRenderLayoutItem fsrli = item.getFieldScreenRenderLayoutItem();
            final String fieldId = fsrli.getOrderableField().getId();
            if (retainValues && fieldsToRetain.contains(fieldId))
            {
                //used when switching from full create back to quick create to keep the field values.
                fsrli.getOrderableField().populateFromParams(operationContext.getFieldValuesHolder(), ActionContext.getParameters());
            }
            else if (fieldsToRetain == null || !fieldsToRetain.contains(fieldId) || fieldId.equals(IssueFieldConstants.ATTACHMENT))
            {
                //JRADEV-7786: Attachments can't be retained between creates. They are temporary after the upload in the dialog
                // and passing them along would involve a lot of work!

                //if this field is not meant to retain its value from a previous create remove it from the
                //fieldValuesHolder.  Then populate it with the field's default value.
                operationContext.getFieldValuesHolder().remove(fieldId);
                fsrli.populateDefaults(operationContext.getFieldValuesHolder(), newIssueObject);
            }
        }

        //then we need to render the fields. Note that this cannot be moved into the block above since some fields
        //depend on the information that other fields have put into the fieldvaluesholder (like log work & timetracking for example).
        final I18nHelper i18nHelper = beanFactory.getInstance(user);
        for (FieldRenderItemWithTab item : items)
        {
            final FieldScreenRenderLayoutItem frli = item.getFieldScreenRenderLayoutItem();
            final String createHtml = frli.getCreateHtml(action, operationContext, newIssueObject, DISPLAY_PARAMS);
            //some custom fields may not have an edit view at all (JRADEV-7032)
            if (StringUtils.isNotBlank(createHtml))
            {
                final Object defaultValue = frli.getFieldLayoutItem().getOrderableField().getDefaultValue(newIssueObject);
                boolean isRequired = false;
                //JRADEV-7689: If a field has a default value, don't mark it required. It annoys users.
                if (defaultValue == null && frli.isRequired())
                {
                    isRequired = true;
                }
                ret.add(new FieldHtmlBean(frli.getFieldLayoutItem().getOrderableField().getId(),
                        i18nHelper.getText(frli.getFieldLayoutItem().getOrderableField().getNameKey()),
                        isRequired, createHtml.trim(), item.getFieldTab()));
            }
        }

        return ret;
    }

    @Override
    public List<FieldHtmlBean> getEditFields(final User user, final OperationContext operationContext,
            final Action action, final Issue issue, final boolean retainValues)
    {
        final List<FieldHtmlBean> ret = new ArrayList<FieldHtmlBean>();
        final List<FieldRenderItemWithTab> items = getRenderableItems(issue, EDIT_ISSUE_OPERATION);

        //First we need to populate the fieldValues holder with the correct stuff.
        for (FieldRenderItemWithTab item : items)
        {
            final FieldScreenRenderLayoutItem fsrli = item.getFieldScreenRenderLayoutItem();
            if (retainValues)
            {
                //this gets used when switching from the full edit form back to quick edit.  We need to keep the values that were
                //posted with this change.
                fsrli.getOrderableField().populateFromParams(operationContext.getFieldValuesHolder(), ActionContext.getParameters());
            }
            else
            {
                fsrli.populateFromIssue(operationContext.getFieldValuesHolder(), issue);
            }
        }

        //then we need to render the fields. Note that this cannot be moved into the block above since some fields
        //depend on the information that other fields have put into the fieldvaluesholder (like log work & timetracking for example).
        final I18nHelper i18nHelper = beanFactory.getInstance(user);
        FieldTab firstTab = null;
        for (FieldRenderItemWithTab item : items)
        {
            final FieldTab currentTab = item.getFieldTab();
            if (firstTab == null && currentTab.getPosition() == 0)
            {
                firstTab = currentTab;
            }
            final FieldScreenRenderLayoutItem fsrli = item.getFieldScreenRenderLayoutItem();
            final String editHtml = fsrli.getEditHtml(action, operationContext, issue, DISPLAY_PARAMS);
            //some custom fields may not have an edit view at all (JRADEV-7032)
            if (StringUtils.isNotBlank(editHtml))
            {
                ret.add(new FieldHtmlBean(fsrli.getFieldLayoutItem().getOrderableField().getId(),
                        i18nHelper.getText(fsrli.getFieldLayoutItem().getOrderableField().getNameKey()),
                        fsrli.isRequired(), editHtml.trim(), currentTab));
            }
        }

        //JRADEV-6908: The comment field is special and will always be there on edit!
        if (permissionManager.hasPermission(Permissions.COMMENT_ISSUE, issue, user))
        {
            final CommentSystemField commentField = (CommentSystemField) fieldManager.getField(IssueFieldConstants.COMMENT);
            if (retainValues)
            {
                commentField.populateFromParams(operationContext.getFieldValuesHolder(), ActionContext.getParameters());
            }
            final FieldLayoutItem commentFieldLayoutItem = getFieldScreenRenderer(issue, EDIT_ISSUE_OPERATION).getFieldScreenRenderLayoutItem(commentField).getFieldLayoutItem();
            ret.add(new FieldHtmlBean(commentField.getId(), commentField.getName(), false,
                    commentField.getEditHtml(commentFieldLayoutItem, operationContext, action, issue, DISPLAY_PARAMS), firstTab));
        }

        return ret;
    }

    @Override
    public List<FieldHtmlBean> getSubTaskCreateFields(final User user, final OperationContext operationContext,
            final Action action, final MutableIssue newIssueObject, boolean retainValues, final List<String> fieldsToRetain)
    {
        return getCreateFields(user, operationContext, action, newIssueObject, retainValues, fieldsToRetain);
    }

    private List<FieldHtmlBean> createProjectAndIssueTypeFields(final OperationContext operationContext, final Action action, final MutableIssue newIssueObject)
    {
        final List<FieldHtmlBean> ret = new ArrayList<FieldHtmlBean>();
        final ProjectSystemField projectField = (ProjectSystemField) fieldManager.getField(IssueFieldConstants.PROJECT);
        final IssueTypeSystemField issueTypeField = (IssueTypeSystemField) fieldManager.getField(IssueFieldConstants.ISSUE_TYPE);

        projectField.updateIssue(null, newIssueObject, operationContext.getFieldValuesHolder());
        issueTypeField.updateIssue(null, newIssueObject, operationContext.getFieldValuesHolder());

        ret.add(new FieldHtmlBean(projectField.getId(), projectField.getName(), true,
                projectField.getCreateHtml(null, operationContext, action, newIssueObject, DISPLAY_PARAMS), null));

        ret.add(new FieldHtmlBean(issueTypeField.getId(), issueTypeField.getName(), true,
                issueTypeField.getCreateHtml(null, operationContext, action, newIssueObject, DISPLAY_PARAMS), null));
        return ret;
    }

    private List<FieldRenderItemWithTab> getRenderableItems(final Issue issue, ScreenableIssueOperation operation)
    {
        final List<FieldRenderItemWithTab> items = new ArrayList<FieldRenderItemWithTab>();
        final FieldScreenRenderer fieldScreenRenderer = getFieldScreenRenderer(issue, operation);
        final List<FieldScreenRenderTab> fieldScreenRenderTabs = fieldScreenRenderer.getFieldScreenRenderTabs();
        for (final FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderTabs)
        {
            final FieldTab currentTab = new FieldTab(fieldScreenRenderTab.getName(), fieldScreenRenderTab.getPosition());
            for (final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
            {
                final String fieldId = fieldScreenRenderLayoutItem.getOrderableField().getId();
                //Add the field if it's not the project and it's not the issuetype (during create issue) and it it's shown.
                boolean excludeIssueType = operation.equals(CREATE_ISSUE_OPERATION) && IssueFieldConstants.ISSUE_TYPE.equals(fieldId);
                if (!IssueFieldConstants.PROJECT.equals(fieldId) && !excludeIssueType && fieldScreenRenderLayoutItem.isShow(issue))
                {
                    items.add(new FieldRenderItemWithTab(fieldScreenRenderLayoutItem, currentTab));
                }
            }
        }
        return items;
    }

    private FieldScreenRenderer getFieldScreenRenderer(final Issue issue, final ScreenableIssueOperation operation)
    {
        return fieldScreenRendererFactory.getFieldScreenRenderer(issue, operation);
    }

    private static class FieldRenderItemWithTab
    {
        private FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem;
        private FieldTab fieldTab;

        private FieldRenderItemWithTab(final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem, final FieldTab fieldTab)
        {
            this.fieldScreenRenderLayoutItem = fieldScreenRenderLayoutItem;
            this.fieldTab = fieldTab;
        }

        public FieldScreenRenderLayoutItem getFieldScreenRenderLayoutItem()
        {
            return fieldScreenRenderLayoutItem;
        }

        public FieldTab getFieldTab()
        {
            return fieldTab;
        }
    }
}
