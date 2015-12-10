package com.atlassian.jira.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotated type (a component), field (a component dependency for another class), or constructor was not designed
 * to be compatible with injection, meaning that:
 *
 * <li>for Types, other classes should not attempt to access an instance of this type as a dependency via auto-wired
 * injection, as instances of this type retain instance-specific state;
 * <li>for Fields, they should be instantiated manually;
 * <li>for Constructors, the parameters declared in them include components which are NonInjectableComponents, and thus
 * would not be satisfiable under default dependency injection circumstances
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(value = RetentionPolicy.CLASS)
@java.lang.annotation.Target(value = { ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR })
public @interface NonInjectableComponent
{}