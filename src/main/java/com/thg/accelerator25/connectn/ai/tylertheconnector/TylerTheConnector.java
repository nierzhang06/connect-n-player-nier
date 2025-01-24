package com.thg.accelerator25.connectn.ai.tylertheconnector;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.InvalidMoveException;
import com.thehutgroup.accelerator.connectn.player.Player;
import com.thehutgroup.accelerator.connectn.player.Position;

import java.util.concurrent.TimeoutException;

public class TylerTheConnector extends Player {
  private static final int MAX_DEPTH = 100;


  public TylerTheConnector(Counter counter) {
    super(counter, TylerTheConnector.class.getName());
  }


  @Override
  public int makeMove(Board board) {
    int bestMove = -1;
    long startTime = System.currentTimeMillis();
    long timeLimit = 10000; // 10 seconds in milliseconds

    for (int depth = 1; depth <= MAX_DEPTH; depth++) {
      System.out.println("Depth: " + depth);
      int currentBestMove = -1;

      try {
        currentBestMove = iterativeDeepeningMinimax(board, depth, startTime, timeLimit);
      } catch (TimeoutException e) {
        System.out.println("TimeoutException: " + e.getMessage());
        break; // Stop searching if we run out of time
      }

      if (currentBestMove != -1) {
        bestMove = currentBestMove; // Update the best move found so far
        System.out.println("Best move: " + bestMove);
      }
    }
    System.out.println("Final best move: " + bestMove);
    return bestMove;
  }



  private int iterativeDeepeningMinimax(Board board, int depth, long startTime, long timeLimit) throws TimeoutException {
    int bestMove = -1;
    int bestScore = Integer.MIN_VALUE;

    for (int col = 0; col < board.getConfig().getWidth(); col++) {
      if (isColumnPlayable(board, col)) {
        // Check if we're running out of time
        if (System.currentTimeMillis() - startTime >= timeLimit - 500) {
          throw new TimeoutException("Time limit reached");
        }

        try {
          Board newBoard = new Board(board, col, getCounter());
          int score = minimax(newBoard, depth - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE, startTime, timeLimit);

          if (score > bestScore) {
            bestScore = score;
            bestMove = col; // Update best move for the current depth
          }
        } catch (InvalidMoveException ignored) {
          // Skip invalid moves
        }
      }
    }
    System.out.println("best score: " + bestScore);
    return bestMove; // Return the best move for this depth
  }

  private int minimax(Board board, int depth, boolean isMaximising, int alpha, int beta, long startTime, long timeLimit) throws TimeoutException {
    if (System.currentTimeMillis() - startTime >= timeLimit - 500) {
      throw new TimeoutException("Time limit reached");
    }
    if (depth == 0 || isGameOver(board)) {
      return evaluateBoard(board);
    }

    if (isMaximising) {
      int maxEval = Integer.MIN_VALUE;
      for (int col = 0; col < board.getConfig().getWidth(); col++) {
        if (isColumnPlayable(board, col)) {
          try {
            Board newBoard = new Board(board, col, getCounter());
            int eval = minimax(newBoard, depth - 1, false, alpha, beta, startTime, timeLimit);
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
            int eval = minimax(newBoard, depth - 1, true, alpha, beta, startTime, timeLimit);
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

  private final int[] columnWeights = {0, 1, 4, 8, 14, 14, 8, 4, 1, 0};

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
            score += getPositionWeight(x, board.getConfig().getWidth());
          } else {
            score -= evaluatePosition(board, position, counter);
            score -= getPositionWeight(x, board.getConfig().getWidth());
          }
        }
      }
    }

    return score;
  }

  // Centre Control
  private int getPositionWeight(int column, int boardWidth) {

    if (boardWidth == columnWeights.length) {
      return columnWeights[column];
    }
    return 0;
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


    Position previousPosition = new Position(x - dx, y - dy);
    if (board.isWithinBoard(previousPosition) && (!board.hasCounterAtPosition(previousPosition))) {
      openEnds++;
    }

    // Count consecutive counters
    for (int i = 0; i < 4; i++) {

      Position newPosition = new Position(x + i * dx, y + i * dy);
      if (board.isWithinBoard(newPosition)) {
        if (counter.equals(board.getCounterAtPosition(newPosition))) {
          count++;
        } else if (!board.hasCounterAtPosition(newPosition)) {
          openEnds++;
          break;
        } else {
          break;
        }
      }
    }

    // Scoring system for connected counters
    if (count == 4) {
      return 10000; // Winning position
    } else if (count == 3 && openEnds == 2) {
      return 400; // Strong position
    } else if (count == 3 && openEnds == 1) {
      return 300; // less Strong position
    }
    else if (count == 2 && openEnds == 2) {
      return 20; // Weak position
    }
    else if (count == 2 && openEnds == 1) {
      return 10; // Weaker position
    }

    return 0;
  }



  private boolean isColumnPlayable(Board board, int col) {
    Position position = new Position(col, board.getConfig().getHeight() - 1);
    return board.isWithinBoard(position) && !board.hasCounterAtPosition(position);
  }
}
