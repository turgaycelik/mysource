package com.atlassian.jira.dev.rest;

import com.atlassian.jira.dev.i18n.QunitLocaleSwitcher;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @since v6.0
 */
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
@Consumes ({ MediaType.APPLICATION_JSON })
@Path ("qunittranslation")
public class QUnitTranslationResource
{
    private final QunitLocaleSwitcher qunitLocaleSwitcher;

    public QUnitTranslationResource(QunitLocaleSwitcher qunitLocaleSwitcher)
    {
        this.qunitLocaleSwitcher = qunitLocaleSwitcher;
    }

    @PUT
    public void reEnableTranslations()
    {
        qunitLocaleSwitcher.resetTranslations();
    }
}
