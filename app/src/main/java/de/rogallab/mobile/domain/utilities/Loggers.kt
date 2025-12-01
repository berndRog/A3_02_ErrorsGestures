package de.rogallab.mobile.domain.utilities

import android.util.Log
import de.rogallab.mobile.Globals.isComp
import de.rogallab.mobile.Globals.isDebug
import de.rogallab.mobile.Globals.isInfo
import de.rogallab.mobile.Globals.isVerbose

fun logError(tag: String, message: String) {
   val msg = formatMessage(message)
   Log.e(tag, msg)
}
fun logWarning(tag: String, message: String) {
   val msg = formatMessage(message)
   Log.w(tag, msg)
}
fun logInfo(tag: String, message: String) {
   val msg = formatMessage(message)
   if(isInfo) Log.i(tag, msg)
}
fun logDebug(tag: String, message: String) {
   val msg = formatMessage(message)
   if (isDebug) Log.d(tag, msg)
}
fun logVerbose(tag: String, message: String) {
   if (isVerbose) Log.v(tag, message)
}
fun logComp(tag: String, message: String) {
   val msg = formatMessage(message)
   if (isComp) Log.d(tag, msg)
}

private fun formatMessage(message: String) =
   String.format("%-110s %s", message, Thread.currentThread().toString())