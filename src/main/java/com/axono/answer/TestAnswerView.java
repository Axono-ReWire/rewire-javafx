package com.axono.answer;

import com.axono.questionmodel.Question;
import java.util.List;

public class TestAnswerView {

        private List<Question> questions = List.of(

                        // test for different amount of possible answers
                        // show up correct for correct/incorrect indication as expected
                        // Q1, Q2 etc shows up as expected in front of each question

                        new Question("What is the answer to this question?", List.of("tttt", "gfff", "dddd", "sss"),
                                        0, 1),

                        new Question("What is this one?", List.of("www", "fff", "dddd"), 2, 2),

                        new Question("What about this one?", List.of("ffhjdafk", "fgdhfgh", "fhjfakbakvb", "ahvbhjb"),
                                        3, 2),

                        new Question("This ggdfgone?", List.of("fhdaddufh", "fhudgdfaf"), 1, 1),

                        new Question("What is???",
                                        List.of("ffff", "wwwwwwwwwwwwwwwwww", "dddddd", "qqqqq", "errdereses", "eee"),
                                        4, 5),
                        new Question("What is thgfis?",
                                        List.of("dhdfjhffdgfj", "hdgrfghuighdui", "hfdfgffh", "dghggjd"), 2, 2),

                        new Question("This gfdone?", List.of("fhdaudgdffh", "fhugdfdaf"), 1, 1),

                        new Question("What is this?", List.of("dhdfjhgfj", "hduighdui", "hfdfh", "dghjd"), 3, 2),

                        // BELOW IS TEST FOR EXCEPTIONS //

                        // question blank - as expected view in UI
                        new Question("", List.of("fhdaufh", "fhudaf"), 1, 0),

                        // NO ANSWERS PASSED THROUGH, list empty- as expected view in UI
                        new Question("ERROR TEST", List.of(),
                                        1, 1)

        );

        public List<Question> getQuestions() {
                return questions;
        }

}
