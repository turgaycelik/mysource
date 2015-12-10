/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.browser;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.VersionProxy;
import com.atlassian.jira.project.util.ReleaseNoteManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ReleaseNote extends JiraWebActionSupport
{
    private long projectId;
    private String styleName;
    private String version;

    private final ProjectManager projectManager;
    private final ReleaseNoteManager releaseNoteManager;
    private final ConstantsManager constantsManager;
    private final VersionManager versionManager;

    public ReleaseNote(ProjectManager projectManager, ReleaseNoteManager releaseNoteManager, ConstantsManager constantsManager, VersionManager versionManager)
    {
        this.projectManager = projectManager;
        this.releaseNoteManager = releaseNoteManager;
        this.constantsManager = constantsManager;
        this.versionManager = versionManager;
    }

    /**
     * Gets all unarchived versions within this project
     * @return All unarchived versions within this project
     * @throws GenericEntityException
     */
    public Collection getVersions() throws GenericEntityException
    {
        Project project = projectManager.getProjectObj(projectId);

        if(project == null)
        {
            return Collections.emptyList();
        }

        List unreleased = new ArrayList();
        Iterator unreleasedIter = versionManager.getVersionsUnreleased(project.getId(), false).iterator();
        if (unreleasedIter.hasNext())
        {
            unreleased.add(new VersionProxy(-2, getText("common.filters.unreleasedversions")));
            while (unreleasedIter.hasNext())
            {
                unreleased.add(new VersionProxy((Version) unreleasedIter.next()));
            }
        }

        //reverse the order of the releasedIter versions.
        List released = new ArrayList();
        List<Version> releasedIter = new ArrayList<Version>(versionManager.getVersionsReleased(project.getId(), false));
        if (!releasedIter.isEmpty())
        {
            released.add(new VersionProxy(-3, getText("common.filters.releasedversions")));
            Collections.reverse(releasedIter);
            for (final Version aReleasedIter : releasedIter)
            {
                released.add(new VersionProxy(aReleasedIter));
            }
        }

        List versions = new ArrayList();
        versions.addAll(unreleased);
        versions.addAll(released);

        return versions;
    }

    public Collection getStyleNames()
    {
        return releaseNoteManager.getStyles().keySet();
    }

    public String getReleaseNote()
    {
        return releaseNoteManager.getReleaseNote(this, getStyleName(), getSelectedVersion(), getLoggedInUser(), getProjectGV());
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    /**
     * Set project by its user-visible name ("Test Project" etc). Equivalent to {@link #setProjectId}.
     */
    public void setProjectName(String projectName)
    {
        GenericValue project = projectManager.getProjectByName(projectName);
        if (project != null) this.projectId = project.getLong("id");
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    /**
     * Set version by its user-visible name ("1.0" etc). Equivalent to {@link #setVersion}.
     */
    public void setVersionName(String versionName)
    {
        Version versionObj = versionManager.getVersion(getProjectId(), versionName);
        if (versionObj != null) this.version = ""+versionObj.getId();
    }

    public void setStyleName(String styleName)
    {
        this.styleName = styleName;
    }

    public String getVersion()
    {
        return version;
    }

    public String getStyleName()
    {
        return styleName;
    }

    public long getProjectId()
    {
        return projectId;
    }

    protected void doValidation()
    {
        if (!TextUtils.stringSet(version) || "-1".equals(version) || "-2".equals(version) || "-3".equals(version))
        {
            addError("version", getText("releasenotes.version.select"));
        }
        else if (getProjectGV() == null)
        {
            addError("version", getText("releasenotes.project.error"));
        }
        else if (getSelectedVersion() == null)
        {
            addError("version", getText("releasenotes.version.error"));
        }
    }

    protected String doExecute() throws GenericEntityException
    {
        return getResult();
    }

    public String doConfigure() throws GenericEntityException
    {
        if (getProjectGV() == null || getVersions().isEmpty() || getStyleNames().isEmpty())
        {
            return ERROR;
        }
        else
        {
            return SUCCESS;
        }
    }

    private Version getSelectedVersion()
    {
        try
        {
            return versionManager.getVersion(Long.parseLong(getVersion()));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private GenericValue getProjectGV()
    {
        return projectManager.getProject(getProjectId());
    }

    public Collection getIssueTypes() throws GenericEntityException
    {
        return constantsManager.getIssueTypes();
    }
}
