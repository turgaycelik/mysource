package com.atlassian.jira.functest.config;

import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.MultiMap;
import com.atlassian.jira.util.collect.MultiMaps;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Text;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Object that can be used to check the state of some JIRA XML. The checks are actually delegated off to the configured
 * {@link ConfigurationCheck} instance.
 *
 * @since v4.0
 */
public class ConfigurationChecker
{
    private static final Logger log = Logger.getLogger(ConfigurationChecker.class);

    private File root = null;
    private boolean cache = true;
    private int version = 0;
    private List<ConfigurationCheck> configurationChecks = new ArrayList<ConfigurationCheck>();
    private List<IOFileFilter> excludes = new ArrayList<IOFileFilter>();

    public ConfigurationChecker(final File root, Collection<? extends ConfigurationCheck> checks)
    {
        setRoot(root);
        setConfigurationChecks(checks);
    }

    public File getRoot()
    {
        return root;
    }

    public void setRoot(final File root)
    {
        this.root = root;
    }

    public List<ConfigurationCheck> getConfigurationChecks()
    {
        return configurationChecks;
    }

    public void setConfigurationChecks(final Collection<? extends ConfigurationCheck> configurationChecks)
    {
        if (configurationChecks == null)
        {
            this.configurationChecks = new ArrayList<ConfigurationCheck>();
        }
        else
        {
            this.configurationChecks = new ArrayList<ConfigurationCheck>(configurationChecks);
        }
    }

    public List<IOFileFilter> getExcludes()
    {
        return excludes;
    }

    public void setExcludes(Collection<IOFileFilter> excludes)
    {
        if (excludes == null)
        {
            this.excludes = new ArrayList<IOFileFilter>();
        }
        else
        {
            this.excludes = new ArrayList<IOFileFilter>(excludes);
        }
    }

    public boolean isCache()
    {
        return cache;
    }

    public void setCache(boolean cache)
    {
        this.cache = cache;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public static ConfigurationChecker createDefaultChecker()
    {
        return createDefaultChecker(ConfigurationDefaults.getDefaultXmlDataLocation());
    }

    public static ConfigurationChecker createDefaultChecker(File root)
    {
        final ConfigurationChecker checker = new ConfigurationChecker(root, ConfigurationDefaults.createDefaultConfigurationChecks());
        checker.setExcludes(ConfigurationDefaults.getDefaultExcludedFilters());
        checker.setVersion(1);
        return checker;
    }

    public CheckResult check()
    {
        final Checksummer<File> checksummer = readCacheSummer();
        final Visitor visitor = new Visitor(checksummer);

        ConfigFileWalker walker = new ConfigFileWalker(getRoot(), visitor);
        walker.setExcludes(getExcludes());
        walker.walk();

        if (checksummer != null && checksummer.isModified())
        {
            writeCache(checksummer);
        }

        return visitor.getResult();
    }

    private Checksummer<File> readCacheSummer()
    {
        if (!cache)
        {
            return null;
        }

        Cache cache = readCache();
        if (cache == null || cache.getSummer() == null)
        {
            return createChecksum();
        }

        if (cache.getVersion() < getVersion())
        {
            return createChecksum();
        }

        List<Class<? extends ConfigurationCheck>> currentChecks = getCheckClasses();
        List<Class<? extends ConfigurationCheck>> cachedChecks = cache.getCheckers();
        if (currentChecks.size() != cachedChecks.size() || !currentChecks.containsAll(cachedChecks))
        {
            return createChecksum();
        }

        return cache.getSummer();
    }

    private static Checksummer<File> createChecksum()
    {
        return Checksummer.fileChecksummer("md5");
    }

    private Cache readCache()
    {
        File cacheFile = getCacheFile();
        if (cacheFile != null)
        {
            Cache cache = new Cache();
            cache.read(cacheFile);
            return cache;
        }
        else
        {
            return null;
        }
    }

    private void writeCache(Checksummer<File> summer)
    {
        if (!cache)
        {
            return;
        }

        File cacheFile = getCacheFile();
        if (cacheFile == null)
        {
            return;
        }

        Cache cache = new Cache();
        cache.setVersion(getVersion());
        cache.setCheckers(getCheckClasses());
        cache.setSummer(summer);
        cache.save(cacheFile);
    }

    private List<Class<? extends ConfigurationCheck>> getCheckClasses()
    {
        List<Class<? extends ConfigurationCheck>> checker = new ArrayList<Class<? extends ConfigurationCheck>>();
        for (ConfigurationCheck check : configurationChecks)
        {
            checker.add(check.getClass());
        }
        return checker;
    }

    private File getCacheFile()
    {
        File parent = getRoot();
        if (!parent.isDirectory())
        {
            return null;
        }
        else
        {
            return new File(parent, "check-cache.xml");
        }
    }

    private class Visitor implements ConfigFileWalker.ConfigVisitor
    {
        private final CheckResult result = new CheckResult();
        private final Checksummer<File> summer;

        private Visitor(Checksummer<File> summer)
        {
            this.summer = summer;
        }

        public void visitConfig(ConfigFile file)
        {
            if (summer != null && !summer.hasChanged(file.getFile()))
            {
                return;
            }

            final Document document;
            try
            {
                document = file.readConfig();
            }
            catch (ConfigFile.ConfigFileException e)
            {
                result.addError(file.getFile(), "Unable to read configuration: " + e.getMessage());
                return;
            }

            if (document == null)
            {
                result.addWarning(file.getFile(), "Does not appear to contain any data.");
            }

            if (!JiraConfig.isJiraXml(document))
            {
                result.addWarning(file.getFile(), "File does not appear to contain JIRA XML.");
                return;
            }

            CheckOptions options = CheckOptionsUtils.parseOptions(document);
            final JiraConfig config = new JiraConfig(document, file.getFile());
            boolean errors = false;
            for (ConfigurationCheck check : configurationChecks)
            {
                final ConfigurationCheck.Result checkResult = check.checkConfiguration(config, options);
                result.addErrors(file.getFile(), checkResult.getErrors());
                result.addWarnings(file.getFile(), checkResult.getWarnings());

                errors = errors || !checkResult.isGood();
            }

            if (summer != null)
            {
                if (errors)
                {
                    summer.remove(file.getFile());
                }
                else
                {
                    summer.update(file.getFile());
                }
            }
        }

        public void visitConfigError(File file, ConfigFile.ConfigFileException e)
        {
            result.addWarning(file, "Error occured while loading: " + e.getMessage());
            summer.remove(file);
            log.error("Unable to read configuration '" + file.getPath() + "'.", e);
        }

        private CheckResult getResult()
        {
            return result;
        }
    }

    public static class CheckResult
    {
        private final MultiMap<File, CheckMessage, List<CheckMessage>> errors = createMultiMap();
        private final MultiMap<File, CheckMessage, List<CheckMessage>> warnings = createMultiMap();

        private CheckResult()
        {
        }

        private void addWarning(File file, String message)
        {
            addWarning(file, new CheckMessage(message));
        }

        private void addWarning(File file, CheckMessage message)
        {
            warnings.putSingle(file, message);
        }

        private void addWarnings(File file, Collection<CheckMessage> messages)
        {
            for (CheckMessage message : messages)
            {
                addWarning(file, message);
            }
        }

        private void addErrors(File file, Collection<CheckMessage> errors)
        {
            for (CheckMessage error : errors)
            {
                addError(file, error);
            }
        }

        private void addError(File file, String message)
        {
            addError(file, new CheckMessage(message));
        }

        private void addError(File file, CheckMessage message)
        {
            errors.putSingle(file, message);
        }

        public Map<File, List<CheckMessage>> getErrors()
        {
            return Collections.unmodifiableMap(errors);
        }

        public Map<File, List<CheckMessage>> getWarnings()
        {
            return Collections.unmodifiableMap(warnings);
        }

        public boolean hasErrors()
        {
            return !errors.isEmpty();
        }

        public boolean hasWarnings()
        {
            return !warnings.isEmpty();
        }

        public String getFormattedMessage()
        {
            StringBuilder buffer = new StringBuilder();
            for (Map.Entry<File, List<CheckMessage>> entry : errors.entrySet())
            {
                buffer.append("\nFile '").append(entry.getKey().getPath()).append("' has errors:");
                for (CheckMessage message : entry.getValue())
                {
                    buffer.append("\n\t[ERROR] ").append(message.getFormattedMessage());
                }

                List<CheckMessage> warns = warnings.get(entry.getKey());
                if (warns != null)
                {
                    for (CheckMessage warn : warns)
                    {
                        buffer.append("\n\t[WARNING] ").append(warn.getFormattedMessage());
                    }
                }
            }

            for (Map.Entry<File, List<CheckMessage>> entry : warnings.entrySet())
            {
                if (errors.get(entry.getKey()) == null)
                {
                    buffer.append("\nFile '").append(entry.getKey().getPath()).append("' has warnings:");
                    for (CheckMessage message : entry.getValue())
                    {
                        buffer.append("\n\t[WARNING] ").append(message.getFormattedMessage());
                    }
                }
            }

            return buffer.toString();
        }

        private static MultiMap<File, CheckMessage, List<CheckMessage>> createMultiMap()
        {
            return MultiMaps.create(new LinkedHashMap<File, List<CheckMessage>>(), new Supplier<List<CheckMessage>>()
            {
                public List<CheckMessage> get()
                {
                    return new ArrayList<CheckMessage>();
                }
            });
        }
    }

    private static class Cache
    {
        private static final String ELEMENT_CACHE = "checker-cache";
        private static final String ATTRIB_VERSION = "version";
        private static final String ELEMENT_CHECKS = "checks";
        private static final String ELEMENT_CHECK = "check";
        private static final String ELEMENT_CHECKSUMS = "checksums";

        private int version;
        private List<Class<? extends ConfigurationCheck>> checkers;
        private Checksummer<File> summer;

        private Cache()
        {
        }

        private int getVersion()
        {
            return version;
        }

        private Cache setVersion(int version)
        {
            this.version = version;
            return this;
        }

        private List<Class<? extends ConfigurationCheck>> getCheckers()
        {
            return Collections.unmodifiableList(checkers);
        }

        private void setCheckers(Collection<Class<? extends ConfigurationCheck>> checkers)
        {
            this.checkers = new ArrayList<Class<? extends ConfigurationCheck>>(checkers == null ? Collections.<Class<? extends ConfigurationCheck>>emptyList() : checkers);
        }

        private Checksummer<File> getSummer()
        {
            return summer;
        }

        private void setSummer(Checksummer<File> summer)
        {
            this.summer = summer;
        }

        private void save(File file)
        {
            DocumentFactory factory = DocumentFactory.getInstance();
            final Document document = factory.createDocument();
            final Element rootElement = document.addElement(ELEMENT_CACHE);
            if (version >= 0)
            {
                rootElement.addAttribute(ATTRIB_VERSION, String.valueOf(version));
            }
            final Element checks = rootElement.addElement(ELEMENT_CHECKS);
            for (Class<? extends ConfigurationCheck> checker : checkers)
            {
                final Element check = checks.addElement(ELEMENT_CHECK);
                check.setText(checker.getName());
            }

            if (summer != null)
            {
                final Element element = rootElement.addElement(ELEMENT_CHECKSUMS);
                summer.write(element);
            }

            writeDocument(file, document);
        }

        private void read(File file)
        {
            version = -1;

            final Document document = readDocument(file);
            if (document != null)
            {
                final List<Class<? extends ConfigurationCheck>> tmpCheckers = new ArrayList<Class<? extends ConfigurationCheck>>();
                final Number number = document.numberValueOf("/checker-cache/@version");
                if (number != null && !Float.isNaN(number.floatValue()))
                {
                    version = number.intValue();
                }

                @SuppressWarnings({"unchecked"}) final List<Text> elements = document.selectNodes("/checker-cache/checks/check/text()");
                for (Text element : elements)
                {
                    String klazzName = element.getText();
                    if (StringUtils.isNotBlank(klazzName))
                    {
                        try
                        {
                            final Class<?> klazz = Class.forName(klazzName);
                            if (ConfigurationCheck.class.isAssignableFrom(klazz))
                            {
                                //This is safe because of the above check.
                                //noinspection unchecked
                                tmpCheckers.add((Class<? extends ConfigurationCheck>) klazz);
                            }
                        }
                        catch (ClassNotFoundException ignored)
                        {
                            //ingore and continue.
                        }
                    }
                }
                this.checkers = Collections.unmodifiableList(tmpCheckers);

                final Element checksums = document.getRootElement().element("checksums");
                if (checksums != null)
                {
                    this.summer = createChecksum();
                    summer.read(checksums);
                }
                else
                {
                    this.summer = null;
                }
            }
            else
            {
                this.checkers = Collections.emptyList();
            }
        }

        private void writeDocument(File file, Document document)
        {
            try
            {
                final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
                try
                {
                    XMLWriter xmlWriter = new XMLWriter(writer, OutputFormat.createPrettyPrint());
                    xmlWriter.write(document);
                    xmlWriter.close();
                }
                finally
                {
                    IOUtils.closeQuietly(writer);
                }
            }
            catch (IOException ignored)
            {
            }
        }

        private Document readDocument(File file)
        {
            try
            {
                final Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
                try
                {
                    SAXReader saxReader = new SAXReader();
                    return saxReader.read(reader);
                }
                finally
                {
                    IOUtils.closeQuietly(reader);
                }
            }
            catch (IOException e)
            {
                return null;
            }
            catch (DocumentException e)
            {
                return null;
            }
        }
    }
}
