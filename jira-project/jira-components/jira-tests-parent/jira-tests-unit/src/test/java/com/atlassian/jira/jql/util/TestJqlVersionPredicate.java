package com.atlassian.jira.jql.util;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.Predicate;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestJqlVersionPredicate extends MockControllerTestCase
{
    @Test
    public void testCreatePredicate() throws Exception
    {
        MockVersion version = createMockVersion(1L, 100L, 1L);
        MockVersion versionDiffProject = createMockVersion(2L, 110L, 1L);
        MockVersion versionLessThan = createMockVersion(3L, 100L, 0L);
        MockVersion versionGreaterThan = createMockVersion(4L, 100L, 2L);
        MockVersion versionGreaterThanDiffProject = createMockVersion(5L, 110L, 2L);
        MockVersion versionLessThanDiffProject = createMockVersion(6L, 110L, 0L);

        mockController.replay();

        Predicate<Version> predicateGreaterThan = new JqlVersionPredicate(Operator.GREATER_THAN_EQUALS, version);
        assertTrue(predicateGreaterThan.evaluate(version));
        assertFalse(predicateGreaterThan.evaluate(versionDiffProject));
        assertFalse(predicateGreaterThan.evaluate(versionLessThan));
        assertTrue(predicateGreaterThan.evaluate(versionGreaterThan));
        assertFalse(predicateGreaterThan.evaluate(versionGreaterThanDiffProject));
        assertFalse(predicateGreaterThan.evaluate(versionLessThanDiffProject));

        Predicate<Version> predicateLessThan = new JqlVersionPredicate(Operator.LESS_THAN, version);
        assertFalse(predicateLessThan.evaluate(version));
        assertFalse(predicateLessThan.evaluate(versionDiffProject));
        assertTrue(predicateLessThan.evaluate(versionLessThan));
        assertFalse(predicateLessThan.evaluate(versionGreaterThan));
        assertFalse(predicateLessThan.evaluate(versionGreaterThanDiffProject));
        assertFalse(predicateLessThan.evaluate(versionLessThanDiffProject));
    }

    private MockVersion createMockVersion(final Long id, final Long projectId, final Long sequence)
    {
        return new MockVersion(id, "name")
        {
            public Project getProjectObject()
            {
                return new MockProject(projectId, "key", "projName");
            }

            @Override
            public Long getSequence()
            {
                return sequence;
            }
        };
    }
}
