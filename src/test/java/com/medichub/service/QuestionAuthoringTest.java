package com.medichub.service;

import com.medichub.dto.request.CreateOptionRequest;
import com.medichub.exception.BadRequestException;
import com.medichub.model.enums.QuestionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionAuthoringTest {

    private static CreateOptionRequest opt(String text, boolean correct) {
        return new CreateOptionRequest(text, correct);
    }

    @Test
    void resolveType_defaultsToMultipleChoiceWhenNull() {
        assertThat(QuestionAuthoring.resolveType(null)).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        assertThat(QuestionAuthoring.resolveType(QuestionType.TRUE_FALSE)).isEqualTo(QuestionType.TRUE_FALSE);
    }

    @Test
    void anyType_requiresAtLeastOneCorrect() {
        List<CreateOptionRequest> noneCorrect = List.of(opt("a", false), opt("b", false));
        assertThatThrownBy(() -> QuestionAuthoring.validate(QuestionType.MULTIPLE_CHOICE, noneCorrect))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least one correct");
    }

    @Test
    void singleChoice_rejectsMoreThanOneCorrect() {
        List<CreateOptionRequest> twoCorrect = List.of(opt("a", true), opt("b", true), opt("c", false));
        assertThatThrownBy(() -> QuestionAuthoring.validate(QuestionType.SINGLE_CHOICE, twoCorrect))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exactly one");
    }

    @Test
    void trueFalse_rejectsMoreThanOneCorrect() {
        List<CreateOptionRequest> twoCorrect = List.of(opt("True", true), opt("False", true));
        assertThatThrownBy(() -> QuestionAuthoring.validate(QuestionType.TRUE_FALSE, twoCorrect))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("true/false");
    }

    @Test
    void singleChoiceAndTrueFalse_acceptExactlyOneCorrect() {
        assertThatCode(() -> QuestionAuthoring.validate(
                QuestionType.SINGLE_CHOICE, List.of(opt("a", true), opt("b", false))))
                .doesNotThrowAnyException();
        assertThatCode(() -> QuestionAuthoring.validate(
                QuestionType.TRUE_FALSE, List.of(opt("True", true), opt("False", false))))
                .doesNotThrowAnyException();
    }

    @Test
    void multipleChoice_acceptsManyCorrect() {
        assertThatCode(() -> QuestionAuthoring.validate(
                QuestionType.MULTIPLE_CHOICE, List.of(opt("a", true), opt("b", true), opt("c", false))))
                .doesNotThrowAnyException();
    }
}
