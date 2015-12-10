package com.atlassian.jira.webtests.ztests.attachment;

import java.util.List;

import com.atlassian.jira.functest.framework.navigation.issue.AttachmentsBlock;
import com.atlassian.jira.functest.framework.navigation.issue.ImageAttachmentsGallery;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;

/**
 * Responsible for holding tests that verify that the image attachments shown in the image gallery on the view issue
 * page can be sorted using a key (i.e. name, date ...) in ascending or descending order.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestImageAttachmentsGallerySorting extends AbstractTestAttachmentsBlockSortingOnViewIssue
{
    public void testAttachmentsDefaultToSortingByNameInDescendingOrder() throws Exception
    {
        final List<ImageAttachmentsGallery.ImageAttachmentItem> expectedImageAttachments =
                CollectionBuilder.newBuilder(
                        new ImageAttachmentsGallery.ImageAttachmentItem("200px-FCB.svg.png", "16 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg", "8 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg", "8 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("tropical-desktop-wallpaper-1280x1024.jpg", "115 kB")
                ).asList();

        final List<ImageAttachmentsGallery.ImageAttachmentItem> actualImageAttachments =
                navigation.issue().attachments("HSP-1").gallery().get();

        assertEquals(expectedImageAttachments, actualImageAttachments);
    }

    public void testCanSortAttachmentsByFileNameInAscendingOrder() throws Exception
    {
        final List<ImageAttachmentsGallery.ImageAttachmentItem> expectedImageAttachments =
                CollectionBuilder.newBuilder(
                        new ImageAttachmentsGallery.ImageAttachmentItem("200px-FCB.svg.png","16 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("tropical-desktop-wallpaper-1280x1024.jpg", "115 kB")
                ).asList();

        final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
        attachmentsBlock.sort(AttachmentsBlock.Sort.Key.NAME, AttachmentsBlock.Sort.Direction.ASCENDING);

        final List<ImageAttachmentsGallery.ImageAttachmentItem> actualImageAttachments =
                attachmentsBlock.gallery().get();

        assertEquals(expectedImageAttachments, actualImageAttachments);
        verifySortingSettingIsStickyDuringTheSession(expectedImageAttachments);
    }

    public void testCanSortAttachmentsByFileNameInDescendingOrder() throws Exception
    {
        final List<ImageAttachmentsGallery.ImageAttachmentItem> expectedImageAttachments =
                CollectionBuilder.newBuilder(
                        new ImageAttachmentsGallery.ImageAttachmentItem("tropical-desktop-wallpaper-1280x1024.jpg", "115 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("200px-FCB.svg.png","16 kB")
                ).asList();

        final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
        attachmentsBlock.sort(AttachmentsBlock.Sort.Key.NAME, AttachmentsBlock.Sort.Direction.DESCENDING);

        final List<ImageAttachmentsGallery.ImageAttachmentItem> actualImageAttachments =
                attachmentsBlock.gallery().get();

        assertEquals(expectedImageAttachments, actualImageAttachments);
        verifySortingSettingIsStickyDuringTheSession(expectedImageAttachments);
    }

    public void testCanSortAttachmentsByDateInAscendingOrder() throws Exception
    {
        final List<ImageAttachmentsGallery.ImageAttachmentItem> expectedImageAttachments =
                CollectionBuilder.newBuilder(
                        new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("200px-FCB.svg.png","16 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("tropical-desktop-wallpaper-1280x1024.jpg", "115 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB")
                ).asList();

        final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
        attachmentsBlock.sort(AttachmentsBlock.Sort.Key.DATE, AttachmentsBlock.Sort.Direction.ASCENDING);

        final List<ImageAttachmentsGallery.ImageAttachmentItem> actualImageAttachments =
                attachmentsBlock.gallery().get();

        assertEquals(expectedImageAttachments, actualImageAttachments);
        verifySortingSettingIsStickyDuringTheSession(expectedImageAttachments);
    }

    public void testCanSortAttachmentsByDateInDescendingOrder() throws Exception
    {
        final List<ImageAttachmentsGallery.ImageAttachmentItem> expectedImageAttachments =
                CollectionBuilder.newBuilder(
                        new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("tropical-desktop-wallpaper-1280x1024.jpg", "115 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("200px-FCB.svg.png","16 kB"),
                        new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB")
                ).asList();

        final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
        attachmentsBlock.sort(AttachmentsBlock.Sort.Key.DATE, AttachmentsBlock.Sort.Direction.DESCENDING);

        final List<ImageAttachmentsGallery.ImageAttachmentItem> actualImageAttachments =
                attachmentsBlock.gallery().get();

        assertEquals(expectedImageAttachments, actualImageAttachments);
        verifySortingSettingIsStickyDuringTheSession(expectedImageAttachments);
    }

    private void verifySortingSettingIsStickyDuringTheSession
            (final List<ImageAttachmentsGallery.ImageAttachmentItem> expectedFileAttachmentsList)
    {
        navigation.gotoDashboard();

        final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
        final List<ImageAttachmentsGallery.ImageAttachmentItem> actualImageAttachments =
                attachmentsBlock.gallery().get();

        assertEquals(expectedFileAttachmentsList, actualImageAttachments);
    }
}
