package com.usts.englishlearning.activity;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.usts.englishlearning.R;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etSchool, etSign;
    private TextView tvBirthday;
    private RadioButton rbBoy, rbGirl;
    private Spinner spCity;
    private ImageView btnBack, profileImage;
    private Button btnTakePhoto, btnChooseGallery, btnSave;

    private String birthday, gender, selectedCity;
    private Uri photoUri; // 保存照片的 URI
    private ActivityResultLauncher<Uri> takePhotoLauncher;
    private ActivityResultLauncher<String> chooseGalleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initView();
        initData();
        initEvent();
        initActivityResultLaunchers();
    }

    private void initView() {
        etName = findViewById(R.id.et_name_text);
        etSchool = findViewById(R.id.et_school_text);
        etSign = findViewById(R.id.et_sign_text);
        tvBirthday = findViewById(R.id.et_birthday_text);
        rbBoy = findViewById(R.id.rb_boy);
        rbGirl = findViewById(R.id.rb_girl);
        spCity = findViewById(R.id.sp_city_text);
        btnBack = findViewById(R.id.btn_back);
        profileImage = findViewById(R.id.profile_image);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnChooseGallery = findViewById(R.id.btn_choose_gallery);
        btnSave = findViewById(R.id.btn_save);

        etName.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                // 按下回车时取消焦点并隐藏软键盘
                clearFocusAndHideKeyboard(view);
                return true;  // 表示事件已处理
            }
            return false;
        });

        etSchool.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                // 按下回车时取消焦点并隐藏软键盘
                clearFocusAndHideKeyboard(view);
                return true;
            }
            return false;
        });

        etSign.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                // 按下回车时取消焦点并隐藏软键盘
                clearFocusAndHideKeyboard(view);
                return true;
            }
            return false;
        });

        spCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCity = parent.getItemAtPosition(position).toString(); // 更新选中城市
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 保留默认值
            }
        });
    }
    private void clearFocusAndHideKeyboard(View view) {
        view.clearFocus();  // 清除焦点
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void initData() {
        SharedPreferences spfRecord = getSharedPreferences("spfRecord", MODE_PRIVATE);
        etName.setText(spfRecord.getString("name", ""));
        etSchool.setText(spfRecord.getString("school", ""));
        etSign.setText(spfRecord.getString("sign", ""));

        // 读取生日
        String savedBirthday = spfRecord.getString("birthday", null);
        if (savedBirthday != null) {
            birthday = savedBirthday;
            tvBirthday.setText(savedBirthday);
        } else {
            tvBirthday.setText("请选择生日");
        }

        gender = spfRecord.getString("gender", "");
        selectedCity = spfRecord.getString("city", "北京"); // 默认城市设置为北京
        String avatarUriString = spfRecord.getString("avatarUri", null);

        // 加载头像
        if (avatarUriString != null) {
            Uri avatarUri = Uri.parse(avatarUriString);
            if (new File(avatarUri.getPath()).exists()) {
                profileImage.setImageURI(avatarUri);
            } else {
                profileImage.setImageResource(R.drawable.head2); // 默认头像
            }
        } else {
            profileImage.setImageResource(R.drawable.head2); // 默认头像
        }

        if ("男".equals(gender)) rbBoy.setChecked(true);
        else if ("女".equals(gender)) rbGirl.setChecked(true);

        String[] cities = getResources().getStringArray(R.array.cities);
        for (int i = 0; i < cities.length; i++) {
            if (cities[i].equals(selectedCity)) {
                spCity.setSelection(i); // 设置 Spinner 的选中项
                break;
            }
        }
    }

    private void initEvent() {
        tvBirthday.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                birthday = year + "年" + (month + 1) + "月" + dayOfMonth + "日";
                tvBirthday.setText(birthday);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        btnBack.setOnClickListener(v -> onBackPressed());
        btnTakePhoto.setOnClickListener(v -> takePhoto());
        btnChooseGallery.setOnClickListener(v -> chooseFromGallery());
        btnSave.setOnClickListener(v -> saveData());
    }

    private void initActivityResultLaunchers() {
        takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), isSuccess -> {
            if (isSuccess) {
                profileImage.setImageURI(photoUri);
                saveAvatarUri(photoUri); // 保存头像 URI
                Toast.makeText(this, "照片已设置为头像", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "取消拍照", Toast.LENGTH_SHORT).show();
            }
        });

        chooseGalleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                profileImage.setImageURI(uri);
                saveAvatarUri(uri); // 保存头像 URI
                Toast.makeText(this, "已设置为头像", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void takePhoto() {
        File photoFile = createImageFile();
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(this, "com.usts.englishlearning.fileprovider", photoFile);
            takePhotoLauncher.launch(photoUri);
        } else {
            Toast.makeText(this, "无法创建文件", Toast.LENGTH_SHORT).show();
        }
    }

    private void chooseFromGallery() {
        chooseGalleryLauncher.launch("image/*");
    }

    private File createImageFile() {
        try {
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile("JPEG_" + System.currentTimeMillis() + "_", ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveAvatarUri(Uri uri) {
        SharedPreferences spfRecord = getSharedPreferences("spfRecord", MODE_PRIVATE);
        SharedPreferences.Editor editor = spfRecord.edit();
        editor.putString("avatarUri", uri.toString());  // 保存头像 URI
        editor.apply();
    }

    private void saveData() {
        String name = etName.getText().toString();
        String school = etSchool.getText().toString();
        String sign = etSign.getText().toString();
        gender = rbBoy.isChecked() ? "男" : "女";

        // 校验输入长度
        if (name.length() > 20) {
            Toast.makeText(this, "昵称超过输入限制", Toast.LENGTH_SHORT).show();
            return;
        }

        if (school.length() > 20) {
            Toast.makeText(this, "学校名称超过输入限制", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sign.length() > 100) {
            Toast.makeText(this, "签名超过输入限制", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty() || school.isEmpty() || birthday == null || birthday.equals("请选择生日")) {
            Toast.makeText(this, "请完整填写信息", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences spfRecord = getSharedPreferences("spfRecord", MODE_PRIVATE);
        SharedPreferences.Editor editor = spfRecord.edit();
        editor.putString("name", name);
        editor.putString("school", school);
        editor.putString("sign", sign);
        editor.putString("birthday", birthday);
        editor.putString("gender", gender);
        editor.putString("city", selectedCity);
        editor.apply();

        Toast.makeText(this, "信息保存成功", Toast.LENGTH_SHORT).show();
        finish();
    }
}
