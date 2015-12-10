package com.atlassian.jira.issue.status.category;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test Case for StatusCategoryImpl
 *
 * @since v6.1
 */
public class TestStatusCategoryImpl
{
    @Test
    public void checkDefaultCategory()
    {
        StatusCategory statusCategory = StatusCategoryImpl.getDefault();

        assertEquals("id of status category should match", 1L, (long)statusCategory.getId());
        assertEquals(StatusCategory.UNDEFINED, statusCategory.getKey());
        assertEquals("medium-gray", statusCategory.getColorName());
    }

}
