package org.gradle.plugins.maven.exec

import org.gradle.api.attributes.Attribute
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.listProperty
import org.gradle.process.CommandLineArgumentProvider

open class MavenExec : Exec {
    constructor() : super() {
        group = "Maven tasks"
        setExecutable(ExecutableProvider())
        argumentProviders.add(CommandLineArgumentProvider {
            goals.get()
        })
        doFirst {
            project.logger.lifecycle("Executing Maven goals ${goals.get()}")
        }
    }

    @Option(option = "maven-args", description = "argument to forward to Maven execution")
    fun setMavenArgs(args: String) {
        setArgs(args.split("\\s".toRegex()))
    }

    @InputFiles
    fun getMavenDistribution() = project.configurations.getByName("maven").incoming.artifactView {
        attributes {
            attribute(Attribute.of("artifactType", String::class.java), "exploded")
        }
    }.files

    private
    val mavenHome by lazy {
        getMavenDistribution().singleFile
    }

    @get:Input
    val goals = project.objects.listProperty<String>().also {
        it.set(mutableListOf(name))
    }

    inner class ExecutableProvider {
        private
        val mavenExtension by lazy {
            project.extensions.findByType(MavenExtension::class.java)
        }

        override
        fun toString(): String {
            val osName: String = System.getProperty("os.name").toLowerCase()
            val mvnwBatFile = project.layout.projectDirectory.file("mvnw.bat").asFile
            val mvnwBinFile = project.layout.projectDirectory.file("mvnw").asFile

            return if (osName.contains("win") && mvnwBatFile.canExecute()) {
                mvnwBatFile.canonicalPath
            } else if (mvnwBinFile.canExecute()) {
                mvnwBinFile.canonicalPath
            } else {
                "$mavenHome/apache-maven-${mavenExtension!!.version}/bin/mvn"
            }
        }
    }
}
