package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelPermission;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.security.ProjectIssueSecuritySchemeHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

@SuppressWarnings ("UnusedDeclaration")
@WebSudoRequired
public class EditIssueSecurities extends SchemeAwareIssueSecurityAction
{

    private String name;
    private String description;
    private Long levelId;
    private List<Project> projects;

    private final ProjectIssueSecuritySchemeHelper helper;
    private final IssueSecurityLevelManager issueSecurityLevelManager;

    public EditIssueSecurities(IssueSecuritySchemeManager issueSecuritySchemeManager, SecurityTypeManager issueSecurityTypeManager,
            IssueSecurityLevelManager issueSecurityLevelManager, ProjectIssueSecuritySchemeHelper helper)
    {
        super(issueSecuritySchemeManager, issueSecurityTypeManager);
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.helper = helper;
    }

    public List<IssueSecurityLevel> getSecurityLevels()
    {
        return issueSecurityLevelManager.getIssueSecurityLevels(getSchemeId());
    }

    public List<IssueSecurityLevelPermission> getSecurities(IssueSecurityLevel issueSecurityLevel)
    {
        return issueSecuritySchemeManager.getPermissionsBySecurityLevel(issueSecurityLevel.getId());
    }

    @RequiresXsrfCheck
    public String doAddLevel() throws Exception
    {
        if (name == null || "".equals(name.trim()))
        {
            addError("name", getText("admin.errors.specify.name.for.security"));
        }

        if (ComponentAccessor.getOfBizDelegator().findByAnd("SchemeIssueSecurityLevels", EasyMap.build("scheme", getSchemeId(), "name", name.trim())).size() > 0)
        {
            addError("name", getText("admin.errors.security.level.with.name.already.exists"));
        }

        if (getErrors().isEmpty())
        {
            issueSecurityLevelManager.createIssueSecurityLevel(getSchemeId(), name, description);
        }

        return getRedirect(getRedirectURL());
    }

    @RequiresXsrfCheck
    public String doMakeDefaultLevel() throws Exception
    {
        GenericValue scheme = getScheme();

        if (scheme != null)
        {
            if ((new Long(-1).equals(levelId))) // -1 sets default to "none"
            {
                scheme.set("defaultlevel", null);
            }
            else
            {
                scheme.set("defaultlevel", levelId);
            }
            issueSecuritySchemeManager.updateScheme(scheme);
        }
        return getRedirect(getRedirectURL());
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Long getLevelId()
    {
        return levelId;
    }

    public void setLevelId(Long levelId)
    {
        this.levelId = levelId;
    }

    public String getRedirectURL()
    {
        return "EditIssueSecurities!default.jspa?schemeId=" + getSchemeId();
    }

    public List<Project> getUsedIn()
    {
        if (projects == null)
        {
            final Scheme issueSecurityScheme = getSchemeObject();
            projects = helper.getSharedProjects(issueSecurityScheme);
        }
        return projects;
    }
}
