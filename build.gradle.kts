import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `build-scan`
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    signing
    id("com.gradle.plugin-publish") version "0.10.0"
    id("gradle.site") version "0.6"
}

val junitVintageVersion = "5.3.2"
val junitPlatformVersion = "1.3.2"
val spekVersion = "2.0.0-rc.1"

group = "org.gradle.plugins"
version = "0.1.0"
description = "Invoke Maven from Gradle"

val webUrl = "https://melix.github.io/${project.name}/"
val githubUrl = "https://github.com/melix/${project.name}.git"

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}

site {
    outputDir.set(file("$rootDir/docs"))
    websiteUrl.set(webUrl)
    vcsUrl.set(githubUrl)
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

sourceSets.test.get().resources.srcDirs("src/test/samples")

val sourcesJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles sources JAR"
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<Test>().configureEach {
        useJUnitPlatform {
            includeEngines("spek2")
        }
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

repositories {
    jcenter()
    maven {
        url = uri("https://repo.gradle.org/gradle/libs")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion") {
        exclude(group = "org.jetbrains.kotlin")
    }

    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion") {
        exclude(group = "org.junit.platform")
        exclude(group = "org.jetbrains.kotlin")
    }

    testImplementation("org.gradle:sample-check:0.6.1")
    testImplementation("org.junit.vintage:junit-vintage-engine:$junitVintageVersion")

    testImplementation("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        register("mavenExecPlugin") {
            id = "gradle.maven.exec"
            implementationClass = "org.gradle.plugins.maven.exec.MavenExecPlugin"
        }
    }
}

pluginBundle {
    website = webUrl
    vcsUrl = githubUrl
    description = project.description
    tags = listOf("maven", "exec")

    plugins {
        named("mavenExecPlugin") {
            displayName = "Gradle Maven Exec Plugin"
        }
    }

    mavenCoordinates {
        groupId = "gradle.plugin.${project.group}"
    }
}

artifacts {
    add(configurations.archives.name, sourcesJar)
}

publishing {
    publications.withType<MavenPublication> {
        artifact(sourcesJar.get())

        pom {
            name.set(project.name)
            description.set(project.description)
            url.set(webUrl)

            scm {
                url.set(githubUrl)
            }

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    id.set("melix")
                    name.set("Cédric Champeau")
                    email.set("cedric@gradle.com")
                }
                developer {
                    id.set("eriwen")
                    name.set("Eric Wendelin")
                    email.set("eric@gradle.com")
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(configurations.archives.get())
    setRequired(Callable {
        gradle.taskGraph.hasTask("publishPlugins")
    })
}
