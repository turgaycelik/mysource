package com.atlassian.jira.functest.framework;

import com.meterware.httpunit.WebForm;

/**
 * Used to set form values in tests.
 */
public interface Form
{
    /**
     * Select an option with a given display value in a select element.
     *
     * @param selectName name of select element.
     * @param option display value of option to be selected.
     */
    public void selectOption(String selectName, String option);

    /**
     * Select multiple options with given display values in a select element.
     *
     * @param selectName name of select element.
     * @param options display values of options to be selected.
     */
    public void selectOptionsByDisplayName(String selectName, String[] options);

    /**
     * Select multiple options with given values in a select element.
     *
     * @param selectName name of select element.
     * @param options values of options to be selected.
     */
    public void selectOptionsByValue(String selectName, String[] options);

    /**
     * Returns all the forms in the current response.
     * @return all the forms in the current response.
     */
    public WebForm[] getForms();    
}
