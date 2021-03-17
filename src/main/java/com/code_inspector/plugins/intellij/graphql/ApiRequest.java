package com.code_inspector.plugins.intellij.graphql;

import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * An object to encapsulate an API response object. The goal is to be able
 * to wait for the object to be ready with a semaphore. As the GraphQL
 * requests are done in the context of another thread, the main thread
 * has to wait for the response. We implement this using a semaphore.
 *
 * @param <T> - the data type we expect from the API.
 */
public class ApiRequest<T> {
    Semaphore semaphore;
    Optional<T> data;

    public ApiRequest() {
        semaphore = new Semaphore(0);
        data = Optional.empty();
    }

    public void setData(T d) {
        this.data = Optional.of(d);
        semaphore.release();
    }

    public void setError() {
        semaphore.release();
    }

    public Optional<T> getData() {
        try {
            semaphore.acquire();
            return this.data;
        } catch (InterruptedException ie) {
            return Optional.empty();
        }
    }

}
