package tools.subathon.timer.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateParserTest {

    @Test
    void builderThrowsIllegalArgumentExceptionOnNoTemplate() {
        assertThrows(IllegalArgumentException.class,
                () -> TemplateParser.builder().build());
    }

    @ParameterizedTest(name = "Invalid template: {0}")
    @MethodSource("provideInvalidTemplates")
    void builderThrowsIllegalArgumentException(String invalidTemplate) {
        assertThrows(IllegalArgumentException.class,
                () -> TemplateParser.builder().withTemplate(invalidTemplate).build());
    }

    @ParameterizedTest(name = "Parse template: {0} -> {1}")
    @MethodSource("provideAmountParsingTestCases")
    void buildsAndParsesAmount(String template, String text, String expectedAmount) {
        TemplateParser parser = assertDoesNotThrow(() -> TemplateParser.builder().withTemplate(template).build());
        TemplateParser.ParseResult result = parser.parse(text);

        assertTrue(result.success());
        assertEquals(1, result.values().size());
        assertTrue(result.values().containsKey("amount"));
        assertEquals(expectedAmount, result.values().get("amount"));
    }

    @ParameterizedTest(name = "Parse template with user and amount: {0}")
    @MethodSource("provideAmountAndUserParsingTestCases")
    void buildsAndParsesAmountAndUser(String template, String text, String expectedUser, String expectedAmount) {
        TemplateParser parser = assertDoesNotThrow(() -> TemplateParser.builder().withTemplate(template).build());
        TemplateParser.ParseResult result = parser.parse(text);

        assertTrue(result.success());
        assertEquals(2, result.values().size());
        assertEquals(expectedUser, result.values().get("user"));
        assertEquals(expectedAmount, result.values().get("amount"));
    }

    @ParameterizedTest(name = "Parse text failure - {2}")
    @MethodSource("provideParsingFailureTestCases")
    void buildsButFailsParsing(String template, String text, String expectedError) {
        TemplateParser parser = assertDoesNotThrow(() -> TemplateParser.builder().withTemplate(template).build());
        TemplateParser.ParseResult result = parser.parse(text);

        assertFalse(result.success());
        assertEquals(expectedError, result.errorMessage());
    }

    static Stream<Arguments> provideInvalidTemplates() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("Template with no placeholder"),
                Arguments.of("Template with unclosed {user placeholder"),
                Arguments.of("Template with illegal {unknown} placeholder"),
                Arguments.of("Template with {user} but no amount placeholder}"),
                Arguments.of("Template with duplicate {user} placeholder {user}"),
                Arguments.of("Template with duplicate {amount} placeholder {amount}"),
                Arguments.of("Template with adjacent {amount}{user} placeholders")
        );
    }

    static Stream<Arguments> provideAmountParsingTestCases() {
        return Stream.of(
                Arguments.of("{amount}", "111", "111"),
                Arguments.of("This simple template contains {amount} eggs.", "This simple template contains 12 eggs.", "12")
        );
    }

    static Stream<Arguments> provideAmountAndUserParsingTestCases() {
        return Stream.of(
                Arguments.of("{user} {amount}", "testuser 123111", "testuser", "123111"),
                Arguments.of("This simple template contains a {user} and {amount} of something.", "This simple template contains a testuser and 123111 of something.", "testuser", "123111"),
                Arguments.of("ttt{user}ttt{amount}ttt", "ttt456ttt123ttt", "456", "123"),
                Arguments.of("abc{user} and {amount}", "abcGigatron and 3", "Gigatron", "3")
        );
    }

    static Stream<Arguments> provideParsingFailureTestCases() {
        return Stream.of(
                Arguments.of("A {user} someText {amount}.", "A  someText 123.", "Placeholder 'user' is empty"),
                Arguments.of("A {user} someText {amount}.", "A testuser someText 123. some more text.", "Did not parse whole message, text remaining after last literal!"),
                Arguments.of("A {user} someText {amount}.", "A testuser someText 123", "Missing anchor '.' for placeholder 'amount'"),
                Arguments.of("A {user} someText {amount}.", "Banana testuser someText 123.", "Literal 'A ' not found at expected position"),
                Arguments.of("A {user} someText {amount}.", "bbb A testuser someText 123", "Literal 'A ' not found at expected position")
        );
    }
}
