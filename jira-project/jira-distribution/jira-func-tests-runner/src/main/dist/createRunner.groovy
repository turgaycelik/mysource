/*
 * Generates a ZIP distribution for running the func tests.
 */

def runnerName = "${project.build.finalName}-dist"
def zipFilename = "${runnerName}.zip"
def pomPath = "${project.build.directory}/${runnerName}.xml"

def jiraFuncTestsJar = "jira-func-tests-${project.version}.jar"

//Some files we don't want to copy over from the func test.
def excludesFromFuncTest = ["containers.properties", "localtest.properties"]

// create a pom.xml for running
new File(pomPath).with {
  parentFile.mkdirs()

  def template = new XmlSlurper().parse(new File("${project.file.parent}/src/main/dist/dist-pom.xml"))
  template.version.replaceNode { version "${project.version}" }

  // dump all <properties> into the new POM
  project.properties.each { key, value ->
    template.properties.appendNode {
      "$key" "$value"
    }
  }

  // add dependencies into new POM
  project.testClasspathElements.each { path ->
    File file = new File(path)
    if (file.isFile() && file.name != jiraFuncTestsJar) {
      template.dependencies.appendNode {
        dependency {
          groupId "com.atlassian.jira"
          artifactId "${file.name}"
          version "${project.version}"
          scope "system"
          systemPath "\${basedir}/lib/${file.name}"
        }
      }
    }
  }

  // add additional test resource dir
  template.build.appendNode {
    testResources {
      testResource {
        directory '${basedir}/src/test/resources'
        filtering 'true'
      }
      testResource {
        directory '${basedir}/jira-func-tests-classes'
        filtering 'false'
        excludes {
          excludesFromFuncTest.each { ex ->
            exclude ex
          }
        }
      }
    }
  }

  def outputBuilder = new groovy.xml.StreamingMarkupBuilder()
  text = outputBuilder.bind{ mkp.yield template }

  log.info "Created ${it.name}"
}

// create a distribution ZIP
log.info "Creating $zipFilename"

def runnerZip = new File("${project.build.directory}/${zipFilename}")
ant.zip(destfile: runnerZip.absolutePath, update: false) {
  def baseDir = new File("${project.basedir}/../..").canonicalPath
  def funcTestRunnerBaseDir = "${project.file.parentFile.canonicalPath}"

  def dirMappings = [
          "${funcTestRunnerBaseDir}/target/classes": "src/main/resources",
          "${funcTestRunnerBaseDir}/target/test-classes": "src/test/resources"
  ]

  project.testClasspathElements.each { path ->

    File file = new File(path)
    if (file.name == jiraFuncTestsJar) {
      zipfileset(prefix: 'jira-func-tests-classes', src: file.absolutePath, excludes: excludesFromFuncTest.join(","))
    }
    else if (file.isDirectory()) {
      def modulePath = file.absolutePath.substring(baseDir.length()+1)
      zipfileset(prefix: dirMappings.get("${file.absolutePath}", modulePath), dir: file.absolutePath)
    }
    else {
      zipfileset(prefix: 'lib', file: file.absolutePath)
    }
  }

  // wars for deploying with cargo
  project.artifacts.each { artifact ->
    if (artifact.type == 'war') {
      // explode the war
      zipfileset(prefix: 'atlassian-jira', src: artifact.file.absolutePath)
    }
  }

  // additional test sources (for Hallelujah)
  zipfileset(prefix: 'src/test', dir:"${project.file.parent}/src/main/dist-test-src")

  zipfileset(fullpath: 'pom.xml', file: pomPath)
}
