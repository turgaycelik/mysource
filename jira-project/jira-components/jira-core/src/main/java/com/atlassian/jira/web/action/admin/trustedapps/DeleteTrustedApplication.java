package com.atlassian.jira.web.action.admin.trustedapps;

import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationService;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Delete confirmation for a trusted application
 *
 * @since v3.12
 */
@WebSudoRequired
public class DeleteTrustedApplication extends AbstractTrustedApplicationAction
{
    public DeleteTrustedApplication(TrustedApplicationService service)
    {
        super(service);
    }

    public void doExecuteAction()
    {
        service.delete(getJiraServiceContext(), getId());
    }
}