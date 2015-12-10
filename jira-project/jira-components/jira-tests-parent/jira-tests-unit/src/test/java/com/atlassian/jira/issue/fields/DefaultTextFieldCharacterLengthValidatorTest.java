package com.atlassian.jira.issue.fields;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class DefaultTextFieldCharacterLengthValidatorTest
{
    private TextFieldLimitProvider textFieldLimitProvider = Mockito.mock(TextFieldLimitProvider.class);

    private TextFieldCharacterLengthValidator validator;

    @Before
    public void setUp()
    {
        validator = new DefaultTextFieldCharacterLengthValidator(textFieldLimitProvider);
    }

    @Test
    public void testIsTextTooLongForNullString() throws Exception
    {
        expectMockTextFieldLimitProviderReturnsLimit(10L);

        assertFalse(validator.isTextTooLong(null));
    }

    @Test
    public void testIsTextTooLongForEmptyString() throws Exception
    {
        expectMockTextFieldLimitProviderReturnsLimit(10L);

        assertFalse(validator.isTextTooLong(""));
    }

    @Test
    public void testIsTextTooLongForShortText() throws Exception
    {
        expectMockTextFieldLimitProviderReturnsLimit(20L);

        assertFalse(validator.isTextTooLong("some short text"));
    }

    @Test
    public void testIsTextTooLongForTextLengthAtLimit() throws Exception
    {
        final String text = "this text must not be longer";
        expectMockTextFieldLimitProviderReturnsLimit(text.length());

        assertFalse(validator.isTextTooLong(text));
    }

    @Test
    public void testIsTextTooLongForTextExceedingLengthByOne() throws Exception
    {
        final String text = "this text must not be longer";
        expectMockTextFieldLimitProviderReturnsLimit(text.length() - 1);

        assertTrue(validator.isTextTooLong(text));
    }

    @Test
    public void testIsTextTooLongForFarTooLongText() throws Exception
    {
        expectMockTextFieldLimitProviderReturnsLimit(5L);

        assertTrue(validator.isTextTooLong("this text is really too long"));
    }

    private void expectMockTextFieldLimitProviderReturnsLimit(final long numberOfCharacters)
    {
        when(textFieldLimitProvider.getTextFieldLimit()).thenReturn(numberOfCharacters);
    }
}
