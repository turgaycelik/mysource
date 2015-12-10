package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.CheckMessage;
import com.atlassian.jira.functest.config.CheckOptions;
import com.atlassian.jira.functest.config.CheckResultBuilder;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import com.atlassian.jira.functest.config.ConfigurationChecker;
import com.atlassian.jira.functest.config.ConfigurationDefaults;
import com.atlassian.jira.functest.config.JiraConfig;
import com.atlassian.jira.functest.config.ZipHelper;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.MultiMap;
import com.atlassian.jira.util.collect.MultiMaps;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.TempDirectoryUtil;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test of the {@link com.atlassian.jira.functest.config.ConfigurationChecker}. This is an integration test
 * that checks the checker against some known data.
 */
public class TestConfigurationChecker extends TestCase implements EnvironmentAware
{
    @Override
    protected void tearDown() throws Exception
    {
    }

    public void testCheckerBadBackup() throws IOException
    {
        final File directory = TempDirectoryUtil.createTempDirectory("testCheckBadBackup").getCanonicalFile();
        try
        {
            ZipHelper.extractTo(getResource("fixerBroken.zip"), directory);

            final List<File> badFiles = new ArrayList<File>();
            final TestResult expectedResult = new TestResult();

            //Build up the expected failures.
            File currentFile = new File(directory, "BackupWithBackupService.xml");
            badFiles.add(currentFile);
            expectedResult.addError(currentFile, "Backup service 'Backup Service' exists.", "backupservice");
            expectedResult.addWarning(currentFile, "Global backup path set to 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\backups'.", "backupglobaldirectory");
            expectedResult.addWarning(currentFile, "File has 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\attachments' configured as it attachment path. It should always be set to 'func_test_attachments' even when using JIRA.HOME.", "attachdirectory");
            expectedResult.addWarning(currentFile, "File has 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\attachments' configured as its attachment path. It should be using its JIRA.HOME.", "attachhome");

            currentFile = new File(directory, "BackupWithBackupServiceCheckDisabled.xml");
            badFiles.add(currentFile);
            expectedResult.addError(currentFile, "Backup service configured to output to directory 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\backups' it should be using JIRA.HOME.", "backupservicehome");
            expectedResult.addWarning(currentFile, "Global backup path set to 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\backups'.", "backupglobaldirectory");
            expectedResult.addWarning(currentFile, "File has 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\attachments' configured as it attachment path. It should always be set to 'func_test_attachments' even when using JIRA.HOME.", "attachdirectory");
            expectedResult.addWarning(currentFile, "Backup service configured to output to 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\backups'. It should always be set to 'func_test_backup' even when using JIRA.HOME.", "backupservicedirectory");
            expectedResult.addWarning(currentFile, "File has 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\attachments' configured as its attachment path. It should be using its JIRA.HOME.", "attachhome");

            currentFile = new File(directory, "BackupWithIndexDirSpecifiedCheckDisabled.xml");
            badFiles.add(currentFile);
            expectedResult.addError(currentFile, "Backup service 'Backup Service' exists.", "backupservice");
            expectedResult.addWarning(currentFile, "File has 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\attachments' configured as its attachment path. It should be using its JIRA.HOME.", "attachhome");

            currentFile = new File(directory, "BackupWithAttachmentDirSpecified.zip");
            badFiles.add(currentFile);
            expectedResult.addError(currentFile, "File has '/private/tmp/jira_autotest/attachments' configured as its attachment path. It should be using its JIRA.HOME.", "attachhome");
            expectedResult.addError(currentFile, "Backup service 'Backup Service' exists.", "backupservice");
            expectedResult.addWarning(currentFile, "Global backup path set to 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\backups'.", "backupglobaldirectory");
            expectedResult.addWarning(currentFile, "File has '/Users/mtokar/tmp/jira/branch/3_13/indexes' configured as its index path. It should be using 'func_test_index' even with JIRA.HOME configured.", "indexdirectory");
            expectedResult.addWarning(currentFile, "File has '/private/tmp/jira_autotest/attachments' configured as it attachment path. It should always be set to 'func_test_attachments' even when using JIRA.HOME.", "attachdirectory");

            currentFile = new File(directory, "BackupWithDashboardAndGadgets.xml");
            badFiles.add(currentFile);
            expectedResult.addError(currentFile, "Non-system dashboard 'AdminDashboard' (10010) exists.", "dashboards");
            expectedResult.addError(currentFile, "Non-system dashboard 'MyNext' (10011) exists.", "dashboards");
            expectedResult.addError(currentFile, "Gadget on dashboard 'System Dashboard' (10000).", "gadgets");

            File subDir = new File(directory, "sub");

            currentFile = new File(subDir, "BackupWithAttachmentDirSpecifiedCheckDisabled.xml");
            badFiles.add(currentFile);
            expectedResult.addError(currentFile, "Backup service 'Backup Service' exists.", "backupservice");
            expectedResult.addWarning(currentFile, "File has '/Users/mtokar/tmp/jira/branch/3_13/indexes' configured as its index path. It should be using 'func_test_index' even with JIRA.HOME configured.", "indexdirectory");
            expectedResult.addWarning(currentFile, "File has '/private/tmp/jira_autotest/attachments' configured as it attachment path. It should always be set to 'func_test_attachments' even when using JIRA.HOME.", "attachdirectory");
            expectedResult.addWarning(currentFile, "Global backup path set to 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\backups'.", "backupglobaldirectory");

            currentFile = new File(subDir, "BackupWithIndexDirSpecified.xml");
            badFiles.add(currentFile);
            expectedResult.addError(currentFile, "File has '/tmp/jira_autotest/indexes' configured as its index path. It should be using its JIRA.HOME.", "indexhome");
            expectedResult.addError(currentFile, "Backup service 'Backup Service' exists.", "backupservice");
            expectedResult.addWarning(currentFile, "File has '/tmp/jira_autotest/indexes' configured as its index path. It should be using 'func_test_index' even with JIRA.HOME configured.", "indexdirectory");
            expectedResult.addWarning(currentFile, "File has 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\attachments' configured as it attachment path. It should always be set to 'func_test_attachments' even when using JIRA.HOME.", "attachdirectory");
            expectedResult.addWarning(currentFile, "Global backup path set to 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\backups'.", "backupglobaldirectory");
            expectedResult.addWarning(currentFile, "File has 'C:\\DOCUME~1\\mchai\\LOCALS~1\\Temp\\\\jira_autotest\\attachments' configured as its attachment path. It should be using its JIRA.HOME.", "attachhome");

            currentFile = new File(subDir, "BackupWithMailServerAndService.xml");
            badFiles.add(currentFile);
            expectedResult.addError(currentFile, "Mail server 'fake server' to 'POP:fake.example' for user 'meh' exists.", "mailserver");
            expectedResult.addError(currentFile, "Mail service 'Pop Service' exists.", "mailservice");
            expectedResult.addWarning(currentFile, "File has '/private/tmp/jira_autotest/indexes' configured as its index path. It should be using 'func_test_index' even with JIRA.HOME configured.", "indexdirectory");
            expectedResult.addWarning(currentFile, "File has 'C:\\temp\\files' configured as it attachment path. It should always be set to 'func_test_attachments' even when using JIRA.HOME.", "attachdirectory");
            expectedResult.addWarning(currentFile, "File has 'C:\\temp\\files' configured as its attachment path. It should be using its JIRA.HOME.", "attachhome");

            currentFile = new File(subDir, "BackupWithAbsoluteAndExternalGadgets.xml");
            badFiles.add(currentFile);
            expectedResult.addError(currentFile, "External gadget 'http://localhost:8091/jira/rest/gadgets/1.0/g/com.atlassian.jira.gadgets:created-vs-resolved-issues-chart-gadget/gadgets/createdvsresolved-gadget.xml' configured.", "externalgadgets");
            expectedResult.addError(currentFile, "Gadget URL 'https://jira.atlaassian.com/rest/gadgets/1.0/g/com.atlassian.jira.gadgets:in-progress-gadget/gadgets/in-progress-gadget.xml' is absolute on dashboard 'MyNext' (10011).", "absolutegadgets");

            currentFile = new File(directory, "empty.xml");
            expectedResult.addErrorRegex(currentFile, "Unable to read configuration.*");

            currentFile = new File(directory, "other.xml");
            expectedResult.addWarning(currentFile, "File does not appear to contain JIRA XML.", null);

            final List<ConfigurationCheck> checks = new ArrayList<ConfigurationCheck>(ConfigurationDefaults.createDefaultConfigurationChecks());
            final RecordingConfigurationCheck recCheck = new RecordingConfigurationCheck();
            checks.add(recCheck);

            final File goodFile = new File(directory, "BackupGood.xml");
            final File checkDisabledFile = new File(directory, "BackupWithMailServerAndServiceCheckDisabled.xml");
            final List<File> goodFiles = Arrays.asList(goodFile, checkDisabledFile);

            final ConfigurationChecker checker = new ConfigurationChecker(directory, checks);
            expectedResult.assertEquals(checker.check());
            assertTrue(recCheck.getFiles().containsAll(goodFiles));
            assertTrue(recCheck.getFiles().containsAll(badFiles));

            //We should now have the cache.
            currentFile = new File(directory, "check-cache.xml");
            assertTrue(currentFile.exists());

            TestResult cacheResult = new TestResult(expectedResult);
            cacheResult.addWarning(currentFile, "File does not appear to contain JIRA XML.", null);
            recCheck.clear();
            cacheResult.assertEquals(checker.check());
            assertTrue(Collections.disjoint(recCheck.getFiles(), goodFiles));
            assertTrue(recCheck.getFiles().containsAll(badFiles));

            //Lets exclude the cache and make sure things still work as expected.
            checker.setExcludes(Collections.<IOFileFilter>singletonList(new NameFileFilter("check-cache.xml")));
            recCheck.clear();
            expectedResult.assertEquals(checker.check());
            assertTrue(Collections.disjoint(recCheck.getFiles(), goodFiles));
            assertTrue(recCheck.getFiles().containsAll(badFiles));

            //Lets add a new check and ensure that every file is rechecked even though they have not changed.
            checks.add(new NoopConfigurationCheck());
            checker.setConfigurationChecks(checks);
            recCheck.clear();
            expectedResult.assertEquals(checker.check());
            assertTrue(recCheck.getFiles().containsAll(goodFiles));
            assertTrue(recCheck.getFiles().containsAll(badFiles));

            //Lets change one of the file (add an extra comment) to make sure it is checked again.
            appendToFile(goodFile, "<!-- Comment by Test -->");
            recCheck.clear();
            expectedResult.assertEquals(checker.check());
            assertTrue(recCheck.getFiles().contains(goodFile));
            assertFalse(recCheck.getFiles().contains(checkDisabledFile));
            assertTrue(recCheck.getFiles().containsAll(badFiles));
        }
        finally
        {
            FileUtils.deleteQuietly(directory);
        }
    }

    public void setEnvironmentData(final JIRAEnvironmentData environmentData)
    {
    }

    private InputStream getResource(final String child)
    {
        return getClass().getResourceAsStream("/xml/" + child);
    }

    private void appendToFile(File file, String data) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(file, true);
        try
        {
            fos.write(data.getBytes("UTF-8"));
        }
        finally
        {
            IOUtils.closeQuietly(fos);
        }
    }

    private static class TestResult
    {
        private final MultiMap<File, Predicate<CheckMessage>, List<Predicate<CheckMessage>>> errors = createMultiMap();
        private final MultiMap<File, Predicate<CheckMessage>, List<Predicate<CheckMessage>>> warnings = createMultiMap();

        private TestResult()
        {
        }

        private TestResult(TestResult result)
        {
            errors.putAll(result.errors);
            warnings.putAll(result.warnings);
        }

        private void addWarning(File file, String message, String checkId)
        {
            addWarning(file, new CheckMessage(message, checkId));
        }

        private void addWarning(File file, final CheckMessage message)
        {
            addMessage(warnings, file, message);
        }

        private void addError(File file, String message, String checkId)
        {
            addError(file, new CheckMessage(message, checkId));
        }

        private void addError(File file, final CheckMessage message)
        {
            addMessage(errors, file, message);
        }

        private void addErrorRegex(File file, final String regex)
        {
            errors.putSingle(file, new Predicate<CheckMessage>()
            {
                public boolean evaluate(final CheckMessage input)
                {
                    return input.toString().matches(regex);
                }

                @Override
                public String toString()
                {
                    return "Match regex: " + regex;
                }
            });
        }

        private void addMessage(MultiMap<File, Predicate<CheckMessage>, List<Predicate<CheckMessage>>> maps, File file,
                final CheckMessage message)
        {
            maps.putSingle(file, new Predicate<CheckMessage>()
            {
                public boolean evaluate(final CheckMessage input)
                {
                    return message.equals(input);
                }

                @Override
                public String toString()
                {
                    return "Match message: " + message;
                }
            });
        }

        private void assertEquals(ConfigurationChecker.CheckResult result)
        {
            assertMaps("ERRORs", errors, result.getErrors());
            assertMaps("WARNs", warnings, result.getWarnings());
        }

        private void assertMaps(String type, Map<File, List<Predicate<CheckMessage>>> expected, Map<File, List<CheckMessage>> actual)
        {
            final Set<File> expectedKeys = expected.keySet();
            final Set<File> actualKeys = actual.keySet();

            if (!actualKeys.equals(expectedKeys))
            {
                Set<File> extras = new HashSet<File>(actualKeys);
                extras.removeAll(expectedKeys);

                Set<File> missing = new HashSet<File>(expectedKeys);
                missing.removeAll(actualKeys);

                fail(String.format("Files for '%s' are not the same. Extras = %s, Missing = %s.", type, extras, missing));
            }

            for (Map.Entry<File, List<CheckMessage>> entry : actual.entrySet())
            {
                final List<CheckMessage> actualList = entry.getValue();
                final List<Predicate<CheckMessage>> predicateList = new ArrayList<Predicate<CheckMessage>>(expected.get(entry.getKey()));

                for (CheckMessage message : actualList)
                {
                    boolean found = false;
                    for (Iterator<Predicate<CheckMessage>> iterator = predicateList.iterator(); iterator.hasNext();)
                    {
                        Predicate<CheckMessage> predicate = iterator.next();
                        if (predicate.evaluate(message))
                        {
                            found = true;
                            iterator.remove();
                        }
                    }
                    if (!found)
                    {
                        fail(String.format("Could not find match. Type = %s, File = '%s', Message = '%s.%n", type, entry.getKey(), message.toString()));
                    }
                }

                if (!predicateList.isEmpty())
                {
                    fail(String.format("Expecting message to match '%s'. Type = %s, File = '%s'", predicateList, type, entry.getKey()));
                }
            }
        }

        private static MultiMap<File, Predicate<CheckMessage>, List<Predicate<CheckMessage>>> createMultiMap()
        {
            return MultiMaps.create(new LinkedHashMap<File, List<Predicate<CheckMessage>>>(), new Supplier<List<Predicate<CheckMessage>>>()
            {
                public List<Predicate<CheckMessage>> get()
                {
                    return new ArrayList<Predicate<CheckMessage>>();
    }
            });
        }
    }

    private static class RecordingConfigurationCheck implements ConfigurationCheck
    {
        private final List<File> files = new ArrayList<File>();

        public Result checkConfiguration(final JiraConfig config, final CheckOptions options)
        {
            files.add(config.getFile());
            return new CheckResultBuilder().buildResult();
        }

        public void fixConfiguration(final JiraConfig config, final CheckOptions options)
        {
            throw new UnsupportedOperationException();
        }

        private List<File> getFiles()
        {
            return files;
        }

        private void clear()
        {
            files.clear();
        }
    }

    private static class NoopConfigurationCheck implements ConfigurationCheck
    {
        public Result checkConfiguration(final JiraConfig config, final CheckOptions options)
        {
            return new CheckResultBuilder().buildResult();
        }

        public void fixConfiguration(final JiraConfig config, final CheckOptions options)
        {
            throw new UnsupportedOperationException();
        }
    }
}
