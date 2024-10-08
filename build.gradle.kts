plugins { id("io.vacco.oss.gitflow") version "1.0.1" }

group = "io.vacco.shax"
version = "2.0.16.0.1.2"

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

dependencies { api("org.slf4j:slf4j-api:2.0.16") }

// Add for OTEL testing
// IO_VACCO_SHAX_DEVMODE=true;IO_VACCO_SHAX_JULOUTPUT=true;OTEL_EXPORTER_OTLP_ENDPOINT=https://otlp-server;OTEL_LOGS_EXPORTER=otlp;OTEL_RESOURCE_ATTRIBUTES=service.name=shax-dev
//tasks.withType<Test>().all {
//  jvmArgs("-javaagent:/Users/jjzazuet/Desktop/opentelemetry-javaagent.jar")
//}