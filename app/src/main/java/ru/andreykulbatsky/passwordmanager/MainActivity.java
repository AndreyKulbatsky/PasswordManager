package ru.andreykulbatsky.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    //todo добавить свою экранную клавиатуру

    private int currentPasswordLength;
    private TextView tvPassword;
    private Button btCopy;
    private KeyboardView mKeyboardView;
    private Keyboard mKeyboard;
    private Constants.KEYS_TYPE mCurrentLocale;
    private Constants.KEYS_TYPE mPreviousLocale;
    private boolean isCapsOn = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        PackageInfo pInfo;
        String version="";

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Toast.makeText(MainActivity.this,getString(R.string.app_name) + "\n" + getString(R.string.version) + " " + version + "\n" + getString(R.string.email), Toast.LENGTH_LONG).show();
        return super.onOptionsItemSelected(item);
    }

    private void playClick(int keyCode) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (keyCode) {
            case Constants.KeyCode.SPACE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Constants.KeyCode.RETURN:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
                break;
        }
    }
    private Keyboard getKeyboard(Constants.KEYS_TYPE locale) {
        switch (locale) {
            case RUSSIAN:
                return new Keyboard(this, R.xml.keys_definition_ru);
            case ENGLISH:
                return new Keyboard(this, R.xml.keys_definition_en);
            case SYMBOLS:
                return new Keyboard(this, R.xml.keys_definition_symbols);
            default:
                return new Keyboard(this, R.xml.keys_definition_ru);
        }
    }

    private void handleSymbolsSwitch() {
        if (mCurrentLocale != Constants.KEYS_TYPE.SYMBOLS) {
            mKeyboard = getKeyboard(Constants.KEYS_TYPE.SYMBOLS);
            mPreviousLocale = mCurrentLocale;
            mCurrentLocale = Constants.KEYS_TYPE.SYMBOLS;
        } else {
            mKeyboard = getKeyboard(mPreviousLocale);
            mCurrentLocale = mPreviousLocale;
            mKeyboard.setShifted(isCapsOn);
        }
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.invalidateAllKeys();
    }

    private void handleShift() {
        isCapsOn = !isCapsOn;
        mKeyboard.setShifted(isCapsOn);
        mKeyboardView.invalidateAllKeys();
    }

    private void handleLanguageSwitch() {
        if (mCurrentLocale == Constants.KEYS_TYPE.RUSSIAN) {
            mCurrentLocale = Constants.KEYS_TYPE.ENGLISH;
            mKeyboard = getKeyboard(Constants.KEYS_TYPE.ENGLISH);
        } else {
            mCurrentLocale = Constants.KEYS_TYPE.RUSSIAN;
            mKeyboard = getKeyboard(Constants.KEYS_TYPE.RUSSIAN);
        }

        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboard.setShifted(isCapsOn);
        mKeyboardView.invalidateAllKeys();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tvKeyPhrase = findViewById(R.id.tvKeyPhrase);
        tvKeyPhrase.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Toast.makeText(MainActivity.this,"key",Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        tvPassword = findViewById(R.id.tvPassword);

        mKeyboardView = findViewById(R.id.keyboard);
        mCurrentLocale = Constants.KEYS_TYPE.RUSSIAN;
        mKeyboard = getKeyboard(mCurrentLocale);
        mKeyboard.setShifted(isCapsOn);
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int primaryCode) {

            }

            @Override
            public void onRelease(int primaryCode) {

            }

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                String text = tvKeyPhrase.getText().toString();
                playClick(primaryCode);
                switch (primaryCode) {
                    case Keyboard.KEYCODE_DELETE:
                        if (!text.equals(""))
                        {
                            text = text.substring(0,text.length()-1);
                            tvKeyPhrase.setText(text);
                        }
                        break;
                    case Keyboard.KEYCODE_SHIFT:
                        handleShift();
                        break;
                    case Keyboard.KEYCODE_DONE:
                        break;
                    case Keyboard.KEYCODE_ALT:
                        handleSymbolsSwitch();
                        break;
                    case Keyboard.KEYCODE_MODE_CHANGE:
                        handleLanguageSwitch();
                        break;
                    default:
                        char code = (char) primaryCode;
                        if (Character.isLetter(code) && isCapsOn) {
                            code = Character.toUpperCase(code);
                        }
                        text += code;
                        tvKeyPhrase.setText(text);
                        break;
                }
                if (!text.equals("")) {
                    tvPassword.setText(codePassword(text));
                    btCopy.setClickable(true);

                }
                else {
                    tvPassword.setText("");
                    btCopy.setClickable(false);
                }

            }

            @Override
            public void onText(CharSequence text) {

            }

            @Override
            public void swipeLeft() {

            }

            @Override
            public void swipeRight() {

            }

            @Override
            public void swipeDown() {

            }

            @Override
            public void swipeUp() {

            }
        });

        btCopy  = findViewById(R.id.btCopy);
        btCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = tvPassword.getText().toString();
                if (!text.equals("")) {
                    ClipboardManager clipboard = (ClipboardManager) MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", text);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.password_copied_notification), Toast.LENGTH_SHORT).show();
                }
            }
        });
        btCopy.setClickable(false);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item);
        for (int i =0;i<=8;i++)
            arrayAdapter.add(Integer.toString(i+8));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        currentPasswordLength = readCurrentPasswordLength();

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setAdapter(arrayAdapter);
        spinner.setSelection(currentPasswordLength-8);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (currentPasswordLength!= (position+8)) {
                    currentPasswordLength = (position+8);
                    saveCurrentPasswordLength(currentPasswordLength);

                    String keyPhrase = tvKeyPhrase.getText().toString();
                    if (!keyPhrase.equals("")) {
                        tvPassword.setText(codePassword(keyPhrase));
                        btCopy.setClickable(true);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private int readCurrentPasswordLength() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        return preferences.getInt("length", 10);
    }

    private void saveCurrentPasswordLength(int length) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("length",length);
        editor.apply();
    }

    private String codePassword(String keyPhrase) {
        MessageDigest messageDigest;
        byte[] md5Hash;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(keyPhrase.getBytes());
            md5Hash = messageDigest.digest();

            byte[] base64Hash = Base64.encode(md5Hash,Base64.DEFAULT);
            StringBuilder resultString = new StringBuilder();

            for (int i=0;i<currentPasswordLength;i++) {
                resultString.append((char) base64Hash[i]);
            }

            return resultString.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
}
