package com.atlassian.jira.upgrade;

import java.util.Collection;

import javax.servlet.ServletContextEvent;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.Shutdown;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockServletContext;

import org.junit.Test;

public class TestConsistencyCheckImpl
{
    /**
     * Check that the consistencyCheckImpl calls destroy for each service, making sure any service that throws a
     * RuntimeException does not prevent other services from being destroyed. JRA-12338
     */
    @Test
    public void testConsitencyCheckImplDestroy()
    {
        //mock out services to test with. Make the middle service throw an exception so to assert
        //rest of the services call the destroy method
        final Mock mockService1 = new Mock(JiraServiceContainer.class);
        mockService1.expectVoid("destroy");

        final Mock mockService2 = new Mock(JiraServiceContainer.class);
        mockService2.expectAndThrow("destroy", new RuntimeException("Exception caused for testing"));

        final Mock mockService3 = new Mock(JiraServiceContainer.class);
        mockService3.expectVoid("destroy");

        final Mock mockTaskManager = new Mock(TaskManager.class);
        mockTaskManager.setStrict(true);
        mockTaskManager.expectAndReturn("shutdownAndWait", new Constraint[] { P.eq(0L) }, Boolean.TRUE);

        final Mock mockIndexManager = new Mock(IssueIndexManager.class);
        mockIndexManager.setStrict(true);
        mockIndexManager.expectVoid("shutdown");

        final Mock mockComponentManager = new Mock(Shutdown.class);
        mockIndexManager.setStrict(true);
        mockIndexManager.expectVoid("shutdown");

        final Collection services = EasyList.build(mockService1.proxy(), mockService2.proxy(), mockService3.proxy());
        final ConsistencyCheckImpl consistencyChecker = new ConsistencyCheckImpl()
        {
            @Override
            protected Collection getServices()
            {
                return services;
            }

            //used for unit testing.
            @Override
            TaskManager getTaskManager()
            {
                return (TaskManager) mockTaskManager.proxy();
            }

            //used for unit testing.
            @Override
            IssueIndexManager getIndexManager()
            {
                return (IssueIndexManager) mockIndexManager.proxy();
            }

            @Override
            Shutdown getComponentManager()
            {
                return (Shutdown) mockComponentManager.proxy();
            }
        };
        final MockServletContext mockServletContext = new MockServletContext();
        mockServletContext.setAttribute(ConsistencyCheckImpl.INIT_KEY, true);
        final ServletContextEvent servletContextEvent = new ServletContextEvent(mockServletContext);
        consistencyChecker.destroy(servletContextEvent.getServletContext());

        //verify that all the services destroy methods were called
        mockService1.verify();
        mockService2.verify();
        mockService3.verify();
        mockTaskManager.verify();
        mockIndexManager.verify();
        mockComponentManager.verify();
    }
}
