package com.atlassian.jira.webtests;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;

/**
 * Used for creating web testers
 *
 * @since v4.3
 */
public class WebTesterFactory
{
    public static WebTester createNewWebTester(JIRAEnvironmentData environmentData)
    {
        WebTester tester = new WebTester();
        setupWebTester(tester, environmentData);
        return tester;
    }

    public static void setupWebTester(WebTester tester, JIRAEnvironmentData environmentData)
    {
        tester.getTestContext().setBaseUrl(environmentData.getBaseUrl().toExternalForm());
        if (StringUtils.isNotBlank(environmentData.getTenant()))
        {
            tester.getTestContext().getWebClient().setHeaderField("X-Atlassian-Tenant", environmentData.getTenant());
        }
    }
}
