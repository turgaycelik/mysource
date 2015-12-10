package com.atlassian.jira.webtests.ztests.plugin.reloadable.disabling;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.plugin.reloadable.AbstractReloadablePluginsTest;
import org.junit.Ignore;

import static com.atlassian.jira.webtests.ztests.plugin.reloadable.ReferencePluginConstants.REFERENCE_PLUGIN_KEY;

/**
 * <p>
 * Responsible for verifying the disablement of component module types. Component module types can't be disabled
 * individually as they are internals of the plugin.
 *
 * Also, disabling a plugin that contains a publicly available component causes all dependant plugins to be disabled.
 *
 * <p>
 * Also known as the ON to OFF scenario.
 *
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT})
public class TestComponentModuleTypeDisabling extends AbstractReloadablePluginsTest
{
    private static final String REFERENCE_COMPONENT_MODULE_KEY = REFERENCE_PLUGIN_KEY + ":reference-component";

    public void testShouldBeAbleToDisableAPluginContainingAComponentModuleType()
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referencePlugin().disable();

        assertTrue(administration.plugins().referencePlugin().isDisabled());
    }

    public void testShouldNotBeAbleToDisableAComponentModuleTypeIndividually()
    {
        administration.plugins().referencePlugin().enable();

        assertFalse(canDisableReferenceComponentModule());
    }

    @Ignore("ohernandez -- Don B. says that this is the behaviour that is expected, but in practice it appears that it "
            + "is not. Ignoring until we get more feedback from Don")
    public void testDisablingAPluginThatContainsAPublicComponentShouldCauseDependantPluginsToBeDisabled()
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referenceDependentPlugin().enable();
        administration.plugins().referencePlugin().disable();

        assertTrue(administration.plugins().referencePlugin().isDisabled());
        assertTrue(administration.plugins().referenceDependentPlugin().isDisabled());
    }

    private boolean canDisableReferenceComponentModule()
    {
        return administration.plugins().canDisablePluginModule(REFERENCE_PLUGIN_KEY, REFERENCE_COMPONENT_MODULE_KEY);
    }
}
