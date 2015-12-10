package com.atlassian.jira.web.action.admin.issuefields.screens.schemes.issuetype;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.issuefields.screens.enterprise.ConfigureIssueTypeScreenScheme;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Responsible for rendering the user interface to add a new issue type to screen scheme entry to an existing
 * issue type screen scheme.
 *
 * @since v5.0.2
 */
@WebSudoRequired
public class AddIssueTypeScreenScreenSchemeAssociation extends JiraWebActionSupport
{
    private Long id;
    private String issueTypeId;
    private Long fieldScreenSchemeId;

    private IssueTypeScreenScheme issueTypeScreenScheme;
    private List<IssueType> addableIssueTypes;
    private Collection<IssueType> allRelevantIssueTypeObjects;
    private Collection<FieldScreenScheme> fieldScreenSchemes;

    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private final ConstantsManager constantsManager;
    private final SubTaskManager subTaskManager;

    public AddIssueTypeScreenScreenSchemeAssociation(final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager,
            final FieldScreenSchemeManager fieldScreenSchemeManager, final ConstantsManager constantsManager,
            final SubTaskManager subTaskManager)
    {
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.constantsManager = constantsManager;
        this.subTaskManager = subTaskManager;
    }

    /**
     * Renders the dialog to input the values for a new issue type to field configuration entry.
     *
     * @return {@link Action#INPUT}
     * @throws Exception
     */
    @Override
    public String doDefault() throws Exception
    {
        return Action.INPUT;
    }

    /**
     * Handles the request to create a new issue type to screen scheme entry as submitted from the dialog.
     *
     * On success, we redirect to the configure issue type screen scheme page.
     *
     * On error, we return the user to the dialog.
     *
     * @return redirects to {@link ConfigureIssueTypeScreenScheme} on success, {@link Action#ERROR}
     * if there are validation errors.
     */
    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (getIssueTypeId() == null)
        {
            addError("issueTypeId", getText("admin.errors.screens.specify.issue.type"));
            return Action.ERROR;
        }
        else if (getFieldScreenSchemeId() == null)
        {
            addError("fieldScreenSchemeId", getText("admin.errors.screens.please.specify.a.screen.name"));
            return Action.ERROR;
        }

        final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity =
                new IssueTypeScreenSchemeEntityImpl
                        (
                                issueTypeScreenSchemeManager, (GenericValue) null,
                                fieldScreenSchemeManager, constantsManager
                        );

        issueTypeScreenSchemeEntity.setIssueTypeId(getIssueTypeId());
        issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenSchemeManager.getFieldScreenScheme(getFieldScreenSchemeId()));
        getIssueTypeScreenScheme().addEntity(issueTypeScreenSchemeEntity);

        return returnComplete("ConfigureIssueTypeScreenScheme.jspa?id=" + getId());
    }

    public List<IssueType> getAddableIssueTypes()
    {
        if (addableIssueTypes == null)
        {
            addableIssueTypes = new LinkedList<IssueType>(getAllRelevantIssueTypeObjects());
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

    public Collection<FieldScreenScheme> getFieldScreenSchemes()
    {
        if (fieldScreenSchemes == null)
        {
            fieldScreenSchemes = fieldScreenSchemeManager.getFieldScreenSchemes();
        }
        return fieldScreenSchemes;
    }

    public String getIssueTypeId()
    {
        return issueTypeId;
    }

    public void setIssueTypeId(final String issueTypeId)
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

    public IssueTypeScreenScheme getIssueTypeScreenScheme()
    {
        if (issueTypeScreenScheme == null)
        {
            issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(getId());
        }
        return issueTypeScreenScheme;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }
}
