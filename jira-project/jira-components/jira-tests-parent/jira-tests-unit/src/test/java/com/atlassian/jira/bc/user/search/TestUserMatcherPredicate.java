package com.atlassian.jira.bc.user.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.MockUser;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link UserMatcherPredicate}
 *
 * @since v5.0
 * @see UserMatcherPredicate
 */
public class TestUserMatcherPredicate
{
    @Test
    public void testUserMatch()
    {
        final User testUser = new MockUser("Tester", "FirstTest LastTest", "this.tester@atlassian.com");

        // These queries should match any user part
        assertTrue(matches(testUser, "Test", true));
        assertTrue(matches(testUser, "Tester", true));
        assertTrue(matches(testUser, "test", true));
        assertTrue(matches(testUser, "TEST", true));
        assertTrue(matches(testUser, "First", true));
        assertTrue(matches(testUser, "Last", true));
        assertTrue(matches(testUser, "LastTest", true));
        assertTrue(matches(testUser, "Lasttest", true));
        assertTrue(matches(testUser, "FirstTest LastTest", true));

        // These queries should not match any user part
        assertFalse(matches(testUser, "Testing", true));
        assertFalse(matches(testUser, "not", true));
        assertFalse(matches(testUser, "tester@atlassian.com", true));
        assertFalse(matches(testUser, "atlassian.com", true));
        assertFalse(matches(testUser, ".com", true));
        assertFalse(matches(testUser, "@", true));
        assertFalse(matches(testUser, "atlas", true));

        // These queries should match the email address only, and fail if it isn't searched
        assertTrue(matches(testUser, "this", true));
        assertFalse(matches(testUser, "this", false));
        assertTrue(matches(testUser, "this.tester", true));
        assertFalse(matches(testUser, "this.tester", false));
        assertTrue(matches(testUser, "this.tester@atlassian.com", true));
        assertFalse(matches(testUser, "this.tester@atlassian.com", false));
    }

    @Test
    public void testUserMatchWithEmailQuery()
    {
        final User testUser = new MockUser("Tester", "FirstTest LastTest", "email.address@atlassian.com");

        // match with both queries
        assertTrue(matchesWithSeparateEmailQuery(testUser, "Test", "Email.Address", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "Tester", "Email", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "test", "email.address", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "TEST", "EMAIL.ADDRESS", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "First", "Email", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "Last", "Email", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "LastTest", "Email.address@atlassian.com", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "Lasttest", "Email.address@Atlassian", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "FirstTest LastTest", "Email.address", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "FirstTest LastTest", "Email.address@atlassian.com", true));

        // match with only user query is already tested in testUserMatch()

        // match with only email query
        assertTrue(matchesWithSeparateEmailQuery(testUser, "", "Email.Address", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "", "Email", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "", "email.address", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "", "EMAIL.ADDRESS", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "", "Email.address@atlassian.com", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "", "Email.Address@Atlassian", true));

        // The user query matches only the email address, but fail because of the separate email query
        assertFalse(matchesWithSeparateEmailQuery(testUser, "Email.Address", "Email.Address", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "Email", "Email.Address", true));

        // when both queries are specified, both must match
        assertFalse(matchesWithSeparateEmailQuery(testUser, "Testing", "Email", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "FirstTester", "Email", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "LastTester", "Email", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "Test", "tester@atlassian.com", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "Test", "atlassian.com", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "Test", ".com", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "Test", "@", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "Test", "atlas", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "Testing", "tester@atlassian.com", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "FirstTester", "atlassian.com", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "LastTester", ".com", true));
        // When only email is specified, email must match
        assertFalse(matchesWithSeparateEmailQuery(testUser, "", "tester@atlassian.com", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "", "atlassian.com", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "", ".com", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "", "@", true));
        assertFalse(matchesWithSeparateEmailQuery(testUser, "", "atlas", true));

        // no queries
        assertTrue(matchesWithSeparateEmailQuery(testUser, "", "", true));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "", "email.address@atlassian.com", false));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "", "NotEmail", false));
        assertTrue(matchesWithSeparateEmailQuery(testUser, "", "", false));
    }

    private boolean matches(User user, String query, boolean canMatchEmailAddress)
    {
        UserMatcherPredicate matcher = new UserMatcherPredicate(query, canMatchEmailAddress);
        return matcher.apply(user);
    }

    private boolean matchesWithSeparateEmailQuery(User user, String nameQuery, String emailQuery, boolean canMatchEmailAddress)
    {
        UserMatcherPredicate matcher = new UserMatcherPredicate(nameQuery, emailQuery, canMatchEmailAddress);
        return matcher.apply(user);
    }
}
