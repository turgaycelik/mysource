package com.atlassian.jira.functest.framework.parser.dashboard;

import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.base.Function;
import org.w3c.dom.Node;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.transform;
import static java.lang.Long.valueOf;
import static java.util.Arrays.asList;

/**
 * Parses the data in the dashboard  table ui component.
 *
 * @since v4.4
 */
public class DashboardTableParser
{
    private final LocatorFactory locators;

    public DashboardTableParser(final LocatorFactory locators)
    {
        this.locators = locators;
    }

    public List<DashboardItem> parse(final String tableId)
    {
        return parse(locators.css("#" + tableId).getNode());
    }

    private List<DashboardItem> parse(Node dashboardTableNode)
    {
        CssLocator cssLocator = locatorForDashboardRows(dashboardTableNode);
        List<Node> filterNodes = asList(cssLocator.getNodes());

        return transform(filterNodes, new Function<Node, DashboardItem>()
        {
            @Override
            public DashboardItem apply(@Nullable Node dashboardNode)
            {
                return parseDashboardItem(dashboardNode);
            }
        });
    }

    private DashboardItem parseDashboardItem(Node dashboardNode)
    {
        return new DashboardItem.Builder().
                id(new ParseDashboardId().apply(dashboardNode)).
                name(new ParseDashboardName().apply(dashboardNode)).
                description(new ParseDashboardDescription().apply(dashboardNode)).
                owner(new ParseDashboardOwner().apply(dashboardNode)).
                build();
    }

    private CssLocator locatorForDashboardRows(Node dashboardTableNode)
    {
        return new CssLocator(dashboardTableNode, "tbody tr");
    }

    private static class ParseDashboardName implements Function<Node, String>
    {
        @Override
        public String apply(@Nullable Node input)
        {
            return new CssLocator(input, "td [data-field=name]").getText();
        }
    }

    private static class ParseDashboardDescription implements Function<Node, String>
    {
        public static final String EMPTY_DESCRIPTION = "";

        @Override
        public String apply(@Nullable Node input)
        {
            final CssLocator filterDescriptionLocator = new CssLocator(input, "td [data-field=description]");
            if (filterDescriptionLocator.exists())
            {
                return filterDescriptionLocator.getText();
            }
            return EMPTY_DESCRIPTION;
        }
    }

    private static class ParseDashboardOwner implements Function<Node, String>
    {
        static final String NO_OWNER_INFO = "";

        @Override
        public String apply(@Nullable Node input)
        {
            final CssLocator filterDescriptionLocator = new CssLocator(input, "td [data-field=owner]");
            if (filterDescriptionLocator.exists())
            {
                return filterDescriptionLocator.getText();
            }
            return NO_OWNER_INFO;
        }
    }

    private static class ParseDashboardId implements Function<Node, Long>
    {
        @Override
        public Long apply(@Nullable Node dashboardRowNode)
        {
            Assertions.notNull("Can not retrieve a dashboard id from a ''null'' DOM Node", dashboardRowNode);
            return valueOf(dashboardRowNode.getAttributes().getNamedItem("id").getNodeValue().substring(3));
        }
    }
}
