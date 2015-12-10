/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuetypes;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.IssueTypeImpl;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.admin.constants.AbstractEditConstant;
import com.atlassian.jira.web.action.admin.issuetypes.events.IssueTypeUpdatedEvent;
import com.atlassian.jira.web.action.issue.URLUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import org.ofbiz.core.entity.GenericValue;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.length;

@WebSudoRequired
public class EditIssueType extends AbstractEditConstant implements IssueTypeTemplateProperties
{
    public class IssueTypeViewData implements IssueTypeTemplateProperties.IssueTypeViewData
    {
        public String getName()
        {
            return EditIssueType.this.getName();
        }

        public String getDescription()
        {
            return EditIssueType.this.getDescription();
        }

        public String getId()
        {
            return EditIssueType.this.getId();
        }

        public Long getAvatarId() { return EditIssueType.this.getAvatarId(); }

        public String getIconUrlContent()
        {
            final String iconurl = EditIssueType.this.getIconurl();
            final String contextPath = getHttpRequest().getContextPath();

            return URLUtil.addContextPathToURLIfAbsent(contextPath, iconurl);
        }
    }

    private Long avatarId;

    @Override
    public Long getAvatarId()
    {
        return avatarId;
    }

    public void setAvatarId(final Long avatarId)
    {
        this.avatarId = avatarId;
    }

    private final IssueTypeManager issueTypeManager;
    private final EventPublisher eventPublisher;

    public EditIssueType(IssueTypeManager issueTypeManager, EventPublisher eventPublisher)
    {
        this.issueTypeManager = issueTypeManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String doDefault() throws Exception
    {
        final GenericValue constant = getConstant();
        if (null!=constant)
        {
            this.avatarId = constant.getLong(IssueTypeImpl.AVATAR_FIELD);
        }

        return super.doDefault();
    }

    protected void doValidation()
    {
        if (avatarId==null && isBlank(getIconurl()))
        {
            addError("iconurl", getText("admin.errors.issuetypes.must.specify.url"));
        }

        if (length(name) > 60)
        {
            addError("name", getText("admin.errors.issuetypes.name.must.not.exceed.max.length"));
        }

        super.doValidation();
    }

    protected String getConstantEntityName()
    {
        return "IssueType";
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.issuetype.lowercase");
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
        return "ViewIssueTypes.jspa";
    }

    protected Collection<GenericValue> getConstants()
    {
        return getConstantsManager().getIssueTypes();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshIssueTypes();
    }

    @Override
    protected String createDuplicateMessage()
    {
        return getText("admin.errors.issue.type.with.this.name.already.exists");
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
        return "EditIssueType.jspa";
    }

    @Override
    @ActionViewData
    public String getCancelAction()
    {
        return "ViewIssueTypes.jspa";
    }

    @Override
    @ActionViewData
    public String getActiveTab()
    {
        return "issue_types";
    }

    @Override
    @ActionViewData
    public String getToken()
    {
        return super.getXsrfToken();
    }

    @Override
    @ActionViewData(key = "errors")
    public Map<String, Object> getWrappedErrorsForView() {
        return MapBuilder.<String,Object>newBuilder()
                .add("errors", super.getErrors())
                .toMap();
    }

    @Override
    @ActionViewData(key = "errorMessages")
    public Collection<String> getErrorMessages()
    {
        return super.getErrorMessages();
    }

    @Override
    @ActionViewData
    public String getDefaultAvatarId() {
        return getApplicationProperties().getString(APKeys.JIRA_DEFAULT_ISSUETYPE_AVATAR_ID);
    }

    @Override
    @ActionViewData
    public String getEditTitleTextId()
    {
        return getI18nHelper().getText("admin.issuesettings.issuetypes.edit.issue.type");
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        IssueType issueType = issueTypeManager.getIssueType(id);
        issueTypeManager.updateIssueType(issueType, name, description, avatarId);
        if (getHasErrorMessages())
        {
            return ERROR;
        }
        else
        {
            eventPublisher.publish(new IssueTypeUpdatedEvent());
            return getRedirect(getRedirectPage());
        }
    }
}
