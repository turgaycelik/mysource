package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.SearchClient;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestCreateConcurrentIssues extends FuncTestCase
{
    /**
     * The username prefix.
     */
    private final String USERNAME_PREFIX = "Tester_";

    /**
     * Sequence used for generating user names.
     */
    private final AtomicInteger USER_SEQUENCE = new AtomicInteger(0);

    /*
     * Use 1 backdoor per thread as these aren't thread safe.
     */
    private final ThreadLocal<Backdoor> THREAD_BACKDOOR = new ThreadLocal<Backdoor>()
    {
        @Override
        protected Backdoor initialValue()
        {
            // brand spanking new backdoor
            return new Backdoor(getEnvironmentData());
        }
    };

    /*
     * Use 1 user per thread to avoid deadlocks caused by concurrent login (JRA-26397)
     */
    private final ThreadLocal<String> THREAD_USER = new ThreadLocal<String>()
    {
        @Override
        protected String initialValue()
        {
            return USERNAME_PREFIX + USER_SEQUENCE.getAndIncrement();
        }
    };

    public void testCreateIssues() throws Exception
    {
        final int THREADS = 50;
        backdoor.restoreBlankInstance();

        // create the test users
        backdoor.usersAndGroups().addUsers(USERNAME_PREFIX, USERNAME_PREFIX, THREADS);

        final SearchClient searchClient = new SearchClient(getEnvironmentData());
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completeLatch = new CountDownLatch(THREADS);
        final ExecutorService execService = Executors.newFixedThreadPool(THREADS);
        Callable<Void> task = new Callable<Void>()
        {
            public Void call() throws Exception
            {
                String reporter = THREAD_USER.get();
                try
                {
                    startLatch.await();

                    // one user per thread so we don't run into JRA-26397
                    THREAD_BACKDOOR.get().issues().loginAs(reporter).createIssue(10000, "Simple Issue by " + reporter);
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Error creating issue as: " + reporter, e);
                }
                finally
                {
                    completeLatch.countDown();
                }
                return null;
            }
        };

        final List<Future<Void>> futures = new ArrayList<Future<Void>>();

        for (int i = 0; i < THREADS; i++)
        {
            futures.add(execService.submit(task));
        }
        startLatch.countDown();
        for (Future<Void> future : futures)
        {
            future.get();
        }
        SearchResult hspResults = searchClient.loginAs("admin").postSearch(new SearchRequest().jql("project = HSP"));
        assertThat(hspResults.total, equalTo(50));
        assertThat(hspResults.issues.size(), equalTo(50));
        assertNoDuplicateKeys(hspResults.issues);

    }

    private void assertNoDuplicateKeys(List<Issue> issues)
    {
        Collections.sort(issues, new Comparator<Issue>()
        {
            @Override
            public int compare(Issue issue, Issue issue1)
            {
                return issue.key.compareTo(issue1.key);
            }
        });
        boolean foundDuplicate = false;
        String key = issues.get(0).key;
        for (Issue issue : issues.subList(1, issues.size()))
        {
            foundDuplicate = issue.key.equals(key);
            key = issue.key;
            if (foundDuplicate)
            {
                break;
            }
        }
        assertFalse(foundDuplicate);

    }
}
