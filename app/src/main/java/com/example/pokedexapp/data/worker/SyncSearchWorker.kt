package com.example.pokedexapp.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pokedexapp.domain.repository.PokemonRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncSearchWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val repository: PokemonRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            repository.syncSearchIndex()
            Result.success()
        } catch (e: Exception) {
            Log.e("WorkManager","Unknown Error Occured",e)
            Result.retry()
        }
    }
}