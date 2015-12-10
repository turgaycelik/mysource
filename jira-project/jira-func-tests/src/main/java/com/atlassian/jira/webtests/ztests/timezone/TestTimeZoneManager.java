package com.atlassian.jira.webtests.ztests.timezone;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.TIME_ZONES;

/**
 * @since v4.4
 */
@WebTest ( { FUNC_TEST, TIME_ZONES })
public class TestTimeZoneManager extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestTimeZoneManager.xml");
        administration.generalConfiguration().setDefaultUserTimeZone("Australia/Sydney");
    }

    public void testTimeZoneManager() throws Exception
    {
        navigation.login("fred");
        tester.gotoPage("/plugins/servlet/functest-timezone-servlet");
        tester.assertTextPresent("DefaultTimeZone=Australia/Sydney");
        tester.assertTextPresent("UserTimeZone=Europe/Berlin");
    }
}
