package com.atlassian.jira.issue.customfields.option;

import com.atlassian.jira.issue.comparator.BeanComparatorIgnoreCase;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.CollectionReorderer;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OptionsImpl extends ArrayList<Option> implements Options
{
    private static final long serialVersionUID = 1946632069203605222L;

    private final Map<Long, Option> optionsLookup;
    private final FieldConfig relatedField;
    private final CollectionReorderer<Option> reorderer;
    private final OptionsManager optionsManager;

    public OptionsImpl(List<Option> options, FieldConfig relatedField, CollectionReorderer<Option> reorderer, OptionsManager optionsManager)
    {
        this.relatedField = relatedField;
        this.reorderer = reorderer;
        this.optionsManager = optionsManager;

        optionsLookup = new HashMap<Long, Option>();

        populateFromOptions(options);
    }

    private void populateFromOptions(List<Option> options)
    {
        if (options != null && !options.isEmpty())
        {
            List<Option> rootOptions = Lists.newArrayList();
            for (final Option option : options)
            {
                Option parentOption = option.getParentOption();
                if (parentOption == null)
                {
                    rootOptions.add(option);
                }
                optionsLookup.put(option.getOptionId(), option);
            }
            this.addAll(rootOptions);
        }
    }

    public List<Option> getRootOptions()
    {
        return this;
    }

    private Collection<Option> getPeerOptions(Option option)
    {
        final Option parentOption = option.getParentOption();
        if (parentOption != null)
        {
            return parentOption.getChildOptions();
        }
        else
        {
            return getRootOptions();
        }
    }

    public Option getOptionForValue(String value, Long parentOptionId)
    {
        Collection<Option> optionsForParent;
        if (parentOptionId != null)
        {
            optionsForParent = getOptionById(parentOptionId).getChildOptions();
        }
        else
        {
            optionsForParent = getRootOptions();
        }

        if (optionsForParent != null)
        {
            for (Option option : optionsForParent)
            {
                if (option != null && option.getValue() != null && option.getValue().equalsIgnoreCase(value))
                {
                    return option;
                }
            }
        }

        return null;
    }

    public void setValue(Option option, String value)
    {
        optionsManager.setValue(option, value);
    }

    public Option addOption(Option parent, String value)
    {
        Collection parentColl;
        Long parentOptionId;
        if (parent != null)
        {
            parentColl = parent.getChildOptions();
            parentOptionId = parent.getOptionId();
        }
        else
        {
            parentColl = getRootOptions();
            parentOptionId = null;
        }

        long lastPosition =  parentColl != null ? parentColl.size() : 0;
        return optionsManager.createOption(getRelatedFieldConfig(), parentOptionId, lastPosition, value);
    }

    public void removeOption(Option option)
    {
        optionsManager.deleteOptionAndChildren(option);

        // Renumber the list
        Collection<Option> peers = getPeerOptions(option);

        int i = 0;
        for (Iterator<Option> iterator = peers.iterator(); iterator.hasNext();)
        {
            Option currentOption = iterator.next();
            if (currentOption.equals(option))
            {
                iterator.remove();
            }
            else
            {
                currentOption.setSequence((long) i);
                i++;
            }
        }

        optionsManager.updateOptions(peers);
    }

    public void sortOptionsByValue(Option parentOption)
    {
        List<Option> options = new ArrayList<Option>(parentOption != null ? parentOption.getChildOptions() : getRootOptions());
        Collections.sort(options, new BeanComparatorIgnoreCase<Option>("value"));
        renumberOptions(options);

        optionsManager.updateOptions(options);
    }

    public void moveOptionToPosition(Map<Integer, Option> positionsToOptions)
    {
        if (positionsToOptions.isEmpty())
        { return; }

        // Assume that all options in the map are from the same option set
        Option option = positionsToOptions.values().iterator().next();
        List<Option> peerOptions = new ArrayList<Option>(getPeerOptions(option));
        reorderer.moveToPosition(peerOptions, positionsToOptions);
        renumberOptions(peerOptions);

        optionsManager.updateOptions(peerOptions);
    }

    public void moveToStartSequence(Option option)
    {
        List<Option> peerOptions = new ArrayList<Option>(getPeerOptions(option));
        reorderer.moveToStart(peerOptions, option);
        renumberOptions(peerOptions);

        optionsManager.updateOptions(peerOptions);
    }

    public void incrementSequence(Option option)
    {
        List<Option> peerOptions = new ArrayList<Option>(getPeerOptions(option));
        reorderer.decreasePosition(peerOptions, option);
        renumberOptions(peerOptions);

        optionsManager.updateOptions(peerOptions);
    }

    public void decrementSequence(Option option)
    {
        List<Option> peerOptions = new ArrayList<Option>(getPeerOptions(option));
        reorderer.increasePosition(peerOptions, option);
        renumberOptions(peerOptions);

        optionsManager.updateOptions(peerOptions);
    }

    public void moveToLastSequence(Option option)
    {
        List<Option> peerOptions = new ArrayList<Option>(getPeerOptions(option));
        reorderer.moveToEnd(peerOptions, option);
        renumberOptions(peerOptions);

        optionsManager.updateOptions(peerOptions);
    }

    public void disableOption(Option option)
    {
        optionsManager.disableOption(option);
    }

    public void enableOption(Option option)
    {
        optionsManager.enableOption(option);
    }

    public Option getOptionById(Long optionId)
    {
        return optionsLookup.get(optionId);
    }

    public FieldConfig getRelatedFieldConfig()
    {
        return relatedField;
    }

    private void renumberOptions(List<Option> options)
    {
        if (options != null)
        {
            int pos = 0;
            for (Option option : options)
            {
                option.setSequence((long)pos);
                pos++;
            }
        }
    }
}
