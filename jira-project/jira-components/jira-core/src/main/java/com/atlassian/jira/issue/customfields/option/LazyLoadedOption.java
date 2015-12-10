package com.atlassian.jira.issue.customfields.option;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.customfields.manager.DefaultOptionsManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LazyLoadedOption implements Option, Comparable<Option>
{
    private final GenericValue gv;

    private final OptionsManager optionsManager;
    private final FieldConfigManager fieldManager;
    private final LazyReference<FieldConfig> relatedField = new LazyReference<FieldConfig>()
    {
        @Override
        protected FieldConfig create() throws Exception
        {
            return fieldManager.getFieldConfig(gv.getLong(DefaultOptionsManager.ENTITY_CONFIG_ID));
        }
    };

    public LazyLoadedOption(final GenericValue gv, final OptionsManager optionsManager, final FieldConfigManager fieldManager)
    {
        this.gv = gv;
        this.fieldManager = fieldManager;
        this.optionsManager = optionsManager;
    }

    public FieldConfig getRelatedCustomField()
    {
        return relatedField.get();
    }

    public Option getParentOption()
    {
        return optionsManager.findByOptionId(gv.getLong(OptionConstants.ENTITY_PARENT_OPTION));
    }

    @Nonnull
    public List<Option> getChildOptions()
    {
        return optionsManager.findByParentId(getOptionId());
    }

    // convenience method - candidate to be removed to a util class?
    public List<Option> retrieveAllChildren(@Nullable List<Option> listToAddTo)
    {
        if (listToAddTo == null)
        {
            listToAddTo = new LinkedList<Option>();
        }

        final Collection<Option> children = getChildOptions();

        if (children != null)
        {
            for (final Option childOption : children)
            {
                listToAddTo.add(childOption);
                childOption.retrieveAllChildren(listToAddTo);
            }
        }

        return listToAddTo;
    }

    public Long getOptionId()
    {
        return gv.getLong(OptionConstants.ENTITY_OPTION_ID);
    }

    public Long getSequence()
    {
        return gv.getLong(OptionConstants.ENTITY_SEQUENCE);
    }

    public String getValue()
    {
        return gv.getString(OptionConstants.ENTITY_VALUE);
    }

    public Boolean getDisabled()
    {
        Boolean disabled = gv.getBoolean(OptionConstants.ENTITY_DISABLED);
		return disabled != null ? disabled : false;
    }

    public void setValue(String value)
    {
        gv.set(OptionConstants.ENTITY_VALUE, value);
    }

    public void setSequence(final Long sequence)
    {
    	gv.set(OptionConstants.ENTITY_SEQUENCE, sequence);
    }

    public void setDisabled(final Boolean disabled)
    {
        gv.set(OptionConstants.ENTITY_DISABLED, disabled);
    }

    public void store()
    {
        try
        {
            gv.store();
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public GenericValue getGenericValue()
    {
        return gv;
    }

    public int compareTo(final Option o)
    {
        return new CompareToBuilder().append(getSequence(), o.getSequence()).append(getOptionId(), o.getOptionId()).toComparison();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (!(o instanceof Option))
        {
            return false;
        }
        final Option rhs = (Option) o;
        return new EqualsBuilder().append(getOptionId(), rhs.getOptionId()).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(147, 17).append(getOptionId()).toHashCode();
    }

    @Override
    public String toString()
    {
        return getValue();
    }

}
