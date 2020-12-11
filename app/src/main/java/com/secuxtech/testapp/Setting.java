package com.secuxtech.testapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;


import androidx.core.content.ContextCompat;

import com.secuxtech.paymentkit.SecuXPaymentHistory;
import com.secuxtech.paymentkit.SecuXUserAccount;

import java.util.ArrayList;


import static android.content.Context.MODE_PRIVATE;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-20
 */
public class Setting {
    private static final Setting ourInstance = new Setting();
    public static Setting getInstance() {
        return ourInstance;
    }

    public SecuXUserAccount mAccount = null;
    public SecuXPaymentHistory mLastPaymentHis = null;

    public String mUserAccountName = "";
    public String mUserAccountPwd = "";
    public boolean mUserLogout = false;
    public boolean mEnableBioVerifyBeforePayment = true;

    public boolean mEnableRefundFlag = true;
    public boolean mEnableRefillFlag = true;

    public ArrayList<Pair<String, String>> mCoinTokenArray = new ArrayList<>();
    public boolean mTestModel = false;
    public boolean mTestSessionTimeout = false;

    public String mPaymentNFCInfo = "";

    public boolean mLogToFile = false;


    private Setting() {

    }

    public void saveSettings(Context context){
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("SecuXEvPay", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UserAccountName", mUserAccountName);
        editor.putString("UserAccountPwd", mUserAccountPwd);
        editor.putBoolean("EnableBioVerifyBeforePayment", mEnableBioVerifyBeforePayment);
        editor.apply();
    }

    public void loadSettings(Context context){
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences("SecuXEvPay", MODE_PRIVATE);
        mUserAccountName = settings.getString("UserAccountName", "");
        mUserAccountPwd = settings.getString("UserAccountPwd", "");
        mEnableBioVerifyBeforePayment = settings.getBoolean("EnableBioVerifyBeforePayment", true);
    }

}
