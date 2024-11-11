import com.github.jk1.license.render.CsvReportRenderer
import com.github.jk1.license.render.ReportRenderer

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

val kotlinVersion = "1.9.25"
val kotlinCoroutinesVersion = "1.8.1"
val jacksonVersion = "2.17.2"
val reactorNettyVersion = "1.1.23"
val nettyVersion = "4.1.114.Final"

plugins {
    kotlin("jvm") version "1.8.10"
    id("com.taktik.gradle.maven-repository") version "1.0.7"
    id("com.taktik.gradle.git-version") version "2.0.8-gb47b2d0e35"
    id("com.github.jk1.dependency-license-report") version "2.0"
}

licenseReport {
    renderers = arrayOf<ReportRenderer>(CsvReportRenderer())
}

buildscript {
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.taktik.be/content/groups/public") }
    }
    dependencies {
        classpath("com.taktik.gradle:gradle-plugin-maven-repository:1.0.7")
        classpath("com.taktik.gradle:gradle-plugin-git-version:2.0.8-gb47b2d0e35")
    }
}

val gitVersion: String? by project
group = "io.icure"
version = gitVersion ?: "0.0.1-SNAPSHOT"


dependencies {
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = kotlinCoroutinesVersion)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-reactor", version = kotlinCoroutinesVersion)
    implementation(group = "io.projectreactor.netty", name = "reactor-netty", version = reactorNettyVersion)
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = jacksonVersion)
    implementation(group = "org.apache.httpcomponents", name = "httpclient", version = "4.5.14")

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter", version = "5.8.0")
    testImplementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = jacksonVersion)
    testImplementation(group = "com.fasterxml.jackson.datatype", name = "jackson-datatype-jsr310", version = jacksonVersion)
    testImplementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = jacksonVersion)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}
