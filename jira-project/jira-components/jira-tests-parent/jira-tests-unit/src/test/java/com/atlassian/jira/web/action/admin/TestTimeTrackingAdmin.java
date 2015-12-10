/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import java.util.Locale;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.web.action.RedirectSanitiser;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.servlet.MockHttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import webwork.action.Action;
import webwork.action.ServletActionContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestTimeTrackingAdmin extends MockControllerTestCase
{
    private MockApplicationProperties applicationProperties = new MockApplicationProperties();
    TimeTrackingAdmin tta;
    private Mock mockFieldManager;
    private JiraDurationUtils mockJiraDurationUtils;
    private ReindexMessageManager reindexMessageManager;

    @Before
    public void setUp() throws Exception
    {
        mockFieldManager = new Mock(FieldManager.class);
        mockFieldManager.setStrict(true);

        applicationProperties.setText(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY, "24");
        applicationProperties.setText(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK, "7");
        mockJiraDurationUtils = new MockJiraDurationUtils();

        reindexMessageManager = mockController.getMock(ReindexMessageManager.class);

        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                .addMock(JiraAuthenticationContext.class,new MockAuthenticationContext(null))
                .addMock(RedirectSanitiser.class, new MockRedirectSanitiser())
        );

        replay();


        tta = new TimeTrackingAdmin(applicationProperties, (FieldManager) mockFieldManager.proxy(), mockJiraDurationUtils, null, null, reindexMessageManager, null);
    }

    @After
    public void tearDown() throws Exception
    {

        // Null the reference to free up memory
        tta = null;
    }

    @Test
    public void testGetsSets()
    {
        tta.setHoursPerDay("5");
        assertEquals("5", tta.getHoursPerDay());
        tta.setHoursPerDay("5.5");
        assertEquals("5.5", tta.getHoursPerDay());
        tta.setDaysPerWeek("6");
        assertEquals("6", tta.getDaysPerWeek());
        tta.setDaysPerWeek("6.5");
        assertEquals("6.5", tta.getDaysPerWeek());
        tta.setDefaultUnit("HOUR");
        assertEquals("HOUR", tta.getDefaultUnit());

        tta.setDefaultUnit("UNKNOWN");
        assertEquals("MINUTE", tta.getDefaultUnit());
        mockFieldManager.verify();
    }

    @Test
    public void testDoDefault2() throws Exception
    {
        applicationProperties.setString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY, "10");
        applicationProperties.setString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK, "4");

        tta = new TimeTrackingAdmin(applicationProperties, (FieldManager) mockFieldManager.proxy(), mockJiraDurationUtils, null, null, reindexMessageManager, null);
        assertEquals(Action.INPUT, tta.doDefault());
        assertEquals("10", tta.getHoursPerDay());
        assertEquals("4", tta.getDaysPerWeek());
        mockFieldManager.verify();
    }

    @Test
    public void testIsTimeTracking()
    {
        assertFalse(tta.isTimeTracking());

        applicationProperties.setOption(APKeys.JIRA_OPTION_TIMETRACKING, true);
        assertTrue(tta.isTimeTracking());
        mockFieldManager.verify();
    }

    @Test
    public void testDeactivate() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("TimeTrackingAdmin!default.jspa");
        mockFieldManager.expectVoid("refresh");
        applicationProperties.setOption(APKeys.JIRA_OPTION_TIMETRACKING, true);
        assertTrue(tta.isTimeTracking());
        tta.doDeactivate();
        assertFalse(tta.isTimeTracking());
        mockFieldManager.verify();
        response.verify();
        ServletActionContext.setResponse(null);
    }

    @Test
    public void testActivate() throws Exception
    {
        mockController.reset();
        reindexMessageManager.pushMessage(null, "admin.notifications.task.timetracking");
        expectLastCall();
        replay();

        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("TimeTrackingAdmin!default.jspa");
        mockFieldManager.expectVoid("refresh");
        tta.doActivate();
        assertTrue(tta.isTimeTracking());
        mockFieldManager.verify();
        response.verify();
        ServletActionContext.setResponse(null);
    }

    @Test
    public void testActivateWithValues() throws Exception
    {
        mockController.reset();
        reindexMessageManager.pushMessage(null, "admin.notifications.task.timetracking");
        expectLastCall();
        replay();

        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("TimeTrackingAdmin!default.jspa");
        mockFieldManager.expectVoid("refresh");
        tta.setHoursPerDay("7");
        tta.setDaysPerWeek("5");
        tta.doActivate();
        assertTrue(tta.isTimeTracking());
        assertEquals("7", applicationProperties.getString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY));
        assertEquals("5", applicationProperties.getString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK));
        mockFieldManager.verify();
        response.verify();
        ServletActionContext.setResponse(null);
    }

    @Test
    public void testInvalidFractionalHoursPerDay() throws Exception
    {
        mockController.reset();
        TimeTrackingAdmin subclassedTta = new TimeTrackingAdmin(applicationProperties, (FieldManager) mockFieldManager.proxy(), mockJiraDurationUtils, null, null, reindexMessageManager, null) {
            public String getText(String key) {
                return "Error description. Overridden for this test as will invoke an I18nHelper from JiraAuthenticationContext";
            }
        };
        replay();
        subclassedTta.setHoursPerDay("6.72");
        subclassedTta.setDaysPerWeek("5");
        assertEquals("error", subclassedTta.doActivate());
    }

    @Test
    public void testValidFractionalHoursPerDay() throws Exception
    {
        mockController.reset();
        reindexMessageManager.pushMessage(null, "admin.notifications.task.timetracking");
        expectLastCall();
        replay();
        mockFieldManager.expectVoid("refresh");
        tta.setHoursPerDay("8.5");
        tta.setDaysPerWeek("5");
        tta.doActivate();
        assertEquals("8.5", applicationProperties.getString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY));
        assertEquals("5", applicationProperties.getString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK));
    }

    private class MockJiraDurationUtils extends JiraDurationUtils
    {
        public MockJiraDurationUtils()
        {
            super(null, null, null, null, null, new MemoryCacheManager());
        }

        public void updateFormatters(ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext)
        {

        }

        public String getFormattedDuration(Long duration)
        {
            return "";
        }

        public String getFormattedDuration(Long duration, Locale locale)
        {
            return "";
        }
    }
}
