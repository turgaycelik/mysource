package com.atlassian.jira.security.auth.trustedapps;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.atlassian.core.util.collection.EasyList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestTrustedApplicationUtil
{
    @Test
    public void testStringSplitWindows() throws Exception
    {
        assertLinesContains123(TrustedApplicationUtil.getLines("two\r\none\r\nthree"));
    }

    @Test
    public void testStringSplitUnix() throws Exception
    {
        assertLinesContains123(TrustedApplicationUtil.getLines("two\none\nthree"));
    }

    @Test
    public void testStringSplitMac() throws Exception
    {
        assertLinesContains123(TrustedApplicationUtil.getLines("two\rone\rthree"));
    }

    @Test
    public void testGetMultilineString() throws Exception
    {
        Set set = new LinkedHashSet(EasyList.build("three", "two", "one"));
        assertEquals("three\ntwo\none", TrustedApplicationUtil.getMultilineString(set));
    }

    @Test
    public void testStringCanoniseWindows() throws Exception
    {
        assertEquals("three\ntwo\none", TrustedApplicationUtil.canonicalize("three\r\ntwo\r\none"));
    }

    @Test
    public void testStringCanoniseUnix() throws Exception
    {
        assertEquals("three\ntwo\none", TrustedApplicationUtil.canonicalize("three\ntwo\none"));
    }

    @Test
    public void testStringCanoniseMac() throws Exception
    {
        assertEquals("three\ntwo\none", TrustedApplicationUtil.canonicalize("three\rtwo\rone"));
    }

    @Test
    public void testStringCanoniseNull() throws Exception
    {
        assertNull(TrustedApplicationUtil.canonicalize((String) null));
    }

    private void assertLinesContains123(Set lines)
    {
        assertEquals(3, lines.size());
        Iterator it = lines.iterator();
        assertEquals("two", it.next());
        assertEquals("one", it.next());
        assertEquals("three", it.next());
    }
}
