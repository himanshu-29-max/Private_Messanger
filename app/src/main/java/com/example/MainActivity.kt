package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.ContactEntity
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FortressViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: FortressViewModel = viewModel()
        val activeCall by viewModel.activeCall.collectAsState()

        var currentScreen by remember { mutableStateOf("DASHBOARD") }
        var selectedRoomId by remember { mutableStateOf<String?>(null) }
        var selectedContact by remember { mutableStateOf<ContactEntity?>(null) }
        var showHelpDialog by remember { mutableStateOf(false) }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Box(modifier = Modifier.padding(innerPadding)) {
            if (activeCall != null) {
              CallScreen(viewModel = viewModel, callSession = activeCall!!)
            } else {
              when (currentScreen) {
                "DASHBOARD" -> {
                  DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToRoom = { roomId ->
                      selectedRoomId = roomId
                      currentScreen = "ROOM"
                    },
                    onNavigateToDirectChat = { contact ->
                      selectedContact = contact
                      currentScreen = "DIRECT_CHAT"
                    },
                    onTriggerHelp = { showHelpDialog = true }
                  )
                }
                "ROOM" -> {
                  if (selectedRoomId != null) {
                    RoomScreen(
                      viewModel = viewModel,
                      roomId = selectedRoomId!!,
                      onNavigateBack = { currentScreen = "DASHBOARD" }
                    )
                  } else {
                    currentScreen = "DASHBOARD"
                  }
                }
                "DIRECT_CHAT" -> {
                  if (selectedContact != null) {
                    DirectChatScreen(
                      viewModel = viewModel,
                      contact = selectedContact!!,
                      onNavigateBack = { currentScreen = "DASHBOARD" }
                    )
                  } else {
                    currentScreen = "DASHBOARD"
                  }
                }
              }
            }

            // CORE COMPREHENSIVE SECURITY POLICY HELPER POPUP
            if (showHelpDialog) {
              AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                containerColor = CardDark,
                title = {
                  Text(
                    text = "Sentinel Fortress Manual",
                    color = GuardGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                  )
                },
                text = {
                  Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                  ) {
                    Text(
                      text = "1. ZERO-KNOWLEDGE ALPHANUMERIC IDENTITY",
                      fontWeight = FontWeight.Bold,
                      color = SecureBlue,
                      fontSize = 12.sp,
                      fontFamily = FontFamily.Monospace
                    )
                    Text(
                      text = "Identities are registered locally via optional alphanumeric display names (e.g. @agent_alpha). If updated, preceding linkages expire to guarantee absolute untraceable stealth resets.",
                      fontSize = 11.sp,
                      color = TextWhite
                    )

                    Divider(color = FrostedGlassWhite)

                    Text(
                      text = "2. END-TO-END VERIFIABLE CALLS",
                      fontWeight = FontWeight.Bold,
                      color = SecureBlue,
                      fontSize = 12.sp,
                      fontFamily = FontFamily.Monospace
                    )
                    Text(
                      text = "WebRTC peer-to-peer signaling happens directly between node devices. Displays secure matching cryptographic audit codes to prevent man-in-the-middle exploits.",
                      fontSize = 11.sp,
                      color = TextWhite
                    )

                    Divider(color = FrostedGlassWhite)

                    Text(
                      text = "3. STEALTH P2P FILE TRANSFERS",
                      fontWeight = FontWeight.Bold,
                      color = SecureBlue,
                      fontSize = 12.sp,
                      fontFamily = FontFamily.Monospace
                    )
                    Text(
                      text = "Direct streaming payload bypasses storage clouds with zero footprint. Features dynamic transit speed and byte counters directly in screen loggings.",
                      fontSize = 11.sp,
                      color = TextWhite
                    )
                  }
                },
                confirmButton = {
                  Button(
                    colors = ButtonDefaults.buttonColors(containerColor = GuardGreen),
                    onClick = { showHelpDialog = false }
                  ) {
                    Text(
                      text = "ENGAGE GATEWAYS",
                      color = OledBlack,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              )
            }
          }
        }
      }
    }
  }
}
