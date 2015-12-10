package com.atlassian.jira.functest.config.xml;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertTrue;

/**
 * Provides file system location of the XML resources for func-tests by reading from classpath. This will only
 * work when the XMLs are stored in a class directory (as opposed to a JAR).
 *
 * @since v6.0
 */
public final class FuncTestsXmlResources
{

    private FuncTestsXmlResources()
    {
        throw new AssertionError("Don't instantiate me");
    }

    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("Don't clone me");
    }

    public static File getXmlLocation()
    {
        return getXmlLocation("xml/func_test_xml_resources");
    }

    /**
     * Get XML location for a given marker resource identified by <tt>markerResourceName</tt>
     *
     * @param markerResourceName name of the resource to use for identifying XML resource directory
     * @return file representing the XML resource directory
     */
    public static File getXmlLocation(String markerResourceName)
    {
        try
        {
            URL markerUrl = FuncTestsXmlResources.class.getClassLoader().getResource(markerResourceName);
            File markerFile = new File(markerUrl.toURI());
            assertTrue(markerFile.exists());
            return markerFile.getParentFile();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to find func test XML location", e);
        }
    }


}
