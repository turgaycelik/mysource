package com.atlassian.jira.web.action.admin.trustedapps;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.auth.trustedapps.KeyFactory;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationBuilder;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationService;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationSyntacticValidator;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.atlassian.security.auth.trustedapps.Application;
import com.atlassian.security.auth.trustedapps.ApplicationRetriever;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collection;

/**
 * View the list of Trusted Applications in the system
 * 
 * @since v3.12
 */
@WebSudoRequired
public class ViewTrustedApplications extends JiraWebActionSupport
{
    private static final Logger log = Logger.getLogger(ViewTrustedApplications.class);

    final class Fields
    {
        static final String BASE_URL = "trustedAppBaseUrl";

        private Fields() {}
    }

    final TrustedApplicationSyntacticValidator validator = new TrustedApplicationSyntacticValidator();

    private final TrustedApplicationService service;
    private final ApplicationProperties properties;

    private String trustedAppBaseUrl;

    public ViewTrustedApplications(TrustedApplicationService service, ApplicationProperties properties)
    {
        this.service = service;
        this.properties = properties;
    }

    protected String doExecute()
    {
        return getResult();
    }

    public String doDefault()
    {
        return getResult();
    }

    /**
     * Request a new Trusted Application's details.
     * 
     * @return a redirect if we can validate and get, otherwise just add errors
     */
    @RequiresXsrfCheck
    public String doRequest()
    {
        // JRA-16003: trim url before doing any validation
        if (trustedAppBaseUrl != null)
        {
            trustedAppBaseUrl = trustedAppBaseUrl.trim();
        }
        if (!validator.validate(getJiraServiceContext(), this, trustedAppBaseUrl))
        {
            return ERROR;
        }

        final ErrorCollection errorCollection = getJiraServiceContext().getErrorCollection();
        final TrustedApplicationBuilder builder;
        try
        {
            builder = requestTrustedApplication(trustedAppBaseUrl);
            if (!builder.toInfo().isValidKey())
            {
                getJiraServiceContext().getErrorCollection().addError(Fields.BASE_URL, getText("admin.trustedapps.request.error.bad.key", builder.toInfo().getPublicKey()));
                KeyFactory.InvalidPublicKey key = ((KeyFactory.InvalidPublicKey) builder.toInfo().getPublicKey());
                log.warn("InvalidKey: " + key, key.getCause());
                return ERROR;
            }
        }
        catch (ApplicationRetriever.RemoteSystemNotFoundException e)
        {
            errorCollection.addError(Fields.BASE_URL, getText("admin.trustedapps.request.error.unknownhost", trustedAppBaseUrl));
            log.warn(e, e);
            return ERROR;
        }
        catch (ApplicationRetriever.RetrievalException e)
        {
            errorCollection.addError(Fields.BASE_URL, getText("admin.trustedapps.request.error.filenotfound", trustedAppBaseUrl));
            log.warn(e, e);
            return ERROR;
        }
        catch (FileNotFoundException e)
        {
            errorCollection.addError(Fields.BASE_URL, getText("admin.trustedapps.request.error.filenotfound", trustedAppBaseUrl));
            log.warn(e, e);
            return ERROR;
        }
        catch (UnknownHostException e)
        {
            errorCollection.addError(Fields.BASE_URL, getText("admin.trustedapps.request.error.unknownhost", trustedAppBaseUrl));
            log.warn(e, e);
            return ERROR;
        }
        catch (IOException e)
        {
            errorCollection.addError(Fields.BASE_URL, getText("admin.trustedapps.request.error.unknown", trustedAppBaseUrl, e.toString()));
            log.warn(e, e);
            return ERROR;
        }
        return getRedirect("EditTrustedApplication!request.jspa?" + builder.toQueryString());
    }

    private TrustedApplicationBuilder requestTrustedApplication(String url) throws IOException, ApplicationRetriever.RetrievalException
    {
        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.setName(url);
        builder.setApplicationId("");
        try
        {
            final InetAddress address = InetAddress.getByName(new URI(url).getHost());
            builder.setIpMatch(address.getHostAddress());
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        final Application application = KeyFactory.getEncryptionProvider().getApplicationCertificate(url);
        builder.setApplicationId(application.getID());
        builder.setPublicKey(application.getPublicKey());
        builder.setUrlMatch(properties.getDefaultBackedString("jira.trustedapps.urlmatch.default"));
        int defaultTimeout = 10000;
        try
        {
            defaultTimeout = Integer.valueOf(properties.getDefaultBackedString("jira.trustedapps.timeout.default")).intValue();
        }
        catch (NumberFormatException e)
        {
            log.warn("Could not set a default timeout due to exception!", e);
        }
        builder.setTimeout(defaultTimeout);
        return builder;
    }

    public Collection getTrustedApplications()
    {
        return service.getAll(getJiraServiceContext());
    }

    public String getTrustedAppBaseUrl()
    {
        return trustedAppBaseUrl;
    }

    public void setTrustedAppBaseUrl(String trustedAppBaseUrl)
    {
        this.trustedAppBaseUrl = trustedAppBaseUrl;
    }
}