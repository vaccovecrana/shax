plugins {
  java
  jacoco
}

repositories { jcenter() }

group = "io.vacco.shax"
version = "1.7.30.0"

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation("org.slf4j:slf4j-api:1.7.30")
  testImplementation("io.github.j8spec:j8spec:3.0.0")
}
