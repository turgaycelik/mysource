package com.atlassian.jira.workflow;

import com.google.common.base.Function;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @since v5.2
 */
public class MockAssignableWorkflowScheme extends MockWorkflowScheme implements AssignableWorkflowScheme
{
    public static Function<AssignableWorkflowScheme, MockAssignableWorkflowScheme> toMock()
    {
        return new Function<AssignableWorkflowScheme, MockAssignableWorkflowScheme>()
        {
            @Override
            public MockAssignableWorkflowScheme apply(AssignableWorkflowScheme input)
            {
                if (input == null)
                {
                    return null;
                }
                else
                {
                    return new MockAssignableWorkflowScheme(input);
                }
            }
        };
    }

    private String name;
    private String description;
    private boolean defaultScheme;

    public MockAssignableWorkflowScheme()
    {
        super();
    }

    public MockAssignableWorkflowScheme(Long id, String name)
    {
        this(id, name, null);
    }

    public MockAssignableWorkflowScheme(Long id, String name, String description)
    {
        super(id);
        this.name = name;
        this.description = description;
    }

    public MockAssignableWorkflowScheme(AssignableWorkflowScheme scheme)
    {
        super(scheme);
        this.name = scheme.getName();
        this.description = scheme.getDescription();
        this.defaultScheme = scheme.isDefault();
    }

    @Nonnull
    @Override
    public AssignableWorkflowScheme.Builder builder()
    {
        return new MockBuilder(this);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public boolean isDraft()
    {
        return false;
    }

    @Override
    public boolean isDefault()
    {
        return defaultScheme;
    }

    public MockAssignableWorkflowScheme setDefaultScheme(boolean defaultScheme)
    {
        this.defaultScheme = defaultScheme;
        return this;
    }

    public MockAssignableWorkflowScheme setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public MockAssignableWorkflowScheme setName(String name)
    {
        this.name = name;
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }

        MockAssignableWorkflowScheme that = (MockAssignableWorkflowScheme) o;

        if (defaultScheme != that.defaultScheme) { return false; }
        if (description != null ? !description.equals(that.description) : that.description != null) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (defaultScheme ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static class MockBuilder implements AssignableWorkflowScheme.Builder
    {
        private final MockAssignableWorkflowScheme mockAssignableWorkflowScheme;

        public MockBuilder(AssignableWorkflowScheme ws)
        {
            mockAssignableWorkflowScheme = new MockAssignableWorkflowScheme(ws);
        }

        @Nonnull
        @Override
        public AssignableWorkflowScheme.Builder setName(@Nonnull String name)
        {
            mockAssignableWorkflowScheme.setName(name);
            return this;
        }

        @Nonnull
        @Override
        public AssignableWorkflowScheme.Builder setDescription(String description)
        {
            mockAssignableWorkflowScheme.setDescription(description);
            return this;
        }

        @Nonnull
        @Override
        public MockAssignableWorkflowScheme build()
        {
            return new MockAssignableWorkflowScheme(mockAssignableWorkflowScheme);
        }

        @Override
        public String getDefaultWorkflow()
        {
            return mockAssignableWorkflowScheme.getConfiguredDefaultWorkflow();
        }

        @Override
        public String getMapping(@Nonnull String issueTypeId)
        {
            return mockAssignableWorkflowScheme.getConfiguredWorkflow(issueTypeId);
        }

        @Override
        public Map<String, String> getMappings()
        {
            return mockAssignableWorkflowScheme.getMappings();
        }

        @Override
        public Long getId()
        {
            return mockAssignableWorkflowScheme.getId();
        }

        @Override
        public boolean isDraft()
        {
            return mockAssignableWorkflowScheme.isDraft();
        }

        @Override
        public boolean isDefault()
        {
            return false;
        }

        @Override
        public String getDescription()
        {
            return mockAssignableWorkflowScheme.getDescription();
        }

        @Override
        public String getName()
        {
            return mockAssignableWorkflowScheme.getName();
        }

        @Nonnull
        @Override
        public MockBuilder setDefaultWorkflow(@Nonnull String workflowName)
        {
            mockAssignableWorkflowScheme.setDefaultWorkflow(workflowName);
            return this;
        }

        @Nonnull
        @Override
        public MockBuilder setMapping(@Nonnull String issueTypeId, @Nonnull String workflowName)
        {
            mockAssignableWorkflowScheme.setMapping(issueTypeId, workflowName);
            return this;
        }

        @Nonnull
        @Override
        public MockBuilder setMappings(@Nonnull Map<String, String> mappings)
        {
            mockAssignableWorkflowScheme.setMappings(mappings);
            return this;
        }

        @Nonnull
        @Override
        public MockBuilder removeMapping(@Nonnull String issueTypeId)
        {
            mockAssignableWorkflowScheme.removeMapping(issueTypeId);
            return this;
        }

        @Nonnull
        @Override
        public MockBuilder removeDefault()
        {
            mockAssignableWorkflowScheme.removeDefault();
            return this;
        }

        @Nonnull
        @Override
        public MockBuilder clearMappings()
        {
            mockAssignableWorkflowScheme.clearMappings();
            return this;
        }

        @Nonnull
        @Override
        public AssignableWorkflowScheme.Builder removeWorkflow(@Nonnull String workflowName)
        {
            mockAssignableWorkflowScheme.removeWorkflow(workflowName);
            return this;
        }
    }
}
