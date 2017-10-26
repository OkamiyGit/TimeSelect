package com.test.okamiy.timeselect;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button selectTime = (Button) findViewById(R.id.bt_select_time);
        final Button totalTime = (Button) findViewById(R.id.bt_total_time);
        //点击监听
        ((LinearLayout) findViewById(R.id.ll_popup_time_selsect)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeSelectUtils timeSelectUtils = new TimeSelectUtils(MainActivity.this, null, selectTime, totalTime, new TimeSelectUtils.GetSubmitTime() {
                    @Override
                    public void selectTime(String startDate, String entDate) {
                        Log.i("TAG", "选择的开始时间：" + startDate + ",结束时间：" + entDate);
                    }
                });

                //显示我们的时间选择器
                timeSelectUtils.dateTimePicKDialog();
            }
        });
    }
}
