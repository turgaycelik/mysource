package com.atlassian.jira.webtests.ztests.ao;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.LicenseKeys;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@WebTest ({Category.FUNC_TEST, Category.REFERENCE_PLUGIN, Category.ACTIVE_OBJECTS, Category.PLUGINS })
public class TestActiveObjectsRestore extends FuncTestCase
{
    public TestActiveObjectsRestore(String name)
    {
        this.setName(name);
    }


    public void testRestoreDataSuccessfully()
    {
        administration.restoreBlankInstance();
        String filePath = "ActiveObjects.zip";
        File file = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/" + filePath);
        copyFileToJiraImportDirectory(file);
        // restore data again
        getTester().gotoPage("secure/admin/XmlRestore!default.jspa");
        getTester().setWorkingForm("restore-xml-data-backup");

        getTester().setFormElement("filename", filePath);
        getTester().setFormElement("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        getTester().submit();
        administration.waitForRestore();
        getTester().assertTextPresent("Your import has been successful");
        getTester().assertTextNotPresent("NullPointerException");
    }

    public void testRestoreWithDatabaseErrors()
    {
        try
        {
            administration.restoreData("ActiveObjectsBadData.zip", false);
        }
        catch (Throwable e)
        {
            assertTrue("Active objects bad data",e.getMessage().startsWith("Failed to restore JIRA data from"));
        }
        final String expectedMessage = "There was a problem restoring ActiveObjects data for the plugin Atlassian JIRA - Plugins - Development Only - Reference Plugin(com.atlassian.jira.dev.reference-plugin) #1.0.0 plugin.";
        if (!tester.getDialog().isTextInResponse(expectedMessage))
        {
            fail(String.format("Could not find '%s' in page. Are you sure you have the jira-reference-plugin installed?", expectedMessage));
        }
        final CssLocator cssLocator = new CssLocator(getTester(), ".aui-message.error");
        assertions.assertNodeHasText(cssLocator, expectedMessage);
        assertions.assertNodeHasText(cssLocator, "Importing table AO_98E482_REF_ENTITY failed. Please check the log for details.");
    }


    public void testRestoreWithParseErrors()
    {
        try
        {
            administration.restoreData("ActiveObjectsBadXml.zip", false);
        }
        catch (Throwable e)
        {
            assertTrue("Active objects bad data", e.getMessage().startsWith("Failed to restore JIRA data from"));
        }

        final CssLocator cssLocator = new CssLocator(getTester(), ".aui-message.error");
        assertions.assertNodeHasText(cssLocator, "There was a problem restoring ActiveObjects data for the <unknown plugin> plugin.");
        assertions.assertNodeHasText(cssLocator, "Caught exception with following message: Unexpected close tag </databaseGARBAGE>; expected </database>.\n at [row,col {unknown-source}]: [10,19].");
        assertions.assertNodeHasText(cssLocator, "Please check the log for details.");
    }

    private void copyFileToJiraImportDirectory(File file)
    {
        File jiraImportDirectory = new File(administration.getJiraHomeDirectory(), "import");
        try
        {
            FileUtils.copyFileToDirectory(file, jiraImportDirectory);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not copy file " + file.getAbsolutePath() +
                    " to the import directory in jira home " + jiraImportDirectory, e);
        }
    }
}
