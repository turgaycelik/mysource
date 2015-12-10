package org.ofbiz.core.entity;

/**
 * Utility class for pseudo-unit tests that need a delegator.
 *
 * @since v5.2
 */
public class GenericDelegatorUtils
{
    /**
     * Returns a new default GenericDelegatord.
     *
     * @return a GenericDelegator
     * @throws GenericEntityException
     * @see #createGenericDelegator(String)
     */
    public static GenericDelegator defaultGenericDelegator() throws GenericEntityException
    {
        return createGenericDelegator("default");
    }

    /**
     * Creates a *new* GenericDelegator instance.
     *
     * @param delegatorName a String indicating the delegator name
     * @return a GenericDelegator
     * @throws GenericEntityException
     */
    public static GenericDelegator createGenericDelegator(String delegatorName) throws GenericEntityException
    {
        return new GenericDelegator(delegatorName);
    }
}
