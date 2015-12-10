package com.atlassian.jira.gadgets.system;

import com.atlassian.gadgets.GadgetSpecProvider;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Adds the Marketing Gadget to JIRA's list of gadgets.  This spec provider will use a Lazy Reference to lookup the
 * marketing gadget once.  If this lookup fails (firewall issues), then it wont check again until JIRA is restarted to
 * avoid performance issues with the directory.
 *
 * @since v4.0
 */
public class MarketingGadgetSpecProvider implements GadgetSpecProvider
{
    private static final Logger log = Logger.getLogger(MarketingGadgetSpecProvider.class);

    private static final String MARKETING_LINK_KEY = "external.link.jira.marketing.gadget";

    private static final int CONNECT_TIMEOUT_MS = 10000;

    //lazy reference to check if the JIRA: News gadget can be reached.  If not, then this gadget spec provider will simply
    //provide nothing and not check again to avoid degrading performance when loading the gadget directory.
    final LazyReference<Set<URI>> uris = new LazyReference<Set<URI>>()
    {
        protected Set<URI> create()
        {
            final Set<URI> ret = new HashSet<URI>();
            final ExternalLinkUtil externalLinkUtilInstance = ExternalLinkUtilImpl.getInstance();
            final String gadgetUrl = externalLinkUtilInstance.getProperty(MARKETING_LINK_KEY);
            if (StringUtils.isBlank(gadgetUrl))
            {
                log.info("Blank gadget URL specified for the Atlassian JIRA News gadget.  Check " + externalLinkUtilInstance.getPropertiesFilename() + " entry for '" + MARKETING_LINK_KEY + "'.");
                return Collections.emptySet();
            }

            try
            {
                final URI gadgetUri = new URI(gadgetUrl);

                final HttpParams params = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT_MS);
                HttpConnectionParams.setSoTimeout(params, CONNECT_TIMEOUT_MS);
                final DefaultHttpClient httpclient = new DefaultHttpClient(params);
                //need to use JVM's default proxy configuration.
                final ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
                        httpclient.getConnectionManager().getSchemeRegistry(),
                        ProxySelector.getDefault());
                httpclient.setRoutePlanner(routePlanner);

                final HttpResponse httpResponse = httpclient.execute(new HttpGet(gadgetUri));
                final int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (HttpStatus.SC_OK == statusCode)
                {
                    log.debug("Successfully retrieved Atlassian JIRA News gadget from '" + gadgetUrl + "'.");
                    ret.add(URI.create(gadgetUrl));
                }
                else
                {
                    log.info("Could not retrieve Atlassian JIRA News gadget from '" + gadgetUrl + "'. Server returned status '" + statusCode + "'");
                }
            }
            catch (IOException e)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("IO error retrieving Atlassian JIRA News gadget from '" + gadgetUrl + "'", e);
                }
                else
                {
                    log.info("IO error retrieving Atlassian JIRA News gadget from '" + gadgetUrl + "': " + e.getMessage());
                }

            }
            catch (RuntimeException e)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Unexpected error retrieving Atlassian JIRA News gadget from '" + gadgetUrl + "'", e);
                }
                else
                {
                    log.info("Unexpected error retrieving Atlassian JIRA News gadget from '" + gadgetUrl + "': " + e.getMessage());
                }
            }
            catch (URISyntaxException e)
            {
                log.info("Invalid gadget URL specified for Atlassian JIRA News gadget '" + gadgetUrl + "'. Check " + externalLinkUtilInstance.getPropertiesFilename() + " entry for '" + MARKETING_LINK_KEY + "'.");
            }

            return ret;
        }
    };

    public Iterable<URI> entries()
    {
        return Collections.unmodifiableSet(uris.get());
    }

    public boolean contains(final URI uri)
    {
        return uris.get().contains(uri);
    }
}
