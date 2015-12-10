package com.atlassian.jira.action.issue.customfields.option;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.action.issue.customfields.impl.MockOptions;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.LazyLoadedOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

import org.ofbiz.core.entity.GenericValue;

public class MockOptionManager implements OptionsManager
{

    Map<Long, GenericValue> fakeDb = new HashMap<Long, GenericValue>();
    MockOfBizDelegator delegator;

    public MockOptionManager(final Map<Long, GenericValue> fakeDb)
    {
        this.fakeDb = fakeDb;
        delegator = new MockOfBizDelegator(new ArrayList<GenericValue>(fakeDb.values()), null);
    }

    public List<Option> getAllOptions()
    {
        return null;
    }

    public Options getOptions(final FieldConfig fieldConfig)
    {
        return new MockOptions(fieldConfig, fakeDb);
    }

    public void setRootOptions(final FieldConfig fieldConfig, final Options options)
    {}

    public void updateOptions(final Collection<Option> options)
    {}

    public Option createOption(final FieldConfig fieldConfig, final Long parentOptionId, final Long sequence, final String value)
    {
        return null;
    }

    public void deleteOptionAndChildren(final Option option)
    {}

    public Option findByOptionId(final Long optionId)
    {
        final Object o = fakeDb.get(optionId);

        if (o != null)
        {
            return new LazyLoadedOption((GenericValue) o, this, null);
        }
        else
        {
            return null;
        }
    }

    public List<Option> findByOptionValue(final String value)
    {
        return null;
    }

    public Option findByValueAndParent(final String value, final Long parentOptionId)
    {
        return null;
    }

    public List<Option> findByParentId(final Long parentOptionId)
    {
        final List<Option> l = new ArrayList<Option>();

        for (final Object element : fakeDb.values())
        {
            final GenericValue genericValue = (GenericValue) element;
            final Long parent = genericValue.getLong("parentoptionid");
            if (parentOptionId.equals(parent))
            {
                l.add(new LazyLoadedOption(genericValue, this, null));
            }
        }

        return l;
    }

    public void removeCustomFieldOptions(final CustomField customField)
    {}

    public void removeCustomFieldConfigOptions(final FieldConfig fieldConfig)
    {
    }

    public void enableOption(final Option option)
    {
    }

    public void disableOption(final Option option)
    {
    }

    public void setValue(final Option option, final String value)
    {
    }

}
