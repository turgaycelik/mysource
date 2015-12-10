package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.security.xml.SecureXmlParserFactory;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

/**
 * Replaces all usages of OSUserGroupCondition with {@link com.atlassian.jira.workflow.condition.UserInGroupCondition}.
 *
 * @since v5.0
 */
public class UpgradeTask_Build701 extends AbstractUpgradeTask
{
    /**
     * Logger for this UpgradeTask_Build701 instance.
     */
    private final static Logger log = LoggerFactory.getLogger(UpgradeTask_Build701.class);
    private final XPathFactory xpathFactory = XPathFactory.newInstance();
    private final XPath xpath = xpathFactory.newXPath();
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public UpgradeTask_Build701()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "701";
    }

    @Override
    public String getShortDescription()
    {
        return "Replaces all usages of OSUserGroupCondition with UserInGroupCondition.";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        DescriptorFactory descriptorFactory = DescriptorFactory.getFactory();
        WorkflowManager workflowManager = ComponentAccessor.getComponentOfType(WorkflowManager.class);

        for (JiraWorkflow workflow : workflowManager.getWorkflows())
        {
            if (workflow instanceof ConfigurableJiraWorkflow)
            {
                ConfigurableJiraWorkflow mutableWorkflow = (ConfigurableJiraWorkflow) workflow;
                try
                {
                    Element upgradedDescriptor = upgradeDescriptor(mutableWorkflow.getDescriptor().asXML());
                    WorkflowDescriptor workflowDescriptor = descriptorFactory.createWorkflowDescriptor(upgradedDescriptor);

                    mutableWorkflow.setDescriptor(workflowDescriptor);
                    workflowManager.saveWorkflowWithoutAudit(mutableWorkflow);
                    log.debug("Upgraded workflow '{}'", mutableWorkflow.getName());
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Failed to upgrade workflow: " + workflow.getName(), e);
                }
            }
        }
    }

    // non-streamy upgrade. not great performance-wise, but quick to implement.
    private Element upgradeDescriptor(String descriptor) throws Exception
    {
        final DocumentBuilder documentBuilder = SecureXmlParserFactory.newNamespaceAwareDocumentBuilder();
        Document workflow = documentBuilder.parse(new ByteArrayInputStream(descriptor.getBytes("utf-8")));

        // upgrades <arg name="class.name">com.opensymphony.workflow.util.OSUserGroupCondition</arg>
        XPathExpression findConditionClass = xpath.compile("//arg[@name='class.name'][text()='com.opensymphony.workflow.util.OSUserGroupCondition']");
        NodeList conditionClasses = (NodeList) findConditionClass.evaluate(workflow, XPathConstants.NODESET);
        for (int i = 0; i < conditionClasses.getLength(); i++)
        {
            Node arg = conditionClasses.item(i);
            arg.setTextContent("com.atlassian.jira.workflow.condition.UserInGroupCondition");
        }

        Transformer transformer = transformerFactory.newTransformer();

        // we need to set the DOCTYPE explicitly
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//OpenSymphony Group//DTD OSWorkflow 2.8//EN");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.opensymphony.com/osworkflow/workflow_2_8.dtd");

        DOMSource source = new DOMSource(workflow);
        StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(source, result);

        return workflow.getDocumentElement();
    }
}
