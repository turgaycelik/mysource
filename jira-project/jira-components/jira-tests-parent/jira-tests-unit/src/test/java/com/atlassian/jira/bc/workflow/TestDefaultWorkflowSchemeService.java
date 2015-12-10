package com.atlassian.jira.bc.workflow;

import java.util.Collections;

import javax.annotation.Nullable;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.MockAssignableWorkflowScheme;
import com.atlassian.jira.workflow.MockDraftWorkflowScheme;
import com.atlassian.jira.workflow.MockJiraWorkflow;
import com.atlassian.jira.workflow.SchemeIsBeingMigratedException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class TestDefaultWorkflowSchemeService
{
    @Mock private PermissionManager permissionManager;
    @Mock private WorkflowSchemeManager workflowSchemeManager;
    @Mock private AssignableWorkflowScheme parentScheme;
    @Mock private DraftWorkflowScheme draftScheme;
    @Mock private WorkflowManager workflowManager;
    @Mock private IssueTypeManager issueTypeManager;

    private ApplicationUser user;
    private DefaultWorkflowSchemeService service;
    private I18nHelper.BeanFactory i18nFactory;

    @Before
    public void setUp() throws Exception
    {
        i18nFactory = new NoopI18nFactory();
        user = new MockApplicationUser("user");

        when(parentScheme.getId()).thenReturn(1L);
        when(draftScheme.getId()).thenReturn(2L);
        when(draftScheme.isDraft()).thenReturn(true);
        when(draftScheme.getParentScheme()).thenReturn(parentScheme);

        service = new DefaultWorkflowSchemeService(permissionManager, i18nFactory, workflowSchemeManager, workflowManager, issueTypeManager);

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
    }

    @Test
    public void createDraftOf()
    {
        assertCreateDraftErrors(new Function<Void, ServiceOutcome<DraftWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<DraftWorkflowScheme> apply(@Nullable Void input)
            {
                return service.createDraft(user, parentScheme.getId());
            }
        });

        when(workflowSchemeManager.isActive(parentScheme)).thenReturn(true);
        when(workflowSchemeManager.hasDraft(parentScheme)).thenReturn(false);
        when(workflowSchemeManager.createDraftOf(user, parentScheme)).thenReturn(draftScheme);
        when(workflowSchemeManager.getWorkflowSchemeObj(parentScheme.getId())).thenReturn(parentScheme);

        ServiceOutcome<DraftWorkflowScheme> serviceOutcome = service.createDraft(user, parentScheme.getId());
        assertOkOutcome(serviceOutcome, draftScheme);
    }

    @Test
    public void createDraft()
    {
        assertCreateDraftErrors(new Function<Void, ServiceOutcome<DraftWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<DraftWorkflowScheme> apply(@Nullable Void input)
            {
                return service.createDraft(user, draftScheme);
            }
        });

        MockAssignableWorkflowScheme workflowScheme = new MockAssignableWorkflowScheme(10L, "Name");
        MockDraftWorkflowScheme draftScheme = new MockDraftWorkflowScheme(10101L, workflowScheme);

        String issueTypeId = "2";
        String workflowName = "workflow";
        draftScheme.setMapping(issueTypeId, workflowName);

        when(workflowSchemeManager.getWorkflowSchemeObj(workflowScheme.getId())).thenReturn(workflowScheme);
        when(workflowSchemeManager.isActive(workflowScheme)).thenReturn(true);
        when(workflowSchemeManager.hasDraft(workflowScheme)).thenReturn(false);

        //Issue Type does not exist.
        ServiceOutcome<DraftWorkflowScheme> scheme = service.createDraft(user, draftScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.invalid.issue.type", issueTypeId);

        //Bad workflow.
        when(issueTypeManager.getIssueType(any(String.class))).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                String id = (String) invocationOnMock.getArguments()[0];
                return new MockIssueType(id, String.format("Issue type for ID %s", id));
            }
        });

        verify(workflowSchemeManager, never())
                .createDraft(Mockito.any(ApplicationUser.class), Mockito.any(DraftWorkflowScheme.class));

        when(workflowManager.getWorkflow(workflowName)).thenReturn(new MockJiraWorkflow());
        when(workflowSchemeManager.createDraft(user, draftScheme)).thenReturn(draftScheme);

        ServiceOutcome<DraftWorkflowScheme> serviceOutcome = service.createDraft(user, draftScheme);
        assertOkOutcome(serviceOutcome, draftScheme);

        verify(workflowSchemeManager)
                .createDraft(user, draftScheme);
    }

    private void assertCreateDraftErrors(Function<Void, ServiceOutcome<DraftWorkflowScheme>> callable)
    {
        //No permission
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);
        ServiceOutcome<DraftWorkflowScheme> serviceOutcome = callable.apply(null);
        assertErrorOutcome(serviceOutcome, "admin.workflowschemes.service.error.no.admin.permission");
        verify(permissionManager).hasPermission(Permissions.ADMINISTER, user);

        //Parent does not exist.
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(workflowSchemeManager.getWorkflowSchemeObj(parentScheme.getId())).thenReturn(null);
        serviceOutcome = callable.apply(null);
        assertErrorOutcome(serviceOutcome, "admin.workflowschemes.service.error.does.not.exist");
        verify(workflowSchemeManager).getWorkflowSchemeObj(parentScheme.getId());

        //Parent already exists.
        when(workflowSchemeManager.isActive(parentScheme)).thenReturn(false);
        when(workflowSchemeManager.getWorkflowSchemeObj(parentScheme.getId())).thenReturn(parentScheme);
        serviceOutcome = callable.apply(null);
        assertErrorOutcome(serviceOutcome, "admin.workflowschemes.service.error.not.active");
        verify(workflowSchemeManager).isActive(parentScheme);

        //Parent already has draft.
        when(workflowSchemeManager.isActive(parentScheme)).thenReturn(true);
        when(workflowSchemeManager.hasDraft(parentScheme)).thenReturn(true);
        when(workflowSchemeManager.getWorkflowSchemeObj(parentScheme.getId())).thenReturn(parentScheme);
        serviceOutcome = service.createDraft(user, parentScheme.getId());
        assertErrorOutcome(serviceOutcome, "admin.workflowschemes.service.error.has.draft");
        verify(workflowSchemeManager).hasDraft(parentScheme);
    }


    @Test
    public void getNonExistingWorkflowScheme()
    {
        when(workflowSchemeManager.getWorkflowSchemeObj(1L)).thenReturn(null);

        ServiceOutcome<AssignableWorkflowScheme> serviceOutcome = service.getWorkflowScheme(user, 1L);

        assertErrorOutcome(serviceOutcome, "admin.workflowschemes.service.error.does.not.exist");

        verify(workflowSchemeManager).getWorkflowSchemeObj(1L);
    }

    @Test
    public void getExistingWorkflowScheme()
    {
        when(workflowSchemeManager.getWorkflowSchemeObj(1L)).thenReturn(parentScheme);

        ServiceOutcome<AssignableWorkflowScheme> serviceOutcome = service.getWorkflowScheme(user, 1L);

        assertOkOutcome(serviceOutcome, parentScheme);

        verify(workflowSchemeManager).getWorkflowSchemeObj(1L);
    }

    @Test
    public void getExistingWorkflowSchemeNoPermission()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);
        when(workflowSchemeManager.getWorkflowSchemeObj(1L)).thenReturn(parentScheme);

        ServiceOutcome<AssignableWorkflowScheme> serviceOutcome = service.getWorkflowScheme(user, 1L);
        assertErrorOutcome(serviceOutcome, "admin.workflowschemes.service.error.no.view.permission");
    }

    @Test
    public void isActive()
    {
        when(workflowSchemeManager.isActive(parentScheme)).thenReturn(false);
        when(workflowSchemeManager.isActive(draftScheme)).thenReturn(true);

        assertFalse(service.isActive(parentScheme));
        assertTrue(service.isActive(draftScheme));
    }

    @Test
    public void deleteWorkflowDraftDoesNotExist()
    {
        when(parentScheme.isDefault()).thenReturn(true);

        final ServiceOutcome<Void> outcome = service.deleteWorkflowScheme(user, draftScheme);
        assertErrorOutcome(outcome, "admin.workflowschemes.service.error.draft.does.not.exist");
    }

    @Test
    public void deleteWorkflowParentDoesNotExist()
    {
        when(parentScheme.isDefault()).thenReturn(true);

        final ServiceOutcome<Void> outcome = service.deleteWorkflowScheme(user, parentScheme);
        assertErrorOutcome(outcome, "admin.workflowschemes.service.error.does.not.exist");
    }

    @Test
    public void deleteWorkflowSchemeDefault()
    {
        when(parentScheme.isDefault()).thenReturn(true);
        when(workflowSchemeManager.getWorkflowSchemeObj(parentScheme.getId())).thenReturn(parentScheme);

        final ServiceOutcome<Void> outcome = service.deleteWorkflowScheme(user, parentScheme);
        assertErrorOutcome(outcome, "admin.workflowschemes.service.error.cant.delete.default");
    }

    @Test
    public void deleteWorkflowSchemeNoPermission()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        final ServiceOutcome<Void> outcome = service.deleteWorkflowScheme(user, draftScheme);
        assertErrorOutcome(outcome, "admin.workflowschemes.service.error.no.admin.permission");
    }

    @Test
    public void deleteWorkflowSchemeIsActive()
    {
        when(workflowSchemeManager.isActive(parentScheme)).thenReturn(true);
        when(workflowSchemeManager.getWorkflowSchemeObj(parentScheme.getId())).thenReturn(parentScheme);

        final ServiceOutcome<Void> outcome = service.deleteWorkflowScheme(user, parentScheme);
        assertErrorOutcome(outcome, "admin.workflowschemes.service.error.delete.active");
    }

    @Test
    public void deleteWorkflowSchemeIsMigrating()
    {
        when(workflowSchemeManager.deleteWorkflowScheme(draftScheme)).thenThrow(new SchemeIsBeingMigratedException());
        when(workflowSchemeManager.getWorkflowSchemeObj(parentScheme.getId())).thenReturn(parentScheme);

        final ServiceOutcome<Void> outcome = service.deleteWorkflowScheme(user, parentScheme);
        assertErrorOutcome(outcome, "admin.workflowschemes.service.error.delete.error");
    }

    @Test
    public void deleteWorkflowSchemeGenericError()
    {
        when(workflowSchemeManager.deleteWorkflowScheme(draftScheme)).thenReturn(false);
        when(workflowSchemeManager.getWorkflowSchemeObj(parentScheme.getId())).thenReturn(parentScheme);

        final ServiceOutcome<Void> outcome = service.deleteWorkflowScheme(user, parentScheme);
        assertErrorOutcome(outcome, "admin.workflowschemes.service.error.delete.error");
    }

    @Test
    public void deleteWorkflowSchemeGenericGood()
    {
        when(workflowSchemeManager.getWorkflowSchemeObj(parentScheme.getId())).thenReturn(parentScheme);
        when(workflowSchemeManager.deleteWorkflowScheme(parentScheme)).thenReturn(true);

        final ServiceOutcome<Void> outcome = service.deleteWorkflowScheme(user, parentScheme);
        assertOkOutcome(outcome, null);
    }

    @Test
    public void getSchemeForProject()
    {
        final DefaultWorkflowSchemeService workflowSchemeService = new DefaultWorkflowSchemeService(permissionManager, i18nFactory, workflowSchemeManager, workflowManager, issueTypeManager)
        {
            @Override
            boolean hasPermissionToEditProject(ApplicationUser user, Project project)
            {
                assertNotNull(user);
                assertNotNull(project);
                assertEquals(TestDefaultWorkflowSchemeService.this.user, user);
                return true;
            }
        };

        final MockProject mockProject = new MockProject();

        when(workflowSchemeManager.getWorkflowSchemeObj(mockProject)).thenReturn(parentScheme);

        final ServiceOutcome<AssignableWorkflowScheme> outcome = workflowSchemeService.getSchemeForProject(user, mockProject);
        assertOkOutcome(outcome, parentScheme);
    }

    @Test
    public void getSchemeForProjectNoPermission()
    {
        final DefaultWorkflowSchemeService workflowSchemeService = new DefaultWorkflowSchemeService(permissionManager, i18nFactory, workflowSchemeManager, workflowManager, issueTypeManager)
        {
            @Override
            boolean hasPermissionToEditProject(ApplicationUser user, Project project)
            {
                assertNotNull(user);
                assertNotNull(project);
                assertEquals(TestDefaultWorkflowSchemeService.this.user, user);
                return false;
            }
        };

        final ServiceOutcome<AssignableWorkflowScheme> outcome = workflowSchemeService.getSchemeForProject(user, new MockProject());
        assertErrorOutcome(outcome, "admin.workflowschemes.service.error.no.permission.project");
    }

    @Test
    public void getDraftWorkflowSchemeDefaultScheme()
    {
        when(parentScheme.isDefault()).thenReturn(true);

        final ServiceOutcome<?> outcome = service.getDraftWorkflowScheme(user, parentScheme);
        assertOkOutcome(outcome, null);
    }

    @Test
    public void getDraftWorkflowScheme()
    {
        when(workflowSchemeManager.getDraftForParent(parentScheme)).thenReturn(draftScheme);

        ServiceOutcome<DraftWorkflowScheme> outcome = service.getDraftWorkflowScheme(user, parentScheme);
        assertOkOutcome(outcome, draftScheme);

        final AssignableWorkflowScheme noParent = Mockito.mock(AssignableWorkflowScheme.class);
        outcome = service.getDraftWorkflowScheme(user, noParent);
        assertOkOutcome(outcome, null);
    }

    @Test
    public void updateDraftWorkflowSchemeNoPermission()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        ServiceOutcome<DraftWorkflowScheme> draftWorkflowSchemeServiceOutcome = service.updateWorkflowScheme(user, draftScheme);
        assertErrorOutcome(draftWorkflowSchemeServiceOutcome, "admin.workflowschemes.service.error.no.admin.permission");
    }

    @Test
    public void updateDraftWorkflowSchemeDoesNotExist()
    {
        ServiceOutcome<DraftWorkflowScheme> draftWorkflowSchemeServiceOutcome = service.updateWorkflowScheme(user, draftScheme);
        assertErrorOutcome(draftWorkflowSchemeServiceOutcome, "admin.workflowschemes.service.error.draft.does.not.exist");
    }

    @Test
    public void updateDraftWorkflowSchemeBadWorkflow()
    {
        String badName = "badName";
        when(workflowSchemeManager.getDraft(draftScheme.getId())).thenReturn(draftScheme);
        when(draftScheme.getMappings()).thenReturn(Collections.<String, String>singletonMap(null, badName));

        ServiceOutcome<DraftWorkflowScheme> draftWorkflowSchemeServiceOutcome = service.updateWorkflowScheme(user, draftScheme);
        assertErrorOutcome(draftWorkflowSchemeServiceOutcome, "admin.workflowschemes.service.error.bad.workflow", badName);
    }

    @Test
    public void updateDraftWorkflowScheme()
    {
        DraftWorkflowScheme newScheme = Mockito.mock(DraftWorkflowScheme.class);
        JiraWorkflow workflow = Mockito.mock(JiraWorkflow.class);

        String goodName = "goodName";
        when(workflowSchemeManager.getDraft(draftScheme.getId())).thenReturn(draftScheme);
        when(draftScheme.getMappings()).thenReturn(Collections.<String, String>singletonMap(null, goodName));
        when(workflowSchemeManager.updateDraftWorkflowScheme(user, draftScheme)).thenReturn(newScheme);
        when(workflowManager.getWorkflow(goodName)).thenReturn(workflow);

        ServiceOutcome<DraftWorkflowScheme> outcome = service.updateWorkflowScheme(user, draftScheme);

        verify(workflowSchemeManager).updateDraftWorkflowScheme(user, draftScheme);

        assertOkOutcome(outcome, newScheme);
    }

    @Test
    public void updateDraftWorkflowSchemeIsBeingMigrated()
    {
        JiraWorkflow workflow = Mockito.mock(JiraWorkflow.class);

        String goodName = "goodName";
        when(workflowSchemeManager.getDraft(draftScheme.getId())).thenReturn(draftScheme);
        when(draftScheme.getMappings()).thenReturn(Collections.<String, String>singletonMap(null, goodName));
        when(workflowManager.getWorkflow(goodName)).thenReturn(workflow);

        when(workflowSchemeManager.updateDraftWorkflowScheme(user, draftScheme)).thenThrow(new SchemeIsBeingMigratedException());

        ServiceOutcome<DraftWorkflowScheme> draftWorkflowSchemeServiceOutcome = service.updateWorkflowScheme(user, draftScheme);
        assertErrorOutcome(draftWorkflowSchemeServiceOutcome, "admin.workflowschemes.manager.migration.in.progress");
    }

    @Test public void getUsageCount()
    {
        Project project1 = new MockProject(11, "ABC");
        Project project2 = new MockProject(12, "DEF");
        Project project3 = new MockProject(13, "GHI");

        when(workflowSchemeManager.getProjectsUsing(parentScheme)).thenReturn(ImmutableList.of(project1, project2, project3));
        assertEquals(3, service.getUsageCount(parentScheme));

        when(workflowSchemeManager.getProjectsUsing(parentScheme)).thenReturn(ImmutableList.of(project1));
        assertEquals(1, service.getUsageCount(parentScheme));
    }

    @Test public void assignableBuilder()
    {
        final AssignableWorkflowScheme.Builder expectedBuilder = mock(AssignableWorkflowScheme.Builder.class);

        when(workflowSchemeManager.assignableBuilder()).thenReturn(expectedBuilder);
        AssignableWorkflowScheme.Builder actualBuilder = service.assignableBuilder();

        assertSame(expectedBuilder, actualBuilder);

        verify(workflowSchemeManager).assignableBuilder();
    }

    @Test public void draftBuilder()
    {
        final DraftWorkflowScheme.Builder expectedBuilder = mock(DraftWorkflowScheme.Builder.class);

        MockAssignableWorkflowScheme parent = new MockAssignableWorkflowScheme();
        parent.setId(101018384L);

        when(workflowSchemeManager.draftBuilder(parent)).thenReturn(expectedBuilder);
        DraftWorkflowScheme.Builder actualBuilder = service.draftBuilder(parent);

        assertSame(expectedBuilder, actualBuilder);

        verify(workflowSchemeManager).draftBuilder(parent);
    }

    @Test public void createScheme()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false, true);

        MockAssignableWorkflowScheme workflowScheme = new MockAssignableWorkflowScheme();
        ServiceOutcome<AssignableWorkflowScheme> scheme = service.createScheme(user, workflowScheme);

        //No permission.
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.no.admin.permission");
        verify(workflowSchemeManager, never()).createScheme(Matchers.<AssignableWorkflowScheme>any());

        //No name.
        scheme = service.createScheme(user, workflowScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.scheme.must.have.name");
        verify(workflowSchemeManager, never()).createScheme(Matchers.<AssignableWorkflowScheme>any());

        //Too long.
        workflowScheme.setName(StringUtils.repeat("*", 256));
        scheme = service.createScheme(user, workflowScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.scheme.name.too.big");
        verify(workflowSchemeManager, never()).createScheme(Matchers.<AssignableWorkflowScheme>any());

        //Duplicate name when new.
        String name = StringUtils.repeat("*", 255);
        workflowScheme.setName(name);
        when(workflowSchemeManager.getWorkflowSchemeObj(name)).thenReturn(new MockAssignableWorkflowScheme());
        scheme = service.createScheme(user, workflowScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.scheme.name.duplicate", name);
        verify(workflowSchemeManager, never()).createScheme(Matchers.<AssignableWorkflowScheme>any());

        //Duplicate name when old.
        workflowScheme.setId(11L);
        when(workflowSchemeManager.getWorkflowSchemeObj(name)).thenReturn(new MockAssignableWorkflowScheme(10L, name));
        scheme = service.createScheme(user, workflowScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.scheme.name.duplicate", name);
        verify(workflowSchemeManager, never()).createScheme(Matchers.<AssignableWorkflowScheme>any());

        String issueTypeId = "2";
        String workflowName = "workflow";
        workflowScheme.setMapping(issueTypeId, workflowName);
        when(workflowSchemeManager.getWorkflowSchemeObj(name)).thenReturn(null);

        //Issue Type does not exist.
        scheme = service.createScheme(user, workflowScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.invalid.issue.type", issueTypeId);
        verify(workflowSchemeManager, never()).createScheme(Matchers.<AssignableWorkflowScheme>any());

        //Bad workflow.
        when(workflowSchemeManager.getWorkflowSchemeObj(name)).thenReturn(null);
        when(issueTypeManager.getIssueType(issueTypeId)).thenReturn(new MockIssueType(issueTypeId, "Something"));
        scheme = service.createScheme(user, workflowScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.bad.workflow", workflowName);
        verify(workflowSchemeManager, never()).createScheme(Matchers.<AssignableWorkflowScheme>any());

        //Good Case.
        when(workflowManager.getWorkflow(workflowName)).thenReturn(new MockJiraWorkflow());
        MockAssignableWorkflowScheme expected = new MockAssignableWorkflowScheme(null, "Name2");
        when(workflowSchemeManager.createScheme(workflowScheme)).thenReturn(expected);

        scheme = service.createScheme(user, workflowScheme);
        assertOkOutcome(scheme, expected);
    }

    @Test public void validateUpdate()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false, true);

        MockAssignableWorkflowScheme workflowScheme = new MockAssignableWorkflowScheme();
        workflowScheme.setId(574748L);
        ServiceOutcome<Void> scheme = service.validateUpdateWorkflowScheme(user, workflowScheme);

        //No permission.
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.no.admin.permission");

        //Scheme does not exist.
        when(workflowSchemeManager.getWorkflowSchemeObj(workflowScheme.getId())).thenReturn(null);
        scheme = service.validateUpdateWorkflowScheme(user, workflowScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.does.not.exist");

        //No name.
        when(workflowSchemeManager.getWorkflowSchemeObj(workflowScheme.getId())).thenReturn(workflowScheme);
        scheme = service.validateUpdateWorkflowScheme(user, workflowScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.scheme.must.have.name");

        //Too long.
        workflowScheme.setName(StringUtils.repeat("*", 256));
        scheme = service.validateUpdateWorkflowScheme(user, workflowScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.scheme.name.too.big");

        //Duplicate name when new.
        String name = StringUtils.repeat("*", 255);
        workflowScheme.setName(name);
        final MockAssignableWorkflowScheme otherScheme = new MockAssignableWorkflowScheme(workflowScheme);
        otherScheme.setId(workflowScheme.getId() + 1);
        when(workflowSchemeManager.getWorkflowSchemeObj(name)).thenReturn(otherScheme);
        scheme = service.validateUpdateWorkflowScheme(user, workflowScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.scheme.name.duplicate", name);

        String issueTypeId = "2";
        String workflowName = "workflow";
        workflowScheme.setMapping(issueTypeId, workflowName);
        when(workflowSchemeManager.getWorkflowSchemeObj(name)).thenReturn(workflowScheme);

        //Issue Type does not exist.
        scheme = service.validateUpdateWorkflowScheme(user, workflowScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.invalid.issue.type", issueTypeId);

        //Bad workflow.
        when(workflowSchemeManager.getWorkflowSchemeObj(name)).thenReturn(null);
        when(issueTypeManager.getIssueType(any(String.class))).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                String id = (String) invocationOnMock.getArguments()[0];
                return new MockIssueType(id, String.format("Issue type for ID %s", id));
            }
        });

        scheme = service.validateUpdateWorkflowScheme(user, workflowScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.bad.workflow", workflowName);

        //Bad case where scheme mappings have changed on an active workflow.
        MockAssignableWorkflowScheme existingScheme = new MockAssignableWorkflowScheme(workflowScheme);
        when(workflowSchemeManager.getWorkflowSchemeObj(workflowScheme.getId())).thenReturn(existingScheme);
        when(workflowManager.getWorkflow(any(String.class))).thenReturn(new MockJiraWorkflow());
        when(workflowSchemeManager.isActive(existingScheme)).thenReturn(true);
        workflowScheme.setMapping("77", "something");

        scheme = service.validateUpdateWorkflowScheme(user, workflowScheme);
        assertErrorOutcome(scheme, "admin.workflowschemes.service.error.change.active");

        when(workflowSchemeManager.isActive(existingScheme)).thenReturn(false);
        //Good Case
        when(workflowSchemeManager.getWorkflowSchemeObj(workflowScheme.getId())).thenReturn(existingScheme);
        MockAssignableWorkflowScheme expected = new MockAssignableWorkflowScheme(null, "Name2");
        when(workflowSchemeManager.updateWorkflowScheme(workflowScheme)).thenReturn(expected);
        scheme = service.validateUpdateWorkflowScheme(user, workflowScheme);
        assertOkOutcome(scheme, null);

        //Good Case change the names but the mappings don't change.
        existingScheme = new MockAssignableWorkflowScheme(workflowScheme)
                .setName("Something Else for a New Name").setDescription("Something nice description");
        when(workflowSchemeManager.isActive(existingScheme)).thenReturn(true);
        assertOkOutcome(scheme, null);

        verify(workflowSchemeManager, never()).updateWorkflowScheme(Matchers.<AssignableWorkflowScheme>any());
    }

    @Test public void updateScheme()
    {
        MockAssignableWorkflowScheme scheme = new MockAssignableWorkflowScheme(18L, "Brenden");
        MockAssignableWorkflowScheme expected = new MockAssignableWorkflowScheme(18L, "Brenden2");

        final String key = "something";

        final DefaultWorkflowSchemeService service = spy(this.service);
        doReturn(ServiceOutcomeImpl.<Void>error(NoopI18nHelper.makeTranslation(key)))
                .when(service).validateUpdateWorkflowScheme(user, scheme);

        ServiceOutcome<AssignableWorkflowScheme> outcome = service.updateWorkflowScheme(user, scheme);
        assertErrorOutcome(outcome, key);
        verify(workflowSchemeManager, never()).updateWorkflowScheme(Matchers.<AssignableWorkflowScheme>any());

        when(workflowSchemeManager.updateWorkflowScheme(scheme)).thenThrow(new SchemeIsBeingMigratedException());
        doReturn(ServiceOutcomeImpl.<Void>ok(null))
                .when(service).validateUpdateWorkflowScheme(user, scheme);
        outcome = service.updateWorkflowScheme(user, scheme);
        assertErrorOutcome(outcome, "admin.workflowschemes.manager.migration.in.progress");
        verify(workflowSchemeManager).updateWorkflowScheme(scheme);
        reset(workflowSchemeManager);

        when(workflowSchemeManager.updateWorkflowScheme(scheme)).thenReturn(expected);
        doReturn(ServiceOutcomeImpl.<Void>ok(null))
                .when(service).validateUpdateWorkflowScheme(user, scheme);
        outcome = service.updateWorkflowScheme(user, scheme);
        assertOkOutcome(outcome, expected);

        verify(workflowSchemeManager).updateWorkflowScheme(scheme);
        verify(service, times(3)).validateUpdateWorkflowScheme(user, scheme);
    }

    private <T> void assertOkOutcome(ServiceOutcome<T> serviceOutcome, T expectedScheme)
    {
        assertTrue(serviceOutcome.isValid());
        assertSame(expectedScheme, serviceOutcome.getReturnedValue());
    }

    private void assertErrorOutcome(ServiceOutcome<?> serviceOutcome, String key, Object...args)
    {
        assertFalse(serviceOutcome.isValid());
        ErrorCollection errorCollection = serviceOutcome.getErrorCollection();
        assertTrue(errorCollection.getErrors().isEmpty());
        assertTrue(errorCollection.getErrorMessages().contains(NoopI18nHelper.makeTranslation(key, args)));
    }
}
