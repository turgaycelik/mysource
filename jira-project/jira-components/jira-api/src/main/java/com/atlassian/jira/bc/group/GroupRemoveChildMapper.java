package com.atlassian.jira.bc.group;

import com.atlassian.core.util.collection.EasyList;

import java.util.*;

/**
 * Holds the parameters that specify which children should be removed from which groups. Also holds a list of
 * default groups which children can be registered to be removed from.
 * This class is not threadsafe.
 *
 * The only time you want to use the register methods and not the {@link #register(String)} method is
 * when you are updating multiple groups and multiple children and not all children are being removed from all groups.
 * In all other cases the usage pattern for this class should be:
 *
 * <code>
 * GroupRemoveChildMapper mapper = new GroupRemoveChildMapper(listOfGroupNamesToRemoveFrom);
 * mapper.registerForSelected(childNameWhoShouldBeRemovedFromGroups);
 * </code>
 *
 * @since v3.12.  In v4.3  - Renamed from GroupRemoveUserMapper.
 */
public class GroupRemoveChildMapper
{
    private final List defaultGroupNames;
    private final Map groupsToRemoveByChildName;

    /**
     * Creates the mapper with no default groups.
     */
    public GroupRemoveChildMapper()
    {
        this(Collections.EMPTY_LIST);
    }

    /**
     * Creates the mapper with the given groups marked as default groups. If there are any
     * children registered to be removed from all default groups, these will be the groups that refers to.
     *
     * @param defaultGroupNames the defaultGroupNames.
     * @since v3.12
     */
    public GroupRemoveChildMapper(List defaultGroupNames)
    {
        if (defaultGroupNames == null)
        {
            throw new IllegalArgumentException("defaultGroupNames cannot be null");
        }
        this.defaultGroupNames = defaultGroupNames;
        this.groupsToRemoveByChildName = new HashMap();
    }

    /**
     * Registers the given childName to be removed from the configured list of default groups.
     *
     * @param childName identifies the child to be removed from the default groups.
     * @return this.
     * @since v3.12
     */
    public GroupRemoveChildMapper register(String childName)
    {
        if (groupsToRemoveByChildName.get(childName) == null)
        {
            groupsToRemoveByChildName.put(childName, new HashSet());
        }
        return this;
    }

    /**
     * Registers the given childName to be removed from the specified groupName. This can be called many times
     * and the groups will be aggregated against the childName. Calling this method implies that the
     * child should be removed from the specified group instead of the default groups.
     *
     * @param childName  identifies the child to be removed from the default groups.
     * @param groupName identifies the group to remove the child from.
     * @return this.
     * @since v3.12
     */
    public GroupRemoveChildMapper register(String childName, String groupName)
    {
        return register(childName, EasyList.build(groupName));
    }

    /**
     * Registers the given childName to be removed from the specified group names. This can be called many times
     * and the groups will be aggregated against the childName. Calling this method implies that the
     * child should be removed from the specified groups instead of the default groups.
     *
     * @param childName  identifies the child to be removed from the default groups.
     * @param groupNames a collection of groupNames that identify the groups to remove the child from.
     * @return this.
     * @since v3.12
     */
    public GroupRemoveChildMapper register(String childName, Collection /*<String>*/ groupNames)
    {
        if (groupNames != null && !groupNames.isEmpty())
        {
            if (groupsToRemoveByChildName.get(childName) == null)
            {
                groupsToRemoveByChildName.put(childName, new HashSet());
            }
            HashSet groups = (HashSet) groupsToRemoveByChildName.get(childName);
            groups.addAll(groupNames);
        }
        return this;
    }

    /**
     * Indicates that the given child is to be removed from all default groups.
     *
     * @param childName identifies the child to be removed from the default groups.
     * @return true if the child is to be removed from all default groups, false otherwise.
     * @since v3.12
     */
    public boolean isRemoveFromAllSelected(String childName)
    {
        // we indicate a removal from all default groups by an empty list
        Set groups = (Set) groupsToRemoveByChildName.get(childName);
        return groups != null && groups.isEmpty();
    }

    /**
     * Provides an Iterator to loop over all registered groups for a child.
     *
     * @param childName identifies the child whose groups we want to iterate over.
     * @return Iterator to loop over all registered groups for a child.
     * @since v3.12
     */
    public Iterator /*<String>*/ getGroupsIterator(String childName)
    {
        return getGroups(childName).iterator();
    }

    /**
     * Provides an unmodifiable collection of all registered groups for a child.
     *
     * @param childName identifies the child to be removed from the default groups.
     * @return unmodifiable collection of all registered groups for a child.
     *
     * @since v3.12
     */
    public Collection getGroups(String childName)
    {
        Collection groups;
        if (isRemoveFromAllSelected(childName))
        {
            groups = Collections.unmodifiableList(defaultGroupNames);
        }
        else
        {
            groups = Collections.unmodifiableSet((Set) groupsToRemoveByChildName.get(childName));
        }
        if (groups == null)
        {
            return Collections.EMPTY_LIST;
        }
        else
        {
            return groups;
        }
    }

    /**
     * Provides an iterator over the childNames.
     *
     * @return iterator for registered childNames.
     * @since v3.12
     */
    public Iterator /*<String>*/ childIterator()
    {
        return groupsToRemoveByChildName.keySet().iterator();
    }

    /**
     * Returns the default groups.
     *
     * @return the list of groupNames that are default.
     * @since v3.12
     */
    public List /*<String>*/ getDefaultGroupNames()
    {
        return Collections.unmodifiableList(defaultGroupNames);
    }
}
