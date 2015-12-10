package com.atlassian.jira.functest.config;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Node;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that can be used to find system admins.
 *
 * @since v4.2
 */
public class ConfigAdminLocator
{
    private static final int PERMISSION_SYSTEM_ADMIN = 44;
    private static final int PERMISSION_ADMIN = 0;

    private final Document document;

    public ConfigAdminLocator(final Document document)
    {
        this.document = document;
    }

    public Set<String> locateSystemAdmins()
    {
        final Set<String> users = new HashSet<String>();
        final Set<String> systemAdminGroups = getPermissionGroups(PERMISSION_SYSTEM_ADMIN);
        for (final String group : systemAdminGroups)
        {
            users.addAll(getUserForGroupCrowd(group));
        }

        if (users.isEmpty())
        {
            for (final String group : systemAdminGroups)
            {
                users.addAll(getUsersForGroup(group));
            }

            if (users.isEmpty())
            {
                for (final String group: getPermissionGroups(PERMISSION_ADMIN))
                {
                    users.addAll(getUsersForGroup(group));
                }
            }
        }

        return users;
    }

    private Set<String> getUsersForGroup(final String groupName)
    {
        return getAttributes(String.format("/*/OSMembership[@groupName='%s']/@userName", groupName));
    }

    private Set<String> getUserForGroupCrowd(final String groupName)
    {
        return getAttributes(String.format("/*/Membership[@membershipType='GROUP_USER' and @parentName='%s']/@childName", groupName));
    }

    private Set<String> getPermissionGroups(final int permission)
    {
        return getAttributes(String.format("/*/SchemePermissions[@type = 'group' and not(@scheme) and @permission='%d']/@parameter", permission));
    }

    private Set<String> getAttributes(final String xpath)
    {
        @SuppressWarnings ({ "unchecked" })
        final List<Attribute> list = document.selectNodes(xpath);
        return toSet(list);
    }

    private static Set<String> toSet(Collection<? extends Node> nodes)
    {
        Set<String> values = new HashSet<String>();
        for (Node node : nodes)
        {
            final String parameter = StringUtils.stripToNull(node.getText());
            if (parameter != null)
            {
                values.add(parameter);
            }
        }
        return values;
    }
}
