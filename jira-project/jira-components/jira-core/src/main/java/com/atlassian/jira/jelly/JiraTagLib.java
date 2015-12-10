/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.atlassian.jira.jelly.tag.AppProps;
import com.atlassian.jira.jelly.tag.LoadComponent;
import com.atlassian.jira.jelly.tag.LoadManager;
import com.atlassian.jira.jelly.tag.LoadProject;
import com.atlassian.jira.jelly.tag.RunSearchRequest;
import com.atlassian.jira.jelly.tag.StringContains;
import com.atlassian.jira.jelly.tag.admin.AddCustomFieldSelectValue;
import com.atlassian.jira.jelly.tag.admin.AddFieldToScreen;
import com.atlassian.jira.jelly.tag.admin.AddPermission;
import com.atlassian.jira.jelly.tag.admin.AddUserToGroup;
import com.atlassian.jira.jelly.tag.admin.CreateCustomField;
import com.atlassian.jira.jelly.tag.admin.CreateGroup;
import com.atlassian.jira.jelly.tag.admin.CreatePermissionScheme;
import com.atlassian.jira.jelly.tag.admin.CreateUser;
import com.atlassian.jira.jelly.tag.admin.GetAssociatedSchemes;
import com.atlassian.jira.jelly.tag.admin.RemoveGroup;
import com.atlassian.jira.jelly.tag.admin.RemovePermissionScheme;
import com.atlassian.jira.jelly.tag.admin.RemoveUser;
import com.atlassian.jira.jelly.tag.admin.SelectProjectScheme;
import com.atlassian.jira.jelly.tag.issue.AddComment;
import com.atlassian.jira.jelly.tag.issue.AddCustomFieldValue;
import com.atlassian.jira.jelly.tag.issue.AssignIssue;
import com.atlassian.jira.jelly.tag.issue.AttachFile;
import com.atlassian.jira.jelly.tag.issue.ChangeIssue;
import com.atlassian.jira.jelly.tag.issue.LinkIssue;
import com.atlassian.jira.jelly.tag.issue.TransitionWorkflow;
import com.atlassian.jira.jelly.tag.login.Login;
import com.atlassian.jira.jelly.tag.project.AddComponent;
import com.atlassian.jira.jelly.tag.project.AddVersion;
import com.atlassian.jira.jelly.tag.project.CreateProject;
import com.atlassian.jira.jelly.tag.project.RemoveProject;
import com.atlassian.jira.jelly.tag.project.enterprise.SelectComponentAssignees;
import com.atlassian.jira.jelly.tag.projectroles.AddActorsToProjectRole;
import com.atlassian.jira.jelly.tag.projectroles.AddDefaultActorsToProjectRole;
import com.atlassian.jira.jelly.tag.projectroles.CreateProjectRole;
import com.atlassian.jira.jelly.tag.projectroles.DeleteProjectRole;
import com.atlassian.jira.jelly.tag.projectroles.GetDefaultRoleActors;
import com.atlassian.jira.jelly.tag.projectroles.GetProjectRole;
import com.atlassian.jira.jelly.tag.projectroles.GetProjectRoleActors;
import com.atlassian.jira.jelly.tag.projectroles.GetProjectRoles;
import com.atlassian.jira.jelly.tag.projectroles.IsProjectRoleNameUnique;
import com.atlassian.jira.jelly.tag.projectroles.RemoveActorsFromProjectRole;
import com.atlassian.jira.jelly.tag.projectroles.RemoveDefaultActorsFromProjectRole;
import com.atlassian.jira.jelly.tag.projectroles.UpdateProjectRole;
import com.atlassian.jira.jelly.tag.util.CsvTag;
import com.atlassian.jira.util.JiraUtils;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.impl.TagFactory;
import org.apache.commons.jelly.impl.TagScript;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JiraTagLib extends TagLibrary// implements TagFactory
{
    private static final transient Logger log = Logger.getLogger(JiraTagLib.class);

    protected static final Map<String, Class<? extends TagSupport>> TAGS;

    static
    {
        Map<String, Class<? extends TagSupport>> tags = new HashMap<String, Class<? extends TagSupport>>();
        tags.put("TransitionWorkflow", TransitionWorkflow.class);
        tags.put("CreateCustomField", CreateCustomField.class);
        tags.put("Login", Login.class);
        tags.put("SelectComponentAssignees", SelectComponentAssignees.class);
        tags.put("CreateIssue", com.atlassian.jira.jelly.tag.issue.enterprise.CreateIssue.class);
        TAGS = Collections.unmodifiableMap(tags);
    }

    protected MyTagFactory jiraTagFactory = new MyTagFactory();

    public JiraTagLib()
    {
        //Entity Tags
        registerTag("AddCustomFieldSelectValue", AddCustomFieldSelectValue.class);
        registerTag("AddCustomFieldValue", AddCustomFieldValue.class);
        //registerTag("AddComment", AddComment.class);
        registerTag("AddComment", AddComment.class);
        registerTag("AddComponent", AddComponent.class);
        registerTagFactory("CreateCustomField", jiraTagFactory);
        registerTag("CreateUser", CreateUser.class);
        registerTag("AddPermission", AddPermission.class);
        registerTag("AddUserToGroup", AddUserToGroup.class);
        registerTag("AddVersion", AddVersion.class);
        registerTag("CreateGroup", CreateGroup.class);
        registerTag("AddFieldToScreen", AddFieldToScreen.class);

        registerTagFactory("CreateIssue", jiraTagFactory);
        registerTag("CreatePermissionScheme", CreatePermissionScheme.class);
        registerTag("CreateProject", CreateProject.class);
        registerTagFactory("Login", jiraTagFactory);
        registerTag("RemoveGroup", RemoveGroup.class);
        registerTag("RemovePermissionScheme", RemovePermissionScheme.class);
        registerTag("RemoveProject", RemoveProject.class);
        registerTag("RemoveUser", RemoveUser.class);
        registerTag("AssignIssue", AssignIssue.class);

        registerTagFactory("TransitionWorkflow", jiraTagFactory);

        registerTag("SelectProjectScheme", SelectProjectScheme.class);
        registerTag("ChangeIssue", ChangeIssue.class);
        registerTag("LinkIssue", LinkIssue.class);
        registerTag("AppProps", AppProps.class);
        registerTag("AttachFile", AttachFile.class);

        //Function Tags
        registerTag("LoadManager", LoadManager.class);
        registerTag("LoadComponent", LoadComponent.class);
        registerTag("LoadProject", LoadProject.class);
        registerTag("RunSearchRequest", RunSearchRequest.class);
        registerTag("StringContains", StringContains.class);

        // Role Service Tags
        registerTag("GetProjectRoles", GetProjectRoles.class);
        registerTag("GetProjectRole", GetProjectRole.class);
        registerTag("CreateProjectRole", CreateProjectRole.class);
        registerTag("AddActorsToProjectRole", AddActorsToProjectRole.class);
        registerTag("AddDefaultActorsToProjectRole", AddDefaultActorsToProjectRole.class);
        registerTag("RemoveActorsFromProjectRole", RemoveActorsFromProjectRole.class);
        registerTag("RemoveDefaultActorsFromProjectRole", RemoveDefaultActorsFromProjectRole.class);
        registerTag("DeleteProjectRole", DeleteProjectRole.class);
        registerTag("GetProjectRoleActors", GetProjectRoleActors.class);
        registerTag("IsProjectRoleNameUnique", IsProjectRoleNameUnique.class);
        registerTag("GetDefaultRoleActors", GetDefaultRoleActors.class);
        registerTag("UpdateProjectRole", UpdateProjectRole.class);

        // Admin Tags
        registerTag("GetAssociatedSchemes", GetAssociatedSchemes.class);

        //Util Tags
        registerTag("csv", CsvTag.class);

    }

    private static class MyTagFactory implements TagFactory
    {
        public Tag createTag(String s, Attributes attributes) throws JellyException
        {
            Class<? extends TagSupport> tagClass = TAGS.get(s);
            return JiraUtils.loadComponent(tagClass);
        }
    }

    public TagScript createTagScript(String name, Attributes attributes) throws JellyException
    {
        log.debug("JiraTagLib.createTagScript");

        TagScript tagScript = super.createTagScript(name, attributes);
        if (tagScript == null)
        {
            return new TagScript();
        }
        return tagScript;
    }

    public Tag createTag(String name, Attributes attributes) throws JellyException
    {
        log.debug("JiraTagLib.createTag");

        NotImplementedTag notimpl = new NotImplementedTag();
        notimpl.setAttribute("name", name);
        return notimpl;
    }

    public Tag createTag() throws Exception
    {
        return new NotImplementedTag();
    }
}
