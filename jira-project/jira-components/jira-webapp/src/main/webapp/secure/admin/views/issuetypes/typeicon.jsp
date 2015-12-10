<ww:if test="typeEnabled == true">
    <ui:radio label="text('admin.issuesettings.issuetypes.type')" name="'style'" list="typesList" listKey="'id'" listValue="'name'"  value="/style">
        <ui:param name="'summary'" value="'description'"/>
        <ui:param name="'mandatory'">true</ui:param>
        <ui:param name="'class'">standardField</ui:param>
        <ui:param name="'description'">
            <ww:text name="'admin.issuesettings.issuetypes.type.description'"/>
        </ui:param>
    </ui:radio>
</ww:if>

<ww:if test="iconEnabled == true">
    <ui:component label="text('admin.common.phrases.icon.url')" name="'iconurl'" template="textimagedisabling.jsp">
        <ui:param name="'imagefunction'">openWindow()</ui:param>
        <ui:param name="'class'">standardField</ui:param>
        <ui:param name="'mandatory'">true</ui:param>
        <ui:param name="'description'"><ww:text name="'admin.issuesettings.issuetypes.icon.url.description'"/></ui:param>
    </ui:component>
</ww:if>
