package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.AdvancedApplicationProperties;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.xml.sax.SAXException;

/**
 *
 * @since v4.4.4
 */
@WebTest ({Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestAdvancedApplicationProperties extends FuncTestCase
{
    private static final String SUBTASK_KEY = "jira.table.cols.subtasks";
    private static final String SUBTASK_COLUMN = "customfield_10000";


    public void testAdvancedSettings()
    {
        administration.restoreBlankInstance();
        AdvancedApplicationProperties properties = administration.advancedApplicationProperties();
        assertTrue(properties.getApplicationProperties().size() > 0);
    }

    public void testSubTaskColumnsAreVisibleWithoutRestart()
    {
        administration.restoreData("TestAdvancedApplicationProperties.xml");
        AdvancedApplicationProperties properties = administration.advancedApplicationProperties();
        String subtaskColumns = properties.getApplicationProperty(SUBTASK_KEY);
        subtaskColumns += String.format(", %s",SUBTASK_COLUMN);
        properties.setApplicationProperty(SUBTASK_KEY, subtaskColumns);
        navigation.issue().viewIssue("HSP-1");
        getAssertions().assertNodeHasText(new CssLocator(tester, "."+SUBTASK_COLUMN), "You should see this in the subtask column view.");
    }



}
