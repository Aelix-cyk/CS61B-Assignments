package byow.Core;

import byow.TileEngine.TETile;
import org.junit.Test;

import static org.junit.Assert.*;

public class EngineTest {

    @Test
    public void testNewGameProducesWorld() {
        Engine e = new Engine();
        TETile[][] world = e.interactWithInputString("n123sss");
        assertNotNull(world);
        assertEquals(Engine.WIDTH, world.length);
        assertEquals(Engine.HEIGHT, world[0].length);
    }

    @Test
    public void testSameSeedDeterministic() {
        Engine e1 = new Engine();
        Engine e2 = new Engine();
        TETile[][] a = e1.interactWithInputString("n123sss");
        TETile[][] b = e2.interactWithInputString("n123sssww");
        assertEquals(TETile.toString(a), TETile.toString(b));
    }

    @Test
    public void testDifferentSeedDifferentWorld() {
        Engine e1 = new Engine();
        Engine e2 = new Engine();
        TETile[][] a = e1.interactWithInputString("n123");
        TETile[][] b = e2.interactWithInputString("n999");
        assertNotEquals(TETile.toString(a), TETile.toString(b));
    }

    @Test
    public void testLoadWithoutSaveReturnsNull() {
        Engine e = new Engine();
        TETile[][] world = e.interactWithInputString("lwww");
        assertNull("load with no save must not fabricate a world", world);
    }

    @Test
    public void testToStringEmptyWhenNoWorld() {
        Engine e = new Engine();
        e.interactWithInputString("l");
        assertEquals("", e.toString());
    }

    @Test
    public void testToStringNonEmptyAfterNewGame() {
        Engine e = new Engine();
        e.interactWithInputString("n42");
        assertTrue(e.toString().length() > 0);
    }

    @Test
    public void testQuitColonStopsProcessing() {
        Engine e = new Engine();
        TETile[][] world = e.interactWithInputString("n123sss:q");
        assertNotNull(world);
        // ':q' must not cause a crash; world generated before it
        assertEquals(Engine.WIDTH, world.length);
    }

    @Test
    public void testEmptyInputFallsBackToDefaultWorld() {
        Engine e = new Engine();
        TETile[][] world = e.interactWithInputString("");
        assertNotNull("empty input should fall back to a default world", world);
    }

    @Test
    public void testMovementKeysAreNoOps() {
        Engine e1 = new Engine();
        Engine e2 = new Engine();
        TETile[][] withMovement = e1.interactWithInputString("n7wwssaad");
        TETile[][] without = e2.interactWithInputString("n7");
        assertEquals(TETile.toString(withMovement), TETile.toString(without));
    }
}
