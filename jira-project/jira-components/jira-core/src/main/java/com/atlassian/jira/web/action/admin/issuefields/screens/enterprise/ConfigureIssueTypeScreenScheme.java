package com.atlassian.jira.web.action.admin.issuefields.screens.enterprise;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.ProjectIssueTypeScreenSchemeHelper;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@WebSudoRequired
public class ConfigureIssueTypeScreenScheme extends JiraWebActionSupport
{
    private Long id;
    private String issueTypeId;
    private Long fieldScreenSchemeId;

    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final ConstantsManager constantsManager;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private final SubTaskManager subTaskManager;
    private final ProjectIssueTypeScreenSchemeHelper helper;
    private List addableIssueTypes;
    private boolean edited;
    private IssueTypeScreenScheme issueTypeScreenScheme;
    private Collection fieldScreenSchemes;
    private Collection allRelevantIssueTypes;
    private Collection allRelevantIssueTypeObjects;
    private List<Project> projects;

    public ConfigureIssueTypeScreenScheme(IssueTypeScreenSchemeManager issueTypeScreenSchemeManager,
            ConstantsManager constantsManager, FieldScreenSchemeManager fieldScreenSchemeManager,
            SubTaskManager subTaskManager, final ProjectIssueTypeScreenSchemeHelper helper)
    {
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.constantsManager = constantsManager;
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.subTaskManager = subTaskManager;
        this.helper = helper;
    }

    protected void doValidation()
    {
        if (getId() == null)
        {
            addErrorMessage(getText("admin.errors.id.cannot.be.null"));
        }
    }

    protected String doExecute() throws Exception
    {
        return getResult();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Collection getIssueTypeScreenSchemeEntities()
    {
        return getIssueTypeScreenScheme().getEntities();
    }

    public IssueTypeScreenScheme getIssueTypeScreenScheme()
    {
        if (issueTypeScreenScheme == null)
        {
            issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(getId());
        }

        return issueTypeScreenScheme;
    }

    public Collection getAddableIssueTypes()
    {
        if (addableIssueTypes == null)
        {
            addableIssueTypes = new LinkedList(getAllRelevantIssueTypeObjects());
            for (Iterator iterator = addableIssueTypes.iterator(); iterator.hasNext();)
            {
                IssueType issueType = (IssueType) iterator.next();
                GenericValue issueTypeGV = issueType.getGenericValue();
                if (getIssueTypeScreenScheme().getEntity(issueTypeGV.getString("id")) != null)
                {
                    iterator.remove();
                }
            }
        }

        return addableIssueTypes;
    }

    public String getIssueTypeId()
    {
        return issueTypeId;
    }

    public void setIssueTypeId(String issueTypeId)
    {
        this.issueTypeId = issueTypeId;
    }

    public Long getFieldScreenSchemeId()
    {
        return fieldScreenSchemeId;
    }

    public void setFieldScreenSchemeId(Long fieldScreenSchemeId)
    {
        this.fieldScreenSchemeId = fieldScreenSchemeId;
    }

    @RequiresXsrfCheck
    public String doDeleteIssueTypeScreenSchemeEntity()
    {
        if (getIssueTypeId() == null)
        {
            addErrorMessage(getText("admin.errors.screens.cannot.delete.default.screen"));
        }

        if (!invalidInput())
        {
            getIssueTypeScreenScheme().removeEntity(getIssueTypeId());
            return redirectToView();
        }

        return getResult();
    }

    protected String redirectToView()
    {
        return getRedirect("ConfigureIssueTypeScreenScheme.jspa?id=" + getId());
    }

    public String doViewEditIssueTypeScreenSchemeEntity()
    {
        if (getId() == null)
        {
            addErrorMessage(getText("admin.errors.id.cannot.be.null"));            
        }

        if (!invalidInput())
        {
            IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = getIssueTypeScreenScheme().getEntity(getIssueTypeId());
            setFieldScreenSchemeId(issueTypeScreenSchemeEntity.getFieldScreenScheme().getId());
        }

        return INPUT;
    }

    @RequiresXsrfCheck
    public String doEditIssueTypeScreenSchemeEntity()
    {
        if (getFieldScreenSchemeId() == null)
        {
            addError("fieldScreenSchemeId", getText("admin.errors.screens.please.select.screen.scheme"));
        }

        if (!invalidInput())
        {
            IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = getIssueTypeScreenScheme().getEntity(getIssueTypeId());
            issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenSchemeManager.getFieldScreenScheme(getFieldScreenSchemeId()));
            issueTypeScreenSchemeEntity.store();
            return redirectToView();
        }

        return INPUT;
    }

    private void validateForEdit()
    {
        if (getId() == null)
        {
            addErrorMessage(getText("admin.errors.id.cannot.be.null"));
        }
    }

    public boolean isEdited()
    {
        return edited;
    }

    public void setEdited(boolean edited)
    {
        this.edited = edited;
    }

    public GenericValue getIssueType()
    {
        return constantsManager.getIssueType(getIssueTypeId());
    }

    public boolean isDefault(IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        return issueTypeScreenSchemeEntity.getIssueTypeId() == null;
    }

    public Collection getFieldScreenSchemes()
    {
        if (fieldScreenSchemes == null)
        {
            fieldScreenSchemes = fieldScreenSchemeManager.getFieldScreenSchemes();
        }

        return fieldScreenSchemes;
    }
    
    public boolean isShouldDisplay(IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        if (issueTypeScreenSchemeEntity.getIssueTypeId() == null)
        {
            // Always display the default entry 
            return true;
        }
        else
        {
            return getAllRelevantIssueTypes().contains(issueTypeScreenSchemeEntity.getIssueType());
        }
    }

    private Collection getAllRelevantIssueTypes()
    {
        if (allRelevantIssueTypes == null)
        {
            if (subTaskManager.isSubTasksEnabled())
            {
                allRelevantIssueTypes = constantsManager.getAllIssueTypes();
            }
            else
            {
                allRelevantIssueTypes = constantsManager.getIssueTypes();
            }

        }

        return allRelevantIssueTypes;
    }

    private Collection getAllRelevantIssueTypeObjects()
    {
        if (allRelevantIssueTypeObjects == null)
        {
            if (subTaskManager.isSubTasksEnabled())
            {
                allRelevantIssueTypeObjects = constantsManager.getAllIssueTypeObjects();
            }
            else
            {
                allRelevantIssueTypeObjects = constantsManager.getRegularIssueTypeObjects();
            }

        }

        return allRelevantIssueTypeObjects;
    }

    public List<Project> getUsedIn()
    {
        if (projects == null)
        {
            final IssueTypeScreenScheme issueTypeScreenScheme = getIssueTypeScreenScheme();
            projects = helper.getProjectsForScheme(issueTypeScreenScheme);
        }
        return projects;
    }
}
