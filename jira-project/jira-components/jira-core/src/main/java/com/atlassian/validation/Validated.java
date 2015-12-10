package com.atlassian.validation;

/**
 * Convenience composite for holding the validation results and a domain object. Typical use is to
 * do validation for the purpose of updating an instance of a T and then returning the results of that
 * request in the form of an instance of this class.
 *
 * @since v4.4
 */
public class Validated<T>
{
    Validator.Result result;
    T operand;

    public Validated(Validator.Result result, T operand)
    {
        this.result = result;
        this.operand = operand;
    }

    public Validator.Result getResult()
    {
        return result;
    }

    public T getValue()
    {
        return operand;
    }
}
