package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.functest.framework.LocatorFactory;
import net.sourceforge.jwebunit.WebTester;

/**
 * Represents reference module type in the reference plugin.
 *
 * @since v4.4
 */
public class ReferenceModuleType extends ReferencePluginModule
{
    private static final String MODULE_TYPE_ACTION_URL = "/ReferenceModuleTypeAction.jspa";
    private static final String MODULE_LIST_CONTAINER_ID = "reference-moduletypes-list";

    private static final int HTTP_ERROR_CODE = 404;

    private static final String MODULE_KEY = "reference-module";
    private static final String MODULE_NAME = "Reference Module Type";

    private final WebTester webTester;
    private final ReferenceModuleImplementation internalWithI18n;
    private final ReferenceModuleImplementation internalWithoutI18n;

    public ReferenceModuleType(WebTester webTester, Administration administration, LocatorFactory locators)
    {
        super(administration);
        this.webTester = webTester;
        this.internalWithI18n = new ReferenceModuleImplementation(ReferencePlugin.KEY, webTester, administration, locators,
                "internal-reference-module-with-i18n",
                "Internal Reference Module (with i18n)",
                "A reference internal 'implementation' of reference module type (with i18n).");
        this.internalWithoutI18n = new ReferenceModuleImplementation(ReferencePlugin.KEY, webTester, administration, locators,
                "internal-reference-module-without-i18n",
                "Internal Reference Module (without i18n)",
                "A reference internal 'implementation' of reference module type (without i18n).");
    }

    @Override
    public String moduleKey()
    {
        return MODULE_KEY;
    }

    @Override
    public String moduleName()
    {
        return MODULE_NAME;
    }

    public ReferenceModuleImplementation internalWithI18n()
    {
         return internalWithI18n;
    }

    public ReferenceModuleImplementation internalWithoutI18n()
    {
         return internalWithoutI18n;
    }

    public static class ReferenceModuleImplementation extends AbstractPluginModule
    {
        private final WebTester webTester;
        private final LocatorFactory locators;
        private final ModuleOutputVerifier verifier;

        protected ReferenceModuleImplementation(String pluginKey, WebTester webTester, Administration administration,
                LocatorFactory locators, String moduleKey, String moduleName, String moduleDesc)
        {
            super(pluginKey, administration);
            this.webTester = webTester;
            this.locators = locators;
            this.verifier = new ModuleOutputVerifier(moduleKey, moduleName, moduleDesc);
        }

        public boolean isAvailable()
        {
            webTester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
            webTester.gotoPage(MODULE_TYPE_ACTION_URL);
            if (webTester.getDialog().getResponse().getResponseCode() == HTTP_ERROR_CODE)
            {
                return false;
            }
            else
            {
                return locators.id(MODULE_LIST_CONTAINER_ID).getText().contains(verifier.assertionText());
            }
        }

        @Override
        public String moduleKey()
        {
            return verifier.moduleKey;
        }

        @Override
        public String moduleName()
        {
            return verifier.moduleName;
        }
    }

    private static class ModuleOutputVerifier
    {
        private final String moduleKey;
        private final String moduleName;
        private final String moduleDesc;


        public ModuleOutputVerifier(String moduleKey, String moduleName, String moduleDesc)
        {
            this.moduleKey = moduleKey;
            this.moduleName = moduleName;
            this.moduleDesc = moduleDesc;
        }

        String assertionText()
        {
            return moduleKey + ": " + moduleName + ": " + moduleDesc;
        }
    }
}
