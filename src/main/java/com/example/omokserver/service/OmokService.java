package com.example.omokserver.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.example.omokserver.model.Stone;
import org.springframework.stereotype.Service;

@Service
public class OmokService {
    private static final int BOARD_SIZE = 15;
    private final Stone[][] board = new Stone[BOARD_SIZE][BOARD_SIZE];
    private Stone currentTurn = Stone.BLACK;
    private boolean isGameOver = false;
    private Map<String, Stone> playerRoles = new ConcurrentHashMap<>();
    private String blackPlayerId = null;
    private String whitePlayerId = null;

    public synchronized Stone assignRole(String sessionId) {
        if (sessionId.equals(blackPlayerId)) return Stone.BLACK;
        if (sessionId.equals(whitePlayerId)) return Stone.WHITE;

        if (blackPlayerId == null) {
            blackPlayerId = sessionId;
            return Stone.BLACK;
        } else if (whitePlayerId == null) {
            whitePlayerId = sessionId;
            return Stone.WHITE;
        }
        return Stone.NONE; // 관전자
    }

    // 리셋 시 역할도 초기화하고 싶다면 추가
    public void resetRoles() {
        playerRoles.clear();
        blackPlayerId = null;
        whitePlayerId = null;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }
    public Stone[][] getBoard() {
        return this.board;
    }

    public Stone getCurrentTurn() {
        return currentTurn;
    }

    public void switchTurn() {
        this.currentTurn = (this.currentTurn == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
    }

    public OmokService() {
        resetBoard();
    }

    public void resetBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = Stone.NONE;
            }
        }
        this.currentTurn = Stone.BLACK;
        this.isGameOver = false;
    }

    // 돌을 놓는 함수 (성공하면 true, 이미 있으면 false)
    public boolean placeStone(int x, int y, Stone stone) {
        if (isGameOver || x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) return false;
        if (board[y][x] != Stone.NONE) return false;

        board[y][x] = stone;
        return true;
    }

    // 승리 판정 알고리즘
    public boolean checkWin(int x, int y, Stone stone) {
        // 4가지 방향: 가로, 세로, 우상향 대각선, 우하향 대각선
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1; // 방금 놓은 돌

            // 한 방향으로 가면서 카운트 (dx, dy)
            count += countConsecutive(x, y, dir[0], dir[1], stone);
            // 반대 방향으로 가면서 카운트 (-dx, -dy)
            count += countConsecutive(x, y, -dir[0], -dir[1], stone);

            if (count >= 5) return true;
        }
        return false;
    }

    private int countConsecutive(int x, int y, int dx, int dy, Stone stone) {
        int count = 0;
        int curX = x + dx;
        int curY = y + dy;

        while (curX >= 0 && curX < BOARD_SIZE && curY >= 0 && curY < BOARD_SIZE
                && board[curY][curX] == stone) {
            count++;
            curX += dx;
            curY += dy;
        }
        return count;
    }
}
