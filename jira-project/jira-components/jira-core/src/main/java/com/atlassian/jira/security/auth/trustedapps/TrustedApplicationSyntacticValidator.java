package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.security.auth.trustedapps.IPAddressFormatException;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Responsible for syntactic validation of the entry of Trusted Application data.
 * 
 * @since v3.12
 */
public class TrustedApplicationSyntacticValidator implements TrustedApplicationValidator
{
    static final class Fields
    {
        static final String NAME = "name";
        static final String TIMEOUT = "timeout";
        static final String APPLICATION_ID = "applicationId";
        static final String IP_MATCH = "ipMatch";
        static final String URL_MATCH = "urlMatch";
        static final String BASE_URL = "trustedAppBaseUrl";
    }

    /**
     * Validate the details of the trusted application request.
     * 
     * @param context the service context that contains the error collection and the user details, must not be null
     * @param helper so we can i18n the messages
     * @param urlString the base urlString of the client application
     * @return true if passed, false otherwise
     */
    public boolean validate(final JiraServiceContext context, final I18nHelper helper, final String urlString)
    {
        Assertions.notNull("context", context);
        Assertions.notNull("i18nHelper", helper);

        if (StringUtils.isBlank(urlString))
        {
            context.getErrorCollection().addError(Fields.BASE_URL, helper.getText("URL must not be empty."));
        }
        else
        {
            try
            {
                new URL(urlString);

                // JRA-16003: URI validation might yield more errors
                new URI(urlString);
            }
            catch (final MalformedURLException e)
            {
                context.getErrorCollection().addError(Fields.BASE_URL, e.toString());
            }
            catch (final URISyntaxException e)
            {
                context.getErrorCollection().addError(Fields.BASE_URL, e.toString());
            }
        }

        return !context.getErrorCollection().hasAnyErrors();
    }

    /**
     * Validate the details of the trusted application request.
     * 
     * @param context the service context that contains the error collection and the user details, must not be null
     * @param helper so we can i18n the messages
     * @param application containing the details of a trustedApplication that is being edited
     * @return true if passed, false otherwise
     */
    public boolean validate(final JiraServiceContext context, final I18nHelper helper, final SimpleTrustedApplication application)
    {
        Assertions.notNull("context", context);
        Assertions.notNull("application", application);
        Assertions.notNull("helper", helper);

        if (StringUtils.isBlank(application.getApplicationId()))
        {
            context.getErrorCollection().addError(Fields.APPLICATION_ID, helper.getText("admin.trustedapps.edit.application.id.blank"));
        }
        if (StringUtils.isBlank(application.getName()))
        {
            context.getErrorCollection().addError(Fields.NAME, helper.getText("admin.trustedapps.edit.name.blank"));
        }
        if (application.getTimeout() <= 0)
        {
            context.getErrorCollection().addError(Fields.TIMEOUT, helper.getText("admin.trustedapps.edit.timeout.too.small"));
        }
        if (StringUtils.isBlank(application.getPublicKey()))
        {
            context.getErrorCollection().addErrorMessage(helper.getText("admin.trustedapps.edit.public.key.blank"));
        }
        if (StringUtils.isBlank(application.getIpMatch()))
        {
            context.getErrorCollection().addError(Fields.IP_MATCH, helper.getText("admin.trustedapps.edit.ipmatch.empty"));
        }
        else
        {
            try
            {
                TrustedApplicationUtil.getIPMatcher(application.getIpMatch());
            }
            catch (final IPAddressFormatException ex)
            {
                context.getErrorCollection().addError(Fields.IP_MATCH, helper.getText("admin.trustedapps.edit.ipmatch.invalid", ex.getBadIPAddress()));
            }
        }
        if (StringUtils.isBlank(application.getUrlMatch()))
        {
            context.getErrorCollection().addError(Fields.URL_MATCH, helper.getText("admin.trustedapps.edit.urlmatch.empty"));
        }
        return !context.getErrorCollection().hasAnyErrors();
    }
}
