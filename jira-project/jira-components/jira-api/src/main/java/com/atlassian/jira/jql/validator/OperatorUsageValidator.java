package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;

/**
 * Performs global validation about where it is appropriate to use an {@link com.atlassian.query.operator.Operator}.
 *
 * This does checks to see that list operators are not used with non-lists and vice-versa.
 *
 * @since v4.0
 */
public interface OperatorUsageValidator
{
    /**
     * Validate the usage of the Operator and Operand that are held in the clause. The clause specific validation,
     * as to whether the clause has any specific issues with the configuration occurs elsewhere. This is just
     * performing global Operator/Operand checks.
     *
     * @param searcher the user performing the validation, used to get i18n information.
     * @param clause the clause that contains the Operator and Operand.
     *
     * @return a MessageSet that will contain any errors or warnings that may have been generated from the validation. 
     */
    MessageSet validate(User searcher, TerminalClause clause);

    /**
     * Check the usage of the Operator and Operand that are held in the clause. The clause specific validation,
     * as to whether the clause has any specific issues with the configuration occurs elsewhere. This is just
     * performing global Operator/Operand checks.
     *
     * @param searcher the user performing the validation, used to get i18n information.
     * @param clause the clause that contains the Operator and Operand.
     *
     * @return true if the passed clause is valid, false otherwise.
     */
    boolean check(User searcher, TerminalClause clause);
}
