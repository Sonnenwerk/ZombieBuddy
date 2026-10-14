package me.zed_0xff.zombie_buddy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import java.nio.file.Path;
import java.io.File;
import me.zed_0xff.zombie_buddy.SteamWorkshop.SteamID64;
import me.zed_0xff.zombie_buddy.SteamWorkshop.WorkshopItemID;

class ZBGsonTest {
    private final Gson gson = ZBGson.PRETTY;

    @Test
    void testPathAdapter() {
        Path p = Path.of("a", "b", "c");
        String json = gson.toJson(p);
        String expectedPath = "a" + File.separator + "b" + File.separator + "c";
        assertTrue(json.contains(expectedPath.replace("\\", "\\\\")), "JSON string should contain the path");

        Path parsed = gson.fromJson("\"" + expectedPath.replace("\\", "\\\\") + "\"", Path.class);
        assertEquals(p, parsed, "Parsed path should match the original");

        assertNull(gson.fromJson("null", Path.class), "Null JSON should parse to null");
        assertEquals("null", gson.toJson(null, Path.class), "Null path should serialize to null");

        assertNull(gson.fromJson("\"\"", Path.class), "Empty string JSON should parse to null");
        assertNull(gson.fromJson("\"   \"", Path.class), "Blank string JSON should parse to null");
    }

    @Test
    void testModFlagsAdapter() {
        ModFlags flags = new ModFlags(5);
        String json = gson.toJson(flags);
        assertEquals("5", json, "ModFlags should serialize to its integer value");

        ModFlags parsed = gson.fromJson("5", ModFlags.class);
        assertEquals(5, parsed.value(), "Parsed ModFlags should have correct value");

        assertEquals(0, gson.fromJson("null", ModFlags.class).value(), "Null JSON should parse to ModFlags with value 0");
        assertEquals("0", gson.toJson((ModFlags)null, ModFlags.class), "Null ModFlags should serialize to 0");
    }

    @Test
    void testLongAdapters() {
        WorkshopItemID wId = new WorkshopItemID(1234567890L);
        assertEquals("1234567890", gson.toJson(wId), "WorkshopItemID should serialize correctly");
        assertEquals(1234567890L, gson.fromJson("1234567890", WorkshopItemID.class).value(), "WorkshopItemID should deserialize correctly");

        SteamID64 sId = new SteamID64(9876543210L);
        assertEquals("9876543210", gson.toJson(sId), "SteamID64 should serialize correctly");
        assertEquals(9876543210L, gson.fromJson("9876543210", SteamID64.class).value(), "SteamID64 should deserialize correctly");

        assertNull(gson.fromJson("null", WorkshopItemID.class), "Null JSON should parse to null WorkshopItemID");
        assertNull(gson.fromJson("null", SteamID64.class), "Null JSON should parse to null SteamID64");

        assertEquals("null", gson.toJson(null, WorkshopItemID.class), "Null WorkshopItemID should serialize to null");
        assertEquals("null", gson.toJson(null, SteamID64.class), "Null SteamID64 should serialize to null");
    }
}
