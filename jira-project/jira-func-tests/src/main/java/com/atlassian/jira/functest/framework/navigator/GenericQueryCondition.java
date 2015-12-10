package com.atlassian.jira.functest.framework.navigator;

import com.meterware.httpunit.WebForm;
import junit.framework.Assert;
import net.sourceforge.jwebunit.HttpUnitDialog;
import net.sourceforge.jwebunit.WebTester;
import org.jsoup.nodes.Document;

/**
 * @since v4.0
 */
public class GenericQueryCondition implements NavigatorCondition
{
    private final String elementName;
    private String query;

    public GenericQueryCondition(final String elementName)
    {
        this.elementName = elementName;
    }


    public GenericQueryCondition setQuery(final String q)
    {
        this.query = q;
        return this;
    }

    public void parseCondition(final WebTester tester)
    {
        final WebForm form = tester.getDialog().getForm();
        query = form.getParameterValue(elementName);
    }

    public void assertSettings(final Document document)
    {
        Assert.assertEquals("Value not set correctly for element: " + elementName, query, document.getElementsByAttributeValue("name", elementName).get(0).val());
    }

    @Override
    public String toString()
    {
        return String.format("[%s: %s]", elementName, query);
    }

    public NavigatorCondition copyCondition()
    {
        throw new UnsupportedOperationException();
    }

    public NavigatorCondition copyConditionForParse()
    {
        throw new UnsupportedOperationException();
    }
}
