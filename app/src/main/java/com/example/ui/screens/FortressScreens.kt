package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.*
import com.example.security.CryptoEngine
import com.example.ui.theme.*
import com.example.ui.viewmodel.CallSession
import com.example.ui.viewmodel.FortressViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

// Format dates
fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FortressViewModel,
    onNavigateToRoom: (String) -> Unit,
    onNavigateToDirectChat: (ContactEntity) -> Unit,
    onTriggerHelp: () -> Unit
) {
    val profile by viewModel.localProfile.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val currentCalls by viewModel.callLogs.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Rooms, 1: Contact handshakes, 2: Security Logs
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var editProfileDialog by remember { mutableStateOf(false) }
    var searchContactPopup by remember { mutableStateOf(false) }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchedContact by viewModel.searchedContact.collectAsState()
    val searchStatus by viewModel.searchStatus.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
    ) {
        // Aesthetic ambient grid circles to break generic styling
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1200FF87), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.2f),
                    radius = size.width * 0.7f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x0E00C3FF), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.8f),
                    radius = size.width * 0.7f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header (Matches Design HTML)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FORTRESS",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "ZERO-KNOWLEDGE NODE",
                        color = GuardGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Help Shield Button
                    IconButton(
                        onClick = onTriggerHelp,
                        modifier = Modifier.testTag("help_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Security Info",
                            tint = SecureBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Avatar (Matches bg-slate-800 border-slate-700)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF334155), CircleShape)
                            .clickable { editProfileDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile?.displayId?.take(2) ?: "@F",
                            color = GuardGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Identity Card (Frosted Glass - Matches Design HTML)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF)), // bg-white/5
                border = BorderStroke(1.dp, Color(0x1BFFFFFF)) // border-white/10
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE IDENTITY",
                                color = Color(0xFF94A3B8), // text-slate-400
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = profile?.displayId ?: "@anonymous_node",
                                color = TextWhite,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }

                        // Edit ID styled button (Matches bg-white/10 px-3 py-1)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x1AFFFFFF))
                                .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(12.dp))
                                .clickable { editProfileDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "EDIT ID",
                                color = TextWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Discovery Mode Row (Matches bg-black/40 rounded-2xl border border-white/5)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x66000000))
                            .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.togglePublicDiscovery(!(profile?.isPublicDiscovery ?: true))
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse"
                            )

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        (if (profile?.isPublicDiscovery == true) GuardGreen else SecureBlue).copy(
                                            alpha = pulseAlpha
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (profile?.isPublicDiscovery == true) "Public Discovery Active" else "Stealth Discovery (Fortified)",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Custom animated toggle bar representation
                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .height(18.dp)
                                .clip(CircleShape)
                                .background(if (profile?.isPublicDiscovery == true) GuardGreen.copy(0.85f) else Color(0xFF334155))
                                .padding(horizontal = 2.dp),
                            contentAlignment = if (profile?.isPublicDiscovery == true) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(if (profile?.isPublicDiscovery == true) OledBlack else TextWhite)
                            )
                        }
                    }
                }
            }

            // Quick Stats / Identity Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Find node
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            viewModel.changeSearchQuery("")
                            searchContactPopup = true
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x3B0F172A)),
                    border = BorderStroke(1.dp, Color(0x0DFFFFFF))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.PersonAdd, "Find User", tint = GuardGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verify ID", color = TextWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                // Add Room card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showCreateRoomDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x3B0F172A)),
                    border = BorderStroke(1.dp, Color(0x0DFFFFFF))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AddHome, "Create Room", tint = SecureBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Engage Room", color = TextWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Section Header Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (activeTab) {
                    0 -> {
                        Text(
                            text = "SECURE ENCLAVES",
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "NEW ROOM",
                            color = GuardGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clickable { showCreateRoomDialog = true }
                                .padding(4.dp)
                        )
                    }
                    1 -> {
                        Text(
                            text = "VERIFIED HANDSHAKES",
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "ADD NODE",
                            color = GuardGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clickable {
                                    viewModel.changeSearchQuery("")
                                    searchContactPopup = true
                                }
                                .padding(4.dp)
                        )
                    }
                    2 -> {
                        Text(
                            text = "VOICE TRANSMISSION RECEIPTS",
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Content dynamic display area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                when (activeTab) {
                    0 -> {
                        if (rooms.isEmpty()) {
                            EmptyTerminalState(
                                icon = Icons.Default.FilterList,
                                title = "NO SECURE REGISTRIES IN CONSOLE",
                                description = "You have no active zero-knowledge Private Rooms. Tap 'Engage Room' to establish a private, PIN-gated zone.",
                                actionText = "Establish Room Node",
                                actionClick = { showCreateRoomDialog = true }
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.testTag("rooms_list")
                            ) {
                                items(rooms) { room ->
                                    RoomItemCard(room = room, onEnter = { onNavigateToRoom(room.roomId) })
                                }
                            }
                        }
                    }
                    1 -> {
                        if (contacts.isEmpty()) {
                            EmptyTerminalState(
                                icon = Icons.Default.LockOpen,
                                title = "NO SECRECY PEERS LINKED",
                                description = "No handshakes recorded. You must verify a client's Alpha Display ID (e.g., @cyber_scout) to engage direct messaging or P2P calls.",
                                actionText = "Link Secure Key Node",
                                actionClick = { searchContactPopup = true }
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.testTag("contacts_list")
                            ) {
                                items(contacts) { contact ->
                                    ContactItemCard(
                                        contact = contact,
                                        onSelected = { onNavigateToDirectChat(contact) },
                                        onTriggerCall = { typ -> viewModel.initiateCall(contact.displayId, typ) },
                                        onDelete = { viewModel.deleteContact(contact.displayId) }
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        if (currentCalls.isEmpty()) {
                            EmptyTerminalState(
                                icon = Icons.Default.Voicemail,
                                title = "VOIP DIRECTORY EMPTY",
                                description = "No encrypted peer voice or video recordings. Trigger a P2P call with linked nodes to authenticate high-security cipher tunnels.",
                                actionText = "Check Online Nodes",
                                actionClick = { activeTab = 1 }
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(currentCalls) { call ->
                                    CallReceiptItemCard(call = call)
                                }
                            }
                        }
                    }
                }
            }

            // Sleek High Density Bottom Navigation (Matches Design HTML)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OledBlack)
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x14FFFFFF))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chats (Rooms) Tab
                    Column(
                        modifier = Modifier
                            .clickable { activeTab = 0 }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = "Chats",
                            tint = if (activeTab == 0) GuardGreen else Color(0xFF475569),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "ROOMS",
                            color = if (activeTab == 0) GuardGreen else Color(0xFF475569),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Handshakes (Contact list) Tab
                    Column(
                        modifier = Modifier
                            .clickable { activeTab = 1 }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Handshakes",
                            tint = if (activeTab == 1) GuardGreen else Color(0xFF475569),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "PEERS",
                            color = if (activeTab == 1) GuardGreen else Color(0xFF475569),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Calls Tab
                    Column(
                        modifier = Modifier
                            .clickable { activeTab = 2 }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = "Receipts",
                            tint = if (activeTab == 2) GuardGreen else Color(0xFF475569),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "RECEIPTS",
                            color = if (activeTab == 2) GuardGreen else Color(0xFF475569),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // PROFILE EDIT POPUP
        if (editProfileDialog) {
            var inputId by remember { mutableStateOf(profile?.displayId?.drop(1) ?: "") }
            var isPublicState by remember { mutableStateOf(profile?.isPublicDiscovery ?: true) }

            AlertDialog(
                onDismissRequest = { editProfileDialog = false },
                containerColor = CardDark,
                title = {
                    Text("Identity Controller Panel", color = GuardGreen, fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Permanent Client UUID:\n${profile?.internalUuid}",
                            fontSize = 11.sp,
                            color = TextGray,
                            fontFamily = FontFamily.Monospace
                        )
                        Divider(color = FrostedGlassWhite)
                        
                        OutlinedTextField(
                            value = inputId,
                            onValueChange = { inputId = it.take(20) },
                            label = { Text("Display Unique ID", color = SecureBlue) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = GuardGreen,
                                unfocusedBorderColor = TextGray
                            ),
                            leadingIcon = { Text("@", color = GuardGreen, fontWeight = FontWeight.Bold) }
                        )
                        Text(
                            text = "Changing this custom alphanumeric ID automatically invalidates all past sharing handshake invites! Peer endpoints must discover your new ID.",
                            fontSize = 10.sp,
                            color = AccentGold,
                            lineHeight = 13.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPublicState = !isPublicState }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = isPublicState,
                                onCheckedChange = { isPublicState = it },
                                colors = CheckboxDefaults.colors(checkedColor = GuardGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enable Public Discovery", color = TextWhite, fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = GuardGreen),
                        onClick = {
                            if (inputId.isNotBlank()) {
                                viewModel.updateDisplayId(inputId)
                                viewModel.togglePublicDiscovery(isPublicState)
                                editProfileDialog = false
                            }
                        }
                    ) {
                        Text("AUTHENTICATE CHANGE", color = OledBlack, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editProfileDialog = false }) {
                        Text("ABORT", color = TextGray)
                    }
                }
            )
        }

        // CREATE PRIVATE ROOM DIALOG
        if (showCreateRoomDialog) {
            var roomName by remember { mutableStateOf("") }
            var pinCode by remember { mutableStateOf("") }
            var createError by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showCreateRoomDialog = false },
                containerColor = CardDark,
                title = {
                    Text("Establish Private Zone", color = GuardGreen, fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Create a local cryptographic enclave. Entry requires manual PIN validation. Your database records stay securely partitioned.")
                        OutlinedTextField(
                            value = roomName,
                            onValueChange = { roomName = it.take(24) },
                            label = { Text("Private Room Name") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GuardGreen)
                        )
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 6) pinCode = it },
                            label = { Text("Lock PIN Key (Numeric)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GuardGreen)
                        )
                        if (createError != null) {
                            Text(createError!!, color = DangerRed, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = GuardGreen),
                        onClick = {
                            val trimmedName = roomName.trim()
                            val trimmedPin = pinCode.trim()
                            if (trimmedName.isEmpty() || trimmedPin.length < 4) {
                                createError = "Failure: Name must not be empty and PIN key must be 4-6 digits."
                            } else {
                                viewModel.createRoom(trimmedName, trimmedPin)
                                showCreateRoomDialog = false
                            }
                        }
                    ) {
                        Text("DEPLOY ENCLAVE", color = OledBlack, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateRoomDialog = false }) {
                        Text("ABORT", color = TextGray)
                    }
                }
            )
        }

        // VERIFY CLIENT ID (DISCOVERY SEARCH) POPUP
        if (searchContactPopup) {
            AlertDialog(
                onDismissRequest = { searchContactPopup = false },
                containerColor = CardDark,
                title = {
                    Text("P2P Client Verification", color = SecureBlue, fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Direct Discovery: Locate any node in the fortress system using their Display Unique ID. No email, phone numbers, or traces recorded.")
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.changeSearchQuery(it) },
                            label = { Text("Display ID (e.g. @cyber_scout)") },
                            placeholder = { Text("@name") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SecureBlue),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { viewModel.initiateSearch() }) {
                                    Icon(Icons.Default.Search, "Query Node", tint = SecureBlue)
                                }
                            }
                        )

                        // Search response feedback area
                        when (searchStatus) {
                            "FOUND" -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GuardGreen.copy(0.1f))
                                        .border(1.dp, GuardGreen.copy(0.3f), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(GuardGreen.copy(0.2f))
                                        ) {
                                            Icon(
                                                Icons.Default.VerifiedUser,
                                                "Active",
                                                tint = GuardGreen,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(searchedContact?.nickname ?: "Node Ready", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(searchedContact?.displayId ?: "", color = GuardGreen, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                            "NOT_FOUND" -> {
                                Text(
                                    text = "⚠️ NO PEER MATCH FOUND\nDatabase cannot locate any online profile registered to this ID. Handshake rejected.",
                                    color = DangerRed,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            "NOT_PUBLIC" -> {
                                Text(
                                    text = "🔒 PEER STEALTH REDIRECT\nFound profile. However, this peer has enabled stealth routing. Peer endpoints are hidden.",
                                    color = AccentGold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Text(
                            text = "💡 Try entering pre-seeded network nodes for quick simulation testing:\n• @cyber_scout\n• @operator_9\n• @alpha_7x\n• @phantom_gate",
                            color = TextGray,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                },
                confirmButton = {
                    if (searchStatus == "FOUND" && searchedContact != null) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = GuardGreen),
                            onClick = {
                                viewModel.addSearchedContact()
                                searchContactPopup = false
                            }
                        ) {
                            Text("SAVE SECURE HANDSHAKE", color = OledBlack, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { searchContactPopup = false }) {
                        Text("HIDE PANEL", color = TextGray)
                    }
                }
            )
        }
    }
}

// --- SUB-COMPONENTS FOR DASHBOARD ---

@Composable
fun EmptyTerminalState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    actionText: String,
    actionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(CardDark)
                .border(1.dp, FrostedGlassWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = GuardGreen, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = GuardGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            color = TextGray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = actionClick,
            colors = ButtonDefaults.buttonColors(containerColor = SecureBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(actionText, color = OledBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun RoomItemCard(room: RoomEntity, onEnter: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEnter() },
        shape = RoundedCornerShape(16.dp), // rounded-2xl
        colors = CardDefaults.cardColors(containerColor = Color(0x3B0F172A)), // bg-slate-900/50
        border = BorderStroke(1.dp, Color(0x0DFFFFFF)) // border border-white/5
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp)) // rounded-xl
                    .background(Color(0xFF1E293B)), // bg-slate-800
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Locked Room", tint = SecureBlue, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.name,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "PIN verification mandatory • Active Node Enclave",
                    color = Color(0xFF64748B), // text-slate-500
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Default.ChevronRight, "Enter Room", tint = GuardGreen, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactItemCard(
    contact: ContactEntity,
    onSelected: () -> Unit,
    onTriggerCall: (String) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        shape = RoundedCornerShape(16.dp), // rounded-2xl
        colors = CardDefaults.cardColors(containerColor = Color(0x3B0F172A)), // bg-slate-900/50
        border = BorderStroke(1.dp, if (contact.isOnline) GuardGreen.copy(0.25f) else Color(0x0DFFFFFF))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status online dot
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(contact.avatarColorHex)).copy(0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.displayId.substring(1, 3).uppercase(),
                            color = Color(android.graphics.Color.parseColor(contact.avatarColorHex)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    if (contact.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(GuardGreen)
                                .border(1.5.dp, OledBlack, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.nickname,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = contact.displayId,
                        color = GuardGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }

                // Call buttons inline trigger
                IconButton(onClick = { onTriggerCall("VOICE") }) {
                    Icon(Icons.Outlined.Call, "Encrypted Voice", tint = SecureBlue, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { onTriggerCall("VIDEO") }) {
                    Icon(Icons.Outlined.Videocam, "Encrypted Video", tint = GuardGreen, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.MoreVert, "More", tint = TextGray)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A)) // bg-slate-900
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tunnel Status: ${contact.statusText}",
                        color = TextGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    TextButton(onClick = {
                        onDelete()
                        expanded = false
                    }) {
                        Text("Delete Handshake", color = DangerRed, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CallReceiptItemCard(call: CallLogEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), // rounded-2xl
        colors = CardDefaults.cardColors(containerColor = Color(0x3B0F172A)), // bg-slate-900/50
        border = BorderStroke(1.dp, Color(0x0DFFFFFF)) // border border-white/5
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (call.callType == "VOICE") Icons.Default.PhoneInTalk else Icons.Default.VideoCameraFront,
                    contentDescription = call.callType,
                    tint = if (call.isOutgoing) GuardGreen else SecureBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (call.isOutgoing) "OUTGOING CIPHER STREAM" else "INCOMING HANDSHAKE STREAM",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = formatTime(call.timestamp), color = TextGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Target Node: ${call.peerDisplayId}",
                color = GuardGreen,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, "Security Matches", tint = AccentGold, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Fingerprint: ${call.verificationCode}",
                    color = AccentGold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Duration: ${call.durationSeconds}s • Network Layer: WebRTC P2P direct",
                color = Color(0xFF64748B), // text-slate-500
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}


// --- PRIVATE SECURE ROOM DETAIL SCREEN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    viewModel: FortressViewModel,
    roomId: String,
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.localProfile.collectAsState()
    val messages by viewModel.currentRoomMessages.collectAsState()
    val members by viewModel.currentRoomMembers.collectAsState()
    val verifiedRooms by viewModel.verifiedRooms.collectAsState()
    val pinError by viewModel.roomPinError.collectAsState()

    var activeViewTab by remember { mutableStateOf(0) } // 0: Chat messages, 1: Member Keys, 2: P2P Stream files
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var inputMessage by remember { mutableStateOf("") }
    var pinVerifiedState by remember { mutableStateOf(verifiedRooms.contains(roomId)) }
    var pinEntered by remember { mutableStateOf("") }

    // P2P File sending helper states
    var fileSelectedName by remember { mutableStateOf("fortress_document_raw.bin") }
    var fileSelectedSize by remember { mutableStateOf("14.5 MB") }

    if (!pinVerifiedState) {
        // Enforce Gatekeeper Security Lock PIN dialog
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OledBlack)
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(1.dp, SecureBlue, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = PanelDark)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SecureBlue.copy(0.15f))
                            .border(1.5.dp, SecureBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, "Required Gatekeeper key", tint = SecureBlue, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ROOM PORT DECRYPT GATEKEY",
                        color = SecureBlue,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Entry is subject to dynamic PIN verification hashing (Signal/Argon2 protocol block). Key is resolved locally.",
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = pinEntered,
                        onValueChange = { if (it.length <= 6) pinEntered = it },
                        label = { Text("Local Security PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = SecureBlue,
                            unfocusedBorderColor = TextGray
                        ),
                        singleLine = true
                    )

                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pinError ?: "",
                            color = DangerRed,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { onNavigateBack() }) {
                            Text("ABORT SESSION", color = TextGray, fontFamily = FontFamily.Monospace)
                        }
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = SecureBlue),
                            onClick = {
                                viewModel.verifyAndEnterRoom(roomId, pinEntered) {
                                    pinVerifiedState = true
                                }
                            }
                        ) {
                            Text("UNLOCK ZONE", color = OledBlack, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    } else {
        // Verified Enclave loaded UI
        var roomName by remember { mutableStateOf("Secured Enclave") }
        LaunchedEffect(roomId) {
            viewModel.enterRoom(roomId)
            // Fetch name safely
            val r = viewModel.rooms.value.find { it.roomId == roomId }
            if (r != null) {
                roomName = r.name
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OledBlack)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header Frosted bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PanelDark)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        viewModel.exitCurrentRoom()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = GuardGreen)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = roomName,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Enclave Crypt ID: ..${roomId.takeLast(12)}",
                            color = GuardGreen,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(onClick = { showAddMemberDialog = true }) {
                        Icon(Icons.Default.GroupAdd, "Invite ID Handshake", tint = GuardGreen)
                    }
                }

                // Inner category view selection row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardDark)
                ) {
                    listOf("DISCRETE LOGS", "HANDSHAKE KEYS", "DIRECT P2P SEEDING").forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeViewTab = index }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column {
                                Text(
                                    text = title,
                                    color = if (activeViewTab == index) GuardGreen else TextGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (activeViewTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (activeViewTab == index) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 24.dp, height = 2.dp)
                                            .background(GuardGreen)
                                            .align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }
                }

                // Main area container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (activeViewTab) {
                        0 -> {
                            // Message stream list
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    items(messages) { msg ->
                                        MessageItemRow(msg = msg, myDisplayId = profile?.displayId ?: "@anonymous")
                                    }
                                }

                                // Interactive input action bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            activeViewTab = 2 // Takes directly to File section
                                        }
                                    ) {
                                        Icon(Icons.Default.AttachFile, "P2P file transfer", tint = SecureBlue)
                                    }

                                    OutlinedTextField(
                                        value = inputMessage,
                                        onValueChange = { inputMessage = it },
                                        placeholder = { Text("Un-routed encrypted log body...", fontSize = 13.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite,
                                            focusedBorderColor = GuardGreen,
                                            unfocusedBorderColor = FrostedGlassWhite
                                        ),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = {
                                            if (inputMessage.isNotBlank()) {
                                                viewModel.sendRoomMessage(inputMessage)
                                                inputMessage = ""
                                            }
                                        },
                                        modifier = Modifier.testTag("send_button")
                                    ) {
                                        Icon(Icons.Default.Send, "Send Cipher", tint = GuardGreen)
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Members identities list
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "VERIFIED CIPHER KEY EXCHANGE LISTING",
                                    color = GuardGreen,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Only listed alphanumeric identities are authorized to trigger handshakes. Relays automatically drop packet structures from unknown identities.",
                                    color = TextGray,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )

                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(members) { member ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = PanelDark),
                                            border = BorderStroke(1.dp, FrostedGlassWhite)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Security, "Secure peer", tint = GuardGreen, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = member.memberDisplayId,
                                                    color = TextWhite,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Text(
                                                    text = "AUTHORIZED NODE",
                                                    color = SecureBlue,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                    onClick = {
                                        viewModel.leaveRoom(roomId)
                                        onNavigateBack()
                                    }
                                ) {
                                    Text("DESTROY DECRYPT PORT (LEAVE)", color = TextWhite, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        2 -> {
                            // NO-LIMIT DIRECT PEER FILE STREAMS LOG
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "UNLIMITED DIRECT P2P SHIELD STREAMING",
                                    color = SecureBlue,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Files are streamed direct peer-to-peer. Zero server storage, meaning absolutely zero trace footprints. Storage threshold maximum is infinite.",
                                    color = TextGray,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = PanelDark),
                                    border = BorderStroke(1.dp, SecureBlue.copy(0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Stream New Local Artifact Payload", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        // Fake selectable file list values to support simple simulation select
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            listOf(
                                                Pair("log_secure.key", "4.2 KB"),
                                                Pair("voice_scrambled.wav", "45 MB"),
                                                Pair("backup_partition.img", "1.4 GB")
                                            ).forEach { (name, size) ->
                                                val isSelected = fileSelectedName == name
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSelected) SecureBlue.copy(0.2f) else CardDark)
                                                        .border(1.dp, if (isSelected) SecureBlue else FrostedGlassWhite, RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            fileSelectedName = name
                                                            fileSelectedSize = size
                                                        }
                                                        .padding(6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(name, color = TextWhite, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = FontFamily.Monospace)
                                                        Text(size, color = GuardGreen, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                viewModel.startP2PFileStream(
                                                    roomId = roomId,
                                                    recipientId = null,
                                                    fileName = fileSelectedName,
                                                    fileSize = fileSelectedSize
                                                )
                                                activeViewTab = 0 // Return to message tab to observe streaming speed
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SecureBlue),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("INITIATE FILE STREAM HANDSHAKE", color = OledBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }

                                Text(
                                    text = "CURRENT STREAMED ARTIFACT HISTORY",
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Filter and display file messages in the list
                                val fileMessages = messages.filter { it.isFile }
                                if (fileMessages.isEmpty()) {
                                    Text("No secure transmissions in active room session log.", color = TextGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(fileMessages) { file ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(CardDark)
                                                    .border(1.dp, FrostedGlassWhite, RoundedCornerShape(8.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.InsertDriveFile, "File", tint = SecureBlue, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(file.fileName ?: "", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                                        Spacer(modifier = Modifier.weight(1f))
                                                        Text(file.fileSize ?: "", color = GuardGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Sender: ${file.senderId} • Speed: ${file.transferSpeed ?: "0.0 MB/s"}",
                                                        color = TextGray,
                                                        fontSize = 10.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    LinearProgressIndicator(
                                                        progress = file.transferProgress / 100f,
                                                        color = GuardGreen,
                                                        trackColor = FrostedGlassWhite,
                                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                                                    )
                                                    Row {
                                                        Text(
                                                            text = "Status: ${file.transferStatus ?: "PENDING"} (${file.transferProgress}%)",
                                                            color = if (file.transferStatus == "COMPLETED") GuardGreen else SecureBlue,
                                                            fontSize = 9.sp,
                                                            fontFamily = FontFamily.Monospace
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ADD MEMBER INVITE POPUP
            if (showAddMemberDialog) {
                var memberIdInput by remember { mutableStateOf("") }
                var inviteErrorText by remember { mutableStateOf<String?>(null) }

                AlertDialog(
                    onDismissRequest = { showAddMemberDialog = false },
                    containerColor = CardDark,
                    title = {
                        Text("Add Node Companion", color = GuardGreen, fontFamily = FontFamily.Monospace, fontSize = 17.sp)
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Authorizing a member gives them direct access to decrypt files. Ensure the Display ID matched is secure.")
                            OutlinedTextField(
                                value = memberIdInput,
                                onValueChange = { memberIdInput = it },
                                label = { Text("Client Display ID") },
                                placeholder = { Text("@cyber_scout") },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GuardGreen)
                            )
                            if (inviteErrorText != null) {
                                Text(inviteErrorText!!, color = DangerRed, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = GuardGreen),
                            onClick = {
                                if (memberIdInput.isBlank()) {
                                    inviteErrorText = "Required: Enter a valid display ID"
                                } else {
                                    viewModel.inviteMemberToRoom(roomId, memberIdInput)
                                    showAddMemberDialog = false
                                }
                            }
                        ) {
                            Text("AUTHORIZE & Handshake", color = OledBlack, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddMemberDialog = false }) {
                            Text("ABORT", color = TextGray)
                        }
                    }
                )
            }
        }
    }
}

// Inner Message Item Row helper
@Composable
fun MessageItemRow(msg: MessageEntity, myDisplayId: String) {
    val isMine = msg.senderId == myDisplayId
    val clipboardManager = LocalClipboardManager.current

    val decryptedBody = remember(msg.encryptedBody) {
        CryptoEngine.decrypt(msg.encryptedBody)
    }

    var showFingerprintInfo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (!isMine) {
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(SecureBlue.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(msg.senderId.take(2).uppercase(), fontSize = 10.sp, color = SecureBlue, fontWeight = FontWeight.Bold)
                }
            }

            // Message Bubble Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isMine) GuardGreen.copy(0.12f) else PanelDark
                ),
                border = BorderStroke(1.dp, if (isMine) GuardGreen.copy(0.3f) else FrostedGlassWhite),
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (isMine) 12.dp else 2.dp,
                    bottomEnd = if (isMine) 2.dp else 12.dp
                ),
                modifier = Modifier.clickable { showFingerprintInfo = !showFingerprintInfo }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    if (!isMine) {
                        Text(msg.senderId, color = GuardGreen, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    if (msg.isFile) {
                        // File transmission body layout style
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.InsertDriveFile, "Stream Transferred", tint = SecureBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(msg.fileName ?: "payload.bin", color = TextWhite, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text("${msg.fileSize ?: "Unchecked"} • Decrypted Locally", color = TextGray, fontSize = 10.sp)
                            }
                        }

                        // Display active progress meter if any
                        if (msg.transferProgress < 100) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = msg.transferProgress / 100f,
                                maxOrLessThan = { 100 },
                                color = SecureBlue,
                                modifier = Modifier.fillMaxWidth().height(2.dp)
                            )
                        }
                    } else {
                        // Raw text message body
                        Text(text = decryptedBody, color = TextWhite, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, "Encrypted", tint = GuardGreen.copy(0.5f), modifier = Modifier.size(9.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = formatTime(msg.timestamp), color = TextGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Dropdown detail panel which shows key digests and copy actions on click
        AnimatedVisibility(visible = showFingerprintInfo) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(top = 4.dp, bottom = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CardDark)
                    .padding(8.dp)
            ) {
                Text(
                    text = "🔐 INTEGRATED SECURITY AUDIT SUMMARY:",
                    fontSize = 8.sp,
                    color = GuardGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Encrypted Block Payload:\n${msg.encryptedBody.take(48)}...",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        contentPadding = PaddingValues(0.dp),
                        onClick = { clipboardManager.setText(AnnotatedString(msg.encryptedBody)) }
                    ) {
                        Text("Copy Cipher Block", color = SecureBlue, fontSize = 9.sp)
                    }

                    TextButton(
                        contentPadding = PaddingValues(0.dp),
                        onClick = { clipboardManager.setText(AnnotatedString(decryptedBody)) }
                    ) {
                        Text("Copy Log Decrypted", color = GuardGreen, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

// linear helper for linear progress meter constraint
@Composable
private fun LinearProgressIndicator(
    progress: Float,
    maxOrLessThan: () -> Int,
    color: Color,
    modifier: Modifier
) {
    LinearProgressIndicator(progress = progress, color = color, trackColor = FrostedGlassWhite, modifier = modifier)
}


// --- DIRECT CHAT AND FILE TRANSPORTS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectChatScreen(
    viewModel: FortressViewModel,
    contact: ContactEntity,
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.localProfile.collectAsState()
    val messages by viewModel.currentDirectMessages.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var selectedTransmissionType by remember { mutableStateOf("Keys payload") }
    var writeMsgText by remember { mutableStateOf("") }
    var expandedInfo by remember { mutableStateOf(false) }

    LaunchedEffect(contact) {
        viewModel.selectDirectPeer(contact)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Frosted top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PanelDark)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.selectDirectPeer(null)
                    onNavigateBack()
                }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = GuardGreen)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.nickname,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (contact.isOnline) GuardGreen else TextGray))
                    }
                    Text(
                        text = contact.displayId,
                        color = GuardGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.initiateCall(contact.displayId, "VOICE") },
                    modifier = Modifier.testTag("voice_call_button")
                ) {
                    Icon(Icons.Default.Phone, "Encrypted Voice Stream", tint = SecureBlue)
                }

                IconButton(onClick = { viewModel.initiateCall(contact.displayId, "VIDEO") }) {
                    Icon(Icons.Default.VideoCameraBack, "Encrypted Video Stream", tint = GuardGreen)
                }

                IconButton(onClick = { expandedInfo = !expandedInfo }) {
                    Icon(Icons.Default.LockClock, "Verify Fingerprint Integrity", tint = AccentGold)
                }
            }

            AnimatedVisibility(visible = expandedInfo) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardDark)
                        .border(1.dp, AccentGold.copy(0.3f))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "SECURITY HANDSHAKE VERIFICATION KEY",
                            color = AccentGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Cryptographic fingerprint generated directly between endpoints. Verify on receiver client to match standard E2EE.",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(OledBlack)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.EnhancedEncryption, "Lock Fingerprint", tint = GuardGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = CryptoEngine.generateFingerprint(contact.displayId, profile?.displayId ?: "Me"),
                                color = GuardGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                val fingerprint = CryptoEngine.generateFingerprint(contact.displayId, profile?.displayId ?: "Me")
                                clipboardManager.setText(AnnotatedString(fingerprint))
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Copy Handshake fingerprint", color = SecureBlue, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Message scrolling frame
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Fingerprint, "Cryptographic handshake ready", tint = SecureBlue, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("SECURE TUNNEL IDLE", color = SecureBlue, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Signal protocol exchange completed. Ready to stream encrypted payloads & files P2P.", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(messages) { msg ->
                            MessageItemRow(msg = msg, myDisplayId = profile?.displayId ?: "@anonymous")
                        }
                    }
                }
            }

            // Dynamic interactive speed transfers pane
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PanelDark)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Transmit P2P Artifact:", color = TextGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.width(6.dp))
                
                listOf(
                    Pair("payload.img", "420 MB"),
                    Pair("audit_data.pdf", "8.9 MB")
                ).forEach { (shName, shSize) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedTransmissionType == shName) SecureBlue.copy(0.15f) else CardDark)
                            .border(1.dp, if (selectedTransmissionType == shName) SecureBlue else FrostedGlassWhite, RoundedCornerShape(6.dp))
                            .clickable { selectedTransmissionType = shName }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("$shName ($shSize)", color = TextWhite, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                IconButton(
                    onClick = {
                        val size = if (selectedTransmissionType == "payload.img") "420 MB" else "8.9 MB"
                        viewModel.startP2PFileStream(
                            roomId = null,
                            recipientId = contact.displayId,
                            fileName = selectedTransmissionType,
                            fileSize = size
                        )
                    }
                ) {
                    Icon(Icons.Default.SendToMobile, "Transceive", tint = SecureBlue, modifier = Modifier.size(20.dp))
                }
            }

            // Message input bottom bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PanelDark)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = writeMsgText,
                    onValueChange = { writeMsgText = it },
                    placeholder = { Text("Un-routed direct crypto log...", fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = GuardGreen,
                        unfocusedBorderColor = FrostedGlassWhite
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        if (writeMsgText.isNotBlank()) {
                            viewModel.sendDirectMessage(writeMsgText)
                            writeMsgText = ""
                        }
                    },
                    modifier = Modifier.testTag("direct_send_button")
                ) {
                    Icon(Icons.Default.Send, "Transmit Signal", tint = GuardGreen)
                }
            }
        }
    }
}


// --- ENCRYPTED WEBRTC VOICE & VIDEO CALL SCREEN COMPONENT ---

@Composable
fun CallScreen(
    viewModel: FortressViewModel,
    callSession: CallSession
) {
    // Phase timer variables to drive Canvas vibration
    val infiniteTransition = rememberInfiniteTransition(label = "audio_vibrate")
    val phaseFactor by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val randomVibe = remember { List(30) { Random.nextFloat() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Secrecy Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(FrostedGlassGreen)
                    .border(1.dp, GuardGreen.copy(0.4f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, "Encrypted", tint = GuardGreen, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "END-TO-END SECURE STREAM (P2P)",
                    color = GuardGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Caller identity
            Text(
                text = callSession.peerDisplayId,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = callSession.statusText,
                color = SecureBlue,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            // Duration timer display
            val min = callSession.durationSeconds / 60
            val sec = callSession.durationSeconds % 60
            val formattedTime = String.format("%02d:%02d", min, sec)

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = formattedTime,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.weight(0.1f))

            // CALL INTERFACE: High Quality Interactive Audio Audio Visualizers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PanelDark)
                    .border(1.dp, FrostedGlassWhite, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Compose Custom drawing Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    val barWidthPx = 6.dp.toPx()
                    val gapPx = 4.dp.toPx()
                    val barCount = (size.width / (barWidthPx + gapPx)).toInt()
                    
                    for (i in 0 until barCount) {
                        val progress = i.toFloat() / barCount
                        // Trigonometric function modulated with high phase frequency
                        val baseSine = kotlin.math.sin(progress * 3.5f * 3.141592f + phaseFactor)
                        val extraNoise = kotlin.math.sin(progress * 12f + phaseFactor * 3f)
                        
                        val rawNormalized = (baseSine * 0.4f + extraNoise * 0.2f + 0.5f).coerceIn(0f, 1f)
                        
                        val heightMultiplier = if (callSession.statusText.contains("SECURE ACTIVE")) {
                            rawNormalized
                        } else {
                            0.05f // Negotating/Connecting - flat line subtle pulse
                        }

                        val barHeight = kotlin.math.max(8.dp.toPx(), size.height * heightMultiplier * 0.85f)
                        val x = i * (barWidthPx + gapPx)
                        val y = (size.height - barHeight) / 2

                        drawRoundRect(
                            color = if (i % 2 == 0) GuardGreen else SecureBlue,
                            topLeft = Offset(x, y),
                            size = Size(barWidthPx, barHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Verification Code matched banner overlay
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AccentGold.copy(0.4f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = PanelDark)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, "Verification Key matches", tint = AccentGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SECURITY AUDIT CODE VERIFIED",
                            color = AccentGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = callSession.verificationCode,
                        color = GuardGreen,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "WebRTC Handshake SDPS: ${callSession.connectionFingerprint.takeLast(24)}",
                        color = TextGray,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Control Actions Panel row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic audio toggle Button
                IconButton(
                    onClick = { viewModel.toggleMuteCall() },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (callSession.isMuted) DangerRed.copy(0.2f) else CardDark)
                        .border(1.dp, if (callSession.isMuted) DangerRed else FrostedGlassWhite, CircleShape)
                ) {
                    Icon(
                        imageVector = if (callSession.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (callSession.isMuted) DangerRed else TextWhite
                    )
                }

                // Call Accept Button (if incoming)
                if (!callSession.isOutgoing && callSession.statusText.contains("Incoming")) {
                    IconButton(
                        onClick = { viewModel.acceptIncomingCall() },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(GuardGreen)
                            .testTag("accept_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Accept Call",
                            tint = OledBlack,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Video screen camera toggle
                IconButton(
                    onClick = { viewModel.toggleCameraCall() },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (!callSession.isCameraOn) PanelDark else CardDark)
                        .border(1.dp, FrostedGlassWhite, CircleShape)
                ) {
                    Icon(
                        imageVector = if (callSession.isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Camera",
                        tint = if (callSession.isCameraOn) GuardGreen else TextWhite
                    )
                }

                // End Terminate Call button
                IconButton(
                    onClick = { viewModel.endActiveCall() },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(DangerRed)
                        .testTag("end_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Terminate Encryption",
                        tint = TextWhite
                    )
                }
            }
        }
    }
}

// String limit extension
private operator fun String.rangeTo(other: Int): String {
    return if (this.length > other) this.take(other) else this
}
