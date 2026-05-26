package com.example.data.repository

import android.content.Context
import com.example.data.database.*
import com.example.security.CryptoEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import kotlin.random.Random

class FortressRepository(context: Context) {

    private val db = FortressDatabase.getDatabase(context)
    private val dao = db.fortressDao()

    // Expose flows to the ViewModel
    val localProfile: Flow<LocalProfile?> = dao.getLocalProfile()
    val rooms: Flow<List<RoomEntity>> = dao.getRooms()
    val contacts: Flow<List<ContactEntity>> = dao.getContacts()
    val callLogs: Flow<List<CallLogEntity>> = dao.getCallLogs()

    /**
     * Set up default profile if not exists, and prepopulate network nodes for discovery.
     */
    suspend fun initializeDatabase() {
        val existingProfile = dao.getLocalProfileOneShot()
        if (existingProfile == null) {
            val defaultProfile = LocalProfile(
                internalUuid = UUID.randomUUID().toString(),
                displayId = "@agent_omega",
                isPublicDiscovery = true,
                avatarColorHex = "#00FF99"
            )
            dao.insertProfile(defaultProfile)
        }

        // Prepopulate network nodes if empty
        val existingContacts = dao.getContacts().firstOrNull()
        if (existingContacts.isNullOrEmpty()) {
            val networkNodes = listOf(
                ContactEntity("@cyber_scout", "Cyber Scout [Sentinel]", true, "#00BFFF", "Active - Encryption Node 4"),
                ContactEntity("@operator_9", "Tactical Operative 9", true, "#FF4500", "Stealth - Guarding P2P Relay"),
                ContactEntity("@shadow_ops", "Shadow Network Admin", false, "#8A2BE2", "Offline - Node Sleep"),
                ContactEntity("@alpha_7x", "Alpha Protocol Node", true, "#EE82EE", "Listening for Handshakes"),
                ContactEntity("@phantom_gate", "Phantom Secure Proxy", true, "#FFD700", "Online - Multi-hop Enabled")
            )
            for (node in networkNodes) {
                dao.insertContact(node)
            }
        }
    }

    // --- Profile Operations ---
    suspend fun updateDisplayId(newDisplayId: String) {
        val profile = dao.getLocalProfileOneShot() ?: return
        dao.updateDisplayId(profile.internalUuid, newDisplayId)
    }

    suspend fun updatePublicDiscovery(isPublic: Boolean) {
        val profile = dao.getLocalProfileOneShot() ?: return
        dao.updatePublicDiscovery(profile.internalUuid, isPublic)
    }

    suspend fun setAppPin(pin: String?) {
        val profile = dao.getLocalProfileOneShot() ?: return
        val updated = profile.copy(appPin = pin?.let { CryptoEngine.sha256(it) })
        dao.insertProfile(updated)
    }

    // --- Secured Contacts Discovery ---
    suspend fun searchContactByDisplayId(displayId: String): ContactEntity? {
        val targetId = if (displayId.startsWith("@")) displayId else "@$displayId"
        return dao.getContactByDisplayId(targetId)
    }

    suspend fun addContact(contact: ContactEntity) {
        dao.insertContact(contact)
    }

    suspend fun deleteContact(displayId: String) {
        dao.deleteContact(displayId)
    }

    // --- Secure Room Management ---
    suspend fun createRoom(name: String, unhashedPin: String, creatorId: String): String {
        val roomId = UUID.randomUUID().toString()
        val hashed = CryptoEngine.sha256(unhashedPin)
        val newRoom = RoomEntity(
            roomId = roomId,
            name = name,
            creatorId = creatorId,
            hashedPin = hashed
        )
        dao.insertRoom(newRoom)
        // Creator joins room as first member
        dao.insertMember(RoomMemberEntity(roomId = roomId, memberDisplayId = creatorId))
        return roomId
    }

    suspend fun verifyRoomPin(roomId: String, enteredPin: String): Boolean {
        val room = dao.getRoomById(roomId) ?: return false
        val hashedEntered = CryptoEngine.sha256(enteredPin)
        return room.hashedPin == hashedEntered
    }

    suspend fun addMemberToRoom(roomId: String, displayId: String) {
        dao.insertMember(RoomMemberEntity(roomId = roomId, memberDisplayId = displayId))
    }

    fun getMembersForRoom(roomId: String): Flow<List<RoomMemberEntity>> {
        return dao.getMembersForRoom(roomId)
    }

    suspend fun getMembersForRoomOneShot(roomId: String): List<RoomMemberEntity> {
        return dao.getMembersForRoomOneShot(roomId)
    }

    suspend fun getRoomById(roomId: String): RoomEntity? {
        return dao.getRoomById(roomId)
    }

    suspend fun leaveRoom(roomId: String, memberDisplayId: String) {
        dao.deleteMember(roomId, memberDisplayId)
        // If no members left or creator leaves, delete room
        val members = dao.getMembersForRoomOneShot(roomId)
        if (members.isEmpty()) {
            dao.deleteRoom(roomId)
        }
    }

    // --- Message Management ---
    fun getRoomMessages(roomId: String): Flow<List<MessageEntity>> {
        return dao.getRoomMessages(roomId)
    }

    fun getDirectMessages(otherUserDisplayId: String): Flow<List<MessageEntity>> {
        // Find messages between ourselves and otherUserDisplayId
        // Needs current username. Better resolved in ViewModel.
        return dao.getDirectMessages("", otherUserDisplayId)
    }

    fun getDirectMessagesEx(myDisplayId: String, otherUserDisplayId: String): Flow<List<MessageEntity>> {
        return dao.getDirectMessages(myDisplayId, otherUserDisplayId)
    }

    suspend fun sendMessage(
        roomId: String?,
        senderId: String,
        recipientId: String?,
        textBody: String,
        isFile: Boolean = false,
        fileName: String? = null,
        fileSize: String? = null
    ): MessageEntity {
        val encrypted = CryptoEngine.encrypt(textBody)
        val message = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            roomId = roomId,
            senderId = senderId,
            recipientId = recipientId,
            encryptedBody = encrypted,
            timestamp = System.currentTimeMillis(),
            isFile = isFile,
            fileName = fileName,
            fileSize = fileSize,
            transferProgress = if (isFile) 100 else 0,
            transferSpeed = if (isFile) "N/A" else null,
            transferStatus = if (isFile) "COMPLETED" else null
        )
        dao.insertMessage(message)
        return message
    }

    suspend fun insertP2PFileTransfer(
        roomId: String?,
        senderId: String,
        recipientId: String?,
        fileName: String,
        fileSize: String,
        speed: String
    ): String {
        // Starts a simulated secure P2P transfer
        val messageId = UUID.randomUUID().toString()
        val placeholderCipher = CryptoEngine.encrypt("P2P_FILE_TRANSFER:$fileName")
        val message = MessageEntity(
            messageId = messageId,
            roomId = roomId,
            senderId = senderId,
            recipientId = recipientId,
            encryptedBody = placeholderCipher,
            timestamp = System.currentTimeMillis(),
            isFile = true,
            fileName = fileName,
            fileSize = fileSize,
            transferProgress = 0,
            transferSpeed = speed,
            transferStatus = "TRANSFERRING"
        )
        dao.insertMessage(message)
        return messageId
    }

    suspend fun updateFileTransferProgress(messageId: String, progress: Int, speed: String, status: String) {
        dao.updateMessageTransfer(messageId, progress, speed, status)
    }

    // --- Call Log Operations ---
    suspend fun logCall(peerDisplayId: String, callType: String, isOutgoing: Boolean, durationSeconds: Int, status: String): CallLogEntity {
        val fingerprint = CryptoEngine.generateFingerprint(peerDisplayId, "Me")
        val call = CallLogEntity(
            callId = UUID.randomUUID().toString(),
            peerDisplayId = peerDisplayId,
            callType = callType,
            isOutgoing = isOutgoing,
            status = status,
            durationSeconds = durationSeconds,
            verificationCode = fingerprint,
            timestamp = System.currentTimeMillis()
        )
        dao.insertCallLog(call)
        return call
    }
}
