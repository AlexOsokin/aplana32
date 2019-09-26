package com.company;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fabric.Company;
import fabric.Securities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class Main {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final static ArrayList<DateTimeFormatter> formats = new ArrayList<>(Arrays.asList(DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yy"),  DateTimeFormatter.ofPattern("dd/MM/yyyy"),  DateTimeFormatter.ofPattern("dd/MM/yy")));

    public static void main(String[] args) throws FileNotFoundException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader("test.json"));
        ArrayList<Company> compList = gson.fromJson(bufferedReader, new TypeToken<ArrayList<Company>>() {
        }.getType());
        getNameAndDate(compList);
        oldAksionet(compList);
        askUser(compList);

    }
    private static void getNameAndDate(ArrayList<Company> compList){
        Function<String, LocalDate> toLocalDate = LocalDate::parse;
        compList.forEach(c -> System.out.println(c.getId() + " Краткое название: " + c.getName_short() + ". Дата основания: " +
                toLocalDate.apply(c.getEgrul_date()).format(formats.get(3))));

    }
    private static void oldAksionet(ArrayList<Company> compList){
        System.out.println("Ценные бумаги, просроченные на сегодняшний день:");
        Function<String, LocalDate> toLocalDate = LocalDate::parse;
        LocalDate date = LocalDate.now();
        long count = 0;
        for (int i = 0; i < compList.size(); i++){
            final int finalI = i;
            List<Securities> securitiesStreamList = compList.get(i).getSecurities().stream().filter(s -> ChronoUnit.DAYS.between(date,toLocalDate
                    .apply(s.getDate_to())) < 0).collect(Collectors.toList());
            securitiesStreamList.forEach(s->System.out.println("Code: " + s.getCode() + "; Дата истечения: " + toLocalDate.apply(s.getDate_to())
                            .format(formats.get(3)) + "; полное название организации-владельца: " +  compList.get(finalI).getName_full()));
           count += securitiesStreamList.size();
        }
        System.out.println("Количество просроченных бумаг: " + count);
    }
    private static void askUser(ArrayList<Company> compList){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите дату (в формате \"ДД.ММ.ГГГГ\", \"ДД.ММ.ГГ\", \"ДД/ММ/ГГГГ\" или \"ДД/ММ/ГГ\") или код валюты (например: EU, USD, RUB, CNY). " +
                "Для завершения рабоы программы введите \"STOP\": ");
        String scanString = scanner.nextLine();
        if (!scanString.toUpperCase().equals("STOP")){
            if (scanString.contains(".")||scanString.contains("/")){
                getForDate(scanString, compList);
            }
            else {
                getForCode(scanString, compList);
            }
        }
    }

    private static void getForDate(String string, ArrayList<Company> compList) {
        LocalDate date = null;
        for (DateTimeFormatter format : formats){
            try {
                date = LocalDate.parse(string, format);
            }
            catch (DateTimeParseException e){
                System.out.println("Определение формата... (" + e.getMessage() +")");
            }
        }
        if (date == null){
            System.out.println("Неправильно введенная дата!");
            askUser(compList);
            return;
        }
        System.out.println("Информация об организациях, основанных после введенной даты:");
        Function<String, LocalDate> toLocalDate = LocalDate::parse;
        LocalDate finalDate = date;
        compList.stream().filter(c -> ChronoUnit.DAYS.between(finalDate,toLocalDate.apply(c.getEgrul_date())) > 0).forEach(c -> System.out.println(c.getId() +
                " Краткое название: " + c.getName_short() + ". Дата основания: " + toLocalDate.apply(c.getEgrul_date()).format(formats.get(3))));
        askUser(compList);

    }

    private static void getForCode(String string, ArrayList<Company> compList) {
        System.out.println("Информация о ценных бумагах, использующих данную валюту:");
        string = string.toUpperCase();
        long count = 0;
        for (Company company : compList) {
            String finalString = string;
            List<Securities>  securitiesStreamList = company.getSecurities().stream().filter(c -> finalString.equals(c.getCurrency().getCode())).collect(Collectors.toList());
            securitiesStreamList.forEach(c -> System.out.println("id: " + c.getId() + "; code: " + c.getCode()));
            count +=securitiesStreamList.size();
        }
        if (count == 0){
            System.out.println("Такой валюты нет, введите запрос еще раз");
        }
        askUser(compList);
    }

}
