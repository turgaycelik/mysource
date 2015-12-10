package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.action.admin.constants.AbstractViewConstants;
import com.atlassian.jira.web.action.admin.issuetypes.events.IssueTypeCreatedFromViewIssueTypesPageEvent;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@WebSudoRequired
public class ViewIssueTypes extends AbstractViewConstants implements AddIssueTypeAction
{
    public static final String NEW_ISSUE_TYPE_DEFAULT_ICON = "/images/icons/issuetypes/genericissue.png";

    private String style;

    private final FieldManager fieldManager;
    private final FieldConfigSchemeManager configSchemeManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final IssueTypeManageableOption issueTypeManageableOption;
    private final IssueTypeManager issueTypeManager;
    private final EventPublisher eventPublisher;

    public ViewIssueTypes(
            final FieldManager fieldManager,
            final FieldConfigSchemeManager configSchemeManager,
            final IssueTypeSchemeManager issueTypeSchemeManager,
            final TranslationManager translationManager,
            final IssueTypeManageableOption issueTypeManageableOption,
            final IssueTypeManager issueTypeManager,
            final EventPublisher eventPublisher)
    {
        super(translationManager);
        this.fieldManager = fieldManager;
        this.configSchemeManager = configSchemeManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.issueTypeManageableOption = issueTypeManageableOption;
        this.issueTypeManager = issueTypeManager;
        this.eventPublisher = eventPublisher;
        setIconurl(NEW_ISSUE_TYPE_DEFAULT_ICON);
    }

    protected String getConstantEntityName()
    {
        return "IssueType";
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.issuetype.lowercase");
    }

    protected Collection<GenericValue> getConstants()
    {
        return getConstantsManager().getAllIssueTypes();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshIssueTypes();
        fieldManager.refresh();
    }

    protected String getIssueConstantField()
    {
        return "type";
    }

    protected GenericValue getConstant(String id)
    {
        return getConstantsManager().getIssueType(id);
    }

    protected String getRedirectPage()
    {
        return "ViewIssues.jspa";
    }

    @RequiresXsrfCheck
    public String doAddIssueType() throws Exception
    {
        final boolean isSubtask = SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE.equals(getStyle());
        final String avatarId = isSubtask ?
                getApplicationProperties().getString(APKeys.JIRA_DEFAULT_ISSUETYPE_SUBTASK_AVATAR_ID) :
                getApplicationProperties().getString(APKeys.JIRA_DEFAULT_ISSUETYPE_AVATAR_ID);

        getConstantsManager().validateCreateIssueTypeWithAvatar(getName(), getStyle(), getDescription(), avatarId, this, "name");
        if (hasAnyErrors())
        {
            return ERROR;
        }
        if (isSubtask)
        {
            issueTypeManager.createSubTaskIssueType(getName(), getDescription(), Long.valueOf(avatarId));
        }
        else
        {
            issueTypeManager.createIssueType(name, description, Long.valueOf(avatarId));
        }

        eventPublisher.publish(new IssueTypeCreatedFromViewIssueTypesPageEvent());

        return redirectToView();
    }

    public String doAddNewIssueType()
    {
        return INPUT;
    }
    
    @Override
    protected GenericValue addConstant() throws GenericEntityException
    {
        throw new UnsupportedOperationException("Use doAddIssueType command instead!");
    }

    protected String redirectToView()
    {
        return returnCompleteWithInlineRedirect("ViewIssueTypes.jspa");
    }

    protected String getDefaultPropertyName()
    {
        return APKeys.JIRA_CONSTANT_DEFAULT_ISSUE_TYPE;
    }

    @Override
    public ManageableOptionType getManageableOption()
    {
        return issueTypeManageableOption;
    }

    @Override
    public List<Pair<String, Object>> getHiddenFields()
    {
        return Collections.emptyList();
    }

    public String getActionType()
    {
        return "view";
    }

    public Collection getAllRelatedSchemes(String id)
    {
        return issueTypeSchemeManager.getAllRelatedSchemes(id);
    }

    public List getSchemes()
    {
        return configSchemeManager.getConfigSchemesForField(fieldManager.getIssueTypeField());
    }

    public FieldConfigScheme getDefaultScheme()
    {
        return issueTypeSchemeManager.getDefaultIssueTypeScheme();
    }

    @Override
    public String getStyle()
    {
        return style;
    }

    @Override
    public void setStyle(String style)
    {
        this.style = style;
    }
    
    @Override
    public String getSubmitUrl()
    {
        return "AddIssueType.jspa";
    }
    
    @Override
    public String getCancelUrl()
    {
        return "ViewIssueTypes.jspa";
    }
}
