package com.thg.accelerator23.connectn.ai.tylertheconnector;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.InvalidMoveException;
import com.thehutgroup.accelerator.connectn.player.Player;
import com.thehutgroup.accelerator.connectn.player.Position;

public class TylerTheConnector extends Player {
  private static final int MAX_DEPTH = 5;

  public TylerTheConnector(Counter counter) {
    super(counter, TylerTheConnector.class.getName());
  }

  @Override
  public int makeMove(Board board) {
    int bestMove = -1;
    int bestScore = Integer.MIN_VALUE;

    for (int col = 0; col < board.getConfig().getWidth(); col++) {
      if (isColumnPlayable(board, col)) {
        try {
          Board newBoard = new Board(board, col, getCounter());
          int score = minimax(newBoard, MAX_DEPTH, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
          if (score > bestScore) {
            bestScore = score;
            bestMove = col;
          }
        } catch (InvalidMoveException ignored) {
          // Skip invalid moves
        }
      }
    }

    return bestMove;
  }

  private int minimax(Board board, int depth, boolean isMaximising, int alpha, int beta) {
    if (depth == 0 || isGameOver(board)) {
      return evaluateBoard(board);
    }

    if (isMaximising) {
      int maxEval = Integer.MIN_VALUE;
      for (int col = 0; col < board.getConfig().getWidth(); col++) {
        if (isColumnPlayable(board, col)) {
          try {
            Board newBoard = new Board(board, col, getCounter());
            int eval = minimax(newBoard, depth - 1, false, alpha, beta);
            maxEval = Math.max(maxEval, eval);
            alpha = Math.max(alpha, eval);
            if (beta <= alpha) break; // Alpha-beta pruning
          } catch (InvalidMoveException ignored) {
          }
        }
      }
      return maxEval;
    } else {
      int minEval = Integer.MAX_VALUE;
      Counter opponentCounter = getCounter().getOther();
      for (int col = 0; col < board.getConfig().getWidth(); col++) {
        if (isColumnPlayable(board, col)) {
          try {
            Board newBoard = new Board(board, col, opponentCounter);
            int eval = minimax(newBoard, depth - 1, true, alpha, beta);
            minEval = Math.min(minEval, eval);
            beta = Math.min(beta, eval);
            if (beta <= alpha) break; // Alpha-beta pruning
          } catch (InvalidMoveException ignored) {
          }
        }
      }
      return minEval;
    }
  }

  public boolean isGameOver(Board board) {
    // Check for a win or draw
    for (int x = 0; x < board.getConfig().getWidth(); x++) {
      for (int y = 0; y < board.getConfig().getHeight(); y++) {
        Position position = new Position(x, y);
        if (board.hasCounterAtPosition(position)) {
          Counter counter = board.getCounterAtPosition(position);

          // Check horizontal, vertical, and diagonal directions
          if (checkDirection(board, position, counter, 1, 0) || // Horizontal
                  checkDirection(board, position, counter, 0, 1) || // Vertical
                  checkDirection(board, position, counter, 1, 1) || // Diagonal \
                  checkDirection(board, position, counter, 1, -1)) { // Diagonal /
            return true; // Winning condition
          }
        }
      }
    }

    // Check if the board is full (draw)
    for (int col = 0; col < board.getConfig().getWidth(); col++) {
      if (isColumnPlayable(board, col)) {
        return false; // Not a draw yet
      }
    }

    return true; // Game over (draw or win)
  }

  private boolean checkDirection(Board board, Position position, Counter counter, int dx, int dy) {
    int count = 0;
    int x = position.getX();
    int y = position.getY();

    // Count consecutive counters in the specified direction
    for (int i = 0; i < 4; i++) {
      Position newPosition = new Position(x + i * dx, y + i * dy);
      if (board.isWithinBoard(newPosition) && counter.equals(board.getCounterAtPosition(newPosition))) {
        count++;
      } else {
        break;
      }
    }

    return count == 4; // Return true if 4 counters are connected
  }

  private int evaluateBoard(Board board) {
    int score = 0;

    // Evaluate each position on the board
    for (int x = 0; x < board.getConfig().getWidth(); x++) {
      for (int y = 0; y < board.getConfig().getHeight(); y++) {
        Position position = new Position(x, y);
        if (board.hasCounterAtPosition(position)) {
          Counter counter = board.getCounterAtPosition(position);

          // Assign scores based on consecutive counters
          if (counter == getCounter()) {
            score += evaluatePosition(board, position, counter);
          } else {
            score -= evaluatePosition(board, position, counter);
          }
        }
      }
    }

    return score;
  }

  private int evaluatePosition(Board board, Position position, Counter counter) {
    int score = 0;

    // Check all directions from this position
    score += scoreDirection(board, position, counter, 1, 0); // Horizontal
    score += scoreDirection(board, position, counter, 0, 1); // Vertical
    score += scoreDirection(board, position, counter, 1, 1); // Diagonal \
    score += scoreDirection(board, position, counter, 1, -1); // Diagonal /

    return score;
  }

  public int scoreDirection(Board board, Position position, Counter counter, int dx, int dy) {
    int count = 0;
    int openEnds = 0;
    int x = position.getX();
    int y = position.getY();

    // Count consecutive counters
    for (int i = 0; i < 4; i++) {
      Position newPosition = new Position(x + i * dx, y + i * dy);
      if (board.isWithinBoard(newPosition)) {
        if (counter.equals(board.getCounterAtPosition(newPosition))) {
          count++;
        } else if (!board.hasCounterAtPosition(newPosition)) {
          openEnds++;
          break;
        }
      }
    }

    // Scoring system for connected counters
    if (count == 4) {
      return 1000; // Winning position
    } else if (count == 3 && openEnds > 0) {
      return 50; // Strong position
    }
    else if (count == 2) {
      return 10; // Weak position
    }

    return 0;
  }

  private boolean isColumnPlayable(Board board, int col) {
    Position position = new Position(col, board.getConfig().getHeight() - 1);
    return board.isWithinBoard(position) && !board.hasCounterAtPosition(position);
  }
}
