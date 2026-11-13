package com.lifelogai.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * LifeLog AI starter:
 * - voice -> Android speech recognition -> editable text -> local text record
 * - quick text entry -> local text record
 * - analysis button shares chosen/all log text to an AI app only when user taps it
 *
 * Replace in-memory storage with Room database before production release.
 */
class MainActivity : ComponentActivity() {
    private val entries = mutableListOf<Pair<Long, String>>()
    private lateinit var list: LinearLayout
    private lateinit var input: EditText
    private lateinit var status: TextView
    private val speech = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) {
                input.setText(spoken)
                status.text = "Voice converted to text. Review, then tap Save."
            }
        }
    }
    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startSpeech() else status.text = "Microphone permission is needed for voice capture."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        renderEntries()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 42, 36, 24)
            setBackgroundColor(0xFF101820.toInt())
        }
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(36 + bars.left, 42 + bars.top, 36 + bars.right, 24 + bars.bottom)
            insets
        }
        fun text(value: String, size: Float, color: Int = 0xFFFFFFFF.toInt()) = TextView(this).apply {
            text = value; textSize = size; setTextColor(color)
        }
        root.addView(text("LIFELOG AI", 14f, 0xFF62D6B0.toInt()))
        root.addView(text("Your day, your story.", 27f).apply { setPadding(0, 12, 0, 8) })
        root.addView(text("Capture a moment. Analyze when you choose.", 14f, 0xFFA7B5C4.toInt()))
        status = text("Records stay on this device in this starter build.", 13f, 0xFF62D6B0.toInt())
        root.addView(status.apply { setPadding(0, 20, 0, 12) })

        input = EditText(this).apply {
            hint = "Type a thought or activity…"
            setTextColor(0xFFFFFFFF.toInt()); setHintTextColor(0xFF9EADB9.toInt())
            setPadding(20, 16, 20, 16)
            setBackgroundColor(0xFF1D2B38.toInt())
        }
        root.addView(input, LinearLayout.LayoutParams(-1, -2))

        val voice = Button(this).apply { text = "🎙 Record by voice"; setOnClickListener {
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startSpeech()
            else permission.launch(Manifest.permission.RECORD_AUDIO)
        } }
        root.addView(voice)
        root.addView(Button(this).apply { text = "Save text entry"; setOnClickListener { saveEntry() } })
        root.addView(Button(this).apply { text = "Analyze / export my log"; setOnClickListener { shareForAnalysis() } })
        root.addView(text("TIMELINE", 14f, 0xFF62D6B0.toInt()).apply { setPadding(0, 24, 0, 12) })

        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun startSpeech() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your routine or thought")
        }
        try { speech.launch(intent) } catch (_: Exception) {
            status.text = "Speech recognition isn't available on this phone."
        }
    }

    private fun saveEntry() {
        val value = input.text.toString().trim()
        if (value.isEmpty()) { status.text = "Enter text or record your voice first."; return }
        entries.add(0, System.currentTimeMillis() to value)
        input.text.clear()
        status.text = "Saved locally as text ✓"
        renderEntries()
    }

    private fun renderEntries() {
        list.removeAllViews()
        val formatter = SimpleDateFormat("EEE, dd MMM · hh:mm a", Locale.getDefault())
        entries.forEach { (time, content) ->
            val card = TextView(this).apply {
                text = formatter.format(Date(time)) + "\n" + content
                textSize = 15f; setTextColor(0xFFFFFFFF.toInt())
                setPadding(20, 18, 20, 18); setBackgroundColor(0xFF1D2B38.toInt())
            }
            val lp = LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = 12
            list.addView(card, lp)
        }
    }

    private fun shareForAnalysis() {
        if (entries.isEmpty()) { status.text = "Record something first."; return }
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val log = entries.asReversed().joinToString("\n") { (t, s) -> "${formatter.format(Date(t))} — $s" }
        val prompt = "Analyze this personal routine log. Identify patterns, time use, distractions, and practical improvements. Do not assume missing information.\n\n$log"
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, prompt)
        }
        startActivity(Intent.createChooser(share, "Send log to AI for analysis"))
    }
}
