package com.atlassian.jira.plugin;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.system.VersionNumber;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.plugin.osgi.container.PackageScannerConfiguration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jira.util.collect.CollectionBuilder.newBuilder;
import static java.util.Collections.emptyList;

/**
 * Default package scanner configuration.  This controls what goes into the OSGI world and what does not.
 *
 * "One day your in...ze next day your outz!"  (Heidi Klum 2008)
 */
public class DefaultPackageScannerConfiguration implements PackageScannerConfiguration, Startable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPackageScannerConfiguration.class);

    private final List<String> packageIncludes = newBuilder(
            "com.atlassian.*",
            "io.atlassian.blobstore.client.api*",
            "com.google.common.*",
            "javax.*",
            "net.jcip.*",
            "org.jfree.*",
            "org.joda.*",
            "org.quartz",
            "org.quartz.*",
            "com.opensymphony.*",
            "org.apache.*",
            "org.ofbiz.*",
            "org.xml.*",
            "org.w3c.*",
            "webwork.*",
            "org.tuckey.web.filters.urlrewrite.*",
            "org.bouncycastle*",
            "org.dom4j*",
            "org.jdom*",
            "com.perforce*",
            "org.slf4j*",
            "org.codehaus.jackson*",
            "com.thoughtworks.xstream*"
    ).asList();

    private final List<String> packageExcludes = newBuilder(
            "com.atlassian.greenhopper",
            "com.springframework*",
            "org.springframework*",
            "com.sun.jersey.*",
            "javax.ws.*",
            "org.apache.tomcat.*",
            "org.apache.catalina.*",
            "org.apache.commons.logging*",
            "org.apache.jasper.*",
            "org.apache.coyote.*",
            "org.apache.naming*",
            "*..svn*"
    ).asList();

    private final List<String> jarIncludes = newBuilder("*.jar").asList();

    private final List<String> jarExcludes = emptyList();

    private final String hostVersion;

    private final Map<String, String> packageVersions;

    private static final String BLANK_OSGI_VERSION = "";

    /**
     * @param buildUtilsInfo The build information for this JIRA instance.
     * @since 4.0
     */
    public DefaultPackageScannerConfiguration(final BuildUtilsInfo buildUtilsInfo)
    {
        this.hostVersion = buildUtilsInfo.getVersion();

        // JIRA version number formatted as a valid OSGI version.
        final String jiraOsgiVersion = getAsOSGIVersion(hostVersion);

        packageVersions = MapBuilder.<String, String>newBuilder()
                .add("com.atlassian.jira*", jiraOsgiVersion)
                .add("com.atlassian.configurable*", jiraOsgiVersion)
                .add("com.atlassian.diff*", jiraOsgiVersion)
                .add("com.atlassian.query*", jiraOsgiVersion)
                .add("com.opensymphony*", jiraOsgiVersion)
                .add("org.apache.commons.dbcp*", "1.4.0")

                        // com.atlassian.crowd is part of Crowd Embedded and should live under the JIRA version. atlassian-extras-2.2.2.jar includes a com.atlassian.crowd package.
                .add("com.atlassian.crowd*", jiraOsgiVersion)

                        // com.atlassian.core.util has classes in atlassian-webwork1-1.1.jar and atlassian-core-4.5.5.jar
                .add("com.atlassian.core*", jiraOsgiVersion)

                        // com.atlassian.seraph.filter has classes in atlassian-trusted-apps-seraph-integration-2.1.jar and atlassian-seraph-2.1.1.jar
                .add("com.atlassian.seraph*", jiraOsgiVersion)

                        // javax.xml.namespace has classes in stax-api-1.0.1.jar and axis-jaxrpc-1.3.jar
                .add("javax.xml*", BLANK_OSGI_VERSION)

                        // Package Scanner found duplicates for package 'org.xml.sax' with different versions. Files: xmlrpc-2.0.jar and xml-apis-1.3.04.jar
                .add("org.xml*", BLANK_OSGI_VERSION)

                        // Package Scanner found duplicates for package 'org.w3c.dom.html' with different versions. Files: xercesImpl-2.9.1.jar and xml-apis-1.3.04.jar
                .add("org.w3c*", BLANK_OSGI_VERSION)

                        // commons-logging may be provided on the boot-class, we provide slf4j bridge, pretend its a known version
                .add("org.apache.commons.logging*", "1.1.1")

                // Package Scanner found duplicates for package 'org.apache.lucene.analysis.miscellaneous'
                // with different versions.
                //
                // Files: lucene-extras-3.3.0-atlassian-1.jar and lucene-analyzers-3.3.0.jar
                .add("org.apache.lucene*", buildUtilsInfo.getLuceneVersion())

                        // SAL is required by UAL
                .add("com.atlassian.sal.api.*", buildUtilsInfo.getSalVersion())
                .add("com.atlassian.soy.renderer", getAsOSGIVersion(buildUtilsInfo.getBuildProperty("soy.templates.version")))

                .add("com.google.common.*", buildUtilsInfo.getGuavaOsgiVersion())

                .toMap();
    }

    private static String getAsOSGIVersion(String version) {
        return new VersionNumber(version).getOSGIVersion();
    }

    @Override
    public void start() throws Exception
    {
        LOGGER.debug("Created package scanner configuration: {}", this);
    }

    public List<String> getJarIncludes()
    {
        return jarIncludes;
    }

    public List<String> getJarExcludes()
    {
        return jarExcludes;
    }

    public List<String> getPackageIncludes()
    {
        return packageIncludes;
    }

    public List<String> getPackageExcludes()
    {
        return packageExcludes;
    }

    public Map<String, String> getPackageVersions()
    {
        return packageVersions;
    }

    public String getCurrentHostVersion()
    {
        return hostVersion;
    }

    public ServletContext getServletContext()
    {
        // This is necessary to fool Spring MVC into thinking it's getting Servlet 2.5 when running in Tomcat 7.
        // In reality it's going to get Servlet 3.0, but the two APIs are binary compatible for clients so it's
        // all good.
        return new ServletContextWithSpecifiedVersion(ServletContextProvider.getServletContext(), 2, 5);
    }

    /**
     * Returns a human-readable representation of this DefaultPackageScannerConfiguration.
     *
     * @return a String
     */
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
