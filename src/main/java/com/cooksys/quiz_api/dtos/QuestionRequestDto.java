package com.cooksys.quiz_api.dtos;

import com.cooksys.quiz_api.entities.Answer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class QuestionRequestDto {

    private String text;

    private List<Answer> answers;
}
