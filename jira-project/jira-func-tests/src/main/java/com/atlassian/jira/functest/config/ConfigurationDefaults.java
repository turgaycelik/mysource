package com.atlassian.jira.functest.config;

import com.atlassian.jira.functest.config.crowd.CrowdApplicationCheck;
import com.atlassian.jira.functest.config.crowd.PlaintextEncoderChecker;
import com.atlassian.jira.functest.config.dashboard.DashboardConfigurationCheck;
import com.atlassian.jira.functest.config.mail.MailChecker;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DelegateFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Holds the default configuration values for the XML checker and fixer.
 */
public final class ConfigurationDefaults
{
    private ConfigurationDefaults()
    {
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("Why are you calling me? I'm static.");
    }

    public static File getDefaultXmlDataLocation()
    {
        LocalTestEnvironmentData environmentData = new LocalTestEnvironmentData();
        final File xmlDataLocation = environmentData.getXMLDataLocation();
        if (xmlDataLocation == null)
        {
            throw new RuntimeException("Func Test XML directory has not been configured.");
        }

        if (!xmlDataLocation.exists())
        {
            throw new RuntimeException(String.format("Func Test XML directory '%s' does not exist.", xmlDataLocation));
        }

        return xmlDataLocation;
    }

    public static List<IOFileFilter> getDefaultExcludedFilters()
    {
        /*
            NOTE: DO NOT JUST ADD YOUR FILE HERE IF THE UNIT TEST FAILS. Have a look at
            https://extranet.atlassian.com/x/GAW7b for information on disabling individual checks for a file.
            By adding a file to this list you could be hiding problems in your XML that can cause the
            func tests to fail intermittently.
         */

        List<IOFileFilter> files = new ArrayList<IOFileFilter>();

        //START: NON-backup files.
        files.add(createNameFilter("TestXmlIssueView-HSP-1-no-issue-links.xml"));
        files.add(createNameFilter("TestXmlIssueView-HSP-1-no-timetracking.xml"));
        files.add(createNameFilter("TestXmlIssueView-HSP-1-due-date-hidden.xml"));
        files.add(createNameFilter("TestXmlIssueView-HSP-1.xml"));
        files.add(createNameFilter("TestXmlIssueView-HSP-1-no-subtasks.xml"));
        files.add(createNameFilter("TestXmlIssueView-HSP-1-no-custom-fields.xml"));
        files.add(createNameFilter("check-cache.xml"));
        files.add(createNameFilter("fixerBroken.zip"));
        files.add(createParentFilter("TestDownloadZipAttachmentEntries/attachments"));
        files.add(createParentFilter("TestZipAttachmentSecurity/attachments"));
        files.add(createParentFilter("TestAttachmentsBlockSortingOnViewIssue/attachments"));
        files.add(createParentFilter("TestBrowseZipAttachmentEntries/attachments"));
        //END: NON-backup files.

        //Generated file.
        files.add(createNameFilter("TestEmptyStringDataRestore_out.xml"));
        /*
            NOTE: DO NOT JUST ADD YOUR FILE HERE IF THE UNIT TEST FAILS. Have a look at
            https://extranet.atlassian.com/x/GAW7b for information on disabling individual checks for a file.
            By adding a file to this list you could be hiding problems in your XML that can cause the
            func/selenium tests to fail intermittently.
         */

        return files;
    }

    public static List<ConfigurationCheck> createDefaultConfigurationChecks()
    {
        return ImmutableList.of(
                new AttachmentDirectoryChecker(),
                new IndexDirectoryChecker(),
                new BackupChecker(),
                new MailChecker(),
                new DashboardConfigurationCheck(),
                new ServiceChecker(),
                new CrowdApplicationCheck(),
                new PlaintextEncoderChecker());
        /* NOTE: if you add any checks here, please add keys to getListOfDefaultConfigurationChecksSuppressKeys() */
    }

    public static List<String> getListOfDefaultConfigurationChecksSuppressKeys()
    {
        return ImmutableList.<String>builder()
                .add(AttachmentDirectoryChecker.CHECKID_ATTACH_DIR)
                .add(AttachmentDirectoryChecker.CHECKID_ATTACH_HOME)
                .add(IndexDirectoryChecker.CHECKID_INDEX_DIRECTORY)
                .add(IndexDirectoryChecker.CHECKID_INDEX_ENABLED)
                .add(IndexDirectoryChecker.CHECKID_INDEX_HOME)
                .add(BackupChecker.CHECK_BACKUP_SERVICE)
                .add(BackupChecker.CHECK_BACKUP_SERVICE_DIRECTORY)
                .add(BackupChecker.CHECK_BACKUP_SERVICE_HOME)
                .add(BackupChecker.CHECK_GLOBAL_BACKUP_DIRECTORY)
                .add(MailChecker.CHECK_MAIL_SERVER)
                .add(MailChecker.CHECK_MAIL_SERVICE)
                .add(DashboardConfigurationCheck.CHECKID_ABSOLUTE)
                .add(DashboardConfigurationCheck.CHECKID_DASHBOARDS)
                .add(DashboardConfigurationCheck.CHECKID_EXTERNAL)
                .add(DashboardConfigurationCheck.CHECKID_GADGETS)
                .add(ServiceChecker.CHECK_SERVICE)
                .add(CrowdApplicationCheck.CHECK_APPLICATION_TYPE)
                .add(PlaintextEncoderChecker.CHECK_PLAINTEXT_ENCODER)
                .build();
    }

    private static IOFileFilter createNameFilter(final String name)
    {
        return new NameFileFilter(name, IOCase.INSENSITIVE);
    }

    private static IOFileFilter createParentFilter(final String name)
    {
        //Bit of a hack but it should work well enough.
        final String s = Pattern.quote(FilenameUtils.separatorsToSystem(name));
        return new DelegateFileFilter(new RegexPathFilter(s));
    }

    private static class RegexPathFilter implements FileFilter
    {
        private final Pattern patten;

        public RegexPathFilter(final String patten)
        {
            this.patten = Pattern.compile(patten, Pattern.CASE_INSENSITIVE);
        }

        public boolean accept(final File pathname)
        {
            return patten.matcher(pathname.getPath()).find();
        }
    }
}
