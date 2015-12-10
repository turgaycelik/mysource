package com.atlassian.jira.webtests.ztests.plugin.reloadable.enabling;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.plugin.reloadable.AbstractReloadablePluginsTest;

import static com.atlassian.jira.webtests.ztests.plugin.reloadable.ReferencePluginConstants.REFERENCE_DEPENDENT_RESOURCE_KEY;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.ReferencePluginConstants.REFERENCE_RESOURCE_KEY;

/**
 * <p>
 * Test that the top-level i18n 'resource' plugin module type behaves correctly when going from 'never enabled'
 * to 'enabled' state. Also referred to as 'ZERO to ON scenario'.
 *
 * <p>
 * This is tested by verifying output of special actions that retrieve i18n resources from bundle in the reference
 * plugin. The actions reside in the reference plugin itself, as well as the reference dependent plugin (to prove
 * inter-plugin resource availability).
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT})
public class TestI18nResourceEnabling extends AbstractReloadablePluginsTest
{
    public void testReferenceDependentResourceShouldNotBeAvailableGivenReferenceDependentPluginDisabled()
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referencePlugin().resourceAction().goTo(REFERENCE_DEPENDENT_RESOURCE_KEY);

        assertFalse(administration.plugins().referencePlugin().resourceAction().isKeyValuePresent());
    }

    public void testReferenceResourceShouldBeAvailableGivenReferencePluginEnabled() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referencePlugin().resourceAction().goTo(REFERENCE_RESOURCE_KEY);
        assertTrue(administration.plugins().referencePlugin().resourceAction().isKeyValuePresent("Reference Resource"));
    }

    public void testReferenceDependentResourceShouldBeAvailableGivenReferenceDependentPluginEnabled() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referenceDependentPlugin().enable();

        administration.plugins().referencePlugin().resourceAction().goTo(REFERENCE_DEPENDENT_RESOURCE_KEY);

        assertTrue(administration.plugins().referencePlugin().resourceAction().isKeyValuePresent("Reference Dependent Resource"));
    }
}
