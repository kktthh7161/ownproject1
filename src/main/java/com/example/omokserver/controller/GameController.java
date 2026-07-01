package com.example.omokserver.controller;

import java.util.Map;
import java.util.HashMap;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import java.util.concurrent.ConcurrentHashMap;
import com.example.omokserver.model.Coordinate;
import com.example.omokserver.model.Stone;
import com.example.omokserver.service.OmokService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor // 생성자를 통해 OmokService를 자동으로 주입받습니다.
public class GameController {

    private final OmokService omokService;
    private final SimpMessagingTemplate messagingTemplate;

    // 클라이언트가 /pub/game/move로 메시지를 보내면 이 메서드가 실행됩니다.
    @MessageMapping("/game/move")
    public void move(Coordinate coord, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId(); // 접속자 고유 ID
        Stone assignedStone = omokService.assignRole(sessionId);
        Stone currentTurn = omokService.getCurrentTurn();

        // 내 차례가 아니거나, 내 돌이 아니면 무시
        if (omokService.isGameOver() || assignedStone != currentTurn) {
            return;
        }

        Stone color = omokService.getCurrentTurn();

        boolean isPlaced = omokService.placeStone(coord.x(), coord.y(), color);

        if (isPlaced) {
            // 돌이 놓였다는 알림 전송
            Map<String, Object> response = new HashMap<>();
            response.put("x", coord.x());
            response.put("y", coord.y());
            response.put("color", color);

            if (omokService.checkWin(coord.x(), coord.y(), color)) {
                omokService.setGameOver(true);
                // 돌은 그리되, 다음 턴 정보는 굳이 넣지 않고 전송
                messagingTemplate.convertAndSend("/sub/game/board", (Object) response);
                // 승리 메시지 전송
                messagingTemplate.convertAndSend("/sub/game/winner", color + " 승리!");
            } else {
                // 승리하지 않았을 때만 다음 턴 계산 및 전송
                omokService.switchTurn();
                response.put("nextTurn", omokService.getCurrentTurn());
                messagingTemplate.convertAndSend("/sub/game/board", (Object) response);
            }
        }
    }

    @MessageMapping("/game/reset")
    public void reset() {
        System.out.println("서버: 게임 초기화 요청 받음");
        omokService.resetBoard(); // 서비스에서 board 배열과 isGameOver 등을 초기화

        // 모든 클라이언트에게 리셋하라고 명령
        messagingTemplate.convertAndSend("/sub/game/reset", "RESET_COMPLETE");
    }
    @MessageMapping("/game/initState")
    public void sendInitialState(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        Stone myRole = omokService.assignRole(sessionId);

        Map<String, Object> state = new HashMap<>();
        state.put("board", omokService.getBoard());
        state.put("isGameOver", omokService.isGameOver());
        state.put("myRole", myRole.toString());
        state.put("currentTurn", omokService.getCurrentTurn().toString());
        state.put("targetSessionId", sessionId);
        // ToUser 대신 공용 채널로 전송 (모든 접속자가 이 정보를 받지만, myRole은 각자 판단)
        messagingTemplate.convertAndSend("/sub/game/init", (Object) state);
    }
}