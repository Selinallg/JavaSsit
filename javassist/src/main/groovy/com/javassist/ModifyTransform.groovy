package com.javassist

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.apache.commons.io.FileUtils
import org.gradle.api.Project;

public class ModifyTransform extends Transform {
    def project
//    内存   windown   1  android 2
    def pool = ClassPool.default
//查找类
    ModifyTransform(Project project) {
        this.project = project
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        project.android.bootClasspath.each {
            pool.appendClassPath(it.absolutePath)
        }
//       1 拿到输入
        transformInvocation.inputs.each {
//            class 1     ---> 文件夹     jar 可能 1  不可能2  N
            it.directoryInputs.each {

                def preFileName = it.file.absolutePath
                pool.insertClassPath(preFileName)

                println "========directoryInputs======== " + preFileName
                findTarget(it.file, preFileName)
//  it.file
                //       2 查询输出的文件夹    目的地
                def dest = transformInvocation.outputProvider.getContentLocation(
                        it.name,
                        it.contentTypes,
                        it.scopes,
                        Format.DIRECTORY
                )

                //       3  文件copy  ---》 下一个环节
                FileUtils.copyDirectory(it.file, dest)
            }
            it.jarInputs.each {
                def dest = transformInvocation.outputProvider.getContentLocation(it.name
                        , it.contentTypes, it.scopes, Format.JAR)
//                    去哪里
                FileUtils.copyFile(it.file, dest)
            }
//            修改class   不是修改 jar
        }

//       2 查询输出的文件夹    目的地

//       3  文件copy  ---》 下一个环节
//        想干嘛干嘛
    }

//    fileName C:\Users\maniu\Downloads\ManiuJavaSsit\app\build\intermediates\javac\debug\classes
    private void findTarget(File dir, String fileName) {
        if (dir.isDirectory()) {
            dir.listFiles().each {
                findTarget(it, fileName)
            }
        } else {
            def filePath = dir.absolutePath
            if (filePath.endsWith(".class")) {

//                修改文件

                modify(filePath, fileName)
            }
        }

    }

    private void modify(def filePath, String fileName) {
        if (filePath.contains('R$') || filePath.contains('R.class')
                || filePath.contains("BuildConfig.class")) {
            return
        }

// 基于javassit  ----》
        def className = filePath.replace(fileName, "").replace("\\", ".").replace("/", ".")

        def name = className.replace(".class", "").substring(1)
        println "========name======== " + name
//        json 文件   ----》 javabean-- 修改---》 fastjson ----》回写到  json文件
        CtClass ctClass = pool.get(name)
        addCode(ctClass, fileName)
    }

    private void addCode(CtClass ctClass, String fileName) {
//        捡出来
        ctClass.defrost()
        CtMethod[] methods = ctClass.getDeclaredMethods()
        for (method in methods) {

            println "method " + method.getName() + "参数个数  " + method.getParameterTypes().length

            String methodName = method.getName();
            if (false) {
                //修改字节码，定义两个long类型变量
                method.addLocalVariable("begin", CtClass.longType);
                method.addLocalVariable("end", CtClass.longType);
                //方法执行前操作
                method.insertBefore("System.out.println(\"进入 [" + methodName + "] 方法\");");
                method.insertBefore("begin = System.currentTimeMillis();");
                //方法执行后操作
                method.insertAfter("end = System.currentTimeMillis();");
                method.insertAfter("System.out.println(\"方法 [" + methodName + "] 耗时:\"+ (end - begin) +\"ns\");");
                method.insertAfter("System.out.println(\"退出 [" + methodName + "] 方法\");");
            }else {
                //修改字节码，定义两个long类型变量
                method.addLocalVariable("begin", CtClass.longType);
                method.addLocalVariable("end", CtClass.longType);
                //方法执行前操作
                method.insertBefore("android.util.Log.d(\"llg-usetime\", \"进入 [" + methodName + "] 方法\");");
                method.insertBefore("begin = System.currentTimeMillis();");
                //方法执行后操作
                method.insertAfter("end = System.currentTimeMillis();");
                method.insertAfter("android.util.Log.d(\"llg-usetime\",\"方法 [" + methodName + "] 耗时:\"+ (end - begin) +\"ms\");");
                method.insertAfter("android.util.Log.d(\"llg-usetime\",\"退出 [" + methodName + "] 方法\");");
            }


//            method.insertAfter("android.util.Log.d(\"llg-usetime\", \"llg: \"+(end - start));")
            method.insertAfter("if(true){}")
            if (method.getParameterTypes().length == 1) {
                method.insertBefore("{ System.out.println(\$1);}")
            }
            if (method.getParameterTypes().length == 2) {
                method.insertBefore("{ System.out.println(\$1); System.out.println(\$2);}")
            }
            if (method.getParameterTypes().length == 3) {
                method.insertBefore("{ System.out.println(\$1);System.out.println(\$2);System.out.println(\\\$3);}")
            }
        }

        ctClass.writeFile(fileName)
        ctClass.detach()
    }

    @Override
    String getName() {
        return "david"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }
}
