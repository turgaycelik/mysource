package com.atlassian.jira.web.less;

import java.util.Map;
import java.util.regex.Pattern;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.web.util.CssSubstitutionWebResourceTransformer;
import com.atlassian.plugin.webresource.WebResourceIntegration;

import org.apache.commons.lang.StringUtils;

/**
 */
public class LookAndFeelLessProvider
{
    private static final Pattern CSS_LITERAL = Pattern.compile("^(" +
            "([A-Za-z-]+)" + // keywords like: border-collapse
            "|" +
            "(#[A-Za-z0-9]+)" + // hash colours
            "|" +
            "(\\d*\\.?\\d+? *(px|%|em|pc|ex|in|deg|s|ms|pt|cm|mm|rad|grad|turn)?)" + // dimension (or dimensionless number)
            "|" +
            "[\"']" + // anything that already looks quoted
            ")$"
    );

    private final ApplicationProperties applicationProperties;
    private final WebResourceIntegration webResourceIntegration;

    public LookAndFeelLessProvider(final ApplicationProperties applicationProperties, final WebResourceIntegration webResourceIntegration)
    {
        this.applicationProperties = applicationProperties;
        this.webResourceIntegration = webResourceIntegration;
    }

    public String makeLookAndFeelLess()
    {
        final LookAndFeelBean laf = LookAndFeelBean.getInstance(applicationProperties);
        CssSubstitutionWebResourceTransformer.VariableMap variableMap =
                new CssSubstitutionWebResourceTransformer.VariableMap(laf, webResourceIntegration);

        StringBuilder out = new StringBuilder();

        for (Map.Entry<String, String> entry : variableMap.getVariableMap(false).entrySet())
        {
            out.append("@").append(entry.getKey()).append(": ");
            out.append(encodeValue(entry.getValue())).append(";\n");
        }

        return out.toString();
    }

    public String encodeState()
    {
        return Long.toString(LookAndFeelBean.getInstance(applicationProperties).getVersion());
    }

    private String encodeValue(String value)
    {
        value = StringUtils.trimToEmpty(value);
        // keep any CSS literals verbatim, quote everything else
        if (CSS_LITERAL.matcher(value).matches()) {
            return value;
        }

        value = value.replaceAll("['\"]", "\\$0");
        return '"' + value + '"'; // good enough, escape " and ' as necessary
    }
}
