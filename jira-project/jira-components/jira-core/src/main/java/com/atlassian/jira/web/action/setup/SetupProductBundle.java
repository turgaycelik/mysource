package com.atlassian.jira.web.action.setup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.atlassian.core.util.FileUtils;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.web.HttpServletVariables;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Allow user choose additional bundle to be installed
 *
 * @since v6.3
 */
public class SetupProductBundle extends AbstractSetupAction
{
    private static final Logger log = Logger.getLogger(SetupProductBundle.class);

    protected static final String SETUP_ALREADY = "setupalready";

    public static final String BUNDLE_TRACKING = "TRACKING";
    public static final String BUNDLE_DEVELOPMENT = "DEVELOPMENT";
    public static final String BUNDLE_SERVICEDESK = "SERVICEDESK";

    private static final String SERVICE_DESK_JAR_PATH = "/WEB-INF/other/service-desk-plugin/";
    private static final String AGILE_JAR_PATH = "/WEB-INF/other/jira-agile-plugin/";
    private JiraHome jiraHome;

    // Default to tracking
    private String selectedBundle = BUNDLE_TRACKING;
    private SetupSharedVariables sharedVariables;

    public SetupProductBundle(final FileFactory fileFactory, final JiraHome jiraHome, final HttpServletVariables servletVariables)
    {
        super(fileFactory);
        this.jiraHome = notNull("jiraHome", jiraHome);
        sharedVariables = new SetupSharedVariables(servletVariables, getApplicationProperties());
    }

    public boolean setupAlready()
    {
        return (getApplicationProperties().getString(APKeys.JIRA_SETUP) != null);
    }

    @Override
    public String doDefault() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        return INPUT;
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        if (getSelectedBundle() == null)
        {
            return ERROR;
        }
        else
        {

            // @TODO persist which bundle was chosen for the license fetch page
            // Nothing to be done for tracking
            if (BUNDLE_DEVELOPMENT.equals(selectedBundle))
            {
                installPluginJars(AGILE_JAR_PATH);
                saveBundleChoice();
            }
            else if (BUNDLE_SERVICEDESK.equals(selectedBundle))
            {
                installPluginJars(SERVICE_DESK_JAR_PATH);
                saveBundleChoice();
            }

            return forceRedirect("SetupLicense!default.jspa");
        }
    }

    public String getSelectedBundle()
    {
        return selectedBundle;
    }

    public void setSelectedBundle(String selectedBundle)
    {
        this.selectedBundle = selectedBundle;
    }

    private void installPluginJars(final String pluginPath)
    {
        try
        {
            // Check the jira plugins directory before proceeding
            final File pluginsDir = jiraHome.getPluginsDirectory();
            final File installedPluginsDir = new File(pluginsDir, "installed-plugins");

            final File[] pluginJars = new File(getServletContext().getRealPath(pluginPath)).listFiles();
            if (pluginJars != null)
            {
                for (final File pluginJar : pluginJars)
                {
                    // write the inputStream to a FileOutputStream if the plugin doesn't already exist
                    final File destinationPluginJar = new File(installedPluginsDir, pluginJar.getName());

                    if (!destinationPluginJar.exists())
                    {
                        InputStream inputStream = getServletContext().getResourceAsStream(pluginPath.concat(pluginJar.getName()));
                        try
                        {
                            FileUtils.copyFile(inputStream, destinationPluginJar, true);
                        }
                        finally
                        {
                            IOUtils.closeQuietly(inputStream);
                        }

                    }
                }
            }
            else
            {
                log.warn("Could not get list of jars to install from " + pluginPath);
            }
        }
        catch (IOException e)
        {
            log.warn("There was a problem installing jars from " + pluginPath, e);
        }
    }

    public void saveBundleChoice()
    {
        sharedVariables.setSelectedBundle(getSelectedBundle());
    }
}
