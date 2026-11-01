package com.focusquest.app.ui

import android.content.Context
import android.content.Intent
import java.util.Locale

data class AppInfo(val pkg: String, val label: String)

fun loadApps(ctx: Context): List<AppInfo> {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0)
        .map { AppInfo(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
        .filter { it.pkg != ctx.packageName }
        .distinctBy { it.pkg }
        .sortedBy { it.label }
}

fun fmtTime(min: Int): String {
    val h = (min / 60) % 24
    val m = min % 60
    val h12 = if (h % 12 == 0) 12 else h % 12
    val ap = if (h >= 12) "مساءً" else "صباحًا"
    return String.format(Locale.US, "%d:%02d %s", h12, m, ap)
}

fun fmtDur(min: Int): String {
    val h = min / 60
    val m = min % 60
    return when {
        h > 0 && m > 0 -> "$h س و $m د"
        h > 0 -> "$h ساعة"
        else -> "$m دقيقة"
    }
}
