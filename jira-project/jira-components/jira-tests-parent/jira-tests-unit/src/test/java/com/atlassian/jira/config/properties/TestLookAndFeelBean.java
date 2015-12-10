package com.atlassian.jira.config.properties;

import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.web.ui.header.CurrentHeader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class TestLookAndFeelBean
{
    @Mock ApplicationProperties applicationProperties;
    @Mock CurrentHeader currentHeader;

    @Test
    public void testMissingColorsWillDefault()
    {
        LookAndFeelBean lookAndFeelBean = new LookAndFeelBean(applicationProperties)
        {
            @Override
            protected CurrentHeader getCurrentHeader()
            {
                return currentHeader;
            }
        };
        when(currentHeader.get()).thenReturn(CurrentHeader.Header.CLASSIC);

        assertEquals(LookAndFeelBean.DefaultColours.TOP_HIGHLIGHTCOLOUR, lookAndFeelBean.getTopHighlightColor());
        assertEquals(LookAndFeelBean.DefaultColours.TOP_BGCOLOUR, lookAndFeelBean.getTopBackgroundColour());
        assertEquals(LookAndFeelBean.DefaultColours.TOP_SEPARATOR_BGCOLOUR, lookAndFeelBean.getTopSeparatorBackgroundColor());
        assertEquals(LookAndFeelBean.DefaultColours.TOP_SEPARATOR_BGCOLOUR, lookAndFeelBean.getTopSeparatorBackgroundColor());
        assertEquals(LookAndFeelBean.DefaultColours.TOP_TEXTCOLOUR, lookAndFeelBean.getTopTxtColour());
        assertEquals(LookAndFeelBean.DefaultColours.TOP_TEXTHIGHLIGHTCOLOUR, lookAndFeelBean.getTopTextHighlightColor());

        assertEquals(LookAndFeelBean.DefaultColours.MENU_BGCOLOUR, lookAndFeelBean.getMenuBackgroundColour());
        assertEquals(LookAndFeelBean.DefaultColours.MENU_TEXTCOLOUR, lookAndFeelBean.getMenuTxtColour());

        assertEquals(LookAndFeelBean.DefaultColours.MENU_SEPARATOR, lookAndFeelBean.getMenuSeparatorColour());
        assertEquals(LookAndFeelBean.DefaultColours.MENU_BGCOLOUR, lookAndFeelBean.getMenuBackgroundColour());
        assertEquals(LookAndFeelBean.DefaultColours.MENU_TEXTCOLOUR, lookAndFeelBean.getMenuTxtColour());

        assertEquals(LookAndFeelBean.DefaultColours.TEXT_ACTIVELINKCOLOR, lookAndFeelBean.getTextActiveLinkColour());
        assertEquals(LookAndFeelBean.DefaultColours.TEXT_LINKCOLOR, lookAndFeelBean.getTextLinkColour());
        assertEquals(LookAndFeelBean.DefaultColours.TEXT_HEADINGCOLOR, lookAndFeelBean.getTextHeadingColour());

        when(currentHeader.get()).thenReturn(CurrentHeader.Header.COMMON);

        assertEquals(LookAndFeelBean.DefaultCommonColours.TOP_HIGHLIGHTCOLOUR, lookAndFeelBean.getTopHighlightColor());
        assertEquals(LookAndFeelBean.DefaultCommonColours.TOP_BGCOLOUR, lookAndFeelBean.getTopBackgroundColour());
        assertEquals(LookAndFeelBean.DefaultCommonColours.TOP_SEPARATOR_BGCOLOUR, lookAndFeelBean.getTopSeparatorBackgroundColor());
        assertEquals(LookAndFeelBean.DefaultColours.TOP_TEXTCOLOUR, lookAndFeelBean.getTopTxtColour());
        assertEquals(LookAndFeelBean.DefaultColours.TOP_TEXTHIGHLIGHTCOLOUR, lookAndFeelBean.getTopTextHighlightColor());

        assertEquals(LookAndFeelBean.DefaultColours.MENU_BGCOLOUR, lookAndFeelBean.getMenuBackgroundColour());
        assertEquals(LookAndFeelBean.DefaultColours.MENU_TEXTCOLOUR, lookAndFeelBean.getMenuTxtColour());

        assertEquals(LookAndFeelBean.DefaultColours.MENU_SEPARATOR, lookAndFeelBean.getMenuSeparatorColour());
        assertEquals(LookAndFeelBean.DefaultColours.MENU_BGCOLOUR, lookAndFeelBean.getMenuBackgroundColour());
        assertEquals(LookAndFeelBean.DefaultColours.MENU_TEXTCOLOUR, lookAndFeelBean.getMenuTxtColour());

        assertEquals(LookAndFeelBean.DefaultColours.TEXT_ACTIVELINKCOLOR, lookAndFeelBean.getTextActiveLinkColour());
        assertEquals(LookAndFeelBean.DefaultColours.TEXT_LINKCOLOR, lookAndFeelBean.getTextLinkColour());
        assertEquals(LookAndFeelBean.DefaultColours.TEXT_HEADINGCOLOR, lookAndFeelBean.getTextHeadingColour());

    }
}
