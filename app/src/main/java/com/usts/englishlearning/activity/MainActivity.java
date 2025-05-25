package com.usts.englishlearning.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.usts.englishlearning.R;
import com.usts.englishlearning.activity.index.FragmentMe;
import com.usts.englishlearning.activity.index.FragmentReview;
import com.usts.englishlearning.activity.index.FragmentWord;
import com.usts.englishlearning.config.ConfigData;
import com.usts.englishlearning.util.ActivityCollector;

public class MainActivity extends BaseActivity {

    private Fragment fragWord, fragReview, fragMe;
    private Fragment[] fragments;

    // 记录上一个显示的 Fragment
    public static int lastFragment = 0;

    private BottomNavigationView bottomNavigationView;
    private LinearLayout linearLayout;

    private static final String TAG = "MainActivity";

    public static boolean needRefresh = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置夜间模式
        boolean isNightMode = ConfigData.getIsNight();
        AppCompatDelegate.setDefaultNightMode(isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        // 恢复上次显示的 Fragment 索引
        if (savedInstanceState != null) {
            lastFragment = savedInstanceState.getInt("last_fragment", 0);
        }

        init();
        initFragment(); // 使用 lastFragment 初始化对应的 Fragment

        // 确保导航栏与 Fragment 状态同步
        bottomNavigationView.getMenu().getItem(lastFragment).setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            int newFragmentIndex = -1;
            switch (menuItem.getItemId()) {
                case R.id.bnavigation_word:
                    newFragmentIndex = 0;
                    break;
                case R.id.bnavigation_review:
                    newFragmentIndex = 1;
                    break;
                case R.id.bnavigation_me:
                    newFragmentIndex = 2;
                    break;
            }

            if (newFragmentIndex != -1) {
                switchFragment(lastFragment, newFragmentIndex);
            }
            return true; // 确保返回 true 表示事件已处理
        });

    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("last_fragment", lastFragment); // 保存当前 Fragment 索引
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        lastFragment = savedInstanceState.getInt("last_fragment", 0);

        // 重新初始化 Fragment
        initFragment();
    }



    // 初始化控件
    private void init() {
        bottomNavigationView = findViewById(R.id.bottom_nav);
        linearLayout = findViewById(R.id.linear_frag_container);
    }

    // 初始化 Fragment
    private void initFragment() {
        fragWord = new FragmentWord();
        fragReview = new FragmentReview();
        fragMe = new FragmentMe();
        fragments = new Fragment[]{fragWord, fragReview, fragMe};

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // 移除所有可能存在的 Fragment
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            transaction.remove(fragment);
        }
        transaction.commitNow();

        // 根据 lastFragment 添加并显示当前的 Fragment
        transaction = getSupportFragmentManager().beginTransaction();
        Fragment currentFragment = fragments[lastFragment];
        if (!currentFragment.isAdded()) {
            transaction.add(R.id.linear_frag_container, currentFragment);
        }
        transaction.show(currentFragment).commit();

        // 同步导航栏状态
        bottomNavigationView.getMenu().getItem(lastFragment).setChecked(true);
    }




    // 切换 Fragment
    private void switchFragment(int lastIndex, int index) {
        if (lastIndex == index) return; // 防止重复切换

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // 隐藏上一个 Fragment
        Fragment lastFragmentInstance = fragments[lastIndex];
        if (lastFragmentInstance.isAdded()) {
            transaction.hide(lastFragmentInstance);
        }

        // 显示目标 Fragment，如果尚未添加则添加
        Fragment nextFragmentInstance = fragments[index];
        if (!nextFragmentInstance.isAdded()) {
            transaction.add(R.id.linear_frag_container, nextFragmentInstance);
        }
        transaction.show(nextFragmentInstance).commit();

        // 更新状态
        lastFragment = index;

        // 同步导航栏选中状态
        bottomNavigationView.getMenu().getItem(index).setChecked(true);
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("今天不再背单词了吗？")
                .setPositiveButton("退出", (dialog, which) -> {
                    needRefresh = true;
                    ActivityCollector.finishAll();
                })
                .setNegativeButton("再看看", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}