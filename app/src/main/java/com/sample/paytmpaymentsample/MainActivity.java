package com.sample.paytmpaymentsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sample.paytmpaymentsample.api.PaytmChecksum;
import com.android.paytmpaymentsample.databinding.ActivityMainBinding;
import com.sample.paytmpaymentsample.model.Paytm;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.util.HashMap;
import java.util.TreeMap;


public class MainActivity extends AppCompatActivity implements PaytmPaymentTransactionCallback {

    private ActivityMainBinding binding;
    private String paytmChecksum;
    private static final String MID = "sfKydF00674006185270";
    private static final String MERCHANT_KEY = "p!CK%0z89e2GdAXA";
    private static final String WEBSITE = "WEBSTAGING";
    private static final String INDUSTRY_TYPE = "Retail";
    private static final String CHANNEL_ID = "WAP";
    private static final String TRANSACTION_URL = "https://securegw-stage.paytm.in/order/process";
    private static final String CALLBACK_URL = "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID=";
    private static final String TXN_AMOUNT = "10";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final Paytm paytm = new Paytm(MID, CHANNEL_ID, TXN_AMOUNT, WEBSITE, CALLBACK_URL, INDUSTRY_TYPE);

        binding.btnPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateChecksum(paytm);
            }
        });

        binding.btnPayPal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PaypalPaymentActivity.class);
                startActivity(intent);
            }
        });

    }

    private void generateChecksum(Paytm paytm) {
        /* initialize an hash */
        TreeMap<String, String> paytmParams = new TreeMap<String, String>();
        paytmParams.put("MID", paytm.getmId());
        paytmParams.put("ORDERID", paytm.getOrderId());
        paytmParams.put("CUST_ID", paytm.getCustId());
        paytmParams.put("CHANNEL_ID", paytm.getChannelId());
        paytmParams.put("TXN_AMOUNT", paytm.getTxnAmount());
        paytmParams.put("WEBSITE", paytm.getWebsite());
        paytmParams.put("CALLBACK_URL", paytm.getCallBackUrl());
        paytmParams.put("INDUSTRY_TYPE_ID", paytm.getIndustryTypeId());
        /**
         * Generate checksum by parameters we have
         * Find your Merchant Key in your Paytm Dashboard at https://dashboard.paytm.com/next/apikeys
         */
        try {
            paytmChecksum = PaytmChecksum.generateSignature(paytmParams, MERCHANT_KEY);
            Log.e("PaytmChecksum: ", paytmChecksum);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean isVerifySignature = false;
        try {
            isVerifySignature = PaytmChecksum.verifySignature(paytmParams, MERCHANT_KEY, paytmChecksum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isVerifySignature) {
            System.out.append("Checksum Matched");
            Log.e("Checksum: ", "Verified");
            initializePaytmPayment(paytmChecksum, paytm);
        } else {
            System.out.append("Checksum Mismatched");
        }
    }

    private void initializePaytmPayment(String checksumHash, Paytm paytm) {

        //getting paytm service
        PaytmPGService Service = PaytmPGService.getStagingService(TRANSACTION_URL);

        //use this when using for production
        //PaytmPGService Service = PaytmPGService.getProductionService();

        //creating a hashmap and adding all the values required
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("MID", MID);
        paramMap.put("ORDER_ID", paytm.getOrderId());
        paramMap.put("CUST_ID", paytm.getCustId());
        paramMap.put("CHANNEL_ID", paytm.getChannelId());
        paramMap.put("TXN_AMOUNT", paytm.getTxnAmount());
        paramMap.put("WEBSITE", paytm.getWebsite());
        paramMap.put("CALLBACK_URL", paytm.getCallBackUrl());
        paramMap.put("CHECKSUMHASH", checksumHash);
        paramMap.put("INDUSTRY_TYPE_ID", paytm.getIndustryTypeId());


        //creating a paytm order object using the hashmap
        PaytmOrder order = new PaytmOrder(paramMap);

        //intializing the paytm service
        Service.initialize(order, null);

        //finally starting the payment transaction
        Service.startPaymentTransaction(this, true, true, this);

    }

    @Override
    public void onTransactionResponse(Bundle inResponse) {
        Toast.makeText(this, inResponse.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void networkNotAvailable() {
        Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show();
    }

    @Override
    public void clientAuthenticationFailed(String inErrorMessage) {
        Toast.makeText(this, inErrorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void someUIErrorOccurred(String inErrorMessage) {
        Toast.makeText(this, inErrorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
        Toast.makeText(this, inErrorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressedCancelTransaction() {
        Toast.makeText(this, "Back Pressed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
        Toast.makeText(this, inErrorMessage + inResponse.toString(), Toast.LENGTH_LONG).show();
    }
}