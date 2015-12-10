package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.jira.functest.framework.backdoor.PermissionSchemesControlExt;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataParticipant;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.ResetDataOnce;
import com.atlassian.jira.pageobjects.pages.viewissue.HistoryModule;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static com.atlassian.jira.pageobjects.pages.viewissue.HistoryModule.IssueHistoryData;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@ResetDataOnce
public class TestViewIssueHistory extends BaseJiraWebTest
{
    private static final String NONEXISTANT_I18_KEY = "nonexistant.key.key.key.random";
    private static final String CUSTOM_DESCRIPTION_KEY = "reference.changehistory.customdescription";
    private static final String CUSTOM_CAUSE_KEY = "reference.changehistory.customescription.cause";
    private IssueClient issueClient;

    @BeforeClass
    public static void allowAnnonsInMKY() {
        final Long anonSchemeId = backdoor.permissionSchemes().copyDefaultScheme("anonymous-allowed");
        ((PermissionSchemesControlExt)backdoor.permissionSchemes()).addEveryonePermission(anonSchemeId, Permissions.BROWSE);
        ((PermissionSchemesControlExt)backdoor.permissionSchemes()).addEveryonePermission(anonSchemeId, Permissions.EDIT_ISSUE);
        final Long mkyProjectId = backdoor.project().getProjectId("MKY");
        backdoor.project().setPermissionScheme(mkyProjectId, anonSchemeId);
    }

    @Before
    public void setUp()
    {
        this.issueClient = new IssueClient(jira.environmentData());
    }

    @Test
    public void testRenderHistory_noMetadata() throws Exception
    {
        // having
        final IssueCreateResponse issue = backdoor.issues().createIssue("MKY", "xxx");
        updateAsAdmin(issue, new IssueUpdateRequest()
                .fields(new IssueFields().summary("newmmary")));
        updateAsAnon(issue, new IssueUpdateRequest()
                .fields(new IssueFields().summary("anonymous summary")));

        // when
        final List<IssueHistoryData> history = renderHistory(issue);

        // then
        assertThat(history, hasSize(3));
        assertThat(history.get(0).getActionDescription(), startsWith("Administrator created issue"));
        assertFieldChange(history.get(1), "Summary", "xxx", "newmmary");
        assertThat(history.get(1).getActionDescription(), startsWith("Administrator made changes"));
        assertThat(history.get(2).getActionDescription(), startsWith("Anonymous made changes"));
    }

    @Test
    public void testRenderHistory_freeformDescriptionInMetadata() throws Exception
    {
        // having
        final IssueCreateResponse issue = backdoor.issues().createIssue("MKY", "xxx");
        final HistoryMetadata withGeneratorAvatar = HistoryMetadata.builder("test")
                .description("This is a test <b>description</b> of history")
                .generator(HistoryMetadataParticipant.builder("foo", "bar").avatarUrl("http://localhost/generatorAvatar").build())
                .build();
        updateAsAdmin(issue, new IssueUpdateRequest()
                .fields(new IssueFields().summary("newmmary"))
                .historyMetadata(HistoryMetadata.builder("test").description("This is a test <b>description</b> of history").build()));
        updateAsAdmin(issue, new IssueUpdateRequest()
                .fields(new IssueFields().summary("summary3"))
                .historyMetadata(withGeneratorAvatar));
        updateAsAnon(issue, new IssueUpdateRequest()
                .fields(new IssueFields().summary("summary4"))
                .historyMetadata(withGeneratorAvatar));

        // when
        final List<IssueHistoryData> history = renderHistory(issue);

        // then
        assertThat(history.get(1).getActionDescription(), startsWith("Administrator made changes"));
        assertThat(history.get(1).getActionDescription(), containsString("This is a test <b>description</b> of history"));
        assertThat(history.get(1).getAvatarUrls(), Matchers.<String>contains(containsString(jira.environmentData().getContext() + "/secure/useravatar")));
        assertThat(history.get(2).getAvatarUrls(), Matchers.<String>contains(containsString(withGeneratorAvatar.getGenerator().getAvatarUrl())));
        assertThat(history.get(3).getAvatarUrls(), Matchers.<String>contains(containsString(withGeneratorAvatar.getGenerator().getAvatarUrl())));
    }

    @Test
    public void testRenderHistory_withRelativeAvatar() throws Exception
    {
        // having
        final IssueCreateResponse issue = backdoor.issues().createIssue("MKY", "xxx");
        final String generatorAvatar = "/secure/projectavatar?pid=10001&avatarId=10011";
        final HistoryMetadata withGeneratorAvatar = createMetadataWithDescriptionKey()
                .generator(HistoryMetadataParticipant.builder("generator", "generator").avatarUrl(generatorAvatar).build())
                .build();
        final String userAvatar = "/secure/projectavatar?pid=10001&avatarId=10010";
        final HistoryMetadata withUserAvatar = createMetadataWithDescriptionKey()
                .generator(HistoryMetadataParticipant.builder("generator", "generator").avatarUrl(userAvatar).build())
                .build();

        updateAsAnon(issue, createUpdateRequest("yyy", withGeneratorAvatar));
        updateAsAnon(issue, createUpdateRequest("zzz", withUserAvatar));

        // when
        final List<IssueHistoryData> history = renderHistory(issue);

        // then
        assertThat(Iterables.getOnlyElement(history.get(1).getAvatarUrls()), equalTo(jira.environmentData().getBaseUrl().toString() + generatorAvatar));
        assertThat(Iterables.getOnlyElement(history.get(2).getAvatarUrls()), equalTo(jira.environmentData().getBaseUrl().toString() + userAvatar));
    }

    @Test
    public void testRenderHistory_richDescriptionInMetadata() throws Exception
    {
        // having
        final IssueCreateResponse issue = backdoor.issues().createIssue("MKY", "0");
        final HistoryMetadata metadata = createMetadata();

        updateAsAdmin(issue, createUpdateRequest("1", metadata));
        updateAsAnon(issue, createUpdateRequest("2", metadata));
        updateAsAnon(issue, createUpdateRequest("3", createMetadataNoLinks()));
        updateAsAnon(issue, createUpdateRequest("4", HistoryMetadata.builder("noKey").descriptionKey(NONEXISTANT_I18_KEY)
                .description("Fallback description").build()));
        updateAsAdmin(issue, createUpdateRequest("5", createMetadataWithDescriptionKey()
                .generator(HistoryMetadataParticipant.builder("generator1", "type").build()).build()));
        updateAsAnon(issue, createUpdateRequest("6", createMetadataWithDescriptionKey()
                .generator(HistoryMetadataParticipant.builder("generator1", "type").build()).actor(metadata.getActor()).build()));
        updateAsAnon(issue, createUpdateRequest("7", HistoryMetadata.builder("noDescription").build()));

        // when
        final List<IssueHistoryData> history = renderHistory(issue);
        final IssueHistoryData historyWithUserContext = history.get(1);
        final IssueHistoryData historyWithoutUserContextWithActor = history.get(2);
        final IssueHistoryData historyWithRawDisplayName = history.get(3);
        final IssueHistoryData historyWithFallbackDescription = history.get(4);
        final IssueHistoryData historyWithUserAndNoGeneratorAvatar = history.get(5);
        final IssueHistoryData historyWithActorAvatarAndNoGeneratorAvatar = history.get(6);
        final IssueHistoryData historyWithNoDescriptionInMetadata = history.get(7);

        // then
        assertDescriptionContent(historyWithUserContext, "Administrator", "generator1", "Just cause #42");
        assertThat("Should show the avatar for the generator only", historyWithUserContext.getAvatarUrls(), Matchers.<String>contains(
                equalTo(metadata.getGenerator().getAvatarUrl()), equalTo(metadata.getCause().getAvatarUrl())
        ));
        assertThat("Should show links for user, generator and cause", historyWithUserContext.getLinks(), Matchers.<String>contains(
                containsString(jira.environmentData().getContext() + "/secure/ViewProfile"), equalTo(metadata.getGenerator().getUrl()), equalTo(metadata.getCause().getUrl())
        ));
        assertFieldChange(historyWithUserContext, "Summary", "0", "1");

        assertDescriptionContent(historyWithoutUserContextWithActor, "user1", "generator1", "Just cause #42");
        assertThat("Should show avatar for the generator only", historyWithoutUserContextWithActor.getAvatarUrls(), Matchers.<String>contains(
                equalTo(metadata.getGenerator().getAvatarUrl()), equalTo(metadata.getCause().getAvatarUrl())
        ));
        assertThat("Should show links for user, generator and cause", historyWithoutUserContextWithActor.getLinks(), Matchers.<String>contains(
                equalTo(metadata.getActor().getUrl()), equalTo(metadata.getGenerator().getUrl()), equalTo(metadata.getCause().getUrl())
        ));
        assertFieldChange(historyWithoutUserContextWithActor, "Summary", "1", "2");

        assertThat("Should have no extra spaces", historyWithRawDisplayName.getActionDescription(),
                startsWith("This happened: <b>user2</b>=actor;<b>generator2</b>=generator;<b>cause2</b>=cause; - "));
        assertThat(historyWithRawDisplayName.getAvatarUrls(), Matchers.<String>emptyIterable());
        assertThat(historyWithRawDisplayName.getLinks(), Matchers.<String>emptyIterable());
        assertFieldChange(historyWithRawDisplayName, "Summary", "2", "3");

        assertThat("Should use description field as description key is invalid",
                historyWithFallbackDescription.getActionDescription(), startsWith("Anonymous made changes Fallback description - "));

        assertDescriptionContent(historyWithUserAndNoGeneratorAvatar, "Administrator", "", "");
        assertThat("Should show an avatar for the JIRA user, since no generator avatar provided",
                historyWithUserAndNoGeneratorAvatar.getAvatarUrls(), Matchers.<String>contains(
                    containsString(jira.environmentData().getContext() + "/secure/useravatar")
        ));
        assertThat("Should contain link for the JIRA user profile",
                historyWithUserAndNoGeneratorAvatar.getLinks(), Matchers.<String>contains(
                    containsString(jira.environmentData().getContext() + "/secure/ViewProfile")
        ));

        assertDescriptionContent(historyWithActorAvatarAndNoGeneratorAvatar, "user1", "generator1", "");
        assertThat("Should show an avatar for the actor, since no generator avatar provided",
                historyWithActorAvatarAndNoGeneratorAvatar.getAvatarUrls(), Matchers.<String>contains(
                        containsString(metadata.getActor().getAvatarUrl())
                ));
        assertThat("Should contain link for the actor",
                historyWithActorAvatarAndNoGeneratorAvatar.getLinks(), Matchers.<String>contains(
                        containsString(metadata.getActor().getUrl())
                ));

        assertThat("Should use standard description, since none provided in metadata",
                historyWithNoDescriptionInMetadata.getActionDescription(), startsWith("Anonymous made changes -"));
    }

    private IssueUpdateRequest createUpdateRequest(String summary, final HistoryMetadata metadata)
    {
        return new IssueUpdateRequest()
                .fields(new IssueFields().summary(summary))
                .historyMetadata(metadata);
    }

    private void updateAsAnon(final IssueCreateResponse issue, final IssueUpdateRequest updateRequest)
    {
        issueClient.anonymous().update(issue.key(), updateRequest);
    }

    private void updateAsAdmin(final IssueCreateResponse issue, final IssueUpdateRequest updateRequest)
    {
        issueClient.loginAs("admin").update(issue.key(), updateRequest);
    }

    private void assertDescriptionContent(final IssueHistoryData history, final String actor, final String generator, final String cause)
    {
        String desc = history.getActionDescription();
        assertThat(desc, startsWith("This happened:"));
        assertThat(desc, containsString(actor + "=actor;"));
        assertThat(desc, containsString(generator + "=generator;"));
        assertThat(desc, containsString(cause + "=cause;"));
    }

    private HistoryMetadata createMetadata()
    {
        return createMetadataWithDescriptionKey()
                .actor(HistoryMetadataParticipant.builder("user1", "comittter")
                        .displayNameKey(NONEXISTANT_I18_KEY)
                        .avatarUrl("http://localhost/committer.png").url("http://localhost/committer").build())
                .generator(HistoryMetadataParticipant.builder("generator-id", "system")
                        .displayName("generator1")
                        .avatarUrl("http://localhost/generator.png").url("http://localhost/generator").build())
                .cause(HistoryMetadataParticipant.builder("#42", "event")
                        .displayNameKey(CUSTOM_CAUSE_KEY)
                        .avatarUrl("http://localhost/cause.png").url("http://localhost/event").build())
                .build();
    }

    private HistoryMetadata createMetadataNoLinks()
    {
        return createMetadataWithDescriptionKey()
                .actor(HistoryMetadataParticipant.builder("<b>user2</b>", "comittter").build())
                .generator(HistoryMetadataParticipant.builder("<b>generator2</b>", "system").build())
                .cause(HistoryMetadataParticipant.builder("<b>cause2</b>", "event").build())
                .build();
    }

    private HistoryMetadata.HistoryMetadataBuilder createMetadataWithDescriptionKey()
    {
        return HistoryMetadata.builder("test").descriptionKey(CUSTOM_DESCRIPTION_KEY);
    }

    private List<IssueHistoryData> renderHistory(final IssueCreateResponse issue)
    {
        final HistoryModule historyTab = jira.goToViewIssue(issue.key()).getActivitySection().historyModule();
        return ImmutableList.copyOf(historyTab.getHistoryItems().now());
    }

    private void assertFieldChange(final IssueHistoryData historyAction1, final String field, final String from, final String to)
    {
        assertThat(historyAction1.getFieldName(), equalTo(field));
        assertThat(historyAction1.getOldValue(), equalTo(from));
        assertThat(historyAction1.getNewValue(), equalTo(to));
    }
}
