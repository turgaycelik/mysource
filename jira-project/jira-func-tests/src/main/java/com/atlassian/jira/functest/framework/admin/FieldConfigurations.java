package com.atlassian.jira.functest.framework.admin;

/**
 * Actions to be performed on the field configurations in JIRA's administration.
 *
 * @since v4.0
 */
public interface FieldConfigurations
{
    /**
     * @return the Default Field Configuration Scheme to operate on
     */
    FieldConfiguration defaultFieldConfiguration();

    /**
     * @param  fieldConfigurationName name of the fieldconfiguration
     * @return thee field configuration to operate on
     */
    FieldConfiguration fieldConfiguration(String fieldConfigurationName);

    /**
     * Represents a field configuration that actions can be carried out on
     */
    interface FieldConfiguration
    {
        /**
         * @deprecated since 4.2. This method is not future-proof; as fields get added to configurations, the 'id'
         * sequence might change for the field you intended to use. Use {@link #showFields(String)} instead.
         * @param id the sequence of the field you want to show in the Field Configuration screen
         */
        void showField(int id);

        /**
         * @deprecated since 4.2. This method is not future-proof; as fields get added to configurations, the 'id'
         * sequence might change for the field you intended to use. Use {@link #hideFields(String)} instead.
         * @param id the sequence of the field you want to hide in the Field Configuration screen
         */
        void hideField(int id);

        /**
         * @param name The name of the field you wish to show e.g. <code>Affects Version/s</code>
         */
        void showFields(String name);

        /**
         * @param name The name of the field you wish to hide e.g. <code>Component/s</code>
         */
        void hideFields(String name);

        /**
         * @param name The name of the field you wish to make required e.g. <code>Description</code>
         */
        void requireField(String name);

        /**
         * @param name The name of the field you wish to make optional e.g. <code>Assignee</code>
         */
        void optionalField(String name);

        /**
         * @param fieldName The name of the field e.g. <code>Comment</code>
         * @return the name of the renderer in effect
         * @throws AssertionError if the field is not renderable.
         * @since v4.2
         */
        String getRenderer(String fieldName);

        /**
         * Note: this assumes that the field is a Renderable Field.
         *
         * @param fieldName  The nme of the field to apply the renderer to e.g. <code>Log Work</code>
         * @param rendererName  The type of renderer to apply e.g. <code>Wiki Style Renderer</code>
         * @since v4.2
         */
        void setRenderer(String fieldName, String rendererName);

        /**
         * @param name The name of the field you wish to associate to screens e.g. <code>Time Tracking</code>
         * @return an object to help you configure field screen associations
         * @since v4.2
         */
        FieldScreenAssociations getScreens(String name);
    }
}
