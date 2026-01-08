package ru.viktorgezz.k1_typing_backend.domain.exercises.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.k1_typing_backend.domain.exercises.Exercise;
import ru.viktorgezz.k1_typing_backend.domain.exercises.dto.rs.ExerciseListItemRsDto;
import ru.viktorgezz.k1_typing_backend.domain.exercises.repo.ExercisePagingAndSortingRepo;
import ru.viktorgezz.k1_typing_backend.domain.exercises.repo.ExerciseRepo;
import ru.viktorgezz.k1_typing_backend.domain.exercises.service.intrf.ExerciseQueryService;
import ru.viktorgezz.k1_typing_backend.exception.BusinessException;
import ru.viktorgezz.k1_typing_backend.exception.ErrorCode;

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
                        exercise.getUser().getUsername()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Exercise getOne(Long id) {
        return exerciseRepo.findById(id).orElseThrow(() ->
                new BusinessException(ErrorCode.EXERCISE_NOT_FOUND));
    }
}
