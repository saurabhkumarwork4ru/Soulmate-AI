package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.SoulMateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
//          ZEN MOON SILHOUETTE LOGO
// ==========================================
@Composable
fun MoonSilhouetteMascot(modifier: Modifier = Modifier, pulseAnimation: Boolean = true) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_pulse")
    val alphaGlow by if (pulseAnimation) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "alpha"
        )
    } else {
        remember { mutableStateOf(0.8f) }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2
            val maxRadius = Math.min(width, height) / 2

            // Draw glowing lunar aura
            drawCircle(
                color = Color(0xFFCBE4FF).copy(alpha = 0.12f * alphaGlow),
                radius = maxRadius * 0.95f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0xFFA5D1FF).copy(alpha = 0.22f * alphaGlow),
                radius = maxRadius * 0.82f,
                center = Offset(centerX, centerY)
            )

            // Draw Full Moon
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFE3F1FF), Color(0xFF90C4FF)),
                    center = Offset(centerX, centerY),
                    radius = maxRadius * 0.65f
                ),
                radius = maxRadius * 0.65f,
                center = Offset(centerX, centerY)
            )

            // Lunar Crates
            drawCircle(
                color = Color(0xFFBDDBFA).copy(alpha = 0.45f),
                radius = maxRadius * 0.12f,
                center = Offset(centerX - maxRadius * 0.22f, centerY - maxRadius * 0.15f)
            )
            drawCircle(
                color = Color(0xFFAECFF7).copy(alpha = 0.4f),
                radius = maxRadius * 0.10f,
                center = Offset(centerX + maxRadius * 0.28f, centerY + maxRadius * 0.18f)
            )
            drawCircle(
                color = Color(0xFFAECFF7).copy(alpha = 0.35f),
                radius = maxRadius * 0.08f,
                center = Offset(centerX - maxRadius * 0.18f, centerY + maxRadius * 0.32f)
            )

            // Meditating Silhouette
            val sPath = Path().apply {
                val base = centerY + maxRadius * 0.65f
                val scale = maxRadius * 1.35f

                // Head
                val headRadius = scale * 0.11f
                val headCenterY = centerY + scale * 0.04f
                addOval(Rect(centerX - headRadius, headCenterY - headRadius, centerX + headRadius, headCenterY + headRadius))

                val palmsY = centerY - scale * 0.32f
                val shoulderY = centerY + scale * 0.19f
                val bodyW = scale * 0.13f
                val kneeW = scale * 0.39f

                // In lotus position, palms touch above head
                moveTo(centerX, base)
                val lKnee = Offset(centerX - kneeW, base - scale * 0.05f)
                val rKnee = Offset(centerX + kneeW, base - scale * 0.05f)

                quadraticTo(centerX - kneeW * 0.5f, base + scale * 0.04f, lKnee.x, lKnee.y)
                quadraticTo(centerX - bodyW * 1.5f, centerY + scale * 0.32f, centerX - bodyW, centerY + scale * 0.2f)

                // Left raised arm
                val lShoulder = Offset(centerX - bodyW * 1.25f, shoulderY)
                lineTo(lShoulder.x, lShoulder.y)

                val lElbow = Offset(centerX - scale * 0.24f, centerY - scale * 0.07f)
                val palmsPoint = Offset(centerX, palmsY)
                quadraticTo(lElbow.x, lElbow.y, palmsPoint.x, palmsPoint.y)
                quadraticTo(centerX - scale * 0.07f, centerY - scale * 0.03f, lShoulder.x + scale * 0.05f, lShoulder.y + scale * 0.04f)

                lineTo(centerX + bodyW - scale * 0.05f, centerY + scale * 0.2f + scale * 0.03f)

                // Right raised arm
                val rShoulder = Offset(centerX + bodyW * 1.25f, shoulderY)
                lineTo(rShoulder.x, rShoulder.y)

                val rElbow = Offset(centerX + scale * 0.24f, centerY - scale * 0.07f)
                quadraticTo(rElbow.x, rElbow.y, palmsPoint.x, palmsPoint.y)
                quadraticTo(centerX + scale * 0.07f, centerY - scale * 0.03f, rShoulder.x - scale * 0.05f, rShoulder.y + scale * 0.04f)

                lineTo(centerX + bodyW, centerY + scale * 0.2f)
                quadraticTo(centerX + bodyW * 1.5f, centerY + scale * 0.32f, rKnee.x, rKnee.y)
                quadraticTo(centerX + kneeW * 0.5f, base + scale * 0.04f, centerX, base)
                close()
            }

            drawPath(
                path = sPath,
                color = Color(0xFF03050C)
            )

            // Heart / Soul Core resonance node
            drawCircle(
                color = Color(0xFFFF4081),
                radius = maxRadius * 0.035f,
                center = Offset(centerX, centerY + maxRadius * 0.30f)
            )
            drawCircle(
                color = Color.White.copy(alpha = alphaGlow),
                radius = maxRadius * 0.015f,
                center = Offset(centerX, centerY + maxRadius * 0.30f)
            )
        }
    }
}

// ==========================================
//        SOCIAL HUB MAIN COMPOSABLE
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialHubScreen(viewModel: SoulMateViewModel) {
    var activeTab by remember { mutableIntStateOf(0) } // 0: Friend Discovery, 1: Messages, 2: Communities, 3: Calling Settings
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val reportAlertText by viewModel.reportSuccessDialogText.collectAsStateWithLifecycle()
    
    // Check call overlays
    val callPeer by viewModel.currentActiveCallPeer.collectAsStateWithLifecycle()
    val callState by viewModel.currentActiveCallState.collectAsStateWithLifecycle()
    val callType by viewModel.currentActiveCallType.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(34.dp)) {
                            MoonSilhouetteMascot(pulseAnimation = true)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SOULMATE NEST",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo("home") },
                        modifier = Modifier.testTag("social_hub_back_home")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { activeTab = 3 },
                        modifier = Modifier.testTag("social_hub_dial_history")
                    ) {
                        Icon(Icons.Filled.History, contentDescription = "Call History", tint = AccentTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftCardBg)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SoftCardBg,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Filled.People, contentDescription = "Discovery") },
                    label = { Text("Discovery", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentTeal,
                        selectedTextColor = AccentTeal,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = AccentTeal.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("tab_explorer")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Filled.MarkUnreadChatAlt, contentDescription = "Direct Inbox") },
                    label = { Text("Direct Box", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmpathyPink,
                        selectedTextColor = EmpathyPink,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = EmpathyPink.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("tab_messages")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Filled.Forum, contentDescription = "Communities") },
                    label = { Text("Channels", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFBB86FC),
                        selectedTextColor = Color(0xFFBB86FC),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFFBB86FC).copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("tab_communities")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Filled.Security, contentDescription = "Privacy & Logs") },
                    label = { Text("Shield & Call", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFFB74D),
                        selectedTextColor = Color(0xFFFFB74D),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFFFFB74D).copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("tab_shield")
                )
            }
        },
        containerColor = CosmicNavy
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (activeTab) {
                0 -> FriendDiscoveryTab(viewModel)
                1 -> DirectMessagingTab(viewModel)
                2 -> CommunitiesTab(viewModel)
                3 -> PrivacyAndCallTab(viewModel)
            }

            // REPORT SUCCESS MESSAGE DIALOG
            if (reportAlertText != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissReportDialog() },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Security, contentDescription = "Shield", tint = EmpathyPink)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Profile Protected Securely", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    },
                    text = { Text(reportAlertText ?: "") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissReportDialog() }) {
                            Text("Safe Connection Reset", color = AccentTeal)
                        }
                    },
                    containerColor = SoftCardBg,
                    textContentColor = Color.White,
                    titleContentColor = Color.White
                )
            }

            // ONE-TO-ONE DYNAMIC WEBRTC/LIVEKIT DIALING OVERLAY SCREEN
            if (callPeer != null && callState != null) {
                VoiceVideoCallOverlay(viewModel = viewModel, peer = callPeer!!, state = callState!!, callType = callType ?: "Voice")
            }
        }
    }
}

// ==========================================
//        TAB 1: FRIEND DISCOVERY HUB
// ==========================================
@Composable
fun FriendDiscoveryTab(viewModel: SoulMateViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val allFriends by viewModel.allSocialFriends.collectAsStateWithLifecycle()
    val incomingReqs by viewModel.incomingRequests.collectAsStateWithLifecycle()
    val outgoingReqs by viewModel.outgoingRequests.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResult by viewModel.searchResult.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val discoverability by viewModel.discoverabilitySetting.collectAsStateWithLifecycle()

    var editingProfile by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(profile.name) }
    var editBio by remember { mutableStateOf(profile.favoriteTopics) }
    var editInterests by remember { mutableStateOf(profile.interests) }
    var editLang by remember { mutableStateOf(profile.preferredLanguageMode) }

    // Unique Simulated User ID
    val generatedUserId = "SMA784" + profile.name.length + "521"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // PERSONAL IDENTITY SHIELD CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, EmpathyPink.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(EmpathyPink.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(profile.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("ID: ", color = AccentTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(generatedUserId, color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Unique User ID", generatedUserId))
                                Toast.makeText(context, "Identity ID $generatedUserId copied! Share with friends.", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy ID", tint = AccentTeal, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { editingProfile = !editingProfile }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (editingProfile) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editInterests,
                        onValueChange = { editInterests = it },
                        label = { Text("Interests (comma separated)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editLang,
                        onValueChange = { editLang = it },
                        label = { Text("Language Preference", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.updateUserProfileIdentity(editName, editBio, editInterests, editLang, discoverability)
                            editingProfile = false
                            Toast.makeText(context, "Your identity card has been realigned successfully!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Identity Update", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Interests: ${profile.interests}", color = Color.LightGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Bio: ${profile.favoriteTopics.ifBlank { "Unwritten frequency description" }}", color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        // CHAT FRIEND REQUEST SEARCH PORTAL
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Search Friend Network by Unique ID / Username", fontWeight = FontWeight.SemiBold, color = Color.LightGray, fontSize = 12.sp)
                
                var queryInput by remember { mutableStateOf("") }
                
                OutlinedTextField(
                    value = queryInput,
                    onValueChange = { 
                        queryInput = it
                        viewModel.searchUserByIdOrUsername(it)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("user_search_input"),
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = AccentTeal) },
                    placeholder = { Text("Enter ID (e.g., SMA784521) or Username...", color = Color.Gray, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                    ),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.searchUserByIdOrUsername(queryInput)
                        keyboardController?.hide()
                    })
                )

                // RESULT CARD
                if (searchResult != null) {
                    val result = searchResult!!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(result.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("@${result.username} • UID: ${result.id}", color = AccentTeal, fontSize = 11.sp)
                                Text(result.bio, color = Color.LightGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            when (result.friendshipStatus) {
                                "Accepted" -> {
                                    Text("Connected ✅", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                "Sent" -> {
                                    Text("Requested 📤", color = Color.Yellow, fontSize = 12.sp)
                                }
                                "Received" -> {
                                    Button(
                                        onClick = { viewModel.acceptFriendRequest(result.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Accept", fontSize = 10.sp, color = Color.Black)
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = { viewModel.sendFriendRequest(result.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                        modifier = Modifier.height(30.dp).testTag("send_request_button_${result.id}")
                                    ) {
                                        Text("Request Link", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else if (queryInput.isNotBlank()) {
                    Text("No matching node found on this frequency yet.", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        // PENDING INCOMING AND OUTGOING REQUESTS LISTS
        if (incomingReqs.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Download, contentDescription = "Incoming", tint = EmpathyPink, modifier = Modifier.size(16.dp))
                    Text("INCOMING CHAT REQUESTS (${incomingReqs.size})", color = EmpathyPink, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
                }
                incomingReqs.forEach { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(req.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text("Wants to join your companion circle", color = Color.Gray, fontSize = 11.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.acceptFriendRequest(req.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                    modifier = Modifier.height(32.dp).testTag("accept_${req.id}")
                                ) {
                                    Text("Accept", fontSize = 11.sp, color = Color.Black)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.rejectFriendRequest(req.id) },
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Decline", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (outgoingReqs.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SENT PENDING REQUESTS (${outgoingReqs.size})", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
                outgoingReqs.forEach { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(req.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            OutlinedButton(
                                onClick = { viewModel.cancelFriendRequest(req.id) },
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Cancel", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // NEARBY DISCOVERY STATION (COSMIC RADAR SECTIONS)
        Divider(color = Color.White.copy(alpha = 0.08f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("LOCAL COMMUNITY STATION", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 15.sp)
                Text("Search other users in Bengaluru who have active radar", color = Color.Gray, fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable {
                        val next = when (discoverability) {
                            "Everyone" -> "Friends Only"
                            "Friends Only" -> "Hidden"
                            else -> "Everyone"
                        }
                        viewModel.updateDiscoverability(next)
                        Toast.makeText(context, "Radar visibility updated: $next", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text("Vibe: $discoverability 🛡️", color = AccentTeal, fontSize = 11.sp)
            }
        }

        // Radar dynamic visualizer simulator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            val infiniteRadar = rememberInfiniteTransition(label = "radar_sweep")
            val sweepAngle by infiniteRadar.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2800, easing = LinearEasing)
                ), label = "angle"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(color = AccentTeal.copy(alpha = 0.05f), radius = 100.dp.toPx(), center = center)
                drawCircle(color = AccentTeal.copy(alpha = 0.1f), radius = 60.dp.toPx(), center = center)
                drawCircle(color = AccentTeal.copy(alpha = 0.15f), radius = 25.dp.toPx(), center = center)

                // sweep line
                val length = 120.dp.toPx()
                val rad = Math.toRadians(sweepAngle.toDouble())
                val endOffset = Offset(
                    (center.x + length * Math.cos(rad)).toFloat(),
                    (center.y + length * Math.sin(rad)).toFloat()
                )
                drawLine(
                    color = AccentTeal.copy(alpha = 0.35f),
                    start = center,
                    end = endOffset,
                    strokeWidth = 3f
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🛰️ SWEEPING CHAT RADAR...", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("Locality filter: Bengaluru Central (Safe check active)", color = Color.Gray, fontSize = 9.sp)
            }
        }

        // AI HYBRID RECOMMENDATION ENGINES
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("🧠 HYBRID AI COMMUNITY RECOMMENDATIONS", color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aria Moon and Saurav are in your area in Bengaluru and have active frequencies matching 'Coding' and 'Yoga'. Connect to collaborate!",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // LIST OF NEARBY DISCOVERABLE PLAYERS
        val nearbyUsers = allFriends.filter { it.friendshipStatus == "None" && !it.isBlocked && it.city == "Bengaluru" }
        if (nearbyUsers.isNotEmpty()) {
            Text("RADAR NODES IN BENGALURU", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 10.sp)
            nearbyUsers.forEach { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (user.is18PlusVerified) {
                                        Text("18+ Verified", color = Color(0xFFFFB74D), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color(0xFF332015), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                                Text("Locality: Central ${user.city} • ${user.distanceKm} km away", color = Color.Gray, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { 
                                    viewModel.sendFriendRequest(user.id)
                                    Toast.makeText(context, "Frequency match request dispatched to ${user.displayName}!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Link Aura", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (user.bio.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(user.bio, color = Color.LightGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        } else {
            Text("No other players visible in range. Adjust discoverability above.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ==========================================
//        TAB 2: DIRECT BOXCHAT INBOX
// ==========================================
@Composable
fun DirectMessagingTab(viewModel: SoulMateViewModel) {
    val friends by viewModel.acceptedSocialFriends.collectAsStateWithLifecycle()
    val activeFriendId by viewModel.activeChatFriendId.collectAsStateWithLifecycle()

    if (activeFriendId != null) {
        val currentFriend = friends.firstOrNull { it.id == activeFriendId }
        ChatWindow(viewModel = viewModel, friend = currentFriend ?: SocialFriend(activeFriendId!!, "unknown", "User Connection"))
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("DIRECT ENCRYPTED OUTPOSTS", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
            Text("Talk one-on-one with accept-status profiles. High-bandwidth sync active.", color = Color.Gray, fontSize = 12.sp)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(friends) { friend ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setActiveChatFriend(friend.id) }
                            .testTag("friend_chat_item_${friend.id}"),
                        colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.9f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (friend.isOnline) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (friend.isOnline) "🟢" else "⚫", fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(friend.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text(friend.lastSeen, color = Color.Gray, fontSize = 11.sp)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(onClick = { viewModel.startSocialCall(friend.id, "Voice") }) {
                                    Icon(Icons.Filled.Call, contentDescription = "Voice Call", tint = AccentTeal)
                                }
                                IconButton(onClick = { viewModel.startSocialCall(friend.id, "Video") }) {
                                    Icon(Icons.Filled.Videocam, contentDescription = "Video Call", tint = EmpathyPink)
                                }
                            }
                        }
                    }
                }

                if (friends.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💤 No direct active outposts established.", color = Color.Gray, fontSize = 13.sp)
                                Text("Try adding users via the 'Discovery' radar.", color = Color.DarkGray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
//     SUB-TAB: ACTIVE CONVERSATION SHEET
// ==========================================
@Composable
fun DMChatConversationPane(viewModel: SoulMateViewModel, friend: SocialFriend) {
    val messages by viewModel.chatFriendMessages.collectAsStateWithLifecycle()
    val typingFriendId by viewModel.typingIndicatorFriendId.collectAsStateWithLifecycle()
    val listState = rememberScrollState()
    val context = LocalContext.current

    var textInput by remember { mutableStateOf("") }
    var selectedReplyMsgId by remember { mutableStateOf(-1L) }
    var editingMsgId by remember { mutableStateOf(-1L) }

    Column(modifier = Modifier.fillMaxSize()) {
        // CONVERSATION SUB HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftCardBg)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.setActiveChatFriend(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(friend.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text(friend.lastSeen, color = AccentTeal, fontSize = 10.sp)
                }
            }

            Row {
                IconButton(onClick = { viewModel.startSocialCall(friend.id, "Voice") }) {
                    Icon(Icons.Filled.Call, contentDescription = "Voice", tint = AccentTeal)
                }
                IconButton(onClick = { viewModel.startSocialCall(friend.id, "Video") }) {
                    Icon(Icons.Filled.Videocam, contentDescription = "Video", tint = EmpathyPink)
                }
                
                var showOptionsMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showOptionsMenu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Options", tint = Color.LightGray)
                }
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false },
                    modifier = Modifier.background(SoftCardBg)
                ) {
                    DropdownMenuItem(
                        text = { Text("Mute Outpost Notifications", color = Color.White) },
                        onClick = { 
                            viewModel.muteUser(friend.id, !friend.isMuted)
                            showOptionsMenu = false 
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Report Abuse / Block", color = EmpathyPink) },
                        onClick = { 
                            viewModel.reportAbuse(friend.id, "Harassing communications")
                            showOptionsMenu = false
                            viewModel.setActiveChatFriend(null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Unfriend / Sever Connection", color = Color.LightGray) },
                        onClick = { 
                            viewModel.unfriendUser(friend.id)
                            showOptionsMenu = false
                            viewModel.setActiveChatFriend(null)
                        }
                    )
                }
            }
        }

        // CONVERSATION STREAM (SCROLLABLE LIST)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "🔒 This direct outpost is protected under quantum compliance schemas.",
                color = Color.DarkGray,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            messages.forEach { msg ->
                val isMe = msg.senderId == "me"
                val replyMsg = messages.firstOrNull { it.id == msg.replyToId }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    if (replyMsg != null) {
                        Text(
                            text = "↩️ Replying to: ${replyMsg.text.take(24)}...",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    Card(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        if (isMe) {
                                            viewModel.deleteSocialMessage(msg.id)
                                            Toast
                                                .makeText(context, "Message obliterated!", Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    }
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) EmpathyPink.copy(alpha = 0.25f) else SoftCardBg
                        ),
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isMe) 12.dp else 2.dp,
                            bottomEnd = if (isMe) 2.dp else 12.dp
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            // Media Renderings
                            if (msg.mediaType == "sticker") {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(msg.text, fontSize = 34.sp)
                                }
                            } else if (msg.mediaType == "voice_note") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = AccentTeal)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Voice Note Simulate (0:14)", color = AccentTeal, fontSize = 12.sp)
                                }
                            } else {
                                Text(msg.text, color = Color.White, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (msg.isEdited) {
                                    Text("edited", color = Color.Gray, fontSize = 8.sp)
                                }
                                val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                Text(sdf.format(java.util.Date(msg.timestamp)), color = Color.DarkGray, fontSize = 8.sp)
                                
                                if (isMe) {
                                    Icon(
                                        imageVector = if (msg.seen) Icons.Filled.CheckCircle else Icons.Filled.Check,
                                        contentDescription = "Seen status",
                                        tint = if (msg.seen) AccentTeal else Color.Gray,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Quick Reaction Row
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("❤️", "👍", "🔥").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .clickable { viewModel.reactToSocialMessage(msg.id, emoji) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(emoji, fontSize = 10.sp)
                            }
                        }
                        if (msg.reactions.isNotBlank()) {
                            Text("Reactions: ${msg.reactions}", color = AccentTeal, fontSize = 9.sp)
                        }
                    }
                }
            }

            if (typingFriendId == friend.id) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                    Text("${friend.displayName} is typing", color = Color.Gray, fontSize = 11.sp)
                    val infiniteDots = rememberInfiniteTransition(label = "dots")
                    val alphaDot by infiniteDots.animateFloat(
                        initialValue = 0.2f, targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(animation = tween(600), repeatMode = RepeatMode.Reverse), label = "alpha"
                    )
                    Text("...", color = AccentTeal.copy(alpha = alphaDot), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // INPUT CONSOLE (MUTLIPASS MULTIMEDIA)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftCardBg)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { 
                selectedReplyMsgId = -1
                // Attach simulated sticker image inside input
                viewModel.sendSocialMessage(friend.id, "💆", "sticker")
            }) {
                Icon(Icons.Filled.EmojiEmotions, contentDescription = "Stickers", tint = Color.LightGray)
            }
            IconButton(onClick = { 
                viewModel.sendSocialMessage(friend.id, "0:14 (Somatic Breathing Playback)", "voice_note")
            }) {
                Icon(Icons.Filled.Mic, contentDescription = "Voice Note", tint = Color.LightGray)
            }

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier.weight(1f).testTag("direct_chat_input"),
                placeholder = { Text("Encrypted frequency note...", color = Color.Gray, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentTeal, unfocusedBorderColor = Color.Transparent
                ),
                maxLines = 2
            )

            IconButton(
                onClick = {
                    if (editingMsgId != -1L) {
                        viewModel.editSocialMessage(editingMsgId, textInput)
                        editingMsgId = -1L
                    } else {
                        viewModel.sendSocialMessage(friend.id, textInput, replyToId = selectedReplyMsgId)
                    }
                    textInput = ""
                    selectedReplyMsgId = -1
                },
                modifier = Modifier.testTag("direct_send_button")
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = AccentTeal)
            }
        }
    }
}

// ==========================================
//        TAB 3: CHANNELS & COMMUNITIES
// ==========================================
@Composable
fun CommunitiesTab(viewModel: SoulMateViewModel) {
    val context = LocalContext.current
    val groups by viewModel.communityGroups.collectAsStateWithLifecycle()
    val activeGroupId by viewModel.activeCommunityGroupId.collectAsStateWithLifecycle()
    
    // Check Age verification status
    val ageChecked by viewModel.age18PlusVerified.collectAsStateWithLifecycle()
    var displayAgeVerificationPrompt by remember { mutableStateOf(false) }

    if (activeGroupId != null) {
        val currentGroup = groups.firstOrNull { it.id == activeGroupId }
        CommunityGroupChatPane(viewModel = viewModel, group = currentGroup ?: CommunityGroup(activeGroupId!!, "Main Group", "", "Public"))
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("COMMUNITY DISCUSSIONS", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
                    Text("Unite, discuss, and study collectively with other users.", color = Color.Gray, fontSize = 11.sp)
                }

                var showCreateDialog by remember { mutableStateOf(false) }
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "Create Group", tint = AccentTeal, modifier = Modifier.size(28.dp))
                }

                if (showCreateDialog) {
                    var newName by remember { mutableStateOf("") }
                    var newDesc by remember { mutableStateOf("") }
                    var isAdultOnly by remember { mutableStateOf(false) }
                    var selectedCat by remember { mutableStateOf("Interest") }

                    AlertDialog(
                        onDismissRequest = { showCreateDialog = false },
                        title = { Text("Establish New Outpost Channel", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = newName,
                                    onValueChange = { newName = it },
                                    label = { Text("Channel Name", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = newDesc,
                                    onValueChange = { newDesc = it },
                                    label = { Text("Brief Focus Description", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isAdultOnly,
                                        onCheckedChange = { isAdultOnly = it },
                                        colors = CheckboxDefaults.colors(checkedColor = EmpathyPink)
                                    )
                                    Text("Mature 18+ Separate Discussion space", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.createAndJoinCommunityGroup(newName, newDesc, selectedCat, isAdultOnly)
                                    showCreateDialog = false
                                    Toast.makeText(context, "$newName created! Welcoming new minds...", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                            ) {
                                Text("Forge Channel Outpost", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCreateDialog = false }) { Text("Cancel", color = Color.Gray) }
                        },
                        containerColor = SoftCardBg
                    )
                }
            }

            // FILTER CARDS
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Local City", "Coding / Career", "Study Groups", "Mental Wellness", "Adult Spaces (🔞)").forEach { cat ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable {
                                if (cat.contains("Adult") && !ageChecked) {
                                    displayAgeVerificationPrompt = true
                                } else {
                                    Toast.makeText(context, "Category filters aligned to $cat", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(cat, color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }

            if (displayAgeVerificationPrompt) {
                AlertDialog(
                    onDismissRequest = { displayAgeVerificationPrompt = false },
                    title = { Text("Age verification check (18+ only)", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Certain spaces inside the communities allow mature relationship discussions. Please verify your age (18+) and accept consensual participation guidelines.", color = Color.LightGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Do you explicitly consent to these policies?", color = EmpathyPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.verifyAdultAge(true)
                                displayAgeVerificationPrompt = false
                                Toast.makeText(context, "18+ space successfully unlocked!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink)
                        ) {
                            Text("I am 18+ and Accept Policies", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { displayAgeVerificationPrompt = false }) { Text("Decline", color = Color.Gray) }
                    },
                    containerColor = SoftCardBg
                )
            }

            // LIST OF CHANNELS
            groups.forEach { grp ->
                if (grp.isAdultOnly && !ageChecked) {
                    // Hide or mask of unverified spaces
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("group_card_${grp.id}"),
                        colors = CardDefaults.cardColors(containerColor = SoftCardBg),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(AccentTeal.copy(alpha = 0.12f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(grp.pfpEmoji, fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(grp.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        Text("${grp.category} Channel • ${grp.memberCount} members", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }

                                if (grp.isJoined) {
                                    Button(
                                        onClick = { viewModel.setActiveCommunityGroup(grp.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                        modifier = Modifier.height(30.dp).testTag("open_group_${grp.id}")
                                    ) {
                                        Text("Open", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { 
                                            viewModel.joinOrLeaveCommunityGroup(grp.id, true)
                                            Toast.makeText(context, "Joined ${grp.name}!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Join", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(grp.description, color = Color.LightGray, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityGroupChatPane(viewModel: SoulMateViewModel, group: CommunityGroup) {
    val messages by viewModel.groupMessages.collectAsStateWithLifecycle()
    var textInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftCardBg)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.setActiveCommunityGroup(null) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(group.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text("${group.memberCount} nodes active", color = AccentTeal, fontSize = 11.sp)
                }
            }

            OutlinedButton(
                onClick = { 
                    viewModel.joinOrLeaveCommunityGroup(group.id, false)
                    viewModel.setActiveCommunityGroup(null)
                },
                border = BorderStroke(1.dp, EmpathyPink.copy(alpha = 0.5f))
            ) {
                Text("Leave Outpost", color = EmpathyPink, fontSize = 11.sp)
            }
        }

        // POSTS/MESSAGES LIST
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "${group.description} Please hold authentic, collaborative discussions.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(10.dp).fillMaxWidth()
                    )
                }
            }

            items(messages) { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(AccentTeal.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(msg.authorEmoji, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(msg.authorName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                            }
                            val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                            Text(sdf.format(java.util.Date(msg.timestamp)), color = Color.Gray, fontSize = 9.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(msg.text, color = Color.LightGray, fontSize = 12.sp, lineHeight = 16.sp)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.likeGroupMessage(msg.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.ThumbUp, contentDescription = "Support", tint = if (msg.isLikedByMe) EmpathyPink else Color.Gray, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${msg.likes} Sparks", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // INPUT BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftCardBg)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier.weight(1f).testTag("group_chat_input"),
                placeholder = { Text("Publish message into community...", color = Color.Gray, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentTeal,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            IconButton(
                onClick = {
                    viewModel.sendCommunityMessage(group.id, textInput)
                    textInput = ""
                    keyboardController?.hide()
                },
                modifier = Modifier.testTag("submit_community_message")
            ) {
                Icon(Icons.Filled.Send, contentDescription = "publish", tint = AccentTeal)
            }
        }
    }
}

// ==========================================
//     TAB 4: PRIVACY PROTECT, SHIELD, DIAL
// ==========================================
@Composable
fun PrivacyAndCallTab(viewModel: SoulMateViewModel) {
    val callLogs by viewModel.callLogs.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Lock, contentDescription = "Shield", tint = AccentTeal)
            Text("FREQUENCY SHIELD & PRIVACY OUTPOST", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Quantum Anti-Abuse Shield Status", color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                Text("When enabled, direct messages from stranger channels or unlinked radar profiles are safely deflected to sandbox quarantine automatically.", color = Color.LightGray, fontSize = 11.sp)
                
                var antiStrangActive by remember { mutableStateOf(true) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Block Unlinked Strangers", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = antiStrangActive,
                        onCheckedChange = { antiStrangActive = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentTeal, checkedTrackColor = AccentTeal.copy(alpha = 0.5f))
                    )
                }
            }
        }

        // COMPLETED CALL RECODS
        Text("DURABLE CALL HISTORY LOGS (${callLogs.size})", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 10.sp, letterSpacing = 1.sp)
        callLogs.forEach { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(if (log.callType == "Video") EmpathyPink.copy(alpha = 0.15f) else AccentTeal.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (log.callType == "Video") Icons.Filled.Videocam else Icons.Filled.Call,
                                contentDescription = log.callType,
                                tint = if (log.callType == "Video") EmpathyPink else AccentTeal,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(log.peerName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            val formattedTime = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))
                            Text("$formattedTime • ${log.status}", color = Color.Gray, fontSize = 11.sp)
                        }
                    }

                    Text("${log.durationSeconds}s", color = AccentTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (callLogs.isEmpty()) {
            Text("No outgoing or incoming calls archived inside Room databases.", color = Color.DarkGray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        } else {
            Button(
                onClick = { 
                    viewModel.clearCallLogs()
                    Toast.makeText(context, "Call logs wiped successfully!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Call History logs", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// =========================================================
//  IMMERSIVE ENCRYPTED ONE-TO-ONE VOICE & VIDEO CALL PANEL
// =========================================================
@Composable
fun VoiceVideoCallOverlay(
    viewModel: SoulMateViewModel,
    peer: SocialFriend,
    state: String,
    callType: String
) {
    val duration by viewModel.callSeconds.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMutedCall.collectAsStateWithLifecycle()
    val isCamOff by viewModel.isCameraDisabled.collectAsStateWithLifecycle()
    val isScreenShare by viewModel.isScreenSharing.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030509))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic Call elements
        if (callType == "Video" && !isCamOff) {
            // Simulated video feed showing cosmic stars pulsation
            val infinityTrans = rememberInfiniteTransition(label = "stars")
            val particleScale by infinityTrans.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.3f,
                animationSpec = infiniteRepeatable(tween(4000), repeatMode = RepeatMode.Reverse), label = "visualizer_particles"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(
                    color = Color(0xFF673AB7).copy(alpha = 0.15f),
                    radius = 200.dp.toPx() * particleScale,
                    center = center
                )
                drawCircle(
                    color = Color(0xFFE91E63).copy(alpha = 0.08f),
                    radius = 350.dp.toPx() * particleScale,
                    center = center
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            // Header Outpost indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.Verified, contentDescription = "SECURE", tint = AccentTeal, modifier = Modifier.size(12.dp))
                Text(
                    text = "QUANTUM END-TO-END SECURE ${callType.uppercase()}",
                    color = AccentTeal,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Avatar Frame incorporating Moon Silhouette mascot
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                MoonSilhouetteMascot(modifier = Modifier.fillMaxSize(), pulseAnimation = state == "Ringing" || state == "Connected")
                
                // Overlay text with peer name
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(peer.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // State and Timing Details
            Text(
                text = when (state) {
                    "Dialing" -> "Dialing out on companion frequencies..."
                    "Ringing" -> "Node ringing..."
                    "Connected" -> "Direct link synchronized! Duration: %02d:%02d".format(duration / 60, duration % 60)
                    else -> "Severing link parameters..."
                },
                color = if (state == "Connected") AccentTeal else Color.LightGray,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            if (isMuted) {
                Text("🔇 Microphones muted", color = EmpathyPink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            if (isCamOff && callType == "Video") {
                Text("📸 Front Camera disabled", color = Color.Gray, fontSize = 11.sp)
            }
            if (isScreenShare) {
                Text("🖥️ Screen Sharing transmission live", color = AccentTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))

            // CALL ACTIONS CONSOLE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute trigger
                IconButton(
                    onClick = { viewModel.toggleMuteCall() },
                    modifier = Modifier
                        .size(54.dp)
                        .background(if (isMuted) EmpathyPink else Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        contentDescription = "Mute Toggle",
                        tint = if (isMuted) Color.White else AccentTeal
                    )
                }

                // Hang Up
                IconButton(
                    onClick = { 
                        viewModel.endSocialCall()
                        Toast.makeText(context, "Outpost frequency terminated securely.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(68.dp)
                        .background(Color(0xFFB71C1C), CircleShape)
                ) {
                    Icon(Icons.Filled.CallEnd, contentDescription = "Hang Up", tint = Color.White, modifier = Modifier.size(34.dp))
                }

                if (callType == "Video") {
                    // Camera Toggle
                    IconButton(
                        onClick = { viewModel.toggleCameraCall() },
                        modifier = Modifier
                            .size(54.dp)
                            .background(if (isCamOff) Color.Gray else Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isCamOff) Icons.Filled.VideocamOff else Icons.Filled.Videocam,
                            contentDescription = "Cam Toggle",
                            tint = Color.White
                        )
                    }

                    // Screen Share
                    IconButton(
                        onClick = { 
                            viewModel.toggleScreenSharing() 
                            Toast.makeText(context, "WebRTC ScreenShare transmission initialized!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .background(if (isScreenShare) AccentTeal else Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isScreenShare) Icons.Filled.ScreenShare else Icons.Filled.StopScreenShare,
                            contentDescription = "ScreenShare",
                            tint = if (isScreenShare) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}
