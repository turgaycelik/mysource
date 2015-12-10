package com.atlassian.jira.sharing.type;

import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.type.ShareType.Name;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation that simply builds a list based on the constructor.
 *
 * @since v3.13
 */
public class DefaultShareTypeFactory implements ShareTypeFactory
{
    private final Map<Name, ShareType> shareMap;
    private final List<ShareType> shareList;

    public DefaultShareTypeFactory(final GlobalShareType globalShareType, final GroupShareType groupShareType, final ProjectShareType projectShareType)
    {
        this(new HashSet<ShareType>(Lists.newArrayList(globalShareType, groupShareType, projectShareType)));
    }

    /**
     * Create the factory with the passed ShareTypes.
     *
     * @param types the ShareTypes to register with the factory. This is a set to ensure that Pico does not call this constructor as there are no sets
     *        registered within (but there are some Collections which causes lots of issues.)
     */

    DefaultShareTypeFactory(final Set<ShareType> types)
    {
        Assertions.notNull("types", types);

        final List<ShareType> typesList = new ArrayList<ShareType>(types);
        Collections.sort(typesList, new ShareTypeComparator());
        final Map<Name, ShareType> typesMap = new HashMap<Name, ShareType>();

        for (final Object element : typesList)
        {
            final ShareType shareType = (ShareType) element;
            typesMap.put(shareType.getType(), shareType);
        }
        shareMap = Collections.unmodifiableMap(typesMap);
        shareList = Collections.unmodifiableList(typesList);
    }

    public Collection<ShareType> getAllShareTypes()
    {
        return shareList;
    }

    public ShareType getShareType(final Name type)
    {
        Assertions.notNull("type", type);
        return shareMap.get(type);
    }

    public Comparator<SharePermission> getPermissionComparator()
    {
        return new SharePermissionsComparator();
    }

    /**
     * Comparator that will sort different permissions with the help of the ShareType objects.
     */
    private class SharePermissionsComparator implements Comparator<SharePermission>
    {
        public int compare(final SharePermission o1, final SharePermission o2)
        {
            if (o1 == o2)
            {
                return 0;
            }
            else if (o1 == null)
            {
                return -1;
            }
            else if (o2 == null)
            {
                return 1;
            }

            final ShareType type1 = getShareType(o1.getType());

            // If the permissions have the same type, then order by asking the ShareType.
            if (o1.getType().equals(o2.getType()))
            {
                return type1.getComparator().compare(o1, o2);
            }
            else
            {
                // permissions of different types should be ordered by their registration order.
                final ShareType type2 = getShareType(o2.getType());

                if (type1 == null)
                {
                    if (type2 == null)
                    {
                        return 0;
                    }
                    else
                    {
                        return -1;
                    }
                }
                else if (type2 == null)
                {
                    return 1;
                }

                return type1.getPriority() - type2.getPriority();
            }
        }
    }

    private static class ShareTypeComparator implements Comparator<ShareType>
    {
        private ShareTypeComparator()
        {}

        public int compare(final ShareType type1, final ShareType type2)
        {
            return type1.getPriority() - type2.getPriority();
        }
    }
}
