plugins { id("io.vacco.oss.gitflow") version "1.8.3" }

group = "io.vacco.shax"
version = "2.0.17.2"

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  addJ8Spec()
  addClasspathHell()
  sharedLibrary(true, false)
}

val api by configurations

dependencies { api("org.slf4j:slf4j-api:2.0.17") }

tasks.processResources {
  filesMatching("io/vacco/shax/version") {
    expand("projectVersion" to version)
  }
}
