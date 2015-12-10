package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestRenderableTextCFType
{
    @Test
    public void testGetProjectImporter() throws Exception
    {
        RenderableTextCFType renderableTextCFType = new RenderableTextCFType(null, null);
        assertTrue(renderableTextCFType.getProjectImporter() instanceof NoTransformationCustomFieldImporter);
    }

}
