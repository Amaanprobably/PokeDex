package com.example.pokedexapp.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pokedexapp.data.repository.PokemonRepositoryImpl
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class SyncSearchWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val repository: PokemonRepositoryImpl by inject()

    override suspend fun doWork(): Result {
        return try {
            Timber.d("WorkManager: Starting Search Sync")

            repository.syncSearchIndex()

            Timber.d("WorkManager: Sync Successful!")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "WorkManager: Sync Failed. Retrying")
            Result.retry()
        }
    }
}