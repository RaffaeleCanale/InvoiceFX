package com.wx.invoicefx.util.string;

import com.nitorcreations.junit.runners.NestedRunner;
import com.wx.util.pair.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.wx.invoicefx.util.string.SentenceItemsParser.*;
import static org.junit.Assert.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 10.06.17.
 */
@RunWith(NestedRunner.class)
public class SentenceItemsParserTest {

    public class ParseItems {

        @Test
        public void emptySentence() {
            assertEquals(Collections.singletonList(""), parseItems(""));
        }

        @Test
        public void singleItemSentence() {
            assertEquals(Collections.singletonList("foo"), parseItems("foo"));
            assertEquals(Collections.singletonList("foo bar"), parseItems("foo bar"));
            assertEquals(Collections.singletonList("foo,bar etc"), parseItems("foo,bar etc"));
        }

        @Test
        public void multipleItemsSentence() {
            assertEquals(Arrays.asList("foo", ", ", "bar"), parseItems("foo, bar"));
            assertEquals(Arrays.asList(" foo  ", ", ", "   bar  "), parseItems(" foo  ,    bar  "));
            assertEquals(Arrays.asList("foo", " und ", "bar", " et ", "goo"), parseItems("foo und bar et goo"));
        }

        @Test
        public void sentenceWithConsecutiveStopWords() {
            assertEquals(Arrays.asList("foo", ", ", "", ", ", "bar"), parseItems("foo, , bar"));
            assertEquals(Arrays.asList("foo", ", ", "", " und ", "bar"), parseItems("foo,  und bar"));
        }

        @Test
        public void sentenceEndingWithStopWord() {
            assertEquals(Arrays.asList("foo", " and ", ""), parseItems("foo and "));

        }

        @Test
        public void sentenceStartingWithStopWord() {
            assertEquals(Arrays.asList("", ", ", "bar"), parseItems(", bar"));
        }

    }

    public class splitStopWords {

        @Test(expected = IllegalArgumentException.class)
        public void emptyList() {
            splitStopWords(Collections.emptyList());
        }

        @Test
        public void singleItemList() {
            assertEquals(Pair.of(
                    Collections.singletonList("hello"),
                    Collections.emptyList()),
                    splitStopWords(Collections.singletonList("hello")));
        }

        @Test
        public void multipleItemsList() {
            assertEquals(Pair.of(
                    Arrays.asList("foo", "bar", "hello", "world"),
                    Arrays.asList(" und ", ", ", " and ")),
                    splitStopWords(Arrays.asList("foo", " und ", "bar", ", ", "hello", " and ", "world")));
        }

        @Test(expected = IllegalArgumentException.class)
        public void invalidList() {
            splitStopWords(Arrays.asList("foo", " und ", ". ", "bar", "hello", " and "));
        }

    }

    public class rebuildSentence {

        @Test(expected = IllegalArgumentException.class)
        public void invalidWordsList() {
            rebuildSentence(Arrays.asList("a","n"), Arrays.asList("b", "b"));
        }

        @Test
        public void emptyWordsList() {
            assertEquals("", rebuildSentence(Collections.emptyList(), Collections.emptyList()));

        }

        @Test
        public void singleWordList() {
            assertEquals("", rebuildSentence(Collections.singletonList(""), Collections.emptyList()));
            assertEquals("foo bar", rebuildSentence(Collections.singletonList("foo bar"), Collections.emptyList()));
        }

        @Test
        public void multipleWordsList() {
            assertEquals("hello, world und foo", rebuildSentence(Arrays.asList("hello", "world", "foo"), Arrays.asList(", ", " und ")));
        }

    }

    public class TableTests {

        private final TestTuple[] sentences = {
                of("hello, world", "hello", "world"),
                of(",  und  and  ", "", "", "", " "),
                of("a and b und c", "a", "b", "c")
        };


        @Test
        public void tableTests() {
            for (TestTuple sentence : sentences) {
                test(sentence);
            }
        }


        private TestTuple of(String sentence, String... words) {
            return new TestTuple(sentence, Arrays.asList(words));
        }

        private void test(TestTuple test) {
            Pair<List<String>, List<String>> parsed = splitStopWords(parseItems(test.sentence));

            List<String> words = parsed.get1();

            assertEquals(test.words, words);
            assertEquals(test.sentence, parsed.apply(SentenceItemsParser::rebuildSentence));
        }

        private class TestTuple {
            private final String sentence;
            private final List<String> words;

            public TestTuple(String sentence, List<String> words) {
                this.sentence = sentence;
                this.words = words;
            }
        }
    }
}