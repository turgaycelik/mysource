package com.atlassian.jira.webtests.ztests.plugin.reloadable.enabling;

import com.atlassian.jira.functest.framework.admin.plugins.ReferenceDependentPlugin;
import com.atlassian.jira.functest.framework.admin.plugins.ReferencePlugin;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.plugin.reloadable.AbstractReloadablePluginsTest;

/**
 * <p>
 * Test that the 'module type' plugin module type behaves correctly when going from 'never enabled'
 * to enabled state. Also referred to as 'ZERO to ON scenario'.
 *
 * <p>
 * The list of reference module type 'implementations' is displayed by a special reference module type action.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT})
public class TestModuleTypeModuleEnabling extends AbstractReloadablePluginsTest
{
    private ReferencePlugin referencePlugin;
    private ReferenceDependentPlugin referenceDependentPlugin;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        this.referencePlugin = administration.plugins().referencePlugin();
        this.referenceDependentPlugin = administration.plugins().referenceDependentPlugin();
    }

    public void testShouldNotDisplayModuleTypeActionContentsGivenReferencePluginDisabled()
    {
        assertNoModuleImplementationAvailable();
    }

    public void testShouldDisplayInternalImplementationGivenReferencePluginOnlyEnabled()
    {
        administration.plugins().referencePlugin().enable();
        assertReferenceModuleImplementationsAvailable();
    }

    public void testShouldDisplayInternalAndExternalImplementationGivenReferencePluginAndReferenceDependentPluginEnabled()
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referenceDependentPlugin().enable();
        assertAllModuleImplementationAvailable();
    }

    public void testShouldDisplayExternalImplementationGivenReferenceDependentPluginDisabledAndEnabledAgain()
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referenceDependentPlugin().enable();
        assertAllModuleImplementationAvailable();
        administration.plugins().referenceDependentPlugin().disable();
        assertReferenceModuleImplementationsAvailable();
        assertReferenceDependentModuleImplementationsNotAvailable();
        administration.plugins().referenceDependentPlugin().enable();
        assertAllModuleImplementationAvailable();
    }

    public void testShouldNotDisplayAnyImplementationGivenReferencePluginDisabled()
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referenceDependentPlugin().enable();
        assertAllModuleImplementationAvailable();
        administration.plugins().referencePlugin().disable();
        assertNoModuleImplementationAvailable();
        administration.plugins().referencePlugin().enable();
        assertAllModuleImplementationAvailable();
    }

    public void testShouldDisplayOnlyEnabledImplementations()
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referenceDependentPlugin().enable();
        assertAllModuleImplementationAvailable();
        administration.plugins().referencePlugin().moduleType().internalWithI18n().disable();
        assertFalse(administration.plugins().referencePlugin().moduleType().internalWithI18n().isAvailable());
        assertTrue(administration.plugins().referencePlugin().moduleType().internalWithoutI18n().isAvailable());
        assertReferenceDependentModuleImplementationsAvailable();
        administration.plugins().referenceDependentPlugin().externalReferenceModuleWithI18n().disable();
        assertFalse(administration.plugins().referencePlugin().moduleType().internalWithI18n().isAvailable());
        assertTrue(administration.plugins().referencePlugin().moduleType().internalWithoutI18n().isAvailable());
        assertFalse(administration.plugins().referenceDependentPlugin().externalReferenceModuleWithI18n().isAvailable());
        assertTrue(administration.plugins().referenceDependentPlugin().externalReferenceModuleWithoutI18n().isAvailable());
    }

    private void assertAllModuleImplementationAvailable()
    {
         assertReferenceModuleImplementationsAvailable();
         assertReferenceDependentModuleImplementationsAvailable();
    }

    private void assertNoModuleImplementationAvailable()
    {
         assertReferenceModuleImplementationsNotAvailable();
         assertReferenceDependentModuleImplementationsNotAvailable();
    }

    private void assertReferenceModuleImplementationsAvailable()
    {
         assertTrue(referencePlugin.moduleType().internalWithI18n().isAvailable());
         assertTrue(referencePlugin.moduleType().internalWithoutI18n().isAvailable());
    }

    private void assertReferenceModuleImplementationsNotAvailable()
    {
         assertFalse(referencePlugin.moduleType().internalWithI18n().isAvailable());
         assertFalse(referencePlugin.moduleType().internalWithoutI18n().isAvailable());
    }

    private void assertReferenceDependentModuleImplementationsAvailable()
    {
         assertTrue(referenceDependentPlugin.externalReferenceModuleWithI18n().isAvailable());
         assertTrue(referenceDependentPlugin.externalReferenceModuleWithoutI18n().isAvailable());
    }

    private void assertReferenceDependentModuleImplementationsNotAvailable()
    {
         assertFalse(referenceDependentPlugin.externalReferenceModuleWithI18n().isAvailable());
         assertFalse(referenceDependentPlugin.externalReferenceModuleWithoutI18n().isAvailable());
    }
}
