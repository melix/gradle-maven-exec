import org.gradle.plugins.maven.exec.MavenExec

plugins {
   id("gradle.maven.exec") version "0.0.1"
}

maven {
   version = "3.5.4"
}

val clean by tasks.registering(MavenExec::class)

val install by tasks.registering(MavenExec::class)
