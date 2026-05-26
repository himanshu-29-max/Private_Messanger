package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_profile")
data class LocalProfile(
    @PrimaryKey val internalUuid: String,
    val displayId: String,
    val isPublicDiscovery: Boolean,
    val avatarColorHex: String,
    val appPin: String? = null // Optional master pin for app Lock
)

@Entity(tableName = "secure_rooms")
data class RoomEntity(
    @PrimaryKey val roomId: String,
    val name: String,
    val creatorId: String,
    val hashedPin: String, // PIN to enter verification
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "room_members")
data class RoomMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: String,
    val memberDisplayId: String
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val roomId: String?, // Null if direct peer message
    val senderId: String,
    val recipientId: String?, // Null if room message
    val encryptedBody: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFile: Boolean = false,
    val fileName: String? = null,
    val fileSize: String? = null,
    val transferProgress: Int = 0, // 0 - 100
    val transferSpeed: String? = null, // e.g. "4.5 MB/s"
    val transferStatus: String? = null // PENDING, TRANSFERRING, COMPLETED
)

@Entity(tableName = "secured_contacts")
data class ContactEntity(
    @PrimaryKey val displayId: String, // e.g. @silent_ghost
    val nickname: String,
    val isOnline: Boolean = true,
    val avatarColorHex: String = "#FF6347",
    val statusText: String = "Active on P2P Node"
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey val callId: String,
    val peerDisplayId: String,
    val callType: String, // "VOICE" or "VIDEO"
    val isOutgoing: Boolean,
    val status: String, // "CONNECTED", "ENDED", "DECLINED", "MISSED"
    val durationSeconds: Int = 0,
    val verificationCode: String, // Crypto security verification string
    val timestamp: Long = System.currentTimeMillis()
)
