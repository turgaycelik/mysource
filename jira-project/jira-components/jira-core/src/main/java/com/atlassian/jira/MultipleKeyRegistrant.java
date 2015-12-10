package com.atlassian.jira;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.lang.Pair;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.parameters.ComponentParameter;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Helper utility which helps to create single instance of object exposed under multiple keys
 *
 * @since v6.2
 */
public class MultipleKeyRegistrant<T>
{
    public static <T> MultipleKeyRegistrant<T> registrantFor(final @Nonnull Class<T> concrete)
    {
        return new MultipleKeyRegistrant<T>(concrete);
    }


    private final List<Pair<Class<? super T>, ComponentContainer.Scope>> implementing = new ArrayList<Pair<Class<? super T>, ComponentContainer.Scope>>();
    private final Class<T> concrete;
    private final List<Parameter> parameters = new ArrayList<Parameter>();

    MultipleKeyRegistrant(final @Nonnull Class<T> concrete)
    {
        this.concrete = notNull("concrete", concrete);
    }

    public MultipleKeyRegistrant<T> parameter(Class<?> parameter)
    {
        this.parameters.add(new ComponentParameter(parameter));
        return this;
    }

    public MultipleKeyRegistrant<T> parameters(Class<?>... parameters)
    {
        for (Class<?> parameter : parameters)
        {
            parameter(parameter);
        }
        return this;
    }

    public MultipleKeyRegistrant<T> implementing(final Class<? super T> interfaceClass)
    {
        implementing(interfaceClass, null);
        return this;
    }

    public MultipleKeyRegistrant<T> implementing(final Class<? super T> interfaceClass, final ComponentContainer.Scope desiredScope)
    {
        implementing.add(Pair.<Class<? super T>, ComponentContainer.Scope>nicePairOf(interfaceClass, desiredScope));
        return this;
    }

    public void registerWith(final ComponentContainer.Scope defaultScope, final ComponentContainer container)
    {
        Assertions.stateTrue("must implement some interfaces", !implementing.isEmpty());

        //at first register under concrete implementation key with default adapter factory
        if(parameters.isEmpty())
            container.implementation(ComponentContainer.Scope.INTERNAL, concrete, concrete);
        else
            container.implementation(ComponentContainer.Scope.INTERNAL, concrete, concrete, parameters.toArray(new Parameter[parameters.size()]));

        final ComponentAdapter adapter = container.getComponentAdapter(concrete);
        for (Pair<Class<? super T>, ComponentContainer.Scope> pair : implementing)
        {
            ComponentAdapter delegatingAdapter = new KeyedDelegateComponentAdapter(pair.first(), adapter);
            container.component(pair.second() != null ? pair.second() : defaultScope, delegatingAdapter);
        }

    }
}
