import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import tech.antibytes.gradle.configuration.apple.ensureAppleDeviceCompatibility
import tech.antibytes.gradle.configuration.runtime.AntiBytesTestConfigurationTask
import tech.antibytes.gradle.configuration.sourcesets.iosx
import tech.antibytes.gradle.project.config.parameter.projectDomain

plugins {
    alias(dependencyCatalog.plugins.openapi)
    alias(antibytesCatalog.plugins.gradle.antibytes.kmpConfiguration)
    alias(antibytesCatalog.plugins.gradle.antibytes.androidLibraryConfiguration)
    alias(antibytesCatalog.plugins.gradle.antibytes.coverage)
    alias(antibytesCatalog.plugins.kotlinx.serialization)
    alias(dependencyCatalog.plugins.kmock)
}

val openApiOutputDir = "$projectDir/build/generated/openapi"

openApiGenerate {
    generatorName = "kotlin"
    inputSpec = "$rootDir/api-contract/src/main/resources/api/xkcd-data-hub-api.yaml"
    outputDir = openApiOutputDir
    apiPackage = "$projectDomain.api"
    modelPackage = "$projectDomain.model"
    modelNameSuffix = "DTO"
    generateApiDocumentation = false
    generateModelDocumentation = false
    configOptions = mapOf(
        "omitGradlePluginVersions" to "true",
        "omitGradleWrapper" to "true",
        "dateLibrary" to "kotlinx-datetime",
    )
    additionalProperties = mapOf(
        "nonPublicApi" to "true",
    )
    library = "multiplatform"
}

val openApiGenerate: Task by tasks.getting {
    outputs.upToDateWhen { false }

    val folders = listOf(
        "test",
        "commonTest",
        "jsTest",
        "jvmTest",
        "iosTest",
    )
    val files = listOf(
        "PartConfig.kt",
        "OctetByteArray.kt",
        "Bytes.kt",
        "Base64ByteArray.kt",
        "ApiAbstractions.kt",
    )

    doLast {
        val src = project.file("$openApiOutputDir/src")
        src.listFiles()?.forEach { file ->
            if (file.isDirectory && folders.contains(file.name)) {
                file.deleteRecursively()
            } else {
                file.walkTopDown().forEach { deepFile ->
                    if (deepFile.isFile && files.contains(deepFile.name.substringAfterLast("/"))) {
                        deepFile.delete()
                    }
                }
            }
        }
    }
}

val moduleName = "$projectDomain.data"
android {
    namespace = moduleName
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    androidTarget()
    jvm()
    js {
        browser()
    }
    iosx {
        binaries.forEach {
             //   target ->
            //target.linkerOpts("-lsqlite3")
        }
    }
    ensureAppleDeviceCompatibility()

    sourceSets {
        commonMain {
            kotlin.srcDir(
                layout.buildDirectory.file("generated/openapi/src/commonMain/kotlin")
                    .get().asFile.absolutePath,
            )

            dependencies {
                implementation(antibytesCatalog.common.kotlinx.coroutines.core)
                implementation(antibytesCatalog.common.kotlinx.serialization.core)
                implementation(antibytesCatalog.common.kotlinx.serialization.json)
                implementation(antibytesCatalog.common.ktor.client.cio)

                implementation(antibytesCatalog.common.ktor.client.core)
                implementation(antibytesCatalog.common.ktor.client.contentNegotiation)
                implementation(antibytesCatalog.common.ktor.client.logging)
                implementation(antibytesCatalog.common.ktor.serialization.core)
                implementation(antibytesCatalog.common.ktor.serialization.json)

                implementation(antibytesCatalog.common.kotlinx.dateTime)

                implementation(antibytesCatalog.common.square.sqldelight.primitiveAdapters)

                implementation(antibytesCatalog.common.koin.core)
            }
        }
        commonTest {
            kotlin.srcDir(
                layout.buildDirectory.file("generated/antibytes/commonTest/kotlin")
                    .get().asFile.absolutePath,
            )
            dependencies {
                implementation(dependencyCatalog.testUtils.core)
                implementation(dependencyCatalog.testUtils.coroutine)
                implementation(dependencyCatalog.testUtils.annotations)
                implementation(dependencyCatalog.testUtils.ktor)
                implementation(dependencyCatalog.testUtils.resourceloader)
                implementation(dependencyCatalog.kmock)
            }
        }

        androidMain {
            dependencies {
                implementation(antibytesCatalog.android.square.sqldelight.driver)
                implementation(antibytesCatalog.android.ktor.client)
            }
        }
        androidUnitTest {
            dependencies {
                implementation(antibytesCatalog.jvm.test.kotlin.junit5)
                implementation(antibytesCatalog.jvm.test.junit.legacyEngineJunit4)
                implementation(antibytesCatalog.android.test.robolectric)
                implementation(antibytesCatalog.android.test.ktx)
                implementation(antibytesCatalog.jvm.test.mockk)
            }
        }

        iosMain {
            dependencies {
                implementation(antibytesCatalog.common.square.sqldelight.driver.native)
                implementation(antibytesCatalog.common.ktor.client.ios)
            }
        }
    }
}

val provideTestConfig by tasks.creating(AntiBytesTestConfigurationTask::class.java) {
    mustRunAfter("clean")
    packageName.set("$moduleName.config")
    stringFields.set(
        mapOf(
            "projectDir" to project.projectDir.absolutePath
        )
    )

    mustRunAfter("clean")
}

tasks.withType(KotlinCompile::class.java) {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    dependsOn(openApiGenerate, provideTestConfig)
    mustRunAfter(openApiGenerate, provideTestConfig)
}

tasks.withType(KotlinNativeCompile::class.java) {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    dependsOn(openApiGenerate, provideTestConfig)
    mustRunAfter(openApiGenerate, provideTestConfig)
}

tasks.withType(KotlinCompileCommon::class.java) {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    dependsOn(openApiGenerate, provideTestConfig)
    mustRunAfter(openApiGenerate, provideTestConfig)
}

tasks.withType(Test::class.java) {
    useJUnitPlatform()
}