plugins { id("io.vacco.oss.gitflow") version "1.0.1" }

group = "io.vacco.shax"
version = "2.0.16.0.4.3"

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  addJ8Spec()
  addClasspathHell()
  sharedLibrary(true, false)
}

val api by configurations

dependencies { api("org.slf4j:slf4j-api:2.0.16") }

tasks.processResources {
  filesMatching("io/vacco/shax/version") {
    expand("projectVersion" to version)
  }
}
