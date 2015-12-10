package com.atlassian.jira.rest.v2.issue;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.UniversalAvatarsServiceImpl;
import com.atlassian.jira.avatar.types.issuetype.IssueTypeAvatarImageResolver;
import com.atlassian.jira.avatar.types.issuetype.IssueTypeTypeAvatarService;
import com.atlassian.jira.avatar.types.project.ProjectAvatarAccessPolicy;
import com.atlassian.jira.avatar.types.project.ProjectAvatarImageResolver;
import com.atlassian.jira.avatar.types.project.ProjectTypeAvatarService;
import com.atlassian.jira.rest.v2.avatar.AvatarUrls;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockAvatar;
import com.atlassian.jira.rest.v2.avatar.TemporaryAvatarHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.atlassian.jira.avatar.Avatar.Type.ISSUETYPE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class UniversalAvatarResourceTest
{
    public static final long AVATAR_ID = 45l;
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    private static final String OWNER_ISSUE1 = "ISSUE_TYPE_1";
    private static final String OWNER_ISSUE2 = "ISSUE_TYPE_2";
    public static final long SAMPLE_SIZE = 16l;
    private final Avatar ISSUE1_AVATAR = new MockAvatar(1, "issue1.jpg", "jpg", ISSUETYPE, OWNER_ISSUE1, false);
    private final Avatar ISSUE2_AVATAR = new MockAvatar(3, "issue2.jpg", "jpg", ISSUETYPE, OWNER_ISSUE2, false);
    private final Avatar SYSTEM_ISSUE_AVATAR = new MockAvatar(2, "system.jpg", "jpg", ISSUETYPE, null, true);

    private final ApplicationUser ADMIN_USER = new MockApplicationUser("admin");

    @Mock
    private JiraAuthenticationContext authContext;

    @Mock
    AvatarManager avatarManager;

    @Mock
    ProjectAvatarAccessPolicy avatarAccessPolicy;

    @Mock
    IssueTypeAvatarImageResolver issueTypeUriResolver;

    @Mock
    AvatarResourceHelper avatarResourceHelper;

    @Mock
    ProjectAvatarImageResolver projectAvatarUriResolver;

    UniversalAvatarsServiceImpl universalAvatars;

    UniversalAvatarResource testObj;

    AvatarUrls avatarUrls = new AvatarUrls();
    private Answer<URI> avatar_and_size_to_uri = new Answer<URI>()
    {
        @Override
        public URI answer(final InvocationOnMock invocationOnMock) throws Throwable
        {
            Avatar avatar = (Avatar) invocationOnMock.getArguments()[1];
            Avatar.Size size = (Avatar.Size) invocationOnMock.getArguments()[2];

            return createUriFor(avatar, size);
        }
    };
    @Mock
    private TemporaryAvatarHelper avatarTemporaryHelper;

    private static URI createUriFor(final Avatar avatar, final Avatar.Size size)
    {
        return URI.create(String.format("http://%s/%s", avatar.getFileName(), size.getPixels()));
    }

    @Before
    public void setUp()
    {
        universalAvatars = new UniversalAvatarsServiceImpl(
                new IssueTypeTypeAvatarService(avatarManager), issueTypeUriResolver,
                new ProjectTypeAvatarService(avatarManager, avatarAccessPolicy), projectAvatarUriResolver);
        testObj = new UniversalAvatarResource(authContext, universalAvatars, avatarUrls, avatarResourceHelper, avatarTemporaryHelper);

        when(authContext.getUser()).thenReturn(ADMIN_USER);

        when(issueTypeUriResolver.getAvatarAbsoluteUri(any(ApplicationUser.class), any(Avatar.class), any(Avatar.Size.class))).
                thenAnswer(avatar_and_size_to_uri);
        when(avatarManager.getAllSystemAvatars(Avatar.Type.ISSUETYPE)).thenReturn(
                ImmutableList.of(SYSTEM_ISSUE_AVATAR)
        );
        when(avatarManager.getCustomAvatarsForOwner(Avatar.Type.ISSUETYPE, OWNER_ISSUE1)).thenReturn(
                ImmutableList.of(ISSUE1_AVATAR)
        );
        when(avatarManager.getCustomAvatarsForOwner(Avatar.Type.ISSUETYPE, OWNER_ISSUE2)).thenReturn(
                ImmutableList.of(ISSUE2_AVATAR)
        );
    }

    @Test
    public void shouldReturnAllAvatarsForGivenOwnerWithType()
    {
        // when
        Response avatars = testObj.getAvatars(Avatar.Type.ISSUETYPE.getName(), OWNER_ISSUE1);

        // then
        Map<String, List<AvatarBean>> result = (Map<String, List<AvatarBean>>) avatars.getEntity();


        final Map<String, List<AvatarBean>> avatarBeans =
                ImmutableMap.<String, List<AvatarBean>>builder().
                        put(
                                "system",
                                ImmutableList.of(
                                        getAvatarBeanForAvatar(SYSTEM_ISSUE_AVATAR)
                                )).
                        put(
                                "custom",
                                ImmutableList.of(
                                        getAvatarBeanForAvatar(ISSUE1_AVATAR)
                                )).
                        build();

        assertThat(result, is(equalTo(avatarBeans)));
    }

    @Test
    public void shouldRespondWithNotFoundWhenAskingForUnknownAvatarTypeAvatars()
    {
        // when
        Response avatars = testObj.getAvatars("this-type-doesnt-exist", OWNER_ISSUE1);

        assertThat(avatars.getStatus(), is(equalTo(Response.Status.NOT_FOUND.getStatusCode())));
    }

    @Test
    public void shoulReturnCreateFromTemporaryServiceResult()
    {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        final AvatarBean avatarBean = new AvatarBean("12", OWNER_ISSUE1);
        final AvatarCroppingBean croppingBean = new AvatarCroppingBean("a", 3, 3, 3, true);
        when(avatarTemporaryHelper.createAvatarFromTemporary(
                ADMIN_USER,
                Avatar.Type.PROJECT,
                OWNER_ISSUE1,
                croppingBean)).thenReturn(avatarBean);

        // when
        final Response response = testObj.createAvatarFromTemporary(
                Avatar.Type.PROJECT.getName(),
                OWNER_ISSUE1,
                croppingBean);

        // then
        assertThat((AvatarBean)response.getEntity(), is(avatarBean));
    }

    @Test
    public void shouldRespondWithNotFoundWhenAskingForUnknownAvatarTypeAvatarCreateFromTemp()
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        final AvatarCroppingBean croppingBean = new AvatarCroppingBean("a", 3, 3, 3, true);
        // when
        Response avatars = testObj.createAvatarFromTemporary("this-type-doesnt-exist", OWNER_ISSUE1,
                croppingBean);

        assertThat(avatars.getStatus(), is(equalTo(Response.Status.NOT_FOUND.getStatusCode())));
    }

    @Test
    public void shoulReturnUploadServiceResultWhenSecurityAcceptsChange()
    {
        // given
        when(avatarAccessPolicy.userCanCreateAvatarFor(ADMIN_USER, OWNER_ISSUE1)).thenReturn(true);
        HttpServletRequest request = mock(HttpServletRequest.class);
        Response mockResponse = mock(Response.class);
        when(avatarTemporaryHelper.storeTemporaryAvatar(
                ADMIN_USER,
                Avatar.Type.PROJECT, OWNER_ISSUE1,
                Avatar.Size.LARGE, "file.jpg",
                SAMPLE_SIZE,
                request)).thenReturn(mockResponse);

        // when
        final Response response = testObj.storeTemporaryAvatar(
                Avatar.Type.PROJECT.getName(),
                OWNER_ISSUE1,
                "file.jpg",
                SAMPLE_SIZE,
                request);

        // then
        assertThat(response, is(mockResponse));
    }

    @Test
    public void shouldRespondWithNotFoundWhenAskingForUnknownAvatarTypeAvatarUpload()
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        // when
        Response avatars = testObj.storeTemporaryAvatar("this-type-doesnt-exist", OWNER_ISSUE1,
                "file.jpg",
                SAMPLE_SIZE,
                request);

        assertThat(avatars.getStatus(), is(equalTo(Response.Status.NOT_FOUND.getStatusCode())));
    }

    @Test
    public void shoulFailAndNotCallDeleteWhenSecurityRejectChange()
    {
        // given
        when(avatarAccessPolicy.userCanCreateAvatarFor(ADMIN_USER, OWNER_ISSUE1)).thenReturn(false);
        HttpServletRequest request = mock(HttpServletRequest.class);

        // when
        final Response response = testObj.deleteAvatar(
                Avatar.Type.PROJECT.getName(),
                OWNER_ISSUE1,
                AVATAR_ID);

        // then
        assertThat(response.getStatus(), is(equalTo(Response.Status.FORBIDDEN.getStatusCode())));
        verify(avatarResourceHelper, never()).deleteAvatar(AVATAR_ID);
    }

    @Test
    public void shoulReturnDeleteServiceResultWhenSecurityAcceptsChange()
    {
        // given
        when(avatarAccessPolicy.userCanCreateAvatarFor(ADMIN_USER, OWNER_ISSUE1)).thenReturn(true);
        Response mockResponse = mock(Response.class);
        when(avatarResourceHelper.deleteAvatar(AVATAR_ID)).thenReturn(mockResponse);

        // when
        final Response response = testObj.deleteAvatar(
                Avatar.Type.PROJECT.getName(),
                OWNER_ISSUE1,
                AVATAR_ID);

        // then
        assertThat(response, is(mockResponse));
    }

    @Test
    public void shouldRespondWithNotFoundWhenAskingForUnknownAvatarTypeAvatarDelete()
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        // when
        Response avatars = testObj.deleteAvatar("this-type-doesnt-exist", OWNER_ISSUE1,
                AVATAR_ID);

        assertThat(avatars.getStatus(), is(equalTo(Response.Status.NOT_FOUND.getStatusCode())));
    }

    private AvatarBean getAvatarBeanForAvatar(final Avatar avatar)
    {
        Map<String, URI> icons = createIconsURIForAvatars(avatar);

        return new AvatarBean(String.valueOf(avatar.getId()), avatar.getOwner(), avatar.isSystemAvatar(), icons);
    }

    private Map<String, URI> createIconsURIForAvatars(final Avatar avatar)
    {
        final Map<String, URI> icons;
        ImmutableMap.Builder<String, URI> iconsBuilder = ImmutableMap.builder();
        for (Avatar.Size size : Avatar.Size.values())
        {
            final int px = size.getPixels();
            if (px <= 48)
            {
                final String sizeName = String.format("%dx%d", px, px);
                iconsBuilder.put(sizeName, createUriFor(avatar, size));
            }
        }
        icons = iconsBuilder.build();

        return icons;
    }
}
