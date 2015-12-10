package com.atlassian.jira.jql.resolver;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link com.atlassian.jira.jql.resolver.ResolutionIndexInfoResolver}.
 *
 * @since v4.0
 */
public class TestResolutionIndexInfoResolver extends MockControllerTestCase
{
    private NameResolver<Resolution> mockNameResolver;

    @Before
    public void setUp() throws Exception
    {
        mockNameResolver = getMock(NameResolver.class);
    }

    @Test
    public void testConstructor()
    {
        replay();

        try
        {
            new ResolutionIndexInfoResolver(null);
            fail("epxected problemo constructing from nulls");
        }
        catch (RuntimeException yay)
        {
            //expected
        }
    }

    @Test
    public void testGetIndexedValuesStringUnresolved()
    {
        replay();

        ResolutionIndexInfoResolver resolver = new ResolutionIndexInfoResolver(mockNameResolver);

        List<String> expectedIds = CollectionBuilder.newBuilder("-1").asList();
        assertEquals(expectedIds, resolver.getIndexedValues("unresolved"));
    }

    @Test
    public void testGetIndexedValuesStringUnresolvedQuoted()
    {
        EasyMock.expect(mockNameResolver.getIdsFromName("unresolved"))
                .andReturn(Collections.singletonList("123"));

        replay();

        ResolutionIndexInfoResolver resolver = new ResolutionIndexInfoResolver(mockNameResolver);

        List<String> expectedIds = CollectionBuilder.newBuilder("123").asList();
        assertEquals(expectedIds, resolver.getIndexedValues("'unresolved'"));
    }

    @Test
    public void testGetIndexedValuesStringSomethingElse()
    {
        EasyMock.expect(mockNameResolver.getIdsFromName("somethingelse"))
                .andReturn(Collections.singletonList("123"));

        replay();

        ResolutionIndexInfoResolver resolver = new ResolutionIndexInfoResolver(mockNameResolver);

        List<String> expectedIds = CollectionBuilder.newBuilder("123").asList();
        assertEquals(expectedIds, resolver.getIndexedValues("somethingelse"));
    }

    @Test
    public void testCleanOperand() throws Exception
    {
        replay();
        
        assertEquals("fixed", ResolutionIndexInfoResolver.cleanOperand("fixed"));
        assertEquals("'fixed'", ResolutionIndexInfoResolver.cleanOperand("'fixed'"));
        assertEquals("''fixed''", ResolutionIndexInfoResolver.cleanOperand("''fixed''"));
        assertEquals("\"fixed\"", ResolutionIndexInfoResolver.cleanOperand("\"fixed\""));
        assertEquals("\"\"fixed\"\"", ResolutionIndexInfoResolver.cleanOperand("\"\"fixed\"\""));

        assertEquals("UNRESolved", ResolutionIndexInfoResolver.cleanOperand("UNRESolved"));
        assertEquals("UNRESolved", ResolutionIndexInfoResolver.cleanOperand("'UNRESolved'"));
        assertEquals("'UNRESolved'", ResolutionIndexInfoResolver.cleanOperand("''UNRESolved''"));
        assertEquals("UNRESolved", ResolutionIndexInfoResolver.cleanOperand("\"UNRESolved\""));
        assertEquals("\"UNRESolved\"", ResolutionIndexInfoResolver.cleanOperand("\"\"UNRESolved\"\""));
        assertEquals("'UNRESolved", ResolutionIndexInfoResolver.cleanOperand("''UNRESolved'"));

        assertEquals(" 'UNRESolved'", ResolutionIndexInfoResolver.cleanOperand(" 'UNRESolved'"));
        assertEquals("s 'UNRESolved'", ResolutionIndexInfoResolver.cleanOperand("s 'UNRESolved'"));
        assertEquals("s 's'UNRESolved's'", ResolutionIndexInfoResolver.cleanOperand("s 's'UNRESolved's'"));
    }
}
