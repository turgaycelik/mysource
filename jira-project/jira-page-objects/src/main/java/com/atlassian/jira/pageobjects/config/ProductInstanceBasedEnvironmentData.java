package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.webtests.util.AbstractEnvironmentData;
import com.atlassian.pageobjects.ProductInstance;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The environment data is built up on the information provided by the product instance. Unknown information is
 * filled up with default values.
 */
public class ProductInstanceBasedEnvironmentData extends AbstractEnvironmentData
{
    public static final String EDITION = "standard";

    private final URL baseUrl;
    private final String contextPath;
    private final File xmlDataLocation;

    public ProductInstanceBasedEnvironmentData(@Nonnull final ProductInstance productInstance)
    {
        super(new Properties());

        notNull(productInstance);
        baseUrl = buildBaseUrl(productInstance);
        contextPath = productInstance.getContextPath();
        xmlDataLocation = new File("./xml");
    }

    @Nonnull
    private URL buildBaseUrl(@Nonnull final ProductInstance productInstance)
    {
        try
        {
            return new URL(productInstance.getBaseUrl());
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("Could not create product instance's base url", e);
        }
    }

    @Override
    protected String getEdition()
    {
        return EDITION;
    }

    @Override
    public String getContext()
    {
        return contextPath;
    }

    @Override
    public URL getBaseUrl()
    {
        return baseUrl;
    }

    @Override
    public File getXMLDataLocation()
    {
        return xmlDataLocation;
    }

    @Override
    public File getWorkingDirectory()
    {
        File file = new File(System.getProperty("java.io.tmpdir"), "jira_autotest");
        try
        {
            return file.getCanonicalFile();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not create JIRA home dir " + file, e);
        }
    }

    @Override
    public File getJIRAHomeLocation()
    {
        return getWorkingDirectory();
    }
}
