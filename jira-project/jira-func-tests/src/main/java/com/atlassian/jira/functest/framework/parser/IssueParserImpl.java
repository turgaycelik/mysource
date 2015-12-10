package com.atlassian.jira.functest.framework.parser;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.changehistory.ChangeHistoryList;
import com.atlassian.jira.functest.framework.changehistory.ChangeHistoryParser;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.parser.comment.Comment;
import com.atlassian.jira.functest.framework.parser.comment.CommentParser;
import com.atlassian.jira.functest.framework.parser.issue.ViewIssueDetails;
import com.atlassian.jira.functest.framework.parser.worklog.Worklog;
import com.atlassian.jira.functest.framework.parser.worklog.WorklogParser;
import com.atlassian.jira.functest.framework.util.dom.DomKit;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class IssueParserImpl extends AbstractFuncTestUtil implements IssueParser
{
    public IssueParserImpl(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel)
    {
        super(tester, environmentData, logIndentLevel);
    }

    public ViewIssueDetails parseViewIssuePage()
    {
        ViewIssueDetails viewIssueDetails = new ViewIssueDetails();

        // Get the issue details
        parseIssueDetails(viewIssueDetails);

        // AvailableWorkflowActions
        viewIssueDetails.setAvailableWorkflowActions(parseAvailableWorkflowActions(tester));

        viewIssueDetails.setComponents(parseComponents());

        viewIssueDetails.setAffectsVersions(parseAffectsVersions());

        viewIssueDetails.setFixVersions(parseFixVersions());

        viewIssueDetails.setSecurityLevel(parseSecurityLevel());

        viewIssueDetails.setProjectName(parseProjectName());

        viewIssueDetails.setSummary(parseSummary());

        viewIssueDetails.setDueDate(parseDueDate());

        viewIssueDetails.setCreatedDate(parseCreatedDate());

        viewIssueDetails.setUpdatedDate(parseUpdatedDate());

        viewIssueDetails.setResolutionDate(parseResolutionDate());

        viewIssueDetails.setOriginalEstimate(parseOriginalEstimate());

        viewIssueDetails.setRemainingEstimate(parseRemaininglEstimate());

        viewIssueDetails.setTimeSpent(parseTimeSpent());

        viewIssueDetails.setAttachments(parseFileAttachments());

        viewIssueDetails.setEnvironment(parseEnvironment());

        viewIssueDetails.setCustomFields(parseCustomFields());

        viewIssueDetails.setDescription(parseDescription());

        viewIssueDetails.setLabels(parseLabels(viewIssueDetails.getId()));

        return viewIssueDetails;
    }

    private List<String> parseLabels(int issueId)
    {
        final List<String> labels = new ArrayList<String>();
        final String value = getValue("labels-" + issueId + "-value");
        if (value == null)
        {
            throw new IllegalStateException("no labels node in page for issue " + issueId);
        }
        final String[] labelStrings = value.split(" ");
        final List<String> strings = new ArrayList<String>(Arrays.asList(labelStrings));
        strings.remove("Edit");
        strings.remove("Labels");
        labels.addAll(strings);
        return labels;
    }

    private void parseIssueDetails(final ViewIssueDetails viewIssueDetails)
    {

        // Now grab the properties from the map
        viewIssueDetails.setKey(getValue("key-val"));
        viewIssueDetails.setId(Integer.valueOf(getAttribute("key-val", "rel")));
        viewIssueDetails.setIssueType(getValue("type-val"));
        viewIssueDetails.setStatus(getValue("status-val"));
        viewIssueDetails.setPriority(getValue("priority-val"));
        viewIssueDetails.setResolution(getValue("resolution-val"));
        //Assignee: 	Fred Normal
        viewIssueDetails.setAssignee(getValue("assignee-val"));
        //Reporter: 	Wilma Flintstone
        viewIssueDetails.setReporter(getValue("reporter-val"));
        //Votes: 	1
        viewIssueDetails.setVotes(getValue("vote-data"));
        //Watchers: 	1
        viewIssueDetails.setWatchers(getValue("watcher-data"));
    }

    private String getValue(String id)
    {
        Locator locator = new IdLocator(tester, id);
        if (locator.getNodes().length > 0)
        {
            return locator.getText();
        }
        return null;
    }

    private String getAttribute(String id, String attribute)
    {
        Locator locator = new IdLocator(tester, id);
        if (locator.getNodes().length > 0)
        {
            final NamedNodeMap attributes = locator.getNodes()[0].getAttributes();
            if (attributes != null)
            {
                final Node attributeNode = attributes.getNamedItem(attribute);
                if (attributeNode != null)
                {
                    return attributeNode.getNodeValue();
                }
            }
        }
        return null;
    }

    private String parseCreatedDate()
    {
        return getValue("create-date");
    }

    private String parseUpdatedDate()
    {
        return getValue("updated-date");
    }

    private String parseEnvironment()
    {
        String env = getValue("environment-val");
        if (StringUtils.isNotBlank(env))
        {
            env = env.trim();
        }
        return env;

    }

    private String parseDescription()
    {
        Locator locator = new CssLocator(tester, "#descriptionmodule .mod-content");
        if (locator.getNodes().length > 0)
        {
            return locator.getText();
        }
        return null;

    }

    private Map<String, String> parseCustomFields()
    {
        Map<String, String> customFields = new HashMap<String, String>();
        int tabNum = 1;
        while (new CssLocator(tester, "#customfield-panel-" + tabNum + " ul.property-list").getNodes().length != 0)
        {
            Locator fieldLabels = getAllCustomFieldLabels(tabNum);
            Locator fieldValues = getAllCustomFieldValues(tabNum);

            final Node[] labelNodes = fieldLabels.getNodes();
            final Node[] valueNoeds = fieldValues.getNodes();

            for (int i = 0; i < labelNodes.length; i++)
            {
                String customFieldName = fieldLabels.getText(labelNodes[i]);
                customFieldName = customFieldName.substring(0, customFieldName.lastIndexOf(":"));
                String customFieldValue = fieldValues.getText(valueNoeds[i]);
                customFields.put(customFieldName, customFieldValue);

            }

            tabNum++;
        }

        {
            //Check out the new position for the date fields.
            final XPathLocator ddLocator = new XPathLocator(tester, "//*[@id='datesmodule' or @id='peopledetails']//span[contains(@id, 'customfield')]");
            
            
            if (ddLocator.getNodes().length > 0)
            {
                final Node[] valueNodes = ddLocator.getNodes();
                for (Node valueNode : valueNodes)
                {
                    final Node nameNode = valueNode.getAttributes().getNamedItem("data-name");
                    
                    if (nameNode != null) {
                        String name = DomKit.getCollapsedText(nameNode);
                        final String value = StringUtils.trimToNull(DomKit.getCollapsedText(valueNode));

                        if (name != null && value != null)
                        {
                            if (name.charAt(name.length() - 1) == ':')
                            {
                                name = name.substring(0, name.length() -1);
                            }
                            customFields.put(name, value);
                        }
                    }
                }
            }
        }

        return customFields;
    }

    private Locator getAllCustomFieldLabels(int tabNum)
    {
        return new CssLocator(tester, "#customfield-panel-" + tabNum + " li.item .name");
    }

    private Locator getAllCustomFieldValues(int tabNum)
    {
        return new CssLocator(tester, "#customfield-panel-" + tabNum + " li.item .value");
    }

    private String findPreviousElementValue(Node node, String elementName)
    {
        Node prev = node.getPreviousSibling();
        while (prev != null)
        {
            if (prev.getNodeType() == Node.ELEMENT_NODE && elementName.equals(prev.getNodeName()))
            {
                return DomKit.getCollapsedText(prev);
            }
            prev = prev.getPreviousSibling();
        }
        return null;
    }

    private List<String> parseFileAttachments()
    {
        List<String> attachments = new ArrayList<String>();
        XPathLocator xPathLocator = new XPathLocator(tester, "//*[@id='file_attachments']//a");

        for (int i = 0; i < xPathLocator.getNodes().length; i++)
        {
            Node node = xPathLocator.getNodes()[i];
            if (i % 2 == 1)
            {
                attachments.add(xPathLocator.getText(node));
            }
        }
        return attachments;
    }

    private List<String> parseAvailableWorkflowActions(final WebTester tester)
    {
        List<String> actions = new ArrayList<String>();
        XPathLocator xPathLocator = new XPathLocator(tester, "//div[@class='ops-cont']//a");
        final Node[] nodes = xPathLocator.getNodes();
        for (Node node : nodes)
        {
            final Node id = node.getAttributes().getNamedItem("id");
            if (id != null)
            {
                final String nodeIDText = DomKit.getCollapsedText(id).trim();
                if (nodeIDText.startsWith("action_id"))
                {
                    actions.add(DomKit.getCollapsedText(node));
                }
            }
        }
        return actions;
    }

    private String parseOriginalEstimate()
    {
        // tt_single_values_orig
        return getValue("tt_single_values_orig");
    }

    private String parseTimeSpent()
    {
        // tt_single_values_orig
        return getValue("tt_single_values_spent");
    }

    private String parseRemaininglEstimate()
    {
        // tt_single_values_orig
        return getValue("tt_single_values_remain");
    }

    private String parseDueDate()
    {
        return getValue("due-date");
    }

    private String parseResolutionDate()
    {
        return getValue("resolved-date");
    }

    private String parseProjectName()
    {
        return getValue("project-name-val");
    }

    private String parseSummary()
    {
        Locator locator = new CssLocator(tester, "header#stalker h1");
        if (locator.getNodes().length > 0)
        {
            return locator.getText();
        }
        return null;
    }

    private List<String> parseComponents()
    {
        return parseCommaSepValueFromIssueHeader("components-val");
    }

    private List<String> parseAffectsVersions()
    {
        return parseCommaSepValueFromIssueHeader("versions-val");
    }

    private List<String> parseFixVersions()
    {
        return parseCommaSepValueFromIssueHeader("fixfor-val");
    }

    private String parseSecurityLevel()
    {
        return getValue("security-val");

    }

    private List<String> parseCommaSepValueFromIssueHeader(String val)
    {
        final String value = getValue(val);
        List<String> values = new ArrayList<String>();

        if (value != null)
        {
            StringTokenizer st = new StringTokenizer(value, ",");
            while (st.hasMoreTokens())
            {
                values.add(st.nextToken().trim());
            }
        }
        return values;
    }

    public ChangeHistoryList parseChangeHistory()
    {
        try
        {
            return ChangeHistoryParser.getChangeHistory(tester);
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<Comment> parseComments()
    {
        return CommentParser.getComments(tester);
    }

    public List<Worklog> parseWorklogs()
    {
        return WorklogParser.getWorklogs(tester);
    }
}
