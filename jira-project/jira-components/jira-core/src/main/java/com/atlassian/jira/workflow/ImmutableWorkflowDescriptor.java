package com.atlassian.jira.workflow;

import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.JoinDescriptor;
import com.opensymphony.workflow.loader.SplitDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.w3c.dom.Element;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Representes an Immutable {@link com.opensymphony.workflow.loader.WorkflowDescriptor}.  Due to OS workflows great
 * implementation (lack of Interface, clone() and constructor via object graph), we need to delegate all our getters
 * to the actual WorkflowDescriptor itself.
 * <p/>
 * Invocation of any setter on this object will cause an UnsupportedOperationException being thrown.
 *
 * @since v3.13
 */
public class ImmutableWorkflowDescriptor extends WorkflowDescriptor
{
    private final WorkflowDescriptor delegate;

    public ImmutableWorkflowDescriptor(final WorkflowDescriptor delegate)
    {
        this.delegate = delegate;
    }

    public ActionDescriptor getAction(final int id)
    {
        return delegate.getAction(id);
    }

    public Map getCommonActions()
    {
        return getNullSafeUnmodifableMap(delegate.getCommonActions());
    }

    public List getGlobalActions()
    {
        return getNullSafeUnmodifiableList(delegate.getGlobalActions());
    }

    public ConditionsDescriptor getGlobalConditions()
    {
        return delegate.getGlobalConditions();
    }

    public ActionDescriptor getInitialAction(final int id)
    {
        return delegate.getInitialAction(id);
    }

    public List getInitialActions()
    {
        return delegate.getInitialActions();
    }

    public JoinDescriptor getJoin(final int id)
    {
        return delegate.getJoin(id);
    }

    public List getJoins()
    {
        return getNullSafeUnmodifiableList(delegate.getJoins());
    }

    public Map getMetaAttributes()
    {
        return getNullSafeUnmodifableMap(delegate.getMetaAttributes());
    }

    public String getName()
    {
        return delegate.getName();
    }

    public List getRegisters()
    {
        return getNullSafeUnmodifiableList(delegate.getRegisters());
    }

    public List getSplits()
    {
        return getNullSafeUnmodifiableList(delegate.getSplits());
    }

    public SplitDescriptor getSplit(final int id)
    {
        return delegate.getSplit(id);
    }

    public StepDescriptor getStep(final int id)
    {
        return delegate.getStep(id);
    }

    public List getSteps()
    {
        return getNullSafeUnmodifiableList(delegate.getSteps());
    }

    public FunctionDescriptor getTriggerFunction(final int id)
    {
        return delegate.getTriggerFunction(id);
    }

    public Map getTriggerFunctions()
    {
        return getNullSafeUnmodifableMap(delegate.getTriggerFunctions());
    }

    public void validate() throws InvalidWorkflowDescriptorException
    {
        delegate.validate();
    }

    public void writeXML(final PrintWriter out, final int indent)
    {
        delegate.writeXML(out, indent);
    }

    public int getEntityId()
    {
        return delegate.getEntityId();
    }

    public int getId()
    {
        return delegate.getId();
    }

    public AbstractDescriptor getParent()
    {
        return delegate.getParent();
    }

    public String asXML()
    {
        return delegate.asXML();
    }

    public boolean hasId()
    {
        return delegate.hasId();
    }

    public void setName(final String name)
    {
        throwUnsupportedException();
    }

    public FunctionDescriptor setTriggerFunction(final int id, final FunctionDescriptor descriptor)
    {
        throw new UnsupportedOperationException("Object is immutable.  You can only edit a WorkflowDescriptor directly.");
    }

    protected void init(final Element root)
    {
        throwUnsupportedException();
    }

    public void addCommonAction(final ActionDescriptor descriptor)
    {
        throwUnsupportedException();
    }

    public void addGlobalAction(final ActionDescriptor descriptor)
    {
        throwUnsupportedException();
    }

    public void addInitialAction(final ActionDescriptor descriptor)
    {
        throwUnsupportedException();
    }

    public void addJoin(final JoinDescriptor descriptor)
    {
        throwUnsupportedException();
    }

    public void addSplit(final SplitDescriptor descriptor)
    {
        throwUnsupportedException();
    }

    public void addStep(final StepDescriptor descriptor)
    {
        throwUnsupportedException();
    }

    public boolean removeAction(final ActionDescriptor actionToRemove)
    {
        throw new UnsupportedOperationException("Object is immutable.  You can only edit a WorkflowDescriptor directly.");
    }

    public void setParent(final AbstractDescriptor parent)
    {
        throwUnsupportedException();
    }

    public void setId(final int id)
    {
        throwUnsupportedException();
    }

    public void setEntityId(final int entityId)
    {
        throwUnsupportedException();
    }

    private void throwUnsupportedException()
    {
        throw new UnsupportedOperationException("Object is immutable.  You can only edit a WorkflowDescriptor directly.");
    }

    private List getNullSafeUnmodifiableList(final List list)
    {
        if (list == null)
        {
            return null;
        }
        return Collections.unmodifiableList(list);
    }

    private Map getNullSafeUnmodifableMap(final Map map)
    {
        if (map == null)
        {
            return null;
        }
        return Collections.unmodifiableMap(map);
    }
}
