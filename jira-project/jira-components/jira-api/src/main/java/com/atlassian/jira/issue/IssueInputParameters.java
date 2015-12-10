package com.atlassian.jira.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;

import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * This represents an issue builder and can be used to provide parameters that can be used to create and update
 * an issue through the {@link com.atlassian.jira.bc.issue.IssueService}.
 *
 * <p/>
 * Any fields that are set on this object will have the values used for validation and population of the issue
 * object created via the validate methods on the IssueService. This object is forgiving as possible so if you
 * try to set a null value for a field no exceptions will be thrown, the value will just be ignored.
 *
 * <p/>
 * Some users might want to provide a FieldValuesHolder to the validation calls with some pre-populated data, if this is
 * the case then you can set the field values holder on this object and the IssueService will clone the contents and
 * pass them along to any field invocations it might make.
 *
 * <p/>
 * If you wish to indicate to the IssueService that you do not want to validate against all fields configured for
 * the create screen then you can set the {@link #setProvidedFields(java.util.Collection)} with the field
 * id's that you want to have validated. All other fields that exist on the screen will be populated with the system
 * default.
 *
 * <p/>
 * To construct a new IssueInputParameters object you can call {@link com.atlassian.jira.bc.issue.IssueService#newIssueInputParameters()}.
 *
 * <p/>
 * NOTE: this object is not thread-safe and is only meant to be used as a transport object.
 *
 * @since v4.1
 * @see com.atlassian.jira.bc.issue.IssueService#newIssueInputParameters()
 * @see com.atlassian.jira.bc.issue.IssueService#newIssueInputParameters(java.util.Map)
 */
@PublicApi
public interface IssueInputParameters
{

    /**
     * @return true if the issues value should be retained when a parameter has not been provided, false if the
     * missing parameter should be treated as an update.
     */
    boolean retainExistingValuesWhenParameterNotProvided();

    /**
     * Flag to designate whether existing values should be retained when no parameter is provided.
     * <p/>
     * Note: setting this flag here will assume that validation of fields should still occur, even if they were not specified.
     * To change this, please use {@link #setRetainExistingValuesWhenParameterNotProvided(boolean, boolean)} instead.
     *
     * @param retain true if non-provided parameters should have the values retained, false otherwise.
     * @see #retainExistingValuesWhenParameterNotProvided()
     * @see #setRetainExistingValuesWhenParameterNotProvided(boolean, boolean)
     */
    void setRetainExistingValuesWhenParameterNotProvided(boolean retain);

    /**
     * Set flags {@link #retainExistingValuesWhenParameterNotProvided()} and {@link #onlyValidatePresentFieldsWhenRetainingExistingValues()}.
     * <p/>
     * The latter is only relevant when the former is true.
     *
     * @see #onlyValidatePresentFieldsWhenRetainingExistingValues()
     * @see com.atlassian.jira.bc.issue.IssueService#validateUpdate(com.atlassian.crowd.embedded.api.User, Long, IssueInputParameters)
     * @since v5.0.2
     * @param retainExistingValues true if non-provided parameters should have the values retained, false otherwise.
     * @param onlyValidatePresentFields whether or not to only validate present fields
     */
    void setRetainExistingValuesWhenParameterNotProvided(boolean retainExistingValues, boolean onlyValidatePresentFields);

    /**
     * Get the flag to designate which fields are to be validated when the {@link #retainExistingValuesWhenParameterNotProvided()}
     * flag is set to <code>true</code>.
     * <p/>
     * If this flag is <code>true</code>, only fields which are "present" will take part in validation (and subsequently
     * update) on the issue. This allows single-field edits to ignore the validation of other fields on the issue.
     * <p/>
     * If this flag is <code>false</code>, or if {@link #retainExistingValuesWhenParameterNotProvided()} is <code>false</code>,
     * then all fields on the screen of the current operation will partake in the "validation-update" step.
     *
     * @see com.atlassian.jira.bc.issue.IssueService#validateUpdate(com.atlassian.crowd.embedded.api.User, Long, IssueInputParameters)
     * @since v5.0.2
     * @return whether or not to only validate present fields
     */
    boolean onlyValidatePresentFieldsWhenRetainingExistingValues();

    /**
     * @return true if default value should be supplied when a parameter has not been provided. false if the
     * default value should not be applied.
     */
    boolean applyDefaultValuesWhenParameterNotProvided();

    /**
     * @param applyDefaults true if default value should be supplied when a parameter has not been provided. false if the
     * default value should not be applied.
     */
    void setApplyDefaultValuesWhenParameterNotProvided(boolean applyDefaults);

    /**
     * Get the flag to skip screen checks during create or update.
     * <p/>
     * By default, the Issue Service only allows setting values to fields which appear on the screen of the operation which you
     * are performing (e.g. the Edit screen). By enabling this flag, the Issue Service will skip this check.
     *
     * @see com.atlassian.jira.bc.issue.IssueService#validateUpdate(com.atlassian.crowd.embedded.api.User, Long, IssueInputParameters)
     * @see com.atlassian.jira.bc.issue.IssueService#validateCreate(com.atlassian.crowd.embedded.api.User, IssueInputParameters)
     * @since v5.0 for editing, v6.3 for creating
     * @return whether or not we are skipping screen check
     */
    public boolean skipScreenCheck();

    /**
     * Set the flag to skip screen checks during create or update.
     * <p/>
     * By default, the Issue Service only allows setting values to fields which appear on the screen of the operation which you
     * are performing (e.g. the Edit screen). By enabling this flag, the Issue Service will skip this check.
     *
     * @see com.atlassian.jira.bc.issue.IssueService#validateUpdate(com.atlassian.crowd.embedded.api.User, Long, IssueInputParameters)
     * @see com.atlassian.jira.bc.issue.IssueService#validateCreate(com.atlassian.crowd.embedded.api.User, IssueInputParameters)
     * @since v5.0 supports for editing, v6.3 for creating
     * @param skipScreenCheck whether or not we are skipping screen check
     */
    void setSkipScreenCheck(boolean skipScreenCheck);

    /**
     * @param customFieldId uniquely identifies the custom field value you are looking for.
     * @return the values for the provided custom field if they exist, null otherwise.
     */
    String[] getCustomFieldValue(Long customFieldId);

    /**
     * @param fullCustomFieldKey identifies the custom field values you are looking for.
     * @return the values for the provided custom field if they exist, null otherwise.
     */
    String[] getCustomFieldValue(String fullCustomFieldKey);

    /**
     * Adds a value for a custom field with the specified id. This will put a value in the web-style parameters
     * with a key of the form "customfield_PROVIDED_ID". If you want to specify the key then use the method
     * {@link #addCustomFieldValue(String, String...)}.
     *
     * @param customFieldId the unique identifier of the custom field.
     * @param values the custom field values, must be in the format the field expects.
     * @return this object.
     */
    IssueInputParameters addCustomFieldValue(Long customFieldId, String... values);

    /**
     * Adds a value for a custom field with the specified full key. This will put a value in the web-style parameters
     * with a key as provided.
     *
     * @param fullCustomFieldKey used in the "web-style" parameters as the key, the custom field should expect this
     * value as the key in its populate from parameters method.
     * @param values the custom field values, must be in the format the field expects.
     * @return this object.
     */
    IssueInputParameters addCustomFieldValue(String fullCustomFieldKey, String... values);

    /**
     * @return the comment string value if it has been set, null otherwise.
     */
    String getCommentValue();
    
    /**
     * Set a comment value with no visibility restrictions.
     *
     * @param comment the comment value.
     * @return this object.
     */
    IssueInputParameters setComment(String comment);

    /**
     * Set a comment value with a project role restriction.
     *
     * @param comment the comment value.
     * @param projectRoleId the id of the project role the comment must be restricted by.
     * @return this object.
     */
    IssueInputParameters setComment(String comment, Long projectRoleId);

    /**
     * Set a comment value with a group restriction.
     *
     * @param comment the comment value.
     * @param groupId the group name that the comment must be restricted by.
     * @return this object.
     */
    IssueInputParameters setComment(String comment, String groupId);

    /**
     * This provides the "web-style" parameters that JIRA fields expect to perform their functions.
     *
     * @return this is a raw map of "web" style input parameters. These parameters are used to allow the
     * fields to attain the user inputted values. This map is of the style: &lt;field_name&gt; &lt;String [] {value}&gt;.
     */
    Map<String, String[]> getActionParameters();

    /**
     * If set the validation will use the seed values from this field values holder.
     *
     * @param fieldValuesHolder provides the seed values for the field values holder.
     */
    void setFieldValuesHolder(final Map<String, Object> fieldValuesHolder);

    /**
     * @return the provided field values holder or an empty map, never null.
     */
    Map<String, Object> getFieldValuesHolder();

    /**
     * @return the field id's that should be validated, null if not specified.
     */
    Collection<String> getProvidedFields();

    /**
     * Use this to indicate which fields validation should be performed on, if left alone then the systems configured
     * fields for either create/update will be used.
     *
     * @param providedFields a collection of {@link com.atlassian.jira.issue.fields.Field#getId()}'s which identify the
     * fields.
     */
    void setProvidedFields(final Collection<String> providedFields);

    /**
     * @param fieldId identifies the field in question, this will be the {@link com.atlassian.jira.issue.fields.Field#getId()}.
     * @return true if a non-null, non-empty value has been set for the field, false if not.
     */
    boolean isFieldSet(String fieldId);

    /**
     * @param fieldId identifies the field in question, this will be the {@link com.atlassian.jira.issue.fields.Field#getId()}.
     * @return true if the field has been set, even with an empty value, false if not.
     */
    boolean isFieldPresent(String fieldId);

    /**
     * Extra fields to consider present, even if they are not in the actionParams. When
     * "setRetainExisitingValuesWhenParameterNotProvided" is true, a field is not considered for update if
     * its parameter is not provided. You can force a field to be considered even if its parameter is not
     * providfed by forcing it to be considered by this setter.
     * <p/>
     * The action post params can also contain an array of fieldIds under the "fieldsToForcePresent" param.
     * <p/>
     * This may be useful if you want to clear a field's value in a single field edit.
     *
     * @since 5.0.2
     * @param fieldId the id of the field to force to be present
     */
    void addFieldToForcePresent(String fieldId);

    /**
     * @param projectId sets the project id for the issue.
     * @return this object.
     */
    IssueInputParameters setProjectId(Long projectId);

    /**
     * @return the set project id, null if one does not exist or if it is not a valid number.
     */
    Long getProjectId();

    /**
     * @param issueTypeId sets the issue type for the issue.
     * @return this object.
     */
    IssueInputParameters setIssueTypeId(String issueTypeId);

    /**
     * @return the set issue type id, null if one does not exist.
     */
    String getIssueTypeId();

    /**
     *
     * @param priorityId sets the priority for the issue.
     * @return this object.
     */
    IssueInputParameters setPriorityId(String priorityId);

    /**
     * @return the set priority id, null if one does not exist.
     */
    String getPriorityId();

    /**
     * @param resolutionId sets the resolution for the issue.
     * @return this object.
     */
    IssueInputParameters setResolutionId(String resolutionId);

    /**
     * @return the set resolution id, null if one does not exist.
     */
    String getResolutionId();

    /**
     * @param statusId sets the status of the issue.
     * @return this object.
     */
    IssueInputParameters setStatusId(String statusId);

    /**
     * @return the set status id, null if one does not exist.
     */
    String getStatusId();

    /**
     * @param summary sets the summary for the issue.
     * @return this object.
     */
    IssueInputParameters setSummary(String summary);

    /**
     * @return the set summary, null if one does not exist.
     */
    String getSummary();

    /**
     * @param description sets the description for the issue.
     * @return this object.
     */
    IssueInputParameters setDescription(String description);

    /**
     * @return the set description, null if one does not exist.
     */
    String getDescription();

    /**
     * @param environment sets the environment of the issue.
     * @return this object.
     */
    IssueInputParameters setEnvironment(String environment);

    /**
     * @return the set environment, null if one does not exist.
     */
    String getEnvironment();

    /**
     * @param assigneeId sets the assignee id for the issue.
     * @return this object.
     */
    IssueInputParameters setAssigneeId(String assigneeId);

    /**
     * @return the set assginee, null if one does not exist.
     */
    String getAssigneeId();

    /**
     * @param reporterId sets the reporter id for the issue.
     * @return this object.
     */
    IssueInputParameters setReporterId(String reporterId);

    /**
     * @return the set reporter, null if one does not exist.
     */
    String getReporterId();

    /**
     * @param componentIds sets the components id's on the issue.
     * @return this object.
     */
    IssueInputParameters setComponentIds(Long... componentIds);

    /**
     * @return the set component id's, null if they do not exist or if they are not valid numbers.
     */
    Long [] getComponentIds();

    /**
     * @param fixVersionIds sets the fix version id's on the issue.
     * @return this object.
     */
    IssueInputParameters setFixVersionIds(Long... fixVersionIds);

    /**
     * @return the set fix version id's, null if they do not exist or if they are not valid numbers.
     */
    Long [] getFixVersionIds();

    /**
     * @param affectedVersionIds sets the affected version id's= on the issue.
     * @return this object.
     */
    IssueInputParameters setAffectedVersionIds(Long... affectedVersionIds);

    /**
     * @return the set affected version id's, null if they do not exist or if they are not valid numbers.
     */
    Long [] getAffectedVersionIds();

    /**
     * @param dueDate the formatted string that JIRA will accept as a date that will be set on the issue.
     * @return this object.
     */
    IssueInputParameters setDueDate(String dueDate);

    /**
     * @return the set due date, null if one does not exist.
     */
    String getDueDate();

    /**
     * @param resolutionDate the formatted string that JIRA will accept as a date that will be set on the issue.
     * @return this object.
     */
    IssueInputParameters setResolutionDate(String resolutionDate);

    /**
     * @return the set resolution date, null if one does not exist.
     */
    String getResolutionDate();

    /**
     * @param securityLevelId sets the security level id on the issue.
     * @return this object.
     */
    IssueInputParameters setSecurityLevelId(Long securityLevelId);

    /**
     * @return the set security level id, null if one does not exist or it is not a valid number.
     */
    Long getSecurityLevelId();

    /**
     * Sets the original estimate on the issue.
     *
     * If ({@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR} is set,
     * we are in legacy time tracking mode and this method behaves differently.
     *
     * See http://confluence.atlassian.com/display/JIRA044/Configuring+Time+Tracking#ConfiguringTimeTracking-legacymode
     * for more information on legacy mode.
     *
     * <ul>
     *   <li>In legacy mode, this will change the original and remaining estimate if work has not yet started.
     *       If work has already been logged, it will alter the remaining estimate but leave the original estimate unchanged </li>
     *   <li>In modern mode, this will <b>only</b> change the original estimate. If you want to change both in a single request
     *       use {@link #setOriginalAndRemainingEstimate(String, String)} / {@link #setOriginalAndRemainingEstimate(Long, Long)}.
     *
     *       Calls to {@link #setOriginalEstimate(Long)} / {@link #setOriginalEstimate(String)} and {@link #setRemainingEstimate(Long)} /
     *       {@link #setRemainingEstimate(String)} are mutually exclusive, so calling both on the same instance will
     *       not produce the desired effect.</li>
     * </ul>
     *
     * @param originalEstimate the new original estimate on the issue. The units used are determined by
     *        {@link com.atlassian.jira.config.properties.APKeys#JIRA_TIMETRACKING_DEFAULT_UNIT}.
     * @return this object.
     */
    IssueInputParameters setOriginalEstimate(Long originalEstimate);

    /**
     * Sets the original estimate on the issue.
     *
     * If ({@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR} is set,
     * we are in legacy time tracking mode and this method behaves differently.
     *
     * See http://confluence.atlassian.com/display/JIRA044/Configuring+Time+Tracking#ConfiguringTimeTracking-legacymode
     * for more information on legacy mode.
     *
     * <ul>
     *   <li>In legacy mode, this will change the original and remaining estimate if work has not yet started.
     *       If work has already been logged, it will alter the remaining estimate but leave the original estimate unchanged </li>
     *   <li>In modern mode, this will <b>only</b> change the original estimate. If you want to change both in a single request
     *       use {@link #setOriginalAndRemainingEstimate(String, String)}. Calls to {@link #setOriginalEstimate(Long)} /
     *       {@link #setOriginalEstimate(String)} and {@link #setRemainingEstimate(Long)} / {@link #setRemainingEstimate(String)}
     *       are mutually exclusive, so calling both on the same instance will not produce the desired effect.</li>
     * </ul>
     *
     * @param originalEstimate the new original estimate on the issue, e.g. "1d 5h" for 1 day and 5 hours
     * @return this object.
     */
    IssueInputParameters setOriginalEstimate(String originalEstimate);

    /**
     * Sets the remaining estimate on the issue
     *
     * If ({@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR} is set,
     * we are in legacy time tracking mode and this method behaves differently.
     *
     * See http://confluence.atlassian.com/display/JIRA044/Configuring+Time+Tracking#ConfiguringTimeTracking-legacymode
     * for more information on legacy mode.
     *
     * <ul>
     *   <li>In legacy mode, this has the same behaviour as {@link #setOriginalEstimate(String)}, changing
     *       the original and remaining estimate if work has not yet started.
     *       If work has already been logged, it will alter the remaining estimate but leave the original
     *       estimate unchanged.</li>
     *   <li>In modern mode, this will <b>only</b> change the remaining estimate. If you want to change both in a single request
     *       use {@link #setOriginalAndRemainingEstimate(String, String)} / {@link #setOriginalAndRemainingEstimate(Long, Long)}.
     *
     *       Calls to {@link #setOriginalEstimate(Long)} / {@link #setOriginalEstimate(String)} and {@link #setRemainingEstimate(Long)} /
     *       {@link #setRemainingEstimate(String)} are mutually exclusive, so calling both on the same instance will
     *       not produce the desired effect.</li>
     * </ul>
     *
     * @param remainingEstimate the remaining estimate on the issue, e.g. "1d 5h" for 1 day and 5 hours
     * @return this object
     */
    IssueInputParameters setRemainingEstimate(String remainingEstimate);

    /**
     * Sets the remaining estimate on the issue
     *
     * If ({@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR} is set,
     * we are in legacy time tracking mode and this method behaves differently.
     *
     * See http://confluence.atlassian.com/display/JIRA044/Configuring+Time+Tracking#ConfiguringTimeTracking-legacymode
     * for more information on legacy mode.
     *
     * <ul>
     *   <li>In legacy mode, this has the same behaviour as {@link #setOriginalEstimate(String)}, changing
     *       the original and remaining estimate if work has not yet started.
     *       If work has already been logged, it will alter the remaining estimate but leave the original
     *       estimate unchanged.</li>
     *   <li>In modern mode, this will <b>only</b> change the remaining estimate. If you want to change both in a single request
     *       use {@link #setOriginalAndRemainingEstimate(String, String)} / {@link #setOriginalAndRemainingEstimate(Long, Long)}.
     *
     *       Calls to {@link #setOriginalEstimate(Long)} / {@link #setOriginalEstimate(String)} and {@link #setRemainingEstimate(Long)} /
     *       {@link #setRemainingEstimate(String)} are mutually exclusive, so calling both on the same instance will
     *       not produce the desired effect.</li>
     * </ul>
     *
     * @param remainingEstimate the remaining estimate on the issue. The units used are determined by
     *        {@link com.atlassian.jira.config.properties.APKeys#JIRA_TIMETRACKING_DEFAULT_UNIT}.
     * @return this object
     */
    IssueInputParameters setRemainingEstimate(Long remainingEstimate);

    /**
     * Sets both the original and remaining estimate on the issue.
     *
     * If ({@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR} is set,
     * we are in legacy time tracking mode and this method has no effect.
     *
     * See http://confluence.atlassian.com/display/JIRA044/Configuring+Time+Tracking#ConfiguringTimeTracking-legacymode
     * for more information on legacy mode.
     *
     * @param originalEstimate the new original estimate, e.g. "1d 5h" for 1 day and 5 hours
     * @param remainingEstimate the new remaining estimate, e.g. "1d 5h" for 1 day and 5 hours
     * @return this object
     */
    IssueInputParameters setOriginalAndRemainingEstimate(String originalEstimate, String remainingEstimate);

    /**
     * Sets both the original and remaining estimate on the issue.
     *
     * If ({@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR} is set,
     * we are in legacy time tracking mode and this method has no effect.
     *
     * See http://confluence.atlassian.com/display/JIRA044/Configuring+Time+Tracking#ConfiguringTimeTracking-legacymode
     * for more information on legacy mode.
     *
     * @param originalEstimate the new original estimate. The units used are determined by
     *        {@link com.atlassian.jira.config.properties.APKeys#JIRA_TIMETRACKING_DEFAULT_UNIT}.
     * @param remainingEstimate the new remaining estimate.The units used are determined by
     *        {@link com.atlassian.jira.config.properties.APKeys#JIRA_TIMETRACKING_DEFAULT_UNIT}.
     * @return this object
     */
    IssueInputParameters setOriginalAndRemainingEstimate(Long originalEstimate, Long remainingEstimate);

    /**
     * Gets the original estimate set.
     *
     * If ({@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR} is set,
     * we are in legacy time tracking mode and this will return the same as {@link #getRemainingEstimate()}.
     *
     * See http://confluence.atlassian.com/display/JIRA044/Configuring+Time+Tracking#ConfiguringTimeTracking-legacymode
     * for more information on legacy mode.
     *
     * @return the set original estimate, null if one does not exist or it is not a valid number.
     */
    Long getOriginalEstimate();

    /**
     * Gets the remaining estimate set.
     *
     * If ({@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR} is set,
     * we are in legacy time tracking mode and this will return the same as {@link #getOriginalEstimate()} .
     *
     * See http://confluence.atlassian.com/display/JIRA044/Configuring+Time+Tracking#ConfiguringTimeTracking-legacymode
     * for more information on legacy mode.
     *
     * @return the set remaining estimate, null if one does not exist or it is not a valid number.
     */
    Long getRemainingEstimate();

    /**
     * Gets the original estimate set.
     *
     * If ({@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR} is set,
     * we are in legacy time tracking mode and this will return the same as {@link #getRemainingEstimateAsDurationString()}.
     *
     * See http://confluence.atlassian.com/display/JIRA044/Configuring+Time+Tracking#ConfiguringTimeTracking-legacymode
     * for more information on legacy mode.
     *
     * @return the set original estimate, null if one does not exist.
     */
    String getOriginalEstimateAsDurationString();

    /**
     * Gets the remaining estimate provided
     *
     * If ({@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR} is set,
     * we are in legacy time tracking mode and this will return the same as {@link #getOriginalEstimateAsDurationString()}.
     *
     * See http://confluence.atlassian.com/display/JIRA044/Configuring+Time+Tracking#ConfiguringTimeTracking-legacymode
     * for more information on legacy mode.
     *
     * @return the set remaining estimate, null if one does not exist.
     */
    String getRemainingEstimateAsDurationString();

    /**
     * @param timeSpent sets the time spent on the issue.
     * @return this object.
     */
    IssueInputParameters setTimeSpent(Long timeSpent);

    /**
     * @return the set time spent, null if one does not exist or it is not a valid number.
     */
    Long getTimeSpent();

    /**
     * Allows defining history metadata related to the issue update.
     * This is only relevant for changes that generate history entries (transitions and updates).
     *
     * @since JIRA 6.3
     * @param historyMetadata the metadata to save with the history change
     * @return this object
     */
    IssueInputParameters setHistoryMetadata(HistoryMetadata historyMetadata);

    /**
     * @since JIRA 6.3
     * @return the current history metadata for the update, null if not defined
     */
    @Nullable
    HistoryMetadata getHistoryMetadata();

    /**
     * Return form token if it is present in input parameters
     *
     * @return
     */
    String getFormToken();
}
