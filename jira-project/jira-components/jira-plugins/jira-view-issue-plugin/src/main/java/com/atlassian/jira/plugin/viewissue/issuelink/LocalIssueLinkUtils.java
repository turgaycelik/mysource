package com.atlassian.jira.plugin.viewissue.issuelink;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Utility class for local issue links.
 *
 * @since v5.0
 */
public class LocalIssueLinkUtils
{
    private enum InternalIssueDirection { OUTWARD, INWARD }

    private LocalIssueLinkUtils() {}

    /**
     * Converts a local issue link collection to a mapping of relationship to issue link contexts.
     *
     * @param linkCollection local issue link collection to convert
     * @param issueId ID of the issue
     * @param baseUrl base URL of the server
     * @param fieldVisibilityManager fieldVisibilityManager
     * @return mapping of relationship to issue link contexts
     */
    public static Map<String, List<IssueLinkContext>> convertToIssueLinkContexts(LinkCollection linkCollection, Long issueId, String baseUrl, FieldVisibilityManager fieldVisibilityManager)
    {
        Map<String, List<IssueLinkContext>> relationshipLinkMap = Maps.newHashMap();

        for (com.atlassian.jira.issue.link.IssueLinkType issueLinkType : linkCollection.getLinkTypes())
        {
            final List<Issue> outwardIssues = linkCollection.getOutwardIssues(issueLinkType.getName());
            final List<Issue> inwardIssues = linkCollection.getInwardIssues(issueLinkType.getName());
            final String linkTypeId = issueLinkType.getId().toString();
            if (CollectionUtils.isNotEmpty(outwardIssues))
            {
                String relationship = issueLinkType.getOutward();
                getOrCreateList(relationshipLinkMap, relationship).addAll(convertLocalIssuesToContexts(issueId, outwardIssues, InternalIssueDirection.OUTWARD, linkTypeId, baseUrl, fieldVisibilityManager));
            }

            if (CollectionUtils.isNotEmpty(inwardIssues))
            {
                String relationship = issueLinkType.getInward();
                getOrCreateList(relationshipLinkMap, relationship).addAll(convertLocalIssuesToContexts(issueId, inwardIssues, InternalIssueDirection.INWARD, linkTypeId, baseUrl, fieldVisibilityManager));
            }
        }

        return relationshipLinkMap;
    }

    private static List<IssueLinkContext> convertLocalIssuesToContexts(Long issueId, List<Issue> linkedIssues, InternalIssueDirection direction, String linkTypeId, String baseUrl, FieldVisibilityManager fieldVisibilityManager)
    {
        List<IssueLinkContext> issueLinkContexts = Lists.newArrayList();
        for (Issue linkedIssue : linkedIssues)
        {
            issueLinkContexts.add(convertLocalIssueToContext(issueId, linkedIssue, direction, linkTypeId, baseUrl, fieldVisibilityManager));
        }
        return issueLinkContexts;
    }

    private static IssueLinkContext convertLocalIssueToContext(Long issueId, Issue linkedIssue, InternalIssueDirection direction, String linkTypeId, String baseUrl, FieldVisibilityManager fieldVisibilityManager)
    {
        final String deleteTypeName = (direction == InternalIssueDirection.OUTWARD ? "destId" : "sourceId");

        final String deleteUrl = String.format(baseUrl + "/secure/DeleteLink.jspa?id=%d&%s=%s&linkType=%s", issueId, deleteTypeName, linkedIssue.getId(), linkTypeId);
        final String htmlElementId = "internal-" + linkedIssue.getId() + "_" + linkTypeId;

        Map<String, Object> localIssueLinkContext = getLocalIssueLinkVelocityContext(linkedIssue, baseUrl, fieldVisibilityManager);

        return IssueLinkContext.newLocalIssueLinkContext(htmlElementId, deleteUrl, false, localIssueLinkContext);
    }

    private static Map<String, Object> getLocalIssueLinkVelocityContext(Issue linkedIssue, String baseUrl, FieldVisibilityManager fieldVisibilityManager)
    {
        String priorityIconUrl = null;
        String priorityIconTooltip = null;

        Priority priority = linkedIssue.getPriorityObject();
        if (priority != null && !fieldVisibilityManager.isFieldHidden("priority", linkedIssue))
        {
            priorityIconUrl = asAbsoluteUrl(baseUrl, priority.getIconUrl());
            priorityIconTooltip = priority.getName() + " - " + priority.getDescription();
        }

        ImmutableMap.Builder<String, Object> contextBuilder = ImmutableMap.builder();
        putMap(contextBuilder, "url", baseUrl + "/browse/" + linkedIssue.getKey());
        putMap(contextBuilder, "title", linkedIssue.getKey());
        putMap(contextBuilder, "issueKey", linkedIssue.getKey());
        putMap(contextBuilder, "iconUrl", asAbsoluteUrl(baseUrl, linkedIssue.getIssueTypeObject().getIconUrl()));
        putMap(contextBuilder, "iconTooltip", linkedIssue.getIssueTypeObject().getName() + " - " + linkedIssue.getIssueTypeObject().getDescription());
        putMap(contextBuilder, "summary", linkedIssue.getSummary());
        putMap(contextBuilder, "tooltip", linkedIssue.getKey() + ": " + linkedIssue.getSummary());
        putMap(contextBuilder, "priorityIconUrl", priorityIconUrl);
        putMap(contextBuilder, "priorityIconTooltip", priorityIconTooltip);
        final Status status = linkedIssue.getStatusObject();
        if (status != null)
        {
            putMap(contextBuilder, "statusIconUrl", asAbsoluteUrl(baseUrl, status.getIconUrl()));
            putMap(contextBuilder, "statusIconTooltip", status.getName() + " - " + status.getDescription());
            putMap(contextBuilder, "status", status.getSimpleStatus());
        }
        putMap(contextBuilder, "resolved", linkedIssue.getResolutionObject() != null);

        return contextBuilder.build();
    }

    private static void putMap(ImmutableMap.Builder<String, Object> mapBuilder, String key, Object value)
    {
        if (value != null)
        {
            mapBuilder.put(key, value);
        }
    }

    /**
     * Returns a list for the specified key. If the key does not exist, a new list will be automatically created.
     *
     * @param map Map to action on
     * @param key key to retrieve
     * @param <K> Key type
     * @param <V> Type of the List value
     * @return a list for the specified key
     */
    private static <K, V> List<V> getOrCreateList(Map<K, List<V>> map, K key)
    {
        if (map.containsKey(key))
        {
            return map.get(key);
        }
        else
        {
            List<V> list = Lists.newArrayList();
            map.put(key, list);
            return list;
        }
    }

    private static String asAbsoluteUrl(String baseUrl, String url)
    {
        try
        {
            return new URL(url).toString();
        }
        catch (MalformedURLException e)
        {
            return  baseUrl + url;
        }
    }
}
