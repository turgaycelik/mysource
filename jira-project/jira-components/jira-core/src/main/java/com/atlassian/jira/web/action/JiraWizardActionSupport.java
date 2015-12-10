package com.atlassian.jira.web.action;

import com.atlassian.jira.util.ParameterUtils;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;


public abstract class JiraWizardActionSupport extends JiraWebActionSupport
{
    protected int currentStep = 1;
    protected String submitBtn;
    protected String finishButton;
    private static final String BUTTON_NAME_NEXT = "nextBtn";
    private static final String BUTTON_NAME_PREVIOUS = "previousBtn";


    protected String doExecute() throws Exception
    {
        if (isPreviousClicked())
        {
            currentStep--;
        }
        else if(!isFinishClicked() && isNextClicked())
        {
            currentStep++;
        }

        return super.doExecute();
    }

    protected boolean isPreviousClicked()
    {

        return isButtonClickedByName(BUTTON_NAME_PREVIOUS);
    }

    protected boolean isNextClicked()
    {
        return isButtonClickedByName(BUTTON_NAME_NEXT);
    }
    protected boolean isFinishClicked()
    {
        return isNextClicked() && getCurrentStep() >= getTotalSteps();
    }

    protected boolean isButtonClickedByValue(String buttonValue)
    {
        String str = getSubmitBtn();
        if (str == null)
        {
            return false;
        }
        return (str.toLowerCase().contains(buttonValue.toLowerCase()));
    }

    protected boolean isButtonClickedByName(String name) {
        return StringUtils.isNotBlank(ParameterUtils.getStringParam(ActionContext.getParameters(), name));
    }

    public abstract int getTotalSteps();

    // ----------------------------------------------------------------- Simple Accessors & Mutators

    public int getCurrentStep()
    {
        return currentStep;
    }

    public void setCurrentStep(int currentStep)
    {
        this.currentStep = currentStep;
    }

    public String getSubmitBtn()
    {
        return submitBtn;
    }

    public void setSubmitBtn(String submitBtn)
    {
        this.submitBtn = submitBtn;
    }

    public String getFinishButton()
    {
        return finishButton;
    }

    public void setFinishButton(String finishButton)
    {
        this.finishButton = finishButton;
    }

}
