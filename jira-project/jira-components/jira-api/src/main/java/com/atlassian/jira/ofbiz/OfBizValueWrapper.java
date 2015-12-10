package com.atlassian.jira.ofbiz;

import com.atlassian.annotations.PublicApi;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;

/**
 * The methods in this interface should not be used.
 * They are here for legacy compatibility with GenericValues
 *
 * @deprecated Use the Data object getters instead. Since v5.0.
 */
@PublicApi
public interface OfBizValueWrapper
{
    /**
     * Retrieve a String field.
     *
     * @param name the field name
     * @return the value for the given field.
     *
     * @deprecated Use the Data object getters instead. Since v5.0.
     */
    String getString(String name);

    /**
     * Retrieve a timestamp field.
     *
     * @param name the field name
     * @return the value for the given field.
     *
     * @deprecated Use the Data object getters instead. Since v5.0.
     */
    Timestamp getTimestamp(String name);

    /**
     * Retrieve a numeric field.
     *
     * @param name the field name
     * @return the value for the given field.
     *
     * @deprecated Use the Data object getters instead. Since v5.0.
     */
    Long getLong(String name);

    /**
     * Get the backing GenericValue object.
     *
     * @return the backing GenericValue object.
     *
     * @deprecated Use the Data object getters instead. Since v5.0.
     */
    GenericValue getGenericValue();

    /**
     * Persist this object's immediate fields.
     *
     * @deprecated Use the Object's Service or Manager to save values. Since v5.0.
     */
    void store();
}
