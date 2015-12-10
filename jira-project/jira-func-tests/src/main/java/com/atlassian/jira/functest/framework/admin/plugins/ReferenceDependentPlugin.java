package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.functest.framework.LocatorFactory;
import net.sourceforge.jwebunit.WebTester;

/**
 * Represents the Reference Dependent Plugin. This plugin has dependencies on reference implementations of the
 * extension points in JIRA.
 *
 * We use it to test inter-plugin dependencies (i.e. component-import ...)
 *
 * @see ReferencePlugin
 * @since v4.4
 */
public class ReferenceDependentPlugin extends Plugin
{
    public static final String KEY = "com.atlassian.jira.dev.reference-dependent-plugin";

    private final Administration administration;
    private final ReferenceModuleType.ReferenceModuleImplementation externalWithI18n;
    private final ReferenceModuleType.ReferenceModuleImplementation externalWithoutI18n;

    public ReferenceDependentPlugin(final WebTester tester, final Administration administration, final LocatorFactory locators)
    {
        super(administration);
        this.administration = administration;
        this.externalWithI18n = new ReferenceModuleType.ReferenceModuleImplementation(KEY, tester, administration, locators,
                "external-reference-module-with-i18n",
                "External Reference Module (with i18n)",
                "A reference external 'implementation' of reference module type (with i18n).");
        this.externalWithoutI18n = new ReferenceModuleType.ReferenceModuleImplementation(KEY, tester, administration, locators,
                "external-reference-module-without-i18n",
                "External Reference Module (without i18n)",
                "A reference external 'implementation' of reference module type (without i18n).");
    }


    public String getKey()
    {
        return KEY;
    }


    public ReferenceModuleType.ReferenceModuleImplementation externalReferenceModuleWithI18n()
    {
        return externalWithI18n;
    }

    public ReferenceModuleType.ReferenceModuleImplementation externalReferenceModuleWithoutI18n()
    {
        return externalWithoutI18n;
    }
}
