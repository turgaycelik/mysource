package com.atlassian.jira.webtests.cargo;

import com.atlassian.cargotestrunner.serverinformation.ServerInformation;
import com.atlassian.jira.webtests.util.AbstractEnvironmentData;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * This guy gets its properties from whatever is specified as the -Djira.functest.containerproperties
 * For instance, for bamboo builds this is the 'bamboo.containers.properties' file. Gotta love this stuff.
 */
public class CargoEnvironmentData extends AbstractEnvironmentData
{
    private static final String DEFAULT_EDITION = "standard";
    private static final String DEFAULT_XML_DATA_LOCATION = "../jira-func-tests/xml";

    private final ServerInformation serverInformation;
    private final String context;
    private final File xmlDataLocation;
    private final String edition;

    public CargoEnvironmentData(ServerInformation serverInformation, String context, String containerId, Properties properties)
    {
        super(properties);
        this.serverInformation = serverInformation;
        this.context = context;
        this.edition = properties.getProperty("cargo." + containerId + ".jira.edition" , DEFAULT_EDITION);
        File unresolvedFileLocation = new File(properties.getProperty("jira.xml.data.location", DEFAULT_XML_DATA_LOCATION));
        try
        {
            this.xmlDataLocation = unresolvedFileLocation.getCanonicalFile();
        }
        catch (IOException e)
        {
            throw new RuntimeException("IOException trying to resolve file " + unresolvedFileLocation);
        }
    }

    public String getContext()
    {
        return context;
    }

    public URL getBaseUrl()
    {
        try
        {
            return new URL("http", "localhost", serverInformation.getHttpPort(), context);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public File getXMLDataLocation()
    {
        return xmlDataLocation;
    }

    public File getWorkingDirectory()
    {
        File file = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + serverInformation.getContainerId() +
                "_" + serverInformation.getHttpPort() + "_jira_autotest");
        try
        {
            return file.getCanonicalFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not create JIRA home dir " + file);
        }
    }

    public File getJIRAHomeLocation()
    {
        return getWorkingDirectory();
    }

    public String getEdition()
    {
        return edition;
    }
}
