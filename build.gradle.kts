plugins { id("io.vacco.oss.gitflow") version "1.9.0" }

var slf4j = "2.0.18"

group = "io.vacco.shax"
version = "${slf4j}.0"

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  addJ8Spec()
  addClasspathHell()
  sharedLibrary(true, false)
}

val api by configurations

dependencies { api("org.slf4j:slf4j-api:${slf4j}") }

tasks.processResources {
  filesMatching("io/vacco/shax/version") {
    expand("projectVersion" to version)
  }
}
