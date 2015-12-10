<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">warning</aui:param>
    <aui:param name="'titleText'"><ww:text name="'common.tasks.tasks.in.progress'"/></aui:param>
    <aui:param name="'messageHtml'">
        <p><ww:text name="'common.tasks.tasks.already.running.consider.impact'"/></p>
    </aui:param>
</aui:component>
