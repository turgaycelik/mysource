package com.atlassian.jira.webtests.ztests.misc;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.rules.RestRule;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.webtests.LicenseKeys;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.meterware.httpunit.WebResponse;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * Ensure the application conform to Platform's specification.
 *
 * @since 4.3.
 */
@WebTest ({ Category.DEV_MODE, Category.PLATFORM_COMPATIBILITY })
public class TestPlatformCompatibility extends FuncTestCase
{
    public static final String PROJECT_NAME = "tautomerism";
    public static final String ISSUE_SUMMARY = "jira blah blah";
    public static final String ISSUE_DESCRIPTION = "ho ho ho and a bottle of rum";

    /**
     * The CTK requires the following system properties since version 2.14. See https://extranet.atlassian.com/x/IpPadw.
     */
    private static final ImmutableMap<String, String> CTK_PROPERTIES = ImmutableMap.<String, String>builder()
            .put("platform.ctk.test.admin.username", FunctTestConstants.ADMIN_USERNAME)
            .put("platform.ctk.test.admin.password", FunctTestConstants.ADMIN_PASSWORD)
            .put("platform.ctk.test.admin.fullname", FunctTestConstants.ADMIN_FULLNAME)
            .put("platform.ctk.test.validlicense", LicenseKeys.V2_OPEN_SOURCE.getLicenseString())
            .put("platform.ctk.test.search.term", ISSUE_SUMMARY)
            .put("platform.ctk.test.search.term.matches", ISSUE_DESCRIPTION)
            .build();

    private static final String PLATFORM_CTK_PLUGIN_KEY = "com.atlassian.refapp.ctk";
    private static final String WEBSUDO_PROPERTY = "jira.websudo.is.disabled";

    private boolean shouldRun;
    private Boolean webSudoEnabled = null;
    private RestRule restRule;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        restRule = new RestRule(this);
        restRule.before();
        administration.restoreBlankInstance();

        shouldRun = isPlatformCtkPluginInstalled();

        if (shouldRun)
        {
            administration.project().addProject("tautomerism", "TTM", ADMIN_USERNAME);
            navigation.issue().createIssue(PROJECT_NAME, "Bug", ISSUE_SUMMARY, ImmutableMap.of("description", new String[] { ISSUE_DESCRIPTION }));
            administration.reIndex();
            navigation.logout();

            // save the "before" WebSudo state
            webSudoEnabled = Boolean.valueOf(backdoor.systemProperties().getProperty(WEBSUDO_PROPERTY));
            backdoor.systemProperties().setProperty(WEBSUDO_PROPERTY, "false");

            // set the required CTK system properties
            for (String property : CTK_PROPERTIES.keySet())
            {
                backdoor.systemProperties().setProperty(property, CTK_PROPERTIES.get(property));
            }
            // set the required DarkFeatures up
            backdoor.darkFeatures().enableForSite("foo");
        }
    }

    @Override
    protected void tearDownTest()
    {
        restRule.after();
        if (shouldRun)
        {
            // unset all the system properties
            for (String property : CTK_PROPERTIES.keySet())
            {
                backdoor.systemProperties().unsetProperty(property);
            }

            // restore the WebSudo state
            backdoor.systemProperties().setProperty(WEBSUDO_PROPERTY, webSudoEnabled.toString());
        }

        super.tearDownTest();
    }

    public void testCtk() throws IOException, SAXException, JSONException
    {
        if (shouldRun)
        {
            log.log("found platform-ctk plugin. run it now!!");
            List<String> skipClasses = getSkippedClasses();

            if (skipClasses.size() == 0)
            {
                log.log("No skipped test. Full suite will be executed.");
            }
            else
            {
                log.log("Tests to be skipped: " + StringUtils.join(skipClasses, "\n"));
            }

            String excludeParam = "";
            if (skipClasses.size() > 0)
            {
                excludeParam = generateExcludeRestParam(skipClasses);
            }

            WebResponse response = restRule.GET("/rest/functest/1.0/junit/runTests?outdir=target/runtest" + excludeParam, ImmutableMap.of("Accept", "application/json"));

            // check that the output is in good format.
            assertEquals("application/json", response.getContentType());
            assertEquals("UTF-8", response.getCharacterSet());

            JSONObject contents = new JSONObject(response.getText());

            // zero here means no test is failing.
            int failures = Integer.parseInt(contents.getString("result"));
            if (failures > 0)
            {
                fail("There were " + failures + " failures. See test output below.\n\n\n" + contents.getString("output"));
            }
        }
        else
        {
            log.log("platform-ctk plugin not found. skipped the test");
        }
    }

    private List<String> getSkippedClasses()
    {
        // classes to be skipped in comma-separated values.n
        String skips = System.getProperty("platform.ctk.skips");
        if (skips == null)
        {
            return Collections.emptyList();
        }

        return ImmutableList.copyOf(StringUtils.split(skips, ","));
    }

    private String generateExcludeRestParam(List<String> skippedClasses)
    {
        StringBuilder output = new StringBuilder();
        for(String skippedClass:skippedClasses)
        {
            output.append("&excludes=");
            output.append(skippedClass);
        }
        return output.toString();
    }

    private boolean isPlatformCtkPluginInstalled()
    {
        navigation.gotoAdmin();
        return administration.plugins().isPluginInstalled(PLATFORM_CTK_PLUGIN_KEY);
    }
}
