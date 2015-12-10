package com.atlassian.jira.rest.v2.search;

import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.base.Functions.compose;
import static com.google.common.collect.Lists.transform;

/**
 * Transfer object for column configuration.
 *
 * @since v6.1
 */
@XmlRootElement
public class ColumnOptions
{
    private static Function<NavigableField, ColumnItem> FIELD_TO_COLUMN = new Function<NavigableField, ColumnItem>()
    {
        @Override
        public ColumnItem apply(NavigableField field)
        {
            return new ColumnOptions.ColumnItem(field.getId(), field.getName());
        }
    };

    public static final Function<ColumnLayoutItem,NavigableField> TO_FIELD = new Function<ColumnLayoutItem, NavigableField>()
    {
        @Override
        public NavigableField apply(ColumnLayoutItem input)
        {
            return input.getNavigableField();
        }
    };

    @XmlElement
    private List<ColumnItem> availableColumns;
    @XmlElement
    private List<ColumnItem> defaultColumns;

    public ColumnOptions(List<ColumnItem> availableColumns, List<ColumnItem> defaultColumns)
    {
        this.availableColumns = availableColumns;
        this.defaultColumns = defaultColumns;
    }

    @SuppressWarnings ({ "UnusedDeclaration", "unused" })
    ColumnOptions()
    {}

    public static List<ColumnItem> toColumnOptions(List<ColumnLayoutItem> items)
    {
        return transform(items, compose(FIELD_TO_COLUMN, TO_FIELD));
    }

    /**
     * Transfer object for a single column.
     */
    @XmlRootElement
    public static class ColumnItem
    {
        @XmlElement
        private String label;
        @XmlElement
        private String value;

        public ColumnItem(String value, String label)
        {
            this.label = label;
            this.value = value;
        }

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        ColumnItem()
        {}
    }
}
