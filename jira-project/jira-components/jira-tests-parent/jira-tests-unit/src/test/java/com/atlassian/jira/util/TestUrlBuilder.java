package com.atlassian.jira.util;

import java.util.Map;

import com.atlassian.jira.util.collect.MapBuilder;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Simple unit test for {@link com.atlassian.jira.util.UrlBuilder}.
 *
 * @since v4.0
 */
public class TestUrlBuilder
{
    @Test
    public void testAddAnchor() throws Exception
    {
        UrlBuilder urlBuilder = new UrlBuilder("froo.com/", "UTF-8", false);
        urlBuilder.addAnchor("boo\u00a5");
        assertEquals("froo.com/#boo%C2%A5", urlBuilder.asUrlString());
    }

    @Test
    public void testAnchorAddedLast()
    {
        UrlBuilder urlBuilder = new UrlBuilder("froo.com/", "UTF-8", false);
        urlBuilder.addAnchor("anchortest");
        urlBuilder.addParameter("test", "blah");
        assertEquals("froo.com/?test=blah#anchortest", urlBuilder.asUrlString());
    }

    @Test
    public void testAddParameterUnsafe()
    {
        UrlBuilder urlBuilder = new UrlBuilder("froo.com/");
        urlBuilder.addParameterUnsafe("f", "%20%3aboo");
        assertEquals("froo.com/?f=%20%3aboo", urlBuilder.asUrlString());

        //The passed parameter should not be escaped.
        urlBuilder = new UrlBuilder("froo.com/");
        urlBuilder.addParameterUnsafe("f", "b=c");
        assertEquals("froo.com/?f=b=c", urlBuilder.asUrlString());
    }

    @Test
    public void testAddParameter()
    {
        //make sure the parameter is escaped with the correct encoding.
        UrlBuilder urlBuilder = new UrlBuilder("froo.com/", "UTF-8", false);
        urlBuilder.addParameter("f", "boo\u00a5");
        assertEquals("froo.com/?f=boo%C2%A5", urlBuilder.asUrlString());

        urlBuilder = new UrlBuilder("froo.com/", "ISO-8859-1", false);
        urlBuilder.addParameter("f", "boo\u00a5");
        assertEquals("froo.com/?f=boo%A5", urlBuilder.asUrlString());

        urlBuilder = new UrlBuilder("froo.com/", "US-ASCII", false);
        urlBuilder.addParameter("f", "a").addParameter("f", "b");
        assertEquals("froo.com/?f=a&f=b", urlBuilder.asUrlString());

        urlBuilder = new UrlBuilder("froo.com/?already=here", "US-ASCII", false);
        urlBuilder.addParameter("f", "a").addParameter("f", "b").addParameter("%", "blah").addParameter("1", new StringBuilder("%"));
        assertEquals("froo.com/?already=here&f=a&f=b&%25=blah&1=%25", urlBuilder.asUrlString());
    }

    @Test
    public void testAddParametersFromMap() throws Exception
    {
        final Map<String, String> map = MapBuilder.<String, String> newBuilder().add("f", "boo\u00a5").add("g", "hoo").toMap();

        UrlBuilder urlBuilder = new UrlBuilder("froo.com/", "UTF-8", false);
        urlBuilder.addParametersFromMap(map);
        assertEquals("froo.com/?f=boo%C2%A5&g=hoo", urlBuilder.asUrlString());

        urlBuilder = new UrlBuilder("froo.com/", "ISO-8859-1", false);
        urlBuilder.addParametersFromMap(map);
        assertEquals("froo.com/?f=boo%A5&g=hoo", urlBuilder.asUrlString());

        final Map<String, String> map1 = MapBuilder.<String, String> newBuilder().add("f", "a").toMap();
        final Map<String, Object> map2 = MapBuilder.<String, Object> newBuilder().add("f", 56).toMap();

        urlBuilder = new UrlBuilder("froo.com/?already=here", "US-ASCII", false);
        urlBuilder.addParametersFromMap(map1).addParametersFromMap(map2).addParameter("%", "blah");
        assertEquals("froo.com/?already=here&f=a&f=56&%25=blah", urlBuilder.asUrlString());
    }

    @Test
    public void testSnippet() throws Exception
    {
        final UrlBuilder urlBuilder = new UrlBuilder("", "UTF-8", true);
        urlBuilder.addParameter("f", "boo");
        assertEquals("&f=boo", urlBuilder.asUrlString());
    }

    @Test
    public void testPercent() throws Exception
    {
        final UrlBuilder urlBuilder = new UrlBuilder("", "UTF-8", true);
        urlBuilder.addParameter("f", "per%cent");
        assertEquals("&f=per%25cent", urlBuilder.asUrlString());
    }

    @Test
    public void testAddPathUnsafe() throws Exception
    {
        final UrlBuilder urlBuilder = new UrlBuilder("", "UTF-8", false);
        urlBuilder.addPathUnsafe("one");
        assertEquals("one", urlBuilder.asUrlString());

        urlBuilder.addPathUnsafe("two");
        assertEquals("one/two", urlBuilder.asUrlString());

        urlBuilder.addPathUnsafe("/three/");
        assertEquals("one/two/three/", urlBuilder.asUrlString());

        urlBuilder.addPathUnsafe("/four");
        assertEquals("one/two/three/four", urlBuilder.asUrlString());
    }

    @Test
    public void testAddPath() throws Exception
    {
        final UrlBuilder urlBuilder = new UrlBuilder("/one/", "UTF-8", false);
        urlBuilder.addPath("two");
        assertEquals("/one/two", urlBuilder.asUrlString());
        urlBuilder.addPath("/three/");
        assertEquals("/one/two/three/", urlBuilder.asUrlString());
        urlBuilder.addPath("/four");
        assertEquals("/one/two/three/four", urlBuilder.asUrlString());
        urlBuilder.addPath("boo\u00a5");
        assertEquals("/one/two/three/four/boo%C2%A5", urlBuilder.asUrlString());
    }

    @Test
    public void testAddPathsRelativePath() throws Exception
    {
        assertThat(new UrlBuilder("/one/", "UTF-8", false).addPaths("two/three").asUrlString(), is("/one/two/three"));
    }

    @Test
    public void testAddPathsAbsolutePath() throws Exception
    {
        assertThat(new UrlBuilder("/one/", "UTF-8", false).addPaths("/two/three").asUrlString(), is("/one/two/three"));
    }

    @Test
    public void testAddPathsEmptySlash() throws Exception
    {
        assertThat(new UrlBuilder("/one/", "UTF-8", false).addPaths("/").asUrlString(), is("/one/"));
    }

    @Test
    public void testAddPathAndParameter() throws Exception
    {
        final UrlBuilder urlBuilder = new UrlBuilder("/one/", "UTF-8", false);
        urlBuilder.addPath("two/");
        assertEquals("/one/two/", urlBuilder.asUrlString());
        urlBuilder.addParameter("param1", "value1");
        assertEquals("/one/two/?param1=value1", urlBuilder.asUrlString());
        urlBuilder.addPath("three");
        assertEquals("/one/two/three?param1=value1", urlBuilder.asUrlString());
    }
}
