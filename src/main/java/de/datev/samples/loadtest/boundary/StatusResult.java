package de.datev.samples.loadtest.boundary;

public class StatusResult<T> {

    int status;
    T result;

    public StatusResult() {
    }

    public StatusResult(int status) {
        this.status = status;
    }

    public StatusResult(int status, T result) {
        this.status = status;
        this.result = result;
    }

    public int getStatus() {
        return status;
    }

    public T getResult() {
        return result;
    }

}
