package com.example.word.utils;

import java.time.LocalDate;

public class EbbinghausUtil {
    // 复习间隔：1天, 1天, 2天, 4天, 7天, 15天
    private static final int[] INTERVALS = {1, 1, 2, 4, 7, 15};

    public static LocalDate calculateNextReviewDate(int currentStage) {
        if (currentStage >= INTERVALS.length) {
            return LocalDate.now().plusDays(30); // 已掌握，丢到很久以后
        }
        return LocalDate.now().plusDays(INTERVALS[currentStage]);
    }

    public static boolean isMastered(int nextStage) {
        return nextStage >= INTERVALS.length;
    }
}