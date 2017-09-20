package com.fsrk.ble.demo;

import com.fsrk.ble.service.BluetoothLeService;
import com.fsrk.ble.service.SampleGattAttributes;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml.Encoding;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private Button bt_send;
	private EditText et_content;
	private EditText et_ble_names;
	private TextView tv_log;
	private MainActivity context;
	private BluetoothLeService mBluetoothLeService;
	private Button bt_confirm;
	private String Tag="MainActivity";
	private Button bt_clean;
	private TextView tv_log2;

	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case 0x11:
					String log=(String)msg.obj;
					String []logs=log.split(",");
					tv_log.setText(logs[0]);
					//连接设备
					break;
				case 0x22:
					String status_c=(String)msg.obj;
					tv_log.setText(status_c);
					break;
				case 0x33:
					String status_d=(String)msg.obj;
					tv_log.setText(status_d);
					break;
				case 0x44:
					String ble_data=(String)msg.obj;
					tv_log.setText(ble_data);
					break;
				case 0x55:
					String write_success=(String)msg.obj;
					tv_log2.setText(write_success);
					break;
				case 0x66:
					String device_status=(String)msg.obj;
					tv_log.setText(device_status);
					break;

				default:
					break;
			}

		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context=this;
		setContentView(R.layout.activity_main);
		initViews();
		initEvents();
		Intent serviceIntent=new Intent(context,BluetoothLeService.class);
		bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, makeGattUpdateIntentFilter());
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	private  IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.EXTRA_DATA);
		intentFilter.addAction(BluetoothLeService.SEARCH_DEVICE);
		intentFilter.addAction(BluetoothLeService.WRITE_SUCCEED);
		intentFilter.addAction(BluetoothLeService.ACTION_NOTIFY);
		return intentFilter;
	}

	BroadcastReceiver receiver=new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			Message message=new Message();
			if(BluetoothLeService.SEARCH_DEVICE.equals(action)){
				String devices=intent.getStringExtra("device_name");
				message.what=0x11;
				message.obj=devices;
				handler.sendMessage(message);
			}else if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){
				String status=intent.getStringExtra("connected");
				message.what=0x22;
				message.obj=status;
				handler.sendMessage(message);
			}else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){
				String status=intent.getStringExtra("disconnected");
				message.what=0x33;
				message.obj=status;
				handler.sendMessage(message);
			}else if(BluetoothLeService.EXTRA_DATA.equals(action)){
				String data=intent.getStringExtra("ble_data");
				message.what=0x44;
				message.obj=data;
				handler.sendMessage(message);
			}else if(BluetoothLeService.WRITE_SUCCEED.equals(action)){
				String data=intent.getStringExtra("write_succeed");
				message.what=0x55;
				message.obj=data;
				handler.sendMessage(message);
			}else if(BluetoothLeService.ACTION_NOTIFY.equals(action)){
				String data=intent.getStringExtra("devicename_status");
				message.what=0x66;
				message.obj=data;
				handler.sendMessage(message);

			}

		}
	};




	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
									   IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			Log.e(Tag, "service成功");
			if (!mBluetoothLeService.initialize()) {
				finish();
			}
		}
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};



	private void initEvents() {
		bt_send.setOnClickListener(context);
		bt_clean.setOnClickListener(context);
		bt_confirm.setOnClickListener(context);

	}

	private void initViews() {
		et_content=(EditText)findViewById(R.id.et_content);
		et_ble_names=(EditText)findViewById(R.id.et_ble_names);
		bt_send=(Button)findViewById(R.id.bt_send);
		tv_log=(TextView)findViewById(R.id.tv_log);
		bt_confirm=(Button)findViewById(R.id.bt_confirm);
		bt_clean=(Button)findViewById(R.id.bt_clean);
		tv_log2=(TextView)findViewById(R.id.tv_log_02);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_send:
				String content=et_content.getText().toString().trim();
				if(TextUtils.isEmpty(content)){
					Toast.makeText(context, "请输入指令内容", 3000).show();
					return;
				}
				if(mBluetoothLeService!=null&&SampleGattAttributes.SEND_CHARACT!=null){
					Log.e(Tag, "mBluetoothLeService不为空");
					SampleGattAttributes.SEND_CHARACT.setValue(hexStringToBytes(content));
					mBluetoothLeService.wirteCharacteristic(SampleGattAttributes.SEND_CHARACT);
				}
				break;
			case R.id.bt_confirm:
				String names=et_ble_names.getText().toString().trim();
				if(TextUtils.isEmpty(names)){
					Toast.makeText(context, "请输入名称", 3000).show();
					return;
				}
				if(mBluetoothLeService!=null){
					Log.e(Tag, "设置名字");
					mBluetoothLeService.setName(names);
				}
				break;
			case R.id.bt_clean:
				tv_log.setText("");
				tv_log2.setText("");
				break;

			default:
				break;
		}

	}

	private  byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private  byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}



}
