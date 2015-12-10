package com.atlassian.jira.functest.framework.parser.filter;

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
 * Parses the data in the filter table ui component.
 *
 * @since v4.4
 */
public class FilterTableParser
{
    private final LocatorFactory locators;

    public FilterTableParser(final LocatorFactory locators)
    {
        this.locators = locators;
    }

    public List<FilterItem> parse(final String tableId)
    {
        return parse(locators.css("#" + tableId).getNode());
    }

    private List<FilterItem> parse(Node filterTableNode)
    {
        CssLocator cssLocator = locatorForFilterRows(filterTableNode);
        List<Node> filterNodes = asList(cssLocator.getNodes());

        return transform(filterNodes, new Function<Node, FilterItem>()
        {
            @Override
            public FilterItem apply(@Nullable Node filterNode)
            {
                return parseFilterItem(filterNode);
            }
        });
    }

    private FilterItem parseFilterItem(Node filterNode)
    {
        return new FilterItem.Builder().
                id(new ParseFilterId().apply(filterNode)).
                name(new ParseFilterName().apply(filterNode)).
                description(new ParseFilterDescription().apply(filterNode)).
                owner(new ParseFilterOwner().apply(filterNode)).
                build();
    }

    private CssLocator locatorForFilterRows(Node filterTableNode)
    {
        return new CssLocator(filterTableNode, "tbody tr");
    }

    private static class ParseFilterName implements Function<Node, String>
    {
        @Override
        public String apply(@Nullable Node input)
        {
            return new CssLocator(input, "td [data-filter-field=name]").getText();
        }
    }

    private static class ParseFilterDescription implements Function<Node, String>
    {
        public static final String EMPTY_DESCRIPTION = "";

        @Override
        public String apply(@Nullable Node input)
        {
            final CssLocator filterDescriptionLocator = new CssLocator(input, "td [data-filter-field=description]");
            if (filterDescriptionLocator.exists())
            {
                return filterDescriptionLocator.getText();
            }
            return EMPTY_DESCRIPTION;
        }
    }

    private static class ParseFilterOwner implements Function<Node, String>
    {
        static final String NO_OWNER_INFO = "";

        @Override
        public String apply(@Nullable Node input)
        {
            final CssLocator filterDescriptionLocator = new CssLocator(input, "td [data-filter-field=owner-full-name]");
            if (filterDescriptionLocator.exists())
            {
                return filterDescriptionLocator.getText();
            }
            return NO_OWNER_INFO;
        }
    }

    private static class ParseFilterId implements Function<Node, Long>
    {
        @Override
        public Long apply(@Nullable Node filterRowNode)
        {
            Assertions.notNull("Can not retrieve a filter id from a ''null'' DOM Node", filterRowNode);
            return valueOf(filterRowNode.getAttributes().getNamedItem("data-filter-id").getNodeValue());
        }
    }
}
