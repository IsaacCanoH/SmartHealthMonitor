package mx.utng.ich.smarthealth.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import mx.utng.ich.smarthealth.data.db.SmartHealthDB
import mx.utng.ich.smarthealth.data.repository.SyncRepository

class NeonSyncWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        return try {
            val dao = SmartHealthDB.getDatabase(applicationContext).lecturaDao()
            val repository = SyncRepository(dao)

            repository.enviarPendientes()
            repository.sincronizarDesdeNeon(limite = PULL_LIMIT)

            Log.d(TAG, "Sincronización con Neon completada")
            Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Sincronización con Neon fallida; se reintentará", exception)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "NeonSyncWork"
        private const val TAG = "SYNC_WORKER"
        private const val PULL_LIMIT = 100

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<NeonSyncWorker>(
                30,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    5,
                    TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
}
