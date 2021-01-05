/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package cn.pda.serialport;

import java.io.FileDescriptor;
import java.io.IOException;

/*
 * SerialPort for open device power
 */
public class MockSerialPort implements ISerialPort{
	private static final String TAG = "MockSerialPort";


	public static int TNCOM_EVENPARITY = 0;//
	public static int TNCOM_ODDPARITY = 1 ;//

	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private boolean trig_on=false;

	byte[] test;
	//
	public MockSerialPort(){}

	public MockSerialPort(int port, int baudrate, int flags) throws SecurityException, IOException {
	}
	// Getters and setters
	public java.io.InputStream getInputStream() {
		// 78187493520
		// byte[] initialArray = { 0x02, 0x30,0x39,0x38,0x37,0x36,0x35,0x34,0x33,0x32,0x31, 0x43,0x39,0x30,0x30, 0x30, 0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x7B,(byte)0x84,0x03 };,
		// 78187493392
		byte[] initialArray = { 0x02, 0x30,0x31,0x38,0x37,0x36,0x35,0x34,0x33,0x32,0x31, 0x43,0x39,0x30,0x30, 0x30, 0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x30,0x7B,(byte)0x84,0x03 };
		//byte[] initialArray = { 0,2,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,3,0,0,0,0xF,0xF,0,7};
		//InputStream targetStream = new ByteArrayInputStream(initialArray);
		//return targetStream;
		return new MockInputStream(initialArray);
	}

	public java.io.OutputStream getOutputStream() {
		return new MockOutputStream();
	}
	public void power_5Von() {
		//zigbeepoweron();
	}
	public void power_5Voff() {
		//zigbeepoweroff();
	}
	public void power_3v3on(){
		//power3v3on();
	}
	public void power_3v3off(){
		//power3v3off();
	}
	public void rfid_poweron(){
		//rfidPoweron();
	}
	public void rfid_poweroff(){
		//rfidPoweroff();
	}
	public void psam_poweron() {
	}
	public void psam_poweroff() {
		//scaner_trigoff();
	}
	public void scaner_poweron() {
	}
	public void scaner_poweroff() {
		}
	public void scaner_trigon() {
		trig_on=true;
	}
	public void scaner_trigoff() {
		trig_on=false;
	}
	public boolean scaner_trig_stat(){
		return trig_on;
	}

	public void close(int port) {};




//	public native void setPortParity(int mode); //
	
	public native void test(byte[] bytes);

}
