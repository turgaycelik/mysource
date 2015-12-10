package com.atlassian.jira.functest.unittests.url;

import com.atlassian.jira.functest.framework.util.url.URLUtil;
import junit.framework.TestCase;

/** @since v3.13 */
public class TestURLUtil extends TestCase
{

    public void testURLComparison()
    {
        assertTrue(URLUtil.compareURLStrings("ViewWorkflowSteps.jspa", "ViewWorkflowSteps.jspa"));
        assertTrue(URLUtil.compareURLStrings("ViewWorkflowSteps.jspa?stuff=dude", "ViewWorkflowSteps.jspa?stuff=dude"));
        assertTrue(URLUtil.compareURLStrings("ViewWorkflowSteps.jspa?workflowMode=live&workflowStep=1&workflowName=Dude+stuff",
                "ViewWorkflowSteps.jspa?workflowMode=live&workflowName=Dude+stuff&workflowStep=1"));
        assertTrue(URLUtil.compareURLStrings("http://localhost:8090/ViewWorkflowSteps.jspa?workflowMode=live&workflowStep=1&workflowName=Dude+stuff",
                "http://localhost:8090/ViewWorkflowSteps.jspa?workflowMode=live&workflowName=Dude+stuff&workflowStep=1"));
        assertFalse(URLUtil.compareURLStrings("http://localhost:8090/ViewWorkflowSteps.jspa?workflowMode=live&workflowStep=1&workflowName=Dude+stuff",
                "http://localhost:8090/ViewWorkflowSteps.jspa?workflowMode=live&workflowName=Dudestuff&workflowStep=1"));
        assertTrue(URLUtil.compareURLStrings("",
                ""));

        assertTrue(URLUtil.compareURLStrings("ViewWorkflowSteps.jspa?stuff=dude", "/jira/stuff/ViewWorkflowSteps.jspa?stuff=dude"));
    }
}
