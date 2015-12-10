package com.atlassian.jira.webtests.ztests.indexing;

import java.text.ParseException;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.Instruments;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@WebTest ({ Category.FUNC_TEST, Category.INDEXING })
public class TestIndexingResources extends FuncTestCase
{
    private Instruments instruments;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestOneProjectWithOneIssueType.xml");
        instruments = new Instruments(navigation, page);
    }

    public void testReindexingShouldNotChangeNumberOfOpenLuceneIndexes() throws ParseException
    {
        // GIVEN
        // make sure index'es are created and loaded by creating issue
        // and then forcing reindex
        backdoor.issues().createIssue(PROJECT_HOMOSAP_KEY, "issue1");
        administration.reIndex();

        long initialLuceneOpenIndexesCount = getCurrentOpenLuceneIndexesCount();
        // sanity check - there should be some indexes open, if not we should change method how we make sure index
        // is loaded
        assertThat("no open indexes", initialLuceneOpenIndexesCount, is(greaterThan(0l)));

        // WHEN
        administration.reIndex();

        // THEN
        long finalLuceneOpenIndexesCount = getCurrentOpenLuceneIndexesCount();
        // number of currently opened indexes must stay the same
        assertThat("number of opened indexes changed", finalLuceneOpenIndexesCount, is(equalTo(initialLuceneOpenIndexesCount)));

    }

    private long getCurrentOpenLuceneIndexesCount() throws ParseException
    {
        final Instruments.Counters currentCounters = instruments.readAllCounters();
        final long luceneOpenCount = currentCounters.getCounter("searcher.lucene.open").getValue().or(0l);
        final long luceneCloseCount = currentCounters.getCounter("searcher.lucene.close").getValue().or(0l);

        return luceneOpenCount - luceneCloseCount;
    }
}
