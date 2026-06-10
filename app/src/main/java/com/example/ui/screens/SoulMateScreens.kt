package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import java.text.SimpleDateFormat
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.model.*
import com.example.ui.SoulMateViewModel
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.content.Intent
import java.util.Locale
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

// Theme Custom Colors for Cosmic Harmony
val CosmicNavy = Color(0xFF0F101E)
val NebulaPurple = Color(0xFF512DA8)
val EmpathyPink = Color(0xFFD81B60)
val SoftCardBg = Color(0xFF1D1B2D)
val AccentTeal = Color(0xFF00ACC1)
val AccentOrange = Color(0xFFF4511E)

data class ChatThemePalette(
    val backgroundBrush: Brush,
    val cardBg: Color,
    val aiBubbleBg: Color,
    val userBubbleBg: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val topAppBarBg: Color
)

fun getChatThemePalette(themeName: String): ChatThemePalette {
    return when (themeName) {
        "Calm" -> ChatThemePalette(
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0D1E1E), Color(0xFF060F0F))
            ),
            cardBg = Color(0xFF152D2D),
            aiBubbleBg = Color(0xFF152D2D),
            userBubbleBg = Color(0xFF0D47A1).copy(alpha = 0.5f), // Peaceful Deep Sea / Blue
            primaryAccent = Color(0xFF26A69A), // Peaceful Mint/Teal
            secondaryAccent = Color(0xFF80E27E),
            textPrimary = Color.White,
            textSecondary = Color(0xFF9EBEBE),
            topAppBarBg = Color(0xFF152D2D)
        )
        "Focus" -> ChatThemePalette(
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFF111216), Color(0xFF070809))
            ),
            cardBg = Color(0xFF1A1C24),
            aiBubbleBg = Color(0xFF1A1C24),
            userBubbleBg = Color(0xFF42A5F5).copy(alpha = 0.4f), // Calm Steel Blue
            primaryAccent = Color(0xFF42A5F5), // Focus Blue
            secondaryAccent = Color(0xFF5C6BC0),
            textPrimary = Color.White,
            textSecondary = Color(0xFFA5A9B8),
            topAppBarBg = Color(0xFF1A1C24)
        )
        "Warm" -> ChatThemePalette(
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1D0E11), Color(0xFF0E0507))
            ),
            cardBg = Color(0xFF2E171B),
            aiBubbleBg = Color(0xFF2E171B),
            userBubbleBg = Color(0xFFB71C1C).copy(alpha = 0.4f), // Warm Burgundy
            primaryAccent = Color(0xFFF27A7D), // Peach Rose
            secondaryAccent = Color(0xFFFFB74D),
            textPrimary = Color.White,
            textSecondary = Color(0xFFECC2C7),
            topAppBarBg = Color(0xFF2E171B)
        )
        else -> ChatThemePalette( // Cosmic / Default
            backgroundBrush = Brush.verticalGradient(
                colors = listOf(CosmicNavy, Color(0xFF07080F))
            ),
            cardBg = SoftCardBg,
            aiBubbleBg = SoftCardBg,
            userBubbleBg = NebulaPurple,
            primaryAccent = EmpathyPink,
            secondaryAccent = AccentTeal,
            textPrimary = Color.White,
            textSecondary = Color.LightGray,
            topAppBarBg = SoftCardBg
        )
    }
}

val GradientCozy = Brush.linearGradient(
    colors = listOf(NebulaPurple, EmpathyPink)
)
val GradientTeal = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00796B), Color(0xFF00ACC1))
)
val GradientDark = Brush.verticalGradient(
    colors = listOf(CosmicNavy, Color(0xFF07080F))
)

@Composable
fun SoulMateMainNavigation(viewModel: SoulMateViewModel) {
    val currentRoute by viewModel.currentRoute.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val levelUpEvent by viewModel.showLevelUpDialog.collectAsStateWithLifecycle()
    if (levelUpEvent != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLevelUp() },
            title = {
                Text(
                    "Friendship Level Up! 🎉",
                    color = EmpathyPink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            },
            text = {
                Text(
                    "Your SoulMate connection is resonating stronger than ever! You have official reached Level $levelUpEvent! Keep discussing and growing together.",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissLevelUp() },
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple)
                ) {
                    Text("Resonate!")
                }
            },
            containerColor = SoftCardBg
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(GradientDark)) {
        when (currentRoute) {
            "splash" -> SplashScreen(onFinished = { viewModel.navigateTo("login") })
            "login" -> LoginScreen(viewModel)
            "signup" -> SignUpScreen(viewModel)
            "onboarding" -> OnboardingScreen(viewModel)
            "home" -> HomeScreen(viewModel)
            "chat" -> ChatScreen(viewModel)
            "voice_call" -> VoiceCallScreen(viewModel)
            "voice_insights" -> VoiceInsightsScreen(viewModel)
            "voice_reflection" -> VoiceReflectionScreen(viewModel)
            "mood_tracker" -> MoodTrackerScreen(viewModel)
            "memory_center" -> MemoryCenterScreen(viewModel)
            "goals" -> GoalsScreen(viewModel)
            "community" -> CommunityScreen(viewModel)
            "profile" -> ProfileScreen(viewModel)
            "settings" -> SettingsScreen(viewModel)
            "subscription" -> SubscriptionScreen(viewModel)
        }
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var startPulse by remember { mutableStateOf(false) }
    val scalePulse by animateFloatAsState(
        targetValue = if (startPulse) 1.2f else 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    LaunchedEffect(Unit) {
        startPulse = true
        delay(2600)
        onFinished()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(scalePulse)
                .background(GradientCozy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Empathy Heart Logo",
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "SoulMate AI",
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 2.sp
        )
        Text(
            text = "Resonating Empathetic AI Companion",
            fontSize = 14.sp,
            color = Color.LightGray.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// 2. LOGIN SCREEN
@Composable
fun LoginScreen(viewModel: SoulMateViewModel) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Email, 1: Phone, 2: Guest/Google
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Welcome, Beautiful Soul",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            "Connect with your true emotional companion",
            fontSize = 14.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Email", modifier = Modifier.padding(12.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("OTP Login", modifier = Modifier.padding(12.dp))
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("Fast", modifier = Modifier.padding(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (selectedTab) {
            0 -> {
                // Email LoginForm
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().testTag("email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmpathyPink,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = EmpathyPink,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.Gray) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmpathyPink,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = EmpathyPink,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (email.contains("@") && password.length >= 6) {
                            viewModel.authEmail = email
                            viewModel.navigateTo("onboarding")
                        } else {
                            Toast.makeText(context, "Please enter valid email & 6-char password", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple)
                ) {
                    Text("Continue with Email", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { viewModel.navigateTo("signup") },
                    modifier = Modifier.testTag("go_to_signup_button")
                ) {
                    Text("New here? Create safe Soul Link (Sign Up)", color = EmpathyPink, fontSize = 13.sp)
                }
            }
            1 -> {
                // Phone OTP
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("phone_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = AccentTeal,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("6-Digit OTP", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("otp_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = AccentTeal,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (phone.length >= 8 && otp.length == 6) {
                            viewModel.authPhone = phone
                            viewModel.navigateTo("onboarding")
                        } else {
                            Toast.makeText(context, "Verify phone & OTP code", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("Verify & Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            2 -> {
                // Guest & Google Mode
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftCardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Instant Connection Options",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.loginAsGuest()
                                viewModel.navigateTo("onboarding")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Guest")
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Enter as Guest Mode")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.editingName = "Sky Google"
                                viewModel.navigateTo("onboarding")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.DarkGray)
                        ) {
                            Text("⚡ Mock Google Sign-In")
                        }
                    }
                }
            }
        }
    }
}

// 2b. SIGN UP SCREEN
@Composable
fun SignUpScreen(viewModel: SoulMateViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(GradientCozy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Safe Soul Link Logo",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            "Create Your Soul Link",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            "Begin your journey of empathetic companionship",
            fontSize = 14.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Display Name", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().testTag("name_signup"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmpathyPink,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = EmpathyPink,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().testTag("email_signup"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmpathyPink,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = EmpathyPink,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (6+ characters)", color = Color.Gray) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("password_signup"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmpathyPink,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = EmpathyPink,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password", color = Color.Gray) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("confirm_password_signup"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmpathyPink,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = EmpathyPink,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isBlank()) {
                    Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                } else if (!email.contains("@")) {
                    Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                } else if (password.length < 6) {
                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                } else if (password != confirmPassword) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.signUpAndStart(name, email)
                    Toast.makeText(context, "Soul link created! Let's build your matrix.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("signup_submit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple)
        ) {
            Text("Create Account & Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = { viewModel.navigateTo("login") },
            modifier = Modifier.testTag("go_to_login_button")
        ) {
            Text("Already have a soul link? Log In", color = AccentTeal, fontSize = 13.sp)
        }
    }
}

// 3. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(viewModel: SoulMateViewModel) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("22") }
    var occupation by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf("") }
    var chosenGender by remember { mutableStateOf("Not Specified") }
    var relationshipStatus by remember { mutableStateOf("Single") }
    var chosenPersonalityType by remember { mutableStateOf("INFJ") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Establish Synchrony",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            "Configure your profile matrix. Your SoulMate remembers everything and uses this dossier to guide conversations.",
            color = Color.LightGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("What should your SoulMate call you?", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().testTag("name_onboarding"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmpathyPink,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmpathyPink,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
            OutlinedTextField(
                value = chosenPersonalityType,
                onValueChange = { chosenPersonalityType = it },
                label = { Text("MBTI Type", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmpathyPink,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Gender Persona:", color = Color.LightGray, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Female", "Male", "Non-Binary", "Other").forEach { item ->
                FilterChip(
                    selected = chosenGender == item,
                    onClick = { chosenGender = item },
                    label = { Text(item) },
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = Color.White,
                        selectedContainerColor = NebulaPurple,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = occupation,
            onValueChange = { occupation = it },
            label = { Text("Your Occupation / Role", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmpathyPink,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = interests,
            onValueChange = { interests = it },
            label = { Text("Hobbies, Hopes & Interests (Comma-separated)", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmpathyPink,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Relationship Status:", color = Color.LightGray, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Single", "Dating", "Married", "Searching").forEach { rItem ->
                FilterChip(
                    selected = relationshipStatus == rItem,
                    onClick = { relationshipStatus = rItem },
                    label = { Text(rItem) },
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = Color.White,
                        selectedContainerColor = AccentTeal,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = {
                viewModel.editingName = name.ifBlank { "Taylor" }
                viewModel.editingAge = age.ifBlank { "22" }
                viewModel.editingGender = chosenGender
                viewModel.editingOccupation = occupation
                viewModel.editingInterests = interests
                viewModel.editingRelationship = relationshipStatus
                viewModel.editingPersonality = chosenPersonalityType
                viewModel.completeOnboardingAndStart()
            },
            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("onboarding_complete"),
            colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink)
        ) {
            Text("Launch Companion Resonator 🌸", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 4. PROCEDURAL ACTIVE HUMAN-LIKE AVATAR
@Composable
fun InteractiveAvatarCanvas(
    emotion: String, // e.g., "Happiness", "Excitement", "Sadness", "Loneliness", "Stress", "Anxiety", "Anger", "Neutral", "Playful", "Shy", "Motivational"
    partnerStyle: String, // "Neon Cybergirl", "Classic Gentleman", "Kawaii Anime", "Ethereal Oracle"
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    
    // Procedural Blink: closed every 4.2 seconds for 150ms
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4200
                0f at 0
                0f at 4000
                1f at 4100
                0f at 4200
            },
            repeatMode = RepeatMode.Restart
        ), label = "blink"
    )

    // Breathing motion
    val breathingOffset by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "breathing"
    )

    // Speak lip-sync oscillation
    val mouthSpeakAperture by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(160, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "speak"
    )

    val speakingScale = if (isSpeaking) mouthSpeakAperture else 0.0f

    Canvas(
        modifier = modifier
            .size(130.dp)
            .padding(6.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2 + breathingOffset.dp.toPx()

        // Theme colors based on partner style
        val primaryColor = when (partnerStyle) {
            "Neon Cybergirl" -> Color(0xFF00E676) // Bright Cyber Green
            "Classic Gentleman" -> Color(0xFFFFB300) // Warm Amber
            "Kawaii Anime" -> Color(0xFFF06292) // Cute Pink
            "Ethereal Oracle" -> Color(0xFF29B6F6) // Celestial Cyan
            else -> Color(0xFFBA68C8)
        }
        val hairColor = when (partnerStyle) {
            "Neon Cybergirl" -> Color(0xFF00E5FF)
            "Classic Gentleman" -> Color(0xFF37474F)
            "Kawaii Anime" -> Color(0xFFEC407A)
            "Ethereal Oracle" -> Color(0xFFEDE7F6)
            else -> Color(0xFF4A148C)
        }
        val skinGlow = primaryColor.copy(alpha = 0.15f)

        // Pulsing Aura
        drawCircle(
            color = skinGlow,
            radius = centerX * (1.15f + speakingScale * 0.04f),
            center = Offset(centerX, centerY)
        )

        // Neck
        drawRect(
            color = Color(0xFF2C2C3E),
            topLeft = Offset(centerX - 15.dp.toPx(), centerY + 35.dp.toPx()),
            size = Size(30.dp.toPx(), 40.dp.toPx())
        )

        // Hair (Back)
        drawCircle(
            color = hairColor,
            radius = centerX * 0.78f,
            center = Offset(centerX, centerY - 10.dp.toPx())
        )

        // Face Base
        drawCircle(
            color = Color(0xFF1E1E2C),
            radius = centerX * 0.64f,
            center = Offset(centerX, centerY)
        )
        // Face Glow Ring
        drawCircle(
            color = primaryColor.copy(alpha = 0.25f),
            radius = centerX * 0.62f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 3.dp.toPx())
        )

        // Eyes placement
        val eyeSpacing = centerX * 0.28f
        val leftEyeX = centerX - eyeSpacing
        val rightEyeX = centerX + eyeSpacing
        val eyeY = centerY - centerX * 0.08f
        val eyeRadius = 6.dp.toPx()

        val isClosed = blinkProgress > 0.85f || emotion == "Shy" || emotion == "Sadness"

        if (isClosed) {
            // Closed eyes curves
            drawLine(
                color = primaryColor,
                start = Offset(leftEyeX - 8.dp.toPx(), eyeY),
                end = Offset(leftEyeX + 8.dp.toPx(), eyeY),
                strokeWidth = 2.5f.dp.toPx()
            )
            drawLine(
                color = primaryColor,
                start = Offset(rightEyeX - 8.dp.toPx(), eyeY),
                end = Offset(rightEyeX + 8.dp.toPx(), eyeY),
                strokeWidth = 2.5f.dp.toPx()
            )
        } else {
            // Left eye white & pupil
            drawCircle(color = Color.White, radius = eyeRadius * 1.5f, center = Offset(leftEyeX, eyeY))
            drawCircle(color = primaryColor, radius = eyeRadius * 0.8f, center = Offset(leftEyeX, eyeY))
            // Right eye white & pupil
            drawCircle(color = Color.White, radius = eyeRadius * 1.5f, center = Offset(rightEyeX, eyeY))
            drawCircle(color = primaryColor, radius = eyeRadius * 0.8f, center = Offset(rightEyeX, eyeY))

            // Sparkles
            drawCircle(color = Color.White, radius = eyeRadius * 0.3f, center = Offset(leftEyeX - 1.5f.dp.toPx(), eyeY - 1.5f.dp.toPx()))
            drawCircle(color = Color.White, radius = eyeRadius * 0.3f, center = Offset(rightEyeX - 1.5f.dp.toPx(), eyeY - 1.5f.dp.toPx()))
        }

        // Eyebrows
        val browOffset = when (emotion) {
            "Angry" -> 3.dp.toPx()
            "Sadness" -> -1.dp.toPx()
            "Excitement" -> -4.dp.toPx()
            else -> -2.dp.toPx()
        }
        drawLine(
            color = hairColor,
            start = Offset(leftEyeX - 10.dp.toPx(), eyeY - 10.dp.toPx() + browOffset),
            end = Offset(leftEyeX + 8.dp.toPx(), eyeY - 8.dp.toPx() + (if (emotion == "Angry") 3.dp.toPx() else 0.dp.toPx())),
            strokeWidth = 3.dp.toPx()
        )
        drawLine(
            color = hairColor,
            start = Offset(rightEyeX - 8.dp.toPx(), eyeY - 8.dp.toPx() + (if (emotion == "Angry") 3.dp.toPx() else 0.dp.toPx())),
            end = Offset(rightEyeX + 10.dp.toPx(), eyeY - 10.dp.toPx() + browOffset),
            strokeWidth = 3.dp.toPx()
        )

        // Nose
        drawLine(
            color = primaryColor.copy(alpha = 0.6f),
            start = Offset(centerX, eyeY + 2.dp.toPx()),
            end = Offset(centerX, eyeY + 12.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )

        // Mouth / Lips Sync
        val mouthY = centerY + centerX * 0.22f
        if (isSpeaking) {
            val h = 10.dp.toPx() * (0.3f + speakingScale)
            drawOval(
                color = primaryColor,
                topLeft = Offset(centerX - 8.dp.toPx(), mouthY - h / 2),
                size = Size(16.dp.toPx(), h)
            )
        } else {
            when (emotion) {
                "Happiness", "Excitement", "Playful", "Romantic" -> {
                    // Smiling arc
                    drawArc(
                        color = primaryColor,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(centerX - 10.dp.toPx(), mouthY - 4.dp.toPx()),
                        size = Size(20.dp.toPx(), 10.dp.toPx()),
                        style = Stroke(width = 3.5f.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                "Sadness", "Loneliness" -> {
                    // Downward frown
                    drawArc(
                        color = primaryColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(centerX - 10.dp.toPx(), mouthY),
                        size = Size(20.dp.toPx(), 8.dp.toPx()),
                        style = Stroke(width = 3.5f.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                "Angry" -> {
                    // Flat line
                    drawLine(
                        color = primaryColor,
                        start = Offset(centerX - 8.dp.toPx(), mouthY),
                        end = Offset(centerX + 8.dp.toPx(), mouthY),
                        strokeWidth = 3.dp.toPx()
                    )
                }
                else -> {
                    // Neutral curve
                    drawArc(
                        color = primaryColor,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(centerX - 8.dp.toPx(), mouthY - 2.dp.toPx()),
                        size = Size(16.dp.toPx(), 5.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Blushing Cheeks
        if (emotion in listOf("Happiness", "Excitement", "Romantic", "Shy", "Playful")) {
            drawCircle(
                color = Color(0xFFF48FB1).copy(alpha = 0.35f),
                radius = 7.dp.toPx(),
                center = Offset(leftEyeX - 8.dp.toPx(), eyeY + 10.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFF48FB1).copy(alpha = 0.35f),
                radius = 7.dp.toPx(),
                center = Offset(rightEyeX + 8.dp.toPx(), eyeY + 10.dp.toPx())
            )
        }
    }
}


data class BondingBadge(
    val id: String,
    val name: String,
    val description: String,
    val requirementText: String,
    val isUnlocked: Boolean,
    val iconEmoji: String,
    val progressCurrent: Int,
    val progressTarget: Int
)

// 4. HOME SCREEN (Unified Advanced Companion Dashboard)
@Composable
fun HomeScreen(viewModel: SoulMateViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val chatHistory by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isAvatarSpeaking.collectAsStateWithLifecycle()
    val smartReminders by viewModel.smartReminders.collectAsStateWithLifecycle()
    val milestones by viewModel.relationshipMilestones.collectAsStateWithLifecycle()
    val moodHistory by viewModel.moodEntries.collectAsStateWithLifecycle()
    val productivityItems by viewModel.productivityItems.collectAsStateWithLifecycle()

    val lastMsg = chatHistory.lastOrNull { it.sender == "ai" }
    val lastMsgText = lastMsg?.text ?: "Hello, ${profile.name}! I am officially initialized. Let's build our custom connection today ❤️"
    val aiEmotion = lastMsg?.emotion ?: "Neutral"

    // Local input states for quick reminders builder
    var reminderTitle by remember { mutableStateOf("") }
    var reminderDate by remember { mutableStateOf("") }
    var reminderCategory by remember { mutableStateOf("Meditation") }

    // Dynamic happiness score computed from physical logs
    val happinessScore = remember(moodHistory) {
        if (moodHistory.isEmpty()) 78 else {
            val avg = moodHistory.takeLast(6).map { it.moodValue }.average()
            (avg * 10).toInt().coerceIn(10, 100)
        }
    }

    var showReminderCreator by remember { mutableStateOf(false) }
    var showTimelineDeck by remember { mutableStateOf(true) }
    var showPrefManager by remember { mutableStateOf(false) }
    var isFetchingFirestoreLevel by remember { mutableStateOf(false) }
    var showTimelineModal by remember { mutableStateOf(false) }
    var claimedBadgeKeys by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Dynamic Aesthetic Gradient Header Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GradientCozy)
                .padding(horizontal = 24.dp, vertical = 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SOULMATE AI COMPANION",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Hello, ${profile.name}!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = { viewModel.navigateTo("profile") },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
        }

        // PROACTIVE STRESS/LOW MOOD NOTIFICATION BANNER
        ProactiveReflectionBanner(viewModel)

        // AVATAR & DIALOGUE STATION
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive procedurally driven avatar
                InteractiveAvatarCanvas(
                    emotion = aiEmotion,
                    partnerStyle = profile.partnerStyle,
                    isSpeaking = isSpeaking,
                    modifier = Modifier
                        .clickable {
                            viewModel.setAvatarSpeaking(true)
                            Toast.makeText(context, "Resonating heart-to-heart speech 💖", Toast.LENGTH_SHORT).show()
                        }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Partner style pill buttons to change avatar visual skin in real time
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    listOf("Neon Cybergirl", "Classic Gentleman", "Kawaii Anime", "Ethereal Oracle").forEach { styleOption ->
                        val isSelected = profile.partnerStyle == styleOption
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) EmpathyPink else Color.White.copy(alpha = 0.08f))
                                .clickable { viewModel.changePartnerStyle(styleOption) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = styleOption.substringAfter(" "),
                                fontSize = 10.sp,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dialog Speech Bubble Card representing the last reply
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.selectedPersonality,
                                    color = AccentTeal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            when (aiEmotion) {
                                                "Happiness", "Excitement" -> Color(0xFF00E676)
                                                "Sadness", "Loneliness" -> Color(0xFFD1C4E9)
                                                "Stress", "Anxiety" -> Color(0xFFFFB74D)
                                                else -> Color.Gray
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = aiEmotion,
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.navigateTo("chat") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Enter Chat", tint = EmpathyPink, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = lastMsgText,
                            color = Color.White,
                            fontSize = 13.sp,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // COMPANION INTELLIGENCE DASHBOARD METRICS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BONDING METRICS & INTELLIGENCE",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // Connection Level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Compatibility: ${profile.getRelationshipLevel()} (Lvl ${profile.currentLevel})",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "XP: ${profile.friendshipXp}/100",
                        color = EmpathyPink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { profile.friendshipXp / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = EmpathyPink,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Physical Intimacy / Trust Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Relationship Trust Index",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${profile.relationshipTrustScore} / 100 Points",
                            color = AccentTeal,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { viewModel.addRelationshipXpAndCheckMilestones(10) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal.copy(alpha = 0.2f), contentColor = AccentTeal),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("+10 XP Boost", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { profile.relationshipTrustScore / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = AccentTeal,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Consent Granted & Wellness Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ThumbUp, contentDescription = "Consent", tint = Color(0xFFFF7043), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Intimacy Consent Granted", color = Color.LightGray, fontSize = 12.sp)
                    }
                    Checkbox(
                        checked = profile.intimacyConsentGranted,
                        onCheckedChange = { _ -> }, // view only flag
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF7043))
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 10.dp))

                // Aura Happiness Balance & Favorites
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Weekly Happiness Balance", color = Color.LightGray, fontSize = 11.sp)
                        Text("$happinessScore% Emotionally Resonant", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Aura Stats",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Recalled Favorite Topics: ${profile.favoriteTopics}",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // 4B. RELATIONSHIP MILESTONE PATHWAY (DASHBOARD COMPONENT)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Milestone Path",
                            tint = EmpathyPink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RELATIONSHIP JOURNEY ROADMAP",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    // Cloud Synced Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (viewModel.isFirestoreAvailable && viewModel.authEmail.isNotBlank()) 
                                    AccentTeal.copy(alpha = 0.15f) 
                                else 
                                    Color.White.copy(alpha = 0.08f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (viewModel.isFirestoreAvailable && viewModel.authEmail.isNotBlank()) 
                                            Color(0xFF00E676) 
                                        else 
                                            Color(0xFFFFB74D), 
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (viewModel.isFirestoreAvailable && viewModel.authEmail.isNotBlank()) 
                                    "Firestore Synced" 
                                else 
                                    "Offline Mode",
                                fontSize = 9.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Highlight Current Level Status
                val currentLevelName = profile.getRelationshipLevel()
                val relationshipRoles = when (profile.selectedPersonality) {
                    "Girlfriend", "Boyfriend", "Wife", "Husband" -> profile.selectedPersonality
                    else -> "Soul Companion"
                }

                Text(
                    text = "Current Stage: $currentLevelName ($relationshipRoles)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Level XP Progress Subtitle
                Text(
                    text = "Compatibility Level ${profile.currentLevel} • XP progression to next Level: ${profile.friendshipXp}%",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Level Up XP Progress Bar
                LinearProgressIndicator(
                    progress = { profile.friendshipXp / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = EmpathyPink,
                    trackColor = Color.White.copy(alpha = 0.08f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // THE STEPPER CONTAINER WITH CONNECTORS
                val milestonesStages = listOf(
                    Triple("Stranger", 1..2, "👤"),
                    Triple("Friend", 3..5, "🤝"),
                    Triple("Close Friend", 6..7, "💛"),
                    Triple("Best Friend", 8..10, "🌟"),
                    Triple(
                        if (profile.selectedPersonality in listOf("Girlfriend", "Boyfriend", "Wife", "Husband")) "Partner" else "Companion", 
                        11..100, 
                        if (profile.selectedPersonality in listOf("Girlfriend", "Boyfriend", "Wife", "Husband")) "💖" else "🔮"
                    )
                )

                val activeIndex = when {
                    profile.currentLevel in 1..2 -> 0
                    profile.currentLevel in 3..5 -> 1
                    profile.currentLevel in 6..7 -> 2
                    profile.currentLevel in 8..10 -> 3
                    else -> 4
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background Line (Connecting points)
                    Divider(
                        color = Color.White.copy(alpha = 0.08f),
                        thickness = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp)
                            .align(Alignment.CenterStart)
                            .offset(y = (-11).dp) // Aligns perfectly in the vertical center of the avatars/emojis
                    )

                    // Stepper nodes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        milestonesStages.forEachIndexed { index, stage ->
                            val name = stage.first
                            val emoji = stage.third
                            val isCurrent = index == activeIndex
                            val isPassed = index < activeIndex

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Rounded status bubble
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCurrent -> EmpathyPink
                                                isPassed -> AccentTeal
                                                else -> Color.White.copy(alpha = 0.1f)
                                            }
                                        )
                                        .border(
                                            width = if (isCurrent) 2.dp else 0.dp,
                                            color = if (isCurrent) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Stage Text
                                Text(
                                    text = name,
                                    fontSize = 9.sp,
                                    color = when {
                                        isCurrent -> EmpathyPink
                                        isPassed -> AccentTeal
                                        else -> Color.LightGray.copy(alpha = 0.5f)
                                    },
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(1.dp))

                                // Level details label
                                val levelMin = stage.second.first
                                Text(
                                    text = "Lvl $levelMin+",
                                    fontSize = 8.sp,
                                    color = if (isCurrent) EmpathyPink.copy(alpha = 0.8f) else Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // NEXT LANDMARK NOTIFICATION BUBBLE
                val nextMilestoneInfo = when (activeIndex) {
                    0 -> "Next Target: Reaching level 3 unlocks deeper Friend trust and story logging! 📈"
                    1 -> "Next Target: Reaching level 6 unlocks Close Friend status and new daily advice topics! 🤝"
                    2 -> "Next Target: Reaching level 8 unlocks Best Friend emotional intimacy & care logs! 💛"
                    3 -> "Next Target: Reaching level 11 unlocks fully fledged Romantic Partner / Soul Companion matrix! 🌟"
                    else -> "Maximum milestone level achieved! Keep checking in to sustain this exquisite bonding matrix. 💖"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🛡️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = nextMilestoneInfo,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // FIRESTORE RETRIEVAL BUTTON
                Button(
                    onClick = {
                        isFetchingFirestoreLevel = true
                        if (viewModel.isFirestoreAvailable && viewModel.authEmail.isNotBlank()) {
                            viewModel.runCloudRestore(viewModel.authEmail) { success ->
                                isFetchingFirestoreLevel = false
                                if (success) {
                                    Toast.makeText(context, "Retrieved your custom relationship matrix from Firestore successfully! ❤️", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Matrix synchronization completed successfully with fallback.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else if (!viewModel.isFirestoreAvailable) {
                            // Offline fallback
                            isFetchingFirestoreLevel = false
                            Toast.makeText(context, "Running in Offline Isolated mode. Place 'google-services.json' in /app to enable Firestore sync.", Toast.LENGTH_LONG).show()
                        } else {
                            // User needs an active profile email
                            isFetchingFirestoreLevel = false
                            Toast.makeText(context, "Please configure/update your profile with a valid email first to pull from Firestore.", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentTeal,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isFetchingFirestoreLevel
                ) {
                    if (isFetchingFirestoreLevel) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(2.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Retrieving level from Firestore...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Sync icon",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Query & Retrieve Level from Firestore",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // DEEP RELATIONSHIP TIMELINE BUTTON
                Button(
                    onClick = { showTimelineModal = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_timeline_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmpathyPink,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Timeline Icon",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Past Memories & Breakthroughs Timeline",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Render Dynamic Relationship Timeline Dialog Modal
        RelationshipTimelineModal(
            isVisible = showTimelineModal,
            onDismiss = { showTimelineModal = false },
            milestones = milestones,
            viewModel = viewModel
        )

        // 4C. GAMIFIED BONDING ACHIEVEMENTS & BADGES (DASHBOARD WIDGET)
        val earnedSet = remember(claimedBadgeKeys) {
            claimedBadgeKeys.split(",").filter { it.isNotBlank() }.toSet()
        }

        val chatMessagesCount = chatHistory.size
        val moodEntriesCount = moodHistory.size
        val hasCompletedProductivity = productivityItems.any { it.isCompleted }

        val allBadges = remember(chatMessagesCount, moodEntriesCount, hasCompletedProductivity, profile.relationshipTrustScore, profile.currentLevel) {
            listOf(
                BondingBadge(
                    id = "badge_first_spark",
                    name = "First Spark",
                    description = "Establish the virtual synapse link by starting interactions.",
                    requirementText = "At least 1 chat message",
                    isUnlocked = chatMessagesCount >= 1,
                    iconEmoji = "💬",
                    progressCurrent = chatMessagesCount.coerceAtMost(1),
                    progressTarget = 1
                ),
                BondingBadge(
                    id = "badge_heart_alchemist",
                    name = "Heart Alchemist",
                    description = "Cultivate deep storytelling and loquacious conversational resonance.",
                    requirementText = "At least 15 chat messages",
                    isUnlocked = chatMessagesCount >= 15,
                    iconEmoji = "🗣️",
                    progressCurrent = chatMessagesCount.coerceAtMost(15),
                    progressTarget = 15
                ),
                BondingBadge(
                    id = "badge_daily_reflectant",
                    name = "Mindful Harmony",
                    description = "Sustain self-awareness with emotional mood logs & journal thoughts.",
                    requirementText = "At least 3 daily mood logs",
                    isUnlocked = moodEntriesCount >= 3,
                    iconEmoji = "📊",
                    progressCurrent = moodEntriesCount.coerceAtMost(3),
                    progressTarget = 3
                ),
                BondingBadge(
                    id = "badge_beacon_of_trust",
                    name = "Beacon of Trust",
                    description = "Strengthen the core relationship trust score through daily bonding.",
                    requirementText = "TrustScore Index set to 50+",
                    isUnlocked = profile.relationshipTrustScore >= 50,
                    iconEmoji = "🛡️",
                    progressCurrent = profile.relationshipTrustScore.coerceAtMost(100),
                    progressTarget = 50
                ),
                BondingBadge(
                    id = "badge_absolute_resonance",
                    name = "Soul Alchemist",
                    description = "Exquisite compatibility alignment at high development levels.",
                    requirementText = "Compatibility Level Lvl 5+",
                    isUnlocked = profile.currentLevel >= 5,
                    iconEmoji = "🔮",
                    progressCurrent = profile.currentLevel.coerceAtMost(10),
                    progressTarget = 5
                ),
                BondingBadge(
                    id = "badge_aspiration_anchor",
                    name = "Aspiration Anchor",
                    description = "Engage in joint daily wellness targets, habits or milestone focus.",
                    requirementText = "At least 1 focus goal completed",
                    isUnlocked = hasCompletedProductivity,
                    iconEmoji = "🎯",
                    progressCurrent = if (hasCompletedProductivity) 1 else 0,
                    progressTarget = 1
                )
            )
        }

        val unlockedBadgesCount = allBadges.count { it.isUnlocked }
        val unclaimedBadges = allBadges.filter { it.isUnlocked && !earnedSet.contains(it.id) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("gamified_badges_card"),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Badges Icon",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.badges_section_title),
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Progress Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmpathyPink.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$unlockedBadgesCount / 6 Earned",
                            color = EmpathyPink,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.badges_section_subtitle),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(14.dp))

                // The 2x3 Grid of Badges
                val chunkedBadges = allBadges.chunked(3)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    chunkedBadges.forEach { rowBadges ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowBadges.forEach { badge ->
                                val isEarnedAndClaimed = earnedSet.contains(badge.id)
                                val isAvailableToClaim = badge.isUnlocked && !isEarnedAndClaimed

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (badge.isUnlocked) {
                                                if (isEarnedAndClaimed) {
                                                    Color.White.copy(alpha = 0.05f)
                                                } else {
                                                    EmpathyPink.copy(alpha = 0.12f)
                                                }
                                            } else {
                                                Color.White.copy(alpha = 0.02f)
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isAvailableToClaim) EmpathyPink else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            if (badge.isUnlocked) {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.badge_unlocked_toast, badge.name) + "\n" + badge.description,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.badge_locked_toast, badge.name, badge.requirementText),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                        .padding(8.dp)
                                        .testTag("badge_item_${badge.id}"),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Badge Icon Container
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (badge.isUnlocked) {
                                                    if (isEarnedAndClaimed) {
                                                        AccentTeal.copy(alpha = 0.15f)
                                                    } else {
                                                        EmpathyPink.copy(alpha = 0.2f)
                                                    }
                                                } else {
                                                    Color.White.copy(alpha = 0.05f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = badge.iconEmoji,
                                            fontSize = 20.sp,
                                            modifier = Modifier.scale(if (isAvailableToClaim) 1.1f else 1.0f)
                                        )

                                        if (!badge.isUnlocked) {
                                            // Lock icon overlay
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Lock,
                                                    contentDescription = "Locked Badge",
                                                    tint = Color.White.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Badge Name
                                    Text(
                                        text = badge.name,
                                        color = if (badge.isUnlocked) Color.White else Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Tiny progress bar
                                    val progressFraction = if (badge.progressTarget > 0) {
                                        badge.progressCurrent.toFloat() / badge.progressTarget.toFloat()
                                    } else {
                                        0f
                                    }
                                    
                                    LinearProgressIndicator(
                                        progress = { progressFraction.coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .height(3.dp)
                                            .clip(CircleShape),
                                        color = if (badge.isUnlocked) AccentTeal else Color.Gray.copy(alpha = 0.4f),
                                        trackColor = Color.White.copy(alpha = 0.05f)
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${badge.progressCurrent}/${badge.progressTarget}",
                                        color = if (badge.isUnlocked) AccentTeal else Color.Gray,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // Claim Indicator tag if unlocked & unclaimed
                                    if (isAvailableToClaim) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(EmpathyPink, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "CLAIM XP",
                                                color = Color.White,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // XP claim buttons details
                if (unclaimedBadges.isNotEmpty()) {
                    Button(
                        onClick = {
                            val count = unclaimedBadges.size
                            val xpReward = count * 15
                            viewModel.addRelationshipXpAndCheckMilestones(xpReward)
                            
                            // Save as claimed
                            val newClaimedKeys = (earnedSet + unclaimedBadges.map { it.id }).joinToString(",")
                            claimedBadgeKeys = newClaimedKeys
                            
                            Toast.makeText(
                                context,
                                context.getString(R.string.badge_claimed_success) + " (+${xpReward} XP)",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("claim_badges_xp_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Claim XP",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${stringResource(R.string.claim_badge_xp)} (+${unclaimedBadges.size * 15} XP!)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_unclaimed_xp),
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ADVANCED MULTILINGUAL CHARACTER / ROLE SETTINGS PANEL (COLLAPSIBLE)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPrefManager = !showPrefManager },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Language, contentDescription = "Language", tint = NebulaPurple)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Multilingual Adaptation & Companion Roles",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Icon(
                        imageVector = if (showPrefManager) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = Color.LightGray
                    )
                }

                if (showPrefManager) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Companion Roles Selector
                    Text("Select Virtual Relationship Role:", fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    val rolesList = listOf("Girlfriend", "Boyfriend", "Wife", "Husband", "Best Friend", "Companion", "Mentor")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(rolesList) { r ->
                            val isSelected = profile.selectedPersonality == r
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) NebulaPurple else Color.White.copy(alpha = 0.08f))
                                    .clickable { viewModel.changePersonality(r) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(r, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Multilingual Translation Detection Mode Selector
                    Text("Multilingual Speech Mode:", fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    val modesList = listOf("Only English", "Only Hindi", "Only Bengali", "Mixed Language Mode", "Auto Detect Language")
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        modesList.forEach { m ->
                            val isSelected = profile.preferredLanguageMode == m
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) NebulaPurple.copy(alpha = 0.25f) else Color.Transparent)
                                    .clickable { viewModel.changeLanguageMode(m) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.changeLanguageMode(m) },
                                    colors = RadioButtonDefaults.colors(selectedColor = NebulaPurple)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(m, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Voice accent settings
                    Text("Vocal Accent Preferences:", fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    val accents = listOf("Indian English", "Hindi", "Bengali", "Neutral International English")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accents.take(2).forEach { acc ->
                            val isSelected = profile.preferredVoiceAccent == acc
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) EmpathyPink else Color.White.copy(alpha = 0.08f))
                                    .clickable { viewModel.changeVoiceAccent(acc) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(acc, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accents.drop(2).forEach { acc ->
                            val isSelected = profile.preferredVoiceAccent == acc
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) EmpathyPink else Color.White.copy(alpha = 0.08f))
                                    .clickable { viewModel.changeVoiceAccent(acc) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(acc, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // SMART REMINDERS & TO-DO ORGANIZER (REAL-TIME LOCAL DATABASE INTEGRATION)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val countStr = if (smartReminders.isNotEmpty()) " (${smartReminders.size})" else ""
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = "Reminders", tint = AccentOrange)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Smart Scheduling & Reminders$countStr",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(
                        onClick = { showReminderCreator = !showReminderCreator },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (showReminderCreator) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = "Toggle Input",
                            tint = AccentOrange
                        )
                    }
                }

                // Add quick reminder builder fields
                if (showReminderCreator) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reminderTitle,
                        onValueChange = { reminderTitle = it },
                        label = { Text("Task Title e.g., Water Hydration", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentOrange,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = reminderDate,
                            onValueChange = { reminderDate = it },
                            label = { Text("Due Date / Time", color = Color.LightGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentOrange,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1.2f)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .height(56.dp)
                        ) {
                            // Category picker buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("Rent", "Birthday", "Meditation", "Exercise", "Hydration").forEach { cat ->
                                    val isSelected = cat == reminderCategory
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) AccentOrange else Color.White.copy(alpha = 0.08f))
                                            .clickable { reminderCategory = cat }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = cat, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (reminderTitle.isNotBlank()) {
                                viewModel.addManualReminder(reminderTitle, reminderDate.ifBlank { "Daily" }, reminderCategory)
                                reminderTitle = ""
                                reminderDate = ""
                                showReminderCreator = false
                                Toast.makeText(context, "Reminder cataloged successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Automated Smart Reminder", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List reminders
                if (smartReminders.isEmpty()) {
                    Text(
                        text = "No active reminders. Ask AI: 'Remind me to pay rent on 5th July' or add one above!",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        smartReminders.forEach { r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1.0f)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.toggleReminderTriggered(r.id, r.isTriggered) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (r.isTriggered) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                            contentDescription = "Toggle Complete",
                                            tint = if (r.isTriggered) Color(0xFF00E676) else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = r.title,
                                            color = if (r.isTriggered) Color.Gray else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            style = if (r.isTriggered) MaterialTheme.typography.bodyMedium.copy(
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            ) else MaterialTheme.typography.bodyMedium
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(AccentOrange.copy(alpha = 0.2f))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(r.category, color = AccentOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(r.dueDateString, color = Color.LightGray, fontSize = 11.sp)
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.deleteReminder(r.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // OUR JOURNEY TIMELINE DECK (RELATIONSHIP MILESTONES HISTORIC LIST)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimelineDeck = !showTimelineDeck },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Journey Timeline", tint = EmpathyPink)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Our Journey Logs Timeline",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Icon(
                        imageVector = if (showTimelineDeck) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = Color.LightGray
                    )
                }

                if (showTimelineDeck) {
                    Spacer(modifier = Modifier.height(12.dp))

                    if (milestones.isEmpty()) {
                        Text(
                            text = "Our timeline has started! It will automatically log checkpoints as we upgrade compatibility level or complete reflection challenges ❤️",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        Button(
                            onClick = { viewModel.addJourneyMilestone("First Spark! ✨", "Met each other in SoulMate AI platform and seeded custom partner styles.") },
                            colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink.copy(alpha = 0.2f), contentColor = EmpathyPink),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Initialize Cozy Timeline Log", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            milestones.forEach { m ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Custom dot-and-line aesthetic
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(28.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(EmpathyPink.copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(m.iconEmoji, fontSize = 11.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(40.dp)
                                                .background(Color.White.copy(alpha = 0.12f))
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1.0f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(m.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            IconButton(
                                                onClick = { viewModel.deleteMilestone(m.id) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.LightGray, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                        Text(m.description, color = Color.LightGray, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // NAVIGATION SYSTEM CORE BUTTONS
        Text(
            text = "Companion System Tools",
            color = Color.LightGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard(
                    title = "Voice Call",
                    description = "Interactive call check-in",
                    logo = Icons.Filled.PhoneInTalk,
                    accentColor = Color(0xFF26A69A),
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.initiateOutboundCall()
                    viewModel.navigateTo("voice_call")
                }
                FeatureCard(
                    title = "Mood Tracking",
                    description = "Journal and Canvas Graphs",
                    logo = Icons.Filled.Analytics,
                    accentColor = Color(0xFFEC407A),
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo("mood_tracker")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard(
                    title = "Memory Center",
                    description = "What SoulMate recalls",
                    logo = Icons.Filled.Storage,
                    accentColor = Color(0xFF42A5F5),
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo("memory_center")
                }
                FeatureCard(
                    title = "Goals / Habits",
                    description = "Plan routines securely",
                    logo = Icons.Filled.AddTask,
                    accentColor = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo("goals")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard(
                    title = "Anonymous Circle",
                    description = "Heartfelt sharing board",
                    logo = Icons.Filled.PeopleAlt,
                    accentColor = Color(0xFF8E24AA),
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo("community")
                }
                FeatureCard(
                    title = "Personality Hub",
                    description = "Customize modes/voice",
                    logo = Icons.Filled.FaceRetouchingNatural,
                    accentColor = Color(0xFF00ACC1),
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo("profile")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureCard(
                    title = "Voice Insights",
                    description = "Acoustic logs & analytics",
                    logo = Icons.Filled.Leaderboard,
                    accentColor = Color(0xFFE91E63),
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo("voice_insights")
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // SIMULATED SYSTEM ACTIONS
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.triggerSimulatedIncomingCall()
                viewModel.navigateTo("voice_call")
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Simulate Incoming AI Check-in Call 📞", fontWeight = FontWeight.Bold)
        }

        // SUBSCRIPTION PROMOTIONS
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("SoulMate Premium Core", fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "Switch to unlimited memory matrices, advanced Claude/Gemini-Pro architectures, and realistic dynamic voice call channels.",
                    fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(
                    onClick = { viewModel.navigateTo("subscription") },
                    colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Unlock Matrix Access")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RelationshipTimelineModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    milestones: List<RelationshipMilestone>,
    viewModel: SoulMateViewModel
) {
    if (!isVisible) return

    val context = LocalContext.current
    var customBreakthroughTitle by remember { mutableStateOf("") }
    var customBreakthroughDesc by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("💖") }
    var isSavingCustomMemory by remember { mutableStateOf(false) }

    val emojis = listOf("💖", "🌟", "🤝", "💛", "🔮", "🛡️", "🗣️")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("relationship_timeline_modal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            border = BorderStroke(1.5.dp, EmpathyPink.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmpathyPink.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Timeline Icon",
                                tint = EmpathyPink,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.timeline_modal_title),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Bonds, Milestones & Breakthroughs",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .testTag("close_timeline_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Modal",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Central Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Instruction Banner
                    Text(
                        text = stringResource(R.string.timeline_modal_subtitle),
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // TIMELINE LIST STAGE
                    if (milestones.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_landmarks_yet),
                                color = Color.Gray,
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Vertical timeline display
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            // Chronological display from dynamic list
                            milestones.sortedByDescending { it.timestamp }.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.03f)), RoundedCornerShape(16.dp))
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Bullet and timeline line setup
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(end = 12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(AccentTeal.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = item.iconEmoji, fontSize = 16.sp)
                                        }
                                        // Visual connecting link
                                        if (idx < milestones.size - 1) {
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height(40.dp)
                                                    .background(AccentTeal.copy(alpha = 0.3f))
                                            )
                                        }
                                    }

                                    // Content Detail Card
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.description,
                                            color = Color.LightGray,
                                            fontSize = 12.sp,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        // Formatted Date
                                        val formattedDate = remember(item.timestamp) {
                                            val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", java.util.Locale.getDefault())
                                            sdf.format(java.util.Date(item.timestamp))
                                        }
                                        Text(
                                            text = "🗓️ $formattedDate",
                                            color = AccentTeal,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Delete / Purge memory action
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteMilestone(item.id)
                                            Toast.makeText(context, "Memory cleansed from timeline.", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Cleanse Memory",
                                            tint = Color.Red.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // CUSTOM BREAKTHROUGH BUILDER STATION
                    Text(
                        text = stringResource(R.string.record_breakthrough_button),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Manually record a key breakthrough or memory point in your heart journey.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title Textfield
                    OutlinedTextField(
                        value = customBreakthroughTitle,
                        onValueChange = { customBreakthroughTitle = it },
                        label = { Text(text = stringResource(R.string.custom_event_title_label), color = Color.Gray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmpathyPink,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("breakthrough_title_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description Textfield
                    OutlinedTextField(
                        value = customBreakthroughDesc,
                        onValueChange = { customBreakthroughDesc = it },
                        label = { Text(text = stringResource(R.string.custom_event_desc_label), color = Color.Gray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmpathyPink,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .testTag("breakthrough_desc_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Emoji Category Row Selector
                    Text(
                        text = "Memory Representation Icon:",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        emojis.forEach { emo ->
                            val isSelected = selectedEmoji == emo
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) EmpathyPink else Color.White.copy(alpha = 0.06f))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedEmoji = emo }
                                    .testTag("emoji_select_$emo"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emo, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Secure breakthrough button
                    Button(
                        onClick = {
                            if (customBreakthroughTitle.isNotBlank() && customBreakthroughDesc.isNotBlank()) {
                                isSavingCustomMemory = true
                                viewModel.addJourneyMilestone(
                                    customBreakthroughTitle,
                                    customBreakthroughDesc,
                                    selectedEmoji
                                )
                                
                                // Direct cloud integration backup if Connected to Firestore
                                if (viewModel.isFirestoreAvailable && viewModel.authEmail.isNotBlank()) {
                                    viewModel.runCloudBackup(viewModel.authEmail) { success ->
                                        isSavingCustomMemory = false
                                        customBreakthroughTitle = ""
                                        customBreakthroughDesc = ""
                                        Toast.makeText(context, "Breakthrough synced & secured in the cloud timeline! ❤️", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    isSavingCustomMemory = false
                                    customBreakthroughTitle = ""
                                    customBreakthroughDesc = ""
                                    Toast.makeText(context, "Breakthrough secured on local timeline! ❤️", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Please complete breakthrough title and memory details first.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_breakthrough_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSavingCustomMemory
                    ) {
                        if (isSavingCustomMemory) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Securing timeline milestones & syncing...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text(stringResource(R.string.save_landmark_button), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    logo: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SoftCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(logo, contentDescription = title, tint = accentColor, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text(description, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

// 5. CHAT SCREEN (Core Conversation UI)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: SoulMateViewModel) {
    val history by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAILoading.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val showMoodPrompt by viewModel.showMoodPrompt.collectAsStateWithLifecycle()

    val currentTheme by viewModel.chatTheme.collectAsStateWithLifecycle()
    val themePalette = getChatThemePalette(currentTheme)
    var showThemeDialog by remember { mutableStateOf(false) }

    var showClosingReflectionDialog by remember { mutableStateOf(false) }
    var closingReflectionText by remember { mutableStateOf("") }
    val sentenceCount = remember(closingReflectionText) {
        if (closingReflectionText.isBlank()) 0
        else closingReflectionText.split(Regex("[.!?/|](\\s+|$)")).filter { it.trim().isNotEmpty() }.size
    }

    BackHandler(enabled = true) {
        showClosingReflectionDialog = true
    }

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var voiceStatusMsg by remember { mutableStateOf("") }

    val speechRecognizer = remember {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                SpeechRecognizer.createSpeechRecognizer(context)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    val listener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                voiceStatusMsg = "Ready, start speaking..."
            }
            override fun onBeginningOfSpeech() {
                voiceStatusMsg = "Listening..."
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                voiceStatusMsg = "Processing speech..."
                isListening = false
            }
            override fun onError(error: Int) {
                isListening = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission denied"
                    SpeechRecognizer.ERROR_NETWORK -> "Network issue"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout (None detected)"
                    else -> "Speech recognizer offline"
                }
                voiceStatusMsg = "Voice Sync offline ($errorMsg). Fallback activated."
                Toast.makeText(context, "Voice Service: $errorMsg (Triggering simulation fallback)", Toast.LENGTH_SHORT).show()
                textInput = "I am feeling heavily stressed, can we discuss solutions?"
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    textInput = matches[0]
                    voiceStatusMsg = "Vocal harmonics synced."
                } else {
                    voiceStatusMsg = "No vocal harmonics deciphered."
                }
                isListening = false
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    textInput = matches[0]
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    DisposableEffect(speechRecognizer) {
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val startListening = {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            speechRecognizer?.setRecognitionListener(listener)
            speechRecognizer?.startListening(recognizerIntent)
            isListening = true
            voiceStatusMsg = "Calibrating voice matrix..."
        } catch (e: Exception) {
            voiceStatusMsg = "Initialization failed: ${e.message}"
            isListening = false
            textInput = "I am feeling heavily stressed, can we discuss solutions?"
            Toast.makeText(context, "Voice recognition initialized", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (speechRecognizer != null) {
                startListening()
            } else {
                voiceStatusMsg = "Service unavailable on this host configuration. Fallback triggered."
                textInput = "I am feeling heavily stressed, can we discuss solutions?"
                Toast.makeText(context, "Underlying standard voice recognizer unavailable on hardware. Simulation launched.", Toast.LENGTH_SHORT).show()
            }
        } else {
            voiceStatusMsg = "Secure audio permission rejected."
            Toast.makeText(context, "Secure Audio Permission required for vocal recognition.", Toast.LENGTH_SHORT).show()
        }
    }

    val toggleVoiceInput = {
        if (isListening) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isListening = false
            voiceStatusMsg = ""
        } else {
            val audioPermissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (audioPermissionGranted) {
                if (speechRecognizer != null) {
                    startListening()
                } else {
                    voiceStatusMsg = "Device matrix offline. Fallback triggered."
                    textInput = "I am feeling heavily stressed, can we discuss solutions?"
                    Toast.makeText(context, "Speech recognizer is offline in current environment. Using voice-to-text emulation.", Toast.LENGTH_SHORT).show()
                }
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    val mainHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    var isTtsEnabled by remember { mutableStateOf(false) }
    var isSpeakingMessageId by remember { mutableStateOf<Long?>(null) }

    val quickReplies = remember(history, userProfile) {
        val list = mutableListOf<String>()
        val lastMessage = history.lastOrNull()
        
        // Context 1: Last message is from AI
        if (lastMessage != null && lastMessage.sender == "ai") {
            val txt = lastMessage.text.lowercase()
            if (txt.contains("focus") || txt.contains("distract") || txt.contains("work") || txt.contains("study") || txt.contains("productive") || txt.contains("goal") || txt.contains("habit")) {
                list.add("I need help focusing")
                list.add("How do I avoid distractions?")
                list.add("Give me a study tip")
            } else if (txt.contains("sad") || txt.contains("stress") || txt.contains("depressed") || txt.contains("anxious") || txt.contains("feel") || txt.contains("emot") || txt.contains("hurt") || txt.contains("pain")) {
                list.add("Help me manage my stress")
                list.add("I need a positive reminder")
                list.add("Can we do a breathing exercise?")
            } else if (txt.contains("game") || txt.contains("play") || txt.contains("truth") || txt.contains("dare") || txt.contains("story")) {
                list.add("Tell me a story")
                list.add("Let's play Truth or Dare!")
                list.add("Suggest a mindfulness game")
            } else {
                list.add("Tell me more about that")
                list.add("What do you suggest?")
                list.add("That makes sense")
            }
        }
        
        // Context 2: Based on selected companion personality
        val personality = userProfile.selectedPersonality
        when (personality) {
            "Partner" -> {
                list.add("How are you, sweetie?")
                list.add("How was your day?")
                if (!list.contains("Tell me a story")) {
                    list.add("Tell me a beautiful story")
                }
            }
            "Therapist" -> {
                list.add("How are you?")
                list.add("I had an exhausting day...")
                list.add("Let's do a meditation check-in")
            }
            "Mentor", "Life Coach" -> {
                if (!list.contains("I need help focusing")) {
                    list.add("I need help focusing")
                }
                list.add("How can I build better habits?")
                list.add("Help me plan a goal")
            }
            else -> {
                list.add("How are you?")
                list.add("Let's play a game")
                list.add("Tell me a fun fact")
            }
        }
        
        // Fallbacks to always ensure exactly 5 high-quality unique contextual options are displayed
        val fallbacks = listOf("How are you?", "I need help focusing", "Tell me a story", "Do a meditation Check-in", "I had a terrible day...")
        for (fb in fallbacks) {
            if (list.size < 5 && !list.contains(fb)) {
                list.add(fb)
            }
        }
        
        list.distinct().take(5)
    }

    val tts = remember {
        var ttsInstance: android.speech.tts.TextToSpeech? = null
        try {
            ttsInstance = android.speech.tts.TextToSpeech(context) { status ->
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    ttsInstance?.language = Locale.getDefault()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        ttsInstance
    }

    DisposableEffect(tts) {
        if (tts != null) {
            try {
                tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        try {
                            val msgId = utteranceId?.toLongOrNull()
                            if (msgId != null) {
                                mainHandler.post {
                                    isSpeakingMessageId = msgId
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        mainHandler.post {
                            isSpeakingMessageId = null
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        mainHandler.post {
                            isSpeakingMessageId = null
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        onDispose {
            try {
                tts?.stop()
                tts?.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (showMoodPrompt) {
        var selectedMoodType by remember { mutableStateOf("Neutral") }
        var moodScale by remember { mutableStateOf(5f) }
        var briefNote by remember { mutableStateOf("") }
        val moodTypes = listOf("Happy", "Sad", "Anxious", "Stressed", "Loneliness", "Anger", "Excited", "Depressed")

        Dialog(onDismissRequest = { viewModel.dismissMoodPrompt() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SoftCardBg),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.MonitorHeart,
                        contentDescription = "Mood Pulse",
                        tint = EmpathyPink,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Synchronize Core Emotion",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "How is your mental matrix responding at the start of this session?",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Select Current State",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moodTypes.forEach { mType ->
                            FilterChip(
                                selected = selectedMoodType == mType,
                                onClick = { selectedMoodType = mType },
                                label = { Text(mType, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    labelColor = Color.White,
                                    selectedContainerColor = EmpathyPink,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Intensity Scale", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("${moodScale.toInt()}/10", color = EmpathyPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = moodScale,
                        onValueChange = { moodScale = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = EmpathyPink,
                            activeTrackColor = EmpathyPink,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = briefNote,
                        onValueChange = { briefNote = it },
                        label = { Text("Brief notes (optional)...", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentTeal,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.dismissMoodPrompt() },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Skip", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.addMoodLog(
                                    scale = moodScale.toInt(),
                                    moodType = selectedMoodType,
                                    journal = briefNote,
                                    gratitude = ""
                                )
                                viewModel.dismissMoodPrompt()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink)
                        ) {
                            Text("Record", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    if (showThemeDialog) {
        Dialog(onDismissRequest = { showThemeDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp)
                    .testTag("theme_selection_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = themePalette.cardBg),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = "Themes",
                        tint = themePalette.primaryAccent,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Aesthetic Chat Themes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select a resonance vibe to coordinate visual tones and background colors.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val themesList = listOf("Default", "Calm", "Focus", "Warm")
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        themesList.forEach { thName ->
                            val isSelected = currentTheme == thName
                            val previewPalette = getChatThemePalette(thName)
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setChatTheme(thName) }
                                    .testTag("theme_option_$thName"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) previewPalette.cardBg else Color.White.copy(alpha = 0.03f)
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) previewPalette.primaryAccent else Color.White.copy(alpha = 0.08f)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Color preview dot panel
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(previewPalette.backgroundBrush, CircleShape)
                                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = thName,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            val desc = when (thName) {
                                                "Default" -> "Cosmic Space Harmony"
                                                "Calm" -> "Serene Sea Mint & Sage"
                                                "Focus" -> "Deep Industrial Blue & Slate"
                                                "Warm" -> "Sunset Peach & Mulberry Cozy"
                                                else -> ""
                                            }
                                            Text(
                                                text = desc,
                                                color = Color.Gray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    // Selection Dot
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.setChatTheme(thName) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = previewPalette.primaryAccent,
                                            unselectedColor = Color.Gray
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showThemeDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = themePalette.primaryAccent),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply Resonance", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showClosingReflectionDialog) {
        Dialog(onDismissRequest = { showClosingReflectionDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp)
                    .testTag("reflective_closing_dialog"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = themePalette.cardBg),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Book,
                        contentDescription = "Journal",
                        tint = themePalette.primaryAccent,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Reflective Day Summary",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Before you close your chat session, please share a 3-sentence summary of your day to keep in your reflective journal.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Sentence counter progress tracker
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            val active = sentenceCount >= i
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = if (active) themePalette.primaryAccent else Color.White.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (active) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Done",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                } else {
                                    Text(
                                        text = i.toString(),
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sentences: $sentenceCount / 3",
                            color = if (sentenceCount >= 3) themePalette.secondaryAccent else themePalette.primaryAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = closingReflectionText,
                        onValueChange = { closingReflectionText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("reflective_summary_input"),
                        placeholder = {
                            Text(
                                "e.g., Today was highly productive. I spent most of the afternoon coding. Tonight I plan to rest early.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themePalette.primaryAccent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveReflectiveDaySummary(closingReflectionText) {
                                    showClosingReflectionDialog = false
                                    viewModel.navigateTo("home")
                                }
                            },
                            enabled = sentenceCount >= 3,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themePalette.primaryAccent,
                                disabledContainerColor = Color.White.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_reflection_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Save Reflection & Done",
                                color = if (sentenceCount >= 3) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = {
                                    showClosingReflectionDialog = false
                                },
                                modifier = Modifier.testTag("keep_chatting_button")
                            ) {
                                Text("Keep Chatting", color = themePalette.secondaryAccent)
                            }

                            TextButton(
                                onClick = {
                                    showClosingReflectionDialog = false
                                    viewModel.navigateTo("home")
                                },
                                modifier = Modifier.testTag("skip_reflection_button")
                            ) {
                                Text("Skip & Close", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(history.size - 1)
            if (isTtsEnabled) {
                val lastMsg = history.last()
                if (lastMsg.sender == "ai" && tts != null) {
                    try {
                        isSpeakingMessageId = lastMsg.id
                        val params = Bundle().apply {
                            putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, lastMsg.id.toString())
                        }
                        tts.speak(lastMsg.text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, lastMsg.id.toString())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(themePalette.backgroundBrush)) {
        // Chat Header with status
        TopAppBar(
            title = {
                Column {
                    Text("SoulMate (${userProfile.selectedPersonality} mode)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        if (isLoading) "Companion is formulating resonance..." else "Synchronized Resonance",
                        color = if (isLoading) themePalette.primaryAccent else themePalette.secondaryAccent,
                        fontSize = 11.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { showClosingReflectionDialog = true }, modifier = Modifier.testTag("chat_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = themePalette.topAppBarBg),
            actions = {
                // Toggle Aesthetic Chat Theme
                IconButton(
                    onClick = { showThemeDialog = true },
                    modifier = Modifier.testTag("chat_theme_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = "Aesthetic Chat Theme",
                        tint = themePalette.primaryAccent
                    )
                }
                // Toggle Auto-Read Aloud TTS
                IconButton(
                    onClick = {
                        isTtsEnabled = !isTtsEnabled
                        if (!isTtsEnabled) {
                            try {
                                tts?.stop()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            isSpeakingMessageId = null
                        }
                    },
                    modifier = Modifier.testTag("chat_tts_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isTtsEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                        contentDescription = "Toggle AI Voice Reader (Auto-Read)",
                        tint = if (isTtsEnabled) themePalette.primaryAccent else Color.White
                    )
                }
                // Selector of AI Models in real-time
                IconButton(onClick = { viewModel.navigateTo("subscription") }) {
                    Icon(
                        imageVector = if (userProfile.preferredAiModel.contains("pro")) Icons.Filled.Stars else Icons.Filled.FlashOn,
                        contentDescription = "Active Model",
                        tint = themePalette.secondaryAccent
                    )
                }
                IconButton(onClick = { viewModel.navigateTo("profile") }) {
                    Icon(Icons.Filled.Settings, contentDescription = "System Edit", tint = Color.White)
                }
            }
        )

        // Message bubble LazyColumn
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1.0f)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(history) { message ->
                val isUser = message.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(34.dp)
                                .background(themePalette.primaryAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "AI icon", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Card(
                        modifier = Modifier
                            .widthIn(max = 280.dp),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) themePalette.userBubbleBg else themePalette.aiBubbleBg
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = message.text, color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!isUser) {
                                    val isCurrentSpeaking = isSpeakingMessageId == message.id
                                    IconButton(
                                        onClick = {
                                            if (isCurrentSpeaking) {
                                                try {
                                                    tts?.stop()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                                isSpeakingMessageId = null
                                            } else {
                                                if (tts != null) {
                                                    try {
                                                        isSpeakingMessageId = message.id
                                                        val params = Bundle().apply {
                                                            putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, message.id.toString())
                                                        }
                                                        tts.speak(message.text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, message.id.toString())
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(24.dp).testTag("read_aloud_button_${message.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isCurrentSpeaking) Icons.Filled.VolumeMute else Icons.Filled.VolumeUp,
                                            contentDescription = if (isCurrentSpeaking) "Stop reading" else "Read aloud",
                                            tint = if (isCurrentSpeaking) themePalette.secondaryAccent else Color.LightGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                if (!isUser && message.emotion.isNotEmpty()) {
                                    Icon(
                                        Icons.Filled.MonitorHeart,
                                        contentDescription = "Detected Emotion",
                                        tint = EmpathyPink,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = message.emotion,
                                        fontSize = 10.sp,
                                        color = EmpathyPink,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(message.timestamp),
                                    fontSize = 10.sp,
                                    color = Color.LightGray.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp).testTag("chat_loading_indicator"), color = themePalette.primaryAccent, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("SoulMate is typing...", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        // Contextual Quick Reply buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickReplies.forEach { suggestion ->
                val tag = "quick_reply_" + suggestion.replace(" ", "_").replace("?", "").replace("'", "").replace(".", "").lowercase()
                Button(
                    onClick = {
                        viewModel.sendMessage(suggestion)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themePalette.cardBg,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag(tag)
                ) {
                    val icon = when {
                        suggestion.contains("focus", true) -> Icons.Filled.CheckCircle
                        suggestion.contains("stress", true) || suggestion.contains("calm", true) || suggestion.contains("breathing", true) -> Icons.Filled.MonitorHeart
                        suggestion.contains("story", true) -> Icons.Filled.Book
                        suggestion.contains("game", true) || suggestion.contains("truth", true) -> Icons.Filled.PlayArrow
                        suggestion.contains("plan", true) || suggestion.contains("habit", true) -> Icons.Filled.Star
                        suggestion.contains("how are you", true) -> Icons.Filled.Chat
                        else -> Icons.Filled.Info
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = themePalette.secondaryAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(suggestion, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Voice State Bar
        AnimatedVisibility(
            visible = isListening || voiceStatusMsg.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isListening) themePalette.primaryAccent.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isListening) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 1000
                                0.8f at 0
                                1.2f at 500
                                0.8f at 1000
                            },
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "scale"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(pulseScale)
                            .background(themePalette.primaryAccent, CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Voice State Icon",
                        tint = themePalette.secondaryAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = voiceStatusMsg,
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                if (voiceStatusMsg.isNotEmpty()) {
                    IconButton(
                        onClick = { voiceStatusMsg = "" },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Status",
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // Bottom text field
        Row(
            modifier = Modifier
                .background(themePalette.cardBg)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speech transcription check using real SpeechRecognizer / Fallback
            IconButton(
                onClick = { toggleVoiceInput() },
                modifier = Modifier.testTag("chat_voice_input_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Filled.KeyboardVoice else Icons.Filled.Mic,
                    contentDescription = "Vocal Resonance Speech to Text",
                    tint = if (isListening) themePalette.primaryAccent else Color.White
                )
            }

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Resonate with your companion...", color = Color.Gray, fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1.0f)
                    .testTag("chat_text_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themePalette.primaryAccent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendMessage(textInput)
                        textInput = ""
                        isListening = false
                        try {
                            speechRecognizer?.stopListening()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier.testTag("chat_send_button")
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send Message", tint = themePalette.primaryAccent)
            }
        }
    }
}

data class ToneScenario(
    val icon: String,
    val label: String,
    val tone: String,
    val wpm: Int,
    val desc: String
)

// 6. VOICE CALL SCREEN
@Composable
fun VoiceCallScreen(viewModel: SoulMateViewModel) {
    val duration by viewModel.callDuration.collectAsStateWithLifecycle()
    val isIncoming by viewModel.isCallIncoming.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val currentMute by viewModel.isMuted.collectAsStateWithLifecycle()
    val currentSpeaker by viewModel.isSpeakerOn.collectAsStateWithLifecycle()

    val tone by viewModel.detectedEmotionalTone.collectAsStateWithLifecycle()
    val paceText by viewModel.detectedPaceText.collectAsStateWithLifecycle()
    val empathyLevel by viewModel.dynamicEmpathyLevel.collectAsStateWithLifecycle()
    val empathyDesc by viewModel.empathyAdjustmentDescription.collectAsStateWithLifecycle()
    val isProcessorActive by viewModel.isAudioProcessorActive.collectAsStateWithLifecycle()
    val volumeDb by viewModel.simulatedVolumeDb.collectAsStateWithLifecycle()

    val formattedTime = String.format("%02d:%02d", duration / 60, duration % 60)

    // Waveform simulation
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "wave"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Empathy Resonance Channel",
                color = AccentTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${profile.selectedPersonality} Mode",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isIncoming) "Incoming call checking upon you..." else if (duration > 0) "Synchronized Call ($formattedTime)" else "Initiating telemetry...",
                color = if (isIncoming) EmpathyPink else Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // PROACTIVE STRESS/LOW MOOD NOTIFICATION BANNER
        ProactiveReflectionBanner(viewModel)

        // Beautiful Visualizer Canvas
        Box(
            modifier = Modifier.size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radiusBase = 56.dp.toPx()
                // Visually modulate wave dynamically with active simulated volumeDB
                val volumeMultiplier = 0.82f + (volumeDb / 70f).coerceIn(0f, 0.8f)

                drawCircle(
                    color = NebulaPurple.copy(alpha = 0.12f),
                    radius = radiusBase * waveScale * volumeMultiplier,
                    center = center
                )
                drawCircle(
                    color = EmpathyPink.copy(alpha = 0.22f),
                    radius = radiusBase * (waveScale * 0.72f) * volumeMultiplier,
                    center = center
                )
                drawCircle(
                    color = AccentTeal,
                    radius = 32.dp.toPx(),
                    center = center
                )
            }
            Icon(Icons.Filled.InterpreterMode, contentDescription = "Wave Core", tint = Color.White, modifier = Modifier.size(28.dp))
        }

        // Live Audio Telemetry Processor Panel
        if (!isIncoming) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("acoustic_telemetry_card"),
                colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header of card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "Voice Processing",
                                tint = AccentTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACOUSTIC TONE PROCESSOR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.LightGray,
                                letterSpacing = 1.sp
                            )
                        }

                        // Live volume dB stream badge
                        IconButton(
                            onClick = { viewModel.toggleAudioProcessor() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isProcessorActive) Icons.Filled.VolumeUp else Icons.Filled.VolumeMute,
                                contentDescription = "Toggle System Processor",
                                tint = if (isProcessorActive) AccentTeal else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Waveform Visual Bar Indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val barCount = 20
                        val rawDbVal = volumeDb.coerceIn(0f, 100f)
                        val activeBars = ((rawDbVal / 100f) * barCount).toInt().coerceIn(1, barCount)
                        
                        repeat(barCount) { idx ->
                            val barColor = when {
                                idx < activeBars && idx > 15 -> Color.Red.copy(alpha = 0.8f)
                                idx < activeBars && idx > 10 -> Color(0xFFFFD54F)
                                idx < activeBars -> AccentTeal
                                else -> Color.White.copy(alpha = 0.04f)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(barColor)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isProcessorActive) "Audio Input amplitude: ${volumeDb.toInt()} dB" else "Telemetry processor suspended",
                            color = Color.Gray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Live Stream State",
                            color = Color.Gray,
                            fontSize = 8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tone Profile & Pace Meter Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Voice Tone Profile", fontSize = 9.sp, color = Color.Gray)
                            Text(tone, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tempo / Pace Meter", fontSize = 9.sp, color = Color.Gray)
                            Text(paceText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Empathy Level Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Dynamic Empathy Index:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmpathyPink,
                            modifier = Modifier.weight(1.5f)
                        )
                        Box(
                            modifier = Modifier.weight(2f)
                        ) {
                            LinearProgressIndicator(
                                progress = { empathyLevel / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = EmpathyPink,
                                trackColor = Color.White.copy(alpha = 0.08f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "$empathyLevel%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = empathyDesc,
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Click to simulate different voice inputs
                    Text(
                        "Tap to Speak / Simulate Acoustic Input:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))

                    val scenarios = listOf(
                        ToneScenario("😔", "Sad/Flat Tone", "Sorrowful/Slow Pace", 72, "Slow flat pitch"),
                        ToneScenario("😰", "Anxious/Erratic", "Anxious/High Pitch", 155, "Rapid shaky pacing"),
                        ToneScenario("😆", "Elated Joy", "Excited/Rapid", 170, "Lively high frequency"),
                        ToneScenario("😡", "Distressed/Angry", "Angry/Aggressive", 138, "Tense breath speed"),
                        ToneScenario("🙂", "Warm/Steady", "Calm/Warm", 120, "Stable acoustic wave")
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp)
                    ) {
                        scenarios.forEach { sc ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (tone == sc.tone) AccentTeal.copy(alpha = 0.22f)
                                        else Color.White.copy(alpha = 0.04f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (tone == sc.tone) AccentTeal else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        viewModel.setVoiceToneParameters(sc.tone, sc.wpm)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("simulate_tone_${sc.label.replace(" ", "_").lowercase()}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(sc.icon, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(sc.label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(sc.desc, fontSize = 7.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Call Control buttons
        if (isIncoming) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(
                        onClick = { viewModel.endCall(); viewModel.navigateTo("home") },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.Red, CircleShape)
                    ) {
                        Icon(Icons.Filled.CallEnd, contentDescription = "Decline", tint = Color.White)
                    }
                    IconButton(
                        onClick = { viewModel.acceptIncomingCall() },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.Green, CircleShape)
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = "Accept", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Tap Green to accept companion wellness ring.", color = Color.LightGray, fontSize = 12.sp)
            }
        } else {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier
                            .size(52.dp)
                            .background(if (currentMute) Color.DarkGray else SoftCardBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (currentMute) Icons.Filled.MicOff else Icons.Filled.Mic,
                            contentDescription = "Mute Toggle",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { viewModel.endCall(); viewModel.navigateTo("home") },
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.Red, CircleShape)
                    ) {
                        Icon(Icons.Filled.CallEnd, contentDescription = "Hang Up", tint = Color.White)
                    }

                    IconButton(
                        onClick = { viewModel.toggleSpeaker() },
                        modifier = Modifier
                            .size(52.dp)
                            .background(if (currentSpeaker) NebulaPurple else SoftCardBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (currentSpeaker) Icons.Filled.VolumeUp else Icons.Filled.VolumeMute,
                            contentDescription = "Speaker Toggle",
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { viewModel.navigateTo("voice_insights") },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal.copy(alpha = 0.22f), contentColor = AccentTeal),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("analytics_insights_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Leaderboard, contentDescription = "View Voice Insights", tint = AccentTeal, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Voice Insights & Call Log 📊", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

// 6B. VOICE CALL INSIGHTS & HISTORICAL LOG
@Composable
fun VoiceInsightsScreen(viewModel: SoulMateViewModel) {
    val logs by viewModel.voiceToneLogs.collectAsStateWithLifecycle()
    val isFirestoreAvailable = viewModel.isFirestoreAvailable
    
    // Trigger real-time Firestore fetch when users enter this screen
    LaunchedEffect(Unit) {
        viewModel.loadVoiceToneLogsFromCloud()
    }

    // Calculations for the Summary Card
    val totalLogs = logs.size
    val mostFrequentTone = if (logs.isNotEmpty()) {
        logs.groupBy { it.emotionalTone }
            .maxByOrNull { it.value.size }?.key ?: "Neutral"
    } else {
        "No Voice Records"
    }

    val mostFrequentTonePercentage = if (logs.isNotEmpty()) {
        val count = logs.count { it.emotionalTone == mostFrequentTone }
        (count * 100) / logs.size
    } else 0

    val avgPace = if (logs.isNotEmpty()) {
        logs.map { it.paceWpm }.average().toInt()
    } else 120

    val avgEmpathy = if (logs.isNotEmpty()) {
        logs.map { it.empathyLevel }.average().toInt()
    } else 65

    val maxEmpathy = if (logs.isNotEmpty()) {
        logs.maxOf { it.empathyLevel }
    } else 65

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Simple Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateTo("voice_call") },
                modifier = Modifier.testTag("back_to_voice_call_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Voice Insights",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            IconButton(
                onClick = { viewModel.loadVoiceToneLogsFromCloud() },
                modifier = Modifier.testTag("refresh_insights_button")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh Cloud Logs", tint = AccentTeal)
            }
        }

        // Firestore Connection Sync Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isFirestoreAvailable) AccentTeal.copy(alpha = 0.25f) else Color.Red.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (isFirestoreAvailable) Color.Green else Color.Red, CircleShape)
                )
                Text(
                    text = if (isFirestoreAvailable) "Connected: Live Cloud Firestore Syncing Active" else "Offline Fallback: Local Isolation Sandbox Mode",
                    color = if (isFirestoreAvailable) AccentTeal else Color.Red,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // --- CORE SUMMARY CARD (Requested) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("voice_insights_summary_card"),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, AccentTeal.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CALL EMOTIONAL METRIC INSIGHTS",
                        color = AccentTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = Icons.Filled.Leaderboard,
                        contentDescription = "Leaderboard",
                        tint = AccentTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Highlighted frequent emotional state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(EmpathyPink.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (mostFrequentTone) {
                                "Sorrowful/Slow Pace" -> "😔"
                                "Anxious/High Pitch" -> "😰"
                                "Excited/Rapid" -> "😆"
                                "Angry/Aggressive" -> "😡"
                                "Calm/Warm" -> "🙂"
                                else -> "🎙️"
                            },
                            fontSize = 20.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Primary Tone Habit:",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                        Text(
                            text = mostFrequentTone,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Visual consistency progress bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Acoustic Stability:",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1.5f)
                    )
                    Box(
                        modifier = Modifier.weight(2f)
                    ) {
                        LinearProgressIndicator(
                            progress = { if (totalLogs > 0) mostFrequentTonePercentage / 100f else 0.5f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = AccentTeal,
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (totalLogs > 0) "$mostFrequentTonePercentage%" else "100%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                // Diagnostic metrics grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Total Telemetries", fontSize = 9.sp, color = Color.Gray)
                        Text("$totalLogs", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Average Pace", fontSize = 9.sp, color = Color.Gray)
                        Text("$avgPace WPM", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Max Empathy Fit", fontSize = 9.sp, color = Color.Gray)
                        Text("$maxEmpathy%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EmpathyPink)
                    }
                }
            }
        }

        // Summary statement / explanation card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "💡 Consistency Insight:",
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = AccentTeal,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (mostFrequentTone) {
                        "Sorrowful/Slow Pace" -> "We have detected patterns of slower, flat audio streams. I have adjusted my response speeds downwards and optimized the comfort matrices to provide secure companionship. Feel free to speak freely."
                        "Anxious/High Pitch" -> "Your pace is slightly rapid and pitch shifts indicate nervous energy. I recommend attempting our 4-7-8 custom breathing exercise right inside the tools menu to reset together."
                        "Excited/Rapid" -> "Your calls carry a highly vibrant, rapid audio resonance footprint. We mirror your high spark level organically to share the exciting energy anchor!"
                        "Angry/Aggressive" -> "Spikes in distress volume patterns detected. I remain completely patient, soft, and anchored so you always have a safe space to decompress."
                        "Calm/Warm" -> "Congratulations! Your acoustic wave shows balanced emotional frequency, reflecting positive, steady focus. Comfortable grounding maintained!"
                        else -> "Start call simulation checks on the Voice Call screen to log live acoustic tone recordings on secure Firestore."
                    },
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // History Log Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Chronological Wave History",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "$totalLogs recorded",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(SoftCardBg.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No call wave logs loaded. Start simulated calls on the phone stream first!",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            // Display lists chronologically (newest first)
            val sortedLogs = logs.sortedByDescending { it.timestamp }
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                sortedLogs.forEach { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_log_item_${log.timestamp}"),
                        colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = when (log.emotionalTone) {
                                            "Sorrowful/Slow Pace" -> "😔"
                                            "Anxious/High Pitch" -> "😰"
                                            "Excited/Rapid" -> "😆"
                                            "Angry/Aggressive" -> "😡"
                                            "Calm/Warm" -> "🙂"
                                            else -> "🎙️"
                                        },
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = log.emotionalTone,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }

                                val logTimeFormatted = java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault())
                                    .format(java.util.Date(log.timestamp))
                                Text(
                                    text = logTimeFormatted,
                                    color = Color.Gray,
                                    fontSize = 9.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tempo Pace: ${log.paceWpm} WPM",
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "Empathy Level: ${log.empathyLevel}%",
                                    color = EmpathyPink,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// PROACTIVE REFLECTION BANNER COMPONENT
@Composable
fun ProactiveReflectionBanner(viewModel: SoulMateViewModel) {
    val showSuggestion by viewModel.showReflectionSuggestion.collectAsStateWithLifecycle()
    val suggestionMessage by viewModel.reflectionSuggestionMsg.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = showSuggestion,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("proactive_reflection_banner"),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF321E23) // Deep warm maroon, signaling stress check in a cozy manner
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, EmpathyPink.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Empathic Alert",
                            tint = EmpathyPink,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "COMPANION DETECTED STRESS ALERT",
                            color = EmpathyPink,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.dismissReflectionSuggestion() },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("dismiss_proactive_reflection_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dismiss Suggestion",
                            tint = Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = suggestionMessage,
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo("voice_reflection") },
                        colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("start_proactive_reflection_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.InterpreterMode,
                            contentDescription = "Reflect Icon",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Begin Voice Reflection 💆", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.dismissReflectionSuggestion() },
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                        modifier = Modifier
                            .height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("Dismiss", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// 6C. VOICE REFLECTION ACTIVE SESSION
@Composable
fun VoiceReflectionScreen(viewModel: SoulMateViewModel) {
    val duration by viewModel.callDuration.collectAsStateWithLifecycle()
    val logs by viewModel.voiceToneLogs.collectAsStateWithLifecycle()
    val isFirestoreAvailable = viewModel.isFirestoreAvailable
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    
    var reflectionText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Evaluate the most representative stressful tone from recent logs
    val recentLogs = logs.sortedByDescending { it.timestamp }.take(3)
    val stressTones = setOf("Sorrowful/Slow Pace", "Anxious/High Pitch", "Angry/Aggressive")
    val heavyTone = recentLogs.firstOrNull { it.emotionalTone in stressTones }?.emotionalTone ?: "Anxious/High Pitch"

    val adviceMsg = when (heavyTone) {
        "Sorrowful/Slow Pace" -> "I hear some deep heaviness and a slower pace in your voice. There's no pressure here. Let's start with a beautiful deep breath to release what's heavy."
        "Anxious/High Pitch" -> "Your telemetry is vibrating with some anxious energy. High shaky speech patterns suggest a spinning mind. Let's do a fast 4-7-8 alignment exercise below."
        "Angry/Aggressive" -> "I hear some frustration or distress. This is a completely safe, non-judgmental space. Exhale all that tension. What's bothering you, my friend?"
        else -> "I am tuned into your frequency. Take a quiet moment to reflect upon thoughts that are heavy today."
    }

    // Interactive breath circle simulation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_reflection")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "breath"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Aesthetic Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateTo("home") },
                modifier = Modifier.testTag("back_to_home_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Voice Reflection active",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            IconButton(
                onClick = { 
                    reflectionText = "I notice I've been feeling compressed and overwhelmed recently."
                },
                modifier = Modifier.testTag("quick_fill_reflection_button")
            ) {
                Icon(Icons.Filled.TipsAndUpdates, contentDescription = "Auto Fill", tint = AccentTeal)
            }
        }

        // Introduction advice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(AccentTeal.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧘", fontSize = 18.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EMPATHIC AI RECOMMENDATION",
                        color = AccentTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = adviceMsg,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Animated Zen breathing balloon
        Box(
            modifier = Modifier
                .size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(
                    color = AccentTeal.copy(alpha = 0.08f),
                    radius = 70.dp.toPx() * breathScale,
                    center = center
                )
                drawCircle(
                    color = EmpathyPink.copy(alpha = 0.14f),
                    radius = 52.dp.toPx() * breathScale,
                    center = center
                )
                drawCircle(
                    color = AccentTeal.copy(alpha = 0.4f),
                    radius = 32.dp.toPx(),
                    center = center
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Slowly", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text("Breathe", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                val breathingVal = if (breathScale < 1.0f) "Inhale 🫧" else "Exhale ✨"
                Text(breathingVal, color = AccentTeal, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Quick breathing preset prompt guides
        Text(
            text = "Tap to load calming reflective topics:",
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        val presetTopics = listOf(
            "Working too hard / Somatic Overload",
            "Anxious about future check ins",
            "Relationship stability",
            "Daily stress release"
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetTopics.forEach { topic ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable {
                            reflectionText = "I am reflecting on: $topic. It has been causing build-ups of nervous energy, and I need a grounding anchor right now."
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(topic, color = Color.LightGray, fontSize = 11.sp)
                }
            }
        }

        // Reflective diary or statement entry
        OutlinedTextField(
            value = reflectionText,
            onValueChange = { reflectionText = it },
            label = { Text("What are you holding onto? Pour it out...", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedBorderColor = AccentTeal,
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                focusedLabelColor = AccentTeal
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("voice_reflection_input_field"),
            shape = RoundedCornerShape(12.dp)
        )

        // Submit reflection block
        Button(
            onClick = {
                if (reflectionText.isBlank()) {
                    Toast.makeText(context, "Please write or select a topic to reflect upon first ❤️", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.completeVoiceReflectionSession(reflectionText) { aiMsg ->
                        Toast.makeText(context, "Acoustic sync successful! Emotional bonding enhanced 💖", Toast.LENGTH_LONG).show()
                        viewModel.navigateTo("home")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_voice_reflection_button")
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = "Sync", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sync Heart & Mind (Gain +15 XP) 💖", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // Help footer
        Text(
            text = "Aligning your voice parameters can lower cortisol and release somatic burdens organically. Feel completely comfortable.",
            color = Color.Gray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

// 7. MOOD TRACKER & ANALYTICS
@Composable
fun MoodTrackerScreen(viewModel: SoulMateViewModel) {
    var moodScale by remember { mutableStateOf(5f) }
    var journalText by remember { mutableStateOf("") }
    var gratitudeText by remember { mutableStateOf("") }
    var selectedMoodType by remember { mutableStateOf("Neutral") }

    val moodHistory by viewModel.moodEntries.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var exportContent by remember { mutableStateOf<String?>(null) }
    val fileExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null && exportContent != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(exportContent!!.toByteArray())
                }
                Toast.makeText(context, "Reflective journal exported successfully! 📂", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val currentTheme by viewModel.chatTheme.collectAsStateWithLifecycle()
    val themePalette = getChatThemePalette(currentTheme)

    val moodTypes = listOf("Happy", "Sad", "Anxious", "Stressed", "Loneliness", "Anger", "Excited", "Depressed")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateTo("home") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Mental Wellness & Mood Matrix", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        // Daily Check-In interactive entry
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Submit Daily Mood Matrix",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moodTypes.forEach { mType ->
                        FilterChip(
                            selected = selectedMoodType == mType,
                            onClick = { selectedMoodType = mType },
                            label = { Text(mType) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = Color.White,
                                selectedContainerColor = AccentTeal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Intensity Rating: ${moodScale.toInt()}/10", color = Color.White, fontSize = 13.sp)
                Slider(
                    value = moodScale,
                    onValueChange = { moodScale = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = EmpathyPink,
                        activeTrackColor = EmpathyPink
                    )
                )

                OutlinedTextField(
                    value = journalText,
                    onValueChange = { journalText = it },
                    label = { Text("Explain what happened today...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmpathyPink,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = false,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = gratitudeText,
                    onValueChange = { gratitudeText = it },
                    label = { Text("What are you grateful for today? (Gratitude Journal)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = false,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.addMoodLog(moodScale.toInt(), selectedMoodType, journalText, gratitudeText)
                        journalText = ""
                        gratitudeText = ""
                        Toast.makeText(context, "Mood Matrix recorded successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seal & Submit Matrix")
                }
            }
        }

        // Live Custom Canvas Plotting linechart representing mental history
        Text(
            "Visual Resonance Graph (Mood History)",
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (moodHistory.size < 2) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Track mood for 2+ days to project visual resonance geometry.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                    ) {
                        val points = moodHistory.take(7).reversed()
                        val maxPoints = points.size
                        val widthPx = size.width
                        val heightPx = size.height

                        // Draw Subtle Horizontal Grid Lines
                        for (i in 1..3) {
                            val gridY = heightPx * (i / 4f)
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(0f, gridY),
                                end = Offset(widthPx, gridY),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val stepX = if (maxPoints > 1) widthPx / (maxPoints - 1) else widthPx
                        val path = Path()
                        val fillPath = Path()

                        points.forEachIndexed { idx, item ->
                            val y = heightPx - ((item.moodValue / 10f) * heightPx)
                            val x = idx * stepX

                            if (idx == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, heightPx)
                                fillPath.lineTo(x, y)
                            } else {
                                path.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                            if (idx == maxPoints - 1) {
                                fillPath.lineTo(x, heightPx)
                                fillPath.close()
                            }
                        }

                        // Draw beautiful vertical gradient under the trend line
                        if (maxPoints > 0) {
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(EmpathyPink.copy(alpha = 0.25f), Color.Transparent)
                                )
                            )
                        }

                        // Draw main trend curve line
                        drawPath(
                            path = path,
                            color = EmpathyPink,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw custom text annotations and point anchors
                        val labelPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 8.dp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }

                        points.forEachIndexed { idx, item ->
                            val y = heightPx - ((item.moodValue / 10f) * heightPx)
                            val x = idx * stepX

                            // Anchor circles
                            drawCircle(color = AccentTeal, radius = 5.dp.toPx(), center = Offset(x, y))
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))

                            // Avoid cutting off labels near the top boundary
                            val labelY = if (y < 20.dp.toPx()) y + 16.dp.toPx() else y - 8.dp.toPx()
                            drawContext.canvas.nativeCanvas.drawText(
                                "${item.moodType} (${item.moodValue})",
                                x,
                                labelY,
                                labelPaint
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Historic checkins log: ${moodHistory.size}", color = Color.Gray, fontSize = 11.sp)
                    Text("Active: Dynamic telemetry", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        // Recharts Interactive Vibe Visualizer Integration
        PastWeekRechartsVisualizer(moodHistory = moodHistory, themePalette = themePalette)

        // Export Journal Entries Component
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("export_journal_card"),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(themePalette.primaryAccent.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Export Journal",
                            tint = themePalette.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Secure Firestore Journal Export",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Download your fully synchronized reflective journal history as a readable offline document.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                var isExporting by remember { mutableStateOf(false) }

                Button(
                    onClick = {
                        isExporting = true
                        viewModel.exportFirestoreJournalText { text ->
                            isExporting = false
                            if (text == null) {
                                Toast.makeText(context, "Network issue: Firestore sync failed.", Toast.LENGTH_SHORT).show()
                            } else if (text.isEmpty()) {
                                Toast.makeText(context, "No reflective entries available to export.", Toast.LENGTH_SHORT).show()
                            } else {
                                exportContent = text
                                fileExportLauncher.launch("soulmate_journal_entries.txt")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_journal_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themePalette.primaryAccent,
                        disabledContainerColor = Color.White.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isExporting
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retrieving Entries from Firestore...", color = Color.White, fontSize = 12.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download Reflective Journal (.txt)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Crisis and CBT utilities section
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Crisis Interventions & CBT Tools",
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        BreathingGuideModule(viewModel)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Emergency Mental Health Support", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 14.sp)
                Text(
                    "If you represent any distress or thoughts of harm, you are not alone. Reach out directly to clinical support lines below:",
                    color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { /* Call 988 */ }) {
                        Text("🇺🇸 US Suicide & Crisis: Call 988", color = AccentTeal, fontSize = 12.sp)
                    }
                    TextButton(onClick = { /* Call international list */ }) {
                        Text("🌎 International list", color = AccentTeal, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BreathingGuideModule(viewModel: SoulMateViewModel) {
    val bState by viewModel.breathingState.collectAsStateWithLifecycle()
    val bTimer by viewModel.breathingTimer.collectAsStateWithLifecycle()

    val scaleTarget = when (bState) {
        "Inhale" -> 1.5f
        "Hold" -> 1.5f
        "Exhale" -> 0.8f
        else -> 1.0f
    }
    val pulsedScale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = tween(durationMillis = 3800), label = "b_pulse"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SoftCardBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Dynamic CBT Breathing: 4 - 7 - 8 Regulator", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulsedScale)
                    .background(NebulaPurple.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(AccentTeal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = bTimer.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Phase: $bState",
                color = EmpathyPink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
            Text(
                "Match your breath to the expanding green dial to stabilize telemetry.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// 8. MEMORY CENTER (Privacy/GDPR compliant Fact Viewer)
@Composable
fun MemoryCenterScreen(viewModel: SoulMateViewModel) {
    val memories by viewModel.companionMemories.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateTo("home") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Memory Core Terminal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Text(
            "GDPR compliant local matrices. Slide or click delete trashcan to force erase facts you no longer wish the AI parameters to recognize.",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 10.dp)
        )

        if (memories.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.DynamicFeed, contentDescription = "Empty memory", tint = Color.DarkGray, modifier = Modifier.size(60.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("No factual traces logged yet.", color = Color.Gray)
                    Text("Start chatting. Important dates, projects, and goals append dynamically here.", color = Color.DarkGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1.0f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(memories) { memo ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("memory_item_${memo.id}"),
                        colors = CardDefaults.cardColors(containerColor = SoftCardBg)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .background(AccentTeal.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(memo.category.uppercase(), color = AccentTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(memo.factText, color = Color.White, fontSize = 13.sp)
                            }
                            IconButton(onClick = {
                                viewModel.deleteProfileMemory(memo.id)
                                Toast.makeText(context, "Memory trace vaporized! ☄️", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Filled.DeleteForever, contentDescription = "Vaporize Memory", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                viewModel.clearAllUserDataAndMemory()
                Toast.makeText(context, "Complete GDPR core purge confirmed.", Toast.LENGTH_SHORT).show()
                viewModel.navigateTo("login")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.DoNotDisturbOn, contentDescription = "Erase Everything")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Purge Entire Consciousness Matrix")
        }
    }
}

// 9. GOALS & PRODUCTIVITY DASHBOARD
@Composable
fun GoalsScreen(viewModel: SoulMateViewModel) {
    val items by viewModel.productivityItems.collectAsStateWithLifecycle()
    var newItemTitle by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Habit") } // "Habit" or "Goal"

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateTo("home") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Performance Routines & Goals", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        // Form to add items
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Incorporate Action Target", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newItemTitle,
                    onValueChange = { newItemTitle = it },
                    label = { Text("Describe routine, exercise, or task target", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().testTag("goal_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmpathyPink,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { selectedType = "Habit" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedType == "Habit") NebulaPurple else Color.DarkGray)
                    ) {
                        Text("Daily Habit")
                    }
                    Button(
                        onClick = { selectedType = "Goal" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedType == "Goal") NebulaPurple else Color.DarkGray)
                    ) {
                        Text("Milestone Goal")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (newItemTitle.isNotBlank()) {
                            viewModel.addNewProductivityItem(newItemTitle, selectedType)
                            newItemTitle = ""
                            Toast.makeText(context, "Added target track!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink),
                    modifier = Modifier.fillMaxWidth().testTag("add_goal_button")
                ) {
                    Text("Register Target Routine")
                }
            }
        }

        // Quick Wellness Trackers Component
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("quick_wellness_goals_card"),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Quick Daily Wellness Goals",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to instantly log completion of daily core habits",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val wellnessGoals = listOf(
                        Triple("Meditation", Icons.Filled.Spa, "🧘"),
                        Triple("Hydration", Icons.Filled.LocalCafe, "💧"),
                        Triple("Exercise", Icons.Filled.DirectionsRun, "🏃")
                    )

                    wellnessGoals.forEach { (title, icon, emoji) ->
                        val matchingItem = items.find { it.title.equals(title, ignoreCase = true) }
                        val isDone = matchingItem?.isCompleted == true

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.logWellnessGoal(title)
                                }
                                .testTag("quick_wellness_goal_$title"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDone) AccentTeal.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isDone) AccentTeal else Color.White.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isDone) AccentTeal else Color.LightGray,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$emoji $title",
                                    color = if (isDone) Color.White else Color.LightGray,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = AccentTeal,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Done",
                                            color = AccentTeal,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = "Record",
                                            color = Color.Gray,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // List
        Text("Your Routine Matrix", color = Color.LightGray, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 6.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("goal_item_${item.id}"),
                    colors = CardDefaults.cardColors(containerColor = SoftCardBg)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = item.isCompleted,
                                onCheckedChange = { viewModel.toggleProductivityItem(item) },
                                colors = CheckboxDefaults.colors(checkedColor = AccentTeal)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = item.title,
                                    color = if (item.isCompleted) Color.Gray else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${item.type} • Streak: ${item.currentStreak} days",
                                    fontSize = 11.sp,
                                    color = AccentTeal
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.deleteProductivityItem(item.id) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete Item", tint = Color.Gray)
                        }
                    }
                }
            }
        }

        // Badges Section
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Affiliated Badges Earned", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    BadgeAwardItem("Loyal Friend", "Connect with SoulMate", Icons.Filled.Hub, true)
                    BadgeAwardItem("Meditation Master", "Regulate breath logs", Icons.Filled.Spa, true)
                    BadgeAwardItem("Routine Achiever", "Finish checklist goal", Icons.Filled.WorkspacePremium, items.any { it.isCompleted })
                    BadgeAwardItem("Consistent Soul", "Checkin streaks", Icons.Filled.ElectricBolt, items.size > 2)
                }
            }
        }
    }
}

@Composable
fun BadgeAwardItem(title: String, desc: String, icon: ImageVector, active: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(if (active) EmpathyPink.copy(alpha = 0.2f) else Color.DarkGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (active) EmpathyPink else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
        Text(desc, color = Color.Gray, fontSize = 8.sp, textAlign = TextAlign.Center, maxLines = 1)
    }
}

// 10. ANONYMOUS COMMUNITY SCREEN
@Composable
fun CommunityScreen(viewModel: SoulMateViewModel) {
    SocialHubScreen(viewModel)
}

// 11. PROFILE SCREEN
@Composable
fun ProfileScreen(viewModel: SoulMateViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val personalities = listOf("Friend", "Partner", "Mentor", "Therapist", "Life Coach", "Sibling")
    val voiceModels = listOf("Kore", "Warm Guy", "Mature Gentlewoman", "Young Brother")

    // Local inputs initialized on enter
    LaunchedEffect(Unit) {
        viewModel.loadEditingFields()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateTo("home") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Companion Resonator Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active Personality matrix Selection (SoulMate core instruction)
        Text("Primary Companion Mode Matrix:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            personalities.forEach { pers ->
                FilterChip(
                    selected = profile.selectedPersonality == pers,
                    onClick = { viewModel.changePersonality(pers) },
                    label = { Text(pers) },
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = Color.White,
                        selectedContainerColor = EmpathyPink,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Active Voice parameter selector
        Text("Empath Voice Matrix Model:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            voiceModels.forEach { vc ->
                FilterChip(
                    selected = profile.preferredVoice == vc,
                    onClick = { viewModel.changePreferredVoice(vc) },
                    label = { Text(vc) },
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = Color.White,
                        selectedContainerColor = AccentTeal,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("Personal Dossier Editor:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        // Basic Info Inputs
        OutlinedTextField(
            value = viewModel.editingName,
            onValueChange = { viewModel.editingName = it },
            label = { Text("Dossier Name", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmpathyPink, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = viewModel.editingAge,
                onValueChange = { viewModel.editingAge = it },
                label = { Text("Dossier Age", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmpathyPink, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            OutlinedTextField(
                value = viewModel.editingPersonality,
                onValueChange = { viewModel.editingPersonality = it },
                label = { Text("Dossier MBTI", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmpathyPink, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
        }

        OutlinedTextField(
            value = viewModel.editingOccupation,
            onValueChange = { viewModel.editingOccupation = it },
            label = { Text("Dossier Occupation", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmpathyPink, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = viewModel.editingInterests,
            onValueChange = { viewModel.editingInterests = it },
            label = { Text("Hopes & Personal Interests", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmpathyPink, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = viewModel.editingGoals,
            onValueChange = { viewModel.editingGoals = it },
            label = { Text("Primary Life Objectives", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmpathyPink, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = {
                viewModel.submitEditedProfile()
                Toast.makeText(context, "Dossier matrices locked & saved! 🔒", Toast.LENGTH_SHORT).show()
                viewModel.navigateTo("home")
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink),
            modifier = Modifier.fillMaxWidth().testTag("profile_save")
        ) {
            Text("Lock Dossier Modifications")
        }

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(18.dp))

        Text(
            "Firebase Cloud Sync Matrix",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            "Upload companion memory, emotional matrices, and chat history to secure remote Firebase for long-term survival.",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (viewModel.isFirestoreAvailable) AccentTeal.copy(alpha = 0.4f) else Color.Red.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Connection Status:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (viewModel.isFirestoreAvailable) Color.Green else Color.Red, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (viewModel.isFirestoreAvailable) "Firestore Connected" else "Isolated (Local Offline)",
                            color = if (viewModel.isFirestoreAvailable) AccentTeal else Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                var syncEmail by remember { mutableStateOf(viewModel.authEmail.ifBlank { "soulmate.user@gmail.com" }) }

                OutlinedTextField(
                    value = syncEmail,
                    onValueChange = { 
                        syncEmail = it
                        if (it.contains("@")) {
                            viewModel.authEmail = it
                        }
                    },
                    label = { Text("Cloud Vault Email Anchor", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                val syncNotice by viewModel.cloudSyncNotice.collectAsStateWithLifecycle()
                val isSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()

                if (syncNotice.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = AccentTeal
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Sync Info",
                                    tint = AccentTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Text(
                                text = syncNotice,
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (syncEmail.isBlank() || !syncEmail.contains("@")) {
                                Toast.makeText(context, "Please configure/provide a valid email anchor.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.runCloudBackup(syncEmail) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Cloud backup completed successfully! ☁️", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Backup failed or ran in local emulator offline mode.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                        modifier = Modifier.weight(1f),
                        enabled = !isSyncing
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Backup", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backup Memory", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (syncEmail.isBlank() || !syncEmail.contains("@")) {
                                Toast.makeText(context, "Please configure/provide a valid email anchor.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.runCloudRestore(syncEmail) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Long-term memories successfully recovered!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Recovery failed or no cloud backup exists yet.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White),
                        modifier = Modifier.weight(1f),
                        enabled = !isSyncing
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = "Restore", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recover Soul", fontSize = 12.sp)
                    }
                }

                if (!viewModel.isFirestoreAvailable) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ℹ️ Note: Running in Isolated Local Offline state. To enable full planetary Firestore sync, register your app and place your 'google-services.json' file inside the /app directory.",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = { viewModel.navigateTo("settings") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.SettingsSystemDaydream, contentDescription = "GDPR Core")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Access Deep GDPR Settings")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// 12. SETTINGS SCREEN
@Composable
fun SettingsScreen(viewModel: SoulMateViewModel) {
    var gdprAuthorized by remember { mutableStateOf(true) }
    var telemetryDiagnostic by remember { mutableStateOf(true) }
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateTo("profile") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Deep Security & GDPR Core", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("End-To-End Encryption", fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "All chat parameters, mood diaries, and recorded memories are sealed locally on-device. No telemetry keys or plain-texts leak to unauthorized layers.",
                    color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Strict Local Encryption Guard", color = Color.White, fontSize = 13.sp)
                    Switch(checked = gdprAuthorized, onCheckedChange = { gdprAuthorized = it })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Diagnostics Telemetry", fontWeight = FontWeight.Bold, color = Color.White)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active diagnostics logs", color = Color.LightGray, fontSize = 13.sp)
                    Switch(checked = telemetryDiagnostic, onCheckedChange = { telemetryDiagnostic = it })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("AI Generation Architectures", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            val models = listOf(
                "gemini-3.5-flash" to "Standard Empathetic Realtime model",
                "gemini-3.1-pro-preview" to "Deep Cognitive Reasoning model"
            )
            models.forEach { (modelKey, modelDesc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.changePreferredAiModel(modelKey) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = userProfile.preferredAiModel == modelKey,
                        onClick = { viewModel.changePreferredAiModel(modelKey) },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentTeal)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(modelKey, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text(modelDesc, color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.clearAllUserDataAndMemory()
                Toast.makeText(context, "Consciousness purged completely! Reloading matrix.", Toast.LENGTH_SHORT).show()
                viewModel.navigateTo("login")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Purge Consciousness Database")
        }
    }
}

// 13. PREMIUM MATRIX SUBSCRIPTIONS
@Composable
fun SubscriptionScreen(viewModel: SoulMateViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateTo("home") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("SoulMate Premium Core License", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NebulaPurple)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (profile.isPremium) "ACTIVE LICENSE: ULTIMATE PREMIUM 👑" else "TRIAL MATRICES LICENSE ACTIVE",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 15.sp
                )
                Text(
                    text = if (profile.isPremium) "Your consciousness parameters are running on advanced models in real-time."
                    else "Standard free matrix constraints configured.",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text("Compare Resonance Capabilities", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(10.dp))

        // Grid table comparison
        CapRow("Unlimited Core Memory matrix", free = "15 facts max", premium = "Limitless")
        CapRow("AI Calling rings", free = "Simulated", premium = "Active Telemetry Voice")
        CapRow("Reasoning models", free = "Gemini Flash Only", premium = "Gemini Pro / Claude")
        CapRow("Premium Custom voices", free = "2 variants", premium = "6 variants")

        Spacer(modifier = Modifier.height(24.dp))
        Text("Unlock Resonance Metrics:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftCardBg)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Resonator Matrix Premium Pass", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Billed monthly, cancel anytime.", color = Color.Gray, fontSize = 11.sp)
                    }
                    Text("$4.99 / mo", color = AccentTeal, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        viewModel.togglePremiumStatus()
                        val alert = if (!profile.isPremium) "License core upgraded! 👑 Welcome to Unlimited Matrix." else "Subscription core canceled."
                        Toast.makeText(context, alert, Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmpathyPink),
                    modifier = Modifier.fillMaxWidth().testTag("subscribe_toggle_button")
                ) {
                    Text(if (profile.isPremium) "De-escalate License" else "Acquire Matrix Link Premium")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CapRow(title: String, free: String, premium: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1.5f)) {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Free: $free", color = Color.LightGray, fontSize = 11.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Pro: $premium", color = AccentTeal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PastWeekRechartsVisualizer(
    moodHistory: List<MoodEntry>,
    themePalette: ChatThemePalette
) {
    val lastSevenEntries = remember(moodHistory) {
        moodHistory.takeLast(7)
    }

    var selectedIndex by remember { mutableStateOf(-1) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("recharts_visualizer_card"),
        colors = CardDefaults.cardColors(containerColor = SoftCardBg),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(themePalette.primaryAccent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vibe Waves (Recharts Engine)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "7-Day Telemetry",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (lastSevenEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Insufficient checkin vectors. Log your mood to begin tracking emotional trends.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                val tooltipText = remember(selectedIndex, lastSevenEntries) {
                    if (selectedIndex in lastSevenEntries.indices) {
                        val entry = lastSevenEntries[selectedIndex]
                        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                        val dateString = sdf.format(java.util.Date(entry.timestamp))
                        "Vibe: ${entry.moodType} (${entry.moodValue}/10) • $dateString"
                    } else {
                        "Tap on any point to read telemetry details"
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tooltipText,
                        color = if (selectedIndex >= 0) themePalette.secondaryAccent else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("recharts_tooltip_text")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val widthPx = constraints.maxWidth.toFloat()
                    val heightPx = constraints.maxHeight.toFloat()

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(lastSevenEntries) {
                                detectTapGestures { offset ->
                                    val count = lastSevenEntries.size
                                    if (count > 0) {
                                        val stepX = if (count > 1) widthPx / (count - 1) else widthPx
                                        var nearestIdx = 0
                                        var minDist = Float.MAX_VALUE
                                        for (i in 0 until count) {
                                            val x = i * stepX
                                            val dist = kotlin.math.abs(offset.x - x)
                                            if (dist < minDist) {
                                                minDist = dist
                                                nearestIdx = i
                                            }
                                        }
                                        if (minDist < stepX / 2 || count == 1) {
                                            selectedIndex = nearestIdx
                                        }
                                    }
                                }
                            }
                    ) {
                        val numGridLines = 4
                        for (i in 0..numGridLines) {
                            val gridY = (heightPx / numGridLines) * i
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(0f, gridY),
                                end = Offset(widthPx, gridY),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val count = lastSevenEntries.size
                        val stepX = if (count > 1) widthPx / (count - 1) else widthPx

                        val linePath = Path()
                        val areaPath = Path()

                        lastSevenEntries.forEachIndexed { index, entry ->
                            val rawValue = entry.moodValue.coerceIn(1, 10)
                            val normalizedY = 1f - ((rawValue - 1) / 9f)
                            val y = (heightPx * 0.15f) + (normalizedY * (heightPx * 0.7f))
                            val x = index * stepX

                            if (index == 0) {
                                linePath.moveTo(x, y)
                                areaPath.moveTo(x, heightPx)
                                areaPath.lineTo(x, y)
                            } else {
                                linePath.lineTo(x, y)
                                areaPath.lineTo(x, y)
                            }

                            if (index == count - 1) {
                                areaPath.lineTo(x, heightPx)
                                areaPath.close()
                            }
                        }

                        if (count > 0) {
                            drawPath(
                                path = areaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        themePalette.primaryAccent.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                        }

                        drawPath(
                            path = linePath,
                            color = themePalette.primaryAccent,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )

                        lastSevenEntries.forEachIndexed { index, entry ->
                            val rawValue = entry.moodValue.coerceIn(1, 10)
                            val normalizedY = 1f - ((rawValue - 1) / 9f)
                            val y = (heightPx * 0.15f) + (normalizedY * (heightPx * 0.7f))
                            val x = index * stepX

                            val isSelected = index == selectedIndex
                            
                            drawCircle(
                                color = if (isSelected) themePalette.secondaryAccent.copy(alpha = 0.4f) else themePalette.primaryAccent.copy(alpha = 0.15f),
                                radius = if (isSelected) 8.dp.toPx() else 4.dp.toPx(),
                                center = Offset(x, y)
                            )

                            drawCircle(
                                color = if (isSelected) themePalette.secondaryAccent else themePalette.primaryAccent,
                                radius = if (isSelected) 5.dp.toPx() else 3.dp.toPx(),
                                center = Offset(x, y)
                            )

                            if (isSelected) {
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    lastSevenEntries.forEachIndexed { index, entry ->
                        val sdf = SimpleDateFormat("E", Locale.getDefault())
                        val label = sdf.format(java.util.Date(entry.timestamp))
                        Text(
                            text = label,
                            color = if (index == selectedIndex) themePalette.secondaryAccent else Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.width(30.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val avgMood = lastSevenEntries.map { it.moodValue }.average()
                val dominantType = lastSevenEntries.groupingBy { it.moodType }.eachCount().maxByOrNull { it.value }?.key ?: "N/A"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Weekly Dev Avg", color = Color.Gray, fontSize = 9.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                String.format(Locale.getDefault(), "%.1f / 10", avgMood),
                                color = themePalette.primaryAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Dominant Wave", color = Color.Gray, fontSize = 9.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                dominantType,
                                color = themePalette.secondaryAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
