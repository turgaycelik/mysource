/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.mockobjects.dynamic.Mock;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestJiraWorkflowComparator
{

    @Test
    public void testCompare()
    {
        JiraWorkflow jira = makeTestDefaultWorkflow();
        JiraWorkflow c = makeTestConfigurableWorkflow("Cataluna");
        JiraWorkflow b = makeTestConfigurableWorkflow("bodacious");
        JiraWorkflow a = makeTestConfigurableWorkflow("Absolutely");

        List list = new ArrayList();
        list.add(b);
        list.add(c);
        list.add(a);
        list.add(jira);
        list.add(makeTestConfigurableWorkflow("Cataluna"));

        Collections.sort(list);

        Iterator it = list.iterator();
        assertEquals(jira, it.next());
        assertEquals(a, it.next());
        assertEquals(b, it.next());
        assertEquals(c, it.next());
    }

    private JiraWorkflow makeTestDefaultWorkflow()
    {
        WorkflowDescriptor descriptor = new WorkflowDescriptor();

        Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        WorkflowManager manager = (WorkflowManager) mockWorkflowManager.proxy();

        return new DefaultJiraWorkflow(descriptor, manager, null);
    }

    private JiraWorkflow makeTestConfigurableWorkflow(String name)
    {
        Mock mockWorkflowManager = new Mock(WorkflowManager.class);
        WorkflowManager manager = (WorkflowManager) mockWorkflowManager.proxy();

        return new TestConfigurableJiraWorkflow(name, manager);
    }

    private class TestConfigurableJiraWorkflow extends AbstractJiraWorkflow
    {
        String name;

        public TestConfigurableJiraWorkflow(String name, WorkflowManager workflowManager)
        {
            super(workflowManager, new WorkflowDescriptor());
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public boolean isDraftWorkflow()
        {
            return false;
        }
    }
}
