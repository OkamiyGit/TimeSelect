package com.test.okamiy.timeselect;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.test.okamiy.timeselect.TimeUtils.getStatetime;

/**
 * Created by Okamiy on 2017/6/22.
 * Email: 542839122@qq.com
 * <p>
 * 时间选择器核心代码块
 */

public class TimeSelectUtils implements NumberPicker.OnValueChangeListener, View.OnClickListener {

    private final Button selectTime;
    private final Button totalTime;
    private String initDateTime;
    private Context activity;
    private Calendar calendar;
    private CustomNumberPicker hourpicker;
    private CustomNumberPicker minutepicker;
    private CustomNumberPicker datepicker;
    private String[] minuteArrs;
    private String hourStr;
    private String minuteStr;
    private String dateStr;
    private Dialog dialog;
    private String[] dayArrays;
    private long currentTimeMillis;
    private RadioButton rgOut;
    private RadioButton rgIn;
    private boolean goIn = true;//标记我们入场时间选取和为选区：true为未选取时间
    private String startTime = "";
    private String endTime = "";
    private GetSubmitTime mSubTime;

    //构造方法
    public TimeSelectUtils(Context activity, String initDateTime, Button selectTime, Button totalTime, GetSubmitTime subTime) {
        this.selectTime = selectTime;
        this.totalTime = totalTime;
        this.activity = activity;
        this.initDateTime = initDateTime;
        this.mSubTime = subTime;
    }

    //初始化时间选择器：设置当前时间（根据自己需求更改）
    public void initPicker() {
        Calendar calendar = Calendar.getInstance();

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        if (45 <= minutes)
            minutes = 0;

        else if (30 <= minutes)
            minutes = 3;

        else if (15 <= minutes)
            minutes = 2;

        else
            minutes = 1;

        // 设置日期 2天以内
        dayArrays = new String[2];
        dayArrays[0] = "今天";
        dayArrays[1] = "明天";
        switch (dayArrays.length) {
            case 0:
                getStatetime();
                break;
            case 1:
                getStatetime(1);
                break;
        }
        currentTimeMillis = System.currentTimeMillis();// 设置当前时间的毫秒值
        //时间选取改变监听
        datepicker.setOnValueChangedListener(this);
        datepicker.setDisplayedValues(dayArrays);
        datepicker.setMinValue(0);
        datepicker.setMaxValue(dayArrays.length - 1);
        datepicker.setValue(0);
        dateStr = dayArrays[0];// 初始值

        // 设置小时 预约15分钟以后
        hourpicker.setOnValueChangedListener(this);
        hourpicker.setMaxValue(23);
        hourpicker.setMinValue(0);
        if (minutes == 0) {
            hourpicker.setValue(hours + 1);
            hourStr = hours + 1 + "";// 初始值

        } else {
            hourpicker.setValue(hours);
            hourStr = hours + "";// 初始值

        }
        // 设置分钟
        minuteArrs = new String[]{"00", "15", "30", "45"};
        minutepicker.setOnValueChangedListener(this);
        minutepicker.setDisplayedValues(minuteArrs);
        minutepicker.setMinValue(0);
        minutepicker.setMaxValue(minuteArrs.length - 1);
        minutepicker.setValue(minutes);
        minuteStr = minuteArrs[minutes];// 初始值
    }

    /**
     * 弹出日期时间选择框方法
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public Dialog dateTimePicKDialog() {
        View dateTimeLayout = View.inflate(activity, R.layout.item_time_select,
                null);
        dateTimeLayout.findViewById(R.id.tv_cancel).setOnClickListener(this);
        dateTimeLayout.findViewById(R.id.tv_confirm).setOnClickListener(this);
        rgIn = (RadioButton) dateTimeLayout.findViewById(R.id.rb_go_in);
        rgOut = (RadioButton) dateTimeLayout.findViewById(R.id.rb_go_out);
        rgIn.setOnClickListener(this);
        rgOut.setOnClickListener(this);

        datepicker = (CustomNumberPicker) dateTimeLayout.findViewById(R.id.datepicker);
        hourpicker = (CustomNumberPicker) dateTimeLayout.findViewById(R.id.hourpicker);
        minutepicker = (CustomNumberPicker) dateTimeLayout.findViewById(R.id.minuteicker);
        //滑动或者点击时取消焦点且屏蔽键盘
        datepicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        hourpicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        minutepicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        //分割线颜色
        datepicker.setNumberPickerDividerColor(datepicker);
        hourpicker.setNumberPickerDividerColor(hourpicker);
        minutepicker.setNumberPickerDividerColor(minutepicker);
        initPicker();
        //弹出时间选择器
        dialog = new Dialog(activity, R.style.dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dateTimeLayout, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = ((Activity) activity).getWindowManager().getDefaultDisplay().getHeight();
        wl.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        wl.height = RelativeLayout.LayoutParams.WRAP_CONTENT;

        dialog.onWindowAttributesChanged(wl);
        // 设置点击外围隐藏
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        onDateChanged();
        return dialog;
    }

    @SuppressWarnings("deprecation")
    public void onDateChanged() {
        // 获得日历实例
        calendar = Calendar.getInstance();
        calendar.setTime(new Date(currentTimeMillis));
        Date date = calendar.getTime();
        date.setHours(Integer.parseInt(hourStr));
        date.setMinutes(Integer.parseInt(minuteStr));
        calendar.setTime(date);
        //当前选取的时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        initDateTime = sdf.format(calendar.getTime()) + " " + hourStr + ":" + minuteStr;
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        switch (picker.getId()) {
            //改变一定要记得更新数据
            case R.id.datepicker:
                currentTimeMillis = System.currentTimeMillis() + newVal * 24 * 3600 * 1000;
                dateStr = dayArrays[newVal];
                onDateChanged();
                break;
            case R.id.hourpicker:
                hourStr = newVal + "";
                onDateChanged();
                break;
            case R.id.minuteicker:
                minuteStr = minuteArrs[newVal];
                onDateChanged();
                break;
            default:
                break;
        }
    }

    //改接口用于在MainActivity获取我们选取的开始时间、结束时间
    public interface GetSubmitTime {
        void selectTime(String startDate, String entDate);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //取消
            case R.id.tv_cancel:
                dialog.dismiss();
                break;
            //下一步
            case R.id.tv_confirm:
                selectTimes();
                break;
            //入场
            case R.id.rb_go_in:
                //点击入场时记录我们之前出场选取的时间且是在入场时间已经选择状态
                if (goIn == false) {
                    endTime = initDateTime;
                    //手动设置我们选取的入场时间
                    if (startTime != null && !startTime.equals("")) {
                        setTimes(startTime);
                    }
                    goIn = true;
                }
                Log.i("TAG", startTime + "------开始时间00");
                break;
            //出场
            case R.id.rb_go_out:
                //未选择入场时间：保存入场时间
                if (goIn) {
                    startTime = initDateTime;
                    Log.i("TAG", startTime + "------开始时间11");
                    if (!TimeUtils.compareNowTime(startTime)) {
                        Toast.makeText(activity, "请选择正确的入场时间", Toast.LENGTH_SHORT).show();
                        Log.i("TAG", startTime + "------开始时间22");
                    } else {
                        goIn = false;
                    }
                }
                //手动设置我们选取的出场时间
                if (endTime != null && !endTime.equals("")) {
                    setTimes(endTime);
                }
                break;
            default:
                break;
        }
    }

    //设置时间(此处用于记录上次选中的时间)
    private void setTimes(String startTime) {
        try {
            Date date = TimeUtils.stringToDate(startTime + ":00", "yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            String time = calendar.get(Calendar.YEAR) + "-" + formatTime(calendar.get(Calendar.MONTH) + 1) + "-" + formatTime(calendar.get(Calendar.DAY_OF_MONTH));
            Log.i("TAG", time + ",000000," + TimeUtils.getStatetime());
            if (time.equals(getStatetime())) {
                datepicker.setValue(0);
            } else {
                datepicker.setValue(1);
            }
            if (45 <= minutes)
                minutes = 3;

            else if (30 <= minutes)
                minutes = 2;

            else if (15 <= minutes)
                minutes = 1;

            else
                minutes = 0;
            //设置我们记录的值
            hourpicker.setValue(hours);
            minutepicker.setValue(minutes);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //分钟小于10补0
    private String formatTime(int t) {
        return t >= 10 ? "" + t : "0" + t;//三元运算符 t>10时取 ""+t
    }

    //点击下一步选取时间
    private void selectTimes() {
        if (rgIn.isChecked()) {
            //点击下一步保存我们的入场时间
            startTime = initDateTime;
            //选取15分钟以后时间段
            if (calendar.getTimeInMillis() <= System.currentTimeMillis() || calendar.getTimeInMillis() > System.currentTimeMillis()
                    + 2 * 24 * 3600 * 1000 || startTime.equals("") || startTime == null) {
                Toast.makeText(activity, "请选择距现在15分钟后有效时间", Toast.LENGTH_SHORT).show();
            } else {
                startTime = initDateTime;
                rgOut.setChecked(true);
                goIn = false;
            }
        } else {
            //点击下一步保存我们的出场时间
            endTime = initDateTime;
            if (!TimeUtils.compareTwoTime(startTime, endTime)) {
                Toast.makeText(activity, "请选择正确的出场时间", Toast.LENGTH_SHORT).show();
            } else {
                endTime = initDateTime;
                Log.i("TAG", endTime + "------结束时间11");
                setTextTime(startTime, endTime);
                dialog.dismiss();
            }
        }
        //将选取的时间保存到借口
        mSubTime.selectTime(startTime + ":00", endTime + ":00");
    }

    //直接设置我们选取的时间：转化为String
    private void setTextTime(String startTime, String endTime) {
        if (startTime.equals("") || startTime == null || endTime.equals("") || endTime == null) {
            return;
        }
        String s = TimeUtils.formatDateTime(startTime);
        //此处这么做是为了防止选择的时间为今天凌晨显示错误
        String s1 = startTime.substring(0, 10).trim().equals(getStatetime()) ? "今天" : "明天";
        String s2 = s.substring(3, s.length()).trim();
        String e = TimeUtils.formatDateTime(endTime);
        String e1 = endTime.substring(0, 10).trim().equals(getStatetime()) ? "今天" : "明天";
        String e2 = e.substring(3, e.length()).trim();
        String result = TimeUtils.getTimeDifference(startTime, endTime);
        Log.i("TAG", "主界面时间选择器选取的时间p:" + s1 + "," + e1);
        if (s1.equals(e1)) {
            selectTime.setText("选择时间：" + s1 + s2 + "-" + e2);
        } else {
            selectTime.setText("选择时间：" + s1 + s2 + "-" + e1 + e2);
        }
        totalTime.setVisibility(View.VISIBLE);
        totalTime.setText("(" + "合计 : " + result + ")");
        Log.i("TAG", s + "------开始时间" + e + "------结束时间");
    }
}
