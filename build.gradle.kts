/*
 *    Copyright 2020 Taktik SA
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

val kotlinVersion = "1.4.21"
val kotlinCoroutinesVersion = "1.4.2"

plugins {
    kotlin("jvm") version "1.4.21"
}

buildscript {
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.taktik.be/content/groups/public") }
    }
    dependencies {
        classpath("com.taktik.gradle:gradle-plugin-git-version:1.0.13")
    }
    plugins {
        `maven-publish`
    }
}

apply(plugin = "maven-publish")
apply(plugin = "git-version")

val gitVersion: String? by project
group = "io.icure"
version = gitVersion ?: "0.0.1-SNAPSHOT"


repositories {
    mavenCentral()
}

dependencies {
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = kotlinCoroutinesVersion)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-reactor", version = kotlinCoroutinesVersion)
    implementation(group = "io.projectreactor.netty", name = "reactor-netty", version = "1.0.9")
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.12.4")
    implementation(group = "org.apache.httpcomponents", name = "httpclient", version = "4.5.13")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withSourcesJar()
}

tasks.publish {
    doFirst {
        println("Artifact >>> ${project.group}:${project.name}:${project.version} <<< published to Maven repository")
    }
}