package ru.andreykulbatsky.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private int currentPasswordLength = 10;
    private TextView tvPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etKeyPhrase = findViewById(R.id.etKeyPhrase);
        etKeyPhrase.setFocusable(true);
        etKeyPhrase.requestFocus();

        PhraseTextWatcher textWatcher = new PhraseTextWatcher(etKeyPhrase);
        etKeyPhrase.addTextChangedListener(textWatcher);

        tvPassword = findViewById(R.id.tvPassword);

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
        editor.commit();
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
            tvPassword.setText(keyPhrase);
        }
    }
}
