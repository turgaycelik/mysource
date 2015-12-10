package com.atlassian.jira.webwork;

import com.atlassian.jira.action.SafeAction;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.webwork.parameters.KnownParameterConverters;
import com.atlassian.jira.webwork.parameters.ParameterConverter;
import com.google.common.collect.MapMaker;
import org.apache.log4j.Logger;
import webwork.action.Action;
import webwork.action.IllegalArgumentAware;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static com.atlassian.jira.util.collect.CollectionUtil.findFirstMatch;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.Assertions.stateTrue;
import static java.util.Arrays.asList;

/**
 * This class is a replacement for the broken and dangerous webwork1 'el' based {@link
 * webwork.util.BeanUtil#setProperties(java.util.Map, Object)}
 * <p/>
 * This uses a different set of rules when accepting input from the web, with some nods towards webwork1 to keep its old
 * behaviour but lose its dangerous nature.
 * <p/>
 * <h3>Top Level Code Only</h3>
 * <p/>
 * Only Action level public {@link java.beans} setters can be invoked.  You can not longer navigate away from an Action
 * into other code.  In the old days one could invoke nearly arbitrary code. Uncool!
 * <p/>
 * <h3>Null Values Are Never Set</h3>
 * <p/>
 * Null parameter values are never set into the action.  This is old behaviour.
 * <p/>
 * <h3>Only Certain Data Types</h3>
 * <p/>
 * See the class {@link com.atlassian.jira.webwork.parameters.KnownParameterConverters} for a complete list but
 * basically its Strings, Longs, Integers, Shorts, Bytes and so on.
 * <p/>
 * <h3>String / String[] / Other Types Precedence</h3>
 * <p/>
 * The webwork1 code used the above precedence in choosing a setter.  More by accident that by design I suspect.
 * <p/>
 * <h3>webwork.action.IllegalArgumentAware</h3>
 * <p/>
 * If an action is {@link webwork.action.IllegalArgumentAware}, then it will be told about bad parameters and the
 * exception will be ignored.  All {@link com.atlassian.jira.web.action.JiraWebActionSupport} actions implements {@link
 * webwork.action.IllegalArgumentAware}. JIRA is aware!
 *
 * <p/>
 * Introduced / changed as part of JRA-15664
 *
 * @since v3.13.2
 */
public class JiraSafeActionParameterSetter
{
    private static final Logger log = Logger.getLogger(JiraSafeActionParameterSetter.class);

    /**
     * Weakly keyed map.
     */
    @ClusterSafe
    private final ConcurrentMap<Class<? extends Action>, PropertyDescriptor[]> setterPropertyDescriptors = new MapMaker().weakKeys().makeMap();

    /**
     * This is called to set a map of parameters into an action.  This is the designated way we want input from the web
     * to be set into into web actions.
     * <p/>
     * The action MUST not be a {@link com.atlassian.jira.action.SafeAction} and an assertions is made to that end.
     *
     * @param action the action in play
     * @param webParameters the map of web request parameters
     */
    public void setSafeParameters(final Action action, final Map<String, ?> webParameters)
    {
        Assertions.notNull("action", action);
        Assertions.not("backend action passed in", action instanceof SafeAction);

        // This should not be true but with webwork1 who knows so lest be defensive!
        if (webParameters == null)
        {
            return;
        }
        final Set<String> parameterKeys = webParameters.keySet();
        if (parameterKeys.isEmpty())
        {
            return;
        }

        // for efficiency only ask for the descriptors once
        final PropertyDescriptor[] setterDescriptors = getSetterPropertyDescriptors(action);
        if (setterDescriptors.length == 0)
        {
            if (log.isDebugEnabled())
            {
                log.debug("This action does not have any settable properties : '" + action + "'");
            }
            return;
        }

        for (final String paramName : parameterKeys)
        {
            final String[] paramValue = normaliseToStringArr(webParameters, paramName);
            if (paramValue == null)
            {
                // nothing to set.  webwork1 never set anything on null parameters and neither do we
                continue;
            }

            final Method setterMethod = getSetterForParameter(paramName, setterDescriptors);
            if (setterMethod != null)
            {
                try
                {
                    setActionProperty(setterMethod, action, paramValue);
                }
                catch (final IllegalArgumentException iae)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Unable invoke setter '" + setterMethod + "' with parameter '" + paramName + "'", iae);
                    }
                    //
                    // In webwork1, if an action is IllegalArgumentAware, then it is told about the problem and the exception is ignored
                    // So we have to do the same.
                    //
                    if (action instanceof IllegalArgumentAware)
                    {
                        ((IllegalArgumentAware) action).addIllegalArgumentException(paramName, iae);
                    }
                    else
                    {
                        throw iae;
                    }
                }
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("No property setter is availble on Action '" + action.getClass() + "' for parameter '" + paramName + "'");
                }
            }
        }
    }

    /**
     * We should only be called if we have a valid method.  This will then set the given value into the action via the
     * setter method.
     *
     * @param setterMethod the setter in play
     * @param action the action in play
     * @param paramValue the value to be set
     * @throws IllegalArgumentException if the parameter cannot be converted or the method cannot be reflected on.  It
     * use this to simulate the same exceptions as webwork1 originally used.
     */
    private void setActionProperty(final Method setterMethod, final Action action, final String[] paramValue)
    {
        //
        // Big assumption being made here.  Previous code should have check for sanity of the arguments
        //
        notNull("paramValue", paramValue);
        notNull("setterMethod", setterMethod);
        notNull("setterMethod", setterMethod.getParameterTypes());
        stateTrue("setterMethod", setterMethod.getParameterTypes().length == 1);

        final Class<?> parameterType = setterMethod.getParameterTypes()[0];

        try
        {
            //
            // webwork1 used to set String.class setters first, then String[].class and then the others
            // so we have to as well.
            //
            final Object convertedObj;
            if (parameterType.equals(String.class))
            {
                convertedObj = paramValue[0];
            }
            else if (parameterType.equals(String[].class))
            {
                convertedObj = paramValue;
            }
            else
            {
                final ParameterConverter converter = KnownParameterConverters.getConverter(parameterType);
                Assertions.notNull("converter", converter);
                convertedObj = converter.convertParameter(paramValue, parameterType);
            }
            //
            // now reflect on the method with the converted value
            // if the value ended up null here, webwork1 still used to invoke the setter and so do we
            //
            setterMethod.invoke(action, convertedObj);
        }
        ///CLOVER:OFF
        catch (final IllegalArgumentException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Unable to convert property value for setter '" + setterMethod + "'");
            }
            throw e;
        }
        catch (final InvocationTargetException e)
        {
            // act like web work which throws IAEs
            throw new IllegalArgumentException(e);
        }
        catch (final IllegalAccessException e)
        {
            // act like web work which throws IAEs
            throw new IllegalArgumentException(e);
        }
        ///CLOVER:ON
    }

    /**
     * This will try to find an acceptable setter method for given parameter name
     *
     * @param paramName the parameter name
     * @param setterDescriptors the setters for this action
     * @return a valid setter method or null if one can be found
     */
    private Method getSetterForParameter(final String paramName, final PropertyDescriptor[] setterDescriptors)
    {
        // These have been sorted into a preferred order.  We pick the first one matches and
        // only the first one only. There is no retry if their happens to be more than one!
        final PropertyDescriptor descriptor = findFirstMatch(asList(setterDescriptors), new Predicate<PropertyDescriptor>()
        {
            public boolean evaluate(final PropertyDescriptor input)
            {
                return paramName.equals(input.getName());
            }
        });
        return (descriptor == null) ? null : descriptor.getWriteMethod();
    }

    /**
     * This will get an array of {@link java.beans.PropertyDescriptor} that represent the simple setters of a class.  It
     * does not include {@link java.beans.IndexedPropertyDescriptor}s
     *
     * @param action the action to check
     * @return a non null array of PropertyDescriptor objects sorted to prefer arrays first, then by wideness of
     *         arguments
     */
    private PropertyDescriptor[] getSetterPropertyDescriptors(final Action action)
    {
        // cache for fast lookup of validated setters of a given class
        final Class<? extends Action> actionClass = action.getClass();
        PropertyDescriptor[] descriptors = setterPropertyDescriptors.get(actionClass);

        if (descriptors == null)
        {
            final BeanInfo beanInfo;
            try
            {
                beanInfo = Introspector.getBeanInfo(actionClass, Object.class);
            }
            ///CLOVER:OFF
            catch (final IntrospectionException e)
            {
                return new PropertyDescriptor[0];
            }
            ///CLOVER:ON

            descriptors = beanInfo.getPropertyDescriptors();
            final List<PropertyDescriptor> descriptorList = new ArrayList<PropertyDescriptor>(descriptors.length);
            for (final PropertyDescriptor descriptor : descriptors)
            {
                final Method setterMethod = descriptor.getWriteMethod();
                if (!(descriptor instanceof IndexedPropertyDescriptor))
                {
                    // we have a setter.  Is it deemed ok to call?
                    if (isMethodShapeOk(setterMethod))
                    {
                        descriptorList.add(descriptor);
                    }
                }
            }
            //
            // sort into a specific order so we prefer some over others
            Collections.sort(descriptorList, new SetterDescriptorComparator());

            descriptors = new PropertyDescriptor[descriptorList.size()];
            descriptorList.toArray(descriptors);

            // Put into cache if its not already there
            final PropertyDescriptor[] result = setterPropertyDescriptors.putIfAbsent(actionClass, descriptors);
            return (result == null) ? descriptors : result;
        }
        return descriptors;
    }

    /**
     * This will test that the setter method take the type of parameters that we allow as web input.
     *
     * @param setterMethod the setter method in play
     * @return true if the shape of the setter is OK
     */
    private boolean isMethodShapeOk(final Method setterMethod)
    {
        boolean ok = false;
        if (setterMethod != null)
        {
            final int modifiers = setterMethod.getModifiers();
            //
            // most of these are true because of the Bean introspection but lets double check
            if (Modifier.isPublic(modifiers) && !(Modifier.isAbstract(modifiers) && (!Modifier.isStatic(modifiers))))
            {
                final Class<?>[] parameterTypes = setterMethod.getParameterTypes();
                if ((parameterTypes != null) && (parameterTypes.length == 1))
                {
                    // make sure its not part of Object.class
                    if ((!setterMethod.getDeclaringClass().equals(Object.class)))
                    {
                        // do we have a setter that can actually set this type of property
                        final Class<?> parameterType = parameterTypes[0];
                        final ParameterConverter converter = KnownParameterConverters.getConverter(parameterType);
                        if (converter != null)
                        {
                            ok = true;
                        }
                        // we don't have a converter but rather do we set it directly
                        else if (String.class.equals(parameterType) || String[].class.equals(parameterType))
                        {
                            ok = true;
                        }
                    }
                }
            }
        }
        return ok;
    }

    /**
     * webwork1 puts a Map over the {@link javax.servlet.http.HttpServletRequest#getParameterValues(String)} and hence
     * we lose the String[] type safety.  But we know its really a String[] but this function helps ensure that it
     * becomes a valid one.
     *
     * @param parameterMap the map of parameter values from webwork1
     * @param paramName the name of the parameter
     * @return a String[] or null if there are no values for that input.
     */
    private String[] normaliseToStringArr(final Map<String, ?> parameterMap, final String paramName)
    {
        final Object paramValue = parameterMap.get(paramName);
        if (paramValue == null)
        {
            return null;
        }
        final String[] returnValue = (paramValue instanceof String[]) ? (String[]) paramValue : new String[] { String.valueOf(paramValue) };
        // now check it for sanity.  If its all null entries then its not good
        // This is really unlikely but lets at least cover the possibility
        for (final String aReturnValue : returnValue)
        {
            if (aReturnValue != null)
            {
                return returnValue;
            }
        }
        // must be all nulls so lets make it null
        return null;
    }

    /**
     * This will sort a bunch of setter methods in the following order
     * <p/>
     * - By preferring an array version of a non array version e.g. setX(int[] val) in preference to setX(int val)
     */
    static class SetterDescriptorComparator implements Comparator<PropertyDescriptor>
    {
        public int compare(final PropertyDescriptor o1, final PropertyDescriptor o2)
        {
            // stage1 prefer arrays first
            final Class<?> parameterType1 = o1.getWriteMethod().getParameterTypes()[0];
            final Class<?> parameterType2 = o2.getWriteMethod().getParameterTypes()[0];

            return preferArrays(parameterType1, parameterType2);
        }

        private int preferArrays(final Class<?> parameterType1, final Class<?> parameterType2)
        {
            if (parameterType1.isArray() && parameterType2.isArray())
            {
                return 0;
            }
            else if (!parameterType1.isArray() && !parameterType2.isArray())
            {
                return 0;
            }
            else
            {
                return (parameterType1.isArray() ? 1 : -1);
            }
        }
    }
}
