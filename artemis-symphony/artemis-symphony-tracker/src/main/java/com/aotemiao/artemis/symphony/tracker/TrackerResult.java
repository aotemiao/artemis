package com.aotemiao.artemis.symphony.tracker;

/** tracker 适配层统一返回值，便于在 Linear / memory 等实现之间复用。 */
public record TrackerResult<T>(boolean success, String errorCode, String errorMessage, T value) {

    public boolean isSuccess() {
        return success;
    }

    public static <T> TrackerResult<T> success(T value) {
        return new TrackerResult<>(true, null, null, value);
    }

    public static <T> TrackerResult<T> failure(String errorCode, String errorMessage) {
        return new TrackerResult<>(false, errorCode, errorMessage, null);
    }
}
