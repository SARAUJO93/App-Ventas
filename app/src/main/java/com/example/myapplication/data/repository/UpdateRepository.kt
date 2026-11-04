package com.example.myapplication.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val isUpdateAvailable: Boolean
)

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class UpdateAvailable(val info: UpdateInfo) : UpdateState()
    object NoUpdateAvailable : UpdateState()
    data class Downloading(val progress: Float) : UpdateState()
    data class ReadyToInstall(val apkFile: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

interface UpdateRepository {
    suspend fun checkForUpdate(currentVersionName: String): UpdateInfo
    suspend fun downloadApk(downloadUrl: String, onProgress: (Float) -> Unit): File?
    fun triggerApkInstallation(context: Context, apkFile: File)
}

@Singleton
class UpdateRepositoryImpl @Inject constructor(
    private val context: Context
) : UpdateRepository {

    override suspend fun checkForUpdate(currentVersionName: String): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            // Intentar primero endpoint de releases/latest
            val url = URL("https://api.github.com/repos/SARAUJO93/App-Ventas/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "NovaPOS-App")
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val tagName = json.optString("tag_name", "v1.0.0").replace("v", "")
                val notes = json.optString("body", "Mejoras de rendimiento y nuevas características.")

                var apkUrl = "https://github.com/SARAUJO93/App-Ventas/releases/download/v$tagName/app-debug.apk"
                val assets = json.optJSONArray("assets")
                if (assets != null && assets.length() > 0) {
                    val assetObj = assets.getJSONObject(0)
                    apkUrl = assetObj.optString("browser_download_url", apkUrl)
                }

                val isNewer = isVersionNewer(tagName, currentVersionName)
                UpdateInfo(
                    latestVersion = tagName,
                    downloadUrl = apkUrl,
                    releaseNotes = notes,
                    isUpdateAvailable = isNewer
                )
            } else {
                // Fallback a consultar las etiquetas (tags)
                checkForTagUpdate(currentVersionName)
            }
        } catch (e: Exception) {
            checkForTagUpdate(currentVersionName)
        }
    }

    private fun checkForTagUpdate(currentVersionName: String): UpdateInfo {
        return try {
            val tagsUrl = URL("https://api.github.com/repos/SARAUJO93/App-Ventas/tags")
            val conn = tagsUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "NovaPOS-App")
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(responseText)
                if (array.length() > 0) {
                    val latestTagObj = array.getJSONObject(0)
                    val tagName = latestTagObj.optString("name", "v1.0.0").replace("v", "")
                    val isNewer = isVersionNewer(tagName, currentVersionName)
                    val downloadUrl = "https://github.com/SARAUJO93/App-Ventas/releases/download/v$tagName/app-debug.apk"
                    UpdateInfo(
                        latestVersion = tagName,
                        downloadUrl = downloadUrl,
                        releaseNotes = "Nueva versión v$tagName con mejoras y correcciones.",
                        isUpdateAvailable = isNewer
                    )
                } else {
                    UpdateInfo(currentVersionName, "", "No hay etiquetas publicadas", false)
                }
            } else {
                UpdateInfo(currentVersionName, "", "Error de servidor GitHub", false)
            }
        } catch (e: Exception) {
            UpdateInfo(currentVersionName, "", "Error de red", false)
        }
    }

    override suspend fun downloadApk(downloadUrl: String, onProgress: (Float) -> Unit): File? = withContext(Dispatchers.IO) {
        try {
            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "NovaPOS-App")
            connection.connectTimeout = 10000
            connection.readTimeout = 30000
            connection.connect()

            val fileLength = connection.contentLength
            val cacheDir = context.externalCacheDir ?: context.cacheDir
            val apkFile = File(cacheDir, "nova_pos_update.apk")
            if (apkFile.exists()) apkFile.delete()

            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(apkFile)

            val buffer = ByteArray(4096)
            var total: Long = 0
            var count: Int

            while (inputStream.read(buffer).also { count = it } != -1) {
                total += count.toLong()
                if (fileLength > 0) {
                    onProgress(total.toFloat() / fileLength.toFloat())
                }
                outputStream.write(buffer, 0, count)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            apkFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun triggerApkInstallation(context: Context, apkFile: File) {
        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        return try {
            val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val maxLen = maxOf(latestParts.size, currentParts.size)

            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}