package com.atlassian.jira.issue.fields.rest.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This class is used to pass full generics type information. It is based on ideas from <a
 * href="http://gafter.blogspot.com/2006/12/super-type-tokens.html" >http://gafter.blogspot.com/2006/12/super-type-tokens.html</a>,
 * <p/>
 * To use this class create an anonymous subclass, e.g. to deserialise to generic type <code>List&lt;User></code>:
 * <pre>
 *  TypeRef ref = new TypeRef&lt;List&lt;User>>() { };
 * </pre>
 */
public abstract class TypeRef<T> implements Comparable<TypeRef<T>>
{
    final Type _type;

    protected TypeRef()
    {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class)
        {
            throw new IllegalArgumentException("Internal error: TypeRef constructed without actual type information");
        }

        _type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public int compareTo(TypeRef<T> o) { return 0; }
}
