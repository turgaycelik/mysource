package com.atlassian.jira.issue.table;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

/**
 * Serialisable IssueTable object.
 *
 * This class was pulled from IssueTableResource.IssueTable to prevent coupling to the gadgets project.
 * IssueTableResource should eventually use IssueTableService and therefore not need it's own internal IssueTable.
 *
 * @deprecated This functionality has been moved into {@code jira-issue-nav-plugin}. Since v6.0.
 * @since v5.1
 */
///CLOVER:OFF
@Deprecated
@XmlRootElement
public class IssueTable
{
    @XmlElement
    private String table;
    @XmlElement
    private int displayed;
    @XmlElement
    private int startIndex;
    @XmlElement
    private int total;
    @XmlElement
    private int end;
    @XmlElement
    private int page;
    @XmlElement
    private int pageSize;
    @XmlElement
    private String url;
    @XmlElement
    private String title;
    @XmlElement
    private String description;
    @XmlElement
    private Map<String, String> columnSortJql;
    @XmlElement
    private boolean jiraHasIssues;
    @XmlElement
    private List<Long> issueIds;
    @XmlElement
    private List<String> issueKeys;

    @SuppressWarnings ({ "UnusedDeclaration", "unused" })
    private IssueTable()
    {
    }

    public String getTable()
    {
        return table;
    }

    public int getTotal()
    {
        return total;
    }

    public int getStartIndex()
    {
        return startIndex;
    }

    public int getEnd()
    {
        return end;
    }

    public boolean getJiraHasIssues()
    {
        return jiraHasIssues;
    }

    public List<Long> getIssueIds()
    {
        return issueIds;
    }

    public List<String> getIssueKeys()
    {
        return issueKeys;
    }

    public static class Builder {
        private final String table;

        private int displayed;
        private int total;
        private int startIndex;
        private int end;
        private int page;
        private int pageSize;
        private String url;
        private String title;
        private String description;
        private Map<String, String> columnSortJql;
        private boolean jiraHasIssues;
        private List<Long> issueIds;
        private List<String> issueKeys;

        public Builder(String table)
        {
            this.table = table;
        }
        
        public Builder displayed(int displayed)
        {
            this.displayed = displayed;
            return this;
        }
        
        public Builder total(int total)
        {
            this.total = total;
            return this;
        }
        
        public Builder startIndex(int startIndex)
        {
            this.startIndex = startIndex;
            return this;
        }
        
        public Builder end(int end)
        {
            this.end = end;
            return this;
        }
        
        public Builder page(int page)
        {
            this.page = page;
            return this;
        }
        
        public Builder pageSize(int pageSize)
        {
            this.pageSize = pageSize;
            return this;
        }
        
        public Builder url(String url)
        {
            this.url = url;
            return this;
        }
        
        public Builder title(String title)
        {
            this.title = title;
            return this;
        }
        
        public Builder description(String description)
        {
            this.description = description;
            return this;
        }

        public Builder columnSortJql(Map<String, String> columnSortJql)
        {
            this.columnSortJql = columnSortJql;
            return this;
        }

        public Builder jiraHasIssues(boolean jiraHasIssues)
        {
            this.jiraHasIssues = jiraHasIssues;
            return this;
        }

        public Builder issueIds(List<Long> issueIds)
        {
            this.issueIds = issueIds;
            return this;
        }

        public Builder issueKeys(List<String> issueKeys)
        {
            this.issueKeys = issueKeys;
            return this;
        }
        
        public IssueTable build()
        {
            return new IssueTable(this);
        }
    }
    
    private IssueTable(Builder builder) 
    {
        table = builder.table;
        displayed = builder.displayed;
        total = builder.total;
        startIndex = builder.startIndex;
        end = builder.end;
        page = builder.page;
        pageSize = builder.pageSize;
        url = builder.url;
        title = builder.title;
        description = builder.description;
        columnSortJql = builder.columnSortJql;
        jiraHasIssues = builder.jiraHasIssues;
        issueIds = builder.issueIds;
        issueKeys = builder.issueKeys;
    }
}
///CLOVER:ON