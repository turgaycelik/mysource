package com.atlassian.jira.upgrade.tasks;

import java.util.Collection;

import com.atlassian.core.ofbiz.AbstractOFBizTestCase;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.config.properties.MockPropertiesManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 */
public class TestUpgradeTask_Build605 extends AbstractOFBizTestCase
{
    private MockPropertiesManager mockPropertiesManager;
    private UpgradeTask_Build605 upgradeTask;

    @Before
    public void setUp() throws Exception
    {
        // Keep old one cos I got no idea what might break if I don't
        mockPropertiesManager = new MockPropertiesManager();
        upgradeTask = new UpgradeTask_Build605(mockPropertiesManager);

        final MockComponentWorker componentAccessorWorker = new MockComponentWorker();
        componentAccessorWorker.registerMock(OfBizDelegator.class, OfBizFactory.getOfBizDelegator());
        ComponentAccessor.initialiseWorker(componentAccessorWorker);
    }

    @After
    public void tearDown() throws Exception
    {
        UtilsForTestSetup.deleteAllEntities();
    }

    @Test
    public void testRemoveListenerWhenExists() throws Exception
    {
        UtilsForTests.getTestEntity("ListenerConfig", EasyMap.build("name", "Issue Cache Listener", "clazz",
                UpgradeTask_Build605.ISSUE_CACHE_LISTENER_CLASS));
        UtilsForTests.getTestEntity("ListenerConfig", EasyMap.build("name", "Some Other Listener", "clazz",
                "foo.bar.Listener"));
        upgradeTask.doUpgrade(false);
        assertListenerDoesntExist(UpgradeTask_Build605.ISSUE_CACHE_LISTENER_CLASS);
        assertListenerExists("foo.bar.Listener");
    }

    @Test
    public void testUpgradeRemoveOptionsWhenSet() throws Exception
    {
        mockPropertiesManager.getPropertySet().setBoolean(UpgradeTask_Build605.JIRA_OPTION_CACHE_ISSUES, false);
        mockPropertiesManager.getPropertySet().setBoolean(UpgradeTask_Build605.JIRA_OPTION_CACHE_PERMISSIONS, false);
        mockPropertiesManager.getPropertySet().setBoolean(UpgradeTask_Build605.JIRA_OPTION_CACHE_PROJECTS, false);
        upgradeTask.doUpgrade(false);
        assertFalse(mockPropertiesManager.getPropertySet().exists(UpgradeTask_Build605.JIRA_OPTION_CACHE_ISSUES));
        assertFalse(mockPropertiesManager.getPropertySet().exists(UpgradeTask_Build605.JIRA_OPTION_CACHE_PERMISSIONS));
        assertFalse(mockPropertiesManager.getPropertySet().exists(UpgradeTask_Build605.JIRA_OPTION_CACHE_PROJECTS));
    }

    @Test
    public void testUpgradeRemoveOptionsWhenNotSet() throws Exception
    {
        // Make sure no exceptions are thrown if the options aren't set
        upgradeTask.doUpgrade(false);
        assertFalse(mockPropertiesManager.getPropertySet().exists(UpgradeTask_Build605.JIRA_OPTION_CACHE_ISSUES));
        assertFalse(mockPropertiesManager.getPropertySet().exists(UpgradeTask_Build605.JIRA_OPTION_CACHE_PERMISSIONS));
        assertFalse(mockPropertiesManager.getPropertySet().exists(UpgradeTask_Build605.JIRA_OPTION_CACHE_PROJECTS));
    }

    private void assertListenerDoesntExist(String clazz) throws Exception
    {
        final Collection<GenericValue> listenerConfigs = CoreFactory.getGenericDelegator().findAll("ListenerConfig");
        if (listenerConfigs != null)
        {
            for (GenericValue gv : listenerConfigs)
            {
                assertFalse("Listener " + clazz + " exists", clazz.equals(gv.getString("clazz")));
            }
        }

    }

    private void assertListenerExists(String clazz) throws Exception
    {
        final Collection<GenericValue> listenerConfigs = CoreFactory.getGenericDelegator().findAll("ListenerConfig");
        if (listenerConfigs != null)
        {
            for (GenericValue gv : listenerConfigs)
            {
                if (clazz.equals(gv.getString("clazz")))
                {
                    return;
                }
            }
        }
        fail("Listener " + clazz + " doesn't exist");
    }
}
