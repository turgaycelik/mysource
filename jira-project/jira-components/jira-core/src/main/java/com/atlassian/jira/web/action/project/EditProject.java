/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.project;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.index.ProjectReindexService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.plugin.profile.DarkFeatures;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.renderer.ProjectDescriptionRenderer;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserManager;
import webwork.action.ActionContext;

public class EditProject extends ViewProject
{
    private final ProjectService projectService;
    private AvatarManager avatarManager;
    private final UserManager userManager;
    private final ProjectDescriptionRenderer projectDescriptionRenderer;
    private final UserKeyService userKeyService;
    private final DarkFeatures darkFeatures;
    private final ProjectReindexService projectReindexService;
    private String originalKey;
    private boolean keyEdited;

    public EditProject(ProjectService projectService, AvatarManager avatarManager, UserManager userManager,
            UserKeyService userKeyService, FeatureManager featureManager, ProjectReindexService projectReindexService)
    {
        this.projectService = projectService;
        this.avatarManager = avatarManager;
        this.userManager = userManager;
        this.userKeyService = userKeyService;
        this.projectReindexService = projectReindexService;
        this.darkFeatures = featureManager.getDarkFeatures();
        projectDescriptionRenderer = ComponentAccessor.getComponentOfType(ProjectDescriptionRenderer.class);
    }

    public String doDefault() throws Exception
    {
        // check if the project exists:
        if (getProject() == null)
        {
            return handleProjectDoesNotExist();
        }
        if (!(hasProjectAdminPermission() || hasAdminPermission()))
        {
            return "securitybreach";
        }
        setName(getProject().getString("name"));
        setLead(userKeyService.getUsernameForKey(getProject().getString("lead")));
        setKeyEdited(false);
        final String key = getProject().getString("key");
        setKey(key);
        setOriginalKey(key);
        setUrl(getProject().getString("url"));
        setDescription(getProject().getString("description"));
        setAssigneeType(getProject().getLong("assigneetype"));
        setAvatarId(getProjectObject().getAvatar().getId());

        return INPUT;
    }

    private String handleProjectDoesNotExist() throws Exception
    {
        if (hasAdminPermission())
        {
            // User is admin - admit that the Project Doesn't exist because they have permission to see any project.
            // We will show the Edit Project Page, but without any values in the fields (and with an error message).
            // This is consistent with what happens if we start to edit a project, but it gets deleted before we save it.
            setName("???");
            addErrorMessage(getText("admin.errors.project.no.project.with.id"));

            return super.doDefault();
        }
        else
        {
            // User is not admin - show security breach because this isn't a Project they have permission to edit.
            return "securitybreach";
        }
    }

    protected ProjectService.UpdateProjectValidationResult getUpdateProjectValidationResult()
    {
        final Project projectObject = getProjectObject();
        if (isProjectKeyRenameAllowed() && isKeyEdited())
        {
            return projectService.validateUpdateProject(getLoggedInApplicationUser(), getProjectObject(), getName(),
                    getKey(), getDescription(), projectObject.getProjectLead(),
                    getUrl(), projectObject.getAssigneeType(), null);
        }
        else
        {
            return projectService.validateUpdateProject(getLoggedInApplicationUser(), getName(),
                    projectObject.getKey(),
                    getDescription(), projectObject.getProjectLead(), getUrl(), projectObject.getAssigneeType(), getAvatarId());
        }
    }
    protected void doValidation()
    {
        // First check that the Project still exists
        if (getProject() == null)
        {
            addErrorMessage(getText("admin.errors.project.no.project.with.id"));
            // Don't try to do any more validation.
            return;
        }

        final ProjectService.UpdateProjectValidationResult result = getUpdateProjectValidationResult();

        if (!result.isValid())
        {
            //map keyed errors to JSP field names
            mapErrorCollection(result.getErrorCollection());
        }

        if(result.isKeyChanged())
        {
            if (!projectReindexService.isReindexPossible(getProjectObject()))
            {
                addError("key", getText("admin.errors.project.key.other.reindex"));
            }
        }

        // This validation seems to be redundant now - but leave it in case we add something special to ViewProject.doValidation()
        super.doValidation();
    }

    public String getAvatarUrl()
    {
        return ActionContext.getRequest().getContextPath() + "/secure/projectavatar?pid=" + getPid() + "&size=large&avatarId="
                + getProjectObject().getAvatar().getId();
    }

    public String getProjectDescriptionEditHtml()
    {
        return projectDescriptionRenderer.getEditHtml(StringUtils.defaultString(getDescription()));
    }

    public ProjectDescriptionRenderer getProjectDescriptionRenderer() {
        return projectDescriptionRenderer;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (!(hasProjectAdminPermission() || hasAdminPermission()))
        {
            return "securitybreach";
        }

        final ProjectService.UpdateProjectValidationResult result = getUpdateProjectValidationResult();

        projectService.updateProject(result);


        final String redirectURL;
        if (result.isKeyChanged())
        {
            redirectURL = "/secure/project/IndexProject.jspa?pid=" + getProjectObject().getId();
        }
        else
        {
            redirectURL = "/plugins/servlet/project-config/" + getProjectObject().getKey() + "/summary";
        }
        if (isInlineDialogMode())
        {
            return returnCompleteWithInlineRedirect(redirectURL);
        }
        return getRedirect(redirectURL);
    }

    public boolean hasInvalidLead()
    {
        final Project projectObject = getProjectObject();

        if (projectObject == null)
        {
            return false;
        }
        else
        {
            final String leadUserName = projectObject.getLeadUserName();
            return userManager.getUserObject(leadUserName) == null;
        }
    }

    public Long getDefaultAvatar()
    {
        return avatarManager.getDefaultAvatarId(Avatar.Type.PROJECT);
    }

    public int getMaxNameLength()
    {
        return projectService.getMaximumNameLength();
    }

    public int getMaxKeyLength()
    {
        return projectService.getMaximumKeyLength();
    }

    public boolean isProjectKeyRenameAllowed() {
        return hasAdminPermission();
    }

    public void setOriginalKey(final String originalKey)
    {
        this.originalKey = originalKey;
    }

    public String getOriginalKey()
    {
        return originalKey;
    }

    public void setKeyEdited(final boolean keyEdited)
    {
        this.keyEdited = keyEdited;
    }

    public boolean isKeyEdited()
    {
        return keyEdited;
    }

    @Override
    public String getKey()
    {
        if (!isKeyEdited())
        {
            return getOriginalKey();
        }
        else
        {
            return super.getKey();
        }
    }
}
