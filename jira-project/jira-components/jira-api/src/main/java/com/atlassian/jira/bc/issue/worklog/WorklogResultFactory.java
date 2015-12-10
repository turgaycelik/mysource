package com.atlassian.jira.bc.issue.worklog;

import com.atlassian.jira.issue.worklog.Worklog;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A static factory class for instantiating {@link com.atlassian.jira.bc.issue.worklog.WorklogResult} objects.
 *
 * @since v4.2
 */
public class WorklogResultFactory
{
    /**
     * @param worklog the worklog
     * @return Creates a simple {@link WorklogResult} that just contains the {@link Worklog} to use. The editable check will be required.
     */
    public static WorklogResult create(final Worklog worklog)
    {
        return create(worklog, true);
    }

    /**
     * @param worklog the worklog
     * @param isEditableCheckRequired flag to set on the result
     * @return Creates a simple {@link WorklogResult} that just contains the {@link Worklog} to use and the flag for the editable check.
     */
    public static WorklogResult create(final Worklog worklog, final boolean isEditableCheckRequired)
    {
        return new WorklogResultImpl(worklog, isEditableCheckRequired, null, null);
    }

    /**
     * Used for copying the {@link WorklogResult} received from the base <code>validate</code> method and adding on the
     * <code>newEstimate</code>.
     *
     * @param worklogResult the worklog result to copy; must not be null.
     * @param newEstimate the new estimate in seconds
     * @return Creates a {@link WorklogResult} that contains the {@link Worklog} and the new estimate. The editable check will be required.
     */
    public static WorklogNewEstimateResult createNewEstimate(final WorklogResult worklogResult, final Long newEstimate)
    {
        notNull("worklogResult", worklogResult);
        return createNewEstimate(worklogResult.getWorklog(), newEstimate, worklogResult.isEditableCheckRequired());
    }

    /**
     * Used for creating {@link WorklogResult} objects that also capture a <code>newEstimate</code> to be set.
     *
     * @param worklog the worklog
     * @param newEstimate the new estimate in seconds
     * @return Creates a {@link WorklogResult} that contains the {@link Worklog} and the new estimate. The editable check will be required.
     */
    public static WorklogNewEstimateResult createNewEstimate(final Worklog worklog, final Long newEstimate)
    {
        return createNewEstimate(worklog, newEstimate, true);
    }

    /**
     * Used for creating {@link WorklogResult} objects that also capture a <code>newEstimate</code> to be set.
     *
     * @param worklog the worklog
     * @param newEstimate the new estimate in seconds
     * @param isEditableCheckRequired flag to set on the result
     * @return Creates a {@link WorklogResult} that contains the {@link Worklog}, the new estimate and the flag for the editable check.
     */
    public static WorklogNewEstimateResult createNewEstimate(final Worklog worklog, final Long newEstimate, final boolean isEditableCheckRequired)
    {
        return new WorklogResultImpl(worklog, isEditableCheckRequired, newEstimate, null);
    }

    /**
     * Used for copying the {@link WorklogResult} received from the base <code>validate</code> method and adding on the
     * <code>adjustmentAmount</code>.
     *
     * @param worklogResult the worklog result to copy; must not be null.
     * @param adjustmentAmount the adjustment amount in seconds
     * @return Creates a {@link WorklogResult} that contains the {@link Worklog} and the adjustment amount. The editable check will be required.
     */
    public static WorklogAdjustmentAmountResult createAdjustmentAmount(final WorklogResult worklogResult, final Long adjustmentAmount)
    {
        notNull("worklogResult", worklogResult);
        return createAdjustmentAmount(worklogResult.getWorklog(), adjustmentAmount, worklogResult.isEditableCheckRequired());
    }

    /**
     * Used for creating {@link WorklogResult} objects that also capture a <code>adjustmentAmount</code> to be set.
     *
     * @param worklog the worklog
     * @param adjustmentAmount the adjustment amount in seconds
     * @return Creates a {@link WorklogResult} that contains the {@link Worklog} and the adjustment amount. The editable check will be required.
     */
    public static WorklogAdjustmentAmountResult createAdjustmentAmount(final Worklog worklog, final Long adjustmentAmount)
    {
        return createAdjustmentAmount(worklog, adjustmentAmount, true);
    }

    /**
     * Used for creating {@link WorklogResult} objects that also capture a <code>adjustmentAmount</code> to be set.
     *
     * @param worklog the worklog
     * @param adjustmentAmount the adjustment amount in seconds
     * @param isEditableCheckRequired flag to set on the result
     * @return Creates a {@link WorklogResult} that contains the {@link Worklog}, the adjustment amount and the flag for the editable check.
     */
    public static WorklogAdjustmentAmountResult createAdjustmentAmount(final Worklog worklog, final Long adjustmentAmount, final boolean isEditableCheckRequired)
    {
        return new WorklogResultImpl(worklog, isEditableCheckRequired, null, adjustmentAmount);
    }

    /**
     * Immutable implementation class of {@link WorklogResult}.
     *
     * @since v4.2
     */
    private static class WorklogResultImpl implements WorklogResult, WorklogNewEstimateResult, WorklogAdjustmentAmountResult
    {
        private final Worklog worklog;
        private final Long newEstimate;
        private final Long adjustmentAmount;
        private final boolean editableCheckRequired;

        private WorklogResultImpl(final Worklog worklog, final boolean editableCheckRequired, final Long newEstimate, final Long adjustmentAmount)
        {
            this.adjustmentAmount = adjustmentAmount;
            this.newEstimate = newEstimate;
            this.worklog = worklog;
            this.editableCheckRequired = editableCheckRequired;
        }

        public Long getAdjustmentAmount()
        {
            return adjustmentAmount;
        }

        public Long getNewEstimate()
        {
            return newEstimate;
        }

        public Worklog getWorklog()
        {
            return worklog;
        }

        public boolean isEditableCheckRequired()
        {
            return editableCheckRequired;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final WorklogResultImpl that = (WorklogResultImpl) o;

            if (editableCheckRequired != that.editableCheckRequired)
            {
                return false;
            }
            if (adjustmentAmount != null ? !adjustmentAmount.equals(that.adjustmentAmount) : that.adjustmentAmount != null)
            {
                return false;
            }
            if (newEstimate != null ? !newEstimate.equals(that.newEstimate) : that.newEstimate != null)
            {
                return false;
            }
            if (worklog != null ? !worklog.equals(that.worklog) : that.worklog != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = worklog != null ? worklog.hashCode() : 0;
            result = 31 * result + (newEstimate != null ? newEstimate.hashCode() : 0);
            result = 31 * result + (adjustmentAmount != null ? adjustmentAmount.hashCode() : 0);
            result = 31 * result + (editableCheckRequired ? 1 : 0);
            return result;
        }
    }
}

