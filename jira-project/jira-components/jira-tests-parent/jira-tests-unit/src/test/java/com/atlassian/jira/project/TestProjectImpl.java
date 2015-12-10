package com.atlassian.jira.project;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.collect.MapBuilder;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v4.0.2
 */
public class TestProjectImpl
{

    @Mock
    @AvailableInContainer
    public AvatarManager avatarManager;

    @Rule
    public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    @Test
    public void testEquals() throws Exception
    {
        GenericValue gvProject1 = new MockGenericValue("Project", MapBuilder.newBuilder("id", 12L, "key", "JSP", "name", "Homo").toMap());
        GenericValue gvProject2 = new MockGenericValue("Project", MapBuilder.newBuilder("id", 12L, "key", "ASB", "name", "Homo Sapien").toMap());
        ProjectImpl project1 = new ProjectImpl(gvProject1);
        ProjectImpl project2 = new ProjectImpl(gvProject2);

        // GenericValue checks for equality on EVERY field. This can't be helped.
        assertThat(gvProject1, not(equalTo(gvProject2)));
        // JRA-20184, JRADEV-21134. ProjectImpl should only care about the ID field, other fields can legitimately change.
        assertThat(project1, equalTo(project2));
        // If the objects are equal, then the hashCodes must be equal
        assertThat(project1.hashCode(), equalTo(project2.hashCode()));
    }

    @Test
    public void testProjectIsEqualWhenNullIds() throws Exception
    {
        GenericValue gvProject1 = new MockGenericValue("Project", MapBuilder.newBuilder("id", 1110L).toMap());
        GenericValue gvProject2 = new MockGenericValue("Project", MapBuilder.newBuilder("id", null).toMap());
        ProjectImpl project1 = new ProjectImpl(gvProject1);
        ProjectImpl project2 = new ProjectImpl(gvProject2);

        // JRA-20184, JRADEV-21134. ProjectImpl should only care about the ID field, other fields can legitimately change.
        assertThat(project1, not(equalTo(project2)));
        assertThat(project2, not(equalTo(project1)));
    }

    @Test
    public void testEqualsIgnoresKey() throws Exception
    {
        GenericValue gvProject1 = new MockGenericValue("Project", MapBuilder.newBuilder("id", 1110L, "key", "HSP").toMap());
        GenericValue gvProject2 = new MockGenericValue("Project", MapBuilder.newBuilder("id", 1110L, "key", "ASD").toMap());
        ProjectImpl project1 = new ProjectImpl(gvProject1);
        ProjectImpl project2 = new ProjectImpl(gvProject2);

        // JRA-20184, JRADEV-21134. ProjectImpl should only care about the ID field, other fields can legitimately change.
        assertThat(project1, equalTo(project2));
        assertThat(project2, equalTo(project1));
    }

    @Test
    public void testEqualsIgnoresNullKeys() throws Exception
    {
        GenericValue gvProject1 = new MockGenericValue("Project", MapBuilder.newBuilder("id", 123L, "key", null, "name", "Homo").toMap());
        GenericValue gvProject2 = new MockGenericValue("Project", MapBuilder.newBuilder("id", 12L, "key", null, "name", "Homo Sapien").toMap());
        ProjectImpl project1 = new ProjectImpl(gvProject1);
        ProjectImpl project2 = new ProjectImpl(gvProject2);

        // GenericValue checks for equality on EVERY field. This can't be helped.
        assertThat(gvProject1, not(equalTo(gvProject2)));
        // JRA-20184. ProjectImpl should only care about the KEY field, other fields can legitimately change.
        assertThat(project1, not(equalTo(project2)));
        // If the objects are equal, then the hashCodes must be equal
        assertThat(project1.hashCode(), not(equalTo(project2.hashCode())));
    }

    @Test
    public void testEqualsNullIds() throws Exception
    {
        GenericValue gvProject1 = new MockGenericValue("Project", MapBuilder.newBuilder("id", null, "key", null, "name", "Homo").toMap());
        GenericValue gvProject2 = new MockGenericValue("Project", MapBuilder.newBuilder("id", null, "key", null, "name", "Homo Sapien").toMap());
        ProjectImpl project1 = new ProjectImpl(gvProject1);
        ProjectImpl project2 = new ProjectImpl(gvProject2);

        // GenericValue checks for equality on EVERY field. This can't be helped.
        assertThat(gvProject1, not(equalTo(gvProject2)));
        // JRA-20184. ProjectImpl should only care about the KEY field, other fields can legitimately change.
        assertThat(project1, equalTo(project2));
        // If the objects are equal, then the hashCodes must be equal
        assertThat(project1.hashCode(), equalTo(project2.hashCode()));
    }

    @Test
    public void shouldReturnDefaultAvatarWhenOneConfiguredDoesNotExists()
    {
        // given
        final long avatarId = 12L;
        final long defaultAvatarId = 6479L;
        final Avatar defaultAvatar = Mockito.mock(Avatar.class);

        when(avatarManager.getById(avatarId)).thenReturn(null);
        when(avatarManager.getById(defaultAvatarId)).thenReturn(defaultAvatar);
        when(avatarManager.getDefaultAvatarId(Avatar.Type.PROJECT)).thenReturn(defaultAvatarId);

        GenericValue gvProject1 = new MockGenericValue("Project", MapBuilder.newBuilder("id", null, "key", null, "name", "Homo", "avatarId", avatarId).toMap());
        Project project = new ProjectImpl(gvProject1);

        // when
        final Avatar avatar = project.getAvatar();

        // then
        assertThat(avatar, Matchers.is(defaultAvatar));
    }
}
