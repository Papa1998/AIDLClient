package com.example.aidlclient2;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.example.aidl.bookaidl.Book;
import com.example.aidl.bookaidl.IBookManager;
import com.example.aidl.bookaidl.IOnNewBookArrivedListener;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;

    private Button btn_getBookList;
    private Button btn_addBook;
    private Button btn_bindService;
    private Button btn_register;
    private Button btn_unregister;

    private IBookManager bookManager;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bookManager = IBookManager.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bookManager = null;
            Log.e(TAG, "binder die");
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MESSAGE_NEW_BOOK_ARRIVED:{
                    Log.i(TAG, "receive new book: " + msg.obj);
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook).sendToTarget();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_getBookList = findViewById(R.id.btn_getBookList);
        btn_addBook = findViewById(R.id.btn_addBook);
        btn_bindService = findViewById(R.id.btn_bindService);
        btn_register = findViewById(R.id.btn_register);
        btn_unregister = findViewById(R.id.btn_unregister);

        btn_bindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.example.aidlservice", "com.example.aidlservice.BookManagerService"));
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
        });

        btn_getBookList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Book> list = bookManager.getBookList();
                    Log.i(TAG, "query book list, list type: " + list.getClass().getCanonicalName());
                    Log.i(TAG, "query book list, list content: " + list.toString());
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });

        btn_addBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Book book = new Book(3, "Android开发艺术探索");
                    bookManager.addBook(book);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    bookManager.registerListener(mOnNewBookArrivedListener);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });

        btn_unregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    bookManager.unregisterListener(mOnNewBookArrivedListener);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bookManager != null  && bookManager.asBinder().isBinderAlive()){
            try {
                Log.i(TAG, "unregister listener:" + mOnNewBookArrivedListener);
                bookManager.unregisterListener(mOnNewBookArrivedListener);
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
    }
}
