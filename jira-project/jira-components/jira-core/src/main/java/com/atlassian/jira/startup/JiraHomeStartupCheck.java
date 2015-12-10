package com.atlassian.jira.startup;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.config.util.StartupJiraHome;
import com.atlassian.jira.help.HelpUrl;
import com.atlassian.jira.help.StaticHelpUrls;
import com.atlassian.jira.plugin.PluginPath;
import com.atlassian.jira.service.services.file.FileService;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.ServletContextProvider;
import com.google.common.annotations.VisibleForTesting;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.servlet.ServletContext;

/**
 * This StartupCheck will check that there is a valid jira.home configured that we can get an exclusive lock on.
 * <p/>
 * <em>Note: this has the side effect that the jira.home directory is created, if required, and "locked".</em> These
 * side-effects are REQUIRED in order to return valid results.
 *
 * @since v4.0
 */
public class JiraHomeStartupCheck implements StartupCheck
{
    private static final Logger log = Logger.getLogger(JiraHomeStartupCheck.class);
    private static JiraHomeStartupCheck HOME_STARTUP_CHECK;

    private final static CompositeJiraHomePathLocator pathLocator = new CompositeJiraHomePathLocator(
            new SystemPropertyJiraHomePathLocator(),
            new WebContextJiraHomePathLocator(),
            new ApplicationPropertiesJiraHomePathLocator());

    @ClusterSafe ("Program Artifact")
    public static synchronized JiraHomeStartupCheck getInstance()
    {
        if (HOME_STARTUP_CHECK == null)
        {
           HOME_STARTUP_CHECK = new JiraHomeStartupCheck(pathLocator);
        }
        return HOME_STARTUP_CHECK;
    }

    private final JiraHomePathLocator locator;
    private final JiraHomeLockAcquirer lockAcquirer;
    private volatile String faultDescription;
    private volatile String faultDescriptionHtml;
    private volatile File jiraHomeDirectory;
    private volatile boolean initalised = false;

    @VisibleForTesting
    JiraHomeStartupCheck(final JiraHomePathLocator locator)
    {
        this.locator = locator;
        this.lockAcquirer = new JiraHomeLockAcquirer();
    }

    public String getName()
    {
        return "Jira.Home Startup Check";
    }

    public boolean isOk()
    {
        if (!initalised)
        {
            try
            {
                // Get configured jira.home
                final String jiraHome = getConfiguredJiraHome();
                // Validate the jira.home, and create the directory if required
                // Note that we only save jiraHomeDirectory if everything is valid.
                jiraHomeDirectory = validateJiraHome(jiraHome);
                // The Jira Home is now available for the JiraHomeService to pick up once Pico starts up.
            }
            catch (final JiraHomeException ex)
            {
                faultDescriptionHtml = ex.getHtmlMessage();
                faultDescription = ex.getMessage();
            }
            finally
            {
                initalised = true;
            }
        }
        return jiraHomeDirectory != null;
    }

    public boolean isInitialised()
    {
        return initalised;
    }

    private String getConfiguredJiraHome() throws JiraHomeException
    {
        final String home = locator.getJiraHome();
        if (StringUtils.isBlank(home))
        {
            // No jira.home is configured by any method.
            throw newJiraHomeExceptionWithHelpLink("No jira.home is configured.\nSee %s for instructions on setting jira.home", "jirahome");
        }
        return home;
    }

    /**
     * Traverses up the specified directory parents' searching for a specified directory
     *
     * @param directoryToTraverse the one to traverse
     * @param directoryToFind the one to find
     * @return if the find directory is in the traverse directory
     */
    private boolean findDirectory(File directoryToTraverse, File directoryToFind)
    {
        File currentDirectory = directoryToTraverse;
        while (currentDirectory != null)
        {
            if (currentDirectory.equals(directoryToFind))
            {
                return true;
            }
            currentDirectory = currentDirectory.getParentFile();
        }
        return false;
    }

    private File validateJiraHome(final String jiraHome) throws JiraHomeException
    {
        final File proposedJiraHome = new File(jiraHome);

        // try to show useful error messages if the user puts a single-backslash in their jira-application.properties
        // for jira.home. java.util.Properties does a lot of magic parsing so we don't have much to work with.
        if (!proposedJiraHome.isAbsolute())
        {
            if (JiraSystemProperties.isDevMode())
            {
                // NOTE: this override is only here for development, this should not be used in production.
                log.warn("jira.home is a relative path, but jira.dev.mode is set to true so we will allow this.");
            }
            else
            {
                final HelpUrl helpPath = StaticHelpUrls.getInstance().getUrl("jirahome");
                final String s = "jira.home must be an absolute path.\nSee %s for instructions on setting jira.home";
                final StringBuilder plainText = new StringBuilder(String.format(s, helpPath.getUrl()));

                final String href = String.format("<a href=\"%s\">%s</a>", helpPath.getUrl(), TextUtils.htmlEncode(helpPath.getTitle()));
                final String htmlText = String.format(TextUtils.htmlEncode(s), href);

                plainText.append("\nYour current jira.home is:\n");
                plainText.append(jiraHome);

                final boolean deadlyBackslash = JiraSystemProperties.getInstance().getProperty("file.separator").equals("\\");
                if (deadlyBackslash)
                {
                    plainText.append("\n");
                    plainText.append("It looks like you are on Windows. This is usually caused by incorrect use of backslashes inside of jira-application.properties.\n");
                    plainText.append("Use forward slashes (/) instead.");
                }

                throw new JiraHomeException(plainText.toString(), htmlText);
            }
        }

        ServletContext servletContext = ServletContextProvider.getServletContext();
        if (servletContext == null)
        {
            log.error("No ServletContext exists - cannot check if jira.home is on the servlet path.");
        }
        else
        {
            // this can be null if there is no "real" path (i.e. for a compressed .war archive). in that case it is safe
            // to skip the check because it is not possible to specify a jira.home inside a .war archive.
            String realPath = servletContext.getRealPath("/");
            if (realPath != null)
            {
                File webappServletPath = new File(realPath);

                if (proposedJiraHome.equals(webappServletPath))
                {
                    throw new JiraHomeException("Configured jira.home '" + proposedJiraHome.getAbsolutePath() +
                            "' must not be the same as the webapp servlet path '" + webappServletPath.getAbsolutePath() + "'");
                }
                if (findDirectory(webappServletPath, proposedJiraHome))
                {
                    throw new JiraHomeException("Configured jira.home '" + proposedJiraHome.getAbsolutePath() +
                            "' must not be a parent directory of the webapp servlet path '" + webappServletPath.getAbsolutePath() + "'");
                }
                if (findDirectory(proposedJiraHome, webappServletPath))
                {
                    throw new JiraHomeException("Configured jira.home '" + proposedJiraHome.getAbsolutePath() +
                            "' must not be a directory within the webapp servlet path '" + webappServletPath.getAbsolutePath() + "'");
                }
            }
        }

        // Check if the jiraHome actually exists
        if (proposedJiraHome.exists())
        {
            // Check that it is a directory
            if (!proposedJiraHome.isDirectory())
            {
                final String message = "Configured jira.home '" + proposedJiraHome.getAbsolutePath() + "' is not a directory.";
                throw new JiraHomeException(message);
            }
        }
        else
        {
            log.info("Configured jira.home '" + proposedJiraHome.getAbsolutePath() + "' does not exist. We will create it.");
            // Attempt to create the directory
            try
            {
                if (!proposedJiraHome.mkdirs())
                {
                    throw newJiraHomeExceptionWithHelpLink("Could not create jira.home directory '" + proposedJiraHome.getAbsolutePath() + "'. "
                            + "Please see %s for more information on how to set up your JIRA home directory.", "jirahome");
                }
            }
            catch (final SecurityException ex)
            {
                throw newJiraHomeExceptionWithHelpLink("Could not create jira.home directory '" + proposedJiraHome.getAbsolutePath() + "'. "
                        + "A Security Exception occured. Please see %s for more information on how to set up your JIRA home directory.", "jirahome");
            }
        }

        // JRA-18645 ensure that all of the subdirectories of jiraHome also exist
        try
        {
            createLocalHomeDirectories(proposedJiraHome);

            JiraHome home = new StartupJiraHome(locator);
            createSharedHomeDirectories(home.getHome());
        }
        catch (final SecurityException ex)
        {
            throw newJiraHomeExceptionWithHelpLink("Could not create jira.home directory '" + proposedJiraHome.getAbsolutePath() + "'. "
                    + "A Security Exception occured. Please see %s for more information on how to set up your JIRA home directory.", "jirahome");
        }

        // attempt to lock the home directory
        lockJiraHome(proposedJiraHome);

        // All tests passed
        log.info("The jira.home directory '" + proposedJiraHome.getAbsolutePath() + "' is validated and locked for exclusive use by this instance.");
        return proposedJiraHome;
    }

    private JiraHomeException newJiraHomeExceptionWithHelpLink(final String messageWithUrlPlaceholder, final String helpPathKey)
    {
        final HelpUrl helpPath = StaticHelpUrls.getInstance().getUrl(helpPathKey);
        final String plainText = String.format(messageWithUrlPlaceholder, helpPath.getUrl());

        final String href = String.format("<a href=\"%s\">%s</a>", helpPath.getUrl(), TextUtils.htmlEncode(helpPath.getTitle()));
        final String htmlText = String.format(TextUtils.htmlEncode(messageWithUrlPlaceholder), href);
        return new  JiraHomeException(plainText, htmlText);
    }

    void createLocalHomeDirectories(final File localJiraHome) throws JiraHomeException
    {
        Set<String> subdirs = CollectionBuilder.<String>newBuilder()
                .add(IndexPathManager.INDEXES_DIR)
                .add(PluginPath.PLUGINS_DIRECTORY)
                .add(PluginPath.INSTALLED_PLUGINS_SUBDIR)
                .addAll(JiraHome.localsubdirs)
                .asMutableSortedSet();

        createHomeDirectories(localJiraHome, subdirs);
    }

    void createSharedHomeDirectories(final File sharedJiraHome) throws JiraHomeException
    {
        Set<String> subdirs = CollectionBuilder.<String>newBuilder()
                .add(AttachmentPathManager.ATTACHMENTS_DIR)
                .add(PluginPath.PLUGINS_DIRECTORY)
                .add(PluginPath.BUNDLED_SUBDIR)
                .add(PluginPath.OSGI_SUBDIR)
                .add(FileService.MAIL_DIR)
                .addAll(JiraHome.sharedsubdirs)
                .asMutableSortedSet();

        createHomeDirectories(sharedJiraHome, subdirs);
    }

    private void createHomeDirectories(final File proposedJiraHome, final Set<String> subdirs)
            throws JiraHomeException
    {
        for (String subdir : subdirs)
        {
            try
            {
                final File dir = new File(proposedJiraHome, subdir);
                if (!dir.exists())
                {
                    if (!dir.mkdirs())
                    {
                        final String s = String.format("Could not create subdirectory '%s' of jira.home '%s'.", subdir, proposedJiraHome);
                        throw new JiraHomeException(s);
                    }
                }
            }
            catch (JiraHomeException homeException)
            {
                throw homeException;
            }
            catch (Exception e)
            {
                final String s = String.format("Error creating subdirectory '%s' of jira.home '%s'.", subdir, proposedJiraHome);
                throw new JiraHomeException(s + "\n" + e.getMessage());
            }
        }
    }

    private void lockJiraHome(final File proposedJiraHome) throws JiraHomeException
    {
        Assertions.notNull("You should not be in this method if you have a null lockAcquirer", lockAcquirer);

        final String jiraHomePath = getJiraHomePath(proposedJiraHome);

        // Look for Lock file
        // Try to lock the directory for ourselves
        String failMsg = "Unable to create and acquire lock file for jira.home directory '" + jiraHomePath + "'.";
        try
        {
            JiraHomeLockAcquirer.LockResult result = lockAcquirer.acquire(proposedJiraHome);
            if (result != JiraHomeLockAcquirer.LockResult.OK)
            {
                if (result == JiraHomeLockAcquirer.LockResult.HELD_BY_OTHERS)
                {
                    final String s = "The jira.home directory '%s' is already locked by another running instance of JIRA.";
                    final String htmlText = String.format(TextUtils.htmlEncode(s), TextUtils.htmlEncode(jiraHomePath));
                    final String plainText = "The jira.home directory '" + jiraHomePath + "' is already locked by another running instance of JIRA.";
                    throw new JiraHomeException(plainText, htmlText);
                }
                else
                {
                    // Creation failed.
                    throw new JiraHomeException(failMsg);
                }
            }
        }
        catch (final JiraHomeException ex)
        {
            throw ex;
        }
        catch (final Exception ex)
        {
            // We log here to get the stack trace - may help the JIRA admin or support. Note that there will be a fatal log message later as well.
            log.error(failMsg + " " + ex.getMessage(), ex);
            throw new JiraHomeException(failMsg);
        }
    }

    private String getJiraHomePath(final File proposedJiraHome)
    {
        try
        {
            return proposedJiraHome.getCanonicalPath();
        }
        catch (IOException e)
        {
            log.debug("Couldn't obtain canonical path for jira.home", e);
            return proposedJiraHome.getAbsolutePath();
        }
    }

    @Override
    public void stop()
    {
        lockAcquirer.release();
    }

    public String getFaultDescription()
    {
        return faultDescription;
    }

    public String getHTMLFaultDescription()
    {
        return faultDescriptionHtml;
    }

    public File getJiraHomeDirectory()
    {
        return jiraHomeDirectory;
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
