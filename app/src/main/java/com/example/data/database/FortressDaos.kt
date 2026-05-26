package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FortressDao {

    // --- Local Profile ---
    @Query("SELECT * FROM local_profile LIMIT 1")
    fun getLocalProfile(): Flow<LocalProfile?>

    @Query("SELECT * FROM local_profile LIMIT 1")
    suspend fun getLocalProfileOneShot(): LocalProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: LocalProfile)

    @Query("UPDATE local_profile SET displayId = :displayId WHERE internalUuid = :internalUuid")
    suspend fun updateDisplayId(internalUuid: String, displayId: String)

    @Query("UPDATE local_profile SET isPublicDiscovery = :isPublic WHERE internalUuid = :internalUuid")
    suspend fun updatePublicDiscovery(internalUuid: String, isPublic: Boolean)


    // --- Secure Rooms ---
    @Query("SELECT * FROM secure_rooms ORDER BY createdAt DESC")
    fun getRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM secure_rooms WHERE roomId = :roomId")
    suspend fun getRoomById(roomId: String): RoomEntity?

    @Query("SELECT * FROM secure_rooms WHERE name = :name")
    suspend fun getRoomByName(name: String): RoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Query("DELETE FROM secure_rooms WHERE roomId = :roomId")
    suspend fun deleteRoom(roomId: String)


    // --- Room Members ---
    @Query("SELECT * FROM room_members WHERE roomId = :roomId")
    fun getMembersForRoom(roomId: String): Flow<List<RoomMemberEntity>>

    @Query("SELECT * FROM room_members WHERE roomId = :roomId")
    suspend fun getMembersForRoomOneShot(roomId: String): List<RoomMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: RoomMemberEntity)

    @Query("DELETE FROM room_members WHERE roomId = :roomId AND memberDisplayId = :memberDisplayId")
    suspend fun deleteMember(roomId: String, memberDisplayId: String)


    // --- Messages ---
    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getRoomMessages(roomId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE (senderId = :user1 AND recipientId = :user2) OR (senderId = :user2 AND recipientId = :user1) ORDER BY timestamp ASC")
    fun getDirectMessages(user1: String, user2: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE messages SET transferProgress = :progress, transferSpeed = :speed, transferStatus = :status WHERE messageId = :messageId")
    suspend fun updateMessageTransfer(messageId: String, progress: Int, speed: String?, status: String)


    // --- Secured Contacts ---
    @Query("SELECT * FROM secured_contacts")
    fun getContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM secured_contacts WHERE displayId = :displayId")
    suspend fun getContactByDisplayId(displayId: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Query("DELETE FROM secured_contacts WHERE displayId = :displayId")
    suspend fun deleteContact(displayId: String)


    // --- Call Logs ---
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getCallLogs(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLogEntity)
}
