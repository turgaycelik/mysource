package com.atlassian.jira.configurator.console;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.configurator.config.ComplexConfigurationReason;
import com.atlassian.jira.configurator.config.FileExistanceWithCancelOptionValidator;
import com.atlassian.jira.configurator.config.FileSystem;
import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.SslSettings;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.configurator.config.Validator;
import com.atlassian.jira.configurator.config.WebServerProfile;
import com.atlassian.jira.configurator.ssl.CertificateDetails;
import com.atlassian.jira.configurator.ssl.CertificatePrettyPrinter;
import com.atlassian.jira.configurator.ssl.KeyStoreAccessor;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.security.UnrecoverableEntryException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class WebServerConfigurationConsole
{
    private static final String SSL_DETAILS_URL = "https://confluence.atlassian.com/display/JIRA/Running+JIRA+over+SSL+or+HTTPS";

    static final String MENU_CHOICE_MAIN_MENU = "Web Server";

    static final char MENU_ITEM_CHANGE_PROFILE = 'P';
    static final String MENU_CHOICE_SELECT_PROFILE = "Profile (leave blank to exit)";

    static final char MENU_ITEM_CONF_HTTP = 'H';
    static final String INPUT_HTTP_PORT = "HTTP Port";

    static final char MENU_ITEM_CONF_SSL = 'S';
    static final String MENU_CHOICE_SELECT_KEY_STORE = "Keystore";
    static final char MENU_ITEM_SYSTEM_KEY_STORE = 'S';
    static final char MENU_ITEM_USER_DEFINED_KEY_STORE = 'U';
    static final char MENU_ITEM_CURRENT_KEY_STORE = 'C';
    static final String INPUT_KEY_STORE_FILE_NAME = "Keystore Path (leave blank to exit)";
    static final String INPUT_KEY_STORE_PASSWORD = "Keystore Password";
    static final String INPUT_KEY_ALIAS = "Key Alias";
    static final String INPUT_HTTPS_PORT = "HTTPs Port";
    static final String YES_NO_USE_CERTIFICATE = "Do you want to use this certificate?";
    static final String YES_NO_CERT_NOT_FOUND_TRY_AGAIN = "The referenced certificate could not be found or accessed. Do you want to try again?";

    static final char MENU_ITEM_EXIT = 'X';

    private final ConsoleProvider console;
    private final ConsoleToolkit consoleToolkit;
    private final KeyStoreAccessor keyStoreAccessor;
    private final FileSystem fileSystem;

    private final Settings settings;

    public WebServerConfigurationConsole(@Nonnull final ConsoleProvider console, @Nonnull final KeyStoreAccessor keyStoreAccessor,
            @Nonnull final FileSystem fileSystem, @Nonnull final Settings settings) {
        this.console = console;
        this.consoleToolkit = new ConsoleToolkit(console);
        this.keyStoreAccessor = keyStoreAccessor;
        this.fileSystem = fileSystem;
        this.settings = settings;
    }

    public void showSettings() throws IOException
    {
        do
        {
            final SslSettings sslSettings = settings.getSslSettings();
            final WebServerProfile webServerProfile = settings.getWebServerProfile();
            final List<ComplexConfigurationReason> reasons = settings.getComplexConfigurationReasons();

            console.println();
            console.println("--- Web Server Configuration ---");
            if (!reasons.isEmpty())
            {
                console.println();
                console.println("Warning: It has been detected, that the current configuration is too complex or partly"
                        + " not supported to offer all options. The following items have been identified:");
                for (final ComplexConfigurationReason reason : reasons)
                {
                    console.println("* " + reason.getDescription());
                }
                console.println();
            }
            console.println("  Control Port  : " + StringUtils.defaultString(settings.getControlPort(), "N/A"));
            console.println("  Profile       : " + webServerProfile.getLabel());
            if (webServerProfile.isHttpEnabled())
            {
                console.println("  HTTP Port     : " + settings.getHttpPort());
            }
            if (webServerProfile.isHttpsEnabled())
            {
                if (sslSettings != null)
                {
                    console.println("  HTTPs Port    : " + sslSettings.getHttpsPort());
                    console.println("  Keystore Path : " + Strings.nullToEmpty(sslSettings.getKeystoreFile()));
                    console.println("  Key Alias     : " + Strings.nullToEmpty(sslSettings.getKeyAlias()));
                }
            }
            console.println();

            consoleToolkit.showMenuItem(MENU_ITEM_CHANGE_PROFILE, "Change the Profile (enable/disable HTTP/HTTPs)");
            if (webServerProfile.isHttpEnabled())
            {
                consoleToolkit.showMenuItem(MENU_ITEM_CONF_HTTP, "Configure HTTP Port");
            }
            if (webServerProfile.isHttpsEnabled())
            {
                consoleToolkit.showMenuItem(MENU_ITEM_CONF_SSL, "Configure SSL Encryption (requires an installed X509 certificate)");
            }
            consoleToolkit.showMenuItem(MENU_ITEM_EXIT, "Return to Main Menu");
            console.println();
        }
        while (processWebServerSettings());
    }

    private boolean processWebServerSettings() throws IOException
    {
        final WebServerProfile profile = settings.getWebServerProfile();
        while (true)
        {
            final char ch = consoleToolkit.readMenuChoice(MENU_CHOICE_MAIN_MENU);
            switch (ch)
            {
                case '\r':
                case '\n':
                    continue;  // reprompt
                case '?':
                    return true;  // redisplay menu
                case MENU_ITEM_EXIT:
                    return false;  // done
                case MENU_ITEM_CHANGE_PROFILE:
                    showChangeProfile();
                    return true;
                case MENU_ITEM_CONF_HTTP:
                    if (profile.isHttpEnabled())
                    {
                        showUpdateHttpConfiguration();
                    }
                    else
                    {
                        printUnknownCommand(ch);
                    }
                    return true;
                case MENU_ITEM_CONF_SSL:
                    if (profile.isHttpsEnabled())
                    {
                        updateHttpsConfiguration();
                    }
                    else
                    {
                        printUnknownCommand(ch);
                    }
                    return true;
                default:
                    printUnknownCommand(ch);
                    return true;
            }
        }
    }

    private void showChangeProfile() throws IOException
    {
        final WebServerProfile currentProfile = settings.getWebServerProfile();
        console.println();
        console.println("To change the web server profile, please select one of the following options. The current"
                + " profile is: " + currentProfile.getLabel() + ".");

        final WebServerProfile newProfile = askForWebServerProfile();
        if (newProfile == null)
        {
            return;
        }

        String httpPort = null;
        if (newProfile.isHttpEnabled())
        {
            if (!currentProfile.isHttpEnabled())
            {
                httpPort = showEnterHttpConfiguration();
            }
            else
            {
                console.println("Using currently configured HTTP port: " + settings.getHttpPort());
                httpPort = settings.getHttpPort();
            }
        }

        SslSettings sslSettings = null;
        if (newProfile.isHttpsEnabled())
        {
            if (!currentProfile.isHttpsEnabled())
            {
                Result<SslSettings> sslSettingsResult = showUpdateHttpsConfiguration();
                if (!sslSettingsResult.hasResult())
                {
                    console.println("The HTTPs configuration is not complete, aborting the profile change.");
                    return;
                }
                sslSettings = sslSettingsResult.getValue();
            }
            else
            {
                console.println("Using currently configured SSL settings");
                sslSettings = settings.getSslSettings();
            }
        }
        settings.updateWebServerConfiguration(httpPort, sslSettings);
        console.println();
        console.println("Updated the profile to '" + newProfile.getLabel() + "'. Remember to save the changes on exit.");
    }

    private WebServerProfile askForWebServerProfile() throws IOException
    {
        final List<WebServerProfile> filteredProfiles = WebServerProfile.getUsableProfiles(settings);
        final SortedMap<Character, WebServerProfile> profileSelectionMap = createProfileSelectionMap(filteredProfiles);

        while (true)
        {
            console.println();
            for (Map.Entry<Character, WebServerProfile> usableProfileEntry : profileSelectionMap.entrySet())
            {
                final Character menuKey = usableProfileEntry.getKey();
                final WebServerProfile profile = usableProfileEntry.getValue();
                consoleToolkit.showMenuItem(menuKey, profile.getLabel());
            }
            
            final char profile = consoleToolkit.readMenuChoice(MENU_CHOICE_SELECT_PROFILE);
            if (profile == '\r' || profile == '\n')
            {
                return null;
            }
            else
            {
                if (profileSelectionMap.containsKey(profile))
                {
                    return profileSelectionMap.get(profile);
                }
            }
            printUnknownCommand(profile);
        }
    }

    private SortedMap<Character, WebServerProfile> createProfileSelectionMap(List<WebServerProfile> filteredProfiles)
    {
        final SortedMap<Character, WebServerProfile> profileSelectionMap = new TreeMap<Character, WebServerProfile>();
        for (int i = 0; i < filteredProfiles.size(); i++)
        {
            final Character key = Integer.toString(i + 1).charAt(0);
            final WebServerProfile webServerProfile =  filteredProfiles.get(i);
            profileSelectionMap.put(key, webServerProfile);
        }
        return profileSelectionMap;
    }

    private void showUpdateHttpConfiguration() throws IOException
    {
        console.println();
        console.println("You can update the HTTP port to a new value. The current port is: " + settings.getHttpPort());
        final String s = processUpdateHttpConfiguration();
        if (s != null)
        {
            settings.setHttpPort(s);
            console.println();
            console.println("Updated the HTTP port to " + s + ". Remember to save the changes on exit.");
        }
    }

    private String showEnterHttpConfiguration() throws IOException
    {
        console.println();
        console.println("There is currently no HTTP port set. Please enter the port you want to use.");
        return processUpdateHttpConfiguration();
    }

    private String processUpdateHttpConfiguration() throws IOException
    {
        final int controlPort = Integer.parseInt(settings.getControlPort());
        final SslSettings sslSettings = settings.getSslSettings();
        final Integer httpsPort = (sslSettings != null) ? Integer.parseInt(sslSettings.getHttpsPort()) : null;
        while (true)
        {
            final int newHttpPort = consoleToolkit.askFor(INPUT_HTTP_PORT, Validator.PORT);
            if (newHttpPort == controlPort)
            {
                console.printErrorMessage("The entered port is already used by the control port. Please pick a different port.");
            }
            else if (httpsPort != null && newHttpPort == httpsPort)
            {
                console.printErrorMessage("The entered port is already used by the HTTPs port. Please pick a different port.");
            }
            else
            {
                return Integer.toString(newHttpPort);
            }
        }
    }

    private void updateHttpsConfiguration() throws IOException
    {
        Result<SslSettings> httpsConfigurationResult = showUpdateHttpsConfiguration();
        if (httpsConfigurationResult.hasResult())
        {
            settings.setSslSettings(httpsConfigurationResult.getValue());
            console.println();
            console.println("Updated the SSL encryption settings. Remember to save the changes on exit.");
        }
    }

    private Result<SslSettings> showUpdateHttpsConfiguration() throws IOException
    {
        console.println();
        console.println("The next steps gather all required information to set up the HTTPs port (HTTP over SSL"
                + " encryption). First of all, you need provide a so called key store containing the private key and the"
                + " signed certificate. This can be either self-signed or obtained from a certified authority (CA). For"
                + " more information, please see the link below. In order to verify the entered information, this tool"
                + " will access the key store and print the certificate found.");
        console.println();
        console.println(SSL_DETAILS_URL);
        final CertificateDetails certificateDetails = askForKeyStoreDetailsOrCancel();
        if (certificateDetails == null)
        {
            return Result.noResult();
        }

        final int httpsPort = consoleToolkit.askFor(INPUT_HTTPS_PORT, Validator.PORT);
        final SslSettings newSslSettings = new SslSettings(Integer.toString(httpsPort), certificateDetails.getKeyStoreLocation(), certificateDetails.getKeyStorePassword(), "JKS", certificateDetails.getKeyAlias());
        return new Result<SslSettings>(newSslSettings);
    }

    private CertificateDetails askForKeyStoreDetailsOrCancel() throws IOException
    {
        while (true)
        {
            final String keystoreLocation = askForKeyStoreLocationOrCancel();
            if (keystoreLocation == null)
            {
                return null;
            }
    
            final String keyStorePassword = consoleToolkit.askForPassword(INPUT_KEY_STORE_PASSWORD, Validator.NON_EMTPY_STRING);
            final String alias = consoleToolkit.askFor(INPUT_KEY_ALIAS, Validator.NON_EMTPY_STRING);
            final CertificateDetails certificateDetails = new CertificateDetails(keystoreLocation, keyStorePassword, alias);
            final X509Certificate x509Certificate = loadCertificate(certificateDetails);
            if (x509Certificate != null)
            {
                console.println();
                console.println("The following certificate was found:");
                console.println();
                console.println(CertificatePrettyPrinter.prettyPrint(x509Certificate));
                final boolean correctCertificate = console.readYesNo(YES_NO_USE_CERTIFICATE, Boolean.TRUE);
                if (correctCertificate)
                {
                    return certificateDetails;
                }
            }
            else
            {
                final boolean tryAgain = console.readYesNo(YES_NO_CERT_NOT_FOUND_TRY_AGAIN, Boolean.TRUE);
                if (!tryAgain)
                {
                    return null;
                }
            }
        }
    }

    private String askForKeyStoreLocationOrCancel() throws IOException
    {
        final File caCertsPath = getCaCertsPath();
        final String canonicalCaCertsPath = caCertsPath.getCanonicalPath();

        final SslSettings sslSettings = settings.getSslSettings();
        final boolean hasExistingKeyStoreFileName = sslSettings != null && !Strings.nullToEmpty(sslSettings.getKeystoreFile()).isEmpty();
        while (true)
        {
            console.println();
            console.println("Please select the keystore from the options below. It must contain the certificate and the"
                    + " private key to be used.");
            consoleToolkit.showMenuItem(MENU_ITEM_SYSTEM_KEY_STORE, String.format("The system-wide Java keystore (%s)", canonicalCaCertsPath));
            consoleToolkit.showMenuItem(MENU_ITEM_USER_DEFINED_KEY_STORE, "User-defined location");
            if (hasExistingKeyStoreFileName)
            {
                consoleToolkit.showMenuItem(MENU_ITEM_CURRENT_KEY_STORE, String.format("The currently configured (%s)", sslSettings.getKeystoreFile()));
            }
            final char keystore = consoleToolkit.readMenuChoice(MENU_CHOICE_SELECT_KEY_STORE);
            switch (keystore)
            {
                case '\r':
                case '\n':
                    continue;  // reprompt
                case MENU_ITEM_SYSTEM_KEY_STORE:
                    return canonicalCaCertsPath;
                case MENU_ITEM_USER_DEFINED_KEY_STORE:
                    return askForUserDefinedKeyStorePathOrCancel();
                case MENU_ITEM_CURRENT_KEY_STORE:
                    if (hasExistingKeyStoreFileName)
                    {
                        return sslSettings.getKeystoreFile();
                    }
                default:
                    printUnknownCommand(keystore);
            }
        }
    }

    private void printUnknownCommand(char ch)
    {
        console.println("Unknown command '" + ch + "'");
    }

    private File getCaCertsPath()
    {
        final String javaHome = JiraSystemProperties.getInstance().getProperty("java.home");
        final String caCertsPathAsText = Joiner.on(File.separatorChar).join(Lists.newArrayList(javaHome, "lib", "security", "cacerts"));
        return new File(caCertsPathAsText);
    }

    @Nullable
    public X509Certificate loadCertificate(@Nonnull final CertificateDetails certificateDetails)
    {
        try
        {
            return keyStoreAccessor.loadCertificate(certificateDetails);
        }
        catch (UnrecoverableEntryException e)
        {
            console.printErrorMessage("The entered password is valid for the key store, but not for the private key."
                    + " You need to synchronize both passwords in order to proceed.");
            return null;
        }
        catch (Exception e)
        {
            console.printErrorMessage(e);
            return null;
        }
    }

    private String askForUserDefinedKeyStorePathOrCancel() throws IOException
    {
        final Validator<String> validator = new FileExistanceWithCancelOptionValidator(fileSystem);
        while (true)
        {
            try
            {
                return validator.apply("Keystore Path", console.readLine(INPUT_KEY_STORE_FILE_NAME));
            }
            catch (ValidationException e)
            {
                console.printErrorMessage(e);
            }
        }
    }

    private static class Result<T>
    {
        private final boolean hasResult;
        private final T value;

        public Result(@Nullable final T value)
        {
            this.hasResult = true;
            this.value = value;
        }

        private Result()
        {
            this.hasResult = false;
            this.value = null;
        }

        public boolean hasResult()
        {
            return hasResult;
        }

        @Nullable
        public T getValue()
        {
            return value;
        }

        public static <T> Result<T> noResult()
        {
            return new Result<T>();
        }
    }
}
