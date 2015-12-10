package com.atlassian.jira.web.action.util;

import com.atlassian.diff.CharacterChunk;
import com.atlassian.diff.DiffChunk;
import com.atlassian.diff.DiffType;
import com.atlassian.diff.DiffViewBean;
import com.atlassian.diff.WordChunk;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A utility class to render a view of a diff between two strings.
 *
 * @since v4.0
 */
public class DiffViewRenderer
{
    private static final Logger log = Logger.getLogger(DiffViewRenderer.class);

    public DiffViewRenderer()
    {
    }

    public String getUnifiedHtml(final DiffViewBean wordLevelDiff)
    {
        return getUnifiedHtml(wordLevelDiff, null, null);
    }

    public String getUnifiedHtml(final DiffViewBean wordLevelDiff, String removedStyle, String addedStyle)
    {
        notNull("wordLevelDiff", wordLevelDiff);
        return renderHtml(wordLevelDiff.getUnifiedChunks(), removedStyle, addedStyle);
    }

    public String getOriginalHtml(final DiffViewBean wordLevelDiff)
    {
        return getOriginalHtml(wordLevelDiff, null, null);
    }
    public String getOriginalHtml(final DiffViewBean wordLevelDiff, String removedStyle, String addedStyle)
    {
        notNull("wordLevelDiff", wordLevelDiff);
        return renderHtml(wordLevelDiff.getOriginalChunks(), removedStyle, addedStyle);
    }

    public String getRevisedHtml(final DiffViewBean wordLevelDiff)
    {
        return getRevisedHtml(wordLevelDiff, null, null);
    }

    public String getRevisedHtml(final DiffViewBean wordLevelDiff, String removedStyle, String addedStyle)
    {
        notNull("wordLevelDiff", wordLevelDiff);
        return renderHtml(wordLevelDiff.getRevisedChunks(), removedStyle, addedStyle);
    }

    private String renderHtml(List<DiffChunk> revisedChunks, String removedStyle, String addedStyle)
    {
        // this is not implemented in velocity because we need tight control over the whitespace in the output
        StringBuilder html = new StringBuilder();
        if (StringUtils.isEmpty(removedStyle)) {
            removedStyle = "background-color: #FFE0E0; font-weight: bold;";
        }
        if (StringUtils.isEmpty(addedStyle)) {
            addedStyle = "background-color: #E0FFE0; font-weight: bold;";
        }
        for (DiffChunk chunk : revisedChunks)
        {
            if (chunk.getType() == DiffType.CHANGED_WORDS)
            {
                WordChunk wordChunk = (WordChunk) chunk;
                for (CharacterChunk charChunk : wordChunk.getCharacterChunks())
                {
                    html.append("<span class=\"").append(charChunk.getType().getClassName()).append("\"");

                    if (charChunk.getType() == DiffType.DELETED_CHARACTERS)
                    {
                        html.append(" style=\"").append(removedStyle).append("\"");
                    }
                    else if (charChunk.getType() == DiffType.ADDED_CHARACTERS)
                    {
                        html.append(" style=\"").append(addedStyle).append("\"");
                    }
                    html.append(">");
                    html.append(print(charChunk.getText()));
                    html.append("</span>");
                }
            }
            else if (chunk.getType().getClassName().equals("unchanged")) // probably dead code, but copied from old line-diff.vm
            {
                html.append(print(chunk.getText()));
            }
            else
            {
                html.append("<span class=\"").append(chunk.getType().getClassName()).append("\"");

                if (chunk.getType() == DiffType.DELETED_WORDS)
                {
                    html.append(" style=\"").append(removedStyle).append("\"");
                }
                else if (chunk.getType() == DiffType.ADDED_WORDS)
                {
                    html.append(" style=\"").append(addedStyle).append("\"");
                }
                html.append(">");
                html.append(print(chunk.getText()));
                html.append("</span>");
            }
            html.append("\n"); // ensure visual spacing
        }
        return html.toString();
    }

    private static String print(String s)
    {
        s = TextUtils.htmlEncode(s, false);
        s = s.replace(" ", "&nbsp;").replaceAll("(\\r\\n|\\n|\\r)", "<br>");
        return s;
    }
}