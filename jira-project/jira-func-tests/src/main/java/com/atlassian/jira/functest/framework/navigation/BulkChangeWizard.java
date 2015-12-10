package com.atlassian.jira.functest.framework.navigation;

import java.util.EnumSet;

/**
 * Interface for working with the Bulk Change Wizard which is part of the Issue Navigator.
 * <p/>
 * Promotes a "fluent-style" of usage by returning the stateful object as the result of each operation.
 * 
 * @see IssueNavigatorNavigation#bulkChange(com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation.BulkChangeOption)
 * @since v4.2
 */
public interface BulkChangeWizard
{
    /**
     * States that the wizard can be in.
     */
    enum WizardState {
        SELECT_ISSUES(1, "Choose Issues"),
        CHOOSE_OPERATION(2, "Choose Operation"),
        CHOOSE_TARGET_CONTEXTS(3, "Operation Details"),
        SET_FIELDS(3, "Operation Details"),
        CONFIRMATION(4, "Confirmation"),
        COMPLETE(5, null);

        public static EnumSet<WizardState> valuesWithLinks()
        {
            return EnumSet.complementOf(EnumSet.of(COMPLETE));
        }

        private final int stage;
        private final String linkText;

        WizardState(final int stage, final String linkText)
        {
            this.stage = stage;
            this.linkText = linkText;
        }

        public int getStage()
        {
            return stage;
        }

        public String getLinkText()
        {
            return linkText;
        }
    }

    interface BulkOperations
    {
        String getRadioValue();
    }
    /**
     * Various operations available in the bulk change wizard.
     */
    enum BulkOperationsImpl implements BulkOperations
    {
        MOVE("bulk.move.operation.name"),
        EDIT("bulk.edit.operation.name"),
        TRANSITION("bulk.workflowtransition.operation.name"),
        DELETE("bulk.delete.operation.name");

        private final String radioValue;

        private BulkOperationsImpl(final String radioValue)
        {
            this.radioValue = radioValue;
        }

        /**
         * @return the value of the radio option that corresponds with this operation.
         */
        public String getRadioValue()
        {
            return radioValue;
        }
    }

    class BulkOperationsCustom implements BulkOperations
    {
        private final String radioValue;

        public BulkOperationsCustom(String radioValue)
        {
            this.radioValue = radioValue;
        }

        @Override
        public String getRadioValue()
        {
            return radioValue;
        }
    }

    /**
     * Types of inputs for the editing controls in a Bulk Change Wizard form
     */
    enum InputTypes { TEXT, SELECT }

    /**
     * Selects all issues from the last search to operate on.
     * <p/>
     * Wizard must be in {@link BulkChangeWizard.WizardState#SELECT_ISSUES} to perform this operation.
     * <p/>
     * Once selected, the wizard will advance to {@link BulkChangeWizard.WizardState#CHOOSE_OPERATION}.
     *
     * @return the wizard
     */
    BulkChangeWizard selectAllIssues();

    /**
     * Select the bulk operation to perform.
     * <p/>
     * Wizard must be in {@link BulkChangeWizard.WizardState#CHOOSE_OPERATION} to perform this operation.
     * <p/>
     * Once selected, the wizard will advance to {@link BulkChangeWizard.WizardState#CHOOSE_TARGET_CONTEXTS}.
     *
     * @param operation the operation to perform
     * @return the wizard
     */
    BulkChangeWizard chooseOperation(BulkOperations operation);

    /**
     * Select the workflow transition to perform.
     * <p/>
     * Wizard must be in {@link BulkChangeWizard.WizardState#CHOOSE_OPERATION} to perform this operation.
     * <p/>
     * Once selected, the wizard will advance
     *
     * @param workflowTransition
     * @return
     */
    BulkChangeWizard chooseWorkflowTransition(BulkOperations workflowTransition);

    /**
     * Chooses the target project for all issues to be moved to. The target issue type will remain as the default selected
     * item. This target context will apply to all issues being moved.
     * <p/>
     * <strong>WARN:</strong> note that this only currently works when you are moving issues from Homosapien project,
     * and when that is the only source project context offered on the page.
     * <p/>
     * Wizard must be in {@link BulkOperationsImpl#MOVE}, {@link BulkChangeWizard.WizardState#CHOOSE_TARGET_CONTEXTS} to
     * perform this operation.
     * <p/>
     * Once selected, the wizard will advance to {@link BulkChangeWizard.WizardState#SET_FIELDS}.
     *
     * @param projectName the name of the project to use in the target context e.g. <code>monkey</code>
     * @return the wizard
     */
    BulkChangeWizard chooseTargetContextForAll(String projectName);

    /**
     * Chooses the target project and issue type for all issues to be moved to. The target issue type will remain as the default selected
     * item. This target context will apply to all issues being moved.
     * <p/>
     * <strong>WARN:</strong> note that this only currently works when you are moving issues from Homosapien project,
     * and when that is the only source project context offered on the page.
     * <p/>
     * Wizard must be in {@link BulkOperationsImpl#MOVE}, {@link BulkChangeWizard.WizardState#CHOOSE_TARGET_CONTEXTS} to
     * perform this operation.
     * <p/>
     * Once selected, the wizard will advance to {@link BulkChangeWizard.WizardState#SET_FIELDS}.
     *
     * @param projectName the name of the project to use in the target context e.g. <code>monkey</code>
     * @return the wizard
     */
    BulkChangeWizard chooseTargetContextForAll(String projectName, String issueType);

    /**
     * Chooses the target project to be moved to for each of the issue types to be moved. This target context will
     * apply to all issues being moved, preserving issue types mapping.
     * <p/>
     * <strong>WARN:</strong> note that this only currently works when you are moving issues from Homosapien project,
     * and when that is the only source project context offered on the page.
     * <p/>
     * Wizard must be in {@link BulkOperationsImpl#MOVE}, {@link BulkChangeWizard.WizardState#CHOOSE_TARGET_CONTEXTS} to
     * perform this operation.
     * <p/>
     * Once selected, the wizard will advance to {@link BulkChangeWizard.WizardState#SET_FIELDS}.
     *
     * @param projectName the name of the project to use in the target context e.g. <code>monkey</code>
     * @param numContextsToSelect the expected number of contexts to be set.
     * @return the wizard
     */
    BulkChangeWizard chooseTargetContextForEach(int numContextsToSelect, String projectName);

    /**
     * Set a value for a field. It is assumed that the field is settable via text input.
     * <p/>
     * Wizard must be in {@link BulkChangeWizard.WizardState#SET_FIELDS} to perform this operation.
     * <p/>
     * Note: wizard does not advance after this operation; multiple calls to this method can be made. Once finished, call
     * {@link #finaliseFields()}.
     *
     * @param fieldName the name of the field to set e.g. <code>components</code>, <code>timetracking_originalestimate</code>
     * @param value the value to set e.g. <code>10000</code>, <code>5h</code>
     * @return the wizard
     * @see #finaliseFields()
     * @see #setFieldValue(com.atlassian.jira.functest.framework.navigation.BulkChangeWizard.InputTypes, String, String)
     */
    BulkChangeWizard setFieldValue(String fieldName, String value);

    /**
     * Set a value for a field.
     * <p/>
     * Wizard must be in {@link BulkChangeWizard.WizardState#SET_FIELDS} to perform this operation.
     * <p/>
     * Note: wizard does not advance after this operation; multiple calls to this method can be made. Once finished, call
     * {@link #finaliseFields()}.
     *
     * @param inputType the type of control you are using to set the field value
     * @param fieldName the name of the field to set e.g. <code>components</code>, <code>timetracking_originalestimate</code>
     * @param value the value to set e.g. <code>10000</code>, <code>5h</code>
     * @return the wizard
     * @see #finaliseFields()
     */
    BulkChangeWizard setFieldValue(InputTypes inputType, String fieldName, String value);

    /**
     * Check the "Retain" checkbox for the chosen field.
     * <p/>
     * Wizard must be in {@link BulkOperationsImpl#MOVE}, {@link BulkChangeWizard.WizardState#SET_FIELDS} to perform this operation.
     * <p/>
     * Note: wizard does not advance after this operation; multiple calls to this method can be made. Once finished, call
     * {@link #finaliseFields()}.
     *
     * @param fieldName the name of the field
     * @return the wizard
     */
    BulkChangeWizard checkRetainForField(String fieldName);

    /**
     * Check the "Action" checkbox for the chosen field.
     * <p/>
     * Wizard must be in {@link BulkOperationsImpl#EDIT}, {@link BulkChangeWizard.WizardState#SET_FIELDS} to perform this operation.
     * <p/>
     * Note: wizard does not advance after this operation; multiple calls to this method can be made. Once finished, call
     * {@link #finaliseFields()}.
     *
     * @param fieldName the name of the field
     * @return the wizard
     */
    BulkChangeWizard checkActionForField(String fieldName);

    /**
     * Completes the entering of fields in this screen.
     * <p/>
     * Wizard must be in {@link BulkChangeWizard.WizardState#SET_FIELDS} to perform this operation.
     * <p/>
     * If there are more field screens to complete, the wizard will remain in {@link BulkChangeWizard.WizardState#SET_FIELDS}.
     * Otherwise, it will advance to {@link BulkChangeWizard.WizardState#CONFIRMATION}.
     *
     * @return the wizard
     */
    BulkChangeWizard finaliseFields();

    /**
     * Completes the wizard and performs the bulk operation.
     * <p/>
     * Wizard must be in {@link BulkChangeWizard.WizardState#CONFIRMATION} to perform this operation.
     * <p/>
     * Once performed, the wizard will advance to {@link BulkChangeWizard.WizardState#COMPLETE}. There is no further
     * state to advance to after this. This wizard instance should not be used again.
     *
     * @return the wizard
     */
    BulkChangeWizard complete();

    /**
     * Reverts the wizard to any given previous stage.
     * <p/>
     * Wizard must be in any state that is further than the requested state.
     * <p/>
     * Once performed, wizard will revert to the desired state.
     *
     * @return the wizard.
     */
    BulkChangeWizard revertTo(WizardState state);

    /**
     * Cancels the wizard. Simulates clicking on the cancel link.
     * <p/>
     * Wizard must be in any state except {@link BulkChangeWizard.WizardState#COMPLETE}.
     * <p/>
     * Once performed the wizard will set itself to state {@link BulkChangeWizard.WizardState#COMPLETE}.
     *
     * @return the wizard.
     * @see #complete()
     */
    BulkChangeWizard cancel();

    /**
     * Returns the current state of the wizard.
     * @return the state of the wizard.
     */
    WizardState getState();
}
