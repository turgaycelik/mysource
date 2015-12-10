package com.atlassian.jira.issue.comparator;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.MockUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestUserNameComparator
{
    User adminUser;
    User lowerCaseAdminUser;
    User accentedAdminUser;
    User alfonzUser;
    User accentedAlfonzUser;
    User cecilUser;
    User accentedCecilUser;
    User umAlfonzUser;
    User dudeUser;
    User nooneUser;
    User accentedNooneUser;
    User zooUser;
    User sUser;
    User ssUser;

    @Before
    public void setUp() throws Exception
    {
        // Spanish testing names
        adminUser = new MockUser("Admin", "OAdministrator", "admin@example.com");
        lowerCaseAdminUser = new MockUser("admin", "Padministrator", "admin@example.com");
        accentedAdminUser = new MockUser("\u00C1dmin", "Zdministrator", "admin@example.com");
        dudeUser = new MockUser("dude", "Dude", "admin@example.com");
        nooneUser = new MockUser("noone", "Noone", "admin@example.com");
        accentedNooneUser = new MockUser("\u00D1oone", "WNoone", "admin@example.com");
        zooUser = new MockUser("zoo", "zoo", "admin@example.com");

        // Solovak testing names
        alfonzUser = new MockUser("alfonz", "zalfonz", "admin@example.com");
        accentedAlfonzUser = new MockUser("\u00e1lfonz", "alfonz", "admin@example.com");
        umAlfonzUser = new MockUser("\u00e4lfonz", "alfonz", "admin@example.com");
        cecilUser = new MockUser("cecil", "cecil", "admin@example.com");
        accentedCecilUser = new MockUser("\u010D\u00E9cil", "cecil", "admin@example.com");

        // German testing names
        sUser = new MockUser("aas", "aas", "admin@example.com");
        ssUser = new MockUser("aa\u00DF", "aas", "admin@example.com");
    }

    @After
    public void tearDown() throws Exception
    {
        adminUser = null;
        lowerCaseAdminUser = null;
        accentedAdminUser = null;
        dudeUser = null;
        nooneUser = null;
        accentedNooneUser = null;
        zooUser = null;
        alfonzUser = null;
        accentedAlfonzUser = null;
        umAlfonzUser = null;
        sUser = null;
        ssUser = null;
    }

    @Test
    public void testSortingInSpanish()
    {
        final UserNameComparator nameComparator = new UserNameComparator(new Locale("es", "ES"));

        final List users = EasyList.build(zooUser, accentedNooneUser, nooneUser, accentedAdminUser, dudeUser, adminUser, lowerCaseAdminUser);
        Collections.sort(users, nameComparator);
        assertEquals(adminUser, users.get(0));
        // This comes second because the username comes into play
        assertEquals(lowerCaseAdminUser, users.get(1));
        assertEquals(accentedAdminUser, users.get(2));
        assertEquals(dudeUser, users.get(3));
        assertEquals(nooneUser, users.get(4));
        assertEquals(accentedNooneUser, users.get(5));
        assertEquals(zooUser, users.get(6));
    }

    @Test
    public void testSortingInEnglishWithSpanishList()
    {
        final UserNameComparator nameComparator = new UserNameComparator(Locale.ENGLISH);

        final List users = EasyList.build(zooUser, accentedNooneUser, nooneUser, accentedAdminUser, dudeUser, adminUser, lowerCaseAdminUser);
        Collections.sort(users, nameComparator);
        assertEquals(adminUser, users.get(0));
        // This comes second because the username comes into play
        assertEquals(lowerCaseAdminUser, users.get(1));
        assertEquals(accentedAdminUser, users.get(2));
        assertEquals(dudeUser, users.get(3));
        assertEquals(nooneUser, users.get(4));
        assertEquals(accentedNooneUser, users.get(5));
        assertEquals(zooUser, users.get(6));
    }

    @Test
    public void testSortingInSolvak()
    {
        final UserNameComparator nameComparator = new UserNameComparator(new Locale("sk"));

        final List users = EasyList.build(accentedCecilUser, cecilUser, accentedAlfonzUser, alfonzUser, umAlfonzUser, adminUser);
        Collections.sort(users, nameComparator);
        assertEquals(adminUser, users.get(0));
        assertEquals(alfonzUser, users.get(1));
        assertEquals(accentedAlfonzUser, users.get(2));
        assertEquals(umAlfonzUser, users.get(3));
        assertEquals(cecilUser, users.get(4));
        assertEquals(accentedCecilUser, users.get(5));
    }

    @Test
    public void testSortingInEnglishWithSolvakList()
    {
        final UserNameComparator nameComparator = new UserNameComparator(Locale.ENGLISH);

        final List users = EasyList.build(accentedCecilUser, cecilUser, accentedAlfonzUser, alfonzUser, umAlfonzUser, adminUser);
        Collections.sort(users, nameComparator);
        assertEquals(adminUser, users.get(0));
        assertEquals(alfonzUser, users.get(1));
        assertEquals(accentedAlfonzUser, users.get(2));
        assertEquals(umAlfonzUser, users.get(3));
        assertEquals(cecilUser, users.get(4));
        assertEquals(accentedCecilUser, users.get(5));
    }

    @Test
    public void testSortingInGerman()
    {
        final UserNameComparator nameComparator = new UserNameComparator(new Locale("de"));

        final List users = EasyList.build(adminUser, ssUser, sUser);
        Collections.sort(users, nameComparator);
        assertEquals(sUser, users.get(0));
        assertEquals(ssUser, users.get(1));
        assertEquals(adminUser, users.get(2));
    }

    @Test
    public void testSortingInEnglishWithGermanList()
    {
        final UserNameComparator nameComparator = new UserNameComparator(Locale.ENGLISH);

        final List users = EasyList.build(adminUser, ssUser, sUser);
        Collections.sort(users, nameComparator);
        assertEquals(sUser, users.get(0));
        assertEquals(ssUser, users.get(1));
        assertEquals(adminUser, users.get(2));
    }

}
