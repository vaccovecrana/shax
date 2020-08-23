plugins { `java-library`; jacoco; `maven-publish` }
repositories { jcenter() }

group = "io.vacco.shax"
version = "1.7.30.0.0.3"

java {
  withJavadocJar()
  withSourcesJar()
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation("org.slf4j:slf4j-api:1.7.30")
  testImplementation("io.github.j8spec:j8spec:3.0.0")
}

tasks.withType<Test> {
  this.testLogging {
    this.showStandardStreams = true
  }
}

object Publishing {
  const val gitUrl = "https://github.com/vaccovecrana/shax.git"
  const val siteUrl = "https://github.com/vaccovecrana/shax"
  const val libraryDesc = "A minimal JSON structured logging backend for SLF4j"
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      versionMapping {
        usage("java-api") { fromResolutionOf("runtimeClasspath") }
        usage("java-runtime") { fromResolutionResult() }
      }
      pom {
        name.set(project.name)
        description.set(Publishing.libraryDesc)
        url.set(Publishing.siteUrl)
        licenses {
          license {
            name.set("Apache License, Version 2.0")
            url.set("https://opensource.org/licenses/Apache-2.0")
          }
        }
        developers {
          developer {
            id.set("jjzazuet")
            name.set("Jesus Zazueta")
            email.set("jjzazuet@vacco.io")
          }
        }
        scm {
          connection.set(Publishing.gitUrl)
          developerConnection.set(Publishing.gitUrl)
          url.set(Publishing.siteUrl)
        }
      }
    }
  }
  repositories {
    maven {
      name = "bintray"
      val bintrayUser = System.getenv("BINTRAY_USER")
      setUrl("https://api.bintray.com/maven/vaccovecrana/vacco-oss/${project.name}")
      credentials {
        username = bintrayUser
        password = System.getenv("BINTRAY_KEY")
      }
    }
  }
}
