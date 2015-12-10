package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.atlassian.jira.webtests.JIRAWebTest;
import junit.framework.Assert;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Tests that the data export does what it should (i.e. does not anonymise the data when it should not).
 */
@WebTest ({Category.FUNC_TEST, Category.ADMINISTRATION, Category.IMPORT_EXPORT })
public class TestDataExport extends JIRAWebTest
{
    private static final String BS_BACKUP_TEST_FILE_XML = "BS_BackupTestFile.xml";

    public TestDataExport(String name)
    {
        super(name);
    }

    public void testNoFilename() throws Exception
    {
        tester.gotoPage("secure/admin/XmlBackup!default.jspa");
        tester.assertTextNotPresent("You must enter a file name to export to");
        tester.setWorkingForm("jiraform");
        tester.setFormElement("filename", "");
        tester.submit();
        tester.assertTextPresent("You must enter a file name to export to");
    }

    public void testInvalidFilename() throws Exception
    {
        final EnvironmentUtils utils = new EnvironmentUtils(tester, getEnvironmentData());
        if (utils.isOnWindows())
        {
            tester.gotoPage("secure/admin/XmlBackup!default.jspa");
            tester.assertTextNotPresent("Filename 'C:\\te:st.xml' is invalid");
            tester.setWorkingForm("jiraform");
            tester.setFormElement("filename", "C:\\te:st.xml");
            tester.submit();
            tester.assertTextPresent("Filename &#39;C:\\te:st.xml&#39; is invalid");
        }
    }

    public void testDataExportDoesNotAnonymise()
            throws IOException, ParserConfigurationException, SAXException, TransformerException
    {
        File backupFile = null;
        try
        {
            // Restore
            administration.restoreData("TestDataExport.xml");

            // Make sure we don't have to overwrite the file on export
            deleteBackupFileIfPresent(BS_BACKUP_TEST_FILE_XML);

            // Export to a file
            backupFile = administration.exportDataToFile(BS_BACKUP_TEST_FILE_XML);

            // Make sure the export we just generated does not contain large strings of XXX for issue and comment data
            assertTrue("The backup file must exist", backupFile.exists());
            Document doc = XMLUnit.buildControlDocument(new InputSource(new ZipFile(backupFile).getInputStream(new ZipEntry("entities.xml"))));
            XMLAssert.assertXpathEvaluatesTo("This is a test summary to make sure that the xml backup does not anonymise this data",
                    "/entity-engine-xml/Issue[@id=10000]/@summary", doc);
            XMLAssert.assertXpathEvaluatesTo("This is a test description to make sure that the xml backup does not anonymise this data",
                    "/entity-engine-xml/Issue[@id=10000]/@description", doc);
            XMLAssert.assertXpathEvaluatesTo("This is a comment to make sure that the xml backup does not anonymise this data",
                    "/entity-engine-xml/Action[@id=10000]/@body", doc);
        }
        finally
        {
            // Make sure we always clean up after ourselves
            if (backupFile != null)
            {
                //noinspection ResultOfMethodCallIgnored
                backupFile.delete();
            }
        }

    }

    /**
     * Checks that the comments in the data export are present and escaped correctly
     */
    public void testInvalidCharactersInComments() throws IOException, ParserConfigurationException, SAXException, TransformerException
    {
        File backupFile = null;
        try
        {
            // Restore
            administration.restoreData("TestXmlBackupInvalidCommentCharacters.xml");

            // Make sure we don't have to overwrite the file on export
            deleteBackupFileIfPresent(BS_BACKUP_TEST_FILE_XML);

            // Export to a file
            backupFile = getAdministration().exportDataToFile(BS_BACKUP_TEST_FILE_XML);

            assertTrue("The backup file must exist", backupFile.exists());
            Document doc = XMLUnit.buildControlDocument(new InputSource(new ZipFile(backupFile).getInputStream(new ZipEntry("entities.xml"))));
            // check the top top level node comments
            checkForValidComments(doc.getChildNodes());

            // check the top node
            final NodeList topNode = doc.getElementsByTagName("entity-engine-xml");
            Assert.assertNotNull(topNode);
            Assert.assertEquals(1, topNode.getLength());
            checkForValidComments(topNode.item(0).getChildNodes());

           // now just for a lark restore it again to ensure we can round trip
            restoreData(backupFile);
        }
        finally
        {
            // Make sure we always clean up after ourselves
            if (backupFile != null)
            {
                //noinspection ResultOfMethodCallIgnored
                backupFile.delete();
            }
        }
    }

    private void checkForValidComments(final NodeList nodes)
    {
        boolean hasComments = false;
        for (int i = 0; i < nodes.getLength(); i++)
        {
            final Node node = nodes.item(i);
            if (node.getNodeType() == Node.COMMENT_NODE)
            {
                String commentText = node.getNodeValue();
                Assert.assertTrue("Comment text has at least one -- in it : '" + commentText, !commentText.contains("--"));
                hasComments = true;
            }
        }
        Assert.assertTrue("There should be comments in the data export nowdays", hasComments);
    }

    private void deleteBackupFileIfPresent(final String fileName)
    {
        File backupFile = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/" + fileName);
        if (backupFile.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            backupFile.delete();
        }
    }
}
