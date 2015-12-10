<%@ taglib uri="webwork" prefix="ww" %><%@ taglib uri="webwork" prefix="ui" %><%@ taglib uri="sitemesh-page" prefix="page" %><% response.setContentType("text/xml"); %>
<users><ww:iterator value="/users">
        <user>
            <name><ww:property value="/xmlEscape(./name)" escape="false" /></name>
            <fullname><ww:property value="/xmlEscape(./fullname)" escape="false" /></fullname>
            <email><ww:property value="/xmlEscape(./email)" escape="false" /></email>
            <properties><ww:iterator value="./userPropertyMap">
                <property>
                <key><ww:property value="/xmlEscape(./key)" escape="false" /></key>
                    <value><ww:property value="/xmlEscape(./value)" escape="false" /></value>
                </property></ww:iterator>
            </properties>
        </user></ww:iterator>
</users>