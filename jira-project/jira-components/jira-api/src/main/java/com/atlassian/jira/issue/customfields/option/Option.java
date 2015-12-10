package com.atlassian.jira.issue.customfields.option;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * should merge with the field.option.Option
 */
@PublicApi
public interface Option extends SimpleOption<Option>
{
    Long getSequence();

    /**
     * Get the disabled status.
     * A disabled option will is not available to be assigned to this associated custom field, It remains
     * valid historically and for searching with.
     */
    Boolean getDisabled();

    GenericValue getGenericValue();

    FieldConfig getRelatedCustomField();

    Option getParentOption();

    void setSequence(Long sequence);

    void setValue(String value);

    void setDisabled(Boolean disabled);

    //convenience method - candidate to be removed to a util class?
    List<Option> retrieveAllChildren(@Nullable List<Option> listToAddTo);

    void store();

    /**
     * {@inheritDoc}
     *
     * NOTE: This method is here is keep the API checker happy (see JRADEV-23901).
     */
    @Nullable
    Long getOptionId();

    /**
     * {@inheritDoc}
     *
     * NOTE: This method is here is keep the API checker happy (JRADEV-23901).
     */
    String getValue();

    /**
     * {@inheritDoc}
     *
     * NOTE: This method is here is keep the API checker happy (JRADEV-23901).
     */
    @Nonnull
    List<Option> getChildOptions();
}
