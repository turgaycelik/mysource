package com.atlassian.jira.bc.project.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestProjectComponentService
{
    private Mock mockAuthenticationContext;
    private Mock mockPermissionManager;
    private Mock mockProjectComponentManager;
    private Mock mockProjectManager;
    private Mock mockUserManager;
    private Mock mockEventPublisher;
    private DefaultProjectComponentService projectComponentServiceEnt;
    private static final Long PROJECT_ID = new Long(1);
    private static final String COMPONENT_NAME = "Component One";
    private static final String INVALID_USER = "Bob";
    private static final String VALID_USER = "Robert";
    protected static final String UNIQUE_COMPONENT_NAME = "Component Name";

    private MockGenericValue mockProjectGV = new MockGenericValue("Project", EasyMap.build("id", PROJECT_ID, "name", "Test Project", "lead", VALID_USER));
    private MockProject mockProject;

    private MutableProjectComponent projectComp = new MutableProjectComponent(new Long(1000), COMPONENT_NAME, null, null, 0, PROJECT_ID);


    private User testUser;
    private User invalidTestUser;

    @After
    public void tearDown() throws Exception
    {

    }

    @Before
    public void setUp() throws Exception
    {
        new MockComponentWorker().init();

        testUser = new MockUser(VALID_USER);
        invalidTestUser = new MockUser(INVALID_USER);

        mockProject = new MockProject(PROJECT_ID, "TP", "Test Project");
        mockProject.setLead(testUser);

        mockAuthenticationContext = new Mock(JiraAuthenticationContext.class);
        mockAuthenticationContext.expectAndReturn("getI18nHelper", new MockI18nBean());

        mockPermissionManager = new Mock(PermissionManager.class);
        mockProjectComponentManager = new Mock(ProjectComponentManager.class);
        mockProjectManager = new Mock(ProjectManager.class);
        mockUserManager = new Mock(UserManager.class);
        mockEventPublisher = new Mock(EventPublisher.class);

        projectComponentServiceEnt = new DefaultProjectComponentService(
                (JiraAuthenticationContext) mockAuthenticationContext.proxy(),
                (PermissionManager) mockPermissionManager.proxy(),
                (ProjectComponentManager) mockProjectComponentManager.proxy(),
                (ProjectManager) mockProjectManager.proxy(),
                null, (UserManager) mockUserManager.proxy(), (EventPublisher) mockEventPublisher.proxy())
        {
            protected void verifyUserExists(Handler handler, String user)
            {
                mockVerifyUserExists(handler, user);
            }
        };
    }

    @Test
    public void testCreateValid()
    {
        ErrorCollection errors = new SimpleErrorCollection();
        // Grant permission to create component
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);
        mockProjectManager.expectAndReturn("getProjectObj", P.ANY_ARGS, mockProject);
        mockProjectComponentManager.expectAndReturn("create", P.ANY_ARGS, projectComp);
        assertEquals(COMPONENT_NAME, projectComponentServiceEnt.create(testUser, errors, COMPONENT_NAME, null, null, PROJECT_ID).getName());
        assertFalse(errors.hasAnyErrors());
        assertEquals(COMPONENT_NAME, projectComponentServiceEnt.create(testUser, null, "random", null, null, PROJECT_ID).getName());

        // Specifiy lead in enterprise
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);
        mockProjectManager.expectAndReturn("getProject", P.ANY_ARGS, mockProjectGV);
        mockProjectComponentManager.expectAndReturn("create", P.ANY_ARGS, projectComp);
        assertEquals(COMPONENT_NAME, projectComponentServiceEnt.create(testUser, errors, COMPONENT_NAME, null, VALID_USER, PROJECT_ID).getName());
        assertFalse(errors.hasAnyErrors());
        assertEquals(COMPONENT_NAME, projectComponentServiceEnt.create(testUser, null, "random", null, VALID_USER, PROJECT_ID).getName());
    }

    @Test
    public void testCreateUserNoPermission()
    {
        ErrorCollection errors = new SimpleErrorCollection();

        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.FALSE);
        // test for invalid input values
        ProjectComponent pc = projectComponentServiceEnt.create(null, errors, null, null, null, null);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        Collection errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("This user does not have permission to complete this operation."));

        assertNull(projectComponentServiceEnt.create(null, null, null, null, null, null));
    }

    @Test
    public void testCreateInvalid()
    {
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);

        ErrorCollection errors = null;

        // test for invalid input values
        ProjectComponent pc = projectComponentServiceEnt.create(testUser, errors = new SimpleErrorCollection(), null, null, null, null);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        Collection errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("A project id must be specified for this operation."));
        Map errorMap = errors.getErrors();
        assertEquals(1, errorMap.size());
        assertEquals("The component name specified is invalid - cannot be an empty string.", errorMap.get("name"));

        assertNull(projectComponentServiceEnt.create(testUser, null, null, null, null, null));

        // name is null, project ID not found in DB
        mockProjectManager.expectAndReturn("getProjectObj", P.ANY_ARGS, null);
        pc = projectComponentServiceEnt.create(testUser, errors = new SimpleErrorCollection(), null, null, null, PROJECT_ID);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("Unable to retrieve the project with the id 1."));
        errorMap = errors.getErrors();
        assertEquals(1, errorMap.size());
        assertEquals("The component name specified is invalid - cannot be an empty string.", errorMap.get("name"));

        assertNull(projectComponentServiceEnt.create(testUser, null, null, null, null, PROJECT_ID));

        // name is null, project ID is valid
        mockProjectManager.expectAndReturn("getProjectObj", P.ANY_ARGS, mockProject);
        pc = projectComponentServiceEnt.create(testUser, errors = new SimpleErrorCollection(), null, null, null, PROJECT_ID);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(0, errorMessages.size());
        errorMap = errors.getErrors();
        assertEquals(1, errorMap.size());
        assertEquals("The component name specified is invalid - cannot be an empty string.", errorMap.get("name"));

        assertNull(projectComponentServiceEnt.create(testUser, null, null, null, null, PROJECT_ID));

        // project ID is null
        pc = projectComponentServiceEnt.create(testUser, errors = new SimpleErrorCollection(), "xyz", null, null, null);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("A project id must be specified for this operation."));
        errorMap = errors.getErrors();
        assertEquals(0, errorMap.size());

        assertNull(projectComponentServiceEnt.create(testUser, null, "xyz", null, null, null));

        // name is null, project ID is valid but DB fails
        mockProjectManager.expectAndThrow("getProjectObj", P.ANY_ARGS, new DataAccessException("test"));
        pc = projectComponentServiceEnt.create(testUser, errors = new SimpleErrorCollection(), null, null, null, PROJECT_ID);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("Unable to retrieve the project with the id 1."));
        errorMap = errors.getErrors();
        assertEquals(1, errorMap.size());
        assertEquals("The component name specified is invalid - cannot be an empty string.", errorMap.get("name"));

        assertNull(projectComponentServiceEnt.create(testUser, new SimpleErrorCollection(), null, null, null, PROJECT_ID));
    }

    private DefaultProjectComponentService.Handler createEmptyHandler(SimpleErrorCollection errorCollection, DefaultProjectComponentService serviceDefault)
    {
        return new DefaultProjectComponentService.Handler(errorCollection, serviceDefault)
        {
            void executeOnSuccess()
            {
            }
        };
    }

    @Test
    public void testFind()
    {
        ErrorCollection errors;

        assertNull(projectComponentServiceEnt.find(testUser, null, null));
        assertNull(projectComponentServiceEnt.find(testUser, null, new Long(2)));

        mockProjectComponentManager.expectAndThrow("find", P.ANY_ARGS, new EntityNotFoundException("stuff"));
        assertNull(projectComponentServiceEnt.find(testUser, null, new Long(2)));

        // Find with null ID specified
        mockProjectComponentManager.expectAndReturn("find", P.IS_NULL, null);
        ProjectComponent pc = projectComponentServiceEnt.find(testUser, errors = new SimpleErrorCollection(), null);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        Collection errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("A component id must be specified to search for a component."));
        Map errorMap = errors.getErrors();
        assertNotNull(errorMap);
        assertEquals(0, errorMap.size());

        // Find - with non-null ID - but component does not exist in store
        mockProjectComponentManager.expectAndThrow("find", P.ANY_ARGS, new EntityNotFoundException("stuff"));
        mockProjectComponentManager.expectAndReturn("findProjectIdForComponent", P.ANY_ARGS, PROJECT_ID);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);
        pc = projectComponentServiceEnt.find(testUser, errors = new SimpleErrorCollection(), new Long(1));
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("The component with id 1 does not exist."));
        errorMap = errors.getErrors();
        assertNotNull(errorMap);
        assertEquals(0, errorMap.size());

        mockProjectComponentManager.expectAndReturn("find", P.ANY_ARGS, projectComp);
        pc = projectComponentServiceEnt.find(testUser, errors = new SimpleErrorCollection(), new Long(1));
        assertNotNull(pc);
        assertFalse(errors.hasAnyErrors());
        errorMap = errors.getErrors();
        assertNotNull(errorMap);
        assertEquals(0, errorMap.size());

    }

    @Test
    public void testFindNoPermissionToBrowseProject()
    {
        ErrorCollection errors;

        mockProjectComponentManager.expectAndReturn("find", P.ANY_ARGS, projectComp);
        mockProjectComponentManager.expectAndReturn("findProjectIdForComponent", P.ANY_ARGS, PROJECT_ID);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.FALSE);
        ProjectComponent pc = projectComponentServiceEnt.find(testUser, errors = new SimpleErrorCollection(), new Long(1));
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("The component with id 1 does not exist.", errors.getErrorMessages().iterator().next());

        // anonymous access
        pc = projectComponentServiceEnt.find(null, errors = new SimpleErrorCollection(), new Long(1));
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("The component with id 1 does not exist.", errors.getErrorMessages().iterator().next());

    }

    @Test
    public void testFindAll()
    {
        ErrorCollection errors;

        assertNull(projectComponentServiceEnt.findAllForProject(null, null));
        assertNull(projectComponentServiceEnt.findAllForProject(null, new Long(2)));

        mockProjectComponentManager.expectAndThrow("findAllForProject", P.ANY_ARGS, new EntityNotFoundException("stuff"));
        assertNull(projectComponentServiceEnt.findAllForProject(null, new Long(2)));

        // Find with null ID specified
        mockProjectComponentManager.expectAndReturn("findAllForProject", P.IS_NULL, null);
        Collection components = projectComponentServiceEnt.findAllForProject(errors = new SimpleErrorCollection(), null);
        assertNull(components);
        assertTrue(errors.hasAnyErrors());
        Collection errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("A project id must be specified for this operation."));
        Map errorMap = errors.getErrors();
        assertNotNull(errorMap);
        assertEquals(0, errorMap.size());

        // Find - with non-null ID - but component does not exist in store
        mockProjectComponentManager.expectAndThrow("findAllForProject", P.ANY_ARGS, new EntityNotFoundException("stuff"));
        components = projectComponentServiceEnt.findAllForProject(errors = new SimpleErrorCollection(), new Long(1));
        assertNull(components);
        assertTrue(errors.hasAnyErrors());
        errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("Unable to retrieve the project with the id 1."));
        errorMap = errors.getErrors();
        assertNotNull(errorMap);
        assertEquals(0, errorMap.size());

        mockProjectManager.expectAndReturn("getProjectObj", P.ANY_ARGS, mockProject);
        mockProjectComponentManager.expectAndReturn("findAllForProject", P.ANY_ARGS, new ArrayList());
        components = projectComponentServiceEnt.findAllForProject(errors = new SimpleErrorCollection(), new Long(1));
        assertNotNull(components);
        assertFalse(errors.hasAnyErrors());
        errorMap = errors.getErrors();
        assertNotNull(errorMap);
        assertTrue(errorMap.isEmpty());

        mockProjectManager.expectAndReturn("getProjectObj", P.ANY_ARGS, null);
        components = projectComponentServiceEnt.findAllForProject(errors = new SimpleErrorCollection(), new Long(1));
        assertNull(components);
        assertTrue(errors.hasAnyErrors());
        errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("Unable to retrieve the project with the id 1."));
        errorMap = errors.getErrors();
        assertNotNull(errorMap);
        assertEquals(0, errorMap.size());

    }

    @Test
    public void testUpdateNoComponent()
    {
        ProjectComponent pc = projectComponentServiceEnt.update(null, null, null);
        assertNull(pc);

        ErrorCollection errors;

        // Update - no component specified
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.FALSE);
        pc = projectComponentServiceEnt.update(null, errors = new SimpleErrorCollection(), null);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        Collection errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("The component does not exist."));
        Map errorMap = errors.getErrors();
        assertTrue(errorMap.isEmpty());
    }

    @Test
    public void testUpdateNoPermission()
    {
        ErrorCollection errors;
        // Update - no permission
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.FALSE);
        ProjectComponent pc = projectComponentServiceEnt.update(null, errors = new SimpleErrorCollection(), projectComp);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        Collection errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("This user does not have permission to complete this operation."));
        Map errorMap = errors.getErrors();
        assertTrue(errorMap.isEmpty());
    }

    @Test
    public void testUpdateInvalidLead()
    {
        ErrorCollection errors;
        // Update - enterprise with invalid lead
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);
        mockProjectComponentManager.expectAndReturn("find", P.ANY_ARGS, projectComp);
        MutableProjectComponent testComponent = MutableProjectComponent.copy(projectComp);
        testComponent.setLead(INVALID_USER);
        ProjectComponent pc = projectComponentServiceEnt.update(null, errors = new SimpleErrorCollection(), testComponent);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        Collection errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertTrue(errorMessages.isEmpty());
        Map errorMap = errors.getErrors();
        assertEquals("The user Bob does not exist.", errorMap.get(DefaultProjectComponentService.FIELD_COMPONENT_LEAD));
    }
    
    @Test
    public void testUpdateNullName()
    {
        ErrorCollection errors;
        // Update - null name
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);
        mockProjectComponentManager.expectAndReturn("findProjectIdForComponent", P.ANY_ARGS, PROJECT_ID);
        mockProjectComponentManager.expectAndReturn("find", P.ANY_ARGS, projectComp);
        MutableProjectComponent testComponent = MutableProjectComponent.copy(projectComp);
        testComponent.setName(null);
        ProjectComponent pc = projectComponentServiceEnt.update(null, errors = new SimpleErrorCollection(), testComponent);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        Collection errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertTrue(errorMessages.isEmpty());
        Map errorMap = errors.getErrors();
        assertEquals("The component name specified is invalid - cannot be an empty string.", errorMap.get(DefaultProjectComponentService.FIELD_NAME));

    }

    @Test
    public void testUpdateNoComponentId()
    {
        ErrorCollection errors;
        // Update - project ID associated with component does not exist in store
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);
        mockProjectComponentManager.expectAndThrow("findProjectIdForComponent", P.ANY_ARGS, new EntityNotFoundException("stuff"));
        mockProjectComponentManager.expectAndThrow("find", P.ANY_ARGS, new EntityNotFoundException("stuff"));
        ProjectComponent pc = projectComponentServiceEnt.update(null, errors = new SimpleErrorCollection(), projectComp);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        Collection errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(errorMessages.size(), 1);
        assertTrue(errorMessages.contains("The component with id 1000 does not exist."));
        Map errorMap = errors.getErrors();
        assertTrue(errorMap.isEmpty());
    }

    @Test
    public void testUpdateNoProjectId()
    {
        ErrorCollection errors;
        // Update - project ID associated with component does not exist in store
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);
        mockProjectComponentManager.expectAndThrow("find", P.ANY_ARGS, new EntityNotFoundException("stuff"));
        MutableProjectComponent testComponent = MutableProjectComponent.copy(projectComp);
        testComponent.setName("");
        ProjectComponent pc = projectComponentServiceEnt.update(null, errors = new SimpleErrorCollection(), testComponent);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        Collection errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(errorMessages.size(), 1);
        assertTrue(errorMessages.contains("The component with id 1000 does not exist."));
        Map errorMap = errors.getErrors();
        assertTrue(errorMap.isEmpty());
        mockProjectComponentManager.verify();
    }

    @Test
    public void testUpdateNullName2()
    {
        ErrorCollection errors;
        // Update - null name
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);
        mockProjectComponentManager.expectAndReturn("findProjectIdForComponent", P.ANY_ARGS, PROJECT_ID);
        mockProjectComponentManager.expectAndReturn("find", P.ANY_ARGS, projectComp);
        MutableProjectComponent testComponent = MutableProjectComponent.copy(projectComp);
        testComponent.setName("");
        ProjectComponent pc = projectComponentServiceEnt.update(null, errors = new SimpleErrorCollection(), testComponent);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        Collection errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertTrue(errorMessages.isEmpty());
        Map errorMap = errors.getErrors();
        assertEquals("The component name specified is invalid - cannot be an empty string.", errorMap.get(DefaultProjectComponentService.FIELD_NAME));
    }

    @Test
    public void testUpdateNameAlreadyExists()
    {
        ErrorCollection errors;
        // Update - name already exists for component associated with this project
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);
        mockProjectComponentManager.expectAndReturn("containsName", P.ANY_ARGS, Boolean.TRUE);
        mockProjectComponentManager.expectAndReturn("findProjectIdForComponent", P.ANY_ARGS, PROJECT_ID);
        mockProjectComponentManager.expectAndReturn("find", P.ANY_ARGS, projectComp);
        // Set new name to force the validateName code to be called
        MutableProjectComponent testcomponent = MutableProjectComponent.copy(projectComp);
        testcomponent.setName("New Name");
        ProjectComponent pc = projectComponentServiceEnt.update(null, errors = new SimpleErrorCollection(), testcomponent);
        assertNull(pc);
        assertTrue(errors.hasAnyErrors());
        Collection errorMessages = errors.getErrorMessages();
        assertNotNull(errorMessages);
        assertTrue(errorMessages.isEmpty());
        Map errorMap = errors.getErrors();
        assertEquals("A component with the name New Name already exists in this project.", errorMap.get(DefaultProjectComponentService.FIELD_NAME));
    }

    @Test
    public void testUpdateSuccessful()
    {
        ErrorCollection errors;
        // valid input data - successful component update
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);
        mockProjectComponentManager.expectAndReturn("containsName", P.ANY_ARGS, Boolean.FALSE);
        mockProjectComponentManager.expectAndReturn("findProjectIdForComponent", P.ANY_ARGS, PROJECT_ID);
        mockProjectComponentManager.expectAndReturn("find", P.ANY_ARGS, projectComp);
        mockProjectComponentManager.expectAndReturn("update", P.ANY_ARGS, projectComp);
        ProjectComponent pc = projectComponentServiceEnt.update(null, errors = new SimpleErrorCollection(), projectComp);
        assertNotNull(pc);
        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void testDeleteAllNulls()
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        JiraServiceContext context = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());

        mockProjectComponentManager.expectAndThrow("find", P.ANY_ARGS, new EntityNotFoundException("stuff"));
        projectComponentServiceEnt.deleteComponentForIssues(context, null);
        assertTrue(context.getErrorCollection().hasAnyErrors());
        Collection errorMessages = context.getErrorCollection().getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("The component does not exist."));
        Map errorMap = context.getErrorCollection().getErrors();
        assertTrue(errorMap.isEmpty());
    }

    @Test
    public void testDeleteNoPermission()
    {
        ErrorCollection errors;
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.FALSE);
        mockProjectComponentManager.expectAndReturn("find", P.ANY_ARGS, projectComp);
          JiraServiceContext context = new JiraServiceContextImpl((User) null, new SimpleErrorCollection());
        projectComponentServiceEnt.deleteComponentForIssues(context, projectComp.getId());
        assertTrue(context.getErrorCollection().hasAnyErrors());
        Collection errorMessages = context.getErrorCollection().getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("This user does not have permission to complete this operation."));
        Map errorMap = context.getErrorCollection().getErrors();
        assertTrue(errorMap.isEmpty());
    }

    @Test
    public void testDeleteNoSuchComponent()
    {
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, Boolean.TRUE);
        mockProjectComponentManager.expectAndThrow("find", P.ANY_ARGS, new EntityNotFoundException("stuff"));
        final JiraServiceContext context = new JiraServiceContextImpl((User) null, new SimpleErrorCollection());
        projectComponentServiceEnt.deleteComponentForIssues(context, projectComp.getId());
        assertTrue(context.getErrorCollection().hasAnyErrors());
        Collection errorMessages = context.getErrorCollection().getErrorMessages();
        assertNotNull(errorMessages);
        assertEquals(1, errorMessages.size());
        assertTrue(errorMessages.contains("The component with id 1000 does not exist."));
        Map errorMap = context.getErrorCollection().getErrors();
        assertTrue(errorMap.isEmpty());
    }  

    @Test
    public void testValidateLead()
    {
        // null is valid
        SimpleErrorCollection errorCollection;
        projectComponentServiceEnt.validateLead(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), null);
        assertFalse(errorCollection.hasAnyErrors());

        // TestUser is a valid user for enterprise edition only
        projectComponentServiceEnt.validateLead(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), testUser.getName());
        assertFalse(errorCollection.hasAnyErrors());

        // InvalidTestUser is an invalid user for all editions
        projectComponentServiceEnt.validateLead(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), invalidTestUser.getName());
        Map errorMap = errorCollection.getErrors();
        assertEquals("The user Bob does not exist.", errorMap.get(DefaultProjectComponentService.FIELD_COMPONENT_LEAD));
        Collection errors = errorCollection.getErrorMessages();
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateName()
    {
        // Null is not a valid component name
        SimpleErrorCollection errorCollection;
        projectComponentServiceEnt.validateName(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), null, null, PROJECT_ID);
        Map errorMap = errorCollection.getErrors();
        assertEquals(errorMap.get(DefaultProjectComponentService.FIELD_NAME), "The component name specified is invalid - cannot be an empty string.");
        Collection errors = errorCollection.getErrorMessages();
        assertTrue(errors.isEmpty());

        // Empty string is not a valid name
        projectComponentServiceEnt.validateName(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), null, "", PROJECT_ID);
        errorMap = errorCollection.getErrors();
        assertEquals(errorMap.get(DefaultProjectComponentService.FIELD_NAME), "The component name specified is invalid - cannot be an empty string.");
        errors = errorCollection.getErrorMessages();
        assertTrue(errors.isEmpty());

        // Valid & unique component name specified
        projectComponentServiceEnt.validateName(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), null, COMPONENT_NAME, PROJECT_ID);
        assertFalse(errorCollection.hasAnyErrors());
        errorMap = errorCollection.getErrors();
        assertTrue(errorMap.isEmpty());
        errors = errorCollection.getErrorMessages();
        assertTrue(errors.isEmpty());

        // Component name is not unique
        mockProjectComponentManager.expectAndReturn("containsName", P.ANY_ARGS, Boolean.TRUE);
        projectComponentServiceEnt.validateName(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), null, COMPONENT_NAME, PROJECT_ID);
        errorMap = errorCollection.getErrors();
        assertEquals(errorMap.get(DefaultProjectComponentService.FIELD_NAME), "A component with the name Component One already exists in this project.");
        errors = errorCollection.getErrorMessages();
        assertTrue(errors.isEmpty());

        // Component name is not unique
        mockProjectComponentManager.expectAndReturn("containsName", P.ANY_ARGS, Boolean.TRUE);
        projectComponentServiceEnt.validateName(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), UNIQUE_COMPONENT_NAME, COMPONENT_NAME, PROJECT_ID);
        errorMap = errorCollection.getErrors();
        assertEquals(errorMap.get(DefaultProjectComponentService.FIELD_NAME), "A component with the name Component One already exists in this project.");
        errors = errorCollection.getErrorMessages();
        assertTrue(errors.isEmpty());

        // Component name is not unique - lower case
        mockProjectComponentManager.expectAndReturn("containsName", P.ANY_ARGS, Boolean.TRUE);
        projectComponentServiceEnt.validateName(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), UNIQUE_COMPONENT_NAME, "component one", PROJECT_ID);
        errorMap = errorCollection.getErrors();
        assertEquals(errorMap.get(DefaultProjectComponentService.FIELD_NAME), "A component with the name component one already exists in this project.");
        errors = errorCollection.getErrorMessages();
        assertTrue(errors.isEmpty());

        // same names are OK
        projectComponentServiceEnt.validateName(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), UNIQUE_COMPONENT_NAME, UNIQUE_COMPONENT_NAME, PROJECT_ID);
        assertFalse(errorCollection.hasAnyErrors());
        errorMap = errorCollection.getErrors();
        assertTrue(errorMap.isEmpty());
        errors = errorCollection.getErrorMessages();
        assertTrue(errors.isEmpty());

        // same name with different case is OK
        projectComponentServiceEnt.validateName(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), UNIQUE_COMPONENT_NAME, "component name", PROJECT_ID);
        assertFalse(errorCollection.hasAnyErrors());
        errorMap = errorCollection.getErrors();
        assertTrue(errorMap.isEmpty());
        errors = errorCollection.getErrorMessages();
        assertTrue(errors.isEmpty());

        // test for name that is stored already
        mockProjectComponentManager.expectAndReturn("containsName", P.ANY_ARGS, Boolean.FALSE);
        projectComponentServiceEnt.validateName(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), COMPONENT_NAME, COMPONENT_NAME, PROJECT_ID);
        assertFalse(errorCollection.hasAnyErrors());
        errorMap = errorCollection.getErrors();
        assertTrue(errorMap.isEmpty());
        errors = errorCollection.getErrorMessages();
        assertTrue(errors.isEmpty());

    }

    @Test
    public void testValidateProjectId()
    {

        SimpleErrorCollection errorCollection;

        // Null is not a valid project ID
        projectComponentServiceEnt.validateProjectId(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), null);
        Collection errorMessages = errorCollection.getErrorMessages();
        assertTrue(errorMessages.contains("A project id must be specified for this operation."));
        Map errorMap = errorCollection.getErrors();
        assertTrue(errorMap.isEmpty());

        // Valid project ID specified
        mockProjectManager.expectAndReturn("getProjectObj", P.ANY_ARGS, new MockProject());
        projectComponentServiceEnt.validateProjectId(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), PROJECT_ID);
        assertFalse(errorCollection.hasAnyErrors());
        errorMap = errorCollection.getErrors();
        assertTrue(errorMap.isEmpty());

        // Invalid project ID specified
        mockProjectManager.expectAndReturn("getProjectObj", P.ANY_ARGS, null);
        projectComponentServiceEnt.validateProjectId(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), PROJECT_ID);
        errorMessages = errorCollection.getErrorMessages();
        assertTrue(errorMessages.contains("Unable to retrieve the project with the id 1."));
        errorMap = errorCollection.getErrors();
        assertTrue(errorMap.isEmpty());

        // Valid project ID specified, but data access fails
        mockProjectManager.expectAndThrow("getProjectObj", P.ANY_ARGS, new DataAccessException("stuff"));
        projectComponentServiceEnt.validateProjectId(createEmptyHandler(errorCollection = new SimpleErrorCollection(), projectComponentServiceEnt), PROJECT_ID);
        errorMessages = errorCollection.getErrorMessages();
        assertTrue(errorMessages.contains("Unable to retrieve the project with the id 1."));
        errorMap = errorCollection.getErrors();
        assertTrue(errorMap.isEmpty());

    }

    private void mockVerifyUserExists
            (DefaultProjectComponentService.Handler
                    handler, String
                    user)
    {
        if (invalidTestUser.getName().equals(user))
        {
            // The user does not exist
            handler.addErrorKey(DefaultProjectComponentService.FIELD_COMPONENT_LEAD, DefaultProjectComponentService.KEY_USER_DOES_NOT_EXIST, invalidTestUser.getName(), ErrorCollection.Reason.VALIDATION_FAILED);
        }
    }

}
