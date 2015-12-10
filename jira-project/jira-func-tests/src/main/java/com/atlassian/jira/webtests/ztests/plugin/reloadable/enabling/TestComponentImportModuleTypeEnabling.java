package com.atlassian.jira.webtests.ztests.plugin.reloadable.enabling;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.plugin.reloadable.AbstractReloadablePluginsTest;

/**
 * <p>Responsible for verifying that a component imported from another plugin works as expected after both plugins have
 * been enabled.
 *
 * This scenario assumes that the plugin have never been enabled and that are loaded in a disabled state when
 * JIRA starts up.</p>
 * <br/>
 * <p>This is also what we call the from ZERO to ON scenario.</p>
 *
 * @since v4.3
 */
@WebTest({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT})
public class TestComponentImportModuleTypeEnabling extends AbstractReloadablePluginsTest
{
    private static final String REFERENCE_COMPONENT_IMPORT_ACTION_URL = "/ReferenceComponentImportAction.jspa";
    private static final String EXPECTED_TEXT_FROM_REFERENCE_PUBLIC_COMPONENT = "This is a simple message exported by the "
            + "JIRA reference plugin";
    private static final String REFERENCE_COMPONENT_MESSAGE_CONTAINER_ID = "reference-component-message";

    public void testShouldExistAndBeAccessibleAfterEnablingThePlugin() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        administration.plugins().referenceDependentPlugin().enable();

        tester.gotoPage(REFERENCE_COMPONENT_IMPORT_ACTION_URL);
        text.assertTextPresent(locator.id(REFERENCE_COMPONENT_MESSAGE_CONTAINER_ID),
                EXPECTED_TEXT_FROM_REFERENCE_PUBLIC_COMPONENT);
    }
}