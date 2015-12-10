package com.atlassian.jira.web.component.jql;

import java.util.Collections;
import java.util.Locale;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.ValueGeneratingClauseHandler;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.operand.FunctionOperandHandler;
import com.atlassian.jira.jql.operand.registry.JqlFunctionHandlerRegistry;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.jql.values.ClauseValuesGenerator;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.easymock.PowerMock;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

 /**
 * @since v4.0
 */
public class TestDefaultAutoCompleteJsonGenerator
{
    @Rule
    public final InitMockitoMocks initMocks = new InitMockitoMocks(this);

    @Mock JqlFunction badJqlFunction;
    @Mock JqlFunction badJqlFunction2;
    @Mock I18nHelper i18nHelper;

    private SearchHandlerManager searchHandlerManager;
    private JqlStringSupport jqlStringSupport;
    private FieldManager fieldManager;
    private JqlFunctionHandlerRegistry jqlFunctionHandlerRegistry;
    private DefaultAutoCompleteJsonGenerator jsonGenerator;

    private EasyMockSupport easyMockSupport;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        easyMockSupport = new EasyMockSupport();
        searchHandlerManager = easyMockSupport.createMock(SearchHandlerManager.class);
        jqlStringSupport = easyMockSupport.createMock(JqlStringSupport.class);
        fieldManager = easyMockSupport.createMock(FieldManager.class);
        jqlFunctionHandlerRegistry = easyMockSupport.createMock(JqlFunctionHandlerRegistry.class);

        jsonGenerator = new DefaultAutoCompleteJsonGenerator(searchHandlerManager, jqlStringSupport, fieldManager, jqlFunctionHandlerRegistry)
        {
            @Override
            String htmlEncode(final String string)
            {
                return string;
            }
        };

    }

    @Test
    public void testGetVisibleFieldNamesJsonHappyPath() throws Exception
    {
        final ClauseInformation information = easyMockSupport.createMock(ClauseInformation.class);
        expect(information.getJqlClauseNames()).andReturn(new ClauseNames("field2"));
        expect(information.getFieldId()).andReturn("id2");
        expect(information.getSupportedOperators()).andReturn(OperatorClasses.EMPTY_ONLY_OPERATORS);
        expect(information.getDataType()).andReturn(JiraDataTypes.DATE);

        final ClauseHandler clauseHandler = easyMockSupport.createMock(ClauseHandler.class);
        expect(clauseHandler.getInformation()).andReturn(information);

        expect(searchHandlerManager.getVisibleClauseHandlers(null)).andReturn(CollectionBuilder.newBuilder(new MockValuesGeneratingClauseHandler("field1"), clauseHandler, new MockValuesGeneratingClauseHandler("cf[12345]", "Custom Field Name")).asList());

        expect(jqlStringSupport.encodeFieldName("Custom Field Name")).andReturn("Custom Field Name");
        expect(jqlStringSupport.encodeFieldName("field1")).andReturn("field1");
        expect(jqlStringSupport.encodeFieldName("field2")).andReturn("field2");

        final NavigableField navigableField = easyMockSupport.createMock(NavigableField.class);
        final Field field = easyMockSupport.createMock(Field.class);
        final CustomField customField = easyMockSupport.createMock(CustomField.class);
        expect(customField.getUntranslatedName()).andReturn("Custom Field Name");

        expect(fieldManager.getField("field1")).andReturn(navigableField);
        expect(fieldManager.getField("id2")).andReturn(field);
        expect(fieldManager.getField("cf[12345]")).andReturn(customField);
        easyMockSupport.replayAll();

        final String json = jsonGenerator.getVisibleFieldNamesJson(null, Locale.ENGLISH);

        JSONArray expectedOperators = new JSONArray();
        for (Operator oper : OperatorClasses.EMPTY_ONLY_OPERATORS)
        {
            expectedOperators.put(oper.getDisplayString());
        }
        JSONArray expectedJsonArr = new JSONArray();

        JSONArray allTypes = new JSONArray();
        allTypes.put("java.lang.Object");
        JSONArray dateTypes = new JSONArray();
        dateTypes.put("java.util.Date");

        JSONObject expectedJsonObj3 = new JSONObject();
        expectedJsonObj3.put("value", "Custom Field Name");
        expectedJsonObj3.put("displayName", "Custom Field Name - cf[12345]");
        expectedJsonObj3.put("auto", "true");
        expectedJsonObj3.put("orderable", "true");
        expectedJsonObj3.put("searchable", "true");
        expectedJsonObj3.put("cfid", "cf[12345]");
        expectedJsonObj3.put("operators", new JSONArray());
        expectedJsonObj3.put("types", allTypes);
        expectedJsonArr.put(expectedJsonObj3);

        JSONObject expectedJsonObj1 = new JSONObject();
        expectedJsonObj1.put("value", "field1");
        expectedJsonObj1.put("displayName", "field1");
        expectedJsonObj1.put("auto", "true");
        expectedJsonObj1.put("orderable", "true");
        expectedJsonObj1.put("searchable", "true");
        expectedJsonObj1.put("operators", new JSONArray());
        expectedJsonObj1.put("types", allTypes);
        expectedJsonArr.put(expectedJsonObj1);

        JSONObject expectedJsonObj2 = new JSONObject();
        expectedJsonObj2.put("value", "field2");
        expectedJsonObj2.put("displayName", "field2");
        expectedJsonObj2.put("searchable", "true");        
        expectedJsonObj2.put("operators", expectedOperators);
        expectedJsonObj2.put("types", dateTypes);
        expectedJsonArr.put(expectedJsonObj2);

        assertEquals(expectedJsonArr.toString(), json);
        easyMockSupport.verifyAll();
    }

    @Test
    public void testGetVisibleFieldNamesJsonCustomFieldWithOnlyId() throws Exception
    {
        final ClauseInformation information = easyMockSupport.createMock(ClauseInformation.class);
        expect(information.getJqlClauseNames()).andReturn(new ClauseNames("field2"));
        expect(information.getFieldId()).andReturn("id2");
        expect(information.getSupportedOperators()).andReturn(OperatorClasses.EMPTY_ONLY_OPERATORS);
        expect(information.getDataType()).andReturn(JiraDataTypes.DATE);

        final ClauseHandler clauseHandler = easyMockSupport.createMock(ClauseHandler.class);
        expect(clauseHandler.getInformation()).andReturn(information);

        expect(searchHandlerManager.getVisibleClauseHandlers(null)).andReturn(CollectionBuilder.newBuilder(new MockValuesGeneratingClauseHandler("field1"), clauseHandler, new MockValuesGeneratingClauseHandler("cf[12345]")).asList());

        expect(jqlStringSupport.encodeFieldName("cf[12345]")).andReturn("cf[12345]");
        expect(jqlStringSupport.encodeFieldName("field1")).andReturn("field1");
        expect(jqlStringSupport.encodeFieldName("field2")).andReturn("field2");


        final NavigableField navigableField = easyMockSupport.createMock(NavigableField.class);
        final Field field = easyMockSupport.createMock(Field.class);
        final CustomField customField = easyMockSupport.createMock(CustomField.class);
        expect(customField.getUntranslatedName()).andReturn("Custom Field Name");

        expect(fieldManager.getField("field1")).andReturn(navigableField);
        expect(fieldManager.getField("id2")).andReturn(field);
        expect(fieldManager.getField("cf[12345]")).andReturn(customField);

        easyMockSupport.replayAll();

        final String json = jsonGenerator.getVisibleFieldNamesJson(null, Locale.ENGLISH);

        JSONArray expectedOperators = new JSONArray();
        for (Operator oper : OperatorClasses.EMPTY_ONLY_OPERATORS)
        {
            expectedOperators.put(oper.getDisplayString());
        }

        JSONArray allTypes = new JSONArray();
        allTypes.put("java.lang.Object");
        JSONArray dateTypes = new JSONArray();
        dateTypes.put("java.util.Date");

        JSONArray expectedJsonArr = new JSONArray();

        JSONObject expectedJsonObj3 = new JSONObject();
        expectedJsonObj3.put("value", "cf[12345]");
        expectedJsonObj3.put("displayName", "Custom Field Name - cf[12345]");
        expectedJsonObj3.put("auto", "true");
        expectedJsonObj3.put("orderable", "true");
        expectedJsonObj3.put("searchable", "true");
        expectedJsonObj3.put("operators", new JSONArray());
        expectedJsonObj3.put("types", allTypes);
        expectedJsonArr.put(expectedJsonObj3);

        JSONObject expectedJsonObj1 = new JSONObject();
        expectedJsonObj1.put("value", "field1");
        expectedJsonObj1.put("displayName", "field1");
        expectedJsonObj1.put("auto", "true");
        expectedJsonObj1.put("orderable", "true");
        expectedJsonObj1.put("searchable", "true");
        expectedJsonObj1.put("operators", new JSONArray());
        expectedJsonObj1.put("types", allTypes);
        expectedJsonArr.put(expectedJsonObj1);

        JSONObject expectedJsonObj2 = new JSONObject();
        expectedJsonObj2.put("value", "field2");
        expectedJsonObj2.put("displayName", "field2");
        expectedJsonObj2.put("searchable", "true");
        expectedJsonObj2.put("operators", expectedOperators);
        expectedJsonObj2.put("types", dateTypes);
        expectedJsonArr.put(expectedJsonObj2);

        assertEquals(expectedJsonArr.toString(), json);
        easyMockSupport.verifyAll();
    }

    @Test
    public void testGetVisibleFieldNamesJsonCustomFieldConflictsWithOtherField() throws Exception
    {
        final ClauseInformation information = easyMockSupport.createMock(ClauseInformation.class);
        expect(information.getJqlClauseNames()).andReturn(new ClauseNames("field2"));
        expect(information.getFieldId()).andReturn("id2");
        expect(information.getSupportedOperators()).andReturn(OperatorClasses.EMPTY_ONLY_OPERATORS);
        expect(information.getDataType()).andReturn(JiraDataTypes.DATE);

        final ClauseHandler clauseHandler = easyMockSupport.createMock(ClauseHandler.class);
        expect(clauseHandler.getInformation()).andReturn(information);

        expect(searchHandlerManager.getVisibleClauseHandlers(null)).andReturn(CollectionBuilder.newBuilder(new MockValuesGeneratingClauseHandler("field1"), clauseHandler, new MockValuesGeneratingClauseHandler("cf[12345]", "field1")).asList());

        expect(jqlStringSupport.encodeFieldName("field1")).andReturn("field1");
        expect(jqlStringSupport.encodeFieldName("cf[12345]")).andReturn("cf[12345]");
        expect(jqlStringSupport.encodeFieldName("field2")).andReturn("field2");

        final NavigableField navigableField = easyMockSupport.createMock(NavigableField.class);
        final Field field = easyMockSupport.createMock(Field.class);
        final CustomField customField = easyMockSupport.createMock(CustomField.class);
        expect(customField.getUntranslatedName()).andReturn("field1");

        expect(fieldManager.getField("field1")).andReturn(navigableField);
        expect(fieldManager.getField("id2")).andReturn(field);
        expect(fieldManager.getField("cf[12345]")).andReturn(customField);

        easyMockSupport.replayAll();

        final String json = jsonGenerator.getVisibleFieldNamesJson(null, Locale.ENGLISH);

        JSONArray expectedOperators = new JSONArray();
        for (Operator oper : OperatorClasses.EMPTY_ONLY_OPERATORS)
        {
            expectedOperators.put(oper.getDisplayString());
        }
        JSONArray allTypes = new JSONArray();
        allTypes.put("java.lang.Object");
        JSONArray dateTypes = new JSONArray();
        dateTypes.put("java.util.Date");

        JSONArray expectedJsonArr = new JSONArray();

        JSONObject expectedJsonObj1 = new JSONObject();
        expectedJsonObj1.put("value", "field1");
        expectedJsonObj1.put("displayName", "field1");
        expectedJsonObj1.put("auto", "true");
        expectedJsonObj1.put("orderable", "true");
        expectedJsonObj1.put("searchable", "true");
        expectedJsonObj1.put("operators", new JSONArray());
        expectedJsonObj1.put("types", allTypes);
        expectedJsonArr.put(expectedJsonObj1);

        JSONObject expectedJsonObj3 = new JSONObject();
        expectedJsonObj3.put("value", "cf[12345]");
        expectedJsonObj3.put("displayName", "field1 - cf[12345]");
        expectedJsonObj3.put("auto", "true");
        expectedJsonObj3.put("orderable", "true");
        expectedJsonObj3.put("searchable", "true");
        expectedJsonObj3.put("operators", new JSONArray());
        expectedJsonObj3.put("types", allTypes);
        expectedJsonArr.put(expectedJsonObj3);

        JSONObject expectedJsonObj2 = new JSONObject();
        expectedJsonObj2.put("value", "field2");
        expectedJsonObj2.put("displayName", "field2");
        expectedJsonObj2.put("searchable", "true");
        expectedJsonObj2.put("operators", expectedOperators);
        expectedJsonObj2.put("types", dateTypes);
        expectedJsonArr.put(expectedJsonObj2);

        assertEquals(expectedJsonArr.toString(), json);
        easyMockSupport.verifyAll();
    }

    @Test
    public void testGetVisibleFieldNamesJsonCustomFieldConflictsWithOtherCustomField() throws Exception
    {
        final ClauseInformation information = easyMockSupport.createMock(ClauseInformation.class);
        expect(information.getJqlClauseNames()).andReturn(new ClauseNames("field2"));
        expect(information.getFieldId()).andReturn("id2");
        expect(information.getSupportedOperators()).andReturn(OperatorClasses.EMPTY_ONLY_OPERATORS);
        expect(information.getDataType()).andReturn(JiraDataTypes.DATE);

        final ClauseHandler clauseHandler = easyMockSupport.createMock(ClauseHandler.class);
        expect(clauseHandler.getInformation()).andReturn(information);

        expect(searchHandlerManager.getVisibleClauseHandlers(null)).andReturn(CollectionBuilder.newBuilder(new MockValuesGeneratingClauseHandler("field1"), clauseHandler, new MockValuesGeneratingClauseHandler("cf[12345]", "Cust Field Conflicts"), new MockValuesGeneratingClauseHandler("cf[54321]", "Cust Field Conflicts")).asList());

        expect(jqlStringSupport.encodeFieldName("cf[12345]")).andReturn("cf[12345]");
        expect(jqlStringSupport.encodeFieldName("cf[54321]")).andReturn("cf[54321]");
        expect(jqlStringSupport.encodeFieldName("field1")).andReturn("field1");
        expect(jqlStringSupport.encodeFieldName("field2")).andReturn("field2");

        final NavigableField navigableField = easyMockSupport.createMock(NavigableField.class);
        final Field field = easyMockSupport.createMock(Field.class);
        final CustomField customField = easyMockSupport.createMock(CustomField.class);
        expect(customField.getUntranslatedName()).andReturn("Cust Field Conflicts").times(2);

        expect(fieldManager.getField("field1")).andReturn(navigableField);
        expect(fieldManager.getField("id2")).andReturn(field);
        expect(fieldManager.getField("cf[12345]")).andReturn(customField);
        expect(fieldManager.getField("cf[54321]")).andReturn(customField);

        easyMockSupport.replayAll();

        final String json = jsonGenerator.getVisibleFieldNamesJson(null, Locale.ENGLISH);

        JSONArray allTypes = new JSONArray();
        allTypes.put("java.lang.Object");
        JSONArray dateTypes = new JSONArray();
        dateTypes.put("java.util.Date");

        JSONArray expectedOperators = new JSONArray();
        for (Operator oper : OperatorClasses.EMPTY_ONLY_OPERATORS)
        {
            expectedOperators.put(oper.getDisplayString());
        }
        JSONArray expectedJsonArr = new JSONArray();

        JSONObject expectedJsonObj3 = new JSONObject();
        expectedJsonObj3.put("value", "cf[12345]");
        expectedJsonObj3.put("displayName", "Cust Field Conflicts - cf[12345]");
        expectedJsonObj3.put("auto", "true");
        expectedJsonObj3.put("orderable", "true");
        expectedJsonObj3.put("searchable", "true");
        expectedJsonObj3.put("operators", new JSONArray());
        expectedJsonObj3.put("types", allTypes);
        expectedJsonArr.put(expectedJsonObj3);

        JSONObject expectedJsonObj4 = new JSONObject();
        expectedJsonObj4.put("value", "cf[54321]");
        expectedJsonObj4.put("displayName", "Cust Field Conflicts - cf[54321]");
        expectedJsonObj4.put("auto", "true");
        expectedJsonObj4.put("orderable", "true");
        expectedJsonObj4.put("searchable", "true");
        expectedJsonObj4.put("operators", new JSONArray());
        expectedJsonObj4.put("types", allTypes);
        expectedJsonArr.put(expectedJsonObj4);

        JSONObject expectedJsonObj1 = new JSONObject();
        expectedJsonObj1.put("value", "field1");
        expectedJsonObj1.put("displayName", "field1");
        expectedJsonObj1.put("auto", "true");
        expectedJsonObj1.put("orderable", "true");
        expectedJsonObj1.put("searchable", "true");
        expectedJsonObj1.put("operators", new JSONArray());
        expectedJsonObj1.put("types", allTypes);
        expectedJsonArr.put(expectedJsonObj1);

        JSONObject expectedJsonObj2 = new JSONObject();
        expectedJsonObj2.put("value", "field2");
        expectedJsonObj2.put("displayName", "field2");
        expectedJsonObj2.put("searchable", "true");
        expectedJsonObj2.put("operators", expectedOperators);
        expectedJsonObj2.put("types", dateTypes);
        expectedJsonArr.put(expectedJsonObj2);

        assertEquals(expectedJsonArr.toString(), json);
        easyMockSupport.verifyAll();
    }

    @Test
    public void testGetVisibleFunctionNamesJson() throws Exception
    {
         // Make sure we test the exclusions
        expect(jqlFunctionHandlerRegistry.getAllFunctionNames()).andReturn(CollectionBuilder.newBuilder("afunc", "cfunc", "bfunc", "currentUser").asMutableList());

        final JqlFunction jqlFunction = easyMockSupport.createMock(JqlFunction.class);
        expect(jqlFunction.getMinimumNumberOfExpectedArguments()).andReturn(0).andReturn(1).andReturn(2);
        expect(jqlFunction.getDataType()).andReturn(JiraDataTypes.ALL).andReturn(JiraDataTypes.DATE).andReturn(JiraDataTypes.ISSUE);


        final FunctionOperandHandler functionOperandHandler = PowerMock.createMock(FunctionOperandHandler.class);
        expect(functionOperandHandler.getJqlFunction()).andReturn(jqlFunction).anyTimes();
        expect(functionOperandHandler.isList()).andReturn(true).andReturn(false).andReturn(false);

        expect(jqlFunctionHandlerRegistry.getOperandHandler(new FunctionOperand("afunc"))).andReturn(functionOperandHandler);
        expect(jqlFunctionHandlerRegistry.getOperandHandler(new FunctionOperand("bfunc"))).andReturn(functionOperandHandler);
        expect(jqlFunctionHandlerRegistry.getOperandHandler(new FunctionOperand("cfunc"))).andReturn(functionOperandHandler);

        expect(jqlStringSupport.encodeFunctionName("afunc")).andReturn("afunc");
        expect(jqlStringSupport.encodeFunctionName("bfunc")).andReturn("bfunc");
        expect(jqlStringSupport.encodeFunctionName("cfunc")).andReturn("cfunc");
        PowerMock.replay(functionOperandHandler);
        easyMockSupport.replayAll();

        JSONArray allTypes = new JSONArray();
        allTypes.put("java.lang.Object");
        JSONArray dateTypes = new JSONArray();
        dateTypes.put("java.util.Date");
        JSONArray issueTypes = new JSONArray();
        issueTypes.put("com.atlassian.jira.issue.Issue");

        final String json = jsonGenerator.getVisibleFunctionNamesJson(null, Locale.ENGLISH);

        JSONArray expectedJsonArr = new JSONArray();
        JSONObject expectedJsonObj1 = new JSONObject();
        expectedJsonObj1.put("value", "afunc()");
        expectedJsonObj1.put("displayName", "afunc()");
        expectedJsonObj1.put("isList", "true");
        expectedJsonObj1.put("types", allTypes);
        expectedJsonArr.put(expectedJsonObj1);

        JSONObject expectedJsonObj2 = new JSONObject();
        expectedJsonObj2.put("value", "bfunc(\"\")");
        expectedJsonObj2.put("displayName", "bfunc(\"\")");
        expectedJsonObj2.put("types", dateTypes);
        expectedJsonArr.put(expectedJsonObj2);

        JSONObject expectedJsonObj3 = new JSONObject();
        expectedJsonObj3.put("value", "cfunc(\"\", \"\")");
        expectedJsonObj3.put("displayName", "cfunc(\"\", \"\")");
        expectedJsonObj3.put("types", issueTypes);
        expectedJsonArr.put(expectedJsonObj3);

        assertEquals(expectedJsonArr.toString(), json);
        PowerMock.verify(functionOperandHandler);
        easyMockSupport.verifyAll();
    }

    @Test
    public void testGetVisibleFunctionNamesJsonSurvivesPluginThrowingExceptions() throws Exception
    {
        expect(jqlFunctionHandlerRegistry.getAllFunctionNames()).andReturn(CollectionBuilder.newBuilder("afunc", "badfunc", "cfunc", "badfunc2", "bfunc").asMutableList());

        Mockito.when(badJqlFunction.getMinimumNumberOfExpectedArguments()).thenThrow(new RuntimeException("Misbehaving plugin exception"));
        Mockito.when(badJqlFunction2.isList()).thenThrow(new RuntimeException("Misbehaving plugin exception"));

        final JqlFunction goodJqlFunction = easyMockSupport.createMock(JqlFunction.class);
        expect(goodJqlFunction.getMinimumNumberOfExpectedArguments()).andReturn(0).andReturn(1).andReturn(2);
        expect(goodJqlFunction.getDataType()).andReturn(JiraDataTypes.ALL).andReturn(JiraDataTypes.DATE).andReturn(JiraDataTypes.ISSUE);

        final FunctionOperandHandler goodFunctionOperandHandler = PowerMock.createMock(FunctionOperandHandler.class);
        expect(goodFunctionOperandHandler.getJqlFunction()).andReturn(goodJqlFunction).anyTimes();
        expect(goodFunctionOperandHandler.isList()).andReturn(true).andReturn(false).andReturn(false);

        final FunctionOperandHandler badFunctionOperandHandler = new FunctionOperandHandler(badJqlFunction, i18nHelper);
        final FunctionOperandHandler badFunctionOperandHandler2 = new FunctionOperandHandler(badJqlFunction2, i18nHelper);

        expect(jqlFunctionHandlerRegistry.getOperandHandler(new FunctionOperand("afunc"))).andReturn(goodFunctionOperandHandler);
        expect(jqlFunctionHandlerRegistry.getOperandHandler(new FunctionOperand("badfunc"))).andReturn(badFunctionOperandHandler);
        expect(jqlFunctionHandlerRegistry.getOperandHandler(new FunctionOperand("bfunc"))).andReturn(goodFunctionOperandHandler);
        expect(jqlFunctionHandlerRegistry.getOperandHandler(new FunctionOperand("badfunc2"))).andReturn(badFunctionOperandHandler2);
        expect(jqlFunctionHandlerRegistry.getOperandHandler(new FunctionOperand("cfunc"))).andReturn(goodFunctionOperandHandler);

        expect(jqlStringSupport.encodeFunctionName("afunc")).andReturn("afunc");
        expect(jqlStringSupport.encodeFunctionName("bfunc")).andReturn("bfunc");
        expect(jqlStringSupport.encodeFunctionName("badfunc2")).andReturn("badfunc2");
        expect(jqlStringSupport.encodeFunctionName("cfunc")).andReturn("cfunc");
        PowerMock.replay(goodFunctionOperandHandler);
        easyMockSupport.replayAll();

        JSONArray allTypes = new JSONArray();
        allTypes.put("java.lang.Object");
        JSONArray dateTypes = new JSONArray();
        dateTypes.put("java.util.Date");
        JSONArray issueTypes = new JSONArray();
        issueTypes.put("com.atlassian.jira.issue.Issue");

        final String json = jsonGenerator.getVisibleFunctionNamesJson(null, Locale.ENGLISH);

        JSONArray expectedJsonArr = new JSONArray();
        JSONObject expectedJsonObj1 = new JSONObject();
        expectedJsonObj1.put("value", "afunc()");
        expectedJsonObj1.put("displayName", "afunc()");
        expectedJsonObj1.put("isList", "true");
        expectedJsonObj1.put("types", allTypes);
        expectedJsonArr.put(expectedJsonObj1);

        JSONObject expectedJsonObj2 = new JSONObject();
        expectedJsonObj2.put("value", "badfunc2()");
        expectedJsonObj2.put("displayName", "badfunc2()");
        expectedJsonObj2.put("types", Collections.emptyList());
        expectedJsonArr.put(expectedJsonObj2);

        JSONObject expectedJsonObj3 = new JSONObject();
        expectedJsonObj3.put("value", "bfunc(\"\")");
        expectedJsonObj3.put("displayName", "bfunc(\"\")");
        expectedJsonObj3.put("types", dateTypes);
        expectedJsonArr.put(expectedJsonObj3);

        JSONObject expectedJsonObj4 = new JSONObject();
        expectedJsonObj4.put("value", "cfunc(\"\", \"\")");
        expectedJsonObj4.put("displayName", "cfunc(\"\", \"\")");
        expectedJsonObj4.put("types", issueTypes);
        expectedJsonArr.put(expectedJsonObj4);

        assertEquals(expectedJsonArr.toString(), json);
        PowerMock.verify(goodFunctionOperandHandler);
        easyMockSupport.verifyAll();
    }

    private class MockValuesGeneratingClauseHandler implements ClauseHandler, ValueGeneratingClauseHandler
    {
        private ClauseInformation clauseInformation;

        private MockValuesGeneratingClauseHandler(final String ... fieldId)
        {
            final ClauseNames names = new ClauseNames(fieldId[0], fieldId);
            this.clauseInformation = new SimpleFieldSearchConstants(fieldId[0], names, fieldId[0], fieldId[0], fieldId[0], Collections.<Operator>emptySet(), JiraDataTypes.ALL);
        }

        public ClauseInformation getInformation()
        {
            return this.clauseInformation;
        }

        public ClauseQueryFactory getFactory()
        {
            return null;
        }

        public ClauseValidator getValidator()
        {
            return null;
        }

        public ClausePermissionHandler getPermissionHandler()
        {
            return null;
        }

        public ClauseContextFactory getClauseContextFactory()
        {
            return null;
        }

        public ClauseValuesGenerator getClauseValuesGenerator()
        {
            return null;
        }
    }
}
