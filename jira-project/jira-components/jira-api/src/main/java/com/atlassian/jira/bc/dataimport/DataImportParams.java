package com.atlassian.jira.bc.dataimport;

import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * Provides all the parameters required to perform a data import in JIRA.  Specifically this includes:
 * <strong>Required</strong>
 * <ul>
 *     <li>filename - Path of the file to be imported. Only compulsory if unsafeJiraBackup has not be specified.
 *     </li>
 *     <li>unsafeJiraBackup - Path of the file to be imported. Only compulsory if filename has not be specified. JIRA
 *       will use the file for import no matter where it is located (i.e. it does not need to be JIRA_HOME).
 *     </li>
 * </ul>
 * <p/>
 * <strong>Optional</strong>
 * <ul>
 *     <li>licenseString - A license string to override the license from the XML data file</li>
 *     <li>quickImport - If this is set to true the plugins system will *not* be restarted. (false by default)</li>
 *     <li>useDefaultPaths - Whether default paths in JIRA home should be used for indexing & attachments (true by default)</li>
 *     <li>isSetup - true if this import is being performed during JIRA's setup (false by default)</li>
 *     <li>disableOutgoingEmail - true if outgoing email should be disabled after importing the xml data file.</li>
 *     <li>unsafeAOBackup - the path to the AO backup. JIRA will use the file for import no matter where
 *          it is located (i.e. it does not need to be JIRA_HOME).
 *     </li>
 * </ul>
 * <p/>
 * Clients should use the provided {@link DataImportParams.Builder} to construct an instance of this class.
 *
 * @since v4.4
 */
public final class DataImportParams
{
    private final String filename;
    private final String licenseString;
    private final boolean quickImport;
    private final boolean useDefaultPaths;
    private final boolean allowDowngrade;
    private final boolean isSetup;
    private final boolean outgoingMail;
    private final boolean changeOutgoingMailSettings;
    private final File unsafeJiraBackup;
    private final File unsafeAOBackup;
    private final boolean safeMode;
    private final boolean noLicenseCheck;
    private final boolean startupDataOnly;

    private DataImportParams(String filename, String licenseString, boolean quickImport, boolean useDefaultPaths,
            boolean allowDowngrade, boolean isSetup, boolean outgoingMail, boolean changeOutgoingMailSettings, File unsafeJiraBackup, File unsafeAOBackup, final boolean safeMode, final boolean noLicenseCheck, final boolean startupDataOnly)
    {
        this.filename = filename;
        this.licenseString = licenseString;
        this.quickImport = quickImport;
        this.useDefaultPaths = useDefaultPaths;
        this.allowDowngrade = allowDowngrade;
        this.isSetup = isSetup;
        this.outgoingMail = outgoingMail;
        this.changeOutgoingMailSettings = changeOutgoingMailSettings;
        this.unsafeJiraBackup = unsafeJiraBackup;
        this.unsafeAOBackup = unsafeAOBackup;
        this.safeMode = safeMode;
        this.noLicenseCheck = noLicenseCheck;
        this.startupDataOnly = startupDataOnly;
    }

    public String getFilename()
    {
        return filename;
    }

    public String getLicenseString()
    {
        return licenseString;
    }

    public boolean isQuickImport()
    {
        return quickImport;
    }

    public boolean isUseDefaultPaths()
    {
        return useDefaultPaths;
    }

    public boolean isAllowDowngrade()
    {
        return allowDowngrade;
    }

    public boolean isSetup()
    {
        return isSetup;
    }

    public File getUnsafeJiraBackup()
    {
        return unsafeJiraBackup;
    }

    public File getUnsafeAOBackup()
    {
        return unsafeAOBackup;
    }

    public boolean outgoingMail()
    {
        return outgoingMail;
    }

    public boolean shouldChangeOutgoingMail()
    {
        return changeOutgoingMailSettings;
    }

    public boolean isSafeMode()
    {
        return safeMode;
    }

    public boolean isNoLicenseCheck()
    {
        return noLicenseCheck;
    }

    public boolean isStartupDataOnly()
    {
        return startupDataOnly;
    }

    public static class Builder
    {
        private final String filename;
        private String licenseString;
        private boolean quickImport = false;
        private boolean useDefaultPaths = true;
        private boolean allowDowngrade = false;
        private boolean isSetup = false;
        private boolean outgoingEmail;
        private File unsafeJiraBackup;
        private File unsafeAOBackup;
        private boolean changeOutgoingMail = false;
        private boolean safeMode = false;
        private boolean noLicenseCheck = false;
        private boolean startupDataOnly = false;

        public Builder(String filename)
        {
            this.filename = StringUtils.stripToNull(filename);
        }

        public Builder setupImport()
        {
            isSetup = true;
            return this;
        }

        public Builder setLicenseString(String licenseString)
        {
            this.licenseString = StringUtils.stripToNull(licenseString);
            return this;
        }

        public Builder setQuickImport(boolean quickImport)
        {
            this.quickImport = quickImport;
            return this;
        }

        public Builder setUseDefaultPaths(boolean useDefaultPaths)
        {
            this.useDefaultPaths = useDefaultPaths;
            return this;
        }

        public Builder setAllowDowngrade(boolean allowDowngrade)
        {
            this.allowDowngrade = allowDowngrade;
            return this;
        }

        public File getUnsafeJiraBackup()
        {
            return unsafeJiraBackup;
        }

        public Builder setUnsafeJiraBackup(File unsafeJiraBackup)
        {
            this.unsafeJiraBackup = unsafeJiraBackup;
            return this;
        }

        public File getUnsafeAOBackup()
        {
            return unsafeAOBackup;
        }

        public Builder setUnsafeAOBackup(File unsafeAOBackup)
        {
            this.unsafeAOBackup = unsafeAOBackup;
            return this;
        }

        public Builder setOutgoingEmailTo(boolean outgoingEmail)
        {
            this.outgoingEmail = outgoingEmail;
            this.changeOutgoingMail = true;
            return this;
        }

        public Builder setSafeMode()
        {
            this.safeMode = true;
            return this;
        }

        public Builder setNoLicenseCheck()
        {
            this.noLicenseCheck = true;
            return this;
        }

        public Builder setStartupDataOnly()
        {
            this.startupDataOnly = true;
            return this;
        }

        public DataImportParams build()
        {
            return new DataImportParams(this.filename, this.licenseString, this.quickImport,
                    this.useDefaultPaths, allowDowngrade, this.isSetup, this.outgoingEmail, this.changeOutgoingMail, this.unsafeJiraBackup, this.unsafeAOBackup, this.safeMode, noLicenseCheck, startupDataOnly);
        }
    }
}
