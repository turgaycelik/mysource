package com.atlassian.jira.functest.framework.navigation.issue;

import com.atlassian.jira.testkit.client.log.FuncTestLogger;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @since v4.2
 */
public class DefaultImageAttachmentsGallery implements ImageAttachmentsGallery
{
    private WebTester tester;
    private static final String ISSUE_KEY_ID = "key-val";
    private FuncTestLogger logger;

    public DefaultImageAttachmentsGallery(final WebTester tester, final FuncTestLogger logger)
    {
        this.tester = tester;
        this.logger = logger;
    }

    public List<ImageAttachmentItem> get()
    {
        tester.assertElementPresent(ISSUE_KEY_ID);
        Locator issueKeyLocator = new IdLocator(tester, ISSUE_KEY_ID);

        logger.log("Beginning to retrieve image attachments for the issue: " + issueKeyLocator.getText());

        final List<ImageAttachmentItem> attachments = new ArrayList<ImageAttachmentItem>();
        XPathLocator xPathLocator = new XPathLocator(tester,
                "//*[@id='attachment_thumbnails']/li[contains(concat(' ', normalize-space(@class), ' '), ' attachment-content ')]");

        for (int i = 0; i < xPathLocator.getNodes().length; i++)
        {
            Node node = xPathLocator.getNodes()[i];

            XPathLocator nameLocator = new XPathLocator(node, "./dl/dt/a[@class='attachment-title']");
            XPathLocator sizeLocator = new XPathLocator(node, "./dl/dd[@class='attachment-size']");
            attachments.add(new ImageAttachmentItem(nameLocator.getText(), sizeLocator.getText()));
        }
        logger.log("Finished retrieving image attachments for the issue: " + issueKeyLocator.getText());
        return attachments;
    }
}
