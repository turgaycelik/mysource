package com.atlassian.jira.issue.customfields.option;

import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since v6.1
 */
public class MockOptionsManager implements OptionsManager
{
    private final OptionsTable table = new OptionsTable();

    @Override
    public Options getOptions(final FieldConfig fieldConfig)
    {
        return new OptionsImpl(toOptions(table.getRootByConfig(fieldConfig), OptionSorts.sequenceSort()), fieldConfig);
    }

    @Override
    public void setRootOptions(final FieldConfig fieldConfig, final Options options)
    {
        removeCustomFieldConfigOptions(fieldConfig);
        int seq = 0;
        for (Option option : options)
        {
            table.create(option.getValue(), null, seq++, fieldConfig);
        }
    }

    @Override
    public void removeCustomFieldOptions(final CustomField customField)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void removeCustomFieldConfigOptions(final FieldConfig fieldConfig)
    {
        table.removeByFieldConfig(fieldConfig);
    }

    @Override
    public void updateOptions(final Collection<Option> options)
    {
        for (Option option : options)
        {
            store(option);
        }
    }

    @Override
    public Option createOption(final FieldConfig fieldConfig, final Long parentOptionId, final Long sequence, final String value)
    {
        return toOption(table.create(value, parentOptionId, sequence, fieldConfig));
    }

    @Override
    public void deleteOptionAndChildren(final Option option)
    {
        for (Option child : option.getChildOptions())
        {
            deleteOptionAndChildren(child);
        }
        table.removeById(option.getOptionId());
    }

    @Override
    public Option findByOptionId(final Long optionId)
    {
        final OptionState byId = table.getById(optionId);
        return byId == null ? null : toOption(byId);
    }

    @Override
    public List<Option> getAllOptions()
    {
        return toOptions(table.getAll(), OptionSorts.valueSort());
    }

    @Override
    public void enableOption(final Option option)
    {
        option.setDisabled(false);
        store(option);
    }

    @Override
    public void disableOption(final Option option)
    {
        option.setDisabled(true);
        store(option);
    }

    @Override
    public void setValue(final Option option, final String value)
    {
        option.setValue(value);
        store(option);
    }

    @Override
    public List<Option> findByOptionValue(final String value)
    {
        return toOptions(table.getByValue(value), OptionSorts.valueSort());
    }

    @Override
    public List<Option> findByParentId(final Long parentOptionId)
    {
        return toOptions(table.getByParentId(parentOptionId), OptionSorts.sequenceSort());
    }

    public void store(Option option)
    {
        table.store(option);
    }

    public static List<String> optionsToPaths(Iterable<? extends Option> options)
    {
        List<String> stringOptions = Lists.newArrayList();
        for (Option option : options)
        {
            final String value = option.getValue();
            stringOptions.add(value);
            stringOptions.addAll(childOptionsToPaths(value, option));
        }
        return stringOptions;
    }

    private static List<String> childOptionsToPaths(String parentStr, Option option)
    {
        List<String> stringOptions = Lists.newArrayList();
        for (Option child : option.getChildOptions())
        {
            final String childStr = String.format("%s/%s", parentStr, child.getValue());
            stringOptions.add(childStr);
            stringOptions.addAll(childOptionsToPaths(childStr, child));
        }
        return stringOptions;
    }

    private Option toOption(OptionState state)
    {
        return new OptionImpl(state);
    }

    private List<Option> toOptions(final Iterable<OptionState> states, Ordering<? super Option> ordering)
    {
        return ordering.sortedCopy(toIterableOptions(states));
    }

    private Iterable<Option> toIterableOptions(final Iterable<OptionState> states)
    {
        return Iterables.transform(states, new Function<OptionState, Option>()
        {
            @Override
            public Option apply(final OptionState input)
            {
                return toOption(input);
            }
        });
    }

    private class OptionsTable
    {
        private long id = 0;
        private Map<Long, OptionState> options = Maps.newHashMap();

        private Iterable<OptionState> getAll()
        {
            return options.values();
        }

        private OptionState getById(long id)
        {
            return options.get(id);
        }

        private Iterable<OptionState> getByParentId(final Long parentId)
        {
            return OptionState.orderBySequence(Iterables.filter(options.values(), OptionState.parentPredicate(parentId)));
        }

        private Iterable<OptionState> getByValue(final String value)
        {
            return Iterables.filter(options.values(), OptionState.valuePredicate(value));
        }

        private Iterable<OptionState> getRootByConfig(final FieldConfig fieldConfig)
        {
            return OptionState.orderBySequence(Iterables.filter(options.values(),
                    Predicates.and(OptionState.isRoot(), OptionState.fieldConfigPredicate(fieldConfig))));
        }

        private void removeByFieldConfig(FieldConfig fieldConfig)
        {
            for (Iterator<OptionState> iterator = options.values().iterator(); iterator.hasNext(); )
            {
                OptionState next = iterator.next();
                if (fieldConfig.equals(next.config))
                {
                    iterator.remove();
                }
            }
        }

        private long nextId()
        {
            return id++;
        }

        private OptionState create(String value, Long parentId, long sequence, FieldConfig config)
        {
            final OptionState optionState = new OptionState(nextId(), value, parentId, sequence, config);
            options.put(optionState.id, optionState);
            return optionState;
        }

        private OptionState store(Option option)
        {
            if (option.getOptionId() == null)
            {
                throw new IllegalArgumentException("Option was not previously stored.");
            }
            final OptionState value = new OptionState(option);
            options.put(option.getOptionId(), value);
            return value;
        }

        private void removeById(final Long optionId)
        {
            options.remove(optionId);
        }
    }

    private static class OptionState
    {
        private static Iterable<OptionState> orderBySequence(final Iterable<OptionState> filter)
        {
            return Ordering.natural().onResultOf(new Function<OptionState, Long>()
            {
                @Override
                public Long apply(final OptionState input)
                {
                    return input.sequence;
                }
            }).sortedCopy(filter);
        }

        private static Predicate<OptionState> fieldConfigPredicate(final FieldConfig config)
        {
            return new Predicate<OptionState>()
            {
                @Override
                public boolean apply(final OptionState input)
                {
                    return config.equals(input.config);
                }
            };
        }

        private static Predicate<OptionState> valuePredicate(final String value)
        {
            return new Predicate<OptionState>()
            {
                @Override
                public boolean apply(final OptionState input)
                {
                    return value.equals(input.value);
                }
            };
        }

        private static Predicate<OptionState> parentPredicate(final Long parentId)
        {
            return new Predicate<OptionState>()
            {
                @Override
                public boolean apply(final OptionState input)
                {
                    return Objects.equal(parentId, input.parentId);
                }
            };
        }

        private static Predicate<OptionState> isRoot()
        {
            return new Predicate<OptionState>()
            {
                @Override
                public boolean apply(final OptionState input)
                {
                    return input.parentId == null;
                }
            };
        }

        private final Long parentId;
        private final long sequence;
        private final Boolean disabled;
        private final FieldConfig config;
        private final String value;
        private final long id;

        private OptionState(long id, String value, Long parentId, long sequence, FieldConfig config)
        {
            this.id = id;
            this.value = value;
            this.parentId = parentId;
            this.disabled = false;
            this.config = config;
            this.sequence = sequence;
        }

        private OptionState(Option option)
        {
            if (option.getOptionId() == null)
            {
                throw new IllegalArgumentException();
            }

            this.sequence = option.getSequence();
            this.disabled = option.getDisabled();
            this.config = option.getRelatedCustomField();
            this.value = option.getValue();
            this.id = option.getOptionId();

            final Option parentOption = option.getParentOption();
            this.parentId = parentOption != null ? parentOption.getOptionId() : null;
        }
    }

    private Option createById(Long id)
    {
        if (id != null)
        {
            OptionState state = table.getById(id);
            if (state != null)
            {
                return new OptionImpl(state);
            }
        }
        return null;
    }

    private List<Option> createByParentId(Long id)
    {
        return toOptions(table.getByParentId(id), OptionSorts.sequenceSort());
    }

    private class OptionImpl implements Option
    {
        private final Long parentId;
        private final Long id;

        private Long sequence;
        private Boolean disabled;
        private FieldConfig config;
        private String value;

        private Option parent;
        private List<Option> children;

        private OptionImpl(final OptionState state)
        {
            this.id = state.id;
            this.sequence = state.sequence;
            this.disabled = state.disabled;
            this.config = state.config;
            this.value = state.value;
            this.parentId = state.parentId;
        }

        @Override
        public Long getSequence()
        {
            return sequence;
        }

        @Override
        public Boolean getDisabled()
        {
            return disabled;
        }

        @Override
        public GenericValue getGenericValue()
        {
            throw new UnsupportedOperationException("Not the mock you are looking for.");
        }

        @Override
        public FieldConfig getRelatedCustomField()
        {
            return this.config;
        }

        @Override
        public Option getParentOption()
        {
            if (parent == null)
            {
                if (parentId != null)
                {
                    parent = createById(parentId);
                }
            }
            return parent;
        }

        @Override
        public void setSequence(final Long sequence)
        {
            this.sequence = sequence;
        }

        @Override
        public void setValue(final String value)
        {
            this.value = value;
        }

        @Override
        public void setDisabled(final Boolean disabled)
        {
            this.disabled = disabled;
        }

        @Override
        public List<Option> retrieveAllChildren(@Nullable List<Option> listToAddTo)
        {
            if (listToAddTo == null)
            {
                listToAddTo = Lists.newArrayList();
            }
            final List<Option> childOptions = getChildOptions();
            for (Option childOption : childOptions)
            {
                childOptions.add(childOption);
                childOption.retrieveAllChildren(listToAddTo);
            }
            return listToAddTo;
        }

        @Override
        public void store()
        {
            MockOptionsManager.this.store(this);
        }

        @Nullable
        @Override
        public Long getOptionId()
        {
            return id;
        }

        @Override
        public String getValue()
        {
            return value;
        }

        @Nonnull
        @Override
        public List<Option> getChildOptions()
        {
            if (children == null)
            {
                if (this.id != null)
                {
                    children = createByParentId(this.id);
                }
                else
                {
                    children = Lists.newArrayList();
                }
            }
            return children;
        }
    }

    private class OptionsImpl implements Options
    {
        private final List<Option> options;
        private final FieldConfig config;

        private OptionsImpl(final List<Option> options, final FieldConfig config)
        {
            this.options = options;
            this.config = config;
        }

        @Override
        public List<Option> getRootOptions()
        {
            return options;
        }

        @Override
        public Option getOptionById(final Long optionId)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Option getOptionForValue(final String value, final Long parentOptionId)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Option addOption(final Option parent, final String value)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void removeOption(final Option option)
        {
            MockOptionsManager.this.deleteOptionAndChildren(option);
        }

        @Override
        public void moveToStartSequence(final Option option)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void incrementSequence(final Option option)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void decrementSequence(final Option option)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void moveToLastSequence(final Option option)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void setValue(final Option option, final String value)
        {
            MockOptionsManager.this.setValue(option, value);
        }

        @Override
        public void enableOption(final Option option)
        {
            MockOptionsManager.this.enableOption(option);
        }

        @Override
        public void disableOption(final Option option)
        {
            MockOptionsManager.this.disableOption(option);
        }

        @Override
        public FieldConfig getRelatedFieldConfig()
        {
            return config;
        }

        @Override
        public void sortOptionsByValue(final Option parentOption)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void moveOptionToPosition(final Map<Integer, Option> positionsToOptions)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean add(final Option option) {return options.add(option);}

        @Override
        public void add(final int index, final Option element) {options.add(index, element);}

        @Override
        public boolean addAll(@Nonnull final Collection<? extends Option> c) {return options.addAll(c);}

        @Override
        public boolean addAll(final int index, @Nonnull final Collection<? extends Option> c) {return options.addAll(index, c);}

        @Override
        public void clear() {options.clear();}

        @Override
        public boolean contains(final Object o) {return options.contains(o);}

        @Override
        public boolean containsAll(@Nonnull final Collection<?> c) {return options.containsAll(c);}

        @Override
        public boolean equals(final Object o) {return options.equals(o);}

        @Override
        public Option get(final int index) {return options.get(index);}

        @Override
        public int hashCode() {return options.hashCode();}

        @Override
        public int indexOf(final Object o) {return options.indexOf(o);}

        @Override
        public boolean isEmpty() {return options.isEmpty();}

        @Nonnull
        @Override
        public Iterator<Option> iterator() {return options.iterator();}

        @Override
        public int lastIndexOf(final Object o) {return options.lastIndexOf(o);}

        @Nonnull
        @Override
        public ListIterator<Option> listIterator() {return options.listIterator();}

        @Nonnull
        @Override
        public ListIterator<Option> listIterator(final int index) {return options.listIterator(index);}

        @Override
        public Option remove(final int index) {return options.remove(index);}

        @Override
        public boolean remove(final Object o) {return options.remove(o);}

        @Override
        public boolean removeAll(@Nonnull final Collection<?> c) {return options.removeAll(c);}

        @Override
        public boolean retainAll(@Nonnull final Collection<?> c) {return options.retainAll(c);}

        @Override
        public Option set(final int index, final Option element) {return options.set(index, element);}

        @Override
        public int size() {return options.size();}

        @Nonnull
        @Override
        public List<Option> subList(final int fromIndex, final int toIndex)
        {
            return options.subList(fromIndex, toIndex);
        }

        @Nonnull
        @Override
        public Object[] toArray() {return options.toArray();}

        @Nonnull
        @Override
        public <T> T[] toArray(final T[] a) {return options.toArray(a);}
    }

    private static class OptionSorts
    {
        public static final Ordering<Option> SEQUENCE_ORDERING = Ordering.natural().onResultOf(new Function<Option, String>()
        {
            @Override
            public String apply(final Option input)
            {
                return input.getValue();
            }
        });

        public static final Ordering<Option> VALUE_ORDERING = Ordering.natural().onResultOf(new Function<Option, Long>()
        {
            @Override
            public Long apply(final Option input)
            {
                return input.getSequence();
            }
        });

        private static Ordering<Option>valueSort()
        {
            return SEQUENCE_ORDERING;
        }

        private static Ordering<Option> sequenceSort()
        {
            return VALUE_ORDERING;
        }
    }
}
