<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.config.properties.APKeys" %>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties" %>
<%@ page import="com.atlassian.jira.web.action.util.CalendarResourceIncluder" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Locale" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<ww:if test="parameters['labelAfter'] != true"><jsp:include page="/template/aui/formFieldLabel.jsp" /></ww:if>
<input <ww:property value="parameters['accesskey']"><ww:if test=".">accesskey="<ww:property value="."/>"</ww:if></ww:property>
    class="text
    <ww:property value="parameters['size']">
        <ww:if test=". == 'long'">long-field</ww:if>
        <ww:elseIf test=". == 'medium'">medium-field</ww:elseIf>
        <ww:elseIf test=". == 'short'">short-field</ww:elseIf>
        <ww:elseIf test=". == 'very-short'">very-short-field</ww:elseIf>
        <ww:elseIf test=". == 'full'">full-width-field</ww:elseIf>
    </ww:property>
    <ww:property value="parameters['cssClass']"><ww:if test="."><ww:property value="."/></ww:if></ww:property>"
    <ww:property value="parameters['data']">
        <ww:if test=".">data="<ww:property value="."/>"</ww:if>
    </ww:property>
    <ww:if test="parameters['disabled'] == true">
        disabled="disabled"
    </ww:if>
    id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="parameters['id']"/>-date-picker"
    <ww:property value="parameters['maxlength']">
        <ww:if test=".">maxlength="<ww:property value="."/>"</ww:if>
    </ww:property>
    name="<ww:property value="parameters['name']"/>"
    <ww:if test="parameters['readonly'] == true">
        readonly="readonly"
    </ww:if>
    <ww:property value="parameters['style']">
        <ww:if test=".">style="<ww:property value="."/>"</ww:if>
    </ww:property>
    <ww:property value="parameters['tabindex']">
        <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
    </ww:property>
    type="text"
    <ww:property value="parameters['title']">
        <ww:if test=".">title="<ww:property value="."/>"</ww:if>
    </ww:property>
    <%-- the parameter 'nameValue' holds the result of evaluating getXXX() where XXX is the 'name' parameter --%>
    <ww:property value="parameters['nameValue']">
        <ww:if test=".">value="<ww:property value="."/>"</ww:if>
    </ww:property>
    />
<ww:if test="parameters['labelAfter'] == true"><jsp:include page="/template/aui/formFieldLabel.jsp" /></ww:if>
<jsp:include page="/template/aui/formFieldIcon.jsp" />
<jsp:include page="/template/aui/formFieldError.jsp" />
<%
    final ApplicationProperties appProperties = ComponentManager.getComponentInstanceOfType(ApplicationProperties.class);
    final Locale locale = ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper().getLocale();
    final CalendarResourceIncluder calendarResourceIncluder = new CalendarResourceIncluder();
    calendarResourceIncluder.includeForLocale(locale);
%>
<script type="text/javascript">
    Calendar.setup({
        firstDay     : <%= Calendar.getInstance(locale).getFirstDayOfWeek()-1 %>,       // first day of the week
        inputField   : "<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="parameters['id']"/>-date-picker",     // id of the input field
        button       : "<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="parameters['id']"/>-icon",  // trigger for the calendar (button ID)
        align        : "Br",                                                            // alignment (defaults to "Bl")
        singleClick  : true,
        useISO8601WeekNumbers   : <%= appProperties.getOption(APKeys.JIRA_DATE_TIME_PICKER_USE_ISO8601) %>, // use ISO8601 date/time standard
        <ww:if test="parameters['showsTime'] == true">
        showsTime    : "true",
        ifFormat     : "<ww:property value="/dateTimeFormat"/>",
        date         : <%= System.currentTimeMillis() %>,
        timeFormat   : "<ww:property value="/timeFormat"/>"
        </ww:if>
        <ww:else>
        ifFormat     : "<ww:property value="/dateFormat"/>"      // our date only format
        </ww:else>
    });
</script>

