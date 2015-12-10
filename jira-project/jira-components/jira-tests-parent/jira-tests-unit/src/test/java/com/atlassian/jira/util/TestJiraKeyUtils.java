package com.atlassian.jira.util;

import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.JiraKeyUtils.KeyMatcher;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import org.easymock.classextension.IMocksControl;
import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.util.JiraKeyUtils.fastFormatIssueKey;
import static com.atlassian.jira.util.JiraKeyUtils.slowFormatIssueKey;
import static org.easymock.classextension.EasyMock.createNiceControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the JiraKeyUtils class
 */
public class TestJiraKeyUtils
{
    private static final String STRING_WITH_KEYS = "I though I fixed this in BOO-123. But it was actually HISS-123. Read more about it at http://whoateallthepies.com/browse/BOO-124";
    private static final String PROJECT_REGEX = "([A-Z][A-Z]+)";
    private static final String HTTP_BASE = "http://base/";

    private KeyMatcher oldKeyMatcher;
    private ComponentAccessor.Worker oldWorker;

    @Before
    public void setUp()
    {
        IMocksControl control = createNiceControl();

        oldKeyMatcher = JiraKeyUtilsAccesor.getCurrentKeyMatcher();
        JiraKeyUtilsAccesor.setKeyMatcher(new JiraKeyUtilsAccesor.MockKeyMatcher(""));

        MockComponentWorker accessorWorker = new MockComponentWorker();
        oldWorker = ComponentAccessor.initialiseWorker(accessorWorker);

        VelocityRequestContext context = control.createMock(VelocityRequestContext.class);
        expect(context.getBaseUrl()).andStubReturn(HTTP_BASE);

        VelocityRequestContextFactory factory = control.createMock(VelocityRequestContextFactory.class);
        expect(factory.getJiraVelocityRequestContext()).andStubReturn(context);

        accessorWorker.addMock(VelocityRequestContextFactory.class, factory);

        control.replay();
    }

    @After
    public void tearDown()
    {
        JiraKeyUtilsAccesor.setKeyMatcher(oldKeyMatcher);
        ComponentAccessor.initialiseWorker(oldWorker);
    }

    @Test
    public void testDefaultGetKeysFromString() throws Exception
    {
        JiraKeyUtils.setKeyMatcher(new JiraKeyUtilsAccesor.MockKeyMatcher(""));
        final List<String> issueKeysFromString = JiraKeyUtils.getIssueKeysFromString(STRING_WITH_KEYS);
        assertEquals(issueKeysFromString.toString(), 2, issueKeysFromString.size());
        assertEquals("BOO-123", issueKeysFromString.get(0));
        assertEquals("HISS-123", issueKeysFromString.get(1));
    }

    @Test
    public void testDefaultGetKeysFromStringWithUrl() throws Exception
    {
        // Bit of a hack to get set the property explicitly, rather than from the ApplicationProperties
        JiraKeyUtils.setKeyMatcher(new JiraKeyUtilsAccesor.MockKeyMatcher("")
        {
            @Override
            public boolean isIgnoreUrlWithKey()
            {
                return false;
            }
        });
        final List<String> issueKeysFromString = JiraKeyUtils.getIssueKeysFromString(STRING_WITH_KEYS);
        assertEquals(issueKeysFromString.size(), 3);
        assertEquals("BOO-123", issueKeysFromString.get(0));
        assertEquals("HISS-123", issueKeysFromString.get(1));
        assertEquals("BOO-124", issueKeysFromString.get(2));
    }

    @Test
    public void testValidProjectKey()
    {
        assertTrue(JiraKeyUtils.validProjectKey("JRA"));
        assertTrue(JiraKeyUtils.validProjectKey("JR"));
        assertTrue(JiraKeyUtils.validProjectKey("JRADFDF"));
        assertTrue(JiraKeyUtils.validProjectKey("JRRRR"));

        assertFalse(JiraKeyUtils.validProjectKey("jra"));
        assertFalse(JiraKeyUtils.validProjectKey(null));
        assertFalse(JiraKeyUtils.validProjectKey(""));
        assertFalse(JiraKeyUtils.validProjectKey("1"));
        assertFalse(JiraKeyUtils.validProjectKey("J"));
        assertFalse(JiraKeyUtils.validProjectKey("1J"));
        assertFalse(JiraKeyUtils.validProjectKey("JR-1"));
        assertFalse(JiraKeyUtils.validProjectKey("JR1"));
        assertFalse(JiraKeyUtils.validProjectKey("JRRRR111111"));

        JiraKeyUtils.setKeyMatcher(new JiraKeyUtilsAccesor.MockKeyMatcher("([A-Z]+)"));
        assertTrue(JiraKeyUtils.validProjectKey("A"));
        assertTrue(JiraKeyUtils.validProjectKey("AB"));
        assertFalse(JiraKeyUtils.validProjectKey("A B"));

        JiraKeyUtils.setKeyMatcher(new JiraKeyUtilsAccesor.MockKeyMatcher("([A-Z]{2,})"));
        assertFalse(JiraKeyUtils.validProjectKey("A"));
        assertTrue(JiraKeyUtils.validProjectKey("AB"));
        assertFalse(JiraKeyUtils.validProjectKey("A B"));
    }

    @Test
    public void testValidIssueKey()
    {
        assertTrue(JiraKeyUtils.validIssueKey("JRA-52"));
        assertTrue(JiraKeyUtils.validIssueKey("JRA-5"));
        assertTrue(JiraKeyUtils.validIssueKey("OM-402"));
        assertTrue(JiraKeyUtils.validIssueKey("JRAAAA-5000"));

        assertFalse(JiraKeyUtils.validIssueKey("JRA-"));
        assertFalse(JiraKeyUtils.validIssueKey("-52"));
        assertFalse(JiraKeyUtils.validIssueKey("J-52"));
        assertFalse(JiraKeyUtils.validIssueKey("J52-52"));
        assertFalse(JiraKeyUtils.validIssueKey(null));
        assertFalse(JiraKeyUtils.validIssueKey(""));
        assertFalse(JiraKeyUtils.validIssueKey("1-52"));

        JiraKeyUtils.setKeyMatcher(new JiraKeyUtilsAccesor.MockKeyMatcher("([A-Z]+)"));
        assertTrue(JiraKeyUtils.validIssueKey("A-1"));
        assertTrue(JiraKeyUtils.validIssueKey("AB-1"));
        assertFalse(JiraKeyUtils.validIssueKey("-1"));
        assertFalse(JiraKeyUtils.validIssueKey("A B-1"));

        JiraKeyUtils.setKeyMatcher(new JiraKeyUtilsAccesor.MockKeyMatcher("([A-Z]{2,})"));
        assertFalse(JiraKeyUtils.validIssueKey("A-1"));
        assertTrue(JiraKeyUtils.validIssueKey("AB-1"));
        assertTrue(JiraKeyUtils.validIssueKey("ABC-1"));
        assertFalse(JiraKeyUtils.validIssueKey("-1"));
        assertFalse(JiraKeyUtils.validIssueKey("A B-1"));
    }

    @Test
    public void testCountFromKey()
    {
        assertEquals(52, JiraKeyUtils.getCountFromKey("JRA-52"));
        assertEquals(-1, JiraKeyUtils.getCountFromKey("JRA-x"));
        assertEquals(-1, JiraKeyUtils.getCountFromKey(null));
        assertEquals(-1, JiraKeyUtils.getCountFromKey("1foobar"));
    }

    @Test
    public void testProjectKeyFromIssueKey()
    {
        assertEquals("JRA", JiraKeyUtils.getProjectKeyFromIssueKey("JRA-52"));
        assertEquals("OM", JiraKeyUtils.getProjectKeyFromIssueKey("OM-406"));
        assertEquals(null, JiraKeyUtils.getProjectKeyFromIssueKey("JRA-"));
        assertEquals(null, JiraKeyUtils.getProjectKeyFromIssueKey("-52"));
        assertEquals(null, JiraKeyUtils.getProjectKeyFromIssueKey(""));
        assertEquals(null, JiraKeyUtils.getProjectKeyFromIssueKey(null));
    }

    @Test
    public void testIsKeyInString()
    {
        assertTrue(JiraKeyUtils.isKeyInString("This is a string with JRA-52 in it."));
        assertTrue(JiraKeyUtils.isKeyInString("This is a string with JRA-52 and JIRA-54 in it."));
        assertTrue(JiraKeyUtils.isKeyInString("This is a string with (JRA-52) in it."));
        assertTrue(JiraKeyUtils.isKeyInString("This is a string with [JRA-52] in it."));
        assertTrue(JiraKeyUtils.isKeyInString("This is a string with\nJRA-52 in it."));
        assertTrue(JiraKeyUtils.isKeyInString("JRA-52 in it."));
    }

    @Test
    public void testGetUrlEnd()
    {
        assertEquals(0, JiraKeyUtils.getUrlEnd("", 0));
        assertEquals(25, JiraKeyUtils.getUrlEnd("http://www.atlassian.com/", 0));
        assertEquals(28, JiraKeyUtils.getUrlEnd("123http://www.atlassian.com/", 0));
        assertEquals(28, JiraKeyUtils.getUrlEnd("123http://www.atlassian.com/", 3));
        assertEquals(25, JiraKeyUtils.getUrlEnd("http://www.atlassian.com/ rocks", 3));
    }

    @Test
    public void testLinkBugKeysDefaultRegexPattern()
    {
        final ApplicationProperties applicationProperties = new MockApplicationProperties();
        final IssueManager issueManager = new MockIssueManager()
        {
            @Override
            public MutableIssue getIssueObject(final String key) throws DataAccessException
            {
                final MockIssue issue = new MockIssue();
                issue.setKey(key);
                issue.setSummary("Summary " + key);
                return issue;
            }
        };
        JiraKeyUtilsAccesor.setKeyMatcher(new JiraKeyUtilsAccesor.MockProductionKeyMatcher(PROJECT_REGEX, applicationProperties, issueManager));

        _testLinkBugKeys("http://localhost/browse/JRA-1", "JRA-1");
        _testLinkBugKeys("JRA-1 http://localhost/browse/JRA-1", "<a href=\""+ HTTP_BASE + "/browse/JRA-1\" title=\"Summary JRA-1\" class=\"issue-link\" data-issue-key=\"JRA-1\">JRA-1</a> http://localhost/browse/JRA-1", "JRA-1");
        _testLinkBugKeys("http://localhost/browse/JRA-1/JRA-1", "JRA-1");
        _testLinkBugKeys("JRA-1 http://localhost/browse/JRA-1/JRA-1", "<a href=\"" + HTTP_BASE + "/browse/JRA-1\" title=\"Summary JRA-1\" class=\"issue-link\" data-issue-key=\"JRA-1\">JRA-1</a> http://localhost/browse/JRA-1/JRA-1", "JRA-1");
        _testLinkBugKeys("Some text around the URL http://localhost/browse/JRA-1/JRA-1 And more text", "JRA-1");
        _testLinkBugKeys("http://localhost/Something.jsp?id=JRA-1", "JRA-1");
        _testLinkBugKeys("http://localhost/Something.jsp?id=JRA-1&id2=JRA-1", "JRA-1");
        _testLinkBugKeys("Some text around the URL http://localhost/Something.jsp?id=JRA-1&id2=JRA-1 And more text", "JRA-1");
        _testLinkBugKeys("<a href=\"http://localhost/browse/JRA-1\">http://localhost/browse/JRA-1</a>", "JRA-1");
        _testLinkBugKeys("JRA-1 <a href=\"http://localhost/browse/JRA-1\">http://localhost/browse/JRA-1</a>", "<a href=\"" + HTTP_BASE + "/browse/JRA-1\" title=\"Summary JRA-1\" class=\"issue-link\" data-issue-key=\"JRA-1\">JRA-1</a> <a href=\"http://localhost/browse/JRA-1\">http://localhost/browse/JRA-1</a>", "JRA-1");
        _testLinkBugKeys("http://localhost/browse/JRA-1 JRA-1 <a href=\"http://localhost/browse/JRA-1\">http://localhost/browse/JRA-1</a>", "http://localhost/browse/JRA-1 <a href=\"" + HTTP_BASE + "/browse/JRA-1\" title=\"Summary JRA-1\" class=\"issue-link\" data-issue-key=\"JRA-1\">JRA-1</a> <a href=\"http://localhost/browse/JRA-1\">http://localhost/browse/JRA-1</a>", "JRA-1");
        _testLinkBugKeys("<a href=\"http://localhost/browse/JRA-1/JRA-1\">http://localhost/browse/JRA-1/JRA-1</a>", "JRA-1");
        _testLinkBugKeys("<p>More text <a href=\"http://localhost/browse/JRA-1/JRA-1\">http://localhost/browse/JRA-1/JRA-1</a> and more text</p>", "JRA-1");
        _testLinkBugKeys("<a href=\"http://localhost/Something.jsp?id=JRA-1\">http://localhost/Something.jsp?id=JRA-1</a>", "JRA-1");
        _testLinkBugKeys("<a href=\"http://localhost/Something.jsp?id=JRA-1&id2=JRA-1\">http://localhost/Something.jsp?id=JRA-1&id2=JRA-1</a>", "JRA-1");
        _testLinkBugKeys("<p>More text <a href=\"http://localhost/Something.jsp?id=JRA-1&id2=JRA-1\">http://localhost/Something.jsp?id=JRA-1&id2=JRA-1</a> and more text</p>", "JRA-1");

        //JRA-20358: Text renderer does not correctly link an issue key if the key is preceded by a URL that is not separated by the ' ' (space) character.
        // We now try to split the key from the URL based on invalid URL characters
        _testLinkBugKeys("http://localhost/browse/dhjdhajss\nJRA-1", "http://localhost/browse/dhjdhajss\n<a href=\""+ HTTP_BASE + "/browse/JRA-1\" title=\"Summary JRA-1\" class=\"issue-link\" data-issue-key=\"JRA-1\">JRA-1</a>", "JRA-1");
        _testLinkBugKeys("http://localhost/browse/dhjdhajss\tJRA-1", "http://localhost/browse/dhjdhajss\t<a href=\""+ HTTP_BASE + "/browse/JRA-1\" title=\"Summary JRA-1\" class=\"issue-link\" data-issue-key=\"JRA-1\">JRA-1</a>", "JRA-1");
        _testLinkBugKeys("http://localhost/browse/ONE-1|JRA-2", "http://localhost/browse/ONE-1|<a href=\""+ HTTP_BASE + "/browse/JRA-2\" title=\"Summary JRA-2\" class=\"issue-link\" data-issue-key=\"JRA-2\">JRA-2</a>", "JRA-2");
        _testLinkBugKeys("http://localhost/browse/ONE-1&amp;JRA-2", "http://localhost/browse/ONE-1&amp;JRA-2", "JRA-2");
        _testLinkBugKeys("http://localhost/browse/ONE-1&amp;&quot;JRA-2", "http://localhost/browse/ONE-1&amp;&quot;JRA-2", "JRA-2");
        _testLinkBugKeys("http://localhost/browse/ONE-1&amp;&quot;JRA-2", "http://localhost/browse/ONE-1&amp;&quot;JRA-2", "JRA-2");
    }

    @Test
    public void testLinkBugKeysRegexPattern1()
    {
        _testLinkBugKeys("http://localhost/browse/JRA-1", "JRA-1");
        _testLinkBugKeys("http://localhost/browse/JRA-1/JRA-1", "JRA-1");
        _testLinkBugKeys("http://localhost/Something.jsp?id=JRA12-1", "JRA12-1");
        _testLinkBugKeys("http://localhost/Something.jsp?id=JRA12-1&id2=JRA12-1", "JRA12-1");
        _testLinkBugKeys("<a href=\"http://localhost/browse/JRA123-1\">http://localhost/browse/JRA123-1</a>", "JRA123-1");
        _testLinkBugKeys("<a href=\"http://localhost/browse/JRA123-1/JRA123-1\">http://localhost/browse/JRA123-1/JRA123-1</a>", "JRA123-1");
        _testLinkBugKeys("<a href=\"http://localhost/Something.jsp?id=JRA1234-1\">http://localhost/Something.jsp?id=JRA1234-1</a>", "JRA1234-1");
        _testLinkBugKeys("<a href=\"http://localhost/Something.jsp?id=JRA1234-1&id2=JRA1234-1\">http://localhost/Something.jsp?id=JRA1234-1&id2=JRA1234-1</a>", "JRA1234-1");
    }

    @Test
    public void testLinkBugKeysnWithMultipleRegexpGroups()
    {
        _testLinkBugKeys("http://localhost/browse/1JRA-1", "1JRA-1");
        _testLinkBugKeys("http://localhost/browse/1JRA-1/1JRA-1", "1JRA-1");
        _testLinkBugKeys("http://localhost/Something.jsp?id=2JRA12-1", "2JRA12-1");
        _testLinkBugKeys("http://localhost/Something.jsp?id=2JRA12-1&id2=2JRA12-1", "2JRA12-1");
        _testLinkBugKeys("<a href=\"http://localhost/browse/3JRA123-1\">http://localhost/browse/3JRA123-1</a>", "3JRA123-1");
        _testLinkBugKeys("<a href=\"http://localhost/browse/3JRA123-1/3JRA123-1\">http://localhost/browse/3JRA123-1/3JRA123-1</a>", "3JRA123-1");
        _testLinkBugKeys("<a href=\"http://localhost/Something.jsp?id=4JRA1234-1\">http://localhost/Something.jsp?id=4JRA1234-1</a>", "4JRA1234-1");
        _testLinkBugKeys("<a href=\"http://localhost/Something.jsp?id=4JRA1234-1&id2=4JRA1234-1\">http://localhost/Something.jsp?id=4JRA1234-1&id2=4JRA1234-1</a>", "4JRA1234-1");
    }

    private void _testLinkBugKeys(final String body, final String key)
    {
        final String result = JiraKeyUtils.linkBugKeys(body);
        assertEquals(body, result);
    }

    private void _testLinkBugKeys(final String body, final String expected, final String key)
    {
        final String result = JiraKeyUtils.linkBugKeys(body);
        assertEquals(expected, result);
    }

    @Test
    public void testIsKeyInStringNullIssueKey()
    {
        try
        {
            JiraKeyUtils.isKeyInString(null, null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("A valid key must be passed.", e.getMessage());
        }
    }

    @Test
    public void testIsKeyInStringNoIssueKey()
    {
        try
        {
            JiraKeyUtils.isKeyInString("", null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            assertEquals("A valid key must be passed.", e.getMessage());
        }
    }

    @Test
    public void testIsKeyInStringNullBody()
    {
        assertFalse(JiraKeyUtils.isKeyInString("JRA-1", null));
    }

    @Test
    public void testIsKeyInStringNoBody()
    {
        assertFalse(JiraKeyUtils.isKeyInString("JRA-1", ""));
    }

    @Test
    public void testIsKeyInStringNoKeyPresent()
    {
        final String issueKey = "JRA-1";
        final String body = "This is some message.";
        assertFalse(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInStringWiderKeyPresent()
    {
        final String issueKey = "JRA-1";
        final String body = "This is some message JRA-11.";
        assertFalse(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInStringPrefixAnotherKey()
    {
        final String issueKey = "JRA-1";
        final String body = "This commit is for issue RAJRA-1 for the project AJRA not JRA";
        assertFalse(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInStringPrefixByDigit()
    {
        final String issueKey = "JRA-1";
        final String body = "This commit is for 121JRA-1 for";
        assertTrue(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInStringPrefixByFullStop()
    {
        final String issueKey = "JRA-1";
        final String body = "This commit is for issue.JRA-1 for";
        assertTrue(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInStringPrefixByColon()
    {
        final String issueKey = "JRA-1";
        final String body = "This commit is for issue:JRA-1 for";
        assertTrue(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInStringPrefixByUrl()
    {
        final String issueKey = "JRA-1";
        final String body = "This commit is for http://jira.atlassian.com/browse/JRA-1 for";
        assertFalse(JiraKeyUtils.isKeyInString(issueKey, body));
        JiraKeyUtils.setKeyMatcher(new JiraKeyUtilsAccesor.MockKeyMatcher("")
        {
            @Override
            public boolean isIgnoreUrlWithKey()
            {
                return false;
            }
        });
        assertTrue(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInStringBackwardsCompatible()
    {
        final String issueKey = "JRA-1";
        final String body = "This commit is for AJRA-1 for the project AJRA not JRA";
        JiraKeyUtils.setKeyMatcher(new JiraKeyUtilsAccesor.MockKeyMatcher("")
        {
            @Override
            public boolean isKeyDetectionBackwardsCompatible()
            {
                return true;
            }
        });
        assertTrue(JiraKeyUtils.isKeyInString(issueKey, body));
        assertTrue(JiraKeyUtils.isKeyInString(issueKey, "This is another test to find JRA-1"));
        JiraKeyUtils.setKeyMatcher(new JiraKeyUtilsAccesor.MockKeyMatcher("")
        {
            @Override
            public boolean isKeyDetectionBackwardsCompatible()
            {
                return false;
            }
        });
        assertFalse(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInStringManyWiderKeyPresent()
    {
        final String issueKey = "JRA-1";
        final String body = "This JRA-11 is JRA-11 some message JRA-11";
        assertFalse(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInStringKeyPresent()
    {
        final String issueKey = "JRA-1";
        final String body = "This is some message JRA-1 here.";
        assertTrue(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInMultipleStringKeyPresent()
    {
        final String issueKey = "JRA-1";
        final String body = "This is some message JRA-1 here JRA-1";
        assertTrue(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInStringKeyPresentAtEnd()
    {
        final String issueKey = "JRA-1";
        final String body = "This is some message JRA-1";
        assertTrue(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInStringWiderKeyAndKeysPresentAtEnd()
    {
        final String issueKey = "JRA-1";
        final String body = "This is some JRA-11 message JRA-1";
        assertTrue(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInStringWiderKeyAndKeysPresent1()
    {
        final String issueKey = "JRA-1";
        final String body = "This is some JRA-11 message JRA-1 here";
        assertTrue(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testIsKeyInStringWiderKeyAndKeysPresent2()
    {
        final String issueKey = "JRA-1";
        final String body = "This is some JRA-1 message JRA-11 here";
        assertTrue(JiraKeyUtils.isKeyInString(issueKey, body));
    }

    @Test
    public void testFormatIssueKey()
    {

        assertEquals("JRA-52", fastFormatIssueKey("JRA", 52L));
        assertEquals("J-52", fastFormatIssueKey("J", 52L));
        // We should support any pattern for project key that has already gone to database
        assertEquals("!@#$%^.;123-1", fastFormatIssueKey("!@#$%^.;123", 1L));

        // There are no negative issue numbers, return null
        assertThat(fastFormatIssueKey("J", -52L), IsNull.nullValue());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testFormatIssueKeyForWrongIssueNumber() throws Exception
    {
        slowFormatIssueKey("JRA", -1L);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testFormatIssueKeyForEmptyProjectKey() throws Exception
    {
        slowFormatIssueKey("", 1L);
    }
}
