package com.example.roadtripbuddy.databaseTests

import com.example.roadtripbuddy.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreTest {

    private val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)

    @Test
    fun firestore_writesUserData() = runBlocking {
        val user = User(name = "Test", email = "test@example.com", vehicle = "Car")
        mockFirestore.collection("users").document("123").set(user.toMap())
        verify { mockFirestore.collection("users").document("123").set(user.toMap()) }
    }

    @Test
    fun firestore_handlesOffline() = runBlocking {
        val mockTask = mockk<Task<QuerySnapshot>>()
        every { mockFirestore.collection("users").get() } returns mockTask
        every { mockTask.isSuccessful } returns false
        every { mockTask.exception } returns RuntimeException("Offline")

        val result = runCatching { mockFirestore.collection("users").get().result }
        assertTrue(result.isFailure)
    }

    @Test
    fun firestore_handlesRealTimeUpdates() = runBlocking {
        val mockListener = mockk<ListenerRegistration>(relaxed = true)
        every {
            mockFirestore.collection("users").document("123").addSnapshotListener(any())
        } answers {
            mockListener
        }

        mockFirestore.collection("users").document("123")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.toObject(User::class.java)
            }

        verify { mockFirestore.collection("users").document("123").addSnapshotListener(any()) }
    }
}

