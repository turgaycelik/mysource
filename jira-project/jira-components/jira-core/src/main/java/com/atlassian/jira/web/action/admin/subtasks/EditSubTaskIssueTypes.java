package com.atlassian.jira.web.action.admin.subtasks;

import java.util.Collection;
import java.util.Map;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.issuetypes.IssueTypeTemplateProperties;
import com.atlassian.jira.web.action.issue.URLUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

@WebSudoRequired
public class EditSubTaskIssueTypes extends JiraWebActionSupport implements IssueTypeTemplateProperties
{
    private final SubTaskManager subTaskManager;
    private final ConstantsManager constantsManager;

    private String id;
    private String name;
    private Long sequence;
    private String description;
    private String iconurl;
    private Long avatarId;

    public EditSubTaskIssueTypes(SubTaskManager subTaskManager, ConstantsManager constantsManager)
    {
        this.subTaskManager = subTaskManager;
        this.constantsManager = constantsManager;
    }

    public String doDefault() throws Exception
    {
        if (!isSubtasksEnabled())
        {
            addErrorMessage(getText("admin.errors.subtasks.disabled"));
            return getResult();
        }

        if (!TextUtils.stringSet(getId()))
        {
            addErrorMessage(getText("admin.errors.no.id.set"));
            return getResult();
        }
        else
        {
            final IssueType subTaskIssueType = subTaskManager.getSubTaskIssueType(getId());
            setName(subTaskIssueType.getName());
            setSequence(subTaskIssueType.getSequence());
            setDescription(subTaskIssueType.getDescription());
            setIconurl(subTaskIssueType.getIconUrl());
            if (null!=subTaskIssueType.getAvatar())
            {
                setAvatarId(subTaskIssueType.getAvatar().getId());
            }
        }

        return INPUT;
    }

    protected void doValidation()
    {
        // Ensure sub tasks are turned on
        if (!isSubtasksEnabled())
        {
            addErrorMessage(getText("admin.errors.subtasks.are.disabled"));
            return;
        }

        if (!TextUtils.stringSet(getId()))
        {
            addErrorMessage(getText("admin.errors.no.id.set"));
        }
        // Ensure that the name is set
        else if (!TextUtils.stringSet(getName()))
        {
            addError("name", getText("admin.errors.specify.a.name.for.this.new.sub.task.issue.type"));
        }
        else
        {
            // Ensure that an issue type with that name does not already exist
            // WARNING: ensure that we allow the same name to be kept!
            final IssueConstant subTaskIssueType = constantsManager.getIssueConstantByName("IssueType", getName());
            if (subTaskIssueType != null)
            {
                // Check if this is NOT the issue type that is being edited
                if (!getId().equals(subTaskIssueType.getId()))
                {
                    // A duplicate name has been entered
                    addError("name", getText("admin.errors.issue.type.with.this.name.already.exists"));
                }
            }
        }
        if (avatarId==null && !TextUtils.stringSet(getIconurl()))
        {
            addError("iconurl", getText("admin.errors.issuetypes.must.specify.url"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        subTaskManager.updateSubTaskIssueType(getId(), getName(), getSequence(), getDescription(), getAvatarId());

        return getRedirect("ManageSubTasks.jspa");
    }

    private boolean isSubtasksEnabled()
    {
        return subTaskManager.isSubTasksEnabled();
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Long getSequence()
    {
        return sequence;
    }

    public void setSequence(Long sequence)
    {
        this.sequence = sequence;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getIconurl()
    {
        return iconurl;
    }

    public void setIconurl(String iconurl)
    {
        this.iconurl = iconurl;
    }

    @Override
    public Long getAvatarId()
    {
        return avatarId;
    }

    public void setAvatarId(final Long avatarId)
    {
        this.avatarId = avatarId;
    }

    @Override
    @ActionViewData (key = "issueType")
    public IssueTypeViewData getIssueTypeValue()
    {
        return new IssueTypeViewData();
    }

    @Override
    @ActionViewData
    public String getAction()
    {
        return "EditSubTaskIssueTypes.jspa";
    }

    @Override
    @ActionViewData
    public String getCancelAction()
    {
        return "ManageSubTasks.jspa";
    }

    @ActionViewData
    public String getActiveTab()
    {
        return "subtasks";
    }

    @Override
    @ActionViewData
    public String getToken()
    {
        return super.getXsrfToken();
    }

    @ActionViewData(key = "errors")
    public Map<String, Object> getWrappedErrorsForView() {
        return MapBuilder.<String,Object>newBuilder()
                .add("errors", super.getErrors())
                .toMap();
    }

    @Override
    @ActionViewData
    public Collection<String> getErrorMessages()
    {
        return super.getErrorMessages();
    }

    @Override
    @ActionViewData
    public String getDefaultAvatarId()
    {
        return getApplicationProperties().getString(APKeys.JIRA_DEFAULT_ISSUETYPE_SUBTASK_AVATAR_ID);
    }

    @Override
    @ActionViewData
    public String getEditTitleTextId()
    {
        return getI18nHelper().getText("admin.subtasks.edit.subtask.issue.type");
    }

    private class IssueTypeViewData implements IssueTypeTemplateProperties.IssueTypeViewData {

        @Override
        public String getName()
        {
            return EditSubTaskIssueTypes.this.getName();
        }

        @Override
        public String getDescription()
        {
            return EditSubTaskIssueTypes.this.getName();
        }

        @Override
        public String getId()
        {
            return EditSubTaskIssueTypes.this.getId();
        }

        @Override
        public Long getAvatarId()
        {
            return EditSubTaskIssueTypes.this.getAvatarId();
        }

        @Override
        public String getIconUrlContent()
        {
            final String iconurl = EditSubTaskIssueTypes.this.getIconurl();
            final String contextPath = getHttpRequest().getContextPath();

            return URLUtil.addContextPathToURLIfAbsent(contextPath, iconurl);
        }
    }

}
