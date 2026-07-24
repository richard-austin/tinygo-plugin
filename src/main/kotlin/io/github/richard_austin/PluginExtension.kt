// Modified by Richard Austin in 2026
package io.github.richard_austin

open class PluginExtension {
    var moduleName:String = ""
    var cgoEnabled:Boolean = false
    var os:List<String> = listOf("linux", "darwin")
    var arch:List<String> = listOf("arm64", "amd64")
    var golangVersion:String = ""
    var defaultGoVersion:String = "1.26.5"
    var tinyGoVersion:String = ""
    var defaultTinyGoVersion:String = "0.41.1"
    var ldFlags:Map<String, String> = mapOf()
    var extraBuildArgs:List<String> = listOf()
    var extraTestArgs:List<String> = listOf()
}
