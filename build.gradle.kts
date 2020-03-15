plugins {
    kotlin("jvm") version "1.3.70"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

dependencies{
    implementation("com.typesafe.akka","akka-actor_2.13","2.6.3")
    implementation("com.typesafe.akka","akka-stream_2.13","2.6.3")
    implementation("com.typesafe.akka","akka-http_2.13","10.1.11")
}