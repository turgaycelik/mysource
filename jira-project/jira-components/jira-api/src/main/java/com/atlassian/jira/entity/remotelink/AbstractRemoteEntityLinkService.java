package com.atlassian.jira.entity.remotelink;

import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.FieldTooLongJsonPropertyException;
import com.atlassian.jira.entity.property.InvalidJsonPropertyException;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import static com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Base tools for implementing a remote entity link service that uses the
 * {@link JsonEntityPropertyManager} to store the link data.
 *
 * @since v6.1.1
 */
// @PublicSpi // <-- what this should become once accepted as stable
@ExperimentalApi
public abstract class AbstractRemoteEntityLinkService
{
    protected static final String MSG_IS_REQUIRED        = "admin.common.words.is.required";     // 0=field
    protected static final String MSG_DOES_NOT_EXIST     = "remotelink.service.does.not.exist";
    protected static final String MSG_ERROR_CREATING     = "remotelink.service.error.creating";  // 0=cause
    protected static final String MSG_ERROR_UPDATING     = "remotelink.service.error.updating";  // 0=cause
    protected static final String MSG_ERROR_REMOVING     = "remotelink.service.error.removing";  // 0=cause
    protected static final String MSG_FIELD_TOO_LONG     = "remotelink.service.field.too.long";  // 0=field 1=maxlen
    protected static final String MSG_GLOBAL_ID          = "remotelink.service.globalid";
    protected static final String MSG_TITLE              = "remotelink.service.title";
    protected static final String MSG_URL                = "remotelink.service.url";
    protected static final String MSG_APPLICATION_TYPE   = "remotelink.service.applicationtype";
    protected static final String MSG_APPLICATION_NAME   = "remotelink.service.applicationname";
    protected static final String MSG_INVALID_JSON       = "remotelink.service.invalid.json";    // 0=exception msg
    protected static final String MSG_INVALID_URI        = "remotelink.service.invalid.uri";     // 0=field

    protected final String entityName;
    protected final JsonEntityPropertyManager jsonEntityPropertyManager;

    /**
     * Super-constructor for all remote entity link services that use JSON entity properties for storage.
     *
     * @param entityName the entity name that should be used when storing the JSON properties for this link service.
     *          this <strong>MUST</strong> be unique to the service.  To reduce the risk of collision, it
     *          <strong>SHOULD NOT</strong> be the same as the name of the entity itself.  For example,
     *          {@link com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService RemoteVersionLinkService}
     *          uses "RemoteVersionLink", not "Version".
     * @param jsonEntityPropertyManager the injected {@code JsonEntityPropertyManager} component
     */
    protected AbstractRemoteEntityLinkService(String entityName, JsonEntityPropertyManager jsonEntityPropertyManager)
    {
        this.entityName = entityName;
        this.jsonEntityPropertyManager = jsonEntityPropertyManager;
    }



    /**
     * Retrieve the stored JSON for an entity link.
     *
     * @param entityId the ID of the specific entity that this remote entity link was stored against
     * @param globalId the global ID against which the entity link was stored.  See the return value of
     *          {@link #putEntityPropertyValue(Long, String, String)} for more information.
     * @return the stored JSON, or {@code null} if the remote entity link does not exist
     */
    @Nullable
    protected String getEntityPropertyValue(@Nonnull Long entityId, @Nonnull String globalId)
    {
        final EntityProperty property = jsonEntityPropertyManager.get(entityName, entityId, globalId);
        return (property != null) ? property.getValue() : null;
    }

    /**
     * Store JSON data representing an entity link.
     *
     * @param entityId the ID of the specific entity that this remote entity link will be stored against
     * @param globalId the global ID against which to store the remote entity link, or {@code null}.  If
     *          it is {@code null}, then the supplied {@code json} value is examined to see if it is a
     *          valid JSON object with a value specified for {@link RemoteEntityLink#GLOBAL_ID}
     *          generate a new unique identifier to serve as the global ID
     * @param json the content to be stored
     * @return the global ID used to store the link.  If the supplied JSON is an an object that contains
     *      a string field called {@value RemoteEntityLink#GLOBAL_ID} at its top level, then that value
     *      will be extracted and used as the global ID for storage.  Otherwise, a global ID will be
     *      generated by this method and returned in its place.
     * @throws FieldTooLongJsonPropertyException if {@code globalId} or {@code json} exceeds the maximum allowed length
     * @throws InvalidJsonPropertyException if {@code json} is malformed
     * @throws IllegalArgumentException if {@code entityId} or {@code json} is {@code null}
     */
    @Nonnull
    protected String putEntityPropertyValue(@Nonnull Long entityId, @Nullable String globalId, @Nonnull String json)
    {
        notNull("entityId", entityId);
        notNull("json", json);
        globalId = fixGlobalId(globalId, json);
        jsonEntityPropertyManager.put(entityName, entityId, globalId, json);
        return globalId;
    }

    /**
     * Verifies the same conditions that {@link #putEntityPropertyValue(Long, String, String)} would without actually
     * creating or updating the property value.  This is intended for use when validating a service request.
     *
     * @param entityId as for {@link #putEntityPropertyValue(Long, String, String)}
     * @param globalId as for {@link #putEntityPropertyValue(Long, String, String)}
     * @param json as for {@link #putEntityPropertyValue(Long, String, String)}
     * @return the global ID that would have been used to store the link.  If this method is used to
     *      perform validation, then this value may be passed in as the {@code globalId} parameter in
     *      the following call to {@link #putEntityPropertyValue(Long, String, String)} to guarantee
     *      that the same value will be used even if it is a generated UUID.
     * @throws FieldTooLongJsonPropertyException if {@code globalId} or {@code json} exceeds the maximum allowed length
     * @throws InvalidJsonPropertyException if {@code json} is malformed
     * @throws IllegalArgumentException if {@code entityId} or {@code json} is {@code null}
     */
    protected String putEntityPropertyDryRun(@Nonnull Long entityId, @Nullable String globalId, @Nonnull String json)
    {
        notNull("entityId", entityId);
        notNull("json", json);
        globalId = fixGlobalId(globalId, json);
        jsonEntityPropertyManager.putDryRun(entityName, globalId, json);
        return globalId;
    }



    protected static void verifyNotNull(ErrorCollection errors, I18nHelper i18n, String field, Object value)
    {
        if (value == null)
        {
            errors.addError(field, i18n.getText(MSG_IS_REQUIRED, field), VALIDATION_FAILED);
        }
    }

    protected static void verifyNotBlank(ErrorCollection errors, I18nHelper i18n, String field, String value)
    {
        if (StringUtils.isBlank(value))
        {
            errors.addError(field, i18n.getText(MSG_IS_REQUIRED, field), VALIDATION_FAILED);
        }
    }



    private String fixGlobalId(String globalId, String json)
    {
        globalId = StringUtils.trimToNull(globalId);
        if (globalId == null)
        {
            globalId = StringUtils.trimToNull(getGlobalIdFromJson(json));
            if (globalId == null)
            {
                globalId = UUID.randomUUID().toString();
            }
        }
        return globalId;
    }

    private String getGlobalIdFromJson(String json)
    {
        try
        {
            final JsonNode node = new ObjectMapper().readTree(new StringReader(json));
            if (node == null || !node.isObject())
            {
                return null;
            }
            final JsonNode globalId = node.get(RemoteEntityLink.GLOBAL_ID);
            if (globalId == null || !globalId.isTextual())
            {
                return null;
            }
            return globalId.asText();
        }
        catch (IOException ioe)
        {
            throw new IllegalArgumentException("JSON validation failed: " + ioe);
        }
    }
}

