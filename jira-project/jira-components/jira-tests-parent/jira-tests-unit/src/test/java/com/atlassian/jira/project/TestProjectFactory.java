package com.atlassian.jira.project;

import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;

import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * Simple test to verify that the project factory generates valid project objects
 * from a GenericValue.
 */
public class TestProjectFactory
{
    private static final Long PROJECT_ID = new Long(1);
    private static final String PROJECT_NAME = "Test Project";
    private static final String PROJECT_URL = "http://jira.atlassian.com";
    private static final String PROJECT_LEAD = "test";
    private static final String PROJECT_DESC = "this is a desc";
    private static final String PROJECT_KEY = "TST";
    private static final Long PROJECT_COUNT = new Long(12);
    private static final Long PROJECT_ASS_TYPE = new Long(2);

    @Test
    public void testCreateFromNull() throws GenericEntityException
    {
        assertNull(new DefaultProjectFactory().getProject(null));
    }

    @Test
    public void testCreateFromGenericValue()
            throws GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        // Its ok to pass in null managers since we do not exercise them in this test
        final DefaultProjectFactory defaultProjectFactory = new DefaultProjectFactory();
        final FieldMap params = FieldMap.build("id", PROJECT_ID, "name", PROJECT_NAME)
                .add("url", PROJECT_URL)
                .add("lead", PROJECT_LEAD)
                .add("description", PROJECT_DESC)
                .add("key", PROJECT_KEY)
                .add("counter", PROJECT_COUNT)
                .add("assigneetype", PROJECT_ASS_TYPE);

        MockUserManager mockUserManager = new MockUserManager();
        new MockComponentWorker().init()
                .addMock(UserManager.class, mockUserManager);

        final ApplicationUser projectLead = new MockApplicationUser("test", "test", "test@test.com");
        mockUserManager.addUser(projectLead);
        final GenericValue projectGV = new MockGenericValue("Project", params);
        final Project project = defaultProjectFactory.getProject(projectGV);

        assertNotNull(project);
        assertEquals(PROJECT_ID, project.getId());
        assertEquals(PROJECT_NAME, project.getName());
        assertEquals(PROJECT_URL, project.getUrl());
        assertEquals(PROJECT_DESC, project.getDescription());
        assertEquals(PROJECT_KEY, project.getKey());
        assertEquals(PROJECT_ASS_TYPE, project.getAssigneeType());
        assertEquals(projectLead, project.getProjectLead());
    }

}
