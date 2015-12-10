package com.atlassian.jira.crowd.embedded.ofbiz;

import java.io.Serializable;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Unit test of DirectoryEntityKey.
 *
 * @since 6.2
 */
public class TestDirectoryEntityKey
{
    @Test
    public void keysShouldBeSerializableForUseInACluster()
    {
        // Set up
        final DirectoryEntityKey key = DirectoryEntityKey.getKeyLowerCase(20, "foo");

        // Invoke and check
        assertTrue("Keys should be Serializable for use in a cluster", Serializable.class.isInstance(key));
    }
}
