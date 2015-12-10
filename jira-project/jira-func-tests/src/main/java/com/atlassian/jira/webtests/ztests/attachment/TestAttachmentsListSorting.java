package com.atlassian.jira.webtests.ztests.attachment;

import java.util.List;

import com.atlassian.jira.functest.framework.navigation.issue.AttachmentsBlock;
import com.atlassian.jira.functest.framework.navigation.issue.FileAttachmentsList;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;

/**
 * Responsible for holding tests that verify that the attachments list on the view issue page
 * can be sorted using a key (i.e. name, date ...) in ascending or descending order.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestAttachmentsListSorting extends AbstractTestAttachmentsBlockSortingOnViewIssue
{
    public void testAttachmentsDefaultToSortingByNameInDescendingOrder() throws Exception
    {
        final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                CollectionBuilder.newBuilder(
                        FileAttachmentsList.Items.file("_fil\u00E5e", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:13 PM"),
                        FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Atlassian.pdf", "193 kB",
                                ADMIN_FULLNAME, "06/May/10 11:27 AM"),
                        FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Printable.pdf", "98 kB",
                                ADMIN_FULLNAME, "06/May/10 12:02 PM"),
                        FileAttachmentsList.Items.file("a", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:12 PM"),
                        FileAttachmentsList.Items.file("a1k4BJwT.jpg.part", "22 kB", ADMIN_FULLNAME, "06/May/10 12:01 PM"),
                        FileAttachmentsList.Items.file("\u00E1 file", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:14 PM"),
                        FileAttachmentsList.Items.file("build.xml", "1 kB", ADMIN_FULLNAME, "06/May/10 12:00 PM"),
                        FileAttachmentsList.Items.file("catalina.sh", "12 kB", ADMIN_FULLNAME, "06/May/10 12:15 PM"),
                        FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:26 AM"),
                        FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:24 AM"),
                        FileAttachmentsList.Items.file("pom.xml", "5 kB", ADMIN_FULLNAME, "06/May/10 11:29 AM"),
                        FileAttachmentsList.Items.file("pom.xml", "2 kB", ADMIN_FULLNAME, "06/May/10 11:25 AM"),
                        FileAttachmentsList.Items.file("Tickspot", "0.1 kB", ADMIN_FULLNAME, "06/May/10 12:03 PM")
                ).asList();

        final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList =
                navigation.issue().attachments("HSP-1").list().get();

        assertEquals(expectedFileAttachmentsList, actualFileAttachmentsList);
    }

    public void testCanSortAttachmentsByFileNameInAscendingOrder() throws Exception
    {
        final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                CollectionBuilder.newBuilder(
                        FileAttachmentsList.Items.file("_fil\u00E5e", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:13 PM"),
                        FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Atlassian.pdf", "193 kB",
                                ADMIN_FULLNAME, "06/May/10 11:27 AM"),
                        FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Printable.pdf", "98 kB",
                                ADMIN_FULLNAME, "06/May/10 12:02 PM"),
                        FileAttachmentsList.Items.file("a", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:12 PM"),
                        FileAttachmentsList.Items.file("a1k4BJwT.jpg.part", "22 kB", ADMIN_FULLNAME, "06/May/10 12:01 PM"),
                        FileAttachmentsList.Items.file("\u00E1 file", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:14 PM"),
                        FileAttachmentsList.Items.file("build.xml", "1 kB", ADMIN_FULLNAME, "06/May/10 12:00 PM"),
                        FileAttachmentsList.Items.file("catalina.sh", "12 kB", ADMIN_FULLNAME, "06/May/10 12:15 PM"),
                        FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:26 AM"),
                        FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:24 AM"),
                        FileAttachmentsList.Items.file("pom.xml", "5 kB", ADMIN_FULLNAME, "06/May/10 11:29 AM"),
                        FileAttachmentsList.Items.file("pom.xml", "2 kB", ADMIN_FULLNAME, "06/May/10 11:25 AM"),
                        FileAttachmentsList.Items.file("Tickspot", "0.1 kB", ADMIN_FULLNAME, "06/May/10 12:03 PM")
                ).asList();

        final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
        attachmentsBlock.sort(AttachmentsBlock.Sort.Key.NAME, AttachmentsBlock.Sort.Direction.ASCENDING);

        final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList = attachmentsBlock.list().get();
        assertEquals(expectedFileAttachmentsList, actualFileAttachmentsList);

        verifySortingSettingIsStickyDuringTheSession(expectedFileAttachmentsList);
    }

    public void testCanSortAttachmentsByFileNameInDescendingOrder() throws Exception
    {
        final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                CollectionBuilder.newBuilder(
                        FileAttachmentsList.Items.file("Tickspot", "0.1 kB", ADMIN_FULLNAME, "06/May/10 12:03 PM"),
                        FileAttachmentsList.Items.file("pom.xml", "2 kB", ADMIN_FULLNAME, "06/May/10 11:25 AM"),
                        FileAttachmentsList.Items.file("pom.xml", "5 kB", ADMIN_FULLNAME, "06/May/10 11:29 AM"),
                        FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:24 AM"),
                        FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:26 AM"),
                        FileAttachmentsList.Items.file("catalina.sh", "12 kB", ADMIN_FULLNAME, "06/May/10 12:15 PM"),
                        FileAttachmentsList.Items.file("build.xml", "1 kB", ADMIN_FULLNAME, "06/May/10 12:00 PM"),
                        FileAttachmentsList.Items.file("\u00E1 file", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:14 PM"),
                        FileAttachmentsList.Items.file("a1k4BJwT.jpg.part", "22 kB", ADMIN_FULLNAME, "06/May/10 12:01 PM"),
                        FileAttachmentsList.Items.file("a", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:12 PM"),
                        FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Printable.pdf", "98 kB",
                                ADMIN_FULLNAME, "06/May/10 12:02 PM"),
                        FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Atlassian.pdf", "193 kB",
                                ADMIN_FULLNAME, "06/May/10 11:27 AM"),
                        FileAttachmentsList.Items.file("_fil\u00E5e", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:13 PM")
                ).asList();

        final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
        attachmentsBlock.sort(AttachmentsBlock.Sort.Key.NAME, AttachmentsBlock.Sort.Direction.DESCENDING);

        final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList = attachmentsBlock.list().get();
        assertEquals(expectedFileAttachmentsList, actualFileAttachmentsList);

        verifySortingSettingIsStickyDuringTheSession(expectedFileAttachmentsList);
    }

    public void testCanSortAttachmentsByDateInAscendingOrder() throws Exception
    {
        final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                CollectionBuilder.newBuilder(
                        FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:24 AM"),
                        FileAttachmentsList.Items.file("pom.xml", "2 kB", ADMIN_FULLNAME, "06/May/10 11:25 AM"),
                        FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:26 AM"),
                        FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Atlassian.pdf", "193 kB",
                                ADMIN_FULLNAME, "06/May/10 11:27 AM"),
                        FileAttachmentsList.Items.file("pom.xml", "5 kB", ADMIN_FULLNAME, "06/May/10 11:29 AM"),
                        FileAttachmentsList.Items.file("build.xml", "1 kB", ADMIN_FULLNAME, "06/May/10 12:00 PM"),
                        FileAttachmentsList.Items.file("a1k4BJwT.jpg.part", "22 kB", ADMIN_FULLNAME, "06/May/10 12:01 PM"),
                                                FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Printable.pdf", "98 kB",
                                                        ADMIN_FULLNAME, "06/May/10 12:02 PM"),
                        FileAttachmentsList.Items.file("Tickspot", "0.1 kB", ADMIN_FULLNAME, "06/May/10 12:03 PM"),
                        FileAttachmentsList.Items.file("a", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:12 PM"),
                        FileAttachmentsList.Items.file("_fil\u00E5e", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:13 PM"),
                        FileAttachmentsList.Items.file("\u00E1 file", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:14 PM"),
                        FileAttachmentsList.Items.file("catalina.sh", "12 kB", ADMIN_FULLNAME, "06/May/10 12:15 PM")
                ).asList();

        final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
        attachmentsBlock.sort(AttachmentsBlock.Sort.Key.DATE, AttachmentsBlock.Sort.Direction.ASCENDING);

        final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList = attachmentsBlock.list().get();
        assertEquals(expectedFileAttachmentsList, actualFileAttachmentsList);

        verifySortingSettingIsStickyDuringTheSession(expectedFileAttachmentsList);
    }

    public void testCanSortAttachmentsByDateInDescendingOrder() throws Exception
    {
        final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                CollectionBuilder.newBuilder(
                        FileAttachmentsList.Items.file("catalina.sh", "12 kB", ADMIN_FULLNAME, "06/May/10 12:15 PM"),
                        FileAttachmentsList.Items.file("\u00E1 file", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:14 PM"),
                        FileAttachmentsList.Items.file("_fil\u00E5e", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:13 PM"),
                        FileAttachmentsList.Items.file("a", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:12 PM"),
                        FileAttachmentsList.Items.file("Tickspot", "0.1 kB", ADMIN_FULLNAME, "06/May/10 12:03 PM"),
                        FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Printable.pdf", "98 kB",
                                ADMIN_FULLNAME, "06/May/10 12:02 PM"),
                        FileAttachmentsList.Items.file("a1k4BJwT.jpg.part", "22 kB", ADMIN_FULLNAME, "06/May/10 12:01 PM"),
                        FileAttachmentsList.Items.file("build.xml", "1 kB", ADMIN_FULLNAME, "06/May/10 12:00 PM"),
                        FileAttachmentsList.Items.file("pom.xml", "5 kB", ADMIN_FULLNAME, "06/May/10 11:29 AM"),
                        FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Atlassian.pdf", "193 kB",
                                ADMIN_FULLNAME, "06/May/10 11:27 AM"),
                        FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:26 AM"),
                        FileAttachmentsList.Items.file("pom.xml", "2 kB", ADMIN_FULLNAME, "06/May/10 11:25 AM"),
                        FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:24 AM")
                ).asList();

        final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
        attachmentsBlock.sort(AttachmentsBlock.Sort.Key.DATE, AttachmentsBlock.Sort.Direction.DESCENDING);

        final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList = attachmentsBlock.list().get();
        assertEquals(expectedFileAttachmentsList, actualFileAttachmentsList);
        verifySortingSettingIsStickyDuringTheSession(expectedFileAttachmentsList);
    }

    private void verifySortingSettingIsStickyDuringTheSession
            (final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList)
    {
        navigation.gotoDashboard();

        final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
        final List<FileAttachmentsList.FileAttachmentItem> actualAttachmentsList = attachmentsBlock.list().get();

        assertEquals(expectedFileAttachmentsList, actualAttachmentsList);
    }
}
