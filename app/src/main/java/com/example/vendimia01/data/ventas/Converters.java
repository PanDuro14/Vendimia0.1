package com.example.vendimia01.data.ventas;

import android.text.TextUtils;

import androidx.room.TypeConverter;
import java.util.Arrays;
import java.util.List;

public class Converters {
    @TypeConverter
    public static List<String> fromString(String value){
        return Arrays.asList(value.split(","));
    }

    @TypeConverter
    public static String fromList(List<String> list){
        return TextUtils.join(",", list);
    }
}
