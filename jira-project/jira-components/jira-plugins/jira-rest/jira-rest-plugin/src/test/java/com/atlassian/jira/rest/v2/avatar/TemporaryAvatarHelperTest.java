package com.atlassian.jira.rest.v2.avatar;

import java.awt.*;
import java.io.File;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImageDataProvider;
import com.atlassian.jira.avatar.CroppingAvatarImageDataProviderFactory;
import com.atlassian.jira.avatar.Selection;
import com.atlassian.jira.avatar.TemporaryAvatar;
import com.atlassian.jira.avatar.TemporaryAvatars;
import com.atlassian.jira.avatar.TypeAvatarService;
import com.atlassian.jira.avatar.UniversalAvatarsService;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.rest.util.AttachmentHelper;
import com.atlassian.jira.rest.v2.issue.AvatarBean;
import com.atlassian.jira.rest.v2.issue.AvatarCroppingBean;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TemporaryAvatarHelperTest
{
    public static final String FILENAME = "filename.png";
    public static final long SOME_FILESIZE = 50000l;
    public static final String SOME_TYPE = "sushi/futomaki";
    public static final int WIDTH = 8;
    public static final int HEIGHT = 27;
    public static final String ISSUETYPE_SUBJECT = "issue1";
    public static final long NEWLY_CREATED_AVATAR_ID = 74924l;
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private TemporaryAvatarUploader avatarUploader;
    @Mock
    private AttachmentHelper attachmentHelper;
    @Mock
    private I18nHelper i18nHelper;
    @Mock
    private CroppingAvatarImageDataProviderFactory croppingAvatarImageDataProviderFactory;
    // can i remove this dep?
    @Mock
    private VelocityRequestContextFactory requestContextFactory;
    @Mock
    private UniversalAvatarsService avatars;
    @Mock
    private TemporaryAvatars temporaryAvatars;
    @Mock
    private AttachmentHelper.ValidationResult attachmentValidationResult;
    @Mock
    private TypeAvatarService issueTypeAvatars;
    @Mock
    private com.atlassian.jira.avatar.Avatar newlyCreatedAvatar;

    private UploadedAvatar xxLargeAvatar;
    private UploadedAvatar smallAvatar;


    private final ApplicationUser FRED = new MockApplicationUser("fred");

    @InjectMocks
    private TemporaryAvatarHelper testObj;

    @Before
    public void setUp() throws Exception
    {
        xxLargeAvatar = new UploadedAvatar(
                mock(File.class),
                SOME_TYPE,
                Avatar.Size.RETINA_XXLARGE.getPixels(),
                Avatar.Size.RETINA_XXLARGE.getPixels());
        smallAvatar = new UploadedAvatar(
                mock(File.class),
                SOME_TYPE,
                Avatar.Size.SMALL.getPixels(),
                Avatar.Size.SMALL.getPixels());
        when(avatarUploader.createUploadedAvatarFromStream(any(InputStream.class), eq(FILENAME), eq(SOME_TYPE), eq(SOME_FILESIZE))).thenReturn(xxLargeAvatar);

        when(attachmentHelper.validate(
                any(HttpServletRequest.class),
                eq(FILENAME),
                eq(SOME_FILESIZE))).thenReturn(attachmentValidationResult);
        when(attachmentValidationResult.getContentType()).thenReturn(SOME_TYPE);
        when(attachmentValidationResult.isValid()).thenReturn(true);
        when(attachmentValidationResult.getSize()).thenReturn(SOME_FILESIZE);

        final VelocityRequestContext velocityRequestContext = mock(VelocityRequestContext.class);
        when(velocityRequestContext.getBaseUrl()).thenReturn("abc/");
        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(velocityRequestContext);

        when(avatars.getAvatars(Avatar.Type.ISSUETYPE)).thenReturn(issueTypeAvatars);
        when(issueTypeAvatars.createAvatar(any(ApplicationUser.class), eq(ISSUETYPE_SUBJECT), any(AvatarImageDataProvider.class))).thenReturn(
                newlyCreatedAvatar
        );
        when(newlyCreatedAvatar.getId()).thenReturn(NEWLY_CREATED_AVATAR_ID);
        when(newlyCreatedAvatar.getOwner()).thenReturn(ISSUETYPE_SUBJECT);

        final TemporaryAvatar temporaryAvatarDeepMock =
                mock(TemporaryAvatar.class, Mockito.RETURNS_DEEP_STUBS);
        //new TemporaryAvatar(SOME_TYPE, SOME_TYPE, FILENAME, mock(File.class), null);
        when(temporaryAvatars.getCurrentTemporaryAvatar()).
                thenReturn(temporaryAvatarDeepMock);
    }

    @Test
    public void shouldRespondWithCroppingInstructionsWhenImageIsBiggerThanExpected() throws Exception
    {
        // given
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(avatarUploader.createUploadedAvatarFromStream(
                        any(InputStream.class),
                        eq(FILENAME),
                        eq(SOME_TYPE),
                        eq(SOME_FILESIZE))
        ).thenReturn(xxLargeAvatar);

        // when
        final Response response = testObj.storeTemporaryAvatar(
                FRED,
                Avatar.Type.ISSUETYPE,
                ISSUETYPE_SUBJECT,
                Avatar.Size.SMALL,
                FILENAME,
                SOME_FILESIZE,
                request);

        // then
        assertThat(response.getStatus(), is(equalTo(Response.Status.CREATED.getStatusCode())));
        final AvatarCroppingBean croppingInstructions = (AvatarCroppingBean) response.getEntity();
        assertThat(croppingInstructions.isNeedsCropping(), is(true));
        // coordinates should be checked with com.atlassian.jira.avatar.AvatarPickerHelperImpl.TemporaryAvatarBean
        // test (absent!)
    }

    @Test
    public void shouldRespondWithConfiguredAvatarBeanIfImageSizeMatchesExpected() throws Exception
    {
        // given
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(avatarUploader.createUploadedAvatarFromStream(
                        any(InputStream.class),
                        eq(FILENAME),
                        eq(SOME_TYPE),
                        eq(SOME_FILESIZE))
        ).thenReturn(smallAvatar);

        when(croppingAvatarImageDataProviderFactory.createStreamsFrom(any(InputStream.class), any(Selection.class))).
                thenReturn(mock(AvatarImageDataProvider.class));

        // when
        final Response response = testObj.storeTemporaryAvatar(
                FRED,
                Avatar.Type.ISSUETYPE,
                ISSUETYPE_SUBJECT,
                Avatar.Size.SMALL,
                FILENAME,
                SOME_FILESIZE,
                request);

        assertThat(response.getStatus(), is(equalTo(Response.Status.CREATED.getStatusCode())));
        final AvatarBean newAvatarBean = (AvatarBean) response.getEntity();
        assertThat(newAvatarBean.getId(), is(equalTo(String.valueOf(NEWLY_CREATED_AVATAR_ID))));
        assertThat(newAvatarBean.getOwner(), is(equalTo(ISSUETYPE_SUBJECT)));
    }

    @Test
    public void shouldUseExpectedAvatarSizeWhenNoCroppingIsNeeded() throws Exception
    {
        // given
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(avatarUploader.createUploadedAvatarFromStream(
                        any(InputStream.class),
                        eq(FILENAME),
                        eq(SOME_TYPE),
                        eq(SOME_FILESIZE))
        ).thenReturn(smallAvatar);

        when(croppingAvatarImageDataProviderFactory.createStreamsFrom(any(InputStream.class), any(Selection.class))).
                thenReturn(mock(AvatarImageDataProvider.class));

        // when
        final Response response = testObj.storeTemporaryAvatar(
                FRED,
                Avatar.Type.ISSUETYPE,
                ISSUETYPE_SUBJECT,
                Avatar.Size.SMALL,
                FILENAME,
                SOME_FILESIZE,
                request);

        final ArgumentCaptor<Selection> selectionArgumentCaptor = ArgumentCaptor.forClass(Selection.class);
        verify(croppingAvatarImageDataProviderFactory).createStreamsFrom(any(InputStream.class), selectionArgumentCaptor.capture());


        final Selection selection = selectionArgumentCaptor.getValue();
        assertThat(
                new Rectangle(0, 0, Avatar.Size.SMALL.getPixels(), Avatar.Size.SMALL.getPixels()),
                is(equalTo(new Rectangle(selection.getTopLeftX(), selection.getTopLeftY(), selection.getWidth(), selection.getHeight()))));
    }
}
