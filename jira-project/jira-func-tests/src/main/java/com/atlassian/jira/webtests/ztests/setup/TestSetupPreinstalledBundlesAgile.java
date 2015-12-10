package com.atlassian.jira.webtests.ztests.setup;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.upm.UpmRestClient;

import org.junit.Assert;

import static com.atlassian.jira.webtests.ztests.setup.SetupPreinstalledBundlesConstants.*;

/**
 * Setup tests like these should be separately added to CI plan configuration. Look into
 * JIRA_ROOT/conf/plan-templates/tier1/plan.func.groovy for example.
 * @since 6.3
 */
@WebTest ({ Category.FUNC_TEST, Category.SETUP_PRISTINE })
public class TestSetupPreinstalledBundlesAgile extends FuncTestCase
{
    private SetupPluginTestInfrastructure setupPluginTestInfrastructure;

    @Override
    protected void setUpTest()
    {
        setupPluginTestInfrastructure = new SetupPluginTestInfrastructure(tester, environmentData);
        setupPluginTestInfrastructure.setup();
    }


    public void testShouldInstallAgileBundle()
    {
        setupPluginTestInfrastructure.getSetupInstanceHelper().setupJIRAStepsInSet(
                STEPS_UNTIL_BUNDLE);

        form.getForms()[0].getScriptableObject().setParameterValue(SELECTED_BUNDLE_FORM_ELEMENT, BUNDLE_AGILE);
        tester.submit();

        setupPluginTestInfrastructure.getSetupInstanceHelper().setupJIRAStepsInSet(
                STEPS_AFTER_BUNDLE);

        UpmRestClient upmRestClient = setupPluginTestInfrastructure.getUpmRestClient();

        Assert.assertTrue(
                "Greenhopper plugin is enabled",
                upmRestClient.isPluginEnabled(PLUGINID_AGILE));
    }

    @Override
    protected boolean shouldSkipSetup()
    {
        return true;
    }
}
