package com.atlassian.jira.functest.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;
import org.dom4j.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Program that will fix up some of the errors in JIRA's XML configuration.
 */
public class ConfigFixerUpperer
{
    private static final Logger log = Logger.getLogger(ConfigFixerUpperer.class);

    private File root;
    private List<ConfigurationCheck> configurationChecks = new ArrayList<ConfigurationCheck>();
    private List<IOFileFilter> excludes = new ArrayList<IOFileFilter>();
    private boolean createBackups;

    public ConfigFixerUpperer()
    {
    }

    public File getRoot()
    {
        return root;
    }

    public ConfigFixerUpperer setRoot(final File root)
    {
        this.root = root;
        return this;
    }

    public List<ConfigurationCheck> getConfigurationChecks()
    {
        return configurationChecks;
    }

    public ConfigFixerUpperer setConfigurationChecks(final Collection<? extends ConfigurationCheck> configurationChecks)
    {
        if (configurationChecks == null)
        {
            this.configurationChecks = new ArrayList<ConfigurationCheck>();
        }
        else
        {
            this.configurationChecks = new ArrayList<ConfigurationCheck>(configurationChecks);
        }
        return this;
    }

    public List<IOFileFilter> getExcludes()
    {
        return excludes;
    }

    public ConfigFixerUpperer setExcludes(Collection<IOFileFilter> excludes)
    {
        if (excludes == null)
        {
            this.excludes = new ArrayList<IOFileFilter>();
        }
        else
        {
            this.excludes = new ArrayList<IOFileFilter>(excludes);
        }
        return this;
    }

    public boolean createBackups()
    {
        return createBackups;
    }

    public ConfigFixerUpperer setCreateBackups(boolean createBackups)
    {
        this.createBackups = createBackups;
        return this;
    }

    public void fix()
    {
        final ConfigFileWalker walker = new ConfigFileWalker(getRoot(), new Visitor());
        walker.setExcludes(getExcludes());
        walker.walk();
    }

    private class Visitor implements ConfigFileWalker.ConfigVisitor
    {
        public void visitConfig(ConfigFile file)
        {
            log.info(String.format("Processing %s.", file.getFile().getAbsoluteFile()));
            final Document document;
            try
            {
                document = file.readConfig();
            }
            catch (ConfigFile.ConfigFileException e)
            {
                log.error("Unable to read configuration '" + file.getFile().getPath() + "'.", e);
                return;
            }

            if (document == null)
            {
                log.warn(String.format("File '%s' does not appear to contain any data.", file.getFile().getPath()));
                return;
            }

            if (!JiraConfig.isJiraXml(document))
            {
                log.warn(String.format("File '%s' does not appear to contain JIRA XML.", file.getFile().getPath()));
                return;
            }

            CheckOptions options = CheckOptionsUtils.parseOptions(document);
            JiraConfig config = new JiraConfig(document, file.getFile());
            for (ConfigurationCheck check : configurationChecks)
            {
                check.fixConfiguration(config, options);
            }

            if (config.save())
            {
                log.info(String.format("File %s has been updated.", file.getFile().getAbsoluteFile()));
                if (!createBackups || saveBackup(file.getFile()))
                {
                    file.writeFile(config.getDocument());
                }
            }
            else
            {
                log.debug(String.format("File %s is good.", config.getFile().getAbsolutePath()));
            }
        }

        public void visitConfigError(File file, ConfigFile.ConfigFileException e)
        {
            log.error("Unable to read configuration '" + file.getPath() + "'.", e);
        }

        private boolean saveBackup(File file)
        {
            final File backup = new File(file.getParent(), file.getName() + ".bak");

            try
            {
                FileUtils.copyFile(file, backup);
                return true;
            }
            catch (IOException e)
            {
                log.error(String.format("Unable to create backup of file '%s' at '%s': %s.", file.getPath(), backup.getPath(), e.getMessage()), e);
                return false;
            }
        }
    }

    public static void main(String[] args)
    {
        ConfigFixerUpperer fixer = new ConfigFixerUpperer();
        fixer.setRoot(ConfigurationDefaults.getDefaultXmlDataLocation());
        fixer.setConfigurationChecks(ConfigurationDefaults.createDefaultConfigurationChecks());
        fixer.setExcludes(ConfigurationDefaults.getDefaultExcludedFilters());
        fixer.setCreateBackups(false);

        fixer.fix();
    }
}
