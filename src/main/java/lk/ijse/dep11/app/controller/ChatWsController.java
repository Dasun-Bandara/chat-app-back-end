package lk.ijse.dep11.app.controller;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import lk.ijse.dep11.app.to.MessageTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.validation.ConstraintViolation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChatWsController extends TextWebSocketHandler {
    private final List<WebSocketSession> webSocketSessionList = new ArrayList<>();
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private LocalValidatorFactoryBean localValidatorFactoryBean;
    @Autowired
    private HikariDataSource pool;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        webSocketSessionList.add(session);
        try (Statement stm = pool.getConnection().createStatement()) {
            ResultSet rst = stm.executeQuery("SELECT email,message FROM messages");
            while (rst.next()){
                String email = rst.getString("email");
                String message = rst.getString("message");
                MessageTO messageTO = new MessageTO(message, email);
                session.sendMessage(new TextMessage(mapper.writer().writeValueAsString(messageTO)));
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            MessageTO messageObj = mapper.readValue(message.getPayload(), MessageTO.class);
            Set<ConstraintViolation<MessageTO>> constraintViolations = localValidatorFactoryBean.getValidator().validate(messageObj);
            if (constraintViolations.isEmpty()){
                try (PreparedStatement stm = pool.getConnection().prepareStatement("INSERT INTO messages (email, message) VALUES (?,?);");){
                    stm.setString(1, messageObj.getEmail());
                    stm.setString(2, messageObj.getMessage());
                    stm.executeUpdate();
                }
                for (WebSocketSession webSocketSession : webSocketSessionList) {
                    if(webSocketSession == session) continue;
                    if(webSocketSession.isOpen()) webSocketSession.sendMessage(new TextMessage(message.getPayload()));
                }
            }else {
                session.sendMessage(new TextMessage("Invalid Message"));
            }
        }catch (JacksonException exp){
            session.sendMessage(new TextMessage("Invalid JSON"));
        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        webSocketSessionList.remove(session);
    }
}
