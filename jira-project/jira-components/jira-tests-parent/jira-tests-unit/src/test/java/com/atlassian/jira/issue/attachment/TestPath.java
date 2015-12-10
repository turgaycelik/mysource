package com.atlassian.jira.issue.attachment;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestPath
{
    @Test
    public void testPaths()
    {
        assertTrue("'' is root", new Path("").isRoot());
        assertTrue("'/' is root", new Path("/").isRoot());
        assertEquals(new Path("/"), new Path(""));
    }

    @Test
    public void testWindowsPath()
    {
        Path p = new Path("hello\\there");
        assertEquals(1, p.getComponents().length);

        p = new Path("hello\\there", true);
        assertEquals(2, p.getComponents().length);

        p = new Path("\\hello\\there", true);
        assertEquals(3, p.getComponents().length);

        Path p2 = new Path(p, "foo", true);
        assertEquals(4, p2.getComponents().length);

        p2 = new Path(p, "foo\\bar", true);
        assertEquals(5, p2.getComponents().length);
    }

    @Test
    public void testAbbrev()
    {
        assertAbbrev("the/brown", "the/brown", 5);
        assertAbbrev("the/.../brown", "the/quick/brown", 5);
        assertAbbrev("the/.../brown", "the/quic1/quic2/brown", 5);
        assertAbbrev("the/.../brown", "the/quic1/very/quic2/brown", 5);

        assertAbbrev("the/quic1/.../quic2/brown", "the/quic1/very/quic2/brown", 21);
        assertAbbrev("longdir/dir1/the/quick/brown/.../blah/foo/bar/file.txt",
                "longdir/dir1/the/quick/brown/fox/jumps/over/the/lazy/dog/too/many/cooks/spoil/the/broth/blah/foo/bar/file.txt", 50);
    }

    private static void assertAbbrev(String expected, String input, int maxlen)
    {
        assertEquals(expected, new Path(input).abbreviate(maxlen).getPath());
    }

    @Test
    public void testJoin()
    {
        assertEquals("", Path.join("", ""));
        assertEquals("/", Path.join("/", ""));
        assertEquals("/", Path.join("", "/"));
        assertEquals("/", Path.join("/", "/"));

        assertEquals("asd", Path.join("asd", ""));
        assertEquals("asd/", Path.join("asd", "/"));
        assertEquals("asd", Path.join("", "asd"));
        assertEquals("/asd", Path.join("/", "asd"));

        assertEquals("asd/asd", Path.join("asd", "asd"));
        assertEquals("asd/asd", Path.join("asd/", "asd"));
        assertEquals("asd/asd", Path.join("asd", "/asd"));
        assertEquals("asd/asd", Path.join("asd/", "/asd"));
    }

    @Test
    public void testExtension()
    {
        assertEquals("txt", new Path("file.txt").getExtension());
        assertEquals("java", new Path("file.txt.java").getExtension());
        assertEquals(null, new Path("file").getExtension());
        assertEquals(null, new Path("file.").getExtension());
    }
}
