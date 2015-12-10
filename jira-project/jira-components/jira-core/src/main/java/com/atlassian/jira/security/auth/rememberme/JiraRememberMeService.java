package com.atlassian.jira.security.auth.rememberme;

import com.atlassian.seraph.ioc.ApplicationServicesRegistry;
import com.atlassian.seraph.service.rememberme.DefaultRememberMeService;
import com.atlassian.seraph.service.rememberme.RememberMeTokenGenerator;
import com.atlassian.seraph.spi.rememberme.RememberMeConfiguration;
import com.atlassian.seraph.spi.rememberme.RememberMeTokenDao;

/**
 * Place here so JIRA coders can find out that it is in fact coming from another library.
 *
 * @since v4.2
 */
public class JiraRememberMeService extends DefaultRememberMeService
{
    public JiraRememberMeService(RememberMeConfiguration rememberMeConfiguration, RememberMeTokenDao rememberMeTokenDao, RememberMeTokenGenerator rememberMeTokenGenerator)
    {
        super(rememberMeConfiguration, rememberMeTokenDao, rememberMeTokenGenerator);
        //
        // once our implementation of RememberMe is created we set it into the Seraph SPI world and it will be used from then on
        ApplicationServicesRegistry.setRememberMeService(this);
    }
}
