package com.atlassian.jira.jelly.tag;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.jelly.JiraDynaBeanTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;

/**
 * Instantiates a JIRA component class (anything registered with {@link ComponentManager}.
 */
public class LoadComponent extends JiraDynaBeanTagSupport
{
    private static final transient Logger log = Logger.getLogger(LoadComponent.class);

    public LoadComponent()
    {
        super();
    }

    public void doTag(XMLOutput xmlOutput) throws JellyTagException
    {
        try
        {
            String variableName = (String) getProperties().get("var");
            String className = (String) getProperties().get("class");
            if (className == null)
                throw new JellyTagException("'class' attribute not found on LoadComponent tag. Please specify the class of the component you wish to load.");
            Class clazz = ClassLoaderUtils.loadClass(className, this.getClass());
            if (clazz == null) throw new JellyTagException("Component with class '" + className + "' not found");
            Object component = ComponentManager.getInstance().getContainer().getComponent(clazz);
            if (component == null) throw new JellyTagException("Component with class '" + className + "' could not be loaded");
            getContext().setVariable(variableName, component);
        }
        catch (Exception e)
        {
            LoadComponent.log.error(e, e);
            throw new JellyTagException(e);
        }
    }
}
