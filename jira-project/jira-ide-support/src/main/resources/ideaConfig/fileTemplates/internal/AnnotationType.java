#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end

#parse("JavaDoc New.java")
public @interface ${NAME} {
}