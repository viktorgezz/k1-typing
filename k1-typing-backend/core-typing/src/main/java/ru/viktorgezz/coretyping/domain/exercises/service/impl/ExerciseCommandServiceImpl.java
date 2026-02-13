package ru.viktorgezz.coretyping.domain.exercises.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.viktorgezz.coretyping.domain.exercises.Exercise;
import ru.viktorgezz.coretyping.domain.exercises.repo.ExerciseRepo;
import ru.viktorgezz.coretyping.domain.exercises.dto.rq.CreationExerciseRqDto;
import ru.viktorgezz.coretyping.domain.exercises.dto.rq.UpdatedExerciseRqDto;
import ru.viktorgezz.coretyping.domain.exercises.service.intrf.ExerciseCommandService;
import ru.viktorgezz.coretyping.domain.user.User;
import ru.viktorgezz.coretyping.exception.BusinessException;
import ru.viktorgezz.coretyping.exception.ErrorCode;
import ru.viktorgezz.coretyping.security.util.CurrentUserUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
class ExerciseCommandServiceImpl implements ExerciseCommandService {

    private final ExerciseRepo exerciseRepo;

    @Override
    @Transactional
    public void create(CreationExerciseRqDto creationExerciseRqDto) {
        User userCurrent = CurrentUserUtils.getCurrentUser();
        exerciseRepo.save(
                new Exercise(
                        creationExerciseRqDto.title(),
                        creationExerciseRqDto.text(),
                        userCurrent,
                        creationExerciseRqDto.language()
                )
        );
    }

    @Override
    @Transactional
    public void update(UpdatedExerciseRqDto updatedExerciseRqDto) {
        Exercise exerciseFound = exerciseRepo
                .findById(updatedExerciseRqDto.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.EXERCISE_NOT_FOUND));

        exerciseFound.setTitle(updatedExerciseRqDto.title());
        exerciseFound.setText(updatedExerciseRqDto.text());

        exerciseRepo.save(exerciseFound);
    }

    @Override
    @Transactional
    public Exercise delete(Long id) {
        Exercise exercise = exerciseRepo.findById(id).orElse(null);
        if (exercise != null) {
            exerciseRepo.delete(exercise);
        }
        return exercise;
    }

    @Override
    @Transactional
    public void deleteMany(List<Long> ids) {
        exerciseRepo.deleteAllById(ids);
    }
}
