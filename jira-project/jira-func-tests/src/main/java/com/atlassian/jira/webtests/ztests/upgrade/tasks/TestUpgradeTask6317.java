package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;

/**
 * @since v6.3
 */
@WebTest ({ FUNC_TEST, UPGRADE_TASKS })
public class TestUpgradeTask6317 extends FuncTestCase
{

    public void testFiltersSubscriptions()
    {
        administration.restoreData("TestUpgradeTask6317.xml");
        assertEquals("0 0 1 ? * *", backdoor.filterSubscriptions().getCronForSubscription(10010L));
        assertEquals("0 0 3 ? * *", backdoor.filterSubscriptions().getCronForSubscription(10021L));
        assertEquals("0 0 4 ? * *", backdoor.filterSubscriptions().getCronForSubscription(10022L));
        assertEquals("0 0 5 ? * *", backdoor.filterSubscriptions().getCronForSubscription(10023L));
        assertEquals("0 2 1 ? * *", backdoor.filterSubscriptions().getCronForSubscription(10024L));

    }
}
