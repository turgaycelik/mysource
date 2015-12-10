package com.atlassian.jira.functest.framework.upm;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClient;
import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.httpclient.spi.ThreadLocalContextManagers;
import com.atlassian.integrationtesting.ApplicationPropertiesImpl;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import org.apache.http.Header;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 6.3
 */
public class UpmRestClient
{
    public static final String CONTENT_TYPE_PLUGIN_JSON = "application/vnd.atl.plugins.plugin+json";

    private final String baseUrl;
    private final UsernamePasswordCredentials credentials;
    private final DefaultHttpClient<Void> httpClient;

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpmRestClient.class);

    public UpmRestClient(final String baseUrl, final UsernamePasswordCredentials credentials)
    {
        this.baseUrl = checkNotNull(baseUrl, "baseurl");
        this.credentials = checkNotNull(credentials, "credentials");
        httpClient = new DefaultHttpClient<Void>(new VoidEventPublisher(), new ApplicationPropertiesImpl(
                baseUrl), ThreadLocalContextManagers.<Void> noop(), new HttpClientOptions());
    }

    public static UpmRestClient withDefaultAdminCredentials(final String baseUrl)
    {
        return new UpmRestClient(baseUrl, DefaultCredentials.getDefaultAdminCredentials());
    }

    public boolean isPluginEnabled(final String pluginKey)
    {
        return getPluginStatus(pluginKey).isEnabled();
    }

    public boolean isPluginUserInstalled(final String pluginKey)
    {
        return getPluginStatus(pluginKey).isUserInstalled();
    }

    public PluginStatus getPluginStatus(String pluginKey)
    {
        try
        {
            return requestPluginStatus(pluginKey).get();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Promise<PluginStatus> requestPluginStatus(final String pluginKey)
    {
        checkNotNull(pluginKey, "pluginKey");

        return newRequest(upmPluginInformation(pluginKey)).get().<PluginStatus> transform()
                .ok(new Function<Response, PluginStatus>()
                {
                    @Override
                    public PluginStatus apply(final Response response)
                    {
                        final String entity = response.getEntity();
                        try
                        {
                            final JSONObject jsonObject = new JSONObject(entity);
                            final boolean enabled = jsonObject.getBoolean("enabled");
                            final boolean userInstalled = jsonObject.getBoolean("userInstalled");

                            return new PluginStatus(pluginKey, jsonObject, enabled, userInstalled);
                        }
                        catch (final JSONException e)
                        {
                            throw new RuntimeException("Requesting details of plugin with key \"" + pluginKey +
                                    "\" returned " + response.getStatusCode() + ": " + entity, e);
                        }
                    }
                }).notFound(new Function<Response, PluginStatus>()
                {
                    @Override
                    public PluginStatus apply(final Response response)
                    {
                        throw new RuntimeException("Could not get details on plugin with key \"" + pluginKey + "\": plugin not installed (404).");
                    }}).toPromise();
    }

    public Promise<Option<PluginStatus>> requestPluginStatusOption(final String pluginKey)
    {
        checkNotNull(pluginKey, "pluginKey");

        return newRequest(upmPluginInformation(pluginKey)).get().<Option<PluginStatus>> transform()
                .ok(new Function<Response, Option<PluginStatus>>()
                {
                    @Override
                    public Option<PluginStatus> apply(final Response response)
                    {
                        final String entity = response.getEntity();
                        try
                        {
                            final JSONObject jsonObject = new JSONObject(entity);
                            final boolean enabled = jsonObject.getBoolean("enabled");
                            final boolean userInstalled = jsonObject.getBoolean("userInstalled");

                            return Option.some(new PluginStatus(pluginKey, jsonObject, enabled, userInstalled));
                        }
                        catch (final JSONException e)
                        {
                            log.error("Got response " + response.getStatusCode() + ": " + entity);
                            throw new RuntimeException(e);
                        }
                    }
                }).notFound(new Function<Response, Option<PluginStatus>>()
                {
                    @Override
                    public Option<PluginStatus> apply(final Response response)
                    {
                        return Option.none();
                    }}).toPromise();
    }

    public ResponsePromise updatePlugin(final String pluginKey, final String json)
    {
        checkNotNull(pluginKey, "pluginKey");

        try
        {
            return newRequest(upmPluginInformation(pluginKey)).setContentType(CONTENT_TYPE_PLUGIN_JSON).setEntity(json)
                    .put();
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private Request newRequest(final URI uri)
    {
        final Header authenticateHeader = BasicScheme.authenticate(credentials, "UTF-8", false);
        return httpClient.newRequest(uri).setHeader(authenticateHeader.getName(), authenticateHeader.getValue());
    }

    private URI upmPluginInformation(final String pluginKey)
    {
        return URI.create(baseUrl + "/rest/plugins/1.0/" + pluginKey + "-key");
    }

    public void destroy() throws Exception
    {
        httpClient.destroy();
    }

    class VoidEventPublisher implements EventPublisher
    {
        @Override
        public void publish(final Object o)
        {
        }

        @Override
        public void register(final Object o)
        {
        }

        @Override
        public void unregister(final Object o)
        {
        }

        @Override
        public void unregisterAll()
        {
        }
    }
}
