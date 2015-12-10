package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestImportIdLinkCFType
{
    @Test
    public void testGetProjectImporter() throws Exception
    {
        ImportIdLinkCFType importIdLinkCFType = new ImportIdLinkCFType(null, null, null, null);
        assertTrue(importIdLinkCFType.getProjectImporter() instanceof NoTransformationCustomFieldImporter);
    }

}
