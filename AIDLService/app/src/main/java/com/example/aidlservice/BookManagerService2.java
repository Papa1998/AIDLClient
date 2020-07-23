package com.example.aidlservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.example.aidl.bookaidl.Book;
import com.example.aidl.bookaidl.IBookManager;
import com.example.aidl.bookaidl.IOnNewBookArrivedListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//服务端的Service实现
public class BookManagerService2 extends Service {

    private static final String TAG = "BookManagerService";

    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();//这个数据结构支持并发读/写
    private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListenerList = new CopyOnWriteArrayList<>();

    //在服务端中创建Binder对象并实现接口中的方法
    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;//返回图书列表
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);//添加图书
            onNewBookArrived(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            if(!mListenerList.contains(listener)){
                mListenerList.add(listener);
                Log.i(TAG, "register listener succeed.");
            }else {
                Log.i(TAG, "already exists.");
            }
            Log.i(TAG, "registerListener, size:" + mListenerList.size());
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            if(!mListenerList.contains(listener)){
                mListenerList.remove(listener);
                Log.i(TAG, "unregister listener succeed.");
            }else {
                Log.i(TAG, "not found, can not unregister");
            }
            Log.i(TAG, "unregisterListener, size:" + mListenerList.size());
        }


    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "AIDL Service is created");
        //图书列表中默认添加有两本书
        mBookList.add(new Book(1, "Android"));
        mBookList.add(new Book(2, "Ios"));
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return mBinder;//返回Binder对象
    }

    private void onNewBookArrived(Book book) throws RemoteException{
        mBookList.add(book);
        Log.i(TAG, "onNewBookArrived, notify listeners:" + mListenerList.size());
        for (int i = 0; i < mListenerList.size(); i++) {
            IOnNewBookArrivedListener listener = mListenerList.get(i);
            Log.i(TAG, "onNewBookArrived, notify listener:" + listener);
            listener.onNewBookArrived(book);
        }
    }
}
