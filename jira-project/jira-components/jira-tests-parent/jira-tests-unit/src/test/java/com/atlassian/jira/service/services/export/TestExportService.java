package com.atlassian.jira.service.services.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.FileUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.util.I18nHelper;

import com.opensymphony.module.propertyset.PropertySet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This TestCase acutally creates files in the Java tmp directory
 */
public class TestExportService
{
    private static final String SLASH = File.separator;
    private static final String BASE_DIRNAME = SLASH + "jiraexportservice" + System.currentTimeMillis();
    private static final String BACKUP_DIRNAME = BASE_DIRNAME + SLASH + "backup";
    private static final String CORRUPTED_DIRNAME = BACKUP_DIRNAME + SLASH + "corrupted";
    private static final String JIRA_EXPORT_ZIP = "export-of-jira.zip";

    @Before
    public void setUp() throws Exception
    {
        deleteDirectory(BASE_DIRNAME);
    }

    @After
    public void tearDown() throws Exception
    {
        deleteDirectory(BASE_DIRNAME);
    }

    @Test
    public void testCreateCorruptedDirectory()
    {
        createDirectory(BACKUP_DIRNAME);

        final File backupDir = tmpFile(BACKUP_DIRNAME);
        final File corruptedDir = tmpFile(CORRUPTED_DIRNAME);

        assertFalse(corruptedDir.exists());

        final ExportService exportService = newExportService();
        exportService.createCorruptedDirectory(backupDir);
        assertTrue(corruptedDir.exists());

        exportService.createCorruptedDirectory(backupDir);
        assertTrue(corruptedDir.exists());

        deleteDirectory(CORRUPTED_DIRNAME);
        exportService.createCorruptedDirectory(backupDir);
        assertTrue(corruptedDir.exists());
    }

    @Test
    public void testWriteReasonFile()
    {
        createDirectory(CORRUPTED_DIRNAME);

        RuntimeException failureEx = new RuntimeException("An exception created for testing");
        final File backupFile = tmpFile(BACKUP_DIRNAME + SLASH + JIRA_EXPORT_ZIP);
        final File corruptedDir = tmpFile(CORRUPTED_DIRNAME);
        final File reasonFile = new File(corruptedDir, backupFile.getName() + ".failure.txt");

        final ExportService exportService = newExportService();
        exportService.writeFailureReasonFile(corruptedDir, backupFile, failureEx);

        assertTrue(reasonFile.exists());

        String contents = readFile(reasonFile);
        assertTrue(contents.indexOf(JIRA_EXPORT_ZIP) != -1);
        assertTrue(contents.indexOf("testWriteReasonFile") != -1);
        assertTrue(contents.indexOf("An exception created for testing") != -1);

        // do it again and make sure it over writes
        failureEx = new RuntimeException("Ran a second time");
        exportService.writeFailureReasonFile(corruptedDir, backupFile, failureEx);
        assertTrue(reasonFile.exists());

        contents = readFile(reasonFile);
        assertTrue(contents.indexOf(JIRA_EXPORT_ZIP) != -1);
        assertTrue(contents.indexOf("testWriteReasonFile") != -1);
        assertTrue(contents.indexOf("Ran a second time") != -1);

    }

    @Test
    public void testMoveFailedBackupFile()
    {

        createDirectory(CORRUPTED_DIRNAME);
        final File backupFile = tmpFile(BACKUP_DIRNAME + SLASH + JIRA_EXPORT_ZIP);
        final File corruptedDir = tmpFile(CORRUPTED_DIRNAME);
        final File corruptedBackupFile = tmpFile(CORRUPTED_DIRNAME + SLASH + JIRA_EXPORT_ZIP);

        final ExportService exportService = newExportService();

        // no data file exists
        boolean ok = exportService.moveFailedBackupFile(backupFile, corruptedDir);
        assertFalse(ok);

        // now create some actually data
        writeFile(backupFile, "Some Data That Would Be Backed Up");

        ok = exportService.moveFailedBackupFile(backupFile, corruptedDir);
        assertTrue(ok);

        assertFalse(backupFile.exists());
        assertTrue(corruptedBackupFile.exists());

        final String contents = readFile(corruptedBackupFile);
        assertTrue(contents.indexOf("Some Data That Would Be Backed Up") != -1);
    }

    @Test
    public void testMoveAside()
    {
        createDirectory(CORRUPTED_DIRNAME);
        final File backupDir = tmpFile(BACKUP_DIRNAME);
        final File backupFile = tmpFile(BACKUP_DIRNAME + SLASH + JIRA_EXPORT_ZIP);
        final File corruptedDir = tmpFile(CORRUPTED_DIRNAME);
        final File corruptedBackupFile = tmpFile(CORRUPTED_DIRNAME + SLASH + JIRA_EXPORT_ZIP);
        final File reasonFile = new File(corruptedDir, backupFile.getName() + ".failure.txt");
        final RuntimeException failureEx = new RuntimeException("An exception created for testing");

        final ExportService exportService = newExportService();

        // no data file exists
        boolean ok = exportService.moveBackupAside(backupDir, backupFile, failureEx);
        assertFalse(ok);

        // now create some actually data
        writeFile(backupFile, "Some Data That Would Be Backed Up");

        ok = exportService.moveBackupAside(backupDir, backupFile, failureEx);
        assertTrue(ok);

        assertFalse(backupFile.exists());
        assertTrue(corruptedBackupFile.exists());

        String contents = readFile(corruptedBackupFile);
        assertTrue(contents.indexOf("Some Data That Would Be Backed Up") != -1);

        assertTrue(reasonFile.exists());
        contents = readFile(reasonFile);
        assertTrue(contents.indexOf(JIRA_EXPORT_ZIP) != -1);
        assertTrue(contents.indexOf("testMoveAside") != -1); // should be in stack trace
        assertTrue(contents.indexOf("An exception created for testing") != -1);

    }

    @Test
    public void testExportService() throws ObjectConfigurationException
    {

        createDirectory(CORRUPTED_DIRNAME);

        final String dateFormatStr = "yyyy-MM-dd";
        final String nowStr = new SimpleDateFormat(dateFormatStr).format(new Date());
        final String backupFileName = nowStr + ".zip";

        final File backupDir = tmpFile(BACKUP_DIRNAME);
        final File backupFile = tmpFile(BACKUP_DIRNAME + SLASH + backupFileName);
        final File corruptedDir = tmpFile(CORRUPTED_DIRNAME);
        final File corruptedBackupFile = tmpFile(CORRUPTED_DIRNAME + SLASH + backupFileName);
        final File reasonFile = new File(corruptedDir, backupFile.getName() + ".failure.txt");
        final RuntimeException failureEx = new RuntimeException("An exception created for testing");

        final Properties props = new Properties();
        props.setProperty("DIR_NAME", backupDir.getAbsolutePath());
        props.setProperty("OPT_DATE_FORMAT", dateFormatStr);

        final Object propertySetDuck = new Object()
        {
            public boolean exists(final String string) throws com.opensymphony.module.propertyset.PropertyException
            {
                return props.containsKey(string);
            }

            public String getString(final String string) throws com.opensymphony.module.propertyset.PropertyException
            {
                return props.getProperty(string);
            }

        };
        final PropertySet propertySet = (PropertySet) DuckTypeProxy.getProxy(PropertySet.class, EasyList.build(propertySetDuck),
            DuckTypeProxy.RETURN_NULL);

        // need a service with action implementation and I18Helper
        final ExportService exportService = new ExportService(null)
        {
            @Override
            void performBackup(final String filename) throws Exception
            {
                // we actually need some data in the backup file after the service is invoked but before it throws an exception
                writeFile(backupFile, "Some Data That Would Be Backed Up");
                throw failureEx;
            }

            @Override
            I18nHelper getI18nHelper()
            {
                return newMockI18nHelper();
            }
        };
        exportService.init(propertySet);
        exportService.run();

        // it should have failed and created all the directories and files
        assertFalse(backupFile.exists());
        assertTrue(corruptedBackupFile.exists());

        String contents = readFile(corruptedBackupFile);
        assertTrue(contents.indexOf("Some Data That Would Be Backed Up") != -1);

        assertTrue(reasonFile.exists());
        contents = readFile(reasonFile);
        assertTrue(contents.indexOf(backupFileName) != -1);
        assertTrue(contents.indexOf("testExportService") != -1); // should be in stack trace
        assertTrue(contents.indexOf("An exception created for testing") != -1);
    }

    private void writeFile(final File file, final String data)
    {
        try
        {

            final PrintWriter writer = new PrintWriter(new FileWriter(file));
            writer.print(data);
            writer.close();
        }
        catch (final IOException e)
        {
            throw new RuntimeException("IOException reading file", e);
        }
    }

    private String readFile(final File file)
    {
        try
        {
            int lineCount = 0;
            String line;
            final StringBuilder sb = new StringBuilder();
            final BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
                if (lineCount++ > 0)
                {
                    sb.append("\n");
                }
            }
            reader.close();
            return sb.toString();
        }
        catch (final IOException e)
        {
            throw new RuntimeException("IOException reading file", e);
        }
    }

    private void createDirectory(final String dirName)
    {
        final File dir = tmpFile(dirName);
        if (!dir.exists())
        {
            if (!dir.mkdirs())
            {
                throw new RuntimeException("Couldnt create dir " + dir.getAbsolutePath());
            }
        }
    }

    private void deleteDirectory(final String dirName)
    {
        final File dir = tmpFile(dirName);
        if (dir.exists())
        {
            FileUtils.recursiveDelete(dir);
        }
    }

    private File tmpFile(String fileName)
    {
        final String tmpDir = System.getProperty("java.io.tmpdir");
        while (fileName.startsWith(SLASH))
        {
            fileName = fileName.substring(1);
        }
        return new File(tmpDir + SLASH + fileName);
    }

    ExportService newExportService()
    {
        return new ExportService(null)
        {

            @Override
            void performBackup(final String filename) throws Exception
            {}

            @Override
            I18nHelper getI18nHelper()
            {
                return newMockI18nHelper();
            }
        };
    }

    I18nHelper newMockI18nHelper()
    {
        return new I18nHelper()
        {

            public String getText(final String key)
            {
                return key;
            }

            public String getUnescapedText(final String key)
            {
                return key;
            }

            @Override
            public String getUntransformedRawText(String key)
            {
                return key;
            }

            @Override
            public boolean isKeyDefined(String key)
            {
                return true;
            }

            public Locale getLocale()
            {
                return null;
            }

            public String getText(final String key, final String value1)
            {
                return getText(key, value1, null, null);
            }

            public String getText(final String key, final String value1, final String value2)
            {
                return getText(key, value1, value2, null);
            }

            public String getText(final String key, final String value1, final String value2, final String value3) //(added by Shaun during i18n)
            {
                return getText(key, value1, value2, value3, null, null, null, null, null, null);
            }

            public String getText(final String key, final String value1, final String value2, final String value3, final String value4)
            {
                return null;
            }

            public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6)
            {
                return null;
            }

            public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7)
            {
                return null;
            }

            public String getText(String key, Object value1, Object value2, Object value3)
            {
                return getText(key, EasyList.build(value1, value2, value3));
            }

            public String getText(String key, Object value1, Object value2, Object value3, Object value4)
            {
                return getText(key, EasyList.build(value1, value2, value3, value4));
            }

            public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5)
            {
                return getText(key, EasyList.build(value1, value2, value3, value4, value5));
            }

            public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7)
            {
                return getText(key, EasyList.build(value1, value2, value3, value4, value5, value6, value7));
            }

            public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7, Object value8)
            {
                return getText(key, EasyList.build(value1, value2, value3, value4, value5, value6, value7, value8));
            }            

            public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7, final String value8, final String value9)
            {
                final StringBuffer sb = new StringBuffer(key);
                appendValue(sb, value1);
                appendValue(sb, value2);
                appendValue(sb, value3);
                appendValue(sb, value4);
                appendValue(sb, value5);
                appendValue(sb, value6);
                appendValue(sb, value7);
                appendValue(sb, value8);
                appendValue(sb, value9);
                return sb.toString();
            }

            private void appendValue(final StringBuffer sb, final String value)
            {
                if (value != null)
                {
                    sb.append(":");
                    sb.append(value);
                }
            }

            public ResourceBundle getDefaultResourceBundle()
            {
                return null;
            }

            public ResourceBundle getResourceBundle()
            {
                return null;
            }

            public String getText(final String key, final Object parameters)
            {
                return getText(key, String.valueOf(parameters));
            }

            public Set<String> getKeysForPrefix(final String prefix)
            {
                return null;
            }
        };
    }
}
