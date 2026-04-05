package tools.subathon.timer.bot.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateParserTest {

    @Test
    void templateParser_builderThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> TemplateParser.builder().build());

        assertThrows(IllegalArgumentException.class,
                () -> TemplateParser.builder().withTemplate("").build());

        assertThrows(IllegalArgumentException.class,
                () -> TemplateParser.builder().withTemplate("Template with no placeholder").build());

        assertThrows(IllegalArgumentException.class,
                () -> TemplateParser.builder().withTemplate("Template with unclosed {user placeholder").build());

        assertThrows(IllegalArgumentException.class,
                () -> TemplateParser.builder().withTemplate("Template with illegal {unknown} placeholder").build());

        assertThrows(IllegalArgumentException.class,
                () -> TemplateParser.builder().withTemplate("Template with {user} but no amount placeholder}").build());

        assertThrows(IllegalArgumentException.class,
                () -> TemplateParser.builder().withTemplate("Template with duplicate {user} placeholder {user}").build());

        assertThrows(IllegalArgumentException.class,
                () -> TemplateParser.builder().withTemplate("Template with duplicate {amount} placeholder {amount}").build());
    }

    @Test
    void templateParser_buildsAndParsesAmount() {
        String amount = "{amount}";
        String amountString = "111";

        TemplateParser parserOne = assertDoesNotThrow(() -> TemplateParser.builder().withTemplate(amount).build());
        TemplateParser.ParseResult resultOne = parserOne.parse(amountString);

        assertTrue(resultOne.success());
        assertEquals(1, resultOne.values().size());
        assertTrue(resultOne.values().containsKey("amount"));
        assertEquals(amountString, resultOne.values().get("amount"));

        String template = "This simple template contains {amount} eggs.";
        String text = "This simple template contains 12 eggs.";

        TemplateParser parser = assertDoesNotThrow(() -> TemplateParser.builder().withTemplate(template).build());

        TemplateParser.ParseResult result = parser.parse(text);

        assertTrue(result.success());
        assertEquals(1, result.values().size());
        assertTrue(result.values().containsKey("amount"));
        assertEquals("12", result.values().get("amount"));
    }

    @Test
    void templateParser_buildsAndParsesAmountAndUser() {
        String justPlaceholders = "{user} {amount}";
        String justPlaceholdersText = "testuser 123111";

        TemplateParser parserOne = assertDoesNotThrow(() -> TemplateParser.builder().withTemplate(justPlaceholders).build());
        TemplateParser.ParseResult resultOne = parserOne.parse(justPlaceholdersText);

        assertTrue(resultOne.success());
        assertEquals(2, resultOne.values().size());
        assertEquals("testuser", resultOne.values().get("user"));
        assertEquals("123111", resultOne.values().get("amount"));

        String template = "This simple template contains a {user} and {amount} of something.";
        String text = "This simple template contains a testuser and 123111 of something.";

        TemplateParser parser = assertDoesNotThrow(() -> TemplateParser.builder().withTemplate(template).build());
        TemplateParser.ParseResult result = parser.parse(text);

        assertTrue(result.success());
        assertEquals(2, result.values().size());
        assertEquals("testuser", result.values().get("user"));
        assertEquals("123111", result.values().get("amount"));

        String repeatingLiterals = "ttt{user}ttt{amount}ttt";
        String placeholderTextDifferentFromLiteral = "ttt456ttt123ttt";

        TemplateParser complexParser = assertDoesNotThrow(() -> TemplateParser.builder().withTemplate(repeatingLiterals).build());
        TemplateParser.ParseResult complexResult = complexParser.parse(placeholderTextDifferentFromLiteral);

        assertTrue(complexResult.success());
        assertEquals(2, complexResult.values().size());
        assertEquals("456", complexResult.values().get("user"));
        assertEquals("123", complexResult.values().get("amount"));
    }

}