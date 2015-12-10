package com.atlassian.jira.util.system.release;

/**
 * This class is here purely as a marker as our unit-tests need to run in a different package to where the actual
 * release.info file lives. The build otherwise interferes with the operation of the tests - the existence of the real
 * release.info causes them to fail.
 */
public final class ReleaseInfoMarker
{
    private ReleaseInfoMarker()
    {}
}