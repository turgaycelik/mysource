<%--
  --
  -- Required Parameters:
  --   * label     - The description that will be used to identfy the control.
  --   * name      - The name of the attribute to put and pull the result from.
  --                 Equates to the NAME parameter of the HTML tag SELECT.
  --   * list      - Iterator that will provide the options for the control.
  --                 Equates to the HTML OPTION tags in the SELECT.
  --   * listKey   - Where to get the values for the OPTION tag.  Equates to
  --                 the VALUE parameter of the OPTION tag.
  --   * listValue - The value displayed by the control.  Equates to the body
  --                 of the HTML OPTION tag.
  --   * listClass - The value displayed by the control.  Equates to the class
  --                 of the HTML OPTION tag which is used in the cascading functionality.
  --   * cascadeFrom  - The name of the select which causes this select to cascade, there must
                        be a relationship between this named elements values and the classes used
                        in the options of this SELECT tag.
  --%>

<%@ taglib uri="webwork" prefix="ww" %>

<ww:if test="!@summaryCounter">
    <ww:property value="0" id="summaryCounter" />
</ww:if>

<ww:property value="parameters['summary']" id="summary" />
<fieldset class="hidden parameters">
    <ww:property value="parameters['headerrow']">
        <ww:if test=". && . != ''">
            <input type="hidden" class="list" title="summaries" value="<ww:property value="parameters['headersummary']"/>"/>
        </ww:if>
    </ww:property>
    <ww:property value="parameters['headerrow2']">
        <ww:if test=". && . != ''">
            <input type="hidden" class="list" title="summaries" value="<ww:property value="parameters['headersummary2']"/>"/>
        </ww:if>
    </ww:property>
    <ww:iterator value="parameters['list']">
        <input type="hidden" class="list" title="summaries" value="<ww:property value="{parameters['summary']}"/>"/>
    </ww:iterator>
    <input type="hidden" title="paramName" value="<ww:property value="parameters['name']"/>"/>
    <input type="hidden" title="paramCascadeFrom" value="<ww:property value="parameters['cascadeFrom']"/>"/>
</fieldset>
<%-- Is this still used?! --%>
<ww:if test="@summary">
    <%
        Integer counter = (Integer) request.getAttribute("summaryCounter");
        if (counter != null)
        {
            int i = counter.intValue();
            int value =  i + 1;
            request.setAttribute("summaryCounter", new Integer(value));
        }
    %>

    <script type="text/javascript">
        function changeDescription<ww:property value="@summaryCounter" />(dropdown)
        {
            document.getElementById(AJS.params.paramName + "_summary").innerHTML =  AJS.params.summaries[dropdown.selectedIndex] ? AJS.params.summaries[dropdown.selectedIndex] : "";
        }

        AJS.$(function() {
            changeDescription<ww:property value="@summaryCounter" />(document.getElementById(AJS.params.paramName + "_select"));
        });
    </script>
</ww:if>

<script type="text/javascript">
    AJS.$(function() {
        dynamicSelect(AJS.params.paramCascadeFrom + "_select", AJS.params.paramName + "_select");
    });
</script>

<jsp:include page="/template/standard/controlheader.jsp"/>
<select name='<ww:property value="parameters['name']"/>' id='<ww:property value="parameters['name']"/>_select'>
    <ww:iterator value="parameters['list']">
        <option value='<ww:property value="{parameters['listKey']}"/>' class='<ww:property value="{parameters['listClass']}"/>'
                <ww:if test="{parameters['listKey']} == parameters['value']">SELECTED</ww:if>
                >
            <ww:if test="parameters['internat'] == true"><ww:text name="{parameters['listValue']}"/>
            </ww:if>
            <ww:else><ww:property value="{parameters['listValue']}"/></ww:else>
        </option>
    </ww:iterator>
</select>

<ww:if test="@summary && !parameters['description']"><br /></ww:if>
<span class="selectDescription" id="<ww:property value="parameters['name']"/>_summary"></span>
<jsp:include page="/template/standard/controlfooter.jsp"/>
