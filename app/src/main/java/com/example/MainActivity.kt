package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.data.local.SoulMateDatabase
import com.example.data.repository.SoulMateRepository
import com.example.ui.SoulMateViewModel
import com.example.ui.screens.SoulMateMainNavigation

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = Room.databaseBuilder(
      applicationContext,
      SoulMateDatabase::class.java,
      "soulmate_database"
    ).fallbackToDestructiveMigration().build()

    val firestoreService = com.example.data.network.FirestoreService(applicationContext)
    val rtdbService = com.example.data.network.RealtimeDatabaseService(applicationContext)

    val repository = SoulMateRepository(
      userProfileDao = database.userProfileDao(),
      chatMessageDao = database.chatMessageDao(),
      companionMemoryDao = database.companionMemoryDao(),
      moodEntryDao = database.moodEntryDao(),
      productivityItemDao = database.productivityItemDao(),
      communityPostDao = database.communityPostDao(),
      smartReminderDao = database.smartReminderDao(),
      relationshipMilestoneDao = database.relationshipMilestoneDao(),
      socialFriendDao = database.socialFriendDao(),
      socialMessageDao = database.socialMessageDao(),
      communityGroupDao = database.communityGroupDao(),
      groupMessageDao = database.groupMessageDao(),
      callLogItemDao = database.callLogItemDao(),
      firestoreService = firestoreService,
      realtimeDatabaseService = rtdbService
    )

    val viewModel = SoulMateViewModel(repository)

    setContent {
      SoulMateMainNavigation(viewModel = viewModel)
    }
  }
}
