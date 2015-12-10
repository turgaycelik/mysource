package com.atlassian.jira.jql.builder;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation of {@link SimpleClauseBuilder} that takes JQL prededence into account when building its associated
 * JQL {@link com.atlassian.query.clause.Clause}. For exmaple, the expression {@code
 * builder.clause(clause1).or().clause(clause2).and().clause(clause3).build()} will return the Clause representation of
 * <code>clause1 OR (clause2 AND clause3)</code>.
 *
 * @since v4.0
 */
@NotThreadSafe
class PrecedenceSimpleClauseBuilder implements SimpleClauseBuilder
{
    private final Stacks stacks;
    private BuilderState builderState;
    private BuilderOperator defaultOperator;

    PrecedenceSimpleClauseBuilder()
    {
        this.stacks = new Stacks();
        this.builderState = StartState.INSTANCE.enter(stacks);
        this.defaultOperator = null;
    }

    PrecedenceSimpleClauseBuilder(final PrecedenceSimpleClauseBuilder copy)
    {
        this.builderState = IllegalState.INSTANCE;
        this.stacks = new Stacks(copy.stacks);
        this.builderState = copy.builderState.copy(this.stacks);
        this.defaultOperator = copy.defaultOperator;
    }

    public SimpleClauseBuilder copy()
    {
        return new PrecedenceSimpleClauseBuilder(this);
    }

    public SimpleClauseBuilder defaultAnd()
    {
        defaultOperator = BuilderOperator.AND;
        return this;
    }

    public SimpleClauseBuilder defaultOr()
    {
        defaultOperator = BuilderOperator.OR;
        return this;
    }

    public SimpleClauseBuilder defaultNone()
    {
        defaultOperator = null;
        return this;
    }

    public SimpleClauseBuilder clear()
    {
        defaultOperator = null;
        stacks.clear();
        builderState = StartState.INSTANCE.enter(stacks);

        return this;
    }

    public SimpleClauseBuilder and()
    {
        builderState = builderState.and(stacks).enter(stacks);
        return this;
    }

    public SimpleClauseBuilder or()
    {
        builderState = builderState.or(stacks).enter(stacks);
        return this;
    }

    public SimpleClauseBuilder not()
    {
        builderState = builderState.not(stacks, defaultOperator).enter(stacks);
        return this;
    }

    public PrecedenceSimpleClauseBuilder clause(final Clause clause)
    {
        notNull("clause", clause);
        builderState = builderState.add(stacks, new SingleMutableClause(clause), defaultOperator).enter(stacks);
        return this;
    }

    public SimpleClauseBuilder sub()
    {
        builderState = builderState.group(stacks, defaultOperator).enter(stacks);
        return this;
    }

    public SimpleClauseBuilder endsub()
    {
        builderState = builderState.endgroup(stacks).enter(stacks);
        return this;
    }

    public Clause build()
    {
        return builderState.build(stacks);
    }

    @Override
    public String toString()
    {
        return stacks.getDisplayString();
    }

    /**
     * Stack used to help with building the JQL clause using operator precedence. There are two stacks here, an operator
     * stack and an operand stack.
     * <p/>
     * It implements the <a href="http://en.wikipedia.org/wiki/Shunting_yard_algorithm">Shunting Yard Algorithm</a> to
     * implement operator precedence.
     *
     * @since v4.0
     */
    private static final class Stacks
    {
        private final List<BuilderOperator> operators;
        private final List<MutableClause> operands;
        private int level = 0;

        Stacks()
        {
            operators = new LinkedList<BuilderOperator>();
            operands = new LinkedList<MutableClause>();
        }

        /**
         * Make a safe deep copy of the passed state. The new state should be independent of the passed state.
         *
         * @param state the state to copy.
         */
        Stacks(Stacks state)
        {
            operators = new LinkedList<BuilderOperator>(state.operators);
            operands = new LinkedList<MutableClause>();
            level = state.level;
            for (final MutableClause operand : state.operands)
            {
                operands.add(operand.copy());
            }
        }

        void clear()
        {
            operands.clear();
            operators.clear();
            level = 0;
        }

        int getLevel()
        {
            return level;
        }

        BuilderOperator popOperator()
        {
            final BuilderOperator builderOperator = operators.remove(0);
            if (builderOperator == BuilderOperator.LPAREN)
            {
                level = level - 1;
            }
            return builderOperator;
        }

        BuilderOperator peekOperator()
        {
            return operators.get(0);
        }

        boolean hasOperator()
        {
            return !operators.isEmpty();
        }

        void pushOperand(MutableClause operand)
        {
            operands.add(0, operand);
        }

        MutableClause popClause()
        {
            return operands.remove(0);
        }

        MutableClause peekClause()
        {
            return operands.get(0);
        }

        /**
         * Process the current operators on the operator stack using the Shunting Yard Algorithm and then push the
         * passed operator onto the operator stack. Passing null to this method will leave the complete MutableClause as
         * the only argument on the operand stack.
         *
         * @param operator the operator to add to the stack. A <code>null</code> argument means that the operator stack
         * should be emptied.
         */
        void processAndPush(BuilderOperator operator)
        {
            if (operator != BuilderOperator.LPAREN && hasOperator())
            {
                BuilderOperator currentTop = peekOperator();

                //The NOT operator is special since it a right-associative unary operator. For right-associative
                //operators only process when something of higher but not equal precedence appears on the stack.
                final int compare = operator == BuilderOperator.NOT ? -1 : 0;

                while (currentTop != null && (operator == null ||
                        operator.compareTo(currentTop) <= compare))
                {
                    popOperator();

                    //Get the operands for the operator we are about to process.
                    MutableClause leftOperand;
                    MutableClause rightOperand;
                    if (currentTop == BuilderOperator.NOT)
                    {
                        leftOperand = popClause();
                        rightOperand = null;
                    }
                    else
                    {
                        rightOperand = popClause();
                        leftOperand = popClause();
                    }

                    //Execute the operator and add the result to the top of the stack as the argument for the next
                    //operator.
                    pushOperand(leftOperand.combine(currentTop, rightOperand));

                    if (hasOperator())
                    {
                        currentTop = peekOperator();
                    }
                    else
                    {
                        currentTop = null;
                    }
                }
            }
            if (operator != null)
            {
                if (operator == BuilderOperator.RPAREN)
                {
                    if (hasOperator() && peekOperator() == BuilderOperator.LPAREN)
                    {
                        popOperator();
                    }
                    else
                    {
                        throw new IllegalStateException("The ')' does not have a matching '('.");
                    }
                }
                else
                {
                    if (operator == BuilderOperator.LPAREN)
                    {
                        level = level + 1;
                    }
                    operators.add(0, operator);
                }
            }
        }

        /**
         * Get the, possibly partial, JQL expression for the current stack state. Used mainly for error messages.
         *
         * @return the current JQL expression given the state of the stacks.
         */
        String getDisplayString()
        {
            final Iterator<BuilderOperator> operatorIterator = new InfiniteReversedIterator<BuilderOperator>(operators.listIterator(operators.size()));
            final Iterator<MutableClause> clauseIterator = new InfiniteReversedIterator<MutableClause>(operands.listIterator(operands.size()));
            final StringBuilder stringBuilder = new StringBuilder();

            boolean start = true;
            BuilderOperator operator = operatorIterator.next();
            while (operator != null)
            {
                switch (operator)
                {
                    case LPAREN:
                        addString(stringBuilder, "(");
                        operator = operatorIterator.next();
                        start = true;
                        break;
                    case RPAREN:
                        //just append these where seen.
                        stringBuilder.append(")");
                        operator = operatorIterator.next();
                        start = false;
                        break;
                    case AND:
                    case OR:
                        //do we need to add the starting operand for this operator. We only do this for new expressions.
                        if (start)
                        {
                            addString(stringBuilder, clauseToString(clauseIterator.next(), operator));
                        }

                        //Fall through here on purpose.
                    case NOT:
                        addString(stringBuilder, operator.toString());

                        final BuilderOperator nextOperator = operatorIterator.next();
                        //We don't want to print the next operand yet for these two operators.
                        if (nextOperator != BuilderOperator.LPAREN && nextOperator != BuilderOperator.NOT)
                        {
                            addString(stringBuilder, clauseToString(clauseIterator.next(), operator));
                        }
                        start = false;
                        operator = nextOperator;
                        break;
                }
            }

            //Loop through any remaining operands and add them. There should only ever be one.
            MutableClause clause = clauseIterator.next();
            while (clause != null)
            {
                addString(stringBuilder, clauseToString(clause, null));
                clause = clauseIterator.next();
            }

            return stringBuilder.toString();
        }

        private static void addString(StringBuilder builder, String append)
        {
            if (append.length() == 0)
            {
                return;
            }

            final int length = builder.length();
            if (length != 0 && builder.charAt(length - 1) != '(')
            {
                builder.append(" ");
            }

            builder.append(append);
        }

        /**
         * Return a string representation of the passed clause.
         *
         * @param clause the clause to stringify.
         * @param operator the operator that this clause belongs to, that is, this clause is an operand of this
         * operator.
         * @return the string version of the passed clause.
         */
        private static String clauseToString(MutableClause clause, BuilderOperator operator)
        {
            if (clause == null)
            {
                return "";
            }
            final Clause jqlClause = clause.asClause();
            if (jqlClause == null)
            {
                return "";
            }

            //We need to bracket the clause if its primary operator has lower precedence than the passed
            //operator.
            BuilderOperator clauseOperator = OperatorVisitor.findOperator(jqlClause);
            if (operator != null && clauseOperator != null && operator.compareTo(clauseOperator) > 0)
            {
                return "(" + jqlClause.toString() + ")";
            }
            else
            {
                return jqlClause.toString();
            }
        }

        Clause asClause()
        {
            return asMutableClause().asClause();
        }

        MutableClause asMutableClause()
        {
            processAndPush(null);
            return peekClause();
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    /**
     * Represents the state of the builder. The PrecedenceBasicBuilder will delegate its operations to its current state
     * to perform.
     *
     * @since v4.0.
     */
    private interface BuilderState
    {
        BuilderState enter(Stacks stacks);

        BuilderState not(Stacks stacks, BuilderOperator defaultOperator);

        BuilderState and(Stacks stacks);

        BuilderState or(Stacks stacks);

        BuilderState add(Stacks stacks, MutableClause clause, BuilderOperator defaultOperator);

        BuilderState group(Stacks stacks, BuilderOperator defaultOperator);

        BuilderState endgroup(Stacks stacks);

        Clause build(Stacks stacks);

        BuilderState copy(Stacks stacks);
    }

    /**
     * This is the initial state for the builder. In this state the builder expects a clause, sub-clause or a NOT clause
     * to be added. It is also possible to build in this state, however, the builder will return <code>null</code> as no
     * condition has can be generated.
     *
     * @since v4.0.
     */
    private static class StartState implements BuilderState
    {
        static final StartState INSTANCE = new StartState();

        private StartState()
        {
        }

        public BuilderState enter(final Stacks stacks)
        {
            return this;
        }

        /**
         * When NOT is called we transition to the {@link PrecedenceSimpleClauseBuilder.NotState} expecting a NOT clause
         * to be added.
         *
         * @param stacks current stacks for the builder.
         * @param defaultOperator the default combining operator.
         * @return the next builder state.
         */
        public BuilderState not(Stacks stacks, final BuilderOperator defaultOperator)
        {
            return NotState.INSTANCE;
        }

        public BuilderState and(Stacks stacks)
        {
            return this;
        }

        public BuilderState or(Stacks stacks)
        {
            return this;
        }

        /**
         * When a clause is added an AND or OR operator is expected next so we transition into the {@link
         * OperatorState}.
         *
         * @param stacks current stacks for the builder.
         * @param clause the clause to add to tbe builder.
         * @param defaultOperator the default combining operator.
         * @return the next builder state.
         */
        public BuilderState add(Stacks stacks, final MutableClause clause, final BuilderOperator defaultOperator)
        {
            stacks.pushOperand(clause);
            return OperatorState.INSTANCE;
        }

        /**
         * We now have a sub-expression so lets transition into that state.
         *
         * @param stacks current stacks for the builder.
         * @param defaultOperator the default combining operator.
         * @return the next builder state.
         */
        public BuilderState group(Stacks stacks, final BuilderOperator defaultOperator)
        {
            return StartGroup.INSTANCE;
        }

        public BuilderState endgroup(Stacks stacks)
        {
            throw new IllegalStateException("Tying to start JQL expression with ')'.");
        }

        public Clause build(Stacks stacks)
        {
            return null;
        }

        public BuilderState copy(Stacks stacks)
        {
            return this;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    /**
     * This is the state of the builder when the user is trying to enter in a negated clause. In this state it is only
     * possible to add a clause, start a sub-expression or negate again.
     *
     * @since v4.0.
     */
    private static class NotState implements BuilderState
    {
        static final NotState INSTANCE = new NotState();

        private NotState()
        {
        }

        /**
         * Upon entry into this state, we always add a not operator.
         *
         * @param stacks current stacks for the builder.
         * @return the next builder state.
         */
        public BuilderState enter(final Stacks stacks)
        {
            stacks.processAndPush(BuilderOperator.NOT);
            return this;
        }

        /**
         * We are negating the clause again, so re-enter our state.
         *
         * @param stacks current stacks for the builder.
         * @param defaultOperator the default combining operator.
         * @return the next builder state.
         */
        public BuilderState not(Stacks stacks, final BuilderOperator defaultOperator)
        {
            return this;
        }

        public BuilderState and(Stacks stacks)
        {
            throw new IllegalStateException("Trying to create the illegal JQL expression 'NOT AND'. The current JQL is '" + stacks.getDisplayString() + "'.");
        }

        public BuilderState or(Stacks stacks)
        {
            throw new IllegalStateException("Trying to create the illegal JQL expression 'NOT OR'. The current JQL is '" + stacks.getDisplayString() + "'.");
        }

        /**
         * Add the clause to the operand stack so that it may be negated.
         *
         * @param stacks current stacks for the builder.
         * @param clause the clause to add to the query.
         * @param defaultOperator the default combining operator.
         * @return the next builder state.
         */
        public BuilderState add(Stacks stacks, final MutableClause clause, final BuilderOperator defaultOperator)
        {
            stacks.pushOperand(clause);
            return OperatorState.INSTANCE;
        }

        public BuilderState group(Stacks stacks, final BuilderOperator defaultOperator)
        {
            return StartGroup.INSTANCE;
        }

        public BuilderState endgroup(Stacks stacks)
        {
            throw new IllegalStateException("Tying to end JQL sub-expression without completing 'NOT' operator. The current JQL is '" + stacks.getDisplayString() + "'.");
        }

        public Clause build(Stacks stacks)
        {
            throw new IllegalStateException("Trying to end JQL expression with the 'NOT' operator. The current JQL is '" + stacks.getDisplayString() + "'.");
        }

        public BuilderState copy(Stacks stacks)
        {
            return this;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    /**
     * This is the state of the builder once the user has enetered in a valid JQL clause. It is possible to:
     * <ul>
     * <li>Add a 'AND' operator.</li>
     * <li>Add a 'OR' operator.</li>
     * <li>Start a sub-expression.</li>
     * <li>End a sub-expression.</li>
     * <li>Build the JQL</li>
     * <li>Add a clause if there is a default operator</li>
     * </ul>
     *
     */
    private static class OperatorState implements BuilderState
    {
        static final OperatorState INSTANCE = new OperatorState();

        private OperatorState()
        {

        }

        public BuilderState enter(final Stacks stacks)
        {
            return this;
        }

        public BuilderState not(Stacks stacks, final BuilderOperator defaultOperator)
        {
            if (defaultOperator == null)
            {
                throw new IllegalStateException("Trying to combine JQL expressions using the 'NOT' operator. The current JQL is '" + stacks.getDisplayString() + "'.");
            }
            else
            {
                stacks.processAndPush(defaultOperator);
                return NotState.INSTANCE;
            }
        }

        /**
         * We now know that the next logical operator is going to be an AND. After this call completes, the user must
         * enter either a clause, sub-clause or NOT clause so we transition into the {@link
         * PrecedenceSimpleClauseBuilder.ClauseState}.
         *
         * @param stacks current stacks for the builder.
         * @return the next builder state.
         */
        public BuilderState and(Stacks stacks)
        {
            stacks.processAndPush(BuilderOperator.AND);
            return new ClauseState(BuilderOperator.AND);
        }

        /**
         * We now know that the next logical operator is going to be an OR. After this call completes, the user must
         * enter either a clause, sub-clause or NOT clause so we transition into the {@link
         * PrecedenceSimpleClauseBuilder.ClauseState}.
         *
         * @param stacks current stacks for the builder.
         * @return the next builder state.
         */
        public BuilderState or(Stacks stacks)
        {
            stacks.processAndPush(BuilderOperator.OR);
            return new ClauseState(BuilderOperator.OR);
        }

        /**
         * In this state we are able to add a clause only if a default operator has been specified.
         *
         * @param clause the clause to add to the builder.
         * @param defaultOperator the default combining operator.
         * @return the next state for the builder.
         */
        public BuilderState add(final Stacks stacks, final MutableClause clause, final BuilderOperator defaultOperator)
        {
            if (defaultOperator == null)
            {
                throw new IllegalStateException("Trying to combine JQL expressions without logical operator. The current JQL is '" + stacks.getDisplayString() + "'.");
            }

            stacks.processAndPush(defaultOperator);
            stacks.pushOperand(clause);
            return this;
        }

        public BuilderState group(Stacks stacks, final BuilderOperator defaultOperator)
        {
            if (defaultOperator == null)
            {
                throw new IllegalStateException("Trying to combine JQL expressions without logical operator. The current JQL is '" + stacks.getDisplayString() + "'.");
            }
            else
            {
                stacks.processAndPush(defaultOperator);
                return StartGroup.INSTANCE;
            }
        }

        /**
         * We are now going to try to end the current sub-expression. After this we transition back into this state
         * awaing the next clause.
         *
         * @param stacks current stacks for the builder.
         * @return the next builder state.
         */
        public BuilderState endgroup(Stacks stacks)
        {
            //If this is true, then there is no current sub-expression.
            if (stacks.getLevel() == 0)
            {
                throw new IllegalStateException("Tyring end JQL sub-expression that does not exist. The current JQL is '" + stacks.getDisplayString() + "'.");
            }
            stacks.processAndPush(BuilderOperator.RPAREN);

            return this;
        }

        /**
         * Try to build the JQL given the current state of the builder.
         *
         * @param stacks current stacks for the builder.
         * @return the next builder state.
         */
        public Clause build(Stacks stacks)
        {
            //If this is true, then there are unfinished sub-expressions.
            if (stacks.getLevel() > 0)
            {
                throw new IllegalStateException("Tyring to build JQL expression that has an incomplete sub-expression. The current JQL is '" + stacks.getDisplayString() + "'.");
            }

            //we must take a copy of the stack to ensure that build does not destruct the builder.
            final Stacks localStacks = new Stacks(stacks);
            return localStacks.asClause();
        }

        public BuilderState copy(Stacks stacks)
        {
            return this;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    /**
     * This is the state of the builder when it is expecting a clause, sub-clause or a NOT clause. This is slightly
     * different from the {@link PrecedenceSimpleClauseBuilder.StartState} as building is illegal and that this must
     * happen after the bulider was in the {@link PrecedenceSimpleClauseBuilder.OperatorState}.
     */
    private static class ClauseState implements BuilderState
    {
        private final BuilderOperator lastOperator;

        public ClauseState(final BuilderOperator lastOperator)
        {
            this.lastOperator = lastOperator;
        }

        public BuilderState enter(final Stacks stacks)
        {
            return this;
        }

        /**
         * When NOT is called we transition to the {@link PrecedenceSimpleClauseBuilder.NotState} expecting a NOT clause
         * to be added.
         *
         * @param stacks current stacks for the builder.
         * @param defaultOperator the default combining operator.
         * @return the next builder state.
         */
        public BuilderState not(Stacks stacks, final BuilderOperator defaultOperator)
        {
            return NotState.INSTANCE;
        }

        public BuilderState and(Stacks stacks)
        {
            throw new IllegalStateException(String.format("Trying to create illegal JQL expression '%s %s'. Current JQL is '%s'.",
                    lastOperator, BuilderOperator.AND, stacks.getDisplayString()));
        }

        public BuilderState or(Stacks stacks)
        {
            throw new IllegalStateException(String.format("Trying to create illegal JQL expression '%s %s'. Current JQL is '%s'.", lastOperator,
                    BuilderOperator.OR, stacks.getDisplayString()));
        }

        /**
         * When a clause is added an AND or OR operator is expected next so we transition into the {@link
         * OperatorState}.
         *
         * @param clause the clause to add to the builder.
         * @param defaultOperator the default combining operator.
         * @return the next state for the builder.
         */
        public BuilderState add(Stacks stacks, final MutableClause clause, final BuilderOperator defaultOperator)
        {
            stacks.pushOperand(clause);
            return OperatorState.INSTANCE;
        }

        /**
         * We now expect a sub-clause, so lets transitiion into that state.
         *
         * @param stacks current stacks for the builder.
         * @param defaultOperator the default combining operator.
         * @return the next builder state.
         */
        public BuilderState group(Stacks stacks, final BuilderOperator defaultOperator)
        {
            return StartGroup.INSTANCE;
        }

        public BuilderState endgroup(Stacks stacks)
        {
            throw new IllegalStateException(String.format("Trying to create illegal JQL expression '%s %s'. Current JQL is '%s'.",
                    lastOperator, BuilderOperator.RPAREN, stacks.getDisplayString()));
        }

        public Clause build(Stacks stacks)
        {
            throw new IllegalStateException(String.format("Trying end the JQL expression with operator '%s'. Current JQL is '%s'.",
                    lastOperator, stacks.getDisplayString()));
        }

        public BuilderState copy(Stacks stacks)
        {
            return this;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    /**
     * This is the state for the builder when the user is expected to enter a sub-expression. From this state it is
     * possible to:
     * <p/>
     * <ul> <li> Start a new sub-expression. <li> Add a new clause to the sub-expression. <li> Add a new NOT to the
     * sub-expression. <ul>
     */
    private static class StartGroup implements BuilderState
    {
        final static StartGroup INSTANCE = new StartGroup();

        private StartGroup()
        {
        }

        /**
         * Upon entry into this state we always start a new sub-expression.
         *
         * @param stacks current stacks for the builder.
         * @return the next builder state.
         */
        public BuilderState enter(final Stacks stacks)
        {
            stacks.processAndPush(BuilderOperator.LPAREN);
            return this;
        }

        /**
         * Starting a NOT clause will put us in the {@link PrecedenceSimpleClauseBuilder.NotState}.
         *
         * @param stacks current stacks for the builder.
         * @param defaultOperator the default combining operator.
         * @return the next builder state.
         */
        public BuilderState not(Stacks stacks, final BuilderOperator defaultOperator)
        {
            return NotState.INSTANCE;
        }

        public BuilderState and(Stacks stacks)
        {
            throw new IllegalStateException("Trying to start sub-expression with 'AND'. Current JQL is '" + stacks.getDisplayString() + "'.");
        }

        public BuilderState or(Stacks stacks)
        {
            throw new IllegalStateException("Trying to start sub-expression with 'OR'. Current JQL is '" + stacks.getDisplayString() + "'.");
        }

        /**
         * When a clause is added an AND or OR operator is expected next so we transition into the {@link
         * OperatorState}.
         *
         * @param stacks current stacks for the builder.
         * @param clause the clause to add to tbe builder.
         * @param defaultOperator the default combining operator.
         * @return the next builder state.
         */
        public BuilderState add(Stacks stacks, final MutableClause clause, final BuilderOperator defaultOperator)
        {
            stacks.pushOperand(clause);
            return OperatorState.INSTANCE;
        }

        /**
         * Starting a new group will renter this state.
         *
         * @param stacks current stacks for the builder.
         * @param defaultOperator the default combining operator.
         * @return the next builder state.
         */
        public BuilderState group(Stacks stacks, final BuilderOperator defaultOperator)
        {
            return this;
        }

        public BuilderState endgroup(Stacks stacks)
        {
            throw new IllegalStateException("Trying to create empty sub-expression. Current JQL is '" + stacks.getDisplayString() + "'.");
        }

        public Clause build(Stacks stacks)
        {
            throw new IllegalStateException("Tyring to build JQL expression that has an incomplete sub-expression. The current JQL is '" + stacks.getDisplayString() + "'.");
        }

        public BuilderState copy(Stacks stacks)
        {
            return this;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    ///CLOVER:OFF

    /**
     * This is a guard state that can be used to stop calls to the builder when it is being initialised.
     *
     * @since v4.0
     */
    private static class IllegalState implements BuilderState
    {
        private static final IllegalState INSTANCE = new IllegalState();

        public BuilderState enter(final Stacks stacks)
        {
            return this;
        }

        public BuilderState not(Stacks stacks, final BuilderOperator defaultOperator)
        {
            throw new IllegalStateException("Trying to access builder in illegal state.");
        }

        public BuilderState and(Stacks stacks)
        {
            throw new IllegalStateException("Trying to add 'AND' to builder before it has been initialised.");
        }

        public BuilderState or(Stacks stacks)
        {
            throw new IllegalStateException("Trying to add 'OR' to builder before it has been initialised.");
        }

        public BuilderState add(Stacks stacks, final MutableClause clause, final BuilderOperator defaultOperator)
        {
            throw new IllegalStateException("Trying to add clause to builder before it has been initialised.");
        }

        public BuilderState group(Stacks stacks, final BuilderOperator defaultOperator)
        {
            throw new IllegalStateException("Trying to start sub-clause in a builder that has not been initialised.");
        }

        public BuilderState endgroup(Stacks stacks)
        {
            throw new IllegalStateException("Trying to end sub-clause in a builder that has not been initialised.");
        }

        public Clause build(Stacks stacks)
        {
            throw new IllegalStateException("Trying to call build before the builder is initialised.");
        }

        public BuilderState copy(Stacks stacks)
        {
            throw new IllegalStateException("Trying to copy a builder that has not been initialised.");
        }

        @Override
        public String toString()
        {
            return "Illegal State";
        }
    }

    ///CLOVER:ON

    /**
     * An iterator that does decorates a {@link java.util.ListIterator} such that it appears reversed and infinite. The
     * iterator will return all the values of the wrapped iterator up util it ends. Once it ends this iterator will
     * continue to return null.
     */
    private static class InfiniteReversedIterator<T> implements Iterator<T>
    {
        private ListIterator<T> delegate;

        public InfiniteReversedIterator(final ListIterator<T> delegate)
        {
            this.delegate = delegate;
        }

        public boolean hasNext()
        {
            return true;
        }

        public T next()
        {
            if (delegate.hasPrevious())
            {
                return delegate.previous();
            }
            else
            {
                return null;
            }
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Simple visitor that will returns the operator associated with the root node of the passed clause. May return null
     * when the root operator has no operator.
     */
    private static class OperatorVisitor implements ClauseVisitor<BuilderOperator>
    {
        static BuilderOperator findOperator(Clause clause)
        {
            OperatorVisitor visitor = new OperatorVisitor();
            return clause.accept(visitor);
        }

        public BuilderOperator visit(final AndClause andClause)
        {
            return BuilderOperator.AND;
        }

        public BuilderOperator visit(final NotClause notClause)
        {
            return BuilderOperator.NOT;
        }

        public BuilderOperator visit(final OrClause orClause)
        {
            return BuilderOperator.OR;
        }

        public BuilderOperator visit(final TerminalClause clause)
        {
            return null;
        }

        @Override
        public BuilderOperator visit(WasClause clause)
        {
            return null;
        }

        @Override
        public BuilderOperator visit(ChangedClause clause)
        {
            return null;
        }
    }
}
