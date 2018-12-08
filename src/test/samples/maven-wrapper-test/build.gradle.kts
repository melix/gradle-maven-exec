import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.version
import org.gradle.plugins.maven.exec.MavenExec

plugins {
   id("gradle.maven.exec") version "0.0.7"
}

val clean by tasks.registering(MavenExec::class)

val install by tasks.registering(MavenExec::class)
