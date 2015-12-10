package com.opensymphony.workflow.loader;

/**
 * Needed for the Unit tests.  Since osworkflow 2.8 all descriptors have package
 * level access restrictions.
 */
public class MockStepDescriptor extends StepDescriptor
{
    private int _id;

    public MockStepDescriptor()
    {
        // TODO: Is this constructor used.
        super();
    }

    public MockStepDescriptor(int id)
    {
        super();
        _id = id;
    }

    public int getId()
    {
        return _id;
    }
}
