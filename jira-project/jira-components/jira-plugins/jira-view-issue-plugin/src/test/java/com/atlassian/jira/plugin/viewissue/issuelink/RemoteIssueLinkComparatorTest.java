package com.atlassian.jira.plugin.viewissue.issuelink;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.plugin.viewissue.issuelink.RemoteIssueLinkComparator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link RemoteIssueLinkComparator}.
 *
 * @since v5.0
 */
public class RemoteIssueLinkComparatorTest
{
    /**
     * <pre>
     * blocker (mixed)
     *     - HSP-2 Critical improvement on project HSP (com.atlassian.jira)
     *     - Blog Title Confluence Page (com.atlassian.confluence)
     *     - Alphabet (remote) (com.aardvark)
     * Blog Links (remote)
     *     - BLOG 3 (com.companytype1) (Instance 1)
     *     - ALL (com.companytype1) (Instance 2)
     *     - BLOG 6 (com.companytype1) (Instance 2)
     *     - HELLO 3 (com.companytype1)
     *     - CAT 4  (com.companytype2)
     * Tickets (remote)
     *     - HELP-123 Need help
     *     - help-124 Need help
     *     - HELP-125 Need help
     * </pre>
     */
    @Test
    public void testComparator() throws Exception
    {
        List<RemoteIssueLink> expected = Arrays.asList(
            createRemoteIssueLink("HSP-2", "Critical improvement on project HSP", "blocker (mixed)", "com.atlassian.jira", "Remote JIRA"),
            createRemoteIssueLink("Blog Title", "Confluence Page", "blocker (mixed)", "com.atlassian.confluence", "Confluence"),
            createRemoteIssueLink("Alphabet (remote)", null, "blocker (mixed)", "com.aardvark", "Aardvark"),

            createRemoteIssueLink("BLOG 3", "(com.companytype1) (Instance 1)", "Blog Links (remote)", "com.companytype1", "Instance 1"),
            createRemoteIssueLink("ALL", "(com.companytype1) (Instance 2)", "Blog Links (remote)", "com.companytype1", "Instance 2"),
            createRemoteIssueLink("BLOG 6", "(com.companytype1) (Instance 2)", "Blog Links (remote)", "com.companytype1", "Instance 2"),
            createRemoteIssueLink("HELLO 3", null, "Blog Links (remote)", "com.companytype1", null),
            createRemoteIssueLink("CAT 4", "(com.companytype2)", "Blog Links (remote)", "com.companytype2", null),

            createRemoteIssueLink("HELP-123", "Need help", "Tickets (remote)", null, null),
            createRemoteIssueLink("help-124", "Need help", "Tickets (remote)", null, null),
            createRemoteIssueLink("HELP-125", "Need help", "Tickets (remote)", null, null)
        );

        List<RemoteIssueLink> actual = new ArrayList<RemoteIssueLink>(expected);
        Collections.shuffle(actual, new Random(123L)); // supplying a seed because we want the same test example every time
        Comparator<RemoteIssueLink> remoteIssueLinkComparator = new RemoteIssueLinkComparator("Web Link");
        Collections.sort(actual, remoteIssueLinkComparator);

        assertEquals(expected, actual);
    }

    private static RemoteIssueLink createRemoteIssueLink(String title, String summary, String relationship, String applicationType, String applicationName)
    {
        return new RemoteIssueLink(null, null, null, title, summary, null, null, null, relationship, null, null, null, null, applicationType, applicationName);
    }
}
