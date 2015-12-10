<%-- show always only comptact status --%>
<ww:property value="./statusObject">
    <%-- handle both objects and genericvalues --%>
    <ww:if test=". != null">
        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
            <ww:param name="'issueStatus'" value="."/>
            <ww:param name="'isSubtle'" value="true"/>
            <ww:param name="'isCompact'" value="false"/>
            <ww:param name="'maxWidth'" value="short"/>
        </ww:component>
    </ww:if>
    <ww:else>
        <ww:property value="/constantsManager/statusObject(string('status'))">
            <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                <ww:param name="'issueStatus'" value="."/>
                <ww:param name="'isSubtle'" value="true"/>
                <ww:param name="'isCompact'" value="false"/>
                <ww:param name="'maxWidth'" value="short"/>
            </ww:component>
        </ww:property>
    </ww:else>
</ww:property>

