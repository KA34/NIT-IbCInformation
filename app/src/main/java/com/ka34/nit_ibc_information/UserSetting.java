package com.ka34.nit_ibc_information;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class UserSetting {
    public String grade;
    public String dep;
    public String clas;
    public String abroad;
    public List<Map<String,String>> parseData;
    public String time;
    // Preferenceのkeyは１つだけなので混乱ない　
    private static final String USER_SETTING_PREF_KEY="USER_SETTING";

    // 保存情報取得メソッド
    public static UserSetting getInstance(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String userSettingString = prefs.getString(USER_SETTING_PREF_KEY, "");

        UserSetting instance;
        // 保存したオブジェクトを取得
        if( !TextUtils.isEmpty(userSettingString)) {
            instance = gson.fromJson(userSettingString, UserSetting.class);
        }else {
            // 何も保存されてない 初期時点 この時はデフォルト値を入れて上げる
            instance = getDefaultInstance();
        }
        return instance;
    }

    // デフォルト値の入ったオブジェクトを返す
    public static UserSetting getDefaultInstance(){
        UserSetting instance = new UserSetting();
        instance.grade = null;
        instance.dep = null;
        instance.clas = null;
        instance.abroad = null;
        instance.parseData = null;
        instance.time = null;
        return instance;
    }

    // 状態保存メソッド
    public void savaInstance(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        // 現在のインスタンスの状態を保存
        prefs.edit().putString(USER_SETTING_PREF_KEY, gson.toJson(this)).apply();
    }
}