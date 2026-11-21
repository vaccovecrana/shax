plugins { id("io.vacco.oss.gitflow") version "1.11.0" }

var slf4j = "2.0.20"

group = "io.vacco.shax"
version = "${slf4j}.2"

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  orgConfigUrl("https://raw.githubusercontent.com/vaccovecrana/org-config/refs/heads/main/vacco-oss.json")
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

tasks.register<JavaExec>("benchLogger") {
  group = "verification"
  description = "Runs the ShLogger level-resolution benchmark"
  classpath = sourceSets["test"].runtimeClasspath
  mainClass.set("io.vacco.shax.test.ShLoggerBench")
}
