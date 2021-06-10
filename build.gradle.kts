plugins { id("io.vacco.oss.gitflow") version "0.9.8" }

group = "io.vacco.shax"
version = "1.7.30.0.0.7"

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  addJ8Spec()
  addClasspathHell()
  sharedLibrary(true, false)
}

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

val api by configurations

dependencies { api("org.slf4j:slf4j-api:1.7.30") }
