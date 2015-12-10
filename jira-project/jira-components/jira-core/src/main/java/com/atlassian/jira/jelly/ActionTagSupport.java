package com.atlassian.jira.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpUtils;

public abstract class ActionTagSupport extends JiraDynaBeanTagSupport
{
    private static final Logger log = Logger.getLogger(ActionTagSupport.class);
    protected final static boolean SUCCESS = true;
    protected final static boolean FAILURE = false;
    private boolean executeWebworkOnClose = false;
    private String actionName = "invalid_action_name";
    protected boolean ignoreErrors;
    private WebWorkAdaptor webWorkAdaptor = null;

    public boolean isExecuteWebworkOnClose()
    {
        return executeWebworkOnClose;
    }

    public void setExecuteWebworkOnClose(boolean executeWebworkOnClose)
    {
        this.executeWebworkOnClose = executeWebworkOnClose;
    }

    public String getActionName()
    {
        return actionName;
    }

    protected void setActionName(String actionName)
    {
        this.actionName = actionName;
    }

    public void setProperty(String key, String value)
    {
        getProperties().put(key, value);
    }

    public void setProperty(String key, String[] value)
    {
        getProperties().put(key, value);
    }

    public String getProperty(String key)
    {
        return (String) getProperties().get(key);
    }

    protected boolean propertyContains(String key)
    {
        return getProperties().containsKey(key);
    }

    public JellyHttpResponse getResponse()
    {
        return getWebWorkAdaptor().getJellyHttpResponse();
    }

    public void beforeSetAttributes() throws JellyTagException
    {
        super.beforeSetAttributes();
        super.getProperties().clear();
    }

    public JellyHttpRequest getRequest()
    {
        return getWebWorkAdaptor().getJellyHttpRequest();
    }

    public WebWorkAdaptor getWebWorkAdaptor()
    {
        if (webWorkAdaptor == null)
        {
            webWorkAdaptor = new WebWorkAdaptor();
        }
        return webWorkAdaptor;
    }

    public String toString()
    {
        return "(" + getTagName() + getDynaBean().toString() + ")";
    }

    /**
     * Evaluates this tag after all the tag properties have been initialized.
     *
     * @param output the xml output stream
     */
    public void doTag(XMLOutput output) throws JellyTagException
    {
        log.debug("ActionTagSupport.doTag :" + toString());

        // First check to see that we have a valid parent tag
        preContextValidation();

        if (contextValidation(output))
        {
            // Pre-tag (data preparation + validation check)
            prePropertyValidation(output);

            boolean preTagValid = propertyValidation(output);
            runTagAndPostTag(preTagValid, output);
        }

        // run function that does stuff on tag close
        endTagExecution(output);
    }

    private void runTagAndPostTag(boolean preTagValid, XMLOutput output) throws JellyTagException
    {
        if (preTagValid)
        {
            // If we want to run the nested tags first the do if
            if (isExecuteWebworkOnClose())
            {
                // invoke the nested tag
                invokeNestedTag(output);
            }

            // Execute the tag
            if (getWebWorkAdaptor().mapJellyTagToAction(this, output))
            {
                // If the tag execution succeeded
                postTagExecution(output);

                // Validaate the left the correct things in the the context
                postTagValidation(output);

                if (!isExecuteWebworkOnClose())
                {
                    // invoke the nested tag
                    invokeNestedTag(output);
                }
            }
            else if (ignoreErrors)
            {
                // invoke the nested tag
                invokeNestedTag(output);
            }
        }
    }

    private void invokeNestedTag(XMLOutput output) throws JellyTagException
    {
        Script body = getBody();
        if (body != null)
        {
            body.run(context, output);
        }
    }

    protected void preContextValidation()
    {
        log.debug("ActionTagSupport.preContextValidation");
    }

    protected boolean contextValidation(XMLOutput output) throws JellyTagException
    {
        log.debug("CreateProject.contextValidation");
        if (!contextContainsBefore(getRequiredContextVariables(), output))
        {
            return FAILURE;
        }
        return SUCCESS;
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        log.debug("ActionTagSupport.prePropertyValidation");
    }

    protected boolean propertyValidation(XMLOutput output) throws JellyTagException
    {
        if (!propertiesContains(getRequiredProperties(), output))
        {
            return FAILURE;
        }
        return SUCCESS;
    }

    protected void postTagExecution(XMLOutput output) throws JellyTagException
    {
        log.debug("ActionTagSupport.postTagExecution");
    }

    protected void postTagValidation(XMLOutput output) throws JellyTagException
    {
        contextContainsAfter(getRequiredContextVariablesAfter(), output);
    }

    protected void endTagExecution(XMLOutput output)
    {
        log.debug("ActionTagSupport.endTagExecution");
    }

    protected void copyRedirectUrlParametersToTag(String url)
    {
        log.debug("ActionTagSupport.copyRedirectUrlParametersToTag");
        // Parse any query parameters out of the request URI
        int question = url.indexOf("?");


        String searchUrl = "/browse/";
        int browseIndex = url.indexOf(searchUrl);
        if (browseIndex != -1)
        {
            int endIndex = question >= 0 ? question : url.length();
            // Get the key from the URL
            final String value = url.substring(browseIndex + searchUrl.length(), endIndex);
            setProperty("key", value);
        }

        searchUrl = "plugins/servlet/project-config/";

        browseIndex = url.indexOf(searchUrl);
        if (browseIndex != -1)
        {
            browseIndex += searchUrl.length();
            int endIndex = url.lastIndexOf("/");
            if (endIndex != -1)
            {
                final String value = url.substring(browseIndex, endIndex);
                setProperty("key", value);

            }
            else
            {
                final String value = url.substring(browseIndex);
                setProperty("key", value);
            }
        }

        String queryString;
        if (question >= 0)
        {
            queryString = url.substring(question + 1, url.length());

            Hashtable parameters = HttpUtils.parseQueryString(queryString);

            if (!parameters.isEmpty())
            {
                Iterator iterator = parameters.keySet().iterator();
                while (iterator.hasNext())
                {
                    String key = (String) iterator.next();
                    String value = ((String[]) (parameters.get(key)))[0];
                    if (getProperties().containsKey(key))
                    {
                        log.warn("ActionTagSupport.copyRedirectUrlParametersToTag : " + "Overwriting property " + key);
                    }
                    setProperty(key, value);
                }
            }
        }
    }

    /**
     * Get the Tag Name
     * The tag name is just the class name after all (without the package name)
     *
     * @return The Name of the Tag
     */
    protected String getTagName()
    {
        return getTagName(this);
    }

    /**
     * Get the Tag Name for another tag
     * The tag name is just the class name after all (without the package name)
     *
     * @return The Name of the Tag
     */
    protected String getTagName(Tag tag)
    {
        String fullClassName = tag.getClass().getName();
        int startPos = fullClassName.lastIndexOf('.') + 1;
        String tagName = fullClassName.substring(startPos, fullClassName.length());
        return tagName;
    }

    protected boolean contextContainsBefore(String[] requiredContextVariablesBefore, XMLOutput output)
            throws JellyTagException
    {
        final Map variables = getContext().getVariables();
        final boolean containsAll = mapContainsAll(requiredContextVariablesBefore, variables);
        if (!containsAll)
        {
            reportRequiredContextVariablesBefore(requiredContextVariablesBefore, variables, output);
        }
        return containsAll;
    }

    protected boolean contextContainsAfter(String[] requiredContextVariablesAfter, XMLOutput output)
            throws JellyTagException
    {
        final Map variables = getContext().getVariables();
        final boolean containsAll = mapContainsAll(requiredContextVariablesAfter, variables);
        if (!containsAll)
        {
            reportRequiredContextVariablesAfter(requiredContextVariablesAfter, output);
        }
        return containsAll;
    }

    protected boolean propertiesContains(String[] requiredProperties, XMLOutput output) throws JellyTagException
    {
        final Map properties = getProperties();
        final boolean containsAll = mapContainsAll(requiredProperties, properties);
        if (!containsAll)
        {
            reportRequiredProperties(requiredProperties, output);
        }
        return containsAll;
    }

    protected boolean mapContainsAll(String[] requiredContextVariables, final Map map)
    {
        boolean valid = true;
        for (final String requiredContextVariable : requiredContextVariables)
        {
            if (!map.containsKey(requiredContextVariable))
            {
                valid = false;
            }
            else if ((map.get(requiredContextVariable) == null))
            {
                map.remove(requiredContextVariable);
                valid = false;
            }
        }
        return valid;
    }

    protected void reportRequiredContextVariablesBefore(String[] requiredContextVariables, Map current, XMLOutput output)
            throws JellyTagException
    {
        final String MESSAGE_SUFFIX = "variables to be set in the Context";
        reportRequired(requiredContextVariables, MESSAGE_SUFFIX, output, null);
    }

    protected void reportRequiredProperties(String[] requiredProperties, XMLOutput output) throws JellyTagException
    {
        final String MESSAGE_SUFFIX = "properties to be set";
        reportRequired(requiredProperties, MESSAGE_SUFFIX, output, null);
    }

    protected void reportRequiredContextVariablesAfter(String[] requiredContextVariablesAfter, XMLOutput output)
            throws JellyTagException
    {
        final String MESSAGE_SUFFIX = "variables to be set in the Context after execution of the tag";
        reportRequired(requiredContextVariablesAfter, MESSAGE_SUFFIX, output, null);
    }

    protected void reportRequired(String[] required, final String MESSAGE_SUFFIX, XMLOutput output, Map current)
            throws JellyTagException
    {
        try
        {
            String tagName = getTagName();
            // TODO fix JRA-10911 here, the array of requireds are message keys! also use a library for comma separating!
            StringBuffer errorMsg = new StringBuffer("Tag " + tagName + " requires (" + arrayToString(required) + ") " + MESSAGE_SUFFIX);
            if (current != null)
            {
                errorMsg.append(current.toString());
            }
            WebWorkAdaptor.writeErrorToXmlOutput(output, errorMsg, getActionName(), this);
        }
        catch (SAXException e)
        {
            log.error(e, e);
            throw new JellyTagException(e);
        }
    }

    protected String arrayToString(String[] array)
    {
        final String SEPERATOR = ", ";
        StringBuilder stringBuffer = new StringBuilder();
        if (array.length != 0)
        {
            for (final String anArray : array)
            {
                stringBuffer.append(anArray);
                stringBuffer.append(SEPERATOR);
            }
            stringBuffer.delete(stringBuffer.length() - SEPERATOR.length(), stringBuffer.length());
        }
        return stringBuffer.toString();
    }

    public abstract String[] getRequiredProperties();

    public abstract String[] getRequiredContextVariablesAfter();

    public abstract String[] getRequiredContextVariables();

    protected void mapProperty(String mapFromKey, Map mapping, String propertyName, XMLOutput output)
            throws SAXException
    {
        mapProperty(mapFromKey, mapFromKey, mapping, propertyName, output);
    }

    protected void mapProperty(String mapFromKey, String mapToKey, Map mapping, String propertyName, XMLOutput output)
            throws SAXException
    {
        String mapFrom = getProperty(mapFromKey);
        if (mapFrom != null)
        {
            mapFrom = mapFrom.trim();
            try
            {
                // if the value is an integer (ie a custom value) - then do nothing
                final Long id = new Long(Long.parseLong(mapFrom));
                if (!mapping.containsKey(id))
                {
                    createError(propertyName, mapFrom, mapping, output);
                }
            }
            catch (NumberFormatException ignore)
            {
                // Try and map the issue type value to an issue type enum value
                final Object keyFromValue = getKeyFromValue(mapping, mapFrom);
                if (keyFromValue == null)
                {
                    createError(propertyName, mapFrom, mapping, output);
                }
                else
                {
                    setProperty(mapToKey, keyFromValue.toString());
                }
            }
        }
    }

    private Object getKeyFromValue(Map map, String value)
    {
        final Set set = map.entrySet();
        for (final Object aSet : set)
        {
            Map.Entry entry = (Map.Entry) aSet;
            if (value.equals(entry.getValue()))
            {
                return entry.getKey();
            }
        }
        return null;
    }

    private void createError(String propertyName, String mapFrom, Map mapping, XMLOutput output) throws SAXException
    {
        StringBuffer errorMsg = new StringBuffer("Unknown " + propertyName + " \"" + mapFrom + "\". ");
        errorMsg.append("Known values are : ");

        Iterator iterator = mapping.keySet().iterator();
        while (iterator.hasNext())
        {
            errorMsg.append("[").append(iterator.next()).append("]");
        }
        WebWorkAdaptor.writeErrorToXmlOutput(output, errorMsg, getActionName(), this);

        // indicate that pre-tag operation failed
    }
}
