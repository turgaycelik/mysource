package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestTextAreaCFType
{
    @Test
    public void testGetProjectImporter() throws Exception
    {
        TextAreaCFType textAreaCFType = new TextAreaCFType(null, null);
        assertTrue(textAreaCFType.getProjectImporter() instanceof NoTransformationCustomFieldImporter);
    }

}
