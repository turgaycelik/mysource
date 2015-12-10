/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.permission;

import java.util.Map;

import com.atlassian.jira.security.Permissions;

import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestSchemePermissions
{

    private SchemePermissions schemePermissions;

    @Before
    public void setUp() throws Exception
    {
        schemePermissions = new SchemePermissions();
        schemePermissions.getSchemePermissions();
    }

    @Test
    public void testEventExists()
    {
        assertTrue(schemePermissions.schemePermissionExists(Permissions.PROJECT_ADMIN));
    }

    @Test
    public void testEventDoesntExists()
    {
        assertFalse(schemePermissions.schemePermissionExists(237878));
    }

    @Test
    public void testGetSchemePermissionsContainsAllValues(){
        //also checks uniqueness of ids
        final Map<Integer, Permission> allValues = schemePermissions.getSchemePermissions();

        final Map<Integer, Permission> attachmentsPermissions = schemePermissions.getAttachmentsPermissions();
        final Map<Integer, Permission> issuePermissions = schemePermissions.getIssuePermissions();
        final Map<Integer, Permission> projectPermissions = schemePermissions.getProjectPermissions();
        final Map<Integer, Permission> timeTrackingPermissions = schemePermissions.getTimeTrackingPermissions();
        final Map<Integer, Permission> votersAndWatchersPermissions = schemePermissions.getVotersAndWatchersPermissions();
        final Map<Integer, Permission> commentsPermissions = schemePermissions.getCommentsPermissions();

        assertTrue(Maps.difference(attachmentsPermissions, allValues).entriesDiffering().isEmpty());
        assertTrue(Maps.difference(commentsPermissions, allValues).entriesDiffering().isEmpty());
        assertTrue(Maps.difference(issuePermissions, allValues).entriesDiffering().isEmpty());
        assertTrue(Maps.difference(projectPermissions, allValues).entriesDiffering().isEmpty());
        assertTrue(Maps.difference(timeTrackingPermissions, allValues).entriesDiffering().isEmpty());
        assertTrue(Maps.difference(votersAndWatchersPermissions, allValues).entriesDiffering().isEmpty());

        final Map<Integer, Permission> allValuesCombined = Maps.newHashMapWithExpectedSize(allValues.size());
        allValuesCombined.putAll(attachmentsPermissions);
        allValuesCombined.putAll(issuePermissions);
        allValuesCombined.putAll(projectPermissions);
        allValuesCombined.putAll(timeTrackingPermissions);
        allValuesCombined.putAll(votersAndWatchersPermissions);
        allValuesCombined.putAll(commentsPermissions);

        assertEquals(allValues, allValuesCombined);
    }


}
