package com.atlassian.jira.web.action.admin.trustedapps;

import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationService;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Edit a Trusted Application's details.
 *
 * @since v3.12
 */
@WebSudoRequired
public class EditTrustedApplication extends AbstractTrustedApplicationAction
{
    public EditTrustedApplication(TrustedApplicationService service)
    {
        super(service);
    }

    public String doRequest()
    {
        return INPUT;
    }

    protected void doValidation()
    {
        service.validate(getJiraServiceContext(), builder.toSimple());
    }

    protected void doExecuteAction()
    {
        service.store(getJiraServiceContext(), builder.toInfo());
    }

    public boolean isRequest()
    {
        return (getId() <= 0);
    }

    public String getApplicationId()
    {
        return builder.getApplicationId();
    }

    public void setApplicationId(String applicationId)
    {
        builder.setApplicationId(applicationId);
    }

    public long getTimeout()
    {
        return builder.getTimeout();
    }

    public void setTimeout(long timeout)
    {
        builder.setTimeout(timeout);
    }

    public String getPublicKey()
    {
        return builder.getPublicKey();
    }

    public void setPublicKey(String string)
    {
        builder.setPublicKey(string);
    }

    public void setName(String name)
    {
        builder.setName(name);
    }

    public String getIpMatch()
    {
        return builder.getIpMatch();
    }

    public void setIpMatch(String ipMatch)
    {
        builder.setIpMatch(ipMatch);
    }

    public String getUrlMatch()
    {
        return builder.getUrlMatch();
    }

    public void setUrlMatch(String urlMatch)
    {
        builder.setUrlMatch(urlMatch);
    }
}