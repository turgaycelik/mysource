package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;

/**
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfImportAndExport extends FuncTestCase
{
    public void testProjectImportExport() throws Exception
    {
        administration.restoreBlankInstance();

        final String tempFilePath = getTempFilePath(".xml");
        new XsrfTestSuite(
            new XsrfCheck("ExportXmlData", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("backup_data");
                    tester.setFormElement("filename", tempFilePath);
                }
            }, new BackupFormSubmission()),
            new XsrfCheck("ImportXmlData", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("restore_data");
                    tester.setFormElement("filename", tempFilePath);
                }
            }, new XsrfCheck.FormSubmission("Restore"))
        ).run(funcTestHelperFactory);
    }


    public void testProjectImport() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck("ProjectImport", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        administration.projectImport().doImportToSummary("TestProjectImportStandardSimpleData.xml", "TestProjectImportStandardSimpleDataNoProject.xml", null);
                    }
                }, new XsrfCheck.FormSubmission("Import"))
        ).run(funcTestHelperFactory);
    }

    private String getTempFilePath(final String suffix)
    {
        final File tempFile;
        try
        {
            tempFile = File.createTempFile("TestXsrfImportAndExport_Export", suffix);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return tempFile.getAbsolutePath();
    }

    /**
     * Submitting the XmlBackup action might prompt us with "File already exists - replace?". So we need to do an additional
     * submit in that case.
     */
    private class BackupFormSubmission extends XsrfCheck.FormSubmission
    {
        public BackupFormSubmission()
        {
            super("Backup");
        }

        @Override
        public void submitRequest()
        {
            super.submitRequest();
            final Element replaceSubmit = tester.getDialog().getElement("Replace File");
            if (replaceSubmit != null)
            {
                tester.submit("Replace File");
            }
        }
    }
}
