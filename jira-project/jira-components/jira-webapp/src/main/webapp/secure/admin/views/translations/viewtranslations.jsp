<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ul>
    <li>
        <ww:text name="'admin.issuesettings.translations.view'">
            <ww:param name="'value0'"><b><a href="<ww:property value="redirectPage" />"></ww:param>
            <ww:param name="'value1'"></a></b></ww:param>
            <ww:param name="'value2'"><ww:property value="/linkName" /></ww:param>
        </ww:text>
    </li>
</ul>
<p><ww:text name="'admin.issuesettings.translations.page.description'"><ww:param name="'value0'">'<b><ww:property value="/issueConstantName" /></b>'</ww:param></ww:text></p>
<form id="changeTranslationLocale" class="aui long-label" name="refresh" method="post" action="ViewTranslations!default.jspa">
    <div class="form-body">
        <div class="field-group">
            <ui:select id="selectNewLocale" label="text('admin.issuesettings.translations.view.locale.translations')" name="'selectedLocale'" theme="'aui'" list="installedLocales" listKey="'key'" listValue="'value'" />
            <input class="aui-button" type="submit" name="<ww:text name="'admin.issuesettings.translations.view.button'"/>" value="View" />
            <ui:component name="'issueConstantType'" template="hidden.jsp" theme="'aui'"  />
        </div>
    </div>
</form>
<form name="update" method="post" action="ViewTranslations.jspa">
    <table class="aui">
        <thead>
            <tr>
                <th width="25%">
                    <ww:property value="/issueConstantName" />
                </th>
                <th width="75%" style="padding-left:120px;">
                    <ww:text name="'admin.issuesettings.translation'"/> <ww:text name="'admin.issuesettings.translations.locale'"/>: <ww:property value="/selectedLocaleDisplayName" />
                </th>
            </tr>
        </thead>
        <tbody>
            <ww:property value="/issueConstants">
            <%/*counter used to alternate background row colors*/%>
            <ww:bean name="'webwork.util.Counter'" id="rowCount">
                <ww:param name="'wrap'" value="true"/>
                <ww:param name="'last'" value="2"/>
            </ww:bean>
            <ww:iterator value="." status="'status'">
                <tr>
                    <td>
                        <ww:if test="/issueConstantTypeStatus == true">
                            <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                <ww:param name="'issueStatus'" value="."/>
                                <ww:param name="'isSubtle'" value="false"/>
                                <ww:param name="'isCompact'" value="false"/>
                            </ww:component>
                        </ww:if>
                        <ww:else>
                            <ww:if test="../iconEnabled == true">
                                <ww:component name="../fieldId" template="constanticon.jsp">
                                    <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                    <ww:param name="'iconurl'" value="iconUrl" />
                                    <ww:param name="'alt'"><ww:property value="name" /></ww:param>
                                </ww:component>
                            </ww:if>
                            <strong><ww:property value="./name" /></strong>
                        </ww:else>
                        <div class="description<ww:if test="../iconEnabled == true"> icon-indent</ww:if>"><ww:property value="./description" /></div>
                    </td>
                    <td>
                        <table class="blank">
                        <ui:textfield label="text('common.words.name')" name="/nameKey(.)" size="'60'" value="/translatedName(.)">
                        </ui:textfield>
                        <ui:textfield label="text('common.words.description')" name="/descKey(.)" size="'60'" value="/translatedDesc(.)">
                        </ui:textfield>
                        </table>
                    </td>
                </tr>
            </ww:iterator>
            </ww:property>
            <ui:component name="'issueConstantType'" template="hidden.jsp" theme="'single'"  />
            <ui:component name="'selectedLocale'" template="hidden.jsp" theme="'single'"  />
            <ui:component name="'atl_token'" template="hidden.jsp" theme="'single'" value="/xsrfToken" />
        </tbody>
    </table>
    <div class="buttons-container">
        <input class="aui-button" type="submit" name="update" value="<ww:text name="'common.forms.update'"/>"/>
    </div>
</form>

