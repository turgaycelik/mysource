<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%--

Basic Usage:
<aui:component template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">error</aui:param>
    <aui:param name="'messageHtml'">Issue not found</aui:param>
</aui:component>

Advanced Usage:
<aui:component id="'customWarning'" template="auimessage.jsp" theme="'aui'">
    <aui:param name="'messageType'">warning</aui:param>
    <aui:param name="'cssClass'">my-class</aui:param>
    <aui:param name="'iconText'">This is a warning</aui:param>
    <aui:param name="'helpKey'">JRA21004</aui:param>
    <aui:param name="'titleText'" >You have no memory left</aui:param>
    <aui:param name="'messageHtml'"><p>To rectify this issue, please allocate more memory.</p></aui:param>
</aui:component>

Note: Use the parameter <aui:param name="'hideIcon'" value="true" /> if you do not want an icon
Note: If the message has a help link use the parameter <aui:param name="'helpKey'">JRAfookey</aui:param>
--%>
<%-- Define variables up here to keep logic separate from the markup --%>
<ww:declare id="varId" value="parameters['id']"><ww:if test="."> id="<ww:property value="."/>"</ww:if></ww:declare>
<ww:declare id="varType" value="parameters['messageType']"><ww:if test=". == 'error'">error</ww:if><ww:elseIf test=". == 'warning'">warning</ww:elseIf><ww:elseIf test=". == 'info'">info</ww:elseIf><ww:elseIf test=". == 'success'">success</ww:elseIf><ww:elseIf test=". == 'hint'">hint</ww:elseIf><ww:elseIf test=". == 'link'">link</ww:elseIf></ww:declare>
<ww:declare id="varTypeClass" value="@varType"><ww:if test=". && . != ''"> <ww:property value="." /></ww:if></ww:declare>
<ww:declare id="varIcon" value="parameters['hideIcon']"><ww:if test="parameters['messageType'] && . != true"><span class="aui-icon icon-<ww:property value="@varType" />"><ww:property value="parameters['iconText']"><ww:if test="."><ww:property value="."/></ww:if></ww:property></span></ww:if></ww:declare>
<ww:declare id="varCssClass" value="parameters['cssClass']"><ww:if test="."> <ww:property value="."/></ww:if></ww:declare>
<ww:declare id="varTitle" value="parameters['titleText']"><ww:if test="."><p class="title"><strong><ww:property value="." escape="false"/></strong></p></ww:if></ww:declare>
<ww:declare id="varHelp" value="parameters['helpKey']"><ww:if test="."><ww:component name="parameters['helpKey']" template="help.jsp" theme="'aui'" /></ww:if></ww:declare>
<%-- Template starts here --%>
<div<ww:property value="@varId" escape="false" /> class="aui-message<ww:property value="@varTypeClass" /><ww:property value="@varCssClass" />"><ww:property value="@varIcon" escape="false" /><ww:property value="@varHelp" escape="false" /><ww:property value="@varTitle" escape="false" /><ww:property value="parameters['messageHtml']" escape="false" /></div>
<%-- Clear out the variables so they don't leak to other components--%>
<ww:property id="varId" value="''"/>
<ww:property id="varType" value="''"/>
<ww:property id="varTypeClass" value="''"/>
<ww:property id="varIcon" value="''"/>
<ww:property id="varCssClass" value="''"/>
<ww:property id="varTitle" value="''"/>
<ww:property id="varHelp" value="''"/>
