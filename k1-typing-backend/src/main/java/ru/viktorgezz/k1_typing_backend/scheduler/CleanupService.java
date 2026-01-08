package ru.viktorgezz.k1_typing_backend.scheduler;

@FunctionalInterface
public interface CleanupService {

    long purgeData();
}
