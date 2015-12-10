/**
 * This script generates the 'jira.idea.properties' file if it is not present, attempting to use ~/jira.idea.properties
 * if present.
 */
String userHome = session.executionProperties['user.home']
String resourcesDir = "${project.basedir}/src/main/resources"
String propFilename = "${resourcesDir}/jira.idea.properties"

File readPropertiesFile = new File(propFilename)
if (!readPropertiesFile.exists()) {
  String copyFrom = "${resourcesDir}/jira.idea.template"

  // try ~/jira.idea.properties
  String homePropFilename = "${userHome}/jira.idea.properties"
  if (new File(homePropFilename).exists()) {
    log.info "Found ${homePropFilename}..."
    copyFrom = homePropFilename
  }

  // copy the defaults
  log.info "Copying default properties from ${copyFrom}"
  ant.copy(file: copyFrom, tofile: readPropertiesFile.canonicalPath)
}

log.info "Reading properties from ${propFilename}"
readPropertiesFile.withInputStream { inputStream ->
  project.properties.load(inputStream)
}

