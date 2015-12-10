<%--

Required Parameters:
    * id                        - ID attribute of the button (inherits IDs of its parents). No spaces, all lowercase,
                                  hyphens instead of underscores
    * name                      - NAME attribute of the button (sent as the NAME in the POST/GET)
    * text                      - i18n text that shows on the button (sent as the VALUE in the POST/GET)

Optional Parameters:
    * accesskey                 - ACCESSKEY to be used for the button (see notes below)
    * title                     - TITLE attribute of the button (see notes below)
    * cssClass                  - CSS classes added to class="button"
    * type (default: button)    - Set to "submit" if you need a submit button

Notes:
    The rendering of the title attribute essentially provides the following 3 outcomes...
        - accesskey AND title set:      title="Return to the previous result list (Press Alt+r)"
        - only title set:               title="Return to the previous result list"
        - only accesskey set:           title="(Press Alt+r)"

Code Example:
    <aui:component theme="'aui'" template="formSubmit.jsp">
        <aui:param name="'id'">reset</aui:param>
        <aui:param name="'name'">reset</aui:param>
        <aui:param name="'text'"><ww:text name="'my.custom.reset.button.text'"/></aui:param>
    </aui:component>

--%>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<input
    <ww:property value="parameters['accesskey']">
        <ww:if test=".">accesskey="<ww:property value="."/>"</ww:if>
    </ww:property>
    <ww:property value="parameters['cssClass']">
        <ww:if test=".">class="aui-button <ww:property value="."/>"</ww:if>
        <ww:else>class="aui-button"</ww:else>
    </ww:property>
    <ww:property value="parameters['id']">
        <ww:if test=".">id="<ww:property value="@jira.sitemesh.decorator.computed.id" /><ww:property value="."/>"</ww:if>
    </ww:property>
    name="<ww:property value="parameters['name']" />"
    <ww:property value="parameters['accesskey']">
        <ww:if test=".">
            <ww:property value="parameters['title']">
                <ww:if test=".">
                    title="<ww:text name="'AUI.form.custom.button.title.tooltip.plus.accesskey'">
                            <ww:param name="'value0'"><ww:property value="."/></ww:param>
                            <ww:param name="'value1'"><ww:property value="@modifierKey"/></ww:param>
                            <ww:param name="'value2'"><ww:property value="parameters['accesskey']"/></ww:param>
                        </ww:text>"
                </ww:if>
                <ww:else>
                    title="<ww:text name="'AUI.form.custom.button.title.accesskey.only'">
                        <ww:param name="'value0'"><ww:property value="@modifierKey"/></ww:param>
                        <ww:param name="'value1'"><ww:property value="parameters['accesskey']"/></ww:param>
                    </ww:text>"
                </ww:else>
            </ww:property>
        </ww:if>
        <ww:else>
            <ww:property value="parameters['title']">
                <ww:if test=".">
                    title="<ww:property value="."/>"
                </ww:if>
            </ww:property>
        </ww:else>
    </ww:property>
    <ww:property value="parameters['type']">
        <ww:if test=".">
            type="<ww:property value="."/>"
        </ww:if>
        <ww:else>
            type="button"
        </ww:else>
    </ww:property>
    <ww:property value="parameters['text']">
        <ww:if test=".">value="<ww:property value="."/>"</ww:if>
    </ww:property>
    />