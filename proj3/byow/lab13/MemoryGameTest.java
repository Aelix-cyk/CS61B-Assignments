package byow.lab13;

import org.junit.Test;

import static org.junit.Assert.*;

public class MemoryGameTest {

    /** Creates a headless game whose input comes from the given string. */
    private static MemoryGame testGame(long seed, String input) {
        return new MemoryGame(40, 40, seed, new StringCharSource(input), new TestDisplay());
    }

    @Test
    public void testGenerateRandomStringDeterministic() {
        MemoryGame a = testGame(42, "");
        MemoryGame b = testGame(42, "");
        assertEquals(a.generateRandomString(10), b.generateRandomString(10));
    }

    @Test
    public void testGenerateRandomStringLength() {
        MemoryGame g = testGame(42, "");
        assertEquals(5, g.generateRandomString(5).length());
        assertEquals(1, g.generateRandomString(1).length());
    }

    @Test
    public void testGenerateRandomStringLowercase() {
        MemoryGame g = testGame(42, "");
        String s = g.generateRandomString(20);
        for (int i = 0; i < s.length(); i += 1) {
            char c = s.charAt(i);
            assertTrue("'" + c + "' should be lowercase", c >= 'a' && c <= 'z');
        }
    }

    @Test
    public void testSolicitNCharsInputReadsFromSource() {
        MemoryGame g = testGame(1, "abc");
        assertEquals("abc", g.solicitNCharsInput(3));
    }

    @Test
    public void testSolicitNCharsInputStopsWhenSourceExhausted() {
        MemoryGame g = testGame(1, "ab");
        assertEquals("ab", g.solicitNCharsInput(5));
    }

    @Test
    public void testStartGameWrongInputEndsGame() {
        MemoryGame g = testGame(1, "zzzzzzzzzzzz");
        g.startGame();
        assertTrue("game should be over after a wrong answer", g.isGameOver());
    }

    @Test
    public void testStartGameShowsGameOverMessage() {
        TestDisplay display = new TestDisplay();
        MemoryGame g = new MemoryGame(40, 40, 1,
                new StringCharSource("zzzzzzzzzzzz"), display);
        g.startGame();
        assertTrue("last frame should mention Game Over, got: " + display.lastShown(),
                display.lastShown().startsWith("Game Over"));
    }

    @Test
    public void testDrawFrameDoesNotThrowHeadless() {
        MemoryGame g = testGame(1, "");
        g.drawFrame("hello"); // must not throw (no JFrame created)
        assertTrue(true);
    }
}
