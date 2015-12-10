package com.atlassian.jira.project;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.validation.Validator;

import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.4
 */
public class TestProjectKeyRegexValidator
{
    private I18nHelper i18nHelper;
    private ProjectManager projectManager;
    private Project mockProject;

    @Before
    public void setUp()
    {
        this.i18nHelper = new NoopI18nHelper();
        this.projectManager = mock(ProjectManager.class);
        this.mockProject = new MockProject(3838L, "DEF");
    }

    @After
    public void tearDown()
    {
        this.i18nHelper = null;
        this.projectManager = null;
    }

    @Test
    public void testInvalidRegex()
    {
        when(projectManager.getProjectObjects()).thenReturn(Lists.newArrayList(mockProject));

        final ProjectKeyRegexValidator projectKeyRegexValidator = getValidatorUnderTest();
        Validator.Result invalidResult = projectKeyRegexValidator.validate("([A-Z]");

        assertFalse(invalidResult.isValid());
        assertEquals(i18nHelper.getText("admin.advancedconfiguration.projectkey.regex.error.invalid", "Unclosed group near index 6\n"
                + "([A-Z]\n"
                + "      ^"),
                invalidResult.getErrorMessage());
    }

    @Test
    public void testBannedCharacters()
    {
        when(projectManager.getProjectObjects()).thenReturn(Collections.<Project>emptyList());

        final ProjectKeyRegexValidator projectKeyRegexValidator = getValidatorUnderTest();

        List<String> bannedChars = ProjectKeyRegexValidator.BANNED_CHARS;
        for (final String bannedChar : bannedChars)
        {
            String bannedCharPattern;
            // Special case
            if(bannedChar.equals("."))
            {
                bannedCharPattern = "\\.";
            }
            else
            {
                bannedCharPattern = bannedChar;
            }
            Validator.Result invalidResult = projectKeyRegexValidator.validate(bannedCharPattern + ".*");

            assertFalse(invalidResult.isValid());
            assertEquals(i18nHelper.getText("admin.advancedconfiguration.projectkey.regex.error.banned.character", bannedChar),
                    invalidResult.getErrorMessage());

            invalidResult = projectKeyRegexValidator.validate("A" + bannedCharPattern + ".*");

            assertFalse(invalidResult.isValid());
            assertEquals(i18nHelper.getText("admin.advancedconfiguration.projectkey.regex.error.banned.character", bannedChar),
                    invalidResult.getErrorMessage());

            invalidResult = projectKeyRegexValidator.validate("AB" + bannedCharPattern);

            assertFalse(invalidResult.isValid());
            assertEquals(i18nHelper.getText("admin.advancedconfiguration.projectkey.regex.error.banned.character", bannedChar),
                    invalidResult.getErrorMessage());

        }

    }

    @Test
    public void testExistingProjectRegex()
    {
        when(projectManager.getProjectObjects()).thenReturn(Lists.newArrayList(mockProject));

        final ProjectKeyRegexValidator projectKeyRegexValidator = getValidatorUnderTest();
        Validator.Result invalidResult = projectKeyRegexValidator.validate("ABC");

        assertFalse(invalidResult.isValid());
        assertEquals(i18nHelper.getText("admin.advancedconfiguration.projectkey.regex.error.existingproject"), invalidResult.getErrorMessage());

        Validator.Result validResult = projectKeyRegexValidator.validate("DEF");

        assertTrue(validResult.isValid());
    }

    @Test
    public void testNoProjectRegex()
    {
        when(projectManager.getProjectObjects()).thenReturn(Collections.<Project>emptyList());

        final ProjectKeyRegexValidator projectKeyRegexValidator = getValidatorUnderTest();
        Validator.Result invalidResult = projectKeyRegexValidator.validate("ABC");

        assertTrue(invalidResult.isValid());
    }

    @Test
    public void testNonAscii()
    {
        when(projectManager.getProjectObjects()).thenReturn(Collections.<Project>emptyList());

        final ProjectKeyRegexValidator projectKeyRegexValidator = getValidatorUnderTest();
        Validator.Result invalidResult = projectKeyRegexValidator.validate("\u00F6");

        assertFalse(invalidResult.isValid());
        assertEquals(i18nHelper.getText("admin.advancedconfiguration.projectkey.regex.error.nonascii"),
                invalidResult.getErrorMessage());

    }

    public ProjectKeyRegexValidator getValidatorUnderTest()
    {

        return new ProjectKeyRegexValidator()
        {
            @Override
            ProjectManager getProjectManager()
            {
                return projectManager;
            }

            @Override
            I18nHelper getI18nHelper()
            {
                return i18nHelper;
            }
        };
    }
}
