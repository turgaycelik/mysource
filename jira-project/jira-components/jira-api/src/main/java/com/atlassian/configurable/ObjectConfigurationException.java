package com.atlassian.configurable;

import com.atlassian.annotations.PublicApi;
import com.atlassian.core.AtlassianCoreException;

/**
 * This exception is thrown an error occurs during the use of ConfigurableObjects
 */
@PublicApi
public class ObjectConfigurationException extends AtlassianCoreException
{
    /**
     * Creates a new ObjectConfigurationException object.
     *
     * @param message
     */
    public ObjectConfigurationException(String message)
    {
        super(message);
    }

    /**
     * Creates a new ObjectConfigurationException object.
     *
     * @param cause Original exception
     */
    public ObjectConfigurationException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a new ObjectConfigurationException object.
     *
     * @param message
     * @param cause   Original exception
     */
    public ObjectConfigurationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
