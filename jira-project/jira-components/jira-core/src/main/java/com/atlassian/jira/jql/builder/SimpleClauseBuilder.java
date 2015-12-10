package com.atlassian.jira.jql.builder;

import com.atlassian.query.clause.Clause;

/**
 * A builder that can create JQL clauses by combining other clauses with logical operators. This builder's interface is
 * very low level and is designed to be built upon by other builders to present a better interface.
 * <p/>
 * The builder is designed to be used in a fluenet style. For example the JQL <tt>clause1 and clause2 or clause3</tt>
 * can be build using {@code new BasicJqlBuilder().add(clause1).and().add(clause2).or().add(clause3).build()}.
 * <p/>
 * It is the caller's responsibility to ensure that they call the methods in an order that will generate valid JQL. The
 * builder will throw an {@link IllegalStateException} if an attempt is made to generate invalid JQL. For example, a call to
 * {@code builder.clause(clause1).clause(clause2)} will fail since there was no operator placed between the
 * clauses.
 * <p/>
 * The builder can be told to inject automatically either an "AND" ({@link #defaultAnd()}) or an "OR" ({@link #defaultOr()})
 * between clauses when no operator have been specified. For example, a call to {@code builder.defaultAnd().clause(clause1).clause(clause2).build()}
 * will actually generate the JQL {@code clause1 AND clause2}. The affect of calling either {@code defaultAnd()} or {@code defaultOr()} will
 * remain in place on the builder until one of {@link #defaultNone()}, {@code defaultAnd()}, {@code defaultOr()} or {@link #clear()} is called.
 * <p/>
 * The builder may handle the precedence in JQL in different ways. For instance, {@code
 * builder.clause(clause1).or().clause(clause2).and().clause(clause3).build()} could create a JQL expression
 * <tt>(clause1 or clause2) and clause3</tt> if JQL precedence is ignored or <tt>clause1 or (clause2 and clause3)</tt>
 * if it takes precedence into account. How precedence is handled is left as an implementation detail.
 *
 * @since v4.0
 */
interface SimpleClauseBuilder
{
    /**
     * Reset the builder to its empty initial state. 
     *
     * @return the reset builder.
     */
    SimpleClauseBuilder clear();

    /**
     * Add a new logical AND operator to the JQL expression being built.
     *
     * @return a builder that can be used to further extends the current JQL expression.
     * @throws IllegalStateException if it is not possible to add the AND operator given the current state of the
     * builder.
     */
    SimpleClauseBuilder and();

    /**
     * Add a new logical OR operator to the JQL expression being built.
     *
     * @return a builder that can be used to further extends the current JQL expression.
     * @throws IllegalStateException if it is not possible to add the OR operator given the current state of the
     * builder.
     */
    SimpleClauseBuilder or();

    /**
     * Add a new logical NOT operator to JQL expression being built.
     *
     * @return a builder that can be used to further extends the current JQL expression.
     * @throws IllegalStateException if it is not possible to add the NOT operator given the current state of the
     * builder.
     */
    SimpleClauseBuilder not();

    /**
     * Add the passed clause to the JQL expression being built.
     *
     * @param clause the clause to add to the current JQL expression.
     * @return a builder that can be used to further extends the current JQL expression.
     * @throws IllegalStateException if it is not possible to add the clause given the current state of the builder.
     */
    SimpleClauseBuilder clause(Clause clause);

    /**
     * Start a new sub-expression in the JQL expression being built. This can be used to override any precendece rules
     * implemented in the builder.
     *
     * @return a builder that can be used to further extends the current JQL expression.
     * @throws IllegalStateException if it is not possible to create a sub-expression given the state of the builder.
     */
    SimpleClauseBuilder sub();

    /**
     * End the current sub-expression in the JQL expression being built.
     *
     * @return a builder that can be used to further extends the current JQL expression.
     * @throws IllegalStateException if it is not possible to end the current sub-expression given the state of the
     * builder.
     */
    SimpleClauseBuilder endsub();

    /**
     * Create a new {@link Clause} for the JQL the builder has been constructing. A <code>null</code> value may be
     * returned to indicate that there is no condition is generate.
     * <p/>
     * A call to build is non destructive and the builder may continue to be used after it is called.
     *
     * @return the generated clause or <code>null</code> if there was not clause to generated.
     * @throws IllegalStateException if the builder is an incorrect state to create a clause.
     */
    Clause build();

    /**
     * Create a copy of this builder.
     *
     * @return a new copy of this builder.
     */
    SimpleClauseBuilder copy();

    /**
     * Tell the builder to combine clauses using the "AND" JQL condition when none has been specified. Normally the
     * caller must ensure that a call to either {@link #and()} or {@link #or()} is placed between calls to {@link
     * #clause(com.atlassian.query.clause.Clause)} to ensure that valid JQL is built (and that no {@link
     * IllegalStateException} is thrown). Calling this method on the builder tells it to automatically add a JQL "AND"
     * between JQL clauses when no calls to either {@code and} or {@code or} have been made. This mode will remain
     * active until one of {@link #defaultNone()}, {@code defaultOr()} or {@link #clear()} is called.
     *
     * @return a builder that can be used to further extends the current JQL expression.
     */
    SimpleClauseBuilder defaultAnd();

    /**
     * Tell the builder to combine clauses using the "OR" JQL condition when none has been specified. Normally the
     * caller must ensure that a call to either {@link #and()} or {@link #or()} is placed between calls to {@link
     * #clause(com.atlassian.query.clause.Clause)} to ensure that valid JQL is built (and that no {@link
     * IllegalStateException} is thrown). Calling this method on the builder tells it to automatically add a JQL "OR"
     * between JQL clauses when no calls to either {@code and} or {@code or} have been made. This mode will remain
     * active until one of {@link #defaultNone()}, {@code defaultAnd()} or {@link #clear()} is called.
     *
     * @return a builder that can be used to further extends the current JQL expression.
     */
    SimpleClauseBuilder defaultOr();

    /**
     * Tell the builder to stop injecting JQL "AND" or "OR" operators automatically between calls to {@link
     * #clause(com.atlassian.query.clause.Clause)}. This essentially turns off the behaviour started by calling either
     * {@link #defaultAnd()} or {@link #defaultOr()}.
     *
     * @return a builder that can be used to further extends the current JQL expression.
     */
    SimpleClauseBuilder defaultNone();
}
