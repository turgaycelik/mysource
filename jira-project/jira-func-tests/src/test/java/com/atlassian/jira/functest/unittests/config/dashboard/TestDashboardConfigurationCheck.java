package com.atlassian.jira.functest.unittests.config.dashboard;

import com.atlassian.jira.functest.config.CheckOptionsUtils;
import com.atlassian.jira.functest.config.CheckResultBuilder;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import com.atlassian.jira.functest.config.JiraConfig;
import com.atlassian.jira.functest.config.dashboard.ConfigDashboard;
import com.atlassian.jira.functest.config.dashboard.ConfigExternalGadget;
import com.atlassian.jira.functest.config.dashboard.ConfigGadget;
import com.atlassian.jira.functest.config.dashboard.DashboardConfigurationCheck;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Test of the {@link com.atlassian.jira.functest.config.dashboard.DashboardConfigurationCheck}.
 *
 * @since v4.2
 */
public class TestDashboardConfigurationCheck extends TestCase
{
    private static final String CHECKID_DASHBOARDS = "dashboards";
    private static final String CHECKID_GADGETS = "gadgets";
    private static final String CHECKID_EXTERNAL = "externalgadgets";
    private static final String CHECKID_ABSOLUTE = "absolutegadgets";
    private static final String ADMIN = "admin";

    public void testCheckConfigurationGoodConfig() throws Exception
    {
        ConfigDashboard system = new ConfigDashboard().setId(1000L);

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system));

        CheckResultBuilder expectedResult = new CheckResultBuilder();

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        ConfigurationCheck.Result actualResult = check.checkConfiguration(config, CheckOptionsUtils.allOptions());

        assertEquals(expectedResult.buildResult(), actualResult);
    }

    public void testCheckConfigurationDashboards() throws Exception
    {
        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        system.setGadgets(asList(new ConfigGadget().setId(102020L)));

        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");
        other.setGadgets(asList(new ConfigGadget().setId(4875783894L)));

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));

        CheckResultBuilder expectedResult = new CheckResultBuilder();
        expectedResult.error("Gadget on dashboard '<unknown>' (1000).", CHECKID_GADGETS);
        expectedResult.error("Non-system dashboard 'other' (10001) exists.", CHECKID_DASHBOARDS);

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        ConfigurationCheck.Result actualResult = check.checkConfiguration(config, CheckOptionsUtils.allOptions());

        assertEquals(expectedResult.buildResult(), actualResult);
    }

    public void testCheckConfigurationExternalGadgets() throws Exception
    {
        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        system.setGadgets(asList(new ConfigGadget().setId(102020L)));

        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");
        other.setGadgets(asList(new ConfigGadget().setId(4875783894L)));

        ConfigExternalGadget externalGadget = new ConfigExternalGadget().setGadgetXml("http://google.com/au").setId(848494L);
        ConfigExternalGadget externalGadget2 = new ConfigExternalGadget().setGadgetXml("http://google.com/nz").setId(6753874L);

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));
        config.setExternalGadgets(asList(externalGadget, externalGadget2));

        CheckResultBuilder expectedResult = new CheckResultBuilder();
        expectedResult.error("Gadget on dashboard '<unknown>' (1000).", CHECKID_GADGETS);
        expectedResult.error("Non-system dashboard 'other' (10001) exists.", CHECKID_DASHBOARDS);
        expectedResult.error("External gadget 'http://google.com/au' configured.", CHECKID_EXTERNAL);
        expectedResult.error("External gadget 'http://google.com/nz' configured.", CHECKID_EXTERNAL);

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        ConfigurationCheck.Result actualResult = check.checkConfiguration(config, CheckOptionsUtils.allOptions());

        assertEquals(expectedResult.buildResult(), actualResult);
    }

    public void testCheckConfigurationDashboardsDisableDashboardAndGadgetCheck() throws Exception
    {
        ConfigExternalGadget externalGadget = new ConfigExternalGadget().setGadgetXml("http://google.com/au").setId(848494L);

        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        system.setGadgets(asList(new ConfigGadget().setId(102020L),
                new ConfigGadget().setId(4875783894L).setGadgetXml(externalGadget.getGadgetXml().toUpperCase(Locale.ENGLISH))));

        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");
        other.setGadgets(asList(new ConfigGadget().setId(3737373L).setGadgetXml("notabsolure"),
                new ConfigGadget().setId(754873984L).setGadgetXml("httpS://www.google.com.au"),
                new ConfigGadget().setId(754873985L).setGadgetXml("HTTP://www.google.com.au")));

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));
        config.setExternalGadgets(asList(externalGadget));

        CheckResultBuilder expectedResult = new CheckResultBuilder();
        expectedResult.error("Gadget URL 'httpS://www.google.com.au' is absolute on dashboard 'other' (10001).", CHECKID_ABSOLUTE);
        expectedResult.error("Gadget URL 'HTTP://www.google.com.au' is absolute on dashboard 'other' (10001).", CHECKID_ABSOLUTE);
        expectedResult.error("External gadget 'http://google.com/au' configured.", CHECKID_EXTERNAL);

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        ConfigurationCheck.Result actualResult = check.checkConfiguration(config, CheckOptionsUtils.disabled(CHECKID_DASHBOARDS, CHECKID_GADGETS));

        assertEquals(expectedResult.buildResult(), actualResult);
    }

    public void testCheckConfigurationDashboardsDisableDashboardAndGadgetAndExternalCheck() throws Exception
    {
        ConfigExternalGadget externalGadget = new ConfigExternalGadget().setGadgetXml("http://google.com/au").setId(848494L);

        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        system.setGadgets(asList(new ConfigGadget().setId(102020L),
                new ConfigGadget().setId(4875783894L).setGadgetXml(externalGadget.getGadgetXml())));

        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");
        other.setGadgets(asList(new ConfigGadget().setId(3737373L).setGadgetXml("notabsolure"),
                new ConfigGadget().setId(754873984L).setGadgetXml("httpS://www.google.com.au"),
                new ConfigGadget().setId(754873985L).setGadgetXml("HTTP://www.google.com.au")));

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));
        config.setExternalGadgets(asList(externalGadget));

        CheckResultBuilder expectedResult = new CheckResultBuilder();
        expectedResult.error("Gadget URL 'httpS://www.google.com.au' is absolute on dashboard 'other' (10001).", CHECKID_ABSOLUTE);
        expectedResult.error("Gadget URL 'HTTP://www.google.com.au' is absolute on dashboard 'other' (10001).", CHECKID_ABSOLUTE);

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        ConfigurationCheck.Result actualResult = check.checkConfiguration(config, CheckOptionsUtils.disabled(CHECKID_DASHBOARDS, CHECKID_GADGETS, CHECKID_EXTERNAL));

        assertEquals(expectedResult.buildResult(), actualResult);
    }

    public void testCheckConfigurationDashboardsDisableDashboardCheck() throws Exception
    {
        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        system.setGadgets(asList(new ConfigGadget().setId(102020L)));

        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");
        other.setGadgets(asList(new ConfigGadget().setId(3737373L).setGadgetXml("notabsolure"),
                new ConfigGadget().setId(754873984L).setGadgetXml("httpS://www.google.com.au"),
                new ConfigGadget().setId(754873985L).setGadgetXml("HTTP://www.google.com.au")));

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));

        CheckResultBuilder expectedResult = new CheckResultBuilder();
        expectedResult.error("Gadget on dashboard '<unknown>' (1000).", CHECKID_GADGETS);
        expectedResult.error("Gadget on dashboard 'other' (10001).", CHECKID_GADGETS);

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        ConfigurationCheck.Result actualResult = check.checkConfiguration(config, CheckOptionsUtils.disabled(CHECKID_DASHBOARDS));

        assertEquals(expectedResult.buildResult(), actualResult);
    }

    public void testCheckConfigurationDashboardsDisableAllChecks() throws Exception
    {
        ConfigExternalGadget externalGadget = new ConfigExternalGadget().setGadgetXml("http://google.com/au").setId(848494L);

        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        system.setGadgets(asList(new ConfigGadget().setId(102020L),
                new ConfigGadget().setId(4875783894L).setGadgetXml(externalGadget.getGadgetXml())));

        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");
        other.setGadgets(asList(new ConfigGadget().setId(3737373L).setGadgetXml("notabsolure"),
                new ConfigGadget().setId(754873984L).setGadgetXml("httpS://www.google.com.au"),
                new ConfigGadget().setId(754873985L).setGadgetXml("HTTP://www.google.com.au")));

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));
        config.setExternalGadgets(asList(externalGadget));

        CheckResultBuilder expectedResult = new CheckResultBuilder();

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        ConfigurationCheck.Result actualResult = check.checkConfiguration(config, CheckOptionsUtils.noOptions());

        assertEquals(expectedResult.buildResult(), actualResult);
    }

    public void testFixConfigurationGoodConfig() throws Exception
    {
        ConfigDashboard system = new ConfigDashboard().setId(1000L);

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(new ConfigDashboard(system)));

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        check.fixConfiguration(config, CheckOptionsUtils.allOptions());

        assertEquals(asList(system), config.getDashboards());
    }

    public void testFixConfigurationDashboards() throws Exception
    {
        ConfigExternalGadget externalGadget = new ConfigExternalGadget().setGadgetXml("http://google.com/au").setId(848494L);
        ConfigExternalGadget externalGadget2 = new ConfigExternalGadget().setGadgetXml(null).setId(4783423L);

        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        system.setGadgets(asList(new ConfigGadget().setId(102020L),
                new ConfigGadget().setId(4875783894L).setGadgetXml(externalGadget.getGadgetXml())));

        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");
        other.setGadgets(asList(new ConfigGadget().setId(3737373L).setGadgetXml("notabsolure"),
                new ConfigGadget().setId(754873984L).setGadgetXml("httpS://www.google.com.au"),
                new ConfigGadget().setId(754873985L).setGadgetXml("HTTP://www.google.com.au")));

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));
        config.setExternalGadgets(asList(externalGadget, externalGadget2));

        List<ConfigDashboard> expectedDashboards = asList(new ConfigDashboard().setId(1000L));

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        check.fixConfiguration(config, CheckOptionsUtils.allOptions());

        assertEquals(expectedDashboards, config.getDashboards());
        assertTrue(config.getExternalGadgets().isEmpty());
    }

    public void testFixConfigurationDashboardsGadgetsDisabled() throws Exception
    {
        ConfigExternalGadget externalGadget = new ConfigExternalGadget().setGadgetXml("http://google.com/au").setId(848494L);

        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        system.setGadgets(asList(new ConfigGadget().setId(102020L),
                new ConfigGadget().setId(4875783894L).setGadgetXml(externalGadget.getGadgetXml())));

        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");
        other.setGadgets(asList(new ConfigGadget().setId(3737373L).setGadgetXml("notabsolure"),
                new ConfigGadget().setId(754873984L).setGadgetXml("httpS://www.google.com.au"),
                new ConfigGadget().setId(754873985L).setGadgetXml("HTTP://www.google.com.au")));

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));
        config.setExternalGadgets(asList(externalGadget));

        List<ConfigDashboard> expectedDashboards = asList(
                new ConfigDashboard().setId(1000L).setGadgets(asList(
                        new ConfigGadget().setId(102020L).setColumnNumber(0).setRowNumber(0)
                ))
        );

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        check.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_GADGETS));

        assertEquals(expectedDashboards, config.getDashboards());
        assertTrue(config.getExternalGadgets().isEmpty());
    }

    public void testFixConfigurationDashboardsDashboardDisabled() throws Exception
    {
        ConfigExternalGadget externalGadget = new ConfigExternalGadget().setGadgetXml("http://google.com/au").setId(848494L);

        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        system.setGadgets(asList(new ConfigGadget().setId(102020L),
                new ConfigGadget().setId(4875783894L).setGadgetXml(externalGadget.getGadgetXml())));

        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");
        other.setGadgets(asList(new ConfigGadget().setId(3737373L).setGadgetXml("notabsolure"),
                new ConfigGadget().setId(754873984L).setGadgetXml("httpS://www.google.com.au"),
                new ConfigGadget().setId(754873985L).setGadgetXml("HTTP://www.google.com.au")));

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));
        config.setExternalGadgets(asList(externalGadget));

        List<ConfigDashboard> expectedDashboards = asList(new ConfigDashboard().setId(1000L),
                new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other"));

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        check.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_DASHBOARDS));

        assertEquals(expectedDashboards, config.getDashboards());
        assertTrue(config.getExternalGadgets().isEmpty());
    }

    public void testFixConfigurationDashboardsDashboardAndGadgetsDisabled() throws Exception
    {
        ConfigExternalGadget externalGadget = new ConfigExternalGadget().setGadgetXml("http://google.com/au").setId(848494L);

        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        system.setGadgets(asList(new ConfigGadget().setId(102020L),
                new ConfigGadget().setId(4875783894L).setGadgetXml(externalGadget.getGadgetXml())));

        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");
        other.setGadgets(asList(new ConfigGadget().setId(3737373L).setGadgetXml("notabsolure"),
                new ConfigGadget().setId(754873984L).setGadgetXml("httpS://www.google.com.au"),
                new ConfigGadget().setId(754873985L).setGadgetXml("HTTP://www.google.com.au")));

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));
        config.setExternalGadgets(asList(externalGadget));

        List<ConfigDashboard> expectedDashboards = asList(
                new ConfigDashboard().setId(1000L).setGadgets(asList(
                        new ConfigGadget().setId(102020L).setColumnNumber(0).setRowNumber(0)
                )),
                new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other").setGadgets(asList(
                        new ConfigGadget().setId(3737373L).setGadgetXml("notabsolure").setColumnNumber(0).setRowNumber(0)
                ))
        );

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        check.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_DASHBOARDS, CHECKID_GADGETS));

        assertEquals(expectedDashboards, config.getDashboards());
        assertTrue(config.getExternalGadgets().isEmpty());
    }

    public void testFixConfigurationDashboardsDashboardAndGadgetsAndExternalDisabled() throws Exception
    {
        ConfigExternalGadget externalGadget = new ConfigExternalGadget().setGadgetXml("http://google.com/au").setId(848494L);

        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        system.setGadgets(asList(new ConfigGadget().setId(102020L),
                new ConfigGadget().setId(4875783894L).setGadgetXml(externalGadget.getGadgetXml())));

        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");
        other.setGadgets(asList(new ConfigGadget().setId(3737373L).setGadgetXml("notabsolure"),
                new ConfigGadget().setId(754873984L).setGadgetXml("httpS://www.google.com.au"),
                new ConfigGadget().setId(754873985L).setGadgetXml("HTTP://www.google.com.au")));

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));
        config.setExternalGadgets(asList(externalGadget));

        List<ConfigDashboard> expectedDashboards = asList(
                new ConfigDashboard(system),
                new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other").setGadgets(
                        asList(new ConfigGadget().setId(3737373L).setGadgetXml("notabsolure")
                            .setRowNumber(0).setColumnNumber(0))
                )
        );

        List<ConfigExternalGadget> expectedExternals = asList(new ConfigExternalGadget(externalGadget));

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        check.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_DASHBOARDS, CHECKID_GADGETS, CHECKID_EXTERNAL));

        assertEquals(expectedDashboards, config.getDashboards());
        assertEquals(expectedExternals, config.getExternalGadgets());
    }

    public void testFixConfigurationDashboardsDashboardAndGadgetsAndAbsoluteDisabled() throws Exception
    {
        ConfigExternalGadget externalGadget = new ConfigExternalGadget().setGadgetXml("http://google.com/au").setId(848494L);

        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        system.setGadgets(asList(new ConfigGadget().setId(102020L),
                new ConfigGadget().setId(4875783894L).setGadgetXml(externalGadget.getGadgetXml())));

        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");
        other.setGadgets(asList(new ConfigGadget().setId(3737373L).setGadgetXml("notabsolure"),
                new ConfigGadget().setId(754873984L).setGadgetXml("httpS://www.google.com.au"),
                new ConfigGadget().setId(754873985L).setGadgetXml("HTTP://www.google.com.au")));

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));
        config.setExternalGadgets(asList(externalGadget));

        List<ConfigDashboard> expectedDashboards = asList(
                new ConfigDashboard().setId(1000L).setGadgets(
                        asList(new ConfigGadget().setId(102020L).setRowNumber(0).setColumnNumber(0))
                ),
                new ConfigDashboard(other)
        );

        List<ConfigExternalGadget> expectedExternals = asList();

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        check.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_DASHBOARDS, CHECKID_GADGETS, CHECKID_ABSOLUTE));

        assertEquals(expectedDashboards, config.getDashboards());
        assertEquals(expectedExternals, config.getExternalGadgets());
    }


    public void testFixConfigurationDashboardsDashboardDisabledNoGadgets() throws Exception
    {
        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));

        List<ConfigDashboard> expectedDashboards = asList(new ConfigDashboard().setId(1000L),
                new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other"));

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        check.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_DASHBOARDS));

        assertEquals(expectedDashboards, config.getDashboards());
        assertTrue(config.getExternalGadgets().isEmpty());
    }

    public void testFixConfigurationDashboardsAllDisabled() throws Exception
    {
        ConfigExternalGadget externalGadget = new ConfigExternalGadget().setGadgetXml("http://google.com/au").setId(848494L);

        ConfigDashboard system = new ConfigDashboard().setId(1000L);
        system.setGadgets(asList(new ConfigGadget().setId(102020L),
                new ConfigGadget().setId(4875783894L).setGadgetXml(externalGadget.getGadgetXml())));

        ConfigDashboard other = new ConfigDashboard().setId(10001L).setOwner(ADMIN).setName("other");
        other.setGadgets(asList(new ConfigGadget().setId(3737373L).setGadgetXml("notabsolure"),
                new ConfigGadget().setId(754873984L).setGadgetXml("httpS://www.google.com.au"),
                new ConfigGadget().setId(754873985L).setGadgetXml("HTTP://www.google.com.au")));

        JiraConfig config = new JiraConfig();
        config.setDashboards(asList(system, other));
        config.setExternalGadgets(asList(externalGadget));

        List<ConfigDashboard> expectedDashboards = asList(new ConfigDashboard(system), new ConfigDashboard(other));

        DashboardConfigurationCheck check = new DashboardConfigurationCheck();
        check.fixConfiguration(config, CheckOptionsUtils.noOptions());

        assertEquals(expectedDashboards, config.getDashboards());
    }

    private static <T> List<T> asList(T... elements)
    {
        return new ArrayList<T>(Arrays.asList(elements));
    }
}
