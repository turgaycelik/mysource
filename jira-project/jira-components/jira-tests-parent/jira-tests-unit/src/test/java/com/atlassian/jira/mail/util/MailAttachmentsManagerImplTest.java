package com.atlassian.jira.mail.util;

import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mail.TemplateUser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.StringStartsWith;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.net.URI;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class MailAttachmentsManagerImplTest
{

    @Rule
    public MockitoContainer initMockitoMocks = MockitoMocksInContainer.rule(this);

    @Mock private UserManager userManagerMock;
    @Mock private AvatarService avatarServiceMock;
    @Mock private AvatarManager avatarManagerMock;
    @Mock private ApplicationProperties applicationProperties;
    @AvailableInContainer @Mock private JiraAuthenticationContext jiraAuthenticationContext;
    private MailAttachmentsManagerImpl mailImagesManager;

    @Before
    public void setUp() throws Exception
    {
        mailImagesManager = new MailAttachmentsManagerImpl(avatarServiceMock, userManagerMock, avatarManagerMock, applicationProperties);
    }

    @Test
    public void testAddAvatarImage() throws Exception
    {
        String url = mailImagesManager.getAvatarUrl(new MockApplicationUser("user1"));

        assertThat(url, CoreMatchers.startsWith("cid:" + MailAttachmentsManagerImpl.CID_PREFIX));
    }

    @Test
    public void testAddAvatarImagesDoesNotDuplicateAttachmentsForSameUser() throws Exception
    {
        String username = "user1";
        MockApplicationUser user = new MockApplicationUser(username);
        MockUser oldUser = new MockUser(username); //To create TemplateUser

        when(userManagerMock.getUserByName(username)).thenReturn(user);

        mailImagesManager.getAvatarUrl(user);
        mailImagesManager.getAvatarUrl(TemplateUser.getUser(oldUser));
        mailImagesManager.getAvatarUrl(username);

        assertThat(mailImagesManager.getAttachmentsCount(), equalTo(1));
    }

    @Test
    public void testAddingMultipleTimeSameImageAlwaysReturnsSameCid() throws Exception
    {
        String username = "user1";
        MockApplicationUser user = new MockApplicationUser(username);
        MockUser oldUser = new MockUser(username); //To create TemplateUser

        when(userManagerMock.getUserByName(username)).thenReturn(user);

        final String cid1 = mailImagesManager.getAvatarUrl(user);
        final String cid2 = mailImagesManager.getAvatarUrl(TemplateUser.getUser(oldUser));
        final String cid3 = mailImagesManager.getAvatarUrl(username);

        assertThat(cid1, startsWith("cid:"));
        assertThat(cid2, startsWith("cid:"));
        assertThat(cid3, startsWith("cid:"));

        assertThat(cid1, equalTo(cid2));
        assertThat(cid2, equalTo(cid3));
    }

    @Test
    public void testAddAvatarShouldNotAddAttachmentIfUsingAnExternalGravatar() throws Exception
    {
        final ApplicationUser loggedInUser = new MockApplicationUser("admin");
        final ApplicationUser avatarUser = new MockApplicationUser("SomeUser");
        String exampleUrl = "http://example.org";

        when(jiraAuthenticationContext.getUser()).thenReturn(loggedInUser);
        when(avatarServiceMock.isUsingExternalAvatar(loggedInUser, avatarUser)).thenReturn(true);
        when(avatarServiceMock.getAvatarUrlNoPermCheck(avatarUser, null)).thenReturn(new URI(exampleUrl));

        String url = mailImagesManager.getAvatarUrl(avatarUser);

        assertThat(url, equalTo(exampleUrl));
    }

    @Test
    public void testAddAvatarShouldAddAttachmentIfGravatarIsEnabledButUserHasInternalAvatar() throws Exception
    {
        final ApplicationUser loggedInUser = new MockApplicationUser("admin");
        final ApplicationUser avatarUser = new MockApplicationUser("SomeUser");

        when(jiraAuthenticationContext.getUser()).thenReturn(loggedInUser);
        when(avatarServiceMock.isUsingExternalAvatar(loggedInUser, avatarUser)).thenReturn(false);

        String url = mailImagesManager.getAvatarUrl(avatarUser);

        assertThat(url, StringStartsWith.startsWith("cid:" + MailAttachmentsManagerImpl.CID_PREFIX));
    }

    @Test
    public void testGetAbsoluteUrl() throws Exception
    {
        when(applicationProperties.getString(APKeys.JIRA_BASEURL)).thenReturn("http://this/is/jira");

        assertThat(mailImagesManager.getAbsoluteUrl("/foo/bar"), equalTo("http://this/is/jira/foo/bar"));
        assertThat(mailImagesManager.getAbsoluteUrl("foo/bar"), equalTo("http://this/is/jira/foo/bar"));

        when(applicationProperties.getString(APKeys.JIRA_BASEURL)).thenReturn("https://this/is/jira/");

        assertThat(mailImagesManager.getAbsoluteUrl("/foo/bar"), equalTo("https://this/is/jira/foo/bar"));
        assertThat(mailImagesManager.getAbsoluteUrl("foo/bar"), equalTo("https://this/is/jira/foo/bar"));
    }
}
