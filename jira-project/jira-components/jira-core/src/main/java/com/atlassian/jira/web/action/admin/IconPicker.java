package com.atlassian.jira.web.action.admin;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.ofbiz.core.entity.GenericValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class IconPicker extends JiraWebActionSupport
{
    private String fieldType;
    private String formName;
    private List iconsOfType;
    private Properties iconProperties;
    private MultiMap issueConstants;
    private String fieldId;

    private static final String ICON_IMAGES_PROPERTIES = "iconimages.properties";

    private final ConstantsManager constantsManager;


    public IconPicker(ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
    }

    public String getFieldId()
    {
        return fieldId;
    }

    public void setFieldId(String fieldId)
    {
        this.fieldId = fieldId;
    }

    public String getFieldType()
    {
        return fieldType;
    }

    public String getFormName()
    {
        return formName;
    }

    public String getFieldName()
    {
        return "issue.field." + fieldType;
    }

    public void setFieldType(String fieldType)
    {
        this.fieldType = fieldType;
    }

    public void setFormName(String formName)
    {
        this.formName = formName;
    }

    public List getIconUrls(String fieldType)
    {
        if (iconsOfType == null)
        {
            iconsOfType = new ArrayList();

            String fieldTypeLowerCase = fieldType.toLowerCase();

            if (getIconProperties() != null)
            {
                for (final Object o : getIconProperties().keySet())
                {
                    String icon = (String) o;
                    if (icon.startsWith("icon." + fieldTypeLowerCase))
                    {
                        iconsOfType.add(icon);
                    }
                }
            }
            else
            {
                addErrorMessage(getText("admin.errors.unable.to.load.properties","'" + ICON_IMAGES_PROPERTIES + "'"));
            }
        }

        Collections.sort(iconsOfType);
        return iconsOfType;
    }

    public String getImage(String key)
    {
        return getIconProperties().getProperty(key);
    }

    private Properties getIconProperties()
    {
        if (iconProperties == null)
        {
            iconProperties = new Properties();
            InputStream in = ClassLoaderUtils.getResourceAsStream(ICON_IMAGES_PROPERTIES, this.getClass());
            try
            {
                iconProperties.load(in);
                in.close();
            }
            catch (IOException e)
            {
                log.error("Unable to load icon properties from '" + ICON_IMAGES_PROPERTIES + "'.");
                return null;
            }
        }
        return iconProperties;
    }

    public Collection getAssociatedImages(String imageLocation)
    {
        return (Collection) getIssueConstants().get(imageLocation);
    }

    private MultiMap getIssueConstants()
    {
        if (issueConstants == null)
        {
            issueConstants = new MultiHashMap();

            Collection<GenericValue> fields;
            if (fieldType.equals("status"))
                fields = constantsManager.getStatuses();
            else if (fieldType.equals("issuetype"))
                fields = constantsManager.getIssueTypes();
            else if (fieldType.equals("priority"))
                fields = constantsManager.getPriorities();
            else if (fieldType.equals("subtasks"))
                fields = constantsManager.getSubTaskIssueTypes();
            else
            {
                throw new IllegalArgumentException("Invalid field type selected.");
            }

            for (GenericValue issueConstantGV : fields)
            {
                String associatedImage = issueConstantGV.getString("iconurl");
                issueConstants.put(associatedImage, constantsManager.getIssueConstant(issueConstantGV));
            }
        }

        return issueConstants;
    }
}
