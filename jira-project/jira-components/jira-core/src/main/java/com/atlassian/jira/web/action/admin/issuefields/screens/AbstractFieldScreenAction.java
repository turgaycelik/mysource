package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.opensymphony.util.TextUtils;

import java.util.Collection;

/**
 * Webwork action for managing screens.
 */
public class AbstractFieldScreenAction extends JiraWebActionSupport
{
    protected final FieldScreenManager fieldScreenManager;

    private Long id;
    protected FieldScreen fieldScreen;

    private String fieldScreenName;
    private String fieldScreenDescription;

    public AbstractFieldScreenAction(FieldScreenManager fieldScreenManager)
    {
        this.fieldScreenManager = fieldScreenManager;
    }

    public Collection<FieldScreen> getFieldScreens()
    {
        return fieldScreenManager.getFieldScreens();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public FieldScreen getFieldScreen()
    {
        if (fieldScreen == null && getId() != null)
        {
            fieldScreen = fieldScreenManager.getFieldScreen(getId());
        }

        return fieldScreen;
    }

    public String getFieldScreenName()
    {
        return fieldScreenName;
    }

    public void setFieldScreenName(String fieldScreenName)
    {
        this.fieldScreenName = fieldScreenName;
    }

    public String getFieldScreenDescription()
    {
        return fieldScreenDescription;
    }

    public void setFieldScreenDescription(String fieldScreenDescription)
    {
        this.fieldScreenDescription = fieldScreenDescription;
    }

    protected void validateId()
    {
        if (getId() == null)
        {
            addErrorMessage(getText("admin.errors.id.cannot.be.null"));
        }
        else if (fieldScreenManager.getFieldScreen(getId()) == null)
        {
            addErrorMessage(getText("admin.errors.screens.screen.with.id.does.not.exist"));
        }
    }

    protected void validateScreenName()
    {
        if (!TextUtils.stringSet(getFieldScreenName()))
        {
            addError("fieldScreenName", getText("admin.common.errors.validname"));
        }
        else if (getFieldScreen() == null || !getFieldScreenName().equals(getFieldScreen().getName()))
        {
            for (final FieldScreen fieldScreen : getFieldScreens())
            {
                if (getFieldScreenName().equals(fieldScreen.getName()) && (getFieldScreen() == null || !fieldScreen.getId().equals(getId())))
                {
                    addError("fieldScreenName", getText("admin.errors.screens.duplicate.screen.name"));
                }
            }
        }
    }

    protected String redirectToView()
    {
        return returnCompleteWithInlineRedirect("ViewFieldScreens.jspa");
    }

    protected FieldScreenManager getFieldScreenManager()
    {
        return fieldScreenManager;
    }
}
