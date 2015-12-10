package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestReadOnlyCFType
{
    @Test
    public void testGetProjectImporter() throws Exception
    {
        ReadOnlyCFType readOnlyCFType = new ReadOnlyCFType(null, null);
        assertTrue(readOnlyCFType.getProjectImporter() instanceof NoTransformationCustomFieldImporter);
    }

}
