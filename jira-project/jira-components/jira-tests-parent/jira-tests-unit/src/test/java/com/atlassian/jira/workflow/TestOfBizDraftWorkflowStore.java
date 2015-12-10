package com.atlassian.jira.workflow;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestOfBizDraftWorkflowStore
{
    private static final String PARENT_WORKFLOW_NAME = "testWorkflow";
    private static final String PARENT_WORKFLOW_CONTENTS =
            "  <initial-actions>\n" +
                    "    <action id=\"1\" name=\"Create Issue\">\n" +
                    "      <results>\n" +
                    "        <unconditional-result old-status=\"Finished\" status=\"Open\" step=\"1\"/>\n" +
                    "      </results>\n" +
                    "    </action>\n" +
                    "  </initial-actions>\n" +
                    "  <steps>\n" +
                    "    <step id=\"1\" name=\"Open\">\n" +
                    "    </step>\n" +
                    "  </steps>\n";

    private static final String PARENT_WORKFLOW_DESCRIPTOR_XML =
            "<workflow>\n" + PARENT_WORKFLOW_CONTENTS + "</workflow>\n";

    public static final String WORKFLOW_XML_HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE workflow PUBLIC \"-//OpenSymphony Group//DTD OSWorkflow 2.7//EN\" \"http://www.opensymphony.com/osworkflow/workflow_2_7.dtd\">\n";

    private static final String PARENT_WORKFLOW_DESCRIPTOR = WORKFLOW_XML_HEADER + PARENT_WORKFLOW_DESCRIPTOR_XML;

    @Rule
    public final RuleChain mockito = MockitoMocksInContainer.forTest(this);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @AvailableInContainer (instantiateMe = true)
    private MockOfBizDelegator mockOfBizDelegator;

    @Mock
    private OfBizDelegator pureMockOfBizDelegator;

    private final ApplicationUser testUser = new MockApplicationUser("testuser");

    private OfBizDraftWorkflowStore ofBizDraftWorkflowStore;

    @Before
    public void setUpDraftWorkflowStore() throws Exception
    {
        ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore(mockOfBizDelegator);
    }

    @Test
    public void shouldGetDraftWhenExists()
    {
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, PARENT_WORKFLOW_DESCRIPTOR));

        final JiraWorkflow draftWorkflow = ofBizDraftWorkflowStore.getDraftWorkflow(PARENT_WORKFLOW_NAME);

        assertNotNull("Unable to fetch draft workflow from data store.", draftWorkflow);
        assertThat(draftWorkflow, instanceOf(JiraDraftWorkflow.class));

        final WorkflowDescriptor descriptor = draftWorkflow.getDescriptor();
        assertEqualsIgnoreWhitespace(PARENT_WORKFLOW_DESCRIPTOR_XML, descriptor.asXML());
    }

    @Test
    public void shouldReturnNullWhenThereIsNoDraftWorkflowToBeFound()
    {
        assertNull(ofBizDraftWorkflowStore.getDraftWorkflow(PARENT_WORKFLOW_NAME));
    }

    @Test
    public void shouldReturnNullWhenThereIsNoDraftWorkflowGvToBeFound()
    {
        assertNull(ofBizDraftWorkflowStore.getDraftWorkflowGV(PARENT_WORKFLOW_NAME));
    }

    @Test
    public void getDraftWorkflowGVWithManyDraftWorkflowsThrowsAnException()
    {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(containsString("There are more than one draft workflows associated with the workflow named 'testWorkflow'"));

        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, PARENT_WORKFLOW_DESCRIPTOR));
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, PARENT_WORKFLOW_DESCRIPTOR));

        ofBizDraftWorkflowStore.getDraftWorkflowGV(PARENT_WORKFLOW_NAME);
    }

    @Test
    public void getDraftWorkflowGVWithNullNameThrowsAnException()
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(containsString("Can not get a draft workflow for a parent workflow name of null."));

        ofBizDraftWorkflowStore.getDraftWorkflowGV(null);
    }

    @Test
    public void shouldAddAndPersistNewDraftWorkflow() throws Exception
    {
        final JiraWorkflow parentWorkflow = mock(JiraWorkflow.class);
        when(parentWorkflow.getName()).thenReturn(PARENT_WORKFLOW_NAME);
        when(parentWorkflow.getDescriptor()).thenReturn(WorkflowUtil.convertXMLtoWorkflowDescriptor(PARENT_WORKFLOW_DESCRIPTOR));


        final JiraWorkflow draftWorkflow = ofBizDraftWorkflowStore.createDraftWorkflow(testUser, parentWorkflow);

        assertNotNull(draftWorkflow);
        assertThat(draftWorkflow, instanceOf(JiraDraftWorkflow.class));

        final WorkflowDescriptor descriptor = draftWorkflow.getDescriptor();
        //timestamps are really hard to test. So, lets remove it.
        descriptor.getMetaAttributes().remove(JiraWorkflow.JIRA_META_UPDATED_DATE);
        //we're also removing the user attribute so that we can do a clean XML comparison below. But we at least
        //check that it's the right user :)
        assertEquals(testUser.getKey(), descriptor.getMetaAttributes().remove(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_KEY));
        assertEqualsIgnoreWhitespace(PARENT_WORKFLOW_DESCRIPTOR_XML, descriptor.asXML());
    }

    @Test
    public void createDraftWorkflowShouldThrowExceptionForNullUser()
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("You can not create a draft workflow with a null user.");

        ofBizDraftWorkflowStore.createDraftWorkflow(null, null);
    }

    @Test
    public void createDraftWorkflowShouldThrowExceptionForNullWorkflow() throws Exception
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can not create a draft workflow for a parent workflow name of null.");

        ofBizDraftWorkflowStore.createDraftWorkflow(testUser, null);
    }

    @Test
    public void findNonexistingParentWorkflowShouldFailQuietly()
    {
        pureMockTest();
        assertFalse(ofBizDraftWorkflowStore.draftWorkflowExistsForParent(PARENT_WORKFLOW_NAME));

        verify(pureMockOfBizDelegator).findByAnd(eq(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME), eq(ImmutableMap.of("parentname", PARENT_WORKFLOW_NAME)));
    }

    @Test
    public void shouldFindParentWorkflowIfExists()
    {
        pureMockTest();

        when(pureMockOfBizDelegator.findByAnd(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME, ImmutableMap.of("parentname", PARENT_WORKFLOW_NAME)))
                .thenReturn(ImmutableList.<GenericValue>of(
                        new MockGenericValue("DraftWorkflow", ImmutableMap.of(
                                "id", 10000L,
                                "parentid", PARENT_WORKFLOW_NAME,
                                "descriptor", PARENT_WORKFLOW_DESCRIPTOR))
                ));

        assertTrue("Did not find an existing parent workflow.", ofBizDraftWorkflowStore.draftWorkflowExistsForParent(PARENT_WORKFLOW_NAME));
    }

    @Test
    public void createDraftWorkflowNullNameShouldThrowException() throws Exception
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can not create a draft workflow for a parent workflow name of null");

        final JiraWorkflow jiraWorkflow = mock(JiraWorkflow.class);
        ofBizDraftWorkflowStore.createDraftWorkflow(testUser, jiraWorkflow);
    }

    @Test
    public void createDraftWorkflowNullDescriptorShouldThrowException() throws Exception
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can not create a draft workflow for a parent workflow with a null descriptor");

        final JiraWorkflow jiraWorkflow = mock(JiraWorkflow.class);
        when(jiraWorkflow.getName()).thenReturn("some name");
        ofBizDraftWorkflowStore.createDraftWorkflow(testUser, jiraWorkflow);
    }

    @Test
    public void shouldDeleteExistingDraftWorkflow()
    {
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, PARENT_WORKFLOW_DESCRIPTOR));

        assertNotNull(ofBizDraftWorkflowStore.getDraftWorkflow(PARENT_WORKFLOW_NAME));
        assertTrue("Draft workflow was not deleted.", ofBizDraftWorkflowStore.deleteDraftWorkflow(PARENT_WORKFLOW_NAME));
        assertNull("Draft workflow was not deleted.", ofBizDraftWorkflowStore.getDraftWorkflow(PARENT_WORKFLOW_NAME));
    }

    @Test
    public void deleteNonExistentDraftWorkflowShouldNotRaiseExceptions()
    {
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, PARENT_WORKFLOW_DESCRIPTOR));

        assertNotNull(ofBizDraftWorkflowStore.getDraftWorkflow(PARENT_WORKFLOW_NAME));
        assertFalse("Draft workflow should not have been deleted.", ofBizDraftWorkflowStore.deleteDraftWorkflow("someOtherWorkflow"));
        assertNotNull(ofBizDraftWorkflowStore.getDraftWorkflow(PARENT_WORKFLOW_NAME));
    }

    @Test
    public void deleteDraftWorflowWithNullParentShouldThrowException() throws Exception
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can not delete a draft workflow for a parent workflow name of null.");

        ofBizDraftWorkflowStore.deleteDraftWorkflow(null);
    }

    @Test
    public void updateDraftWorkflowWithNullUpdatedWorkflowShouldThrowException() throws Exception
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can not update a draft workflow with a null workflow/descriptor.");

        ofBizDraftWorkflowStore.updateDraftWorkflow(testUser, PARENT_WORKFLOW_NAME, null);
    }

    @Test
    public void updateDraftWorkflowWithNullUpdatedWorkflowDescriptorShouldThrowException() throws Exception
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can not update a draft workflow with a null workflow/descriptor.");

        ofBizDraftWorkflowStore.updateDraftWorkflow(testUser, PARENT_WORKFLOW_NAME, mock(JiraWorkflow.class));
    }

    @Test
    public void updateDraftWorkflowWithNonExistentParentShouldThrowException() throws Exception
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Unable to find a draft workflow associated with the parent workflow name '" + PARENT_WORKFLOW_NAME + "'");

        final JiraDraftWorkflow parentWorkflow = mock(JiraDraftWorkflow.class);
        when(parentWorkflow.getDescriptor()).thenReturn(WorkflowUtil.convertXMLtoWorkflowDescriptor(PARENT_WORKFLOW_DESCRIPTOR));
        when(parentWorkflow.isDraftWorkflow()).thenReturn(true);
        ofBizDraftWorkflowStore.updateDraftWorkflow(testUser, PARENT_WORKFLOW_NAME, parentWorkflow);
    }

    @Test
    public void shouldUpdateExistingDraftWorkflow() throws Exception
    {
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, "my bs descriptor"));

        final JiraDraftWorkflow parentWorkflow = mock(JiraDraftWorkflow.class);
        when(parentWorkflow.getDescriptor()).thenReturn(WorkflowUtil.convertXMLtoWorkflowDescriptor(PARENT_WORKFLOW_DESCRIPTOR));
        when(parentWorkflow.isDraftWorkflow()).thenReturn(true);

        final JiraWorkflow workflow = ofBizDraftWorkflowStore.updateDraftWorkflow(testUser, PARENT_WORKFLOW_NAME, parentWorkflow);
        //the timestamp is too hard to test.  I wont do it!
        workflow.getDescriptor().getMetaAttributes().remove(JiraWorkflow.JIRA_META_UPDATED_DATE);

        assertEquals(testUser.getKey(), workflow.getDescriptor().getMetaAttributes().get(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_KEY));
        workflow.getDescriptor().getMetaAttributes().remove(JiraDraftWorkflow.JIRA_META_UPDATE_AUTHOR_KEY);

        assertEqualsIgnoreWhitespace(PARENT_WORKFLOW_DESCRIPTOR_XML, workflow.getDescriptor().asXML());
    }

    @Test
    public void updateDraftWorkflowWithNormalWorkflowShouldThrowException() throws Exception
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Only draft workflows may be updated via this method.");

        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD, "my bs descriptor"));

        final JiraWorkflow parentWorkflow = mock(JiraWorkflow.class);
        when(parentWorkflow.getDescriptor()).thenReturn(WorkflowUtil.convertXMLtoWorkflowDescriptor(PARENT_WORKFLOW_DESCRIPTOR));

        ofBizDraftWorkflowStore.updateDraftWorkflow(testUser, PARENT_WORKFLOW_NAME, parentWorkflow);
    }

    @Test
    public void updateDraftWorkflowNullUsernameShouldThrowException()
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can not update a draft workflow with a null user.");

        ofBizDraftWorkflowStore.updateDraftWorkflow(null, null, null);
    }

    @Test
    public void updatingExistingDraftWorkflowWithoutAuditShouldWork() throws FactoryException
    {
        final String workflowString =
                "<workflow>\n"
                        + "<meta name=\"jira.update.author.key\">admin</meta>\n"
                        + "<meta name=\"jira.updated.date\">1256700109714</meta>\n"
                        + PARENT_WORKFLOW_CONTENTS
                        + "</workflow>\n";
        UtilsForTests.getTestEntity(OfBizDraftWorkflowStore.DRAFT_WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizDraftWorkflowStore.PARENTNAME_ENTITY_FIELD, PARENT_WORKFLOW_NAME,
                        OfBizDraftWorkflowStore.DESCRIPTOR_ENTITY_FIELD,
                        WORKFLOW_XML_HEADER + workflowString));


        final JiraDraftWorkflow parentWorkflow = mock(JiraDraftWorkflow.class);
        when(parentWorkflow.getDescriptor()).thenReturn(WorkflowUtil.convertXMLtoWorkflowDescriptor(PARENT_WORKFLOW_DESCRIPTOR));
        when(parentWorkflow.isDraftWorkflow()).thenReturn(true);

        final JiraWorkflow workflow = ofBizDraftWorkflowStore.updateDraftWorkflowWithoutAudit(PARENT_WORKFLOW_NAME, parentWorkflow);
        assertEquals("1256700109714", workflow.getDescriptor().getMetaAttributes().get(JiraWorkflow.JIRA_META_UPDATED_DATE));
        assertEquals("admin", workflow.getDescriptor().getMetaAttributes().get(JiraWorkflow.JIRA_META_UPDATE_AUTHOR_KEY));

        assertEqualsIgnoreWhitespace(workflowString, workflow.getDescriptor().asXML());
    }

    private void pureMockTest()
    {
        ofBizDraftWorkflowStore = new OfBizDraftWorkflowStore(pureMockOfBizDelegator);
    }

    private void assertEqualsIgnoreWhitespace(final String original, final String stringBeingTested)
    {
        assertEquals(StringUtils.deleteWhitespace(original), StringUtils.deleteWhitespace(stringBeingTested));
    }

}
