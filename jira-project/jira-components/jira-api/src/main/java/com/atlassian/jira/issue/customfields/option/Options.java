package com.atlassian.jira.issue.customfields.option;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.config.FieldConfig;

import java.util.List;
import java.util.Map;

@PublicApi
public interface Options extends List<Option>
{
    /**
     * This is the same as <code>getOptions(null)</code>
     */
    public List<Option> getRootOptions();

    public Option getOptionById(Long optionId);

    public Option getOptionForValue(String value, Long parentOptionId);

    public Option addOption(Option parent, String value);

    public void removeOption(Option option);

    public void moveToStartSequence(Option option);

    public void incrementSequence(Option option);

    public void decrementSequence(Option option);

    public void moveToLastSequence(Option option);

    public void setValue(Option option, String value);

    public void enableOption(Option option);

    public void disableOption(Option option);

    public FieldConfig getRelatedFieldConfig();

    void sortOptionsByValue (Option parentOption);

    void moveOptionToPosition(Map<Integer, Option> positionsToOptions);
}
