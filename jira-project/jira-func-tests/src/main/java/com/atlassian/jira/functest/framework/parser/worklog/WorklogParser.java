package com.atlassian.jira.functest.framework.parser.worklog;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses worklog off the view issue screen.
 *
 * @since v3.13
 */
public class WorklogParser
{
    public static List<Worklog> getWorklogs(final WebTester tester)
    {
        List<Worklog> worklogList = new ArrayList<Worklog>();
        XPathLocator xPathLocator = new XPathLocator(tester, "//div[@class='actionContainer']");
        for (int i = 0; i < xPathLocator.getNodes().length; i++)
        {
            Node actionContainerNode = xPathLocator.getNodes()[i];
            final String details = getTextFromSubNode(actionContainerNode, "div[@class='action-details']");
            //check if the action really is a work log
            if(details.indexOf("logged work") == -1)
            {
                continue;
            }
            Worklog worklog = new Worklog();
            worklog.setDetails(details);
            worklog.setTimeWorked(getTextFromSubNode(actionContainerNode, "div[@class='action-body']//dd[@class='worklog-duration']"));
            worklog.setLog(getTextFromSubNode(actionContainerNode, "div[@class='action-body']//dd[@class='worklog-comment']"));
            worklogList.add(worklog);
        }

        return worklogList;
    }

    private static String getTextFromSubNode(final Node node, final String xpath)
    {
        final String text = new XPathLocator(node, xpath).getText();
        if (text == null)
        {
            return null;
        }
        return text.trim();
    }
}
