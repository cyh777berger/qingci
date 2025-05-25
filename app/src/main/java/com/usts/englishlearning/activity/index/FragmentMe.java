package com.usts.englishlearning.activity.index;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.AboutActivity;
import com.usts.englishlearning.activity.AlarmActivity;
import com.usts.englishlearning.activity.CalendarActivity;
import com.usts.englishlearning.activity.ChartActivity;
import com.usts.englishlearning.activity.LearnInNotifyActivity;
import com.usts.englishlearning.activity.ListActivity;
import com.usts.englishlearning.activity.MainActivity;
import com.usts.englishlearning.activity.UserProfileActivity;
import com.usts.englishlearning.activity.PlanActivity;
import com.usts.englishlearning.activity.SynchronyActivity;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.database.MyDate;
import com.usts.englishlearning.database.User;
import com.usts.englishlearning.util.MyApplication;
import com.usts.englishlearning.util.TimeController;

import org.litepal.LitePal;

import java.util.Date;
import java.util.List;

public class FragmentMe extends Fragment implements View.OnClickListener {

    private LinearLayout layoutCalendar, layoutWordList, layoutData, layoutPlan, layoutMoney;
    private RelativeLayout layoutAlarm, layoutNotify, layoutAbout, layoutSyno;
    private TextView textDays, textWordNum, textMoney, tvNameText, userprofile;  // 保留 userprofile 按钮
    private Switch aSwitchNight;

    private static final String TAG = "FragmentMe";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();

        Log.d(TAG, "onActivityCreated: ");

        // 初始化 Switch 的状态
        boolean isNight = ConfigData.getIsNight();
        aSwitchNight.setChecked(isNight);

        // 设置 Switch 的监听器
        aSwitchNight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConfigData.setIsNight(isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

            // 确保重建后停留在 FragmentMe
            MainActivity.lastFragment = 2; // FragmentMe 的索引
            requireActivity().recreate(); // 重建 Activity
        });

        // 其他按钮点击逻辑
        layoutMoney.setOnClickListener(v -> handleMoneyClick());
    }

    private void handleMoneyClick() {
        // 示例逻辑：花费铜板补打卡
        final String[] date = TimeController.getStringDate(TimeController.getCurrentDateStamp()).split("-");
        final int currentMoney = Integer.parseInt(textMoney.getText().toString().trim());
        if (currentMoney >= 100) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("提示")
                    .setMessage("确定要花费100铜板进行日历补卡吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                                (view, year, month, dayOfMonth) -> {
                                    List<MyDate> myDateList = LitePal.where("year = ? and month = ? and date = ?", year + "", (month + 1) + "", dayOfMonth + "").find(MyDate.class);
                                    if (myDateList.isEmpty()) {
                                        if (year == Integer.parseInt(date[0]) && month == Integer.parseInt(date[1]) - 1 && dayOfMonth == Integer.parseInt(date[2])) {
                                            Toast.makeText(requireContext(), "不可对今日进行补打卡", Toast.LENGTH_SHORT).show();
                                        } else {
                                            MyDate myDate = new MyDate();
                                            myDate.setDate(dayOfMonth);
                                            myDate.setUserId(ConfigData.getSinaNumLogged());
                                            myDate.setYear(year);
                                            myDate.setMonth(month + 1);
                                            myDate.save();
                                            User user = new User();
                                            user.setUserMoney(currentMoney - 100);
                                            user.updateAll("userId = ?", ConfigData.getSinaNumLogged() + "");
                                            updateData();
                                            Toast.makeText(requireContext(), "补卡成功！", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(requireContext(), "已在该日进行打卡，不可重复", Toast.LENGTH_SHORT).show();
                                    }
                                },
                                Integer.parseInt(date[0]),
                                Integer.parseInt(date[1]) - 1,
                                Integer.parseInt(date[2]));
                        datePickerDialog.show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            Toast.makeText(requireContext(), "抱歉，铜板不足，需 100 铜板才能补打卡", Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {
        // 初始化布局组件
        layoutCalendar = getActivity().findViewById(R.id.layout_me_calendar);
        layoutWordList = getActivity().findViewById(R.id.layout_me_word_list);
        tvNameText = getActivity().findViewById(R.id.tv_name_text);  // 显示用户名
        userprofile = getActivity().findViewById(R.id.userprofile);  // 保留原始的 userprofile 按钮
        textDays = getActivity().findViewById(R.id.text_me_days);
        textWordNum = getActivity().findViewById(R.id.text_me_words);
        textMoney = getActivity().findViewById(R.id.text_me_money);
        layoutData = getActivity().findViewById(R.id.layout_me_analyse);
        layoutPlan = getActivity().findViewById(R.id.layout_me_plan);
        aSwitchNight = getActivity().findViewById(R.id.switch_night);
        layoutAlarm = getActivity().findViewById(R.id.layout_me_alarm);
        layoutNotify = getActivity().findViewById(R.id.layout_me_notify);
        layoutMoney = getActivity().findViewById(R.id.layout_me_money);
        layoutAbout = getActivity().findViewById(R.id.layout_me_about);
        layoutSyno = getActivity().findViewById(R.id.layout_me_syno);

        // 从 SharedPreferences 获取用户名
        SharedPreferences spfRecord = getActivity().getSharedPreferences("spfRecord", getActivity().MODE_PRIVATE);
        String name = spfRecord.getString("name", "默认用户"); // 默认值为 "默认用户"，如果没有存储

        // 设置到 tvNameText TextView
        tvNameText.setText(name);

        // 添加点击事件
        layoutCalendar.setOnClickListener(this);
        layoutWordList.setOnClickListener(this);
        layoutData.setOnClickListener(this);
        layoutPlan.setOnClickListener(this);
        layoutAlarm.setOnClickListener(this);
        layoutNotify.setOnClickListener(this);
        layoutAbout.setOnClickListener(this);
        layoutSyno.setOnClickListener(this);
        tvNameText.setOnClickListener(this);  // tvNameText 点击跳转到 UserProfileActivity
        userprofile.setOnClickListener(this); // 保持原有的 userprofile 按钮点击事件
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.layout_me_calendar:
                intent.setClass(getActivity(), CalendarActivity.class);
                break;
            case R.id.tv_name_text:  // 点击用户名跳转到 UserProfileActivity
            case R.id.userprofile:  // 保持原有的 userprofile 按钮点击事件
                intent.setClass(getActivity(), UserProfileActivity.class);
                break;
            case R.id.layout_me_word_list:
                intent.setClass(getActivity(), ListActivity.class);
                break;
            case R.id.layout_me_analyse:
                intent.setClass(getActivity(), ChartActivity.class);
                break;
            case R.id.layout_me_plan:
                intent.setClass(getActivity(), PlanActivity.class);
                break;
            case R.id.layout_me_alarm:
                intent.setClass(getActivity(), AlarmActivity.class);
                break;
            case R.id.layout_me_notify:
                intent.setClass(getActivity(), LearnInNotifyActivity.class);
                break;
            case R.id.layout_me_about:
                intent.setClass(getActivity(), AboutActivity.class);
                break;
            case R.id.layout_me_syno:
                intent.setClass(getActivity(), SynchronyActivity.class);
                break;
        }
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateData();
    }

    private void updateData() {
        // 更新天数、单词数和铜板数
        List<MyDate> myDateList = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(MyDate.class);
        textDays.setText(String.valueOf(myDateList.size()));
        List<User> userList = LitePal.where("userId = ?", ConfigData.getSinaNumLogged() + "").find(User.class);
        textWordNum.setText(String.valueOf(userList.get(0).getUserWordNumber()));
        textMoney.setText(String.valueOf(userList.get(0).getUserMoney()));

        // 每次更新时，刷新用户名
        SharedPreferences spfRecord = getActivity().getSharedPreferences("spfRecord", getActivity().MODE_PRIVATE);
        String name = spfRecord.getString("name", "默认用户"); // 获取最新的用户名
        tvNameText.setText(name);  // 更新 tvNameText 的显示内容
    }

}
