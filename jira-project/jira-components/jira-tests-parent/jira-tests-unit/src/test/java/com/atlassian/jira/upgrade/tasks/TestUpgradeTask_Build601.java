package com.atlassian.jira.upgrade.tasks;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Collection;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.help.HelpUrls;
import com.atlassian.jira.help.MockHelpUrls;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.ofbiz.core.entity.GenericEntityException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @since v4.3
 */
public class TestUpgradeTask_Build601
{
    @Rule
    public RuleChain container = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private HelpUrls urls = new MockHelpUrls();

    private final static String ONE_UNKNOWN_PROVIDER =
            "<opensymphony-user>\n"
            + "    <provider class=\"my.private.test.CredentialsProvidor\">\n"
            + "        <property name=\"exclusive-access\">true</property>\n"
            + "    </provider>\n"
            + "</opensymphony-user>";

    private final static String MIXED_UNKNOWN_PROVIDER =
            "<opensymphony-user>\n"
            + "    <provider class=\"my.private.test.CredentialsProvidor\">\n"
            + "        <property name=\"exclusive-access\">true</property>\n"
            + "    </provider>\n"
            + "    <provider class=\"com.atlassian.jira.user.osuser.JiraOFBizProfileProvider\">\n"
            + "        <property name=\"exclusive-access\">true</property>\n"
            + "    </provider>\n"
            + "    <provider class=\"com.atlassian.jira.user.osuser.JiraOFBizAccessProvider\">\n"
            + "        <property name=\"exclusive-access\">true</property>\n"
            + "    </provider>\n"
            + "</opensymphony-user>";

    private final static String MIXED_KNOWN_PROVIDER =
            "<opensymphony-user>\n"
                    + "    <provider class=\"com.atlassian.crowd.integration.osuser.CrowdAccessProvider\">\n"
                    + "        <property name=\"exclusive-access\">true</property>\n"
                    + "    </provider>\n"
                    + "    <provider class=\"com.atlassian.jira.user.osuser.JiraOFBizProfileProvider\">\n"
                    + "        <property name=\"exclusive-access\">true</property>\n"
                    + "    </provider>\n"
                    + "    <provider class=\"com.atlassian.jira.user.osuser.JiraOFBizAccessProvider\">\n"
                    + "        <property name=\"exclusive-access\">true</property>\n"
                    + "    </provider>\n"
                    + "</opensymphony-user>";

    private final static String TOO_FEW_PROVIDERS =
            "<opensymphony-user>\n"
                    + "    <provider class=\"com.atlassian.jira.user.osuser.JiraOFBizProfileProvider\">\n"
                    + "        <property name=\"exclusive-access\">true</property>\n"
                    + "    </provider>\n"
                    + "    <provider class=\"com.atlassian.jira.user.osuser.JiraOFBizAccessProvider\">\n"
                    + "        <property name=\"exclusive-access\">true</property>\n"
                    + "    </provider>\n"
                    + "</opensymphony-user>";

    private final static String TOO_MANY_PROVIDERS =
            "<opensymphony-user>\n"
                    + "    <provider class=\"com.atlassian.core.ofbiz.osuser.CoreOFBizCredentialsProvider\">\n"
                    + "        <property name=\"exclusive-access\">true</property>\n"
                    + "    </provider>"
                    + "    <provider class=\"com.atlassian.core.ofbiz.osuser.CoreOFBizCredentialsProvider\">\n"
                    + "        <property name=\"exclusive-access\">true</property>\n"
                    + "    </provider>"
                    + "    <provider class=\"com.atlassian.jira.user.osuser.JiraOFBizProfileProvider\">\n"
                    + "        <property name=\"exclusive-access\">true</property>\n"
                    + "    </provider>\n"
                    + "    <provider class=\"com.atlassian.jira.user.osuser.JiraOFBizAccessProvider\">\n"
                    + "        <property name=\"exclusive-access\">true</property>\n"
                    + "    </provider>\n"
                    + "</opensymphony-user>";

    private ApplicationProperties applicationProperties = new MockApplicationProperties();

    @Test
    public void testUnknownProviders() throws Exception
    {
        UpgradeTask_Build601 task = new MyUpgradeTask_Build601(ONE_UNKNOWN_PROVIDER, false, applicationProperties);

        task.doUpgrade(false);
        Collection<String> errors = task.getErrors();

        assertEquals(1, errors.size());
        assertTrue(errors.iterator().next().contains("JIRA is unable to migrate the User Directory configuration because the osuser.xml file contains unrecognised providers."));

    }

    @Test
    public void testMixedUnknownProviders() throws Exception
    {
        UpgradeTask_Build601 task = new MyUpgradeTask_Build601(MIXED_UNKNOWN_PROVIDER, false, applicationProperties);

        task.doUpgrade(false);
        Collection<String> errors = task.getErrors();

        assertEquals(1, errors.size());
        assertTrue(errors.iterator().next().contains("JIRA is unable to migrate the User Directory configuration because the osuser.xml file contains unrecognised providers."));

    }

    @Test
    public void testMixedKnownProviders() throws Exception
    {
        UpgradeTask_Build601 task = new MyUpgradeTask_Build601(MIXED_KNOWN_PROVIDER, false, applicationProperties);

        task.doUpgrade(false);
        Collection<String> errors = task.getErrors();

        assertEquals(1, errors.size());
        assertTrue(errors.iterator().next().contains("JIRA is unable to migrate the User Directory configuration because the osuser.xml file does not contain a recognised configuration."));

    }

    @Test
    public void testTooFewProviders() throws Exception
    {
        UpgradeTask_Build601 task = new MyUpgradeTask_Build601(TOO_FEW_PROVIDERS, false, applicationProperties);

        task.doUpgrade(false);
        Collection<String> errors = task.getErrors();

        assertEquals(1, errors.size());
        assertTrue(errors.iterator().next().contains("JIRA is unable to migrate the User Directory configuration because the osuser.xml file does not contain a recognised configuration."));

    }

    @Test
    public void testTooManyProviders() throws Exception
    {
        UpgradeTask_Build601 task = new MyUpgradeTask_Build601(TOO_MANY_PROVIDERS, false, applicationProperties);

        task.doUpgrade(false);
        Collection<String> errors = task.getErrors();

        assertEquals(1, errors.size());
        assertTrue(errors.iterator().next().contains("JIRA is unable to migrate the User Directory configuration because the osuser.xml file does not contain a recognised configuration."));
    }

    @Test
    public void testExternalPasswordManagementNoOsUser() throws Exception
    {
        applicationProperties.setOption("jira.option.user.externalpasswordmanagement", true);
        UpgradeTask_Build601 task = new MyUpgradeTask_Build601(null, false, applicationProperties);

        task.doUpgrade(false);
        Collection<String> errors = task.getErrors();

        assertEquals(1, errors.size());
        assertTrue(errors.iterator().next().contains("JIRA is unable to migrate the User Directory configuration because external password management is enabled but the osuser.xml file is not available."));
    }

    @Test
    public void testExternalUserManagementNoOsUser() throws Exception
    {
        applicationProperties.setOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT, true);
        UpgradeTask_Build601 task = new MyUpgradeTask_Build601(null, false, applicationProperties);

        task.doUpgrade(false);
        Collection<String> errors = task.getErrors();

        assertEquals(1, errors.size());
        assertTrue(errors.iterator().next().contains("JIRA is unable to migrate the User Directory configuration because external user management is enabled but the osuser.xml file is not available."));
    }

    @Test
    public void shouldRemoveSoapPathNotEndingWithASlashFromTheLegacyCrowdPropertiesFile()
    {
        final String expectedServiceUrl = "http://test-crowd.atlassian.com";

        UpgradeTask_Build601.CrowdServiceUrlBuilder serviceUrlBuilder =
                new UpgradeTask_Build601.CrowdServiceUrlBuilder();

        final String serviceUrl = serviceUrlBuilder.setPropertiesUrlTo("http://test-crowd.atlassian.com/services").build();

        assertEquals
                (
                        "The service url should be built without '/services' at the end of the url path.",
                        expectedServiceUrl, serviceUrl
                );
    }

    @Test
    public void shouldRemoveSoapPathEndingWithASlashFromTheLegacyCrowdPropertiesFile()
    {
        final String expectedServiceUrl = "http://test-crowd.atlassian.com";

        UpgradeTask_Build601.CrowdServiceUrlBuilder serviceUrlBuilder =
                new UpgradeTask_Build601.CrowdServiceUrlBuilder();

        final String serviceUrl = serviceUrlBuilder.setPropertiesUrlTo("http://test-crowd.atlassian.com/services/").build();

        assertEquals
                (
                        "The service url should be built without '/services/' at the end of the url path.",
                        expectedServiceUrl, serviceUrl
                );
    }

    @Test
    public void shouldNotRemoveServicesFromTheHostOfTheUrlFromTheLegacyCrowdPropertiesFile()
    {
        final String expectedServiceUrl = "http://services-crowd.atlassian.com";

        UpgradeTask_Build601.CrowdServiceUrlBuilder serviceUrlBuilder =
                new UpgradeTask_Build601.CrowdServiceUrlBuilder();

        final String serviceUrl = serviceUrlBuilder.setPropertiesUrlTo("http://services-crowd.atlassian.com").build();

        assertEquals
                (
                        "If 'services' is part of the url it should be preserved unless it's at the end of the url path.",
                        expectedServiceUrl, serviceUrl
                );
    }

    @Test
    public void shouldOnlyRemoveServicesFromTheEndOfTheUrlInTheLegacyCrowdPropertiesFile()
    {
        final String expectedServiceUrl = "http://services-crowd.atlassian.com";

        UpgradeTask_Build601.CrowdServiceUrlBuilder serviceUrlBuilder =
                new UpgradeTask_Build601.CrowdServiceUrlBuilder();

        final String serviceUrl = serviceUrlBuilder.setPropertiesUrlTo("http://services-crowd.atlassian.com/services").build();

        assertEquals
                (
                        "The service url should be built without '/services' at the end of the url path and no other"
                                + " instances of the word 'services' should be removed from the original url.",
                        expectedServiceUrl, serviceUrl
                );
    }


    private static class MyUpgradeTask_Build601 extends UpgradeTask_Build601
    {
        public MyUpgradeTask_Build601(final String osuserxml, final boolean externalUsers, final ApplicationProperties applicationProperties)
        {
            //noinspection NullableProblems
            super(null, null, null, applicationProperties);
            this.externalUsers = externalUsers;
            this.osuserxml = osuserxml;
        }

        private final String osuserxml;
        private final boolean externalUsers;
        @Override
        protected InputStream getOSUserXmlStream()
        {
            if (osuserxml == null)
            {
                return null;
            }

            try
            {
                return new ByteArrayInputStream(osuserxml.getBytes("UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected boolean areExternalUsersPresent() throws SQLException
        {
            return externalUsers;
        }

        @Override
        protected void addJiraApplication() throws GenericEntityException
        {
            // Do nothing. Not testing this now.
        }

        @Override
        protected I18nHelper getI18nBean()
        {
            return Stubs.I18nHelper.get();
        }

        private static class Stubs
        {
            private static class I18nHelper
            {
                private static com.atlassian.jira.util.I18nHelper get()
                {
                    final MockI18nHelper mockI18nHelper = new MockI18nHelper();
                    mockI18nHelper.stubWith("admin.errors.upgrade.601.error.bad.providers", "JIRA is unable to migrate the User Directory configuration because the osuser.xml file contains unrecognised providers.");
                    mockI18nHelper.stubWith("admin.errors.upgrade.601.error.bad.osuser.config", "JIRA is unable to migrate the User Directory configuration because the osuser.xml file does not contain a recognised configuration.");
                    mockI18nHelper.stubWith("admin.errors.upgrade.601.error.missing.osuser.xml", "JIRA is unable to migrate the User Directory configuration because external users exist but the osuser.xml file is not available.");
                    mockI18nHelper.stubWith("admin.errors.upgrade.601.error.missing.extrenal.user.management", "JIRA is unable to migrate the User Directory configuration because external user management is enabled but the osuser.xml file is not available.");
                    mockI18nHelper.stubWith("admin.errors.upgrade.601.error.missing.extrenal.password.management", "JIRA is unable to migrate the User Directory configuration because external password management is enabled but the osuser.xml file is not available.");
                    return mockI18nHelper;
                }
            }
        }
    }
}