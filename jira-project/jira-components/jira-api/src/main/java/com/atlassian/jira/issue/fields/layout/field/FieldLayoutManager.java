package com.atlassian.jira.issue.fields.layout.field;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;

import org.ofbiz.core.entity.GenericValue;

// TODO introduce project object (or id) alternatives to methods that take GVs

/**
 * The FieldLayoutManager is responsible for managing field configurations and field configuration schemes.
 */
@PublicApi
public interface FieldLayoutManager
{
    /**
     * JIRA must have a default field layout. This is used  identify the default layout.
     */
    public static final String TYPE_DEFAULT = "default";

    /**
     * Persists a new field Layout scheme (i.e Field Configuration Scheme).
     *
     * @param fieldLayoutScheme The {@link FieldLayoutScheme} to persist.
     * @return The stored {@link FieldLayoutScheme} object
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    public FieldLayoutScheme createFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme);

    /**
     * Persists a new field Layout scheme (i.e Field Configuration Scheme).
     */
    public FieldLayoutScheme createFieldLayoutScheme(@Nonnull String name, @Nullable String description);

    /**
     * Copies an existing field layout to a new one.
     */
    public FieldLayoutScheme copyFieldLayoutScheme(@Nonnull FieldLayoutScheme scheme, @Nonnull String name, @Nullable String description);

    /**
     * Retrieves a {@link FieldConfigurationScheme} by id
     *
     * @param schemeId FieldConfigurationScheme ID
     * @return A {@link FieldConfigurationScheme} instance
     * @throws com.atlassian.jira.exception.DataAccessException If there is a DB exception.
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId);

    /**
     * Retrieves a mutable {@link FieldLayoutScheme} by id.
     *
     *
     * <p>This returns a new copy of the object from the DB, and so will incur some performance penalty.
     * Please use {@link #getFieldConfigurationScheme(Long)} for access to a cached immutable scheme object.
     *
     * @param schemeId the scheme ID
     * @return A {@link FieldLayoutScheme} instance
     * @throws com.atlassian.jira.exception.DataAccessException if an error occurs in the DB layer
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    public FieldLayoutScheme getMutableFieldLayoutScheme(Long schemeId);

    /**
     * Checks if a FieldConfigurationScheme with the given name exists.
     *
     * @param schemeName The scheme name
     * @return {@code true} if a FieldConfigurationScheme with the given name exists.
     * @throws com.atlassian.jira.exception.DataAccessException If there is a DB error.
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    public boolean fieldConfigurationSchemeExists(String schemeName);

    /**
     * Retrieves editable (see {@link EditableFieldLayout} versions of the field layouts.
     * <b>Note:</b> For standard edition this simply returns an editable version of the default field
     * layout. (see {@link EditableDefaultFieldLayout})
     *
     * @return A list of {@link EditableFieldLayout} and {@link EditableDefaultFieldLayout}
     */
    public List<EditableFieldLayout> getEditableFieldLayouts();

    /**
     * Retries Field Configuration Schemes.  These are used to link field configurations to projects.
     *
     * @return A list of {@link FieldLayoutScheme}s.
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    public List<FieldLayoutScheme> getFieldLayoutSchemes();

    /**
     * Persists the {@link FieldLayoutScheme} supplied.
     *
     * @param fieldLayoutScheme The FieldLayoutScheme
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    public void updateFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme);

    /**
     * Removes the {@link FieldLayoutScheme} supplied.
     *
     * @param fieldLayoutScheme The FieldLayoutScheme
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    public void deleteFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme);

    /**
     * Retrieves the Field Configuration Scheme associated with the supplied project.
     *
     * @param project A project
     * @return A {@link FieldConfigurationScheme} or null if none exists.
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     * @throws UnsupportedOperationException If this is executed against standard edition
     * @throws IllegalArgumentException If the project supplied is null.
     */
    public FieldConfigurationScheme getFieldConfigurationScheme(Project project);

    /**
     * Retrieves the Field Configuration Scheme associated with the supplied project.
     *
     * @param project A project {@link GenericValue}
     * @return A {@link FieldConfigurationScheme} or null if none exists.
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     * @throws UnsupportedOperationException If this is executed against standard edition
     * @throws IllegalArgumentException If the project supplied is null.
     *
     * @deprecated Use {@link #getFieldConfigurationScheme(com.atlassian.jira.project.Project)} instead. Since v4.3
     */
    @Nullable
    public FieldConfigurationScheme getFieldConfigurationScheme(GenericValue project);

    /**
     * Retrieves all the {@link com.atlassian.jira.issue.fields.layout.field.FieldLayout}'s for a project.
     *
     * @param project a project
     *
     * @return the unique set of FieldLayout's for the provided project, an empty set if there are none.
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     */
    public Set<FieldLayout> getUniqueFieldLayouts(Project project);

    /**
     * Associates a {@link FieldLayoutScheme} to the project supplied.
     *
     * @param project           A project {@link GenericValue}
     * @param fieldLayoutSchemeId ID of the FieldLayoutScheme
     * @throws com.atlassian.jira.exception.DataAccessException If there is an error in the DB layer
     * @throws UnsupportedOperationException If this is executed against standard edition
     * @throws IllegalArgumentException If the project supplied is null.
     *
     * @deprecated Use {@link #addSchemeAssociation(com.atlassian.jira.project.Project, Long)} instead. Since v5.2.
     */
    public void addSchemeAssociation(GenericValue project, Long fieldLayoutSchemeId);

    /**
     * Removes an association between a particular project and field configuration scheme.
     *
     * @param project           A project {@link GenericValue}
     * @param fieldLayoutSchemeId The fieldLayoutScheme ID
     * @throws UnsupportedOperationException If this is executed against standard edition
     * @throws com.atlassian.jira.exception.DataAccessException If there is an error in the DB layer.
     *
     * @deprecated Use {@link #removeSchemeAssociation(com.atlassian.jira.project.Project, Long)} instead. Since v5.2.
     */
    public void removeSchemeAssociation(GenericValue project, Long fieldLayoutSchemeId);

    /**
     * Associates a {@link FieldLayoutScheme} to the project supplied.
     *
     * @param project             The project
     * @param fieldLayoutSchemeId ID of the FieldLayoutScheme
     * @throws IllegalArgumentException If the project supplied is null.
     */
    public void addSchemeAssociation(Project project, Long fieldLayoutSchemeId);

    /**
     * Removes an association between a particular project and field configuration scheme.
     *
     * @param project             The project
     * @param fieldLayoutSchemeId The fieldLayoutScheme ID
     */
    public void removeSchemeAssociation(Project project, Long fieldLayoutSchemeId);

    /**
     * Used to retrieve {@link FieldLayout} information when rendering a screen.
     *
     * @return the default FieldLayout
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     */
    public FieldLayout getFieldLayout();

    /**
     * Used to retrieve {@link FieldLayout} information when rendering a screen given the id
     * of the field layout.  If the ID is null, the default layout is returned.
     *
     * @param id The FieldLayout ID.
     * @return The {@link FieldLayout}
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    public FieldLayout getFieldLayout(Long id);

    /**
     * Returns the fieldLayout for an issue.
     *
     * @param issue An issue {@link GenericValue}
     * @return A {@link FieldLayout}
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     *
     * @deprecated Use {@link #getFieldLayout(com.atlassian.jira.issue.Issue)} instead. Since v5.0.
     */
    public FieldLayout getFieldLayout(GenericValue issue);

    /**
     * Returns the fieldLayout for an issue.
     *
     * @param issue An {@link Issue}
     * @return A {@link FieldLayout}
     */
    public FieldLayout getFieldLayout(Issue issue);

    /**
     * @param project     A project
     * @param issueTypeId The IssueType id of the issue.
     * @return A {@link FieldLayout}
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     * @see #getFieldLayout(Issue)
     */
    public FieldLayout getFieldLayout(Project project, String issueTypeId);

    /**
     * @param project     the Project
     * @param issueTypeId The IssueType id of the issue.
     * @return A {@link FieldLayout}
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     *
     * @deprecated Use {@link #getFieldLayout(com.atlassian.jira.project.Project, String)} instead. Since v4.3
     */
    public FieldLayout getFieldLayout(GenericValue project, String issueTypeId);

    /**
     * Returns the default {@link EditableDefaultFieldLayout}.
     *
     * @return the default {@link EditableDefaultFieldLayout}.
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     */
    public EditableDefaultFieldLayout getEditableDefaultFieldLayout();

    /**
     * Persist the given default {@link EditableDefaultFieldLayout}
     *
     * @param editableDefaultFieldLayout The EditableDefaultFieldLayout.
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     */
    public void storeEditableDefaultFieldLayout(EditableDefaultFieldLayout editableDefaultFieldLayout);

    /**
     * Persists the {@link EditableFieldLayout} provided.
     *
     * @param editableFieldLayout the EditableFieldLayout.
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     */
    public void storeEditableFieldLayout(EditableFieldLayout editableFieldLayout);

    /**
     * Persists the {@link EditableFieldLayout} provided and returns a new instance as stored
     * in the database.
     *
     * @param editableFieldLayout the EditableFieldLayout.
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     * @return the layout as stored in the database.
     */
    EditableFieldLayout storeAndReturnEditableFieldLayout(EditableFieldLayout editableFieldLayout);

    /**
     * This method can be used to rollback any changes to the default field configuration.
     *
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     */
    public void restoreDefaultFieldLayout();

    /**
     * Restores the field layout associated with the provided scheme to defaults.  Essentially
     * this involves removing all previously configured custom items.
     *
     * @param scheme A scheme {@link GenericValue}
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     * @throws IllegalArgumentException if the scheme passes is null.
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    public void restoreSchemeFieldLayout(GenericValue scheme);

    /**
     * Checks to see if a {@link FieldLayout} entity with type {@link #TYPE_DEFAULT} exists
     *
     * @return True if a default {@link FieldLayout} exists, false otherwise.
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     */
    public boolean hasDefaultFieldLayout();

    /**
     * Returns all projects that use the given FieldConfigurationScheme.
     *
     * @param fieldConfigurationScheme the FieldConfigurationScheme
     * @return A list of projects that use the given FieldConfigurationScheme.
     */
    public Collection<GenericValue> getProjects(FieldConfigurationScheme fieldConfigurationScheme);

    /**
     * Returns all associated projects for the {@link FieldLayoutScheme} supplied.
     *
     * @param fieldLayoutScheme the FieldLayoutScheme
     * @return A list of project {@link GenericValue}s
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    public Collection<GenericValue> getProjects(FieldLayoutScheme fieldLayoutScheme);

    /**
     * Clears all local caches.
     */
    public void refresh();

    /**
     * Returns an {@link EditableFieldLayout} for the id supplied.
     *
     * @param id If the id is NULL, the default layout is returned.
     * @return An {@link EditableFieldLayout}
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    EditableFieldLayout getEditableFieldLayout(Long id);

    /**
     * Deletes a custom {@link FieldLayout}
     *
     * @param fieldLayout The FieldLayout
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    void deleteFieldLayout(FieldLayout fieldLayout);

    /**
     * Returns a collection of {@link FieldLayoutSchemeEntity}s.  These are used to
     * record mappings from {@link com.atlassian.jira.issue.issuetype.IssueType} -> {@link FieldLayout}
     * for the {@link FieldLayoutScheme} passed in.
     *
     * @param fieldLayoutScheme The FieldLayoutScheme
     * @return A collection of {@link FieldLayoutSchemeEntity}s.
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    Collection<FieldLayoutSchemeEntity> getFieldLayoutSchemeEntities(FieldLayoutScheme fieldLayoutScheme);

    /**
     * Persists a new {@link FieldLayoutSchemeEntity} for a particular {@link FieldLayoutScheme}.
     * The appropriate scheme is retrieved using {@link com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity#getFieldLayoutScheme()}
     *
     * @param fieldLayoutSchemeEntity The FieldLayoutSchemeEntity
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    void createFieldLayoutSchemeEntity(FieldLayoutSchemeEntity fieldLayoutSchemeEntity);

    /**
     * Persists a new {@link FieldLayoutSchemeEntity} for a particular {@link FieldLayoutScheme}.
     * The appropriate scheme is retrieved using {@link com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity#getFieldLayoutScheme()}
     *
     *
     * @param fieldLayoutScheme
     * @param issueTypeId
     * @param fieldConfigurationId
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    FieldLayoutSchemeEntity createFieldLayoutSchemeEntity(FieldLayoutScheme fieldLayoutScheme, String issueTypeId, Long fieldConfigurationId);

    /**
     * Updates a {@link FieldLayoutSchemeEntity}.
     *
     * @param fieldLayoutSchemeEntity The FieldLayoutSchemeEntity
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    void updateFieldLayoutSchemeEntity(FieldLayoutSchemeEntity fieldLayoutSchemeEntity);

    /**
     * Removes a {@link FieldLayoutSchemeEntity}
     *
     * @param fieldLayoutSchemeEntity The FieldLayoutSchemeEntity
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    void removeFieldLayoutSchemeEntity(FieldLayoutSchemeEntity fieldLayoutSchemeEntity);

    /**
     *
     * @param fieldLayoutScheme The FieldLayoutScheme
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    void removeFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme);

    /**
     * Returns a collection of {@link FieldConfigurationScheme}s that include the given {@link FieldLayout}.
     * <p> This is determined by retrieving all {@link FieldLayoutSchemeEntity}s with the {@link FieldLayout} and
     * calculating a set of {@link FieldConfigurationScheme}s using these entities.
     *
     * @param fieldLayout The FieldLayout.
     * @return A collection of {@link FieldConfigurationScheme}s
     * @throws UnsupportedOperationException If this is executed against standard edition
     */
    Collection<FieldConfigurationScheme> getFieldConfigurationSchemes(FieldLayout fieldLayout);

    /**
     * Finds all projects that use the given {@link FieldLayout} (via the configured {@link FieldConfigurationScheme}).
     * <p/>
     * <b>NOTE:</b> In the case of Standard & Professional, this simply returns ALL projects, as the only
     * fieldlayout is the default field layout.
     *
     * @param fieldLayout The FieldLayout.
     * @return The set of Projects that use the given FieldLayout.
     */
    Collection<GenericValue> getRelatedProjects(FieldLayout fieldLayout);

    /**
     * Will determine whether or not two {@link com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme}s are
     * "visibly equivalent".
     * <p>
     * This can be useful for determining if swapping one field layout scheme for another in a project
     * will have any affect on the visibility of fields in the project's issues. For instance, this will let us know
     * if we need to re-index or not.
     * <p>
     * Since a scheme is a mapping from issue types to field layouts, two schemes are visibly equivalent if:
     * <ul>
     * <li>All issue types map to the same field layouts; or else
     * <li>The field layouts that an issue type is associated with in each scheme are visibly equivalent
     * </ul>
     * It is possible that one scheme may have a mapping for an issue type, but the other does not. In this case,
     * equivalence is compared between the issue type specific one and the default field layout.
     * <p>
     * Note that equivalence is reflexive - all the associations in scheme1 must have equivalent associations in scheme2,
     * and vice versa.
     *
     * @param fieldConfigurationSchemeId1 the first scheme; can use <code>null</code> for the system default scheme
     * @param fieldConfigurationSchemeId2 the second scheme; can use <code>null</code> for the system default scheme
     * @return the result of the equivalence comparison
     * @throws com.atlassian.jira.exception.DataAccessException If there is a Data Layer error.
     */
    boolean isFieldLayoutSchemesVisiblyEquivalent(Long fieldConfigurationSchemeId1, Long fieldConfigurationSchemeId2);
    /**
     * Will determine whether or not two {@link com.atlassian.jira.issue.fields.layout.field.FieldLayout}s are
     * "visibly equivalent".
     * <p>
     * This can be useful for determining if swapping one field layout for another in a project
     * will have any affect on the visibility of fields in the project's issues. For instance, this will let us know
     * if we need to re-index or not.
     * <p>
     * Two field layouts are visibly equivalent if:
     * <ul>
     * <li>They contain the same fields, and
     * <li>Each field has the same shown/hidden flag
     * </ul>
     * Note that equivalence is reflexive: layout1 == layout2 implies layout2 == layout1.
     *
     * @param fieldLayoutId1 the first layout id; null signifies the default field layout in the system
     * @param fieldLayoutId2 the second layout id; null signifies the default field layout in the system
     * @return the result of the equivalence comparison
     */
    boolean isFieldLayoutsVisiblyEquivalent(Long fieldLayoutId1, Long fieldLayoutId2);

}
