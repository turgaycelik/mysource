package com.atlassian.jira.issue.changehistory.metadata;

import com.atlassian.annotations.ExperimentalApi;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Map;

/**
 * Represents the complete set of metadata for a history changegroup.
 *
 * @since JIRA 6.3
 */
@ExperimentalApi
@JsonSerialize (include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties (ignoreUnknown = true)
public final class HistoryMetadata
{
    @JsonProperty
    private String type;

    @JsonProperty
    private String description;

    @JsonProperty
    private String descriptionKey;

    @JsonProperty
    private String activityDescription;

    @JsonProperty
    private String activityDescriptionKey;

    @JsonProperty
    private String emailDescription;

    @JsonProperty
    private String emailDescriptionKey;

    @JsonProperty
    private HistoryMetadataParticipant actor;

    @JsonProperty
    private HistoryMetadataParticipant generator;

    @JsonProperty
    private HistoryMetadataParticipant cause;

    @JsonProperty
    private Map<String, String> extraData;

    // for jackson
    private HistoryMetadata()
    {
    }

    private HistoryMetadata(final HistoryMetadataBuilder builder)
    {
        this.type = builder.type;
        this.description = builder.description;
        this.descriptionKey = builder.descriptionKey;
        this.activityDescription = builder.activityDescription;
        this.activityDescriptionKey = builder.activityDescriptionKey;
        this.emailDescription = builder.emailDescription;
        this.emailDescriptionKey = builder.emailDescriptionKey;
        this.actor = builder.actor;
        this.generator = builder.generator;
        this.cause = builder.cause;
        this.extraData = builder.extraData.build();
    }

    /**
     * The person or agent that triggered the history change
     */
    public HistoryMetadataParticipant getActor()
    {
        return actor;
    }

    /**
     * The system that triggered the history change
     */
    public HistoryMetadataParticipant getGenerator()
    {
        return generator;
    }

    /**
     * The event or state that triggered the history change
     */
    public HistoryMetadataParticipant getCause()
    {
        return cause;
    }

    /**
     * A unique id to identify the plugin/system that generated this metadata
     */
    public String getType()
    {
        return type;
    }

    /**
     * Textual description of the change
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * i18n key for the textual description of the change, will be used before description if present
     */
    public String getDescriptionKey()
    {
        return descriptionKey;
    }

    /**
     * Textual description of the change for the activity stream
     */
    public String getActivityDescription()
    {
        return activityDescription;
    }

    /**
     * i18n key for the description of the change for the activity stream, will be used before activityDescription if present
     */
    public String getActivityDescriptionKey()
    {
        return activityDescriptionKey;
    }

    /**
     * Textual description of the change for notification emails
     */
    public String getEmailDescription()
    {
        return emailDescription;
    }

    /**
     * i18n key for the textual description of the change for the notification email, will be used before emailDescription if present
     */
    public String getEmailDescriptionKey()
    {
        return emailDescriptionKey;
    }

    /**
     * Additional metadata related to the history change
     */
    public Map<String, String> getExtraData()
    {
        return extraData;
    }

    /**
     * This method is implemented for usage in Unit Tests.
     */
    @Override
    public int hashCode()
    {
        return Objects.hashCode(type, actor, generator, cause, extraData, description, descriptionKey,
                activityDescription, activityDescriptionKey, emailDescription, emailDescriptionKey);
    }

    /**
     * This method is implemented for usage in Unit Tests.
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        final HistoryMetadata other = (HistoryMetadata) obj;
        return Objects.equal(this.type, other.type) && Objects.equal(this.actor, other.actor) && Objects.equal(this.generator, other.generator) &&
                Objects.equal(this.cause, other.cause) && Objects.equal(this.extraData, other.extraData) &&
                Objects.equal(this.description, other.description) && Objects.equal(this.descriptionKey, other.descriptionKey) &&
                Objects.equal(this.activityDescription, other.activityDescription) && Objects.equal(this.activityDescriptionKey, other.activityDescriptionKey) &&
                Objects.equal(this.emailDescription, other.emailDescription) && Objects.equal(this.emailDescriptionKey, other.emailDescriptionKey);
    }

    /**
     * @param type the type of the metadata object being created
     * @return a builder for a HistoryMetadata object
     */
    public static HistoryMetadataBuilder builder(String type)
    {
        return new HistoryMetadataBuilder(type);
    }

    @ExperimentalApi
    public static class HistoryMetadataBuilder
    {
        private String type;
        private String description;
        private String descriptionKey;
        private String activityDescription;
        private String activityDescriptionKey;
        private String emailDescription;
        private String emailDescriptionKey;
        private HistoryMetadataParticipant actor;
        private HistoryMetadataParticipant generator;
        private HistoryMetadataParticipant cause;
        private ImmutableMap.Builder<String, String> extraData = ImmutableMap.builder();

        private HistoryMetadataBuilder(final String type)
        {
            this.type = Preconditions.checkNotNull(type);
        }

        public HistoryMetadataBuilder description(String description)
        {
            this.description = description;
            return this;
        }

        public HistoryMetadataBuilder descriptionKey(String descriptionKey)
        {
            this.descriptionKey = descriptionKey;
            return this;
        }

        public HistoryMetadataBuilder activityDescription(String activityDescription)
        {
            this.activityDescription = activityDescription;
            return this;
        }

        public HistoryMetadataBuilder activityDescriptionKey(String activityDescriptionKey)
        {
            this.activityDescriptionKey = activityDescriptionKey;
            return this;
        }

        public HistoryMetadataBuilder emailDescription(String emailDescription)
        {
            this.emailDescription = emailDescription;
            return this;
        }

        public HistoryMetadataBuilder emailDescriptionKey(String emailDescriptionKey)
        {
            this.emailDescriptionKey = emailDescriptionKey;
            return this;
        }

        public HistoryMetadataBuilder actor(HistoryMetadataParticipant actor)
        {
            this.actor = actor;
            return this;
        }

        public HistoryMetadataBuilder generator(HistoryMetadataParticipant generator)
        {
            this.generator = generator;
            return this;
        }

        public HistoryMetadataBuilder cause(HistoryMetadataParticipant cause)
        {
            this.cause = cause;
            return this;
        }

        public HistoryMetadataBuilder extraData(Map<String, String> extraData)
        {
            this.extraData.putAll(extraData);
            return this;
        }

        public HistoryMetadataBuilder extraData(String extraDataKey, String extraDataValue)
        {
            this.extraData.put(extraDataKey, extraDataValue);
            return this;
        }

        public HistoryMetadata build()
        {
            return new HistoryMetadata(this);
        }
    }
}
