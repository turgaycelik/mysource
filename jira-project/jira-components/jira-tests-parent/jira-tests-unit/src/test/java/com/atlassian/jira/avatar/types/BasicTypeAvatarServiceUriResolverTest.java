package com.atlassian.jira.avatar.types;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Map;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockAvatar;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class BasicTypeAvatarServiceUriResolverTest
{
    public static final String BASE_URL = "http://not-so-fancy.com/path/secure/viewavatar";
    public static final String UTF_8 = "utf-8";
    public static final int AVATAR_ID = 121314;
    public static final String AVATAR_ID_STRING = String.valueOf(AVATAR_ID);
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    VelocityRequestContextFactory velocityRequestContextFactory;
    @Mock
    ApplicationProperties applicationProperties;
    @Mock
    VelocityRequestContext velocityRequestContext;

    BasicAvatarsImageResolver testObj;

    private final Avatar AVATAR = new MockAvatar(AVATAR_ID, "zObie.jpg", "jpg", Avatar.Type.ISSUETYPE, "some_issue", true );
    private final ApplicationUser ADMIN_USER = new MockApplicationUser("admin");

    @Before
    public void setUp() {
        testObj = new BasicAvatarsImageResolver(Avatar.Type.ISSUETYPE,velocityRequestContextFactory,applicationProperties);

        when(velocityRequestContextFactory.getJiraVelocityRequestContext()).thenReturn(velocityRequestContext);
        when(velocityRequestContext.getCanonicalBaseUrl()).thenReturn(BASE_URL);
        when(applicationProperties.getEncoding()).thenReturn(UTF_8);
    }

    @Test
    public void shouldSetAvatarIdentifierInPath() throws UnsupportedEncodingException
    {
        // when
        URI result = testObj.getAvatarRelativeUri(null, AVATAR, Avatar.Size.defaultSize());

        // expect
        String query_part = result.getQuery();

        final Map<String, String> query_params = splitQuery(query_part);
        assertThat(
                query_params,
                Matchers.hasEntry(BasicAvatarsImageResolver.AVATAR_ID_PARAM, AVATAR_ID_STRING)
        );
    }

    @Test
    public void shouldSetAvatarIdentifierInParamter() throws UnsupportedEncodingException
    {
        // when
        URI result = testObj.getAvatarAbsoluteUri(null, AVATAR, Avatar.Size.defaultSize());

        // expect
        String query_part = result.getQuery();

        final Map<String, String> query_params = splitQuery(query_part);
        assertThat(
                query_params,
                Matchers.hasEntry(BasicAvatarsImageResolver.AVATAR_ID_PARAM, AVATAR_ID_STRING)
        );
    }

    @Test
    public void shouldSetAvatarTypeInParamter() throws UnsupportedEncodingException
    {
        // when
        URI result = testObj.getAvatarRelativeUri(null, AVATAR, Avatar.Size.defaultSize());

        // expect
        String query_part = result.getQuery();

        final Map<String, String> query_params = splitQuery(query_part);
        assertThat(
                query_params,
                Matchers.hasEntry(BasicAvatarsImageResolver.AVATAR_TYPE_PARAM, Avatar.Type.ISSUETYPE.getName())
        );
    }

    @Test
    public void shouldNotIncludeDefaultSizeInParamter() throws UnsupportedEncodingException
    {
        // when
        URI result = testObj.getAvatarRelativeUri(null, AVATAR, Avatar.Size.defaultSize());

        // expect
        String query_part = result.getQuery();

        final Map<String, String> query_params = splitQuery(query_part);
        assertThat(
                query_params,
                not(hasKey(BasicAvatarsImageResolver.SIZE_PARAM))
        );
    }

    @Test
    public void shouldIncludeNotDefaultSizeInParamter() throws UnsupportedEncodingException
    {
        // when
        URI result = testObj.getAvatarRelativeUri(null, AVATAR, Avatar.Size.XLARGE);

        // expect
        String query_part = result.getQuery();

        final Map<String, String> query_params = splitQuery(query_part);
        assertThat(
            query_params,
            Matchers.hasEntry(BasicAvatarsImageResolver.SIZE_PARAM, Avatar.Size.XLARGE.getParam())
        );
    }

    @Test
    public void shouldAcceptNullSizeAndNotSetSizeParamter() throws UnsupportedEncodingException
    {
        // when
        URI result = testObj.getAvatarRelativeUri(null, AVATAR, null);

        // expect
        String query_part = result.getQuery();

        final Map<String, String> query_params = splitQuery(query_part);
        assertThat(
                query_params,
                not(hasKey(BasicAvatarsImageResolver.SIZE_PARAM))
        );
    }

    @Test
    public void shouldAbsolutURIStartWithBasePath() throws UnsupportedEncodingException
    {
        // when
        URI result = testObj.getAvatarAbsoluteUri(null, AVATAR, Avatar.Size.XLARGE);

        // expect
        final String full_url = result.toASCIIString();

        assertThat(
                full_url,
                Matchers.startsWith(BASE_URL)
        );
    }



    public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        final ImmutableMap.Builder<String, String> query_pairs_builder = ImmutableMap.builder();

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            final String[] key_and_value = pair.split("=", 2);
            query_pairs_builder.put(
                    URLDecoder.decode(key_and_value[0], UTF_8),
                    URLDecoder.decode(key_and_value[1], UTF_8));
        }

        return query_pairs_builder.build();
    }
}
