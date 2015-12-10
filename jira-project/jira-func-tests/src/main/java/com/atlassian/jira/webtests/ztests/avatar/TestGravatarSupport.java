package com.atlassian.jira.webtests.ztests.avatar;

import java.util.List;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.rules.RestRule;
import com.atlassian.jira.testkit.client.restclient.User;
import com.atlassian.jira.testkit.client.restclient.UserClient;

import com.google.common.collect.Lists;
import com.meterware.httpunit.ClientProperties;
import com.meterware.httpunit.WebResponse;

import org.w3c.dom.Node;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST })
public class TestGravatarSupport extends FuncTestCase
{
    private static final String ADMIN_MD5 = "e64c7d89f26bd1972efa854d13d7dd61";
    private static final String FRED_MD5 = "6255165076a5e31273cbda50bb9f9636";

    private String adminGravatarSmall;
    private String adminGravatarNormal;
    private String adminGravatarLarge;

    private String fredGravatarSmall;
    private String fredGravatarNormal;
    private String fredGravatarLarge;
    private RestRule restRule;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        restRule = new RestRule(this);
        restRule.before();

        // JRA-29934: use the "mystery man" avatar supplied by Gravatar
        String defaultUserAvatar = "mm";

        adminGravatarSmall = String.format("http://www.gravatar.com/avatar/%s?d=%s&s=16", ADMIN_MD5, defaultUserAvatar);
        adminGravatarNormal = String.format("http://www.gravatar.com/avatar/%s?d=%s&s=24", ADMIN_MD5, defaultUserAvatar);
        adminGravatarLarge = String.format("http://www.gravatar.com/avatar/%s?d=%s&s=48", ADMIN_MD5, defaultUserAvatar);

        fredGravatarSmall = String.format("http://www.gravatar.com/avatar/%s?d=%s&s=16", FRED_MD5, defaultUserAvatar);
        fredGravatarNormal = String.format("http://www.gravatar.com/avatar/%s?d=%s&s=24", FRED_MD5, defaultUserAvatar);
        fredGravatarLarge = String.format("http://www.gravatar.com/avatar/%s?d=%s&s=48", FRED_MD5, defaultUserAvatar);

        // set up database
        administration.restoreBlankInstance();
        administration.generalConfiguration().useGravatars(true);
    }

    @Override
    public void tearDownTest()
    {
        restRule.after();
    }


    public void testGravatarShouldBeDisplayedInUserProfile() throws Exception
    {
        navigation.userProfile().gotoUserProfile("admin");

        assertThat(locator.css("img.avatar-image").getNode().getAttributes().getNamedItem("src").getTextContent(), equalTo(adminGravatarLarge));
        assertThat(locator.css("#content > header .aui-avatar img").getNode().getAttributes().getNamedItem("src").getTextContent(), equalTo(adminGravatarLarge));
    }

    public void testGravatarShouldBeDisplayedInIssueComments() throws Exception
    {
        String key = navigation.issue().createIssue("homosapien", "Bug", "an issue");

        navigation.issue().addComment(key, "comment 10000");

        Node adminAvatarLink = locator.css("#commentauthor_10000_verbose").getNode();
        List<String> adminAvatarChildClasses = Lists.newArrayList(adminAvatarLink.getFirstChild().getAttributes().getNamedItem("class").getTextContent().split("\\s+"));
        Node adminAvatarImage = locator.css("#commentauthor_10000_verbose img").getNode();
        assertThat("An AUI avatar is output within the verbose author link", adminAvatarChildClasses, hasItem("aui-avatar"));
        assertEquals(adminAvatarImage.getAttributes().getNamedItem("src").getTextContent(), adminGravatarSmall);


        navigation.login(FRED_USERNAME);
        navigation.issue().addComment(key, "comment 10001");

        Node fredAvatarLink = locator.css("#commentauthor_10001_concise").getNode();
        List<String> fredAvatarChildClasses = Lists.newArrayList(adminAvatarLink.getFirstChild().getAttributes().getNamedItem("class").getTextContent().split("\\s+"));
        Node fredAvatarImage = locator.css("#commentauthor_10001_concise img").getNode();
        assertThat("An AUI avatar is output within the concise author link", fredAvatarChildClasses, hasItem("aui-avatar"));
        assertEquals(fredAvatarImage.getAttributes().getNamedItem("src").getTextContent(), fredGravatarSmall);
    }

    public void testGravatarShouldBeDisplayedInApplicationHeaderProfileLink()
    {
        Node adminAvatarLink = locator.css("#header-details-user-fullname img").getNode();
        assertEquals(adminAvatarLink.getAttributes().getNamedItem("src").getTextContent(), adminGravatarNormal);

        navigation.login(FRED_USERNAME);

        Node fredAvatarLink = locator.css("#header-details-user-fullname img").getNode();
        assertEquals(fredAvatarLink.getAttributes().getNamedItem("src").getTextContent(), fredGravatarNormal);
    }

    public void testGravatarShouldBeDisplayedInUserResource() throws Exception
    {
        UserClient userClient = new UserClient(environmentData);

        User fred = userClient.get(FRED_USERNAME);
        assertThat(fred.avatarUrls, hasEntry("16x16", fredGravatarSmall));
        assertThat(fred.avatarUrls, hasEntry("24x24", fredGravatarNormal));
        assertThat(fred.avatarUrls, hasEntry("48x48", fredGravatarLarge));
    }

    /*
     * Tests that we handle backward compatibility for plugins that build the avatar URL instead of calling the
     * AvatarService.
     *
     * This is only possible when the ownerId param is provided.
     */
    public void testAvatarServletShouldRedirectToGravatar() throws Exception
    {
        ClientProperties clientProperties = tester.getDialog().getWebClient().getClientProperties();
        boolean redirect = clientProperties.isAutoRedirect();
        clientProperties.setAutoRedirect(false);
        try
        {
            // make sure we get a 302 redirect for large avatars
            WebResponse largeAvatarResponse = restRule.GET(String.format("secure/useravatar?ownerId=%s", FRED_USERNAME));
            assertThat(largeAvatarResponse.getResponseCode(), equalTo(302));
            assertThat(largeAvatarResponse.getHeaderField("Location"), equalTo(fredGravatarLarge));

            // make sure we get a 302 redirect for small avatars
            WebResponse smallAvatarResponse = restRule.GET(String.format("secure/useravatar?ownerId=%s&size=xsmall", FRED_USERNAME));
            assertThat(smallAvatarResponse.getResponseCode(), equalTo(302));
            assertThat(smallAvatarResponse.getHeaderField("Location"), equalTo(fredGravatarSmall));

            // make sure we get a 302 redirect for normal avatars
            WebResponse normalAvatarResponse = restRule.GET(String.format("secure/useravatar?ownerId=%s&size=small", FRED_USERNAME));
            assertThat(normalAvatarResponse.getResponseCode(), equalTo(302));
            assertThat(normalAvatarResponse.getHeaderField("Location"), equalTo(fredGravatarNormal));

            // anon requests should get a 404
            navigation.logout();
            WebResponse anonAvatarResponse = restRule.GET(String.format("secure/useravatar?ownerId=%s", FRED_USERNAME));
            assertThat(anonAvatarResponse.getResponseCode(), equalTo(404));
        }
        finally
        {
            clientProperties.setAutoRedirect(redirect);
        }
    }
}
