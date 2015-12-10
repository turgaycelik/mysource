package com.atlassian.jira.license;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.InitMockitoMocks;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test case for {@link com.atlassian.jira.license.TestJiraLicenseStoreImpl}.
 *
 * @since v4.0
 */
public class TestJiraLicenseStoreImpl
{
    private static final String GOOD_LICENSE_STRING = "Goodlicensestring";

    // keys from APKeys.
    private static final String POST20_LICENSE_STRING_KEY = "License20";
    private static final String OLD_PRE20_LICENSE_MESSAGE_KEY_TEXT = "License Message Text";
    private static final String OLD_PRE20_LICENSE_HASH_KEY_TEXT = "License Hash 1 Text";
    private static final String REALLY_OLD_MESSAGE_KEY = "License Message";
    private static final String REALLY_OLD_MESSAGE_HASH = "License Hash 1";

    /*
    * The license recomposed from license message and hash is never the same, the end of the license string however is
    * constant, that's what we test with here.
    */
    private static final String LIC_MSG = "License message\u00C9";
    private static final String LIC_HASH = "License hash\u00C9";
    private static final String ENTERPRISE = "enterprise";
    private static final String JIRA_EDITION = "jira.edition";
    private static final String USER_NAME = "userName";
    private static final String LIC_WITH_WHITE_SPACE_IN_IT = "Some Lic\nwith\twhite\rspace in it";
    private static final String LIC_WITH_NO_WHITE_SPACE_IN_IT = "SomeLicwithwhitespaceinit";


    @Rule public InitMockitoMocks initMocks = new InitMockitoMocks(this);

    @Mock private ApplicationProperties applicationProperties;
    @Mock private LicenseStringFactory licenseStringFactory;


    private JiraLicenseStoreImpl createStore()
    {
        return new JiraLicenseStoreImpl(applicationProperties, licenseStringFactory);
    }

    @Test
    public void testRetrieve_From20Key()
    {
        when(applicationProperties.getText(POST20_LICENSE_STRING_KEY)).thenReturn(GOOD_LICENSE_STRING);
        final String actualLicenseString = createStore().retrieve();
        assertEquals(GOOD_LICENSE_STRING, actualLicenseString);
    }

    @Test
    public void testRetrieve_FromPre20TextKeys()
    {
        when(applicationProperties.getText(POST20_LICENSE_STRING_KEY)).thenReturn(null);

        when(applicationProperties.getText(OLD_PRE20_LICENSE_MESSAGE_KEY_TEXT)).thenReturn(LIC_MSG);
        when(applicationProperties.getText(OLD_PRE20_LICENSE_HASH_KEY_TEXT)).thenReturn(LIC_HASH);

        when(licenseStringFactory.create(LIC_MSG, LIC_HASH)).thenReturn(GOOD_LICENSE_STRING);

        assertEquals(GOOD_LICENSE_STRING, createStore().retrieve());
    }

    @Test
    public void testRetrieve_FromReallyOldPre20TextKeys()
    {
        when(applicationProperties.getText(POST20_LICENSE_STRING_KEY)).thenReturn(null);

        when(applicationProperties.getText(OLD_PRE20_LICENSE_MESSAGE_KEY_TEXT)).thenReturn(null);
        when(applicationProperties.getText(OLD_PRE20_LICENSE_HASH_KEY_TEXT)).thenReturn(null);

        when(applicationProperties.getString(REALLY_OLD_MESSAGE_KEY)).thenReturn(LIC_MSG);
        when(applicationProperties.getString(REALLY_OLD_MESSAGE_HASH)).thenReturn(LIC_HASH);

        when(licenseStringFactory.create(LIC_MSG, LIC_HASH)).thenReturn(GOOD_LICENSE_STRING);

        assertEquals(GOOD_LICENSE_STRING, createStore().retrieve());
    }

    @Test
    public void testRetrieve_NotSetAnywhere()
    {
        when(applicationProperties.getText(POST20_LICENSE_STRING_KEY)).thenReturn(null);

        when(applicationProperties.getText(OLD_PRE20_LICENSE_MESSAGE_KEY_TEXT)).thenReturn(null);
        when(applicationProperties.getText(OLD_PRE20_LICENSE_HASH_KEY_TEXT)).thenReturn(null);

        when(applicationProperties.getString(REALLY_OLD_MESSAGE_KEY)).thenReturn(null);
        when(applicationProperties.getString(REALLY_OLD_MESSAGE_HASH)).thenReturn(null);

        final String actualLicenseString = createStore().retrieve();
        assertNull(actualLicenseString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreNullString()
    {
        createStore().store(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreEmptyString()
    {
        createStore().store("");
    }

    @Test
    public void testStore_HappyPath() throws Exception
    {
        createStore().store(GOOD_LICENSE_STRING);
        verify(applicationProperties).setText(POST20_LICENSE_STRING_KEY, GOOD_LICENSE_STRING);
        verify(applicationProperties).setString(JIRA_EDITION, ENTERPRISE);

    }

    @Test
    public void testStore_WithWhitespaceInIt() throws Exception
    {
        createStore().store(LIC_WITH_WHITE_SPACE_IN_IT);
        verify(applicationProperties).setText(POST20_LICENSE_STRING_KEY, LIC_WITH_NO_WHITE_SPACE_IN_IT);
        verify(applicationProperties).setString(JIRA_EDITION, ENTERPRISE);
    }

    @Test
    public void testConfirmProceed() throws Exception
    {
        createStore().confirmProceedUnderEvaluationTerms(USER_NAME);

        verify(applicationProperties).setOption(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE, true);
        verify(applicationProperties).setString(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_USER, USER_NAME);
        verify(applicationProperties).setString(eq(APKeys.JIRA_CONFIRMED_INSTALL_WITH_OLD_LICENSE_TIMESTAMP), any(String.class));

    }

    @Test
    public void testRetriveServerId()
    {
        final String serverId = "A server ID";
        when(applicationProperties.getString(APKeys.JIRA_SID)).thenReturn(serverId);
        assertEquals(serverId, createStore().retrieveServerId());
    }

    @Test
    public void testStoreServerId()
    {
        final String serverId = "A server ID";
        createStore().storeServerId(serverId);
        verify(applicationProperties).setString(APKeys.JIRA_SID, serverId);
    }

    @Test
    public void shouldRemoveAllLicenseProperties()
    {
        createStore().remove();
        verify(applicationProperties).setText(POST20_LICENSE_STRING_KEY, null);
        verify(applicationProperties).setText(OLD_PRE20_LICENSE_HASH_KEY_TEXT, null);
        verify(applicationProperties).setText(OLD_PRE20_LICENSE_HASH_KEY_TEXT, null);
        verify(applicationProperties).setString(REALLY_OLD_MESSAGE_KEY, null);
        verify(applicationProperties).setString(REALLY_OLD_MESSAGE_HASH, null);
    }
}
