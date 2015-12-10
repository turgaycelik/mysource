package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import junit.framework.Test;

/**
 * Test suite for all functional tests against plugin modules reloadability. 
 *
 * @since v4.3
 */
public class FuncTestSuiteReloadablePluginModules extends FuncTestSuite
{

    public static final FuncTestSuiteReloadablePluginModules SUITE = new FuncTestSuiteReloadablePluginModules();

    public static Test suite()
    {
        return SUITE;
    }

    public FuncTestSuiteReloadablePluginModules()
    {
        // DON'T add anything here. we run plugins tests using categorising test suite
        // this class will soon be removed as the old mechanism of collecting tests is not necessary any more
        // as we move forward
//        addTest(TestCustomFieldTypeModuleEnabling.class);
//        addTest(TestWebWork1ActionModuleEnabling.class);
//        addTest(TestComponentModuleTypeEnabling.class);
//        addTest(TestWorkflowFunctionModuleEnabling.class);
//        addTest(TestWorkflowConditionModuleEnabling.class);
//        addTest(TestWorkflowValidatorModuleEnabling.class);
//        addTest(TestComponentImportModuleTypeEnabling.class);
//        addTest(TestServletEnabling.class);
//        addTest(TestReportModuleEnabling.class);
//        addTest(TestModuleTypeModuleEnabling.class);
//        addTest(TestCustomFieldTypeSearcherEnabling.class);
//        addTest(TestRestModuleEnabling.class);
//        addTest(TestI18nResourceEnabling.class);
//        addTest(TestPortletModuleEnabling.class);
//        addTest(TestIssueTabPanelModuleTypeEnabling.class);
    }
}
