package com.medichub.service;

import com.medichub.model.Question;
import com.medichub.model.QuestionOption;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TestGraderTest {

    // --- helpers -------------------------------------------------------------

    private static QuestionOption option(long id, String text, boolean correct) {
        QuestionOption o = new QuestionOption();
        o.setId(id);
        o.setText(text);
        o.setCorrect(correct);
        return o;
    }

    private static Question question(long id, QuestionOption... options) {
        Question q = new Question();
        q.setId(id);
        for (QuestionOption o : options) {
            q.getOptions().add(o);
        }
        return q;
    }

    // Model Test is fully-qualified to avoid clashing with JUnit's @Test.
    private static com.medichub.model.Test test(int passMark) {
        com.medichub.model.Test t = new com.medichub.model.Test();
        t.setPassMarkPercent(passMark);
        return t;
    }

    /** Four questions, each with a correct option (id = 10*q) and a wrong option (id = 10*q + 1). */
    private static List<Question> fourQuestions() {
        return List.of(
                question(1, option(10, "c", true), option(11, "w", false)),
                question(2, option(20, "c", true), option(21, "w", false)),
                question(3, option(30, "c", true), option(31, "w", false)),
                question(4, option(40, "c", true), option(41, "w", false)));
    }

    // --- tests ---------------------------------------------------------------

    @Test
    void allCorrect_scores100_andPasses() {
        Map<Long, List<Long>> selected = Map.of(1L, List.of(10L), 2L, List.of(20L), 3L, List.of(30L), 4L, List.of(40L));

        TestGrader.Result result = TestGrader.grade(test(50), fourQuestions(), selected);

        assertThat(result.scorePercent()).isEqualTo(100);
        assertThat(result.passed()).isTrue();
        assertThat(result.outcomes()).allMatch(TestGrader.QuestionOutcome::correct);
    }

    @Test
    void noneCorrect_scores0_andFails() {
        Map<Long, List<Long>> selected = Map.of(1L, List.of(11L), 2L, List.of(21L), 3L, List.of(31L), 4L, List.of(41L));

        TestGrader.Result result = TestGrader.grade(test(50), fourQuestions(), selected);

        assertThat(result.scorePercent()).isZero();
        assertThat(result.passed()).isFalse();
    }

    @Test
    void partial_twoOfFour_is50() {
        Map<Long, List<Long>> selected = Map.of(1L, List.of(10L), 2L, List.of(20L), 3L, List.of(31L), 4L, List.of(41L));

        TestGrader.Result result = TestGrader.grade(test(50), fourQuestions(), selected);

        assertThat(result.scorePercent()).isEqualTo(50);
    }

    @Test
    void passBoundary_scoreEqualToPassMarkPasses() {
        Map<Long, List<Long>> selected = Map.of(1L, List.of(10L), 2L, List.of(20L), 3L, List.of(31L), 4L, List.of(41L)); // 50%

        assertThat(TestGrader.grade(test(50), fourQuestions(), selected).passed()).isTrue();
        assertThat(TestGrader.grade(test(51), fourQuestions(), selected).passed()).isFalse();
    }

    @Test
    void unansweredQuestion_countsWrong() {
        Map<Long, List<Long>> selected = new HashMap<>();
        selected.put(1L, List.of(10L));
        selected.put(2L, List.of(20L));
        selected.put(3L, List.of(30L));
        // question 4 unanswered

        TestGrader.Result result = TestGrader.grade(test(50), fourQuestions(), selected);

        assertThat(result.scorePercent()).isEqualTo(75);
        assertThat(result.outcomes()).filteredOn(o -> o.questionId().equals(4L))
                .singleElement()
                .matches(o -> !o.correct() && o.selectedOptionIds().isEmpty());
    }

    @Test
    void optionFromAnotherQuestion_countsWrong() {
        // For question 1, submit an option id that belongs to question 2.
        Map<Long, List<Long>> selected = Map.of(1L, List.of(20L), 2L, List.of(20L), 3L, List.of(30L), 4L, List.of(40L));

        TestGrader.Result result = TestGrader.grade(test(50), fourQuestions(), selected);

        assertThat(result.scorePercent()).isEqualTo(75); // q1 wrong, others correct
        assertThat(result.outcomes()).filteredOn(o -> o.questionId().equals(1L))
                .singleElement().matches(o -> !o.correct());
    }

    @Test
    void emptyTest_scores0_andFails() {
        TestGrader.Result result = TestGrader.grade(test(0), List.of(), Map.of());

        assertThat(result.scorePercent()).isZero();
        assertThat(result.passed()).isFalse();
    }

    // --- multiple-choice (multiple correct) ----------------------------------

    /** One question with TWO correct options (10, 11) and one wrong (12). */
    private static List<Question> multiSelectQuestion() {
        return List.of(question(1, option(10, "a", true), option(11, "b", true), option(12, "c", false)));
    }

    @Test
    void multiSelect_allCorrectSelected_isCorrect() {
        Map<Long, List<Long>> selected = Map.of(1L, List.of(10L, 11L));
        TestGrader.Result result = TestGrader.grade(test(50), multiSelectQuestion(), selected);
        assertThat(result.scorePercent()).isEqualTo(100);
        assertThat(result.outcomes()).singleElement().matches(TestGrader.QuestionOutcome::correct);
    }

    @Test
    void multiSelect_missingOneCorrect_isWrong() {
        Map<Long, List<Long>> selected = Map.of(1L, List.of(10L)); // only one of two correct
        assertThat(TestGrader.grade(test(50), multiSelectQuestion(), selected).outcomes())
                .singleElement().matches(o -> !o.correct());
    }

    @Test
    void multiSelect_includesWrongOption_isWrong() {
        Map<Long, List<Long>> selected = Map.of(1L, List.of(10L, 11L, 12L)); // both correct + a wrong one
        assertThat(TestGrader.grade(test(50), multiSelectQuestion(), selected).outcomes())
                .singleElement().matches(o -> !o.correct());
    }
}
