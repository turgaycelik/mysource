package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.imports.project.customfield.SelectCustomFieldImporter;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestSelectCFType
{
    @Test
    public void testGetProjectImporter() throws Exception
    {
        SelectCFType selectCFType = new SelectCFType(null, null, null, null);
        assertTrue(selectCFType.getProjectImporter() instanceof SelectCustomFieldImporter);
    }
}
