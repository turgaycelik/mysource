package com.atlassian.jira.functest.framework;

import com.meterware.httpunit.WebForm;
import net.sourceforge.jwebunit.UnableToSetFormException;
import net.sourceforge.jwebunit.WebTester;
import org.xml.sax.SAXException;

public class FormImpl implements Form
{
    private WebTester webTester;

    public FormImpl(final WebTester webTester)
    {
        this.webTester = webTester;
    }

    public void selectOption(String selectName, String option) {
        webTester.assertFormElementPresent(selectName);
        webTester.selectOption(selectName, option);
    }

    public void selectOptionsByDisplayName(String selectName, String[] options)
    {
        webTester.assertFormElementPresent(selectName);

        checkFormStateWithParameter(selectName);
        
        if (options.length > 0)
        {
            webTester.getDialog().getForm().setParameter(selectName, getValuesForOptions(selectName, options));
        }
        else
        {
            webTester.getDialog().getForm().removeParameter(selectName);
        }
    }

    public void selectOptionsByValue(final String selectName, final String[] options)
    {
        webTester.assertFormElementPresent(selectName);

        checkFormStateWithParameter(selectName);

        if (options.length > 0)
        {
            webTester.getDialog().getForm().setParameter(selectName, options);
        }
        else
        {
            webTester.getDialog().getForm().removeParameter(selectName);
        }
    }

    /**
     * Converts a list of select box option labels to the underlying values.
     *
     * @param selectName name of the select box.
     * @param options labels of the options.
     * @return the list of values for these labels.
     */
    private String[] getValuesForOptions(String selectName, final String[] options)
    {
        final String[] values = new String[options.length];
        for (int i = 0; i < options.length; i++)
        {
            String option = options[i];
            // Get the value for the label of the option of the select box.
            values[i] = webTester.getDialog().getValueForOption(selectName, option);
        }
        return values;
    }

    private void checkFormStateWithParameter(String paramName) {
		if (webTester.getDialog().getForm() == null) {
			try {
				webTester.getDialog().setWorkingForm(getFormWithParameter(paramName).getID());
			} catch (UnableToSetFormException e) {
				throw new UnableToSetFormException("Unable to set form based on parameter [" + paramName + "].");
			}
		}
	}

	private WebForm getFormWithParameter(String paramName) {
        for (int i = 0; i < getForms().length; i++) {
            WebForm webForm = getForms()[i];
            String[] names = webForm.getParameterNames();
            for (String name : names)
            {
                if (name.equals(paramName))
                {
                    return webForm;
                }
            }
        }
        return null;
	}

    public WebForm[] getForms()
    {
        try
        {
            return webTester.getDialog().getResponse().getForms();
        }
        catch (SAXException ex)
        {
            throw new RuntimeException("Unable to get forms from the WebResponse.", ex);
        }
    }
}
