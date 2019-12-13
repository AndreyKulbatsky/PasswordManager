package ru.andreykulbatsky.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    //todo добавить свою экранную клавиатуру
    //todo добавить о программе

    private int currentPasswordLength;
    private TextView tvPassword;
    private Button btCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText etKeyPhrase = findViewById(R.id.etKeyPhrase);
        etKeyPhrase.setFocusable(true);
        etKeyPhrase.requestFocus();

        PhraseTextWatcher textWatcher = new PhraseTextWatcher(etKeyPhrase);
        etKeyPhrase.addTextChangedListener(textWatcher);

        tvPassword = findViewById(R.id.tvPassword);

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

                    String keyPhrase = etKeyPhrase.getText().toString();
                    if (!keyPhrase.equals("")) {
                        tvPassword.setText(codePassword(keyPhrase));
                        btCopy.setClickable(false);
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

    private class PhraseTextWatcher implements TextWatcher {

        private final EditText editText;

        PhraseTextWatcher(EditText editText){
            super();
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String keyPhrase = editText.getText().toString();
            if (!keyPhrase.equals("")) {
                tvPassword.setText(codePassword(keyPhrase));
                btCopy.setClickable(true);

            }
            else {
                tvPassword.setText("");
                btCopy.setClickable(false);
            }
        }
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

    //TODO написать криптографическое преобразование строки
}
