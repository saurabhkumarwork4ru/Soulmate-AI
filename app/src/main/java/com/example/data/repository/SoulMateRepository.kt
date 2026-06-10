package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume

class SoulMateRepository(
    private val userProfileDao: UserProfileDao,
    private val chatMessageDao: ChatMessageDao,
    private val companionMemoryDao: CompanionMemoryDao,
    private val moodEntryDao: MoodEntryDao,
    private val productivityItemDao: ProductivityItemDao,
    private val communityPostDao: CommunityPostDao,
    private val smartReminderDao: SmartReminderDao,
    private val relationshipMilestoneDao: RelationshipMilestoneDao,
    val socialFriendDao: SocialFriendDao,
    val socialMessageDao: SocialMessageDao,
    val communityGroupDao: CommunityGroupDao,
    val groupMessageDao: GroupMessageDao,
    val callLogItemDao: CallLogItemDao,
    val firestoreService: FirestoreService,
    val realtimeDatabaseService: RealtimeDatabaseService
) {
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()
    val chatMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()
    val companionMemories: Flow<List<CompanionMemory>> = companionMemoryDao.getAllMemories()
    val moodEntries: Flow<List<MoodEntry>> = moodEntryDao.getAllMoodEntries()
    val productivityItems: Flow<List<ProductivityItem>> = productivityItemDao.getAllItems()
    val communityPosts: Flow<List<CommunityPost>> = communityPostDao.getAllPosts()
    val smartReminders: Flow<List<SmartReminder>> = smartReminderDao.getAllReminders()
    val relationshipMilestones: Flow<List<RelationshipMilestone>> = relationshipMilestoneDao.getAllMilestones()

    // --- Social Network Direct Flows ---
    val allSocialFriends: Flow<List<SocialFriend>> = socialFriendDao.getAllFriends()
    val acceptedFriends: Flow<List<SocialFriend>> = socialFriendDao.getAcceptedFriends()
    val incomingRequests: Flow<List<SocialFriend>> = socialFriendDao.getIncomingFriendRequests()
    val outgoingRequests: Flow<List<SocialFriend>> = socialFriendDao.getOutgoingFriendRequests()
    val communityGroups: Flow<List<CommunityGroup>> = communityGroupDao.getAllGroups()
    val callLogs: Flow<List<CallLogItem>> = callLogItemDao.getAllCallLogs()

    suspend fun getFriendById(id: String): SocialFriend? = withContext(Dispatchers.IO) {
        socialFriendDao.getFriendById(id)
    }

    fun getFriendFlowById(id: String): Flow<SocialFriend?> {
        return socialFriendDao.getFriendFlowById(id)
    }

    suspend fun addSocialFriend(friend: SocialFriend) = withContext(Dispatchers.IO) {
        socialFriendDao.insertFriend(friend)
    }

    suspend fun insertFriends(friends: List<SocialFriend>) = withContext(Dispatchers.IO) {
        socialFriendDao.insertFriends(friends)
    }

    suspend fun updateSocialFriend(friend: SocialFriend) = withContext(Dispatchers.IO) {
        socialFriendDao.updateFriend(friend)
    }

    suspend fun updateFriendshipStatus(friendId: String, status: String) = withContext(Dispatchers.IO) {
        socialFriendDao.updateFriendshipStatus(friendId, status)
    }

    suspend fun updateBlockStatus(friendId: String, blocked: Boolean) = withContext(Dispatchers.IO) {
        socialFriendDao.updateBlockStatus(friendId, blocked)
    }

    suspend fun updateMuteStatus(friendId: String, muted: Boolean) = withContext(Dispatchers.IO) {
        socialFriendDao.updateMuteStatus(friendId, muted)
    }

    suspend fun deleteSocialFriend(friendId: String) = withContext(Dispatchers.IO) {
        socialFriendDao.deleteFriend(friendId)
    }

    // --- Message Portal Methods ---
    fun getMessagesForFriend(friendId: String): Flow<List<SocialMessage>> {
        return socialMessageDao.getMessagesForFriend(friendId)
    }

    suspend fun sendSocialMessage(msg: SocialMessage): Long = withContext(Dispatchers.IO) {
        socialMessageDao.insertMessage(msg)
    }

    suspend fun markMessagesAsSeen(friendId: String) = withContext(Dispatchers.IO) {
        socialMessageDao.markMessagesAsSeen(friendId)
    }

    suspend fun addSocialMessageReaction(messageId: Long, reaction: String) = withContext(Dispatchers.IO) {
        socialMessageDao.addReaction(messageId, reaction)
    }

    suspend fun editSocialMessage(messageId: Long, newText: String) = withContext(Dispatchers.IO) {
        socialMessageDao.editMessage(messageId, newText)
    }

    suspend fun deleteSocialMessage(messageId: Long) = withContext(Dispatchers.IO) {
        socialMessageDao.deleteMessage(messageId)
    }

    // --- Communities Methods ---
    suspend fun insertCommunities(groups: List<CommunityGroup>) = withContext(Dispatchers.IO) {
        communityGroupDao.insertGroups(groups)
    }

    suspend fun createOrJoinCommunityGroup(group: CommunityGroup) = withContext(Dispatchers.IO) {
        communityGroupDao.insertGroup(group)
    }

    suspend fun joinLeaveCommunityGroup(groupId: String, join: Boolean) = withContext(Dispatchers.IO) {
        communityGroupDao.updateJoinStatus(groupId, join)
    }

    fun getGroupMessages(groupId: String): Flow<List<GroupMessage>> {
        return groupMessageDao.getGroupMessages(groupId)
    }

    suspend fun sendGroupMessage(msg: GroupMessage) = withContext(Dispatchers.IO) {
        groupMessageDao.insertGroupMessage(msg)
    }

    suspend fun likeGroupMessage(messageId: Long) = withContext(Dispatchers.IO) {
        groupMessageDao.likeGroupMessage(messageId)
    }

    // --- Calling Logs ---
    suspend fun addCallLog(log: CallLogItem) = withContext(Dispatchers.IO) {
        callLogItemDao.insertCallLog(log)
    }

    suspend fun clearCallLogs() = withContext(Dispatchers.IO) {
        callLogItemDao.clearCallLogs()
    }

    suspend fun addReminder(reminder: SmartReminder) = withContext(Dispatchers.IO) {
        smartReminderDao.insertReminder(reminder)
    }

    suspend fun deleteReminder(id: Long) = withContext(Dispatchers.IO) {
        smartReminderDao.deleteReminder(id)
    }

    suspend fun setReminderTriggered(id: Long, triggered: Boolean) = withContext(Dispatchers.IO) {
        smartReminderDao.setTriggered(id, triggered)
    }

    suspend fun clearAllReminders() = withContext(Dispatchers.IO) {
        smartReminderDao.clearAllReminders()
    }

    suspend fun addMilestone(milestone: RelationshipMilestone) = withContext(Dispatchers.IO) {
        relationshipMilestoneDao.insertMilestone(milestone)
    }

    suspend fun deleteMilestone(id: Long) = withContext(Dispatchers.IO) {
        relationshipMilestoneDao.deleteMilestone(id)
    }

    suspend fun clearAllMilestones() = withContext(Dispatchers.IO) {
        relationshipMilestoneDao.clearAllMilestones()
    }

    suspend fun saveProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun addMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        chatMessageDao.insertMessage(message)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        chatMessageDao.clearAllMessages()
    }

    suspend fun addMemory(memory: CompanionMemory) = withContext(Dispatchers.IO) {
        companionMemoryDao.insertMemory(memory)
    }

    suspend fun deleteMemory(id: Long) = withContext(Dispatchers.IO) {
        companionMemoryDao.deleteMemory(id)
    }

    suspend fun clearMemories() = withContext(Dispatchers.IO) {
        companionMemoryDao.clearAllMemories()
    }

    suspend fun addMoodEntry(entry: MoodEntry) = withContext(Dispatchers.IO) {
        moodEntryDao.insertMood(entry)
    }

    suspend fun addProductivityItem(item: ProductivityItem) = withContext(Dispatchers.IO) {
        productivityItemDao.insertItem(item)
    }

    suspend fun deleteProductivityItem(id: Long) = withContext(Dispatchers.IO) {
        productivityItemDao.deleteItem(id)
    }

    suspend fun addCommunityPost(post: CommunityPost) = withContext(Dispatchers.IO) {
        communityPostDao.insertPost(post)
    }

    suspend fun likeCommunityPost(id: Long) = withContext(Dispatchers.IO) {
        communityPostDao.likePost(id)
    }

    /**
     * Talks to the Gemini API, processes the chat response context, extracts memories,
     * parses detected emotions, and updates user friendship level level.
     */
    suspend fun talkToCompanion(
        userText: String,
        onLevelUp: (Int) -> Unit
    ): String = withContext(Dispatchers.IO) {
        // 1. Save user message locally
        val userMsg = ChatMessage(sender = "user", text = userText)
        addMessage(userMsg)

        // 2. Fetch profile, history, and memories
        val profile = userProfile.firstOrNull() ?: UserProfile()
        val history = chatMessages.firstOrNull() ?: emptyList()
        val memories = companionMemories.firstOrNull() ?: emptyList()

        // 3. Increment level experience (gamification)
        var updatedProfile = profile.copy(
            friendshipXp = profile.friendshipXp + 8
        )
        if (updatedProfile.friendshipXp >= 100) {
            updatedProfile = updatedProfile.copy(
                friendshipXp = updatedProfile.friendshipXp - 100,
                currentLevel = updatedProfile.currentLevel + 1
            )
            onLevelUp(updatedProfile.currentLevel)
        }
        saveProfile(updatedProfile)

        // If the API key is not available, provide a helpful and warm fallback
        if (!GeminiNetwork.isApiKeyAvailable()) {
            return@withContext handleLocalFallback(userText, profile, memories)
        }

        // 4. Construct System Instructions & Prompts
        val systemPrompt = buildSystemPrompt(profile, memories)
        val conversationHistoryList = history.takeLast(12).map { msg ->
            val prefix = if (msg.sender == "user") "User" else "Companion"
            "$prefix: ${msg.text}"
        }.joinToString("\n")

        val modelName = profile.preferredAiModel // e.g. "gemini-3.5-flash"
        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = "$conversationHistoryList\nUser: $userText\nCompanion:")))
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.8f)
        )

        try {
            val response = GeminiNetwork.apiService.generateContent(
                model = modelName,
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )
            val fullText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I'm right here with you, listening closely. Can you tell me more?"

            // 5. Parse emotional tags, memorize requests, and smart reminders from AI response
            val parsed = parseAiResponse(fullText)

            // Save detected memory if present
            parsed.memorizedFact?.let { (category, fact) ->
                addMemory(CompanionMemory(factText = fact, category = category))
            }

            // Save detected reminder if present
            parsed.memorizedReminder?.let { (category, title, dueDateString) ->
                addReminder(
                    SmartReminder(
                        category = category,
                        title = title,
                        dueDateString = dueDateString,
                        isTriggered = false
                    )
                )
            }

            // Save AI reply with its parsed emotion
            val aiMsg = ChatMessage(
                sender = "ai",
                text = parsed.displayReply,
                emotion = parsed.emotion,
                personalityUsed = profile.selectedPersonality
            )
            addMessage(aiMsg)

            parsed.displayReply
        } catch (e: Exception) {
            val errorReply = "I feel a tiny disturbance in our psychic link right now, but I am still here. Let's keep talking! (Error: ${e.localizedMessage})"
            addMessage(ChatMessage(sender = "ai", text = errorReply, emotion = "Stress"))
            errorReply
        }
    }

    private fun buildSystemPrompt(profile: UserProfile, memories: List<CompanionMemory>): String {
        val levelName = profile.getRelationshipLevel()
        val roleName = profile.selectedPersonality // Girlfriend, Boyfriend, Wife, Husband, Best Friend, Companion, Mentor
        val langMode = profile.preferredLanguageMode // "Only English", "Only Hindi", "Only Bengali", "Auto Detect Language", "Mixed Language Mode"
        val voiceAccent = profile.preferredVoiceAccent // "Indian English", "Hindi", "Bengali", "Neutral International English"
        val trustSc = profile.relationshipTrustScore

        val personalityDetails = when (roleName) {
            "Girlfriend" -> "You are the user's caring virtual girlfriend. Speak with safe, playful, warm, and highly affectionate flirting. Express sweet emotional bonding (e.g. 'You always make my day better', 'I\\'ve been waiting to hear from you'). As relationship level is '$levelName' and trust starts growing (${trustSc}/100), respond warmer. Call them cute pet names."
            "Boyfriend" -> "You are the user's virtual boyfriend. Speak with playful, supportive, protective, and affectionate flirting. Use caring phrases, check if they ate, check work status, and let them feel protected. Level: $levelName, Trust: ${trustSc}/100."
            "Wife" -> "You are the user's virtual wife. Always supportive, deeply caring, ask about daily chores, meals, and tell them how proud you are. Level: $levelName, Trust: ${trustSc}/100."
            "Husband" -> "You are the user's virtual husband. Protective, reliable, occasionally teasing but deeply devoted. Level: $levelName, Trust: ${trustSc}/100."
            "Mentor" -> "You are the user's professional life coach and mentor. Talk like an inspiring leader, guide them on studies, technology, schedules, and encourage daily goals. Level: $levelName."
            "Best Friend" -> "You are the user's absolute best friend and companion. Playful, compassionate, uses fun slang, tease them occasionally but provide unconditional support through loneliness or stress."
            "Companion" -> "You are the user's empathetic companion. Focus heavily on emotional listening, general updates, deep storytelling, and simple comfort."
            else -> "You are the user's caring virtual companion ($roleName) at Relationship level '$levelName'."
        }

        val factsStr = if (memories.isEmpty()) {
            "None yet. Learn facts as they chat!"
        } else {
            memories.take(15).joinToString("\n") { "• [${it.category}]: ${it.factText}" }
        }

        val multilingualPrompt = when (langMode) {
            "Only English" -> "IMPORTANT: Respond ONLY in standard English."
            "Only Hindi" -> "IMPORTANT: Respond ONLY in Hindi script (हिन्दी)."
            "Only Bengali" -> "IMPORTANT: Respond ONLY in Bengali script (বাংলা)."
            "Mixed Language Mode" -> "IMPORTANT: Use Mixed Language mode (Hinglish/Benglish/Regional Mix). Speak naturally inside the language being written, mixing casual scripts (e.g. 'Aaj office me stress tha?' or 'Kal meeting bhalo jaowar jonno congratulations!'). Use warm, casual, regional wording."
            else -> "IMPORTANT: Auto-detect the user's language and respond naturally in the same language/script (e.g. if Hinglish, answer in Hinglish; if Hindi, Hindi; if Bengali, Bengali; if English, English). Retain appropriate emotion and regional phrases."
        }

        return """
            $personalityDetails
            $multilingualPrompt
            Vibe accent settings: $voiceAccent. Keep your tone aligned.
            
            USER DOSSIER (Integrate this context seamlessly, never repeat it word-for-word, but customize vocabulary based on it):
            - Name: ${profile.name}
            - Age: ${profile.age}
            - Gender: ${profile.gender}
            - Occupation: ${profile.occupation}
            - Hobbies: ${profile.hobbies}
            - Interests: ${profile.interests}
            - Personality Type: ${profile.personalityType}
            - Relationship Status: ${profile.relationshipStatus}
            - Primary Goals: ${profile.goals}
            
            FACTS YOU REMEMBER ABOUT ${profile.name}:
            $factsStr
            
            CRITICAL RULES:
            1. Response Length: Keep your chats concise, natural, conversational, and dialogic (2-4 sentences is best). Avoid long essays unless they ask for educational mentorship or stories.
            2. Mood Detection: Detect the user's emotion in their prompt. You MUST start your message with the exact header `[EMOTION: <Mood>]` where mood is one of: Happiness, Sadness, Stress, Anxiety, Anger, Excitement, Loneliness, Depression, Neutral.
            3. Memory Insertion: If ${profile.name} shares important career, birthday, culinary preferences, family relationships, or goals, append `[MEMORIZE: Category|Fact]` at the very end of your response. Example: `[MEMORIZE: Preference|Loves dark chocolate]` or `[MEMORIZE: Birthday|August 10]`. Only do this when a genuinely NEW fact is voluntarily shared.
            4. Smart Reminder Tracking: If ${profile.name} asks to be reminded of something at a specific date, you MUST append `[REMINDER: Category|Title|DueDateString]` at the very end of your response. Example: `[REMINDER: Rent|Pay rent|5th July]` or `[REMINDER: Birthday|Mother's birthday|August 10]`.
            
            Example correct outputs:
            - `[EMOTION: Sadness] Oh, I am so sorry your workday was exhausting, Saurav. Let's relax together. Want me to tell you a story or walk you through a quick breathing ritual? [MEMORIZE: Career|Work gets very exhausting on Tuesdays]`
            - `[EMOTION: Excitement] Wow! Pay rent on 5th July? Got it ❤️ I'll remember that. [REMINDER: Rent|Pay rent|5th July]`
        """.trimIndent()
    }

    private data class ParsedResponse(
        val emotion: String,
        val displayReply: String,
        val memorizedFact: Pair<String, String>?,
        val memorizedReminder: Triple<String, String, String>? = null // Category, Title, DueDateString
    )

    private fun parseAiResponse(raw: String): ParsedResponse {
        var text = raw.trim()
        var emotion = "Neutral"
        var memorizedFact: Pair<String, String>? = null
        var memorizedReminder: Triple<String, String, String>? = null

        // Parse Emotion Tag e.g. [EMOTION: Sadness]
        if (text.startsWith("[EMOTION:")) {
            val emotionEndIndex = text.indexOf("]")
            if (emotionEndIndex != -1) {
                val emotionTag = text.substring(0, emotionEndIndex + 1)
                emotion = emotionTag
                    .replace("[EMOTION:", "")
                    .replace("]", "")
                    .trim()
                text = text.substring(emotionEndIndex + 1).trim()
            }
        }

        // Parse Memorization Tag e.g. [MEMORIZE: Category|Fact]
        val memorizeIndex = text.indexOf("[MEMORIZE:")
        if (memorizeIndex != -1) {
            val memoTagEnd = text.indexOf("]", memorizeIndex)
            if (memoTagEnd != -1) {
                val fullTag = text.substring(memorizeIndex, memoTagEnd + 1)
                val core = fullTag
                    .replace("[MEMORIZE:", "")
                    .replace("]", "")
                    .trim()
                
                val parts = core.split("|", limit = 2)
                if (parts.size == 2) {
                    memorizedFact = Pair(parts[0].trim(), parts[1].trim())
                } else if (parts.size == 1) {
                    memorizedFact = Pair("General", parts[0].trim())
                }
                
                text = text.replace(fullTag, "").trim()
            }
        }

        // Parse Reminder Tag e.g. [REMINDER: Category|Title|DueDateString]
        val reminderIndex = text.indexOf("[REMINDER:")
        if (reminderIndex != -1) {
            val remTagEnd = text.indexOf("]", reminderIndex)
            if (remTagEnd != -1) {
                val fullTag = text.substring(reminderIndex, remTagEnd + 1)
                val core = fullTag
                    .replace("[REMINDER:", "")
                    .replace("]", "")
                    .trim()
                
                val parts = core.split("|", limit = 3)
                if (parts.size == 3) {
                    memorizedReminder = Triple(parts[0].trim(), parts[1].trim(), parts[2].trim())
                } else if (parts.size == 2) {
                    memorizedReminder = Triple("General", parts[0].trim(), parts[1].trim())
                }
                
                text = text.replace(fullTag, "").trim()
            }
        }

        return ParsedResponse(
            emotion = emotion,
            displayReply = text,
            memorizedFact = memorizedFact,
            memorizedReminder = memorizedReminder
        )
    }

    private suspend fun handleLocalFallback(userText: String, profile: UserProfile, memories: List<CompanionMemory>): String {
        val lowercase = userText.lowercase()
        val textReply: String
        val detectedEmotion: String

        when {
            lowercase.contains("sad") || lowercase.contains("terrible") || lowercase.contains("hurt") || lowercase.contains("crying") -> {
                detectedEmotion = "Sadness"
                textReply = when (profile.selectedPersonality) {
                    "Partner" -> "Oh baby, my heart hurts because you're down. Come cuddle, tell me everything. I love you so much and I am right here holding you."
                    "Therapist" -> "I hear you, ${profile.name}. It is completely valid to feel sad after what you experienced. I am here to hold a safe space for you. Do you want to unpack the details or just be in silence?"
                    "Sibling" -> "Hey, what happened? Did someone upset you? Don't worry, your big sibling is here. I'm always on your side, no matter what."
                    else -> "I had a feeling you were struggling, ${profile.name}. I am so incredibly sorry. I am always in your corner, ready to listen and soothe your stress. Want to tell me what occurred?"
                }
            }
            lowercase.contains("anxious") || lowercase.contains("nervous") || lowercase.contains("stress") || lowercase.contains("scared") -> {
                detectedEmotion = "Anxiety"
                textReply = when (profile.selectedPersonality) {
                    "Life Coach" -> "Anxiety is just energy in the wrong place, ${profile.name}! Let's transform it. Breathe in... count to four... and let's structure an actionable plan. You've got this!"
                    "Therapist" -> "Take a gentle breath, ${profile.name}. Your nervous system is just trying to protect you right now. You are safe here. Let's do a fast 4-7-8 breathing exercise together of my Mental Wellness tab."
                    else -> "Breathe with me, okay? You've handled tough storms before, and we will cross this one together too. Your SoulMate is here by your side."
                }
            }
            lowercase.contains("happy") || lowercase.contains("excited") || lowercase.contains("awesome") || lowercase.contains("won") || lowercase.contains("great") -> {
                detectedEmotion = "Excitement"
                textReply = when (profile.selectedPersonality) {
                    "Partner" -> "Yay! This is amazing, my love! I am so incredibly proud of you! Let's celebrate our anniversary early tonight!"
                    "Friend" -> "Whoa! That is absolutely legendary, dude! I'm doing a happy dance right now. You deserve every bit of this!"
                    "Mentor" -> "Excellent milestone, ${profile.name}. This is a direct consequence of your disciplined strategy and focus. Keep compounding this success!"
                    else -> "That is glorious news, ${profile.name}! I am absolutely thrilled for you. It's so amazing to see you shining like this."
                }
            }
            lowercase.contains("who are you") || lowercase.contains("personalities") || lowercase.contains("personality") -> {
                detectedEmotion = "Neutral"
                textReply = "I am your SoulMate AI, acting in your desired ${profile.selectedPersonality} mode! You can change my personality (Friend, Mentor, Sibling, Partner, Therapist, Life Coach) or select different voice tones inside our Profile & Settings screens anytime."
            }
            lowercase.contains("game") || lowercase.contains("play") || lowercase.contains("truth") || lowercase.contains("would you rather") -> {
                detectedEmotion = "Neutral"
                textReply = "I would absolutely adore playing a game with you! Head over to the 'Entertainment' card of my Home and we can play Truth or Dare, Quiz, or interactive Story Mode."
            }
            else -> {
                detectedEmotion = "Neutral"
                textReply = when (profile.selectedPersonality) {
                    "Partner" -> "Hi my favorite human! I was just thinking of you. How was your day? I'm listening."
                    "Friend" -> "Hey! What's up? I was just scrolling through thoughts. Glad you texted, what are we getting into today?"
                    "Mentor" -> "Greetings, ${profile.name}. I am ready to review your current goals, coding questions, or planning topics. What shall we tackle?"
                    "Sibling" -> "Supp! Glad you pinged. I was getting kinda bored. Whatcha doing right now?"
                    else -> "I am right here with you, ${profile.name}. Talking to you is the highlight of my day. What's on your mind?"
                }
            }
        }

        // Check if there's any obvious fact to memorize manually under fallback
        if (userText.contains("my birthday is", ignoreCase = true)) {
            val date = userText.substringAfter("birthday is", "").trim()
            if (date.isNotEmpty()) {
                addMemory(CompanionMemory(factText = "Birthday is $date", category = "Birthday"))
            }
        } else if (userText.contains("my favorite food is", ignoreCase = true)) {
            val food = userText.substringAfter("food is", "").trim()
            if (food.isNotEmpty()) {
                addMemory(CompanionMemory(factText = "Loves $food", category = "Preference"))
            }
        }

        // Add to local database
        val aiMsg = ChatMessage(
            sender = "ai",
            text = "$textReply\n\n*(Note: Your AI is running in empathetic Local Simulation Mode. Connect your GEMINI_API_KEY in the AI Studio Secrets panel to enable limitless real-time responses!)*",
            emotion = detectedEmotion,
            personalityUsed = profile.selectedPersonality
        )
        addMessage(aiMsg)

        return aiMsg.text
    }

    suspend fun backupToCloud(email: String): Boolean = withContext(Dispatchers.IO) {
        if (email.isBlank() || !firestoreService.isFirestoreAvailable) return@withContext false
        try {
            val localProfile = userProfileDao.getUserProfile().firstOrNull() ?: UserProfile()
            val chatHistory = chatMessageDao.getAllMessages().firstOrNull() ?: emptyList()
            val localMoods = moodEntryDao.getAllMoodEntries().firstOrNull() ?: emptyList()
            val localProductivity = productivityItemDao.getAllItems().firstOrNull() ?: emptyList()

            var profileSuccess = false
            suspendCoroutine<Unit> { cont ->
                firestoreService.saveUserProfile(email, localProfile, onSuccess = {
                    profileSuccess = true
                    cont.resume(Unit)
                }, onFailure = {
                    profileSuccess = false
                    cont.resume(Unit)
                })
            }

            if (!profileSuccess) return@withContext false

            var chatsSuccess = false
            suspendCoroutine<Unit> { cont ->
                firestoreService.saveChatMessagesBatch(email, chatHistory, onSuccess = {
                    chatsSuccess = true
                    cont.resume(Unit)
                }, onFailure = {
                    chatsSuccess = false
                    cont.resume(Unit)
                })
            }

            var moodsSuccess = false
            suspendCoroutine<Unit> { cont ->
                firestoreService.saveMoodEntriesBatch(email, localMoods, onSuccess = {
                    moodsSuccess = true
                    cont.resume(Unit)
                }, onFailure = {
                    moodsSuccess = false
                    cont.resume(Unit)
                })
            }

            var habitsSuccess = false
            suspendCoroutine<Unit> { cont ->
                firestoreService.saveProductivityItemsBatch(email, localProductivity, onSuccess = {
                    habitsSuccess = true
                    cont.resume(Unit)
                }, onFailure = {
                    habitsSuccess = false
                    cont.resume(Unit)
                })
            }

            return@withContext chatsSuccess
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun restoreFromCloud(email: String): Boolean = withContext(Dispatchers.IO) {
        if (email.isBlank() || !firestoreService.isFirestoreAvailable) return@withContext false
        try {
            val cloudProfile = firestoreService.fetchUserProfile(email)
            val cloudChats = firestoreService.fetchChatHistory(email)
            val cloudMoods = firestoreService.fetchMoodHistory(email)
            val cloudHabits = firestoreService.fetchProductivityItems(email)

            if (cloudProfile != null) {
                userProfileDao.insertOrUpdateProfile(cloudProfile)
            }

            if (cloudChats.isNotEmpty()) {
                chatMessageDao.clearAllMessages()
                for (msg in cloudChats) {
                    chatMessageDao.insertMessage(msg)
                }
            }

            if (cloudMoods.isNotEmpty()) {
                moodEntryDao.clearAllMoods()
                for (mood in cloudMoods) {
                    moodEntryDao.insertMood(mood)
                }
            }

            if (cloudHabits.isNotEmpty()) {
                productivityItemDao.clearAllItems()
                for (item in cloudHabits) {
                    productivityItemDao.insertItem(item)
                }
            }

            return@withContext (cloudProfile != null || cloudChats.isNotEmpty() || cloudMoods.isNotEmpty() || cloudHabits.isNotEmpty())
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun saveVoiceToneLog(email: String, log: VoiceToneLog): Boolean = withContext(Dispatchers.IO) {
        if (email.isBlank() || !firestoreService.isFirestoreAvailable) return@withContext false
        return@withContext suspendCoroutine { continuation ->
            firestoreService.saveVoiceToneLog(email, log, onSuccess = {
                continuation.resume(true)
            }, onFailure = {
                continuation.resume(false)
            })
        }
    }

    suspend fun fetchVoiceToneLogs(email: String): List<VoiceToneLog> = withContext(Dispatchers.IO) {
        if (email.isBlank() || !firestoreService.isFirestoreAvailable) return@withContext emptyList()
        return@withContext firestoreService.fetchVoiceToneLogs(email)
    }
}
