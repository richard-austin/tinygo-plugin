// Modified by Richard Austin in 2026
package io.github.richard_austin.tasks

import io.github.richard_austin.TINYGO_BINARY
import io.github.richard_austin.GO_INSTALL_TASK
import io.github.richard_austin.GO_SETUP_DIR
import io.github.richard_austin.GRADLE_FILES_DIR
import io.github.richard_austin.utils.PluginUtils.ext
import io.github.richard_austin.utils.PluginUtils.tinyGoBinary
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.ExecActionFactory
import java.io.File
import kotlin.collections.set

// 1. Declare the class as abstract to let Gradle safely proxy the fields
@CacheableTask
abstract class TinyGoTask : AbstractExecTask<TinyGoTask>(TinyGoTask::class.java) {
    @get:Input abstract val tinyGoVersion: Property<String>
    @get:Input abstract val goVersion: Property<String>
    @get:Input abstract val defaultTinyGoVersion: Property<String>
    @get:Input abstract val defaultGoVersion: Property<String>
    @get:Input abstract val rootDir: Property<File>

    // 2. Delegate getters to Gradle's internal service injection container
    @Inject
    abstract override fun getObjectFactory(): ObjectFactory

    @Inject
    abstract override fun getExecActionFactory(): ExecActionFactory

    @Input
    var tinyGoTaskArgs: MutableList<String> = mutableListOf()

    @Internal
    var tinyGoTaskEnv: MutableMap<String, Any> = mutableMapOf()

    init {
        tinyGoVersion.set(project.ext.tinyGoVersion)
        goVersion.set(project.ext.golangVersion)
        defaultTinyGoVersion.set(project.ext.defaultTinyGoVersion)
        defaultGoVersion.set(project.ext.defaultGoVersion)
        rootDir.set(project.rootDir)
        dependsOn(GO_INSTALL_TASK)
    }

    @TaskAction
    override fun exec()
    {
        val tinyGolangVersion = tinyGoVersion.get().ifEmpty {
            defaultTinyGoVersion.get()
        }
        val goVersion = goVersion.get().ifEmpty {
            defaultGoVersion.get()
        }

        val tinyGoBinary = tinyGoBinary(tinyGoVersion.get(), defaultTinyGoVersion.get(), rootDir.get())
        logger.info("tinyGoBinary: $tinyGoBinary")
        logger.info("tinyGoVersion: $tinyGolangVersion")
        // Configure GOROOT (if needed)
        if (tinyGoBinary != TINYGO_BINARY) {
            tinyGoTaskEnv["GOROOT"] = "${rootDir.get()}/$GRADLE_FILES_DIR/$GO_SETUP_DIR-$goVersion/go"
        }
        executable = tinyGoBinary
        args = tinyGoTaskArgs
        tinyGoTaskEnv.forEach { (key, value) ->
            environment(key, value)
        }

        logger.info("tinyGoTaskEnv: $tinyGoTaskEnv")
        logger.info("tinyGoTaskArgs: $tinyGoTaskArgs")

        super.environment["PATH"] = (super.environment["PATH"] as String) +":${rootDir.get()}/$GRADLE_FILES_DIR/$GO_SETUP_DIR-$goVersion/go/bin"
        super.exec()
    }
}
