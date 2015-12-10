package com.atlassian.jira.lookandfeel.transformer;

import com.atlassian.jira.lookandfeel.LookAndFeelProperties;
import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.plugin.webresource.transformer.CharSequenceDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import org.dom4j.Element;

/**
 *
 *
 * @since v4.4
 */
public class LogoTransformer implements WebResourceTransformer
{
    private final LookAndFeelProperties lookAndFeelProperties;
    private final WebResourceUrlProvider webResourceUrlProvider;

    public LogoTransformer(LookAndFeelProperties lookAndFeelProperties, WebResourceUrlProvider provider)
    {
        this.lookAndFeelProperties = lookAndFeelProperties;
        this.webResourceUrlProvider = provider;
    }

    @Override
    public DownloadableResource transform(Element configElement, ResourceLocation location, String filePath, DownloadableResource nextResource)
    {
        return new CharSequenceDownloadableResource(nextResource)
        {
            protected String transform(final CharSequence originalContent)
            {
                String replacedContent = originalContent.toString();
                String prefix = webResourceUrlProvider.getStaticResourcePrefix(UrlMode.AUTO);
                // JRADEV-6590 If this is the root app, then ignore the slash
                if ("/".equals(prefix))
                {
                    prefix = "";
                }
                String defaultLogoUrl = lookAndFeelProperties.getDefaultCssLogoUrl();
                if (defaultLogoUrl != null && !defaultLogoUrl.startsWith("http://") && !defaultLogoUrl.startsWith("."))
                {
                    defaultLogoUrl = prefix+defaultLogoUrl;
                }
                replacedContent = replacedContent.replace("@defaultLogoUrl", defaultLogoUrl);
                replacedContent = replacedContent.replace("@maxLogoHeight", String.valueOf(lookAndFeelProperties.getMaxLogoHeight()));
                return replacedContent;
            }
        };
    }
}
