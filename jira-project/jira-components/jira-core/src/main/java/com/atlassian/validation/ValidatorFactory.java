package com.atlassian.validation;

import com.atlassian.util.concurrent.LazyReference;
import com.google.common.base.Supplier;
import org.apache.log4j.Logger;

/**
 * Creates an instance of a {@link Validator} from a string.
 * <p/>
 * Do not use this API, it is subject to change.
 *
 * @since v4.4
 */
public final class ValidatorFactory
{
    private static final Logger log = Logger.getLogger(ValidatorFactory.class);

    /**
     * Gets an instance given a validator FQ class name.
     *
     * @param validator the serialised form of the validator.
     * @return the validator.
     */
    public Validator getInstance(String validator)
    {

        log.debug("Instantiating validator of type " + validator);
        try
        {
            return (Validator) Class.forName(validator).newInstance();
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Supplier<Validator> getInstanceLazyReference(final String validator)
    {
        return new LazyReference<Validator>()
        {
            @Override
            protected Validator create() throws Exception
            {
                return getInstance(validator);
            }
        };
    }
}
