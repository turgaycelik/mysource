package com.atlassian.jira.issue.changehistory.metadata;

import com.atlassian.annotations.ExperimentalApi;
import com.google.common.base.Objects;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Represents a identifiable participant in the metadata history. This might be a remote system, an event that caused
 * the change to occur, or a user on a remote system.
 *
 * @since JIRA 6.3
 */
@ExperimentalApi
@JsonSerialize (include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties (ignoreUnknown = true)
public final class HistoryMetadataParticipant
{
    @JsonProperty
    String id;

    @JsonProperty
    String displayName;

    @JsonProperty
    String displayNameKey;

    @JsonProperty
    String type;

    @JsonProperty
    String avatarUrl;

    @JsonProperty
    String url;

    // for jackson
    private HistoryMetadataParticipant()
    {}

    private HistoryMetadataParticipant(final HistoryMetadataParticipantBuilder builder)
    {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.displayNameKey = builder.displayNameKey;
        this.type = builder.type;
        this.avatarUrl = builder.avatarUrl;
        this.url = builder.url;
    }

    /**
     * The identifier of this participant
     */
    public String getId()
    {
        return id;
    }

    /**
     * The user readable name of the participant
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * i18n key for the  user readable name of the participant, will be used before displayName if present
     */
    public String getDisplayNameKey()
    {
        return displayNameKey;
    }

    /**
     * The type of the participant
     */
    public String getType()
    {
        return type;
    }

    /**
     * The avatar image url of the participant
     */
    public String getAvatarUrl()
    {
        return avatarUrl;
    }

    /**
     * The url to this participant's details page
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * This method is implemented for usage in Unit Tests.
     */
    @Override
    public int hashCode()
    {
        return Objects.hashCode(id, displayName, displayNameKey, type, avatarUrl, url);
    }

    /**
     * This method is implemented for usage in Unit Tests.
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) {return true;}
        if (obj == null || getClass() != obj.getClass()) {return false;}
        final HistoryMetadataParticipant other = (HistoryMetadataParticipant) obj;
        return Objects.equal(this.id, other.id) && Objects.equal(this.displayName, other.displayName) &&
                Objects.equal(this.displayNameKey, other.displayNameKey) && Objects.equal(this.type, other.type) &&
                Objects.equal(this.avatarUrl, other.avatarUrl) && Objects.equal(this.url, other.url);
    }

    /**
     * @param id the id of the participant being created
     * @param type the type of the participant being created
     * @return a builder for a HistoryMetadataParticipant object
     */
    public static HistoryMetadataParticipantBuilder builder(String id, String type)
    {
        return new HistoryMetadataParticipantBuilder(id, type);
    }

    @ExperimentalApi
    public static class HistoryMetadataParticipantBuilder
    {
        private String id;
        private String type;

        private String displayName;
        private String displayNameKey;
        private String avatarUrl;
        private String url;

        private HistoryMetadataParticipantBuilder(String id, String type)
        {
            this.id = id;
            this.type = type;
        }

        public HistoryMetadataParticipantBuilder displayName(String displayName)
        {
            this.displayName = displayName;
            return this;
        }

        public HistoryMetadataParticipantBuilder displayNameKey(String displayNameKey)
        {
            this.displayNameKey = displayNameKey;
            return this;
        }

        public HistoryMetadataParticipantBuilder avatarUrl(String avatarUrl)
        {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public HistoryMetadataParticipantBuilder url(String url)
        {
            this.url = url;
            return this;
        }

        public HistoryMetadataParticipant build()
        {
            return new HistoryMetadataParticipant(this);
        }
    }
}
