package com.example.omokserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커를 활성화합니다.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 메시지를 받을 때(구독) 사용하는 경로의 접두사입니다.
        config.enableSimpleBroker("/sub");

        // 클라이언트가 서버로 메시지를 보낼 때 사용하는 경로의 접두사입니다.
        config.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 처음 WebSocket 연결을 맺을 엔드포인트 주소입니다.
        // setAllowedOrigins("*")는 모든 도메인에서의 접속을 허용한다는 뜻입니다 (테스트용).
        registry.addEndpoint("/ws-omok").setAllowedOriginPatterns("*").withSockJS();
    }
}