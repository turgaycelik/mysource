package com.atlassian.jira.webtests.ztests.sendheadearly;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.regex.Pattern;

/**
 * These contain tests to verify that pages contain the same content with flush-head-early disabled and enabled.
 *
 * There are some specific rules to parse content that we known will vary between requests - either:
 * <ul>
 *     <li>because of known different order with flush-head-early disabled or enabled - eg the title tag has a different
 *     location</li>
 *     <li>because of differences between pages, eg the REQUEST ID field in HTML comments</li>
 * </ul>
 *
 * These features make this test inherently likely to break if other parts of the page change; for example if the layout
 * of the REQUEST ID comment changes. However, this test will only exist as long as we have flush-head-early disabled
 * by default - it should be enabled by default for OD and BTF.
 *
 * @since v7.0
 */
@WebTest ({ Category.FUNC_TEST, Category.HTTP })
public class TestSendHeadEarly extends FuncTestCase
{
    private static final String SEND_HEAD_EARLY_KEY = "com.atlassian.plugins.SEND_HEAD_EARLY";

    @Override
    protected void tearDownTest()
    {
        backdoor.darkFeatures().disableForSite(SEND_HEAD_EARLY_KEY);
    }

    public void testDashboard()
    {
        compareEnabledVsDisabled("/secure/Dashboard.jspa");
    }

    // Compares the HTML output between flush-head-early enabled and disabled.
    private void compareEnabledVsDisabled(String url)
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        backdoor.darkFeatures().enableForSite(SEND_HEAD_EARLY_KEY);
        PageResult enabledResponse = getPage(url);

        backdoor.darkFeatures().disableForSite(SEND_HEAD_EARLY_KEY);
        PageResult disabledResponse = getPage(url);

        assertEquals(disabledResponse.content, enabledResponse.content);
        assertEquals(disabledResponse.titleTag, enabledResponse.titleTag);
        assertEquals(disabledResponse.osdTag, enabledResponse.osdTag);
    }

    private static final class PageResult
    {
        // content with title and osd tags removed
        private final String content;
        private final String titleTag;
        private final String osdTag;

        private PageResult(String content, String titleTag, String osdTag)
        {
            this.content = content;
            this.titleTag = titleTag;
            this.osdTag = osdTag;
        }
    }

    private PageResult getPage(String url)
    {
        tester.gotoPage(url);
        String response = tester.getDialog().getResponseText();

        Document doc = Jsoup.parse(response);

        // Extract title + osd (these are in different locations between flush-head-early and normal
        Element titleElement = doc.select("title").first();
        String title = titleElement.outerHtml();
        titleElement.remove();

        Element osdElement = doc.select("link[rel=search][type*=opensearchdescription]").first();
        String osd = osdElement.outerHtml();
        osdElement.remove();

        // Replace known things that vary between test runs
        String[] selectors = new String[] {
                // Remove dark features as we changed them in the test
                "meta[name=ajs-enabled-dark-features]",
                // This varies between requests
                "form#jira_request_timing_info"
        };

        for (String selector : selectors)
        {
            doc.select(selector).remove();
        }

        // Do some regex replacement - I tried and failed to express these with Jsoup :matches or :contains selectors
        String filteredResponse = doc.outerHtml();

        String[] patterns = new String[] {
                // This varies between requests
                "AG\\.DashboardManager\\.setup\\(\\{.*?\\}\\);",
                // This varies between requests
                "<!--\\s+REQUEST ID.*?-->"
        };

        for (String pattern : patterns)
        {
            filteredResponse = replace(pattern, filteredResponse);
        }

        // finally remove whitespace differences
        filteredResponse = filteredResponse.replaceAll("\\s+", " ");

        return new PageResult(filteredResponse, title, osd);
    }

    private final String replace(String pattern, String response)
    {
        Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        return regex.matcher(response).replaceAll("");
    }
}