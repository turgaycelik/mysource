package com.atlassian.jira.rest.v2.admin.workflowscheme;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.MockDraftWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith (MockitoJUnitRunner.class)
public class TestRestWorkflowScheme
{
    @Mock
    private WorkflowSchemeBeanFactory factory;
    private SimpleRestWorkflowScheme workflowScheme;

    @Before
    public void setup()
    {
        workflowScheme = new SimpleRestWorkflowScheme(factory);
    }

    @Test
    public void testMergeWorkflowMappings()
    {
        final MockDraftWorkflowScheme scheme = new MockDraftWorkflowScheme();
        final MockDraftWorkflowScheme.MockBuilder builder = scheme.builder();

        WorkflowMappingBean bean = new WorkflowMappingBean("WF1", "IT1", "IT2", "IT3", "IT4", "IT5");
        workflowScheme.mergeWorkflowMappings(builder, bean);

        //Simple change.
        MockDraftWorkflowScheme expected = new MockDraftWorkflowScheme();
        expected.setMapping("IT1", "WF1");
        expected.setMapping("IT2", "WF1");
        expected.setMapping("IT3", "WF1");
        expected.setMapping("IT4", "WF1");
        expected.setMapping("IT5", "WF1");

        assertEquals(expected, builder.build());

        //Set the default.
        bean = new WorkflowMappingBean("WF1");
        bean.setDefaultMapping(true);
        workflowScheme.mergeWorkflowMappings(builder, bean);

        expected.setDefaultWorkflow("WF1");
        assertEquals(expected, builder.build());

        //Do nothing.
        bean = new WorkflowMappingBean("WFIgnore");
        workflowScheme.mergeWorkflowMappings(builder, bean);
        assertEquals(expected, builder.build());

        //Set some issue types by leave the default alone.
        bean = new WorkflowMappingBean("WFDefault", "IT6", "IT1", "IT2");
        workflowScheme.mergeWorkflowMappings(builder, bean);

        expected.setMapping("IT6", "WFDefault");
        expected.setMapping("IT1", "WFDefault");
        expected.setMapping("IT2", "WFDefault");
        assertEquals(expected, builder.build());

        //Remove the default.
        bean = new WorkflowMappingBean("WF1");
        bean.setDefaultMapping(false);
        workflowScheme.mergeWorkflowMappings(builder, bean);

        expected.removeDefault();
        assertEquals(expected, builder.build());

        //Set issue types and set the default.
        bean = new WorkflowMappingBean("WFDefault", "IT3", "IT4", "IT5");
        bean.setDefaultMapping(true);

        expected.clearMappings()
                .setMapping("IT3", "WFDefault")
                .setMapping("IT4", "WFDefault")
                .setMapping("IT5", "WFDefault")
                .setDefaultWorkflow("WFDefault");

        workflowScheme.mergeWorkflowMappings(builder, bean);
        assertEquals(expected, builder.build());

        //Set issue types leaving the default alone
        bean = new WorkflowMappingBean("WFDefault", "IT3");

        expected.clearMappings()
                .setMapping("IT3", "WFDefault")
                .setDefaultWorkflow("WFDefault");

        workflowScheme.mergeWorkflowMappings(builder, bean);
        assertEquals(expected, builder.build());

        //Set issue types leaving the default alone
        bean = new WorkflowMappingBean("WF1", "IT3");
        bean.setDefaultMapping(false);

        expected.clearMappings()
                .setMapping("IT3", "WF1")
                .setDefaultWorkflow("WFDefault");

        workflowScheme.mergeWorkflowMappings(builder, bean);
        assertEquals(expected, builder.build());
    }

    @Test
    public void getWorkflowMap()
    {
        final Map<String, String> actual = RestWorkflowScheme.getWorkflowMap(null, new WorkflowSchemeBean());
        assertTrue(actual.isEmpty());

        WorkflowSchemeBean bean = new WorkflowSchemeBean();
        bean.setDefaultWorkflow("default");
        assertEquals(MapBuilder.build(null, "default"), RestWorkflowScheme.getWorkflowMap(null, bean));

        final MockDraftWorkflowScheme scheme = new MockDraftWorkflowScheme();
        scheme.setDefaultWorkflow("WF2").setMapping("IT1", "WF1").setMapping("IT2", "WF2");

        Map<String, String> expected = Maps.newHashMap();
        expected.put(null, "WF2");
        expected.put("IT1", "WF1");
        expected.put("IT2", "WF2");

        bean = new WorkflowSchemeBean();
        assertEquals(expected, RestWorkflowScheme.getWorkflowMap(scheme, bean));

        bean = new WorkflowSchemeBean();
        bean.setDefaultWorkflow(null);
        expected.remove(null);
        assertEquals(expected, RestWorkflowScheme.getWorkflowMap(scheme, bean));

        bean = new WorkflowSchemeBean();
        bean.setDefaultWorkflow("WF3");
        expected.put(null, "WF3");
        assertEquals(expected, RestWorkflowScheme.getWorkflowMap(scheme, bean));

        bean = new WorkflowSchemeBean();
        bean.setIssueTypeMappings(MapBuilder.build("IT3", "WF3"));
        expected.clear();
        expected.put("IT3", "WF3");
        expected.put(null, "WF2");

        assertEquals(expected, RestWorkflowScheme.getWorkflowMap(scheme, bean));

        bean = new WorkflowSchemeBean();
        bean.setIssueTypeMappings(MapBuilder.build("IT3", "WF3"));
        bean.setDefaultWorkflow("WF3");
        expected.clear();
        expected.put("IT3", "WF3");
        expected.put(null, "WF3");

        assertEquals(expected, RestWorkflowScheme.getWorkflowMap(scheme, bean));

        bean = new WorkflowSchemeBean();
        bean.setIssueTypeMappings(MapBuilder.build("IT6", "WF4"));
        bean.setDefaultWorkflow(null);
        expected.clear();
        expected.put("IT6", "WF4");

        assertEquals(expected, RestWorkflowScheme.getWorkflowMap(scheme, bean));

        scheme.setDefaultWorkflow(null);
        bean = new WorkflowSchemeBean();
        bean.setIssueTypeMappings(MapBuilder.build("IT6", "WF4"));
        expected.clear();
        expected.put("IT6", "WF4");

        assertEquals(expected, RestWorkflowScheme.getWorkflowMap(scheme, bean));
    }

    @Test
    public void mergeIssueTypeMapping()
    {
        final MockDraftWorkflowScheme scheme = new MockDraftWorkflowScheme();
        final MockDraftWorkflowScheme.MockBuilder builder = scheme.builder();

        MockDraftWorkflowScheme expectedScheme = new MockDraftWorkflowScheme();

        scheme.setDefaultWorkflow("WFDefault").setMapping("IT1", "WF1");

        //Do nothing.
        IssueTypeMappingBean bean = new IssueTypeMappingBean();
        workflowScheme.mergeIssueTypeMapping(builder, bean);
        assertEquals(expectedScheme, builder.build());

        //Add a mapping.
        bean = new IssueTypeMappingBean();
        bean.setWorkflow("WF2");
        bean.setIssueType("IT2");
        workflowScheme.mergeIssueTypeMapping(builder, bean);
        expectedScheme.setMapping("IT2", "WF2");
        assertEquals(expectedScheme, builder.build());

        //Update a mapping.
        bean = new IssueTypeMappingBean();
        bean.setWorkflow("WF3");
        bean.setIssueType("IT2");
        workflowScheme.mergeIssueTypeMapping(builder, bean);
        expectedScheme.setMapping("IT2", "WF3");
        assertEquals(expectedScheme, builder.build());

        //Remove a mapping.
        bean = new IssueTypeMappingBean();
        bean.setWorkflow(null);
        bean.setIssueType("IT2");
        workflowScheme.mergeIssueTypeMapping(builder, bean);
        expectedScheme.removeMapping("IT2");
        assertEquals(expectedScheme, builder.build());
    }

    @Test
    public void setDefaultMapping()
    {
        MockDraftWorkflowScheme expectedScheme = new MockDraftWorkflowScheme();
        expectedScheme.setDefaultWorkflow("WF1");
        expectedScheme.setMapping("IT1", "WF1");

        final MockDraftWorkflowScheme.MockBuilder builder = expectedScheme.builder();

        //Noop
        DefaultBean bean = new DefaultBean();
        SimpleRestWorkflowScheme.setDefaultMapping(builder, bean);
        assertEquals(expectedScheme, builder.build());

        //set the default
        bean.setWorkflow("WF2");
        SimpleRestWorkflowScheme.setDefaultMapping(builder, bean);
        assertEquals(expectedScheme.setDefaultWorkflow("WF2"), builder.build());

        //remove the default
        bean.setWorkflow(null);
        SimpleRestWorkflowScheme.setDefaultMapping(builder, bean);
        assertEquals(expectedScheme.removeDefault(), builder.build());
    }


    private static class SimpleRestWorkflowScheme extends RestWorkflowScheme
    {
        protected SimpleRestWorkflowScheme(WorkflowSchemeBeanFactory factory)
        {
            super(factory);
        }

        @Override
        WorkflowScheme getScheme()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        WorkflowSchemeBean asBean()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        ServiceOutcome<Void> delete()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        ServiceOutcome<? extends RestWorkflowScheme> update(WorkflowSchemeBean bean)
        {
            throw new UnsupportedOperationException();
        }
    }
}
