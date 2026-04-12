package tools.subathon.timer.bot.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        assertThrows(IllegalArgumentException.class,
                () -> TemplateParser.builder().withTemplate("Template with adjacent {amount}{user} placeholders").build());
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

        TemplateParser repeatingLiteralParser = assertDoesNotThrow(() -> TemplateParser.builder().withTemplate(repeatingLiterals).build());
        TemplateParser.ParseResult repeatingLiteralResult = repeatingLiteralParser.parse(placeholderTextDifferentFromLiteral);

        assertTrue(repeatingLiteralResult.success());
        assertEquals(2, repeatingLiteralResult.values().size());
        assertEquals("456", repeatingLiteralResult.values().get("user"));
        assertEquals("123", repeatingLiteralResult.values().get("amount"));

        String placeholderAtEnd = "abc{user} and {amount}";
        String placeholderAtEndText = "abcGigatron and 3";

        TemplateParser placeholderAtEndParser = assertDoesNotThrow(() -> TemplateParser.builder().withTemplate(placeholderAtEnd).build());
        TemplateParser.ParseResult placeholderAtEndResult = placeholderAtEndParser.parse(placeholderAtEndText);

        assertTrue(placeholderAtEndResult.success());
        assertEquals(2, placeholderAtEndResult.values().size());
        assertEquals("Gigatron", placeholderAtEndResult.values().get("user"));
        assertEquals("3", placeholderAtEndResult.values().get("amount"));
    }

    @Test
    void templateParser_buildsButFailsParsing() {
        String validTemplate = "A {user} someText {amount}.";

        TemplateParser parser = assertDoesNotThrow(() -> TemplateParser.builder().withTemplate(validTemplate).build());

        String missingUser = "A  someText 123.";
        TemplateParser.ParseResult missingUserResult = parser.parse(missingUser);

        assertFalse(missingUserResult.success());
        assertEquals("Placeholder 'user' is empty", missingUserResult.errorMessage());

        String textContinues = "A testuser someText 123. some more text.";

        TemplateParser.ParseResult textContinuesResult = parser.parse(textContinues);

        assertFalse(textContinuesResult.success());
        assertEquals("Did not parse whole message, text remaining after last literal!",  textContinuesResult.errorMessage());

        String textMissingLastLiteral = "A testuser someText 123";

        TemplateParser.ParseResult textMissingLastLiteralResult = parser.parse(textMissingLastLiteral);

        assertFalse(textMissingLastLiteralResult.success());
        assertEquals("Missing anchor '.' for placeholder 'amount'",  textMissingLastLiteralResult.errorMessage());

        String unexpectedFirstLiteral = "Banana testuser someText 123.";

        TemplateParser.ParseResult unexpectedFirstLiteralResult = parser.parse(unexpectedFirstLiteral);

        assertFalse(unexpectedFirstLiteralResult.success());
        assertEquals("Literal 'A ' not found at expected position", unexpectedFirstLiteralResult.errorMessage());

        String extraLiteralAtStart = "bbb A testuser someText 123";

        TemplateParser.ParseResult extraLiteralAtStartResult = parser.parse(extraLiteralAtStart);

        assertFalse(extraLiteralAtStartResult.success());
        assertEquals("Literal 'A ' not found at expected position", extraLiteralAtStartResult.errorMessage());
    }

}