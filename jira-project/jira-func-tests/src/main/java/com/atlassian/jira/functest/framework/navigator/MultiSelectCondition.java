package com.atlassian.jira.functest.framework.navigator;

import com.meterware.httpunit.WebForm;
import junit.framework.Assert;
import net.sourceforge.jwebunit.HttpUnitDialog;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Represents a navigator condition in a multi-select box.
 *
 * @since v3.13
 */
public abstract class MultiSelectCondition implements NavigatorCondition
{
    private List<String> options = new ArrayList<String>();
    private final String elementName;

    protected MultiSelectCondition(String elementName)
    {
        this.elementName = elementName;
    }

    protected MultiSelectCondition(String elementName, Collection<String> options)
    {
        this(elementName);
        addOptions(options);
    }

    protected MultiSelectCondition(MultiSelectCondition condition)
    {
        this(condition.elementName, condition.options);
    }

    public void setOptions(Collection<String> options)
    {
        this.options = options == null ? new ArrayList<String>() : new ArrayList<String>(options);
    }

    public boolean addOption(String option)
    {
        return option != null && options.add(option);
    }

    public boolean removeOption(String option)
    {
        return option != null && options.remove(option);
    }

    public void clearOptions()
    {
        options.clear();
    }

    public void addOptions(Collection<String> options)
    {
        if (options != null)
        {
            this.options.addAll(options);
        }
    }

    public void removeOptions(Collection<String> options)
    {
        if (options != null)
        {
            this.options.removeAll(options);
        }
    }

    public List<String> getOptions()
    {
        return options;
    }

    public String getElementName()
    {
        return elementName;
    }

    public void parseCondition(WebTester tester)
    {
        final WebForm form = tester.getDialog().getForm();
//        setOptions(getSetOptions(form));
    }

    public void assertSettings(final Document document)
    {
        Document select = Jsoup.parse(document.getElementsByAttributeValue("name", elementName).html());
        Elements selected = select.getElementsByAttributeValue("selected", "selected");
        List<String> vals = new ArrayList<String>();
        for (Element element : selected)
        {
            vals.add(element.text().trim());
        }
        assertEquals("Value not set correctly for element: " + elementName, options, vals);
    }


    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        MultiSelectCondition that = (MultiSelectCondition) o;

        if (elementName != null ? !elementName.equals(that.elementName) : that.elementName != null)
        {
            return false;
        }
        if (options != null ? !options.equals(that.options) : that.options != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (options != null ? options.hashCode() : 0);
        result = 31 * result + (elementName != null ? elementName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "MultiSelectCondition{" +
                "elementName='" + elementName + '\'' +
                ", options=" + options +
                '}';
    }
}
