package com.atlassian.jira.config.util;

import java.io.File;

import javax.annotation.Nonnull;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link AbstractJiraHome}.
 *
 * @since v4.1
 */
public class TestAbstractJiraHome
{

    private final File localHome = new File("testLocalHome");
    private final File sharedHome = new File("testSharedHome");

    @Test
    public void testGetLogDirectory() throws Exception
    {
        final FixedHome home = new FixedHome(localHome, sharedHome);

        assertEquals(home.getLogDirectory(), new File(localHome, "log"));
    }

    @Test
    public void testGetCachesDirectory() throws Exception
    {
        final FixedHome home = new FixedHome(localHome, sharedHome);

        assertEquals(home.getCachesDirectory(), new File(localHome, "caches"));
    }

    @Test
    public void testGetExportDirectory() throws Exception
    {
        final FixedHome home = new FixedHome(localHome, sharedHome);

        assertEquals(home.getExportDirectory(), new File(sharedHome, "export"));
    }

    @Test
    public void testGetPluginsDirectory() throws Exception
    {
        final FixedHome home = new FixedHome(localHome, sharedHome);

        assertEquals(home.getPluginsDirectory(), new File(sharedHome, "plugins"));
    }

    @Test
    public void testGetDataDirectory() throws Exception
    {
        final FixedHome home = new FixedHome(localHome, sharedHome);

        assertEquals(home.getDataDirectory(), new File(sharedHome, "data"));
    }

    public static class FixedHome extends AbstractJiraHome
    {
        private final File localHome;
        private final File sharedHome;

        public FixedHome(final File localHome, final File sharedHome)
        {
            this.localHome = localHome;
            this.sharedHome = sharedHome;
        }

        @Nonnull
        @Override
        public File getHome()
        {
            return sharedHome;
        }

        @Nonnull
        @Override
        public File getLocalHome()
        {
            return localHome;
        }
    }
}
