package com.secuxtech.testapp;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import com.secuxtech.paymentkit.SecuXAccountManager;
import com.secuxtech.paymentkit.SecuXCoinAccount;
import com.secuxtech.paymentkit.SecuXPaymentManager;
import com.secuxtech.paymentkit.SecuXServerRequestHandler;
import com.secuxtech.paymentkit.SecuXStoreInfo;
import com.secuxtech.paymentkit.SecuXUserAccount;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private SecuXPaymentManager mPaymentManager = new SecuXPaymentManager();
    private SecuXAccountManager mAccountManager = new SecuXAccountManager();

    private SecuXUserAccount account = new SecuXUserAccount("maochuntest26@secuxtech.com", "12345678");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String serverUrl = "https://pmsweb-sandbox.secuxtech.com";

        mAccountManager.setBaseServer(serverUrl);
        mAccountManager.setAdminAccount("bitsense_register", "!bitsense_register@168");


    }

    @Override
    protected void onResume() {
        super.onResume();

        showProgress("Login...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Pair<Integer, String> ret = mAccountManager.registerUserAccount(account, "DCT", "BEE");

                ret = mAccountManager.loginUserAccount(account);
                hideProgressInMain();
                if (ret.first != SecuXServerRequestHandler.SecuXRequestOK) {

                    showAlertInMain("Login failed! Invalid email account or password", ret.second);
                    return;
                }

                Setting.getInstance().mUserLogout = false;
                Setting.getInstance().mAccount = account;

                if (!loadAccounts()) {

                    showMessageInMain("Get coin token account list failed!");
                    return;
                }
            }
        }).start();
    }

    private Boolean loadAccounts(){
        Pair<Integer, String> ret = mAccountManager.getCoinAccountList(Setting.getInstance().mAccount);
        if (ret.first!= SecuXServerRequestHandler.SecuXRequestOK){


            return false;
        }

        ArrayList<CoinTokenAccount> tokenAccountArray = AccountUtil.getCoinTokenAccounts();
        for(CoinTokenAccount account : tokenAccountArray){
            mAccountManager.getAccountBalance(Setting.getInstance().mAccount, account.mCoinType, account.mToken);
        }

        return true;
    }


    public void onTestButtonClick(View v){

        showProgress("Loading...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                handlePaymentInfo("{\"amount\":\"1\", \"coinType\":\"DCT:BEE\", \"nonce\":\"d7728ffe\", \"deviceIDhash\":\"4afff62e0b314266d9e1b3a48158d56134331a9f\"}");
                hideProgressInMain();
            }
        }).start();

    }

    public void handlePaymentInfo(String payinfoRaw){

        int startPos = payinfoRaw.indexOf("{");
        int endPos = payinfoRaw.indexOf("}");
        if (startPos == -1 || endPos == -1){
            showAlertInMain("Invalid QRCode", "");
            return;
        }

        final String payinfo = payinfoRaw.substring(startPos, endPos+1);

        boolean refundFlag=false, refillFlag=false;
        String nonce = "";
        int devMedia = 0;
        try {
            JSONObject infoJson = new JSONObject(payinfo);
            String refundAmount = infoJson.optString("refund");
            String refillAmount = infoJson.optString("refill");
            String devIDHash = infoJson.getString("deviceIDhash");
            nonce = infoJson.optString("nonce");

            String devMediaStr = infoJson.optString("media");
            if (devMediaStr != null && devMediaStr.length()>0){
                devMedia = Integer.parseInt(devMediaStr);
            }

            if (refundAmount.length() > 0){
                return;

            }else if (refillAmount.length() > 0){
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            showAlertInMain("Invalid QRCode", "");
            return;

        }

        final boolean refundOpt = refundFlag, refillOpt = refillFlag;
        final String payNonce = nonce;
        final int payDevMedia = devMedia;

        Pair<Integer, String> ret = mPaymentManager.getDeviceInfo(payinfo);
        if (ret.first== SecuXServerRequestHandler.SecuXRequestUnauthorized){

            return;

        }else if (ret.first==SecuXServerRequestHandler.SecuXRequestFailed){

            if (ret.second.contains("No token")){

                return;
            }

            return;
        }



        try{
            String amount = "0", coinType = "", token = "";
            final String payInfoReply = ret.second;

            JSONObject payinfoJson = new JSONObject(payInfoReply);
            if (payinfoJson.has("amount") && payinfoJson.getString("amount").compareTo("null")!=0) {
                amount = payinfoJson.getString("amount");
            }

            String coinTypeInfo = "";
            if (payinfoJson.has("coinType") && payinfoJson.getString("coinType").compareTo("null")!=0) {
                coinTypeInfo = payinfoJson.getString("coinType");

                if (coinTypeInfo.contains(":")){
                    coinType = coinTypeInfo.substring(0, coinTypeInfo.indexOf(':'));
                }else{
                    coinType = coinTypeInfo;
                }

            }

            if (payinfoJson.has("token") && payinfoJson.getString("token").compareTo("null")!=0) {
                token = payinfoJson.getString("token");
            }else if (coinTypeInfo.contains(":")){
                token = coinTypeInfo.substring(coinTypeInfo.indexOf(':')+1);
            }else{
                token = "";
            }

            final String devID = payinfoJson.getString("deviceID");
            final String devIDHash = payinfoJson.getString("deviceIDhash");
            if (devID.length()==0){

                showAlertInMain("Unsupported device!", "");
                return;
            }

            Pair<Pair<Integer, String>, SecuXStoreInfo> storeInfoRet = mPaymentManager.getStoreInfo(devIDHash);

            if (storeInfoRet.first.first != SecuXServerRequestHandler.SecuXRequestOK || storeInfoRet.second == null){
                if (storeInfoRet.first.first== SecuXServerRequestHandler.SecuXRequestUnauthorized ||
                        storeInfoRet.first.second.contains("No token")){


                    return;

                }

                showAlertInMain("Get store info. failed!", storeInfoRet.first.second);
                return;
            }


            ArrayList<CoinTokenAccount> coinTokenAccountList = new ArrayList<>();
            final SecuXStoreInfo storeInfo = storeInfoRet.second;
            if (coinType.length() == 0 || token.length() == 0){
                for(Pair<String, String> info : storeInfo.mCoinTokenArr){
                    SecuXCoinAccount coinAccount = Setting.getInstance().mAccount.getCoinAccount(info.first);
                    if (coinAccount != null && coinAccount.getBalance(info.second) != null) {
                        CoinTokenAccount coinTokenAccount = new CoinTokenAccount(coinAccount, info.second);
                        coinTokenAccountList.add(coinTokenAccount);
                    }
                }

            }else{
                SecuXCoinAccount coinAcc = Setting.getInstance().mAccount.getCoinAccount(coinType);
                if (coinAcc !=null && coinAcc.getBalance(token) != null){
                    coinTokenAccountList.add(new CoinTokenAccount(coinAcc, token));
                }
            }

            if (coinTokenAccountList.size()==0){
                showAlertInMain("Unsupported Coin/Token Type!", "");
                return;
            }else{
                for (CoinTokenAccount account : coinTokenAccountList) {
                    mAccountManager.getAccountBalance(Setting.getInstance().mAccount, account.mCoinType, account.mToken);
                    account.mBalance = Setting.getInstance().mAccount.getCoinAccount(account.mCoinType).getBalance(account.mToken);
                }
            }

            if (refillOpt){

                return;
            }else if (refundOpt){

                return;
            }


            final String payAmount = amount;
            final ArrayList<CoinTokenAccount> accList = coinTokenAccountList;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(getApplicationContext(),"Scan result: "+scanContent, Toast.LENGTH_LONG).show();
                    PaymentDetailsActivity.mStoreInfo = storeInfo;
                    PaymentDetailsActivity.mCoinTokenAccountList = accList;
                    Intent newIntent = new Intent(mContext, PaymentDetailsActivity.class);
                    newIntent.putExtra(PaymentDetailsActivity.PAYMENT_INFO, payInfoReply);
                    newIntent.putExtra(PaymentDetailsActivity.PAYMENT_AMOUNT, payAmount);
                    newIntent.putExtra(PaymentDetailsActivity.PAYMENT_DEVID, devID);
                    newIntent.putExtra(PaymentDetailsActivity.PAYMENT_DEVIDHASH, devIDHash);
                    newIntent.putExtra(PaymentDetailsActivity.PAYMENT_NONCE, payNonce);
                    newIntent.putExtra(PaymentDetailsActivity.PAYMENT_MEDIA, payDevMedia);
                    startActivity(newIntent);
                }
            });
        }catch (Exception e){
            hideProgressInMain();
            showAlertInMain("Invalid payment information!", "");
            return;
        }

    }
}