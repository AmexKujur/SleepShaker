package com.example.sleepshaker;

import androidx.room.TypeConverter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class DaysSetConverter {
    @TypeConverter
    public String fromDaySet(Set<Integer> days) {
        if (days == null) {
            return null;
        }
        return days.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @TypeConverter
    public Set<Integer> toDaySet(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        return Arrays.stream(data.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }
}