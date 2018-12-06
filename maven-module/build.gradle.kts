import org.gradle.maven.MavenExec

plugins {
   `maven-embedder`
}

maven {
   version = "3.5.4"
}

val clean by tasks.registering(MavenExec::class)

val install by tasks.registering(MavenExec::class)