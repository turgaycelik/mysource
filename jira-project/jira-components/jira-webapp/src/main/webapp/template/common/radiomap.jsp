<ww:iterator value="parameters['list']" status="'status'">
    <input type="radio" class="radio"
            <ww:if test="{parameters['listKey']} == '' && !parameters['nameValue']">checked="checked"</ww:if>
            <ww:if test="{parameters['listKey']} == parameters['nameValue']">checked="checked"</ww:if>
            <ww:if test="{parameters['listKey']} == parameters['checkRadio']">checked="checked"</ww:if>
           name="<ww:property value="parameters['name']"/>"
           value="<ww:property value="{parameters['listKey']}"/>"
           id="<ww:property value="parameters['name']" />_<ww:property value="{parameters['listKey']}"/>"
            <ww:property value="parameters['disabled']">
                <ww:if test="{parameters['disabled']}">DISABLED</ww:if>
            </ww:property>
            <ww:property value="parameters['tabindex']">
                <ww:if test=".">tabindex="<ww:property value="."/>"</ww:if>
            </ww:property>
            <ww:property value="parameters['onchange']">
                <ww:if test=".">onchange="<ww:property value="."/>"</ww:if>
            </ww:property>
            <ww:property value="parameters['onclick']">
                <ww:if test=".">onclick="<ww:property value="."/>"</ww:if>
            </ww:property>
            />
    <label for="<ww:property value="parameters['name']" />_<ww:property value="{parameters['listKey']}"/>">
        <ww:property value="{parameters['listValue']}" escape="false" />
    </label>
    <ww:property value="parameters['inline']">
        <ww:if test=". != true"><ww:if test="@status/last != true"><br /></ww:if></ww:if>
    </ww:property>
</ww:iterator>
