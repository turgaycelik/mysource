package com.atlassian.jira.functest.framework.navigation.issue;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.testkit.client.log.FuncTestLogger;

import org.w3c.dom.Node;

import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;

/**
 * @see FileAttachmentsList
 * @since v4.2
 */
public class DefaultFileAttachmentsList extends Assert implements FileAttachmentsList
{
    private final static String ISSUE_KEY_ID = "key-val";
    private final WebTester tester;
    private final FuncTestLogger logger;
    private final AttachmentManagement attachmentManagement;

    private final ImageAttachmentsGallery imageAttachmentsGallery;

    public DefaultFileAttachmentsList(WebTester tester, FuncTestLogger logger)
    {
        this.tester = tester;
        this.logger = logger;
        this.attachmentManagement = new DefaultAttachmentManagement(tester, logger);
        this.imageAttachmentsGallery = new DefaultImageAttachmentsGallery(tester, logger);
    }

    public List<FileAttachmentsList.FileAttachmentItem> get()
    {
        tester.assertElementPresent(ISSUE_KEY_ID);
        Locator issueKeyLocator = new IdLocator(tester, ISSUE_KEY_ID);

        logger.log("Beginning to retrieve attachments for the issue: " + issueKeyLocator.getText());

        final List<FileAttachmentsList.FileAttachmentItem> fileAttachments = new ArrayList<FileAttachmentsList.FileAttachmentItem>();
        XPathLocator xPathLocator = new XPathLocator(tester, "//*[@id='file_attachments']/li[contains(concat(' ', normalize-space(@class), ' '), ' attachment-content ')]");

        for (Node node : xPathLocator.getNodes())
        {
            XPathLocator zipContents = new XPathLocator(node, ".//dd[@class='zip-contents']");
            if (zipContents.getNode() == null)
            {
                XPathLocator nameLocator = new XPathLocator(node, "./dl/dt[@class='attachment-title']/a");
                XPathLocator sizeLocator = new XPathLocator(node, "./dl/dd[@class='attachment-size']");
                XPathLocator authorLocator = new XPathLocator(node, "./dl/dd[@class='attachment-author']");
                XPathLocator dateLocator = new XPathLocator(node, "./dl/dd[@class='attachment-date']");

                XPathLocator urlLocator = new XPathLocator(node, "./dl/dt[@class='attachment-title']/a/@href");

                String url = urlLocator.getText();
                int lastSlash = url.lastIndexOf("/");
                String id = url.substring(url.lastIndexOf("/", lastSlash - 1) + 1, lastSlash);

                fileAttachments.add(Items.file(Long.parseLong(id), nameLocator.getText(), sizeLocator.getText(), authorLocator.getText(), dateLocator.getText()));
            }
            else
            {
                List<ZipFileAttachmentEntry> zipEntries = new ArrayList<ZipFileAttachmentEntry>();
                XPathLocator zipFileEntriesLocator = new XPathLocator(zipContents.getNode(), "ol/li");
                for (Node zipFileEntry : zipFileEntriesLocator.getNodes())
                {
                    XPathLocator nameLocator = new XPathLocator(zipFileEntry, "./a/@title");
                    XPathLocator sizeLocator = new XPathLocator(zipFileEntry, "./span[@class='attachment-size']");

                    zipEntries.add(Items.zipEntry(nameLocator.getText(), sizeLocator.getText()));
                }

                XPathLocator zipContentsTrailer = new XPathLocator(zipContents.getNode(), "./span[@class='zip-contents-trailer']");
                //The 'Showing x of x' text should only appear in the zip-contents-trailer if the file has more than
                //30 items. However, we can't tell if the file has more than 30 items or exactly 30 items. For this
                //assert we're making the assumption that if 30 items are shown, there are more items available.
                boolean containsShowingItemsCount = zipContentsTrailer.getText().contains("Showing 30 of ");
                if (zipEntries.size() == 30)
                {
                    assertTrue("'Showing x of x' should appear if the file has more than 30 items.", containsShowingItemsCount);
                }
                else
                {
                    assertFalse("'Showing x of x' should only appear if the file has more than 30 items.", containsShowingItemsCount);
                }

                XPathLocator nameLocator = new XPathLocator(node, ".//div[@class='twixi-wrap concise']/dl/dt[@class='attachment-title']/a");
                XPathLocator sizeLocator = new XPathLocator(node, ".//div[@class='twixi-wrap concise']/dl/dd[@class='attachment-size']");
                XPathLocator authorLocator = new XPathLocator(node, ".//div[@class='twixi-wrap concise']/dl/dd[@class='attachment-author']");
                XPathLocator dateLocator = new XPathLocator(node, ".//div[@class='twixi-wrap concise']/dl/dd[@class='attachment-date']");
                fileAttachments.add(Items.zip(nameLocator.getText(), sizeLocator.getText(), authorLocator.getText(), dateLocator.getText(), zipEntries));
            }
        }
        logger.log("Finished retrieving attachments for the issue: " + issueKeyLocator.getText());
        return fileAttachments;
    }
}
