package org.gradle.plugins.maven.exec

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.transform.ArtifactTransform
import org.gradle.api.attributes.Attribute
import java.io.File
import java.util.zip.ZipFile

class MavenExecPlugin : Plugin<Project> {
    override
    fun apply(project: Project) = project.run {

        val extension = extensions.create("maven", MavenExtension::class.java)

        val mavenConfiguration = configurations.create("maven") {
            isCanBeConsumed = false
            isCanBeResolved = true
        }

        afterEvaluate {
            repositories.maven {
                url = uri(extension.repositoryUrl)
            }
        }

        dependencies.registerTransform {
            from.attribute(Attribute.of("artifactType", String::class.java), "zip")
            to.attribute(Attribute.of("artifactType", String::class.java), "exploded")
            artifactTransform(ExplodeZip::class.java)
        }

        tasks.addRule("Pattern: <mavenTaskList>") {
            val mavenTasks = this.split(Regex("(?=[A-Z])")).map(String::decapitalize)
            tasks.register(this, MavenExec::class.java) {
                goals.set(mavenTasks)
            }
        }

        afterEvaluate {
            mavenConfiguration.dependencies.add(project.dependencies.create("org.apache.maven:apache-maven:${extension.version}:bin@zip"))
        }
    }
}

open class ExplodeZip : ArtifactTransform() {
    override fun transform(input: File): MutableList<File> {
        ZipFile(input).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    File(outputDirectory, entry.name).run {
                        parentFile.mkdirs()
                        if (!entry.isDirectory) {
                            outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        if (parentFile.name == "bin") {
                            setExecutable(true)
                        }
                    }
                }
            }
        }
        return mutableListOf(outputDirectory)
    }
}
