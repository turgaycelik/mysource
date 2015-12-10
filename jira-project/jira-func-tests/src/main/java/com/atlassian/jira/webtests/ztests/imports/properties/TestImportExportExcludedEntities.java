package com.atlassian.jira.webtests.ztests.imports.properties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.ParserConfigurationException;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import org.custommonkey.xmlunit.XMLUnit;
import org.hamcrest.Matchers;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static com.atlassian.jira.entity.property.EntityPropertyType.ISSUE_PROPERTY;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Tests that the data export does not contain entities excluded from export. Nor importing data with
 * such entities results in the entity being in db.
 */
@WebTest ({ Category.FUNC_TEST, Category.ADMINISTRATION, Category.IMPORT_EXPORT })
public class TestImportExportExcludedEntities extends FuncTestCase
{
    private static final String BS_BACKUP_TEST_FILE_XML = "BS_BackupTestFile.xml";

    public void testExcludedEntityNotImported() throws IOException, ParserConfigurationException, SAXException
    {
        final String backupFile = "TestImportEntityPropertyIndexDocument.xml";
        FileReader reader = null;
        try
        {
            reader = new FileReader(new File(environmentData.getXMLDataLocation().getAbsolutePath() + File.separator + backupFile));
            Document doc = XMLUnit.buildControlDocument(new InputSource(reader));

            NodeList entityPropertyIndexDocument = doc.getElementsByTagName("EntityPropertyIndexDocument");

            // assert data backup contains one entity which should not be imported
            assertThat(entityPropertyIndexDocument.getLength(), Matchers.is(1));

            // after importing without plugin system restart there should be no plugin index configurations in db (as modules were not restarted)
            administration.restoreData(backupFile);
            assertThat(backdoor.getPluginIndexConfigurationControl().getDocumentsForEntity(ISSUE_PROPERTY.getDbEntityName()), hasSize(0));

            // after importing with plugin reload there should be only one plugin index configurations, the one coming from
            // func test plugin and dev status panel
            administration.restoreDataWithPluginsReload(backupFile);

            // warning: JBAC runs tests with reference plugin installed. Run jmake with -rp
            assertThat(backdoor.getPluginIndexConfigurationControl().getDocumentsForEntity(ISSUE_PROPERTY.getDbEntityName()),
                    containsInAnyOrder("com.atlassian.jira.dev.func-test-plugin:index-doc-conf", "com.atlassian.jira.dev.reference-plugin:IssueProperty", "com.atlassian.jira.plugins.jira-development-integration-plugin:jira-issue-fusion-jql"));
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    public void testExcludedEntityNotExported() throws IOException, ParserConfigurationException, SAXException
    {
        File backupFile = null;
        try
        {
            backdoor.getPluginIndexConfigurationControl().putDocumentConfiguration("pluginKey", "moduleKey", getConfiguration());
            assertThat(backdoor.getPluginIndexConfigurationControl().getDocumentsForEntity(ISSUE_PROPERTY.getDbEntityName()),
                    hasItem("pluginKey:moduleKey"));

            deleteBackupFileIfPresent(BS_BACKUP_TEST_FILE_XML);

            backupFile = administration.exportDataToFile(BS_BACKUP_TEST_FILE_XML);

            assertTrue("The backup file must exist", backupFile.exists());
            Document doc = XMLUnit.buildControlDocument(new InputSource(new ZipFile(backupFile).getInputStream(new ZipEntry("entities.xml"))));

            // file with exported data doesn't contain any EntityPropertyIndexDocuments
            NodeList entityPropertyIndexDocument = doc.getElementsByTagName("EntityPropertyIndexDocument");
            assertThat(entityPropertyIndexDocument.getLength(), Matchers.is(0));
        }
        finally
        {
            if (backupFile != null)
            {
                backupFile.delete();
            }
        }
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

    @Override
    public void tearDownTest()
    {
        backdoor.plugins().enablePlugin("com.atlassian.jira.dev.reference-plugin");
    }

    private String getConfiguration()
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<index-document-configuration entity-key=\"IssueProperty\"><key property-key=\"foo.bar\">"
                + "<extract path=\"foo1.bar2\" type=\"STRING\"/></key><key property-key=\"foo.bar\">"
                + "<extract path=\"foo1.bar2\" type=\"DATE\"/></key>"
                + "</index-document-configuration>";
    }
}
