package me.proton.pass.autofill

import android.app.assist.AssistStructure
import android.content.Context
import android.content.pm.PackageManager
import android.service.autofill.FillContext

object Utils {

    fun getApplicationPackageName(windowNode: AssistStructure.WindowNode): String {
        val wholePackageName = windowNode.title
        val packageComponents = wholePackageName.split("/")
        return packageComponents.first()
    }

    fun getApplicationName(context: Context, packageName: String): String {
        val packageManager = context.packageManager
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return packageManager.getApplicationLabel(appInfo).toString()
    }

    fun getWindowNodes(fillContext: FillContext): List<AssistStructure.WindowNode> {
        val structure: AssistStructure = fillContext.structure
        return if (structure.windowNodeCount > 0)
            (0 until structure.windowNodeCount).map { structure.getWindowNodeAt(it) } else
            emptyList()
    }
}