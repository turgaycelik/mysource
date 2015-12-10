package com.atlassian.jira.issue.link;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple helper class to help out in how we display issues links
 *
 * @since v4.4
 */
public class IssueLinkDisplayHelper
{
    private final UserHistoryManager userHistoryManager;
    private final User user;

    public IssueLinkDisplayHelper(final UserHistoryManager userHistoryManager, final User user)
    {
        this.userHistoryManager = userHistoryManager;
        this.user = user;
    }

    /**
     * Sorts the link types into a list of strings (for a combo box) sorted in previous usage order
     *
     * @param linkTypes the collection of link types to sort
     * @return the list of link types sorted by previous preference
     */
    public Collection<String> getSortedIssueLinkTypes(final Collection<IssueLinkType> linkTypes)
    {
        List<UserHistoryItem> userHistoryItems = userHistoryManager.getHistory(UserHistoryItem.ISSUELINKTYPE, user);
        final List<String> history = new ArrayList<String>();
        for (UserHistoryItem userHistoryItem : userHistoryItems)
        {

            history.add(StringUtils.defaultString(userHistoryItem.getData()));
            }

        final List<String> linkTypeDesc = new ArrayList<String>();
        for (IssueLinkType linkType : linkTypes)
        {
            linkTypeDesc.add(linkType.getOutward());
            linkTypeDesc.add(linkType.getInward());
        }
        Collections.sort(linkTypeDesc, new Comparator<String>()
        {
            @Override
            public int compare(String s1, String s2)
            {
                if (s1.equals(s2))
                {
                    return 0;
                }
                if (!history.contains(s1) && !history.contains(s2))
                {
                    return 0;
                }
                if (history.contains(s1) && !history.contains(s2))
                {
                    return -1;
                }
                if (!history.contains(s1) && history.contains(s2))
                {
                    return 1;
                }
                return history.indexOf(s1) - history.indexOf(s2);
            }
        });
        // if the user has provided duplicate outward and inwards link desciptions then lets remove as options.  It will be outward only in that case
        // because it insensible to infer direction when A 'relates to' B and B 'relates to' A in the words 'relates to'
        return Sets.newLinkedHashSet(linkTypeDesc);
    }

    /**
     * @return the issue link type that was last used by this user
     */
    public String getLastUsedLinkType()
    {
        List<UserHistoryItem> userHistoryItems = userHistoryManager.getHistory(UserHistoryItem.ISSUELINKTYPE, user);
        String selectedLinkType = "";
        if (userHistoryItems.size() > 0)
        {
            // there can be ten by default  but I am NOT optimising it to be only via jira-applications.properties.  Ship!
            selectedLinkType = userHistoryItems.get(0).getData();
        }
        return selectedLinkType;
    }

}
