package com.atlassian.jira.external.beans;

import java.util.Map;

import com.atlassian.jira.ofbiz.FieldMap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v3.13
 */
public class TestExternalProject
{
    @Test
    public void testToFieldsMap()
    {
        final ExternalProject externalProject = new ExternalProject();

        // ID should be exluded always.  Lead should be excluded by virtue of being blank.
        externalProject.setId("42");
        externalProject.setLead("");

        // These should be included
        externalProject.setAssigneeType("3");
        externalProject.setKey("FOO");
        externalProject.setDescription("  ");

        final Map<String,Object> expectedMap = FieldMap.build(
                "assigneetype", 3L,
                "description", "  ",
                "key", "FOO");
        assertEquals(expectedMap, externalProject.toFieldsMap());
    }
}
