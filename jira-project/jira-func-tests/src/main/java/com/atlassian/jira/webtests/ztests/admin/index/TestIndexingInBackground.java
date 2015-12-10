package com.atlassian.jira.webtests.ztests.admin.index;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.backdoor.IndexingControl;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.INDEXING })
public class TestIndexingInBackground extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        backdoor.restoreBlankInstance();
    }

    public void testBackgroundReindexingSmokeTest() throws Exception
    {
        final String summary = "first issue!";
        final String reporter = "admin";

        // create an issue and reindex
        IssueCreateResponse testIssue = backdoor.issues().createIssue("HSP", summary, reporter);
        assertTrue(backdoor.indexing().isIndexConsistent());
        new BackgroundReindex().run().waitForCompletion();
        assertTrue(backdoor.indexing().isIndexConsistent());

        // run sanity check on issue created above
        SearchResult all = backdoor.search().getSearch(new SearchRequest());
        assertThat(all.total, equalTo(1));

        Issue issue = all.issues.get(0);
        assertThat(issue.key, equalTo(testIssue.key()));
        assertThat(issue.fields.summary, equalTo(summary));
        assertThat(issue.fields.reporter.name, equalTo(reporter));
    }

    public void testEditingAnIssueDuringABackgroundReindexShouldNotCauseLostUpdates() throws Exception
    {
        // create an issue for testing
        final IssueCreateResponse testIssue = backdoor.issues().createIssue("HSP", "first issue!", "admin");
        assertTrue(backdoor.indexing().isIndexConsistent());

        final BackgroundReindex reindex = new BackgroundReindex();
        // do an "online" edit while background reindex is in progress
        reindex.runConcurrently(new Runnable()
        {
            @Override
            public void run()
            {
                // dates only have second precision in lucene. so wait a 1.1 seconds to make sure we don't
                // hit that window.
                sleep(1100);

                // re-assign to fred
                backdoor.usersAndGroups().addUserToGroup("fred", "jira-developers");
                backdoor.issues().assignIssue(testIssue.key(), "fred");

                // now test that the issue has been indexed "online"
                SearchResult results = backdoor.search().getSearch(new SearchRequest().jql("assignee = fred"));
                assertThat("Single issue indexing during background reindex did not work", results.total, equalTo(1));
                assertThat("Single issue indexing during background reindex did not work", results.issues.get(0).key, equalTo(testIssue.key()));
            }
        }).waitForCompletion();

        // make sure the edited issue's updates have not been lost in the index
        SearchResult results = backdoor.search().getSearch(new SearchRequest().jql("assignee = fred"));
        assertThat(results.total, equalTo(1));
        assertThat("Detected lost update in index", results.issues.get(0).key, equalTo(testIssue.key()));
    }

    public void testBackgroundReindexWillReindexIssueEvenIfPreviousIndexingOperationWasLost() throws Exception
    {
        // create an issue for testing
        final IssueCreateResponse testIssue = backdoor.issues().createIssue("HSP", "first issue!", "admin");
        assertTrue(backdoor.indexing().isIndexConsistent());

        // dates only have second precision in lucene. so wait a 1.1 seconds to make sure we don't
        // hit that window.
        sleep(1100);
        backdoor.issueNavControl().touch(testIssue.key());
        assertThat(backdoor.indexing().isIndexUpdatedFieldConsistent(), equalTo(false));

        backdoor.indexing().startInBackground().waitForCompletion();
        assertThat(backdoor.indexing().isIndexUpdatedFieldConsistent(), equalTo(true));
    }

    public void testBackgroundReindexWithComments() throws Exception
    {
        // create issues and comments for testing
        final IssueCreateResponse testIssue = backdoor.issues().createIssue("HSP", "first issue!", "admin");
        backdoor.issues().commentIssue(testIssue.key, "First searchable comment. Magic word is hamster");
        final IssueCreateResponse testIssue2 = backdoor.issues().createIssue("HSP", "second issue!", "admin");
        backdoor.issues().commentIssue(testIssue2.key, "Second searchable comment. Magic word is Basil");

        assertTrue(backdoor.indexing().isIndexConsistent());

        // Now we will deindex the issue and add it back, but without the comments
        backdoor.indexing().deindex(testIssue.key);
        backdoor.indexing().deindex(testIssue2.key);

        backdoor.indexing().indexDummyIssue(Long.valueOf(testIssue.id), 10000, "1", testIssue.key, "first issue!", "first issue!");
        backdoor.indexing().indexDummyIssue(Long.valueOf(testIssue2.id), 10000, "1", testIssue2.key, "second issue!", "second issue!");

        // Searching for issue by comment should find nothing
        SearchResult result = backdoor.search().getSearch(new SearchRequest().jql("comment ~ hamster"));
        assertEquals(0, result.issues.size());
        result = backdoor.search().getSearch(new SearchRequest().jql("reporter WAS admin"));
        assertEquals(0, result.issues.size());

        // Reindex in the background no comments etc
        new BackgroundReindex(false, false).run().waitForCompletion();

        // Searching for issue by comment should find issue
        result = backdoor.search().getSearch(new SearchRequest().jql("comment ~ hamster"));
        assertEquals(0, result.issues.size());
        result = backdoor.search().getSearch(new SearchRequest().jql("assignee WAS admin"));
        assertEquals(0, result.issues.size());

        // Now reindex in the background.  All should be fixed
        new BackgroundReindex(true, true).run().waitForCompletion();

        // Searching for issue by comment should find issue
        result = backdoor.search().getSearch(new SearchRequest().jql("comment ~ hamster"));
        assertEquals(1, result.issues.size());
        result = backdoor.search().getSearch(new SearchRequest().jql("comment ~ magic"));
        assertEquals(2, result.issues.size());

        // Change result search should also find something
        result = backdoor.search().getSearch(new SearchRequest().jql("assignee WAS admin"));
        assertEquals(2, result.issues.size());

    }

    public void testConfigChangeDuringIndexGetsNewMessage() throws Exception
    {
        // We need an issue to get re-index mmessages to appear
        backdoor.issues().createIssue("HSP", "first issue!", "admin");
        assertTrue(backdoor.indexing().isIndexConsistent());

        // create an issue for testing
        final BackgroundReindex reindex = new BackgroundReindex();
        administration.generalConfiguration().disableVoting();
        administration.generalConfiguration().enableVoting();

        assertVotingMessage();

        // do an "online" edit while background reindex is in progress
        reindex.runConcurrently(new Runnable()
        {
            @Override
            public void run()
            {
                // Turn time tracking off and on to force a reindex message
                administration.generalConfiguration().disableVoting();
                administration.generalConfiguration().enableVoting();

                // make sure the reindex message is present still.
                assertVotingMessage();
            }
        }).waitForCompletion();
        assertTrue(backdoor.indexing().isIndexConsistent());
    }

    public void testIssueIndexIsCorrupt() throws Exception
    {
        // Restore data a small dataset
        administration.restoreData("TestIndexingCorrupt.xml");
        SearchResult result = backdoor.search().getSearch(new SearchRequest());
        assertEquals(20, result.issues.size());
        assertTrue(backdoor.indexing().isIndexConsistent());

        // Kill the index
        backdoor.indexing().deleteIndex();
        try
        {
            result = backdoor.search().getSearch(new SearchRequest());
            assertEquals(0, result.issues.size());
            assertFalse(backdoor.indexing().isIndexConsistent());

            // Add some issues.  These should go into the index,  but the original 20 will be missing
            for (int i = 0; i < 20; i++)
            {
                backdoor.issues().createIssue("HSP", "An issue which will be indexed " + i);
            }
            result = backdoor.search().getSearch(new SearchRequest());
            assertEquals(20, result.issues.size());
            assertFalse(backdoor.indexing().isIndexConsistent());
        }
        finally
        {
            // Make sure we don't leave the system in a bad state
            administration.reIndex();
        }
    }

    public void testReindexWhenIssuesMissing() throws Exception
    {
        // Restore data a small dataset
        administration.restoreData("TestIndexingCorrupt.xml");
        SearchResult result = backdoor.search().getSearch(new SearchRequest());
        assertEquals(20, result.issues.size());
        assertTrue(backdoor.indexing().isIndexConsistent());

        // Kill the index
        backdoor.indexing().deleteIndex();
        try
        {
            result = backdoor.search().getSearch(new SearchRequest());
            assertEquals(0, result.issues.size());
            assertFalse(backdoor.indexing().isIndexConsistent());

            // Add some issues.  These should go into the index,  but the original 20 will be missing
            for (int i = 0; i < 20; i++)
            {
                backdoor.issues().createIssue("HSP", "An issue which will be indexed " + i);
            }
            result = backdoor.search().getSearch(new SearchRequest());
            assertEquals(20, result.issues.size());
            assertFalse(backdoor.indexing().isIndexConsistent());

            // Now reindex in the background.  All should be fixed
            new BackgroundReindex(true, true).run().waitForCompletion();
            assertTrue(backdoor.indexing().isIndexConsistent());
        }
        finally
        {
            // Make sure we don't leave the system in a bad state
            administration.reIndex();
        }
    }

    public void testReindexWhenExtraIssues() throws Exception
    {
        // Restore data a small dataset
        administration.restoreData("TestIndexingCorrupt.xml");
        SearchResult result = backdoor.search().getSearch(new SearchRequest());
        assertEquals(20, result.issues.size());
        assertTrue(backdoor.indexing().isIndexConsistent());

        try
        {
            // Add some issues just to the index.  These should not go into the database.
            for (int i = 0; i < 20; i++)
            {
                backdoor.indexing().indexDummyIssue(1000 + i, 10000, "1", "HSP-" + (1000 + i), "New issue summary " + i, "new issue description" + i);
            }
            result = backdoor.search().getSearch(new SearchRequest());
            assertEquals(40, result.issues.size());
            assertFalse(backdoor.indexing().isIndexConsistent());

            // Now reindex in the background.  All should be fixed
            new BackgroundReindex().run().waitForCompletion();
            assertTrue(backdoor.indexing().isIndexConsistent());
            result = backdoor.search().getSearch(new SearchRequest());
            assertEquals(20, result.issues.size());
            assertTrue(backdoor.indexing().isIndexConsistent());
        }
        finally
        {
            // Make sure we don't leave the system in a bad state
            administration.reIndex();
        }
    }

    public void testCommentIndexIsCorrupt() throws Exception
    {
        // Restore data a small dataset
        administration.restoreData("TestIndexingCorrupt.xml");
        SearchResult result = backdoor.search().getSearch(new SearchRequest().jql("comment ~ again"));
        assertEquals(20, result.issues.size());
        assertTrue(backdoor.indexing().isIndexConsistent());

        // Kill the index
        backdoor.indexing().deleteIndex();
        try
        {
            assertFalse(backdoor.indexing().isIndexConsistent());
            result = backdoor.search().getSearch(new SearchRequest().jql("comment ~ again"));
            assertEquals(0, result.issues.size());

            // Assign a few issues.  This will re-index the issues, but not the comments
            for (int i = 0; i < 20; i++)
            {
                backdoor.issues().assignIssue("MKY-1", "admin");
                backdoor.issues().assignIssue("MKY-2", "admin");
                backdoor.issues().assignIssue("MKY-3", "admin");
                backdoor.issues().assignIssue("MKY-4", "admin");
                backdoor.issues().assignIssue("MKY-5", "admin");
            }
            result = backdoor.search().getSearch(new SearchRequest());
            assertEquals(5, result.issues.size());
            result = backdoor.search().getSearch(new SearchRequest().jql("comment ~ again"));
            assertEquals(0, result.issues.size());
            assertFalse(backdoor.indexing().isIndexConsistent());

            // Add some comments
            backdoor.issues().commentIssue("MKY-1", "This is a comment again");
            backdoor.issues().commentIssue("MKY-2", "This is a comment again");
            backdoor.issues().commentIssue("MKY-3", "This is a comment and again");
            backdoor.issues().commentIssue("MKY-4", "This is a comment");
            backdoor.issues().commentIssue("MKY-5", "This is a comment");
            result = backdoor.search().getSearch(new SearchRequest().jql("comment ~ again"));
            assertEquals(3, result.issues.size());

            assertFalse(backdoor.indexing().isIndexConsistent());
        }
        finally
        {
            // Make sure we don't leave the system in a bad state
            administration.reIndex();
        }
    }


    class BackgroundReindex
    {
        final boolean comments;
        final boolean changeHistory;
        
        
        private IndexingControl.IndexingProgress progress;

        BackgroundReindex() 
        {
            this.comments = false;
            this.changeHistory = false;
        }

        BackgroundReindex(final boolean comments, final boolean changeHistory) 
        {
            this.comments = comments;
            this.changeHistory = changeHistory;
        }

        public BackgroundReindex run()
        {
            progress = backdoor.indexing().startInBackground(comments, changeHistory);
            return this;
        }

        public BackgroundReindex waitForCompletion()
        {
            progress.waitForCompletion();
            return this;
        }

        public BackgroundReindex runConcurrently(final Runnable testCode)
        {
            backdoor.barrier().raiseBarrierAndRun("backgroundReindex", new Runnable()
            {
                @Override
                public void run()
                {
                    // kick off background reindex. this will block on the barrier
                    BackgroundReindex.this.run();
                    backdoor.indexing().getInBackgroundProgress().waitForIndexingStarted();

                    // run test code
                    testCode.run();
                }
            });

            return this;
        }
    }

    private void assertVotingMessage()
    {
        assertMessage("Voting");
    }

    private void assertMessage(final String section)
    {
        assertions.getTextAssertions().assertTextPresent(new CssLocator(tester, ".aui-message.info"),
                ADMIN_FULLNAME + " made configuration changes in section '" + section + "'");
    }

    private void sleep(int millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
