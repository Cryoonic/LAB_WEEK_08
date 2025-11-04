package com.example.lab_week_08

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.*
import com.example.lab_week_08.worker.FirstWorker
import com.example.lab_week_08.worker.SecondWorker
import com.example.lab_week_08.worker.ThirdWorker   // ✅ Tambahkan ThirdWorker

class MainActivity : AppCompatActivity() {

    private val workManager by lazy { WorkManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Izin untuk Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        val id = "001"
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // ✅ Request Workers
        val firstRequest = OneTimeWorkRequest.Builder(FirstWorker::class.java)
            .setInputData(getIdInputData(FirstWorker.INPUT_DATA_ID, id))
            .setConstraints(constraints)
            .build()

        val secondRequest = OneTimeWorkRequest.Builder(SecondWorker::class.java)
            .setInputData(getIdInputData(SecondWorker.INPUT_DATA_ID, id))
            .setConstraints(constraints)
            .build()

        val thirdRequest = OneTimeWorkRequest.Builder(ThirdWorker::class.java)
            .setInputData(getIdInputData(ThirdWorker.INPUT_DATA_ID, id))
            .setConstraints(constraints)
            .build()

        // ✅ Jalankan First → Second → NOTIFICATION SERVICE → Third → SecondNotificationService
        workManager.beginWith(firstRequest).then(secondRequest).enqueue()

        // ✅ Setelah First selesai → Toast
        workManager.getWorkInfoByIdLiveData(firstRequest.id).observe(this) { info ->
            if (info != null && info.state.isFinished) {
                showResult("First process is done")
            }
        }

        // ✅ Setelah Second selesai → Mulai NotificationService
        workManager.getWorkInfoByIdLiveData(secondRequest.id).observe(this) { info ->
            if (info != null && info.state.isFinished) {
                showResult("Second process is done")
                launchNotificationServices()

                // ✅ Setelah NotificationService selesai → jalankan ThirdWorker
                NotificationService.trackingCompletion.observe(this) {
                    showResult("NotificationService is done!")
                    workManager.enqueue(thirdRequest)
                }
            }
        }

        // ✅ Setelah Third selesai → Jalankan SecondNotificationService
        workManager.getWorkInfoByIdLiveData(thirdRequest.id).observe(this) { info ->
            if (info != null && info.state.isFinished) {
                showResult("Third process is done")
                launchSecondNotificationService()
            }
        }
    }

    private fun getIdInputData(key: String, id: String): Data {
        return Data.Builder()
            .putString(key, id)
            .build()
    }

    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun launchNotificationServices() {
        val serviceIntent = Intent(this, NotificationService::class.java).apply {
            putExtra(NotificationService.Extra_ID, "001")
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun launchSecondNotificationService() {
        val serviceIntent = Intent(this, SecondNotificationService::class.java).apply {
            putExtra(SecondNotificationService.EXTRA_ID, "001")
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}
