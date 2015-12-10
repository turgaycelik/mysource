package com.atlassian.jira.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotated type (a component), field (a component dependency for another class), or constructor was designed to be
 * compatible with injection, meaning that:
 *
 * <li>for Types, other classes can confidently access an instance of this type as a dependency via auto-wired
 * injection;
 * <li>for Fields, they can be instantiated via auto-wired dependency injection;
 * <li>for Constructors, there are no parameters declared which are NonInjectableComponents, and thus should be
 * satisfiable under default dependency injection circumstances
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(value = RetentionPolicy.CLASS)
@java.lang.annotation.Target(value = { ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR })
public @interface InjectableComponent
{}