package com.atlassian.jira.webtests.ztests.user;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import com.google.common.collect.ImmutableMap;

import org.junit.Assert;

/**
 * @since v6.3.3
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUE_LINKS, Category.UPGRADE_TASKS })
public class TestUpgradeTask_Build6331 extends FuncTestCase
{

    public void testFixingIncorrectWatchCountForClonedIssues()
    {
        administration.restoreDataWithBuildNumber("TestUpgradeTask_Build6331.xml", 6330);

        Assert.assertEquals(Long.valueOf(1L), getWatches(10000));
        Assert.assertEquals(Long.valueOf(1L), getWatches(10001)); // before fix it is 2 and should be fixed to 1
        Assert.assertEquals(Long.valueOf(1L), getWatches(10002)); // before fix it is 3 and should be fixed to 1
        Assert.assertEquals(Long.valueOf(0L), getWatches(10004)); // before fix it is 1 and should be fixed to 0
    }

    private Long getWatches(long id) {
        // FIXME:
        // it is not List<Map<String, String>> but List<Map<String, Object>>
        // it returns also Integer (e.g.: watches) as a value not only String
        List<Map<String, Object>> issues = (List) backdoor.entityEngine().findByAnd("Issue", ImmutableMap.of("id", id));
        assertEquals("Issue with id: " + id + " must exists!", 1, issues.size());
        return Long.parseLong(String.valueOf(issues.get(0).get("watches")));
    }

}
