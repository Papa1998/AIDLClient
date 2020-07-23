package com.example.aidl.bookaidl;

import com.example.aidl.bookaidl.Book;

interface IOnNewBookArrivedListener {

   void onNewBookArrived(in Book newBook);
}
