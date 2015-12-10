package com.atlassian.jira.config;

import java.util.List;
import java.util.Map;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import junit.framework.Assert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @since v5.0
 */
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultPriorityManager
{
    private ConstantsManager constantsManager;
    private DefaultPriorityManager priorityManager;
    private OfBizDelegator ofBizDelegator;
    private ApplicationProperties applicationProperties;
    private MockIssueConstantFactory factory;
    private ClusterLockService clusterLockService;

    @Before
    public void setUp()
    {
        constantsManager = Mockito.mock(ConstantsManager.class);
        IssueIndexManager issueIndexManager = Mockito.mock(IssueIndexManager.class);
        ofBizDelegator = Mockito.mock(OfBizDelegator.class);
        applicationProperties = Mockito.mock(ApplicationProperties.class);
        factory = new MockIssueConstantFactory();
        clusterLockService = new SimpleClusterLockService();
        priorityManager = new DefaultPriorityManager(constantsManager, ofBizDelegator, issueIndexManager, applicationProperties, factory, clusterLockService)
        {
            @Override
            protected String getNextStringId() throws GenericEntityException
            {
                return "10";
            }

            @Override
            protected void removePropertySet(GenericValue constantGv)
            {
                //DO NOTHING
            }
        };
        priorityManager.start();
    }

    @Test
    public void testCreatePriority() throws Exception
    {
        MockGenericValue prioBlockerGV = new MockGenericValue("Priority", 1L);
        prioBlockerGV.set("name", "Blocker");
        prioBlockerGV.set("sequence", Long.valueOf(1));

        MockGenericValue prioMinorGV = new MockGenericValue("Priority", 2L);
        prioMinorGV.set("name", "Minor");
        prioMinorGV.set("sequence", Long.valueOf(2));

        MockGenericValue myResGV = new MockGenericValue("Priority", 3L);
        myResGV.set("name", "Major");
        myResGV.set("description", "Major issue");
        myResGV.set("iconurl", "http://www.web.de");
        myResGV.set("statusColor", "#345345");
        myResGV.set("sequence", Long.valueOf(3));

        Priority priorityBlocker = factory.createPriority(prioBlockerGV);
        Priority priorityMinor = factory.createPriority(prioMinorGV);
        when(constantsManager.getPriorityObjects()).thenReturn(Lists.newArrayList(priorityBlocker, priorityMinor));

        when(ofBizDelegator.createValue(eq(ConstantsManager.PRIORITY_CONSTANT_TYPE), argThat(new PriorityFieldsdArgumentMatcher("10", "Major", "Major issue", "http://www.web.de", "#345345")))).thenReturn(myResGV);

        Priority priority = priorityManager.createPriority("Major", "Major issue", "http://www.web.de", "#345345");
        Assert.assertEquals("Major", priority.getName());
        Assert.assertEquals("Major issue", priority.getDescription());
        Assert.assertEquals("http://www.web.de", priority.getIconUrl());
        Assert.assertEquals("#345345", priority.getStatusColor());
    }

    @Test
    public void testEditPriority() throws Exception
    {
        MockGenericValue prioBlockerGV = new MockGenericValue("Priority", 1L);
        prioBlockerGV.set("name", "Blocker");
        prioBlockerGV.set("sequence", Long.valueOf(1));

        MockGenericValue prioMinorGV = new MockGenericValue("Priority", 2L);
        prioMinorGV.set("name", "Minor");
        prioMinorGV.set("sequence", Long.valueOf(2));

        Priority priorityBlocker = factory.createPriority(prioBlockerGV);
        Priority priorityMinor = factory.createPriority(prioMinorGV);

        MockGenericValue myPrioGV = new MockGenericValue("Priority", 3L)
        {
            @Override
            public void store()
            {
            }
        };
        myPrioGV.set("name", "Trivial");
        myPrioGV.set("description", "a trivial bug");
        myPrioGV.set("sequence", Long.valueOf(4));

        Priority priorityToEdit = factory.createPriority(myPrioGV);
        when(constantsManager.getPriorityObjects()).thenReturn(Lists.newArrayList(priorityToEdit, priorityBlocker, priorityMinor));

        priorityManager.editPriority(priorityToEdit, "Super trivial", "none", "http://ftd.de", "#FF00FF");

        Assert.assertEquals("Super trivial", priorityToEdit.getName());
        Assert.assertEquals("none", priorityToEdit.getDescription());
        Assert.assertEquals("http://ftd.de", priorityToEdit.getIconUrl());
        Assert.assertEquals("#FF00FF", priorityToEdit.getStatusColor());
    }

    @Test
    public void testRemovePriority() throws Exception
    {
        final GenericValue relatedIssueGv = new MockGenericValue("Issue", 1000L);

        final BooleanHolder removedOne = new BooleanHolder();
        MockGenericValue majorGV = new MockGenericValue("Priority", 1L)
        {
            @Override
            public void remove()
            {
                removedOne.booleanValue = true;
            }

            @Override
            public List<GenericValue> getRelated(String s) throws GenericEntityException
            {
                return Lists.newArrayList(relatedIssueGv);
            }
        };
        majorGV.set("name", "Fixed");
        majorGV.set("sequence", Long.valueOf(1));

        final BooleanHolder removedTwo = new BooleanHolder();
        MockGenericValue minorGV = new MockGenericValue("Priority", 2L)
        {
            @Override
            public void remove()
            {
                removedTwo.booleanValue = true;
            }
        };
        minorGV.set("name", "Minor");
        minorGV.set("sequence", Long.valueOf(2));

        Priority major = factory.createPriority(majorGV);
        Priority minor = factory.createPriority(minorGV);

        when(constantsManager.getPriorityObject("1")).thenReturn(major);
        when(constantsManager.getPriorityObject("2")).thenReturn(minor);
        priorityManager.removePriority("1", "2");
        assertTrue(removedOne.booleanValue);
        assertFalse(removedTwo.booleanValue);
        assertThat(relatedIssueGv.getString(IssueFieldConstants.PRIORITY), is("2"));
        verify(ofBizDelegator).storeAll(eq(Lists.newArrayList(relatedIssueGv)));
    }

    @Test
    public void testSetDefaultPriority() throws Exception
    {
        MockGenericValue majorGV = new MockGenericValue("Priority", 1L);
        majorGV.set("name", "Fixed");
        majorGV.set("sequence", Long.valueOf(1));
        Priority major = factory.createPriority(majorGV);

        when(constantsManager.getPriorityObject("1")).thenReturn(major);
        priorityManager.setDefaultPriority("1");
        verify(applicationProperties).setString(APKeys.JIRA_CONSTANT_DEFAULT_PRIORITY, "1");
    }

    @Test
    public void testGetDefaultPriority() throws Exception
    {
        MockGenericValue majorGV = new MockGenericValue("Priority", 1L);
        majorGV.set("name", "Fixed");
        majorGV.set("sequence", Long.valueOf(1));
        Priority major = factory.createPriority(majorGV);
        when(applicationProperties.getString(APKeys.JIRA_CONSTANT_DEFAULT_PRIORITY)).thenReturn("1");
        when(constantsManager.getPriorityObject("1")).thenReturn(major);
        Priority priority = priorityManager.getDefaultPriority();
        assertEquals("1", priority.getId());
        assertEquals("Fixed", priority.getName());
    }

    @Test
    public void testMovePriorityUp() throws Exception
    {
        GenericValue blockerPrioGV = new MockGenericValue("Priority", 1L);
        blockerPrioGV.set("name", "Blocker");
        blockerPrioGV.set("sequence", Long.valueOf(1));

        GenericValue criticalPrioGV = new MockGenericValue("Priority", 2L);
        criticalPrioGV.set("name", "Critical");
        criticalPrioGV.set("sequence", Long.valueOf(2));

        GenericValue minorPrioGV = new MockGenericValue("Priority", 3L);
        minorPrioGV.set("name", "Minor");
        minorPrioGV.set("sequence", Long.valueOf(3));

        Priority blockerPrio = factory.createPriority(blockerPrioGV);
        Priority criticalPrio = factory.createPriority(criticalPrioGV);
        Priority minorPrio = factory.createPriority(minorPrioGV);

        when(constantsManager.getPriorityObject("2")).thenReturn(criticalPrio);
        when(constantsManager.getPriorityObjects()).thenReturn(Lists.newArrayList(blockerPrio, criticalPrio, minorPrio));
        priorityManager.movePriorityUp("2");

        verify(ofBizDelegator).storeAll(eq(Lists.newArrayList(criticalPrioGV, blockerPrioGV, minorPrioGV)));
        verify(constantsManager).refreshPriorities();
    }

    @Test
    public void testMovePriorityDown() throws Exception
    {
        GenericValue blockerPrioGV = new MockGenericValue("Priority", 1L);
        blockerPrioGV.set("name", "Blocker");
        blockerPrioGV.set("sequence", Long.valueOf(1));

        GenericValue criticalPrioGV = new MockGenericValue("Priority", 2L);
        criticalPrioGV.set("name", "Critical");
        criticalPrioGV.set("sequence", Long.valueOf(2));

        GenericValue minorPrioGV = new MockGenericValue("Priority", 3L);
        minorPrioGV.set("name", "Minor");
        minorPrioGV.set("sequence", Long.valueOf(3));

        Priority blockerPrio = factory.createPriority(blockerPrioGV);
        Priority criticalPrio = factory.createPriority(criticalPrioGV);
        Priority minorPrio = factory.createPriority(minorPrioGV);

        when(constantsManager.getPriorityObject("2")).thenReturn(criticalPrio);
        when(constantsManager.getPriorityObjects()).thenReturn(Lists.newArrayList(blockerPrio, criticalPrio, minorPrio));
        priorityManager.movePriorityDown("2");

        verify(ofBizDelegator).storeAll(eq(Lists.newArrayList(blockerPrioGV, minorPrioGV, criticalPrioGV)));
        verify(constantsManager).refreshPriorities();
    }


    class PriorityFieldsdArgumentMatcher extends ArgumentMatcher<Map<String, Object>>
    {
        final String id;
        private final String name;
        private final String descpription;
        private final String iconUrl;
        private final String colour;

        PriorityFieldsdArgumentMatcher(String id, String name, String descpription, String iconUrl, String colour)
        {
            this.id = id;
            this.name = name;
            this.descpription = descpription;
            this.iconUrl = iconUrl;
            this.colour = colour;
        }

        public boolean matches(Object o)
        {
            Map<String, Object> gv = (Map<String, Object>) o;
            return id.equals(gv.get("id")) && name.equals(gv.get("name")) && descpription.equals(gv.get("description")) && iconUrl.equals(gv.get("iconurl")) && colour.equals(gv.get("statusColor"));
        }
    }


}
