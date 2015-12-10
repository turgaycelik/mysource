package com.atlassian.jira.auditing;

import com.atlassian.annotations.ExperimentalApi;

import javax.annotation.Nullable;

/**
 * This class represents filters that can be applied when getting Audit log records.
 * Use AuditingFilter#builder() to acquire builder for constructing object of this class.
 *
 * @since v6.3
 */
@ExperimentalApi
public class AuditingFilter
{
    static public Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String filter;
        private Long fromTimestamp;
        private Long toTimestamp;
        private boolean hideExternalDirectories = false;

        private Builder() {}

        /**
         * @param filter text filter query; each record in result set will need to contain this text
         */
        public Builder filter(@Nullable final String filter)
        {
            this.filter = filter;
            return this;
        }

        /**
         * @param fromTimestamp timestamp in past to narrow the results set; fromTimestamp must be less or equal
         * toTimestamp, otherwise the result set will be empty only records that where created in the same moment or after
         * the fromTimestamp will be provided in results set
         */
        public Builder fromTimestamp(@Nullable final Long fromTimestamp)
        {
            this.fromTimestamp = fromTimestamp;
            return this;
        }

        /**
         * @param toTimestamp timestamp in past to narrow the results set; fromTimestamp must be less or equal
         * toTimestamp, otherwise the result set will be empty only records that where created in the same moment or earlier
         * than the toTimestamp will be provided in results set
         */
        public Builder toTimestamp(@Nullable final Long toTimestamp)
        {
            this.toTimestamp = toTimestamp;
            return this;
        }

        /**
         * @param hideExternalDirectories if true only records related to internal directory will be returned
         */
        public Builder setHideExternalDirectories(boolean hideExternalDirectories) {
            this.hideExternalDirectories = hideExternalDirectories;
            return this;
        }

        public AuditingFilter build()
        {
            return new AuditingFilter(filter, fromTimestamp, toTimestamp, hideExternalDirectories);
        }
    }

    private final String filter;
    private final Long fromTimestamp;
    private final Long toTimestamp;
    private final boolean hideExternalDirectories;

    private AuditingFilter(@Nullable String filter, @Nullable Long fromTimestamp, @Nullable Long toTimestamp, boolean hideExternalDirectories)
    {
        this.filter = filter;
        this.fromTimestamp = fromTimestamp;
        this.toTimestamp = toTimestamp;
        this.hideExternalDirectories = hideExternalDirectories;
    }

    @Nullable
    public String getFilter()
    {
        return filter;
    }

    @Nullable
    public Long getFromTimestamp()
    {
        return fromTimestamp;
    }

    @Nullable
    public Long getToTimestamp()
    {
        return toTimestamp;
    }

    public boolean isHideExternalDirectories() {
        return hideExternalDirectories;
    }
}
