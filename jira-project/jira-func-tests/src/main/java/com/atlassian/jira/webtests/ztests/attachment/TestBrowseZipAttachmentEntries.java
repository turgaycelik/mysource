package com.atlassian.jira.webtests.ztests.attachment;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.issue.FileAttachmentsList;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.rules.CopyAttachmentsRule;
import com.atlassian.jira.util.collect.CollectionBuilder;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.apache.commons.io.FileUtils;

import static com.google.common.collect.Iterables.transform;

/**
 * Responsible for verifying that a user can browse the entries in a zip file from the View Issue page.
 * @since v4.2
 */
@WebTest({Category.FUNC_TEST, Category.ATTACHMENTS })
public class TestBrowseZipAttachmentEntries extends FuncTestCase
{
    protected CopyAttachmentsRule copyAttachmentsRule;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestBrowseZipAttachmentEntries/TestBrowseZipAttachmentEntries.xml");

        copyAttachmentsRule = new CopyAttachmentsRule(this);
        copyAttachmentsRule.before();
        copyAttachmentsRule.copyAttachmentsFrom("TestBrowseZipAttachmentEntries/attachments");
        copyBrokenAttachmentToJira();
    }

    @Override
    protected void tearDownTest()
    {
        copyAttachmentsRule.after();
    }

    static Iterable<FileAttachmentsList.FileAttachmentItem> sortZipEntries(List<FileAttachmentsList.FileAttachmentItem> original)
    {
        return ImmutableList.copyOf(transform(original, new Function<FileAttachmentsList.FileAttachmentItem, FileAttachmentsList.FileAttachmentItem>()
        {
            @Override
            public FileAttachmentsList.FileAttachmentItem apply(FileAttachmentsList.FileAttachmentItem input)
            {
                return input.sortZipEntries(FileAttachmentsList.Items.ZIP_ENTRY_DEFUALT_ORDERING);
            }
        }));
    }

    public void testDoesNotExpandAnyZipFileWhenZipSupportIsOff() throws Exception
    {
        administration.attachments().disableZipSupport();

        final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                CollectionBuilder.newBuilder(
                        FileAttachmentsList.Items.file("jira-labels-plugin-2.3.jar", "99 kB", ADMIN_FULLNAME, "25/May/10 6:01 PM"),
                        FileAttachmentsList.Items.file("logs.zip", "13 kB", ADMIN_FULLNAME, "25/May/10 6:01 PM"),
                        FileAttachmentsList.Items.file("patch-JRA-21004-3.12.2.zip", "216 kB", ADMIN_FULLNAME, "25/May/10 6:01 PM"),
                        FileAttachmentsList.Items.file("Safari TPB-1.pdf", "113 kB", ADMIN_FULLNAME, "25/May/10 6:01 PM"),
                        FileAttachmentsList.Items.file("sample-images.zip", "137 kB", ADMIN_FULLNAME, "25/May/10 6:02 PM")
                ).asList();

        final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList =
                navigation.issue().attachments("HSP-1").list().get();

        assertThereAreNoZipAttachmentsOn(actualFileAttachmentsList);
        assertEquals(expectedFileAttachmentsList, actualFileAttachmentsList);
    }

    public void testDoesNotExpandMsOfficeDocumentsAsZipAttachments() throws Exception
    {
        final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                CollectionBuilder.newBuilder(
                        FileAttachmentsList.Items.file("Sample Document.docx", "745 kB", ADMIN_FULLNAME, "17/Aug/10 4:38 PM"),
                        FileAttachmentsList.Items.file("Sample Document Template.dotx", "745 kB", ADMIN_FULLNAME, "17/Aug/10 4:38 PM"),
                        FileAttachmentsList.Items.file("Sample Presentation.pptx", "687 kB", ADMIN_FULLNAME, "17/Aug/10 4:38 PM"),
                        FileAttachmentsList.Items.file("Sample Presentation (Run-Only).ppsx", "687 kB", ADMIN_FULLNAME, "17/Aug/10 4:38 PM"),
                        FileAttachmentsList.Items.file("Sample Presentation Template.potx", "687 kB", ADMIN_FULLNAME, "17/Aug/10 4:38 PM"),
                        FileAttachmentsList.Items.file("Sample Spreadsheet.xlsx", "10 kB", ADMIN_FULLNAME, "17/Aug/10 4:38 PM"),
                        FileAttachmentsList.Items.file("Sample Spreadsheet Template.xltx", "10 kB", ADMIN_FULLNAME, "17/Aug/10 4:38 PM")
                ).asList();

        final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList =
                navigation.issue().attachments("HSP-2").list().get();

        assertThereAreNoZipAttachmentsOn(actualFileAttachmentsList);
        assertEquals(expectedFileAttachmentsList, actualFileAttachmentsList);
    }

    public void testDoesNotExpandOpenOfficeDocumentsAsZipAttachments() throws Exception
    {
        final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                CollectionBuilder.newBuilder(
                        FileAttachmentsList.Items.file("Sample 1.0 Drawing.sxd", "7 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample 1.0 Drawing Template.std", "7 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample 1.0 Master Document.sxg", "7 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample 1.0 Presentation.sxi", "8 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample 1.0 Presentation Template.sti", "8 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample 1.0 Spreadsheet.sxc", "6 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample 1.0 Spreadsheet Template.stc", "7 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample 1.0 Text Document.sxw", "7 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample 1.0 Text Document Template.stw", "7 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample Database.odb", "3 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample Drawing.odg", "9 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample Drawing Template.otg", "7 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample Formula.odf", "5 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample Master Document.odm", "7 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample Presentation.odp", "205 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample Presentation Template.otp", "9 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample Spreadsheet.ods", "7 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample Spreadsheet Template.ots", "7 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample Text Document.odt", "7 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM"),
                        FileAttachmentsList.Items.file("Sample Text Document Template.ott", "7 kB", ADMIN_FULLNAME, "18/Aug/10 3:19 PM")
                ).asList();

        final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList =
                navigation.issue().attachments("HSP-3").list().get();

        assertThereAreNoZipAttachmentsOn(actualFileAttachmentsList);
        assertEquals(expectedFileAttachmentsList, actualFileAttachmentsList);
    }

    public void testCanExpandJarsAndZipsAsZipAttachments()
    {
        final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                CollectionBuilder.newBuilder(
                        FileAttachmentsList.Items.zip("jira-labels-plugin-2.3.jar", "99 kB", ADMIN_FULLNAME, "25/May/10 6:01 PM",
                                CollectionBuilder.newBuilder(
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/upgradetask/GadgetUpgradeTask.class", "4 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/upgradetask/GadgetUpgradeTask$LabelPortletUpgradeTask.class", "1 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/upgradetask/GadgetUpgradeTask$1.class", "4 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/rest/LabelResource.class", "19 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/rest/LabelResource$LabelSuggestions.class", "1 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/rest/LabelResource$LabelFields.class", "0.8 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/rest/LabelResource$LabelField.class", "0.7 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/rest/LabelResource$1.class", "1 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/portlets/LabelsDashboardPortlet.class", "5 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/LabelSearchInputTransformer.class", "6 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/LabelSearchInputTransformer$QueryInputPlaceHolder.class", "1 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/LabelSearchInputTransformer$LabelsClauseVisitor.class", "2 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/LabelSearchInputTransformer$1.class", "0.3 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/LabelSearcher.class", "6 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/LabelSearcher$LabelIndexer.class", "3 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/LabelSearcher$LabelCustomFieldValueProvider.class", "3 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/LabelsClauseValuesGenerator.class", "4 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/LabelsCFType.class", "9 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/LabelsCFType$1.class", "2 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/Labels-I18N_de_DE.properties", "3 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/Labels-I18N.properties", "3 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/LabelCustomFieldImporter.class", "3 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/LabelCustomFieldImporter$2.class", "2 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/LabelCustomFieldImporter$1.class", "2 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/Label.class", "0.1 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/heatmap/LabelsHeatmapProjectTabPanel.class", "8 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/Constants.class", "0.5 kB"),
                                        FileAttachmentsList.Items.zipEntry("com/atlassian/jira/plugin/labels/ajax/CustomFieldInfo.class", "0.6 kB"),
                                        FileAttachmentsList.Items.zipEntry("atlassian-plugin.xml", "6 kB"),
                                        FileAttachmentsList.Items.zipEntry("META-INF/MANIFEST.MF", "0.1 kB")
                                ).asList()),
                        FileAttachmentsList.Items.zip("logs.zip", "13 kB", ADMIN_FULLNAME, "25/May/10 6:01 PM",
                                CollectionBuilder.newBuilder(
                                        FileAttachmentsList.Items.zipEntry("__MACOSX/._logs", "0.2 kB"),
                                        FileAttachmentsList.Items.zipEntry("__MACOSX/logs/._login-good-topright.log", "0.1 kB"),
                                        FileAttachmentsList.Items.zipEntry("logs/login-good-topright.log", "76 kB"),
                                        FileAttachmentsList.Items.zipEntry("logs/login-good-refresh.log", "90 kB"),
                                        FileAttachmentsList.Items.zipEntry("logs/login-error.log", "7 kB")
                                ).asList()),
                        FileAttachmentsList.Items.zip("patch-JRA-21004-3.12.2.zip", "216 kB", ADMIN_FULLNAME, "25/May/10 6:01 PM",
                                CollectionBuilder.newBuilder(
                                        FileAttachmentsList.Items.zipEntry("JRA-21004-3.12.2-patch.md5", "0.1 kB"),
                                        FileAttachmentsList.Items.zipEntry("JRA-21004-3.12.2-patch-instructions.txt", "5 kB"),
                                        FileAttachmentsList.Items.zipEntry("JRA-21004-3.12.2-patch.zip", "214 kB")
                                ).asList()),
                        FileAttachmentsList.Items.file("Safari TPB-1.pdf", "113 kB", ADMIN_FULLNAME, "25/May/10 6:01 PM"),
                        FileAttachmentsList.Items.zip("sample-images.zip", "137 kB", ADMIN_FULLNAME, "25/May/10 6:02 PM",
                                CollectionBuilder.newBuilder(
                                        FileAttachmentsList.Items.zipEntry("__MACOSX/sample-images-zip/._tropical-desktop-wallpaper-1280x1024.jpg", "0.2 kB"),
                                        FileAttachmentsList.Items.zipEntry("sample-images-zip/tropical-desktop-wallpaper-1280x1024.jpg", "115 kB"),
                                        FileAttachmentsList.Items.zipEntry("__MACOSX/sample-images-zip/._235px-Floppy_disk_2009_G1.jpg", "0.2 kB"),
                                        FileAttachmentsList.Items.zipEntry("sample-images-zip/235px-Floppy_disk_2009_G1.jpg", "8 kB"),
                                        FileAttachmentsList.Items.zipEntry("__MACOSX/sample-images-zip/._200px-FCB.svg.png", "0.2 kB"),
                                        FileAttachmentsList.Items.zipEntry("sample-images-zip/200px-FCB.svg.png", "16 kB")
                                ).asList())
                ).asList();

        final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList =
                navigation.issue().attachments("HSP-1").list().get();

        // an issue in jaxen (XPATH engine) causes the list read from the page to be in different order depending on the JDK used
        // we don't really care about the order as long as all expected entries are on the page, hence the sort
        assertEquals(sortZipEntries(expectedFileAttachmentsList), sortZipEntries(actualFileAttachmentsList));
    }

    // JRA-22980 - Webowork can blowup when files have certain chars in them
    public void testProblemeaticFileNamesInsideOfZips()
    {
        navigation.issue().viewIssue("HSP-4");
        //previouslythis page just blew up
        assertions.assertNodeByIdExists("file_attachments");
        final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                CollectionBuilder.newBuilder(FileAttachmentsList.Items.zip("{[('BadFileName.zip", "389 kB", "Administrator", "23/Nov/10 11:53 AM",
                        CollectionBuilder.newBuilder(
                                FileAttachmentsList.Items.zipEntry("__MACOSX/._{[('BadFileName.png", "0.1 kB"),
                                FileAttachmentsList.Items.zipEntry("{[('BadFileName.png", "389 kB")
                        ).asList())).asList();

        final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList =
                navigation.issue().attachments("HSP-4").list().get();

        // an issue in jaxen (XPATH engine) causes the list read from the page to be in different order depending on the JDK used
        // we don't really care about the order as long as all expected entries are on the page, hence the sort
        assertEquals(sortZipEntries(expectedFileAttachmentsList), sortZipEntries(actualFileAttachmentsList));
    }

    private void copyBrokenAttachmentToJira()
    {
        try
        {
            // Bad file names don't live well in svn.  Need to rename it.
            FileUtils.moveFile(new File(administration.getCurrentAttachmentPath() +"/HSP/HSP-4/10040_tempName.zip"),
                    new File(administration.getCurrentAttachmentPath() +"/HSP/HSP-4/10040_{[('BadFileName.zip"));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void assertThereAreNoZipAttachmentsOn(final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList)
    {
        assertTrue(
                Iterables.all(actualFileAttachmentsList, new Predicate<FileAttachmentsList.FileAttachmentItem>()
                {
                    public boolean apply(final FileAttachmentsList.FileAttachmentItem item)
                    {
                        return !item.isZip();
                    }
                }));
    }
}
