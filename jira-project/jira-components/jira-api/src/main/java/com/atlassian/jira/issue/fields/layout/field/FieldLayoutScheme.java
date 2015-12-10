package com.atlassian.jira.issue.fields.layout.field;

import java.util.Collection;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.NamedWithDescription;
import com.atlassian.jira.util.NamedWithId;

import org.ofbiz.core.entity.GenericValue;

/**
 * FieldLayoutScheme is the Java Data Object representing what is called a "Field Configuration Scheme" in the UI.
 *
 * <p> A Field Configuration Scheme maps each Issue Type to a "Field Configuration" ({@link FieldLayoutSchemeEntity}).
 * A Field Configuration defines for each field if it is required or not, whether it is visible or hidden, and what
 * "Screens" it will appear on. (The Screen defines the order the fields are shown in, and can define multiple tabs).
 */
@PublicApi
public interface FieldLayoutScheme extends NamedWithDescription, NamedWithId
{
    Long getId();

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    GenericValue getGenericValue();

    void setGenericValue(GenericValue genericValue);

    void store();

    /**
     * Returns the id of the field layout to use for this given issue type id. This will do all the necessary work to
     * lookup the default entry if no specific mapping for the given isuse type id exists. So after calling this method
     * simply use the returned field layout id.
     *
     * @param issueTypeId the Issue Type ID.
     * @return the id of the {@link FieldLayout} ("Field Configuration") to use for this given issue type id.
     */
    Long getFieldLayoutId(String issueTypeId);

    void addEntity(FieldLayoutSchemeEntity fieldLayoutSchemeEntity);

    void removeEntity(String issueTypeId);

    Collection<FieldLayoutSchemeEntity> getEntities();

    void remove();

    FieldLayoutSchemeEntity getEntity(String issueTypeId);

    FieldLayoutSchemeEntity getEntity(EditableFieldLayout editableFieldLayout);

    Collection<GenericValue> getProjects();

    boolean containsEntity(String issueTypeId);
}
