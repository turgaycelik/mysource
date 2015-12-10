package com.atlassian.jira.issue.comparator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.user.MockUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * @since v3.13.3
 */
@RunWith (MockitoJUnitRunner.class)
public class TestUserBestNameComparator
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

    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        // Spanish testing names
        adminUser = new MockUser("admin", "Administrator", "admin@example.com");
        lowerCaseAdminUser = new MockUser("lowercaseadmin", "administrator", "admin@example.com");
        accentedAdminUser = new MockUser("accentedadmin", "\u00C1dministrator", "admin@example.com");
        dudeUser = new MockUser("dude", "Dude", "admin@example.com");
        nooneUser = new MockUser("noone", "Noone", "admin@example.com");
        accentedNooneUser = new MockUser("accentednoone", "\u00D1oone", "admin@example.com");
        zooUser = new MockUser("zoo", "zoo", "admin@example.com");

        // Solovak testing names
        alfonzUser = new MockUser("alf", "alfonz", "admin@example.com");
        accentedAlfonzUser = new MockUser("alf", "\u00e1lfonz", "admin@example.com");
        umAlfonzUser = new MockUser("alf", "\u00e4lfonz", "admin@example.com");
        cecilUser = new MockUser("cecil", "cecil", "admin@example.com");
        accentedCecilUser = new MockUser("cecil", "\u010D\u00E9cil", "admin@example.com");

        // German testing names
        sUser = new MockUser("aas", "aas", "admin@example.com");
        ssUser = new MockUser("aas", "aa\u00DF", "admin@example.com");
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
        final UserCachingComparator nameComparator = new UserCachingComparator(new Locale("es", "ES"));

        final List<User> users = Arrays.asList(zooUser, accentedNooneUser, nooneUser, accentedAdminUser, dudeUser, adminUser, lowerCaseAdminUser);
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
        final UserCachingComparator nameComparator = new UserCachingComparator(Locale.ENGLISH);

        final List<User> users = Arrays.asList(zooUser, accentedNooneUser, nooneUser, accentedAdminUser, dudeUser, adminUser, lowerCaseAdminUser);
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
        final UserCachingComparator nameComparator = new UserCachingComparator(new Locale("sk"));

        final List<User> users = Arrays.asList(accentedCecilUser, cecilUser, accentedAlfonzUser, alfonzUser, umAlfonzUser, adminUser);
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
        final UserCachingComparator nameComparator = new UserCachingComparator(Locale.ENGLISH);

        final List<User> users = Arrays.asList(accentedCecilUser, cecilUser, accentedAlfonzUser, alfonzUser, umAlfonzUser, adminUser);
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
        final UserCachingComparator nameComparator = new UserCachingComparator(new Locale("de"));

        final List<User> users = Arrays.asList(adminUser, ssUser, sUser);
        Collections.sort(users, nameComparator);
        assertEquals(sUser, users.get(0));
        assertEquals(ssUser, users.get(1));
        assertEquals(adminUser, users.get(2));
    }

    @Test
    public void testSortingInEnglishWithGermanList()
    {
        final UserCachingComparator nameComparator = new UserCachingComparator(Locale.ENGLISH);

        final List<User> users = Arrays.asList(adminUser, ssUser, sUser);
        Collections.sort(users, nameComparator);
        assertEquals(sUser, users.get(0));
        assertEquals(ssUser, users.get(1));
        assertEquals(adminUser, users.get(2));
    }
}
