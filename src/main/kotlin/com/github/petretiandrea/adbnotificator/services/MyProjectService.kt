package com.github.petretiandrea.adbnotificator.services

import com.github.petretiandrea.adbnotificator.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
