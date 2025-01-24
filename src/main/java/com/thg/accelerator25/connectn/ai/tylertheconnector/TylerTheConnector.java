package com.thg.accelerator25.connectn.ai.tylertheconnector;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.InvalidMoveException;
import com.thehutgroup.accelerator.connectn.player.Player;
import com.thehutgroup.accelerator.connectn.player.Position;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class TylerTheConnector extends Player {
  private static final int MAX_DEPTH = 100;

  private final HashMap<Long, Integer> transpositionTable = new HashMap<>();

  private final long[][][] zobristTable;



  public TylerTheConnector(Counter counter) {
    super(counter, TylerTheConnector.class.getName());
    zobristTable = generateZobristTable();
  }

  private long[][][] generateZobristTable() {
    Random random = new Random();
    int width = 10;  // Adjust to your board's width
    int height = 8; // Adjust to your board's height
    long[][][] table = new long[width][height][3]; // 3 states: empty, yourCounter, opponentCounter
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        for (int state = 0; state < 3; state++) {
          table[x][y][state] = random.nextLong();
        }
      }
    }
    return table;
  }
//
  private long computeHash(Board board) {
    long hash = 0;
    for (int x = 0; x < board.getConfig().getWidth(); x++) {
      for (int y = 0; y < board.getConfig().getHeight(); y++) {
        Position position = new Position(x, y);
        int state = getStateForPosition(board, position); // 0: empty, 1: yourCounter, 2: opponentCounter
        hash ^= zobristTable[x][y][state];
      }
    }
    return hash;
  }

  // Get the state for a position (0: empty, 1: yourCounter, 2: opponentCounter)
  private int getStateForPosition(Board board, Position position) {
    if (!board.hasCounterAtPosition(position)) return 0; // Empty
    return board.getCounterAtPosition(position) == getCounter() ? 1 : 2; // Your counter or opponent's
  }

  // Update the Zobrist hash after a move
  private long updateHash(long currentHash, int x, int y, int oldState, int newState) {
    currentHash ^= zobristTable[x][y][oldState]; // Remove the old state
    currentHash ^= zobristTable[x][y][newState]; // Add the new state
    return currentHash;
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
    long hash = computeHash(board);
    if (transpositionTable.containsKey(hash)) {
      return transpositionTable.get(hash); // Return cached score
    }
    if (System.currentTimeMillis() - startTime >= timeLimit - 500) {
      throw new TimeoutException("Time limit reached");
    }
    if (depth == 0 || isGameOver(board)) {
//      return evaluateBoard(board);
      int score = evaluateBoard(board);
      transpositionTable.put(hash, score); // Cache the result
      return score;
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
      transpositionTable.put(hash, maxEval); // Cache the result
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
      transpositionTable.put(hash, minEval); // Cache the result
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

  private final int[] columnWeights = {0, 1, 2, 3, 4, 4, 3, 2, 1, 0};

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
    int gapCount = 0;
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
          x = newPosition.getX();
          y = newPosition.getY();
          Position nextPosition = new Position(x + dx, y + dy);
          if (count > 0 && openEnds == 2 && board.hasCounterAtPosition(nextPosition)) {
            gapCount++;
          }
          break;
        } else {
          break;
        }
      }
    }

    // Scoring system for connected counters
    if (count == 4) {
      return 1000; // Winning position
    }
    else if (count == 3 && openEnds > 0) {
      return 400; // Strong position
    }
    else if (count == 2 && gapCount == 1) {
      return 350; // Strong position
    }
    else if (count == 1 && gapCount == 1) {
      return 100; // Strong position
    }
    else if (count == 2 && openEnds == 2) {
      return 30; // Weak position
    }
    else if (count == 2 && openEnds == 1) {
      return 8; // Weaker position
    }

    return 0;
  }

  private boolean isColumnPlayable(Board board, int col) {
    Position position = new Position(col, board.getConfig().getHeight() - 1);
    return board.isWithinBoard(position) && !board.hasCounterAtPosition(position);
  }
}