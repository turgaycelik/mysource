package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.HackyRendererType;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * The action that controls the confirmation screen.
 */
@WebSudoRequired
public class EditFieldLayoutItemRendererConfirmation extends JiraWebActionSupport
{
    private boolean confirmed = false;
    private String selectedRendererType;
    private String fieldName;
    private Integer rendererEdit;
    private Long id;
    private RendererManager rendererManager;

    public EditFieldLayoutItemRendererConfirmation(RendererManager rendererManager)
    {
        this.rendererManager = rendererManager;
    }

    public String doDefault()
    {
        return SUCCESS;
    }

    public String getCancelUrl()
    {
        if(getId() != null)
        {
            return "ConfigureFieldLayout!default.jspa?id=" + getId();
        }
        else
        {
            return "ViewIssueFields.jspa";
        }
    }

    public String getSelectedRendererType()
    {
        return selectedRendererType;
    }

    public void setSelectedRendererType(String selectedRendererType)
    {
        this.selectedRendererType = selectedRendererType;
    }

    public Integer getRendererEdit()
    {
        return rendererEdit;
    }

    public void setRendererEdit(Integer rendererEdit)
    {
        this.rendererEdit = rendererEdit;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed)
    {
        this.confirmed = confirmed;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getRendererDisplayName(String rendererType)
    {
        HackyRendererType hackyRendererType = HackyRendererType.fromKey(rendererType);
        if (hackyRendererType != null)
        {
            return getText(hackyRendererType.getDisplayNameI18nKey());
        }
        else
        {
            return rendererManager.getRendererForType(rendererType).getDescriptor().getName();
        }
    }

}
