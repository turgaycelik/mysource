package com.atlassian.jira.security.roles.actor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.util.MockUserManager;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestUserRoleActorFactory
{
    private final MockUserManager userManager = new MockUserManager();
    private final RoleActorFactory roleActorFactory = new UserRoleActorFactory(new UserRoleActorFactory.UserFactory()
    {
        @Override
        public ApplicationUser getUser(final String key)
        {
            try
            {
                return TestUserRoleActorFactory.this.getUser(key);
            }
            catch (OperationNotPermittedException e)
            {
                throw new RuntimeException(e);
            }
            catch (InvalidUserException e)
            {
                throw new RuntimeException(e);
            }
            catch (InvalidCredentialException e)
            {
                throw new RuntimeException(e);
            }
        }
    }, userManager);

    ApplicationUser getUser(final String name)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        return getUser(name, name, name + "@example.com");
    }

    ApplicationUser getUser(final String name, final String displayName, final String email)
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        ApplicationUser user = new MockApplicationUser(name, displayName, email);
        userManager.updateUser(user);
        return user;
    }

    @Before
    public void setUp() throws Exception
    {
        MockComponentWorker mockComponentWorker = new MockComponentWorker();
        mockComponentWorker.registerMock(CrowdService.class, new MockCrowdService());
        ComponentAccessor.initialiseWorker(mockComponentWorker);
    }

    @Test
    public void testRoleActorContains() throws Exception
    {
        final RoleActor roleActor = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertNotNull(roleActor);
        assertTrue(roleActor.contains(getUser("username")));
    }

    @Test
    public void testRoleActorGetUsers() throws Exception
    {
        final ApplicationUser user = getUser("username", "Daniel Boone", "dan@example.com");
        final RoleActorFactory factory = new UserRoleActorFactory(new UserRoleActorFactory.UserFactory()
        {
            public ApplicationUser getUser(final String name)
            {
                return user;
            }
        }, userManager);
        final RoleActor roleActor = factory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertNotNull(roleActor);
        assertTrue(roleActor.contains(user));
        assertEquals(1, roleActor.getUsers().size());
        assertTrue(roleActor.getUsers().contains(user.getDirectoryUser()));
        assertEquals("Daniel Boone", roleActor.getDescriptor());
    }

    @Test
    public void testRoleActorNotContains() throws Exception
    {
        final RoleActor roleActor = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertNotNull(roleActor);
        assertFalse(roleActor.contains(getUser("test")));
    }

    @Test
    public void testRoleActorNotContainsNull() throws Exception
    {
        final RoleActor roleActor = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertNotNull(roleActor);
        assertFalse(roleActor.contains((ApplicationUser)null));
    }

    @Test
    public void testUserRoleActorEqualsAndHashcode() throws Exception
    {
        final RoleActor roleActor1 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        final RoleActor roleActor2 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertEquals(roleActor1, roleActor2);
        assertEquals(roleActor2, roleActor1);
        assertEquals(roleActor1.hashCode(), roleActor2.hashCode());
    }

    @Test
    public void testUserRoleActorEqualsAndHashcodeIdIrrelevant() throws Exception
    {
        final RoleActor roleActor1 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        final RoleActor roleActor2 = roleActorFactory.createRoleActor(new Long(2), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertEquals(roleActor1, roleActor2);
        assertEquals(roleActor2, roleActor1);
        assertEquals(roleActor1.hashCode(), roleActor2.hashCode());
    }

    @Test
    public void testUserRoleActorEqualsAndHashcodeProjectRelevant() throws Exception
    {
        final RoleActor roleActor1 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        final RoleActor roleActor2 = roleActorFactory.createRoleActor(new Long(1), new Long(2), new Long(1), UserRoleActorFactory.TYPE, "username");
        assertFalse(roleActor1.equals(roleActor2));
        assertFalse(roleActor2.equals(roleActor1));
        assertFalse(roleActor1.hashCode() == roleActor2.hashCode());
    }

    @Test
    public void testUserRoleActorEqualsAndHashcodeProjectRoleRelevant() throws Exception
    {
        final RoleActor roleActor1 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        final RoleActor roleActor2 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(2), UserRoleActorFactory.TYPE, "username");
        assertFalse(roleActor1.equals(roleActor2));
        assertFalse(roleActor2.equals(roleActor1));
        assertFalse(roleActor1.hashCode() == roleActor2.hashCode());
    }

    @Test
    public void testUserRoleActorEqualsAndHashcodeUsernameRelevant() throws Exception
    {
        final RoleActor roleActor1 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username");
        final RoleActor roleActor2 = roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(2), UserRoleActorFactory.TYPE,
            "another-username");
        assertFalse(roleActor1.equals(roleActor2));
        assertFalse(roleActor2.equals(roleActor1));
        assertFalse(roleActor1.hashCode() == roleActor2.hashCode());
    }

    @Test
    public void testUserRoleActorIncorrectType() throws Exception
    {
        try
        {
            roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), "blah", "username");
            fail("should be bad!");
        }
        catch (final IllegalArgumentException yay)
        {}
    }

    @Test
    public void testRoleActorOptimizeAggregatesMultiple() throws Exception
    {
        Set userRoleActors = new HashSet();
        userRoleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username"));
        userRoleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "somedude"));
        userRoleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "whatsit"));
        userRoleActors = roleActorFactory.optimizeRoleActorSet(userRoleActors);
        assertNotNull(userRoleActors);
        assertEquals(1, userRoleActors.size());
        final RoleActor roleActor = (RoleActor) userRoleActors.iterator().next();
        assertTrue(roleActor instanceof UserRoleActorFactory.AggregateRoleActor);

        assertTrue(roleActor.contains(getUser("username")));
        assertTrue(roleActor.contains(getUser("somedude")));
        assertFalse(roleActor.contains(getUser("whosee-whatsit")));
        assertFalse(roleActor.contains((ApplicationUser)null));
    }

    @Test
    public void testRoleActorOptimizeDoesNotAggregatesSingle() throws Exception
    {
        Set userRoleActors = new HashSet();
        userRoleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "username"));
        userRoleActors = roleActorFactory.optimizeRoleActorSet(userRoleActors);
        assertNotNull(userRoleActors);
        assertEquals(1, userRoleActors.size());
        final RoleActor roleActor = (RoleActor) userRoleActors.iterator().next();
        assertFalse(roleActor instanceof UserRoleActorFactory.AggregateRoleActor);
        assertTrue(roleActor instanceof UserRoleActorFactory.UserRoleActor);
    }

    @Test
    public void testAggregateRoleActorContains() throws Exception
    {
        Set roleActors = new HashSet();
        roleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "fred"));
        roleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "jim"));
        roleActors.add(roleActorFactory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "david"));

        roleActors = Collections.unmodifiableSet(roleActors);
        roleActors = roleActorFactory.optimizeRoleActorSet(roleActors);
        assertNotNull(roleActors);
        final RoleActor roleActor = ((RoleActor) roleActors.iterator().next());
        assertTrue(roleActor.getUsers().contains(getUser("fred").getDirectoryUser()));
        assertTrue(roleActor.getUsers().contains(getUser("jim").getDirectoryUser()));
        assertTrue(roleActor.getUsers().contains(getUser("david").getDirectoryUser()));
        assertFalse(roleActor.getUsers().contains(getUser("sally").getDirectoryUser()));
    }

    @Test
    public void testIllegalArgumentExceptionIfEntityNotFoundThrownByFactory() throws Exception
    {
        final UserRoleActorFactory factory = new UserRoleActorFactory(new UserRoleActorFactory.UserFactory()
        {
            public ApplicationUser getUser(final String name)
            {
                return null;
            }
        }, null);

        try
        {
            factory.createRoleActor(new Long(1), new Long(1), new Long(1), UserRoleActorFactory.TYPE, "fred");
            fail("RoleActorDoesNotExistException should have been thrown");
        }
        catch (final RoleActorDoesNotExistException yay)
        {}
    }

}
