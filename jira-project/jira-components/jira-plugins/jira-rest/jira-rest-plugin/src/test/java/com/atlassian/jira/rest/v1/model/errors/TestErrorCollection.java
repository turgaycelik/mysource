package com.atlassian.jira.rest.v1.model.errors;

import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestErrorCollection extends TestCase
{

    public void testAddNull()
    {
        try
        {
            ErrorCollection.Builder.newBuilder().addErrorMessage(null);
            fail("Should have thrown exception");
        }
        catch (Exception e)
        {
            //yay
        }
        try
        {
            ErrorCollection.Builder.newBuilder().addErrorCollection(null);
            fail("Should have thrown exception");
        }
        catch (Exception e)
        {
            //yay
        }
        try
        {
            ErrorCollection.Builder.newBuilder().addError(null, "blah");
            fail("Should have thrown exception");
        }
        catch (Exception e)
        {
            //yay
        }
        try
        {
            ErrorCollection.Builder.newBuilder().addError("blah", null);
            fail("Should have thrown exception");
        }
        catch (Exception e)
        {
            //yay
        }
        try
        {
            ErrorCollection.Builder.newBuilder((ErrorCollection) null);
            fail("Should have thrown exception");
        }
        catch (Exception e)
        {
            //yay
        }
        try
        {
            ErrorCollection.Builder.newBuilder((Collection<ValidationError>) null);
            fail("Should have thrown exception");
        }
        catch (Exception e)
        {
            //yay
        }
    }

    public void testAddEmpty()
    {
        final ErrorCollection errorCollection = ErrorCollection.Builder.newBuilder().addErrorCollection(new SimpleErrorCollection()).build();
        assertFalse(errorCollection.hasAnyErrors());
    }

    public void testAddSimpleErrorCollection()
    {
        com.atlassian.jira.util.ErrorCollection errors = new SimpleErrorCollection();
        errors.addError("user", "Bad username");
        errors.addError("field", "Bad field value");
        errors.addErrorMessage("Generic problem");
        errors.addErrorMessage("Another generic issue");
        errors.addErrorMessage("Third generic issue");

        final ErrorCollection errorCollection = ErrorCollection.Builder.newBuilder().addErrorCollection(errors).build();
        assertEquals(3, errorCollection.getErrorMessages().size());
        assertEquals(2, errorCollection.getErrors().size());

        List<String> errorMessages = new ArrayList<String>(errorCollection.getErrorMessages());
        assertEquals("Generic problem", errorMessages.get(0));
        assertEquals("Another generic issue", errorMessages.get(1));
        assertEquals("Third generic issue", errorMessages.get(2));

        Collection<ValidationError> fieldErrors = errorCollection.getErrors();
        for (ValidationError fieldError : fieldErrors)
        {
            if (fieldError.getField().equals("user"))
            {
                assertEquals("Bad username", fieldError.getError());
                assertNull(fieldError.getParams());
            }
            else
            {
                assertEquals("Bad field value", fieldError.getError());
                assertNull(fieldError.getParams());
            }
        }

        final ErrorCollection copiedCollection = ErrorCollection.Builder.newBuilder(errorCollection).build();
        assertNotSame(errorCollection, copiedCollection);

        assertEquals(3, copiedCollection.getErrorMessages().size());
        assertEquals(2, copiedCollection.getErrors().size());

        errorMessages = new ArrayList<String>(copiedCollection.getErrorMessages());
        assertEquals("Generic problem", errorMessages.get(0));
        assertEquals("Another generic issue", errorMessages.get(1));
        assertEquals("Third generic issue", errorMessages.get(2));

        fieldErrors = copiedCollection.getErrors();
        for (ValidationError fieldError : fieldErrors)
        {
            if (fieldError.getField().equals("user"))
            {
                assertEquals("Bad username", fieldError.getError());
                assertNull(fieldError.getParams());
            }
            else
            {
                assertEquals("Bad field value", fieldError.getError());
                assertNull(fieldError.getParams());
            }
        }

    }

    public void testAddErrorMessage()
    {
        final ErrorCollection errorCollection = ErrorCollection.Builder.newBuilder().addErrorMessage("First message").addErrorMessage("Second message").build();
        assertEquals(2, errorCollection.getErrorMessages().size());

        final List<String> errorMessages = new ArrayList<String>(errorCollection.getErrorMessages());
        assertEquals("First message", errorMessages.get(0));
        assertEquals("Second message", errorMessages.get(1));
    }

    public void testAddErrors()
    {
        final ErrorCollection errorCollection = ErrorCollection.Builder.newBuilder().addError("field1", "bad error", "param1", "param2").
                addError("field2", "bad error2").build();

        assertEquals(2, errorCollection.getErrors().size());

        final Collection<ValidationError> fieldErrors = errorCollection.getErrors();
        for (ValidationError fieldError : fieldErrors)
        {
            if (fieldError.getField().equals("field1"))
            {
                assertEquals("bad error", fieldError.getError());
                assertEquals(2, fieldError.getParams().size());
                final List<String> params = new ArrayList<String>(fieldError.getParams());
                assertEquals("param1", params.get(0));
                assertEquals("param2", params.get(1));
            }
            else
            {
                assertEquals("bad error2", fieldError.getError());
                assertNull(fieldError.getParams());
            }
        }

    }

    public void testAddValidationErrors()
    {
        final ErrorCollection errors = ErrorCollection.Builder.newBuilder(new ValidationError("field1", "error1"), new ValidationError("field2", "error2")).build();
        assertEquals(2, errors.getErrors().size());
        assertEquals(0, errors.getErrorMessages().size());


        final Collection<ValidationError> fieldErrors = errors.getErrors();
        for (ValidationError fieldError : fieldErrors)
        {
            if (fieldError.getField().equals("field1"))
            {
                assertEquals("error1", fieldError.getError());
                assertNull(fieldError.getParams());
            }
            else
            {
                assertEquals("field2", fieldError.getField());
                assertEquals("error2", fieldError.getError());
                assertNull(fieldError.getParams());
            }
        }
    }

    public void testAddValidationErrorCollection()
    {
        final List<ValidationError> list = CollectionBuilder.newBuilder(new ValidationError("field1", "error1"), new ValidationError("field2", "error2")).asList();
        final ErrorCollection errors = ErrorCollection.Builder.newBuilder(list).build();
        assertEquals(2, errors.getErrors().size());
        assertEquals(0, errors.getErrorMessages().size());


        final Collection<ValidationError> fieldErrors = errors.getErrors();
        for (ValidationError fieldError : fieldErrors)
        {
            if (fieldError.getField().equals("field1"))
            {
                assertEquals("error1", fieldError.getError());
                assertNull(fieldError.getParams());
            }
            else
            {
                assertEquals("field2", fieldError.getField());
                assertEquals("error2", fieldError.getError());
                assertNull(fieldError.getParams());
            }
        }
    }


    public void testHasAnyErrors()
    {
        final ErrorCollection empty = ErrorCollection.Builder.newBuilder().build();
        assertFalse(empty.hasAnyErrors());

        final ErrorCollection errorCollection = ErrorCollection.Builder.newBuilder().addErrorMessage("blah").build();
        assertTrue(errorCollection.hasAnyErrors());

        final ErrorCollection errorCollection2 = ErrorCollection.Builder.newBuilder().addError("blah", "boo").build();
        assertTrue(errorCollection2.hasAnyErrors());
    }
}
