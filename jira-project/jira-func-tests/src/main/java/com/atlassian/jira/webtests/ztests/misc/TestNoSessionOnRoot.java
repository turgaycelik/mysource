package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 */
@WebTest({ Category.FUNC_TEST, Category.BROWSING })
public class TestNoSessionOnRoot extends FuncTestCase {
    public void testNoSessionAtRoot() throws Exception
    {
        // don't use httpunit, as it logs you in and establishes a session
        // we want a "pure" http request that we can reason about

        String url = getEnvironmentData().getBaseUrl().toExternalForm();
        if (!url.endsWith("/"))
        {
            // we want to hit default.jsp, not tomcat's redirector
            url += "/";
        }
        URL baseUrl = new URL(url);
        HttpURLConnection uc = (HttpURLConnection) baseUrl.openConnection();
        uc.setInstanceFollowRedirects(false);
        uc.connect();

        int responseCode = uc.getResponseCode();
        assertThat("expect redirect", responseCode, anyOf(equalTo(301), equalTo(302)));

        for (Map.Entry<String, List<String>> entry : uc.getHeaderFields().entrySet()) {
            if ("Set-Cookie".equalsIgnoreCase(entry.getKey())) {
                String cookie = StringUtils.join(entry.getValue(), " ").toLowerCase();
                assertThat(cookie, not(containsString("jsessionid")));
                break;
            }
        }
    }

}
