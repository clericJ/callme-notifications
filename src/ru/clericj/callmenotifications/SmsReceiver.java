package ru.clericj.callmenotifications;

import java.io.InputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
	public static int NOTIFY_ID = 0;
	public static final String SMS_EXTRA_NAME = "pdus";

	@Override
	public void onReceive(Context context, Intent intent) {
		// Get SMS map from Intent
		Bundle extras = intent.getExtras();

		if (extras != null) {
			// Get received SMS array
			Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);

			// Берём только первую СМС
			SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[0]);
			InputStream settings = context.getResources().openRawResource(
					R.raw.parsersettings);

			SmsParser parser = new SmsParser(sms, settings);
			if (parser.isCallMeSms()) {

				String phoneNumber = parser.getPhoneNumber();
				String who = getContactDisplayNameByNumber(context, phoneNumber);
				if (who.isEmpty()) {
					who = phoneNumber;
				}
				String text = who + " просит перезвонить";
				sendNotification(context, intent, phoneNumber, text);
				this.abortBroadcast();
				
				playNotificationSound(context);
			}
		}
	}

	private String getContactDisplayNameByNumber(Context context, String number) {
		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));

		String name = "";
		ContentResolver resolver = context.getContentResolver();
		Cursor lookup = resolver.query(uri, new String[] { BaseColumns._ID,
				ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

		try {
			if (lookup != null && lookup.getCount() > 0) {
				lookup.moveToNext();
				name = lookup.getString(lookup
						.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
			}
		} finally {
			if (lookup != null) {
				lookup.close();
			}
		}
		return name;
	}

	private void playNotificationSound(Context context) {
		Uri soundUri = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		if (soundUri != null) {
			Ringtone sound = RingtoneManager.getRingtone(context, soundUri);
			sound.play();
		}
	}

	private void sendNotification(Context context, Intent intent,
			String phoneNumber, String text) {

		Notification notify = new Notification(R.drawable.call_black, text,
				System.currentTimeMillis());

		NotificationManager notifier = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent toLaunch = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
				+ phoneNumber));
		PendingIntent intentBack = PendingIntent.getActivity(context, 0,
				toLaunch, 0);

		notify.flags |= Notification.FLAG_AUTO_CANCEL;
		notify.setLatestEventInfo(context, text, "Нажимте чтобы перезвонить ("
				+ phoneNumber + ")", intentBack);

		notifier.notify(NOTIFY_ID, notify);
		NOTIFY_ID += 1;
	}
}
