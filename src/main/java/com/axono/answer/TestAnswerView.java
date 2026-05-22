package com.axono.answer;

import com.axono.questionmodel.Question;
import java.util.List;

/**
 * Test data provider used by {@link ViewAnswerView}. Builds a fixed
 * list of {@link Question} instances exercising assorted answer
 * counts, selection outcomes (correct, incorrect, all-correct
 * unchosen) and the two error cases handled by {@code Question}
 * (blank question text and empty answer list).
 */
public final class TestAnswerView {

        /**
         * Index 3 — extracted as a named constant to satisfy
         * Checkstyle's MagicNumber check on test data.
         */
        private static final int IDX_3 = 3;

        /** Index 4 — see {@link #IDX_3}. */
        private static final int IDX_4 = 4;

        /** Index 5 — see {@link #IDX_3}. */
        private static final int IDX_5 = 5;

        /** Canned test questions covering normal and edge cases. */
        private final List<Question> questions = List.of(

                        // Different answer counts, varied outcomes.
                        // Q-prefix and correct/incorrect rendering in UI verified
                        // against this fixture.

                        new Question(
                                        "What is the answer to this question?",
                                        List.of("tttt", "gfff", "dddd", "sss"),
                                        0, 1),

                        new Question(
                                        "What is this one?",
                                        List.of("www", "fff", "dddd"),
                                        2, 2),

                        new Question(
                                        "What about this one?",
                                        List.of("ffhjdafk", "fgdhfgh",
                                                        "fhjfakbakvb", "ahvbhjb"),
                                        IDX_3, 2),

                        new Question(
                                        "This ggdfgone?",
                                        List.of("fhdaddufh", "fhudgdfaf"),
                                        1, 1),

                        new Question(
                                        "What is???",
                                        List.of("ffff", "wwwwwwwwwwwwwwwwww", "dddddd",
                                                        "qqqqq", "errdereses", "eee"),
                                        IDX_4, IDX_5),

                        new Question(
                                        "What is thgfis?",
                                        List.of("dhdfjhffdgfj", "hdgrfghuighdui",
                                                        "hfdfgffh", "dghggjd"),
                                        2, 2),

                        new Question(
                                        "This gfdone?",
                                        List.of("fhdaudgdffh", "fhugdfdaf"),
                                        1, 1),

                        new Question(
                                        "What is this?",
                                        List.of("dhdfjhgfj", "hduighdui",
                                                        "hfdfh", "dghjd"),
                                        IDX_3, 2),

                        // Exception cases below.

                        // Blank question text — fallback prompt should render.
                        new Question("",
                                        List.of("fhdaufh", "fhudaf"),
                                        1, 0),

                        // Empty answer list — fallback row should render.
                        new Question("ERROR TEST",
                                        List.of(),
                                        1, 1));

        /**
         * Returns the canned list of test questions.
         *
         * @return the fixed list of {@link Question} instances
         */
        public List<Question> getQuestions() {
                return questions;
        }
}