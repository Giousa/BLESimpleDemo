/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fsrk.ble.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;





/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
@SuppressLint("NewApi")
public class BluetoothLeService extends Service {

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	private int mConnectionState = STATE_DISCONNECTED;
	private BluetoothGattCharacteristic mBluetoothGattCharacteristic_Notify_01=null;
	private BluetoothGattCharacteristic mBluetoothGattCharacteristic_Notify_02=null;
	private BluetoothGattCharacteristic mBluetoothGattCharacteristic_Notify_03=null;
	private BluetoothGattCharacteristic mBluetoothGattCharacteristic_Notify=null;

	private String Tag="BluetoothLeService";

	private Handler mHandler=new Handler();

	private boolean receivedata=false;

	float value = 1f;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
	public final static String ACTION_NOTIFY= "com.example.bluetooth.le.ACTION_NOTIFY";
	public static final String BLE_TiZhong_Data = "ble.tizhong.value";
	public static final String BLE_change_Data = "ble.change.value";
	public static final String BLE_SHENGGAO_Data = "ble.shenggao.value";
	public static final String WRITE_SUCCEED = "write_succeed";
	public static final String SEARCH_DEVICE = "search_device";
	private List<UUIDBean>uuid_list=new ArrayList<UUIDBean>();

	private long time=0;

	int level = 1;
	int usersex =0;
	int age = 20;
	float high = 1.70f;// 单位

	public List<String>list=new ArrayList<String>();
	// Implements callback methods for GATT events that the app cares about. For
	// example,
	// connection change and services discovered.
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
											int newState) {
			String intentAction;
			System.out.println("=======status:" + status+",newState==="+newState);
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				//连接成功
				Log.e(Tag, "连接成功");
				sendBroadCast(ACTION_GATT_CONNECTED, "connected", gatt.getDevice().getName()+"连接成功");
				mBluetoothGatt.discoverServices();

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				//连接失败
				Log.e(Tag, "连接失败");
				sendBroadCast(ACTION_GATT_DISCONNECTED, "disconnected", gatt.getDevice().getName()+"连接失败");
			}
		}



		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.e(Tag, "status==="+status);
			if (status == BluetoothGatt.GATT_SUCCESS) {

				UUIDBean bean01=new UUIDBean(SampleGattAttributes.UUID_01, SampleGattAttributes.CH_01);
				UUIDBean bean02=new UUIDBean(SampleGattAttributes.UUID_02, SampleGattAttributes.CH_02);
				UUIDBean bean03=new UUIDBean(SampleGattAttributes.UUID_01, SampleGattAttributes.EWQ_UUID_Notify);
				UUIDBean bean04=new UUIDBean(SampleGattAttributes.UUID_FAT, SampleGattAttributes.CH_FAT);
				uuid_list.add(bean02);
				uuid_list.add(bean03);
				uuid_list.add(bean01);
				uuid_list.add(bean04);

				for(int i=0;i<list.size();i++){
					if(gatt.getDevice().getName().equals(list.get(i))){
						Log.e(Tag, "开始激活");
						for(UUIDBean bean:uuid_list){
							Log.e(Tag, "uuid==="+bean.getUuid()+",uuid_list.size==="+uuid_list.size());
							BluetoothGattService mBluetoothGattSevice= gatt.getService(UUID.fromString(bean.getUuid()));
							if(mBluetoothGattSevice!=null){
								mBluetoothGattCharacteristic_Notify=mBluetoothGattSevice.getCharacteristic(UUID.fromString(bean.getCh()));
								int charaProp = mBluetoothGattCharacteristic_Notify.getProperties();
								if((charaProp&BluetoothGattCharacteristic.PROPERTY_NOTIFY)>0){
									Log.e(Tag,"具有通知属性。。。");
									mBluetoothGatt.setCharacteristicNotification(mBluetoothGattCharacteristic_Notify, true);
									BluetoothGattDescriptor descriptor = mBluetoothGattCharacteristic_Notify.getDescriptor(UUID.fromString(SampleGattAttributes.CHARACTERISTIC_UUID_RETURN));
									if(descriptor!=null){
										descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
									}
								}
							}
						}

					}
				}

				List<BluetoothGattService> gattServices = gatt.getServices();
				Log.e(Tag, "获取服务gattServices==="+gattServices.size());
				for (BluetoothGattService gattService : gattServices) {
					for(UUIDBean bean:uuid_list){
						if(gattService.getUuid().toString().equals(bean.getUuid())){
							List<BluetoothGattCharacteristic>characteristics=gattService.getCharacteristics();
							for(BluetoothGattCharacteristic characteristic : characteristics){
								Log.e(Tag, "charac===="+characteristic.getUuid());
								if (SampleGattAttributes.WRITE.equals(characteristic.getUuid().toString())
										||SampleGattAttributes.WRITE_FAT.equals(characteristic.getUuid().toString())){
									SampleGattAttributes.SEND_CHARACT = characteristic;
									Log.e(Tag, "设置写成功");
								}
							}
						}
					}
				}


			}

		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic, int status) {
			System.out.println("onCharacteristicRead");
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
									  BluetoothGattDescriptor descriptor, int status) {
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic) {
			Log.e(Tag, "数据改变");
			if (characteristic.getValue() != null) {
				String codeString=bytesToHexString(characteristic.getValue());
				Log.e("MainActivity", codeString);
				Log.e(Tag, "codeString==="+codeString);
				for(int i=0;i<list.size();i++){
					if(gatt.getDevice().getName().equals(list.get(i))){
						//不处理，自已解析
						sendBroadCast(EXTRA_DATA, "ble_data","你获取的数据是从"+gatt.getDevice().getName()+"发出的"+ codeString);
					}
//					sendBroadCast(EXTRA_DATA, "ble_data","你获取的数据是从"+gatt.getDevice().getName()+"发出的"+ codeString);

				}
			}
		}



		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
		}

		public void onCharacteristicWrite(BluetoothGatt gatt,
										  BluetoothGattCharacteristic characteristic, int status) {
			sendBroadCast(WRITE_SUCCEED, "write_succeed", gatt.getDevice().getName()+"写入数据成功");


		};
	};

	public void setName(String name){
		list.clear();
		if(name.contains(",")){
			String[]names=name.split(",");
			for(int i=0;i<names.length;i++){
				list.add(names[i]);
			}
		}else{
			list.add(name);
		}
		Log.e("MainActivity", "list.size==="+list.size());
		scanLeDevice(true);

	}


	private void sendBroadCast(String action,String key,String value){
		Intent intent = new Intent(action);
		intent.putExtra(key, value);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}


	public class LocalBinder extends Binder {
		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	public  String bytesToHexString(byte[] src){
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		close();
		return super.onUnbind(intent);
	}

	@TargetApi(18)
	@SuppressLint("NewApi")
	public void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, 10*1000);
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	@SuppressLint("NewApi")
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			for(int i=0;i<list.size();i++){
				if(list.get(i).equals(device.getName())){
					sendBroadCast(SEARCH_DEVICE, "device_name", "搜到设备"+device.getName()+","+device.getAddress());
					connect(device.getAddress());
				}
			}
		}
	};



	private final IBinder mBinder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 *
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(Tag, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(Tag, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 *
	 * @param address
	 *            The device address of the destination device.
	 *
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			Log.e(Tag,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device. Try to reconnect. (先前连接的设备。 尝试重新连接)
		if (mBluetoothDeviceAddress != null
				&& address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.d(Tag,
					"Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				mConnectionState = STATE_CONNECTING;

				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(address);
		if (device == null) {
			Log.w(Tag, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		Log.e(Tag, "开始连接蓝牙设备");
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		Log.d(Tag, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;
		mConnectionState = STATE_CONNECTING;
		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(Tag, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	public void wirteCharacteristic(BluetoothGattCharacteristic characteristic) {

		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(Tag, "BluetoothAdapter not initialized");
			return;
		}

		mBluetoothGatt.writeCharacteristic(characteristic);

	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 *
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(Tag, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 *
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(Tag, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
				.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
		if (descriptor != null) {
			System.out.println("write descriptor");
			descriptor
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
		/*
		 * // This is specific to Heart Rate Measurement. if
		 * (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
		 * System
		 * .out.println("characteristic.getUuid() == "+characteristic.getUuid
		 * ()+", "); BluetoothGattDescriptor descriptor =
		 * characteristic.getDescriptor
		 * (UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
		 * descriptor
		 * .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		 * mBluetoothGatt.writeDescriptor(descriptor); }
		 */
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 *
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null)
			return null;

		return mBluetoothGatt.getServices();
	}

	public void setInfo(int level,int usersex,int age,float high){
		this.level=level;
		this.usersex=usersex;
		this.age=age;
		this.high=high;

	}


	public void sendLight(byte[] sum) {
		if (SampleGattAttributes.SEND_CHARACT!= null
				&& mBluetoothGatt != null) {
			Log.e(Tag, "脂肪称设置写成功");
			SampleGattAttributes.SEND_CHARACT.setValue(sum);
			Log.e("send",
					"####"
							+ mBluetoothGatt
							.writeCharacteristic(SampleGattAttributes.SEND_CHARACT)
							+ "       " + bytesToHexString(sum));
		}
	}
	/**
	 * Read the RSSI for a connected remote device.
	 * */
	public boolean getRssiVal() {
		if (mBluetoothGatt == null)
			return false;

		return mBluetoothGatt.readRemoteRssi();
	}
}
