package com.atlassian.jira.functest.framework.parser.filter;

import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Parse a list of filters from filter-list.jsp
 *
 * @since v3.13
 */
public class FilterList
{
    private static final String GLOBAL_PERM_STRING = "Shared with all users";
    private static final Pattern GROUP_SHARE_REGEX = Pattern.compile("Group\\:\\s+(\\S*)");
    private static final Pattern ROLE_SHARE_REGEX = Pattern.compile("Project\\:\\s+(\\S*)\\s+Role\\:\\s+(\\S*)");
    private static final Pattern PROJECT_SHARE_REGEX = Pattern.compile("Project\\:\\s+(\\S*)");

    private String filterListName = null;
    private List<String> columns = null;
    private List<FilterItem> filterItems = null;

    public FilterList(TableLocator tableLocator)
    {
        columns = newArrayList();
        WebTable table = tableLocator.getTable();
        if (table.getColumnCount() > 1)
        {
            for (int i = 0; i < table.getColumnCount(); i++)
            {
                String cell = table.getCellAsText(0, i).trim();
                columns.add(cell);

            }

            filterItems = newArrayList();

            for (int j = 1; j < table.getRowCount(); j++)
            {
                long id = 0;
                String name = null;
                String description = null;
                String author = null;
                List sharing = null;
                boolean isFav = false;
                long subscriptions = 0;
                List operations = null;
                long favCount = 0;
                for (int i = 0; i < columns.size(); i++)
                {
                    String col = columns.get(i);
                    TableCell cell = table.getTableCell(j, i);
                    if (col.equals("Name"))
                    {
                        name = parseName(cell);
                        description = parseDescription(cell);
                        isFav = parseIsFav(cell);
                        id = parseId(cell);
                    }
                    else if (col.equals("Owner"))
                    {
                        author = parseAuthor(cell);
                    }
                    else if (col.equals("Shared With"))
                    {
                        sharing = parseSharing(tableLocator.getNode(), id);
                    }
                    else if (col.equals("Subscriptions"))
                    {
                        subscriptions = parseSubscriptions(cell);
                    }
                    else if (col.equals("")) // Operations no longer has a heading
                    {
                        operations = parseOperations(cell);
                    }
                    else if (col.equals("Popularity"))
                    {
                        favCount = parseFavCount(cell);
                    }

                }
                filterItems.add(new FilterItem(id, name, description, author, sharing, isFav, subscriptions, operations, favCount));
            }
        }
        else
        {
            filterListName = table.getCellAsText(0, 0);
        }
    }

    private List<WebTestSharePermission> parseSharing(final Node tableNode, final Long rowId)
    {
        String str = new XPathLocator(tableNode, "tbody/tr[contains(@id,'" + rowId + "')]//ul[@class='shareList']").getText();

        List<WebTestSharePermission> list = newArrayList();

        int lastIndex = -1, currentIndex = 0;
        while (lastIndex < currentIndex && currentIndex < str.length())
        {
            lastIndex = currentIndex;
            if (str.indexOf(GLOBAL_PERM_STRING, currentIndex) >= 0)
            {
                //we have found a Shared with all users.
                list.add(new WebTestSharePermission(WebTestSharePermission.GLOBAL_TYPE, null, null));
                currentIndex += GLOBAL_PERM_STRING.length();
            }
            else
            {
                //we have found one shared with the group.
                Matcher matcher = GROUP_SHARE_REGEX.matcher(str);
                boolean matched = false;
                if (matcher.find(currentIndex))
                {
                    final String name = matcher.group(1);

                    list.add(new WebTestSharePermission(WebTestSharePermission.GROUP_TYPE, name, null));
                    currentIndex = matcher.end();
                    matched = true;
                }
                matcher = ROLE_SHARE_REGEX.matcher(str);
                if (!matched && matcher.find(currentIndex))
                {
                    final String project = matcher.group(1);
                    final String role = matcher.group(2);

                    list.add(new WebTestSharePermission(WebTestSharePermission.PROJECT_TYPE, project, role));
                    currentIndex = matcher.end();
                    matched = true;
                }
                matcher = PROJECT_SHARE_REGEX.matcher(str);
                if (!matched && matcher.find(currentIndex))
                {
                    final String project = matcher.group(1);

                    list.add(new WebTestSharePermission(WebTestSharePermission.PROJECT_TYPE, project, null));
                    currentIndex = matcher.end();
                }
            }
        }

        return list;
    }

    private Long parseId(final TableCell cell)
    {
        if (cell.getLinks().length > 1)
        {
            return new Long(cell.getLinks()[1].getParameterValues("requestId")[0]);
        }
        else
        {
            final WebLink webLink = cell.getLinks()[0];

            final String id = webLink.getID();
            Assert.assertNotNull("id must not be null + " + webLink, id);
            final String idString = id.substring("filterlink_".length(), id.length());
            Assert.assertNotNull(idString);
            Assert.assertFalse("id must not equal null string: + " + id, "null".equals(idString));
            return new Long(idString);
        }
    }

    private Long parseFavCount(final TableCell cell)
    {
        try
        {
            return new Long(cell.asText().trim());
        }
        catch (NumberFormatException e)
        {
            return new Long(cell.asText().trim().substring(0, 2).trim());
        }
    }

    private List<String> parseOperations(final TableCell cell)
    {
        final List<String> result = newArrayList();
        WebLink[] links = cell.getLinks();
        for (WebLink link : links)
        {
            if (!StringUtils.defaultString(link.getClassName()).contains("aui-list-item-link"))
            {
                continue;
            }
            String op = link.asText();
            op = op.trim();
            result.add(op);
        }
        return result;
    }

    private Long parseSubscriptions(final TableCell cell)
    {
        if (cell.asText().indexOf("None") > 0)
        {
            return 0L;
        }
        else
        {
            return new Long(cell.getLinks()[0].asText().substring(0, cell.getLinks()[0].asText().indexOf(" ")));
        }
    }

    private Boolean parseIsFav(final TableCell cell)
    {
        return cell.getLinks()[0].getTitle() != null && cell.getLinks()[0].getTitle().contains("Remove this filter from your favourites");
    }

    private Long parseIssues(final TableCell cell)
    {
        String issueCount;
        if (cell.getLinks().length == 0)
        {
            issueCount = cell.asText().trim();

        }
        else
        {
            issueCount = cell.getLinks()[0].asText().trim();
        }
        return new Long(issueCount);
    }

    private String parseAuthor(final TableCell cell)
    {
        return cell.asText().trim();
    }

    private String parseDescription(final TableCell cell)
    {
        if (cell.getLinks().length > 1)
        {
            return cell.asText().substring(cell.asText().lastIndexOf(cell.getLinks()[1].asText()) + cell.getLinks()[1].asText().length()).trim();
        }
        else
        {
            return cell.asText().substring(cell.asText().lastIndexOf(cell.getLinks()[0].asText()) + cell.getLinks()[0].asText().length()).trim();
        }
    }

    private String parseName(final TableCell cell)
    {
        if (cell.getLinks().length > 1)
        {
            return cell.getLinks()[1].asText().trim();
        }
        else
        {
            return cell.getLinks()[0].asText().trim();
        }
    }

    public boolean containsColumn(String columnName)
    {
        return columns.contains(columnName);
    }

    public String getFilterListName()
    {
        return filterListName;
    }

    public List getColumns()
    {
        return columns;
    }

    public List<FilterItem> getFilterItems()
    {
        return filterItems;
    }

    public boolean isEmpty()
    {
        return filterItems == null || filterItems.isEmpty();
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final FilterList that = (FilterList) o;

        if (columns != null ? !columns.equals(that.columns) : that.columns != null)
        {
            return false;
        }
        if (filterItems != null ? !filterItems.equals(that.filterItems) : that.filterItems != null)
        {
            return false;
        }
        if (!filterListName.equals(that.filterListName))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = filterListName.hashCode();
        result = 31 * result + (columns != null ? columns.hashCode() : 0);
        result = 31 * result + (filterItems != null ? filterItems.hashCode() : 0);
        return result;
    }
}
