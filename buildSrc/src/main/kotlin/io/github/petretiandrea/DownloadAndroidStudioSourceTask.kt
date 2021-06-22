
package io.github.petretiandrea

import de.undercouch.gradle.tasks.download.DownloadAction
import de.undercouch.gradle.tasks.download.DownloadExtension
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.findByType
import java.io.File
import javax.inject.Inject

open class AndroidStudioDepsExtension @Inject constructor(objectFactory: ObjectFactory) {
    val androidStudioVersion: Property<String> = objectFactory.property(String::class.java)
}

open class DownloadAndroidStudioSourceTask
@Inject constructor(private val extension: AndroidStudioDepsExtension) : DefaultTask() {

    companion object {
        const val URL = "https://redirector.gvt1.com/edgedl/android/studio/ide-zips/"
        const val TARGET_FILE = "android-studio.zip"
    }

    @TaskAction
    fun downloadStudioSource() {
        println("oooo")
        if (!extension.androidStudioVersion.getOrElse("").isNullOrEmpty()) {
            val targetFilename = File(project.buildDir, TARGET_FILE)
            val closureConfig = closureOf<DownloadAction> {
                src(buildDownloadUrl(extension.androidStudioVersion.get()))
                onlyIfModified(true)
                overwrite(true)
                dest(targetFilename)
            }
            project.extensions.findByType<DownloadExtension>()?.configure(closureConfig)
        }
    }

    private fun buildDownloadUrl(version: String): String {
        return "${URL}$version/${buildAndroidStudioFilename(version)}"
    }

    private fun buildAndroidStudioFilename(version: String): String {
        return "android-studio-$version-${osVariant()}.zip"
    }

    private fun osVariant(): String {
        return when {
            Os.isFamily(Os.FAMILY_WINDOWS) -> {
                "windows"
            }
            Os.isFamily(Os.FAMILY_MAC) -> {
                "mac"
            }
            else -> {
                "linux"
            }
        }
    }
}
