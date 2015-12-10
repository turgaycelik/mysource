package com.atlassian.jira.config;

import java.util.List;
import java.util.Map;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.resolution.Resolution;
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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v5.0
 */
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultResolutionManager
{
    private DefaultResolutionManager resolutionManager;
    private ConstantsManager constantsManager;
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
        resolutionManager = new DefaultResolutionManager(constantsManager, issueIndexManager, ofBizDelegator, applicationProperties, factory, clusterLockService)
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
        resolutionManager.start();
    }

    @Test
    public void testCreateResolution() throws Exception
    {
        MockGenericValue resFixedGV = new MockGenericValue("Resolution", 1L);
        resFixedGV.set("name", "Fixed");
        resFixedGV.set("sequence", Long.valueOf(1));

        MockGenericValue notFixedGV = new MockGenericValue("Resolution", 2L);
        notFixedGV.set("name", "Not Fixed");
        notFixedGV.set("sequence", Long.valueOf(2));

        MockGenericValue myResGV = new MockGenericValue("Resolution", 3L);
        myResGV.set("name", "My Resolution");
        myResGV.set("description", "My Description");
        myResGV.set("sequence", Long.valueOf(3));

        Resolution resolutionFixed = factory.createResolution(resFixedGV);
        Resolution resolutionNotFixed = factory.createResolution(notFixedGV);
        when(constantsManager.getResolutionObjects()).thenReturn(Lists.newArrayList(resolutionFixed, resolutionNotFixed));

        when(ofBizDelegator.createValue(eq(ConstantsManager.RESOLUTION_CONSTANT_TYPE), argThat(new ResolutionFieldsdArgumentMatcher("10", "My Resolution", "My Description")))).thenReturn(myResGV);

        Resolution resolution = resolutionManager.createResolution("My Resolution", "My Description");
        assertThat(resolution.getName(), is("My Resolution"));
        assertThat(resolution.getDescription(), is("My Description"));
    }

    @Test
    public void testCreateResolutionDuplicateName() throws Exception
    {
        MockGenericValue resFixedGV = new MockGenericValue("Resolution", 1L);
        resFixedGV.set("name", "Fixed");
        resFixedGV.set("sequence", Long.valueOf(1));

        MockGenericValue notFixedGV = new MockGenericValue("Resolution", 2L);
        notFixedGV.set("name", "Not Fixed");
        notFixedGV.set("sequence", Long.valueOf(2));

        MockGenericValue myResGV = new MockGenericValue("Resolution", 3L);
        myResGV.set("name", "nOt FIxEd ");
        myResGV.set("description", "My Description");
        myResGV.set("sequence", Long.valueOf(3));

        Resolution resolutionFixed = factory.createResolution(resFixedGV);
        Resolution resolutionNotFixed = factory.createResolution(notFixedGV);
        when(constantsManager.getResolutionObjects()).thenReturn(Lists.newArrayList(resolutionFixed, resolutionNotFixed));

        try
        {
            resolutionManager.createResolution("nOt FIxEd ", "My Description");
            fail("Expected exception because resolution with name Not Fixed exists already!");
        }
        catch (IllegalStateException ex)
        {
            assertThat(ex.getMessage(), is("A resolution with the name 'nOt FIxEd ' already exists."));
        }
    }

    @Test
    public void testEditResolution() throws Exception
    {
        MockGenericValue resFixedGV = new MockGenericValue("Resolution", 1L);
        resFixedGV.set("name", "Fixed");
        resFixedGV.set("sequence", Long.valueOf(1));

        MockGenericValue notFixedGV = new MockGenericValue("Resolution", 2L);
        notFixedGV.set("name", "Not Fixed");
        notFixedGV.set("sequence", Long.valueOf(2));

        MockGenericValue myResGV = new MockGenericValue("Resolution", 3L)
        {
            @Override
            public void store()
            {
            }
        };
        myResGV.set("name", "nOt FIxEd ");
        myResGV.set("description", "My Description");
        myResGV.set("sequence", Long.valueOf(3));

        Resolution resolutionFixed = factory.createResolution(resFixedGV);
        Resolution resolutionNotFixed = factory.createResolution(notFixedGV);
        Resolution resolutionToEdit = factory.createResolution(myResGV);
        when(constantsManager.getResolutionObjects()).thenReturn(Lists.newArrayList(resolutionToEdit, resolutionFixed, resolutionNotFixed));

        resolutionManager.editResolution(resolutionToEdit, "nOt FIxEd ", "Bob");
        assertThat(resolutionToEdit.getName(), is("nOt FIxEd "));
        assertThat(resolutionToEdit.getDescription(), is("Bob"));
    }

    @Test
    public void testEditResolutionNameExists() throws Exception
    {
        MockGenericValue resFixedGV = new MockGenericValue("Resolution", 1L);
        resFixedGV.set("name", "Fixed");
        resFixedGV.set("sequence", Long.valueOf(1));

        MockGenericValue notFixedGV = new MockGenericValue("Resolution", 2L);
        notFixedGV.set("name", "Not Fixed");
        notFixedGV.set("sequence", Long.valueOf(2));

        MockGenericValue myResGV = new MockGenericValue("Resolution", 3L)
        {
            @Override
            public void store()
            {
            }
        };
        myResGV.set("name", "nOt FIxEd ");
        myResGV.set("description", "My Description");
        myResGV.set("sequence", Long.valueOf(3));

        Resolution resolutionFixed = factory.createResolution(resFixedGV);
        Resolution resolutionNotFixed = factory.createResolution(notFixedGV);
        Resolution resolutionToEdit = factory.createResolution(myResGV);
        when(constantsManager.getResolutionObjects()).thenReturn(Lists.newArrayList(resolutionToEdit, resolutionFixed, resolutionNotFixed));

        try
        {
            resolutionManager.editResolution(resolutionToEdit, "Not Fixed", "Bob");
            fail("Expected error: A resolution with the name 'Not Fixed' exists already.");
        }
        catch (IllegalArgumentException ex)
        {
            assertThat(ex.getMessage(), is("Cannot rename resolution. A resolution with the name 'Not Fixed' exists already."));
        }
    }

    @Test
    public void testRemoveResolution() throws Exception
    {
        final GenericValue relatedIssueGv = new MockGenericValue("Issue", 1000L);

        final BooleanHolder removedOne = new BooleanHolder();
        MockGenericValue resFixedGV = new MockGenericValue("Resolution", 1L)
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
        resFixedGV.set("name", "Fixed");
        resFixedGV.set("sequence", Long.valueOf(1));

        final BooleanHolder removedTwo = new BooleanHolder();
        MockGenericValue notFixedGV = new MockGenericValue("Resolution", 2L)
        {
            @Override
            public void remove()
            {
                removedTwo.booleanValue = true;
            }
        };
        notFixedGV.set("name", "Not Fixed");
        notFixedGV.set("sequence", Long.valueOf(2));

        Resolution resFixed = factory.createResolution(resFixedGV);
        Resolution resNotFixed = factory.createResolution(notFixedGV);

        when(constantsManager.getResolutionObject("1")).thenReturn(resFixed);
        when(constantsManager.getResolutionObject("2")).thenReturn(resNotFixed);
        resolutionManager.removeResolution("1", "2");
        assertTrue(removedOne.booleanValue);
        assertFalse(removedTwo.booleanValue);
        assertThat(relatedIssueGv.getString(IssueFieldConstants.RESOLUTION), is("2"));
        verify(ofBizDelegator).storeAll(eq(Lists.newArrayList(relatedIssueGv)));
    }

    @Test
    public void testMoveResolutionUp() throws Exception
    {
        GenericValue resFixedGV = new MockGenericValue("Resolution", 1L);
        resFixedGV.set("name", "Fixed");
        resFixedGV.set("sequence", Long.valueOf(1));

        GenericValue notFixedGV = new MockGenericValue("Resolution", 2L);
        notFixedGV.set("name", "Not Fixed");
        notFixedGV.set("sequence", Long.valueOf(2));

        GenericValue willNotFix = new MockGenericValue("Resolution", 3L);
        willNotFix.set("name", "Will Not Fixed");
        willNotFix.set("sequence", Long.valueOf(3));

        Resolution resolutionFixed = factory.createResolution(resFixedGV);
        Resolution resolutionNotFixed = factory.createResolution(notFixedGV);
        Resolution resolutionWillNotFix = factory.createResolution(willNotFix);

        when(constantsManager.getResolutionObject("2")).thenReturn(resolutionNotFixed);
        when(constantsManager.getResolutionObjects()).thenReturn(Lists.newArrayList(resolutionFixed, resolutionNotFixed, resolutionWillNotFix));
        resolutionManager.moveResolutionUp("2");

        verify(ofBizDelegator).storeAll(eq(Lists.newArrayList(notFixedGV, resFixedGV, willNotFix)));
        verify(constantsManager).refreshResolutions();
    }

    @Test
    public void testMoveResolutionDown() throws Exception
    {
        GenericValue resFixedGV = new MockGenericValue("Resolution", 1L);
        resFixedGV.set("name", "Fixed");
        resFixedGV.set("sequence", Long.valueOf(1));

        GenericValue notFixedGV = new MockGenericValue("Resolution", 2L);
        notFixedGV.set("name", "Not Fixed");
        notFixedGV.set("sequence", Long.valueOf(2));

        GenericValue willNotFix = new MockGenericValue("Resolution", 3L);
        willNotFix.set("name", "Will Not Fixed");
        willNotFix.set("sequence", Long.valueOf(3));

        Resolution resolutionFixed = factory.createResolution(resFixedGV);
        Resolution resolutionNotFixed = factory.createResolution(notFixedGV);
        Resolution resolutionWillNotFix = factory.createResolution(willNotFix);

        when(constantsManager.getResolutionObject("2")).thenReturn(resolutionNotFixed);
        when(constantsManager.getResolutionObjects()).thenReturn(Lists.newArrayList(resolutionFixed, resolutionNotFixed, resolutionWillNotFix));
        resolutionManager.moveResolutionDown("2");

        verify(ofBizDelegator).storeAll(eq(Lists.newArrayList(resFixedGV, willNotFix, notFixedGV)));
        verify(constantsManager).refreshResolutions();
    }


    @Test
    public void testSetDefaultResolution() throws Exception
    {
        GenericValue resFixedGV = new MockGenericValue("Resolution", 1L);
        resFixedGV.set("name", "Fixed");
        resFixedGV.set("sequence", Long.valueOf(1));
        Resolution resolutionFixed = factory.createResolution(resFixedGV);
        when(constantsManager.getResolutionObject("1")).thenReturn(resolutionFixed);
        resolutionManager.setDefaultResolution("1");
        verify(applicationProperties).setString(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION, "1");
    }

    @Test
    public void testGetDefaultResolution() throws Exception
    {
        GenericValue resFixedGV = new MockGenericValue("Resolution", 1L);
        resFixedGV.set("name", "Fixed");
        resFixedGV.set("sequence", Long.valueOf(1));
        Resolution resolutionFixed = factory.createResolution(resFixedGV);

        when(applicationProperties.getString(APKeys.JIRA_CONSTANT_DEFAULT_RESOLUTION)).thenReturn("1");
        when(constantsManager.getResolutionObject("1")).thenReturn(resolutionFixed);
        Resolution defaultResolution = resolutionManager.getDefaultResolution();
        assertThat(defaultResolution.getId(), is("1"));
        assertThat(defaultResolution.getName(), is("Fixed"));
    }


    class ResolutionFieldsdArgumentMatcher extends ArgumentMatcher<Map<String, Object>>
    {
        final String id;
        private final String name;
        private final String descpription;

        ResolutionFieldsdArgumentMatcher(String id, String name, String descpription)
        {
            this.id = id;
            this.name = name;
            this.descpription = descpription;
        }

        public boolean matches(Object o)
        {
            Map<String, Object> gv = (Map<String, Object>) o;
            return id.equals(gv.get("id")) && name.equals(gv.get("name")) && descpription.equals(gv.get("description"));
        }
    }


}
