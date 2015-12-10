package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.page.ViewIssuePage;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @since v6.2
 */
@WebTest ({ FUNC_TEST, UPGRADE_TASKS })
public class TestUpgradeTask6208 extends FuncTestCase
{
    private Map<String, String>  issueCreators;
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueCreators = MapBuilder.newBuilder("HSP-1","Anonymous")
                                  .add("HSP-2", "Barney Rubble")
                                  .add("HSP-3", "BamBam Rubble")
                                  .add("HSP-4", "betty")
                                  .add("HSP-5", "Fred Flinstone")
                                  .add("HSP-6", "Pebble Flinstone")
                                  .add("HSP-7", "Wilma Flinstone")
                                  .add("HSP-8", "Wilma Flinstone")
                                  .toMap();
    }

    public void testCreatorsCreatedAsExpected()
    {
        administration.restoreData("TestUpgradeTask6208.xml");
        for (Map.Entry<String, String> entry : issueCreators.entrySet())
        {
            assertIssueCreator(entry.getKey(), entry.getValue());
        }
    }

    private void assertIssueCreator(final String issueKey, final String creator)
    {
        ViewIssuePage viewIssuePage = navigation.issue().viewIssue(issueKey).openTabWithId("changehistory-tabpanel");
        assertEquals(creator, viewIssuePage.getCreatorUserName());
    }

    private List<String> getTextFromNodes(final String selector)
    {
        Node[] nodes = locator.css(selector).getNodes();
        return Lists.transform(Lists.newArrayList(nodes), new Function<Node, String>()
        {
            @Override
            public String apply(@Nullable final Node item)
            {
                return (item == null) ? null : String.valueOf(item.getNodeValue());
            }
        });
    }
}
