<%--

Required Parameters:
    * text                      - The text to be used in the heading

Optional Parameters:
    * cssClass                  - CSS classes added to the heading (class doesn't display if left empty)

Code Example:
    <aui:component theme="'aui'" template="formHeading.jsp">
        <aui:param name="'text'"><ww:text name="'my.custom.form.heading.text'"/></aui:param>
        <aui:param name="'cssClass'">custom</aui:param>
    </aui:component>

Notes:
    See http://confluence.atlassian.com/display/AUI/Forms for more information

--%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<h2<ww:property value="parameters['cssClass']"><ww:if test="."> class="<ww:property value="."/>"</ww:if></ww:property>>
    <ww:property value="parameters['escape']">
        <ww:if test=". == false">
            <ww:property value="parameters['text']" escape="false"/>
        </ww:if>
        <ww:else>
            <ww:property value="parameters['text']"/>            
        </ww:else>
    </ww:property>
    <ww:property value="parameters['helpURL']">
        <ww:if test=".">
            <ww:component template="help.jsp" theme="'aui'">
                <ww:param name="'name'"><ww:property value="."/></ww:param>
                <ww:param name="'helpURLFragment'"><ww:property value="parameters['helpURLFragment']"/></ww:param>
            </ww:component>
        </ww:if>
    </ww:property>
</h2>