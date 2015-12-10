package com.atlassian.jira.functest.framework.navigation.issue;

import com.atlassian.jira.testkit.client.log.FuncTestLogger;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.meterware.httpunit.WebLink;
import net.sourceforge.jwebunit.WebTester;
import org.xml.sax.SAXException;

/**
 *
 * @since v4.1
 */
public class DefaultAttachmentManagement implements AttachmentManagement
{
    private final WebTester tester;
    private final FuncTestLogger logger;
    private static final String ISSUE_KEY_ID = "key-val";

    public DefaultAttachmentManagement(WebTester tester, final FuncTestLogger logger)
    {
        this.tester = tester;
        this.logger = logger;
    }

    public void delete()
    {
        tester.assertElementPresent("manage-attachments");

        Locator issueKeyLocator = new IdLocator(tester, ISSUE_KEY_ID);
        logger.log("Beginning to delete attachments for issue:  " + issueKeyLocator.getText());

        final WebLink[] links;
        try
        {
            links = tester.getDialog().getResponse().getLinks();
        }
        catch (SAXException e)
        {
            throw new RuntimeException("Error while trying to parse the links of the Manage Attachments page", e);
        }

        for (WebLink link : links)
        {
            if (link.getID().startsWith("del_"))
            {
                tester.clickLink(link.getID());
                tester.submit("Delete");
            }
        }
        logger.log("Finished deleting attachments for issue:  " + issueKeyLocator.getText());
    }

    public String downloadAttachmentAsString(long id, String title)
    {
        tester.gotoPage("/secure/attachment/" + id + "/" + title);
        return tester.getDialog().getResponseText();
    }
}
