package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @since v4.4.5
 */
public class AdvancedApplicationPropertiesImpl extends AbstractFuncTestUtil implements AdvancedApplicationProperties
{
    private final static String REST_URL = "/rest/api/2/application-properties/";
    public static final String MEDIA_TYPE = "application/json";

    public AdvancedApplicationPropertiesImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    @Override
    public Map<String, String> getApplicationProperties()
    {
        try
        {
            final Map<String, String> properties = new HashMap<String, String>();
            disableErrorChecking();
            final GetMethodWebRequest request = new GetMethodWebRequest(getAppPropertiesRestUrl().toString());
            WebResponse resp123 = tester.getDialog().getWebClient().sendRequest(request);
            Assert.assertEquals(200, resp123.getResponseCode());
            JSONArray content = new JSONArray(resp123.getText());
            for (int i =0;i < content.length(); i++)
            {
                JSONObject appProperty = content.getJSONObject(i);
                properties.put(appProperty.getString("key"),appProperty.getString("value"));
            }
            return properties;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setApplicationProperty(String key, String value)
    {
        Assert.assertNotNull(getApplicationProperty(key));
        Assert.assertNotNull(value);
        try
        {
            final URL sendURL = new URL(getAppPropertiesRestUrl(), key);
            disableErrorChecking();

            final PutMethodWebRequest request = new PutMethodWebRequest(sendURL.toString(), new ByteArrayInputStream(getJsonString(key, value).getBytes()), MEDIA_TYPE);
            tester.getDialog().getWebClient().sendRequest(request);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getApplicationProperty(String key)
    {
        final Map<String, String> properties = getApplicationProperties();
        return properties.get(key);
    }

    private String getJsonString(String key, String value)
    {
        final StringBuilder jsonBuilder = new StringBuilder("{\"id\":\"");
        jsonBuilder.append(key).append("\",\"value\":\"").append(value).append("\"}");
        return  jsonBuilder.toString();
    }
    
    private void disableErrorChecking()
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
    }

    private Navigation getNavigation()
    {
        return getFuncTestHelperFactory().getNavigation();
    }

    private void goToAdvancedSettings()
    {
        getNavigation().gotoAdminSection("general_configuration");
        tester.clickLink("edit-advanced-properties");
    }
    
    private URL getAppPropertiesRestUrl()
    {
        try
        {
            return new URL(this.environmentData.getBaseUrl().toString() + REST_URL);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
