package com.atlassian.jira.webtests.ztests.upgrade.tasks;


import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.RemoteIssueLink;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.UPGRADE_TASKS })
public class TestUpgradeTask707 extends FuncTestCase
{
    private static class Backups
    {
        private static class TrackbacksEnabledBy
        {
            private static final String DATABASE_VALUE = "TestUpgradeTask707/trackbacks-enabled-by-database-value.xml";
            private static final String DEFAULT_PROPERTY_VALUE =
                    "TestUpgradeTask707/trackbacks-enabled-by-default-property-value.xml";
        }
        private static class TrackbacksDisabledBy
        {
            private static final String DATABASE_VALUE = "TestUpgradeTask707/trackbacks-disabled-by-database-value.xml";
        }
    }

    public void testUpgradeTaskMigratesIncomingTrackbacksToIssueLinksIfTrackbacksWereEnabledInTheDatabase()
    {
        restoreDataWithVersionCheck(Backups.TrackbacksEnabledBy.DATABASE_VALUE);

        IssueClient issueClient = new IssueClient(getEnvironmentData());

        List<RemoteIssueLink> remoteIssueLinks = issueClient.getRemoteIssueLinks("BULK-3");

        CollectionBuilder<RemoteIssueLink> builder = CollectionBuilder.newBuilder();
        builder.add(createLink(10010, "My Excerpt", "Foo Bar", "http://www.bar.com/", "Foo"));
        builder.add(createLink(10011, "<iframe src=\"http://www", "Foo Bar", "http://www.bar2.com/", null));
        builder.add(createLink(10012, "ssss", "Foo Bar", "http://www.bar3.com/", "<iframe "));
        builder.add(createLink(10020, "Excerpt", "Test", "http://www.google1.com", "Blog N"));

        List<RemoteIssueLink> expected = builder.asList();
        assertEquals(expected.size(), remoteIssueLinks.size());
        compareLinks(expected.get(0), remoteIssueLinks.get(0));
        compareLinks(expected.get(1), remoteIssueLinks.get(1));
        compareLinks(expected.get(2), remoteIssueLinks.get(2));
        compareLinks(expected.get(3), remoteIssueLinks.get(3));
    }

    public void testUpgradeTaskMigratesIncomingTrackbacksToIssueLinksIfTrackbacksWereEnabledDueToTheDefaultValue()
    {
        restoreDataWithVersionCheck(Backups.TrackbacksEnabledBy.DEFAULT_PROPERTY_VALUE);

        IssueClient issueClient = new IssueClient(getEnvironmentData());

        List<RemoteIssueLink> remoteIssueLinks = issueClient.getRemoteIssueLinks("BULK-3");

        CollectionBuilder<RemoteIssueLink> builder = CollectionBuilder.newBuilder();
        builder.add(createLink(10010, "My Excerpt", "Foo Bar", "http://www.bar.com/", "Foo"));
        builder.add(createLink(10011, "<iframe src=\"http://www", "Foo Bar", "http://www.bar2.com/", null));
        builder.add(createLink(10012, "ssss", "Foo Bar", "http://www.bar3.com/", "<iframe "));
        builder.add(createLink(10020, "Excerpt", "Test", "http://www.google1.com", "Blog N"));

        List<RemoteIssueLink> expected = builder.asList();
        assertEquals(expected.size(), remoteIssueLinks.size());
        compareLinks(expected.get(0), remoteIssueLinks.get(0));
        compareLinks(expected.get(1), remoteIssueLinks.get(1));
        compareLinks(expected.get(2), remoteIssueLinks.get(2));
        compareLinks(expected.get(3), remoteIssueLinks.get(3));
    }

    public void testUpgradeTaskDoesNotMigrateTrackbacksToIssueLinksIfTrackbacksWereDisabledInTheDatabase()
    {
        restoreDataWithVersionCheck(Backups.TrackbacksDisabledBy.DATABASE_VALUE);

        IssueClient issueClient = new IssueClient(getEnvironmentData());

        List<RemoteIssueLink> remoteIssueLinks = issueClient.getRemoteIssueLinks("BULK-3");

        assertTrue("Trackbacks should not have been converted, yet some remote links exist", remoteIssueLinks.isEmpty());
    }

    private void restoreDataWithVersionCheck(final String xmlFile)
    {
        administration.restoreDataWithBuildNumber(xmlFile, 660);
    }

    private void compareLinks(RemoteIssueLink l1, RemoteIssueLink l2)
    {
        assertEquals("Links relationship is different for link: " + l2.id, l1.relationship, l2.relationship);
        assertEquals("Links globalid is different for link: " + l2.id, l1.globalId, l2.globalId);
        assertEquals("Links applicationType is different for link: " + l2.id, l1.application.type, l2.application.type);
        assertEquals("Links applicationName is different for link: " + l2.id, l1.application.name, l2.application.name);
        assertEquals("Links summary is different for link: " + l2.id, l1.object.summary, l2.object.summary);
        assertEquals("Links title is different for link: " + l2.id, l1.object.title, l2.object.title);
        assertEquals("Links url is different for link: " + l2.id, l1.object.url, l2.object.url);
    }

    private RemoteIssueLink createLink(int id, String summary, String title, String url, String blogName)
    {
        RemoteIssueLink link = new RemoteIssueLink();
        link.globalId = "com.atlassian.jira:legacy-trackbacks-" + id;
        link.application = new RemoteIssueLink.Application();
        link.application.type = "legacy-trackbacks";
        link.application.name = blogName;
        link.relationship = "Trackbacks";
        link.object = new RemoteIssueLink.RemoteObject();
        link.object.summary = summary;
        link.object.title = title;
        link.object.url = url;
        return link;
    }
}
