package com.atlassian.jira.functest.framework.navigation.issue;

import com.atlassian.jira.testkit.client.log.FuncTestLogger;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import net.sourceforge.jwebunit.WebTester;

/**
 *
 * @since v4.2
 */
public class DefaultAttachmentsBlock implements AttachmentsBlock
{
    private WebTester tester;
    private static final String ISSUE_KEY_ID = "key-val";
    private FuncTestLogger logger;
    private AttachmentManagement attachmentManagement;
    private static final String MANAGE_ATTACHMENTS_LINK_ID = "manage-attachment-link";
    private ImageAttachmentsGallery imageAttachmentsGallery;
    private FileAttachmentsList fileAttachmentsList;

    public DefaultAttachmentsBlock(final WebTester tester, final FuncTestLogger logger,
            final FileAttachmentsList fileAttachmentsList, final ImageAttachmentsGallery imageAttachmentsGallery, final AttachmentManagement attachmentManagement
    )
    {
        this.tester = tester;
        this.logger = logger;
        this.attachmentManagement = attachmentManagement;
        this.imageAttachmentsGallery = imageAttachmentsGallery;
        this.fileAttachmentsList = fileAttachmentsList;
    }

    public void sort(final Sort.Key key, final Sort.Direction direction)
    {
        tester.assertElementPresent(ISSUE_KEY_ID);
        Locator issueKeyLocator = new IdLocator(tester, ISSUE_KEY_ID);

        logger.log("Beginning to sort attachments by " + key + " in " + direction + " order, " + "for the issue: " + issueKeyLocator.getText());

        tester.clickLink(key.getLinkId());
        tester.clickLink(direction.getLinkId());

        logger.log("Finished sorting attachments by " + key + " in " + direction + " order, " + "for the issue: " + issueKeyLocator.getText());
    }

    public AttachmentManagement manage()
    {
        tester.assertElementPresent(ISSUE_KEY_ID);
        tester.clickLink(MANAGE_ATTACHMENTS_LINK_ID);
        return attachmentManagement;
    }

    public ImageAttachmentsGallery gallery()
    {
        return this.imageAttachmentsGallery;
    }

    public FileAttachmentsList list()
    {
        return this.fileAttachmentsList;
    }
}
