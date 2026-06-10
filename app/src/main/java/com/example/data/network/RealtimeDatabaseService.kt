package com.example.data.network

import android.content.Context
import android.util.Log
import com.example.data.model.SocialMessage
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.UUID

class RealtimeDatabaseService(private val context: Context) {
    private var database: FirebaseDatabase? = null
    var isDatabaseAvailable: Boolean = false
        private set

    // Safe simulated callback flows for preview purposes
    private val simulatedMessagesMap = HashMap<String, MutableStateFlow<List<SocialMessage>>>()
    private val simulatedTypingMap = HashMap<String, MutableStateFlow<Map<String, Boolean>>>()

    init {
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isEmpty()) {
                Log.w("RealtimeDatabaseService", "FirebaseApp not initialized yet in Context.")
            }
            // Attempt to get the standard instance
            database = FirebaseDatabase.getInstance()
            isDatabaseAvailable = true
            Log.d("RealtimeDatabaseService", "Firebase Realtime Database initialized successfully.")
        } catch (e: Exception) {
            Log.e("RealtimeDatabaseService", "Realtime Database init failure. App will run in resilient simulation mode: ${e.localizedMessage}")
            isDatabaseAvailable = false
        }
    }

    /**
     * Listen dynamically to a stream of chat messages for a specific conversation of [chatId].
     */
    fun listenToMessages(chatId: String): Flow<List<SocialMessage>> {
        if (!isDatabaseAvailable || database == null) {
            Log.d("RealtimeDatabaseService", "Using simulated flow for listenToMessages($chatId)")
            return getSimulatedMessagesFlow(chatId)
        }

        return callbackFlow {
            val messagesRef = database!!.getReference("chats").child(chatId).child("messages")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<SocialMessage>()
                    for (child in snapshot.children) {
                        try {
                            val id = child.child("id").getValue(Long::class.java) ?: child.key?.hashCode()?.toLong() ?: 0L
                            val friendId = child.child("friendId").getValue(String::class.java) ?: chatId
                            val senderId = child.child("senderId").getValue(String::class.java) ?: ""
                            val text = child.child("text").getValue(String::class.java) ?: ""
                            val timestamp = child.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()
                            val seen = child.child("seen").getValue(Boolean::class.java) ?: false
                            val mediaType = child.child("mediaType").getValue(String::class.java) ?: "text"
                            val mediaUrl = child.child("mediaUrl").getValue(String::class.java) ?: ""
                            val replyToId = child.child("replyToId").getValue(Long::class.java) ?: -1L
                            val reactions = child.child("reactions").getValue(String::class.java) ?: ""
                            val isEdited = child.child("isEdited").getValue(Boolean::class.java) ?: false

                            messages.add(
                                SocialMessage(
                                    id = id,
                                    friendId = friendId,
                                    senderId = senderId,
                                    text = text,
                                    timestamp = timestamp,
                                    mediaType = mediaType,
                                    mediaUrl = mediaUrl,
                                    seen = seen,
                                    replyToId = replyToId,
                                    reactions = reactions,
                                    isEdited = isEdited
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("RealtimeDatabaseService", "Error parsing message child", e)
                        }
                    }
                    trySend(messages.sortedBy { it.timestamp })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RealtimeDatabaseService", "listenToMessages Cancelled: ${error.message}")
                }
            }

            messagesRef.addValueEventListener(listener)
            awaitClose { messagesRef.removeEventListener(listener) }
        }
    }

    /**
     * Send a single message to the conversation [chatId] using RTDB.
     */
    fun sendMessage(chatId: String, msg: SocialMessage) {
        if (!isDatabaseAvailable || database == null) {
            Log.d("RealtimeDatabaseService", "Sending message via simulator in chat $chatId")
            val currentList = getSimulatedMessagesFlow(chatId).value.toMutableList()
            val newMsg = msg.copy(id = UUID.randomUUID().hashCode().toLong() and 0xFFFFFFF)
            currentList.add(newMsg)
            getSimulatedMessagesFlow(chatId).value = currentList
            return
        }

        try {
            val messagesRef = database!!.getReference("chats").child(chatId).child("messages")
            val newMsgRef = messagesRef.push()
            val msgKey = newMsgRef.key ?: UUID.randomUUID().toString()
            val finalMsg = if (msg.id == 0L) msg.copy(id = msgKey.hashCode().toLong() and 0xFFFFFFF) else msg

            val msgMap = hashMapOf(
                "id" to finalMsg.id,
                "friendId" to finalMsg.friendId,
                "senderId" to finalMsg.senderId,
                "text" to finalMsg.text,
                "timestamp" to finalMsg.timestamp,
                "seen" to finalMsg.seen,
                "mediaType" to finalMsg.mediaType,
                "mediaUrl" to finalMsg.mediaUrl,
                "replyToId" to finalMsg.replyToId,
                "reactions" to finalMsg.reactions,
                "isEdited" to finalMsg.isEdited
            )

            newMsgRef.setValue(msgMap)
                .addOnFailureListener { e ->
                    Log.e("RealtimeDatabaseService", "Failed to write real-time message", e)
                }
        } catch (e: Exception) {
            Log.e("RealtimeDatabaseService", "RTDB Exception when sending message", e)
        }
    }

    /**
     * Listen dynamically to typing status changes for a specific user in conversation [chatId].
     */
    fun listenToTypingStatus(chatId: String, peerId: String): Flow<Boolean> {
        if (!isDatabaseAvailable || database == null) {
            return callbackFlow {
                val flow = getSimulatedTypingFlow(chatId)
                val collector = launch {
                    flow.collect { map ->
                        trySend(map[peerId] ?: false)
                    }
                }
                awaitClose { collector.cancel() }
            }
        }

        return callbackFlow {
            val typingRef = database!!.getReference("chats").child(chatId).child("typing").child(peerId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isTyping = snapshot.getValue(Boolean::class.java) ?: false
                    trySend(isTyping)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RealtimeDatabaseService", "listenToTypingStatus Cancelled: ${error.message}")
                }
            }
            typingRef.addValueEventListener(listener)
            awaitClose { typingRef.removeEventListener(listener) }
        }
    }

    /**
     * Publish current typing state to RTDB.
     */
    fun setTypingState(chatId: String, userId: String, isTyping: Boolean) {
        if (!isDatabaseAvailable || database == null) {
            val currentMap = getSimulatedTypingFlow(chatId).value.toMutableMap()
            currentMap[userId] = isTyping
            getSimulatedTypingFlow(chatId).value = currentMap
            return
        }

        try {
            val typingRef = database!!.getReference("chats").child(chatId).child("typing").child(userId)
            typingRef.setValue(isTyping)
        } catch (e: Exception) {
            Log.e("RealtimeDatabaseService", "RTDB setTypingState Exception", e)
        }
    }

    /**
     * Update seen status of all unread messages sent by the peer.
     */
    fun markMessagesAsSeen(chatId: String, myId: String) {
        if (!isDatabaseAvailable || database == null) {
            Log.d("RealtimeDatabaseService", "Marking messages seen via simulator in chat $chatId")
            val currentList = getSimulatedMessagesFlow(chatId).value.toMutableList()
            var modified = false
            for (i in currentList.indices) {
                if (currentList[i].senderId != myId && !currentList[i].seen) {
                    currentList[i] = currentList[i].copy(seen = true)
                    modified = true
                }
            }
            if (modified) {
                getSimulatedMessagesFlow(chatId).value = currentList
            }
            return
        }

        try {
            val messagesRef = database!!.getReference("chats").child(chatId).child("messages")
            messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val senderId = child.child("senderId").getValue(String::class.java)
                        val seen = child.child("seen").getValue(Boolean::class.java) ?: false
                        if (senderId != null && senderId != myId && !seen) {
                            child.ref.child("seen").setValue(true)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RealtimeDatabaseService", "markMessagesAsSeen Cancelled: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("RealtimeDatabaseService", "RTDB markMessagesAsSeen Exception", e)
        }
    }

    // Lazy load state flows for in-memory simulated fallback
    private fun getSimulatedMessagesFlow(chatId: String): MutableStateFlow<List<SocialMessage>> {
        synchronized(simulatedMessagesMap) {
            return simulatedMessagesMap.getOrPut(chatId) {
                val demoHistory = listOf(
                    SocialMessage(
                        id = 101L,
                        friendId = chatId,
                        senderId = chatId,
                        text = "Establishing quantum communication freq...",
                        timestamp = System.currentTimeMillis() - 600000,
                        seen = true
                    ),
                    SocialMessage(
                        id = 102L,
                        friendId = chatId,
                        senderId = "me",
                        text = "Hello! Secure frequency bridged and fully active.",
                        timestamp = System.currentTimeMillis() - 300000,
                        seen = true
                    )
                )
                MutableStateFlow(demoHistory)
            }
        }
    }

    private fun getSimulatedTypingFlow(chatId: String): MutableStateFlow<Map<String, Boolean>> {
        synchronized(simulatedTypingMap) {
            return simulatedTypingMap.getOrPut(chatId) {
                MutableStateFlow(mapOf(chatId to false, "me" to false))
            }
        }
    }
}
