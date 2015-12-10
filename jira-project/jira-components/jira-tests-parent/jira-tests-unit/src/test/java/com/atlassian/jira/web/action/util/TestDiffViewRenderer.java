package com.atlassian.jira.web.action.util;

import com.atlassian.diff.DiffViewBean;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class TestDiffViewRenderer
{
    public static final String ADDED_STYLE = "added";
    public static final String REMOVED_STYLE = "removed";

    ExpectedHtmlMatcherBuilder builder;

    @Before
    public void setUp()
    {
        builder = new ExpectedHtmlMatcherBuilder();
    }

    @Test
    public void simpleAddition()
    {
        final String unifiedHtml = getUnifiedDiff("hello", "hello world");

        builder.unchanged("hello");
        builder.added("&nbsp;world");
        assertThat(unifiedHtml, builder.build());
    }

    @Test
    public void simpleReplacement()
    {
        final String unifiedHtml = getUnifiedDiff("word", "replaced");

        builder.removed("word");
        builder.added("replaced");

        assertThat(unifiedHtml, builder.build());
    }

    @Test
    public void multilineCommentEdited()
    {
        final String unifiedHtml = getUnifiedDiff("line1\n\nline3", "line1\r\n\r\nline3 (edited)");

        builder.unchanged("line1<br><br>line3");
        builder.added("&nbsp;(edited)");

        assertThat(unifiedHtml, builder.build());
    }

    @Test
    public void multilineCommentWithSpaceEdited()
    {
        final String unifiedHtml = getUnifiedDiff("line1\n \nline3", "line1\r\n  \r\nline3 (edited)");

        builder.unchanged("line1<br>");
        builder.removed("&nbsp;<br>");
        builder.added("&nbsp;&nbsp;<br>");   // added '\r\n' is also represented as nbps;br
        builder.unchanged("line3");
        builder.added("&nbsp;(edited)");

        assertThat(unifiedHtml, builder.build());
    }

    @Test
    public void multilineCommentWithSpaceReplaced()
    {
        final String unifiedHtml = getUnifiedDiff("line1\n \nline3", "line1\r\nline2\r\nline3 (edited)");

        builder.unchanged("line1<br>");
        builder.removed("&nbsp;<br>");
        builder.added("line2<br>");
        builder.unchanged("line3");
        builder.added("&nbsp;(edited)");

        assertThat(unifiedHtml, builder.build());
    }

    @Test
    public void multilineCommentWithManyNewlinesShortened()
    {
        final String unifiedHtml = getUnifiedDiff("A\n\n\n\n\n\n\n\n\n\nZ", "A\r\n\r\n\r\nK\r\n\r\n\r\nZ");

        builder.unchanged("A<br><br><br>");
        builder.added("K");
        builder.unchanged("<br><br><br>");
        builder.removed("<br><br><br><br>"); // removed '\n' is represented as nbps;br
        builder.unchanged("Z");

        assertThat(unifiedHtml, builder.build());
    }

    @Test
    public void multilineCommentEditedAndOneNewlineRemoved()
    {
        final String unifiedHtml = getUnifiedDiff("ABC\n\nXYZ", "ABCDEF\r\nXYZ");

        builder.removed("ABC");
        builder.added("ABCDEF");
        builder.unchanged("<br>");
        builder.removed("<br>");
        builder.unchanged("XYZ");

        assertThat(unifiedHtml, builder.build());
    }

    @Test
    public void multilineCommentTextAddedAndOneNewlineRemoved()
    {
        final String unifiedHtml = getUnifiedDiff("ABC\n\nXYZ", "ABC DEF\r\nXYZ");

        builder.unchanged("ABC");
        builder.added("&nbsp;DEF");
        builder.unchanged("<br>");
        builder.removed("<br>");
        builder.unchanged("XYZ");

        assertThat(unifiedHtml, builder.build());
    }

    @Test
    public void multilineCommenNewlineAdded()
    {
        final String unifiedHtml = getUnifiedDiff("start\n\nend", "start\r\n\r\n\r\nend");

        builder.unchanged("start<br><br>");
        builder.added("<br>");
        builder.unchanged("end");

        assertThat(unifiedHtml, builder.build());
    }

    @Test
    public void editNewlineDifferencesIgnored()
    {
        final String unifiedHtml = getUnifiedDiff("And now for something\ncompletely different",
                "And now for something\r\ncompletely different: the larch");

        builder.unchanged("And&nbsp;now&nbsp;for&nbsp;something<br>completely&nbsp;different");
        builder.added(":&nbsp;the&nbsp;larch");

        assertThat(unifiedHtml, builder.build());
    }

    @Test
    public void editNewlineDifferencesIgnoredAdditionalSpaceInBetweenIsPickedUp()
    {
        final String unifiedHtml = getUnifiedDiff("And now for something\n\ncompletely different",
                "And now for something\r\n \r\ncompletely different: the larch");

        builder.unchanged("And&nbsp;now&nbsp;for&nbsp;something<br>");
        builder.removed("<br>");
        builder.added("&nbsp;<br>");
        builder.unchanged("completely&nbsp;different");
        builder.added(":&nbsp;the&nbsp;larch");

        assertThat(unifiedHtml, builder.build());
    }

    @Test
    public void edit2ndTimeAdditionalSpaceInBetweenIsPickedUp()
    {
        final String unifiedHtml = getUnifiedDiff("And now for something\r\n \r\ncompletely different",
                "And now for something\r\n  \r\ncompletely different: the larch");

        builder.unchanged("And&nbsp;now&nbsp;for&nbsp;something<br>");
        builder.removed("&nbsp;<br>");
        builder.added("&nbsp;&nbsp;<br>");
        builder.unchanged("completely&nbsp;different");
        builder.added(":&nbsp;the&nbsp;larch");

        assertThat(unifiedHtml, builder.build());
    }

    private String getUnifiedDiff(final String originalLine, final String revisedLine)
    {
        final DiffViewBean diff = DiffViewBean.createWordLevelDiff(originalLine, revisedLine);
        return (new DiffViewRenderer()).getUnifiedHtml(diff, REMOVED_STYLE, ADDED_STYLE);
    }

    static class ExpectedHtmlMatcherBuilder
    {
        public static final String DIFFREMOVEDCHARS = "diffremovedchars";
        public static final String DIFFADDEDCHARS = "diffaddedchars";
        final String FORMAT_CHANGED = "<span class=\"%s\" style=\"%s\">%s</span>\n";
        final String FORMAT_UNCHANGED = "<span class=\"diffcontext\">%s</span>\n";
        StringBuilder sb = new StringBuilder();

        ExpectedHtmlMatcherBuilder()
        {
        }

        ExpectedHtmlMatcherBuilder unchanged(String content)
        {
            sb.append(String.format(FORMAT_UNCHANGED, content));
            return this;
        }

        ExpectedHtmlMatcherBuilder removed(String content)
        {
            sb.append(String.format(FORMAT_CHANGED, DIFFREMOVEDCHARS, REMOVED_STYLE, content));
            return this;
        }

        ExpectedHtmlMatcherBuilder added(String content)
        {
            sb.append(String.format(FORMAT_CHANGED, DIFFADDEDCHARS, ADDED_STYLE, content));
            return this;
        }

        Matcher<String> build()
        {
            final String str = sb.toString();
            return new BaseMatcher<String>()
            {
                @Override
                public boolean matches(final Object item)
                {
                    return str.equals(item);
                }

                @Override
                public void describeTo(final Description description)
                {
                    description.appendText(str);
                }
            };
        }
    }

}
