package com.githubtimemachine.exception;

public class GitAnalysisException extends RuntimeException {

    public GitAnalysisException(String message) {
        super(message);
    }

    public GitAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
