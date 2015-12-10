package com.atlassian.jira.issue.transport;

import com.atlassian.annotations.PublicApi;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * This is a field params with Lists as the value
 */
@PublicApi
public interface CollectionParams extends FieldParams
{
    /**
     * Return all values of all keys, flattened into a single collection.
     * Use {@link #getValuesForNullKey()} instead if, for example, you just need the values of the custom field.
     */
    Collection getAllValues();

    /**
     * Return the values of the custom field.
     * <p/>
     * The values associated with the null key represents the values of the custom field.
     * For example, the user selected in a single user picker, or the list of users selected in a multiple user picker.
     * <p/>
     * Note that unlike {@link #getAllValues()}, this method does not return values associated with other non-null keys.
     */
    Collection getValuesForNullKey();

    /**
     * Return the values associated with the given {@code key} in the parameters.
     * <p/>
     * Depending on the type of field, additional keys might be introduced in addition to the null key.
     * JIRA might also add additional keys into the parameters.
     * For example, issue id and project id might be passed into the parameters under separate keys during custom field validation.
     */
    Collection<String> getValuesForKey(@Nullable String key);

    /**
     * Put the values in.
     *
     * @param key for mapping
     * @param value a Collection of Strings.
     */
    void put(String key, Collection<String> value);
}
