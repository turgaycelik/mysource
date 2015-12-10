/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.AnswerWith;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.RedirectSanitiser;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import webwork.action.Action;
import webwork.action.ActionContext;

import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestCopyFieldLayoutScheme
{
    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private FieldLayoutManager mockFieldLayoutManager;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    @AvailableInContainer
    private RedirectSanitiser redirectSanitiser;

    @Mock
    private I18nHelper i18nHelper;

    @Mock
    private FieldLayoutScheme fieldLayoutScheme;

    private CopyFieldLayoutScheme copyFieldLayoutScheme;

    private final long id = 10123L;
    private final long nonExistentId = id + 100;
    private final String description = "I just made this up.";
    public static final String NO_SUCH_ID_ERROR_MESSAGE = "No such id!";

    @BeforeClass
    public static void sanitizeActionContext()
    {
        ActionContext.setResponse(null);
    }

    @Before
    public void setUp() throws Exception
    {
        copyFieldLayoutScheme = new CopyFieldLayoutScheme(mockFieldLayoutManager);

        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(i18nHelper.getText(anyString())).thenAnswer(AnswerWith.firstParameter());

        when(fieldLayoutScheme.getId()).thenReturn(id);
        when(fieldLayoutScheme.getDescription()).thenReturn(description);

        when(mockFieldLayoutManager.getMutableFieldLayoutScheme(id)).thenReturn(fieldLayoutScheme);
        when(mockFieldLayoutManager.getFieldLayoutSchemes()).thenReturn(singletonList(fieldLayoutScheme));

        when(redirectSanitiser.makeSafeRedirectUrl(anyString())).thenAnswer(AnswerWith.firstParameter());
    }

    @Test
    public void freshlyCreatedInstanceShouldReturnNullId()
    {
        assertNull("CopyFieldLayoutScheme returns non-null id after creation ", copyFieldLayoutScheme.getId());
    }

    @Test
    public void schemeShouldBeFetchedBuIdFromManagerAndNameShouldBeAppended() throws Exception
    {
        final String copiedSchemeName = "Copy of tested scheme";
        when(fieldLayoutScheme.getName()).thenReturn("A name");

        when(i18nHelper.getText("common.words.copyof", fieldLayoutScheme.getName())).thenReturn(copiedSchemeName);

        copyFieldLayoutScheme.setId(id);

        assertEquals(Action.INPUT, copyFieldLayoutScheme.doDefault());
        assertEquals(copiedSchemeName, copyFieldLayoutScheme.getFieldLayoutSchemeName());
        assertEquals(description, copyFieldLayoutScheme.getFieldLayoutSchemeDescription());
    }

    @Test
    public void validateWithoutDataShouldRaiseErrorThatANameIsMissing() throws Exception
    {
        final String result = copyFieldLayoutScheme.execute();
        assertEquals(Action.INPUT, result);
        assertEquals(
                ImmutableMap.of("fieldLayoutSchemeName", "admin.common.errors.validname"),
                copyFieldLayoutScheme.getErrors()
        );
    }

    @Test
    public void settingNameOfCopiedSchemeShouldRaiseErrorsForIdOnDoDefault() throws Exception
    {
        copyFieldLayoutScheme.setFieldLayoutSchemeName("Test scheme");

        assertEquals(Action.INPUT, copyFieldLayoutScheme.doDefault());
        assertEquals(singletonList("admin.errors.id.required"), copyFieldLayoutScheme.getErrorMessages());
    }

    @Test
    public void settingNameOfCopiedSchemeShouldRaiseErrorsForIdExecute() throws Exception
    {
        copyFieldLayoutScheme.setFieldLayoutSchemeName("Test scheme");

        assertEquals(Action.INPUT, copyFieldLayoutScheme.execute());
        assertEquals(singletonList("admin.errors.id.required"), copyFieldLayoutScheme.getErrorMessages());
    }

    @Test
    public void shouldNotExecuteWithClashingNames() throws Exception
    {
        final String clashingSchemeName = "ClashingSchemeName";
        when(fieldLayoutScheme.getName()).thenReturn(clashingSchemeName);

        copyFieldLayoutScheme.setFieldLayoutSchemeName(clashingSchemeName);

        copyFieldLayoutScheme.setId(nonExistentId);

        assertEquals(Action.INPUT, copyFieldLayoutScheme.execute());
        assertEquals(
                ImmutableMap.of("fieldLayoutSchemeName", "admin.errors.fieldlayout.scheme.name.exists"),
                copyFieldLayoutScheme.getErrors()
        );
    }

    @Test
    public void shouldRejectInvalidIdInDoDefault() throws Exception
    {
        when(i18nHelper.getText("admin.errors.fieldlayout.invalid.id", String.format("'%d'", nonExistentId)))
                .thenReturn(NO_SUCH_ID_ERROR_MESSAGE);

        copyFieldLayoutScheme.setFieldLayoutSchemeName("A perfectly reasonable name");
        copyFieldLayoutScheme.setId(nonExistentId);

        assertEquals(Action.INPUT, copyFieldLayoutScheme.doDefault());
        assertEquals(singletonList(NO_SUCH_ID_ERROR_MESSAGE), copyFieldLayoutScheme.getErrorMessages());
    }

    @Test
    public void shouldRejectInvalidIdInExecute() throws Exception
    {
        when(fieldLayoutScheme.getName()).thenReturn("A name");
        when(i18nHelper.getText("admin.errors.fieldlayout.invalid.id", String.format("'%d'", nonExistentId)))
                .thenReturn(NO_SUCH_ID_ERROR_MESSAGE);

        copyFieldLayoutScheme.setFieldLayoutSchemeName("A perfectly reasonable name");
        copyFieldLayoutScheme.setId(nonExistentId);

        assertEquals(Action.INPUT, copyFieldLayoutScheme.execute());
        assertEquals(singletonList(NO_SUCH_ID_ERROR_MESSAGE), copyFieldLayoutScheme.getErrorMessages());
    }

    @Test
    public void existingIdAndUniqueNameSucceedsAndRedirects() throws Exception
    {
        when(fieldLayoutScheme.getName()).thenReturn("Scheme 1");

        copyFieldLayoutScheme.setId(id);
        copyFieldLayoutScheme.setFieldLayoutSchemeName("Scheme 2");

        assertEquals(Action.SUCCESS, copyFieldLayoutScheme.execute());
        verify(redirectSanitiser, atLeastOnce()).makeSafeRedirectUrl("ViewFieldLayoutSchemes.jspa");
        verify(mockFieldLayoutManager).copyFieldLayoutScheme(same(fieldLayoutScheme), anyString(), anyString());
    }
}
