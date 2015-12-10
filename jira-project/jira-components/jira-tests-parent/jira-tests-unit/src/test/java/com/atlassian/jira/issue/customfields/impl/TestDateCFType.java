package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.DateFieldFormat;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestDateCFType extends MockControllerTestCase
{
    @Mock
    DateFieldFormat dateFieldFormat;

    @Test
    public void testGetProjectImporter() throws Exception
    {
        DateCFType dateCFType = instantiate(DateCFType.class);
        assertTrue(dateCFType.getProjectImporter() instanceof NoTransformationCustomFieldImporter);
    }
}
