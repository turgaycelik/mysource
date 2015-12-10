package com.atlassian.jira.functest.framework.util.form;


import com.atlassian.jira.functest.framework.util.dom.DomNodeCopier;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebRequest;
import net.sourceforge.jwebunit.HttpUnitDialog;
import net.sourceforge.jwebunit.TestContext;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import static com.atlassian.jira.util.dbc.Assertions.stateTrue;

import java.io.IOException;
import java.util.Map;

/**
 * This class is used to modify form requests that are to be submitted It does this by modifying the request on a form
 * before submitting it.  If you attempt to resunbmit a form then an assertion error ewill be thrown.  also note that for
 * duration from construction to form submission the validation of parameter values in the test is disabled.
 * You have to work with the returned DOM, rather than with the tester, as the state of the
 * request and response objects will not be in sync
 *
 * @since v4.2
 */
public class FormParameterUtil
{
    private final String formIdentifierOrName;
    private final TestContext context;
    private final HttpUnitDialog dialog;
    private final WebRequest request;
    boolean isNotSubmitted = true;

    /**
     * @param tester the currently executing WebTester
     * @param formIdentifierOrName either the id or the name of the form you want to modify
     * @param submitButtonName the name of the submit button, if null then the default button is used
     */
    public FormParameterUtil(WebTester tester, String formIdentifierOrName, String submitButtonName)
    {
        HttpUnitOptions.setParameterValuesValidated(false);
        this.formIdentifierOrName = formIdentifierOrName;
        this.context = tester.getTestContext();
        this.dialog = tester.getDialog();
        dialog.setWorkingForm(formIdentifierOrName);
        if (StringUtils.isNotBlank(submitButtonName))
        {
            request = dialog.getForm().getRequest(submitButtonName);
        }
        else
        {
            request = dialog.getForm().getRequest();
        }
    }

    /**
     * allows you to add options to an existing select HTML element
     *
     * @param select the name of the select to which to add an option
     * @param value a string array containing the option values you want to submit
     */
    public void addOptionToHtmlSelect(String select, String[] value)
    {
        if (value != null)
        {
            String[] vals = dialog.getOptionValuesFor(select);
            String[] modifiedVals = new String[vals.length + value.length];
            System.arraycopy(vals, 0, modifiedVals, 0, vals.length);
            System.arraycopy(value, 0, modifiedVals, vals.length, value.length);
            request.setParameter(select, modifiedVals);
        }
    }

    /**
     * allows you to replace the options in an existing select HTML element
     *
     * @param select the name of the select to which to repalce all options
     * @param value a string array containing the option values you want to submit
     */
    public void replaceOptionsinHtmlSelect(String select, String[] value)
    {
        if (value != null)
        {
            request.setParameter(select, value);
        }
    }

    /**
     * allows you to replace the options in an existing select HTML element
     *
     * @param formElementName the name of the formElement
     * @param value a string  containing the value you want to submit
     */
    public void setFormElement(String formElementName, String value)
    {
        if (value != null)
        {
            request.setParameter(formElementName,value);
        }
    }

    /**
     * allows you to set the value of any parameter in the request
     *
     * @param parameters A map containing the names and values of the parmeters to be submited
     */
    public void setParameters(Map<String,String[]> parameters)
    {
        for (String param : parameters.keySet())
        {
            request.setParameter(param, parameters.get(param));
        }
    }

    /**
     * @return The DOM Document that represents the returned response.  To guarantee lower case the {@link
     *         DomNodeCopier} is used to return a shallow copy of the Document
     */
    public Node submitForm()
    {
        stateTrue("form has not been submitted",isNotSubmitted);
        try
        {
            DomNodeCopier copier = new DomNodeCopier(dialog.getWebClient().sendRequest(request).getDOM(), true);
            if (copier != null)
            {
                return copier.getCopiedNode();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            isNotSubmitted = false;
            HttpUnitOptions.setParameterValuesValidated(true);
        }
        return null;
    }
}
