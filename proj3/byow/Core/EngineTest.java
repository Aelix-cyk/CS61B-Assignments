package byow.Core;

import byow.TileEngine.TETile;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EngineTest {

    @Before
    public void cleanSave() {
        // remove any leftover saved game so tests start in a clean state
        Engine.clearSave();
    }

    @Test
    public void testNewGameProducesWorld() {
        Engine e = new Engine();
        TETile[][] world = e.interactWithInputString("n123sss");
        assertNotNull(world);
        assertEquals(Engine.WIDTH, world.length);
        assertEquals(Engine.HEIGHT, world[0].length);
        assertTrue("world should contain the avatar tile @",
                TETile.toString(world).contains("@"));
    }

    @Test
    public void testSameSeedDeterministic() {
        Engine e1 = new Engine();
        Engine e2 = new Engine();
        TETile[][] a = e1.interactWithInputString("n123sss");
        TETile[][] b = e2.interactWithInputString("n123sss");
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
    public void testLoadWithoutSaveReturnsDefaultWorld() {
        Engine e = new Engine();
        TETile[][] world = e.interactWithInputString("lwww");
        // load with no save falls back to a default world (never null), matching keyboard mode
        assertNotNull("interactWithInputString must never return null", world);
        assertEquals(Engine.WIDTH, world.length);
        assertEquals(Engine.HEIGHT, world[0].length);
    }

    @Test
    public void testToStringNonEmptyEvenWithoutWorld() {
        Engine e = new Engine();
        e.interactWithInputString("l");
        // no save exists, but the engine fabricates a default world (never null)
        assertTrue(e.toString().length() > 0);
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
    public void testSaveThenLoadRestoresWorld() {
        // play a world, move, then quit-save
        Engine e = new Engine();
        TETile[][] played = e.interactWithInputString("n123ddd:q");
        assertNotNull(played);

        // load it in a fresh engine
        Engine e2 = new Engine();
        TETile[][] loaded = e2.interactWithInputString("l");
        assertNotNull("load should restore a saved world", loaded);
        assertEquals(TETile.toString(played), TETile.toString(loaded));
    }

    @Test
    public void testSplitInputWithSaveLoadEqualsSingleInput() {
        // spec equivalence: n123sss:q then lww == n123sssww
        Engine a = new Engine();
        a.interactWithInputString("n123sss:q");

        Engine b = new Engine();
        TETile[][] split = b.interactWithInputString("lww");

        Engine c = new Engine();
        TETile[][] single = c.interactWithInputString("n123sssww");

        assertEquals("split input with save/load must equal single input",
                TETile.toString(split), TETile.toString(single));
    }

    @Test
    public void testMovementWorksAfterLoad() {
        // save a world, then load and move: the avatar must actually move
        // (regression: reference-equality on deserialized tiles blocked movement)
        new Engine().interactWithInputString("n123sss:q");

        Engine e = new Engine();
        TETile[][] before = e.interactWithInputString("l");      // just load
        Engine e2 = new Engine();
        TETile[][] after = e2.interactWithInputString("lwww");   // load + move
        assertNotEquals("movement must work after loading a saved world",
                TETile.toString(before), TETile.toString(after));
    }

    @Test
    public void testEmptyInputFallsBackToDefaultWorld() {
        Engine e = new Engine();
        TETile[][] world = e.interactWithInputString("");
        assertNotNull("empty input should fall back to a default world", world);
    }

    @Test
    public void testMovementMovesAvatar() {
        Engine e1 = new Engine();
        Engine e2 = new Engine();
        TETile[][] moved = e1.interactWithInputString("n7wwssaad");
        TETile[][] static_ = e2.interactWithInputString("n7");
        assertNotEquals("movement keys should change the world (avatar moves)",
                TETile.toString(moved), TETile.toString(static_));
    }
}
