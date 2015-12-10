package com.atlassian.jira.workflow;

import com.atlassian.gzipfilter.org.apache.commons.lang.builder.ReflectionToStringBuilder;
import com.atlassian.gzipfilter.org.apache.commons.lang.builder.ToStringStyle;
import com.atlassian.jira.user.ApplicationUser;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Map;

/**
 * @since v5.2
 */
public class MockDraftWorkflowScheme extends MockWorkflowScheme implements DraftWorkflowScheme
{
    private ApplicationUser lastModifiedUser;
    private Date lastModifiedDate;
    private AssignableWorkflowScheme parentScheme;

    public MockDraftWorkflowScheme()
    {
        this((Long)null);
    }

    public MockDraftWorkflowScheme(Long id)
    {
        this(id, null);
    }

    public MockDraftWorkflowScheme(DraftWorkflowScheme ws)
    {
        super(ws);
        this.lastModifiedUser = ws.getLastModifiedUser();
        this.lastModifiedDate = ws.getLastModifiedDate();
        this.parentScheme = ws.getParentScheme();
    }

    public MockDraftWorkflowScheme(Long l, AssignableWorkflowScheme parentScheme)
    {
        super(l);
        this.parentScheme = parentScheme;
    }

    @Override
    public ApplicationUser getLastModifiedUser()
    {
        return lastModifiedUser;
    }

    @Nonnull
    @Override
    public Date getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    @Override
    public AssignableWorkflowScheme getParentScheme()
    {
        return parentScheme;
    }

    public MockDraftWorkflowScheme setParentScheme(AssignableWorkflowScheme parentScheme)
    {
        this.parentScheme = parentScheme;
        return this;
    }

    @Override
    public MockBuilder builder()
    {
        return new MockBuilder(this);
    }

    public MockDraftWorkflowScheme setLastModifiedUser(ApplicationUser lastModifiedUser)
    {
        this.lastModifiedUser = lastModifiedUser;
        return this;
    }

    public MockDraftWorkflowScheme setLastModifiedDate(Date lastModifiedDate)
    {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    @Override
    public String getName()
    {
        return parentScheme == null ? null : parentScheme.getName();
    }

    @Override
    public String getDescription()
    {
        return parentScheme == null ? null : parentScheme.getDescription();
    }

    @Override
    public boolean isDraft()
    {
        return true;
    }

    @Override
    public boolean isDefault()
    {
        return false;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }

        MockDraftWorkflowScheme that = (MockDraftWorkflowScheme) o;

        if (lastModifiedDate != null ? !lastModifiedDate.equals(that.lastModifiedDate) : that.lastModifiedDate != null)
        { return false; }
        if (lastModifiedUser != null ? !lastModifiedUser.equals(that.lastModifiedUser) : that.lastModifiedUser != null)
        { return false; }
        if (parentScheme != null ? !parentScheme.equals(that.parentScheme) : that.parentScheme != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (lastModifiedUser != null ? lastModifiedUser.hashCode() : 0);
        result = 31 * result + (lastModifiedDate != null ? lastModifiedDate.hashCode() : 0);
        result = 31 * result + (parentScheme != null ? parentScheme.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static class MockBuilder implements DraftWorkflowScheme.Builder
    {
        private final MockDraftWorkflowScheme mockDraftWorkflowScheme;

        public MockBuilder(DraftWorkflowScheme ws)
        {
            mockDraftWorkflowScheme = new MockDraftWorkflowScheme(ws);
        }

        @Override
        public ApplicationUser getLastModifiedUser()
        {
            return mockDraftWorkflowScheme.getLastModifiedUser();
        }

        @Override
        public Date getLastModifiedDate()
        {
            return mockDraftWorkflowScheme.getLastModifiedDate();
        }

        @Override
        public AssignableWorkflowScheme getParentScheme()
        {
            return mockDraftWorkflowScheme.getParentScheme();
        }

        @Override
        public DraftWorkflowScheme build()
        {
            return new MockDraftWorkflowScheme(mockDraftWorkflowScheme);
        }

        @Override
        public String getDefaultWorkflow()
        {
            return mockDraftWorkflowScheme.getConfiguredDefaultWorkflow();
        }

        @Override
        public String getMapping(@Nonnull String issueTypeId)
        {
            return mockDraftWorkflowScheme.getConfiguredWorkflow(issueTypeId);
        }

        @Override
        public Map<String, String> getMappings()
        {
            return mockDraftWorkflowScheme.getMappings();
        }

        @Override
        public Long getId()
        {
            return mockDraftWorkflowScheme.getId();
        }

        @Override
        public boolean isDraft()
        {
            return true;
        }

        @Override
        public boolean isDefault()
        {
            return false;
        }

        @Override
        public String getDescription()
        {
            return mockDraftWorkflowScheme.getDescription();
        }

        @Override
        public String getName()
        {
            return mockDraftWorkflowScheme.getName();
        }

        @Nonnull
        @Override
        public DraftWorkflowScheme.Builder setDefaultWorkflow(@Nonnull String workflowName)
        {
            mockDraftWorkflowScheme.setDefaultWorkflow(workflowName);
            return this;
        }

        @Nonnull
        @Override
        public DraftWorkflowScheme.Builder setMapping(@Nonnull String issueTypeId, @Nonnull String workflowName)
        {
            mockDraftWorkflowScheme.setMapping(issueTypeId, workflowName);
            return this;
        }

        @Nonnull
        @Override
        public DraftWorkflowScheme.Builder setMappings(@Nonnull Map<String, String> mappings)
        {
            mockDraftWorkflowScheme.setMappings(mappings);
            return this;
        }

        @Nonnull
        @Override
        public DraftWorkflowScheme.Builder removeMapping(@Nonnull String issueTypeId)
        {
            mockDraftWorkflowScheme.removeMapping(issueTypeId);
            return this;
        }

        @Nonnull
        @Override
        public DraftWorkflowScheme.Builder removeDefault()
        {
            mockDraftWorkflowScheme.removeDefault();
            return this;
        }

        @Nonnull
        @Override
        public DraftWorkflowScheme.Builder clearMappings()
        {
            mockDraftWorkflowScheme.clearMappings();
            return this;
        }

        @Nonnull
        @Override
        public DraftWorkflowScheme.Builder removeWorkflow(@Nonnull String workflowName)
        {
            mockDraftWorkflowScheme.removeWorkflow(workflowName);
            return this;
        }

        @Override
        public String toString()
        {
            return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
