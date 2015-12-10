package com.atlassian.jira.web.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Manage and allow to print out assets (javscript and css) which are included into Metal Pages
 *
 * @since v6.1
 */
public class MetalResourcesManager
{

    /**
     * Descriptive list of all files which are considered as static assets
     */
    public static Set<String> STATIC_ASSETS = ImmutableSet.of(
            "/static-assets/metal-all.css",
            "/static-assets/metal-all-ie.css",
            "/static-assets/metal-all-ie9.css",
            "/static-assets/metal-all.js",
            "/static-assets/metal-all-ie.js"
    );

    public static String getMetalResources(String contextPath)
    {
        StringWriter sw = new StringWriter();
        includeMetalResources(sw, contextPath);
        return sw.toString();
    }

    public static void includeMetalResources(Writer out, String contextPath)
    {

        MetalResourcesManager manager = new MetalResourcesManager(contextPath, out);
        includeMetalResources(manager);
    }

    static void includeMetalResources(MetalResourcesManager metalResourcesManager)
    {
        try
        {
            metalResourcesManager.writeStyle("metal-all.css")
                    .writeScript("metal-all.js")
                    .startIf("lt IE 9")
                    .writeStyle("metal-all-ie.css")
                    .writeScript("metal-all-ie.js")
                    .endIf()
                    .startIf("IE 9")
                    .writeStyle("metal-all-ie9.css")
                    .endIf();
        }
        catch (IOException e)
        {

        }
    }

    private final String contextPath;
    private final Writer out;

    public MetalResourcesManager(final String contextPath, final Writer out)
    {
        this.contextPath = contextPath;
        this.out = out;
    }

    public MetalResourcesManager writeStyle(String filename) throws IOException
    {
        out.write("<link type='text/css' rel='stylesheet' href='" + resourcePath(filename) + "' media='all'>");
        return this;
    }

    public MetalResourcesManager writeScript(String filename) throws IOException
    {
        out.write("<script src='" + resourcePath(filename) + "'></script>");
        return this;
    }

    public MetalResourcesManager startIf(String condition) throws IOException
    {
        out.write("<!--[if " + condition + "]>");
        return this;
    }

    public MetalResourcesManager endIf() throws IOException
    {
        out.write("<![endif]-->");
        return this;
    }

    private String resourcePath(final String filename)
    {
        return contextPath + "/static-assets/" + filename;
    }


}
