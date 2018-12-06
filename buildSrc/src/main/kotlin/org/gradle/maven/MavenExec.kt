package org.gradle.maven

import org.gradle.api.attributes.Attribute
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.kotlin.dsl.listProperty
import org.gradle.process.CommandLineArgumentProvider
import java.lang.IllegalStateException

open class MavenExec : Exec {
    constructor() : super() {
        group = "Maven tasks"
        setExecutable(ExecutableProvider())
        argumentProviders.add(CommandLineArgumentProvider {
            tasks.get()
        })
    }

    @InputFiles
    fun getMavenDistribution() = project.configurations.getByName("maven").incoming.artifactView {
        attributes {
            attribute(Attribute.of("artifactType", String::class.java), "exploded")
        }
    }.files

    val mavenHome by lazy {
        getMavenDistribution().singleFile
    }

    @get:Input
    val tasks = project.objects.listProperty<String>().also {
        it.set(mutableListOf(name))
    }

    inner class ExecutableProvider {
        val mavenExtension by lazy {
            project.extensions.findByType(MavenExtension::class.java)
        }

        override
        fun toString() = "${mavenHome}/apache-maven-${mavenExtension!!.version}/bin/mvn"
    }
}