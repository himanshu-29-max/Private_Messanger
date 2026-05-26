package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.repository.FortressRepository
import com.example.security.CryptoEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

data class CallSession(
    val callId: String,
    val peerDisplayId: String,
    val callType: String, // "VOICE" or "VIDEO"
    val isOutgoing: Boolean,
    val durationSeconds: Int,
    val verificationCode: String,
    val statusText: String,
    val isMuted: Boolean = false,
    val isCameraOn: Boolean = true,
    val isSpeakerOn: Boolean = false,
    val connectionFingerprint: String = ""
)

class FortressViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FortressRepository(application)

    // Flows observed in Compose
    val localProfile: StateFlow<LocalProfile?> = repository.localProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rooms: StateFlow<List<RoomEntity>> = repository.rooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<ContactEntity>> = repository.contacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callLogs: StateFlow<List<CallLogEntity>> = repository.callLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchedContact = MutableStateFlow<ContactEntity?>(null)
    val searchedContact = _searchedContact.asStateFlow()

    private val _searchStatus = MutableStateFlow<String?>(null) // "FOUND", "NOT_FOUND", "NOT_PUBLIC", null
    val searchStatus = _searchStatus.asStateFlow()

    // Room context
    private val _enteredRoomId = MutableStateFlow<String?>(null)
    val enteredRoomId = _enteredRoomId.asStateFlow()

    private val _verifiedRooms = MutableStateFlow<Set<String>>(emptySet())
    val verifiedRooms = _verifiedRooms.asStateFlow()

    private val _roomPinError = MutableStateFlow<String?>(null)
    val roomPinError = _roomPinError.asStateFlow()

    val currentRoomMessages: StateFlow<List<MessageEntity>> = _enteredRoomId
        .flatMapLatest { roomId ->
            if (roomId == null) flowOf(emptyList())
            else repository.getRoomMessages(roomId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentRoomMembers: StateFlow<List<RoomMemberEntity>> = _enteredRoomId
        .flatMapLatest { roomId ->
            if (roomId == null) flowOf(emptyList())
            else repository.getMembersForRoom(roomId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Direct msg context
    private val _activeDirectPeer = MutableStateFlow<ContactEntity?>(null)
    val activeDirectPeer = _activeDirectPeer.asStateFlow()

    val currentDirectMessages: StateFlow<List<MessageEntity>> = combine(
        localProfile.filterNotNull(),
        _activeDirectPeer.filterNotNull()
    ) { profile, peer ->
        Pair(profile.displayId, peer.displayId)
    }.flatMapLatest { (myId, otherId) ->
        repository.getDirectMessagesEx(myId, otherId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ongoing Active Call Stream
    private val _activeCall = MutableStateFlow<CallSession?>(null)
    val activeCall = _activeCall.asStateFlow()

    private var callTimerJob: Job? = null
    private var simulatedP2PJobs = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
        }
    }

    // --- Profile Settings ---
    fun updateDisplayId(newId: String) {
        viewModelScope.launch {
            val formatted = if (newId.startsWith("@")) newId else "@$newId"
            repository.updateDisplayId(formatted)
            // Simulates that when user updates their ID, previous room links/credentials reset/expire
            // to preserve core anonymity!
            _verifiedRooms.value = emptySet() 
        }
    }

    fun togglePublicDiscovery(isPublic: Boolean) {
        viewModelScope.launch {
            repository.updatePublicDiscovery(isPublic)
        }
    }

    // --- Contacts Discovery ---
    fun changeSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchedContact.value = null
            _searchStatus.value = null
        }
    }

    fun initiateSearch() {
        val query = _searchQuery.value
        if (query.isBlank()) return
        viewModelScope.launch {
            val contact = repository.searchContactByDisplayId(query)
            if (contact != null) {
                // If contact is found, check if they are discoverable (mock simulation)
                // Let's pretend @shadow_ops is offline/private if discovery toggle matches
                if (contact.displayId == "@shadow_ops") {
                    _searchStatus.value = "NOT_PUBLIC"
                    _searchedContact.value = null
                } else {
                    _searchStatus.value = "FOUND"
                    _searchedContact.value = contact
                }
            } else {
                _searchStatus.value = "NOT_FOUND"
                _searchedContact.value = null
            }
        }
    }

    fun addSearchedContact() {
        val contact = _searchedContact.value ?: return
        viewModelScope.launch {
            repository.addContact(contact)
            // Reset query and matches
            _searchQuery.value = ""
            _searchedContact.value = null
            _searchStatus.value = null
        }
    }

    fun deleteContact(displayId: String) {
        viewModelScope.launch {
            repository.deleteContact(displayId)
            if (_activeDirectPeer.value?.displayId == displayId) {
                _activeDirectPeer.value = null
            }
        }
    }

    // --- Secure Private Rooms ---
    fun createRoom(name: String, pin: String) {
        viewModelScope.launch {
            val myId = localProfile.value?.displayId ?: "@anonymous"
            val roomId = repository.createRoom(name, pin, myId)
            // Auto lock & enter Room
            _verifiedRooms.value = _verifiedRooms.value + roomId
            _enteredRoomId.value = roomId
        }
    }

    fun enterRoom(roomId: String) {
        _roomPinError.value = null
        // If already verified, enter direct!
        if (_verifiedRooms.value.contains(roomId)) {
            _enteredRoomId.value = roomId
        } else {
            // Screen handles triggering PIN input popup
        }
    }

    fun verifyAndEnterRoom(roomId: String, enteredPin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val matched = repository.verifyRoomPin(roomId, enteredPin)
            if (matched) {
                _verifiedRooms.value = _verifiedRooms.value + roomId
                _enteredRoomId.value = roomId
                _roomPinError.value = null
                onSuccess()
            } else {
                _roomPinError.value = "Verification Failure: SECURE PIN INVALID"
            }
        }
    }

    fun exitCurrentRoom() {
        _enteredRoomId.value = null
        _roomPinError.value = null
    }

    fun inviteMemberToRoom(roomId: String, displayId: String) {
        viewModelScope.launch {
            val formatted = if (displayId.startsWith("@")) displayId else "@$displayId"
            repository.addMemberToRoom(roomId, formatted)
            // Add as placeholder contact as well
            if (repository.searchContactByDisplayId(formatted) == null) {
                repository.addContact(ContactEntity(
                    displayId = formatted,
                    nickname = formatted.drop(1).replaceFirstChar { it.uppercase() } + " Network Node"
                ))
            }
            // Send automatic room setup handshake greetings
            repository.sendMessage(
                roomId = roomId,
                senderId = formatted,
                recipientId = null,
                textBody = "SECURE PROTOCOL ENGAGED: Handshake broadcast set from node $formatted."
            )
        }
    }

    fun leaveRoom(roomId: String) {
        viewModelScope.launch {
            val myId = localProfile.value?.displayId ?: "@anonymous"
            repository.leaveRoom(roomId, myId)
            _verifiedRooms.value = _verifiedRooms.value - roomId
            if (_enteredRoomId.value == roomId) {
                _enteredRoomId.value = null
            }
        }
    }

    // --- Message Broadcasting & Direct Messaging ---
    fun selectDirectPeer(contact: ContactEntity?) {
        _activeDirectPeer.value = contact
    }

    fun sendRoomMessage(text: String) {
        val roomId = _enteredRoomId.value ?: return
        val myId = localProfile.value?.displayId ?: "@anonymous"
        viewModelScope.launch {
            repository.sendMessage(
                roomId = roomId,
                senderId = myId,
                recipientId = null,
                textBody = text
            )
        }
    }

    fun sendDirectMessage(text: String) {
        val peer = _activeDirectPeer.value ?: return
        val myId = localProfile.value?.displayId ?: "@anonymous"
        viewModelScope.launch {
            repository.sendMessage(
                roomId = null,
                senderId = myId,
                recipientId = peer.displayId,
                textBody = text
            )
        }
    }

    // --- P2P Direct File Transfers ---
    fun startP2PFileStream(roomId: String?, recipientId: String?, fileName: String, fileSize: String) {
        val myId = localProfile.value?.displayId ?: "@anonymous"
        viewModelScope.launch {
            // Unique identifier
            val speedSeed = Random.nextInt(4, 25)
            val initialSpeed = "$speedSeed.4 MB/s P2P"
            val messageId = repository.insertP2PFileTransfer(
                roomId = roomId,
                senderId = myId,
                recipientId = recipientId,
                fileName = fileName,
                fileSize = fileSize,
                speed = initialSpeed
            )

            // Dynamic stream simulator
            val streamJob = launch {
                for (prog in 10..100 step 15) {
                    delay(500)
                    val speed = "${Random.nextInt(12, 38)}.${Random.nextInt(1, 9)} MB/s"
                    repository.updateFileTransferProgress(messageId, prog, speed, "TRANSFERRING")
                }
                delay(300)
                repository.updateFileTransferProgress(messageId, 100, "0.0 MB/s", "COMPLETED")
                
                // Alert peer automatic confirmation handshake text
                repository.sendMessage(
                    roomId = roomId,
                    senderId = myId,
                    recipientId = recipientId,
                    textBody = "FILE RECEIVED CONFIRMATION: $fileName ($fileSize) checksum verification passed secure hash."
                )
            }
            simulatedP2PJobs[messageId] = streamJob
        }
    }

    // --- P2P WebRTC Calls (Voice & Video) ---
    fun initiateCall(peerDisplayId: String, callType: String) {
        val callId = java.util.UUID.randomUUID().toString()
        val verificationCode = CryptoEngine.generateFingerprint(peerDisplayId, "My_Sentinel_Client")
        
        val initialSession = CallSession(
            callId = callId,
            peerDisplayId = peerDisplayId,
            callType = callType,
            isOutgoing = true,
            durationSeconds = 0,
            verificationCode = verificationCode,
            statusText = "Negotiating WebRTC Handshake...",
            connectionFingerprint = CryptoEngine.generateSdpFingerprint()
        )
        _activeCall.value = initialSession

        // Simulate signalling state machine
        viewModelScope.launch {
            delay(1200)
            _activeCall.value = _activeCall.value?.copy(statusText = "ICE Candidates Exchanged, P2P Routing...")
            delay(1500)
            _activeCall.value = _activeCall.value?.copy(statusText = "STUN/TURN Established • SECURE ACTIVE (E2EE)")
            
            // Start call duration clock
            startCallTicker()
        }
    }

    private fun startCallTicker() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _activeCall.value ?: break
                _activeCall.value = current.copy(durationSeconds = current.durationSeconds + 1)
            }
        }
    }

    fun incomingCallMock(peerDisplayId: String, callType: String) {
        val callId = java.util.UUID.randomUUID().toString()
        val verificationCode = CryptoEngine.generateFingerprint(peerDisplayId, "My_Sentinel_Client")
        
        val callSession = CallSession(
            callId = callId,
            peerDisplayId = peerDisplayId,
            callType = callType,
            isOutgoing = false,
            durationSeconds = 0,
            verificationCode = verificationCode,
            statusText = "Incoming Handshake Request...",
            connectionFingerprint = CryptoEngine.generateSdpFingerprint()
        )
        _activeCall.value = callSession
    }

    fun acceptIncomingCall() {
        val current = _activeCall.value ?: return
        viewModelScope.launch {
            _activeCall.value = current.copy(statusText = "Exchanging SDPS & Certificates...")
            delay(1000)
            _activeCall.value = _activeCall.value?.copy(statusText = "STUN/TURN Established • SECURE ACTIVE (E2EE)")
            startCallTicker()
        }
    }

    fun toggleMuteCall() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isMuted = !current.isMuted)
    }

    fun toggleCameraCall() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isCameraOn = !current.isCameraOn)
    }

    fun toggleSpeakerCall() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isSpeakerOn = !current.isSpeakerOn)
    }

    fun endActiveCall() {
        val current = _activeCall.value ?: return
        callTimerJob?.cancel()
        viewModelScope.launch {
            repository.logCall(
                peerDisplayId = current.peerDisplayId,
                callType = current.callType,
                isOutgoing = current.isOutgoing,
                durationSeconds = current.durationSeconds,
                status = if (current.durationSeconds > 0) "CONNECTED" else "MISSED"
            )
            _activeCall.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        callTimerJob?.cancel()
        simulatedP2PJobs.values.forEach { it.cancel() }
    }
}
