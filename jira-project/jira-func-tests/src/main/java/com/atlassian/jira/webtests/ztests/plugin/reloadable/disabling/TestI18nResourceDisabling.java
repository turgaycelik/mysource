package com.atlassian.jira.webtests.ztests.plugin.reloadable.disabling;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.plugin.reloadable.AbstractReloadablePluginsTest;

import static com.atlassian.jira.webtests.ztests.plugin.reloadable.ReferencePluginConstants.REFERENCE_DEPENDENT_RESOURCE_KEY;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.ReferencePluginConstants.REFERENCE_RESOURCE_KEY;

/**
 * <p>
 * Test that top-level i18n 'resource' descriptors load / unload i18n keys correctly when going from 'enabled'
 * to 'disabled' and then to 'enabled' back again. Also referred to as the 'ON to OFF scenario'.
 *
 * <p>
 * This is tested by verifying the output of special actions that retrieve i18n resources from bundles in the reference
 * plugin and reference dependent plugin.
 *
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT})
public class TestI18nResourceDisabling extends AbstractReloadablePluginsTest
{
    public void testReferenceDependentResourceShouldNotBeAvailableAfterDisablingTheReferenceDependentPlugin()
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referenceDependentPlugin().enable();
        administration.plugins().referenceDependentPlugin().disable();

        administration.plugins().referencePlugin().resourceAction().goTo(REFERENCE_DEPENDENT_RESOURCE_KEY);

        assertFalse(administration.plugins().referencePlugin().resourceAction().isKeyValuePresent());
    }

    public void testReferenceResourceShouldBeAvailableAfterEnablingTheReferencePluginBackAgain()
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referencePlugin().resourceAction().goTo(REFERENCE_RESOURCE_KEY);
        administration.plugins().referencePlugin().disable();
        administration.plugins().referencePlugin().enable();

        administration.plugins().referencePlugin().resourceAction().goTo(REFERENCE_RESOURCE_KEY);

        assertTrue(administration.plugins().referencePlugin().resourceAction().isKeyValuePresent("Reference Resource"));
    }
}
