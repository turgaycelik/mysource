package com.atlassian.jira;

import com.atlassian.jira.extension.ContainerProvider;
import com.atlassian.jira.junit.rules.InitMockitoMocks;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.picocontainer.MutablePicoContainer;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @since v6.2
 */
public class TestWrappedComponentContainer
{
    @Mock
    private MutablePicoContainer internalContainer;
    @Mock
    private ComponentContainer componentContainer;

    @Rule
    public InitMockitoMocks mocks = new InitMockitoMocks(this);
    private WrappedComponentContainer wrappedComponentContainer;

    @Before
    public void before(){
        internalContainer = mock(MutablePicoContainer.class);
        when(componentContainer.getPicoContainer()).thenReturn(internalContainer);

        wrappedComponentContainer = new WrappedComponentContainer(componentContainer);
    }

    @Test
    public void shouldExposeComponentsContainerPicoWhenNotWrapped(){
        Assert.assertSame(internalContainer, wrappedComponentContainer.getPicoContainer());
        Assert.assertFalse(wrappedComponentContainer.isWrapped());
    }

    @Test
    public void shouldExposeWrappedContainerAfterWrapping(){
        MutablePicoContainer wrapper = mock(MutablePicoContainer.class);
        ContainerProvider wrapperProvider = mock(ContainerProvider.class);
        when(wrapperProvider.getContainer(internalContainer)).thenReturn(wrapper);

        wrappedComponentContainer.wrapWith(wrapperProvider);

        Assert.assertSame(wrapper, wrappedComponentContainer.getPicoContainer());
        Assert.assertTrue(wrappedComponentContainer.isWrapped());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowToWrapTwice(){
        MutablePicoContainer wrapper = mock(MutablePicoContainer.class);
        ContainerProvider wrapperProvider = mock(ContainerProvider.class);
        when(wrapperProvider.getContainer(internalContainer)).thenReturn(wrapper);

        wrappedComponentContainer.wrapWith(wrapperProvider);

        wrappedComponentContainer.wrapWith(mock(ContainerProvider.class));
    }

    @Test
    public void shouldDetachComponentsOnDispose(){
        MutablePicoContainer wrapper = mock(MutablePicoContainer.class);
        ContainerProvider wrapperProvider = mock(ContainerProvider.class);
        when(wrapperProvider.getContainer(internalContainer)).thenReturn(wrapper);

        wrappedComponentContainer.wrapWith(wrapperProvider);

        wrappedComponentContainer.dispose();
        verify(wrapper).dispose();

        assertNull(wrappedComponentContainer.getComponentContainer());
        assertNull(wrappedComponentContainer.getPicoContainer());
    }

}
