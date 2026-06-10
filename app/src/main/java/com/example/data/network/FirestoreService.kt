package com.example.data.network

import android.content.Context
import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.UserProfile
import com.example.data.model.MoodEntry
import com.example.data.model.ProductivityItem
import com.example.data.model.VoiceToneLog
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirestoreService(private val context: Context) {
    private var firestore: FirebaseFirestore? = null
    var isFirestoreAvailable: Boolean = false
        private set

    init {
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setProjectId("soulmate-companion-ai")
                    .setApplicationId("1:555555555555:android:feedbeeffeedbeef")
                    .setApiKey("placeholder-key-for-soulmate-firestore")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            firestore = FirebaseFirestore.getInstance()
            isFirestoreAvailable = true
            Log.d("FirestoreService", "Firestore successfully initialized.")
        } catch (e: Exception) {
            Log.e("FirestoreService", "Unable to initialize Firestore. Operating in local-only fallback style.", e)
            isFirestoreAvailable = false
        }
    }

    fun saveUserProfile(email: String, profile: UserProfile, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        if (!isFirestoreAvailable || email.isBlank()) {
            onFailure(IllegalStateException("Firestore is not initialized or email is empty."))
            return
        }
        val userMap = hashMapOf(
            "name" to profile.name,
            "age" to profile.age,
            "gender" to profile.gender,
            "occupation" to profile.occupation,
            "interests" to profile.interests,
            "relationshipStatus" to profile.relationshipStatus,
            "goals" to profile.goals,
            "hobbies" to profile.hobbies,
            "personalityType" to profile.personalityType,
            "selectedPersonality" to profile.selectedPersonality,
            "preferredAiModel" to profile.preferredAiModel,
            "preferredVoice" to profile.preferredVoice,
            "currentLevel" to profile.currentLevel,
            "friendshipXp" to profile.friendshipXp,
            "isPremium" to profile.isPremium,
            "lastSynced" to System.currentTimeMillis()
        )
        firestore?.collection("users")?.document(email)?.set(userMap)
            ?.addOnSuccessListener {
                onSuccess()
            }
            ?.addOnFailureListener { e ->
                Log.e("FirestoreService", "Failed to upload Profile", e)
                onFailure(e)
            }
    }

    suspend fun fetchUserProfile(email: String): UserProfile? = suspendCancellableCoroutine { continuation ->
        if (!isFirestoreAvailable || email.isBlank()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        firestore?.collection("users")?.document(email)?.get()
            ?.addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val profile = UserProfile(
                        name = doc.getString("name") ?: "Taylor",
                        age = doc.getLong("age")?.toInt() ?: 22,
                        gender = doc.getString("gender") ?: "Not Specified",
                        occupation = doc.getString("occupation") ?: "Student",
                        interests = doc.getString("interests") ?: "Music, Reading, Gaming",
                        relationshipStatus = doc.getString("relationshipStatus") ?: "Single",
                        goals = doc.getString("goals") ?: "Stay focused & positive",
                        hobbies = doc.getString("hobbies") ?: "Creative Writing, Walking",
                        personalityType = doc.getString("personalityType") ?: "INFJ",
                        currentLevel = doc.getLong("currentLevel")?.toInt() ?: 1,
                        friendshipXp = doc.getLong("friendshipXp")?.toInt() ?: 15,
                        selectedPersonality = doc.getString("selectedPersonality") ?: "Friend",
                        preferredAiModel = doc.getString("preferredAiModel") ?: "gemini-3.5-flash",
                        preferredVoice = doc.getString("preferredVoice") ?: "Kore",
                        isPremium = doc.getBoolean("isPremium") ?: false
                    )
                    continuation.resume(profile)
                } else {
                    continuation.resume(null)
                }
            }
            ?.addOnFailureListener { e ->
                Log.e("FirestoreService", "Failed to fetch Profile from Firestore", e)
                continuation.resume(null)
            } ?: continuation.resume(null)
    }

    fun saveChatMessage(email: String, message: ChatMessage, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        if (!isFirestoreAvailable || email.isBlank()) {
            onFailure(IllegalStateException("Firestore is not available."))
            return
        }
        val msgMap = hashMapOf(
            "id" to message.id,
            "sender" to message.sender,
            "text" to message.text,
            "timestamp" to message.timestamp,
            "emotion" to message.emotion,
            "personalityUsed" to message.personalityUsed
        )
        firestore?.collection("users")?.document(email)
            ?.collection("chats")?.add(msgMap)
            ?.addOnSuccessListener {
                onSuccess()
            }
            ?.addOnFailureListener { e ->
                Log.e("FirestoreService", "Failed to save message to Firestore", e)
                onFailure(e)
            }
    }

    fun saveChatMessagesBatch(email: String, messages: List<ChatMessage>, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        if (!isFirestoreAvailable || email.isBlank() || messages.isEmpty()) {
            onSuccess()
            return
        }
        val batch = firestore?.batch() ?: return
        val userChatsRef = firestore?.collection("users")?.document(email)?.collection("chats") ?: return

        messages.takeLast(100).forEach { message ->
            val docRef = userChatsRef.document(if (message.id > 0) message.id.toString() else java.util.UUID.randomUUID().toString())
            val msgMap = hashMapOf(
                "id" to message.id,
                "sender" to message.sender,
                "text" to message.text,
                "timestamp" to message.timestamp,
                "emotion" to message.emotion,
                "personalityUsed" to message.personalityUsed
            )
            batch.set(docRef, msgMap)
        }

        batch.commit()
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { e -> onFailure(e) }
    }

    suspend fun fetchChatHistory(email: String): List<ChatMessage> = suspendCancellableCoroutine { continuation ->
        if (!isFirestoreAvailable || email.isBlank()) {
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }
        firestore?.collection("users")?.document(email)
            ?.collection("chats")?.orderBy("timestamp")?.get()
            ?.addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = ArrayList<ChatMessage>()
                    for (doc in snapshot.documents) {
                        val msg = ChatMessage(
                            id = doc.getLong("id") ?: 0L,
                            sender = doc.getString("sender") ?: "user",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            emotion = doc.getString("emotion") ?: "Neutral",
                            personalityUsed = doc.getString("personalityUsed") ?: "Friend"
                        )
                        messages.add(msg)
                    }
                    continuation.resume(messages)
                } else {
                    continuation.resume(emptyList())
                }
            }
            ?.addOnFailureListener { e ->
                Log.e("FirestoreService", "Failed to fetch chat log from Firestore", e)
                continuation.resume(emptyList())
            } ?: continuation.resume(emptyList())
    }

    fun saveMoodEntry(email: String, mood: MoodEntry, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        if (!isFirestoreAvailable || email.isBlank()) {
            onFailure(IllegalStateException("Firestore is not initialized or email is empty."))
            return
        }
        val moodMap = hashMapOf(
            "id" to mood.id,
            "moodValue" to mood.moodValue,
            "moodType" to mood.moodType,
            "journalText" to mood.journalText,
            "gratitudeText" to mood.gratitudeText,
            "timestamp" to mood.timestamp
        )
        firestore?.collection("users")?.document(email)
            ?.collection("moods")?.document(mood.timestamp.toString())?.set(moodMap)
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { e -> onFailure(e) }
    }

    fun saveMoodEntriesBatch(email: String, moods: List<MoodEntry>, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        if (!isFirestoreAvailable || email.isBlank() || moods.isEmpty()) {
            onSuccess()
            return
        }
        val batch = firestore?.batch() ?: return
        val userMoodsRef = firestore?.collection("users")?.document(email)?.collection("moods") ?: return

        moods.forEach { mood ->
            val docRef = userMoodsRef.document(mood.timestamp.toString())
            val moodMap = hashMapOf(
                "id" to mood.id,
                "moodValue" to mood.moodValue,
                "moodType" to mood.moodType,
                "journalText" to mood.journalText,
                "gratitudeText" to mood.gratitudeText,
                "timestamp" to mood.timestamp
            )
            batch.set(docRef, moodMap)
        }

        batch.commit()
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { e -> onFailure(e) }
    }

    suspend fun fetchMoodHistory(email: String): List<MoodEntry> = suspendCancellableCoroutine { continuation ->
        if (!isFirestoreAvailable || email.isBlank()) {
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }
        firestore?.collection("users")?.document(email)
            ?.collection("moods")?.orderBy("timestamp")?.get()
            ?.addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val moodList = ArrayList<MoodEntry>()
                    for (doc in snapshot.documents) {
                        val mood = MoodEntry(
                            id = doc.getLong("id")?.toInt() ?: 0,
                            moodValue = doc.getLong("moodValue")?.toInt() ?: 5,
                            moodType = doc.getString("moodType") ?: "Neutral",
                            journalText = doc.getString("journalText") ?: "",
                            gratitudeText = doc.getString("gratitudeText") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                        moodList.add(mood)
                    }
                    continuation.resume(moodList)
                } else {
                    continuation.resume(emptyList())
                }
            }
            ?.addOnFailureListener { e ->
                Log.e("FirestoreService", "Failed to fetch mood log from Firestore", e)
                continuation.resume(emptyList())
            } ?: continuation.resume(emptyList())
    }

    fun saveProductivityItem(email: String, item: ProductivityItem, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        if (!isFirestoreAvailable || email.isBlank()) {
            onFailure(IllegalStateException("Firestore is not initialized or email is empty."))
            return
        }
        val itemMap = hashMapOf(
            "id" to item.id,
            "title" to item.title,
            "type" to item.type,
            "isCompleted" to item.isCompleted,
            "currentStreak" to item.currentStreak,
            "lastCompletedTimestamp" to item.lastCompletedTimestamp,
            "targetDate" to item.targetDate,
            "timestamp" to item.timestamp
        )
        firestore?.collection("users")?.document(email)
            ?.collection("habits")?.document(item.timestamp.toString())?.set(itemMap)
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { e -> onFailure(e) }
    }

    fun saveProductivityItemsBatch(email: String, items: List<ProductivityItem>, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        if (!isFirestoreAvailable || email.isBlank() || items.isEmpty()) {
            onSuccess()
            return
        }
        val batch = firestore?.batch() ?: return
        val userHabitsRef = firestore?.collection("users")?.document(email)?.collection("habits") ?: return

        items.forEach { item ->
            val docRef = userHabitsRef.document(item.timestamp.toString())
            val itemMap = hashMapOf(
                "id" to item.id,
                "title" to item.title,
                "type" to item.type,
                "isCompleted" to item.isCompleted,
                "currentStreak" to item.currentStreak,
                "lastCompletedTimestamp" to item.lastCompletedTimestamp,
                "targetDate" to item.targetDate,
                "timestamp" to item.timestamp
            )
            batch.set(docRef, itemMap)
        }

        batch.commit()
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { e -> onFailure(e) }
    }

    suspend fun fetchProductivityItems(email: String): List<ProductivityItem> = suspendCancellableCoroutine { continuation ->
        if (!isFirestoreAvailable || email.isBlank()) {
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }
        firestore?.collection("users")?.document(email)
            ?.collection("habits")?.orderBy("timestamp")?.get()
            ?.addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val habitList = ArrayList<ProductivityItem>()
                    for (doc in snapshot.documents) {
                        val item = ProductivityItem(
                            id = doc.getLong("id") ?: 0L,
                            title = doc.getString("title") ?: "",
                            type = doc.getString("type") ?: "Habit",
                            isCompleted = doc.getBoolean("isCompleted") ?: false,
                            currentStreak = doc.getLong("currentStreak")?.toInt() ?: 0,
                            lastCompletedTimestamp = doc.getLong("lastCompletedTimestamp") ?: 0L,
                            targetDate = doc.getString("targetDate") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                        habitList.add(item)
                    }
                    continuation.resume(habitList)
                } else {
                    continuation.resume(emptyList())
                }
            }
            ?.addOnFailureListener { e ->
                Log.e("FirestoreService", "Failed to fetch habit log from Firestore", e)
                continuation.resume(emptyList())
            } ?: continuation.resume(emptyList())
    }

    fun saveVoiceToneLog(email: String, log: VoiceToneLog, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        if (!isFirestoreAvailable || email.isBlank()) {
            onFailure(IllegalStateException("Firestore is not initialized or email is empty."))
            return
        }
        val logMap = hashMapOf(
            "id" to log.id,
            "emotionalTone" to log.emotionalTone,
            "paceWpm" to log.paceWpm,
            "empathyLevel" to log.empathyLevel,
            "timestamp" to log.timestamp
        )
        firestore?.collection("users")?.document(email)
            ?.collection("voice_logs")?.document(log.timestamp.toString())?.set(logMap)
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { e -> onFailure(e) }
    }

    suspend fun fetchVoiceToneLogs(email: String): List<VoiceToneLog> = suspendCancellableCoroutine { continuation ->
        if (!isFirestoreAvailable || email.isBlank()) {
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }
        firestore?.collection("users")?.document(email)
            ?.collection("voice_logs")?.orderBy("timestamp")?.get()
            ?.addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val logsList = ArrayList<VoiceToneLog>()
                    for (doc in snapshot.documents) {
                        val log = VoiceToneLog(
                            id = doc.getString("id") ?: "",
                            emotionalTone = doc.getString("emotionalTone") ?: "Neutral",
                            paceWpm = doc.getLong("paceWpm")?.toInt() ?: 120,
                            empathyLevel = doc.getLong("empathyLevel")?.toInt() ?: 65,
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                        logsList.add(log)
                    }
                    continuation.resume(logsList)
                } else {
                    continuation.resume(emptyList())
                }
            }
            ?.addOnFailureListener { e ->
                Log.e("FirestoreService", "Failed to fetch voice tone logs from Firestore", e)
                continuation.resume(emptyList())
            } ?: continuation.resume(emptyList())
    }
}
