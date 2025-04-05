package yunbierdika.rpg2android.utils

import android.app.Activity
import android.webkit.JavascriptInterface
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class JavaScriptInterface(
    private val activity: Activity,
    private val writeLogToLocal: WriteLogToLocal
) {
    // 缓存存档数据
    private val cache = HashMap<String, String>()

    // 获取存档目录
    private fun getSaveDir(): File {
        val saveDir = File(activity.getExternalFilesDir(null), "save")
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            writeLogToLocal.logError("Failed to create save directory: ${saveDir.absolutePath}")
        }
        return saveDir
    }

    // 使结束游戏按钮功能可用
    @JavascriptInterface
    fun closeGame() {
        // 调用 Activity 的 finish() 方法
        activity.runOnUiThread { activity.finish() }
    }

    // 将发送过来的存档保存到指定目录
    @JavascriptInterface
    fun saveGameData(saveData: String, fileName: String) {
        // 如果缓存中有旧存档数据则更新
        cache[fileName]?.let {
            cache.remove(fileName)
            cache[fileName] = saveData
        }

        // 目录：Android/data/包名/file/save
        val saveDir = getSaveDir()
        val saveFile = File(saveDir, fileName)

        // 使用 LZString 加密压缩
        val compressed = LZString.compressToBase64(saveData)

        // 将存档数据写入文件
        try {
            FileOutputStream(saveFile).use { fos ->
                fos.write(compressed.toByteArray())
            }
        } catch (e: IOException) {
            writeLogToLocal.logError("Failed to save game data：${e.message}", e)
        }
    }

    // 加载存档，返回值为存档文件里的内容
    @JavascriptInterface
    fun loadGameData(fileName: String): String? {
        // 如果缓存中已有数据，直接返回
        cache[fileName]?.let {
            return it
        }

        // 文件读取和解码
        val saveDir = getSaveDir()
        val saveFile = File(saveDir, fileName)
        // 判断文件是否存在
        if (!saveFile.exists()) return null

        return try {
            FileInputStream(saveFile).use { fis ->
                val reader = BufferedReader(InputStreamReader(fis, StandardCharsets.UTF_8))
                val base64Data = reader.readText().trim()

                // 使用 LZString 解码
                val decodedData = LZString.decompressFromBase64(base64Data)

                // 将解码后的数据存入缓存
                decodedData?.let {
                    cache[fileName] = it
                }

                decodedData
            }
        } catch (e: IOException) {
            writeLogToLocal.logError("Failed to load game data${e.message}", e)
            null
        }
    }

    // 判断存档文件是否存在
    @JavascriptInterface
    fun existsGameSave(fileName: String): Boolean {
        val saveDir = getSaveDir()
        val saveFile = File(saveDir, fileName)
        return saveFile.exists()
    }

    // 删除CommonSave插件的专用存档（用于跨周目继承点数的存档）
    @JavascriptInterface
    fun removeCommonSave() {
        val targetDir = getSaveDir()
        val saveFile = File(targetDir, "common.rpgsave")

        if (saveFile.exists() && !saveFile.delete()) {
            writeLogToLocal.logError("Failed to delete common save: ${saveFile.absolutePath}")
        }
    }
}