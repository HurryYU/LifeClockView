package com.hurryyu.lifeclockview;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final LifeClockView lifeClockView = findViewById(R.id.life_clock);
        final EditText etAge = findViewById(R.id.et_age);
        Button btnCalcLife = findViewById(R.id.btn_calc_life);

        btnCalcLife.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String age = etAge.getText().toString();
                if (TextUtils.isEmpty(age)) {
                    Toast.makeText(MainActivity.this, "请输入年龄", Toast.LENGTH_SHORT).show();
                    return;
                }
                lifeClockView.setCurrentAge(Integer.parseInt(age));
            }
        });

        lifeClockView.setOnEditClickListener(new LifeClockView.OnEditClickListener() {
            @Override
            public void onClick() {
                Toast.makeText(MainActivity.this, "edit", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
