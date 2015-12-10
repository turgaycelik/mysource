package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.imports.project.customfield.GroupCustomFieldImporter;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestMultiGroupCFType
{
    @Test
    public void testGetProjectImporter()
    {
        MultiGroupCFType multiGroupCFType = new MultiGroupCFType(null, null, null, null, null, null, null, null);
        assertTrue(multiGroupCFType.getProjectImporter() instanceof GroupCustomFieldImporter);
    }
}
