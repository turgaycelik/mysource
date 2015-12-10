package com.atlassian.jira.web.bean;

import com.atlassian.jira.config.properties.ApplicationProperties;

import org.junit.Test;

import static com.atlassian.jira.config.properties.APKeys.JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST;
import static com.atlassian.jira.web.bean.ApplicationPropertiesBackedNonZipExpandableExtensions.DEFAULT_JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Responsible for holding unit tests for {@link ApplicationPropertiesBackedNonZipExpandableExtensions}
 *
 * @since v4.2
 */
public class TestApplicationPropertiesBackedNonZipExpandableExtensions
{
    private static final String PROP_KEY_EXTENSIONS = JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST;

    @Test
    public void testContainsDoesNotAcceptNullExtensions() throws Exception
    {
        ApplicationPropertiesBackedNonZipExpandableExtensions nonZipExpandableExtensions = new ApplicationPropertiesBackedNonZipExpandableExtensions(null);

        try
        {
            nonZipExpandableExtensions.contains(null, DEFAULT_JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST);
            fail("ApplicationPropertiesBackedNonZipExpandableExtensions.contains(String extension) "
                    + "should not accept a null value for the extension.");
        }
        catch (IllegalArgumentException expectedExceptions)
        {
        }
    }

    @Test
    public void testContainsUsesDefaultExtensionsListWhenApplicationPropertyUndefined() throws Exception
    {
        ApplicationProperties mockApplicationProperties = mock(ApplicationProperties.class);
        when(mockApplicationProperties.getDefaultBackedString(PROP_KEY_EXTENSIONS))
                .thenReturn(null);
        ApplicationPropertiesBackedNonZipExpandableExtensions nonZipExpandableExtensions =
                new ApplicationPropertiesBackedNonZipExpandableExtensions(mockApplicationProperties);
        assertTrue(nonZipExpandableExtensions.contains("docx"));
        assertFalse(nonZipExpandableExtensions.contains("jar"));
        verify(mockApplicationProperties, times(2)).getDefaultBackedString(PROP_KEY_EXTENSIONS);
    }

    @Test
    public void testContainsPrefersTheExtensionsSpecifiedInTheJiraApplicationPropertyWhenItIsSpecified()
    {
        ApplicationProperties mockApplicationProperties = mock(ApplicationProperties.class);
        when(mockApplicationProperties.getDefaultBackedString(PROP_KEY_EXTENSIONS))
                .thenReturn("xlsx");
        ApplicationPropertiesBackedNonZipExpandableExtensions nonZipExpandableExtensions =
                new ApplicationPropertiesBackedNonZipExpandableExtensions(mockApplicationProperties);

        assertTrue(nonZipExpandableExtensions.contains("xlsx"));
        assertFalse(nonZipExpandableExtensions.contains("docx"));
    }

    @Test
    public void testContainsHandlesWhiteSpaceAndCaseVariation()
    {
        String extensionList = "xlsx , docx,   pptx";

        ApplicationPropertiesBackedNonZipExpandableExtensions nonZipExpandableExtensions = new ApplicationPropertiesBackedNonZipExpandableExtensions(null);

        assertTrue(nonZipExpandableExtensions.contains("xlsx", extensionList));
        assertTrue(nonZipExpandableExtensions.contains("docx", extensionList));
        assertTrue(nonZipExpandableExtensions.contains("pptx", extensionList));

        assertTrue(nonZipExpandableExtensions.contains("xLSx", extensionList));
        assertTrue(nonZipExpandableExtensions.contains("docX", extensionList));
        assertTrue(nonZipExpandableExtensions.contains("Pptx", extensionList));
    }

    @Test
    public void testContainsIsAbleToUseAnEmptyApplicationPropertyToEnableTurningOffSelectiveZipFileExpansion()
            throws Exception
    {
        ApplicationPropertiesBackedNonZipExpandableExtensions nonZipExpandableExtensions = new ApplicationPropertiesBackedNonZipExpandableExtensions(null);

        assertFalse(nonZipExpandableExtensions.contains("xlsx", ""));
        assertFalse(nonZipExpandableExtensions.contains("docx", ""));
    }
}
