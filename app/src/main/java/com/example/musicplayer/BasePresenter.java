package com.example.musicplayer;

public interface BasePresenter<T> {

    T getDataItem(int position);

    int getDataItemCount();
}
