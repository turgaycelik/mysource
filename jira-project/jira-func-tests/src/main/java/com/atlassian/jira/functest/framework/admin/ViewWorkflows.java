package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.admin.workflows.PublishDraftPage;
import com.atlassian.jira.functest.framework.admin.workflows.ViewWorkflowPage;
import com.atlassian.jira.functest.framework.admin.workflows.WorkflowDesignerPage;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.concurrent.Immutable;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.filter;

/**
 * Represents operations on the 'Workflows' page in administration.
 *
 * @since v4.3
 */
public interface ViewWorkflows
{
    /**
     * Go to 'Workflows' page.
     *
     * @return this workflows instance
     */
    ViewWorkflows goTo();

    /**
     * Add new workflow with given <tt>name</tt> and <tt>description</tt>.
     *
     * @param name name of the new workflow
     * @param description description of the new workflow
     * @return this workflows instance
     */
    ViewWorkflows addWorkflow(String name, String description);

    WorkflowDesignerPage edit(String workflowName);

    ViewWorkflows delete(String workflowName);

    WorkflowSteps createDraft(String name);

    PublishDraftPage publishDraft(String name);

    /**
     * Cope workflow with given <tt>nameToCopy</tt> as a new workflow named <tt>newWorkflowName</tt>.
     *
     * @param nameToCopy name of the workflow to copy (must exist)
     * @param newWorkflowName name of the new workflow
     * @return this workflows instance
     */
    WorkflowDesignerPage copyWorkflow(String nameToCopy, String newWorkflowName);

    WorkflowDesignerPage copyWorkflow(String nameToCopy, String newWorkflowName, String newWorkflowDescription);

    /**
     * Go to 'Workflow steps' page for given workflow
     *
     * @param workflowName name of the workflow
     * @return workflow steps
     */
    WorkflowSteps workflowSteps(String workflowName);

    /**
     * Launch the Workflow Designer for the given workflow
     *
     * @param workflowName name of the workflow
     * @return this workflows instance
     * @deprecated Now the edit button always takes you to the workflow designer.
     * Use {@link ViewWorkflows#edit(String)} instead
     */
    @Deprecated
    ViewWorkflows launchDesigner(String workflowName);

    /**
     * Whether the import workflow from xml operation is available.
     *
     * @return true, if the import workflow from xml operation is available; otherwise, false.
     */
    boolean isImportWorkflowFromXmlButtonPresent();

    WorkflowItemsList active();

    WorkflowItemsList inactive();

    boolean isEditable(String workflowName);

    ViewWorkflowPage view(String workflowName);

    /**
     * Going to "Edit workflow designer", which is necessary step to manually browse edit "Create" transition page.
     *
     * @param workflowName
     * @return {@see WorkflowInitialStep}
     * @since 6.3
     */
    WorkflowInitialStep workflowInitialStep(String workflowName);

    public static class WorkflowItemsList extends ForwardingList<WorkflowItem>
    {
        private List<WorkflowItem> delegate;

        public WorkflowItemsList(List<WorkflowItem> delegate)
        {
            this.delegate = delegate;
        }

        @Override
        protected List<WorkflowItem> delegate()
        {
            return delegate;
        }

        public WorkflowItemsList drafts()
        {
            return new WorkflowItemsList(copyOf(filter(this, new Predicate<WorkflowItem>()
            {
                @Override
                public boolean apply(WorkflowItem input)
                {
                    return input.isDraft();
                }
            })));
        }

        public boolean contains(final String workflowName)
        {
            return any(delegate, new Predicate<WorkflowItem>()
            {
                @Override
                public boolean apply(WorkflowItem input)
                {
                    return input.name().equals(workflowName);
                }
            });
        }

        public static class Predicates
        {
            public static Predicate<WorkflowItem> byName(final String name)
            {
                return new ByNamePredicate(name);
            }

            private static class ByNamePredicate implements Predicate<WorkflowItem>
            {
                private final String name;

                public ByNamePredicate(final String name)
                {
                    this.name = name;
                }

                @Override
                public boolean apply(WorkflowItem input)
                {
                    return input.name().equals(name);
                }
            }

            public static Predicate<WorkflowItem> byDescription(final String description)
            {
                return new ByDescriptionPredicate(description);
            }

            public static Predicate<WorkflowItem> schemesEqual(final Iterable<String> schemes)
            {
                return new Predicate<WorkflowItem>()
                {
                    @Override
                    public boolean apply(WorkflowItem input)
                    {
                        return input.schemes().equals(schemes);
                    }
                };
            }

            private static class ByDescriptionPredicate implements Predicate<WorkflowItem>
            {
                private final String description;

                public ByDescriptionPredicate(String description)
                {
                    this.description = description;
                }

                @Override
                public boolean apply(final WorkflowItem input)
                {
                    return input.description().equals(description);
                }
            }
        }
    }

    @Immutable
    public static class WorkflowItem
    {
        private final String name;
        private final String lastModified;
        private final Multiset<String> schemes;
        private final int steps;
        private final boolean isDraft;
        private final String description;

        public WorkflowItem(String name, String description, String lastModified, Multiset<String> schemes, int steps, WorkflowState state)
        {
            this.name = name;
            this.description = description;
            this.lastModified = lastModified;
            this.schemes = schemes;
            this.steps = steps;
            this.isDraft = state.equals(WorkflowState.DRAFT);
        }

        public String name()
        {
            return name;
        }

        public String lastModified()
        {
            return lastModified;
        }

        public Multiset<String> schemes()
        {
            return schemes;
        }

        public int steps()
        {
            return steps;
        }

        public boolean isDraft()
        {
            return isDraft;
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder().
                    append(name()).
                    append(isDraft()).
                    toHashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) { return true; }

            if (!(obj instanceof WorkflowItem)) { return false; }

            final WorkflowItem rhs = (WorkflowItem) obj;

            return new EqualsBuilder().
                    append(name(), rhs.name()).
                    append(isDraft(), rhs.isDraft()).
                    isEquals();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this).
                    append("name", name).
                    append("description", description).
                    append("isDraft", isDraft()).
                    append("lastModified", lastModified()).
                    append("steps", steps()).
                    append("schemes", Iterables.toString(schemes())).
                    toString();
        }

        public String description()
        {
            return description;
        }
    }

    public enum WorkflowState
    {
        ACTIVE(), DRAFT(), INACTIVE()
    }
}
