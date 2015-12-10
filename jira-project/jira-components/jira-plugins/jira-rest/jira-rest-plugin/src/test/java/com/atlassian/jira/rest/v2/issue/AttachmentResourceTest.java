package com.atlassian.jira.rest.v2.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.config.ConstantsService;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentConstants;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.issue.thumbnail.ThumbnailedImage;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.opensymphony.module.propertyset.memory.MemoryPropertySet;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpStatus;
import org.easymock.EasyMock;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 * @since v4.2
 */
public class AttachmentResourceTest extends TestCase
{
    static final String JIRA_BASE_URI = "http://localhost:8090/jira/";

    protected UserManager userManager;
    private JiraAuthenticationContext authContext;
    private ConstantsService constantsService;
    private VelocityRequestContextFactory velocityReqCtxFactory;
    private ResourceUriBuilder uriBuilder;
    private BeanBuilderFactory beanBuilderFactory;
    private AttachmentManager attachmentManager;
    private I18nHelper i18n;
    private AttachmentService attachmentService;
    private ThumbnailManager thumbnailManager;
    private ContextUriInfo contextUriInfo;
    private AvatarService avatarService;
    private IssueManager issueManager;
    private PermissionManager permissionManager;


    public void testAttachmentResourceWithAThumbnailWithSpacesInTheFileName() throws Exception
    {
        final long attachmentId = 100;
        final GenericValue attachment1 = new MockGenericValue(AttachmentConstants.ATTACHMENT_ENTITY_NAME, EasyMap.build("id", attachmentId, "issue", new Long(1), "filename", "a file with spaces in the name",
                "filesize", 123L));

        final User user = new MockUser("mockUser", "Mock User", "mock@user.org");
        final JiraBaseUrls mockJiraBaseUrls = Mockito.mock(JiraBaseUrls.class);
        Mockito.when(mockJiraBaseUrls.restApi2BaseUrl()).thenReturn(JIRA_BASE_URI + "rest/api/2/");

        MemoryPropertySet propertySet = new MemoryPropertySet();
        propertySet.init(null, null);
        propertySet.setString("mime-type", "img/png");

        final Attachment attachment = new Attachment(issueManager, attachment1, propertySet);

        AttachmentBeanBuilder attachmentBeanBuilder = new AttachmentBeanBuilder(
                mockJiraBaseUrls, thumbnailManager, attachment);

        final String thumbnailName = "my name with space.png";
        final String encodedThumbnailName = "my+name+with+space.png";

        final Thumbnail thumbnail = new Thumbnail(100, 200, thumbnailName, attachmentId, Thumbnail.MimeType.PNG);
        final ThumbnailedImage thumbnailedImage = new MockThumbnailedImage(thumbnail, encodedThumbnailName);

        expect(authContext.getLoggedInUser()).andReturn(user).anyTimes();
        MockIssue mockIssue = new MockIssue();
        expect(issueManager.getIssueObject(anyLong())).andReturn(mockIssue).anyTimes();
        expect(permissionManager.hasPermission(Permissions.BROWSE, mockIssue, user)).andReturn(true).anyTimes();
        expect(attachmentManager.attachmentsEnabled()).andReturn(Boolean.TRUE).anyTimes();
        expect(attachmentService.getAttachment(any(JiraServiceContext.class), EasyMock.eq(new Long(attachmentId)))).andReturn(attachment).anyTimes();
        expect(thumbnailManager.getThumbnail(attachment)).andReturn(thumbnail).anyTimes();
        expect(thumbnailManager.toThumbnailedImage(thumbnail)).andStubReturn(thumbnailedImage);
        expect(beanBuilderFactory.newAttachmentBeanBuilder(attachment)).andReturn(attachmentBeanBuilder);
        expect(contextUriInfo.getBaseUriBuilder()).andReturn(UriBuilder.fromUri(JIRA_BASE_URI)).anyTimes();
        expect(userManager.getUser(any(String.class))).andReturn(user).anyTimes();
        expect(avatarService.getAvatarAbsoluteURL(any(User.class), any(String.class), any(Avatar.Size.class))).andReturn(UriBuilder.fromUri("http://avatar/url").build()).anyTimes();

        replayMocks();

        final AttachmentResource attachmentResource = new AttachmentResource(attachmentService, attachmentManager,
                permissionManager, authContext, beanBuilderFactory, i18n, contextUriInfo);
        final Response resp = attachmentResource.getAttachment("100");
        assertEquals(HttpStatus.SC_OK, resp.getStatus());
        final AttachmentBean attachmentBean = (AttachmentBean) resp.getEntity();

        assertTrue(attachmentBean.getThumbnail().startsWith(JIRA_BASE_URI));
        assertEquals("Spaces should be encoded", -1, attachmentBean.getThumbnail().indexOf(' '));
        assertEquals(123, attachmentBean.getSize());

    }

    @Override
    protected void setUp() throws Exception
    {
        authContext = createMock(JiraAuthenticationContext.class);
        constantsService = createMock(ConstantsService.class);
        velocityReqCtxFactory = createMock(VelocityRequestContextFactory.class);
        uriBuilder = createMock(ResourceUriBuilder.class);
        beanBuilderFactory = createMock(BeanBuilderFactory.class);
        contextUriInfo = createMock(ContextUriInfo.class);
        i18n = createMock(I18nHelper.class);
        userManager = createMock(UserManager.class);
        thumbnailManager = createMock(ThumbnailManager.class);
        attachmentManager = createMock(AttachmentManager.class);
        attachmentService = createMock(AttachmentService.class);
        avatarService = createMock(AvatarService.class);
        issueManager = createMock(IssueManager.class);
        permissionManager = createMock(PermissionManager.class);

        final MockComponentWorker worker = new MockComponentWorker();
        worker.addMock(AvatarService.class, avatarService);
        ComponentAccessor.initialiseWorker(worker);
    }

    protected void replayMocks(Object... mocks)
    {
        replay(mocks);
        replay(
                beanBuilderFactory,
                contextUriInfo,
                i18n,
                userManager,
                thumbnailManager,
                attachmentManager,
                attachmentService,
                authContext,
                constantsService,
                velocityReqCtxFactory,
                uriBuilder,
                avatarService,
                issueManager,
                permissionManager
        );
    }

    /* copied from com.atlassian.jira.mock.matcher.EasyMockMatcherUtils which is not accessible by this project */
    private static <T> T any(Class<T> argumentType)
    {
        notNull("argumentType", argumentType);
        return argumentType.cast(EasyMock.anyObject());
    }


    private class MockThumbnailedImage implements ThumbnailedImage
    {
        private final Thumbnail thumbnail;
        private final String encodedFilename;

        public MockThumbnailedImage(Thumbnail thumbnail, String encodedFilename) {this.thumbnail = thumbnail;
            this.encodedFilename = encodedFilename;
        }

        @Override
        public int getHeight() {return thumbnail.getHeight();}

        @Override
        public int getWidth() {return thumbnail.getWidth();}

        @Override
        public String getFilename() {return thumbnail.getFilename();}

        @Override
        public long getAttachmentId() {return thumbnail.getAttachmentId();}

        @Override
        public String getMimeType() {return thumbnail.getMimeType().toString();}

        @Override
        public String getImageURL()
        {
            return String.format("%s/secure/thumbnail/%s/%s", JIRA_BASE_URI, thumbnail.getAttachmentId(), encodedFilename);
        }
    }
}
