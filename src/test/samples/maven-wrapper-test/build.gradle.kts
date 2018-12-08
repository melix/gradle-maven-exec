import org.gradle.plugins.maven.exec.MavenExec

plugins {
   id("gradle.maven.exec") version "0.1.0"
}

val clean by tasks.registering(MavenExec::class)

val install by tasks.registering(MavenExec::class)
