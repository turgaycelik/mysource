package com.atlassian.jira.functest.config;

import com.atlassian.jira.util.Function;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static java.lang.String.format;

/**
 * Provides a framework for finding and processing JIRA's XML files.
 */
public final class ConfigFileWalker
{
    private File root;
    private ArrayList<IOFileFilter> excludes = new ArrayList<IOFileFilter>();
    private FileFilter filter = null;
    private ConfigVisitor visitor = null;
    private Function<File, ConfigFile> configFileFactory = null;

    public ConfigFileWalker(File root, ConfigVisitor visitor)
    {
        this(root, visitor, DefaultConfigFileFactory.INSTANCE);
    }

    public ConfigFileWalker(File root, ConfigVisitor visitor, Function<File, ConfigFile> configFileFactory)
    {
        setRoot(root);
        setVisitor(visitor);
        setConfigFileFactory(configFileFactory);
    }

    public File getRoot()
    {
        return root;
    }

    public ConfigFileWalker setRoot(File root)
    {
        this.root = normalizeFile(root);
        return this;
    }

    public ConfigFileWalker setExcludes(Collection<IOFileFilter> excludes)
    {
        filter = null;
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

    public ConfigFileWalker addFileNameExclude(String fileName)
    {
        filter = null;
        excludes.add(new NameFileFilter(fileName));

        return this;
    }

    public ConfigVisitor getVisitor()
    {
        return visitor;
    }

    public ConfigFileWalker setVisitor(final ConfigVisitor visitor)
    {
        this.visitor = visitor;
        return this;
    }

    public Function<File, ConfigFile> getConfigFileFactory()
    {
        return configFileFactory;
    }

    public ConfigFileWalker setConfigFileFactory(final Function<File, ConfigFile> configFileFactory)
    {
        this.configFileFactory = configFileFactory;
        return this;
    }

    public void walk()
    {
        if (root == null)
        {
            throw new IllegalStateException("The root has not been set.");
        }
        if (!root.exists())
        {
            throw new IllegalStateException(format("Root '%s' does not exist", root.getAbsolutePath()));
        }
        if (visitor == null)
        {
            throw new IllegalStateException("The visitor has not been set.");
        }

        processFile(root);
    }

    private FileFilter getFilter()
    {
        if (filter == null)
        {
            //We only only want XML and ZIP files.
            IOFileFilter tmpFilter = new OrFileFilter(Arrays.asList(new WildcardFileFilter("*.xml", IOCase.INSENSITIVE),
                    new WildcardFileFilter("*.zip", IOCase.INSENSITIVE), FileFilterUtils.directoryFileFilter()));

            if (!excludes.isEmpty())
            {
                final OrFileFilter orFilter = new OrFileFilter(excludes);
                tmpFilter = FileFilterUtils.andFileFilter(FileFilterUtils.notFileFilter(orFilter), tmpFilter);
            }

            filter = FileFilterUtils.makeSVNAware(tmpFilter);
        }
        return filter;
    }

    private void processFile(final File file)
    {
        if (file.isFile())
        {
            processConfig(normalizeFile(file));
        }
        else
        {
            for (File sub : file.listFiles(getFilter()))
            {
                processFile(sub);
            }
        }
    }

    private void processConfig(final File file)
    {
        try
        {
            visitor.visitConfig(configFileFactory.get(file));
        }
        catch (ConfigFile.ConfigFileException e)
        {
            visitor.visitConfigError(file, e);
        }
    }

    private static File normalizeFile(File file)
    {
        if (file == null)
        {
            return file;
        }
        try
        {
            return file.getCanonicalFile();
        }
        catch (IOException e)
        {
            return file.getAbsoluteFile();
        }
    }

    public interface ConfigVisitor
    {
        void visitConfig(ConfigFile file);
        void visitConfigError(File file, ConfigFile.ConfigFileException e);
    }

    private static class DefaultConfigFileFactory implements Function<File, ConfigFile>
    {
        private static final DefaultConfigFileFactory INSTANCE = new DefaultConfigFileFactory();

        public ConfigFile get(final File input)
        {
            return ConfigFile.create(input);
        }
    }
}
