package com.atlassian.jira.web.action.admin.issuefields.configuration.schemes;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntityImpl;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.issuefields.enterprise.FieldLayoutSchemeHelper;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import org.ofbiz.core.entity.GenericValue;

/**
 * Responsible for rendering the user interface to add a new issue type to field configuration entry to an existing
 * field configuration scheme.
 *
 * @since v5.0.1
 */
@WebSudoRequired
public class AddIssueTypeToFieldConfigurationAssociation extends JiraWebActionSupport
{
    private String issueTypeId;
    private Long id;
    private Long fieldConfigurationId;

    private FieldLayoutScheme fieldLayoutScheme;
    private List<IssueType> addableIssueTypes;
    private Collection<IssueType> allRelevantIssueTypeObjects;
    private List<EditableFieldLayout> editableFieldLayouts;

    private final FieldLayoutManager fieldLayoutManager;
    private final FieldLayoutSchemeHelper fieldLayoutSchemeHelper;
    private final ReindexMessageManager reindexMessageManager;
    private final SubTaskManager subTaskManager;

    public AddIssueTypeToFieldConfigurationAssociation(final FieldLayoutManager fieldLayoutManager,
            final FieldLayoutSchemeHelper fieldLayoutSchemeHelper, final ReindexMessageManager reindexMessageManager,
            final SubTaskManager subTaskManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.fieldLayoutSchemeHelper = fieldLayoutSchemeHelper;
        this.reindexMessageManager = reindexMessageManager;
        this.subTaskManager = subTaskManager;
    }

    /**
     * Renders the dialog to input the values for a new issue type to field configuration entry.
     *
     * @return {@link webwork.action.Action#INPUT}
     * @throws Exception
     */
    @Override
    public String doDefault() throws Exception
    {
        return INPUT;
    }

    @Override
    protected void doValidation()
    {
        if (getId() == null)
        {
            addErrorMessage(getText("admin.errors.id.required"));
        }
    }

    /**
     * Handles the request to create a new issue type to field configuration entry as submitted from the dialog.
     *
     * On success, we redirect to the configure field configuration scheme page.
     *
     * On error, we return the user to the dialog.
     *
     * @return redirects to {@link com.atlassian.jira.web.action.admin.issuefields.enterprise.ConfigureFieldLayoutScheme} on sucess,
     * {@link webwork.action.Action#ERROR} if there are validation errors.
     */
    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (getIssueTypeId() == null)
        {
            addError("issueTypeId", getText("admin.errors.fieldlayoutscheme.no.issue.type"));
        }

        if (!invalidInput())
        {
            fieldLayoutManager.createFieldLayoutSchemeEntity(getFieldLayoutScheme(), getIssueTypeId(), getFieldConfigurationId());

            if (isReindexRequired())
            {
                reindexMessageManager.pushMessage(getLoggedInUser(), "admin.notifications.task.field.configuration");
            }
            return returnComplete("ConfigureFieldLayoutScheme.jspa?id=" + getId());
        }

        return getResult();
    }

    private boolean isReindexRequired()
    {
        // need to compare the default for unmapped issue types and the new field configuration
        final Long unmappedLayoutId = getFieldLayoutScheme().getFieldLayoutId(null);
        return fieldLayoutSchemeHelper.
                doesChangingFieldLayoutAssociationRequireMessage
                        (
                                getLoggedInUser(), getFieldLayoutScheme(), unmappedLayoutId, getFieldConfigurationId()
                        );
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


    private Collection<IssueType> getAllRelevantIssueTypeObjects()
    {
        if (allRelevantIssueTypeObjects == null)
        {
            if (subTaskManager.isSubTasksEnabled())
            {
                allRelevantIssueTypeObjects = getConstantsManager().getAllIssueTypeObjects();
            }
            else
            {
                allRelevantIssueTypeObjects = getConstantsManager().getRegularIssueTypeObjects();
            }
        }
        return allRelevantIssueTypeObjects;
    }

    public String getIssueTypeId()
    {
        return issueTypeId;
    }

    public void setIssueTypeId(final String issueTypeId)
    {
        this.issueTypeId = issueTypeId;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
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

    public Long getFieldConfigurationId()
    {
        return fieldConfigurationId;
    }

    public void setFieldConfigurationId(final Long fieldConfigurationId)
    {
        this.fieldConfigurationId = fieldConfigurationId;
    }
}
