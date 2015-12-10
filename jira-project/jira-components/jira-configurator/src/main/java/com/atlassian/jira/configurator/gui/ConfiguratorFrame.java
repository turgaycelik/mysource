package com.atlassian.jira.configurator.gui;

import com.atlassian.jira.config.database.DatabaseDriverRegisterer;
import com.atlassian.jira.configurator.Configurator;
import com.atlassian.jira.configurator.config.ComplexConfigurationReason;
import com.atlassian.jira.configurator.config.ConnectionPoolField;
import com.atlassian.jira.config.database.DatabaseType;
import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.SettingsLoader;
import com.atlassian.jira.configurator.config.SslSettings;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.configurator.config.Validator;
import com.atlassian.jira.configurator.config.WebServerProfile;
import com.atlassian.jira.configurator.db.DatabaseConfigPanel;
import com.atlassian.jira.configurator.db.HsqlConfigPanel;
import com.atlassian.jira.configurator.db.MySqlConfigPanel;
import com.atlassian.jira.configurator.db.OracleConfigPanel;
import com.atlassian.jira.configurator.db.PostgresConfigPanel;
import com.atlassian.jira.configurator.db.SqlServerConfigPanel;
import com.atlassian.jira.configurator.ssl.CertificateDetails;
import com.atlassian.jira.configurator.ssl.CertificatePrettyPrinter;
import com.atlassian.jira.configurator.ssl.DefaultKeyStoreProvider;
import com.atlassian.jira.configurator.ssl.KeyStoreAccessor;
import com.atlassian.jira.configurator.ssl.KeyStoreAccessorImpl;
import com.atlassian.jira.exception.ParseException;
import com.google.common.collect.Iterables;
import org.ofbiz.core.entity.config.ConnectionPoolInfo;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.security.UnrecoverableEntryException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils.join;
import static java.lang.String.format;

/**
 * The main frame for the GUI.
 */
public class ConfiguratorFrame extends JFrame
{
    private final JTextField tfJiraHome = new JTextField(32);
    private final JTextField tfControlPort = new JTextField(10);
    private final JLabel tooComplexConfigurationWarning = new JLabel();
    private final JComboBox cbWebServerProfile = new JComboBox();

    private final JTextField tfHttpPort = new JTextField(10);

    private final JTextField tfHttpsPort = new JTextField(10);
    private final JTextField tfKeyStorePath = new JTextField(32);
    private final JButton browseKeyStoreButton = new JButton("Browse");
    private final JTextField tfKeyStorePassword = new JTextField(32);
    private final JTextField tfKeyAlias = new JTextField(10);
    private final JButton checkCertificate = new JButton("Check Certificate in Key Store");

    private final Map<ConnectionPoolField,LabelledComponent> advancedSettings = new HashMap<ConnectionPoolField,LabelledComponent>();
    private JComboBox ddDatabaseType;
    private JPanel cardPanel;

    private CardLayout jdbcTypeCardLayout;
    private Map<DatabaseType, DatabaseConfigPanel> configPanelMap = null;
    final ConnectionTestDialog connectionTestDialog;
    private String previousJiraHome;
    private KeyStoreAccessor keyStoreAccessor;

    public ConfiguratorFrame()
    {
        init();

        // We need to construct the ConnectionTestDialog after the ConfiguratorFrame has finished initialising because
        // the Dialog width is set based on the Frame width.
        connectionTestDialog = new ConnectionTestDialog(ConfiguratorFrame.this);
        keyStoreAccessor = new KeyStoreAccessorImpl(new DefaultKeyStoreProvider());
    }

    private void init()
    {
        setIconImage(new ImageIcon("../atlassian-jira/images/64jira.png").getImage());
        setLayout(new BorderLayout());
        add(newTabbedPanel(), BorderLayout.CENTER);
        add(newButtonPanel(), BorderLayout.SOUTH);
        // Override the Window Close event so we can show a Confirm Dialog.
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(final WindowEvent e)
            {
                closeApplication();
            }
        });
        // Let the Window resize itself to the right size for this platform.
        this.pack();
    }

    /**
     * Closes the application after checking for unsaved changes.
     * If there are unsaved changes, then a confirm dialog is shown.
     */
    private void closeApplication()
    {
        // Check if there are unsaved changes
        try
        {
            final Settings newSettings = gatherNewSettings();
            // The settings gathered from the UI have nulls in them for when we want "default" values but the saved settings
            // will have set these to the actual default values in order to save in the XML config.
            // We therefore need to apply the defaults here in order to compare correctly.
            newSettings.applyDefaultAdvancedSettings();
            if (Configurator.settingsEqual(newSettings))
            {
                // No changes - just exit without a prompt
                System.exit(0);
            }
        }
        catch (ValidationException e)
        {
            // Presumably the user changed the settings to make them invalid. We will show the "Conform Exit" prompt.
        }
        // Unsaved changes - confirm the close operation.
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you wish to close without saving?\n\n",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION)
        {
            System.exit(0);
        }
    }

    private Component newTabbedPanel()
    {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                checkForJiraHomeChange();
            }
        });
        tabbedPane.addTab("JIRA Home", newJiraHomePanel());
        tabbedPane.addTab("Database", newDatabasePanel());
        tabbedPane.addTab("Web Server", newWebServerPanel());
        tabbedPane.addTab("Advanced", newAdvancedPanel());
        return tabbedPane;
    }

    private JComponent newJiraHomePanel()
    {
        ConfigPanelBuilder panelBuilder = new ConfigPanelBuilder();
        panelBuilder.add("JIRA Home Directory", getJiraHomePicker());
        JPanel panel = panelBuilder.getPanel();
        // set 4 pixel border
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));

        return panel;
    }

    private JComponent getJiraHomePicker()
    {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        tfJiraHome.setToolTipText("Set the 'JIRA Home' directory (used to store data for this JIRA installation)");
        // Let TextField take up most room
        panel.add(tfJiraHome, BorderLayout.CENTER);
        // Add a button on the end
        final JButton btnJiraHome = new JButton("Browse");
        btnJiraHome.setToolTipText("Opens a dialog box to select the JIRA Home");
        btnJiraHome.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                browseForJiraHome();
            }
        });
        panel.add(btnJiraHome, BorderLayout.EAST);

        return panel;
    }

    private void browseForJiraHome()
    {
        JFileChooser chooser = new JFileChooser(tfJiraHome.getText());
        chooser.setDialogTitle("JIRA Home directory");
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileFilter()
        {
            public boolean accept(final File f)
            {
                return f.isDirectory();
            }

            public String getDescription()
            {
                return "Directory";
            }
        });
        // Show the file chooser with custom text for Accept button
        int returnVal = chooser.showDialog(this, "OK");
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File jirahome = chooser.getSelectedFile().getAbsoluteFile();

            // Annoyingly, if you choose a directory by double-clicking into
            // it and then choosing OK, you can end up with something like
            // /path/to/jira-home/jira-home instead of what you wanted.  This
            // is a workaround for the chooser doing that.  We only do this
            // if what the chooser returned does not exist but has an existing
            // parent with the same name.  Further, it is still possible to
            // enter the doubled directory name manually if that's really what
            // you wanted.
            if (!jirahome.exists())
            {
                File parent = jirahome.getParentFile();
                if (parent != null && parent.exists() && parent.getName().equals(jirahome.getName()))
                {
                    jirahome = parent;
                }
            }
            tfJiraHome.setText(jirahome.getPath());
        }
    }

    private JComponent newWebServerPanel()
    {
        final JPanel webServerPanel = new JPanel(new BorderLayout());
        webServerPanel.add(initWebServerWarningLabel(), BorderLayout.NORTH);
        webServerPanel.add(createWebServerConfigPanel(), BorderLayout.CENTER);
        webServerPanel.add(initTestSslSetupButton(), BorderLayout.SOUTH);
        return webServerPanel;
    }

    private JComponent initWebServerWarningLabel()
    {
        tooComplexConfigurationWarning.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
        tooComplexConfigurationWarning.setVisible(false);
        tooComplexConfigurationWarning.setBorder(new EmptyBorder(4, 4, 10, 4));
        return tooComplexConfigurationWarning;
    }
    
    private void updateWebServerWarningLabel(@Nonnull final List<ComplexConfigurationReason> reasons)
    {
        if (!reasons.isEmpty())
        {
            final StringBuilder builder = new StringBuilder();
            // html for the word-wrap win
            builder.append("<html>");
            builder.append("It has been detected, that the current configuration is too complex or partly not supported"
                    + " to offer all options. The following items have been identified:");
            for (final ComplexConfigurationReason reason : reasons)
            {
                builder.append("<br />* ").append(reason.getDescription());
            }
            builder.append("</html>");
            tooComplexConfigurationWarning.setText(builder.toString());
        }
        tooComplexConfigurationWarning.setVisible(!reasons.isEmpty());
    }

    private JComponent createWebServerConfigPanel()
    {
        ConfigPanelBuilder panelBuilder = new ConfigPanelBuilder();
        panelBuilder.add("Control Port", tfControlPort);
        panelBuilder.add("Profile", cbWebServerProfile);
        panelBuilder.add("HTTP Port", tfHttpPort);
        panelBuilder.add("HTTPs Port", tfHttpsPort);
        panelBuilder.add("Keystore Path", createKeyStorePanel());
        panelBuilder.add("Keystore Password", tfKeyStorePassword);
        panelBuilder.add("Key Alias", tfKeyAlias);

        JPanel panel = panelBuilder.getPanel();
        // set 4 pixel border
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));

        initWebServerComboBox();

        return panel;
    }

    private void initWebServerComboBox()
    {
        final WebServerProfile[] allProfiles = WebServerProfile.values();
        for (final WebServerProfile profile : allProfiles)
        {
            cbWebServerProfile.addItem(profile);
        }
    }

    @Nonnull
    private WebServerProfile getWebServerProfile()
    {
        return (WebServerProfile) cbWebServerProfile.getSelectedItem();
    }

    private void updateWebServerConfigurationFields(@Nonnull final WebServerProfile profile)
    {
        final boolean httpEnabled = profile.isHttpEnabled();
        tfHttpPort.setEnabled(httpEnabled);
        
        final boolean httpsEnabled = profile.isHttpsEnabled();
        tfHttpsPort.setEnabled(httpsEnabled);
        tfKeyStorePath.setEnabled(httpsEnabled);
        browseKeyStoreButton.setEnabled(httpsEnabled);
        tfKeyStorePassword.setEnabled(httpsEnabled);
        tfKeyAlias.setEnabled(httpsEnabled);
        checkCertificate.setEnabled(httpsEnabled);
    }

    private JComponent createKeyStorePanel()
    {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(tfKeyStorePath, BorderLayout.CENTER);
        browseKeyStoreButton.setToolTipText("Opens a dialog box to select the key store.");
        browseKeyStoreButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                browseForKeyStore();
            }
        });
        panel.add(browseKeyStoreButton, BorderLayout.EAST);

        return panel;
    }

    private void browseForKeyStore()
    {
        JFileChooser chooser = new JFileChooser(tfKeyStorePath.getText());
        chooser.setDialogTitle("Keystore Path");
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = chooser.showDialog(this, "OK");
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File keyStorePath = chooser.getSelectedFile().getAbsoluteFile();
            tfKeyStorePath.setText(keyStorePath.getPath());
        }
    }

    private JButton initTestSslSetupButton()
    {
        checkCertificate.setToolTipText("Verifies that the entered values for the HTTPs set up are valid.");
        checkCertificate.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    final CertificateDetails certificateDetails = new CertificateDetails(validateKeyStoreLocation(), validateKeyStorePassword(), validateKeyAlias());
                    final X509Certificate x509Certificate = keyStoreAccessor.loadCertificate(certificateDetails);
                    if (x509Certificate != null)
                    {
                        final String certificateInformation = CertificatePrettyPrinter.prettyPrint(x509Certificate);
                        showInformationDialog(format("The following certificate was found:%n%n%s%n", certificateInformation));
                    }
                    else
                    {
                        showErrorDialog("The private key could not be found in the key store.");
                    }
                }
                catch (UnrecoverableEntryException e)
                {
                    showErrorDialog("The entered password is valid for the key store, but not for the private key.\n"
                            + "You need to synchronize both passwords in order to proceed.");
                }
                catch (Exception e)
                {
                    // TODO try to map the various exceptions to user friendly error messages
                    showErrorDialog(e);
                }
            }
        });
        return checkCertificate;
    }

    private String validateKeyStoreLocation() throws ValidationException
    {
        return Validator.EXISTING_FILE.apply("Keystore Path", tfKeyStorePath.getText());
    }

    private String validateKeyStorePassword() throws ValidationException
    {
        return Validator.NON_EMTPY_STRING.apply("Keystore Password", tfKeyStorePassword.getText());
    }

    private String validateKeyAlias() throws ValidationException
    {
        return Validator.NON_EMTPY_STRING.apply("Key Alias", tfKeyAlias.getText());
    }

    private JComponent newAdvancedPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));

        // North Panel
        JPanel pnlNorth = new JPanel(new VerticalFlowLayout());
        panel.add(pnlNorth, BorderLayout.NORTH);

        pnlNorth.add(newAdvancedConfigPanel("Scalability and Performance", ConnectionPoolField.getScalabityAndPerformanceFields()));
        pnlNorth.add(newAdvancedConfigPanel("Eviction Policy", ConnectionPoolField.getEvictionPolicyFields()));
        return panel;
    }

    private JPanel newAdvancedConfigPanel(final String title, List<ConnectionPoolField> fields)
    {
        ConfigPanelBuilder panelBuilder = new ConfigPanelBuilder();
        for (ConnectionPoolField field : fields)
        {
            addConnectionPoolField(panelBuilder, field);
        }
        final JPanel panel = panelBuilder.getPanel();
        panel.setBorder(new TitledBorder(title));
        return panel;
    }

    private void addConnectionPoolField(ConfigPanelBuilder panelBuilder, ConnectionPoolField field)
    {
        final int len = field.getLengthHint();
        final JComponent component = (len > 0) ? new JTextField(len) : new JCheckBox();
        component.setToolTipText(field.description());
        advancedSettings.put(field, panelBuilder.add(field.label(), component, field.defaultValue()));
    }

    private JComponent newDatabasePanel()
    {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));

        // North Panel
        JPanel pnlNorth = new JPanel(new VerticalFlowLayout());
        panel.add(pnlNorth, BorderLayout.NORTH);

        // DB Type drop down
        createDataBaseDropDown();
        ConfigPanelBuilder panelBuilder = new ConfigPanelBuilder();
        panelBuilder.add("Database type", ddDatabaseType);
        pnlNorth.add(panelBuilder.getPanel());

        // Card panel with DB details
        createCardPanel();
        pnlNorth.add(cardPanel);

        // on the bottom put the test button
        panel.add(newTestConnectionButton(), BorderLayout.SOUTH);

        return panel;
    }

    private JComponent newTestConnectionButton()
    {
        JButton button = new JButton("Test Connection");
        button.setToolTipText("Try to connect to the database with these settings");
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                // Reset the Connection test message
                connectionTestDialog.setText("Attempting to connect to the " + getSelectedDatabaseConfigPanel().getDisplayName() + " database server...");
                // Don't allow the dialog to clase until the test is complete
                connectionTestDialog.enableCloseButton(false);

                // Because we are going to show a modal dialog, our test has to run in a separate thread.
                new Thread(new Runnable()
                {
                    public void run()
                    {
                        testConnection();
                    }
                }).start();

                // Show the modal dialog
                connectionTestDialog.setVisible(true);
            }
        });
        return button;
    }

    private void testConnection()
    {
        DatabaseConfigPanel jdbcPanel = getSelectedDatabaseConfigPanel();
        String errorMessage = null;

        try
        {
            jdbcPanel.validate();
            jdbcPanel.testConnection(getCanonicalPathToSelectedJiraHomeDirectory());
        }
        catch (UnsupportedClassVersionError ex)
        {
            errorMessage = "UnsupportedClassVersionError occurred. It is likely your JDBC drivers use a newer version of Java than you are running now.";
        }
        catch (ClassNotFoundException ex)
        {
            errorMessage = join(DatabaseDriverRegisterer.forDriverClass(jdbcPanel.getClassName())
                    .getErrorMessage().iterator(), "\n");
        }
        catch (SQLException ex)
        {
            String message = ex.getMessage();
            if (message.contains("Stack Trace"))
            {
                // postgres wants to throw the stack trace in the error message (even for host not found - dumb)
                // try to make this more user friendly
                if (message.contains("UnknownHostException"))
                {
                    message = "Unknown host.";
                }
                else if (message.contains("Check that the hostname and port are correct"))
                {
                    message = "Check that the hostname and port are correct.";
                }
                // other message unknown - show in full.
            }
            errorMessage = "Could not connect to the DB: " + message;
        }
        catch (ValidationException ex)
        {
            errorMessage = ex.getMessage();
        }
        catch (RuntimeException ex)
        {
            errorMessage = "An unexpected error occurred: " + ex.getMessage();
            System.err.println(errorMessage);
            ex.printStackTrace(System.err);
        }

        // Not really supposed to do Swing stuff in a different thread :(
        // make a Runnable to invoke on the Swing thread.
        class SwingRunner implements Runnable
        {
            private String errorMessage;

            SwingRunner(String errorMessage)
            {
                this.errorMessage = errorMessage;
            }
            public void run()
            {
                if (errorMessage == null)
                { connectionTestDialog.addText("Connection successful."); }
                else
                { connectionTestDialog.addText(errorMessage); }
                connectionTestDialog.enableCloseButton(true);
            }
        }

        SwingUtilities.invokeLater(new SwingRunner(errorMessage));
    }

    private DatabaseConfigPanel getSelectedDatabaseConfigPanel()
    {
        return getDatabaseConfigPanel(getSelectedDatabaseType());
    }

    private DatabaseType getSelectedDatabaseType()
    {
        return (DatabaseType) ddDatabaseType.getSelectedItem();
    }

    private void createCardPanel()
    {
        cardPanel = new JPanel();
        jdbcTypeCardLayout = new CardLayout();
        cardPanel.setLayout(jdbcTypeCardLayout);
        cardPanel.setBorder(new TitledBorder("Connection Details"));

        for (DatabaseType databaseType : DatabaseType.knownTypes())
        {
            cardPanel.add(getDatabaseConfigPanel(databaseType).getPanel(), databaseType.getDisplayName());
        }
    }

    private DatabaseConfigPanel getDatabaseConfigPanel(final DatabaseType databaseType)
    {
        if (configPanelMap == null)
        {
            configPanelMap = new HashMap<DatabaseType, DatabaseConfigPanel>(5);
            configPanelMap.put(DatabaseType.HSQL, new HsqlConfigPanel());
            configPanelMap.put(DatabaseType.SQL_SERVER, new SqlServerConfigPanel());
            configPanelMap.put(DatabaseType.MY_SQL, new MySqlConfigPanel());
            configPanelMap.put(DatabaseType.ORACLE, new OracleConfigPanel());
            configPanelMap.put(DatabaseType.POSTGRES, new PostgresConfigPanel());
        }
        return configPanelMap.get(databaseType);
    }


    private void createDataBaseDropDown()
    {
        ddDatabaseType = new JComboBox(Iterables.toArray(DatabaseType.knownTypes(), DatabaseType.class));
        ddDatabaseType.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e)
            {
                DatabaseType databaseType = getSelectedDatabaseType();
                if (databaseType == null)
                {
                    return;
                }
                // Show the specific config panel for this JDBC connection type
                jdbcTypeCardLayout.show(cardPanel, databaseType.getDisplayName());
            }
        });
    }


    private JPanel newButtonPanel()
    {
        JPanel panel = new JPanel();
        panel.add(newSaveButton());
        panel.add(newCloseButton());
        return panel;
    }

    private Component newSaveButton()
    {
        JButton button = new JButton("Save");
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                try
                {
                    checkForJiraHomeChange();
                    Configurator.saveSettings(gatherNewSettings());
                    showInformationDialog("Settings saved successfully.");
                }
                catch (ValidationException ex)
                {
                    showErrorDialog(ex);
                }
                catch (IOException ex)
                {
                    showErrorDialog(ex);
                }
                catch (ParseException ex)
                {
                    showErrorDialog(ex);
                }
                catch (RuntimeException ex)
                {
                    ex.printStackTrace();
                    showErrorDialog("Unexpected error during save:\n" + ex.getMessage());
                }
            }
        });
        return button;
    }

    private Settings gatherNewSettings() throws ValidationException
    {
        Settings newSettings = new Settings();
        // jira.home
        newSettings.setJiraHome(getCanonicalPathToSelectedJiraHomeDirectory());
        // DB Connection
        getSelectedDatabaseConfigPanel().saveSettings(newSettings, getCanonicalPathToSelectedJiraHomeDirectory());
        // Web Server
        saveWebServerSettings(newSettings);
        // Advanced settings
        gatherAdvancedSettings(newSettings);

        // In the config tool we use a separate ConnectionPoolInfoBuilder.
        // Null this one out for when we want to compare it to saved settings.
        newSettings.getJdbcDatasourceBuilder().setConnectionPoolInfo(null);
        return newSettings;
    }

    private String getCanonicalPathToSelectedJiraHomeDirectory() throws ValidationException
    {
        final File jiraHomeTextFieldInputAsFile = new File(tfJiraHome.getText());
        try
        {
            return jiraHomeTextFieldInputAsFile.getCanonicalPath();
        }
        catch (IOException e)
        {
            throw new ValidationException(
                    format
                            (
                                    "The path: '%s' specified for the JIRA home directory could not be resolved to a "
                                            + "canonical file system path. Please verify the specified path is correct.",
                                    tfJiraHome.getText()
                            )
            );
        }
    }

    private void saveWebServerSettings(@Nonnull final Settings newSettings) throws ValidationException
    {
        newSettings.setControlPort(gatherControlPort());
        final WebServerProfile selectedWebServerProfile = getWebServerProfile();
        final String httpPort = gatherHttpPort(selectedWebServerProfile);
        final SslSettings sslSettings = gatherSslSettings(selectedWebServerProfile);
        newSettings.updateWebServerConfiguration(httpPort, sslSettings);
    }

    private String gatherControlPort() throws ValidationException
    {
        final String controlPort = tfControlPort.getText().trim();
        final Integer validatedControlPort = validatePort("Control Port", controlPort);
        return validatedControlPort.toString();
    }

    private String gatherHttpPort(@Nonnull final WebServerProfile profile) throws ValidationException
    {
        if (profile.isHttpEnabled())
        {
            final String httpPort = tfHttpPort.getText().trim();
            final Integer validatedHttpPort = validatePort("HTTP Port", httpPort);
            return validatedHttpPort.toString();
        }
        return null;
    }

    private SslSettings gatherSslSettings(@Nonnull final WebServerProfile profile) throws ValidationException
    {
        if (profile.isHttpsEnabled())
        {
            final Integer httpsPort = validatePort("HTTPs Port", tfHttpsPort.getText());
            final String keyStoreLocation = validateKeyStoreLocation();
            final String keyStorePassword = validateKeyStorePassword();
            final String keyAlias = validateKeyAlias();

            return new SslSettings(httpsPort.toString(), keyStoreLocation, keyStorePassword, "JKS", keyAlias);
        }
        return null;
    }

    private Integer validatePort(String fieldName, String port) throws ValidationException
    {
        return Validator.PORT.apply(fieldName, port);
    }

    private Component newCloseButton()
    {
        JButton button = new JButton("Close");
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                closeApplication();
            }
        });
        return button;
    }

    public void setSettings(final Settings settings) throws ParseException
    {
        // JIRA home
        tfJiraHome.setText(settings.getJiraHome());
        previousJiraHome = settings.getJiraHome();

        // Set the drop down according to the current Database type
        ddDatabaseType.setSelectedItem(settings.initDatabaseType(false));
        getSelectedDatabaseConfigPanel().setSettings(settings);

        // Web Server
        final WebServerProfile webServerProfile = settings.getWebServerProfile();
        tfControlPort.setText(settings.getControlPort());
        updateWebServerProfile(settings);
        if (webServerProfile.isHttpEnabled())
        {
            tfHttpPort.setText(settings.getHttpPort());
        }
        final SslSettings sslSettings = settings.getSslSettings();
        if (webServerProfile.isHttpsEnabled() && sslSettings != null)
        {
            tfHttpsPort.setText(sslSettings.getHttpsPort());
            tfKeyStorePath.setText(sslSettings.getKeystoreFile());
            tfKeyStorePassword.setText(sslSettings.getKeystorePass());
            tfKeyAlias.setText(sslSettings.getKeyAlias());
        }
        updateWebServerConfigurationFields(webServerProfile);

        // Advanced settings
        initAdvancedSettings(settings);
    }

    private void updateWebServerProfile(@Nonnull final Settings settings)
    {
        final List<WebServerProfile> usableProfiles = WebServerProfile.getUsableProfiles(settings);
        cbWebServerProfile.setRenderer(new DisabledComboBoxRenderer(usableProfiles));
        cbWebServerProfile.setSelectedItem(settings.getWebServerProfile());
        cbWebServerProfile.setAction(new AbstractAction()
        {
            private WebServerProfile currentProfile = settings.getWebServerProfile();
            private List<WebServerProfile> usableProfile = usableProfiles;
            
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                final WebServerProfile selectedProfile = getWebServerProfile();
                if (usableProfile.contains(selectedProfile))
                {
                    currentProfile = selectedProfile;
                    updateWebServerConfigurationFields(selectedProfile);
                }
                else
                {
                    cbWebServerProfile.setSelectedItem(currentProfile);
                }
            }
        });

        updateWebServerWarningLabel(settings.getComplexConfigurationReasons());
    }

    private void showErrorDialog(final Exception ex)
    {
        showErrorDialog(ex.getMessage());
    }

    private void showErrorDialog(final String message)
    {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInformationDialog(final String message)
    {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void initAdvancedSettings(Settings settings)
    {
        final ConnectionPoolInfo.Builder poolInfo = settings.getConnectionPoolInfoBuilder();

        advancedSettings.get(ConnectionPoolField.MAX_SIZE).setValue(poolInfo.getPoolMaxSize());
        advancedSettings.get(ConnectionPoolField.MAX_IDLE).setValue(poolInfo.getPoolMaxIdle());
        advancedSettings.get(ConnectionPoolField.MIN_SIZE).setValue(poolInfo.getPoolMinSize());
        advancedSettings.get(ConnectionPoolField.INITIAL_SIZE).setValue(poolInfo.getPoolInitialSize());
        advancedSettings.get(ConnectionPoolField.MAX_WAIT).setValue(poolInfo.getPoolMaxWait());
        advancedSettings.get(ConnectionPoolField.POOL_STATEMENTS).setValue(poolInfo.getPoolPreparedStatements());
        advancedSettings.get(ConnectionPoolField.MAX_OPEN_STATEMENTS).setValue(poolInfo.getMaxOpenPreparedStatements());

        advancedSettings.get(ConnectionPoolField.VALIDATION_QUERY).setValue(poolInfo.getValidationQuery());
        advancedSettings.get(ConnectionPoolField.VALIDATION_QUERY_TIMEOUT).setValue(poolInfo.getValidationQueryTimeout());
        advancedSettings.get(ConnectionPoolField.TEST_ON_BORROW).setValue(poolInfo.getTestOnBorrow());
        advancedSettings.get(ConnectionPoolField.TEST_ON_RETURN).setValue(poolInfo.getTestOnReturn());
        advancedSettings.get(ConnectionPoolField.TEST_WHILE_IDLE).setValue(poolInfo.getTestWhileIdle());
        advancedSettings.get(ConnectionPoolField.TIME_BETWEEN_EVICTION_RUNS).setValue(poolInfo.getTimeBetweenEvictionRunsMillis());
        advancedSettings.get(ConnectionPoolField.MIN_EVICTABLE_IDLE_TIME).setValue(poolInfo.getMinEvictableTimeMillis());
        advancedSettings.get(ConnectionPoolField.REMOVE_ABANDONED).setValue(poolInfo.getRemoveAbandoned());
        advancedSettings.get(ConnectionPoolField.REMOVE_ABANDONED_TIMEOUT).setValue(poolInfo.getRemoveAbandonedTimeout());
    }

    private <T> T gatherAdvancedSetting(ConnectionPoolField field, Validator<T> validator) throws ValidationException
    {
        final LabelledComponent component = advancedSettings.get(field);
        return validator.apply(component.label(), component.getValue());
    }

    private ConnectionPoolInfo.Builder verifyAdvancedSettings() throws ValidationException
    {
        final ConnectionPoolInfo.Builder poolInfo = ConnectionPoolInfo.builder();
        poolInfo.setPoolMaxSize(gatherAdvancedSetting(ConnectionPoolField.MAX_SIZE, Validator.INTEGER_POSITIVE));
        poolInfo.setPoolMaxIdle(gatherAdvancedSetting(ConnectionPoolField.MAX_IDLE, Validator.INTEGER_ALLOW_MINUS_1));
        poolInfo.setPoolMinSize(gatherAdvancedSetting(ConnectionPoolField.MIN_SIZE, Validator.INTEGER_POSITIVE_OR_ZERO));
        poolInfo.setPoolInitialSize(gatherAdvancedSetting(ConnectionPoolField.INITIAL_SIZE, Validator.INTEGER_POSITIVE_OR_ZERO));
        poolInfo.setPoolMaxWait(gatherAdvancedSetting(ConnectionPoolField.MAX_WAIT, Validator.LONG_ALLOW_MINUS_1));
        poolInfo.setPoolPreparedStatements(gatherAdvancedSetting(ConnectionPoolField.POOL_STATEMENTS, Validator.BOOLEAN));
        poolInfo.setMaxOpenPreparedStatements(gatherAdvancedSetting(ConnectionPoolField.MAX_OPEN_STATEMENTS, Validator.INTEGER_ALLOW_MINUS_1));

        poolInfo.setValidationQuery(gatherAdvancedSetting(ConnectionPoolField.VALIDATION_QUERY, Validator.TRIMMED_STRING));
        poolInfo.setValidationQueryTimeout(gatherAdvancedSetting(ConnectionPoolField.VALIDATION_QUERY_TIMEOUT, Validator.INTEGER_ALLOW_MINUS_1));
        poolInfo.setTestOnBorrow(gatherAdvancedSetting(ConnectionPoolField.TEST_ON_BORROW, Validator.BOOLEAN));
        poolInfo.setTestOnReturn(gatherAdvancedSetting(ConnectionPoolField.TEST_ON_RETURN, Validator.BOOLEAN));
        poolInfo.setTestWhileIdle(gatherAdvancedSetting(ConnectionPoolField.TEST_WHILE_IDLE, Validator.BOOLEAN));
        poolInfo.setTimeBetweenEvictionRunsMillis(gatherAdvancedSetting(ConnectionPoolField.TIME_BETWEEN_EVICTION_RUNS, Validator.LONG_ALLOW_MINUS_1));
        poolInfo.setMinEvictableTimeMillis(gatherAdvancedSetting(ConnectionPoolField.MIN_EVICTABLE_IDLE_TIME, Validator.LONG_POSITIVE));
        poolInfo.setRemoveAbandoned(gatherAdvancedSetting(ConnectionPoolField.REMOVE_ABANDONED, Validator.BOOLEAN));
        poolInfo.setRemoveAbandonedTimeout(gatherAdvancedSetting(ConnectionPoolField.REMOVE_ABANDONED_TIMEOUT, Validator.INTEGER_POSITIVE));

        return poolInfo;
    }


    private void gatherAdvancedSettings(Settings settings) throws ValidationException
    {
        settings.setConnectionPoolInfoBuilder(verifyAdvancedSettings());
        // Invalid values may get defaulted instead of retained or rejected, so refresh the display
        initAdvancedSettings(settings);
    }

    private void checkForJiraHomeChange()
    {
        final String newJiraHome;
        try
        {
            newJiraHome = getCanonicalPathToSelectedJiraHomeDirectory();
            if (!newJiraHome.equals(previousJiraHome))
            {
                // Check if this folder actually exists and contains a dbconfig file
                File configFile = new File(newJiraHome, "dbconfig.xml");
                if (configFile.exists())
                {
                    // Offer to reload the DB config from the file
                    int option = JOptionPane.showConfirmDialog(ConfiguratorFrame.this, "Would you like to reload the DB configuration from the new JIRA Home?", "Reload DB Config", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (option == JOptionPane.YES_OPTION)
                    {
                        try
                        {
                            final Settings settings = SettingsLoader.reloadDbConfig(newJiraHome);
                            // Set the drop down according to the current Database type
                            ddDatabaseType.setSelectedItem(settings.initDatabaseType(false));
                            getSelectedDatabaseConfigPanel().setSettings(settings);
                            initAdvancedSettings(settings);
                        }
                        catch (IOException ex)
                        {
                            showErrorDialog(ex);
                        }
                        catch (ParseException ex)
                        {
                            showErrorDialog(ex);
                        }
                    }
                }
                previousJiraHome = newJiraHome.trim();
            }
        }
        catch (ValidationException e)
        {
            showErrorDialog(e);
        }
    }
}
