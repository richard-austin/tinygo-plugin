// Modified by Richard Austin in 2026
package io.github.richard_austin.tasks

import io.github.richard_austin.GO_SETUP_DIR
import io.github.richard_austin.TINYGO_SETUP_DIR
import io.github.richard_austin.GRADLE_FILES_DIR
import io.github.richard_austin.utils.PluginUtils
import io.github.richard_austin.utils.PluginUtils.getArch
import io.github.richard_austin.utils.PluginUtils.getOs
import io.github.richard_austin.utils.PluginUtils.goBinary
import io.github.richard_austin.utils.PluginUtils.tinyGoBinary
import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

@CacheableTask
abstract class DownloadGoTask @Inject constructor(
    @get:Internal val projectLayout: ProjectLayout
) : DefaultTask() {
    @get:Input
    abstract val tinyGoVersion: Property<String>
    @get:Input
    abstract val goVersion: Property<String>
    @get:Input
    abstract val defaultTinyGoVersion: Property<String>
    @get:Input
    abstract val defaultGoVersion: Property<String>
    @get:Input
    abstract val rootDir: Property<File>

    init {
    }

    @TaskAction
    fun downloadGo() {
        val buildDir = projectLayout.buildDirectory.get().asFile
        val tinyGolangVersion = tinyGoVersion.get().ifEmpty {
            defaultTinyGoVersion.get()
        }

        val golangVersion = goVersion.get().ifEmpty {
            defaultGoVersion.get()
        }

        // val goVersion = "1.25.4"
        val goUrl = "https://go.dev/dl/go${golangVersion}.${getOs()}-${getArch()}.tar.gz"

        val tinyGoUrl = "https://github.com/tinygo-org/tinygo/releases/download/v${tinyGolangVersion}/tinygo${tinyGolangVersion}.${getOs()}-${getArch()}.tar.gz"

        val tinyGoOutputLocation = "$buildDir/go${tinyGolangVersion}.${getOs()}-${getArch()}.tar.gz"
        val outputLocation = "$buildDir/go${golangVersion}.${getOs()}-${getArch()}.tar.gz"


        if (!File(tinyGoBinary(tinyGoVersion.get(), defaultTinyGoVersion.get(),rootDir.get())).exists()) {
            // Setup the build directory
            buildDir.mkdirs()

            val tinyGoOutputFile = File(tinyGoOutputLocation)
            tinyGoOutputFile.createNewFile()
            val tinyGoDestinationDir = File("${rootDir.get()}/$GRADLE_FILES_DIR/$TINYGO_SETUP_DIR-$tinyGolangVersion")
            tinyGoDestinationDir.mkdirs()

            // Download the file
            logger.lifecycle("Downloading TinyGo version $tinyGolangVersion")
            logger.info("Source URL: $tinyGoUrl")
            PluginUtils.downloadFile(tinyGoUrl, tinyGoOutputFile)
            logger.lifecycle("Done")
        }

        if (!File(goBinary(goVersion.get(), defaultGoVersion.get(),rootDir.get())).exists()) {
            // Setup the build directory
            buildDir.mkdirs()

            val outputFile = File(outputLocation)
            outputFile.createNewFile()
            val destinationDir = File("${rootDir.get()}/$GRADLE_FILES_DIR/$GO_SETUP_DIR-$golangVersion")
            destinationDir.mkdirs()
            logger.error("rootDir = ${rootDir.get()}")
            // Download the file
            logger.lifecycle("Downloading Go version $golangVersion")
            logger.info("Source URL: $goUrl")
            PluginUtils.downloadFile(goUrl, outputFile)
            logger.lifecycle("Done")
        }
    }
}
