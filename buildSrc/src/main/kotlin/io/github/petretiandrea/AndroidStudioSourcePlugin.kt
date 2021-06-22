package io.github.petretiandrea

import de.undercouch.gradle.tasks.download.DownloadTaskPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import java.io.File

class AndroidStudioSourcePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val sourceZip = File(project.buildDir, DownloadAndroidStudioSourceTask.TARGET_FILE)
        val sourceFolder = File(project.buildDir, DownloadAndroidStudioSourceTask.TARGET_FILE.substringBefore(".zip"))
        val extension = project.extensions.create<AndroidStudioDepsExtension>("androidstudio", project.objects)

        project.plugins.apply(DownloadTaskPlugin::class.java)

        val downloadTask = project.tasks.create<DownloadAndroidStudioSourceTask>(
            "downloadAndroidStudioSource",
            extension
        )
        project.tasks.create("unzipSource", Copy::class.java) {
            dependsOn(downloadTask)
            from(project.zipTree(sourceZip))
            into(sourceFolder.parent)
        }

        project.extra.set("androidStudioSource", sourceFolder.absolutePath)

        // TODO: this should be automatized as much as possible to set dependecy to org.intellij.plugin

        println("Applied")
    }
}
