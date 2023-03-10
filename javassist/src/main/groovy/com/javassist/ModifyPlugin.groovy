package com.javassist

import org.gradle.api.Plugin
import org.gradle.api.Project;
//Plugin  成了一个插件
public class ModifyPlugin implements Plugin<Project> {
//    main  函数  javassitst
    @Override
    void apply(Project project) {
        println "---------------------"
        project.android.registerTransform(new ModifyTransform(project))
    }
}
