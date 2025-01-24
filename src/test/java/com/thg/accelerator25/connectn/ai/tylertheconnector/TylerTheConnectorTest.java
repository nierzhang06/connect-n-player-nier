package com.thg.accelerator25.connectn.ai.tylertheconnector;

import com.thehutgroup.accelerator.connectn.player.*;
import com.thg.accelerator25.connectn.ai.tylertheconnector.TylerTheConnector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class TylerTheConnectorTest {
  /**
   * Rigorous Test :-)
   */

//  public void shouldAnswerWithTrue() {
//    assertTrue(true);
//  }
  @Test
  public void testScoreDirectionThreeGap() throws Exception {
    // Arrange
    Board board = new Board(new GameConfig(10, 8, 4));
    TylerTheConnector ai = new TylerTheConnector(Counter.X);

    // Simulate placing counters using the Board constructor
//    board = new Board(board, 2, Counter.O);
//    board = new Board(board, 3, Counter.X);
    board = new Board(board, 1, Counter.X);
    board = new Board(board, 2, Counter.X);
    board = new Board(board, 4, Counter.X);
//    board = new Board(board, 3, Counter.O);


    int score = ai.scoreDirection(board, new Position(1, 0), Counter.X, 1, 0); // Horizontal scoring


    assertEquals(350, score, "Score should match the expected value for two connected counters with open ends.");
  }

  @Test
  public void testScoreDirectionTwoGap() throws Exception {
    // Arrange
    Board board = new Board(new GameConfig(10, 8, 4));
    TylerTheConnector ai = new TylerTheConnector(Counter.X);

    // Simulate placing counters using the Board constructor
//    board = new Board(board, 2, Counter.O);
//    board = new Board(board, 3, Counter.X);
    board = new Board(board, 1, Counter.X);
    board = new Board(board, 3, Counter.X);
//    board = new Board(board, 4, Counter.X);
//    board = new Board(board, 3, Counter.O);


    int score = ai.scoreDirection(board, new Position(1, 0), Counter.X, 1, 0); // Horizontal scoring


    assertEquals(100, score, "Score should match the expected value for two connected counters with open ends.");
  }

  @Test
  public void testIsGameOver() throws Exception {
    Board board = new Board(new GameConfig(10, 8, 4));
    TylerTheConnector ai = new TylerTheConnector(Counter.X);
    board = new Board(board, 0, Counter.X);
    board = new Board(board, 0, Counter.X);
    board = new Board(board, 0, Counter.X);
    board = new Board(board, 0, Counter.X);

    assertTrue(ai.isGameOver(board));


  }


//    @Test
//    public void testOpponentAboutToWinInColumn1() throws InvalidMoveException {
//      // Setup the board configuration
//      Board board = new Board(new GameConfig(10, 8, 4)); // 10x8 board with 4 counters needed to win
//
//      // Place counters to create the situation
//      // Opponent counters
//      board = new Board(board, 1, Counter.X);
//      board = new Board(board, 2, Counter.O);
//      board = new Board(board, 3, Counter.X);
//      board = new Board(board, 4, Counter.X);
//      board = new Board(board, 6, Counter.O);
//      board = new Board(board, 7, Counter.O);
//      board = new Board(board, 5, Counter.O);
//      board = new Board(board, 8, Counter.X);
//      board = new Board(board, 2, Counter.O);
//      board = new Board(board, 3, Counter.O);
//      board = new Board(board, 4, Counter.O);
//      board = new Board(board, 5, Counter.X);
//      board = new Board(board, 6, Counter.O);
//      board = new Board(board, 3, Counter.X);
//      board = new Board(board, 4, Counter.X);
//      board = new Board(board, 5, Counter.O);
//      board = new Board(board, 3, Counter.X);
//      board = new Board(board, 4, Counter.X);
//      board = new Board(board, 5, Counter.O);
//      board = new Board(board, 5, Counter.O);
//      board = new Board(board, 5, Counter.X);
//
//
//      TylerTheConnector aiPlayer = new TylerTheConnector(Counter.X);
//
//      // Perform the move
//      int chosenColumn = aiPlayer.makeMove(board);
//
//      // Assert that the AI blocks the opponent's winning move in column 1
//      assertEquals(1, chosenColumn, "AI did not block the opponent's winning move!");
//    }




}




