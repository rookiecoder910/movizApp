package com.example.movizapp.sync

import com.example.movizapp.room.WatchlistDao
import com.example.movizapp.room.WatchlistItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncManager @Inject constructor(
    private val watchlistDao: WatchlistDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun getUserWatchlistRef() =
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("watchlist")
        }

    /**
     * Upload local watchlist to Firestore (merge strategy)
     */
    suspend fun uploadWatchlist() {
        val ref = getUserWatchlistRef() ?: return
        val localItems = watchlistDao.getAll().first()
        for (item in localItems) {
            val docData = mapOf(
                "tmdbId" to item.tmdbId,
                "title" to item.title,
                "posterPath" to (item.posterPath ?: ""),
                "mediaType" to item.mediaType,
                "voteAverage" to item.voteAverage,
                "addedAt" to item.addedAt
            )
            ref.document("${item.mediaType}_${item.tmdbId}")
                .set(docData, SetOptions.merge())
                .await()
        }
    }

    /**
     * Download watchlist from Firestore → merge into Room
     */
    suspend fun downloadWatchlist() {
        val ref = getUserWatchlistRef() ?: return
        val snapshot = ref.get().await()
        for (doc in snapshot.documents) {
            val tmdbId = (doc.getLong("tmdbId") ?: continue).toInt()
            val title = doc.getString("title") ?: continue
            val posterPath = doc.getString("posterPath")?.takeIf { it.isNotEmpty() }
            val mediaType = doc.getString("mediaType") ?: continue
            val voteAverage = doc.getDouble("voteAverage") ?: 0.0
            val addedAt = doc.getLong("addedAt") ?: System.currentTimeMillis()

            watchlistDao.insert(
                WatchlistItem(
                    tmdbId = tmdbId,
                    title = title,
                    posterPath = posterPath,
                    mediaType = mediaType,
                    voteAverage = voteAverage,
                    addedAt = addedAt
                )
            )
        }
    }

    /**
     * Full sync: upload local → download remote → merge
     */
    suspend fun syncWatchlist() {
        uploadWatchlist()
        downloadWatchlist()
    }

    /**
     * Delete a specific item from Firestore
     */
    suspend fun deleteFromCloud(tmdbId: Int, mediaType: String) {
        val ref = getUserWatchlistRef() ?: return
        ref.document("${mediaType}_${tmdbId}").delete().await()
    }
}
