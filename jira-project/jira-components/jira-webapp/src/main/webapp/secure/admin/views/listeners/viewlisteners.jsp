<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.listeners.listeners'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="listeners"/>
</head>

<body>

<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.listeners.listeners'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">listeners</page:param>
    <page:param name="description"><ww:text name="'admin.listeners.description'"/>
        <p>
        <ww:text name="'admin.listeners.some.listeners.are.internal'"/>
    </page:param>
</page:applyDecorator>


<table class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th width="30%">
                <ww:text name="'admin.common.phrases.name.class'"/>
            </th>
            <th>
                <ww:text name="'admin.common.words.properties'"/>
            </th>
            <th width="10%">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
	<tbody>
	<ww:iterator value="listeners" status="'status'">
        <tr>
            <td>
                <ww:if test="listenerImplementation(.)/internal == true">
                    <div class="hiddenField">
                        <b><ww:property value="string('name')"/></b> (<ww:text name="'admin.listeners.internal'"/>)
                        <div class="description"><ww:property value="string('clazz')"/></div>
                    </div>
                </ww:if>
                <ww:else>
                    <b><ww:property value="string('name')"/></b>
                    <div class="description"><ww:property value="string('clazz')"/></div>
                </ww:else>
            </td>
            <td>
                <ww:property value="propertySet(.)/keys('',5)">
                    <ww:if test=". != null">
                        <ul>
                        <ww:iterator value=".">
                            <li><b><ww:property value="." /></b> <ww:property value="propertySet(../..)/string(.)"/></li>
                        </ww:iterator>
                        </ul>
                    </ww:if>
                </ww:property>
            </td>
            <td>
                <ul class="operations-list">
                    <ww:if test="/listenerEditable(.) == true">
                        <li><a id="<ww:property value="'edit_' + long('id')"/>" href="<ww:url page=" EditListener!default.jspa"><ww:param name="'id'" value="long('id')"/></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                    </ww:if>
                    <ww:if test="/listenerDeletable(.) == true">
                        <li><a id="<ww:property value="'delete_' + long('id')"/>" href="<ww:url page="ViewListeners!delete.jspa"><ww:param name="'delete'" value="long('id')"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                    </ww:if>
                </ul>
            </td>
        </tr>
	</ww:iterator>
    </tbody>
</table>


<p>
<script type="text/javascript">
  function showListeners() {
        var listenersDiv = document.getElementById("builtinListeners");
        var listenersArrow = document.getElementById("builtinListenersArrow");
        if (listenersDiv.style.display == 'none') {
          listenersDiv.style.display = '';
          listenersArrow.src='<%= request.getContextPath() %>/images/icons/navigate_down.gif';
        } else {
          listenersDiv.style.display='none';
          listenersArrow.src='<%= request.getContextPath() %>/images/icons/navigate_right.gif';
        }
  }
  function setListener(clazz) {
        var classField = document.getElementById("listenerClass");
        var nameField = document.getElementById("listenerName");
        classField.value = clazz;
        nameField.focus();
  }
</script>

<page:applyDecorator name="jiraform">
	<page:param name="action">ViewListeners.jspa</page:param>
	<page:param name="submitId">add_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
	<page:param name="width">100%</page:param>
	<page:param name="title"><ww:text name="'admin.listeners.add.listener'"/></page:param>
	<page:param name="helpURL">listeners</page:param>
<%--	<page:param name="helpDescription">with Listeners</page:param>--%>
	<page:param name="description">
  <ww:text name="'admin.listeners.add.instruction'"/></page:param>

	<ui:textfield label="text('common.words.name')" name="'name'" size="'30'">
        <ui:param name="'cssId'">listenerName</ui:param>
    </ui:textfield>
	<ui:textfield label="text('admin.listeners.class')" name="'clazz'" size="'60'">
        <ui:param name="'cssId'">listenerClass</ui:param>
        <ui:param name="'description'">

        <img id="builtinListenersArrow" src="<%= request.getContextPath() %>/images/icons/navigate_right.gif" width="8" height="8" border="0" alt="">
        <a href="#"  onclick="showListeners(); return false;"><ww:text name="'admin.listeners.built.in.listeners'"/></a>

              <div id="builtinListeners" style="display: none">
                  <ul>
                      <li> <a href="#" onclick="setListener('com.atlassian.jira.event.listeners.mail.DebugMailListener');"><ww:text name="'admin.listeners.debug.maillistener'"/></a> </li>
                      <li> <a href="#" onclick="setListener('com.atlassian.jira.event.listeners.DebugListener');"><ww:text name="'admin.listeners.print.events.to.sout'"/> </a> </li>
                      <li> <a href="#" onclick="setListener('com.atlassian.jira.event.listeners.DebugParamListener');"><ww:text name="'admin.listeners.print.events.to.sout.with.params'"/></a> </li>
                  </ul>
              </div>
        </ui:param>
    </ui:textfield>

</page:applyDecorator>
</p>

</body>
</html>
