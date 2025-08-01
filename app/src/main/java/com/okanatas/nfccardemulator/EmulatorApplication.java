package com.okanatas.nfccardemulator;

import android.app.Application;

import com.okanatas.nfccardemulator.control.WebSocketServerConnection;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class EmulatorApplication extends Application {

    private static EmulatorApplication instance;
    private WebSocketServerConnection webSocketServerConnection;
    private RequestResponseFlow requestResponseFlow;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        requestResponseFlow = new RequestResponseFlow();
        InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", 6666);

        webSocketServerConnection = new WebSocketServerConnection(bindAddress);
        webSocketServerConnection.start();
    }

    public void onTerminate() {
        super.onTerminate();
        if (webSocketServerConnection != null) {
            webSocketServerConnection.stop();
        }
    }

    public static EmulatorApplication getInstance() {
        return instance;
    }

    public WebSocketServerConnection getWebSocketServerConnection() {
        return webSocketServerConnection;
    }

    /**
     * This method is used to get the application context.
     * @return Application context
     */
    public static EmulatorApplication getAppContext() {
        return instance;
    }

    public RequestResponseFlow getRequestResponseFlow() {
        return requestResponseFlow;
    }
}
