package gato.naranja.homeland

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GlobalExceptionHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e("GlobalException", "Uncaught exception on thread: ${thread.name}", throwable)
            writeExceptionToFile(throwable)
        } finally {
            // Let the default handler finish (shows the crash dialog)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun writeExceptionToFile(throwable: Throwable) {
        try {
            val file = File(context.filesDir, "crash_log.txt")
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val stackTrace = Log.getStackTraceString(throwable)
            file.appendText("[$timestamp]\n$stackTrace\n\n")
        } catch (e: Exception) {
            Log.e("GlobalException", "Failed to write crash log", e)
        }
    }
}