package com.atlassian.jira.sharing;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.parameters.lucene.sort.MappedSortComparator;
import com.atlassian.jira.issue.statistics.AssigneeStatisticsMapper;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;

import javax.annotation.concurrent.Immutable;

/**
 * Implementation details of a {@link SharedEntityColumn} which is used for sorting Shared Entity search results.
 * <p>
 * Note: the sortColumn is used for equality/hashCode and MUST be distinct.
 *
 * @since v3.13 - was broken out from SharedEntityColumn in v5.0
 *
 * @see SharedEntityColumn
 */
@Immutable
public class SharedEntityColumnDefinition
{
    public static final SharedEntityColumnDefinition ID = new SharedEntityColumnDefinition("id", SortField.INT);
    public static final SharedEntityColumnDefinition NAME = new SharedEntityColumnDefinition("name", "nameSort", "nameCaseless", SortField.STRING);
    public static final SharedEntityColumnDefinition DESCRIPTION = new SharedEntityColumnDefinition("description", "descriptionSort", SortField.STRING);
    public static final SharedEntityColumnDefinition OWNER = new SharedEntityColumnDefinition("owner", new SortComparatorFactory()
    {
        public FieldComparatorSource getSortComparator()
        {
            return new MappedSortComparator(ComponentAccessor.getComponentOfType(AssigneeStatisticsMapper.class));
        }
    });
    public static final SharedEntityColumnDefinition FAVOURITE_COUNT = new SharedEntityColumnDefinition("favouriteCount", SortField.INT);
    public static final SharedEntityColumnDefinition IS_SHARED = new SharedEntityColumnDefinition("isShared", SortField.STRING);

    private final String name;
    private final String sortColumn;
    private final String caseInsensitiveColumn;
    private final int sortType;
    private final SortComparatorFactory sortComparatorFactory;

    private SharedEntityColumnDefinition(final String sortColumn, final int sortType)
    {
        this(sortColumn, sortColumn, sortType);
    }

    private SharedEntityColumnDefinition(final String name, final String sortColumn, final int sortType)
    {
        Assertions.notNull("sortColumn", sortColumn);
        this.name = name;
        this.sortColumn = sortColumn;
        this.caseInsensitiveColumn = null;
        this.sortType = sortType;
        sortComparatorFactory = null;
    }

    private SharedEntityColumnDefinition(final String name, final String sortColumn, final String caseInsensitiveColumn, final int sortType)
    {
        Assertions.notNull("sortColumn", sortColumn);
        this.name = name;
        this.sortColumn = sortColumn;
        this.sortType = sortType;
        this.caseInsensitiveColumn = caseInsensitiveColumn;
        sortComparatorFactory = null;
    }

    private SharedEntityColumnDefinition(final String name, final SortComparatorFactory sortComparatorFactory)
    {
        Assertions.notNull("sortColumn", name);
        this.name = name;
        this.sortComparatorFactory = sortComparatorFactory;
        this.caseInsensitiveColumn = null;
        sortColumn = name;
        sortType = SortField.CUSTOM;
    }

    /**
     * @return the column (field) name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the column (field) name used for sorting
     */
    public String getSortColumn()
    {
        return sortColumn;
    }

    /**
     * @return the {@link org.apache.lucene.search.SortField} int used to determine the comparison algorithm
     */
    public int getSortType()
    {
        return sortType;
    }

    public String getCaseInsensitiveColumn()
    {
        return caseInsensitiveColumn;
    }

    public boolean isCustomSort()
    {
        return sortComparatorFactory != null;
    }

    public FieldComparatorSource createSortComparator()
    {
        return sortComparatorFactory.getSortComparator();
    }

    public String toString()
    {
        return name;
    }

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final SharedEntityColumnDefinition sortOrder = (SharedEntityColumnDefinition) o;

        if (sortColumn != null ? !sortColumn.equals(sortOrder.sortColumn) : sortOrder.sortColumn != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (sortColumn != null ? sortColumn.hashCode() : 0);
    }

    /**
     * Returns the definition details for the given SharedEntityColumn.
     *
     * @param column The simple SharedEntityColumn
     * @return the definition details for the given SharedEntityColumn.
     */
    public static SharedEntityColumnDefinition definitionFor(SharedEntityColumn column)
    {
        switch (column)
        {
            case ID:
                return ID;
            case NAME:
                return NAME;
            case DESCRIPTION:
                return DESCRIPTION;
            case OWNER:
                return OWNER;
            case FAVOURITE_COUNT:
                return FAVOURITE_COUNT;
        }
        throw new IllegalArgumentException("Unknown SharedEntityColumn " + column);
    }

    ///CLOVER:ON

    /**
     * Used for implementing custom sorting. Create the SortComparator
     */
    interface SortComparatorFactory
    {
        FieldComparatorSource getSortComparator();
    }
}
