package com.example.caucse.servicetest;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by CAUCSE on 2016-05-09.
 */
public class InternetConnectService extends Service {

    private final static String TAG = "InternetService";
    private final static String serverIP = "165.194.17.214";
    private boolean timerSwitch = false;
    private boolean tempValue = false;


    private static Socket connectSocket;
    private static DataOutputStream dataOutputStream;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private FifthTimerThread mfifth_1Thread = null;
    private FifthTimerThread mfifth_2Thread = null;
    private SocketThread mSocketThread;

    private static final int STATE_NONE = 0; // we're doing nothing
    private static final int STATE_LISTEN = 1; // now listening for incoming
    // connections
    private static final int STATE_CONNECTING = 2; // now initiating an outgoing
    // connection
    private static final int STATE_CONNECTED = 3; // now connected to a remote

    static final int MSG_SEND = 1;
    static final int MSG_REGISTER_CLIENT = 2;
    static final int MSG_UNREGISTER_CLIENT = 3;
    static final int MSG_SEND_TIMER = 4;
    static final int MSG_SEND_BUILDING = 5;
    static final int MSG_SEND_NUMBER = 6;
    static final int MSG_SEND_GENDER = 7;
    static final int MSG_SEND_AVAILABLE = 8;
    static final int MSG_SEND_ACCEPTTIME = 9;

    private static boolean isRunning = false;
    private int mState;

    static String buildings;
    static String number;
    static String gender;
    static String available;
    static String acceptTime;
    static String anotherData;

    static boolean setTime = false;

    static boolean fifth_1boolean = false;
    static boolean fifth_2boolean = false;



    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                default:
                    super.handleMessage(msg);
            }
            //super.handleMessage(msg);
        }
    }

    private void sendMessageToMain(String data, String subData, int kind) {
        switch(kind){
            case MSG_SEND_BUILDING:
                for (int num = mClients.size() - 1; num >= 0; num--) {
                    try {
                        Bundle b = new Bundle();
                        b.putString("data", data);
                        b.putString("subData",subData);
                        Message msg = Message.obtain(null, MSG_SEND_BUILDING);
                        msg.setData(b);
                        mClients.get(num).send(msg);
                        //Log.d(TAG, "complete Sending Msg");
                    } catch (RemoteException e) {
                        mClients.remove(num);
                    }


                }
                break;

            default:
                Log.d("TAG", "MsgHandler ERROR");
                break;


        }

    }

    private void sendMessageToMain(String where, int count) {
        for (int num = mClients.size() - 1; num >= 0; num--) {
            try {
                Bundle b = new Bundle();
                b.putString("where", where);
                b.putInt("time", count);
                Message msg = Message.obtain(null, MSG_SEND_TIMER);
                msg.setData(b);
                mClients.get(num).send(msg);
            } catch (RemoteException e) {
                mClients.remove(num);
            }

        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "bind complete");
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mSocketThread = new SocketThread();
        mSocketThread.start();
        return super.onStartCommand(intent, flags, startId);

    }

    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    private void connectionFailed() {
        setState(STATE_LISTEN);
    }

    private void connectionLost() {
        setState(STATE_LISTEN);

    }

    public synchronized void connect(Socket socket){
        Log.d(TAG, "connect to: " + socket.getInetAddress().toString());

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread == null) {

            } else {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(socket);

        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(Socket socket){
        Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread == null) {

        } else {
            Log.d(TAG,"ConnectThread not null");
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            Log.d(TAG,"ConnectedThread not null");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        Log.d(TAG, "go ConnectedThread");
        mConnectedThread = new ConnectedThread(socket);
        //Log.d(TAG, "go ConnectedThread");
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    private class ConnectThread extends Thread{
        private final Socket mSocket;


        public ConnectThread(Socket socket){
            mSocket = socket;
            Log.i(TAG, mSocket.getInetAddress().toString());
        }

        public void run(){
            Log.i(TAG, "mConnectThread Start");


            synchronized (InternetConnectService.this){
                setName("ConnectThread");
                mConnectThread = null;
            }

            connected(mSocket);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread{
        private final Socket mmSocket;
        private InputStream inputStream;
        private OutputStream outputStream;
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;
        private ObjectInputStream objectInputStream;
        private String[] RecvMsg;


        public ConnectedThread(Socket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            RecvMsg = null;


            // Socket의 inputstream 과 outputstream을 얻는다.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();

            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            try {
                inputStream = tmpIn;
                outputStream = tmpOut;
                dataInputStream = new DataInputStream(inputStream);
                dataOutputStream = new DataOutputStream(outputStream);
                //objectInputStream = new ObjectInputStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }

            //dataOutputStream = null;
        }
        public void run(){
            Log.i(TAG, "ConnectedThread Start");
            Log.i(TAG,"One More");
            byte[] buffer = new byte[1024];
            int bytes;
            /*try {
                //dataOutputStream.writeUTF("mobile");
                RecvMsg = (String[])objectInputStream.readObject();
                Log.i(TAG, "Here");
                stringArraySeperator(RecvMsg);
                Log.d(TAG, RecvMsg.toString());
                sendMessageToMain(buildings, MSG_SEND_BUILDING);
                sendMessageToMain(number, MSG_SEND_NUMBER);
                sendMessageToMain(gender, MSG_SEND_GENDER);
                sendMessageToMain(available, MSG_SEND_AVAILABLE);
                sendMessageToMain(acceptTime, MSG_SEND_ACCEPTTIME);

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("OUTPUT","제대로 안넘어감");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }*/

            while (true) {
                try {
                    // InputStream으로부터 값을 받는 읽는 부분(값을 받는다)

                    //bytes = inputStream.read(buffer);

                    String data;
                    try {
                        data = dataInputStream.readUTF();
                        Log.d(TAG, data.toString());

                        // Log.d(TAG, data+"");
                        stringArraySeperator(data);

                        // Log.d(TAG, data.toString());
                        //sendMessageToMain(number, MSG_SEND_NUMBER);

                    }finally{

                    }
                    // stringArraySeperator(data);

                    // Log.d(TAG, data);
                    //sendMessageToMain(data);




                  /* if (data.contains(".")) {
                        data = beforData + data;

                        if (Float.parseFloat(data) > 80) {
                            timerSwitch = false;
                            if(valueChanged(timerSwitch)){
                                if(mTimerThread.isAlive()){
                                    mTimerThread.interrupt();
                                    sendMessageToMain(0);
                                }
                                else{
                                    sendMessageToMain(0);
                                }
                                tempValue = timerSwitch;
                            }
                            else{
                                tempValue = timerSwitch;
                                sendMessageToMain(0);
                            }
                        } else {
                            timerSwitch = true;
                            if(valueChanged(timerSwitch)){
                                if(mTimerThread.isAlive()){
                                    Log.d(TAG,"이게 말이 돼?");
                                }
                                else{
                                    mTimerThread = new TimerThread();
                                    mTimerThread.start();
                                }
                                tempValue = timerSwitch;
                            }else{
                                tempValue = timerSwitch;
                            }
                        }
                        Log.d(TAG, data);
                        sendMessageToMain(data);
                        //send(Message.obtain(null, MSG_SEND, msg.obj));
                        beforData = "";
                    } else {
                        beforData = data;
                    }*/

                } catch (IOException e) {
                    Log.e(TAG, "disconnected");
                    connectionLost();
                    break;
                }
            }

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }

        public boolean valueChanged(boolean flag){
            if(tempValue != flag){
                return true;
            }
            else{
                return false;
            }
        }
    }

    public class FifthTimerThread extends Thread {
        int count = 0;
        String where = null;
        public FifthTimerThread(String where, int count){
            this.count = count;
            this.where = where;
        }
        public void run() {
            Log.d(TAG, "run!");
            //count = 0;

            if(where.equals("501")){
                try{
                    while(!Thread.currentThread().isInterrupted()){
                        count++;
                        sendMessageToMain(where, count);

                        sleep(1000, 0);
                    }
                }catch (InterruptedException e){

                }
                finally{
                    count = 0;
                    Log.d(TAG, "timer is dead");
                }
            }
            else if(where.equals("502")){
                try{
                    while(!Thread.currentThread().isInterrupted()){
                        count++;
                        sendMessageToMain(where, count);

                        sleep(1000, 0);
                    }
                }catch (InterruptedException e){

                }
                finally{
                    count = 0;
                    Log.d(TAG, "timer is dead");
                }
            }
            else{


            }



        }

    }
    public static boolean isRunning() {
        return isRunning;
    }

    public class SocketThread extends Thread{
        //Socket socket;

        public SocketThread(){
        }

        public void run(){
            try {
                connectSocket = new Socket(serverIP, 9999);
                byte[] check = {'m','o','b','i','l','e'};
                DataOutputStream tmpOutput = new DataOutputStream(connectSocket.getOutputStream());
                tmpOutput.write(check);
                connect(connectSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //connect(socket);
        }
    }

    public void onRebind(Intent intent) {

        Log.d(TAG, "rebind");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            connectSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stringArraySeperator(String msg){
        String[] subValues = msg.split("/");
        for(int i = 0; i < subValues.length; i++){
            stringSeperator(subValues[i]);
        }

    }

    public void stringSeperator(String msg){
        String[] values = msg.split(",");

        buildings = values[0];
        number = values[1];
        gender = values[2];
        available = values[3];
        acceptTime = values[4];

        timerFunction(number, available, acceptTime);

        anotherData = number + "," + gender +"," + available +"," + acceptTime;
        sendMessageToMain(buildings, anotherData, MSG_SEND_BUILDING);


    }

    public void timerFunction(String where, String avail, String count){
        if(where.equals("501")){
            if(!fifth_1boolean){
                if(avail.equals("0")){
                    sendMessageToMain(where, 0);
                }else{
                    mfifth_1Thread = new FifthTimerThread(where, Integer.parseInt(count));
                    mfifth_1Thread.start();
                    fifth_1boolean = true;
                }
            }
            else{
                if(avail.equals("0")){
                    fifth_1boolean = false;
                    if(mfifth_1Thread != null){
                        mfifth_1Thread.interrupt();
                        //fifth_1boolean = false;
                    }
                    else{
                        //fifth_1boolean = false;
                        sendMessageToMain(where, 0);
                    }
                }else{

                }
            }
        }
        else if(where.equals("502")){
            if(!fifth_2boolean){
                if(avail.equals("0")){
                    sendMessageToMain(where, 0);
                }else{
                    mfifth_2Thread = new FifthTimerThread(where, Integer.parseInt(count));
                    mfifth_2Thread.start();
                    fifth_2boolean = true;
                }
            }
            else{
                if(avail.equals("0")){
                    fifth_2boolean = false;
                    if(mfifth_2Thread!=null){
                        mfifth_2Thread.interrupt();
                        //fifth_1boolean = false;
                    }
                    else{
                        //fifth_1boolean = false;
                        sendMessageToMain(where, 0);
                    }
                }else{

                }
            }
        }
        else{

        }
    }


}
