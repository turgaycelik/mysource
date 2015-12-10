package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @since v6.1
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.INDEXING })
public class TestEditIssueOnIndexing extends FuncTestCase
{

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    public void testUpdatedTimeInDBInSyncWithIndex() throws Exception
    {
        backdoor.issues().createIssue("MKY", "Test issue");
        waitForSecondSinceUpdatedFieldIsStoredInIndexWithPerSecondResolution();
        backdoor.issues().setDescription("MKY-1", "Test description!");
        assertThat(backdoor.indexing().isIndexUpdatedFieldConsistent(), equalTo(true));

        waitForSecondSinceUpdatedFieldIsStoredInIndexWithPerSecondResolution();
        //we make edit that does not change contents of issue and expect that index will be still in sync with db
        backdoor.issues().setDescription("MKY-1", "Test description!");

        assertThat(backdoor.indexing().isIndexUpdatedFieldConsistent(), equalTo(true));
    }

    private void waitForSecondSinceUpdatedFieldIsStoredInIndexWithPerSecondResolution() throws InterruptedException
    {
        // dates only have second precision in lucene. so wait a 1.1 seconds to make sure we don't
        // hit that window.
        Thread.sleep(1100);
    }

}
