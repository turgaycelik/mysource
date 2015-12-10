//
//
// This script cleans out stale libraries from the jira/WEB-INF/lib in the
// Maven build directory. This makes it safe to rebuild the atlassian-jira-webapp
// module without having to do 'mvn clean' first, even if the dependencies have
// changed (i.e. you will not have duplicate JARs on the classpath).
//
// See http://docs.codehaus.org/display/GMAVEN/Executing+Groovy+Code
//
//

def webInfLib = new File("${project.build.directory}/${project.build.finalName}/WEB-INF/lib")
def jarsInWebInfLib = webInfLib.list() as Set
def jarsInPom = project.runtimeArtifacts.collect { it.file.name } as Set

def jarsToDelete = jarsInWebInfLib.findAll { !jarsInPom.contains(it) }
if (jarsToDelete)
{
  log.info "Removing stale files in WEB-INF/lib..."
  ant.delete(verbose: true) {
    fileset(dir: webInfLib.absolutePath) {
      jarsToDelete.each {
        include(name: it)
      }
    }
  }
}
