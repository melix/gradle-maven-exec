package org.gradle.plugins.maven.exec

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals

object MavenExecPluginFunctionalTest : Spek({
    describe("MavenExecPlugin") {
        fun gradleRunner(projectDir: File, arguments: Array<out String>) =
                GradleRunner.create()
                        .withProjectDir(projectDir)
                        .withArguments(arguments.toList())
                        .withPluginClasspath()

        fun execute(projectDir: File, vararg args: String) = gradleRunner(projectDir, args).build()
        fun executeWithFailure(projectDir: File, vararg args: String) = gradleRunner(projectDir, args).buildAndFail()

        context("with configured maven exec plugin") {
            val testProjectDir = Files.createTempDirectory("maven_exec_plugin_test")
            val mavenOutputLocation = testProjectDir.resolve("target").toFile()
            val buildFile = Files.createFile(testProjectDir.resolve("build.gradle")).toFile()
            val mavenGoalName = "install"

            buildFile.writeText("""
                plugins {
                    id 'gradle.maven.exec'
                }

                maven {
                    version = '3.5.4'
                }

                task $mavenGoalName(type: org.gradle.plugins.maven.exec.MavenExec)
            """.trimIndent())

            val pomXml = Files.createFile(testProjectDir.resolve("pom.xml")).toFile()
            pomXml.writeText("""
                    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                      <modelVersion>4.0.0</modelVersion>

                      <groupId>com.mycompany.app</groupId>
                      <artifactId>my-app</artifactId>
                      <version>1.0-SNAPSHOT</version>

                      <properties>
                        <maven.compiler.source>1.8</maven.compiler.source>
                        <maven.compiler.target>1.8</maven.compiler.target>
                      </properties>

                      <dependencies>
                        <dependency>
                          <groupId>commons-io</groupId>
                          <artifactId>commons-io</artifactId>
                          <version>2.6</version>
                        </dependency>
                      </dependencies>
                    </project>
                    """.trimIndent())

            it("succeeds if valid pom.xml") {
                val buildResult = execute(testProjectDir.toFile(), mavenGoalName, "-s")

                assertEquals(buildResult.task(":$mavenGoalName")!!.outcome, TaskOutcome.SUCCESS)
                println(testProjectDir.root.toAbsolutePath())
                // TODO: assert contents of something
            }

            it("allows multiple maven goals to be executed") {
                val buildResult = execute(testProjectDir.toFile(), "clean", "-s")

                assertEquals(buildResult.task(":clean")!!.outcome, TaskOutcome.SUCCESS)
            }

            xit("uses Maven wrapper if present") {}
            xit("forwards arguments to Maven execution") {}
        }
    }
})
