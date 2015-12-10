package com.atlassian.jira.functest.framework.admin;

/**
 * Represents the form used to edit the configuration of a POP/IMAP Server.
 *
 * @since v4.3
 */
public interface EditPopServerConfiguration
{
    /**
     * Sets the name field to an specified value. This can not be blank.
     * @param name The value of the name to set.
     * @return This instance of the Edit Pop Server Configuration form.
     */
    EditPopServerConfiguration setName(String name);

    /**
     * Sets the description field to an specified value. This field is optional and can be blank.
     * @param description The value of the description to set.
     * @return This instance of the Edit Pop Server Configuration form.
     */
    EditPopServerConfiguration setDescription(String description);

    /**
     * Sets the protocol select list to an specified option. <em>The default option is POP.</em>
     * @param protocol The value of the protocol to set.
     * This must be one of the display values of the available options in the select list
     * <strong>(POP, SECURE_POP, IMAP, SECURE_IMAP)</strong>
     * @return This instance of the Edit Pop Server Configuration form.
     */
    EditPopServerConfiguration setProtocol(String protocol);

    /**
     * Sets the host name field to an specified value. This can not be blank.
     * @param hostName The value of the host name to set.
     * @return This instance of the Edit Pop Server Configuration form.
     */
    EditPopServerConfiguration setHostName (String hostName);

    /**
     * Sets the port field to an specified value. This field is optional and can be blank, if left blank the port will
     * be set to the default value for the selected protocol.
     * @param portNumber The value of the port to set.
     * @return This instance of the Edit Pop Server Configuration form.
     */
    EditPopServerConfiguration setPort(String portNumber);

    /**
     * Sets the timeout field to an specified value. This is optional and can be blank. If left blank it will default to
     * 10000 milliseconds.
     * @param timeout The value of the timeout to set.
     * @return This instance of the Edit Pop Server Configuration form.
     */
    EditPopServerConfiguration setTimeout(String timeout);

    /**
     * Sets the user name field to an specified value. This is a mandatory field and can not be left blank.
     * @param userName The value of the user name to set.
     * @return This instance of the Edit Pop Server Configuration form.
     */
    EditPopServerConfiguration setUserName(String userName);

    /**
     * Sets the password field to an specified value. It is mandatory to call this method before calling
     * {@link #update()} to submit the form, this is because the field is always set blank when the update form
     * is displayed.
     * @param password The value of the password to set.
     * @return This instance of the Edit Pop Server Configuration form.
     */
    EditPopServerConfiguration setPassword(String password);

    /**
     * Submits the edit POP/IMAP server configuration form.
     * @return The Mail Server Administration page instance that is displayed after submitting the form.
     * @throws DefaultEditPopServerConfiguration.PasswordIsMandatoryException if no value has been set for the password
     * field, you should always call {@link #setPassword(String)} before calling this method.
     */
    MailServerAdministration update() throws DefaultEditPopServerConfiguration.PasswordIsMandatoryException;
}
