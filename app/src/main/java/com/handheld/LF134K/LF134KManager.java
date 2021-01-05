package com.handheld.LF134K;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

//import cn.pda.serialport.ISerialPort;
import cn.pda.serialport.*; //MockSerialPort;
//import cn.pda.serialport.SerialPort;
//import cn.pda.serialport.Tools;

/**
 * 134.2K接口 interface
 * @author admin
 *
 */
public class LF134KManager {
    private ISerialPort mSerialport ;
    private InputStream mIn ;
    private OutputStream mOut ;
    public static int Port = 0 ;
    public static int BaudRate = 9600 ;
    public static int Power = ISerialPort.Power_5v;
    public static final int MSG_RFID_134K = 1101 ;
    public static final String KEY_134K_ID = "134k_id" ;
    public static final String KEY_134K_COUNTRY = "134k_country" ;

    private Handler handler = null ;
    private boolean running = true ;
    private boolean startFlag = false ;

    private String TAG = "RFIDService" ;
    public LF134KManager(Handler handler) throws SecurityException, IOException {
        this.handler = handler ;
        mSerialport = new SerialPort(Port, BaudRate,0) ;
        //mSerialport = new MockSerialPort(Port, BaudRate,0) ;
//		Log.e("port", Port+":"+BaudRate+":"+Power);
        //open power
        switch (Power) {
            case ISerialPort.Power_Scaner:
                mSerialport.scaner_poweron();
                break;
            case ISerialPort.Power_3v3:
                mSerialport.power_3v3on();
                break;
            case ISerialPort.Power_5v:
                mSerialport.power_5Von();
                break;
            case ISerialPort.Power_Psam:
                mSerialport.psam_poweron();
                break;
            case ISerialPort.Power_Rfid:
                mSerialport.rfid_poweron();
                break;
        }
        mIn = mSerialport.getInputStream() ;
        mOut = mSerialport.getOutputStream() ;
        sleep(500) ;
        //clear useless data
        byte[] temp = new byte[16] ;
        mIn.read(temp);
        readThread = new ReadThread();
        readThread.start();
    }
    ReadThread readThread;
    public void setHandler(Handler handler){
        this.handler = handler ;
    }


    //read id thread
    private class ReadThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(running){
                if(startFlag){
//					Log.e("134.2k", "running");
                    LF134KManager.sleep(10) ;
                    findCardCMD() ;//发鿁指仿 Imitation
                    getRecv() ;
                }
            }
        }
    }

    //获取串口返回数据 Obtenir les données de retour du port série
    public byte[] getRecv(){
        byte[] buffer = new byte[512] ;
        byte[] recv = null ;
        int size = 0 ;
        int available = 0 ;
        try{
            available = mIn.available() ;
            if(available > 0){
                sleep(40) ;
                size = mIn.read(buffer) ;
                if(size > 0){
                    recv = new byte[size] ;
                    System.arraycopy(buffer, 0, recv, 0, size) ;
                    if(recv != null){
                        Log.d(TAG,  "Received : " + Tools.Bytes2HexString(recv, recv.length)) ;
                        //resolveData(recv) ;
                        Lf134kDataModel model = this.getData(recv);
                        sendMsg(Tools.BytesToLong(model.Country) + "", Tools.BytesToLong(model.ID) + "");
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace() ;
        }
        return recv ;
    }

    //解析返回数据匿 Analyse et renvoi des données
    private Map<Integer, Integer> resolveData(byte[] recv){
        Log.d(TAG, Tools.Bytes2HexString(recv, recv.length));
        Map<Integer, Integer> map = null;
        int countryCode ;
        int id ;
        //丿条完整的数据包长度为88 La longueur d'un paquet complet est de 88
        if(recv.length > 7){
            //E7 03 C6 FB 65 08 00 8F
            map = new HashMap<Integer, Integer>();
            //如国家代码 Tels que le code du pays：E7 03--->0x03E7--十进制 Décimal int--->999
            countryCode = (recv[0]&0xff) + (recv[1] &0xff)*256 ;
            //ID：C6 FB 65 08 00--->0x000865FBC6--十进制 Décimal int--->140901318
            id = ((recv[2]&0xff) + (recv[3] &0xff)*256 + (recv[4] &0xff)*256*256
                    + (recv[5] &0xff)*256*256*256 + (recv[6] &0xff)*256*256*256*256) ;
            //返回数据 Renvoyer les données
            //sendMsg(countryCode, id);
            map.put(id, countryCode) ;
        }
        return map ;
    }

    private Lf134kDataModel getData(byte[] buffer) {
        Lf134kDataModel model = new Lf134kDataModel();
        byte[] id = new byte[10];
        byte[] nation = new byte[4];
        byte[] reserved = new byte[4];
        byte[] extend = new byte[6];
        System.arraycopy(buffer, 1, id, 0, 10);
        System.arraycopy(buffer, 11, nation, 0, 4);
        model.DataBlock = (byte) (buffer[15] - 30);
        model.AnamalFlag = (byte) (buffer[16] - 30);
        System.arraycopy(buffer, 17, reserved, 0, 4);
        System.arraycopy(buffer, 21, extend, 0, 6);
        for (int i = 0; i < 10; i++) {
            model.ID[i] = id[9 - i];
        }
        for (int i2 = 0; i2 < 4; i2++) {
            model.Country[i2] = nation[3 - i2];
        }
        for (int i3 = 0; i3 < 4; i3++) {
            model.Reserved[i3] = reserved[3 - i3];
        }
        for (int i4 = 0; i4 < 6; i4++) {
            model.Extend[i4] = extend[5 - i4];
        }
        try {
            model.ID = Tools.HexString2Bytes(new String(model.ID, "US-ASCII"));
            model.Country = Tools.HexString2Bytes(new String(model.Country, "US-ASCII"));
            model.Reserved = Tools.HexString2Bytes(new String(model.Reserved, "US-ASCII"));
            model.Extend = Tools.HexString2Bytes(new String(model.Extend, "US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (buffer[29] == 3) {
            model.Type = "FDX-B";
        }
        if (buffer[29] == 7) {
            model.Type = "HDX";
        }
        if (Tools.BytesToLong(id) == 0 && Tools.BytesToLong(nation) == 0) {
            return null;
        }
        return model;
    }

    private void sendMsg(String countryCode, String id){
        if(handler != null){
            Message msg = new Message() ;
            Bundle bundle = new Bundle() ;
            msg.what = MSG_RFID_134K ;
            bundle.putString(KEY_134K_ID, id); ;
            bundle.putString(KEY_134K_COUNTRY, countryCode) ;
            msg.setData(bundle) ;
            Log.d(TAG, "sendMsg: id " + id);
            handler.sendMessage(msg) ;
        }
    }


//	//计算校验咿 Vérification du calcul
//	private byte checkCRC(byte[] recv){
//		byte crc = 0 ;
//		for(int i = 0 ; i < 7 ; i++){
//			crc = (byte) (crc^recv[i]) ;
//		}
//		return crc ;
//	}

    //发鿁寻卡指仿 // Trouvez l'imitation du doigt de la carte
    public void findCardCMD(){
        byte[] cmd = new byte[]{(byte)0xAA};
        try {
            mOut.write(cmd) ;
            mOut.flush() ;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //弿始读卿  Lire Qing
    public void startRead(){
        startFlag = true ;
        mSerialport.rfid_poweron() ;

    }

    //停止读卡 Arrêter de lire la carte
    public void stopRead(){
        Log.d(TAG, "stopRead: ");
        startFlag = false ;
        mSerialport.rfid_poweroff() ;

    }

    //close rfid reader
    public void Close(){
        try{
            running = false ;
            startFlag = false ;
            readThread.interrupt();
            sleep(100) ;
            if(mOut != null){
                mOut.close() ;
            }
            if(mIn != null){
                mIn.close() ;
            }
            if(mSerialport != null){
                switch (Power) {
                    case SerialPort.Power_Scaner:
                        mSerialport.scaner_poweroff();
                        break;
                    case SerialPort.Power_3v3:
                        mSerialport.power_3v3off();
                        break;
                    case SerialPort.Power_5v:
                        mSerialport.power_5Voff();
                        break;
                    case SerialPort.Power_Psam:
                        mSerialport.psam_poweroff();
                        break;
                    case SerialPort.Power_Rfid:
                        mSerialport.rfid_poweroff();
                        break;
                }
                mSerialport.close(Port) ;
                Log.d(TAG, "Close port " + Port);
            }
        }catch(Exception e){

        }
    }

    //delay
    private static void sleep(final int time ){
        try {
            Thread.sleep(time) ;
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
