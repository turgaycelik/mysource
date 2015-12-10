package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.ConfigAdminLocator;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import junit.framework.TestCase;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Test {@link ConfigAdminLocator}.
 *
 * @since v4.2
 */
public class TestConfigAdminLocator extends TestCase
{
    private static final int PERMISSION_SYSTEM_ADMIN = 44;
    private static final int PERMISSION_ADMIN = 0;
    private static final String ADMIN = "admin";

    public void testOSUsersSystemAdmins() throws Exception
    {
        final Element root = createDocument();
        createPermission(root, PERMISSION_SYSTEM_ADMIN, "none", "jira-users");
        createPermission(root, PERMISSION_SYSTEM_ADMIN, "group", "jira-admins");
        createPermission(root, PERMISSION_SYSTEM_ADMIN, "group", "jira-sysadmins");
        createPermission(root, PERMISSION_ADMIN, "group", "jira-users");
        createPermission(root, PERMISSION_SYSTEM_ADMIN, "group", "jira-users", 5L);
        createPermission(root, PERMISSION_ADMIN, "group", "jira-admins");
        createPermission(root, PERMISSION_SYSTEM_ADMIN, "group", "");

        createMembership(root, ADMIN, "jira-admins");
        createMembership(root, "user", "jira-users");
        createMembership(root, "sysadmin", "jira-sysadmins");
        createMembership(root, "sysadmin2", "jira-sysadmins");

        final ConfigAdminLocator locator = new ConfigAdminLocator(root.getDocument());
        assertEquals(asSet(ADMIN, "sysadmin", "sysadmin2"), locator.locateSystemAdmins());
    }

    public void testCrowdUsersSystemAdmins() throws Exception
    {
        final Element root = createDocument();
        createPermission(root, PERMISSION_SYSTEM_ADMIN, "none", "jira-users");
        createPermission(root, PERMISSION_SYSTEM_ADMIN, "group", "jira-admins");
        createPermission(root, PERMISSION_SYSTEM_ADMIN, "group", "jira-sysadmins");
        createPermission(root, PERMISSION_ADMIN, "group", "jira-users", 5L);
        createPermission(root, PERMISSION_ADMIN, "group", "jira-users");

        createMembership(root, "don't Find Me Either", "jira-admins");
        createGoodCrowdMembership(root, ADMIN, "jira-admins");
        createGoodCrowdMembership(root, "user", "jira-users");
        createGoodCrowdMembership(root, "sysadmin", "jira-sysadmins");
        createGoodCrowdMembership(root, "sysadmin2", "jira-sysadmins");
        createBadCrowdMembership(root, "don'tFindMe", "jira-sysadmins");
        createGoodCrowdMembership(root, null, "jira-sysadmins");

        final ConfigAdminLocator locator = new ConfigAdminLocator(root.getDocument());
        assertEquals(asSet(ADMIN, "sysadmin", "sysadmin2"), locator.locateSystemAdmins());
    }

    public void testOldUsersAdmins() throws Exception
    {
        final Element root = createDocument();
        createPermission(root, PERMISSION_SYSTEM_ADMIN, "group", "jira-sysadmins");
        createPermission(root, PERMISSION_ADMIN, "group", "jira-users", 5L);
        createPermission(root, PERMISSION_ADMIN, "group", "jira-admins");

        createMembership(root, ADMIN, "jira-admins");
        createMembership(root, "user", "jira-users");

        final ConfigAdminLocator locator = new ConfigAdminLocator(root.getDocument());
        assertEquals(asSet(ADMIN), locator.locateSystemAdmins());
    }

    private void createMembership(Element root, String userName, String groupName)
    {
        //<OSMembership id="10000" userName="admin" groupName="jira-administrators"/>
        final Element element = ConfigXmlUtils.createNewElement(root, "OSMembership");
        ConfigXmlUtils.setAttribute(element, "userName", userName);
        ConfigXmlUtils.setAttribute(element, "groupName", groupName);
    }

    private void createGoodCrowdMembership(Element root, String usernName, String groupName)
    {
        createCrowdMembership(root, usernName, groupName, "GROUP_USER");
    }

    private void createBadCrowdMembership(Element root, String usernName, String groupName)
    {
        createCrowdMembership(root, usernName, groupName, "BAD_TYPE");
    }

    private void createCrowdMembership(Element root, String usernName, String groupName, String type)
    {
        //<Membership id="10000" parentId="10000" childId="10000" membershipType="GROUP_USER" parentName="jira-administrators" lowerParentName="jira-administrators" childName="admin" lowerChildName="admin" directoryId="1"/>
        final Element crowdMembership = ConfigXmlUtils.createNewElement(root, "Membership");
        ConfigXmlUtils.setAttribute(crowdMembership, "parentName", groupName);
        ConfigXmlUtils.setAttribute(crowdMembership, "childName", usernName);
        ConfigXmlUtils.setAttribute(crowdMembership, "membershipType", type);
    }

    private void createPermission(Element root, int permission, String type, String parameter)
    {
        createPermission(root, permission, type, parameter, null);
    }

    private void createPermission(Element root, int permission, String type, String parameter, Long scheme)
    {
        //<SchemePermissions id="10000" permission="0" type="group" parameter="jira-administrators"/>
        final Element permissionEl = ConfigXmlUtils.createNewElement(root, "SchemePermissions");
        ConfigXmlUtils.setAttribute(permissionEl, "permission", permission);
        ConfigXmlUtils.setAttribute(permissionEl, "type", type);
        ConfigXmlUtils.setAttribute(permissionEl, "parameter", parameter);
        ConfigXmlUtils.setAttribute(permissionEl, "scheme", scheme);
    }

    private Element createDocument()
    {
        final DocumentFactory factory = DocumentFactory.getInstance();
        return factory.createDocument().addElement("entity-engine-xml");
    }

    private<T> Set<T> asSet(T...elements)
    {
        return new HashSet<T>(Arrays.asList(elements));
    }
}
