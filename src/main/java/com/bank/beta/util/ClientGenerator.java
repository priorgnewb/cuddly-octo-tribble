package com.bank.beta.util;

import com.bank.beta.entity.Client;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

@Component
public class ClientGenerator {

    private static final String[] FIRST_NAMES = {"IVAN", "PETR", "SERGEY", "ANDREY", "MIKHAIL", "ALEXEY", "DMITRY", "NIKOLAY"};
    private static final String[] LAST_NAMES = {"Surname", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};
    private static final String[] MIDDLE_NAMES = {"ALEXANDROVICH", "PETROVICH", "SERGEYEVICH", "ANDREEVICH", "MIKHAILOVICH"};
    private static final String[] EMAIL_DOMAINS = {"gmail.com", "yahoo.com", "hotmail.com", "mail.ru", "yandex.ru"};

    private static final Random random = new SecureRandom();

    public Client generateRandomClient() {
        Client client = new Client();

        // Генерируем имя в формате "SurnameABCDEFGH"
        String randomSuffix = generateRandomString(8);
        String fullName = "Surname" + randomSuffix;
        client.setFullName(fullName);

        // Генерируем паспорт: 2 заглавные буквы + 7 цифр
        client.setPassportNumber(generatePassportNumber());

        // Генерируем телефон: +7XXXXXXXXXX
        client.setPhoneNumber(generatePhoneNumber());

        // Генерируем email на основе имени
        client.setEmail(generateEmail(fullName));

        return client;
    }

    public Client generateReadableClient() {
        Client client = new Client();

        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String middleName = MIDDLE_NAMES[random.nextInt(MIDDLE_NAMES.length)];

        // Формируем ФИО
        String fullName = lastName + " " + firstName + " " + middleName;
        client.setFullName(fullName);

        // Паспорт
        client.setPassportNumber(generatePassportNumber());

        // Телефон
        client.setPhoneNumber(generatePhoneNumber());

        // Email
        client.setEmail(generateEmail(firstName + "." + lastName));

        return client;
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generatePassportNumber() {
        // Формат: 2 буквы + 7 цифр (например, AB1234567)
        String letters = generateRandomString(2);
        int numbers = 1000000 + random.nextInt(9000000); // 7 цифр
        return letters + numbers;
    }

    private String generatePhoneNumber() {
        // +7XXXXXXXXXX
        long number = 9000000000L + random.nextInt(1000000000);
        return "+7" + number;
    }

    private String generateEmail(String base) {
        String domain = EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];
        String cleanBase = base.replace(" ", ".").toLowerCase();
        return cleanBase + "@" + domain;
    }
}