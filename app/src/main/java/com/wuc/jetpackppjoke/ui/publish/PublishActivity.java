package com.wuc.jetpackppjoke.ui.publish;

import android.os.Bundle;
import android.view.View;

import com.wuc.jetpackppjoke.R;
import com.wuc.libnavannotation.ActivityDestination;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author: wuchao
 * @date: 2020-01-22 21:32
 * @desciption:
 */
@ActivityDestination(pageUrl = "main/tabs/publish", needLogin = true)
public class PublishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        findViewById(R.id.btn_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
