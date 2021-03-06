package com.colourMe.networking.client;

import com.colourMe.common.marshalling.MessageDecoder;
import com.colourMe.common.marshalling.MessageEncoder;
import com.colourMe.common.messages.Message;
import com.colourMe.common.util.Log;
import com.colourMe.common.util.U;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;

/**
 * ColourMe ClientEndPoint
 *
 * @author Arsalan Macknojia
 */
@ClientEndpoint(
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class
)
public class GameClientEndpoint {
    private volatile long lastMessageReceivedTime = System.currentTimeMillis() * 2;
    public Session session;
    private PriorityBlockingQueue<Message> receivedQueue;

    /**
     * Constructor
     *
     * @param ServerEndpointURI - URI to connect to the server.
     */
    public GameClientEndpoint(URI ServerEndpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, ServerEndpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Function to handle connection open events.
     *
     * @param session - The session which is opened.
     */
    @OnOpen
    public void onOpen(Session session) {
        Logger logger = Log.get(this);
        logger.info("Connected to server endpoint successfully");
        this.session = session;
    }

    /**
     * Function to handle incoming messages from server.
     *
     * @param session - The current session
     * @param update - The message from the server
     */
    @OnMessage
    public void onMessage(Session session, Message update) {
        lastMessageReceivedTime = System.currentTimeMillis();
        Logger logger = Log.get(this);
        logger.info("Received message from server");
        logger.info("Message received: " + U.json(update));
        receivedQueue.put(update);
    }

    /**
     * OnError
     *
     * @param session
     */
    @OnError
    public void onError(Session session, Throwable throwable){
        U.handleExceptionBase(Log.get(this), (Exception) throwable);
    }

    public void addReceiveQueue(PriorityBlockingQueue<Message> queue) {
        this.receivedQueue = queue;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(Message message) {
        this.session.getAsyncRemote().sendObject(message);
    }

    /**
     * Function to handle connection close events.
     *
     * @param session - The session which is getting closed.
     * @param reason - The reason for connection close.
     */
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Logger logger = Log.get(this);
        logger.warning("Closing WebSocket." + reason.getReasonPhrase());
        this.session = null;
    }

    public void disconnect() throws IOException {
        session.close();
        session = null;
    }

    public long getLastMessageReceivedTime() {
        return this.lastMessageReceivedTime;
    }


}