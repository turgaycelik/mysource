package com.atlassian.jira.issue.comparator;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertTrue;

public class TestVersionComparator
{
    VersionComparator versionComparator = new VersionComparator();
    GenericValue project1 = new MockGenericValue("Project", EasyMap.build("id", 11L, "key", "ABC", "name", "alpha"));
    GenericValue project2 = new MockGenericValue("Project", EasyMap.build("id", 12L, "key", "XYZ", "name", "end alpha"));

    @Test
    public void testVersionSortSimple()
    {
        Version version1 = new MyMockVersion(1, project1);
        Version version2 = new MyMockVersion(2, project1);

        assertTrue(versionComparator.compare(version1, version2) < 0);
        assertTrue(versionComparator.compare(version2, version1) > 0);
        assertTrue(versionComparator.compare(version2, version2) == 0);
        assertTrue(versionComparator.compare(version1, version1) == 0);

        assertTrue(versionComparator.compare(version1, null) < 0);
        assertTrue(versionComparator.compare(version2, null) < 0);
        assertTrue(versionComparator.compare(null, version1) > 0);
        assertTrue(versionComparator.compare(null, version2) > 0);

    }

    @Test
    public void testVersionIfProjectIsDifferent()
    {
        Version version1a = new MyMockVersion(1, project1);
        Version version1b = new MyMockVersion(2, project1);
        Version version2a = new MyMockVersion(1, project2);
        Version version2b = new MyMockVersion(2, project2);

        assertTrue(versionComparator.compare(version1a, version1b) < 0);
        assertTrue(versionComparator.compare(version1a, version2a) < 0);
        assertTrue(versionComparator.compare(version1b, version2a) < 0);
        assertTrue(versionComparator.compare(version2a, version2b) < 0);

        assertTrue(versionComparator.compare(version1b, version1a) > 0);
        assertTrue(versionComparator.compare(version2a, version1a) > 0);
        assertTrue(versionComparator.compare(version2a, version1b) > 0);
        assertTrue(versionComparator.compare(version2b, version2a) > 0);

        assertTrue(versionComparator.compare(version2a, version2a) == 0);
        assertTrue(versionComparator.compare(version1b, version1b) == 0);
    }

    public static class MyMockVersion extends MockVersion
    {
        private final Long sequence;
        private final GenericValue project;
        private final MockProject projectObject;

        public GenericValue getProject() 
        {
            return project;
        }

        @Override
        public Project getProjectObject()
        {
            return projectObject;
        }

        public MyMockVersion(long sequence, GenericValue project)
        {
            this.project = project;
            this.sequence = new Long(sequence);
            this.projectObject = new MockProject(project);
        }

        public String getName()
        {
            return "Stupid version " + sequence;
        }

        public Long getSequence()
        {
            return sequence;
        }
    }
}
