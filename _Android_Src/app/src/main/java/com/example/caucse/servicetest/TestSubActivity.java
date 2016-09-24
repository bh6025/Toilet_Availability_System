package com.example.caucse.servicetest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.sql.Time;

public class TestSubActivity extends AppCompatActivity {
    private TextView showtext;
    private TextView fifthleft_1_timer;
    private TextView fifthleft_2_timer;

    private Button fifthleft_1;
    private Button fifthleft_2;
    private Button fifthleft_3;

    private static int fifthleft_1_time;
    private static boolean fifthleft_1_boolean;
    private static int fifthleft_2_time;
    private static boolean fifthleft_2_boolean;
    private static final String TAG = "Sub";

    private String getAddress;
    private String getFloor;

    private Intent intent;
    private int fl_1 = 0;
    private int fl_2 = 0;


    Messenger mService = null;
    boolean mIsBound;

    private static final int MSG_RECEIVE = 1;

    private Button moveBtn;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {


            switch (msg.what) {
                case InternetConnectService.MSG_SEND_BUILDING:
                    if (msg.getData().getString("data").equals("CAU208")) {
                        String[] values = msg.getData().getString("subData").split(",");

                        if (values[0].equals("501")) {
                            if (values[2].equals("1")) {
                                fl_1 = 1;
                                fifthleft_1.setBackgroundResource(R.drawable.inperson_icon);
                            } else {
                                fl_1 = 0;
                                fifthleft_1.setBackgroundResource(R.drawable.noperson_icon);
                            }
                        } else if (values[0].equals("502")) {
                            if (values[2].equals("1")) {
                                fl_2 = 1;
                                fifthleft_2.setBackgroundResource(R.drawable.inperson_icon);
                            } else {
                                fl_2 = 0;
                                fifthleft_2.setBackgroundResource(R.drawable.noperson_icon);
                            }
                        } else {

                        }
                    } else {

                    }


                    break;
                case InternetConnectService.MSG_SEND_TIMER:
                    if(msg.getData().getString("where").equals("501")){
                        int minute = msg.getData().getInt("time")/60;
                        int second = msg.getData().getInt("time")%60;
                        fifthleft_1_timer.setText(minute+""+":"+""+second);
                    }
                    else if(msg.getData().getString("where").equals("502")){
                        int minute = msg.getData().getInt("time")/60;
                        int second = msg.getData().getInt("time")%60;
                        fifthleft_2_timer.setText(minute+""+":"+""+second);
                        //fifthleft_1_timer.setText(minute+""+":"+""+second);
                    }
                    else{

                    }
                   /* int minute = msg.getData().getInt("time") / 60;
                    int second = msg.getData().getInt("time") % 60;
                    fifthleft_1_timer.setText(minute + "" + " : " + "" + second);*/
                default:
                    super.handleMessage(msg);
            }
            //super.handleMessage(msg);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.d(TAG, "Attached");
            try {
                Message msg = Message.obtain(null, InternetConnectService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            Log.d(TAG, "Disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_sub);
        showtext = (TextView) findViewById(R.id.sub_showtext);

        intent = getIntent();
        getAddress = intent.getStringExtra("address");

        if (intent.getStringExtra("floor").equals("fifth_left")) {
            setContentView(R.layout.fifth_left);

            fifthleft_1 = (Button) findViewById(R.id.fifthleft_1);
            fifthleft_1_time = 0;
            fifthleft_1_boolean = false;
            fifthleft_2 = (Button) findViewById(R.id.fifthleft_2);
            fifthleft_2_time = 0;
            fifthleft_2_boolean = false;
            //fifthleft_2.setBackgroundResource(R.drawable.noperson_icon);
            fifthleft_3 = (Button) findViewById(R.id.fifthleft_3);
            fifthleft_3.setBackgroundResource(R.drawable.noperson_icon);

            fifthleft_1_timer = (TextView) findViewById(R.id.fifthleft_1_timer);
            fifthleft_2_timer = (TextView) findViewById(R.id.fifthleft_2_timer);
        }

        Intent startIntent = new Intent(this, InternetConnectService.class);
        //startIntent.putExtra("address", getAddress);
        //Log.d(TAG, getAddress);

        restoreMe(savedInstanceState);
        Log.d(TAG, "restoreMe");

        CheckIfServiceIsRunning();
        Log.d(TAG, "CheckIfServiceIsRunning");

        doBindService();


    }

    void doBindService() {
        Intent bindIntent = new Intent(this, InternetConnectService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

        mIsBound = true;
        Log.d(TAG, "Binding");
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, InternetConnectService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            Log.d(TAG, "Unbinding");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
            Log.d(TAG, "onDestroy");
        } catch (Throwable t) {
            Log.e("MainActivity", "Failed to unbind from the service", t);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("textStatus", showtext.getText().toString());

    }

    private void restoreMe(Bundle state) {
        if (state != null) {
            showtext.setText(state.getString("textStatus"));

        }
    }

    private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (InternetConnectService.isRunning()) {
            doBindService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public class TimerThread extends Thread {
        int count = 0;
        String where;

        public TimerThread(String where, int time){
            count = time;
            this.where = where;
        }

        public void run() {
            Log.d(TAG, "run!");
            //count = 0;
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    count++;
                    returnTime(count);

                    sleep(1000, 0);
                }
            } catch (InterruptedException e) {

            } finally {
                count = 0;
                Log.d(TAG, "timer is dead");
            }


        }

        public int returnTime(int n){
            return n;
        }

    }
}
