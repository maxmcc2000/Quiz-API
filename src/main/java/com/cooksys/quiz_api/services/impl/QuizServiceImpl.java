package com.cooksys.quiz_api.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.cooksys.quiz_api.dtos.QuestionRequestDto;
import com.cooksys.quiz_api.dtos.QuestionResponseDto;
import com.cooksys.quiz_api.dtos.QuizRequestDto;
import com.cooksys.quiz_api.dtos.QuizResponseDto;
import com.cooksys.quiz_api.entities.Answer;
import com.cooksys.quiz_api.entities.Question;
import com.cooksys.quiz_api.entities.Quiz;
import com.cooksys.quiz_api.mappers.QuestionMapper;
import com.cooksys.quiz_api.mappers.QuizMapper;
import com.cooksys.quiz_api.repositories.QuestionRepository;
import com.cooksys.quiz_api.repositories.QuizRepository;
import com.cooksys.quiz_api.services.QuizService;

import javassist.NotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import javax.swing.text.html.Option;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

  private final QuizRepository quizRepository;
  private final QuestionRepository questionRepository;

  private final QuizMapper quizMapper;
  private final QuestionMapper questionMapper;

  private Quiz getQuiz(Long id) throws NotFoundException {
    Optional<Quiz> optionalQuiz = quizRepository.findById(id);

    if (optionalQuiz.isEmpty()) {
      throw new NotFoundException("No Quiz found with id " + id);
    }
    return optionalQuiz.get();
  }

  private Question getQuizQuestion(Long id, Long questionID) throws NotFoundException {
    Optional<Question> optionalQuizQuestion = questionRepository.findByQuizIdAndId(id, questionID);

    if (optionalQuizQuestion.isEmpty()) {
      throw new NotFoundException("No Question found with id " + questionID + " in Quiz with id " + id);
    }
    return optionalQuizQuestion.get();
  }

  /**
   * Sets the quiz reference for each question and the question reference for each answer.
   *
   * This method iterates through each question in the provided quiz, setting the quiz reference
   * for each question. It also iterates through each answer of a question, setting the question
   * reference for each answer. This ensures that all questions and answers have the correct
   * references set before the quiz is saved to the database.
   *
   * @param quiz the Quiz entity containing questions and answers, with their references initially set to null
   * @return the Quiz entity with updated question and answer references
   */
  private Quiz setQuizAndQuestionReferences(Quiz quiz) {
    for (Question q : quiz.getQuestions()) {
      q.setQuiz(quiz);

      for (Answer a : q.getAnswers()) {
        a.setQuestion(q);
      }
    }
    return quiz;
  }

  @Override
  public List<QuizResponseDto> getAllQuizzes() {
    return quizMapper.entitiesToDtos(quizRepository.findAll());
  }

  @Override
  public QuizResponseDto createQuiz(QuizRequestDto quizRequestDto) {

    Quiz quizToBeSaved = quizMapper.requestDtoToEntity(quizRequestDto);

    quizToBeSaved = setQuizAndQuestionReferences(quizToBeSaved);

    Quiz savedQuiz = quizRepository.saveAndFlush(quizToBeSaved);
    return quizMapper.entityToResponseDto(savedQuiz);

  }

  @Override
  public QuizResponseDto deleteQuiz(Long id) throws NotFoundException {
    Quiz quizToDelete = getQuiz(id);
    quizRepository.delete(quizToDelete);
    return quizMapper.entityToResponseDto(quizToDelete);
  }

  @Override
  public QuestionResponseDto deleteQuestion(Long id, Long questionID) throws NotFoundException {
      Question questionToDelete = getQuizQuestion(id, questionID);
      questionRepository.delete(questionToDelete);
      return questionMapper.entityToDto(questionToDelete);
  }

  @Override
  public QuestionResponseDto getRandomQuestion(Long id) throws NotFoundException {
    Quiz quiz = getQuiz(id);
    List<Question> questions = quiz.getQuestions();
    Random random = new Random();
    int randomIndex = random.nextInt(questions.size());
    return questionMapper.entityToDto(questions.get(randomIndex));
  }

  @Override
  public QuizResponseDto changeQuizName(Long id, String newName) throws NotFoundException {
    Quiz quizToUpdate = getQuiz(id);
    quizToUpdate.setName(newName);
    return quizMapper.entityToResponseDto(quizRepository.saveAndFlush(quizToUpdate));
  }

  @Override
  public QuizResponseDto addQuestionToQuiz(Long id, QuestionRequestDto questionRequestDto) throws NotFoundException {
    Quiz quizToUpdate = getQuiz(id);
    List<Question> questions = quizToUpdate.getQuestions();
    questions.add(questionMapper.questionRequestDtoToEntity(questionRequestDto));

    quizToUpdate = setQuizAndQuestionReferences(quizToUpdate);
    return quizMapper.entityToResponseDto(quizRepository.saveAndFlush(quizToUpdate));
  }
}
