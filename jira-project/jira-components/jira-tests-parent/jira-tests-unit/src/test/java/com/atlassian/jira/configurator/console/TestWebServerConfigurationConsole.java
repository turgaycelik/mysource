package com.atlassian.jira.configurator.console;

import java.io.IOException;
import java.security.UnrecoverableKeyException;

import javax.annotation.Nonnull;

import com.atlassian.jira.configurator.config.FileSystem;
import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.SslSettings;
import com.atlassian.jira.configurator.config.WebServerProfile;
import com.atlassian.jira.configurator.ssl.CertificateDetails;
import com.atlassian.jira.configurator.ssl.KeyStoreAccessor;
import com.atlassian.jira.configurator.ssl.TestCertificatePrettyPrinter;

import org.junit.Test;

import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.INPUT_HTTPS_PORT;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.INPUT_HTTP_PORT;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.INPUT_KEY_ALIAS;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.INPUT_KEY_STORE_FILE_NAME;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.INPUT_KEY_STORE_PASSWORD;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.MENU_CHOICE_MAIN_MENU;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.MENU_CHOICE_SELECT_KEY_STORE;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.MENU_CHOICE_SELECT_PROFILE;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.MENU_ITEM_CHANGE_PROFILE;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.MENU_ITEM_CONF_HTTP;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.MENU_ITEM_CONF_SSL;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.MENU_ITEM_EXIT;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.MENU_ITEM_USER_DEFINED_KEY_STORE;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.YES_NO_CERT_NOT_FOUND_TRY_AGAIN;
import static com.atlassian.jira.configurator.console.WebServerConfigurationConsole.YES_NO_USE_CERTIFICATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestWebServerConfigurationConsole
{
    private static final int TIMEOUT_IN_MILLIS = 5000;

    private static final String DEFAULT_HTTP_PORT = "8080";
    private static final String DEFAULT_HTTPS_PORT = "8443";
    private static final String KEY_STORE_FILE_NAME = "/home/some/where/file";
    private static final String KEY_STORE_PASSWORD = "secret";
    private static final String KEY_ALIAS = "alias";

    private static final SslSettings DEFAULT_SSL_SETTINGS = createDefaultSettings();

    private final ConsoleProvider console = mock(ConsoleProvider.class);
    private final KeyStoreAccessor keyStoreAccessor = mock(KeyStoreAccessor.class);
    private final FileSystem fileSystem = mock(FileSystem.class);

    private final Settings settings = new Settings();

    private final WebServerConfigurationConsole dialog = new WebServerConfigurationConsole(console, keyStoreAccessor, fileSystem, settings);

    @Test(timeout = TIMEOUT_IN_MILLIS)
    public void testUpdateHttpPortInHttpOnlyProfile() throws IOException
    {
        settings.setHttpPort(DEFAULT_HTTP_PORT);
        String newHttpPort = "8090";

        when(console.readFirstChar(MENU_CHOICE_MAIN_MENU)).thenReturn(MENU_ITEM_CONF_HTTP, MENU_ITEM_EXIT);
        when(console.readLine(INPUT_HTTP_PORT)).thenReturn(newHttpPort);

        dialog.showSettings();

        assertEquals(WebServerProfile.HttpOnly, settings.getWebServerProfile());

        assertEquals(settings.getHttpPort(), newHttpPort);

        verify(console, never()).printErrorMessage(any(Exception.class));
        verify(console, never()).printErrorMessage(any(String.class));
    }

    @Test(timeout = TIMEOUT_IN_MILLIS)
    public void testUpdateHttpPortInHttpAndHttpsProfile() throws IOException
    {
        settings.updateWebServerConfiguration(DEFAULT_HTTP_PORT, DEFAULT_SSL_SETTINGS);
        final String newHttpPort = "8090";

        when(console.readFirstChar(MENU_CHOICE_MAIN_MENU)).thenReturn(MENU_ITEM_CONF_HTTP, MENU_ITEM_EXIT);
        when(console.readLine(INPUT_HTTP_PORT)).thenReturn(newHttpPort);

        dialog.showSettings();

        assertEquals(WebServerProfile.HttpRedirectedToHttps, settings.getWebServerProfile());

        assertEquals(settings.getHttpPort(), newHttpPort);

        verify(console, never()).printErrorMessage(any(Exception.class));
        verify(console, never()).printErrorMessage(any(String.class));
    }

    @Test(timeout = TIMEOUT_IN_MILLIS)
    public void testSettingsUnchangedWhenNoCertificateFound() throws Exception
    {
        final SslSettings currentSslSettings = createDifferentSslSettings();
        settings.updateWebServerConfiguration(DEFAULT_HTTP_PORT, currentSslSettings);

        when(console.readFirstChar(MENU_CHOICE_MAIN_MENU)).thenReturn(MENU_ITEM_CONF_SSL, MENU_ITEM_EXIT);
        expectEnterCertificateDetails();
        when(keyStoreAccessor.loadCertificate(any(CertificateDetails.class))).thenReturn(null);
        when(console.readYesNo(eq(YES_NO_CERT_NOT_FOUND_TRY_AGAIN), anyBoolean())).thenReturn(Boolean.FALSE);

        dialog.showSettings();

        assertEquals(WebServerProfile.HttpRedirectedToHttps, settings.getWebServerProfile());
        assertSslSettings(currentSslSettings, settings);

        verify(console, never()).printErrorMessage(any(Exception.class));
        verify(console, never()).printErrorMessage(any(String.class));
    }

    @Test(timeout = TIMEOUT_IN_MILLIS)
    public void testWarningPresentWhenKeyStoreAndPrivateKeyPasswordsDifferent() throws Exception
    {
        final SslSettings currentSslSettings = createDifferentSslSettings();
        settings.updateWebServerConfiguration(DEFAULT_HTTP_PORT, currentSslSettings);

        when(console.readFirstChar(MENU_CHOICE_MAIN_MENU)).thenReturn(MENU_ITEM_CONF_SSL, MENU_ITEM_EXIT);
        expectEnterCertificateDetails();
        when(keyStoreAccessor.loadCertificate(any(CertificateDetails.class))).thenThrow(new UnrecoverableKeyException());
        when(console.readYesNo(eq(YES_NO_CERT_NOT_FOUND_TRY_AGAIN), anyBoolean())).thenReturn(Boolean.FALSE);

        dialog.showSettings();

        assertEquals(WebServerProfile.HttpRedirectedToHttps, settings.getWebServerProfile());
        assertSslSettings(currentSslSettings, settings);

        verify(console, never()).printErrorMessage(any(Exception.class));
        verify(console, times(1)).printErrorMessage(any(String.class));
    }

    @Test(timeout = TIMEOUT_IN_MILLIS)
    public void testUpdateSslConfiguration() throws Exception
    {
        settings.updateWebServerConfiguration(DEFAULT_HTTP_PORT, createDifferentSslSettings());

        when(console.readFirstChar(MENU_CHOICE_MAIN_MENU)).thenReturn(MENU_ITEM_CONF_SSL, MENU_ITEM_EXIT);
        expectEnterCertificateDetails();
        expectCertificateExists();
        expectUseCertificate();
        expectEnterHttpsPort();

        dialog.showSettings();

        assertEquals(WebServerProfile.HttpRedirectedToHttps, settings.getWebServerProfile());
        assertEquals(settings.getHttpPort(), DEFAULT_HTTP_PORT);
        assertSslSettings(DEFAULT_SSL_SETTINGS, settings);

        verify(console, never()).printErrorMessage(any(Exception.class));
        verify(console, never()).printErrorMessage(any(String.class));
    }

    @Test(timeout = TIMEOUT_IN_MILLIS)
    public void testChangeFromHttpOnlyProfileToHttpAndHttps() throws Exception
    {
        settings.setHttpPort(DEFAULT_HTTP_PORT);

        when(console.readFirstChar(MENU_CHOICE_MAIN_MENU)).thenReturn(MENU_ITEM_CHANGE_PROFILE, MENU_ITEM_EXIT);
        when(console.readFirstChar(MENU_CHOICE_SELECT_PROFILE)).thenReturn(resolveMenuEntry(WebServerProfile.HttpRedirectedToHttps));
        expectEnterCertificateDetails();
        expectCertificateExists();
        expectUseCertificate();
        expectEnterHttpsPort();

        dialog.showSettings();

        assertEquals(WebServerProfile.HttpRedirectedToHttps, settings.getWebServerProfile());
        assertEquals(settings.getHttpPort(), DEFAULT_HTTP_PORT);
        assertSslSettings(DEFAULT_SSL_SETTINGS, settings);
    }

    @Test(timeout = TIMEOUT_IN_MILLIS)
    public void testChangeFromHttpProfileToHttpsOnly() throws Exception
    {
        settings.updateWebServerConfiguration(DEFAULT_HTTP_PORT, null);

        when(console.readFirstChar(MENU_CHOICE_MAIN_MENU)).thenReturn(MENU_ITEM_CHANGE_PROFILE, MENU_ITEM_EXIT);
        when(console.readFirstChar(MENU_CHOICE_SELECT_PROFILE)).thenReturn(resolveMenuEntry(WebServerProfile.HttpsOnly));
        expectEnterCertificateDetails();
        expectCertificateExists();
        expectUseCertificate();
        expectEnterHttpsPort();

        dialog.showSettings();

        assertEquals(WebServerProfile.HttpsOnly, settings.getWebServerProfile());
        assertSslSettings(DEFAULT_SSL_SETTINGS, settings);
    }

    @Test(timeout = TIMEOUT_IN_MILLIS)
    public void testChangeFromHttpAndHttpsProfileToHttpOnly() throws Exception
    {
        settings.updateWebServerConfiguration(DEFAULT_HTTP_PORT, DEFAULT_SSL_SETTINGS);

        when(console.readFirstChar(MENU_CHOICE_MAIN_MENU)).thenReturn(MENU_ITEM_CHANGE_PROFILE, MENU_ITEM_EXIT);
        when(console.readFirstChar(MENU_CHOICE_SELECT_PROFILE)).thenReturn(resolveMenuEntry(WebServerProfile.HttpOnly));

        dialog.showSettings();

        assertEquals(WebServerProfile.HttpOnly, settings.getWebServerProfile());
        assertNull(settings.getSslSettings());
    }

    @Test(timeout = TIMEOUT_IN_MILLIS)
    public void testChangeFromHttpsOnlyProfileToHttpOnly() throws Exception
    {
        settings.updateWebServerConfiguration(null, DEFAULT_SSL_SETTINGS);

        when(console.readFirstChar(MENU_CHOICE_MAIN_MENU)).thenReturn(MENU_ITEM_CHANGE_PROFILE, MENU_ITEM_EXIT);
        when(console.readFirstChar(MENU_CHOICE_SELECT_PROFILE)).thenReturn(resolveMenuEntry(WebServerProfile.HttpOnly));
        when(console.readLine(INPUT_HTTP_PORT)).thenReturn(DEFAULT_HTTP_PORT);

        dialog.showSettings();

        assertEquals(WebServerProfile.HttpOnly, settings.getWebServerProfile());
        assertEquals(DEFAULT_HTTP_PORT, settings.getHttpPort());
        assertNull(settings.getSslSettings());
    }

    private void expectEnterCertificateDetails() throws Exception
    {
        when(console.readFirstChar(MENU_CHOICE_SELECT_KEY_STORE)).thenReturn(MENU_ITEM_USER_DEFINED_KEY_STORE);
        when(console.readLine(INPUT_KEY_STORE_FILE_NAME)).thenReturn(KEY_STORE_FILE_NAME);
        when(fileSystem.isFileExisting(KEY_STORE_FILE_NAME)).thenReturn(Boolean.TRUE);
        when(fileSystem.getAbsolutePath(KEY_STORE_FILE_NAME)).thenReturn(KEY_STORE_FILE_NAME);
        when(console.readPassword(INPUT_KEY_STORE_PASSWORD)).thenReturn(KEY_STORE_PASSWORD);
        when(console.readLine(INPUT_KEY_ALIAS)).thenReturn(KEY_ALIAS);
    }

    private void expectCertificateExists() throws Exception
    {
        when(keyStoreAccessor.loadCertificate(any(CertificateDetails.class))).thenReturn(TestCertificatePrettyPrinter.createCertificate());
    }

    private void expectUseCertificate() throws IOException
    {
        when(console.readYesNo(eq(YES_NO_USE_CERTIFICATE), anyBoolean())).thenReturn(Boolean.TRUE);
    }

    private void expectEnterHttpsPort() throws IOException
    {
        when(console.readLine(INPUT_HTTPS_PORT)).thenReturn(DEFAULT_HTTPS_PORT);
    }

    private void assertSslSettings(@Nonnull final SslSettings expectedSslSettings, @Nonnull final Settings settings)
    {
        assertNotNull(settings);

        final SslSettings actualSslSettings = settings.getSslSettings();
        assertNotNull(actualSslSettings);
        assertEquals(expectedSslSettings.getHttpsPort(), actualSslSettings.getHttpsPort());
        assertEquals(expectedSslSettings.getKeystoreFile(), actualSslSettings.getKeystoreFile());
        assertEquals(expectedSslSettings.getKeystorePass(), actualSslSettings.getKeystorePass());
        assertEquals(expectedSslSettings.getKeyAlias(), actualSslSettings.getKeyAlias());
    }

    private static SslSettings createDefaultSettings()
    {
        return new SslSettings(DEFAULT_HTTPS_PORT, KEY_STORE_FILE_NAME, KEY_STORE_PASSWORD, "JKS", KEY_ALIAS);
    }

    private SslSettings createDifferentSslSettings()
    {
        return new SslSettings("1111", "/some/different/file", "not-secret", "JKS", "another-alias");
    }

    private char resolveMenuEntry(WebServerProfile profile)
    {
        return Integer.toString(profile.ordinal() + 1).charAt(0);
    }
}
