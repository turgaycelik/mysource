package com.atlassian.jira.configurator;

import com.atlassian.jira.config.database.JdbcDatasource;
import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.SettingsLoader;
import com.atlassian.jira.configurator.console.ConfiguratorConsole;
import com.atlassian.jira.configurator.gui.ConfiguratorFrame;
import com.atlassian.jira.exception.ParseException;
import org.xml.sax.SAXException;

import java.awt.*;
import java.io.IOException;
import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;

public class Configurator
{
    private static Settings settings;
    // do we want to run in console mode or GUI mode
    private static boolean consoleMode = false;

    public static void main(String[] args)
    {
        // JdbcDatasource normally registers the configured JDBC driver during construction, which does not work for the
        // Config Tool, because we don't have the drivers in our default ClassLoader.
        JdbcDatasource.setRegisterDriverOnConstruct(false);

        // Parse the arguments
        parseArguments(args);
        
        // Load the settings from the various config files.
        loadCurrentSettings();

        start();
    }

    private static void start()
    {
        if (!consoleMode)
        {
            try
            {
                startGui();
                return;
            }
            catch (HeadlessException hex)
            {
                System.err.println("No graphics display available; using console.");
                consoleMode = true;
            }
        }

        startConsole();
    }

    private static void startConsole()
    {
        ConfiguratorConsole console = new ConfiguratorConsole();
        console.setSettings(settings);
        console.start();                        
    }

    private static void startGui()
    {
        ConfiguratorFrame configuratorFrame = new ConfiguratorFrame();
        // Appear in the middle of the screen
        configuratorFrame.setLocationRelativeTo(null);
        // "Configurator" is the nickname, but the official name from the Documentation Team is "JIRA Configuration Tool"
        configuratorFrame.setTitle(System.getProperty("jira.configurator.name", "JIRA Configuration Tool"));
        try
        {
            configuratorFrame.setSettings(settings);
        }
        catch (ParseException e)
        {
            showWarningDialog("Unable to fully parse the current JDBC settings.  Some settings may be blank.\n" + e.getMessage());
        }
        configuratorFrame.setVisible(true);
    }

    private static void parseArguments(String[] args)
    {
        for (String arg : args)
        {
            if ("-c".equals(arg) || "--console".equals(arg))
            {
                consoleMode = true;
            }
        }
    }

    public static void saveSettings(Settings newSettings) throws IOException, ParseException
    {
        SettingsLoader.saveSettings(newSettings);
        // saved successfully - set the current settings. We use this to see if the user has made changes.
        settings = newSettings;
    }
    
    private static void loadCurrentSettings()
    {
        try
        {
            settings = SettingsLoader.loadCurrentSettings();
        }
        catch (IOException e)
        {
            showErrorDialogAndExit("IO Exception occurred while trying to load settings.\n" + e.getMessage());
        }
        catch (SAXException e)
        {
            showErrorDialogAndExit("SAX Exception occurred while trying to load settings.\n" + e.getMessage());
        }
        catch (ParserConfigurationException e)
        {
            showErrorDialogAndExit("Parser Configuration Exception occurred while trying to load settings.\n" + e.getMessage());
        }
        catch (ParseException e)
        {
            showErrorDialogAndExit("Parse Exception occurred while trying to load settings.\n" + e.getMessage());
        }
    }

    private static void showErrorDialogAndExit(final String message)
    {
        System.err.println(message);
        JOptionPane.showMessageDialog(null, message, "Fatal Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private static void showWarningDialog(final String message)
    {
        System.err.println(message);
        JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean settingsEqual(final Settings newSettings)
    {
        // We use our own copy of ConnectionPoolInfo.Builder so clear out the JdbcDatasourceBuilder.ConnectionPoolInfo
        // for successful comparison.
        settings.getJdbcDatasourceBuilder().setConnectionPoolInfo(null);
        return settings.equals(newSettings);
    }
}
