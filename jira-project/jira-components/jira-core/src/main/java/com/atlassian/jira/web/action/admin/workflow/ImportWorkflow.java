package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.WorkflowImportedFromXmlEvent;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@WebSudoRequired
public class ImportWorkflow extends JiraWebActionSupport
{
    private String name;
    private String description;
    private String definition;
    private String filePath;
    private String workflowXML;

    private final WorkflowManager workflowManager;
    private final EventPublisher eventPublisher;

    public ImportWorkflow(WorkflowManager workflowManager, EventPublisher eventPublisher)
    {
        this.workflowManager = workflowManager;
        this.eventPublisher = eventPublisher;
    }

    protected void doValidation()
    {
        if (isBlank(name))
        {
            addError("name", getText("admin.errors.workflows.specify.workflow.name"));
        }
        else
        {
            try
            {
                if (workflowManager.workflowExists(name))
                {
                    addError("name", getText("admin.errors.a.workflow.with.this.name.already.exists"));
                }
            }
            catch (WorkflowException e)
            {
                log.error("Error occurred while reading workflow information.", e);
                addErrorMessage(getText("admin.errors.workflows.error.reading.information"));
            }
        }

        if (isNotBlank(definition) && definition.equals("file"))
        {
            if (isNotBlank(filePath))
            {
                final File workflowFile = new File(filePath);
                if (!workflowFile.exists())
                {
                    addError("filePath", getText("admin.errors.workflows.file.does.not.exist"));
                }
                else if (!workflowFile.canRead())
                {
                    addError("filePath", getText("admin.errors.workflows.file.not.readable"));
                }
                else if (!workflowFile.isFile())
                {
                    addError("filePath", getText("admin.errors.workflows.file.not.regular"));
                }
            }
            else
            {
                addError("filePath", getText("admin.errors.workflows.specify.path"));
            }
            workflowXML = null;
        }
        else
        {
            definition = "inline";
            filePath = null;
            if (isBlank(workflowXML))
            {
                addError("filePath", getText("admin.errors.workflows.specify.xml"));
            }
        }

        super.doValidation();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            // TODO need to ensure that parsing the workflow descriptor works in different encodings
            final WorkflowDescriptor workflowDescriptor = WorkflowLoader.load(getWorkflowXMLInputStream(), true);
            final ConfigurableJiraWorkflow newWorkflow = new ConfigurableJiraWorkflow(name, workflowDescriptor, workflowManager);
            newWorkflow.setDescription(description);
            workflowManager.createWorkflow(getLoggedInApplicationUser(), newWorkflow);

            eventPublisher.publish(new WorkflowImportedFromXmlEvent(newWorkflow));

            return returnComplete("ListWorkflows.jspa");
        }
        catch (FileNotFoundException e)
        {
            String message = "Error loading workflow from file.";
            addErrorMessage(message + " Please see log for more details.");
            log.error(message, e);
            return ERROR;
        }
        catch (InvalidWorkflowDescriptorException e)
        {
            String message = "Invalid workflow XML: " + e.getMessage();
            addErrorMessage(message + ". Please see log for more details.");
            log.error(message, e);
            return ERROR;
        }
        catch (IOException e)
        {
            String message = "Error loading workflow.";
            addErrorMessage(getText("admin.errors.workflows.error.loading.workflow") + " Please see log for more details.");
            log.error(message, e);
            return ERROR;
        }
        catch (SAXException e)
        {
            String message = "Error parsing workflow XML: " + e.getMessage();
            addErrorMessage(getText("admin.errors.workflows.error.parsing.xml", e.getMessage()) + ". Please see log for more details.");
            log.error(message, e);
            return ERROR;
        }
        catch (WorkflowException e)
        {
            String message = "Error saving workflow: " + e.getMessage();
            addErrorMessage(getText("admin.errors.workflows.error.saving.workflow", e.getMessage()) + " Please see log for more details.");
            log.error(message, e);
            return ERROR;
        }
    }

    private InputStream getWorkflowXMLInputStream() throws FileNotFoundException, UnsupportedEncodingException
    {
        if (isNotBlank(filePath))
        {
            return new FileInputStream(filePath);
        }
        else if (isNotBlank(workflowXML))
        {
            // TODO This uses system default encoding to convert string to bytes - reading workflow
            // might break
            return new ByteArrayInputStream(workflowXML.getBytes());
        }

        throw new IllegalStateException("Neither file path nor XML are given.");
    }


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDefinition()
    {
        return definition;
    }

    public void setDefinition(String definition)
    {
        this.definition = definition;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public String getWorkflowXML()
    {
        return workflowXML;
    }

    public void setWorkflowXML(String workflowXML)
    {
        this.workflowXML = workflowXML;
    }
}
