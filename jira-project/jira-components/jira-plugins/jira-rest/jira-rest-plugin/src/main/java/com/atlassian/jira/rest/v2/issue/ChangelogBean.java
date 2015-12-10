package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.rest.Dates;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @since v5.0
 */
public class ChangelogBean
{
    @JsonProperty
    private Integer startAt;

    @JsonProperty
    private Integer maxResults;

    @JsonProperty
    private Integer total;

    @JsonProperty
    private List<ChangeHistoryBean> histories;

    public Integer getStartAt()
    {
        return startAt;
    }

    public void setStartAt(int startAt)
    {
        this.startAt = startAt;
    }

    public Integer getMaxResults()
    {
        return maxResults;
    }

    public void setMaxResults(int maxResults)
    {
        this.maxResults = maxResults;
    }

    public Integer getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public List<ChangeHistoryBean> getHistories()
    {
        return histories;
    }

    public void setHistories(List<ChangeHistoryBean> histories)
    {
        this.histories = histories;
    }

    public static class ChangeHistoryBean
    {
        @JsonProperty
        private String id;

        @JsonProperty
        private UserJsonBean author;

        @JsonProperty
        @XmlJavaTypeAdapter (Dates.DateTimeAdapter.class)
        private Date created;

        @JsonProperty
        private List<ChangeItemBean> items;

        /**
         * @since JIRA 6.3
         */
        @JsonProperty
        @JsonSerialize (include = JsonSerialize.Inclusion.NON_NULL) @XmlElement(nillable = true)
        private HistoryMetadata historyMetadata;

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public UserJsonBean getAuthor()
        {
            return author;
        }

        public void setAuthor(UserJsonBean author)
        {
            this.author = author;
        }

        public Date getCreated()
        {
            return created;
        }

        public void setCreated(Date created)
        {
            this.created = created;
        }

        public List<ChangeItemBean> getItems()
        {
            return items;
        }

        public void setItems(List<ChangeItemBean> items)
        {
            this.items = items;
        }

        public HistoryMetadata getHistoryMetadata()
        {
            return historyMetadata;
        }

        public void setHistoryMetadata(final HistoryMetadata historyMetadata)
        {
            this.historyMetadata = historyMetadata;
        }
    }

    public static class ChangeItemBean
    {
        @JsonProperty
        private String field;
        @JsonProperty
        private String fieldtype;
        
        @JsonProperty
        @JsonSerialize (include = JsonSerialize.Inclusion.ALWAYS) @XmlElement(nillable = true)
        private String from;
        @JsonProperty
        @JsonSerialize (include = JsonSerialize.Inclusion.ALWAYS) @XmlElement(nillable = true)
        private String fromString;
        @JsonProperty
        @JsonSerialize (include = JsonSerialize.Inclusion.ALWAYS) @XmlElement(nillable = true)
        private String to;
        @JsonProperty
        @JsonSerialize (include = JsonSerialize.Inclusion.ALWAYS) @XmlElement(nillable = true)
        private String toString;

        public String getField()
        {
            return field;
        }

        public void setField(String field)
        {
            this.field = field;
        }

        public String getFrom()
        {
            return from;
        }

        @JsonProperty
        public void setFrom(String from)
        {
            this.from = from;
        }

        public String getFromString()
        {
            return fromString;
        }

        public void setFromString(String fromString)
        {
            this.fromString = fromString;
        }

        public String getTo()
        {
            return to;
        }

        public void setTo(String to)
        {
            this.to = to;
        }

        public String getToString()
        {
            return toString;
        }

        public void setToString(String toString)
        {
            this.toString = toString;
        }

        public String getFieldtype()
        {
            return fieldtype;
        }

        public void setFieldtype(String fieldtype)
        {
            this.fieldtype = fieldtype;
        }
    }
}
