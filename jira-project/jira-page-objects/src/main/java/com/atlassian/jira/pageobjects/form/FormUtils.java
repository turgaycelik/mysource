package com.atlassian.jira.pageobjects.form;

import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.openqa.selenium.By;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.stripToNull;

/**
 * Some utilities for dealing with forms.
 *
 * @since v5.0.1
 */
public class FormUtils
{
    /**
     * Set the value of an element. Will do nothing if value is null.
     *
     * @param element the element whose value should be changed.
     * @param value the value to set the element to.
     */
    public static void setElement(final PageElement element, final String value)
    {
        if (value != null)
        {
            element.clear();
            if (isNotBlank(value))
            {
                element.type(value);
            }
        }
    }

    /**
     * Return a mapping of the errors currently on the form. The mapping if from parameterName -> error.
     *
     * @param formElement the form.
     * @return a mapping from parameterName -> error of all the errors currently on the form.
     */
    public static Map<String, String> getAuiFormErrors(PageElement formElement)
    {
        Map<String, String> errors = Maps.newLinkedHashMap();
        List<PageElement> errorNodes = formElement.findAll(By.cssSelector("div.error"));

        for (PageElement errorNode : errorNodes)
        {
            final String attribute = stripToNull(errorNode.getAttribute("data-field"));
            if (attribute != null)
            {
                errors.put(attribute, stripToNull(errorNode.getText()));
            }
        }

        return errors;
    }

    /**
     * Return the global error on the passed form. Global errors are not associated with a field.
     *
     * @param formElement the form.
     * @return a list of global errors stripped of any whitespace.
     */
    public static List<String> getAuiFormGlobalErrors(PageElement formElement)
    {
        LinkedList<String> errors = Lists.newLinkedList();
        List<PageElement> errorNodes = formElement.findAll(By.cssSelector("div.error"));

        for (PageElement errorNode : errorNodes)
        {
            final String attribute = stripToNull(errorNode.getAttribute("data-field"));
            if (attribute == null)
            {
                errors.add(stripToNull(errorNode.getText()));
            }
        }

        return errors;
    }
}
