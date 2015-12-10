package com.atlassian.jira.imports.project.core;

import java.util.Collections;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.external.beans.ExternalProject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/** @since v3.13 */
public class TestBackupOverviewImpl
{
    @Test
    public void testIllegalConstructorArguments()
    {
        try
        {
            new BackupOverviewImpl(null, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // We want this to happen
        }

        try
        {
            new BackupOverviewImpl(null, EasyList.build(new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST)));
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // We want this to happen
        }

    }

    @Test
    public void testGetProject()
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("123", "Enterprise", Collections.EMPTY_LIST, true, Collections.EMPTY_MAP, 0);
        final BackupOverviewImpl backupOverview = new BackupOverviewImpl(backupSystemInformation, EasyList.build(backupProject));

        assertEquals(backupProject, backupOverview.getProject("TST"));
    }

    @Test
    public void testGetProjects()
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("123", "Enterprise", Collections.EMPTY_LIST, true, Collections.EMPTY_MAP, 0);
        final BackupOverviewImpl backupOverview = new BackupOverviewImpl(backupSystemInformation, EasyList.build(backupProject));

        assertEquals(1, backupOverview.getProjects().size());
    }

    @Test
    public void testGetProjectsIsOrdered()
    {
        final ExternalProject project1 = new ExternalProject();
        project1.setKey("TST");
        project1.setName("Test");
        final ExternalProject project2 = new ExternalProject();
        project2.setKey("ANA");
        project2.setName("Another");
        final BackupProject backupProject1 = new BackupProjectImpl(project1, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        final BackupProject backupProject2 = new BackupProjectImpl(project2, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("123", "Enterprise", Collections.EMPTY_LIST, true, Collections.EMPTY_MAP, 0);
        final BackupOverviewImpl backupOverview = new BackupOverviewImpl(backupSystemInformation, EasyList.build(backupProject1, backupProject2));

        assertEquals(2, backupOverview.getProjects().size());
        assertEquals(backupProject2, backupOverview.getProjects().get(0));
        assertEquals(backupProject1, backupOverview.getProjects().get(1));
    }

    @Test
    public void testGetSystemInformation()
    {
        final ExternalProject project1 = new ExternalProject();
        project1.setKey("TST");
        project1.setName("Test");
        final BackupProject backupProject1 = new BackupProjectImpl(project1, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("123", "Enterprise", Collections.EMPTY_LIST, true, Collections.EMPTY_MAP, 0);
        final BackupOverviewImpl backupOverview = new BackupOverviewImpl(backupSystemInformation, EasyList.build(backupProject1));

        assertEquals(backupSystemInformation, backupOverview.getBackupSystemInformation());
        assertEquals(1, backupOverview.getProjects().size());
        assertEquals(backupProject1, backupOverview.getProjects().get(0));
    }
}
