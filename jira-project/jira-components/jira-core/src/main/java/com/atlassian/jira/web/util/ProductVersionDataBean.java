package com.atlassian.jira.web.util;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import com.opensymphony.util.TextUtils;

import static java.lang.String.format;

/**
 * Generates the &lt;meta> tags and attributes needed to provide version data
 * as per https://extranet.atlassian.com/display/~jgraham/Incorporating+version+data+in+a+resource
 * 
 * @since v5.2.x
 */
public class ProductVersionDataBean
{
    private final String metaTags;
    private final String bodyAttributes;

    public ProductVersionDataBean(BuildUtilsInfo buildUtilsInfo)
    {
        this.metaTags = format("<meta name=\"application-name\" content=\"JIRA\" data-name=\"jira\" data-version=\"%s\">",
                TextUtils.htmlEncode(buildUtilsInfo.getVersion()));
        this.bodyAttributes = format("data-version=\"%s\"", TextUtils.htmlEncode(buildUtilsInfo.getVersion()));
    }

    @HtmlSafe
    public String getMetaTags() {
        return metaTags;

    }
    @HtmlSafe
    public String getBodyHtmlAttributes() {
        return bodyAttributes;
    }
}
