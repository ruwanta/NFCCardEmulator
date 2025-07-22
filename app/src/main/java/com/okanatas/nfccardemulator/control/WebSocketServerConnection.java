package com.okanatas.nfccardemulator.control;


import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.lang.ref.WeakReference;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Starts the connection to PDC, manages its lifecycle and fires events based on
 * requests from PDC.
 */
public class WebSocketServerConnection {

    private static final String LOGGER_TAG = WebSocketServerConnection.class.getSimpleName();

    private final int PING_INTERVAL_SECONDS = 30;
    private final int CACHE_CLEAR_INTERVAL_MILLIS = 11000; //Run the cache cleanup every 11 seconds
    public static final int DEFAULT_WS_PORT = 16108;

    private WsListeningServer webSocketServer;

    private final InetSocketAddress bindAddress;

    private ScheduledExecutorService webSocketSupervisorService;

    private Set<WebSocket> clientWebSokets = new HashSet<>();


    private boolean webSocketServerStarted = false;
    private boolean isStarting = false;

    private List<WeakReference<ControlCommandListener>> controlCommandListeners = new LinkedList<>();

    private CommandParser commandParser = new CommandParser();

    public WebSocketServerConnection(InetSocketAddress socketAddress) {
        this.bindAddress = socketAddress;
    }

    public boolean isBound() {
        if(webSocketServer == null) {
            return false;
        }

        return webSocketServer.isBound();
    }

    public void start() {
        if(isStarting || webSocketServerStarted) {
            Log.e(LOGGER_TAG, "Websocket server is already running. Ignoring the start request.");
            return;
        }
        isStarting = true;

        Runnable worker = new WebSocketServerStarter();

        webSocketSupervisorService = Executors.newSingleThreadScheduledExecutor();
        webSocketSupervisorService.schedule(worker, 0, TimeUnit.SECONDS);

        webSocketSupervisorService.scheduleWithFixedDelay(mPinger, PING_INTERVAL_SECONDS,
                PING_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }


    /**
     * Stops the server and the supervisor service.
     * This method should be called when the application is shutting down.
     */
    public void stop() {

        if(webSocketSupervisorService != null) {
            webSocketSupervisorService.shutdownNow();
        }
        if(webSocketServer != null) {
            try {
                Log.i(LOGGER_TAG, "Stopping the websocket server");
                webSocketServer.stop();
            } catch (InterruptedException e) {
                Log.e(LOGGER_TAG, "Error occurred while stopping the websocket server", e);
            }
        }

    }

    /**
     * Adds a control command listener to the list of listeners.
     *
     * @param listener
     */
    public void addControlCommandListener(ControlCommandListener listener) {
        if (listener == null) {
            Log.e(LOGGER_TAG, "Cannot add a null listener");
            return;
        }
        controlCommandListeners.add(new WeakReference<>(listener));
    }

    /**
     * Removes a control command listener from the list of listeners.
     *
     * @param listener
     */
    public void removeControlCommandListener(ControlCommandListener listener) {
        if (listener == null) {
            Log.e(LOGGER_TAG, "Cannot remove a null listener");
            return;
        }
        controlCommandListeners.removeIf(ref -> ref.get() == null || ref.get() == listener);
    }

    /**
     * Sends binary response to all clients.
     *
     * @param data
     */
    public void sendResponse(ByteBuffer data) {

        webSocketServer.broadcast(data);
    }

    /**
     * Sends binary response to all clients.
     *
     * @param data
     */
    public void sendMessage(String data) {

        webSocketServer.broadcast(data);
    }



    public boolean isActive() {
        return webSocketServerStarted || isStarting;
    }

    public String getDisplayName() {
        if (bindAddress == null) {
            return "Not connected";
        }
        return bindAddress.toString();
    }

    /**
     * Starts the websocket server.
     * Starting the server in a separate thread to avoid blocking the main thread.
     */
    private class WebSocketServerStarter implements Runnable {

        @Override
        public void run() {
            isStarting = true;
            startWsServer();
            isStarting = false;
        }
    }

    private void startWsServer() {

        webSocketServer = new WsListeningServer(bindAddress);
        webSocketServer.setReuseAddr(true);
        webSocketServer.run();
    }

    /**
     * Websocket server implementation to listen to incoming connections and messages.
     */
    public class WsListeningServer extends WebSocketServer
    {

        public WsListeningServer(InetSocketAddress address)
        {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake)
        {
            Log.d(LOGGER_TAG, "Opened connection from client: " + conn.getRemoteSocketAddress() );

            clientWebSokets.add(conn);

        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote)
        {
            Log.d(LOGGER_TAG, "Closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
            clientWebSokets.remove(conn);
        }

        @Override
        public void onMessage(WebSocket conn, String message)
        {
            Log.d(LOGGER_TAG, "Received message from " + conn.getRemoteSocketAddress() + ": " + message);
            Command command = commandParser.parseCommand(message);
            if(command != null) {
                notifyListeners(command);
            }
        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message)
        {
            Log.d(LOGGER_TAG, "Received ByteBuffer from " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onError(WebSocket conn, Exception ex)
        {
            if(ex instanceof BindException) {
                Log.e(LOGGER_TAG, "Port is already in use. Please check if the port is already in use by another application.");
            } else {
                Log.e(LOGGER_TAG, "An error occurred on connection " + conn , ex);
            }
        }

        @Override
        public void onStart()
        {
            Log.d(LOGGER_TAG, "Server started successfully");
            webSocketServerStarted = true;
        }

        @Override
        public void onWebsocketPing(WebSocket conn, Framedata f) {
            super.onWebsocketPing(conn, f);
        }

        public boolean isBound() {
            return webSocketServerStarted ;
        }
    }

    public class SecureWsListeningServer extends WsListeningServer {
        public SecureWsListeningServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void stop() throws InterruptedException {
            super.stop();
            Log.i(LOGGER_TAG, "Secure WebSocket server stopped.");
        }
    }

    private Runnable mPinger = () -> {
        for (WebSocket conn: clientWebSokets) {
            if(conn.isOpen()) {
                conn.sendPing();
            }
        }
    };


    private void notifyListeners(Command command) {
        controlCommandListeners.forEach(
                ref -> {
                    ControlCommandListener listener = ref.get();
                    if (listener != null) {
                        listener.onCommandReceived(command);
                    } else {
                        controlCommandListeners.remove(ref);
                    }
                }
        );
    }
}
