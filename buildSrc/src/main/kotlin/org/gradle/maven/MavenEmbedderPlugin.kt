package org.gradle.maven

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.transform.ArtifactTransform
import org.gradle.api.attributes.Attribute
import org.gradle.kotlin.dsl.ivy
import java.io.File
import java.util.zip.ZipFile

class MavenEmbedderPlugin : Plugin<Project> {
    override
    fun apply(project: Project) = project.run {

        val extension = extensions.create("maven", MavenExtension::class.java)

        val mavenConfiguration = configurations.create("maven") {
            isCanBeConsumed = false
            isCanBeResolved = true
        }

        afterEvaluate {
            // http://mirrors.standaloneinstaller.com/apache/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.zip
            repositories.ivy("maven-distributions") {
                setUrl(extension.repositoryUrl)
                patternLayout {
                    artifact("[revision]/binaries/[artifact]-[revision]-bin(.[ext])")
                }
                content {
                    onlyForConfigurations("maven")
                }
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
            mavenConfiguration.dependencies.add(project.dependencies.create("maven:apache-maven:${extension.version}@zip"))
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