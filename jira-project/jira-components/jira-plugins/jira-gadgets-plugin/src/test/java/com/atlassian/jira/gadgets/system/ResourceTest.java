package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import junit.framework.TestCase;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Abstract test case class for REST resources test case, with some useful reusable utility methods common to most REST
 * resources.
 *
 * @since v4.0
 */
public abstract class ResourceTest extends TestCase
{
    private static final ToStringStyle TO_STRING_STYLE = ToStringStyle.SHORT_PREFIX_STYLE;

    protected IMocksControl mockControl;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        createMockControl();
        EasyMockAnnotations.initMocks(this);
    }

    /**
     * Creates a {@link org.easymock.classextension.IMocksControl} with default settings. Subclasses can override to
     * create nice or strict control.
     */
    protected void createMockControl()
    {
        mockControl = EasyMock.createControl();
    }

    /**
     * Asserts that the REST responses equal.
     * <p/>
     * Currently, only status and entity are checked. Cache control is not checked.
     *
     * @param expected the expected response
     * @param actual   the actual response
     */
    protected void assertEquals(Response expected, Response actual)
    {
        assertEquals("Response has an unexpected status code. expected <" + expected.getStatus() + "> but was:<" +
                actual.getStatus() + ">. Response expected:<" +
                ToStringBuilder.reflectionToString(expected, TO_STRING_STYLE) + "> but was:<" +
                ToStringBuilder.reflectionToString(actual, TO_STRING_STYLE) + ">",
                expected.getStatus(), actual.getStatus());
        if (expected.getEntity() instanceof ErrorCollection && actual.getEntity() instanceof ErrorCollection)
        {
            assertEquals((ErrorCollection) expected.getEntity(), (ErrorCollection) actual.getEntity());
        }
        else
        {
            assertEntityEquals(expected.getEntity(), actual.getEntity());
        }
        // TODO: assert cache control equals but there is no apparent API to retrieve this info
    }

    /**
     * Intended to be overriden by subclass where it cannot reply on the equals() method implementation of the entity
     * class.
     * <p/>
     * It is recommended to override {@link Object#equals(Object)} method for all classes referenced by entities
     * contained in the response.
     *
     * @param expected the expected entity contained in the {@link javax.ws.rs.core.Response} object
     * @param actual   the actual entity contained in the {@link javax.ws.rs.core.Response} object
     */
    protected void assertEntityEquals(final Object expected, final Object actual)
    {
        assertThat(actual, equalTo(expected));
    }

    /**
     * Asserts that the error collections equal.
     *
     * @param expected the expected error collection
     * @param actual   the actual error collection
     */
    protected void assertEquals(ErrorCollection expected, ErrorCollection actual)
    {
        assertEquals(expected.hasAnyErrors(), actual.hasAnyErrors());
        assertEquals(expected.getErrorMessages(), actual.getErrorMessages());
        List<ValidationError> expectedVes = new ArrayList<ValidationError>(expected.getErrors());
        List<ValidationError> actualVes = new ArrayList<ValidationError>(actual.getErrors());
        assertEquals(expectedVes.size(), actualVes.size());
        for (int i = 0; i < expectedVes.size(); i++)
        {
            ValidationError e = expectedVes.get(i);
            ValidationError a = actualVes.get(i);
            assertTrue("expected:<" + ToStringBuilder.reflectionToString(e, TO_STRING_STYLE) +
                    "> but was:<" + ToStringBuilder.reflectionToString(a, TO_STRING_STYLE) + ">",
                    EqualsBuilder.reflectionEquals(e, a));
        }
    }

    /**
     * Creates a mock of the given interface or non-final class using the mock control created by {@link
     * #createMockControl()}.
     *
     * @param toMock the class object of the interface or non-final class to mock
     * @param <T>    the type
     * @return a mock object
     */
    protected <T> T mock(Class<T> toMock)
    {
        return mockControl.createMock(toMock);
    }

    /**
     * Switches from record mode to replay mode for all mocks created using {@link #mock(Class)} method.
     */
    protected void replayAll()
    {
        mockControl.replay();
        EasyMockAnnotations.replayMocks(this);
    }

    /**
     * Verifies the behaviours of all mocks created using {@link #mock(Class)} method.
     */
    protected void verifyAll()
    {
        mockControl.verify();
    }
}
