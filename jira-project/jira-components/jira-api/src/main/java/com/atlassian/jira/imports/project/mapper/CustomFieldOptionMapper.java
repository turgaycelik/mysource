package com.atlassian.jira.imports.project.mapper;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldOption;
import org.apache.commons.collections.map.MultiValueMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to map custom field options from a backup project to an existing project.
 *
 * @since v3.13
 */
@PublicApi
public class CustomFieldOptionMapper extends AbstractMapper
{
    private final MultiValueMap parentOptionMap;
    private final MultiValueMap childOptionMap;
    private final Map<String, ExternalCustomFieldOption> allOptionMap;

    public CustomFieldOptionMapper()
    {
        parentOptionMap = new MultiValueMap();
        childOptionMap = new MultiValueMap();
        allOptionMap = new HashMap<String, ExternalCustomFieldOption>();
    }

    public void registerOldValue(final ExternalCustomFieldOption customFieldOption)
    {
        // NOTE: this is not used and we are doing it so the AbstractMapper methods will work, this may use more memory
        // than we want, we will check.
        super.registerOldValue(customFieldOption.getId(), customFieldOption.getValue());

        allOptionMap.put(customFieldOption.getId(), customFieldOption);
        // check if it is a ParentOption
        if (customFieldOption.getParentId() == null)
        {
            parentOptionMap.put(customFieldOption.getFieldConfigId(), customFieldOption);
        }
        else
        {
            // We assume that the child's fieldConfigId is correct and the same as the parent's, otherwise something
            // is really wrong with the data and we don't really know how to behave
            childOptionMap.put(customFieldOption.getParentId(), customFieldOption);
        }
    }

    public ExternalCustomFieldOption getCustomFieldOption(final String oldId)
    {
        return allOptionMap.get(oldId);
    }

    public Collection /*<ExternalCustomFieldOption>*/getParentOptions(final String configurationSchemeId)
    {
        final Collection collection = parentOptionMap.getCollection(configurationSchemeId);
        if (collection == null)
        {
            return Collections.EMPTY_LIST;
        }
        return collection;
    }

    public Collection /*<ExternalCustomFieldOption>*/getChildOptions(final String parentOptionId)
    {
        final Collection collection = childOptionMap.getCollection(parentOptionId);
        if (collection == null)
        {
            return Collections.EMPTY_LIST;
        }
        return collection;
    }

    public void flagValueAsRequired(final String oldCustomFieldOptionId)
    {
        super.flagValueAsRequired(oldCustomFieldOptionId);
    }
}
