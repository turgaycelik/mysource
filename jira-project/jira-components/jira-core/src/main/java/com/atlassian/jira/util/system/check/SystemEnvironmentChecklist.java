package com.atlassian.jira.util.system.check;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.web.bean.I18nBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class contains checks related to System environment variables / settings. Each check can return a warning
 * message. The warning messages are currently displayed in the jira log, the admin portlet and the system info page.
 *
 * @since v4.0
 */
public class SystemEnvironmentChecklist
{
    private static final JiraProperties jiraSystemProperties = JiraSystemProperties.getInstance();

    private static final SystemEnvironmentCheck[] ENVIRONMENT_CHECKS = {
            new JVMCheck(jiraSystemProperties),
            new JRA12525Check(),
            new JRA10145Check(jiraSystemProperties),
            new JRA18659Check(jiraSystemProperties),
            new JRA20617Check(),
            new JRA21205Check(),
            new JRA15731Check(),
            new JRA21845Check(),
            new JRA24857Check()
    };

    private static List<I18nMessage> warnings;

    private static List<I18nMessage> getWarnings()
    {
        if (warnings == null)
        {
            List<I18nMessage> newWarnings = new ArrayList<I18nMessage>();

            for (final SystemEnvironmentCheck check : ENVIRONMENT_CHECKS)
            {
                final I18nMessage warningMessage = check.getWarningMessage();
                if (warningMessage != null)
                {
                    newWarnings.add(warningMessage);
                }
            }
            warnings = newWarnings;
        }

        return warnings;
    }

    public static List<String> getWarningMessages(Locale locale, final boolean asHtml)
    {
        I18nBean i18nBean = new I18nBean(locale);
        List<String> translatedWarnings = new ArrayList<String>();

        final List<I18nMessage> warnings = getWarnings();
        for (I18nMessage warning : warnings)
        {
            String warningMessage;
            List<Object> parameters = warning.getParameters();
            if (asHtml)
            {

                if (warning.hasLink())
                {
                    parameters.add("<a href=\"" + warning.getLink() + "\">");
                    parameters.add("</a>");
                }
                parameters.add("<em>");
                parameters.add("</em>");
                warningMessage = i18nBean.getText(warning.getKey() + ".html", parameters);
            }
            else
            {
                if (warning.hasLink())
                {
                    parameters.add(warning.getLink());
                }
                warningMessage = i18nBean.getText(warning.getKey() + ".text", parameters);
            }

            translatedWarnings.add(warningMessage);
        }

        return translatedWarnings;
    }

    public static List<String> getEnglishWarningMessages()
    {
        return getWarningMessages(Locale.ENGLISH, false);
    }

}
