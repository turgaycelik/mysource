// --------------------------------------------------------------------------------------------------
// This file contains shortcuts common to all plan templates for the Java 8 project
// --------------------------------------------------------------------------------------------------
java8Project() {
    project(key: 'JJDK8', name: 'JIRA JDK8 Plans', description: 'All plans that run on Java 8')
}

java8Labels() {
    label(name: 'plan-templates')
    label(name: 'java8')
}
