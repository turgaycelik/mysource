package com.atlassian.jira.bc.license;

import com.atlassian.jira.license.JiraLicenseManager;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import java.text.NumberFormat;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The implementation of JiraLicenseService
 *
 * @since v4.0
 */
public class JiraLicenseServiceImpl implements JiraLicenseUpdaterService
{
    /**
     * the minimal license version accepted by JIRA (from 4.0)
     */
    private static final int MIN_LICENSE_VERSION = 2;

    private final JiraLicenseManager licenseManager;
    private final UserUtil userUtil;

    public JiraLicenseServiceImpl(JiraLicenseManager licenseManager, final UserUtil userUtil)
    {
        this.licenseManager = notNull("licenseManager", licenseManager);
        this.userUtil = notNull("userUtil", userUtil);
    }

    @Override
    public String getServerId()
    {
        return licenseManager.getServerId();
    }

    @Override
    public LicenseDetails getLicense()
    {
        return licenseManager.getLicense();
    }

    @Override
    public ValidationResult validate(final I18nHelper i18nHelper, final String licenseString)
    {
        final OurValidationResult validationResult = new OurValidationResult(licenseString);

        if (!licenseManager.isDecodeable(licenseString))
        {
            validationResult.addError(i18nHelper.getText("setup.error.invalidlicensekey"));
        }
        else
        {
            final LicenseDetails licenseDetails = licenseManager.getLicense(licenseString);

            checkLicenseVersion(i18nHelper, validationResult, licenseDetails);

        }
        return validationResult;
    }

    private boolean checkLicenseVersion(final I18nHelper i18nHelper, final OurValidationResult validationResult, final LicenseDetails licenseDetails)
    {
        final int licenseVersion = licenseDetails.getLicenseVersion();
        validationResult.setLicenseVersion(licenseVersion);
        if (licenseVersion < MIN_LICENSE_VERSION)
        {
            final NumberFormat nf = NumberFormat.getNumberInstance();
            final int totalUserCount = userUtil.getTotalUserCount();
            final int activeUserCount = userUtil.getActiveUserCount();

            validationResult.setTotalUserCount(totalUserCount);
            validationResult.setActiveUserCount(activeUserCount);
            validationResult.addError(i18nHelper.getText("setup.error.invalidlicensekey.v1.license.version", nf.format(totalUserCount), nf.format(activeUserCount)));
            return false;
        }
        return true;
    }

    @Override
    public LicenseDetails setLicense(final ValidationResult validationResult)
    {
        if (validationResult == null || validationResult.getErrorCollection() == null || validationResult.getErrorCollection().hasAnyErrors())
        {
            throw new IllegalStateException("setLicense called with illegal ValidationResult object");
        }
        return licenseManager.setLicense(validationResult.getLicenseString());
    }

    @Override
    public LicenseDetails setLicenseNoEvent(ValidationResult validationResult)
    {
        if (validationResult == null || validationResult.getErrorCollection() == null || validationResult.getErrorCollection().hasAnyErrors())
        {
            throw new IllegalStateException("setLicense called with illegal ValidationResult object");
        }
        return licenseManager.setLicenseNoEvent(validationResult.getLicenseString());
    }

    @Override
    public void confirmProceedUnderEvaluationTerms(final String userName)
    {
        licenseManager.confirmProceedUnderEvaluationTerms(userName);
    }

    @Override
    public Iterable<LicenseDetails> getLicenses()
    {
        return licenseManager.getLicenses();
    }

    private static final class OurValidationResult implements ValidationResult
    {
        private static final String LICENSE_FIELD = "license";

        private final SimpleErrorCollection errorCollection;
        private final String licenceString;

        private int totalUserCount;
        private int activeUserCount;

        private int licenseVersion;

        public OurValidationResult(final String licenceString)
        {
            this.licenceString = licenceString;
            this.errorCollection = new SimpleErrorCollection();
        }

        private void addError(String message)
        {
            // we always use the license field!
            errorCollection.addError(LICENSE_FIELD, message);
        }

        public ErrorCollection getErrorCollection()
        {
            final SimpleErrorCollection copied = new SimpleErrorCollection();
            copied.addErrorCollection(errorCollection);
            return copied;
        }

        public String getLicenseString()
        {
            return licenceString;
        }

        public int getLicenseVersion()
        {
            return licenseVersion;
        }

        public void setLicenseVersion(final int licenseVersion)
        {
            this.licenseVersion = licenseVersion;
        }

        public int getTotalUserCount()
        {
            return totalUserCount;
        }

        public void setTotalUserCount(final int totalUserCount)
        {
            this.totalUserCount = totalUserCount;
        }

        public int getActiveUserCount()
        {
            return activeUserCount;
        }

        public void setActiveUserCount(final int activeUserCount)
        {
            this.activeUserCount = activeUserCount;
        }
    }
}
