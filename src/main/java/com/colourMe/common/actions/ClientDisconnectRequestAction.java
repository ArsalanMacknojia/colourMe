package com.colourMe.common.actions;

import com.colourMe.common.gameState.GameService;
import com.colourMe.common.messages.Message;
import com.colourMe.common.messages.MessageType;

public class ClientDisconnectRequestAction extends ActionBase{
    @Override
    public Message execute(Message message, GameService gameService) {
        String playerID = message.getPlayerID();
        gameService.killPlayer(playerID);
        message.setMessageType(MessageType.ClientDisconnectResponse);
        return message;
    }
}
