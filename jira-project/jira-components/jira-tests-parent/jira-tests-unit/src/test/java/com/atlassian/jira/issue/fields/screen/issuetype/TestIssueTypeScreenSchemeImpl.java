package com.atlassian.jira.issue.fields.screen.issuetype;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.MockFieldScreenScheme;
import com.atlassian.jira.issue.issuetype.MockIssueType;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

public class TestIssueTypeScreenSchemeImpl
{
    @Test
    public void testConcurrentLoadingOfInternalEntities() throws Exception
    {
        for (int i = 0; i < 10; i++)
        {
            _testConcurrentLoadingOfInternalEntities();
        }
    }

    public void _testConcurrentLoadingOfInternalEntities() throws Exception
    {
        final CountDownLatch waitForTest = new CountDownLatch(1);
        final CountDownLatch waitForBothThreadsRunning = new CountDownLatch(2);
        final Object issueTypeScreenSchemeEntityDelegate = new Object()
        {
            public String getIssueTypeId()
            {
                return null;
            }
        };

        final Object issueTypeScreenSchemeEntityProxy = DuckTypeProxy.getProxy(IssueTypeScreenSchemeEntity.class, issueTypeScreenSchemeEntityDelegate);

        final Object managerDelegate = new Object()
        {
            public Collection getIssueTypeScreenSchemeEntities(final IssueTypeScreenScheme issueTypeScreenScheme)
            {
                try
                {
                    waitForTest.await();
                }
                catch (final InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return EasyList.build(issueTypeScreenSchemeEntityProxy);
            }
        };
        final IssueTypeScreenSchemeManager manager = (IssueTypeScreenSchemeManager) DuckTypeProxy.getProxy(IssueTypeScreenSchemeManager.class,
                managerDelegate);
        final IssueTypeScreenScheme scheme = new IssueTypeScreenSchemeImpl(manager, null);

        final ExecutorService pool = Executors.newFixedThreadPool(2);
        class DefaultSchemeRetriever implements Callable
        {
            public Object call() throws Exception
            {
                waitForBothThreadsRunning.countDown();
                return scheme.getEntity(null);
            }
        }
        final Future future1 = pool.submit(new DefaultSchemeRetriever());
        final Future future2 = pool.submit(new DefaultSchemeRetriever());
        waitForBothThreadsRunning.await();
        waitForTest.countDown();
        assertNotNull(future1.get());
        assertNotNull(future2.get());
        assertSame(issueTypeScreenSchemeEntityProxy, future1.get());
        assertSame(future1.get(), future2.get());
    }

    @Test
    public void testRemove() throws Exception
    {
        final Object issueTypeScreenSchemeEntityDelegate = new Object()
        {
            public String getIssueTypeId()
            {
                return null;
            }
        };

        final Object anotherTypeScreenSchemeEntityDelegate = new Object()
        {
            public String getIssueTypeId()
            {
                return "ANOTHER";
            }

            public void remove()
            {}
        };

        final Object defaultIssueTypeScreenSchemeEntityProxy = DuckTypeProxy.getProxy(IssueTypeScreenSchemeEntity.class,
                issueTypeScreenSchemeEntityDelegate);
        final Object anotherIssueTypeScreenSchemeEntityProxy = DuckTypeProxy.getProxy(IssueTypeScreenSchemeEntity.class,
                anotherTypeScreenSchemeEntityDelegate);

        final Object managerDelegate = new Object()
        {
            public Collection getIssueTypeScreenSchemeEntities(final IssueTypeScreenScheme issueTypeScreenScheme)
            {
                return EasyList.build(defaultIssueTypeScreenSchemeEntityProxy, anotherIssueTypeScreenSchemeEntityProxy);
            }
        };
        final IssueTypeScreenSchemeManager manager = (IssueTypeScreenSchemeManager) DuckTypeProxy.getProxy(IssueTypeScreenSchemeManager.class,
                managerDelegate);
        final IssueTypeScreenScheme scheme = new IssueTypeScreenSchemeImpl(manager, null);

        // check default
        assertNotNull(scheme.getEntity(null));

        // check the other one
        assertNotNull(scheme.getEntity("ANOTHER"));

        scheme.removeEntity("ANOTHER");

        // should be gone
        assertNull(scheme.getEntity("ANOTHER"));
    }

    @Test
    public void testAdd() throws Exception
    {
        final Object issueTypeScreenSchemeEntityDelegate = new Object()
        {
            public String getIssueTypeId()
            {
                return null;
            }

            public void store()
            {}
        };

        final Object anotherTypeScreenSchemeEntityDelegate = new Object()
        {
            public String getIssueTypeId()
            {
                return "ANOTHER";
            }

            public void remove()
            {}

            public void setIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme)
            {}

            public void store()
            {}
        };

        final Object defaultIssueTypeScreenSchemeEntityProxy = DuckTypeProxy.getProxy(IssueTypeScreenSchemeEntity.class,
                issueTypeScreenSchemeEntityDelegate);
        final Object managerDelegate = new Object()
        {
            public Collection getIssueTypeScreenSchemeEntities(final IssueTypeScreenScheme issueTypeScreenScheme)
            {
                return EasyList.build(defaultIssueTypeScreenSchemeEntityProxy);
            }
        };
        final IssueTypeScreenSchemeManager manager = (IssueTypeScreenSchemeManager) DuckTypeProxy.getProxy(IssueTypeScreenSchemeManager.class,
                managerDelegate);
        final IssueTypeScreenScheme scheme = new IssueTypeScreenSchemeImpl(manager, null);

        final IssueTypeScreenSchemeEntity anotherIssueTypeScreenSchemeEntityProxy = (IssueTypeScreenSchemeEntity) DuckTypeProxy.getProxy(
                IssueTypeScreenSchemeEntity.class, anotherTypeScreenSchemeEntityDelegate);

        // note: cannot check the internal state with a getEntity call as that will init it and we don't want it inited

        // add the other one and hope it doesn't blow up
        scheme.addEntity(anotherIssueTypeScreenSchemeEntityProxy);

        // check default
        assertNotNull(scheme.getEntity(null));

        // should be there now
        assertNotNull(scheme.getEntity("ANOTHER"));
    }

    @Test
    public void getActualFieldScreenSchemeReturnsDefaultScreenWithNoExplicitMapping()
    {
        final IssueTypeScreenSchemeManager screenManager = Mockito.mock(IssueTypeScreenSchemeManager.class);

        //Create IssueTypeScreenSchemeEntity objects that do the mapping:
        // * -> SS(10)
        final MockIssueTypeScreenSchemeEntity defaultSchemeEntity = new MockIssueTypeScreenSchemeEntity();
        final MockFieldScreenScheme fieldScreenScheme = defaultSchemeEntity.createFieldScreenScheme(10);
        final MockIssueType issueType = new MockIssueType("id", "ID");

        final IssueTypeScreenSchemeImpl issueTypeScreenScheme = new IssueTypeScreenSchemeImpl(screenManager, null);
        when(screenManager.getIssueTypeScreenSchemeEntities(issueTypeScreenScheme))
                .thenReturn(Arrays.asList(defaultSchemeEntity));

        assertThat(issueTypeScreenScheme.getEffectiveFieldScreenScheme(issueType),
                Matchers.<FieldScreenScheme>equalTo(fieldScreenScheme));
    }

    @Test
    public void getActualFieldScreenSchemeReturnsScreenFromExplicitMapping()
    {
        final MockIssueType issueType = new MockIssueType("id", "ID");

        final IssueTypeScreenSchemeManager screenManager = Mockito.mock(IssueTypeScreenSchemeManager.class);

        //Create IssueTypeScreenSchemeEntity objects that do the mapping:
        // * -> SS(10)
        // IT(ID) -> SS(11)
        final MockIssueTypeScreenSchemeEntity defaultSchemeEntity = new MockIssueTypeScreenSchemeEntity();
        defaultSchemeEntity.createFieldScreenScheme(10);
        final MockIssueTypeScreenSchemeEntity idSchemeEntity = new MockIssueTypeScreenSchemeEntity()
                .issueType(issueType);
        idSchemeEntity.createFieldScreenScheme(11);

        final IssueTypeScreenSchemeImpl issueTypeScreenScheme = new IssueTypeScreenSchemeImpl(screenManager, null);
        when(screenManager.getIssueTypeScreenSchemeEntities(issueTypeScreenScheme))
                .thenReturn(Arrays.asList(defaultSchemeEntity, idSchemeEntity));

        assertThat(issueTypeScreenScheme.getEffectiveFieldScreenScheme(issueType),
                Matchers.equalTo(idSchemeEntity.getFieldScreenScheme()));
    }
}
