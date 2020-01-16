package ru.andreykulbatsky.passwordmanager

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.media.AudioManager
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ru.andreykulbatsky.passwordmanager.Constants.KeysType
import ru.andreykulbatsky.passwordmanager.databinding.ActivityMainBinding
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

//todo добавить шрифты

class MainActivity : AppCompatActivity() {

    private var currentPasswordLength = 0
    private lateinit var btCopy: Button
    private lateinit var mKeyboardView: KeyboardView
    private lateinit var mKeyboard: Keyboard
    private lateinit var mCurrentLocale: KeysType
    private lateinit var mPreviousLocale: KeysType
    private var isCapsOn = false

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itHelp -> showHelp()
            R.id.itAbout -> showAboutDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showHelp() {
        val title = TextView(this)
        title.setText(R.string.help)
        title.gravity = Gravity.CENTER
        val builder = AlertDialog.Builder(this)
        builder.setCustomTitle(title)
        builder.setCancelable(true)
        builder.setMessage(getString(R.string.help_text))
        val dialog = builder.create()
        dialog.show()
    }

    private fun showAboutDialog() {
        val title = TextView(this)
        title.setText(R.string.about)
        title.gravity = Gravity.CENTER
        val pInfo: PackageInfo
        var version = ""
        try {
            pInfo = packageManager.getPackageInfo(packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val about = getString(R.string.app_name) + "\n" + getString(R.string.version) + " " + version + "\n" + getString(R.string.email)
        val builder = AlertDialog.Builder(this)
        builder.setCustomTitle(title)
        builder.setCancelable(true)
        builder.setMessage(about)
        val dialog = builder.create()
        dialog.show()
        val message = dialog.findViewById<TextView>(android.R.id.message)
        message.gravity = Gravity.CENTER
    }

    private fun playClick(keyCode: Int) {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (keyCode) {
            Constants.KeyCode.SPACE -> am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
            Keyboard.KEYCODE_DONE, Constants.KeyCode.RETURN -> am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN)
            Keyboard.KEYCODE_DELETE -> am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE)
            else -> am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
    }

    private fun getKeyboard(locale: KeysType?): Keyboard {
        return when (locale) {
            KeysType.ENGLISH -> Keyboard(this, R.xml.keys_definition_en)
            KeysType.SYMBOLS -> Keyboard(this, R.xml.keys_definition_symbols)
            else -> Keyboard(this, R.xml.keys_definition_ru)
        }
    }

    private fun handleSymbolsSwitch() {
        if (mCurrentLocale != KeysType.SYMBOLS) {
            mPreviousLocale = mCurrentLocale
            mCurrentLocale = KeysType.SYMBOLS
            mKeyboard = getKeyboard(mCurrentLocale)
        } else {
            mKeyboard = getKeyboard(mPreviousLocale)
            mCurrentLocale = mPreviousLocale
            mKeyboard.isShifted = isCapsOn
        }
        mKeyboardView.keyboard = mKeyboard
        mKeyboardView.invalidateAllKeys()
    }

    private fun handleShift() {
        isCapsOn = !isCapsOn
        mKeyboard.isShifted = isCapsOn
        mKeyboardView.invalidateAllKeys()
    }

    private fun handleLanguageSwitch() {
        if (mCurrentLocale == KeysType.RUSSIAN) {
            mCurrentLocale = KeysType.ENGLISH
            mKeyboard = getKeyboard(KeysType.ENGLISH)
        } else {
            mCurrentLocale = KeysType.RUSSIAN
            mKeyboard = getKeyboard(KeysType.RUSSIAN)
        }
        mKeyboardView.keyboard = mKeyboard
        mKeyboard.isShifted = isCapsOn
        mKeyboardView.invalidateAllKeys()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding : ActivityMainBinding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)

        val tvKeyPhrase = binding.tvKeyPhrase
        mCurrentLocale = KeysType.RUSSIAN
        mKeyboard = getKeyboard(mCurrentLocale)
        mKeyboard.isShifted = isCapsOn
        mKeyboardView = binding.keyboard
        mKeyboardView.keyboard = mKeyboard
        mKeyboardView.setOnKeyboardActionListener(object : OnKeyboardActionListener {
            override fun onPress(primaryCode: Int) {}
            override fun onRelease(primaryCode: Int) {}
            override fun onKey(primaryCode: Int, keyCodes: IntArray) {
                var text = tvKeyPhrase.text.toString()
                playClick(primaryCode)
                when (primaryCode) {
                    Keyboard.KEYCODE_DELETE -> if (text != "") {
                        text = text.substring(0, text.length - 1)
                        tvKeyPhrase.text = text
                    }
                    Keyboard.KEYCODE_SHIFT -> handleShift()
                    Keyboard.KEYCODE_DONE -> {
                    }
                    Keyboard.KEYCODE_ALT -> handleSymbolsSwitch()
                    Keyboard.KEYCODE_MODE_CHANGE -> handleLanguageSwitch()
                    else -> {
                        var code = primaryCode.toChar()
                        if (Character.isLetter(code) && isCapsOn) {
                            code = Character.toUpperCase(code)
                        }
                        text += code
                        tvKeyPhrase.text = text
                    }
                }
                if (text.length > currentPasswordLength) {
                    binding.password = codePassword(text)
                    btCopy.isClickable = true
                    btCopy.setTextColor(Color.BLACK)
                } else {
                    binding.password = ""
                    btCopy.isClickable = false
                    btCopy.setTextColor(Color.GRAY)
                }
            }

            override fun onText(text: CharSequence) {}
            override fun swipeLeft() {}
            override fun swipeRight() {}
            override fun swipeDown() {}
            override fun swipeUp() {}
        })
        btCopy = binding.btCopy
        btCopy.setOnClickListener {
            if (binding.password != "") {
                val clipboard = this@MainActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("text", binding.password)
                clipboard.primaryClip = clip
                Toast.makeText(this@MainActivity, this@MainActivity.getString(R.string.password_copied_notification), Toast.LENGTH_SHORT).show()
            }
        }
        btCopy.isClickable = false
        btCopy.setTextColor(Color.GRAY)
        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        for (i in 0..8) arrayAdapter.add((i + 8).toString())
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        currentPasswordLength = readCurrentPasswordLength()
        val spinner = binding.spinner
        spinner.adapter = arrayAdapter
        spinner.setSelection(currentPasswordLength - 8)
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                if (currentPasswordLength != position + 8) {
                    currentPasswordLength = position + 8
                    saveCurrentPasswordLength(currentPasswordLength)
                    val keyPhrase = tvKeyPhrase.text.toString()
                    if (keyPhrase.length > currentPasswordLength) {
                        binding.password = codePassword(keyPhrase)
                        btCopy.isClickable = true
                        btCopy.setTextColor(Color.BLACK)
                    } else {
                        binding.password = ""
                        btCopy.isClickable = false
                        btCopy.setTextColor(Color.GRAY)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun readCurrentPasswordLength(): Int {
        val preferences = getPreferences(Context.MODE_PRIVATE)
        return preferences.getInt("length", 10)
    }

    private fun saveCurrentPasswordLength(length: Int) {
        val preferences = getPreferences(Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("length", length)
        editor.apply()
    }

    private fun codePassword(keyPhrase: String): String {
        var messageDigest: MessageDigest
        return try {
            messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.reset()
            messageDigest.update(keyPhrase.toByteArray(Charset.forName("ASCII")))
            val md5Hash1 = messageDigest.digest()
            messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.reset()
            messageDigest.update(md5Hash1)
            val sha256 = messageDigest.digest()
            messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.reset()
            messageDigest.update(sha256)
            val md5Hash2 = messageDigest.digest()
            " " + Base64.encodeToString(md5Hash2, 0, md5Hash2.size, Base64.DEFAULT).substring(0, currentPasswordLength)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            ""
        }
    }
}