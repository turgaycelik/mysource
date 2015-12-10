package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.DarkFeature;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.SiteDarkFeaturesClientExt;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Func tests for SiteDarkFeaturesResource.
 *
 * @since v5.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestSiteDarkFeaturesResource extends RestFuncTest
{
    private static final String DARK_FEATURE_KEY = "my.dark.feature";
    private static final String DARK_FEATURE_OTHER_KEY = "my.other.dark.feature";

    private SiteDarkFeaturesClientExt client;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        client = new SiteDarkFeaturesClientExt(getEnvironmentData());
        administration.restoreBlankInstance();
    }

    /**
     * Tests getting dark feature
     */
    public void testGet() throws Exception
    {
        client.loginAs("admin", "admin");
        DarkFeature darkFeature = client.get(DARK_FEATURE_KEY);
        assertFalse(darkFeature.enabled);

        backdoor.darkFeatures().enableForSite(DARK_FEATURE_KEY);
        darkFeature = client.get(DARK_FEATURE_KEY);
        assertTrue(darkFeature.enabled);
    }

    /**
     * Tests enabling dark feature
     */
    public void testEnable() throws Exception
    {
        client.loginAs("admin", "admin");
        assertFalse(backdoor.darkFeatures().isGlobalEnabled(DARK_FEATURE_KEY));

        client.put(DARK_FEATURE_KEY, true);
        assertTrue(backdoor.darkFeatures().isGlobalEnabled(DARK_FEATURE_KEY));
    }

    /**
     * Tests disabling
     */
    public void testDisable() throws Exception
    {
        backdoor.darkFeatures().enableForSite(DARK_FEATURE_KEY);
        assertTrue(backdoor.darkFeatures().isGlobalEnabled(DARK_FEATURE_KEY));

        client.loginAs("admin", "admin");
        client.put(DARK_FEATURE_KEY, false);
        assertFalse(backdoor.darkFeatures().isGlobalEnabled(DARK_FEATURE_KEY));
    }

    /**
     * Tests non-admin permissions
     */
    public void testNonAdministrator() throws Exception
    {
        client.loginAs("fred");

        Response response = client.putResponse(DARK_FEATURE_KEY, true);
        assertEquals(403, response.statusCode);

        response = client.putResponse(DARK_FEATURE_KEY, false);
        assertEquals(403, response.statusCode);

        response = client.getResponse(DARK_FEATURE_KEY);
        assertEquals(403, response.statusCode);
    }

    public void testBatchUpdateDarkFeature() throws Exception
    {
        // first enable both dark features
        Map<String, DarkFeature> enabledFeatures_1 = client.post(ImmutableMap.of(
                DARK_FEATURE_KEY, new DarkFeature(true),
                DARK_FEATURE_OTHER_KEY, new DarkFeature(true)
        )).siteFeatures;

        assertThat(enabledFeatures_1, allOf(hasKey(DARK_FEATURE_KEY), hasKey(DARK_FEATURE_OTHER_KEY)));
        assertThat(enabledFeatures_1.get(DARK_FEATURE_KEY).enabled, is(TRUE));
        assertThat(enabledFeatures_1.get(DARK_FEATURE_OTHER_KEY).enabled, is(TRUE));

        // now disable one of them
        Map<String, DarkFeature> enabledFeatures_2 = client.post(ImmutableMap.of(
                DARK_FEATURE_KEY, new DarkFeature(false),
                DARK_FEATURE_OTHER_KEY, new DarkFeature(true)
        )).siteFeatures;

        assertThat(enabledFeatures_2, allOf(not(hasKey(DARK_FEATURE_KEY)), hasKey(DARK_FEATURE_OTHER_KEY)));
        assertTrue(enabledFeatures_2.get(DARK_FEATURE_OTHER_KEY).enabled);

        // now toggle them
        Map<String, DarkFeature> enabledFeatures_3 = client.post(ImmutableMap.of(
                DARK_FEATURE_KEY, new DarkFeature(true),
                DARK_FEATURE_OTHER_KEY, new DarkFeature(false)
        )).siteFeatures;

        assertThat(enabledFeatures_3, allOf(hasKey(DARK_FEATURE_KEY), not(hasKey(DARK_FEATURE_OTHER_KEY))));
        assertTrue(enabledFeatures_3.get(DARK_FEATURE_KEY).enabled);
    }
}
