/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.jql.context.QueryContext;

import java.util.Set;

@PublicApi
public interface FieldManager
{
    public static final String CUSTOM_FIELD_PREFIX = "customfield_";

    /**
     * Get a field by its id.
     * @param id An {@link com.atlassian.jira.issue.IssueFieldConstants} constant, or custom field key (eg. "customfield_10010")
     * @return the Field
     */
    public Field getField(String id);

    public boolean isCustomField(String id);

    public boolean isCustomField(Field field);

    /**
     * Get a CustomField by its text key (eg 'customfield_10000').
     * @param id Eg. 'customfield_10000'
     * @return The {@link CustomField} or null if not found.
     */
    public CustomField getCustomField(String id);

    public boolean isHideableField(String id);

    public boolean isHideableField(Field field);

    public HideableField getHideableField(String id);

    public boolean isOrderableField(String id);

    public boolean isOrderableField(Field field);

    public OrderableField getOrderableField(String id);

    public ConfigurableField getConfigurableField(String id);

    public Set<OrderableField> getOrderableFields();

    public Set<NavigableField> getNavigableFields();

    public boolean isNavigableField(String id);

    public boolean isNavigableField(Field field);

    public NavigableField getNavigableField(String id);

    public boolean isRequirableField(String id);

    public boolean isRequirableField(Field field);

    public boolean isMandatoryField(String id);

    public boolean isMandatoryField(Field field);

    public boolean isRenderableField(String id);

    public boolean isRenderableField(Field field);

    public boolean isUnscreenableField(String id);

    public boolean isUnscreenableField(Field field);

    public RequirableField getRequiredField(String id);

    /**
     * @return FieldLayoutManager
     * @deprecated Declare your dependency and let PicoContainer resolve it instead
     */
    @Deprecated
    public FieldLayoutManager getFieldLayoutManager();

    /**
     * @return ColumnLayoutManager
     * @deprecated Declare your dependency and let PicoContainer resolve it instead
     */
    @Deprecated
    public ColumnLayoutManager getColumnLayoutManager();

    /**
     * Invalidates <em>all field-related caches</em> in JIRA.
     * <font color="red"><h1>WARNING</h1></font>
     * This method invalidates a whole lot of JIRA caches, which means that JIRA performance significantly degrades
     * after this method has been called. For this reason, you should <b>avoid calling this method at all costs</b>.
     * <p/>
     * The correct approach to invalidate the cache entries is to do it in the "store" inside the {@code FooStore.updateFoo()}
     * method, where you can invalidate a <b>single</b> cache entry. If the cache lives in another class then the store
     * should raise a {@code FooUpdatedEvent} which that class can listen to in order to keep its caches up to date.
     * <p/>
     * If you add any calls to this method in JIRA I will hunt you down and subject you to a Spanish inquisition.
     */
    public void refresh();

    public Set<Field> getUnavailableFields();

    boolean isFieldHidden(User remoteUser, Field field);

    /**
     * Determines whether the field with id of fieldId is NOT hidden in AT LEAST one {@link com.atlassian.jira.issue.fields.layout.field.FieldLayout} that the user can see
     * (assigned to projects for which the user has the {@link com.atlassian.jira.security.Permissions#BROWSE} permission).
     *
     * @param remoteUser the remote user.
     * @param fieldId The Field ID
     */
    boolean isFieldHidden(User remoteUser, String fieldId);

    /**
     * Checks whether the given field is hidden in all of the given Field Layouts.
     * <p>
     * This method can be used in conjunction with {@link #getVisibleFieldLayouts(com.atlassian.crowd.embedded.api.User)}
     * to provide a more performant way of looking up {@link #isFieldHidden(com.atlassian.crowd.embedded.api.User, Field)} multiple times.
     * Instead, it is more efficient to do something like:
     * <pre>
     *     Set<FieldLayout> fieldLayouts = fieldManager.getVisibleFieldLayouts(loggedInUser);
     *     for (Field field: myFields)
     *     {
     *       if (fieldManager.isFieldHidden(fieldLayouts1, field))
     *       {
     *           ...
     *       }
     *     }
     * </pre>
     *
     * @param fieldLayouts The FieldLayouts to check
     * @param field The field to check
     * @return true if the given field is hidden in all of the given Field Layouts.
     *
     * @see #getVisibleFieldLayouts(com.atlassian.crowd.embedded.api.User)
     * @see #isFieldHidden(com.atlassian.crowd.embedded.api.User, Field)
     */
    boolean isFieldHidden(Set<FieldLayout> fieldLayouts, Field field);

    /**
     * Returns all the visible FieldLayouts for the given user.
     * <p>
     * This is used in conjunction with {@link #isFieldHidden(Set, Field)} as a performance optimisation in usages that want to call {@link #isFieldHidden(User, Field)} multiple times.
     * Instead, it is more efficient to do something like:
     * <pre>
     *     Set<FieldLayout> fieldLayouts = fieldManager.getVisibleFieldLayouts(loggedInUser);
     *     for (Field field: myFields)
     *     {
     *       if (fieldManager.isFieldHidden(fieldLayouts1, field))
     *       {
     *           ...
     *       }
     *     }
     * </pre>
     *
     * @param user the user whose project browsing permissions are used to limit visible FieldLayouts.
     * @return all the visible FieldLayouts for the given user.
     */
    Set<FieldLayout> getVisibleFieldLayouts(User user);

    /**
     * Gets all the available fields that the user can see, this is providing no context scope.
     *
     * @param user the remote user.
     * @return a set of NavigableFields that can be show because their visibility/configuration fall within what the
     * user can see.
     *
     * @throws FieldException thrown if there is a problem looking up the fields
     */
    public Set<NavigableField> getAvailableNavigableFieldsWithScope(User user) throws FieldException;

    /**
     * Gets all the available fields within the defined scope of the QueryContext.
     *
     * @param user the user making the request
     * @param queryContext the context of the search request.
     * @return a set of NavigableFields that can be show because their visibility/configuration fall within the specified
     * context
     *
     * @throws FieldException thrown if there is a problem looking up the fields
     */
    public Set<NavigableField> getAvailableNavigableFieldsWithScope(User user, QueryContext queryContext) throws FieldException;

    /**
     * Retrieves custom fields in scope for the given issue
     *
     * @param remoteUser Remote User
     * @param issue Issue
     * @throws FieldException if cannot retrieve the projects the user can see, or if cannot retrieve
     *                        the field layouts for the viewable projects
     * @return custom fields in scope for the given issue
     */
    public Set<CustomField> getAvailableCustomFields(User remoteUser, Issue issue) throws FieldException;

    public Set<NavigableField> getAllAvailableNavigableFields() throws FieldException;

    public Set<NavigableField> getAvailableNavigableFields(User remoteUser) throws FieldException;

    /**
     * Return all the searchable fields in the system. This set will included all defined custom fields.
     *
     * @return the set of all searchable fields in the system.
     */
    Set<SearchableField> getAllSearchableFields();

    /**
     * Return all the searchable systems fields. This set will *NOT* include defined custom fields.
     *
     * @return the set of all searchable systems fields defined.
     */
    Set<SearchableField> getSystemSearchableFields();

    // --------------------------------------------------------------------------------------------- Convenience Methods

    /**
     * Retrieve the IssueType system Field.
     * @return the IssueType system Field.
     */
    public IssueTypeField getIssueTypeField();

    /**
     * Retrieve the Project system Field.
     * @return the Project system Field.
     */
    public ProjectField getProjectField();

    boolean isTimeTrackingOn();
}
