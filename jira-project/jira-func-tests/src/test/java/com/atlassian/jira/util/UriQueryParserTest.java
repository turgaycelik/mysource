package com.atlassian.jira.util;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;

public class UriQueryParserTest
{
    private UriQueryParser uriQueryParser;

    @Before
    public void createTestedObject()
    {
        uriQueryParser = new UriQueryParser();
    }

    @Test
    public void testParsingString() throws Exception
    {
        final Map<String, String> result = uriQueryParser.parse("param1=US%233aA&x=ddd&param3=d4fr43r%de&cswd=d3");
        assertThat("not all keys are present.", result.keySet(), containsInAnyOrder("param1", "param3", "x", "cswd"));
    }

    @Test
    public void testParsingURI() throws Exception
    {
        final Map<String, String> result = uriQueryParser.parse(URI.create("http://atlassian.com/a/path?x=5&y=9&z=ohno"));
        assertThat("not all keys are present.", result.keySet(), containsInAnyOrder("x", "y", "z"));
    }

    @Test
    public void testMappingValues() throws Exception
    {
        final Map<String, String> expected = ImmutableMap.of("a", "1", "b", "2", "c", "3", "d", "4");
        final Map<String, String> result = uriQueryParser.parse("a=1&b=2&c=3&d=4");
        assertThat(result.entrySet(), everyItem(isIn(expected.entrySet())));
    }

    @Test
    public void shouldAcceptEmptyStringsAsValues() throws Exception
    {
        Map<String, String> expected = ImmutableMap.of("a", "", "b", "", "c", "", "d", "");
        Map<String, String> result = uriQueryParser.parse("a=&b=&c=&d=");
        assertThat(result.entrySet(), everyItem(isIn(expected.entrySet())));

        expected = ImmutableMap.of("a", "");
        result = uriQueryParser.parse("a=");
        assertThat(result.entrySet(), everyItem(isIn(expected.entrySet())));
    }

    @Test
    public void testAcceptsEmptyQuery() throws Exception
    {
        Map<String, String> result = uriQueryParser.parse("");
        assertThat(result.keySet(), Matchers.<String>emptyIterable());

        result = uriQueryParser.parse(URI.create("http://localhost/path/resource"));
        assertThat(result.keySet(), Matchers.<String>emptyIterable());
    }

    @Test
    public void exceptionShouldBeThrownOnIncorrectSyntax() throws Exception
    {
        final ImmutableList<String> incorrectQueries = ImmutableList.of(
                "param1=1&&param2=2",
                "param1=3&param2",
                "=",
                "&",
                "&=&&=&=&==&=&=&=&=&=&=&&&===&=&==&"
        );

        final UriQueryParser uriQueryParser = new UriQueryParser();
        assertThat("The following patters were validated although erroneous", Iterables.filter(incorrectQueries, new Predicate<String>()
        {
            public boolean apply(final String input)
            {
                return uriQueryParser.validate(input);
            }
        }), Matchers.<String>emptyIterable());
    }
}
