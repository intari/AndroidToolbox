package net.intari.AndroidToolbox.Extensions

import net.intari.CustomLogger.CustomLog
import java.lang.Exception

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 18.11.2017.
 * Kotlin extension functions for logging
 *
 */

fun Any?.logException(error: Exception, message: String = "exception") {
    CustomLog.e(this?.javaClass?.simpleName ?: "null",message)
    CustomLog.logException(this?.javaClass?.simpleName ?: "null",error)
}
fun Any?.logException(error: Exception) {
    CustomLog.logException(this?.javaClass?.simpleName ?: "null",error)
}
fun Any?.logThrowable(error: Throwable, message: String = "throwable") {
    CustomLog.e(this?.javaClass?.simpleName ?: "null",message)
    CustomLog.logException(this?.javaClass?.simpleName ?: "null",error)
}
fun Any?.logThrowable(error: Throwable) {
    CustomLog.logException(this?.javaClass?.simpleName ?: "null",error)
}
fun Any?.logError(message: String = "error") {
    CustomLog.e(this?.javaClass?.simpleName ?: "null",message)
}
fun Any?.logWarning(message: String = "warning") {
    CustomLog.w(this?.javaClass?.simpleName ?: "null",message)
}
fun Any?.logInfo(message: String = "info") {
    CustomLog.i(this?.javaClass?.simpleName ?: "null",message)
}
fun Any?.logVerbose(message: String = "verbose") {
    CustomLog.l(this?.javaClass?.simpleName ?: "null",message)
}
fun Any?.logDebug(message: String = "debug") {
    CustomLog.d(this?.javaClass?.simpleName ?: "null",message)
}

