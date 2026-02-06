// Top-level build file
plugins {
    id("com.android.application") version "8.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.6" apply false
}

// #region agent log
//fun debugLog(hypothesisId: String, location: String, message: String, data: Map<String, Any?>) {
//    fun escape(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"")
//    fun jsonValue(value: Any?): String = when (value) {
//        null -> "null"
//        is Number, is Boolean -> value.toString()
//        else -> "\"${escape(value.toString())}\""
//    }
//    val dataJson = data.entries.joinToString(",") { "\"${escape(it.key)}\":${jsonValue(it.value)}" }
//    val payload =
//        "{\"sessionId\":\"debug-session\",\"runId\":\"pre-fix\",\"hypothesisId\":\"${escape(hypothesisId)}\",\"location\":\"${escape(location)}\",\"message\":\"${escape(message)}\",\"data\":{${dataJson}},\"timestamp\":${System.currentTimeMillis()}}"
//    val logFile = java.io.File("C:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log")
//    logFile.parentFile?.mkdirs()
//    logFile.appendText(payload + System.lineSeparator())
//}
//
//val javaHome = System.getProperty("java.home")
//val javaVersion = System.getProperty("java.version")
//val gradleUserHome = System.getenv("GRADLE_USER_HOME")
//val sdkDirFromEnv = System.getenv("ANDROID_SDK_ROOT") ?: System.getenv("ANDROID_HOME")
//val localPropsFile = file("local.properties")
//val sdkDirFromLocalProps = if (localPropsFile.exists()) {
//    localPropsFile.readLines()
//        .firstOrNull { it.startsWith("sdk.dir=") }
//        ?.substringAfter("sdk.dir=")
//} else null
//val sdkDir = sdkDirFromEnv ?: sdkDirFromLocalProps
//val coreJar = sdkDir?.let { java.io.File(it, "platforms/android-34/core-for-system-modules.jar") }
//val jlinkExe = javaHome?.let { java.io.File(it, "bin/jlink.exe") }
//val gradleCacheDir = gradleUserHome?.let { java.io.File(it) }
//
//debugLog(
//    hypothesisId = "H4",
//    location = "android-app/build.gradle.kts:env",
//    message = "Gradle JVM info",
//    data = mapOf(
//        "javaHome" to javaHome,
//        "javaVersion" to javaVersion,
//        "javaVendor" to System.getProperty("java.vendor")
//    )
//)
//
//debugLog(
//    hypothesisId = "H1",
//    location = "android-app/build.gradle.kts:sdk",
//    message = "Android SDK core-for-system-modules.jar info",
//    data = mapOf(
//        "sdkDir" to sdkDir,
//        "coreJarExists" to (coreJar?.exists() ?: false),
//        "coreJarSize" to (coreJar?.length() ?: -1L),
//        "coreJarPath" to (coreJar?.absolutePath ?: "")
//    )
//)
//
//debugLog(
//    hypothesisId = "H2",
//    location = "android-app/build.gradle.kts:jlink",
//    message = "JLink executable info",
//    data = mapOf(
//        "jlinkPath" to (jlinkExe?.absolutePath ?: ""),
//        "jlinkExists" to (jlinkExe?.exists() ?: false)
//    )
//)
//
//debugLog(
//    hypothesisId = "H3",
//    location = "android-app/build.gradle.kts:gradle-cache",
//    message = "Gradle cache directory info",
//    data = mapOf(
//        "gradleUserHome" to gradleUserHome,
//        "cacheExists" to (gradleCacheDir?.exists() ?: false),
//        "cacheWritable" to (gradleCacheDir?.canWrite() ?: false)
//    )
//)
// #endregion
