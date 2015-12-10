package com.atlassian.jira.functest.rules;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HeaderOnlyWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebResponse;

import org.apache.commons.lang.StringUtils;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.xml.sax.SAXException;

import static java.util.Arrays.asList;

/**
 * Rule for using REST.
 *
 * @since v4.2
 */
public class RestRule extends ExternalResource
{
    private final FuncTestCase testCase;
    /**
     * The base URL used during func tests.
     */
    private String baseUrl;

    public RestRule(FuncTestCase testCase)
    {
        this.testCase = testCase;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public String getBaseUrlPlus(String... paths)
    {
        return getBaseUrlPlus(Arrays.asList(paths));
    }

    public String getBaseUrlPlus(Iterable<String> paths)
    {
        Iterable<String> pathsNoLeadingSlashes = Iterables.transform(paths, new Function<String, String>()
        {
            @Override
            public String apply(String path)
            {
                // remove leading slashes so we don't end up with duplicates
                return path.startsWith("/") ? path.substring(1) : path;
            }
        });

        String path = pathsNoLeadingSlashes != null ? StringUtils.join(Lists.newArrayList(pathsNoLeadingSlashes), '/') : "";

        return String.format("%s/%s", getBaseUrl(), path);
    }

    public URI getBaseUriPlus(Iterable<String> paths)
    {
        try
        {
            return new URI(getBaseUrlPlus(paths));
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    public URI getBaseUriPlus(String... paths)
    {
        return getBaseUriPlus(Arrays.asList(paths));
    }

    public String getRestApiUrl(String... paths)
    {
        List<String> list = CollectionBuilder.<String>newBuilder("rest", "api", "2").addAll(paths).asList();
        return getBaseUrlPlus(list);
    }

    public URI getRestApiUri(String... paths)
    {
        return getRestApiUri(asList(paths));
    }

    public URI getRestApiUri(Iterable<String> paths)
    {
        List<String> all = Lists.newArrayList("rest", "api", "2");
        all.addAll(Lists.newArrayList(paths));
        return getBaseUriPlus(all);
    }

    public JSONObject getJSON(final String url, String... expand) throws JSONException
    {
        String queryString = (expand != null && expand.length > 0) ? ("?expand=" + StringUtils.join(expand, ',')) : "";
        testCase.getTester().gotoPage(url + queryString);

        return new JSONObject(testCase.getTester().getDialog().getResponseText());
    }

    public WebResponse GET(final String url) throws IOException, SAXException
    {
        return GET(url, Collections.<String, String>emptyMap());
    }

    public WebResponse GET(final String url, Map<String, String> headers) throws IOException, SAXException
    {
        testCase.getTester().getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
        for (Map.Entry<String, String> headerField : headers.entrySet())
        {
            testCase.getTester().getDialog().getWebClient().setHeaderField(headerField.getKey(), headerField.getValue());
        }

        final GetMethodWebRequest request = new GetMethodWebRequest(getBaseUrlPlus(url));
        return testCase.getTester().getDialog().getWebClient().sendRequest(request);
    }

    public WebResponse DELETE(final String url) throws IOException, SAXException
    {
        testCase.getTester().getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);

        final HeaderOnlyWebRequest delete = new HeaderOnlyWebRequest(getBaseUrlPlus(url))
        {
            @Override
            public String getMethod()
            {
                return "DELETE";
            }

            // If you don't override this then the above getMethod() never gets called and the request goes through
            // as a GET. Thanks httpunit.
            @Override
            protected void completeRequest(final URLConnection connection) throws IOException
            {
                ((HttpURLConnection) connection).setRequestMethod(getMethod());
                super.completeRequest(connection);
            }
        };

        return testCase.getTester().getDialog().getWebClient().sendRequest(delete);
    }

    public WebResponse POST(final String url, final JSONObject json) throws IOException, SAXException
    {
        return POST(url, json.toString());
    }

    public WebResponse POST(final String url, final String postBody) throws IOException, SAXException
    {
        testCase.getTester().getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);

        final PostMethodWebRequest request = new PostMethodWebRequest(getBaseUrlPlus(url), new ByteArrayInputStream(postBody.getBytes()), "application/json");
        return testCase.getTester().getDialog().getWebClient().sendRequest(request);
    }


    public WebResponse PUT(final String url, final JSONObject json) throws IOException, SAXException
    {
        return PUT(url, json.toString());
    }

    public WebResponse PUT(final String url, final String postBody) throws IOException, SAXException
    {
        testCase.getTester().getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);

        final PutMethodWebRequest request = new PutMethodWebRequest(getBaseUrlPlus(url), new ByteArrayInputStream(postBody.getBytes()), "application/json");
        return testCase.getTester().getDialog().getWebClient().sendRequest(request);
    }

    @Override
    public void before() {
        baseUrl = testCase.getEnvironmentData().getBaseUrl().toExternalForm();
    }

    @Override
    public void after() {
        HttpUnitOptions.resetDefaultCharacterSet();
    }

}
