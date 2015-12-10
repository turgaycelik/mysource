package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;


/**
 *
 * @since v4.3
 */
public class PluginsImpl extends AbstractFuncTestUtil implements Plugins
{
    private final Navigation navigation;
    private final Administration administration;

    private final ReferencePlugin referencePlugin;
    private final ReferenceDependentPlugin referenceDependentPlugin;
    private final ReferenceLanguagePack referenceLanguagePack;

    public PluginsImpl(final WebTester tester, final JIRAEnvironmentData environmentData, final int logIndentLevel,
            final Navigation navigation, final Administration administration, LocatorFactory locators)
    {
        super(tester, environmentData, logIndentLevel);
        this.navigation = navigation;
        this.administration = administration;
        this.referencePlugin = new ReferencePlugin(tester, administration, locators, navigation);
        this.referenceDependentPlugin = new ReferenceDependentPlugin(tester, administration, locators);
        this.referenceLanguagePack = new ReferenceLanguagePack(administration);
    }

    @Override
    public boolean isPluginInstalled(String pluginKey)
    {
        try
        {
            String currentPage = navigation.getCurrentPage();
            navigation.gotoAdmin();
            boolean found = (this.findPlugin(pluginKey) != null);
            navigation.gotoPage(currentPage);
            return found;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Plugin utility failed",e);
        }
    }

    @Override
    public ReferencePlugin referencePlugin()
    {
        return referencePlugin;
    }

    @Override
    public ReferenceDependentPlugin referenceDependentPlugin()
    {
        return referenceDependentPlugin;
    }

    @Override
    public ReferenceLanguagePack referenceLanguagePack()
    {
        return referenceLanguagePack;
    }

    public void disablePlugin(final String pluginKey)
    {
        togglePlugin(pluginKey, false);
    }


    public void enablePlugin(final String pluginKey)
    {
        togglePlugin(pluginKey, true);
    }

    public void disablePluginModule(final String pluginKey, final String completeModuleKey)
    {
        togglePluginModule(pluginKey, completeModuleKey, false);
    }


    public void enablePluginModule(final String pluginKey, final String completeModuleKey)
    {
        togglePluginModule(pluginKey, completeModuleKey, true);
    }

    @Override
    public boolean canDisablePluginModule(String pluginKey, String completeModuleKey)
    {
        try
        {
            String currentPage = navigation.getCurrentPage();
            JSONObject plugin = findPlugin(pluginKey);
            plugin = fetchFullObjectInfo(plugin);
            JSONObject module = findModule(plugin, completeModuleKey);
            navigation.gotoPage(currentPage);
            return module.getBoolean("optional");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canDisablePlugin(String pluginKey)
    {
        try
        {
            String currentPage = navigation.getCurrentPage();
            String contentType = tester.getDialog().getWebClient().getCurrentPage().getHeaderField("CONTENT-TYPE");
            JSONObject plugin = findPlugin(pluginKey);
            plugin = fetchFullObjectInfo(plugin);
            navigation.gotoPage(currentPage);
            return plugin.getBoolean("optional");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isPluginEnabled(final String pluginKey)
    {
        try
        {
            final String contentType = tester.getDialog().getWebClient().getCurrentPage().getHeaderField("CONTENT-TYPE");
            JSONObject plugin = findPlugin(pluginKey);
            plugin = fetchFullObjectInfo(plugin);
            navigation.gotoPage(navigation.getCurrentPage());
            return plugin.getBoolean("enabled");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isPluginDisabled(final String pluginKey)
    {
        return !isPluginEnabled(pluginKey);
    }

    @Override
    public boolean isPluginModuleEnabled(final String pluginKey, final String completeModuleKey)
    {
        try
        {
            String currentPage = navigation.getCurrentPage();
            navigation.gotoAdmin();
            JSONObject plugin = findPlugin(pluginKey);
            plugin = fetchFullObjectInfo(plugin);
            JSONObject module = findModule(plugin, completeModuleKey);
            navigation.gotoPage(currentPage);
            return module.getBoolean("enabled");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }      
    }

    @Override
    public boolean isPluginModuleDisabled(final String pluginKey, final String completeModuleKey)
    {
        return !isPluginModuleEnabled(pluginKey, completeModuleKey);
    }

    private void togglePlugin(final String pluginKey,boolean enabled)
    {
        try
        {
            String currentPage = navigation.getCurrentPage();
            navigation.gotoAdmin();
            JSONObject plugin = findPlugin(pluginKey);
            if (plugin == null)
            {
                throw new IllegalStateException(pluginKey +" plugin can not be found! Is it installed?");
            }
            plugin = fetchFullObjectInfo(plugin);
            plugin.put("enabled",enabled);
            sendObject(plugin,"application/vnd.atl.plugins.plugin+json");
            navigation.gotoPage(currentPage);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void togglePluginModule(final String pluginKey, final String completeModuleKey, final boolean enabled)
    {
        try
        {
            String currentPage = navigation.getCurrentPage();
            navigation.gotoAdmin();
            JSONObject plugin = findPlugin(pluginKey);
            if (plugin == null)
            {
                throw new IllegalStateException(pluginKey +" plugin can not be found! Is it installed?");
            }
            plugin = fetchFullObjectInfo(plugin);
            JSONObject module = findModule(plugin, completeModuleKey);
            if (module == null)
            {
                throw new IllegalStateException(completeModuleKey+ " module for "+pluginKey +" plugin "+ "cannot be found.");
            }
            module = fetchFullObjectInfo(module);
            module.put("enabled",enabled);
            sendObject(module,"application/vnd.atl.plugins.plugin.module+json");
            navigation.gotoPage(currentPage);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    private JSONObject findPlugin(final String pluginKey) throws Exception
    {
        WebResponse resp123 = GET("/rest/plugins/1.0/");
        Assert.assertEquals(200, resp123.getResponseCode());
        JSONObject content = new JSONObject(resp123.getText());

        for (int i =0;i < content.getJSONArray("plugins").length(); i++)
        {
            JSONObject plugin = content.getJSONArray("plugins").getJSONObject(i);
            if (plugin.getString("key").equals(pluginKey))
            {
                return plugin;
            }
        }
        return null;

    }

    private JSONObject findModule(JSONObject plugin, String completeModuleKey)
    {
        try
        {
            for (int i =0;i < plugin.getJSONArray("modules").length(); i++)
            {
                JSONObject module = plugin.getJSONArray("modules").getJSONObject(i);
                if (module.getString("completeKey").equals(completeModuleKey))
                {
                    return module;
                }
            }
            return null;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    private JSONObject fetchFullObjectInfo(JSONObject object) throws JSONException, IOException, SAXException
    {
        URL sendURL;
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);

        sendURL = new URL(this.environmentData.getBaseUrl(), object.getJSONObject("links").getString("self"));
        final GetMethodWebRequest request = new GetMethodWebRequest(sendURL.toString());
        WebResponse resp123 =  tester.getDialog().getWebClient().sendRequest(request);
        Assert.assertEquals(200, resp123.getResponseCode());
        return new JSONObject(resp123.getText());
    }


    private void sendObject(JSONObject plugin,String mediaType) throws Exception
    {
        WebResponse resp123 = PUT(plugin.getJSONObject("links").getString("modify"),plugin,mediaType);
        Assert.assertEquals(200, resp123.getResponseCode());

    }

    public WebResponse PUT(final String url, final JSONObject json,final String mediaType) throws IOException, SAXException
    {
        return PUT(url, json.toString(),mediaType);
    }

    public WebResponse PUT(final String url, final String postBody,final String mediaType) throws IOException, SAXException
    {
        URL sendURL;
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);

        sendURL = new URL(this.environmentData.getBaseUrl(), url);
        final PutMethodWebRequest request = new PutMethodWebRequest(sendURL.toString(), new ByteArrayInputStream(postBody.getBytes()), mediaType);
        return tester.getDialog().getWebClient().sendRequest(request);
    }

    public WebResponse GET(final String url) throws IOException, SAXException
    {
        return GET(url, Collections.<String, String>emptyMap());
    }

    public WebResponse GET(final String url, Map<String, String> headers) throws IOException, SAXException
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
        for (Map.Entry<String, String> headerField : headers.entrySet())
        {
            tester.getDialog().getWebClient().setHeaderField(headerField.getKey(), headerField.getValue());
        }

        final GetMethodWebRequest request = new GetMethodWebRequest(getBaseUrlPlus(url));
        return tester.getDialog().getWebClient().sendRequest(request);
    }

    protected String getBaseUrlPlus(String... paths)
    {
        String path = paths != null ? StringUtils.join(paths, '/') : "";

        return String.format("%s/%s", environmentData.getBaseUrl(), path);
    }

}
