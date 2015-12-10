package com.atlassian.jira.security.roles;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.atlassian.cache.Cache;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.roles.CachingProjectRoleAndActorStore.ProjectRoleActorsKey;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import junit.framework.AssertionFailedError;

import static com.atlassian.jira.mock.MockProjectRoleManager.DEFAULT_ROLE_TYPES;
import static com.atlassian.jira.mock.MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
import static com.atlassian.jira.mock.MockProjectRoleManager.PROJECT_ROLE_TYPE_2;
import static com.atlassian.jira.mock.MockProjectRoleManager.PROJECT_ROLE_TYPE_3;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestCachingProjectRoleAndActorStore
{
    private static final Comparator<ProjectRoleActorsKey> KEY_COMPARATOR = new ProjectRoleActorsKeyComparator();
    private static final Set<User> NO_USERS = ImmutableSet.of();
    private static final List<ProjectRole> NO_ROLES = ImmutableList.of();

    @Mock private ProjectRoleAndActorStore delegate;
    private CachingProjectRoleAndActorStore actorStore;

    @Before
    public void setUp() throws Exception
    {
        actorStore = new CachingProjectRoleAndActorStore(delegate, new MockProjectRoleManager.MockRoleActorFactory(),
                new MemoryCacheManager());
        new MockComponentWorker().init();
    }

    // Coverage note: The methods getAllProjectRoles, getProjectRole, and getProjectRoleByName are
    // verified implicitly in several of the other tests as part of checking cache clearing,
    // so they are not called out individually.

    @Test
    public void testAddProjectRole()
    {
        when(delegate.getAllProjectRoles())
                .thenReturn(NO_ROLES)
                .thenReturn(DEFAULT_ROLE_TYPES);

        // Make sure cache is effective
        assertEquals(NO_ROLES, actorStore.getAllProjectRoles());
        assertEquals(NO_ROLES, actorStore.getAllProjectRoles());
        verify(delegate).getAllProjectRoles();

        when(delegate.addProjectRole(PROJECT_ROLE_TYPE_1)).thenReturn(PROJECT_ROLE_TYPE_2);
        assertThat("Should return the value from the delegate, not our own object",
                actorStore.addProjectRole(PROJECT_ROLE_TYPE_1), is(PROJECT_ROLE_TYPE_2));

        // Cache should have cleared, causing second return from delegate to take effect
        assertEquals(DEFAULT_ROLE_TYPES, actorStore.getAllProjectRoles());
        assertEquals(DEFAULT_ROLE_TYPES, actorStore.getAllProjectRoles());

        verify(delegate).addProjectRole(PROJECT_ROLE_TYPE_1);
        verify(delegate, times(2)).getAllProjectRoles();
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testUpdateProjectRole()
    {
        when(delegate.getAllProjectRoles())
                .thenReturn(NO_ROLES)
                .thenReturn(DEFAULT_ROLE_TYPES);

        // Make sure cache is effective
        assertEquals(NO_ROLES, actorStore.getAllProjectRoles());
        verify(delegate).getAllProjectRoles();

        assertEquals(NO_ROLES, actorStore.getAllProjectRoles());
        assertEquals(null, actorStore.getProjectRole(1L));
        assertEquals(null, actorStore.getProjectRole(2L));
        assertEquals(null, actorStore.getProjectRoleByName(PROJECT_ROLE_TYPE_3.getName()));
        verifyNoMoreInteractions(delegate);

        actorStore.updateProjectRole(PROJECT_ROLE_TYPE_1);

        // Cache should have cleared, causing second return from delegate to take effect
        assertEquals(DEFAULT_ROLE_TYPES, actorStore.getAllProjectRoles());
        assertEquals(DEFAULT_ROLE_TYPES, actorStore.getAllProjectRoles());
        assertEquals(PROJECT_ROLE_TYPE_1, actorStore.getProjectRole(1L));
        assertEquals(PROJECT_ROLE_TYPE_2, actorStore.getProjectRole(2L));
        assertEquals(PROJECT_ROLE_TYPE_3, actorStore.getProjectRoleByName(PROJECT_ROLE_TYPE_3.getName()));

        verify(delegate).updateProjectRole(PROJECT_ROLE_TYPE_1);
        verify(delegate, times(2)).getAllProjectRoles();
        verifyNoMoreInteractions(delegate);
    }

    // Delete also clears the default role actors and project role actors cache entries that
    // match the deleted role.  It's important that these cache interactions also get covered.
    @Test
    public void testDeleteProjectRole()
    {
        final DefaultRoleActors dra1 = mockDefaultRoleActors(1L);
        final DefaultRoleActors dra2 = mockDefaultRoleActors(2L);
        final ProjectRoleActors pra1 = mockProjectRoleActors(1L, 42L);
        final ProjectRoleActors pra2 = mockProjectRoleActors(1L, 43L);
        final ProjectRoleActors pra3 = mockProjectRoleActors(2L, 42L);

        final Collection<ProjectRole> afterDelete = ImmutableList.of(PROJECT_ROLE_TYPE_2, PROJECT_ROLE_TYPE_3);
        when(delegate.getAllProjectRoles())
                .thenReturn(DEFAULT_ROLE_TYPES)
                .thenReturn(afterDelete);

        // Load the cache
        assertEquals(DEFAULT_ROLE_TYPES, actorStore.getAllProjectRoles());
        assertThat(actorStore.getDefaultRoleActors(1L), hasRoleActors(dra1));
        assertThat(actorStore.getDefaultRoleActors(2L), hasRoleActors(dra2));
        assertThat(actorStore.getDefaultRoleActors(3L), nullValue());
        assertThat(actorStore.getProjectRoleActors(1L, 42L), hasRoleActors(pra1));
        assertThat(actorStore.getProjectRoleActors(1L, 43L), hasRoleActors(pra2));
        assertThat(actorStore.getProjectRoleActors(1L, 44L), nullValue());
        assertThat(actorStore.getProjectRoleActors(2L, 42L), hasRoleActors(pra3));
        assertThat(actorStore.getProjectRoleActors(2L, 43L), nullValue());
        verify(delegate).getAllProjectRoles();
        verify(delegate).getDefaultRoleActors(1L);
        verify(delegate).getDefaultRoleActors(2L);
        verify(delegate).getDefaultRoleActors(3L);
        verify(delegate).getProjectRoleActors(1L, 42L);
        verify(delegate).getProjectRoleActors(1L, 43L);
        verify(delegate).getProjectRoleActors(1L, 44L);
        verify(delegate).getProjectRoleActors(2L, 42L);
        verify(delegate).getProjectRoleActors(2L, 43L);
        verifyNoMoreInteractions(delegate);

        // Make sure caches have the expected contents and are effective
        assertEquals(ImmutableList.of(1L, 2L, 3L), sort(actorStore.defaultRoleActors.getKeys()));
        assertThat(actorStore.projectRoleActors, hasRoleActorKeys(
                key(1L, 42L), key(1L, 43L), key(1L, 44L), key(2L, 42L), key(2L, 43L)));
        assertEquals(DEFAULT_ROLE_TYPES, actorStore.getAllProjectRoles());
        assertThat(actorStore.getDefaultRoleActors(1L), hasRoleActors(dra1));
        assertThat(actorStore.getDefaultRoleActors(2L), hasRoleActors(dra2));
        assertThat(actorStore.getDefaultRoleActors(3L), nullValue());
        assertThat(actorStore.getProjectRoleActors(1L, 42L), hasRoleActors(pra1));
        assertThat(actorStore.getProjectRoleActors(1L, 43L), hasRoleActors(pra2));
        assertThat(actorStore.getProjectRoleActors(1L, 44L), nullValue());
        assertThat(actorStore.getProjectRoleActors(2L, 42L), hasRoleActors(pra3));
        assertThat(actorStore.getProjectRoleActors(2L, 43L), nullValue());
        verifyNoMoreInteractions(delegate);

        // Zap the role and make sure that the cached info was cleaned as expected
        actorStore.deleteProjectRole(PROJECT_ROLE_TYPE_1);
        assertEquals(ImmutableList.of(2L, 3L), sort(actorStore.defaultRoleActors.getKeys()));
        // projectRoleActors should be cleared on delete
        assertThat(actorStore.projectRoleActors.getKeys(), Matchers.<ProjectRoleActorsKey>empty());
    }

    private DefaultRoleActors mockDefaultRoleActors(final Long projectRoleId)
    {
        final DefaultRoleActors dra = mock(DefaultRoleActors.class);
        when(dra.getProjectRoleId()).thenReturn(projectRoleId);
        when(delegate.getDefaultRoleActors(projectRoleId)).thenReturn(dra).thenReturn(null);
        return dra;
    }

    private ProjectRoleActors mockProjectRoleActors(final Long projectRoleId, final Long projectId)
    {
        final ProjectRoleActors pra = mock(ProjectRoleActors.class);
        when(pra.getProjectRoleId()).thenReturn(projectRoleId);
        when(pra.getProjectId()).thenReturn(projectId);
        when(delegate.getProjectRoleActors(projectRoleId, projectId)).thenReturn(pra);
        return pra;
    }

    @Test
    public void testGetProjectRoleActors() throws Exception
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        final Set<RoleActor> actors = newHashSet();
        final Long roleId = projectRoleType1.getId();
        actors.add(new MockProjectRoleManager.MockRoleActor(7L, roleId, null, NO_USERS, UserRoleActorFactory.TYPE, "testuser"));
        final ProjectRoleActorsImpl projectRoleActors = new ProjectRoleActorsImpl(42L, roleId, actors);
        when(delegate.getProjectRoleActors(roleId, 42L)).thenReturn(projectRoleActors);

        assertThat(actorStore.getProjectRoleActors(roleId, 42L), hasRoleActors(projectRoleActors));

        //noinspection unchecked
        when(delegate.getProjectRoleActors(roleId, 42L)).thenThrow(AssertionFailedError.class);

        assertThat(actorStore.getProjectRoleActors(roleId, 42L), hasRoleActors(projectRoleActors));
    }

    @Test
    public void testGetDefaultRoleActors() throws Exception
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        final Set<RoleActor> actors = newHashSet();
        final Long roleId = projectRoleType1.getId();
        actors.add(new MockProjectRoleManager.MockRoleActor(7L, roleId, null, NO_USERS, UserRoleActorFactory.TYPE, "testuser"));
        final DefaultRoleActorsImpl defaultRoleActors = new DefaultRoleActorsImpl(roleId, actors);
        when(delegate.getDefaultRoleActors(roleId)).thenReturn(defaultRoleActors);

        assertThat(actorStore.getDefaultRoleActors(roleId), hasRoleActors(defaultRoleActors));

        //noinspection unchecked
        when(delegate.getDefaultRoleActors(roleId)).thenThrow(AssertionFailedError.class);

        assertThat(actorStore.getDefaultRoleActors(roleId), hasRoleActors(defaultRoleActors));
    }

    @Test
    public void testUpdateProjectRoleActors() throws RoleActorDoesNotExistException
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        final Set<RoleActor> actors = newHashSet();
        final Long roleId = projectRoleType1.getId();
        actors.add(new MockProjectRoleManager.MockRoleActor(7L, roleId, null, NO_USERS, UserRoleActorFactory.TYPE, "testuser"));
        final ProjectRoleActorsImpl projectRoleActors = new ProjectRoleActorsImpl(42L, roleId, actors);
        when(delegate.getProjectRoleActors(roleId, 42L)).thenReturn(projectRoleActors);

        // puts it in the cache
        final ProjectRoleActors before = actorStore.getProjectRoleActors(roleId, 42L);
        assertThat(before, hasRoleActors(projectRoleActors));
        verify(delegate).getProjectRoleActors(roleId, 42L);

        // clear the cache for this project role actor
        actorStore.updateProjectRoleActors(projectRoleActors);

        final ProjectRoleActors after = actorStore.getProjectRoleActors(roleId, 42L);
        assertThat(after, hasRoleActors(projectRoleActors));
        assertNotSame("The cached copy should be a new instance", before, after);
        verify(delegate, times(2)).getProjectRoleActors(roleId, 42L);
        verify(delegate).updateProjectRoleActors(projectRoleActors);
    }

    @Test
    public void testUpdateDefaultRoleActors() throws Exception
    {
        final ProjectRole projectRoleType1 = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        final Set<RoleActor> actors = newHashSet();
        final Long roleId = projectRoleType1.getId();
        actors.add(new MockProjectRoleManager.MockRoleActor(7L, roleId, null, NO_USERS, UserRoleActorFactory.TYPE, "testuser"));
        final DefaultRoleActorsImpl defaultRoleActors = new DefaultRoleActorsImpl(roleId, actors);
        when(delegate.getDefaultRoleActors(roleId)).thenReturn(defaultRoleActors);

        // puts it in the cache
        final DefaultRoleActors before = actorStore.getDefaultRoleActors(roleId);
        assertThat(before, hasRoleActors(defaultRoleActors));
        verify(delegate).getDefaultRoleActors(roleId);

        // clear the cache for this default role actor
        actorStore.updateDefaultRoleActors(defaultRoleActors);

        final DefaultRoleActors after = actorStore.getDefaultRoleActors(roleId);
        assertThat(after, hasRoleActors(defaultRoleActors));
        assertNotSame("The cached copy should be a new instance", before, after);
        verify(delegate, times(2)).getDefaultRoleActors(roleId);
        verify(delegate).updateDefaultRoleActors(defaultRoleActors);
    }

    @Test
    public void projectRoleActorsKeyShouldBeSerializble()
    {
        // Set up
        final Serializable key = new ProjectRoleActorsKey(1L, 2L);

        // Invoke
        final Object roundTrippedKey = deserialize(serialize(key));

        // Check
        assertEquals(key, roundTrippedKey);
        assertNotSame(key, roundTrippedKey);
    }



    private static ProjectRoleActorsKey key(Long projectRoleId, Long projectId)
    {
        return new ProjectRoleActorsKey(projectRoleId, projectId);
    }

    private static <T extends Comparable<T>> List<T> sort(Collection<? extends T> c)
    {
        final List<T> list = newArrayList(c);
        Collections.sort(list);
        return list;
    }

    private static <T> List<T> sort(Collection<? extends T> c, Comparator<? super T> comparator)
    {
        final List<T> list = newArrayList(c);
        Collections.sort(list, comparator);
        return list;
    }

    static Matcher<Cache<ProjectRoleActorsKey,?>> hasRoleActorKeys(ProjectRoleActorsKey... expected)
    {
        return hasRoleActorKeys(Arrays.asList(expected));
    }

    static Matcher<Cache<ProjectRoleActorsKey,?>> hasRoleActorKeys(final Collection<ProjectRoleActorsKey> exp)
    {
        final List<ProjectRoleActorsKey> expected = sort(exp, KEY_COMPARATOR);
        return new BaseMatcher<Cache<ProjectRoleActorsKey,?>>()
        {
            @Override
            public boolean matches(final Object o)
            {
                if (o instanceof Cache)
                {
                    final Collection<?> other = ((Cache<?,?>)o).getKeys();
                    return expected.size() == other.size() && other.containsAll(expected);
                }
                return false;
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("has role actor keys ");
                description.appendValue(expected);
            }
        };
    }

    static Matcher<DefaultRoleActors> hasRoleActors(final DefaultRoleActors expected)
    {
        return new BaseMatcher<DefaultRoleActors>()
        {
            @Override
            public boolean matches(final Object o)
            {
                if (o == null || !(o instanceof DefaultRoleActors))
                {
                    return false;
                }

                final DefaultRoleActors other = (DefaultRoleActors)o;
                if (!other.getProjectRoleId().equals(other.getProjectRoleId()))
                {
                    return false;
                }

                if (expected instanceof ProjectRoleActors)
                {
                    if (!(other instanceof ProjectRoleActors))
                    {
                        return false;
                    }
                    final ProjectRoleActors exp = (ProjectRoleActors)expected;
                    final ProjectRoleActors oth = (ProjectRoleActors)other;
                    if (exp.getProjectId() != null && !exp.getProjectId().equals(oth.getProjectId()))
                    {
                        return false;
                    }
                }

                return sort(expected.getUsers()).equals(sort(other.getUsers()));
            }

            @Override
            public void describeTo(final Description description)
            {
                if (expected instanceof ProjectRoleActors)
                {
                    description.appendText("ProjectRoleActors[projectId=")
                            .appendValue(((ProjectRoleActors)expected).getProjectId())
                            .appendText(",");
                }
                else
                {
                    description.appendText("DefaultRoleActors[");
                }
                description.appendText("projectRoleId=").appendValue(expected.getProjectRoleId());
                description.appendText("]");
            }
        };
    }

    static class ProjectRoleActorsKeyComparator implements Comparator<ProjectRoleActorsKey>
    {

        @Override
        public int compare(final ProjectRoleActorsKey key1, final ProjectRoleActorsKey key2)
        {
            if (key1.getProjectRoleId() != key2.getProjectRoleId())
            {
                return (key1.getProjectRoleId() < key2.getProjectRoleId()) ? -1 : 1;
            }
            if (key1.getProjectId() != key2.getProjectId())
            {
                return (key1.getProjectId() < key2.getProjectId()) ? -1 : 1;
            }
            return 0;
        }
    }
}
