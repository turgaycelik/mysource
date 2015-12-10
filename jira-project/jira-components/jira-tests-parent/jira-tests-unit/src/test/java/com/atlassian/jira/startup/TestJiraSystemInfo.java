package com.atlassian.jira.startup;

import com.atlassian.jira.mock.servlet.MockServletContext;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.BuildUtilsInfoImpl;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestJiraSystemInfo
{


    @Test
    public void testobtainBasicInfo() throws Exception
    {

        BuildUtilsInfo buildUtilsInfo = new BuildUtilsInfoImpl();
        FormattedLogMsg logMsg = new FormattedLogMsg();

        JiraSystemInfo jiraSystemInfo = new JiraSystemInfo(logMsg, buildUtilsInfo);


        MockServletContext context = new MockServletContext();
        jiraSystemInfo.obtainBasicInfo(context);

        String logText = logMsg.toString();

        assertTrue(logText.contains("JIRA Build"));
        assertTrue(logText.contains("Build Date"));
        assertTrue(logText.contains("JIRA Installation Type"));
        assertTrue(logText.contains("Application Server"));
        assertTrue(logText.contains("Java Version"));
        assertTrue(logText.contains("Current Working Director"));
        assertTrue(logText.contains("Maximum Allowable Memory"));
        assertTrue(logText.contains("Total Memory"));
        assertTrue(logText.contains("Free Memory"));
        assertTrue(logText.contains("Used Memory"));
        // We don't assert that Memory pools exist because some JVMs may not expose this data
        assertTrue(logText.contains("JVM Input Arguments"));
        // We don't assert that Applied Patches exist because some JVMs may not expose this data
    }

}
