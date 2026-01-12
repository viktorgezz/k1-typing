package ru.viktorgezz.k1_typing_backend.domain.contest;

/**
 * CREATED → (все присоединились) → WAITING → (отсчёт закончился) → PROGRESS → (все финишировали) → FINISHED
 * */
public enum Status {

    CREATED, WAITING, FINISHED, PROGRESS
}
