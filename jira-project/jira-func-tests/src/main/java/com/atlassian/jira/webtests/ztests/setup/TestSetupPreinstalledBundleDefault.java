package com.atlassian.jira.webtests.ztests.setup;

import java.util.concurrent.ExecutionException;

import com.atlassian.fugue.Option;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.upm.DefaultCredentials;
import com.atlassian.jira.functest.framework.upm.PluginStatus;
import com.atlassian.jira.functest.framework.upm.UpmRestClient;

import org.junit.Assert;

import static com.atlassian.jira.webtests.ztests.setup.SetupPreinstalledBundlesConstants.BUNDLE_NONE;
import static com.atlassian.jira.webtests.ztests.setup.SetupPreinstalledBundlesConstants.PLUGINID_AGILE;
import static com.atlassian.jira.webtests.ztests.setup.SetupPreinstalledBundlesConstants.PLUGINID_SERVICEDESK;
import static com.atlassian.jira.webtests.ztests.setup.SetupPreinstalledBundlesConstants.SELECTED_BUNDLE_FORM_ELEMENT;
import static com.atlassian.jira.webtests.ztests.setup.SetupPreinstalledBundlesConstants.STEPS_AFTER_BUNDLE;
import static com.atlassian.jira.webtests.ztests.setup.SetupPreinstalledBundlesConstants.STEPS_UNTIL_BUNDLE;

/**
 * @since 6.3
 */
@WebTest ({ Category.FUNC_TEST, Category.SETUP_PRISTINE })
public class TestSetupPreinstalledBundleDefault extends FuncTestCase
{
    private SetupPluginTestInfrastructure setupPluginTestInfrastructure;

    @Override
    protected void setUpTest()
    {
        setupPluginTestInfrastructure = new SetupPluginTestInfrastructure(tester, environmentData);
        setupPluginTestInfrastructure.setup();
    }

    public void testShouldInstallNoBundles() throws ExecutionException, InterruptedException
    {
        setupPluginTestInfrastructure.getSetupInstanceHelper().setupJIRAStepsInSet(STEPS_UNTIL_BUNDLE);

        form.getForms()[0].getScriptableObject().setParameterValue(SELECTED_BUNDLE_FORM_ELEMENT, BUNDLE_NONE);
        tester.submit();

        setupPluginTestInfrastructure.getSetupInstanceHelper().setupJIRAStepsInSet(STEPS_AFTER_BUNDLE);

        UpmRestClient upmRestClient = new UpmRestClient(
                environmentData.getBaseUrl().toString(),
                DefaultCredentials.getDefaultAdminCredentials());

        Assert.assertTrue(
                "In default option there should be no additional plugins",
                isPluginAbsent(upmRestClient, PLUGINID_SERVICEDESK));
        Assert.assertTrue(
                "In default option there should be no additional plugins",
                isPluginAbsent(upmRestClient, PLUGINID_AGILE));

    }

    boolean isPluginAbsent(UpmRestClient client, String pluginId) throws ExecutionException, InterruptedException
    {
        final Option<PluginStatus> pluginStatus = client.requestPluginStatusOption(pluginId).get();

        return pluginStatus.isEmpty();
    }

    @Override
    protected boolean shouldSkipSetup()
    {
        return true;
    }
}
