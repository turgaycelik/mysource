package com.atlassian.jira.avatar;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.mock.propertyset.MockPropertySet;
import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.PropertySet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AvatarServiceImplTest.
 *
 * @since v4.3
 */
@RunWith(MockitoJUnitRunner.class)
public class AvatarServiceImplTest
{
    static final String BASE_URL = "http://jira.atlassian.com";

    static final String FRED_HASH = "6255165076a5e31273cbda50bb9f9636";
    static final String SMALL_GRAVATAR_PARAMS = "?d=mm&s=16";
    static final String FRED_SMALL_GRAVATAR_URL = "http://www.gravatar.com/avatar/" + FRED_HASH + SMALL_GRAVATAR_PARAMS;
    static final String LIBRAVATAR_API = "http://cdn.libravatar.org/avatar/";
    static final String FRED_LIBRAVATAR_URL = LIBRAVATAR_API + FRED_HASH + SMALL_GRAVATAR_PARAMS;

    @Mock AvatarManager avatarManager;
    @Mock UserManager userManager;
    @Mock UserPropertyManager userPropertyManager;
    @Mock VelocityRequestContext context;
    @Mock VelocityRequestContextFactory factory;
    @Mock ApplicationProperties applicationProperties;
    @Mock GravatarSettings gravatarSettings;

    @InjectMocks AvatarServiceImpl avatarService;

    private User callingUser;

    @Test
    public void getAvatarShouldReturnAnonymousAvatarForUnknownUser() throws Exception
    {
        long anonAvatarId = 5L;
        String unknownUsername = "unknown_user";

        Avatar anonAvatar = mockAvatar(anonAvatarId);

        fixture().withAnonymousAvatarId(anonAvatarId)
                .withUser(unknownUsername, null)
                .withAvatar(anonAvatarId, anonAvatar)
                .prepare();

        Avatar avatar = avatarService.getAvatar(callingUser, unknownUsername);
        assertThat(avatar, equalTo(anonAvatar));
    }

    @Test
    public void getAvatarShouldReturnDefaultAvatarForUserWithNoConfiguredAvatar() throws Exception
    {
        long defaultAvatarId = 42L;
        String knownUsername = "known_user";
        ApplicationUser knownUser = new MockApplicationUser(knownUsername);
        Avatar defaultAvatar = mockAvatar(defaultAvatarId);

        fixture().withDefaultUserAvatarId(defaultAvatarId)
                .withUser(knownUsername, knownUser)
                .withAvatar(defaultAvatarId, defaultAvatar)
                .prepare();

        Avatar avatar = avatarService.getAvatar(callingUser, knownUsername);
        assertThat(avatar, equalTo(defaultAvatar));
    }

    @Test
    public void avatarUrlShouldStartWithASlashWhenContextPathIsEmpty() throws Exception
    {
        final String knownUsername = "known_user";
        final ApplicationUser knownUser = new MockApplicationUser(knownUsername);

        fixture()
                .withContextPath("")
                .withUser(knownUsername, knownUser)
                .prepare();

        URI url = avatarService.getAvatarURL(new MockUser(knownUsername), knownUsername, Avatar.Size.SMALL);
        assertThat(url.toString(), equalTo("/secure/useravatar?size=xsmall"));
    }

    @Test
    public void avatarUrlShouldStartWithASlashWhenContextPathIsNotEmpty() throws Exception
    {
        final String knownUsername = "known_user";
        final ApplicationUser knownUser = new MockApplicationUser(knownUsername);

        fixture()
                .withContextPath("/jira")
                .withUser(knownUsername, knownUser)
                .prepare();

        URI url = avatarService.getAvatarURL(new MockUser(knownUsername), knownUsername, Avatar.Size.SMALL);
        assertThat(url.toString(), equalTo("/jira/secure/useravatar?size=xsmall"));
    }

    @Test
    public void shouldReturnCorrectProjectDefaultAvatarURL() throws Exception
    {
        long defaultProjectAvatarId = 10L;

        fixture()
                .withContextPath("/jira")
                .withDefaultProjectAvatarId(defaultProjectAvatarId)
                .prepare();

        URI url = avatarService.getProjectDefaultAvatarURL(Avatar.Size.SMALL);
        assertThat(url.toString(), equalTo("/jira/secure/projectavatar?size=xsmall&avatarId="+defaultProjectAvatarId));
    }

    @Test
    public void serviceReturnsCanonicalAvatarUrl() throws Exception
    {
        final String knownUsername = "known_user";
        final ApplicationUser knownUser = new MockApplicationUser(knownUsername);

        fixture().withUser(knownUsername, knownUser)
                .prepare();

        URI url = avatarService.getAvatarAbsoluteURL(new MockUser(knownUsername), knownUsername, Avatar.Size.SMALL);
        assertThat(url.toString(), equalTo(BASE_URL + "/secure/useravatar?size=xsmall"));
    }

    @Test
    public void gravatarShouldReturnDefaultAvatarWhenEmailAddressIsNull() throws Exception
    {
        final ApplicationUser knownUser = mockUserWithEmail(null);
        final String knownUsername = "known_user";

        fixture()
                .withGravatar()
                .withUser(knownUsername, knownUser)
                .prepare();

        URI url = avatarService.getAvatarAbsoluteURL(new MockUser(knownUsername), knownUsername, Avatar.Size.SMALL);
        assertThat(url.toString(), equalTo(BASE_URL + "/secure/useravatar?size=xsmall"));
    }

    @Test
    public void gravatarShouldReturnCorrectUrlForFredAtExampleCom() throws Exception
    {
        final ApplicationUser knownUser = mockUserWithEmail("fred@example.com");
        final String knownUsername = "known_user";

        fixture()
                .withGravatar()
                .withUser(knownUsername, knownUser)
                .prepare();

        URI url = avatarService.getAvatarAbsoluteURL(new MockUser(knownUsername), knownUsername, Avatar.Size.SMALL);
        assertThat(url.toString(), equalTo(FRED_SMALL_GRAVATAR_URL));
    }

    @Test
    public void gravatarShouldReturnCorrectUrlForFredAtExampleComInMixedCase() throws Exception
    {
        final ApplicationUser knownUser = mockUserWithEmail("FrEd@eXamPle.CoM");
        final String knownUsername = "known_user";

        fixture()
                .withGravatar()
                .withUser(knownUsername, knownUser)
                .prepare();

        URI url = avatarService.getAvatarAbsoluteURL(new MockUser(knownUsername), knownUsername, Avatar.Size.SMALL);
        assertThat(url.toString(), equalTo(FRED_SMALL_GRAVATAR_URL));
    }

    @Test
    public void gravatarShouldUseCustomApiUrlIfAvailable() throws Exception
    {
        final ApplicationUser knownUser = mockUserWithEmail("fred@example.com");
        final String knownUsername = "known_user";

        fixture().withGravatar()
                .withCustomGravatarApiAddress(LIBRAVATAR_API)
                .withUser(knownUsername, knownUser)
                .prepare();

        URI url = avatarService.getAvatarAbsoluteURL(callingUser, knownUsername, Avatar.Size.SMALL);
        assertThat(url.toString(), equalTo(FRED_LIBRAVATAR_URL));
    }

    @Before
    public void setUp() throws Exception
    {
        callingUser = new MockUser("caller");
    }

    private ApplicationUser mockUserWithEmail(String emailAddress)
    {
        return new MockApplicationUser("somebody", "someone", emailAddress);
    }

    private Avatar mockAvatar(long avatarId)
    {
        Avatar anonAvatar = mock(Avatar.class);
        when(anonAvatar.getId()).thenReturn(avatarId);

        return anonAvatar;
    }

    protected FixturePreparer fixture()
    {
        return new FixturePreparer();
    }

    /**
     * Builder class for preparing the test fixture.
     */
    class FixturePreparer
    {
        private Long anonymousAvatarId = null;
        private Long defaultUserAvatarId = null;
        private Long defaultProjectAvatarId = null;
        private Map<Long, Avatar> avatars = Maps.newHashMap();
        private Map<String, ApplicationUser> users = Maps.newHashMap();
        private Map<ApplicationUser, PropertySet> userProperties = Maps.newHashMap();
        private boolean useGravatar = false;
        private String customGravatarApiAddress = null;
        private String contextPath = "";

        public FixturePreparer withAnonymousAvatarId(Long anonymousAvatarId)
        {
            this.anonymousAvatarId = anonymousAvatarId;
            return this;
        }

        public FixturePreparer withDefaultUserAvatarId(Long defaultAvatarId)
        {
            this.defaultUserAvatarId = defaultAvatarId;
            return this;
        }

        public FixturePreparer withDefaultProjectAvatarId(Long defaultAvatarId)
        {
            this.defaultProjectAvatarId = defaultAvatarId;
            return this;
        }

        public FixturePreparer withAvatar(long avatarId, Avatar avatar)
        {
            avatars.put(avatarId, avatar);
            return this;
        }

        public FixturePreparer withUser(String username, ApplicationUser user)
        {
            return withUser(username, user, null);
        }

        public FixturePreparer withUser(String username, ApplicationUser user, PropertySet userProperties)
        {
            this.users.put(username, user);
            this.userProperties.put(user, userProperties != null ? userProperties : new MockPropertySet());
            return this;
        }

        public FixturePreparer withContextPath(String contextPath)
        {
            this.contextPath = contextPath;
            return this;
        }

        public FixturePreparer withGravatar()
        {
            this.useGravatar = true;
            return this;
        }

        public FixturePreparer withCustomGravatarApiAddress(String customGravatarApiAddress)
        {
            this.customGravatarApiAddress = customGravatarApiAddress;
            return this;
        }

        void prepare()
        {
            // setup avatar manager
            when(avatarManager.getAnonymousAvatarId()).thenReturn(anonymousAvatarId);
            when(avatarManager.getDefaultAvatarId(Avatar.Type.USER)).thenReturn(defaultUserAvatarId);
            when(avatarManager.getDefaultAvatarId(Avatar.Type.PROJECT)).thenReturn(defaultProjectAvatarId);


            for (Map.Entry<Long, Avatar> entry : avatars.entrySet())
            {
                when(avatarManager.getById(entry.getKey())).thenReturn(entry.getValue());
            }

            // setup user manager
            for (Map.Entry<String, ApplicationUser> fixtureUser : users.entrySet())
            {
                when(userManager.getUserByName(fixtureUser.getKey())).thenReturn(fixtureUser.getValue());
                if (fixtureUser.getValue() != null) {
                    when(userManager.isUserExisting(fixtureUser.getValue())).thenReturn(true);
                }
            }
            when(userManager.isUserExisting(null)).thenReturn(false);

            // setup property manager
            for (Map.Entry<ApplicationUser, PropertySet> propertySetEntry : userProperties.entrySet())
            {
                when(userPropertyManager.getPropertySet(propertySetEntry.getKey())).thenReturn(propertySetEntry.getValue());
            }

            when(context.getCanonicalBaseUrl()).thenReturn(BASE_URL);
            when(context.getBaseUrl()).thenReturn(contextPath);
            when(factory.getJiraVelocityRequestContext()).thenReturn(context);

            when(gravatarSettings.isAllowGravatars()).thenReturn(useGravatar);
            when(gravatarSettings.getCustomApiAddress()).thenReturn(customGravatarApiAddress);
            when(applicationProperties.getEncoding()).thenReturn("utf-8");
        }
    }
}
