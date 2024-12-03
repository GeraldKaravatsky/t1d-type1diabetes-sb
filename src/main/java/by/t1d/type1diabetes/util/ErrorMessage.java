package by.t1d.type1diabetes.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorMessage {

    INCORRECT_CARBS("Вес поглощенной углеводной пищи должен быть от 0 до 551 грамм."),

    INCORRECT_DURATION("Продолжительность приема пищи должна быть от 0 до 60 минут."),


    ;

    @Getter
    private final String message;

}
