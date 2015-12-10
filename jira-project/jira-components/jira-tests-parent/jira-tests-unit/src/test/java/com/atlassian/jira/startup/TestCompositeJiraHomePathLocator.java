package com.atlassian.jira.startup;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestCompositeJiraHomePathLocator
{
//    public void testCompositeNull() throws Exception
//    {
//        try
//        {
//            new CompositeJiraHomePathLocator(null);
//            fail("IllegalArgumentException expected");
//        }
//        catch (IllegalArgumentException expected)
//        {
//        }
//    }
//
//    public void testCompositeNullSecond() throws Exception
//    {
//        try
//        {
//            new CompositeJiraHomePathLocator(new JiraHomePathLocator(){
//                public String getJiraHome()
//                {
//                    return null;
//                }
//            }, null);
//            fail("IllegalArgumentException expected");
//        }
//        catch (IllegalArgumentException expected)
//        {
//        }
//    }

    @Test
    public void testCompositeNull() throws Exception
    {
        final CompositeJiraHomePathLocator pathLocator = new CompositeJiraHomePathLocator(new MockLocator("   \t\r"));
        assertEquals(null, pathLocator.getJiraHome());
    }

    @Test
    public void testCompositeWithLeadingSpace() throws Exception
    {
        final CompositeJiraHomePathLocator pathLocator = new CompositeJiraHomePathLocator(new MockLocator("    /bugger"));
        assertEquals("/bugger", pathLocator.getJiraHome());
    }

    @Test
    public void testCompositeWithTrailingSpace() throws Exception
    {
        final CompositeJiraHomePathLocator pathLocator = new CompositeJiraHomePathLocator(new MockLocator("/bugger   "));
        assertEquals("/bugger", pathLocator.getJiraHome());
    }

    @Test
    public void testCompositeNullFirst() throws Exception
    {
        final CompositeJiraHomePathLocator pathLocator = new CompositeJiraHomePathLocator(new MockLocator("   \t\r"), new MockLocator("second!"));
        assertEquals("second!", pathLocator.getJiraHome());
    }

    @Test
    public void testCompositeNotNullFirst() throws Exception
    {
        final CompositeJiraHomePathLocator pathLocator = new CompositeJiraHomePathLocator(new MockLocator("first!"), new MockLocator("second!"));
        assertEquals("first!", pathLocator.getJiraHome());
    }

    private class MockLocator implements JiraHomePathLocator
    {
        private final String home;

        public MockLocator(final String home)
        {
            this.home = home;
        }


        public String getJiraHome()
        {
            return home;
        }

        public String getDisplayName()
        {
            return "";
        }
    }
}
