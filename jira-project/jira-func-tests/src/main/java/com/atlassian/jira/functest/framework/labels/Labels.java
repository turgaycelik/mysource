package com.atlassian.jira.functest.framework.labels;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.util.dom.DomKit;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that represents the state of labels - linked, lozenged, editable and the actual labels them selves.
 *
 * @since v4.2
 */
public class Labels
{
    private final boolean editable;
    private final boolean linked;
    private final boolean lozenges;
    private final Set<String> labelValues;

    public Labels(boolean editable, boolean linked, boolean lozenges, String... labelValues)
    {
        this(editable, linked, lozenges, new HashSet<String>(Arrays.asList(labelValues)));
    }

    public Labels(boolean editable, boolean linked, boolean lozenges, Set<String> labelValues)
    {
        this.editable = editable;
        this.linked = linked;
        this.lozenges = lozenges;
        this.labelValues = labelValues;
    }

    /**
     * Factory method for parsing the current page for the labels and returning the current state.
     *
     * @param tester  the webtester
     * @param issueId The id of the issue you want to get the labels for
     * @param fieldId The id of the field you want to get labels for.  Use 'labels' for system field.
     * @return The labels representing the current state of paly for teh given issue and field
     */
    public static Labels parseLabels(WebTester tester, String issueId, String fieldId)
    {
        final Set<String> labelValues = parseLabelValues(tester, issueId, fieldId);

        if (labelValues == null)
        {
            return null;
        }

        boolean editable = parseIsEditable(tester, issueId, fieldId);
        boolean linked = parseIsLinked(tester, issueId, fieldId);
        boolean lozenges = parseIsLozenges(tester, issueId, fieldId);

        return new Labels(editable, linked, lozenges, labelValues);
    }

    private static Set<String> parseLabelValues(WebTester tester, String issueId, String fieldId)
    {
        final IdLocator idLocator = new IdLocator(tester, fieldId + "-" + issueId + "-value");

        if (!idLocator.hasNodes())
        {
            return null;
        }
        if (idLocator.getNodes().length > 1)
        {
            //Shit!
            throw new RuntimeException("Id should only ever refer to one set of labels.");
        }
        final Node containerNode = idLocator.getNode();

        Set<String> labels = Collections.emptySet();
        // check for basic
        if (containerNode.getNodeName().equalsIgnoreCase("TD"))
        {

            final String labelValues = idLocator.getText().trim();

            final String[] strings = labelValues.split(",\\s");

            if (strings.length > 0 && !(strings.length == 1 && strings[0].equalsIgnoreCase("none")))
            {
                labels = new HashSet<String>(Arrays.asList(strings));
            }
        }
        else if (containerNode.getNodeName().equalsIgnoreCase("UL"))
        {
            final XPathLocator locator = new XPathLocator(tester, "//ul[@id='" + fieldId + "-" + issueId + "-value" + "']//a[contains(@class, 'lozenge')]");

            labels = new HashSet<String>();
            for (Node node : locator.getNodes())
            {
                labels.add(DomKit.getCollapsedText(node).trim());
            }

        }
        else if (containerNode.getNodeName().equalsIgnoreCase("span"))
        {
            // this should be empty
        }
        return labels;

    }

    private static boolean parseIsEditable(WebTester tester, String issueId, String fieldId)
    {
        Locator locator = new IdLocator(tester, "edit-labels-" + issueId + "-" + fieldId);

        return locator.getNodes().length > 0;
    }

    private static boolean parseIsLozenges(WebTester tester, String issueId, String fieldId)
    {
        XPathLocator pathLocator = new XPathLocator(tester, "//*[@id='" + fieldId + "-" + issueId + "-value']//a[contains(@class, 'lozenge')]");
        return pathLocator.exists();
    }

    private static boolean parseIsLinked(WebTester tester, String issueId, String fieldId)
    {

        XPathLocator pathLocator = new XPathLocator(tester, "//*[@id='" + fieldId + "-" + issueId + "-value']//a[contains(@class, 'nolink')]");

        if (pathLocator.exists())
        {
            return false;
        }
        pathLocator = new XPathLocator(tester, "//*[@id='" + fieldId + "-" + issueId + "-value']//a[contains(@class, 'lozenge')]");

        if (pathLocator.exists())
        {
            return true;
        }
        pathLocator = new XPathLocator(tester, "//*[@id='" + fieldId + "-" + issueId + "-value']//a");

        return pathLocator.exists();
    }

    public boolean isEditable()
    {
        return editable;
    }

    public boolean isLinked()
    {
        return linked;
    }

    public boolean isLozenges()
    {
        return lozenges;
    }

    public Set<String> getLabelValues()
    {
        return labelValues;
    }

    @Override
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

        Labels labels = (Labels) o;

        if (editable != labels.editable)
        {
            return false;
        }
        if (linked != labels.linked)
        {
            return false;
        }
        if (lozenges != labels.lozenges)
        {
            return false;
        }
        if (labelValues != null ? !labelValues.equals(labels.labelValues) : labels.labelValues != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (editable ? 1 : 0);
        result = 31 * result + (linked ? 1 : 0);
        result = 31 * result + (lozenges ? 1 : 0);
        result = 31 * result + (labelValues != null ? labelValues.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "Labels{" +
                "editable=" + editable +
                ", linked=" + linked +
                ", lozenges=" + lozenges +
                ", labelValues=" + labelValues +
                '}';
    }
}
