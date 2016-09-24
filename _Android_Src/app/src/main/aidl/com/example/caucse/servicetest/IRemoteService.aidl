// IMyAidlInterface.aidl
package com.example.caucse.servicetest;

// Declare any non-default types here with import statements

import com.example.caucse.servicetest.IRemoteServiceCallback;

interface IRemoteService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     boolean registerCallback(IRemoteServiceCallback callback);
     boolean unregisterCallback(IRemoteServiceCallback callback);

}
