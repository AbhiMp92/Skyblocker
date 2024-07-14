package de.hysky.skyblocker.skyblock.chat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class ChatRuleTest {

    @Test
    void isMatch() {
        ChatRule testRule = new ChatRule(
                "Test Rule",    // name
                true,           // enabled
                false,          // isPartialMatch
                false,          // isRegex
                true,           // isIgnoreCase
                "test",         // filter
                "",             // validLocations (assuming empty for test)
                false,          // hideMessage
                false,          // showActionBar
                false,          // showAnnouncement
                null,           // replaceMessage (null for no replacement)
                null            // customSound (assuming null for test)
        );

        Assertions.assertTrue(testRule.isMatch("test"));           // test simple filter works
        Assertions.assertFalse(testRule.isMatch("test extra"));    // test partial match works

        // Test with isPartialMatch true
        testRule = new ChatRule(
                "Test Rule", true, true, false, true, "test", "", false, false, false, null, null
        );
        Assertions.assertTrue(testRule.isMatch("test extra"));

        // Test with regex filter
        testRule = new ChatRule(
                "Regex Rule",   // name
                true,           // enabled
                false,          // isPartialMatch
                true,           // isRegex
                true,           // isIgnoreCase
                "[0-9]+",       // filter
                "",             // validLocations
                false,          // hideMessage
                false,          // showActionBar
                false,          // showAnnouncement
                null,           // replaceMessage (null for no replacement)
                null            // customSound
        );

        Assertions.assertTrue(testRule.isMatch("1234567"));
        Assertions.assertFalse(testRule.isMatch("1234567 test"));
    }
}
