package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.atlassian.webresource.api.assembler.PageBuilderService;

/**
 * Page for the ADMIN to do the read/update of {@link com.atlassian.jira.license.LicenseRole}s.
 *
 * @since v6.3
 */
@WebSudoRequired
public class LicenseRoles extends JiraWebActionSupport
{
    private final PageBuilderService pageBuilder;

    public LicenseRoles(PageBuilderService pageBuilder)
    {
        this.pageBuilder = pageBuilder;
    }

    @Override
    protected String doExecute()
    {
        pageBuilder.assembler().resources().requireWebResource("jira.webresources:license-roles");
        return SUCCESS;
    }
}
