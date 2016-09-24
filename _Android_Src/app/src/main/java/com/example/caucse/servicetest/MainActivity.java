package com.example.caucse.servicetest;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private Button fifth_left;
    private Intent intent;
    //private TextView showText = "showText";

    private static final String TAG = "Main";

    private String getAddress;


    Messenger mService = null;
    boolean mIsBound;

    private static final int MSG_RECEIVE = 1;

    private static Socket socket = null;
    private static OutputStreamWriter osw = null;
    private static InputStreamReader isr = null;
    private static PrintWriter pw = null;

    //private Button moveBtn;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case InternetConnectService.MSG_SEND_BUILDING:
                    int fl_1 = 0;
                    int fl_2 = 0;

                    String[] values = msg.getData().getString("subData").split(",");
                    if(values[0].equals("501")){
                        if(values[2].equals("0"))
                            fl_1 = 0;
                        else
                            fl_1 = 1;
                    }
                    else if(values[0].equals(("502"))){
                        if(values[2].equals("0"))
                            fl_2 = 0;
                        else
                            fl_2 = 1;

                    }
                    else{

                    }

                    int fl = fl_1+fl_2;

                    if(msg.getData().getString("data").equals("1")){
                        fifth_left.setBackgroundResource(R.drawable.empty);
                        fifth_left.setText("5F_LEFT("+ String.valueOf(fl)+"/3)");
                    }
                    else{
                        fifth_left.setBackgroundResource(R.drawable.empty);
                        fifth_left.setText("5F_LEFT("+ String.valueOf(fl)+"/3)");
                    }
                    break;
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

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            Log.d(TAG, "Disconnected");
        }
    };

        //private ServiceManager serviceManager;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Button first_left = (Button)findViewById(R.id.first_left);
            first_left.setBackgroundResource(R.drawable.empty);
            Button first_right = (Button)findViewById(R.id.first_right);
            first_right.setBackgroundResource(R.drawable.empty);
            Button second_left = (Button)findViewById(R.id.second_left);
            second_left.setBackgroundResource(R.drawable.empty);
            Button second_right = (Button)findViewById(R.id.second_right);
            second_right.setBackgroundResource(R.drawable.empty);
            Button third_left = (Button)findViewById(R.id.third_left);
            third_left.setBackgroundResource(R.drawable.empty);
            Button third_right = (Button)findViewById(R.id.third_right);
            third_right.setBackgroundResource(R.drawable.empty);
            Button fourth_left = (Button)findViewById(R.id.fourth_left);
            fourth_left.setBackgroundResource(R.drawable.empty);
            Button fourth_right = (Button)findViewById(R.id.fourth_right);
            fourth_right.setBackgroundResource(R.drawable.empty);
            fifth_left = (Button)findViewById(R.id.fifth_left);
            fifth_left.setBackgroundResource(R.drawable.empty);
            Button fifth_right = (Button)findViewById(R.id.fifth_right);
            fifth_right.setBackgroundResource(R.drawable.empty);

            /*ConnectService client = new ConnectService();
            client.start();*/

            intent = getIntent();
            getAddress = intent.getStringExtra("address");

            fifth_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, TestSubActivity.class);
                    intent.putExtra("address", getAddress);
                    intent.putExtra("floor", "fifth_left");
                    startActivity(intent);
                }
            });

            Intent startIntent = new Intent(this, InternetConnectService.class);
            //startIntent.putExtra("address", getAddress);
           // Log.d(TAG, getAddress);

            restoreMe(savedInstanceState);
            Log.d(TAG, "restoreMe");

            CheckIfServiceIsRunning();
            Log.d(TAG, "CheckIfServiceIsRunning");

            startService(startIntent);
            doBindService();


        }

    void doBindService() {
        Intent bindIntent = new Intent(this, InternetConnectService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.d("TAG", "Binding");
    }
    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, InternetConnectService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
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
    public void onDestroy(){
        super.onDestroy();
        try{
            doUnbindService();
            android.os.Process.killProcess(android.os.Process.myPid());
        }catch (Throwable t) {
            Log.e("MainActivity", "Failed to unbind from the service", t);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putString("textStatus", showText.getText().toString());

    }
    private void restoreMe(Bundle state) {
        if (state!=null) {
           // showText.setText(state.getString("textStatus"));

        }
    }
    private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (InternetConnectService.isRunning()) {
            doBindService();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //doUnbindService();
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if( keyCode == KeyEvent.KEYCODE_BACK )
        {
            new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Quit").setMessage("Do you want to quit").setPositiveButton("Yes", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    stopService(new Intent(MainActivity.this, InternetConnectService.class));
                    finish();
                }
            }).setNegativeButton( "No", null ).show();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}

