package com.nextrow.csvtojson.controller;

import com.mongodb.client.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.bson.Document;
import org.json.simple.JSONObject;

import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.HashMap;

@RestController
public class Controller {

    @GetMapping("/get")
    public void convertCSVToJSON() throws IOException, CsvValidationException {

        CSVReader csvReader;
        csvReader = new CSVReader(new FileReader("movies.csv"));

        String header[];
        header=csvReader.readNext();

        String data[];

        HashMap<String,Object> totalData=new HashMap<>();

        while ((data = csvReader.readNext()) != null) {
            HashMap<String,String> movieInfo =new HashMap<>();
            for (int i = 1; i < header.length; i++) {
                movieInfo.put(header[i], data[i]);
            }
            totalData.put(data[11], movieInfo);
        }

        JSONObject jsonObject=new JSONObject(totalData);

        try(FileWriter fileWriter = new FileWriter("Index.json")) {
            fileWriter.append(jsonObject.toJSONString());
        }
        catch (Exception e)
        {
            System.out.println("Exception");
        }

        MongoClient mongoClient= MongoClients.create();
        MongoDatabase mongoDatabase=mongoClient.getDatabase("movies-in-mongo");
        MongoCollection<Document> mongoCollection= mongoDatabase.getCollection("list-of-movies");

        for (Object o: totalData.keySet())
        {
            Document document=new Document();
            Object val=totalData.get(o);
            document.put(o.toString(),val);
            mongoCollection.insertOne(document);
        }

        System.out.println("Done");

    }

    @GetMapping("/getData/{title}")
    public Document getData(@PathVariable String title){

        MongoClient mongoClient= MongoClients.create();
        MongoDatabase mongoDatabase=mongoClient.getDatabase("movies-in-mongo");
        MongoCollection<Document> mongoCollection= mongoDatabase.getCollection("list-of-movies");
        System.out.println(title);

        for (Document value : mongoCollection.find()) {
            value.remove("_id");
            JSONObject jsonObject=new JSONObject(value);
            for (Object o: jsonObject.keySet())
            {
                String n=o.toString();

                if (n.equals(title))
                {
                    return value;
                }
            }
        }

        return null;
    }

    @PostMapping("/addMovie")
    public void addMovie(@RequestBody Document document){

        MongoClient mongoClient= MongoClients.create();
        MongoDatabase mongoDatabase=mongoClient.getDatabase("movies-in-mongo");
        MongoCollection<Document> mongoCollection= mongoDatabase.getCollection("list-of-movies");

        Document doc=new Document();
        doc.put(document.getString("title"),document);

        mongoCollection.insertOne(doc);

        System.out.println("Movie Added!");
    }

    @DeleteMapping("/deleteMovie/{title}")
    public void deleteMovie(@PathVariable String title){

        MongoClient mongoClient= MongoClients.create();
        MongoDatabase mongoDatabase=mongoClient.getDatabase("movies-in-mongo");
        MongoCollection<Document> mongoCollection= mongoDatabase.getCollection("list-of-movies");

        for (Document value : mongoCollection.find()) {
            value.remove("_id");
            JSONObject jsonObject=new JSONObject(value);
            for (Object o: jsonObject.keySet())
            {
                String n=o.toString();

                if (n.equals(title))
                {
                    mongoCollection.deleteOne(value);
                }
            }

        }

        System.out.println("Movie Deleted!");
    }

}