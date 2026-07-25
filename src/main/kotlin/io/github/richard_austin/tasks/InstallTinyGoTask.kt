// Added by Richard Austin in 2026
package io.github.richard_austin.tasks

import io.github.richard_austin.GO_SETUP_DIR
import io.github.richard_austin.TINYGO_SETUP_DIR
import io.github.richard_austin.GRADLE_FILES_DIR
import io.github.richard_austin.utils.PluginUtils.getArch
import io.github.richard_austin.utils.PluginUtils.getOs
import io.github.richard_austin.utils.PluginUtils.goBinary
import io.github.richard_austin.utils.PluginUtils.tinyGoBinary
import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations // Required for tarTree
import org.gradle.api.file.FileSystemOperations // Required for copy
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

@CacheableTask
abstract class InstallTinyGoTask @Inject constructor(
    @get:Internal val projectLayout: ProjectLayout,
    @get:Internal val fileSystemOperations: FileSystemOperations,
    @get:Internal val archiveOperations: ArchiveOperations // Inject ArchiveOperations here
) : DefaultTask() {

    @get:Input
    abstract val tinyGoVersion: Property<String>
    @get:Input
    abstract val defaultTinyGoVersion: Property<String>
    @get:Input
    abstract val goVersion: Property<String>

    @get:Input
    abstract val defaultGoVersion: Property<String>

    @get:Input
    abstract val rootDir: Property<File>

    init {
        group = "tinygo"
        description = "Installs TinyGo"
    }

    @TaskAction
    fun installTinyGoBinaries() {
        val tinyGoVersion = this@InstallTinyGoTask.tinyGoVersion.get().ifEmpty {
            defaultTinyGoVersion.get()
        }

        val golangVersion = goVersion.get().ifEmpty {
            defaultGoVersion.get()
        }

        if (!File(tinyGoBinary(tinyGoVersion, defaultTinyGoVersion.get(), rootDir.get())).exists()) {
            val buildDir = projectLayout.buildDirectory.get().asFile
            val tarfileLocation = File(buildDir, "go${tinyGoVersion}.${getOs()}-${getArch()}.tar.gz")
            val tinyGoOutputDir = File(rootDir.get(), "$GRADLE_FILES_DIR/$TINYGO_SETUP_DIR-$tinyGoVersion")

            logger.lifecycle("Extracting  ${tarfileLocation.absolutePath} ::::: into ${tinyGoOutputDir.absolutePath}")

            // Perform the extraction during execution phase
            fileSystemOperations.copy { spec ->
                // Use archiveOperations to create the tarTree
                spec.from(archiveOperations.tarTree(tarfileLocation))
                spec.into(tinyGoOutputDir)
            }

            // Delete the tarfile safely after successful extraction
            if (tarfileLocation.exists()) {
                tarfileLocation.delete()
            }
        }


        if (!File(goBinary(golangVersion, defaultGoVersion.get(), rootDir.get())).exists()) {
            val buildDir = projectLayout.buildDirectory.get().asFile
            val tarfileLocation = File(buildDir, "go${golangVersion}.${getOs()}-${getArch()}.tar.gz")
            val outputDir = File(rootDir.get(), "$GRADLE_FILES_DIR/$GO_SETUP_DIR-$golangVersion")
            logger.lifecycle("Extracting  ${tarfileLocation.absolutePath} ::::: into ${outputDir.absolutePath}")

            // Perform the extraction during execution phase
            fileSystemOperations.copy { spec ->
                // Use archiveOperations to create the tarTree
                spec.from(archiveOperations.tarTree(tarfileLocation))
                spec.into(outputDir)
            }

            // Delete the tarfile safely after successful extraction
            if (tarfileLocation.exists()) {
                tarfileLocation.delete()
            }
        }

    }
}
