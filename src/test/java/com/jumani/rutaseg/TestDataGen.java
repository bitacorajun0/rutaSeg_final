package com.jumani.rutaseg;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class TestDataGen {

    private static final Random RANDOM = new Random();

    private TestDataGen() {

    }

    public static long randomId() {
        return RANDOM.nextLong(1L, 99999999L);
    }

    public static String randomShortString() {
        return randomString(10);
    }

    public static String randomString(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    public static boolean randomBoolean() {
        return pickRandomElement(List.of(true, false));
    }

    public static <T extends Enum<T>> T randomEnum(Class<T> enumClass) {
        final T[] values = enumClass.getEnumConstants();
        return pickRandomElement(Arrays.asList(values));
    }

    public static <T> T pickRandomElement(List<T> list) {
        final int randomPosition = randomInt(0, list.size());
        return list.get(randomPosition);
    }

    public static int randomPositiveInt(int bound) {
        return randomInt(1, bound);
    }

    public static int randomInt(int origin, int bound) {
        return RANDOM.nextInt(origin, bound);
    }
}
