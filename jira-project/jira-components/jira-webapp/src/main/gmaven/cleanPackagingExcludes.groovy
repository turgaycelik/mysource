//
//
// This script cleans files from the exploded WAR directory that are configured as <packagingExcludes> in the
// maven-war-plugin. The reason we do this is because IDEA likes to use the exploded WAR to run JIRA, and we don't want
// it to find JARs in WEB-INF/lib that should have been excluded.
//
// See http://docs.codehaus.org/display/GMAVEN/Executing+Groovy+Code
//
//

final File warRoot = new File("${project.build.directory}/${project.build.finalName}")

// parse the <configuration> element for the maven-war-plugin
def warPlugin = project.build.plugins.find { it.key == 'org.apache.maven.plugins:maven-war-plugin' }
def warConfiguration = warPlugin?.configuration ? new XmlParser().parseText(warPlugin.configuration as String) : null

def packagingExcludes = warConfiguration?.packagingExcludes?.text().tokenize(',')
if (packagingExcludes)
{
  log.info "Removing <packagingExcludes> files from exploded WAR..."
  ant.delete(verbose: true) {
    fileset(dir: warRoot) {
      packagingExcludes.each {
        include(name: it)
      }
    }
  }
}
