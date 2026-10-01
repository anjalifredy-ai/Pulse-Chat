package com.pulsechat.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PermissionHelperTest {

    @Test
    fun contactsPermission_isReadContacts() {
        assertEquals(android.Manifest.permission.READ_CONTACTS, PermissionHelper.contactsPermission)
    }

    @Test
    fun micPermission_isRecordAudio() {
        assertEquals(android.Manifest.permission.RECORD_AUDIO, PermissionHelper.micPermission)
    }

    @Test
    fun cameraPermission_isCamera() {
        assertEquals(android.Manifest.permission.CAMERA, PermissionHelper.cameraPermission)
    }
}
