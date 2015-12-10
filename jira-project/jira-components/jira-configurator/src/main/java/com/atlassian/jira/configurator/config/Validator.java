
package com.atlassian.jira.configurator.config;

import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

/**
 * Cleans or validates a string input value for a field.
 *
 * @since v5.1
 */
public abstract class Validator<T>
{
    public abstract T apply(String label, String input) throws ValidationException;

    static Boolean parseBoolean(String label, String input) throws ValidationException
    {
        input = trim(input);
        if (input == null)
        {
            return null;
        }
        input = input.toLowerCase();
        switch (input.charAt(0))
        {
            case '1': case 'y': case 't': return Boolean.TRUE;
            case '0': case 'n': case 'f': return Boolean.FALSE;
        }
        throw new ValidationException(label, "Please use a boolean value like 'true' or 'false'");
    }

    static Integer parseInteger(String label, String input) throws ValidationException
    {
        return parseInteger(label, input, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    static Integer parseInteger(String label, String input, int minValue, int maxValue) throws ValidationException
    {
        final Long value = parseLong(label, input);
        if (value == null)
        {
            return null;
        }
        if (value < minValue || value > maxValue)
        {
            throw new ValidationException(label, "The value " + value + " is outside of the accepted range [" + minValue + ',' + maxValue + ']');
        }
        return value.intValue();
    }

    static Long parseLong(String label, String input) throws ValidationException
    {
        input = trim(input);
        if (input == null)
        {
            return null;
        }
        try
        {
            return Long.valueOf(input);
        }
        catch (NumberFormatException nfe)
        {
            throw new ValidationException(label, "An integer value is required");
        }
    }

    static String trim(String input)
    {
        if (input != null)
        {
            input = input.trim();
            if (input.length() == 0)
            {
                return null;
            }
        }
        return input;
    }

    public static final Validator<Integer> INTEGER = new Validator<Integer>()
    {
        @Override
        public Integer apply(String label, String input) throws ValidationException
        {
            return parseInteger(label, input);
        }
    };

    public static final Validator<Integer> INTEGER_POSITIVE = new Validator<Integer>()
    {
        @Override
        public Integer apply(String label, String input) throws ValidationException
        {
            final Integer value = parseInteger(label, input);
            if (value != null && value <= 0)
            {
                throw new ValidationException(label, "Only positive values are allowed");
            }
            return value;
        }
    };

    public static final Validator<Integer> INTEGER_POSITIVE_OR_ZERO = new Validator<Integer>()
    {
        @Override
        public Integer apply(String label, String input) throws ValidationException
        {
            final Integer value = parseInteger(label, input);
            if (value != null && value < 0)
            {
                throw new ValidationException(label, "Negative values are not allowed");
            }
            return value;
        }
    };

    public static final Validator<Integer> INTEGER_ALLOW_MINUS_1 = new Validator<Integer>()
    {
        @Override
        public Integer apply(String label, String input) throws ValidationException
        {
            final Integer value = parseInteger(label, input);
            if (value != null && value < -1)
            {
                throw new ValidationException(label, "Negative one (-1) is the only negative value that is allowed");
            }
            return value;
        }
    };

    public static final Validator<Long> LONG = new Validator<Long>()
    {
        @Override
        public Long apply(String label, String input) throws ValidationException
        {
            return parseLong(label, input);
        }
    };

    public static final Validator<Long> LONG_POSITIVE = new Validator<Long>()
    {
        @Override
        public Long apply(String label, String input) throws ValidationException
        {
            final Long value = parseLong(label, input);
            if (value != null && value <= 0L)
            {
                throw new ValidationException(label, "Only positive values are allowed");
            }
            return value;
        }
    };

    public static final Validator<Long> LONG_POSITIVE_OR_ZERO = new Validator<Long>()
    {
        @Override
        public Long apply(String label, String input) throws ValidationException
        {
            final Long value = parseLong(label, input);
            if (value != null && value < 0L)
            {
                throw new ValidationException(label, "Negative values are not allowed");
            }
            return value;
        }
    };

    public static final Validator<Long> LONG_ALLOW_MINUS_1 = new Validator<Long>()
    {
        @Override
        public Long apply(String label, String input) throws ValidationException
        {
            final Long value = parseLong(label, input);
            if (value != null && value < -1L)
            {
                throw new ValidationException(label, "Negative one (-1) is the only negative value that is allowed");
            }
            return value;
        }
    };

    public static final Validator<Boolean> BOOLEAN = new Validator<Boolean>()
    {
        @Override
        public Boolean apply(String label, String input) throws ValidationException
        {
            return parseBoolean(label, input);
        }
    };

    public static final Validator<String> TRIMMED_STRING = new Validator<String>()
    {
        @Override
        public String apply(String label, String input)
        {
            return trim(input);
        }
    };

    public static final Validator<Integer> PORT = new Validator<Integer>()
    {
        private final int MIN_PORT = 0;
        private final int MAX_PORT = 65535;
        private final String ERROR_MESSAGE = "The port must be a number between " + Integer.toString(MIN_PORT) + " and " + Integer.toString(MAX_PORT) + ".";

        @Override
        public Integer apply(@Nullable final String label, @Nullable final String input) throws ValidationException
        {
            final String nonNullInput = Validator.NON_EMTPY_STRING.apply(label, input);
            try
            {
                final int httpsPort = Integer.parseInt(nonNullInput);
                if (MIN_PORT >= httpsPort || httpsPort >= MAX_PORT)
                {
                    throw new ValidationException(label, ERROR_MESSAGE);
                }
                return httpsPort;
            }
            catch (NumberFormatException e)
            {
                throw new ValidationException(label, ERROR_MESSAGE);
            }
        }
    };

    public static final Validator<String> NON_EMTPY_STRING = new Validator<String>()
    {
        @Override
        public String apply(@Nullable String label, @Nullable String input) throws ValidationException
        {
            final String nonNullInput = Strings.nullToEmpty(input).trim();
            if (nonNullInput.isEmpty())
            {
                throw new ValidationException(label, label + " is a mandatory field.");
            }
            return nonNullInput;
        }
    };

    public static final Validator<String> EXISTING_FILE = new Validator<String>()
    {
        @Override
        public String apply(@Nullable String label, @Nonnull String input) throws ValidationException
        {
            final String nonNullInput = NON_EMTPY_STRING.apply(label, input);
            final File file = new File(nonNullInput).getAbsoluteFile();
            if (!file.exists())
            {
                throw new ValidationException(label, "The specified path doesn't exist.");
            }
            else if (!file.isFile())
            {
                throw new ValidationException(label, "The specified path doesn't denote a file.");
            }
            else
            {
                return file.getPath();
            }
        }
    };

}

