package com.atlassian.jira.config.database;

import com.atlassian.jira.util.RuntimeIOException;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Base implementation of DatabaseConfigurationLoader for loading Database Configuration from jira-home.
 *
 * @since v4.4
 */
public abstract class AbstractJiraHomeDatabaseConfigurationLoader implements DatabaseConfigurationLoader
{
    public static final String FILENAME_DBCONFIG = "dbconfig.xml";
    private static final String CONFIG_ROOT = "jira-database-config";

    @Override
    public boolean configExists()
    {
        try
        {

            return getConfigFile(false).exists();
        }
        catch (IllegalStateException e)
        {
            // getHome() contract states that IllegalStateException will be thrown if home is not set
            // in such case just say that config does not exist
            return false;
        }
    }

    @Override
    public DatabaseConfig loadDatabaseConfiguration()
    {
        DatabaseConfigHandler dbConfigHandler = new DatabaseConfigHandler();

        Reader fileReader = null;
        SAXReader xmlReader = new SAXReader();
        xmlReader.setStripWhitespaceText(true);
        try
        {
            fileReader = getReader();
            Document root = xmlReader.read(fileReader);
            return dbConfigHandler.parse(root.getRootElement());
        }
        catch (DocumentException de)
        {
            throw new RuntimeException("Error parsing database configuration file.", de);
        }
        catch (IOException ioe)
        {
            throw new RuntimeIOException("Error reading database configuration file.", ioe);
        }
        finally
        {
            IOUtils.closeQuietly(fileReader);
        }
    }

    @Override
    public void saveDatabaseConfiguration(DatabaseConfig config)
    {
        Document document = DocumentHelper.createDocument();
        Element rootElement = document.addElement(CONFIG_ROOT);

        DatabaseConfigHandler dbConfigHandler = new DatabaseConfigHandler();
        dbConfigHandler.writeTo(rootElement, config);
        Writer writer = null;
        try
        {
            writer = getWriter();
            XMLWriter xmlWriter = new XMLWriter(writer, OutputFormat.createPrettyPrint());
            xmlWriter.write(document);
        }
        catch (IOException ioe)
        {
            throw new RuntimeIOException("Error writing database configuration file.", ioe);
        }
        finally
        {
            IOUtils.closeQuietly(writer);
        }

    }

    Reader getReader() throws IOException
    {
        final File configFile = getConfigFile(false);
        logInfo("Reading database configuration from " + configFile.getCanonicalPath());
        return new FileReader(configFile);
    }

    Writer getWriter() throws IOException
    {
        final File configFile = getConfigFile(true);
        FileWriter writer = new FileWriter(configFile);
        logInfo("Storing database configuration in " + configFile.getCanonicalPath());
        return writer;
    }

    /**
     *
     * @param createHome
     * @return
     * @throws IllegalStateException if the JIRA home is not set.
     */
    private File getConfigFile(boolean createHome)
    {
        final File home = new File(getJiraHome());
        if (createHome && !home.exists())
        {
            if (!home.mkdirs())
            {
                throw new RuntimeException("Can't create home directory to write database config file.");
            }
        }
        return new File(home, FILENAME_DBCONFIG);
    }

    /**
     *
     * @return path to jira's home
     * @throws IllegalStateException if the JIRA home is not set.
     */
    protected abstract String getJiraHome();

    protected abstract void logInfo(String message);
}
