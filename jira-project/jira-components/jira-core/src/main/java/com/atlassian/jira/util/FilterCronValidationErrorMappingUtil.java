package com.atlassian.jira.util;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class FilterCronValidationErrorMappingUtil
{
    private final Map errorMap;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public FilterCronValidationErrorMappingUtil(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        Map tempMap = new HashMap();
        tempMap.put("Unexpected end of expression.", new ErrorMapping("filter.subsription.cron.errormessage.unexpected.end.of.expr"));
        tempMap.put("Invalid Day-of-Week value: '", new ErrorMapping("filter.subsription.cron.errormessage.invalid.day.of.week", "'"));
        // Is never thrown
        tempMap.put("Expression string length too short. ", new ErrorMapping("filter.subsription.cron.errormessage.string.too.short"));
        tempMap.put("Illegal cron expression format (", new ErrorMapping("filter.subsription.cron.errormessage.illegal.format", ")"));
        // Is never thrown
        tempMap.put("Invalid Month value: '", new ErrorMapping("filter.subsription.cron.errormessage.invalid.month", "'"));
        tempMap.put("A numeric value between 1 and 5 must follow the '#' option", new ErrorMapping("filter.subsription.cron.errormessage.numeric.value.between.after.hash"));
        tempMap.put("Illegal characters for this position: '", new ErrorMapping("filter.subsription.cron.errormessage.illegal.characters.for.position", "'"));
        tempMap.put("Illegal character after '?': ", new ErrorMapping("filter.subsription.cron.errormessage.illegal.character.after.question.mark"));
        tempMap.put("'?' can only be specfied for Day-of-Month or Day-of-Week.", new ErrorMapping("filter.subsription.cron.errormessage.question.mark.invalid.position"));
        tempMap.put("'?' can only be specfied for Day-of-Month -OR- Day-of-Week.", new ErrorMapping("filter.subsription.cron.errormessage.question.mark.invalid.for.both"));
        tempMap.put("'/' must be followed by an integer.", new ErrorMapping("filter.subsription.cron.errormessage.slash.must.be.followed.by.integer"));
        tempMap.put("Increment > 60 : ", new ErrorMapping("filter.subsription.cron.errormessage.increment.greater.than.sixty"));
        tempMap.put("Increment > 31 : ", new ErrorMapping("filter.subsription.cron.errormessage.increment.greater.than.thirtyone"));
        tempMap.put("Increment > 24 : ", new ErrorMapping("filter.subsription.cron.errormessage.increment.greater.than.twentyfour"));
        tempMap.put("Increment > 7 : ", new ErrorMapping("filter.subsription.cron.errormessage.increment.greater.than.seven"));
        tempMap.put("Increment > 12 : ", new ErrorMapping("filter.subsription.cron.errormessage.increment.greater.than.twelve"));
        tempMap.put("Unexpected character: ", new ErrorMapping("filter.subsription.cron.errormessage.unexpected.character"));
        tempMap.put("'L' option is not valid here. (pos=", new ErrorMapping("filter.subsription.cron.errormessage.l.not.valid", ")"));
        tempMap.put("'W' option is not valid here. (pos=", new ErrorMapping("filter.subsription.cron.errormessage.w.not.valid", ")"));
        tempMap.put("'#' option is not valid here. (pos=", new ErrorMapping("filter.subsription.cron.errormessage.hash.not.valid", ")"));
        tempMap.put("'C' option is not valid here. (pos=", new ErrorMapping("filter.subsription.cron.errormessage.c.not.valid", ")"));
        tempMap.put("Unexpected character '", new ErrorMapping("filter.subsription.cron.errormessage.unexpected.character.after.slash", "' after '/'"));
        tempMap.put("Minute and Second values must be between 0 and 59", new ErrorMapping("filter.subsription.cron.errormessage.minute.and.seconds.between.zero.fiftynine"));
        tempMap.put("Hour values must be between 0 and 23", new ErrorMapping("filter.subsription.cron.errormessage.hour.between.zero.twentythree"));
        tempMap.put("Day of month values must be between 1 and 31", new ErrorMapping("filter.subsription.cron.errormessage.day.of.month.between.one.thirtyone"));
        tempMap.put("Month values must be between 1 and 12", new ErrorMapping("filter.subsription.cron.errormessage.month.between.one.twelve"));
        tempMap.put("Day-of-Week values must be between 1 and 7", new ErrorMapping("filter.subsription.cron.errormessage.day.of.week.between.one.seven"));
        tempMap.put("Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.", new ErrorMapping("filter.subsription.cron.errormessage.day.of.week.and.day.of.month"));

        errorMap = Collections.unmodifiableMap(tempMap);
        
    }

    public void mapError(JiraServiceContext context, ParseException e)
    {
        String origErrorMessage = e.getMessage();
        String i18nErrorMessage = null;
        if (errorMap.containsKey(origErrorMessage))
        {
            ErrorMapping mapping = (ErrorMapping) errorMap.get(origErrorMessage);
            i18nErrorMessage = getText(mapping.getPropertyString());
        }
        else
        {
            for (final Object o : errorMap.keySet())
            {
                String startOfError = (String) o;
                if (origErrorMessage.startsWith(startOfError))
                {
                    ErrorMapping mapping = (ErrorMapping) errorMap.get(startOfError);
                    String param;
                    if (mapping.getEndString() == null)
                    {
                        param = origErrorMessage.substring(startOfError.length());
                    }
                    else
                    {
                        param = origErrorMessage.substring(startOfError.length(), origErrorMessage.length() - mapping.getEndString().length());
                    }

                    i18nErrorMessage = getText(mapping.getPropertyString(), param);
                }
            }
        }

        // If mapping not found
        if (i18nErrorMessage == null)
        {
            i18nErrorMessage = origErrorMessage;
        }

        context.getErrorCollection().addErrorMessage(i18nErrorMessage);

    }

    private static class ErrorMapping
    {
        private final String propertyString;
        private final String endString;

        protected ErrorMapping(String propertyString, String endString)
        {

            this.propertyString = propertyString;
            this.endString = endString;
        }

        protected ErrorMapping(String propertyString)
        {

            this.propertyString = propertyString;
            this.endString = null;
        }


        public String getPropertyString()
        {
            return propertyString;
        }

        public String getEndString()
        {
            return endString;
        }

    }

    /**
     * Translates a given key using i18n bean
     *
     * @param key key to translate
     * @return i18n string for given key
     */
    protected String getText(String key)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key);
    }

    /**
     * Translates a given key using i18n bean, passing in param
     *
     * @param key   key to transkate
     * @param param param to insert into property
     * @return i18n string for given key, with param inserted
     */
    protected String getText(String key, Object param)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key, param);
    }

}
