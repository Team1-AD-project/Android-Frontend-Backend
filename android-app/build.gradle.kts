// Top-level build file
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.8.5" apply false
}

// #region agent log
fun debugLog(hypothesisId: String, location: String, message: String, data: Map<String, Any?>) {
    fun escape(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"")
    fun jsonValue(value: Any?): String = when (value) {
        null -> "null"
        is Number, is Boolean -> value.toString()
        else -> "\"${escape(value.toString())}\""
    }
    val dataJson = data.entries.joinToString(",") { "\"${escape(it.key)}\":${jsonValue(it.value)}" }
    val payload =
        "{\"sessionId\":\"debug-session\",\"runId\":\"pre-fix\",\"hypothesisId\":\"${escape(hypothesisId)}\",\"location\":\"${escape(location)}\",\"message\":\"${escape(message)}\",\"data\":{${dataJson}},\"timestamp\":${System.currentTimeMillis()}}"
    val logFile = java.io.File("C:\\Users\\csls\\Desktop\\ad-ui\\.cursor\\debug.log")
    logFile.parentFile?.mkdirs()
    logFile.appendText(payload + System.lineSeparator())
}

val javaHome = System.getProperty("java.home")
val javaVersion = System.getProperty("java.version")
val gradleUserHome = System.getenv("GRADLE_USER_HOME")
val sdkDirFromEnv = System.getenv("ANDROID_SDK_ROOT") ?: System.getenv("ANDROID_HOME")
val localPropsFile = file("local.properties")
val sdkDirFromLocalProps = if (localPropsFile.exists()) {
    localPropsFile.readLines()
        .firstOrNull { it.startsWith("sdk.dir=") }
        ?.substringAfter("sdk.dir=")
} else null
val sdkDir = sdkDirFromEnv ?: sdkDirFromLocalProps
val coreJar = sdkDir?.let { java.io.File(it, "platforms/android-34/core-for-system-modules.jar") }
val jlinkExe = javaHome?.let { java.io.File(it, "bin/jlink.exe") }
val gradleCacheDir = gradleUserHome?.let { java.io.File(it) }


