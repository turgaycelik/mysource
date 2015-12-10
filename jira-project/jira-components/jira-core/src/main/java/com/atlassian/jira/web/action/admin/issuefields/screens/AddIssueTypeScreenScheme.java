package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.Collection;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Responsible for rendering the user interface to add a new field configuration to a JIRA instance.
 *
 * @since v5.0.2
 */
@WebSudoRequired
public class AddIssueTypeScreenScheme extends JiraWebActionSupport
{
    private String schemeName;
    private String schemeDescription;
    private Long fieldScreenSchemeId;
    private Collection<FieldScreenScheme> fieldScreenSchemes;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;

    public AddIssueTypeScreenScheme(final FieldScreenSchemeManager fieldScreenSchemeManager,
            final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager)
    {
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
    }

    /**
     * Renders the dialog to input the values for a new issue type screen scheme.
     *
     * @return {@link webwork.action.Action#INPUT}
     * @throws Exception
     */
    @Override
    public String doDefault() throws Exception
    {
        return INPUT;
    }

    /**
     * Handles the request to create a new issue type screen scheme.
     *
     * On success, we redirect to the view issue type screen schemes page.
     *
     * On error, we return the user to the dialog.
     *
     * @return redirects to 
     * {@link com.atlassian.jira.web.action.admin.issuefields.screens.enterprise.ViewIssueTypeScreenSchemes} on success,
     * or {@link webwork.action.Action#ERROR} if there are validation errors.
     */
    @RequiresXsrfCheck
    protected String doExecute()
    {
        if (isBlank(getSchemeName()))
        {
            addError("schemeName", getText("admin.errors.add.issue.type.screen.scheme.empty.name"));
            return Action.ERROR;
        }
        else if (getFieldScreenSchemeId() == null)
        {
            addError("fieldScreenSchemeId", getText("admin.errors.screens.please.specify.a.screen.name"));
            return Action.ERROR;
        }
        else
        {
            for (final IssueTypeScreenScheme issueTypeScreenScheme : issueTypeScreenSchemeManager.getIssueTypeScreenSchemes())
            {
                if (getSchemeName().equals(issueTypeScreenScheme.getName()))
                {
                    addError("schemeName", getText("admin.errors.screens.duplicate.screen.scheme.name"));
                    return Action.ERROR;
                }
            }
        }

        final IssueTypeScreenScheme issueTypeScreenScheme = new IssueTypeScreenSchemeImpl(issueTypeScreenSchemeManager, null);
        issueTypeScreenScheme.setName(getSchemeName());
        issueTypeScreenScheme.setDescription(getSchemeDescription());
        issueTypeScreenScheme.store();
        IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity =
                new IssueTypeScreenSchemeEntityImpl
                        (
                                issueTypeScreenSchemeManager, (GenericValue) null, fieldScreenSchemeManager, getConstantsManager()
                        );
        issueTypeScreenSchemeEntity.setIssueTypeId(null);
        issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenSchemeManager.getFieldScreenScheme(getFieldScreenSchemeId()));
        issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);
        return returnCompleteWithInlineRedirect(format("ConfigureIssueTypeScreenScheme.jspa?id=%d", issueTypeScreenScheme.getId()));
    }

    public String getSchemeName()
    {
        return schemeName;
    }

    public void setSchemeName(String schemeName)
    {
        this.schemeName = schemeName;
    }

    public String getSchemeDescription()
    {
        return schemeDescription;
    }

    public void setSchemeDescription(String schemeDescription)
    {
        this.schemeDescription = schemeDescription;
    }

    public Long getFieldScreenSchemeId()
    {
        return fieldScreenSchemeId;
    }

    public void setFieldScreenSchemeId(Long fieldScreenSchemeId)
    {
        this.fieldScreenSchemeId = fieldScreenSchemeId;
    }

    public Collection<FieldScreenScheme> getFieldScreenSchemes()
    {
        if (fieldScreenSchemes == null)
        {
            fieldScreenSchemes = fieldScreenSchemeManager.getFieldScreenSchemes();
        }
        return fieldScreenSchemes;
    }
}
