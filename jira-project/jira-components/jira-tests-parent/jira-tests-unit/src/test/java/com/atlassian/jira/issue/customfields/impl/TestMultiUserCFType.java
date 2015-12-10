package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.imports.project.customfield.UserCustomFieldImporter;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestMultiUserCFType
{
    @Test
    public void test()
    {
        MultiUserCFType multiUserCFType = new MultiUserCFType(null, null, null, null, null, null, null, null, null);
        assertTrue(multiUserCFType.getProjectImporter() instanceof UserCustomFieldImporter);
    }
}
