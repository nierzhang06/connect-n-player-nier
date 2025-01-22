package com.thg.accelerator21.connectn.ai.tylertheconnector;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.GameConfig;
import com.thehutgroup.accelerator.connectn.player.Position;
import com.thg.accelerator23.connectn.ai.tylertheconnector.TylerTheConnector;
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
  public void testScoreDirection() throws Exception {
    // Arrange
    Board board = new Board(new GameConfig(10, 8, 4));
    TylerTheConnector ai = new TylerTheConnector(Counter.X);

    // Simulate placing counters using the Board constructor
//    board = new Board(board, 2, Counter.O);
//    board = new Board(board, 3, Counter.X);
    board = new Board(board, 4, Counter.O);
    board = new Board(board, 5, Counter.O);
    board = new Board(board, 5, Counter.O);
    board = new Board(board, 4, Counter.X);
    board = new Board(board, 5, Counter.X);

    int score = ai.scoreDirection(board, new Position(4, 1), Counter.X, 1, 1); // Horizontal scoring


    assertEquals(10, score, "Score should match the expected value for two connected counters with open ends.");
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




}




