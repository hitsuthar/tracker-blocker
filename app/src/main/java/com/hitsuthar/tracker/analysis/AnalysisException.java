package com.hitsuthar.tracker.analysis;
import androidx.annotation.Nullable;
public class AnalysisException extends Exception {
    String message;
    public AnalysisException(String message) {
        super(message);

        this.message = message;
    }
    @Nullable
    @Override
    public String getMessage() {
        return message;
    }
}
