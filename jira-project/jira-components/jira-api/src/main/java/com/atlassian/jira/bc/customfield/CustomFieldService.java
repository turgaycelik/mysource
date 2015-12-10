package com.atlassian.jira.bc.customfield;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Service front for the custom field manager.  Implementations of this interface are responsible for
 * carrying out any validation and permission logic required to carry out a certain task.  The actual
 * work required to do a certain task should be delegated to the {@link com.atlassian.jira.issue.CustomFieldManager}.
 *
 * @since v3.13
 */
@PublicApi
public interface CustomFieldService
{
    /**
     * Return the custom field if the passed user has permission to edit its configuration.
     *
     * @param user the user to check.
     * @param fieldId the field to search for.
     *
     * @return the custom field with the passed fieldId if the passed user has permission to edit its configuration
     *  and it actually exists.
     */
    ServiceOutcome<CustomField> getCustomFieldForEditConfig(@Nullable ApplicationUser user, String fieldId);

    /**
     * Return the {@link CustomFieldType}s that the passed user can use to create a new {@link CustomField}.
     *
     * A {@code CustomFieldType} can be hidden if the passed user either does not have permission to see it or when
     * the type is locked and not meant to be created by users.
     *
     * @param user the user to check against.
     * @return the {@code CustomFieldType}s that the passed user can use to create a {@code CustomField}.
     */
    @Nonnull
    Iterable<CustomFieldType<?,?>> getCustomFieldTypesForUser(@Nullable ApplicationUser user);

    /**
     * Return the default {@link CustomFieldSearcher} for the passed {@link CustomFieldType}. The default searcher can
     * be null if there is no searcher associated with the type.
     *
     * @param type the {@code CustomFieldType} to query.
     * @return the default searcher for the passed {@code CustomFieldType}. Can be null if the type has no associated
     * searcher.
     */
    @Nullable
    CustomFieldSearcher getDefaultSearcher(@Nonnull CustomFieldType<?,?> type);

    /**
     * Validates that the custom field with the provided id can be deleted.  This means we check whether
     * or not the custom field is used in any permission or issue level security schemes.  This method will also
     * check that the custom field with the given id exists. The user performing this operation needs to have
     * global admin permission.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who is performing the change and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors
     *                           in calling the method
     * @param customFieldId      the custom field id of the custom field about to be deleted.
     */
    void validateDelete(JiraServiceContext jiraServiceContext, Long customFieldId);

    /**
     * Validates that the custom field with the provided id can be updated.  This means we check whether
     * or not the custom field is used in any permission or issue level security schemes if the custom field's
     * searcher is being set to null.  This method will also check that the custom field with the given id exists
     * and that all its attributes are valid. The user performing this operation needs to have global admin permission.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who is performing the change and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors
     *                           in calling the method
     * @param customFieldId      the custom field id of the customfield about to be updated
     * @param name the updated name of the customfield
     * @param description the description of the customfield
     * @param searcherKey the customfield searcher that should be used
     */
    void validateUpdate(JiraServiceContext jiraServiceContext, Long customFieldId, String name, String description, String searcherKey);

    /**
     * Validates that the custom field with the provided data can be created. Data placeholder is the
     * {@link com.atlassian.jira.bc.customfield.CustomFieldDefinition}. The result of this operation is
     * {@link com.atlassian.jira.bc.customfield.CreateValidationResult} which after validation will contain all necessery data to create
     * a custom field (including user)
     *
     * @param user - the user who is performing the validation
     * @param customFieldDefinition - custom field data
     * @return validation result, which contains valid data to cre`ate a custom field. This should be passed to create method.
     */
    ServiceOutcome<CreateValidationResult> validateCreate(User user, CustomFieldDefinition customFieldDefinition);

    /**
     * Creates a custom field using a {@link com.atlassian.jira.bc.customfield.CreateValidationResult} as parameter.
     * CreateValidationResult is an output of createValidation method which should be executed before executing create.
     *
     * @param createValidationResult - data needed to create custom field, containing user.
     * @return ServiceOutcome with CustomField or with errorCollection
     * @throws DataAccessException throwed when there is a problem with creating custom field in database
     */
    ServiceOutcome<CustomField> create(CreateValidationResult createValidationResult) throws DataAccessException;

    /**
     * Adds a custom field with the given id to selected tabs. It returns list of ids of tabs on which custom field
     * is present after performing "add" operation.
     * @param user              user who performs the change
     * @param customFieldId     id of custom field
     * @param tabIds            list of tab's id's to which we want to add custom field
     * @return                  service outcome containing list of ids of tabs on which custom field is present after operation
     */
    ServiceOutcome<List<Long>> addToScreenTabs(User user, Long customFieldId, List<Long> tabIds);

    /**
     * Removes a custom field with the given id from selected tabs. It returns list of ids of tabs on which custom field
     * is present after performing "remove" operation.
     * @param user              user who performs the change
     * @param customFieldId     id of custom field
     * @param tabIds            list of tab's ids from which we want to remove custom field
     * @return                  service outcome containing list of ids of tabs on which custom field is present after operation
     */
    ServiceOutcome<List<Long>> removeFromScreenTabs(User user, Long customFieldId, List<Long> tabIds);

    /**
     * Validates that the parameters to set a translation for a custom field are valid
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who is performing the change and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors
     *                           in calling the method
     * @param customFieldId      the custom field id of the customfield about to be updated
     * @param name the updated name of the customfield
     * @param description the description of the customfield
     * @param locale the locale of the translation
     */
    void validateTranslation(JiraServiceContext jiraServiceContext, Long customFieldId, String name, String description, String locale);

    /**
     * Sets the current a translation for a custom field.  The name and description can be empty and if so the translation will be cleared.
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who is performing the change and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors
     *                           in calling the method
     * @param customFieldId      the custom field id of the customfield about to be updated
     * @param name the updated name of the customfield
     * @param description the description of the customfield
     * @param locale the locale of the translation
     */
    void updateTranslation(JiraServiceContext jiraServiceContext, Long customFieldId, String name, String description, String locale);
}
