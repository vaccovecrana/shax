plugins {
  id("io.vacco.common-build") version "0.5.1"
}

group = "io.vacco.shax"
version = "1.7.30.0.0.6"

configure<io.vacco.common.CbPluginProfileExtension> {
  addJ8Spec()
  addPmd()
  addSpotBugs()
  addClasspathHell()
  setPublishingUrlTransform { repo -> "${repo.url}/${project.name}" }
  sharedLibrary()
}

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

val api by configurations

dependencies {
  api("org.slf4j:slf4j-api:1.7.30")
}

/*
tasks.withType<Test> {
  extensions.configure(JacocoTaskExtension::class) {
    output = JacocoTaskExtension.Output.TCP_CLIENT
    address = "localhost"
    port = 6300
    sessionId = "test"
  }
}
*/
