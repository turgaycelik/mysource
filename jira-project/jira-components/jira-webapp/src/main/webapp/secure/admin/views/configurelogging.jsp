<%@ taglib uri="webwork" prefix="ww"  %>
<%@ taglib uri="webwork" prefix="ui"  %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<page:applyDecorator name="jiraform">
	<page:param name="action">ConfigureLogging.jspa</page:param>
	<page:param name="submitId">update_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
	<page:param name="cancelURI">ViewLogging.jspa</page:param>
	<page:param name="title"><ww:text name="'admin.loggingandprofiling.configure.log.level'"/></page:param>
	<page:param name="width">100%</page:param>



    <ui:component label="text('admin.loggingandprofiling.logger.name')" name="'/logger/name'"  template="textlabel.jsp" />
    <ui:component name="'loggerName'" value="/logger/name" template="hidden.jsp" theme="'single'"  />

    <ui:select label="text('admin.loggingandprofiling.level')" name="'levelName'" list="/availableLevels"  value="/logger/level" />

</page:applyDecorator>


