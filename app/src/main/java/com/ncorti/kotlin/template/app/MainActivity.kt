package com.ncorti.kotlin.template.app

import android.Manifest.permission.RECORD_AUDIO
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.RecognizerIntent.EXTRA_LANGUAGE
import android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL
import android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ncorti.kotlin.template.library.FactorialCalculator
import com.ncorti.kotlin.template.library.android.NotificationUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Locale

private const val DEFAULT_INPUT = 3

private const val REQUEST_CODE_RECOGNITION = 4711

class MainActivity : AppCompatActivity() {

    private val notificationUtil: NotificationUtil by lazy { NotificationUtil(this) }

    private var speechRecognizer: SpeechRecognizer? = null
    private val textToSpeechEngine: TextToSpeech by lazy {
        // Pass in context and the listener.
        TextToSpeech(
            this
        ) { status ->
            // set our locale only if init was success.
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechEngine.language = Locale.getDefault()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_compute.setOnClickListener {
            val input = edit_text_factorial.text.toString().toIntOrNull() ?: DEFAULT_INPUT
            val result = FactorialCalculator.computeFactorial(input).toString()

            text_result.text = result
            text_result.visibility = View.VISIBLE

            notificationUtil.showNotification(
                context = this,
                title = getString(R.string.notification_title),
                message = result
            )
        }

        mic.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    startSpeechRecognizing()
                }
                shouldShowRequestPermissionRationale(RECORD_AUDIO) -> {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected. In this UI,
                    // include a "cancel" or "no thanks" button that allows the user to
                    // continue using your app without granting the permission.
                    Toast.makeText(
                        this@MainActivity,
                        "please think carefully not to use us",
                        Toast.LENGTH_LONG
                    ).show()
                    requestPermissions(arrayOf(RECORD_AUDIO), REQUEST_CODE_RECOGNITION)
                }
                else -> {
                    // You can directly ask for the permission.
                    requestPermissions(arrayOf(RECORD_AUDIO), REQUEST_CODE_RECOGNITION)
                }
            }
        }
        speaker.setOnClickListener {
            val text = stt.text.toString().trim()
            if (text.isNotEmpty()) {
                textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
            } else {
                Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_RECOGNITION -> {
                // If request is cancelled, the result arrays are empty.
                if ((
                    grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                    )
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    startSpeechRecognizing()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(
                        this@MainActivity,
                        "We don't work without mic permissions",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
            else ->
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun startSpeechRecognizing() {
        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(p0: Bundle?) {
                    Log.i("test", "ready for speech")
                }

                override fun onBeginningOfSpeech() {
                    Log.i("test", "start listening")
                    stt.text = "start listening"
                }

                override fun onRmsChanged(p0: Float) {
                    // nothing
                }

                override fun onBufferReceived(p0: ByteArray?) {
                    // nothing
                }

                override fun onEndOfSpeech() {
                    Log.i("test", "end speech")
                }

                override fun onError(p0: Int) {
                    Log.i("test", "error")
                }

                override fun onResults(p0: Bundle?) {
                    Log.i("test", "result ready")

                    p0?.run {
                        val stringArray = getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        Log.i("test", "result $stringArray")
                        stt.text = stringArray?.get(0)
                    }
                }

                override fun onPartialResults(p0: Bundle?) {
                    Log.i("test", "partial result ready")
                    p0?.run {
                        val stringArray = getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        Log.i("test", "partial $stringArray")
                        stt.text = stringArray?.get(0)
                    }
                }

                override fun onEvent(p0: Int, p1: Bundle?) {
                    // nothing
                }
            }
        )
        Log.i("test", "ready for speech")

        Log.i("test", "is available? ${SpeechRecognizer.isRecognitionAvailable(this)}")
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(EXTRA_LANGUAGE_MODEL, LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizer?.startListening(speechIntent)
        Log.i("test", "start recognizer")
    }

    override fun onResume() {
        super.onResume()
        if (null == speechRecognizer) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }
    }

    override fun onPause() {
        speechRecognizer?.stopListening()
        textToSpeechEngine.stop()
        speechRecognizer = null
        super.onPause()
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        textToSpeechEngine.shutdown()
        super.onDestroy()
    }
}
