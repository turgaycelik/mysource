package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.fields.ProjectFieldLayoutSchemeHelper;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
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

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@WebSudoRequired
public class ConfigureFieldLayoutScheme extends JiraWebActionSupport
{
    private Long id;
    private String issueTypeId;
    private Long fieldConfigurationId;

    private final FieldLayoutManager fieldLayoutManager;
    private final ConstantsManager constantsManager;
    private final SubTaskManager subTaskManager;
    private final ReindexMessageManager reindexMessageManager;
    private final FieldLayoutSchemeHelper fieldLayoutSchemeHelper;
    private final ProjectFieldLayoutSchemeHelper helper;

    private FieldLayoutScheme fieldLayoutScheme;
    private List<IssueType> addableIssueTypes;
    private List<EditableFieldLayout> editableFieldLayouts;
    private Collection<GenericValue> allRelevantIssueTypes;
    private Collection<IssueType> allRelevantIssueTypeObjects;
    private String edited;
    private List<Project> projects;

    public ConfigureFieldLayoutScheme(final FieldLayoutManager fieldLayoutManager,
            final ConstantsManager constantsManager, final SubTaskManager subTaskManager,
            final ReindexMessageManager reindexMessageManager, final FieldLayoutSchemeHelper fieldLayoutSchemeHelper,
            final ProjectFieldLayoutSchemeHelper helper)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.constantsManager = constantsManager;
        this.subTaskManager = subTaskManager;
        this.reindexMessageManager = notNull("reindexMessageManager", reindexMessageManager);
        this.fieldLayoutSchemeHelper = notNull("fieldLayoutSchemeHelper", fieldLayoutSchemeHelper);
        this.helper = helper;
    }

    protected void doValidation()
    {
        if (getId() == null)
        {
            addErrorMessage(getText("admin.errors.id.required"));
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

    public void setId(final Long id)
    {
        this.id = id;
    }

    public Collection<FieldLayoutSchemeEntity> getFieldLayoutSchemeItems()
    {
        return getFieldLayoutScheme().getEntities();
    }

    public FieldLayout getFieldLayout(final Long fieldLayoutId)
    {
        if (fieldLayoutId == null)
        {
            return fieldLayoutManager.getEditableDefaultFieldLayout();
        }
        else
        {
            return fieldLayoutManager.getEditableFieldLayout(fieldLayoutId);
        }
    }

    public FieldLayoutScheme getFieldLayoutScheme()
    {
        if (fieldLayoutScheme == null)
        {
            fieldLayoutScheme = fieldLayoutManager.getMutableFieldLayoutScheme(getId());
        }

        return fieldLayoutScheme;
    }

    public Collection<IssueType> getAddableIssueTypes()
    {
        if (addableIssueTypes == null)
        {
            addableIssueTypes = new LinkedList<IssueType>(getAllRelevantIssueTypeObjects());

            for (Iterator<IssueType> iterator = addableIssueTypes.iterator(); iterator.hasNext();)
            {
                final IssueType issueType = iterator.next();
                final GenericValue issueTypeGV = issueType.getGenericValue();
                if (getFieldLayoutScheme().getEntity(issueTypeGV.getString("id")) != null)
                {
                    iterator.remove();
                }
            }
        }

        return addableIssueTypes;
    }

    public Collection<EditableFieldLayout> getFieldLayouts()
    {
        if (editableFieldLayouts == null)
        {
            editableFieldLayouts = fieldLayoutManager.getEditableFieldLayouts();
        }

        return editableFieldLayouts;
    }

    public Long getFieldConfigurationId()
    {
        return fieldConfigurationId;
    }

    public void setFieldConfigurationId(final Long fieldConfigurationId)
    {
        this.fieldConfigurationId = fieldConfigurationId;
    }

    public String getIssueTypeId()
    {
        return issueTypeId;
    }

    public void setIssueTypeId(final String issueTypeId)
    {
        this.issueTypeId = issueTypeId;
    }

    public String getFieldLayoutId(final EditableFieldLayout editableFieldLayout)
    {
        // For the default field layout set no id
        if (editableFieldLayout.getType() != null)
        {
            return "";
        }
        else
        {
            return editableFieldLayout.getId().toString();
        }
    }

    @RequiresXsrfCheck
    public String doDeleteFieldLayoutSchemeEntity()
    {
        if (getIssueTypeId() == null)
        {
            addErrorMessage(getText("admin.errors.fieldlayoutscheme.cannot.delete.default"));
        }

        if (!invalidInput())
        {
            // need to compare the default for unmapped issue types and the new field configuration
            final Long unmappedLayoutId = getFieldLayoutScheme().getFieldLayoutId(null);
            final Long layoutToRemoveId = getFieldLayoutScheme().getFieldLayoutId(getIssueTypeId());
            if (fieldLayoutSchemeHelper.doesChangingFieldLayoutAssociationRequireMessage(getLoggedInUser(), getFieldLayoutScheme(), layoutToRemoveId, unmappedLayoutId))
            {
                reindexMessageManager.pushMessage(getLoggedInUser(), "admin.notifications.task.field.configuration");
            }
            getFieldLayoutScheme().removeEntity(getIssueTypeId());

            return getRedirect("ConfigureFieldLayoutScheme.jspa?id=" + getId());
        }

        return getResult();
    }

    public String doViewEditFieldLayoutSchemeEntity()
    {
        if (getId() == null)
        {
            addErrorMessage(getText("admin.errors.fieldlayoutscheme.no.issue.type"));
        }

        setFieldConfigurationId(getFieldLayoutScheme().getFieldLayoutId(getIssueTypeId()));
        return INPUT;
    }

    @RequiresXsrfCheck
    public String doEditFieldLayoutSchemeEntity()
    {
        if (getId() == null)
        {
            addErrorMessage(getText("admin.errors.fieldlayoutscheme.no.issue.type"));
        }

        final FieldLayoutSchemeEntity fieldLayoutSchemeEntity = getFieldLayoutScheme().getEntity(getIssueTypeId());

        // need to compare the previous and new field configurations
        final Long oldFieldLayoutId = fieldLayoutSchemeEntity.getFieldLayoutId();
        final Long newFieldLayoutId = getFieldConfigurationId();

        fieldLayoutSchemeEntity.setFieldLayoutId(newFieldLayoutId);
        fieldLayoutSchemeEntity.store();

        if (fieldLayoutSchemeHelper.doesChangingFieldLayoutAssociationRequireMessage(getLoggedInUser(), getFieldLayoutScheme(), oldFieldLayoutId, newFieldLayoutId))
        {
            reindexMessageManager.pushMessage(getLoggedInUser(), "admin.notifications.task.field.configuration");
        }
        return getRedirect("ConfigureFieldLayoutScheme.jspa?id=" + getId());
    }

    public String getEdited()
    {
        return edited;
    }

    public void setEdited(final String edited)
    {
        this.edited = edited;
    }

    public GenericValue getIssueType()
    {
        return constantsManager.getIssueType(getIssueTypeId());
    }

    public boolean isShouldDisplay(final FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        if (fieldLayoutSchemeEntity.getIssueTypeId() == null)
        {
            // Always display the default entry 
            return true;
        }
        else
        {
            return getAllRelevantIssueTypes().contains(fieldLayoutSchemeEntity.getIssueType());
        }
    }

    private Collection<GenericValue> getAllRelevantIssueTypes()
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

    private Collection<IssueType> getAllRelevantIssueTypeObjects()
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
            final FieldLayoutScheme fieldLayoutScheme = getFieldLayoutScheme();
            projects = helper.getProjectsForScheme(fieldLayoutScheme == null ? null : fieldLayoutScheme.getId());
        }
        return projects;
    }
}
