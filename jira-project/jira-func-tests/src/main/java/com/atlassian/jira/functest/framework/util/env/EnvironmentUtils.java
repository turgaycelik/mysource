package com.atlassian.jira.functest.framework.util.env;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.NavigationImpl;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.meterware.httpunit.WebTable;
import net.sourceforge.jwebunit.WebTester;
import org.xml.sax.SAXException;

/**
 * Utility methods for environment settings JIRA runs in.
 *
 * @since v3.13
 */
public class EnvironmentUtils extends AbstractFuncTestUtil
{
    private static final String INFO_OS = "Operating System";
    private static final String OS_WINDOWS = "WINDOWS";
    public static final float JDK_1_5_VERSION = 1.5f;

    private final Navigation navigation;

    public EnvironmentUtils(WebTester tester, JIRAEnvironmentData environmentData)
    {
        this(tester, environmentData, new NavigationImpl(tester, environmentData));
    }

    public EnvironmentUtils(WebTester tester, JIRAEnvironmentData environmentData, final Navigation navigation)
    {
        super(tester, environmentData, 2);
        this.navigation = navigation;
    }

    /**
     * Checks if this instance of JIRA is running on Windows OS
     *
     * @return true if on Windows, false otherwise
     */
    public boolean isOnWindows()
    {
        return isOnOperatingSystem(OS_WINDOWS);
    }

    /**
     * Checks is the given string is found in the operating system info. The check is done case insensitive.
     *
     * @param os operating system
     * @return true if OS matches, false otherwise
     * @throws RuntimeException if page does not contain Operating System information or cannot parse the page
     */
    private boolean isOnOperatingSystem(final String os)
    {
        navigation.login(FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD);
        navigation.gotoAdminSection("system_info");
        navigation.webSudoAuthenticateUsingLastPassword();
        try
        {
            WebTable systemInfoTable = tester.getDialog().getResponse().getTableWithID("system_info_table");
            for (int i = 0; i < systemInfoTable.getRowCount(); i++)
            {
                final String key = systemInfoTable.getCellAsText(i, 0);
                if (key != null && key.contains(INFO_OS))
                {
                    final String value = systemInfoTable.getCellAsText(i, 1);
                    if (value == null)
                    {
                        break;
                    }
                    else
                    {
                        return value.toUpperCase().contains(os.toUpperCase());
                    }
                }
            }
            throw new RuntimeException("System Info page does not contain '" + INFO_OS + "' information!");
        }
        catch (SAXException e)
        {
            throw new RuntimeException("Error parsing the System Infor page", e);
        }
    }

    /**
     * Retrieves the version of Java being used to run the JIRA instance from the System Info page. Method copied from
     * {@link com.atlassian.jira.webtests.JIRAWebTest#getJiraJavaVersion()}.
     *
     * @return the Java version as a float - e.g. Java 1.5 returns 1.5f (not 5.0f)
     */
    public float getJiraJavaVersion()
    {
        navigation.gotoAdminSection("system_info");
        String responseText = tester.getDialog().getResponseText();
        String jvmVersion;
        int idx = responseText.indexOf("<strong>Java Version</strong>");
        if (idx != -1)
        {
            String JVM__HTML_TABLE_TD = "<td class=\"cell-type-value\">";
            idx = responseText.indexOf(JVM__HTML_TABLE_TD, idx);
            if (idx != -1)
            {
                idx += JVM__HTML_TABLE_TD.length();
                jvmVersion = responseText.substring(idx, responseText.indexOf("</td>", idx));
                if (jvmVersion.contains("_"))
                {
                    jvmVersion = jvmVersion.substring(0, jvmVersion.indexOf("_"));
                }
                int firstDecimalIdx = jvmVersion.indexOf(".");
                int lastDecimalIdx = jvmVersion.lastIndexOf(".");
                if (firstDecimalIdx != lastDecimalIdx)
                {
                    jvmVersion = jvmVersion.substring(0, lastDecimalIdx);
                }
                try
                {
                    return Float.parseFloat(jvmVersion);
                }
                catch (NumberFormatException nfe)
                {
                    // oh well
                }
            }
        }
        return 0.0f;
    }

    /**
     * @return true if we're running a SUN JVM
     */
    public boolean isSunJVM()
    {
        //Java VM
        //BEA JRockit(R)

        //Java VM
        //IBM J9 VM

        //Java VM
        //Java HotSpot(TM) 64-Bit Server VM

        String jvmVendor = getSystemInfoProperty("Java VM");
        return jvmVendor != null && jvmVendor.contains("HotSpot");
    }

    /**
     * @return true if the Java version determined by {@link #getJiraJavaVersion()} precedes Java 1.5; false otherwise
     */
    public boolean isJavaBeforeJdk15()
    {
        return getJiraJavaVersion() < JDK_1_5_VERSION;
    }

    public boolean isOracle()
    {
        return isDatabaseEqualTo("oracle");
    }

    private boolean isDatabaseEqualTo(String database)
    {
        String databaseType = getSystemInfoProperty("Database type");
        return databaseType != null && databaseType.toLowerCase().contains(database.toLowerCase());
    }

    public String getSystemInfoProperty(String propertyLabel)
    {
        navigation.gotoAdminSection("system_info");
        WebTable table = tester.getDialog().getWebTableBySummaryOrId("system_info_table");
        for (int i = 0; i < table.getRowCount(); i++)
        {
            if (table.getTableCell(i, 0).asText().contains(propertyLabel))
            {
                return table.getTableCell(i, 1).asText();
            }
        }
        return null;
    }

    public boolean isJvmWithPermGen()
    {
        navigation.gotoAdminSection("system_info");
        String responseText = tester.getDialog().getResponseText();
        String jvmPermGen;
        int idx = responseText.indexOf("<strong>Total PermGen Memory</strong>");
        if (idx != -1)
        {
            String JVM__HTML_TABLE_TD = "<td class=\"cell-type-value\">";
            idx = responseText.indexOf(JVM__HTML_TABLE_TD, idx);
            if (idx != -1)
            {
                idx += JVM__HTML_TABLE_TD.length();
                jvmPermGen = responseText.substring(idx, responseText.indexOf("</td>", idx));
                if (jvmPermGen.contains(" "))
                {
                    jvmPermGen = jvmPermGen.substring(0, jvmPermGen.indexOf(" "));
                }
                try
                {
                    return Float.parseFloat(jvmPermGen) > 0;
                }
                catch (NumberFormatException nfe)
                {
                    // oh well
                }
            }
        }
        return false;
    }
}
