package com.atlassian.jira.lookandfeel.image;

import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.StringUtils;
import webwork.multipart.MultiPartRequestWrapper;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This represents an image that is uploaded from a multipart
 *
 * @since v4.4
 */
public class MultiPartImageDescriptor extends ImageDescriptor
{

    private final File file;
    private final I18nHelper i18nHelper;


    public MultiPartImageDescriptor(String parameterName, MultiPartRequestWrapper multiPart, I18nHelper i18nHelper) throws IOException
    {
        this.i18nHelper = i18nHelper;
        fileName = multiPart.getFilesystemName(parameterName);
        contentType = multiPart.getContentType(parameterName);
        file = multiPart.getFile(parameterName);
        if (file != null)
        {
            imageData = new FileInputStream(file);
        }
    }


    @Override
    public String getImageDescriptorType()
    {
        return i18nHelper.getText("jira.lookandfeel.multipartimagedescriptor.type");
    }
}
