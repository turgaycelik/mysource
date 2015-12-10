//
//
// Creates the 'JIRA' exploded artifact that's used to run JIRA in IDEA.
//
//


/*
 * extension methods for the artifact sets. this allows us to use a DSL-like syntax below.
 */
private class ArtifactsCategory
{
  static final JIRA_PREFIX = "com.atlassian.jira:"
  static final LANG_PACK_PREFIX = JIRA_PREFIX + "jira-languages"

  static Set jiraModules(Set artifacts)
  {
    return artifacts.findAll { it.id.startsWith JIRA_PREFIX } - languagePacks(artifacts)
  }

  static Set thirdPartyJars(Set artifacts)
  {
    return artifacts.findAll { !(it.id.startsWith(JIRA_PREFIX)) }
  }

  static Set languagePacks(Set artifacts)
  {
    return artifacts.findAll { it.id.startsWith LANG_PACK_PREFIX }
  }
}

/**
 * Writes an XML description of the exploded JIRA WAR suitable for use in IntelliJ IDEA into the file with the given
 * name. If the file does not exist, it is created (including any path components).
 *
 * @param explodedWarPath a String containing the path where the exploded WAR will live
 * @param jiraXmlPath a String containing the path to write the artifact definition to
 */
private void writeJiraXmlFile(String explodedWarPath, String jiraXmlPath)
{
  // build up the XML into a string writer
  def writer = new StringWriter()
  def xml = new groovy.xml.MarkupBuilder(writer)

  xml.component(name: 'ArtifactManager') {
    artifact(type: 'exploded-war', name: 'JIRA') {
      'output-path'(explodedWarPath)
      root(id: 'root') {
        element(id: 'directory', name: 'WEB-INF') {
          Set artifacts = project.runtimeArtifacts
          use(ArtifactsCategory) {
            // contents of WEB-INF/classes
            element(id: 'directory', name: 'classes') {
              // all JIRA modules (doesn't include language packs)
              artifacts.jiraModules().each {
                element(id: 'module-output', name: "$it.artifactId")
              }

              // the bundled plugins ZIP
              element(id: 'file-copy', path: '$PROJECT_DIR$/jira-components/jira-plugins/jira-bundled-plugins/target/atlassian-bundled-plugins.zip')
            }

            // contents of WEB-INF/lib
            element(id: 'directory', name: 'lib') {

              artifacts.thirdPartyJars().each {
                element(id: 'library', level: 'project', name: "Maven: ${it.groupId}:${it.artifactId}:${it.baseVersion}")
              }

              artifacts.languagePacks().each {
                def language = it.artifactId + "-" + it.baseVersion + "-" + it.classifier
                element(id: 'file-copy', path: '$PROJECT_DIR$' + "/jira-components/jira-languages/target/${language}.jar")
              }

            }

          }
        }

        // atlassian-jira-webapp resources
        element(id: 'javaee-facet-resources', facet: 'atlassian-jira-webapp/web/Web')

        // include the screenshot applet in /secure/applet
        element(id: 'directory', name: 'secure') {
          element(id: 'directory', name: 'applet') {
            element(id: 'file-copy', path: '$PROJECT_DIR$/jira-components/jira-screenshot-applet/target/screenshot.jar')
          }
        }
      }
    }
  }

  // now actually write the file
  new File(jiraXmlPath).with {
    parentFile.mkdirs()
    write(writer.toString())

    log.info "Wrote JIRA artifact configuration to ${it.canonicalPath}"
  }
}

/**
 * Deletes all JARs from the given directory that are not in the project's "runtimeArtifacts" set. This happens if
 * dependencies are removed, or their versions changed.
 *
 * @param directory the directory to delete from
 */
private void deleteUnusedJars(File directory)
{
  if (!directory.exists())
  {
    return
  }

  use(ArtifactsCategory) {
    Set runtimeArtifacts = project.runtimeArtifacts

    def thirdPartyJars = runtimeArtifacts.thirdPartyJars().collect { it.file.name }
    def languagePackJars = runtimeArtifacts.languagePacks().collect { "${it.artifactId}.jar".toString() }

    def jarsInDir = directory.list() as Set
    def jarsInPom = (thirdPartyJars + languagePackJars) as Set

    def jarsToDelete = jarsInDir.findAll { !jarsInPom.contains(it) }
    if (jarsToDelete)
    {
      log.info "Removing unreferenced files in ${directory.canonicalPath}..."
      ant.delete(verbose: true) {
        fileset(dir: directory.absolutePath) {
          jarsToDelete.each {
            include(name: it)
          }
        }
      }
    }
  }
}

// 0. paths, etc
final String explodedWarPath = 'classes/artifacts/jira';
final String jiraXmlPath = '.idea/artifacts/JIRA.xml'

// 1. create the artifact XML
writeJiraXmlFile("\$PROJECT_DIR\$/${explodedWarPath}", jiraXmlPath)

// 2. delete old jars from WEB-INF/lib
deleteUnusedJars(new File("${project.basedir}/../../${explodedWarPath}/WEB-INF/lib"))
