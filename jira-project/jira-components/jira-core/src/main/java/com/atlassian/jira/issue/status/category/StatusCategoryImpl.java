package com.atlassian.jira.issue.status.category;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.Iterables.filter;

/**
 * @since v6.1
 */
public class StatusCategoryImpl implements StatusCategory
{
    private static final List<StatusCategory> CATEGORIES;
    private static final Map<String, StatusCategory> CATEGORIES_BY_KEY;
    private static final Map<String, StatusCategory> CATEGORIES_BY_NAME;
    private static final Map<Long, StatusCategory> CATEGORIES_BY_ID;

    private final Long id;
    private final String key;
    private final String name;
    private final String colorName;
    private final Long sequence;

    private StatusCategoryImpl(final Long id, final String key, final String name, final String colorName, final Long sequence)
    {
        this.id = id;
        this.key = key;
        this.name = name;
        this.colorName = colorName;
        this.sequence = sequence;
    }

    public Long getId()
    {
        return id;
    }

    public String getKey()
    {
        return key;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getTranslatedName()
    {
        return getTranslatedName(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper());
    }

    @Override
    public String getTranslatedName(final String locale)
    {
        return getTranslatedName(ComponentAccessor.getI18nHelperFactory().getInstance(new Locale(locale)));
    }

    public String getTranslatedName(I18nHelper i18n)
    {
        return i18n.getText("common.statuscategory." + getKey());
    }

    public String getColorName()
    {
        return colorName;
    }

    public Long getSequence()
    {
        return sequence;
    }

    @Override
    public int compareTo(final StatusCategory o)
    {
        return id.compareTo(o.getId());
    }

    public static StatusCategory findById(Long id)
    {
        return CATEGORIES_BY_ID.get(id);
    }

    public static StatusCategory findByKey(String key){
        return CATEGORIES_BY_KEY.get(key);
    }

    public static StatusCategory findByName(String key){
        return CATEGORIES_BY_NAME.get(key);
    }

    static
    {
        /*
          Status Category Name is hardcoded here instead of taking from default i18n bundle because changing it would
          break existing JQL filters and requires an upgrade task and/or backward compatibility coded into
          StatusCategoryValidator and StatusCategoryClauseQueryFactory.
         */
        CATEGORIES = ImmutableList.<StatusCategory>of(
                new StatusCategoryImpl(1L, UNDEFINED, "No Category", "medium-gray", 1L),
                new StatusCategoryImpl(2L, TO_DO, "New", "blue-gray", 2L),
                new StatusCategoryImpl(3L, COMPLETE, "Complete", "green", 3L),
                new StatusCategoryImpl(4L, IN_PROGRESS, "In Progress", "yellow", 4L)
        );

        CATEGORIES_BY_ID = Maps.uniqueIndex(CATEGORIES, new Function<StatusCategory, Long>()
        {
            @Override
            public Long apply(@Nullable final StatusCategory input)
            {
                return input.getId();
            }
        });

        CATEGORIES_BY_KEY = Maps.uniqueIndex(CATEGORIES, new Function<StatusCategory, String>()
        {
            @Override
            public String apply(@Nullable final StatusCategory input)
            {
                return input.getKey();
            }
        });

        CATEGORIES_BY_NAME = Maps.uniqueIndex(CATEGORIES, new Function<StatusCategory, String>()
        {
            @Override
            public String apply(final StatusCategory input)
            {
                return input.getName();
            }
        });
    }

    public static StatusCategory getDefault()
    {
        return CATEGORIES_BY_KEY.get(UNDEFINED);
    }

    public static List<StatusCategory> getAllCategories()
    {
        return CATEGORIES;
    }

    public static List<StatusCategory> getUserVisibleCategories()
    {
        return Lists.newArrayList(filter(CATEGORIES, new Predicate<StatusCategory>(){
            @Override
            public boolean apply(final StatusCategory statusCategory)
            {
                return !statusCategory.getKey().equals(StatusCategory.UNDEFINED);
            }
        }));
    }
}
