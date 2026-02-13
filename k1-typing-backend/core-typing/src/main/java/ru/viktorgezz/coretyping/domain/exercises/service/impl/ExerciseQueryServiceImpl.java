package ru.viktorgezz.coretyping.domain.exercises.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.viktorgezz.coretyping.domain.exercises.Exercise;
import ru.viktorgezz.coretyping.domain.exercises.dto.rs.ExerciseListItemRsDto;
import ru.viktorgezz.coretyping.domain.exercises.repo.ExercisePagingAndSortingRepo;
import ru.viktorgezz.coretyping.domain.exercises.repo.ExerciseRepo;
import ru.viktorgezz.coretyping.domain.exercises.service.intrf.ExerciseQueryService;
import ru.viktorgezz.coretyping.exception.BusinessException;
import ru.viktorgezz.coretyping.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class ExerciseQueryServiceImpl implements ExerciseQueryService {

    private final ExercisePagingAndSortingRepo exercisePagingAndSortingRepo;

    private final ExerciseRepo exerciseRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<ExerciseListItemRsDto> findAll(Pageable pageable) {
        return exercisePagingAndSortingRepo.findAll(pageable)
                .map(exercise -> new ExerciseListItemRsDto(
                        exercise.getId(),
                        exercise.getTitle(),
                        exercise.getUser().getUsername(),
                        exercise.getLanguage()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Exercise getOne(Long id) {
        return exerciseRepo.findById(id).orElseThrow(() ->
                new BusinessException(ErrorCode.EXERCISE_NOT_FOUND));
    }
}
