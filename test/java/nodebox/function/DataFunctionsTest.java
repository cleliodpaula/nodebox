package nodebox.function;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;
import static nodebox.function.DataFunctions.importCSV;
import static nodebox.function.DataFunctions.lookup;
import static nodebox.util.Assertions.assertResultsEqual;

public class DataFunctionsTest {

    @Test
    public void testLookupNull() {
        assertNull(lookup(null, "xxx"));
        assertNull(lookup(new Point(11, 22), null));
    }

    @Test
    public void testLookupInMap() {
        Map<String, Integer> greek = ImmutableMap.of("alpha", 1, "beta", 2, "gamma", 3);
        assertEquals(1, lookup(greek, "alpha"));
        assertEquals(2, lookup(greek, "beta"));
        assertNull(lookup(greek, "xxx"));
    }

    @Test
    public void testLookupInObject() {
        Point awtPoint = new Point(11, 22);
        assertEquals(11.0, lookup(awtPoint, "x"));
        assertEquals(22.0, lookup(awtPoint, "y"));
        assertNull(lookup(awtPoint, "xxx"));
    }

    @Test
    public void testImportCSV() {
        List<Map<String, Object>> l = importCSV("test/files/colors.csv");
        assertEquals(5, l.size());
        Map<String, Object> black = l.get(0);
        assertResultsEqual(black.keySet(), "Name", "Red", "Green", "Blue");
        assertEquals("Black", black.get("Name"));
        // Numerical data is automatically converted to doubles.
        assertEquals(0.0, black.get("Red"));
    }

    @Test
    public void testImportCSVUnicode() {
        List<Map<String, Object>> l = importCSV("test/files/unicode.csv");
        assertEquals(2, l.size());
        Map<String, Object> frederik = l.get(0);
        assertResultsEqual(frederik.keySet(), "Name", "Age");
        assertEquals("Fr\u00e9d\u00ebr\u00eck", frederik.get("Name"));
        Map<String, Object> bob = l.get(1);
        assertEquals("B\u00f8b", bob.get("Name"));
    }

    @Test
    public void testImportEmptyCSV() {
        List l = importCSV(null);
        assertTrue(l.isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void testImportNonexistentCSV() {
        importCSV("blah/blah.csv");
    }

    @Test
    public void testImportCSVWithWhitespace() {
        List<Map<String, Object>> l = importCSV("test/files/whitespace.csv");
        assertEquals(2, l.size());
        Map<String, Object> alice = l.get(0);
        assertResultsEqual(alice.keySet(), "Name", "Age");
        assertEquals("Alice", alice.get("Name"));
        // Numerical data is automatically converted to doubles.
        assertEquals(41.0, alice.get("Age"));
    }

    @Test
    public void testImportCSVWithBadHeaders() {
        List<Map<String, Object>> l = importCSV("test/files/bad-headers.csv");
        assertEquals(2, l.size());
        Map<String, Object> row1 = l.get(0);
        assertResultsEqual(row1.keySet(), "Alpha", "Column 2", "Column 3");
        assertResultsEqual(row1.values(), 1.0, 2.0, 3.0);
    }

}
