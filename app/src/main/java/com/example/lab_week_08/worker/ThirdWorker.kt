package com.example.lab_week_08.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class ThirdWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val id = inputData.getString(INPUT_DATA_ID) ?: "Unknown"
        Log.d("ThirdWorker", "ThirdWorker is running for ID: $id")

        Thread.sleep(3000) // 3 detik

        Log.d("ThirdWorker", "ThirdWorker completed for ID: $id")
        return Result.success()
    }

    companion object {
        const val INPUT_DATA_ID = "input_data_id_3"
    }
}
