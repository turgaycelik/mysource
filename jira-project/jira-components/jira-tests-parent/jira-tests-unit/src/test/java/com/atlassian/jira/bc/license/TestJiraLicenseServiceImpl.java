package com.atlassian.jira.bc.license;

import com.atlassian.jira.license.JiraLicenseManager;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 */
public class TestJiraLicenseServiceImpl extends MockControllerTestCase
{
    private JiraLicenseManager licenseManager;
    private LicenseDetails licenseDetails;
    private UserUtil userUtil;
    private I18nHelper i18nHelper;
   private static final String EXPECTED_MSG = "expectedMsg";
    private static final String BAD_LIC_STRING = "somebadstring";
    private static final String GOOD_LIC_STRING = "adam goodes";
    private BuildUtilsInfo buildUtilsInfo;
    private static final String USER_NAME = "userName";

    @Before
    public void setUp() throws Exception
    {
        licenseManager = getMock(JiraLicenseManager.class);
        licenseDetails = getMock(LicenseDetails.class);
        i18nHelper = getMock(I18nHelper.class);
        buildUtilsInfo = getMock(BuildUtilsInfo.class);
        userUtil = getMock(UserUtil.class);
    }

    @Test
    public void testGetLicense()
    {
        expect(licenseManager.getLicense()).andReturn(licenseDetails);

        JiraLicenseServiceImpl licenseService = instantiate(JiraLicenseServiceImpl.class);
        LicenseDetails actualLicenseDetails = licenseService.getLicense();
        assertSame(licenseDetails, actualLicenseDetails);
    }

    @Test
    public void testSetLicense_NullValidationResult()
    {
        try
        {
            final JiraLicenseServiceImpl licenseService = instantiate(JiraLicenseServiceImpl.class);
            licenseService.setLicense(null);
            fail("Should have barfed");
        }
        catch (IllegalStateException expected)
        {
        }
    }

    @Test
    public void testSetLicense_ValidationResultHasNoErrorCollection()
    {
        JiraLicenseService.ValidationResult validationResult = getMock(JiraLicenseService.ValidationResult.class);
        expect(validationResult.getErrorCollection()).andReturn(null);
        try
        {
            final JiraLicenseServiceImpl licenseService = instantiate(JiraLicenseServiceImpl.class);
            licenseService.setLicense(validationResult);
            fail("Should have barfed");
        }
        catch (IllegalStateException expected)
        {
        }
    }

    @Test
    public void testSetLicense_ValidationResultHasErrorCollectionWithErrors()
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addError("shite", "happens");

        JiraLicenseService.ValidationResult validationResult = getMock(JiraLicenseService.ValidationResult.class);
        expect(validationResult.getErrorCollection()).andReturn(errorCollection).times(2);
        try
        {
            final JiraLicenseServiceImpl licenseService = instantiate(JiraLicenseServiceImpl.class);
            licenseService.setLicense(validationResult);
            fail("Should have barfed");
        }
        catch (IllegalStateException expected)
        {
        }
    }

    @Test
    public void testSetLicense_HappyPath()
    {
        expect(licenseManager.setLicense(GOOD_LIC_STRING)).andReturn(licenseDetails);

        JiraLicenseService.ValidationResult validationResult = getMock(JiraLicenseService.ValidationResult.class);
        expect(validationResult.getErrorCollection()).andReturn(new SimpleErrorCollection()).anyTimes();
        expect(validationResult.getLicenseString()).andReturn(GOOD_LIC_STRING);

        JiraLicenseServiceImpl licenseService = instantiate(JiraLicenseServiceImpl.class);
        LicenseDetails actualLicenseDetails = licenseService.setLicense(validationResult);
        assertSame(licenseDetails, actualLicenseDetails);
    }

    @Test
    public void testValidate_InvalidString()
    {

        expect(licenseManager.isDecodeable(BAD_LIC_STRING)).andReturn(false);
        expect(i18nHelper.getText("setup.error.invalidlicensekey")).andReturn(EXPECTED_MSG);

        JiraLicenseServiceImpl licenseService = instantiate(JiraLicenseServiceImpl.class);
        final JiraLicenseService.ValidationResult validationResult = licenseService.validate(i18nHelper, BAD_LIC_STRING);
        assertValidationResult(validationResult, EXPECTED_MSG);
    }
    
    @Test
    public void testValidate_InvalidLicenseVersion()
    {
        final int invalidLicenseVersion = 1;
        final String partner = "Sam";

        expect(licenseManager.isDecodeable(BAD_LIC_STRING)).andReturn(true);
        expect(licenseManager.getLicense(BAD_LIC_STRING)).andReturn(licenseDetails);
        expect(licenseDetails.getLicenseVersion()).andReturn(invalidLicenseVersion);
        expect(i18nHelper.getText(this.<String>eq("setup.error.invalidlicensekey.v1.license.version"), this.<String>anyObject(), this.<String>anyObject() )).andReturn(EXPECTED_MSG);
        expect(userUtil.getTotalUserCount()).andReturn(10);
        expect(userUtil.getActiveUserCount()).andReturn(5);

        JiraLicenseServiceImpl licenseService = instantiate(JiraLicenseServiceImpl.class);
        final JiraLicenseService.ValidationResult validationResult = licenseService.validate(i18nHelper, BAD_LIC_STRING);
        assertValidationResult(validationResult, EXPECTED_MSG);
    }
    @Test
    public void testConfirmProceed()
    {
        licenseManager.confirmProceedUnderEvaluationTerms(USER_NAME);
        expectLastCall();

        JiraLicenseService licenseService = instantiate(JiraLicenseServiceImpl.class);
        licenseService.confirmProceedUnderEvaluationTerms(USER_NAME);
    }

    @Test
    public void testGetServerId() throws Exception
    {
        final String serverId = "A server ID";
        expect(licenseManager.getServerId()).andReturn(serverId);
        JiraLicenseService licenseService = instantiate(JiraLicenseServiceImpl.class);
        assertEquals(serverId, licenseService.getServerId());
    }

    private void assertValidationResult(final JiraLicenseService.ValidationResult validationResult, final String expectedMsg)
    {
        assertNotNull(validationResult);
        assertTrue(validationResult.getErrorCollection().hasAnyErrors());
        assertEquals(expectedMsg, validationResult.getErrorCollection().getErrors().get("license"));
    }
}
