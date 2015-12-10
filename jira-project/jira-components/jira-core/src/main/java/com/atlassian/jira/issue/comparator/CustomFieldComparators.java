package com.atlassian.jira.issue.comparator;

import java.util.Comparator;
import java.util.Locale;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;

import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

/**
 * Utility class for dealing with custom fields.
 *
 * @since v5.1
 */
public class CustomFieldComparators
{
    /**
     * Returns a comparator that compares {@code CustomField} instances by name.
     * This is sorted by the customfield's untranslated name to preserve existing behaviour.
     *
     * @return a Comparator&lt;CustomField&gt;
     */
    public static Comparator<CustomField> byName()
    {
        return new NameComparator();
    }

    /**
     * Returns a comparator that compares {@code CustomField} instances by the translated customfield name, using the supplied locale.
     * @param locale The locale to use for comparison
     * @param translationManager The translation manger component
     *
     * @return a Comparator&lt;CustomField&gt;
     */
    public static Comparator<CustomField> byTranslatedName(Locale locale, TranslationManager translationManager)
    {
        return new TranslatedNameComparator(locale, translationManager);
    }

    /**
     * Returns a comparator that compares custom field {@code GenericValue} instances by name.
     *
     * @return a Comparator&lt;GenericValue&gt;
     */
    public static Comparator<GenericValue> byGvName()
    {
        return new CustomFieldComparator();
    }

    static int compareNames(String name1, String name2)
    {
        if (name1 == null && name2 == null)
            return 0;

        if (name1 == null)
            return -1;

        if (name2 == null)
            return 1;

        return name1.compareTo(name2);
    }

    /**
     * Compares two {@code CustomField} objects by their translated names.
     */
    private static class NameComparator implements Comparator<CustomField>
    {
        public NameComparator()
        {
        }

        @Override
        public int compare(CustomField o1, CustomField o2)
        {
            if (o1 == null && o2 == null)
                return 0;

            if (o1 == null)
                return -1;

            if (o2 == null)
                return 1;

            return compareNames(getCustomFieldName(o1), getCustomFieldName(o2));
        }

        /**
         * This is an optimised get of the field names so we don't continually need to resolve the locale
         * @param customField
         * @return Name
         */
        protected String getCustomFieldName(final CustomField customField)
        {
            return customField.getUntranslatedName();
        }
    }

    /**
     * Compares two {@code CustomField} objects by their translated names.
     */
    private static class TranslatedNameComparator extends NameComparator implements Comparator<CustomField>
    {
        private final TranslationManager translationManager;
        private final Locale locale;

        public TranslatedNameComparator(final Locale locale, TranslationManager translationManager)
        {
            super();
            this.locale = locale;
            this.translationManager = translationManager;
        }

        /**
         * This is an optimised get of the field names so we don't continually need to resolve the locale
         * @param customField
         * @return Name
         */
        protected String getCustomFieldName(final CustomField customField)
        {
            String translatedName = translationManager.getCustomFieldNameTranslation(customField, locale);
            if (StringUtils.isNotEmpty(translatedName))
            {
                return translatedName;
            }
            return customField.getUntranslatedName();
        }
    }
}
