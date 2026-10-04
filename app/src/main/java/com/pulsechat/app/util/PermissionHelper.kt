package com.pulsechat.app.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun hasContacts(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED

    fun hasMicrophone(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED

    fun hasCamera(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    fun hasLocation(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    fun hasNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else true
    }

    /** All runtime permissions we ask after login */
    fun requiredAfterLogin(): Array<String> {
        val list = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list += Manifest.permission.POST_NOTIFICATIONS
            list += Manifest.permission.READ_MEDIA_IMAGES
            list += Manifest.permission.READ_MEDIA_VIDEO
            list += Manifest.permission.READ_MEDIA_AUDIO
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            list += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list += Manifest.permission.BLUETOOTH_CONNECT
        }
        return list.toTypedArray()
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
