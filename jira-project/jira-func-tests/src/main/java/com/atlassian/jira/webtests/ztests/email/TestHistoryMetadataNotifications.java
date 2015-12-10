package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataParticipant;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.api.issue.ResourceRef;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.TransitionsClient;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import org.junit.Before;

import java.util.List;
import javax.mail.internet.MimeMessage;

@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
public class TestHistoryMetadataNotifications extends EmailFuncTestCase
{
    private IssueClient issueClient;
    private TransitionsClient transitionsClient;

    private final HistoryMetadata metadataWIthRawDescription = createMetdata()
            .emailDescriptionKey("nonexistant.description.key.xxxxx")
            .emailDescription("custom description")
            .build();
    private final HistoryMetadataParticipant.HistoryMetadataParticipantBuilder actor = HistoryMetadataParticipant
            .builder("<b>actor</b>", "actorType")
            .displayName("<b>Bitbucket</b> User")
            .url("http://bitbucket.org");
    private final HistoryMetadata metadataWithActor = createMetdata().actor(actor.build()).build();
    private final HistoryMetadata metadataWithActorKey = createMetdata()
            .actor(actor.displayNameKey("reference.changehistory.customemaildescription.cause").build())
            .cause(HistoryMetadataParticipant.builder("review", "review").build()).build();

    @Before
    public void setUpTest()
    {
        super.setUpTest();
        this.issueClient = new IssueClient(environmentData);
        this.transitionsClient = new TransitionsClient(environmentData);

        administration.restoreData("TestIssueNotificationsCurrentAssignee.xml");
        configureAndStartSmtpServerWithNotify();
        administration.generalConfiguration().setAllowUnassignedIssues(true);
        administration.generalConfiguration().setBaseUrl(environmentData.getBaseUrl().toString());
    }

    public void testUpdateWithCustomDescription() throws Exception
    {
        // having
        updateSummary("COW-1", "custom description", metadataWIthRawDescription);
        updateSummary("COW-2", "custom description key", createMetdata().build());

        // when
        flushMailQueueAndWait(3);
        final List<MimeMessage> emails = getMessagesForRecipient("admin@example.com");

        // then
        assertEmailBodyContainsLine(emails.get(0), ".*Administrator.*updated.*an issue.*custom description");
        assertEmailBodyContainsLine(emails.get(1), ".*Automated transition updated an issue - triggered by.*Administrator.*RemoteSystem.*pull request #1");
    }

    public void testUpdateWithCustomDescriptionInTextFormat() throws Exception
    {
        // having
        backdoor.userProfile().changeUserNotificationType(ADMIN_USERNAME, "text");
        updateSummary("COW-1", "custom description", metadataWIthRawDescription);
        updateSummary("COW-2", "custom description key", createMetdata().build());
        updateSummary("COW-3", "plain update", null);

        // when
        flushMailQueueAndWait(4);
        final List<MimeMessage> emails = getMessagesForRecipient("admin@example.com");

        // then
        assertEmailBodyContainsLine(emails.get(0), "Administrator updated COW-1: custom description");
        assertEmailBodyContainsLine(emails.get(1), "Automated transition updated an issue - triggered by Administrator's RemoteSystem pull request #1");
        assertEmailBodyContainsLine(emails.get(2), "Administrator updated COW-3:");

    }

    public void testUpdateWithCustomAvatar() throws Exception
    {
        // having
        HistoryMetadata withGeneratorAvatar = createMetdata()
                .generator(HistoryMetadataParticipant.builder("generatorAvatar", "generator").avatarUrl("http://localhost/genavatar").build())
                .build();
        HistoryMetadata withActorAvatar = createMetdata()
                .actor(HistoryMetadataParticipant.builder("actorAvatar", "actor").avatarUrl("http://localhost/actoravatar").build())
                .build();
        HistoryMetadata withRelativeAvatar = createMetdata()
                .generator(HistoryMetadataParticipant.builder("relativeAvatar", "generator")
                        .avatarUrl("/secure/projectavatar?pid=10000&avatarId=10011")
                        .build())
                .build();

        updateSummary("COW-1", "generatorAvatar", withGeneratorAvatar);
        updateSummary("COW-2", "actorAvatar", withActorAvatar);
        updateSummary("COW-3", "relativeAvatar", withRelativeAvatar);

        // when
        flushMailQueueAndWait(4);
        final List<MimeMessage> emails = getMessagesForRecipient("admin@example.com");

        // then
        assertEmailBodyContainsLine(emails.get(0), ".*<img id=\"header-avatar-image\" class=\"image_fix\" src=\"http://localhost/genavatar\".*");
        assertEmailBodyContainsLine(emails.get(1), ".*<img id=\"header-avatar-image\" class=\"image_fix\" src=\"http://localhost/actoravatar\".*");

        assertEmailBodyContainsLine(emails.get(2), ".*<img id=\"header-avatar-image\" class=\"image_fix\" src=\"cid:jira-generated-image-static-projectavatar.*");
        assertEmailBodyContainsLine(emails.get(2), "Content-ID: <jira-generated-image-static-projectavatar.*");
        assertEmailHasNumberOfParts(emails.get(2), 5);

    }

    public void testTransitionWithCustomActor() throws Exception
    {
        // having
        transitionsClient = transitionsClient.anonymous();
        transition("COW-1", metadataWithActor, 4);
        transition("COW-2", metadataWithActorKey, 4);

        // when
        flushMailQueueAndWait(3);
        final List<MimeMessage> emails = getMessagesForRecipient("admin@example.com");

        // then
        assertEmailBodyContainsLine(emails.get(0),
                ".*Automated transition updated an issue - triggered by <a href=\"http://bitbucket.org\".*&lt;b&gt;Bitbucket&lt;/b&gt; User</a>'s RemoteSystem pull request #1.*",
                "Change By:",
                ".*<a href=\"http://bitbucket.org\".*&lt;b&gt;Bitbucket&lt;/b&gt; User</a>.*");
        assertEmailBodyContainsLine(emails.get(1),
                ".*Automated transition updated an issue - triggered by <a href=\"http://bitbucket.org\".*pull request &lt;b&gt;actor&lt;/b&gt;</a>'s RemoteSystem review.*",
                "Change By:",
                ".*a href=\"http://bitbucket.org\".*pull request &lt;b&gt;actor&lt;/b&gt;</a>.*");
    }

    public void testTransitionWithCustomActorTextFormat() throws Exception
    {
        // having
        backdoor.userProfile().changeUserNotificationType(ADMIN_USERNAME, "text");
        transitionsClient = transitionsClient.anonymous();
        transition("COW-1", metadataWithActor, 4); // start progress
        transition("COW-2", metadataWithActorKey, 4);
        transition("COW-1", metadataWithActor, 301); // stop progress
        transition("COW-1", metadataWithActor, 5); // resolve
        transition("COW-1", metadataWithActor, 3); // reopen
        transition("COW-1", metadataWithActor, 2); // close

        transitionsClient = transitionsClient.loginAs(ADMIN_USERNAME);
        transition("COW-3", metadataWithActor, 2); // close

        // when
        flushMailQueueAndWait(12);
        final List<MimeMessage> emails = getMessagesForRecipient("admin@example.com");

        // then
        assertEmailBodyContainsLine(emails.get(0), "Automated transition updated an issue - triggered by Bitbucket\\s+User's RemoteSystem pull request #1");
        assertEmailBodyContainsLine(emails.get(1), "Automated transition updated an issue - triggered by pull request\\s+actor's RemoteSystem review");
        assertEmailBodyContainsLine(emails.get(2), "Automated transition updated an issue - triggered by Bitbucket\\s+User's RemoteSystem pull request #1");
        assertEmailBodyContainsLine(emails.get(3), "Automated transition updated an issue - triggered by Bitbucket\\s+User's RemoteSystem pull request #1");
        assertEmailBodyContainsLine(emails.get(4), "Automated transition updated an issue - triggered by Bitbucket\\s+User's RemoteSystem pull request #1");
        assertEmailBodyContainsLine(emails.get(5), "Automated transition updated an issue - triggered by Bitbucket\\s+User's RemoteSystem pull request #1");
        assertEmailBodyContainsLine(emails.get(6), "Automated transition updated an issue - triggered by Administrator's RemoteSystem pull request #1");
    }

    public void testTransitionIssueTextFormat() throws Exception
    {
        // having
        backdoor.userProfile().changeUserNotificationType(ADMIN_USERNAME, "text");
        transition("COW-1", null, 4); // start progress
        transition("COW-1", null, 301); // stop progress
        transition("COW-1", null, 5); // resolve
        transition("COW-1", null, 3); // reopen
        transition("COW-1", null, 2); // close

        // when
        flushMailQueueAndWait(10);
        final List<MimeMessage> emails = getMessagesForRecipient("admin@example.com");

        // then
        assertEmailBodyContainsLine(emails.get(0), "Work on COW-1 started by Administrator.");
        assertEmailBodyContainsLine(emails.get(1), "Work on COW-1 stopped by Administrator.");
        assertEmailBodyContainsLine(emails.get(2), "Administrator resolved COW-1.");
        assertEmailBodyContainsLine(emails.get(3), "Administrator reopened COW-1.");
        assertEmailBodyContainsLine(emails.get(4), "Administrator closed COW-1.");
    }

    private void transition(String issueKey, final HistoryMetadata metadataWithActor, int transitionId)
    {
        transitionsClient.postResponse(issueKey, new IssueUpdateRequest()
                .transition(ResourceRef.withId(Integer.valueOf(transitionId).toString()))
                .historyMetadata(metadataWithActor));
    }

    private HistoryMetadata.HistoryMetadataBuilder createMetdata()
    {
        return HistoryMetadata.builder("test")
                .cause(HistoryMetadataParticipant.builder("#1", "causetype")
                        .displayNameKey("reference.changehistory.customemaildescription.cause")
                        .build())
                .generator(HistoryMetadataParticipant.builder("RemoteSystem", "systemtype").build())
                .emailDescriptionKey("reference.changehistory.customemaildescription");
    }

    private void updateSummary(String issueKey, String summary, final HistoryMetadata metadata)
    {
        issueClient.update(issueKey, new IssueUpdateRequest()
                .fields(new IssueFields().summary(summary))
                .historyMetadata(metadata));
    }
}
