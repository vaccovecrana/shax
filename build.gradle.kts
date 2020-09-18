buildscript {
  repositories {
    jcenter(); gradlePluginPortal()
    maven { name = "VaccoOss"; setUrl("https://dl.bintray.com/vaccovecrana/vacco-oss") }
  }
  dependencies { classpath("io.vacco:common-build-gradle-plugin:0.5.0") }
}

apply{plugin(io.vacco.common.CbPlugin::class.java)}

group = "io.vacco.shax"
version = "1.7.30.0.0.5"

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

val implementation by configurations
dependencies {
  implementation("org.slf4j:slf4j-api:1.7.30")
}
