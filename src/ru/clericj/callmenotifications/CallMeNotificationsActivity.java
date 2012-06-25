package ru.clericj.callmenotifications;

import android.app.Activity;
import android.os.Bundle;

public class CallMeNotificationsActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

//        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED" );
//        filter.setPriority( IntentFilter.SYSTEM_HIGH_PRIORITY );
//        registerReceiver( new SmsReceiver(), filter );
    }
}