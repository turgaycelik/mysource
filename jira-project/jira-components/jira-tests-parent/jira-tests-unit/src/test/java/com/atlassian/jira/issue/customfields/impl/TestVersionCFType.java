package com.atlassian.jira.issue.customfields.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigImpl;
import com.atlassian.jira.issue.fields.util.VersionHelperBean;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.project.version.MockVersionManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestVersionCFType
{
    @Test
    public void testValidate()
    {
        MockVersionManager mockVersionManager = new MockVersionManager();
        mockVersionManager.add(new MockVersion(1, "1.0"));
        mockVersionManager.add(new MockVersion(2, "2.0"));

        ErrorCollection errors = checkValidateFromParams(mockVersionManager, null);
        assertFalse(errors.hasAnyErrors());

        errors = checkValidateFromParams(mockVersionManager, Collections.<String>emptyList());
        assertFalse(errors.hasAnyErrors());

        errors = checkValidateFromParams(mockVersionManager, Arrays.asList(new String[]{"1", "2"}));
        assertFalse(errors.hasAnyErrors());

        errors = checkValidateFromParams(mockVersionManager, Arrays.asList(new String[]{"1", "blah blah blah"}));
        assertEquals(1, errors.getErrors().size());
        assertEquals("Version with id 'blah blah blah' does not exist.", errors.getErrors().get("customfield_1"));
    }

    private ErrorCollection checkValidateFromParams(MockVersionManager mockVersionManager, Collection<String> params)
    {
        CustomFieldParams customFieldParams = new CustomFieldParamsImpl();
        if (null != params)
        {
            customFieldParams.put(null, params);
        }
        ErrorCollection errors = new SimpleErrorCollection();

        FieldConfig fieldConfig = new FieldConfigImpl(1L, "Some Versions", null, null, "customfield_1");

        newVersionCFType(mockVersionManager).validateFromParams(customFieldParams, errors, fieldConfig);
        return errors;
    }

    private VersionCFType newVersionCFType(VersionManager versionManager)
    {
        MockAuthenticationContext mockAuthenticationContext = new MockAuthenticationContext(null);
        VersionHelperBean versionHelperBean = new VersionHelperBean(versionManager, null);
        return new VersionCFType(null, mockAuthenticationContext, null, null, null, versionHelperBean, null);
    }
}
