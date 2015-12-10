package com.atlassian.jira.pageobjects.config;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jira.pageobjects.config.HttpClientCloser.closeQuietly;

/**
 * Abstract class for checking whether some plugin is installed.
 *
 * @since v6.1
 */
abstract public class AbstractPluginDetector
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractPluginDetector.class);

    private final HttpClient client = new DefaultHttpClient();

    private volatile boolean installed = false;

    public boolean isInstalled()
    {
        if (!installed) // if false -> recheck (might be installed by completing setup)
        {
            installed = checkInstalled();
        }
        return installed;
    }

    abstract protected boolean checkInstalled();

    protected boolean checkInstalledViaGet(final String uri)
    {
        HttpResponse response = null;
        try
        {
            response = client.execute(new HttpGet(uri));
            return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        }
        catch (Exception e)
        {
            logger.warn("Exception while checking for plugin", e);
            return false;
        }
        finally
        {
            closeQuietly(response);
        }
    }
}
