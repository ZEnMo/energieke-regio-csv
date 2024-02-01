package com.zenmo;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: <executable> <energieke-regio-api-key>");
            System.exit(1);
        }

        var energiekeRegioApiKey = args[0];

        System.out.println("Starting");

        var request = new Request.Builder()
                .header("Authorization", "Bearer " + energiekeRegioApiKey)
                .url("https://energiekeregio.nl/api/v1/zenmo/")
                .header("Accept", "application/json")
                .build();

        ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();

        try {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            var client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
            var submissionWalker = new SubmissionCsvCreator(client, energiekeRegioApiKey);

            var response = client.newCall(request).execute();

            JsonReader jsonReader = Json.createReader(new StringReader(response.body().string()));
            var submissions = jsonReader.readArray();
            for (var submission: submissions) {
                var objSubmission = (JsonObject) submission;
                var link = objSubmission.getString("Link");

                result.add(submissionWalker.getSubmission(link));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Here we are calling the allKeys() method with our result list, and storing it in a Set<String> object.
        Set<String> allKeysSet = allKeys(result);

        System.out.println("Resulting key set: " + allKeysSet);

        var allKeysList = new ArrayList<>(allKeysSet);
        try (CSVPrinter printer = new CSVPrinter(new FileWriter("output.csv"), CSVFormat.EXCEL))
        {
            printer.printRecord(allKeysList);
            for (var submission : result) {
                var record = new ArrayList<String>();
                for (var key : allKeysList) {
                    record.add(submission.getOrDefault(key, ""));
                }
                printer.printRecord(record);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("done");
    }

    public static Set<String> allKeys(ArrayList<Map<String, String>> allData) {
        Set<String> allKeys = new LinkedHashSet<String>();
        for (Map<String, String> data : allData) {
            allKeys.addAll(data.keySet());
        }
        return allKeys;
    }
}