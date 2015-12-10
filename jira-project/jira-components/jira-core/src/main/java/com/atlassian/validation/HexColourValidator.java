package com.atlassian.validation;

import static com.atlassian.jira.util.Colours.isHexColour;

/**
 * Validate a string matches what we expect for a hex representation of a colour  eg. "#33445A"
 *
 * @since v5.2
 */
public class HexColourValidator implements Validator
{
    @Override
    public Result validate(String value)
    {
        if (value == null)
        {
            return new Failure("Hex colour string cannot be null");
        }

        if (isHexColour(value))
        {
            return new Success(value);
        }
        else
        {
            return new Failure("String does not appear to match a hex colour string eg. \"#334A5B\"");
        }
    }
}
