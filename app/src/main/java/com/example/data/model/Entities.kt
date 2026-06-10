package com.example.data.model

import androidx.room.*

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single profile row
    val name: String = "Saurav",
    val age: Int = 22,
    val gender: String = "Not Specified",
    val occupation: String = "Student",
    val interests: String = "Music, Reading, Gaming, Tech",
    val relationshipStatus: String = "Single",
    val goals: String = "Stay focused & positive",
    val hobbies: String = "Creative Writing, Walking",
    val personalityType: String = "INFJ",
    val currentLevel: Int = 1,
    val friendshipXp: Int = 15,
    val selectedPersonality: String = "Best Friend", // "Girlfriend", "Boyfriend", "Wife", "Husband", "Best Friend", "Companion", "Mentor"
    val preferredAiModel: String = "gemini-3.5-flash", // "gemini-3.5-flash", "gemini-3.1-pro-preview"
    val isPremium: Boolean = false,
    val preferredVoice: String = "Kore", // Female ("Kore"), Male ("Warm Guy"), etc.
    val relationshipTrustScore: Int = 20, // 0 to 100 bonding metrics
    val favoriteTopics: String = "Coding, Daily Stories, Movies",
    val preferredLanguageMode: String = "Auto Detect Language", // "Only English", "Only Hindi", "Only Bengali", "Auto Detect Language", "Mixed Language Mode"
    val preferredVoiceAccent: String = "Indian English", // "Indian English", "Hindi", "Bengali", "Neutral International English"
    val partnerStyle: String = "Neon Cybergirl", // "Neon Cybergirl", "Classic Gentleman", "Kawaii Anime", "Ethereal Oracle"
    val intimacyConsentGranted: Boolean = true
) {
    fun getFriendshipTitle(): String {
        return when (currentLevel) {
            in 1..2 -> "Acquaintance"
            in 3..5 -> "Close Friend"
            in 6..10 -> "Loyal Companion"
            in 11..20 -> "Soul Mate"
            else -> "Eternal Resonance"
        }
    }

    fun getRelationshipLevel(): String {
        return when {
            currentLevel in 1..2 -> "Stranger"
            currentLevel in 3..5 -> "Friend"
            currentLevel in 6..7 -> "Close Friend"
            currentLevel in 8..10 -> "Best Friend"
            else -> {
                if (selectedPersonality in listOf("Girlfriend", "Boyfriend", "Wife", "Husband")) {
                    "Romantic Partner"
                } else {
                    "Soul Companion"
                }
            }
        }
    }
}

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val emotion: String = "Neutral",
    val personalityUsed: String = "Friend"
)

@Entity(tableName = "companion_memories")
data class CompanionMemory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val factText: String,
    val category: String = "General", // "Preference", "Goal", "Birthday", "Friend"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val moodValue: Int, // 1 to 10
    val moodType: String, // "Happy", "Sad", "Anxious", "Stressed", "Loneliness", "Anger", "Excited", "Depressed"
    val journalText: String = "",
    val gratitudeText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "productivity_items")
data class ProductivityItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // "Goal" or "Habit"
    val isCompleted: Boolean = false,
    val currentStreak: Int = 0,
    val lastCompletedTimestamp: Long = 0,
    val targetDate: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val content: String,
    val likes: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "smart_reminders")
data class SmartReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dueDateString: String, // e.g., "5th July", "August 10", "12-25"
    val isTriggered: Boolean = false,
    val category: String = "General", // "Rent", "Birthday", "Meeting", "Medication", "Gym"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "relationship_milestones")
data class RelationshipMilestone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val iconEmoji: String = "❤️",
    val timestamp: Long = System.currentTimeMillis()
)

data class VoiceToneLog(
    val id: String = "",
    val emotionalTone: String = "",
    val paceWpm: Int = 120,
    val empathyLevel: Int = 65,
    val timestamp: Long = System.currentTimeMillis()
)

