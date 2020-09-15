buildscript {
  repositories {
    maven { name = "VaccoOss"; setUrl("https://dl.bintray.com/vaccovecrana/vacco-oss") }
  }
  dependencies { classpath("io.vacco.common:common-build:0.1.0") }
}

apply(from = project.buildscript.classLoader.getResource("io/vacco/common/java-library.gradle.kts").toURI())

group = "io.vacco.shax"
version = "1.7.30.0.0.4"

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

val implementation by configurations
dependencies { implementation("org.slf4j:slf4j-api:1.7.30") }
