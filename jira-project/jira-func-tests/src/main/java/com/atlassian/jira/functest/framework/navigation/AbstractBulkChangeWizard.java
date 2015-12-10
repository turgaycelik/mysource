package com.atlassian.jira.functest.framework.navigation;

import java.util.EnumSet;

import com.atlassian.jira.functest.framework.FunctTestConstants;

/**
 * Abstract implementation of the {@link BulkChangeWizard}. Defines the state and operations of the wizard. Specific
 * details on how to drive the wizard are supplied by the implementations for func tests and selenium tests.
 *
 * @since v4.2
 */
public abstract class AbstractBulkChangeWizard implements BulkChangeWizard
{
    protected static final String SAME_FOR_ALL = "sameAsBulkEditBean";
    protected static final String BULK_EDIT_KEY = "10000_1_";
    protected static final String TARGET_PROJECT_ID = "10000_1_pid";
    protected static final String ISSUE_TYPE_SELECT = "10000_1_issuetype";
    protected static final String TARGET_PROJECT_ID_TEMPLATE = "10000_%d_pid";

    private WizardState state = WizardState.SELECT_ISSUES;
    private BulkOperations operation = null;

    public BulkChangeWizard selectAllIssues()
    {
        validateState(WizardState.SELECT_ISSUES);

        selectAllIssueCheckboxes();
        clickOnNext();

        state = WizardState.CHOOSE_OPERATION;

        return this;
    }

    public BulkChangeWizard chooseOperation(final BulkOperations operation)
    {
        validateState(WizardState.CHOOSE_OPERATION);

        chooseOperationRadioButton(operation);
        clickOnNext();

        this.operation = operation;

        if(operation.getRadioValue().equals(BulkOperationsImpl.MOVE.getRadioValue()))
        {
            state = WizardState.CHOOSE_TARGET_CONTEXTS;
        }
        else if(operation.getRadioValue().equals(BulkOperationsImpl.EDIT.getRadioValue()))
        {
            state = WizardState.SET_FIELDS;
        }
        else if(operation.getRadioValue().equals(BulkOperationsImpl.DELETE.getRadioValue()))
        {
            state = WizardState.CONFIRMATION;
        }


        return this;
    }

    public BulkChangeWizard chooseWorkflowTransition(BulkOperations workflowTransition)
    {
        validateState(WizardState.CHOOSE_OPERATION);

        chooseCustomRadioButton(FunctTestConstants.FIELD_WORKFLOW, workflowTransition.getRadioValue());
        clickOnNext();

        this.operation = workflowTransition;

        return this;
    }


    public BulkChangeWizard chooseTargetContextForAll(final String projectName)
    {
        return chooseTargetContextForAll(projectName, null);
    }

    public BulkChangeWizard chooseTargetContextForAll(String projectName, String issueType)
    {
        validateState(BulkOperationsImpl.MOVE, WizardState.CHOOSE_TARGET_CONTEXTS);

        // note that this only currently works when you are moving issues from Homosapien project, and when that is the
        // first project context offered on the page. Might need to fix it if the data is different!
        checkSameTargetForAllCheckbox();
        selectFirstTargetProject(projectName);
        if (issueType != null && !issueType.isEmpty()) {
            selectIssueType(issueType);
        }
        clickOnNext();

        state = WizardState.SET_FIELDS;

        return this;
    }

    public BulkChangeWizard chooseTargetContextForEach(final int numContextsToSelect, final String projectName)
    {
        validateState(BulkOperationsImpl.MOVE, WizardState.CHOOSE_TARGET_CONTEXTS);

        // note that this only currently works when you are moving issues from Homosapien project, and when that is the
        // only project context offered on the page. Might need to fix it if the data is different!
        selectEachTargetProject(numContextsToSelect, projectName);
        clickOnNext();

        state = WizardState.SET_FIELDS;

        return this;
    }


    public BulkChangeWizard setFieldValue(final String fieldName, final String value)
    {
        return setFieldValue(InputTypes.TEXT, fieldName, value);
    }

    public BulkChangeWizard setFieldValue(final InputTypes inputType, final String fieldName, final String value)
    {
        validateState(WizardState.SET_FIELDS);

        switch (inputType)
        {
            case SELECT:
                setSelectElement(fieldName, value);
                break;
            default:
                setTextElement(fieldName, value);
        }

        return this;
    }

    public BulkChangeWizard checkRetainForField(final String fieldName)
    {
        validateState(BulkOperationsImpl.MOVE, WizardState.SET_FIELDS);

        checkCheckbox("retain_" + fieldName);

        return this;
    }

    public BulkChangeWizard checkActionForField(final String fieldName)
    {
        validateState(BulkOperationsImpl.EDIT, WizardState.SET_FIELDS);

        checkCheckbox("actions", fieldName);

        return this;
    }

    public BulkChangeWizard finaliseFields()
    {
        validateState(WizardState.SET_FIELDS);

        clickOnNext();

        // check to see if we have any more fields to set
        if (operation == BulkOperationsImpl.MOVE || operation == BulkOperationsImpl.EDIT)
        {
            // this particular text appears at the top of the "Issue Fields" screen
            if (pageContainsText("Step 4 of 4: Confirmation"))
            {
                this.state = WizardState.CONFIRMATION;
            }
        }

        return this;
    }

    public BulkChangeWizard complete()
    {
        validateState(WizardState.CONFIRMATION);

        if (operation == BulkOperationsImpl.MOVE)
        {
            clickOnNext();
        }
        else if (operation == BulkOperationsImpl.DELETE || operation == BulkOperationsImpl.EDIT)
        {
            clickOnConfirm();
        }

        this.state = WizardState.COMPLETE;

        return this;
    }

    @Override
    public BulkChangeWizard revertTo(final WizardState state)
    {
        if(this.state.getStage() <= state.getStage()) {
            throw new IllegalStateException(String.format("Cannot revert from state: %s to state: %s: target state does"
                    + " not precede the current state.", this.state, state));
        }
        switch(state) {
            case CHOOSE_TARGET_CONTEXTS:
                 // This state is native only to move operation.
                if(!operation.equals(BulkOperationsImpl.MOVE))
                {
                    throw new IllegalStateException(illegalStateForOperation(state, this.operation));
                }
                break;
            case SET_FIELDS:
                if(EnumSet.of(BulkOperationsImpl.MOVE, BulkOperationsImpl.DELETE).contains(this.operation))
                {
                    throw new IllegalStateException(illegalStateForOperation(state, this.operation));
                }
                break;
            case SELECT_ISSUES:
            case CHOOSE_OPERATION:
                this.operation = null;
                break;
            case COMPLETE:
            case CONFIRMATION:
                throw new IllegalStateException(String.format("Cannot revert to state: %s.", state));
        }
        clickOnLinkWithText(state.getLinkText());
        this.state = state;
        return this;
    }

    @Override
    public BulkChangeWizard cancel()
    {
        clickOnLinkId("cancel");
        this.state = WizardState.COMPLETE;
        return this;
    }

    private String illegalStateForOperation(final WizardState state, final BulkOperations operation)
    {
        return String.format("Cannot revert to: %s: Operation %s does not this state.", state,  operation);
    }

    protected abstract void clickOnNext();

    protected abstract void clickOnConfirm();

    protected abstract void clickOnLinkId(String id);

    protected abstract void clickOnLinkWithText(String linkText);

    protected abstract void selectAllIssueCheckboxes();

    protected abstract void chooseOperationRadioButton(BulkOperations operation);

    protected abstract void chooseCustomRadioButton(String radiogroupName, String radiobuttonValue);

    protected abstract void selectFirstTargetProject(String projectName);

    protected abstract void selectIssueType(String issueType);

    protected abstract void selectEachTargetProject(int numContextsToSelect, String projectName);

    protected abstract void checkSameTargetForAllCheckbox();

    protected abstract void setTextElement(String fieldName, String value);

    protected abstract void setSelectElement(String fieldName, String value);

    protected abstract void checkCheckbox(String fieldName);

    protected abstract void checkCheckbox(final String checkboxName, final String value);

    protected abstract boolean pageContainsText(String text);

    private void validateState(final WizardState expectedState)
    {
        if (this.state != expectedState || this.state == WizardState.COMPLETE)
        {
            throw new IllegalStateException("Wizard is in invalid state. Expected state: " + expectedState + "; actual state: " + state.toString());
        }
    }

    private void validateState(final BulkOperations expectedOperation, final WizardState expectedState)
    {
        if (this.operation != expectedOperation)
        {
            throw new IllegalStateException("Wizard is in invalid state. Expected operation: " + expectedOperation + "; actual operation: " + expectedOperation.toString());
        }

        validateState(expectedState);
    }

    public WizardState getState()
    {
        return state;
    }
}
