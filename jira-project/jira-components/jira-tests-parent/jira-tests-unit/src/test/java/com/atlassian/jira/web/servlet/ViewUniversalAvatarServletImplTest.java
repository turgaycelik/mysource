package com.atlassian.jira.web.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.TypeAvatarService;
import com.atlassian.jira.avatar.UniversalAvatarsService;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockAvatar;
import com.atlassian.jira.mock.servlet.MockServletOutputStream;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.Consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ViewUniversalAvatarServletImplTest
{
    public static final long AVATAR_ID = 3456l;
    public static final long NON_EXISTING_AVATAR_ID = 1111l;
    public static final String SAMPLE_OUTPUT = "sample output";
    public static final String DEFAULT_OUTPUT = "default output";
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    UniversalAvatarsService avatars;
    @Mock
    TypeAvatarService typeAvatars;
    @Mock
    JiraAuthenticationContext authenticationContext;
    @Mock
    AvatarManager avatarManager;

    @InjectMocks
    AvatarToStream avatarToStream;

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;


    ApplicationUser admin = new MockApplicationUser("admin");
    MockAvatar mockAvatar = new MockAvatar(AVATAR_ID, "acd.jpg", "jpg", Avatar.Type.ISSUETYPE, "23123", true);
    MockAvatar defaultAvatar = new MockAvatar(1, "default.jpg", "jpg", Avatar.Type.ISSUETYPE, "1", true);

    ViewUniversalAvatarServletImpl testObj;

    @Before
    public void setUp() throws Exception
    {
        testObj = new ViewUniversalAvatarServletImpl(authenticationContext, avatars, avatarToStream);

        when(authenticationContext.getUser()).thenReturn(admin);

        when(avatars.getAvatars(Avatar.Type.ISSUETYPE)).thenReturn(typeAvatars);

        when(typeAvatars.getAvatar(admin, AVATAR_ID)).thenReturn(mockAvatar);
        when(typeAvatars.getAvatar(admin, NON_EXISTING_AVATAR_ID)).thenReturn(null);
        when(typeAvatars.getDefaultAvatar()).thenReturn(defaultAvatar);

        when(request.getParameter(ViewUniversalAvatarServletImpl.AVATAR_ID_PARAM)).
                thenReturn(String.valueOf(AVATAR_ID));
        when(request.getParameter(ViewUniversalAvatarServletImpl.AVATAR_TYPE_PARAM)).
                thenReturn(String.valueOf(Avatar.Type.ISSUETYPE.getName()));
    }

    @Test
    public void shouldRetrieveAvatarAndSendIt() throws Exception
    {
        // when
        testObj.doGet(request, response);

        // expect
        Mockito.verify(avatarManager).readAvatarData(refEq(mockAvatar), any(AvatarManager.ImageSize.class), any(Consumer.class));
    }

    @Test
    public void shouldUseDefaultSizeWhenItsNotSetInParameter() throws Exception
    {
        // when
        testObj.doGet(request, response);

        // expect
        Mockito.verify(avatarManager).readAvatarData(any(Avatar.class), refEq(AvatarManager.ImageSize.defaultSize()), any(Consumer.class));
    }

    @Test
    public void shouldUsePassedSize() throws Exception
    {
        // given
        when(request.getParameter(ViewUniversalAvatarServletImpl.AVATAR_SIZE_PARAM)).
                thenReturn(Avatar.Size.RETINA_XXLARGE.getParam());

        // when
        testObj.doGet(request, response);

        // expect
        Mockito.verify(avatarManager).readAvatarData(any(Avatar.class), refEq(AvatarManager.ImageSize.RETINA_XXLARGE), any(Consumer.class));
    }

    @Test
    public void shouldPassAvatarDataToResponseStream() throws Exception
    {
        returnSampleDataForAvatar(mockAvatar, SAMPLE_OUTPUT);
        final CatchResponseOutput responseOutput = new CatchResponseOutput(response);

        // when
        testObj.doGet(request, response);

        // then
        String outputResult = responseOutput.getOutput();
        Assert.assertThat(
                outputResult,
                is(equalTo(SAMPLE_OUTPUT)));
    }

    private static class CatchResponseOutput {

        private final StringWriter resultWriter;

        public CatchResponseOutput(final HttpServletResponse mockResponse) throws IOException
        {
            resultWriter = new StringWriter();
            MockServletOutputStream resultStream = new MockServletOutputStream(resultWriter);
            when(mockResponse.getOutputStream()).thenReturn(resultStream);
        }
        String getOutput() {
            return resultWriter.getBuffer().toString();
        }
    }

    private void returnSampleDataForAvatar(final MockAvatar avatar, final String sampleData) throws IOException
    {
        // AvatarManger send sample data to client
        final Answer sendSampleData = new Answer()
        {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable
            {
                ByteArrayInputStream sample_data = new ByteArrayInputStream(sampleData.getBytes());
                Consumer<InputStream> streamConsumer = (Consumer<InputStream>) invocationOnMock.getArguments()[2];
                streamConsumer.consume(sample_data);

                return null;
            }
        };
        doAnswer(sendSampleData).when(avatarManager).readAvatarData(
                refEq(avatar),
                any(AvatarManager.ImageSize.class),
                any(Consumer.class));
    }

    @Test
    public void shouldFailWithNotFoundWhenNonExistingAvatarIsPassed() throws Exception
    {
        // given
        returnSampleDataForAvatar(defaultAvatar, DEFAULT_OUTPUT);
        final CatchResponseOutput responseOutput = new CatchResponseOutput(response);

        HttpServletRequest nonExistingAvatarIdRequest = mock(HttpServletRequest.class);
        when(nonExistingAvatarIdRequest.getParameter(ViewUniversalAvatarServletImpl.AVATAR_ID_PARAM)).
                thenReturn(String.valueOf(NON_EXISTING_AVATAR_ID));
        when(nonExistingAvatarIdRequest.getParameter(ViewUniversalAvatarServletImpl.AVATAR_TYPE_PARAM)).
                thenReturn(String.valueOf(Avatar.Type.ISSUETYPE.getName()));

        // when
        testObj.doGet(nonExistingAvatarIdRequest, response);

        // then
        String outputResult = responseOutput.getOutput();
        Assert.assertThat(
                outputResult,
                is(equalTo(DEFAULT_OUTPUT)));
    }

    @Test
    public void shouldFailWithNotFoundWhenBadAvatarTypePassed() throws Exception
    {
        // given
        HttpServletRequest badAvatarTypeRequest = mock(HttpServletRequest.class);

        when(badAvatarTypeRequest.getParameter(ViewUniversalAvatarServletImpl.AVATAR_ID_PARAM)).
                thenReturn(String.valueOf(AVATAR_ID));
        when(badAvatarTypeRequest.getParameter(ViewUniversalAvatarServletImpl.AVATAR_TYPE_PARAM)).
                thenReturn("this-type-doesnt-exist");

        // when
        testObj.doGet(badAvatarTypeRequest, response);

        // expect
        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), any(String.class));
    }

    @Test
    public void shouldFailWithNotFoundWhenBadAvatarIdPassed() throws Exception
    {
        // given
        HttpServletRequest badAvatarId = mock(HttpServletRequest.class);

        when(badAvatarId.getParameter(ViewUniversalAvatarServletImpl.AVATAR_ID_PARAM)).
                thenReturn("a");
        when(badAvatarId.getParameter(ViewUniversalAvatarServletImpl.AVATAR_TYPE_PARAM)).
                thenReturn("this-type-doesnt-exist");

        // when
        testObj.doGet(badAvatarId, response);

        // expect
        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), any(String.class));
    }

    @Test
    public void shouldFailWithNotFoundWhenNoParametersPassed() throws Exception
    {
        // given
        HttpServletRequest badAvatarId = mock(HttpServletRequest.class);

        // when
        testObj.doGet(badAvatarId, response);

        // expect
        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), any(String.class));
    }
}
