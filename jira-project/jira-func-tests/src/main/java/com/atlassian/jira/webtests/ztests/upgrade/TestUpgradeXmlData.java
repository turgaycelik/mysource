package com.atlassian.jira.webtests.ztests.upgrade;

import com.atlassian.jira.functest.config.CheckOptions;
import com.atlassian.jira.functest.config.CheckOptionsUtils;
import com.atlassian.jira.functest.config.ConfigFile;
import com.atlassian.jira.functest.config.ConfigFileWalker;
import com.atlassian.jira.functest.config.ConfigurationDefaults;
import com.atlassian.jira.functest.config.JiraConfig;
import com.atlassian.jira.functest.config.xml.Checks;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * You can run this test to upgrade the JIRA's test XML. It basically goes through all of the XML (ZIP) files in your
 * func test (selenium test) XML directories as identified from your localtest.properties and upgrades them. It does
 * this by importing the data into JIRA and then exports it.
 *
 * To upgrade your data simply setup JIRA for a regular func test run (i.e start the server and configure your
 * localtest.properties) and then run this test. If you run it from the  jira-func-test module then it will upgrade the
 * func test xml. If you run it from the jira-selenium-test module then it will upgrade the selenium test xml.
 *
 * You can add 'suppresscheck: upgrade' to the top level comment in an XML to stop it from being upgraded. This
 * is useful for XML data that is meant test upgrade tasks. It is also useful for upgrade tasks that have replacement
 * tokens and can only be imported using their associated tests.

 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.INFRASTRUCTURE, Category.SLOW_IMPORT })
public class TestUpgradeXmlData extends FuncTestCase implements ConfigFileWalker.ConfigVisitor
{
    private static final Logger log = Logger.getLogger(TestUpgradeXmlData.class);

    private static final String JIRA_XML_UPGRADE_DATA_LOCATION = "jira.xml.upgrade.dataLocation";
    private static final String JIRA_XML_UPGRADE_ADD_SUPPRESS_OPTIONS = "jira.xml.upgrade.add.suppress.options";
    private static final String ENTITIES_XML = "entities.xml";

    private List<File> brokenFiles = new LinkedList<File>();
    private long buildNumber;
    private File dataLocation;

    public void testUpgradeTestData() throws Exception
    {
        dataLocation = getDataLocation();
        log.info(String.format("Using the following data location %s ", dataLocation.getAbsolutePath()));
        buildNumber = administration.getBuildNumber();

        final ConfigFileWalker fileWalker = new ConfigFileWalker(dataLocation, this);
        fileWalker.setExcludes(ConfigurationDefaults.getDefaultExcludedFilters());
        fileWalker.walk();

        if (!brokenFiles.isEmpty())
        {
            final StringWriter writer = new StringWriter();
            PrintWriter builder = new PrintWriter(writer);
            builder.println("Unable to update all the XML.");
            for (File brokenFile : brokenFiles)
            {
                builder.format("\t%s%n", brokenFile.getAbsolutePath());
            }

            builder.close();
            fail(writer.toString());
        }
        dataLocation = null;
        brokenFiles = null;
    }

    public void visitConfig(final ConfigFile configFile)
    {
        final File file = configFile.getFile();
        final Document document = configFile.readConfig();
        final CheckOptions checkOptions = CheckOptionsUtils.parseOptions(document);
        if (checkOptions.checkEnabled(Checks.UPGRADE))
        {
            if (!JiraConfig.isJiraXml(document))
            {
                brokenFiles.add(file);
                log.error(String.format("Not upgrading '%s' as it does not appear to be a JIRA backup.", file));
                return;
            }

            final JiraConfig config = new JiraConfig(document, file);
            final long number = config.getBuildNumber();
            if (number < buildNumber)
            {
                final Set<String> names = config.getSystemAdmins();
                if (restoreDataSafely(file, names))
                {
                    File newFile = administration.exportDataToFile(file.getName());

                    // It was originally an XML file, so get the XML out of the zip and continue to use that
                    if ("xml".equals(FilenameUtils.getExtension(file.getName())))
                    {
                        try
                        {
                            final InputStream inputStream = new ZipFile(newFile).getInputStream(new ZipEntry(ENTITIES_XML));
                            final File extractedXml = new File(newFile.getParentFile(), file.getName());
                            try
                            {
                                FileUtils.forceDelete(extractedXml);
                            }
                            catch (FileNotFoundException ignored) {}

                            final FileWriter fileWriter = new FileWriter(extractedXml);
                            IOUtils.copy(inputStream, fileWriter);
                            fileWriter.close();
                            newFile = extractedXml;
                        }
                        catch (IOException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                    final ConfigFile newConfigFile = ConfigFile.create(newFile);

                    //Make sure the document has the correct suppress checks.
                    final Document newDocument = newConfigFile.readConfig();
                    CheckOptionsUtils.writeOptions(newDocument, CheckOptionsUtils.disableIn(checkOptions, extraDisabledOptions()));
                    newConfigFile.writeFile(newDocument);

                    assertTrue(file.delete());
                    moveFile(newFile, file);
                }
                else
                {
                    brokenFiles.add(file);
                }
            }
        }
        else
        {
            log.info(String.format("Not upgrading '%s' as it contains the '%s' suppresscheck flag.", file, Checks.UPGRADE));
        }
    }

    private String[] extraDisabledOptions()
    {
        /*
         * This option is designed to be enabled on CI when this test runs after TestXmlBackups test.
         * Its purpose is to mark all the upgraded xmls as passing existing checks, thus speeding up
         * TextXmlBackups significantly.
         */
        if (Boolean.getBoolean(JIRA_XML_UPGRADE_ADD_SUPPRESS_OPTIONS))
        {
            List<String> keyList = ConfigurationDefaults.getListOfDefaultConfigurationChecksSuppressKeys();
            return keyList.toArray(new String[keyList.size()]);
        }
        else
        {
            return new String[0];
        }

    }

    public void visitConfigError(final File file, final ConfigFile.ConfigFileException e)
    {
        throw e;
    }

    private File getDataLocation()
    {
        final String fromSystem = System.getProperty(JIRA_XML_UPGRADE_DATA_LOCATION);
        if (fromSystem != null)
        {
            return normalizeFile(new File(fromSystem));
        }
        else
        {
            return normalizeFile(ConfigurationDefaults.getDefaultXmlDataLocation());
        }
    }

    private boolean restoreDataSafely(final File file, final Set<String> admins)
    {
        try
        {
            copyFileToDataLocation(file);

            if (admins.isEmpty())
            {
                administration.restoreDataSlowOldWay(getRelativeFile(dataLocation, file));
            }
            else
            {
                administration.restoreDataSlowOldWayAndLogin(getRelativeFile(dataLocation, file), admins.iterator().next());
            }
            return true;
        }
        catch (Throwable e)
        {
            log.error("Unable to restore '" + file.getAbsolutePath() + "'.", e);
            return false;
        }
    }

    private void copyFileToDataLocation(File file) throws IOException
    {
        final File subdirectoryWithinSource = new File(getRelativeFile(dataLocation, file)).getParentFile();
        if (subdirectoryWithinSource != null)
        {
            FileUtils.copyFileToDirectory(file, getSubdirectoryWithinTarget(subdirectoryWithinSource));
        }
        else
        {
            FileUtils.copyFileToDirectory(file, environmentData.getXMLDataLocation());
        }
    }

    private File getSubdirectoryWithinTarget(File subdirectoryWithinDataLocation)
    {
        return new File(environmentData.getXMLDataLocation().getPath() + File.separator + subdirectoryWithinDataLocation.getPath());
    }

    private static String getRelativeFile(final File base, final File file)
    {
        final String basePath = base.getPath();
        final String filePath = file.getPath();
        if (filePath != null && filePath.startsWith(basePath))
        {
            String s = filePath.substring(basePath.length());
            if (s.startsWith(File.separator))
            {
                s = s.substring(File.separator.length());
            }
            return s;
        }
        else
        {
            return file.getName();
        }
    }

    private static void moveFile(final File src, final File dest)
    {
        try
        {
            FileUtils.moveFile(src, dest);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static File normalizeFile(final File importFile)
    {
        try
        {
            return importFile.getCanonicalFile();
        }
        catch (IOException e)
        {
            return importFile.getAbsoluteFile();
        }
    }
}
