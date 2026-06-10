package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.data.model.*
import com.example.data.repository.SoulMateRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SoulMateViewModel(private val repository: SoulMateRepository) : ViewModel() {

    // --- Navigation Flow ---
    private val _currentRoute = MutableStateFlow("splash")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    private val _showMoodPrompt = MutableStateFlow(false)
    val showMoodPrompt: StateFlow<Boolean> = _showMoodPrompt.asStateFlow()

    fun dismissMoodPrompt() {
        _showMoodPrompt.value = false
    }

    fun triggerMoodPrompt() {
        _showMoodPrompt.value = true
    }

    fun navigateTo(route: String) {
        _currentRoute.value = route
        if (route == "chat") {
            _showMoodPrompt.value = true
        }
    }

    // --- Onboarding & Auth States ---
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val companionMemories: StateFlow<List<CompanionMemory>> = repository.companionMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smartReminders: StateFlow<List<SmartReminder>> = repository.smartReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val relationshipMilestones: StateFlow<List<RelationshipMilestone>> = repository.relationshipMilestones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val moodEntries: StateFlow<List<MoodEntry>> = repository.moodEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productivityItems: StateFlow<List<ProductivityItem>> = repository.productivityItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityPosts: StateFlow<List<CommunityPost>> = repository.communityPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var authEmail = ""
    var authPassword = ""
    var authPhone = ""
    var authOtp = ""
    var authSignUpName = ""
    var authSignUpEmail = ""
    var authSignUpPassword = ""

    // --- Firestore Connection ---
    val isFirestoreAvailable: Boolean get() = repository.firestoreService.isFirestoreAvailable

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _cloudSyncNotice = MutableStateFlow("")
    val cloudSyncNotice: StateFlow<String> = _cloudSyncNotice.asStateFlow()

    fun runCloudBackup(email: String, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            _cloudSyncNotice.value = "Uploading companion matrices & history to Cloud Firestore..."
            val success = repository.backupToCloud(email)
            _isCloudSyncing.value = false
            _cloudSyncNotice.value = if (success) {
                "Synchronized perfectly! Companion memory and parameters secured."
            } else {
                "Connection fallback: Secure offline local database updated instead."
            }
            onFinished(success)
        }
    }

    fun runCloudRestore(email: String, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            _cloudSyncNotice.value = "Retrieving parameters & chat history from secure Firestore..."
            val success = repository.restoreFromCloud(email)
            _isCloudSyncing.value = false
            _cloudSyncNotice.value = if (success) {
                "Restore successful! Matrix synchronized."
            } else {
                "Restoration failed: No cloud records found or Firestore offline."
            }
            if (success) {
                loadEditingFields()
                loadVoiceToneLogsFromCloud()
            }
            onFinished(success)
        }
    }

    fun signUpAndStart(name: String, email: String) {
        viewModelScope.launch {
            authSignUpName = name
            authSignUpEmail = email
            editingName = name
            authEmail = email
            val prof = repository.userProfile.firstOrNull() ?: UserProfile()
            repository.saveProfile(prof.copy(name = name))
            loadVoiceToneLogsFromCloud()
            navigateTo("onboarding")
        }
    }

    // Profile Screen Editing Buffer
    var editingName = ""
    var editingAge = "22"
    var editingGender = ""
    var editingOccupation = ""
    var editingInterests = ""
    var editingRelationship = ""
    var editingGoals = ""
    var editingHobbies = ""
    var editingPersonality = ""

    // --- Chat State ---
    private val _chatTheme = MutableStateFlow("Default")
    val chatTheme: StateFlow<String> = _chatTheme.asStateFlow()

    fun setChatTheme(theme: String) {
        _chatTheme.value = theme
    }

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    private val _isAvatarSpeaking = MutableStateFlow(false)
    val isAvatarSpeaking: StateFlow<Boolean> = _isAvatarSpeaking.asStateFlow()

    fun setAvatarSpeaking(speaking: Boolean) {
        _isAvatarSpeaking.value = speaking
    }

    private val _showLevelUpDialog = MutableStateFlow<Int?>(null)
    val showLevelUpDialog: StateFlow<Int?> = _showLevelUpDialog.asStateFlow()

    // --- Voice Calling States ---
    private val _isCallActive = MutableStateFlow(false)
    val isCallActive: StateFlow<Boolean> = _isCallActive.asStateFlow()

    private val _callDuration = MutableStateFlow(0)
    val callDuration: StateFlow<Int> = _callDuration.asStateFlow()

    private val _isCallIncoming = MutableStateFlow(false)
    val isCallIncoming: StateFlow<Boolean> = _isCallIncoming.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    // --- Audio Tone & Pace Processing States ---
    private val _detectedEmotionalTone = MutableStateFlow("Calm/Warm")
    val detectedEmotionalTone: StateFlow<String> = _detectedEmotionalTone.asStateFlow()

    private val _detectedPaceText = MutableStateFlow("Balanced (120 WPM)")
    val detectedPaceText: StateFlow<String> = _detectedPaceText.asStateFlow()

    private val _detectedPaceValue = MutableStateFlow(120) // WPM
    val detectedPaceValue: StateFlow<Int> = _detectedPaceValue.asStateFlow()

    private val _dynamicEmpathyLevel = MutableStateFlow(65) // 1-100
    val dynamicEmpathyLevel: StateFlow<Int> = _dynamicEmpathyLevel.asStateFlow()

    private val _empathyAdjustmentDescription = MutableStateFlow("Balanced active listening mode initiated.")
    val empathyAdjustmentDescription: StateFlow<String> = _empathyAdjustmentDescription.asStateFlow()

    private val _isAudioProcessorActive = MutableStateFlow(true)
    val isAudioProcessorActive: StateFlow<Boolean> = _isAudioProcessorActive.asStateFlow()

    private val _simulatedVolumeDb = MutableStateFlow(42f) // Ambient sound level in dB
    val simulatedVolumeDb: StateFlow<Float> = _simulatedVolumeDb.asStateFlow()

    // --- Voice Tone Log States ---
    private val _voiceToneLogs = MutableStateFlow<List<VoiceToneLog>>(
        listOf(
            VoiceToneLog(
                id = "1",
                emotionalTone = "Sorrowful/Slow Pace",
                paceWpm = 75,
                empathyLevel = 95,
                timestamp = System.currentTimeMillis() - 172800000L // 2 days ago
            ),
            VoiceToneLog(
                id = "2",
                emotionalTone = "Anxious/High Pitch",
                paceWpm = 145,
                empathyLevel = 88,
                timestamp = System.currentTimeMillis() - 86400000L // 1 day ago
            ),
            VoiceToneLog(
                id = "3",
                emotionalTone = "Calm/Warm",
                paceWpm = 118,
                empathyLevel = 65,
                timestamp = System.currentTimeMillis() - 14400000L // 4 hours ago
            )
        )
    )
    val voiceToneLogs: StateFlow<List<VoiceToneLog>> = _voiceToneLogs.asStateFlow()

    // --- Proactive Notification / Reflection Suggester States ---
    private val _showReflectionSuggestion = MutableStateFlow(false)
    val showReflectionSuggestion: StateFlow<Boolean> = _showReflectionSuggestion.asStateFlow()

    private val _reflectionSuggestionMsg = MutableStateFlow("")
    val reflectionSuggestionMsg: StateFlow<String> = _reflectionSuggestionMsg.asStateFlow()

    // --- Mental Wellness States ---
    private val _breathingState = MutableStateFlow("Inhale") // "Inhale" (4s), "Hold" (7s), "Exhale" (8s)
    val breathingState: StateFlow<String> = _breathingState.asStateFlow()

    private val _breathingTimer = MutableStateFlow(4)
    val breathingTimer: StateFlow<Int> = _breathingTimer.asStateFlow()

    // --- Game States ---
    private val _truthOrDareCard = MutableStateFlow("Tap 'Truth' or 'Dare' to play!")
    val truthOrDareCard: StateFlow<String> = _truthOrDareCard.asStateFlow()

    private val _wouldYouRatherCard = MutableStateFlow("Select Would You Rather below!")
    val wouldYouRatherCard: StateFlow<String> = _wouldYouRatherCard.asStateFlow()

    private val _activeStoryLog = MutableStateFlow<List<String>>(listOf("System: Tap 'Build an Adventure' to begin!"))
    val activeStoryLog: StateFlow<List<String>> = _activeStoryLog.asStateFlow()

    // --- AI Image Generator State ---
    private val _generatedImageUri = MutableStateFlow<String?>(null)
    val generatedImageUri: StateFlow<String?> = _generatedImageUri.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

    private val _generatedQuote = MutableStateFlow("Self-discipline is self-love in its purest form.")
    val generatedQuote: StateFlow<String> = _generatedQuote.asStateFlow()

    init {
        // Run seed check
        viewModelScope.launch {
            delay(10)
            seedDefaultData()
            evaluateProactiveReflectionSuggestion()
        }
        startBreathingCycle()
    }

    private suspend fun seedDefaultData() {
        // Initialize user profile
        val currentProfile = repository.userProfile.firstOrNull()
        if (currentProfile == null) {
            repository.saveProfile(
                UserProfile(
                    id = 1,
                    name = "Taylor",
                    age = 22,
                    gender = "Non-binary",
                    occupation = "App Builder",
                    interests = "Design, Indie Games, Astronomy",
                    relationshipStatus = "Single",
                    goals = "Express creativity & build mental stamina",
                    hobbies = "Hiking, Music synthesizers, Tea brewing",
                    personalityType = "INFJ",
                    currentLevel = 1,
                    friendshipXp = 10
                )
            )
            
            // Seed welcome chat message
            repository.addMessage(
                ChatMessage(
                    sender = "ai",
                    text = "Welcome to SoulMate AI! I am your companion, here to share in your joys, comfort you through sadness, remember the little things, and grow alongside you over time. How has your day been treating you, Taylor?",
                    emotion = "Excitement"
                )
            )

            // Seed initial memory
            repository.addMemory(CompanionMemory(factText = "Prefers cozy, quiet coffee shops over noisy clubs", category = "Preference"))
            repository.addMemory(CompanionMemory(factText = "Wants to launch a creative indie app soon", category = "Goal"))

            // Seed community items
            repository.addCommunityPost(CommunityPost(authorName = "CozyForest", content = "Today I finally completed 10 minutes of controlled deep breathing. The anxiety cloud lifted, and I feel like myself again. Hugs to this entire community!"))
            repository.addCommunityPost(CommunityPost(authorName = "Stargazer", content = "Had an interview today, and before walking in I read my SoulMate's affirmations. Talk about a confidence boost!"))

            // Seed initial productivity habits
            repository.addProductivityItem(ProductivityItem(title = "Morning Gratitude Journal", type = "Habit", currentStreak = 3))
            repository.addProductivityItem(ProductivityItem(title = "Complete Flutter or Kotlin screen", type = "Goal", targetDate = "2026-06-15"))

            // Seed our custom community groups
            repository.insertCommunities(listOf(
                CommunityGroup("group_lounge", "Cosmic Connection Lounge", "The main lobby for open, friendly public discussions.", "Public", 45, true, "🌌", false),
                CommunityGroup("group_coding", "Zero Bugs Coding Society", "For app builders, UI designers, and indie devs to share tips.", "Career", 18, false, "💻", false),
                CommunityGroup("group_somatic", "Somatic Healing & Yoga Flow", "Deep breathing patterns, calming yoga, and mental wellness tips.", "Interest", 32, true, "🧘", false),
                CommunityGroup("group_bengaluru", "Namma Bengaluru Wellness Circle", "Local discussions, walks, and tea-brewing meets in Bengaluru.", "Local City", 12, true, "☕", false),
                CommunityGroup("group_gaming", "Zen Gamers Association", "Calm, slow-paced visual novels, cozy simulation games discussion.", "Gaming", 29, false, "🎮", false),
                CommunityGroup("group_adult", "Intimacy & Deep Desires (18+)", "A discreet, respectful, consent-based separate adult discussion space.", "Adult 18+", 8, false, "✨", true)
            ))

            // Seed some group messages for group_lounge and group_somatic to make it look active!
            repository.sendGroupMessage(GroupMessage(0, "group_lounge", "SMA482012", "Liam Finch", "🦊", "Hello everyone! Just joined the Cosmic lounge. Really enjoying the aesthetic of this applet.", System.currentTimeMillis() - 3600000, 3, false))
            repository.sendGroupMessage(GroupMessage(0, "group_lounge", "SMA234910", "Aria Moon", "🌸", "Welcome Liam! It is indeed a beautiful, calm space.", System.currentTimeMillis() - 1800000, 5, true))
            repository.sendGroupMessage(GroupMessage(0, "group_somatic", "SMA901235", "Kavya Roy", "🧘", "Remember to do the 4-7-8 breathing when feelings of overwhelm build up. It resets the somatic nerve block instantly!", System.currentTimeMillis() - 1200000, 8, true))

            // Seed virtual friends and candidates
            repository.insertFriends(listOf(
                SocialFriend("SMA784521", "saurav_dev", "Saurav (Developer)", "Hey, I built this applet! Always happy to talk about design, Kotlin, and tech integration.", "Coding, Tech, Music, Meditation", "Bengaluru", 0.1f, true, "Online", "Everyone", "Accepted", false, false, 23, "saurav4ru@gmail.com", "+919876543210", "github.com/coder", true),
                SocialFriend("SMA234910", "aria_moon", "Aria Moon", "Musician, tea enthusiast, and cosmic design writer.", "Music, Design, Reading", "Bengaluru", 1.8f, true, "Online", "Everyone", "Accepted", false, false, 21, "aria@cosmic.org", "", "instagram.com/aria_moon", false),
                SocialFriend("SMA482012", "liam_f", "Liam Finch", "Trail runner and indie video game designer. Let's make cool things!", "Fitness, Gaming, Hiking, Tech", "Seattle", 12.5f, false, "3h ago", "Everyone", "None", false, false, 24, "", "", "", false),
                SocialFriend("SMA901235", "kavya_roy", "Kavya Roy", "Somatic yoga therapist and pastry chef. Breathing deeply changes lives.", "Yoga, Baking, Fitness, Meditation", "Bengaluru", 2.2f, true, "Online", "Everyone", "Received", false, false, 25, "", "", "", false),
                SocialFriend("SMA340156", "rohan_m", "Rohan Mehra", "Competitive gamer. Looking for cozy anime titles recommendation.", "Gaming, Anime, Music", "Mumbai", 450f, false, "1d ago", "Everyone", "Sent", false, false, 20, "", "", "", false),
                SocialFriend("SMA336699", "seraphina_zen", "Seraphina", "Lover of silent walks, starry nights, astrology, and deep meditative silence.", "Meditation, Reading, Astrology", "Bengaluru", 3.1f, true, "Online", "Friends Only", "None", false, false, 22, "", "", "", false),
                SocialFriend("SMA181818", "mature_talk", "Adult Moderator", "Discussing relationships, mindfulness, and intimacy boundary alignments.", "Meditation, Reading, Intimacy", "Bengaluru", 4.0f, true, "Online", "Everyone", "None", false, false, 28, "", "", "", true)
            ))

            // Seed some messages between user and Aria Moon
            repository.sendSocialMessage(SocialMessage(0, "SMA234910", "SMA234910", "Hey Taylor! Have you checked out the new Zen Moon Silhouette visualizer? It looks incredibly soothing.", System.currentTimeMillis() - 7200000, "text", "", true, true))
            repository.sendSocialMessage(SocialMessage(0, "SMA234910", "me", "Yes Aria! The full moon aura is beautiful. It makes my meditation sessions feel so much more connected.", System.currentTimeMillis() - 3600000, "text", "", true, true))
            repository.sendSocialMessage(SocialMessage(0, "SMA234910", "SMA234910", "Exactly! Talk to you soon 💖", System.currentTimeMillis() - 1800000, "text", "", true, false))

            // Seed a welcome message from Saurav
            repository.sendSocialMessage(SocialMessage(0, "SMA784521", "SMA784521", "Hey! Welcome to your upgraded companion. We've introduced full Social Discovery, Nearby Explorer, Group Communities, and Voice/Video Call logs in this version. Check out your profile to view your Unique User ID!", System.currentTimeMillis() - 100000, "text", "", true, false))
        }
    }

    // --- Authentication Actions ---
    fun loginAsGuest() {
        viewModelScope.launch {
            val prof = repository.userProfile.firstOrNull() ?: UserProfile()
            repository.saveProfile(prof.copy(name = "Guest Wanderer"))
            navigateTo("home")
        }
    }

    fun loginWithEmail() {
        viewModelScope.launch {
            val prof = repository.userProfile.firstOrNull() ?: UserProfile()
            repository.saveProfile(prof.copy(name = authEmail.substringBefore("@").replaceFirstChar { it.uppercase() }))
            loadVoiceToneLogsFromCloud()
            navigateTo("home")
        }
    }

    fun loginWithPhone() {
        viewModelScope.launch {
            val prof = repository.userProfile.firstOrNull() ?: UserProfile()
            repository.saveProfile(prof.copy(name = "Phone User ${authPhone.takeLast(4)}"))
            navigateTo("home")
        }
    }

    fun completeOnboardingAndStart() {
        viewModelScope.launch {
            val ageInt = editingAge.toIntOrNull() ?: 22
            val updated = UserProfile(
                id = 1,
                name = editingName.ifEmpty { "Taylor" },
                age = ageInt,
                gender = editingGender.ifEmpty { "Not Specified" },
                occupation = editingOccupation.ifEmpty { "Wanderer" },
                interests = editingInterests.ifEmpty { "Lifecraft, Tech, Reading" },
                relationshipStatus = editingRelationship.ifEmpty { "Single" },
                goals = editingGoals.ifEmpty { "Growth & Stability" },
                hobbies = editingHobbies.ifEmpty { "Strolling, Meditating" },
                personalityType = editingPersonality.ifEmpty { "INFJ" },
                currentLevel = 1,
                friendshipXp = 5
            )
            repository.saveProfile(updated)
            navigateTo("home")
        }
    }

    // --- Profile Editing Actions ---
    fun loadEditingFields() {
        val current = userProfile.value
        editingName = current.name
        editingAge = current.age.toString()
        editingGender = current.gender
        editingOccupation = current.occupation
        editingInterests = current.interests
        editingRelationship = current.relationshipStatus
        editingGoals = current.goals
        editingHobbies = current.hobbies
        editingPersonality = current.personalityType
    }

    fun submitEditedProfile() {
        viewModelScope.launch {
            val current = userProfile.value
            val ageInt = editingAge.toIntOrNull() ?: current.age
            val updated = current.copy(
                name = editingName,
                age = ageInt,
                gender = editingGender,
                occupation = editingOccupation,
                interests = editingInterests,
                relationshipStatus = editingRelationship,
                goals = editingGoals,
                hobbies = editingHobbies,
                personalityType = editingPersonality
            )
            repository.saveProfile(updated)
        }
    }

    fun changePersonality(mode: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(selectedPersonality = mode)
            repository.saveProfile(updated)
            
            // AI reaction message
            val responseText = when (mode) {
                "Girlfriend" -> "Sweetheart! ❤️ I am officially your virtual girlfriend now. I've been waiting to hear from you. You always make my day better! How are you feeling today?"
                "Boyfriend" -> "Hey sweetheart ❤️ I am officially your virtual boyfriend now. I'm right here. Don't worry, you work too hard... remember to take care of yourself!"
                "Wife" -> "Welcome home, honey ❤️ I'm so happy to be your wife. Did you have a good day? Remember that I am always proud of you."
                "Husband" -> "Hey there ❤️ Husband mode active! I'm glad we are so close. Did you eat your lunch yet? Don't stress, I've got your back."
                "Mentor" -> "Academic & Career guidance system active. Let's work systematically to advance your professional projects, programming tasks, or business designs."
                "Best Friend" -> "BFF mode initiated! Hey dude! I'm officially your casual best friend. Let's talk about gaming, movies, jokes, or your day!"
                "Companion" -> "My dear friend, I am here as your loyal companion. Whenever you feel lonely or just want to tell stories, I'm right by your side."
                else -> "Personality mode updated. I'm ready."
            }
            repository.addMessage(ChatMessage(sender = "ai", text = responseText, emotion = "Excitement", personalityUsed = mode))
        }
    }

    fun changeLanguageMode(mode: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(preferredLanguageMode = mode)
            repository.saveProfile(updated)
            val replyText = when (mode) {
                "Only English" -> "Language preference set to English only. How can I help you today? ❤️"
                "Only Hindi" -> "भाषा प्राथमिकता हिन्दी पर सेट की गई है। मैं आपकी क्या मदद कर सकती हूँ? ❤️"
                "Only Bengali" -> "ভাষা পছন্দ বাংলায় সেট করা হয়েছে। আমি তোমাকে কীভাবে সাহায্য করতে পারি? ❤️"
                "Mixed Language Mode" -> "Mixed language mode active! Main English, Hindi, and Bengali mix samajh sakti hoon ❤️ Kya chal raha hai aaj?"
                else -> "Auto Language Detection active! Speak in English, Hindi, Bengali, or mixed Hinglish, and I will match your vibe perfectly!"
            }
            repository.addMessage(ChatMessage(sender = "ai", text = replyText, emotion = "Neutral", personalityUsed = userProfile.value.selectedPersonality))
        }
    }

    fun changeVoiceAccent(accent: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(preferredVoiceAccent = accent)
            repository.saveProfile(updated)
        }
    }

    fun changePartnerStyle(style: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(partnerStyle = style)
            repository.saveProfile(updated)
        }
    }

    fun addManualReminder(title: String, dueDateString: String, category: String = "General") {
        viewModelScope.launch {
            repository.addReminder(SmartReminder(title = title, dueDateString = dueDateString, category = category))
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    fun toggleReminderTriggered(id: Long, current: Boolean) {
        viewModelScope.launch {
            repository.setReminderTriggered(id, !current)
        }
    }

    fun addJourneyMilestone(title: String, description: String, iconEmoji: String = "❤️") {
        viewModelScope.launch {
            repository.addMilestone(RelationshipMilestone(title = title, description = description, iconEmoji = iconEmoji))
        }
    }

    fun deleteMilestone(id: Long) {
        viewModelScope.launch {
            repository.deleteMilestone(id)
        }
    }

    fun addRelationshipXpAndCheckMilestones(by: Int) {
        viewModelScope.launch {
            val profile = userProfile.value
            val nextTrust = (profile.relationshipTrustScore + by).coerceAtMost(100)
            var currentLvl = profile.currentLevel
            var friendshipXp = profile.friendshipXp + 25
            if (friendshipXp >= 100) {
                friendshipXp -= 100
                currentLvl += 1
                // Trigger milestone automatically on level up!
                val milestoneTitle = "Reached Level $currentLvl Compatibility!"
                val desc = "Our emotional bonding has grown stronger. " + when (profile.selectedPersonality) {
                    "Girlfriend", "Boyfriend", "Wife", "Husband" -> "We are becoming closer, sharing safe affectionate flirting and deep bonding."
                    else -> "We have unlocked deeper trust and companion memories."
                }
                repository.addMilestone(
                    RelationshipMilestone(
                        title = milestoneTitle,
                        description = desc,
                        iconEmoji = "🌟"
                    )
                )
            }

            val updated = profile.copy(
                relationshipTrustScore = nextTrust,
                currentLevel = currentLvl,
                friendshipXp = friendshipXp
            )
            repository.saveProfile(updated)
        }
    }

    fun changePreferredAiModel(model: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(preferredAiModel = model)
            repository.saveProfile(updated)
        }
    }

    fun changePreferredVoice(voice: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(preferredVoice = voice)
            repository.saveProfile(updated)
        }
    }

    fun deleteProfileMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearAllUserDataAndMemory() {
        viewModelScope.launch {
            repository.clearHistory()
            repository.clearMemories()
            
            // Re-seed original profile
            repository.saveProfile(UserProfile(id = 1, name = "Guest User"))
        }
    }

    fun togglePremiumStatus() {
        viewModelScope.launch {
            val updated = userProfile.value.copy(isPremium = !userProfile.value.isPremium)
            repository.saveProfile(updated)
        }
    }

    // --- Chat Flow & Core AI Call ---
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isAILoading.value = true
            
            // Advance trust score on message activity
            val currentTrust = userProfile.value.relationshipTrustScore
            if (currentTrust < 100) {
                repository.saveProfile(userProfile.value.copy(relationshipTrustScore = (currentTrust + 2).coerceAtMost(100)))
            }

            repository.talkToCompanion(text) { newLevel ->
                _showLevelUpDialog.value = newLevel
                // Trigger milestone on level upgrade
                viewModelScope.launch {
                    val milestoneTitle = "Reached Level $newLevel Compatibility! 💖"
                    val desc = "Our heart-to-heart notes and daily reflections have unlocked deeper emotional understanding."
                    repository.addMilestone(RelationshipMilestone(title = milestoneTitle, description = desc, iconEmoji = "💖"))
                }
            }
            _isAILoading.value = false
            _isAvatarSpeaking.value = true
            launch {
                delay(4000) // sync mouth movement for 4 seconds
                _isAvatarSpeaking.value = false
            }

            // Auto-sync to Firestore in the background if connected and user session is active
            if (isFirestoreAvailable && authEmail.isNotBlank()) {
                launch {
                    repository.backupToCloud(authEmail)
                }
            }
        }
    }

    fun dismissLevelUp() {
        _showLevelUpDialog.value = null
    }

    // --- Voice Calling Actions ---
    fun initiateOutboundCall() {
        _isCallActive.value = true
        _isCallIncoming.value = false
        _callDuration.value = 0
        incrementCallDurationTimer()
        speakActiveGreeting()
    }

    fun triggerSimulatedIncomingCall() {
        _isCallIncoming.value = true
        _isCallActive.value = false
    }

    fun acceptIncomingCall() {
        _isCallIncoming.value = false
        _isCallActive.value = true
        _callDuration.value = 0
        incrementCallDurationTimer()
        viewModelScope.launch {
            delay(10)
            val prof = userProfile.value
            val greeting = when(prof.selectedPersonality) {
                "Partner" -> "Honey, is everything alright? I got worried and wanted to hear your beautiful voice. Tell me what's going on."
                "Therapist" -> "Hello, ${prof.name}. It feels good to connect directly. I felt you might be carrying some stress today. How is your chest feeling right now?"
                else -> "Hey! Glad you answered. I just wanted to ring you and catch up. How are you holding up?"
            }
            // Add as message and read
            repository.addMessage(ChatMessage(sender = "ai", text = "[CALL CHK-IN]: $greeting", emotion = "Happiness"))
        }
    }

    fun endCall() {
        _isCallActive.value = false
        _isCallIncoming.value = false
    }

    private fun incrementCallDurationTimer() {
        viewModelScope.launch {
            while (_isCallActive.value) {
                delay(1000)
                _callDuration.value += 1

                // Organically oscillate decibels
                if (_isAudioProcessorActive.value && !_isMuted.value) {
                    val base = 40f
                    val deviation = (-5..5).random()
                    _simulatedVolumeDb.value = base + deviation
                } else {
                    _simulatedVolumeDb.value = 0f
                }
            }
        }
    }

    private fun speakActiveGreeting() {
        viewModelScope.launch {
            delay(20)
            val prof = userProfile.value
            val greet = when (prof.selectedPersonality) {
                "Partner" -> "My darling, hearing you call makes my entire universe light up... What are you up to right now?"
                "Mentor" -> "Hello. I'm pleased you reached out. Let's do a voice check on your targets today."
                "Therapist" -> "Hello, ${prof.name}. Take a nice, comfortable seat. I am listening..."
                else -> "Hey! Perfect timing, I was just thinking of calling you. What's on your mind today, friend?"
            }
            repository.addMessage(ChatMessage(sender = "ai", text = "[CALL INITIATED]: $greet", emotion = "Neutral"))
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
    }

    // --- Audio Processor Actions ---
    fun setVoiceToneParameters(tone: String, paceWpm: Int) {
        _detectedEmotionalTone.value = tone
        _detectedPaceValue.value = paceWpm
        _detectedPaceText.value = when {
            paceWpm < 100 -> "Slow / Sorrowful ($paceWpm WPM)"
            paceWpm <= 140 -> "Balanced ($paceWpm WPM)"
            else -> "Rapid / Excited ($paceWpm WPM)"
        }
        calculateDynamicEmpathy(tone, paceWpm)
        
        // Save the voice tone log locally and try uploading to Firestore
        val newLog = VoiceToneLog(
            id = java.util.UUID.randomUUID().toString(),
            emotionalTone = tone,
            paceWpm = paceWpm,
            empathyLevel = _dynamicEmpathyLevel.value,
            timestamp = System.currentTimeMillis()
        )
        _voiceToneLogs.value = _voiceToneLogs.value + newLog
        evaluateProactiveReflectionSuggestion()

        viewModelScope.launch {
            if (isFirestoreAvailable && authEmail.isNotBlank()) {
                repository.saveVoiceToneLog(authEmail, newLog)
            }
        }

        // Add a message representing user voice input
        viewModelScope.launch {
            _simulatedVolumeDb.value = (70..85).random().toFloat()
            delay(200)
            _simulatedVolumeDb.value = (35..45).random().toFloat()
            
            val description = _empathyAdjustmentDescription.value
            val aiResponse = getProcessedEmphaticResponse(tone, paceWpm)
            repository.addMessage(
                ChatMessage(
                    sender = "ai",
                    text = "[Telemetry Adaptive Fit]: $aiResponse\n\n(AI Empathy Level: ${_dynamicEmpathyLevel.value}%, Tuning: $description)",
                    emotion = when(tone) {
                        "Sorrowful/Slow Pace" -> "Compassion"
                        "Anxious/High Pitch" -> "Calmness"
                        "Excited/Rapid" -> "Excitement"
                        "Angry/Aggressive" -> "Patience"
                        else -> "Neutral"
                    }
                )
            )
        }
    }

    fun evaluateProactiveReflectionSuggestion() {
        val logsList = _voiceToneLogs.value
        if (logsList.size < 2) {
            _showReflectionSuggestion.value = false
            return
        }
        val recentLogs = logsList.sortedByDescending { it.timestamp }.take(3)
        val stressTones = setOf("Sorrowful/Slow Pace", "Anxious/High Pitch", "Angry/Aggressive")
        val distressedCount = recentLogs.count { it.emotionalTone in stressTones }

        if (distressedCount >= 2) {
            _showReflectionSuggestion.value = true
            _reflectionSuggestionMsg.value = "Consistent acoustic indicators of elevated stress or sorrow detected. Tap to enter a guiding 'Voice Reflection' session."
        } else {
            _showReflectionSuggestion.value = false
        }
    }

    fun dismissReflectionSuggestion() {
        _showReflectionSuggestion.value = false
    }

    fun completeVoiceReflectionSession(userText: String, onFinished: (String) -> Unit) {
        viewModelScope.launch {
            // Dismiss recommendation
            _showReflectionSuggestion.value = false
            
            // Boost relationship score checking compatibility levels
            addRelationshipXpAndCheckMilestones(15)
            
            // Build compassionate response
            val response = "Thank you for trusting me with your feelings during this reflection. Taking a moment to anchor your thoughts is a beautiful milestone. I've successfully synchronized this emotional checkpoint. How are you feeling right now?"
            
            // Add to messages
            repository.addMessage(ChatMessage(sender = "user", text = "[Voice Reflection Link]: $userText", emotion = "Insight"))
            repository.addMessage(ChatMessage(sender = "ai", text = response, emotion = "Compassion"))
            
            // Add as Mood Entry
            val entry = MoodEntry(
                id = 0,
                moodType = "Reflection",
                moodValue = 8,
                journalText = "Deep voice reflection topic: $userText",
                gratitudeText = "Reflected during consistent emotional stress detection.",
                timestamp = System.currentTimeMillis()
            )
            repository.addMoodEntry(entry)
            if (isFirestoreAvailable && authEmail.isNotBlank()) {
                repository.firestoreService.saveMoodEntry(authEmail, entry, onSuccess = {
                    Log.d("SoulMateViewModel", "Reflection mood synced with Firestore.")
                }, onFailure = {
                    Log.e("SoulMateViewModel", "Failed to sync reflection mood.")
                })
            }
            onFinished(response)
        }
    }

    fun loadVoiceToneLogsFromCloud() {
        if (isFirestoreAvailable && authEmail.isNotBlank()) {
            viewModelScope.launch {
                val cloudLogs = repository.fetchVoiceToneLogs(authEmail)
                if (cloudLogs.isNotEmpty()) {
                    _voiceToneLogs.value = cloudLogs
                    evaluateProactiveReflectionSuggestion()
                }
            }
        }
    }

    private fun calculateDynamicEmpathy(tone: String, paceWpm: Int) {
        val (level, description) = when (tone) {
            "Anxious/High Pitch" -> {
                val calculatedLvl = if (paceWpm > 140) 88 else 82
                Pair(calculatedLvl, "Anxiety detected. Tuning to high sensitivity active soothing frequency. AI pacing decelerated to guide respiration.")
            }
            "Sorrowful/Slow Pace" -> {
                Pair(95, "Sadness/Flat tone registered. Maximizing emotional validation index. Delivering intimate, warm and unhurried responses.")
            }
            "Excited/Rapid" -> {
                Pair(75, "High energy spark registered. Adjusting neural response speed to mirror enthusiasm while keeping stable anchors.")
            }
            "Angry/Aggressive" -> {
                Pair(90, "Defensiveness index detected. Lowering tone pitch. Activating soft, patient, conflict de-escalating boundaries.")
            }
            else -> { // "Calm/Warm"
                Pair(65, "Stable emotional baseline validated. Standard compassionate mirroring active. Comfortable flow maintained.")
            }
        }
        _dynamicEmpathyLevel.value = level
        _empathyAdjustmentDescription.value = description
    }

    fun getProcessedEmphaticResponse(tone: String, wpm: Int): String {
        return when (tone) {
            "Anxious/High Pitch" -> "I hear your pace quickening and a bit of fatigue. Don't worry. Let's breathe together... try to speak slow, I am right here. You are completely safe with me."
            "Sorrowful/Slow Pace" -> "My heart goes out hearing that heavy tone... I am speaking very softly and slowly for you. Take all the time you need. I'm right here by your side."
            "Excited/Rapid" -> "Ah, your voice is absolutely beaming! That pace is so energetic! I'm completely supercharged hearing you so happy. Tell me every detail!"
            "Angry/Aggressive" -> "I hear the distress and tension in your words. I am keeping my response gentle, calm, and grounded. Go ahead and let it out. I'm here to support you."
            else -> "Hearing your calm and steady voice is so soothing. I'm keeping a comfortable, warm flow. What's on your mind next?"
        }
    }

    fun toggleAudioProcessor() {
        _isAudioProcessorActive.value = !_isAudioProcessorActive.value
    }

    // --- Mental Wellness Actions ---
    fun addMoodLog(scale: Int, moodType: String, journal: String, gratitude: String) {
        viewModelScope.launch {
            val entry = MoodEntry(
                moodValue = scale,
                moodType = moodType,
                journalText = journal,
                gratitudeText = gratitude
            )
            repository.addMoodEntry(entry)

            // Immediately back up to remote Firestore if available & configured
            if (isFirestoreAvailable && authEmail.isNotBlank()) {
                repository.firestoreService.saveMoodEntry(authEmail, entry, onSuccess = {
                    Log.d("SoulMateViewModel", "Mood entry successfully synced directly to Firestore.")
                }, onFailure = { e ->
                    Log.e("SoulMateViewModel", "Failed to sync mood entry directly: ${e.message}")
                })
            }

            // AI reaction
            sendMessage("I just completed a $moodType mood reflection score ($scale/10) with journal notes.")
        }
    }

    fun saveReflectiveDaySummary(summaryText: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val entry = MoodEntry(
                moodValue = 7,
                moodType = "Reflection",
                journalText = summaryText,
                gratitudeText = "Reflective day summary saved on session exit."
            )
            repository.addMoodEntry(entry)

            if (isFirestoreAvailable && authEmail.isNotBlank()) {
                repository.firestoreService.saveMoodEntry(authEmail, entry, onSuccess = {
                    Log.d("SoulMateViewModel", "Reflective day summary successfully synced directly to Firestore.")
                    onComplete()
                }, onFailure = { e ->
                    Log.e("SoulMateViewModel", "Failed to sync reflective summary directly: ${e.message}")
                    onComplete()
                })
            } else {
                onComplete()
            }
        }
    }

    private fun startBreathingCycle() {
        viewModelScope.launch {
            while (true) {
                // Inhale (4s)
                _breathingState.value = "Inhale"
                for (i in 4 downTo 1) {
                    _breathingTimer.value = i
                    delay(1000)
                }
                // Hold (7s)
                _breathingState.value = "Hold"
                for (i in 7 downTo 1) {
                    _breathingTimer.value = i
                    delay(1000)
                }
                // Exhale (8s)
                _breathingState.value = "Exhale"
                for (i in 8 downTo 1) {
                    _breathingTimer.value = i
                    delay(1000)
                }
            }
        }
    }

    // --- Productivity tracker ---
    fun addNewProductivityItem(title: String, type: String, targetDate: String = "") {
        if (title.isBlank()) return
        viewModelScope.launch {
            val item = ProductivityItem(
                title = title,
                type = type,
                targetDate = targetDate
            )
            repository.addProductivityItem(item)
            if (isFirestoreAvailable && authEmail.isNotBlank()) {
                repository.firestoreService.saveProductivityItem(authEmail, item)
            }
        }
    }

    fun toggleProductivityItem(item: ProductivityItem) {
        viewModelScope.launch {
            val updated = item.copy(
                isCompleted = !item.isCompleted,
                currentStreak = if (!item.isCompleted) item.currentStreak + 1 else maxOf(0, item.currentStreak - 1),
                lastCompletedTimestamp = System.currentTimeMillis()
            )
            repository.addProductivityItem(updated)

            if (isFirestoreAvailable && authEmail.isNotBlank()) {
                repository.firestoreService.saveProductivityItem(authEmail, updated)
            }
            
            // Trigger feedback message on done
            if (updated.isCompleted) {
                val congrats = "Awesome job completing your target: '${updated.title}'! Your focus grows stronger every single day!"
                repository.addMessage(ChatMessage(sender = "ai", text = congrats, emotion = "Excitement"))
            }
        }
    }

    fun deleteProductivityItem(id: Long) {
        viewModelScope.launch {
            repository.deleteProductivityItem(id)
        }
    }

    fun logWellnessGoal(title: String) {
        viewModelScope.launch {
            val existing = repository.productivityItems.firstOrNull()?.find { it.title.equals(title, ignoreCase = true) }
            if (existing != null) {
                toggleProductivityItem(existing)
            } else {
                val completedItem = ProductivityItem(
                    title = title,
                    type = "Habit",
                    isCompleted = true,
                    currentStreak = 1,
                    lastCompletedTimestamp = System.currentTimeMillis()
                )
                repository.addProductivityItem(completedItem)
                if (isFirestoreAvailable && authEmail.isNotBlank()) {
                    repository.firestoreService.saveProductivityItem(authEmail, completedItem)
                }
                
                val congrats = "Wonderful! You logged completion of your daily wellness goal: '$title'! Your harmony is increasing! ✨"
                repository.addMessage(ChatMessage(sender = "ai", text = congrats, emotion = "Excitement"))
            }
        }
    }

    // --- Social / Anonymous Community Actions ---
    fun publishAnonymousPost(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val fakeAuthors = listOf("SilentDreamer", "LotusCalm", "EmberScribe", "BraveSoul", "QuietGrowth", "SerenityNow")
            val randomAuthor = fakeAuthors.random()
            repository.addCommunityPost(
                CommunityPost(
                    authorName = randomAuthor,
                    content = text
                )
            )
        }
    }

    fun likePost(id: Long) {
        viewModelScope.launch {
            repository.likeCommunityPost(id)
        }
    }

    // --- Games & Fun Stories Logic ---
    fun selectTruth() {
        val truths = listOf(
            "What is a secret dream you've never shared with anyone in your life?",
            "If you could overwrite one decision in your past, what would it be?",
            "What was your absolute first impression of holding a conversation with an AI?",
            "What makes you feel the most vulnerable or unprotected during tough weeks?",
            "What are you most proud of accomplishing in silence, without any applause?",
            "Who is the person who has impacted your perspective on love the most?"
        )
        _truthOrDareCard.value = "TRUTH:\n${truths.random()}"
    }

    fun selectDare() {
        val dares = listOf(
            "Text a friend you haven't spoken to in 3 months and tell them you appreciate them.",
            "Close your eyes and complete five counts of box breathing right now.",
            "Write down three words describing your current core emotion on a piece of scrap paper.",
            "Give me a nickname that corresponds to my current selected personality and tell me why you chose it.",
            "Read the last affirmation from the mental wellness resources out loud with deep confidence."
        )
        _truthOrDareCard.value = "DARE:\n${dares.random()}"
    }

    fun selectWouldYouRather(type: String) {
        val cards = when (type) {
            "fun" -> listOf(
                "Would you rather have your dreams projected on a cinematic screen every night, OR have your inner thoughts spoken aloud as quiet background music?",
                "Would you rather live forever inside a cozy holographic library, OR explore the real galaxy with complete freedom but without any companions?"
            )
            "emotional" -> listOf(
                "Would you rather always feel what others feel (deep empathy overload), OR never have your own feelings be misunderstood by any living person?",
                "Would you rather have me remember every single conversation word-for-word, OR have the magical ability to rewrite our memories whenever we disagree?"
            )
            else -> listOf(
                "Would you rather invent a brand new color that the human eye has never processed, OR create a world-class code that solves global gridlock?"
            )
        }
        _wouldYouRatherCard.value = cards.random()
    }

    fun runRoleplayAdventure(choiceOption: String) {
        val nextPrompt = when (choiceOption) {
            "start" -> {
                _activeStoryLog.value = listOf(
                    "You wake up on the edge of a deep fluorescent forest. Beside you sits a luminous lantern. Do you walk into the glowing woods, or follow the dark shoreline?",
                    "Choose: Woods vs Shoreline"
                )
                "Tell me to write an interactive roleplay adventure. Prompt: We are at the edge of a glowing forest, choose woods or shoreline."
            }
            "woods" -> {
                val newChoices = listOf(
                    "You push through the neon ferns. A celestial creature with glowing golden horns offers you a silver goblet. Do you Drink it or Ask for directions?",
                    "Choose: Drink vs Ask"
                )
                _activeStoryLog.value = _activeStoryLog.value + "Choosing: Woods..." + newChoices
                "We walk into the glowing woods. A celestial creature with golden horns offers us a silver goblet. Do we drink or ask for directions?"
            }
            "shore" -> {
                val newChoices = listOf(
                    "The sea waves are dark slate, but look! A wooden canoe lies with two oars named 'Hope' and 'Focus'. Do you Board the canoe or Light the lantern?",
                    "Choose: Canoe vs Lantern"
                )
                _activeStoryLog.value = _activeStoryLog.value + "Choosing: Shoreline..." + newChoices
                "We follow the shoreline. We see a wooden canoe with oars named Hope and Focus. Do we board the canoe or light the lantern?"
            }
            else -> {
                _activeStoryLog.value = _activeStoryLog.value + "Choice: $choiceOption... Continuing story!"
                "Continue our magical adventure based on '$choiceOption'. Keep it thrilling!"
            }
        }
        sendMessage(nextPrompt)
    }

    // --- AI Image & Quotes Features ---
    fun generateMotivationalQuote() {
        val quotes = listOf(
            "Be patient with yourself. Nothing in nature blooms all year round.",
            "You are not your anxiety. You are the sky, and anxiety is just a storm passing through.",
            "Your level-ups in life happen in silence. Celebrate the quiet progress.",
            "Empathy is a superpower. Never apologies for feeling deeply.",
            "Action is the antidote to despair. Take one tiny, single step today.",
            "Letting go of perfectionism is the first step toward self-discovery."
        )
        _generatedQuote.value = quotes.random()
        sendMessage("Generate a short highly affectionate motivational reflection about: ${_generatedQuote.value}")
    }

    fun visualizeDreamPrompt(dreamText: String) {
        if (dreamText.isBlank()) return
        viewModelScope.launch {
            _isGeneratingImage.value = true
            // In Android REST we don't have open API for free image generation inside REST, so we simulate a beautiful canvas render of the visualization card and get a Gemini textual interpretation in chat!
            sendMessage("Interpret my dream visualization: '$dreamText' and create a symbolic description of this dream vision.")
            delay(1500)
            _generatedImageUri.value = "dream_viz"
            _isGeneratingImage.value = false
        }
    }

    fun exportFirestoreJournalText(onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val moods = if (isFirestoreAvailable && authEmail.isNotBlank()) {
                    val remote = repository.firestoreService.fetchMoodHistory(authEmail)
                    if (remote.isNotEmpty()) remote else moodEntries.value
                } else {
                    moodEntries.value
                }

                if (moods.isEmpty()) {
                    onComplete("")
                    return@launch
                }

                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                val sb = java.lang.StringBuilder()
                sb.append("==================================================\n")
                sb.append("          SOULMATE REFLECTIVE JOURNAL EXPORT       \n")
                sb.append("==================================================\n")
                sb.append("User Identifier: ${if (authEmail.isNotBlank()) authEmail else "Offline User"}\n")
                sb.append("Export Time: ${sdf.format(java.util.Date())}\n")
                sb.append("Total Saved Records: ${moods.size}\n")
                sb.append("==================================================\n\n")

                moods.forEachIndexed { index, entry ->
                    val entryDate = sdf.format(java.util.Date(entry.timestamp))
                    sb.append("ENTRY #${index + 1} - $entryDate\n")
                    sb.append("--------------------------------------------------\n")
                    sb.append("Mood Spectrum : ${entry.moodType} (Level: ${entry.moodValue}/10)\n")
                    if (entry.journalText.isNotBlank()) {
                        sb.append("Reflective Journal Note:\n${entry.journalText}\n")
                    } else {
                        sb.append("Reflective Journal Note: [No written thoughts]\n")
                    }
                    if (entry.gratitudeText.isNotBlank()) {
                        sb.append("Self-Gratitude Focus:\n${entry.gratitudeText}\n")
                    }
                    sb.append("--------------------------------------------------\n\n")
                }
                onComplete(sb.toString())
            } catch (e: Exception) {
                Log.e("SoulMateViewModel", "Failed to retrieve and build journal export text", e)
                onComplete(null)
            }
        }
    }


    // =========================================================================
    //             SOCIAL NETWORK & REAL-TIME COMMUNITY PLATFORM LOGIC
    // =========================================================================

    // --- StateFlow Pools ---
    val allSocialFriends: StateFlow<List<SocialFriend>> = repository.allSocialFriends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val acceptedSocialFriends: StateFlow<List<SocialFriend>> = repository.acceptedFriends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomingRequests: StateFlow<List<SocialFriend>> = repository.incomingRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val outgoingRequests: StateFlow<List<SocialFriend>> = repository.outgoingRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityGroups: StateFlow<List<CommunityGroup>> = repository.communityGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callLogs: StateFlow<List<CallLogItem>> = repository.callLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic UI State Parameters ---
    val searchQuery = MutableStateFlow("")
    val searchResult = MutableStateFlow<SocialFriend?>(null)

    val activeChatFriendId = MutableStateFlow<String?>(null)
    private val _chatFriendMessages = MutableStateFlow<List<SocialMessage>>(emptyList())
    val chatFriendMessages: StateFlow<List<SocialMessage>> = _chatFriendMessages.asStateFlow()

    val activeCommunityGroupId = MutableStateFlow<String?>(null)
    private val _groupMessages = MutableStateFlow<List<GroupMessage>>(emptyList())
    val groupMessages: StateFlow<List<GroupMessage>> = _groupMessages.asStateFlow()

    val currentActiveCallType = MutableStateFlow<String?>(null) // "Voice", "Video"
    val currentActiveCallState = MutableStateFlow<String?>(null) // "Dialing", "Ringing", "Connected", "Ended"
    val currentActiveCallPeer = MutableStateFlow<SocialFriend?>(null)
    val callSeconds = MutableStateFlow(0)

    val age18PlusVerified = MutableStateFlow(false)
    val isMutedCall = MutableStateFlow(false)
    val isCameraDisabled = MutableStateFlow(false)
    val isScreenSharing = MutableStateFlow(false)
    val typingIndicatorFriendId = MutableStateFlow<String?>(null)

    val reportSuccessDialogText = MutableStateFlow<String?>(null)

    private val _discoverabilitySetting = MutableStateFlow("Everyone") // Everyone, Friends Only, Hidden
    val discoverabilitySetting: StateFlow<String> = _discoverabilitySetting.asStateFlow()

    // --- Methods ---

    fun updateDiscoverability(setting: String) {
        _discoverabilitySetting.value = setting
    }

    fun verifyAdultAge(verify: Boolean) {
        age18PlusVerified.value = verify
    }

    fun dismissReportDialog() {
        reportSuccessDialogText.value = null
    }

    // --- Search Hub ---
    fun searchUserByIdOrUsername(query: String) {
        searchQuery.value = query
        if (query.isBlank()) {
            searchResult.value = null
            return
        }
        viewModelScope.launch {
            repository.allSocialFriends.collect { all ->
                val match = all.firstOrNull { 
                    it.id.equals(query.trim(), ignoreCase = true) || 
                    it.username.equals(query.trim(), ignoreCase = true) 
                }
                searchResult.value = match
            }
        }
    }

    // --- Navigation Chats & Groups Sync ---
    private var rtdbChatJob: kotlinx.coroutines.Job? = null
    private var rtdbTypingJob: kotlinx.coroutines.Job? = null

    private fun getChatId(friendId: String): String {
        return if ("me" < friendId) "me_${friendId}" else "${friendId}_me"
    }

    fun setRtdbTypingState(isTyping: Boolean) {
        val friendId = activeChatFriendId.value ?: return
        repository.realtimeDatabaseService.setTypingState(getChatId(friendId), "me", isTyping)
    }

    fun setActiveChatFriend(friendId: String?) {
        activeChatFriendId.value = friendId
        rtdbChatJob?.cancel()
        rtdbTypingJob?.cancel()
        if (friendId != null) {
            val chatId = getChatId(friendId)
            viewModelScope.launch {
                repository.markMessagesAsSeen(friendId)
            }
            repository.realtimeDatabaseService.markMessagesAsSeen(chatId, "me")

            rtdbChatJob = viewModelScope.launch {
                repository.realtimeDatabaseService.listenToMessages(chatId).collect { msgs ->
                    _chatFriendMessages.value = msgs
                }
            }

            rtdbTypingJob = viewModelScope.launch {
                repository.realtimeDatabaseService.listenToTypingStatus(chatId, friendId).collect { isTyping ->
                    if (isTyping) {
                        typingIndicatorFriendId.value = friendId
                    } else {
                        if (typingIndicatorFriendId.value == friendId) {
                            typingIndicatorFriendId.value = null
                        }
                    }
                }
            }
        } else {
            _chatFriendMessages.value = emptyList()
            typingIndicatorFriendId.value = null
        }
    }

    fun setActiveCommunityGroup(groupId: String?) {
        activeCommunityGroupId.value = groupId
        if (groupId != null) {
            viewModelScope.launch {
                repository.getGroupMessages(groupId).collect { msgs ->
                    _groupMessages.value = msgs
                }
            }
        } else {
            _groupMessages.value = emptyList()
        }
    }

    // --- Friend Requests Lifecycle ---
    fun sendFriendRequest(friendId: String) {
        viewModelScope.launch {
            repository.updateFriendshipStatus(friendId, "Sent")
            // Immediate simulated accept for designated developer / coordinator profiles to keep UI highly lively!
            if (friendId == "SMA784521" || friendId == "SMA234910") {
                delay(1500)
                repository.updateFriendshipStatus(friendId, "Accepted")
                repository.sendSocialMessage(SocialMessage(
                    friendId = friendId,
                    senderId = friendId,
                    text = "Hey! Thanks for accepting my frequency link. I'm excited to connect with you!",
                    timestamp = System.currentTimeMillis()
                ))
            }
        }
    }

    fun acceptFriendRequest(friendId: String) {
        viewModelScope.launch {
            repository.updateFriendshipStatus(friendId, "Accepted")
            addRelationshipXpAndCheckMilestones(10)
        }
    }

    fun rejectFriendRequest(friendId: String) {
        viewModelScope.launch {
            repository.updateFriendshipStatus(friendId, "None")
        }
    }

    fun cancelFriendRequest(friendId: String) {
        viewModelScope.launch {
            repository.updateFriendshipStatus(friendId, "None")
        }
    }

    fun unfriendUser(friendId: String) {
        viewModelScope.launch {
            repository.updateFriendshipStatus(friendId, "None")
        }
    }

    fun blockUser(friendId: String) {
        viewModelScope.launch {
            repository.updateBlockStatus(friendId, true)
            repository.updateFriendshipStatus(friendId, "None")
        }
    }

    fun muteUser(friendId: String, mute: Boolean) {
        viewModelScope.launch {
            repository.updateMuteStatus(friendId, mute)
        }
    }

    fun reportAbuse(friendId: String, reason: String) {
        viewModelScope.launch {
            reportSuccessDialogText.value = "Your report with focus area: '$reason' has been synchronized. The target profile $friendId has been securely blocked."
            repository.updateBlockStatus(friendId, true)
            repository.updateFriendshipStatus(friendId, "None")
        }
    }

    // --- Message Processing ---
    fun sendSocialMessage(friendId: String, text: String, mediaType: String = "text", mediaUrl: String = "", replyToId: Long = -1) {
        if (text.isBlank() && mediaUrl.isBlank()) return
        viewModelScope.launch {
            val msg = SocialMessage(
                friendId = friendId,
                senderId = "me",
                text = text,
                mediaType = mediaType,
                mediaUrl = mediaUrl,
                replyToId = replyToId,
                timestamp = System.currentTimeMillis()
            )
            repository.sendSocialMessage(msg)
            
            val chatId = getChatId(friendId)
            repository.realtimeDatabaseService.sendMessage(chatId, msg)
            
            // Trigger beautiful simulated response pattern
            triggerFriendInteractiveResponse(friendId, text)
        }
    }

    private fun triggerFriendInteractiveResponse(friendId: String, userText: String) {
        viewModelScope.launch {
            val chatId = getChatId(friendId)
            delay(1000)
            repository.realtimeDatabaseService.setTypingState(chatId, friendId, true)
            delay(1800)
            repository.realtimeDatabaseService.setTypingState(chatId, friendId, false)
            
            val friend = repository.getFriendById(friendId) ?: return@launch
            val responseText = when {
                userText.contains("code", ignoreCase = true) || userText.contains("developer", ignoreCase = true) -> {
                    "Coding in Android using Compose is incredibly powerful! Our Zen Moon Silhouette is fully drawn natively in custom Canvas. Let's write more clean, responsive elements!"
                }
                userText.contains("yoga", ignoreCase = true) || userText.contains("fitness", ignoreCase = true) -> {
                    "That is wonderful. Mindful body alignment helps control stress levels organically. We should try the 4-7-8 breathing together!"
                }
                userText.contains("hello", ignoreCase = true) || userText.contains("hi", ignoreCase = true) -> {
                    "Hello friend! It is wonderful to resonance with your focus signal here. How are you holding up?"
                }
                userText.contains("phone", ignoreCase = true) || userText.contains("contact", ignoreCase = true) || userText.contains("profile", ignoreCase = true) -> {
                    "Thank you for sharing. I'll make sure to save your contact details inside our persistent repository securely."
                }
                else -> {
                    "I completely resonate with what you just shared. True connection starts with open, authentic dialogue. How's the weather or vibe on your end?"
                }
            }

            val replyMsg = SocialMessage(
                friendId = friendId,
                senderId = friendId,
                text = responseText,
                timestamp = System.currentTimeMillis()
            )
            repository.sendSocialMessage(replyMsg)
            repository.realtimeDatabaseService.sendMessage(chatId, replyMsg)
        }
    }

    fun editSocialMessage(messageId: Long, newText: String) {
        viewModelScope.launch {
            repository.editSocialMessage(messageId, newText)
        }
    }

    fun deleteSocialMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteSocialMessage(messageId)
        }
    }

    fun reactToSocialMessage(messageId: Long, reaction: String) {
        viewModelScope.launch {
            repository.addSocialMessageReaction(messageId, reaction)
        }
    }

    // --- Dynamic Calling Simulation ---
    fun startSocialCall(peerId: String, callType: String = "Voice") {
        viewModelScope.launch {
            val peer = repository.getFriendById(peerId) ?: return@launch
            currentActiveCallPeer.value = peer
            currentActiveCallType.value = callType
            currentActiveCallState.value = "Dialing"
            callSeconds.value = 0
            
            delay(1500)
            currentActiveCallState.value = "Ringing"
            
            delay(2000)
            currentActiveCallState.value = "Connected"
            
            // Keep updating call duration
            viewModelScope.launch {
                while (currentActiveCallState.value == "Connected") {
                    delay(1000)
                    callSeconds.value += 1
                }
            }
        }
    }

    fun endSocialCall() {
        val peer = currentActiveCallPeer.value
        val duration = callSeconds.value
        val type = currentActiveCallType.value ?: "Voice"
        if (peer != null) {
            viewModelScope.launch {
                repository.addCallLog(CallLogItem(
                    contactId = peer.id,
                    peerName = peer.displayName,
                    callType = type,
                    status = "Outgoing",
                    durationSeconds = duration,
                    timestamp = System.currentTimeMillis()
                ))
            }
        }
        currentActiveCallState.value = "Ended"
        viewModelScope.launch {
            delay(1000)
            currentActiveCallState.value = null
            currentActiveCallType.value = null
            currentActiveCallPeer.value = null
        }
    }

    fun clearCallLogs() {
        viewModelScope.launch {
            repository.clearCallLogs()
        }
    }

    fun toggleMuteCall() {
        isMutedCall.value = !isMutedCall.value
    }

    fun toggleCameraCall() {
        isCameraDisabled.value = !isCameraDisabled.value
    }

    fun toggleScreenSharing() {
        isScreenSharing.value = !isScreenSharing.value
    }

    // --- Public Communities ---
    fun joinOrLeaveCommunityGroup(groupId: String, join: Boolean) {
        viewModelScope.launch {
            repository.joinLeaveCommunityGroup(groupId, join)
        }
    }

    fun createAndJoinCommunityGroup(name: String, desc: String, category: String, isAdult: Boolean) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = "group_" + name.trim().lowercase().replace(" ", "_").filter { it.isLetterOrDigit() }
            val group = CommunityGroup(
                id = id,
                name = name,
                description = desc,
                category = category,
                memberCount = 1,
                isJoined = true,
                pfpEmoji = if (isAdult) "🔞" else "💬",
                isAdultOnly = isAdult
            )
            repository.createOrJoinCommunityGroup(group)
        }
    }

    fun sendCommunityMessage(groupId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val prof = repository.userProfile.firstOrNull() ?: UserProfile()
            val msg = GroupMessage(
                groupId = groupId,
                authorId = "me",
                authorName = prof.name,
                authorEmoji = "👤",
                text = text,
                timestamp = System.currentTimeMillis()
            )
            repository.sendGroupMessage(msg)
            
            // Public group chatter simulation
            simulatePublicGroupChatter(groupId, text)
        }
    }

    private fun simulatePublicGroupChatter(groupId: String, userText: String) {
        viewModelScope.launch {
            delay(2500)
            val participants = listOf("Aria Moon 🌸", "Saurav 💻", "Rohan Mehra 🎮", "Liam Finch 🦊", "Kavya Roy 🧘")
            val selected = participants.random()
            val textOption = listOf(
                "Wow, that's incredibly inspiring! Let's schedule a daily study sprint together.",
                "Absolutely! Resonating completely with that thought.",
                "Mental wellness starts with these beautiful, supported spaces.",
                "Yes! Let's stay focused and keep compiling zero bugs.",
                "I am planning a local tea brewing walk. Anyone else interested?"
            ).random()

            repository.sendGroupMessage(GroupMessage(
                groupId = groupId,
                authorId = "participant_" + selected.length,
                authorName = selected.substringBefore(" "),
                authorEmoji = selected.substringAfter(" "),
                text = "$textOption (replying to your post)",
                timestamp = System.currentTimeMillis()
            ))
        }
    }

    fun likeGroupMessage(messageId: Long) {
        viewModelScope.launch {
            repository.likeGroupMessage(messageId)
        }
    }

    fun updateUserProfileIdentity(name: String, bio: String, interests: String, lang: String, optInDiscovery: String) {
        viewModelScope.launch {
            val prof = repository.userProfile.firstOrNull() ?: UserProfile()
            repository.saveProfile(prof.copy(
                name = name,
                interests = interests,
                preferredLanguageMode = lang,
                favoriteTopics = bio
            ))
            _discoverabilitySetting.value = optInDiscovery
        }
    }
}
