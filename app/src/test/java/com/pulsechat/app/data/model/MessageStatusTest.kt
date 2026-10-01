package com.pulsechat.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageStatusTest {

    @Test
    fun messageDefaults_areCorrect() {
        val msg = Message()
        assertEquals(MessageType.TEXT, msg.type)
        assertEquals(MessageStatus.SENDING, msg.status)
        assertTrue(msg.reactions.isEmpty())
        assertTrue(msg.deletedFor.isEmpty())
    }

    @Test
    fun conversationType_directVsGroup() {
        assertEquals(ConversationType.DIRECT, ConversationType.valueOf("DIRECT"))
        assertEquals(ConversationType.GROUP, ConversationType.valueOf("GROUP"))
    }

    @Test
    fun callStatus_values() {
        val statuses = CallStatus.entries.map { it.name }
        assertTrue(statuses.contains("RINGING"))
        assertTrue(statuses.contains("CONNECTED"))
        assertTrue(statuses.contains("MISSED"))
        assertTrue(statuses.contains("ENDED"))
    }

    @Test
    fun privacyVisibility_defaults() {
        val privacy = PrivacySettings()
        assertEquals(Visibility.EVERYONE, privacy.lastSeen)
        assertTrue(privacy.readReceipts)
    }
}
