<%--

Required Parameters:
    * iconText                  - i18n text of the icon link
    * iconCssClass              - Additional CSS classes added to the icon link


Optional Parameters:
    * id                        - Inherits the ID of the form field if not used as a stand-alone component
    * iconURI                   - URI for the icon link ("#" used if left blank)
    * iconTitle                 - TITLE attribute icon link

Notes:
    If iconText is not set no icon will display

--%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<ww:property value="parameters['iconText']">
    <ww:if test=".">
        <%-- Help icons are a special case. --%>
        <ww:if test="parameters['iconCssClass']/contains('help') == true">
            <aui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Links.helpLink'">
                <aui:param name="'isLocal'" value="false"/>
                <aui:param name="'url'"><ww:property value="parameters['iconURI']" escape="false" /></aui:param>
                <ww:if test="parameters['iconTitle']">
                    <aui:param name="'title'"><ww:property value="parameters['iconTitle']" escape="false" /></aui:param>
                    <aui:param name="'description'"><ww:property value="parameters['iconText']" escape="false" /></aui:param>
                </ww:if>
                <ww:else>
                    <aui:param name="'title'"><ww:property value="parameters['iconText']" escape="false" /></aui:param>
                </ww:else>
                <aui:param name="'extraAttributes'"><ww:property value="parameters['id']"><ww:if test=".">id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>-icon"</ww:if></ww:property></aui:param>
            </aui:soy>
        </ww:if>
        <%-- Output regular icons in all other cases. --%>
        <ww:else>
            <ww:property value="parameters['iconURI']">
                <ww:if test=".">
                    <a href="<ww:property value="parameters['iconURI']"><ww:if test="."><ww:property value="."/></ww:if><ww:else>#</ww:else></ww:property>"<ww:property value="parameters['id']"><ww:if test="."> id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>-icon"</ww:if></ww:property><ww:property value="parameters['iconTitle']"><ww:if test="."> title="<ww:property value="."/>"</ww:if></ww:property>>
                        <span class="aui-icon<ww:property value="parameters['iconCssClass']"><ww:if test="."> <ww:property value="."/></ww:if></ww:property>"><ww:property id="parameters['iconText']" /></span>
                    </a>
                </ww:if>
                <ww:else>
                    <span<ww:property value="parameters['id']"><ww:if test="."> id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>-icon"</ww:if></ww:property><ww:property value="parameters['iconTitle']"><ww:if test="."> title="<ww:property value="."/>"</ww:if></ww:property>>
                        <span class="aui-icon<ww:property value="parameters['iconCssClass']"><ww:if test="."> <ww:property value="."/></ww:if></ww:property>"><ww:property value="parameters['iconText']" /></span>
                    </span>
                </ww:else>
            </ww:property>
        </ww:else>
    </ww:if>
</ww:property>