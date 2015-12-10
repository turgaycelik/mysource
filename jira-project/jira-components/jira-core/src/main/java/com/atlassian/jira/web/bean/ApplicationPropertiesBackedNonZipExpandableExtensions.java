package com.atlassian.jira.web.bean;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation of {@link NonZipExpandableExtensions} that is backed by a jira application property.
 *
 * By default, it includes the MS Office OpenXml extensions and OpenOffice OpenDocument extensions.
 *
 * @since v4.2
 */
public class ApplicationPropertiesBackedNonZipExpandableExtensions implements NonZipExpandableExtensions
{
    static final String DEFAULT_JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST = "docx, docm, dotx, dotm,"
            + " xlsx, xlsm, xltx, xltm, xlsb, xlam, pptx, pptm, potx, potm, ppam, ppsx, ppsm, sldx, sldm, thmx,odt,"
            + " odp, ods, odg, odb, odf, ott, otp, ots, otg, odm, sxw, stw, sxc, stc, sxi, sti, sxd, std, sxg";

    private final ApplicationProperties applicationProperties;

    public ApplicationProperties getApplicationProperties()
    {
        return applicationProperties;
    }

    public ApplicationPropertiesBackedNonZipExpandableExtensions(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public String getNonExpandableExtensionsList()
    {
        if (getApplicationProperties().getDefaultBackedString(APKeys.JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST) == null)
        {
            return DEFAULT_JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST;
        }
        else
        {
            return getApplicationProperties().getDefaultBackedString(APKeys.JIRA_ATTACHMENT_DO_NOT_EXPAND_AS_ZIP_EXTENSIONS_LIST);
        }
    }

    @Override
    public boolean contains(String extension)
    {
        return contains(extension, getNonExpandableExtensionsList());
    }

    boolean contains(String extension, String nonExpandableExtensionsList)
    {
        notNull("extension", extension);

        if (!StringUtils.isBlank(extension))
        {
            final StrTokenizer tokenizer = StrTokenizer.getCSVInstance(nonExpandableExtensionsList);
            while (tokenizer.hasNext())
            {
                String nonExpandableExtension = tokenizer.nextToken();
                if (nonExpandableExtension.equalsIgnoreCase(extension))
                {
                    return true;
                }
            }
        }
        return false;
    }
}