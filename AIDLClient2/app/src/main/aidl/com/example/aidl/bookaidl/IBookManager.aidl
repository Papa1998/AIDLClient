package com.example.aidl.bookaidl;

import com.example.aidl.bookaidl.Book;
import com.example.aidl.bookaidl.IOnNewBookArrivedListener;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
    void registerListener(IOnNewBookArrivedListener listener);
    void unregisterListener(IOnNewBookArrivedListener listener);
}
