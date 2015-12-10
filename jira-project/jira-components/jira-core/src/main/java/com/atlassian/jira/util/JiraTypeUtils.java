package com.atlassian.jira.util;

import com.atlassian.core.util.ClassLoaderUtils;
import com.google.common.collect.Maps;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

public class JiraTypeUtils
{
    private static final Logger log = Logger.getLogger(JiraTypeUtils.class);

    public static <T> Map<String, T> loadTypes(String resource, Class<?> typeClass)
    {
        Map<String, T> loadedTypes = Maps.newLinkedHashMap();

        //read in the events from an xml file in the class path
        InputStream is = ClassLoaderUtils.getResourceAsStream(resource, typeClass);
        try
        {
            Document doc = new Document(is);
            Element root = doc.getRoot();
            Elements actions = root.getElements("type");

            while (actions.hasMoreElements())
            {
                Element action = (Element) actions.nextElement();
                String id = action.getAttribute("id");
                String className = action.getElement("class").getTextString();
                try
                {
                    loadedTypes.put(id, JiraUtils.<T>loadComponent(className, typeClass));
                }
                catch (Exception e)
                {
                    log.error("Exception loading type: " + className, e);
                }
            }
            return loadedTypes;
        }
        catch (ParseException e)
        {
            log.error("Parse exception parsing: " + resource, e);
            return Collections.emptyMap();
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                log.warn("Could not close " + resource + " inputStream");
            }
        }
    }
}