package com.atlassian.jira.webtests.cargo;

import com.atlassian.cargotestrunner.testrunner.Junit4TestRunner;
import com.atlassian.cargotestrunner.testrunner.TestRunner;
import com.atlassian.cargotestrunner.testrunner.TestRunnerConfig;
import com.atlassian.jira.testkit.client.log.FuncTestOut;
import com.atlassian.jira.functest.framework.SuiteListenerWrapper;
import com.atlassian.jira.functest.framework.TomcatShutdownListener;
import com.atlassian.jira.functest.framework.dump.TestInformationKit;
import com.atlassian.jira.webtests.AcceptanceTestHarness;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class CargoTestHarness extends TestSuite
{
    public static final String URL_PREFIX = "/atlassian-jira";
    public static final String DEFAULT_CONTAINERS_LOCATION = "src/etc/java/containers.properties";
    public static final String DEFAULT_WAR_LOCATION = "../../target/atlassian-jira";

    public static final String CONTAINER_LOCATION_PROPERTY = "jira.functest.containerproperties";
    public static final String WAR_LOCATION_PROPERTY = "jira.functest.warlocation";

    public static final String CONTAINERS_PROPERTY = "cargo.containers";
    public static final String DELETE_TEMP_DIR_PROPERTY = "cargo.container.deleteTempDirectory";

    public static final String JIRA_HOME_PROPERTY = "jira.functest.home";
    public static final String DEFAULT_JIRA_HOME = "target/jirahome/";


    public static Test suite() throws IOException
    {
        return suite(AcceptanceTestHarness.class);
    }

    public static Test suite(Class<? extends Test> suiteClass) throws IOException
    {
        FuncTestOut.out.println("_________________________");
        FuncTestOut.out.println("JIRA CargoTestHarness has started...");
        FuncTestOut.out.println("_________________________");
        printSystemPropertiesInEffect();
        printEnvironmentInEffect();

        final Properties containerProperties = getProperties();
        final File war = initWarFile();

        final Test testSuite = suite(suiteClass, war, containerProperties);
        int testCaseCount = testSuite.countTestCases();
        TestInformationKit.startTestSuite(testCaseCount);

        return new SuiteListenerWrapper(testSuite, new TomcatShutdownListener());
    }

    public static Runner cargoRunner(Runner testRunner) throws IOException, InitializationError
    {
        FuncTestOut.out.println("_________________________");
        FuncTestOut.out.println("JIRA CargoTestHarness has started...");
        FuncTestOut.out.println("_________________________");
        printSystemPropertiesInEffect();
        printEnvironmentInEffect();

        final Properties containerProperties = getProperties();
        final File war = initWarFile();

        final Runner cargoRunner = Junit4TestRunner.cargoRunner(testRunner, containerProperties,
                new JiraRunnerCallbackFactory(URL_PREFIX), war);
        TestInformationKit.startTestSuite(cargoRunner.testCount());
        return cargoRunner;
    }

    private static void printSystemPropertiesInEffect()
    {
        final Properties properties = System.getProperties();
        List<String> keys = new ArrayList<String>();
        for (Object key : properties.keySet())
        {
            keys.add((String) key);
        }
        Collections.sort(keys);

        FuncTestOut.out.println("The following (" + keys.size() + ") Java System properties are in effect");
        for (String key : keys)
        {
            String value = properties.getProperty(key);
            FuncTestOut.out.println("\t" + key + " : " + value);
        }
        FuncTestOut.out.println("_________________________");
    }

    private static void printEnvironmentInEffect()
    {
        final Map<String, String> map = System.getenv();
        List<String> keys = new ArrayList<String>(map.keySet());
        Collections.sort(keys);

        FuncTestOut.out.println("The following (" + keys.size() + ") environment variables are in effect");
        for (String key : keys)
        {
            String value = map.get(key);
            FuncTestOut.out.println("\t" + key + " : " + value);
        }
        FuncTestOut.out.println("_________________________");
    }

    protected static File initWarFile()
            throws IOException
    {
        String warLocation = System.getProperty(WAR_LOCATION_PROPERTY, DEFAULT_WAR_LOCATION);
        FuncTestOut.out.println("Using war from '" + warLocation + "'");
        File war = new File(warLocation);
        if (!war.exists())
        {
            throw new RuntimeException("Could not find JIRA war at " + war.getCanonicalPath() + ".  Please ensure that the working directory is the functional test dir");
        }
        return war;
    }

    private static Collection<String> parseContainersList(String containersList)
    {
        StringTokenizer st = new StringTokenizer(containersList, ",", false);
        Collection<String> containers = new ArrayList<String>();
        while (st.hasMoreTokens())
        {
            String token = StringUtils.trimToNull(st.nextToken());
            if (token != null)
            {
                containers.add(token);
            }
        }
        return containers;
    }

    public static Test suite(Class<? extends Test> suiteClass, File warLocation, Properties properties) throws IOException
    {
        final Test test = getSuite(suiteClass);
        // now run this collection of TestCases in Cargo!
        return TestRunner.suite(Collections.singletonList(test), properties, new JIRACallbackFactory(URL_PREFIX),
                warLocation, new TestRunnerConfig(true, false, false));
    }

    private static Test getSuite(Class<? extends Test> suiteClass)
    {
        try
        {
            return (Test) suiteClass.getMethod("suite").invoke(null);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    protected static Properties getProperties() throws IOException
    {
        final String containerPropertiesLocation = System.getProperty(CONTAINER_LOCATION_PROPERTY, DEFAULT_CONTAINERS_LOCATION);
        FuncTestOut.out.println("Using container properties from '" + containerPropertiesLocation + "'");

        final File containerProperties = new File(containerPropertiesLocation);
        if (!containerProperties.exists())
        {
            throw new RuntimeException("Could not find containers.properties at " + containerProperties.getCanonicalPath() + ".  Please ensure that the working directory is the functional test dir");
        }
        return getProperties(containerProperties);
    }

    private static Properties getProperties(File containerProperties) throws IOException
    {
        final Properties properties = new Properties();
        final FileInputStream inStream = new FileInputStream(containerProperties);
        try
        {
            properties.load(inStream);
        }
        finally
        {
            IOUtils.closeQuietly(inStream);
        }

        final Collection<String> containerIds = getContainerNames(properties);
        cleanTemp(properties, containerIds);
        addJiraHome(properties, containerIds);

        return properties;
    }

    private static void cleanTemp(final Properties properties, final Collection<String> containerIds)
            throws IOException
    {
        final boolean clean = Boolean.parseBoolean(properties.getProperty(DELETE_TEMP_DIR_PROPERTY));
        for (String containerId : containerIds)
        {
            final String httpPortPropertyName = "cargo." + containerId + ".port";
            File containerTempDirectory = new File(System.getProperty("java.io.tmpdir"),
                    containerId + "_" + properties.getProperty(httpPortPropertyName));
            if (containerTempDirectory.exists())
            {
                if (clean)
                {
                    FuncTestOut.out.println("Deleting temp directory " + containerTempDirectory);
                    boolean deleted = FileUtils.deleteQuietly(containerTempDirectory);
                    FuncTestOut.out.println("Temp Directory deleted: " + deleted);
                }
                else
                {
                    throw new IllegalStateException("File " + containerTempDirectory.getCanonicalPath() + " exists and property " + DELETE_TEMP_DIR_PROPERTY + " is not set.  Cargo will not be able to run.");
                }
            }
        }
    }

    private static void addJiraHome(Properties properties, Collection<String> containerIds)
    {
        final String home = properties.getProperty(JIRA_HOME_PROPERTY, DEFAULT_JIRA_HOME);
        final File homeFile = new File(home).getAbsoluteFile();

        for (String containerId : containerIds)
        {
            final String propertyName = "cargo." + containerId + ".jvmArgs";
            String vmArgs = properties.getProperty(propertyName, "");

            if (!vmArgs.contains("-Djava.home"))
            {
                vmArgs = vmArgs + " -Djira.home=\"" + homeFile.getPath() + "\"";
                properties.put(propertyName, vmArgs);
            }
        }
    }

    private static Collection<String> getContainerNames(Properties serverProperties)
    {
        final String containersList = serverProperties.getProperty(CONTAINERS_PROPERTY);
        final Collection<String> containerNames;
        if (containersList == null)
        {
            FuncTestOut.out.println("Empty list of containerNames.  Please set property '" + CONTAINERS_PROPERTY + "'");
            containerNames = Collections.emptyList();
        }
        else
        {
            containerNames = parseContainersList(containersList);
        }

        if (containerNames.isEmpty())
        {
            FuncTestOut.out.println("Empty list of containerNames.  Please set property '" + CONTAINERS_PROPERTY + "'");
        }
        return containerNames;
    }
}