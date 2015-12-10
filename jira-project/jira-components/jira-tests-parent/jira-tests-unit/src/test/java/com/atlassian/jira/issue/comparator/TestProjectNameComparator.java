package com.atlassian.jira.issue.comparator;

import java.util.Comparator;

import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;

import org.junit.Test;

public class TestProjectNameComparator extends AbstractComparatorTestCase
{

    @Test
    public void testCompareProjects()
    {
        final Comparator projectNameComparator = ProjectNameComparator.COMPARATOR;
        final Project projectA = new MockProject(new Long(3), "PRA", "Project A");
        final Project projectB = new MockProject(new Long(2), "PRB", "Project B");
        final Project projectB1 = new MockProject(new Long(2), "PRB", "Project B");
        final Project projectNullName = new MockProject(new Long(1), "PRN", null);

        //test null cases
        assertEqualTo(projectNameComparator, null, null);
        assertLessThan(projectNameComparator, null, projectA);
        assertGreaterThan(projectNameComparator, projectA, null);

        //test standard cases
        assertEqualTo(projectNameComparator, projectA, projectA);
        assertEqualTo(projectNameComparator, projectB, projectB);
        assertEqualTo(projectNameComparator, projectB, projectB1);
        assertLessThan(projectNameComparator, projectA, projectB);
        assertGreaterThan(projectNameComparator, projectB, projectA);

        //test null name cases
        assertEqualTo(projectNameComparator, projectNullName, projectNullName);
        assertLessThan(projectNameComparator, projectNullName, projectA);
        assertGreaterThan(projectNameComparator, projectA, projectNullName);
    }
}
