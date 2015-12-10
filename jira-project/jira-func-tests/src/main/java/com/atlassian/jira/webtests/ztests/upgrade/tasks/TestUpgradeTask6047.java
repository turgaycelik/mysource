package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.AbstractJqlFuncTest;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.RENAME_USER;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Verify that the values in the username column in the table containing OAuth Service Provider Tokens are converted
 * to lowercase if they are mixed case.
 *
 * @since v6.0
 */
@WebTest ({ FUNC_TEST, RENAME_USER, UPGRADE_TASKS })
public class TestUpgradeTask6047 extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        administration.restoreDataWithBuildNumber("TestUpgradeTask6047.xml", 6030);
    }

    public void testSearchForMixedCaseUsernames()
    {

        // Make sure the token now contains the username in lowercase
        List<Map<String, String>> tokens = backdoor.entityEngine().findByAnd("OAuthServiceProviderToken", ImmutableMap.of("username", "mixed"));
        assertEquals(1, tokens.size());
        final Map<String, String> token = tokens.get(0);
        assertThat(token.get("username"), is("mixed"));
    }
}
