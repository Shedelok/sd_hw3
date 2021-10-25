package ru.akirakozov.sd.refactoring.util;

public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T connection) throws E;

}
