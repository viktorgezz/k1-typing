package ru.viktorgezz.coretyping.domain.contest;

/**
 * CREATED → (все присоединились) → WAITING → (отсчёт закончился) → PROGRESS → (все финишировали) → FINISHED
 * */
public enum Status {

    CREATED, WAITING, FINISHED, PROGRESS
}
