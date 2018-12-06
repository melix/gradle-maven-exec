plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("maven-embedder") {
            id = "maven-embedder"
            implementationClass = "org.gradle.maven.MavenEmbedderPlugin"
        }
    }
}

repositories {
    jcenter()
}