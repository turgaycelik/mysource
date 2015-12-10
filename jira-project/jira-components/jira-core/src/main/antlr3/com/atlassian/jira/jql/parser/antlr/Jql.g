/**
 * This is the ANTLRv3 grammar for JQL. The lexer (JqlLexer) and parser (JqlParser) can be generated from
 * this file by running mvn generate-sources
 *
 * This grammar uses JqlStringSupportImpl.isReservedString to determine whether or not a string is reserved.
 * We tried doing this in the grammar by listing all the reserved words as tokens but ANTLR did not 
 * react very well to this (it generated a very large and slow Lexer). Thus changes to JqlStringSupportImpl.isReservedString
 * will change what strings this grammar will parse.
 * 
 * NOTE: Making changes to the grammar is likely to affect JqlStringSupportImpl which makes assumptions
 * about the structure of this file.
 */

grammar Jql;
options {language=Java;}
@parser::header {
package com.atlassian.jira.jql.parser.antlr;

import java.util.Collections;
import com.atlassian.query.operand.*;
import com.atlassian.query.operator.*;
import com.atlassian.query.clause.*;
import com.atlassian.query.order.*;
import com.atlassian.query.history.*;
import com.atlassian.jira.jql.util.FieldReference;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.jql.parser.JqlParseErrorMessage;
import com.atlassian.jira.jql.parser.JqlParseErrorMessages;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.validator.EntityPropertyClauseValidator;
import com.atlassian.fugue.Option;
import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;
}

/* START HACK
 * The following code will basically tell ANTLR to freak out immediately on error rather than trying to recover. 
 * This seems to be a hack as the ANTLR runtime seems to change quite frequently between releases. For instance, 
 * the ANTLRv3 book tells us to overwrite the wrong methods.
 */
@members {
private static final Logger log = Logger.getLogger(JqlParser.class);

private int operandLevel = 0;
private boolean supportsHistoryPredicate = false;

@Override
protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException
{
	throw new MismatchedTokenException(ttype, input);
} 

@Override
/** This method does not appear to be used, but better safe the sorry. **/
public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException 
{ 
	throw e;
}

@Override
/** Override this method to change where error messages go */
public void emitErrorMessage(String msg) 
{
	log.warn(msg);
}

/**
 * Make sure that the passed token can be turned into a long. In ANTLR there
 * does not appear to be an easy way to limit numbers to a valid Long range, so
 * lets do so in Java.
 *
 * @param token the token to turn into a long.
 * @return the valid long.
 */
private long parseLong(Token token)
{
    final String text = token.getText();
    try
    {
        return Long.parseLong(text);
    }
    catch (NumberFormatException e)
    {
        JqlParseErrorMessage message = JqlParseErrorMessages.illegalNumber(text, token.getLine(), token.getCharPositionInLine());
        throw new RuntimeRecognitionException(message, e);
    }
}

private String checkFieldName(Token token)
{
    final String text = token.getText();
    if (StringUtils.isBlank(text))
    {
        reportError(JqlParseErrorMessages.emptyFieldName(token.getLine(), token.getCharPositionInLine()), null);
    }
    return text;
}

private String checkFunctionName(Token token)
{
    final String text = token.getText();
    if (StringUtils.isBlank(text))
    {
        reportError(JqlParseErrorMessages.emptyFunctionName(token.getLine(), token.getCharPositionInLine()), null);
    }
    return text;
}

private void reportError(JqlParseErrorMessage message, Throwable th) throws RuntimeRecognitionException
{
    throw new RuntimeRecognitionException(message, th);
}
}
@rulecatch {
catch (RecognitionException e) 
{
	throw e;
}
}

/*
 * END HACK
 */

@lexer::header {
package com.atlassian.jira.jql.parser.antlr;

import org.apache.log4j.Logger;
import com.atlassian.jira.jql.util.JqlStringSupportImpl;
import com.atlassian.jira.jql.parser.JqlParseErrorMessage;
import com.atlassian.jira.jql.parser.JqlParseErrorMessages;
import java.util.List;
import java.util.LinkedList;
}

@lexer::members {
private static final Logger log = Logger.getLogger(JqlLexer.class);

private final List<AntlrPosition> stack = new LinkedList<AntlrPosition>();

private void stripAndSet()
{
	String text = getText();
	text = text.substring(1, text.length() - 1);
	text = JqlStringSupportImpl.decode(text);
	setText(text);
}

private void checkAndSet()
{
    final String text = JqlStringSupportImpl.decode(getText());
    if (JqlStringSupportImpl.isReservedString(text))
    {
        JqlParseErrorMessage message = JqlParseErrorMessages.reservedWord(text, state.tokenStartLine, state.tokenStartCharPositionInLine);
        throw new RuntimeRecognitionException(message);
    }
    setText(text);
}

@Override
/** Override this method to change where error messages go */
public void emitErrorMessage(String msg) 
{
	log.debug(msg);
}

/* START HACK 
 * We need to get the "lexer" to fail when it detects errors. At the moment ANTLR just drops the input
 * up until the error and tries again. This can leave antlr actually parsing JQL strings that are not
 * valid. For example, the string "priority = \kbjb" will actually be parsed as "priority = bjb". 
 * To stop this we throw a RuntimeRecognitionRuntimeException which the DefaultJqlParser is careful to catch. 
 * Throwing the RecognitionException  will not work as JqlLexer.nextToken catches this exception and tries 
 * again (which will cause an infinite loop).
 *
 * Antlr (check up to 3.1.3) does not seem to be able to handle the "catch" clause on lexer rules. It throws a RuntimeException
 * when trying to process the grammar. To get around this we have hacked the error reporting using a "stack" to push
 * on the rule we currently have an error in. We can use this information to produce a pretty good error message when
 * the lexer tries to recover though is does make for some pretty strange logic.
 */

@Override
public void recover(RecognitionException re)
{
    LexerErrorHelper handler = new LexerErrorHelper(input, peekPosition());
    handler.handleError(re);
}

private void recover()
{
    MismatchedSetException e = new MismatchedSetException(null, input);
    recover(e);
}

private void pushPosition(int tokenType)
{
    stack.add(0, new AntlrPosition(tokenType, input));
}

private AntlrPosition popPosition()
{
    return stack.isEmpty() ? null : stack.remove(0);
}

private AntlrPosition peekPosition()
{
    return stack.isEmpty() ? null : stack.get(0);
}

/* END HACK */
}

query returns [Clause clause, OrderBy order]
	: (where = clause)?  (sort = orderBy)? EOF
	{
		$clause = $where.clause;
		$order = $sort.order;
	}
	;
	catch [MismatchedTokenException e]
	{
	    if (e.expecting == EOF)
        {
            if (sort != null)
            {
                //If the sort has trailing tokens then "," must be the next token as this is the only way to
                //continue a sort.
                reportError(JqlParseErrorMessages.expectedText(e.token, ","), e);
            }
            else if (where == null)
            {
                //If there is no where clause, then we were not able to find a field name. If we found a field
                //name we would have found a clause and got an error there.
                reportError(JqlParseErrorMessages.badFieldName(e.token), e);
            }
            else
            {
                //If we get a clause but have other stuff after, then the next token must be an "AND" or "OR" as this
                //is the only valid way to combine two clauses.
                reportError(JqlParseErrorMessages.needLogicalOperator(e.token), e);
            }
        }
	    reportError(JqlParseErrorMessages.genericParseError(e.token), e);
	}
	catch [RecognitionException e]
	{
	    reportError(JqlParseErrorMessages.genericParseError(e.token), e);
	}

/*
 * Represents a JQL clause.
 */
clause	returns [Clause clause]
	: orClause { $clause = $orClause.clause; }
	;

/*
 * Represents a set of 1..* JQL clauses combined by an 'OR'. 
 */
orClause returns [Clause clause]
	@init {
		final List<Clause> clauses = new ArrayList<Clause>();
	}
	// a clause is composed of 'AND' clauses since they have higher precedence than the 'OR' composition
	: cl = andClause { clauses.add($cl.clause); } (OR cl = andClause { clauses.add($cl.clause); })* {$clause = clauses.size() == 1? clauses.get(0) : new OrClause(clauses); }
	;

/*
 * Represents a set of 1..* JQL clauses combined by an 'AND'.
 */
andClause returns [Clause clause]
	@init {
		final List<Clause> clauses = new ArrayList<Clause>();
	}
	: cl = notClause { clauses.add($cl.clause); } (AND cl = notClause { clauses.add($cl.clause); })* {$clause = clauses.size() == 1? clauses.get(0) : new AndClause(clauses); }
	;

/*
 * Represents a JQL 'terminalClause', negated 'terminalClause' or bracketed expression.
 */
notClause returns [Clause clause]
	: (NOT | BANG) nc = notClause { $clause = new NotClause($nc.clause); }
	| subClause { $clause = $subClause.clause; }
                      | terminalClause { $clause = $terminalClause.clause; } 
	;
	catch [NoViableAltException e]
	{
	    //If there is no option here then we assume that the user meant a field expression.
	    reportError(JqlParseErrorMessages.badFieldName(e.token), e);
	}


/*
 * Represents a bracketed JQL expression.
 */
subClause returns [Clause clause]
    : LPAREN orClause RPAREN { $clause = $orClause.clause; }
    ;
    catch [MismatchedTokenException e]
    {
        if (e.expecting == RPAREN)
        {
            reportError(JqlParseErrorMessages.expectedText(e.token, ")"), e);
        }
        else
        {
            throw e;
        }
    }

/*
 * Represents a JQL termincalClause or matched brackets.
 */
terminalClause  returns [Clause clause]
	: f = field op = operator  ( (opand = operand pred= historyPredicate?) | chPred=historyPredicate?)
	{
        if (f != null && $f.field.isEntityProperty())
        {
            if ($operand.operand == null)
            {
                final RecognitionException e = new RecognitionException(input);
                reportError(JqlParseErrorMessages.badOperand(e.token), e);
            }
            $clause = new TerminalClauseImpl($f.field.getName(), $operator.operator, $operand.operand, Option.some($f.field.getProperty()));
        }
	    else if ($operator.operator  == Operator.CHANGED)
	    {
            if ($operand.operand != null)
            {
                final RecognitionException e = new RecognitionException(input);
                reportError(JqlParseErrorMessages.unsupportedOperand($operator.operator.toString(), $operand.operand.getDisplayString()),e);
            }
            $clause = new ChangedClauseImpl($field.field.getName(), $operator.operator, $chPred.predicate);
	    }
	    else
	    {
	       if ($operator.operator == Operator.WAS || $operator.operator == Operator.WAS_NOT || $operator.operator == Operator.WAS_IN || $operator.operator == Operator.WAS_NOT_IN )
		   {
		        if ($operand.operand == null)
                {
                    final RecognitionException e = new RecognitionException(input);
                    reportError(JqlParseErrorMessages.badOperand(e.token), e);
                }
                $clause = new WasClauseImpl($field.field.getName(), $operator.operator, $operand.operand, $pred.predicate);
                supportsHistoryPredicate=true;
           }
           else
           {
		        if ($operand.operand == null)
                {
                    final RecognitionException e = new RecognitionException(input);
                    reportError(JqlParseErrorMessages.badOperand(e.token), e);
                }
                $clause = new TerminalClauseImpl($field.field.getName(), $operator.operator, $operand.operand);
                supportsHistoryPredicate=false;
                if ($pred.predicate != null)
                {
                    final JqlParseErrorMessage errorMessage = JqlParseErrorMessages.unsupportedPredicate($pred.predicate.getDisplayString(), $operator.operator.toString());
                    JqlParseException exception = new JqlParseException(errorMessage);
                    reportError(errorMessage, exception);
                }
           }
        }
	}
	;
	catch [RecognitionException e]
	{
	    //Because of the ANTLR lookahead, these ain't actually called ;-)
	    if (f == null)
	    {
	        reportError(JqlParseErrorMessages.badFieldName(e.token), e);
	    }
	    else if (op == null)
	    {
	        reportError(JqlParseErrorMessages.badOperator(e.token), e);
	    }
	    else if (opand == null)
	    {
	        reportError(JqlParseErrorMessages.badOperand(e.token), e);
	    }
	    else
	    {
	        reportError(JqlParseErrorMessages.genericParseError(e.token), e);
	    }
	}


historyPredicate returns [HistoryPredicate predicate]
	@init {
		final List<HistoryPredicate> predicates = new ArrayList<HistoryPredicate>();
	}
	: (p = terminalHistoryPredicate { predicates.add($p.predicate);})+ {$predicate = predicates.size() == 1? predicates.get(0) : new AndHistoryPredicate(predicates); }
	;

/*
historyPredicate returns [HistoryPredicate predicate]
	@init {
		final List<HistoryClause> clauses = new ArrayList<HistoryClause>();
	}
	: orHistoryPredicate {$predicate = orHistoryPredicate.predicate;}
	;


orHistoryPredicate returns [HistoryPredicate predicate]
	@init {
		final List<HistoryPredicate> predicates = new ArrayList<HistoryPredicate>();
	}
	: p = andHistoryPredicate { predicates.add($p.predicate);} ( OR p = andHistoryPredicate { predicates.add($p.predicate);} )* 
	{
		$predicates.size() == 1 ? predicates.get(0) : new OrHistoryPredicate(predicates);
	}
	;
	
andHistoryPredicate returns [HistoryPredicate predicate]
	@init {
		final List<HistoryPredicate> predicates  = new ArrayList<HistoryPredicate>();
	}
	: p = notHistoryPredicate { predicates.add($p.predicate);} (AND? p = notHistoryPredicate {predicates.add($p.predicate);} )* 
	{
		$predicates.size() == 1 ? predicates.get(0) : new AndHistoryPredicate(predicates);
	}
	;
	
notHistoryPredicate returns [HistoryPredicate predicate]
	@init {
		final List<HistoryPredicate> predicates  = new ArrayList<HistoryPredicate>();
	}
	: (NOT|BANG) np = notHistoryPredicate { $predicate = new NotHistoryPredicate($np.predicate); }
	| terminalHistoryPredicate { $predicate = $terminalHistoryPredicate.predicate; }
	| subHistoryPredicate { $predicate = $subPredicate.predicate; }
	;
	catch [NoViableAltException e]
	{
	    // not sure what this case suggests at this stage
	    e.printStackTrace();
	}
	


subHistoryPredicate returns [HistoryPredicate predicate]
    : LPAREN orHistoryPredicate RPAREN { $predicate = $orHistoryPredicateClause.predicate; }
    ;
    catch [MismatchedTokenException e]
    {
        if (e.expecting == RPAREN)
        {
            reportError(JqlParseErrorMessages.expectedText(e.token, ")"), e);
        }
        else
        {
            throw e;
        }
    }
*/

terminalHistoryPredicate returns [HistoryPredicate predicate]
	: historyPredicateOperator operand
	{
		$predicate = new TerminalHistoryPredicate($historyPredicateOperator.operator, $operand.operand);
	}
	;


historyPredicateOperator returns [Operator operator]
	: FROM { $operator = Operator.FROM; }
	| TO {$operator = Operator.TO; }
	| BY {$operator = Operator.BY; }
	| BEFORE {$operator = Operator.BEFORE; }
	| AFTER {$operator = Operator.AFTER; }
	| ON {$operator = Operator.ON; } 
	| DURING {$operator = Operator.DURING;};
	
	

/*
 * Parse the current operator.
 */
operator returns [Operator operator]
	: EQUALS { $operator = Operator.EQUALS; }
	| NOT_EQUALS { $operator = Operator.NOT_EQUALS; }
	| LIKE { $operator = Operator.LIKE; }
	| NOT_LIKE { $operator = Operator.NOT_LIKE; }	
	| LT { $operator = Operator.LESS_THAN; }
	| GT { $operator = Operator.GREATER_THAN; }
	| LTEQ { $operator = Operator.LESS_THAN_EQUALS; }
	| GTEQ { $operator = Operator.GREATER_THAN_EQUALS; }
	| IN { $operator = Operator.IN; }
	| IS NOT { $operator = Operator.IS_NOT; }
	| IS { $operator = Operator.IS; }
	| NOT IN { $operator = Operator.NOT_IN; }
	| WAS { $operator = Operator.WAS; }
	| WAS NOT { $operator = Operator.WAS_NOT; }
     	| WAS IN { $operator = Operator.WAS_IN; }
     	| WAS NOT IN { $operator = Operator.WAS_NOT_IN; }
     	| CHANGED { $operator = Operator.CHANGED; }
     	;
	catch [MismatchedTokenException e]
	{
	    //This will only get thrown when we read in "IS" or "NOT" not followed by its correct string.
        if (e.expecting == NOT)
        {
            reportError(JqlParseErrorMessages.expectedText(e.token, "NOT"), e);
        }
        else if (e.expecting == IN)
        {
            reportError(JqlParseErrorMessages.expectedText(e.token, "IN"), e);
        }
        else
        {
            //We will do this just in case.
            reportError(JqlParseErrorMessages.badOperator(e.token), e);
        }
	}
	catch [RecognitionException e]
	{
        Token currentToken = input.LT(1);
        if (currentToken.getType() == IS)
        {
            //This happens when we get an IS is not followed by a NOT.
            reportError(JqlParseErrorMessages.expectedText(e.token, "NOT"), e);
        }
        else if (currentToken.getType() == NOT)
        {
            //This happens when we get a NOT is not followed by a IN.
            reportError(JqlParseErrorMessages.expectedText(e.token, "IN"), e);
        }
        else if (currentToken.getType() == WAS)
        {
            reportError(JqlParseErrorMessages.badOperand(e.token),e);
        }
        else
        {
            reportError(JqlParseErrorMessages.badOperator(e.token), e);
        }
    }

/*
 * Parse the JQL field name of a JQL clause.
 */
field returns [FieldReference field]
    @init {
        ArrayList names = new ArrayList();
        ArrayList arrays = new ArrayList();
        ArrayList propertyRefs = new ArrayList();
    }
    @after {
        $field = new FieldReference(names, arrays, propertyRefs);
    }
	:
    num = numberString { names.add($num.string); }
    |
    (
        (
            str = string { names.add(checkFieldName($str.start)); }
            | cf = customField { names.add($cf.field); }
        )
        (
	        (
	            LBRACKET
	            (
	                ref = argument {arrays.add($ref.arg);}
	            )
	            RBRACKET
	        )
	        (
	            ref = propertyArgument {propertyRefs.add($ref.arg);}
	        )*
        )*
	)
	;
	catch [MismatchedTokenException e]
	{
	    switch (e.expecting)
	    {
	        case LBRACKET:
                reportError(JqlParseErrorMessages.expectedText(e.token, "["), e);
                break;
            case RBRACKET:
                reportError(JqlParseErrorMessages.expectedText(e.token, "]"), e);
                break;
	    }
	}
	catch [EarlyExitException e]
	{
        reportError(JqlParseErrorMessages.badPropertyArgument(e.token), e);
	}
	catch [RecognitionException e]
	{
	    if (e.token.getType() == LBRACKET)
        {
            //We probably have some sort of custom field id that does not start with cf. Lets tell the user all about it.
            reportError(JqlParseErrorMessages.expectedText(e.token, "cf"), e);
        }
        else
        {
            reportError(JqlParseErrorMessages.badFieldName(e.token), e);
        }
	}

customField returns [String field]
    : CUSTOMFIELD LBRACKET posnum = POSNUMBER RBRACKET { $field = JqlCustomFieldId.toString(parseLong($posnum)); }
    ;
    catch [MismatchedTokenException e]
    {
        switch(e.expecting)
        {
            case CUSTOMFIELD:
                reportError(JqlParseErrorMessages.expectedText(e.token, "cf"), e);
                break;
            case LBRACKET:
                reportError(JqlParseErrorMessages.expectedText(e.token, "["), e);
                break;
            case POSNUMBER:
                reportError(JqlParseErrorMessages.badCustomFieldId(e.token), e);
                break;
            case RBRACKET:
                reportError(JqlParseErrorMessages.expectedText(e.token, "]"), e);
                break;
            default:
                throw e;
        }
    }

/*
 * Used by JIRA to check if a field name is actually valid.
 */
fieldCheck returns [FieldReference field]
	: f = field { $field = $f.field; } EOF
	;

/*
 * Parse the operand (RHS) of a JQL clause.
 */
operand	returns [Operand operand]
	: EMPTY { $operand = new EmptyOperand(); }
	| str = string { $operand = new SingleValueOperand($str.string); }
	| number = numberString { $operand = new SingleValueOperand(parseLong($numberString.start)); }
	| fn = func {$operand = $fn.func;}
	| l = list {$operand = $l.list;}
	;
	catch [NoViableAltException e]
	{
        final Token currentToken = input.LT(1);
        final Token errorToken = e.token;

        //HACKETY, HACKETY HACK. ANTLR uses a DFA to decide which type of operand we are going to handle. This DFA
        //will only find "string" if it is followed by one of {EOF, OR, AND, RPAREN, COMMA, ORDER}. This means that
        //a query like "a=b c=d" will actually fail here and appear to be an illegal operand. So we use a simple
        //heuristic here. If the currentToken != errorToken then it means we have a valid value its just that we
        //have not seen one of the expected trailing tokens.

        if (currentToken.getTokenIndex() < errorToken.getTokenIndex())
        {
            if (operandLevel <= 0)
            {
                //If not in a MutliValueOperand, we probably mean "AND" and "OR"
                reportError(JqlParseErrorMessages.needLogicalOperator(errorToken), e);
            }
            else
            {
                //If in a MutliValueOperand, we probably mean "," or ")".
                reportError(JqlParseErrorMessages.expectedText(errorToken, ",", ")"), e);
            }
        }
        else
        {
            reportError(JqlParseErrorMessages.badOperand(errorToken), e);
        }
	}
	catch [RecognitionException e]
	{
	    reportError(JqlParseErrorMessages.badOperand(e.token), e);
	}
 
/*
 * Parse a String in JQL.
 */
string returns [String string]
	: str = STRING { $string = $str.text; }
	| str = QUOTE_STRING { $string = $str.text; }
	| str = SQUOTE_STRING { $string = $str.text; }
	;

numberString returns [String string]
	: num = (POSNUMBER | NEGNUMBER) { $string = $num.text; }
	;

/*
 * This rule is called by JIRA to ensure 
 */
stringValueCheck returns [String string]
	: str = string { $string = $str.string; } EOF
	;
	
/*
 * Parse the JQL list structure used for the 'IN' operator.
 */
list returns [Operand list]
	@init {
		final List <Operand> args = new ArrayList<Operand>();
		operandLevel++;
	}
	@after {
	    operandLevel--;
	}
	: LPAREN opnd = operand {args.add($opnd.operand);} 
		(COMMA opnd = operand {args.add($opnd.operand);})* RPAREN {$list = new MultiValueOperand(args);}
	;
	catch [MismatchedTokenException e]
    {
        if (e.expecting == RPAREN)
        {
            reportError(JqlParseErrorMessages.expectedText(e.token, ")"), e);
        }
        else
        {
            throw e;
        }
    }
    
/*
 * Parse out the JQL function.
 */
func returns [FunctionOperand func]
	: fname = funcName LPAREN arglist? RPAREN
	{
	    final List<String> args = $arglist.args == null ? Collections.<String>emptyList() : $arglist.args;
        $func = new FunctionOperand($fname.name, args);
    }
	;
	catch [MismatchedTokenException e]
	{
	    //We should only get here when the query is trying to match ')'.
        if (e.expecting == RPAREN)
        {
            if (e.token.getType() == EOF || e.token.getType() == COMMA)
            {
                reportError(JqlParseErrorMessages.expectedText(e.token, ")"), e);
            }
            else
            {
                //There is some argument that is not a string or number.
                reportError(JqlParseErrorMessages.badFunctionArgument(e.token), e);
            }
        }
        else
        {
            reportError(JqlParseErrorMessages.genericParseError(e.token), e);
        }
    }
	catch [RecognitionException e]
	{
        reportError(JqlParseErrorMessages.genericParseError(e.token), e);
	}

/*
 * Rule to match function names.
 */ 
funcName returns [String name]
	: string { $name = checkFunctionName($string.start); }
	| num = numberString { $name = $num.string; }
	;

/*
 * Rule used by JIRA to check the validity of a function name.
 */	
funcNameCheck returns [String name]
	: fname = funcName { $name = $fname.name; } EOF
	;

/*
 * Parse out a JQL function argument list.
 */
arglist	returns [List<String> args]
	@init {
		args = new ArrayList<String>();
	}
	@after {
	    //ANTLR will exit the arg list after the first token that is NOT a comma. If we exit and we are not
	    //at the end of the argument list, then we have an error.

	    final Token currentToken = input.LT(1);
	    if (currentToken.getType() != EOF && currentToken.getType() != RPAREN)
        {
            reportError(JqlParseErrorMessages.expectedText(currentToken, ")", ","), null);
        }
	}
	: str = argument { args.add($str.arg); }
	(COMMA str = argument { args.add($str.arg); } )*
	;

propertyArgument returns [String arg]
	: a = argument
	{
	    //property argument should at least contain one dot and property reference
	        if (a.length()<2 || a.charAt(0) != '.' || a.charAt(1) == '.')
            {

                Token currentToken = input.LT(-1);
                if(input.get(input.LT(-1).getTokenIndex()-1).getType() == MATCHWS )
                {
                    reportError(JqlParseErrorMessages.badOperator(currentToken), null);
                }
                else
                {
                    reportError(JqlParseErrorMessages.badPropertyArgument(currentToken), null);
                }
            }
            //remove leading dot as this is unnecessary
            $arg = $a.arg.substring(1);

    };

/*
 * Parse out a JQL function argument. Must be strings for the time being.
 */
argument returns [String arg]
	: str = string { $arg = $str.string; } 
	| number = numberString { $arg = $number.string; }
	;
	catch [RecognitionException e]
	{
	    switch (e.token.getType())
	    {
	        case COMMA:
	        case RPAREN:
	            reportError(JqlParseErrorMessages.emptyFunctionArgument(e.token), e);
                break;
            case RBRACKET:
                reportError(JqlParseErrorMessages.badPropertyArgument(e.token), e);
                break;
            default:
                reportError(JqlParseErrorMessages.badFunctionArgument(e.token), e);
                break;
	    }
	}

/*
 * Used by JIRA to check that an argument value is actually valid.
 */
argumentCheck returns [String arg]
	: a = argument { $arg = $a.arg; } EOF
	;

/*
 * Represents the ORDER BY clause in JQL.
 */
orderBy returns [OrderBy order]
	 @init {
		final List <SearchSort> args = new ArrayList<SearchSort>();
	}
	: ORDER BY f = searchSort { args.add(f); }
		(COMMA f = searchSort { args.add(f); })* { $order = new OrderByImpl(args); }
	;
	catch [MismatchedTokenException e]
	{
        if (e.expecting == BY)
        {
            reportError(JqlParseErrorMessages.expectedText(e.token, "by"), e);
        }
        else if (e.expecting == ORDER)
        {
            //This should not happen since the lookahead ensures that ORDER has been matched.
            reportError(JqlParseErrorMessages.expectedText(e.token, "order"), e);
        }
        else
        {
            reportError(JqlParseErrorMessages.genericParseError(e.token), e);
        }
	}
	catch [RecognitionException e]
	{
	    reportError(JqlParseErrorMessages.genericParseError(e.token), e);
	}

/*
 * Represents a JQL field followed by a sort order.
 */
searchSort returns [SearchSort sort]
	: f = field (o = DESC | o = ASC)?
	{
	    if (o == null)
	    {
	        Token token = input.LT(1);
	        //ANTLR is not very strict here. If ANTLR sees an illegal sort order, then it will simply leave
	        //the order by clause. We want to be a little stricter and find illegal SORT ORDERS.
	        if (token.getType() != EOF && token.getType() != COMMA)
	        {
	            reportError(JqlParseErrorMessages.badSortOrder(token), null);
	        }
	    }

	    SortOrder order = (o == null) ? null : SortOrder.parseString($o.text);
	    if (f != null && $f.field.isEntityProperty())
	    {
	        $sort = new SearchSort($f.field.getName(), Option.some($f.field.getProperty()), order);
	    }
	    else
	    {
		    $sort = new SearchSort($f.text, order);
		}
	}
	;
	catch [RecognitionException e]
	{
	    if (f == null)
	    {
            //We could not find a field.
            reportError(JqlParseErrorMessages.badFieldName(e.token), e);
	    }
	    else
	    {
	        //We could not find a correct order.
	        reportError(JqlParseErrorMessages.badSortOrder(e.token), e);
	    }
	}

/**
 * Some significant characters that need to be matched.
 */
LPAREN      : 	'(';
RPAREN		:	')';
COMMA		: 	',';
LBRACKET	:	'[';
RBRACKET 	: 	']';

fragment MINUS:  '-';

/**
 * JQL Operators
 */

BANG		:	'!';
LT		:	'<';
GT		:	'>';
GTEQ		:	'>=';
LTEQ 		:	'<=';
EQUALS		:	'=' ;
NOT_EQUALS	:	'!=';
LIKE		:	'~';
NOT_LIKE	:	'!~';		
IN		:	('I'|'i')('N'|'n');
IS		:	('I'|'i')('S'|'s');
AND 		:	('A'|'a')('N'|'n')('D'|'d') | AMPER | AMPER_AMPER;
OR		:	('O'|'o')('R'|'r') | PIPE | PIPE_PIPE;	
NOT		:	('N'|'n')('O'|'o')('T'|'t');
EMPTY		:	('E'|'e')('M'|'m')('P'|'p')('T'|'t')('Y'|'y') | ('N'|'n')('U'|'u')('L'|'l')('L'|'l');

WAS		:	('W'|'w')('A'|'a')('S'|'s');
CHANGED		:	('C'|'c')('H'|'h')('A'|'a')('N'|'n')('G'|'g')('E'|'e')('D'|'d');

BEFORE		:	('B'|'b')('E'|'e')('F'|'f')('O'|'o')('R'|'r')('E'|'e');
AFTER		:	('A'|'a')('F'|'f')('T'|'t')('E'|'e')('R'|'r');
FROM		:	('F'|'f')('R'|'r')('O'|'o')('M'|'m');
TO		:	('T'|'t')('O'|'o');


ON		:	('O'|'o')('N'|'n');
DURING		:	('D'|'d')('U'|'u')('R'|'r')('I'|'i')('N'|'n')('G'|'g');


/**
 * Order by
 */
ORDER	: ('o'|'O')('r'|'R')('d'|'D')('e'|'E')('r'|'R');
BY	: ('b'|'B')('y'|'Y');	
ASC	:	('a'|'A')('s'|'S')('c'|'C');
DESC:	('d'|'D')('e'|'E')('s'|'S')('c'|'C');

/*
 * Numbers
 */
POSNUMBER
	: DIGIT+;

NEGNUMBER
	: MINUS DIGIT+;

/**
 * The custom field prefix.
 */
CUSTOMFIELD
	: ('c'|'C')('f'|'F');

/**
 * String handling in JQL.
 */
 
STRING
    @init { pushPosition(STRING); }
    @after { popPosition(); }
	: (ESCAPE | ~(BSLASH | WS | STRINGSTOP))+
	{
		//Once this method is called, the text of the current token is fixed. This means that this Lexical rule
		//should not be called from other lexical rules.
		checkAndSet();
	}
	;

QUOTE_STRING
    @init { pushPosition(QUOTE_STRING); }
    @after { popPosition(); }
	: (QUOTE (ESCAPE | ~(BSLASH | QUOTE | CONTROLCHARS))* QUOTE)
	{
		//Once this method is called, the text of the current token is fixed. This means that this Lexical rule
		//should not be called from other lexical rules.
		stripAndSet();
	};
		
SQUOTE_STRING
    @init { pushPosition(SQUOTE_STRING); }
    @after { popPosition(); }
	: (SQUOTE (ESCAPE | ~(BSLASH | SQUOTE | CONTROLCHARS))* SQUOTE)
	{
		//Once this method is called, the text of the current token is fixed. This means that this Lexical rule
		//should not be called from other lexical rules.
		stripAndSet();
	};

/**
 * Match any whitespace and then ignore it.
 */	
MATCHWS  		:  	WS+ { $channel = HIDDEN; };

/**
 * These are some characters that we do not use now but we want to reserve. We have not reserved MINUS because we
 * really really really don't want to force people into quoting issues keys and dates.
 */
fragment RESERVED_CHARS
	: '{' | '}'
	| '*' | '/' | '%' | '+' | '^'
	| '$' | '#' | '@'
	| '?' | ';'
	;

/**
 * This is yet another large hack. We want this to be a different error message for reserved characters so we just match
 * them and try to recover.
 */
ERROR_RESERVED
    @init
    {
        pushPosition(ERROR_RESERVED);
        recover();
    }
    @after { popPosition(); }
    : RESERVED_CHARS
    ;


/**
 * This is a large HACK. Match any character that we did not match using one of the previous rules. It must be an error
 * so lets try and do something.
 */
ERRORCHAR
    @init
    {
        pushPosition(ERRORCHAR);
        recover();
    }
    @after { popPosition(); }
    : .
    ;

fragment QUOTE		:	'"' ;
fragment SQUOTE 	:	'\'';
fragment BSLASH		:	'\\';
fragment NL		:	'\r';
fragment CR		:	'\n';
fragment SPACE		:	' ';	
fragment AMPER	:	'&';
fragment AMPER_AMPER:	 '&&';
fragment PIPE	:	'|';	
fragment PIPE_PIPE	:	'||';	


/*
 * I would like to use the @afer rule but it does not appear to work for fragment rulez.
 */
fragment ESCAPE
    @init { pushPosition(ESCAPE); }
	:   BSLASH
	(
             	't'
             |  'n'
             |  'r' 
             |  QUOTE 
             |  SQUOTE
             |  BSLASH 
             |  SPACE
             |	'u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT
	) { popPosition(); }
	;

/**
 * These are the Tokens that should not be included as a part of an unquoted string.
 */
fragment STRINGSTOP
	: CONTROLCHARS  
	| QUOTE | SQUOTE
	| EQUALS 
	| BANG 
	| LT | GT
	| LPAREN | RPAREN 
	| LIKE 
	| COMMA 
	| LBRACKET | RBRACKET
	| PIPE
	| AMPER
	| RESERVED_CHARS
	| NEWLINE;

/*
 * These are control characters minus whitespace. We use the negation of this set as the set of 
 * characters that we allow in a string.
 *
 * NOTE: This list needs to be synchronised with JqlStringSupportImpl.isJqlControlCharacter.
 */
fragment CONTROLCHARS
	:	'\u0000'..'\u0009'  //Exclude '\n' (\u000a)
	|   '\u000b'..'\u000c'  //Exclude '\r' (\u000d)
	|   '\u000e'..'\u001f'
	|	'\u007f'..'\u009f'
	//The following are Unicode non-characters. We don't want to parse them. Importantly, we wish 
	//to ignore U+FFFF since ANTLR evilly uses this internally to represent EOF which can cause very
	//strange behaviour. For example, the Lexer will incorrectly tokenise the POSNUMBER 1234 as a STRING
	//when U+FFFF is not excluded from STRING.
	//
	//http://en.wikipedia.org/wiki/Unicode
	| 	'\ufdd0'..'\ufdef'
	|	'\ufffe'..'\uffff' 
	;

fragment NEWLINE
    :   NL | CR;

fragment HEXDIGIT
	:	DIGIT | ('A'|'a') | ('B'|'b') | ('C'|'c') | ('D'|'d') | ('E'|'e') | ('F'|'f')
	;
	
fragment DIGIT
	:	'0'..'9'
	;

fragment WS 
	: 	(SPACE|'\t'|NEWLINE)
	;
