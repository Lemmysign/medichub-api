package com.medichub.mapper;

import com.medichub.dto.response.AttemptResponse;
import com.medichub.dto.response.OptionResponse;
import com.medichub.dto.response.QuestionResponse;
import com.medichub.dto.response.StudentOptionResponse;
import com.medichub.dto.response.StudentQuestionResponse;
import com.medichub.dto.response.StudentTestResponse;
import com.medichub.dto.response.TestResponse;
import com.medichub.model.Question;
import com.medichub.model.QuestionOption;
import com.medichub.model.Test;
import com.medichub.model.TestAttempt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TestMapper {

    @Mapping(target = "courseId", source = "test.course.id")
    @Mapping(target = "questionCount", source = "questionCount")
    TestResponse toTestResponse(Test test, long questionCount);

    // --- instructor views (include correct answers) ---
    OptionResponse toOption(QuestionOption option);

    List<OptionResponse> toOptions(List<QuestionOption> options);

    QuestionResponse toQuestion(Question question);

    List<QuestionResponse> toQuestions(List<Question> questions);

    // --- student views (never expose correct) ---
    StudentOptionResponse toStudentOption(QuestionOption option);

    List<StudentOptionResponse> toStudentOptions(List<QuestionOption> options);

    StudentQuestionResponse toStudentQuestion(Question question);

    List<StudentQuestionResponse> toStudentQuestions(List<Question> questions);

    @Mapping(target = "courseId", source = "test.course.id")
    @Mapping(target = "questions", source = "questions")
    StudentTestResponse toStudentTest(Test test, List<Question> questions);

    // --- attempts ---
    @Mapping(target = "testId", source = "test.id")
    AttemptResponse toAttempt(TestAttempt attempt);
}
