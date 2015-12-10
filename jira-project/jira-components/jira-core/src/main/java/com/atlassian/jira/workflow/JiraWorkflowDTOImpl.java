package com.atlassian.jira.workflow;

import com.opensymphony.workflow.loader.WorkflowDescriptor;

/**
 * This is a simple data transfer object used to convey workflow information out of the
 * Workflow store.
 *
 * @since v3.13
 */
public class JiraWorkflowDTOImpl implements JiraWorkflowDTO
{
    private final Long id;
    private final ImmutableWorkflowDescriptor descriptor;
    private final String name;

    public JiraWorkflowDTOImpl(final Long id, final String name, final WorkflowDescriptor descriptor)
    {
        this.id = id;
        this.descriptor = descriptor != null ? new ImmutableWorkflowDescriptor(descriptor) : null;
        this.name = name;
    }

    public Long getId()
    {
        return id;
    }

    public ImmutableWorkflowDescriptor getDescriptor()
    {
        return descriptor;
    }

    public String getName()
    {
        return name;
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final JiraWorkflowDTOImpl dto = (JiraWorkflowDTOImpl) o;

        if (descriptor != null ? !descriptor.equals(dto.descriptor) : dto.descriptor != null)
        {
            return false;
        }
        if (id != null ? !id.equals(dto.id) : dto.id != null)
        {
            return false;
        }
        if (name != null ? !name.equals(dto.name) : dto.name != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (descriptor != null ? descriptor.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
