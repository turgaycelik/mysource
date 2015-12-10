package com.atlassian.jira.issue.fields.renderer.wiki.embedded;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.components.block.LineWalker;
import com.atlassian.renderer.v2.components.table.TableBlockRenderer;
import com.atlassian.renderer.v2.components.table.TableRow;
import com.opensymphony.util.TextUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Overrides the TableBlockRenderer to undo "RNDR-74 add wrapper divs for tables and images."
 * The point is to keep the HTML rendering of tables stable within JIRA 5.x even though
 * we are updating RNDR to 7.1.
 *
 * TODO IN JIRA 6.0 - restore the default TableBlockRenderer JRADEV-13037
 *
 * @since v5.2
 */
public class JiraTableBlockRenderer extends TableBlockRenderer
{
    // Everything here is copied as-is from TableBlockRenderer.  We use our own
    // version of the Table class to implement the override, but the superclass
    // doesn't make that easy for us to do...

    private static final Pattern START_TABLE_LINE_PATTERN = Pattern.compile("\\s*\\|.*");
    private static final Pattern END_TABLE_LINE_PATTERN = Pattern.compile(".*\\|\\s*");

    @Override
    public String renderNextBlock(String thisLine, LineWalker nextLines, RenderContext context, SubRenderer subRenderer)
    {
        if (!context.getRenderMode().renderTables())
        {
            return null;
        }

        String line = thisLine;

        Matcher matcher = START_TABLE_LINE_PATTERN.matcher(line);

        if (!matcher.matches())
        {
            return null;
        }

        Table table = new Table();
        ArrayList potentialLines = new ArrayList();

        if (END_TABLE_LINE_PATTERN.matcher(line).matches())
        {
            table.addRow(prerenderLinks(subRenderer, line, context));
        }
        else
        {
            potentialLines.add(prerenderLinks(subRenderer, line, context));
        }

        while (nextLines.hasNext())
        {
            line = nextLines.next();
            if (RenderUtils.isBlank(line) || (potentialLines.isEmpty() && !START_TABLE_LINE_PATTERN.matcher(line).matches()))
            {
                nextLines.pushBack(line);
                break;
            }

            // If this line starts with a pipe, it's not a continuation of the previous line.
            // I'd rather it worked differently, but we have to do this for backwards
            // compatibility with the old renderer. --cm
            if (START_TABLE_LINE_PATTERN.matcher(line).matches() && !potentialLines.isEmpty())
            {
                addNextRow(table, potentialLines);
            }

            potentialLines.add(prerenderLinks(subRenderer, line, context));

            if (END_TABLE_LINE_PATTERN.matcher(line).matches())
            {
                addNextRow(table, potentialLines);
            }
        }

        if (!potentialLines.isEmpty())
        {
            table.addRow(TextUtils.join("\n", potentialLines));
        }

        StringBuffer buffer = new StringBuffer();
        table.render(subRenderer, context, buffer);
        return buffer.toString();
    }

    // Since the pipe-signs in the link, macro and image syntax might interfere with table cells, we pre-render them.
    private String prerenderLinks(SubRenderer subRenderer, String line, RenderContext context)
    {
        return subRenderer.render(line, context, context.getRenderMode().and(RenderMode.allow(RenderMode.F_LINKS | RenderMode.F_MACROS | RenderMode.F_IMAGES | RenderMode.F_TEMPLATE)));
    }

    private void addNextRow(Table table, ArrayList potentialLines)
    {
        table.addRow(TextUtils.join("\n", potentialLines));
        potentialLines.clear();
    }

    static class Table extends com.atlassian.renderer.v2.components.table.Table
    {
        private final List<TableRow> rows = new LinkedList<TableRow>();

        @Override
        public void addRow(TableRow row)
        {
            rows.add(row);
            super.addRow(row);
        }

        @Override
        public void render(SubRenderer subRenderer, RenderContext context, StringBuffer buff)
        {
            buff.append("<table class='confluenceTable'><tbody>\n");
            for (TableRow tableRow : rows)
            {
                tableRow.render(subRenderer, context, buff);
            }
            buff.append("</tbody></table>\n");
        }
    }
}

