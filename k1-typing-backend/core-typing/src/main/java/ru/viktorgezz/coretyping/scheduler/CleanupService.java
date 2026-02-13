package ru.viktorgezz.coretyping.scheduler;

@FunctionalInterface
public interface CleanupService {

    long purgeData();
}
