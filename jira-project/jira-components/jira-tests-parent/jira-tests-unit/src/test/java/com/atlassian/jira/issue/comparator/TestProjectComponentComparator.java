package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentComparator;

import org.junit.Test;

import static com.atlassian.jira.bc.project.component.ProjectComponentComparator.COMPONENT_NAME_COMPARATOR;
import static org.junit.Assert.assertEquals;

/**
 * Test for the {@link com.atlassian.jira.bc.project.component.ProjectComponentComparator}
 *
 * @since v4.0
 */
public class TestProjectComponentComparator
{
    @Test
    public void testProjectComponentSortByProjectIDs()
    {
        ProjectComponent component1 = new MockProjectComponent(1l, null, 123l);
        ProjectComponent component2 = new MockProjectComponent(1l, null, 456l);

        final int result = ProjectComponentComparator.INSTANCE.compare(component1, component2);

        assertEquals(-1, result);

        ProjectComponent component3 = new MockProjectComponent(1l, null, 7823l);
        ProjectComponent component4 = new MockProjectComponent(1l, null, 123l);

        final int result2 = ProjectComponentComparator.INSTANCE.compare(component3, component4);

        assertEquals(1, result2);

        ProjectComponent component5 = new MockProjectComponent(1l, null, null);
        ProjectComponent component6 = new MockProjectComponent(1l, null, null);

        final int result3 = ProjectComponentComparator.INSTANCE.compare(component5, component6);

        assertEquals(0, result3);

        ProjectComponent component7 = new MockProjectComponent(1l, null, 123l);

        final int result4 = ProjectComponentComparator.INSTANCE.compare(component5, component7);

        assertEquals(1, result4);

        final int result5 = ProjectComponentComparator.INSTANCE.compare(component7, component5);

        assertEquals(-1, result5);
    }

    @Test
    public void testProjectComponentSortByName()
    {
        ProjectComponent component1 = new MockProjectComponent(1l, null, 123l);
        ProjectComponent component2 = new MockProjectComponent(1l, null, 123l);

        final int result = ProjectComponentComparator.INSTANCE.compare(component1, component2);

        assertEquals(0, result);

        ProjectComponent component3 = new MockProjectComponent(1l, "Component B", 123l);
        ProjectComponent component4 = new MockProjectComponent(1l, null, 123l);

        final int result2 = ProjectComponentComparator.INSTANCE.compare(component3, component4);

        assertEquals(-1, result2);

        final int result3 = ProjectComponentComparator.INSTANCE.compare(component4, component3);

        assertEquals(1, result3);

        ProjectComponent component5 = new MockProjectComponent(1l, "Component B", 123l);

        final int result4 = ProjectComponentComparator.INSTANCE.compare(component5, component3);

        assertEquals(0, result4);

        ProjectComponent component6 = new MockProjectComponent(1l, "Component A", 123l);

        final int result5 = ProjectComponentComparator.INSTANCE.compare(component6, component3);

        assertEquals(-1, result5);

        final int result6 = ProjectComponentComparator.INSTANCE.compare(component3, component6);

        assertEquals(1, result6);
    }

    @Test
    public void testNameComparator()
    {
        assertEquals(-1, COMPONENT_NAME_COMPARATOR.compare("a", "b"));
        assertEquals(-1, COMPONENT_NAME_COMPARATOR.compare("a", "B"));
        assertEquals(-1, COMPONENT_NAME_COMPARATOR.compare("A", "b"));
        assertEquals(-1, COMPONENT_NAME_COMPARATOR.compare("A", "B"));

        assertEquals(0, COMPONENT_NAME_COMPARATOR.compare("a", "a"));
        assertEquals(0, COMPONENT_NAME_COMPARATOR.compare("a", "A"));
        assertEquals(0, COMPONENT_NAME_COMPARATOR.compare("A", "a"));
        assertEquals(0, COMPONENT_NAME_COMPARATOR.compare("A", "A"));

        assertEquals(1, COMPONENT_NAME_COMPARATOR.compare("b", "a"));
        assertEquals(1, COMPONENT_NAME_COMPARATOR.compare("b", "A"));
        assertEquals(1, COMPONENT_NAME_COMPARATOR.compare("B", "a"));
        assertEquals(1, COMPONENT_NAME_COMPARATOR.compare("B", "A"));
    }
}
