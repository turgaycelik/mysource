package com.atlassian.jira.ofbiz;

import java.util.Collections;
import java.util.Set;

import com.atlassian.jira.util.Function;

import org.junit.Test;

import static com.atlassian.jira.util.collect.CollectionBuilder.newBuilder;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestFieldSupportValidator
{
    FieldSupportValidator getPassThroughValidator(final String name, final Set<String> badTypes)
    {
        return new FieldSupportValidator(name, badTypes, new Function<String, Function<String, String>>()
        {
            public Function<String, String> get(final String outer)
            {
                return new Function<String, String>()
                {
                    public String get(final String input)
                    {
                        return input;
                    }
                };
            }
        });
    }

    @Test
    public void testGoodMapping()
    {
        getPassThroughValidator("name", newBuilder("bad").asSet()).check("table", "good");
    }

    @Test
    public void testBadMapping()
    {
        try
        {
            getPassThroughValidator("badname", newBuilder("bad").asSet()).check("table", "bad");
            fail("UnsupportedTypeException expected");
        }
        catch (final UnsupportedTypeException expected)
        {
            assertTrue("exception starts with badname", expected.getMessage().startsWith("badname: "));
        }
    }

    @Test
    public void testNullTable()
    {
        try
        {
            getPassThroughValidator("somename", newBuilder("bad").asSet()).check(null, "bad");
            fail("UnsupportedTypeException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testNullField()
    {
        try
        {
            getPassThroughValidator("name", newBuilder("bad").asSet()).check("table", null);
            fail("UnsupportedTypeException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testNoTable()
    {
        try
        {
            new FieldSupportValidator("something", Collections.<String> emptySet(), new Function<String, Function<String, String>>()
            {
                public Function<String, String> get(final String outer)
                {
                    return null;
                }
            }).check("table", "field");
            fail("MissingTableException expected");
        }
        catch (final MissingTableException expected)
        {
            assertTrue("exception starts with something", expected.getMessage().startsWith("something: "));
        }
    }

    @Test
    public void testNoField()
    {
        try
        {
            new FieldSupportValidator("expected", Collections.<String> emptySet(), new Function<String, Function<String, String>>()
            {
                public Function<String, String> get(final String outer)
                {
                    return new Function<String, String>()
                    {
                        public String get(final String input)
                        {
                            return null;
                        }
                    };
                }
            }).check("table", "field");
            fail("MissingFieldException expected");
        }
        catch (final MissingFieldException expected)
        {
            assertTrue("exception starts with expected", expected.getMessage().startsWith("expected: "));
        }
    }
}
