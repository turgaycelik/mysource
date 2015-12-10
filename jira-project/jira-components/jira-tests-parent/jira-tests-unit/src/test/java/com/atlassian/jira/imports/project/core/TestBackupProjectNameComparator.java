package com.atlassian.jira.imports.project.core;

import java.util.Collections;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.external.beans.ExternalProject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestBackupProjectNameComparator
{
    @Test
    public void testComparator()
    {
        ExternalProject project1 = new ExternalProject();
        project1.setName("Zebra project");
        ExternalProject project2 = new ExternalProject();
        project2.setName("Godzilla project");
        ExternalProject project3 = new ExternalProject();
        project3.setName("Ardvark project");

        BackupProject backupProject1 = new BackupProjectImpl(project1, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        BackupProject backupProject2 = new BackupProjectImpl(project2, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        BackupProject backupProject3 = new BackupProjectImpl(project3, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        BackupProjectNameComparator comparator =  new BackupProjectNameComparator();
        assertTrue(comparator.compare(backupProject3, backupProject2) < 0);
        assertTrue(comparator.compare(backupProject2, backupProject3) > 0);
        assertTrue(comparator.compare(backupProject2, backupProject2) == 0);
        assertTrue(comparator.compare(null, null) == 0);
        assertTrue(comparator.compare(backupProject2, null) < 0);
        assertTrue(comparator.compare(null, backupProject2) > 0);

        final List list = EasyList.build(backupProject1, backupProject3, backupProject2);

        Collections.sort(list, comparator);
        assertEquals(backupProject3, list.get(0));
        assertEquals(backupProject2, list.get(1));
        assertEquals(backupProject1, list.get(2));
    }
}
