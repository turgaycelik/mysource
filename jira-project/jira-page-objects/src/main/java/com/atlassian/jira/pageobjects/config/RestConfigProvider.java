package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.pageobjects.ProductInstance;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.inject.Inject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.atlassian.jira.pageobjects.config.HttpClientCloser.closeQuietly;

/**
 * Component providing config information about JIRA.
 *
 * @since v4.4
 */
public class RestConfigProvider implements JiraConfigProvider
{
    private static final Logger logger = LoggerFactory.getLogger(RestConfigProvider.class);

    @Inject private ProductInstance jiraProduct;

    public RestConfigProvider()
    {
    }

    RestConfigProvider(ProductInstance jiraProduct)
    {
        this.jiraProduct = jiraProduct;
    }

    private final HttpClient client = new DefaultHttpClient();
    private final Gson gson = new Gson();
    private Config config;

    public String jiraHomePath()
    {
        return getConfig().jiraHomePath;
    }

    // TODO this doesn't work obviously - 400 from REST when JIRA is not set up
    @Override
    public boolean isSetUp()
    {
        // we can't cache the config, it may change!
        return loadConfig().isSetUp;
    }

    private Config getConfig()
    {
        if (config == null)
        {
            config = loadConfig();
        }
        return config;
    }

    private Config loadConfig()
    {
        final String uri = jiraProduct.getBaseUrl() + "/rest/testkit-test/1.0/config-info";
        logger.debug("Request to " + uri);
        final HttpGet get = new HttpGet(uri);
        HttpResponse response = null;
        try
        {
            response = client.execute(get);
            final String responseString = EntityUtils.toString(response.getEntity());
            return parseResponse(responseString);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            closeQuietly(response);
        }
    }

    private Config parseResponse(String responseString)
    {
        try
        {
            Config config = gson.fromJson(responseString, Config.class);
            Assertions.notNull("Unable to parse response into Config class:\n" + responseString, config);
            return config;
        }
        catch (JsonParseException e)
        {
            throw new RuntimeException("Exception while parsing response:\n" + responseString, e);
        }
    }



    public static class Config
    {
        String jiraHomePath;
        boolean isSetUp;
    }
}
