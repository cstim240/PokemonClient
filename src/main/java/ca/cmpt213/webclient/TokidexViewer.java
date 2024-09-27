/*
 * @author Tim Supan
 * @version 1.0
 * Course: CMPT 213 D100
 * Assignment 5 - JavaFX Application
 */

package ca.cmpt213.webclient;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.Gson;

public class TokidexViewer extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        List<TokimonCard> tokimonCards = fetchAllTokimonCards();
        AtomicInteger currentIndex = new AtomicInteger(0);

        //mainContainer will contain the tokiInfo, and a button menu
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(10, 10, 10, 10));
        Image backgroundImage = new Image("file:src/main/resources/pattern2.png");

        //Create BackgroundImage obj
        BackgroundImage background = new BackgroundImage(backgroundImage, null, null, null, new BackgroundSize(BackgroundSize.DEFAULT.getWidth(), BackgroundSize.DEFAULT.getHeight(), true, true, false, true));
        mainContainer.setBackground(new Background(background));
        mainContainer.setAlignment(Pos.CENTER);

        // tokiInfo will contain two parts: the photo and the info of the tokimon
        HBox tokiInfo = new HBox(50);
        tokiInfo.setAlignment(Pos.CENTER);

        // photoContainer to hold the imageview of the tokimon
        VBox photoContainer = new VBox();
        photoContainer.setAlignment(Pos.CENTER_LEFT);
        photoContainer.getStyleClass().add("photoContainer");

        String imageURL = "http://localhost:8080/images/" + tokimonCards.get(currentIndex.get()).getImageName();
        Image tokiImage = new Image(imageURL);

        ImageView tokiImageView = new ImageView(tokiImage);
        tokiImageView.setFitHeight(250);
        tokiImageView.setFitWidth(200);
        tokiImageView.setPreserveRatio(true);

        photoContainer.getChildren().add(tokiImageView);

        tokiInfo.getChildren().add(photoContainer);

        // info will contain the info of the tokimon
        VBox info = new VBox(10);
        info.setAlignment(Pos.CENTER);
        info.setPrefSize(200, 250);
        Label tokiName = new Label("Name: ");
        tokiName.getStyleClass().add("tokimonName");

        VBox moreInfo = new VBox(5);
        Label tokiElement = new Label("Element: ");
        Label tokiId = new Label("ID: ");
        Label rarity = new Label("Rarity: ");
        Label healthPoints = new Label("HP: ");
        Label attackPoints = new Label("AP: ");
        moreInfo.getChildren().addAll(tokiElement, tokiId, rarity, healthPoints, attackPoints);
        moreInfo.setVisible(false); // initially hide the labels
        moreInfo.getStyleClass().add("moreInfo");

        //Create a ComboBox for the dropdown menu
        Button revealInfoBtn = new Button("Reveal Info");

        revealInfoBtn.setOnAction(event -> {
            if ("Reveal Info".equals(revealInfoBtn.getText())){
                revealInfoBtn.setText("Hide Info");
                moreInfo.setVisible(true);
            } else {
                revealInfoBtn.setText("Reveal Info");
                moreInfo.setVisible(false);
            }
        });

        info.getChildren().addAll(tokiName, revealInfoBtn, moreInfo);
        tokiInfo.getChildren().add(info);

        // Create button menu for the user to interact with the program, contains 2 parts: navigation buttons and search bar
        HBox buttonMenu = new HBox(70);
        buttonMenu.setAlignment(Pos.CENTER);

        //Initial load of first TokimonCard
        if (!tokimonCards.isEmpty()) {
            updateTokimonCard(tokimonCards, currentIndex, tokiName, tokiElement, tokiId, rarity, healthPoints, attackPoints, tokiImageView);
        }

        // Navigation buttons
        HBox navButtons = new HBox(5); // 10 pixel spacing between buttons
        Button deleteButton = new Button("\uD83D\uDC80");
        deleteButton.setOnAction(event -> handleDelete(tokimonCards, currentIndex, tokiName, tokiElement, tokiId, rarity, healthPoints, attackPoints, tokiImageView));
        Button prevButton = new Button("\u2190");
        prevButton.setOnAction(event -> handlePrev(tokimonCards, currentIndex, tokiName, tokiElement, tokiId, rarity, healthPoints, attackPoints, tokiImageView));
        Button nextButton = new Button("\u2192");
        nextButton.setOnAction(event -> handleNext(tokimonCards, currentIndex, tokiName, tokiElement, tokiId, rarity, healthPoints, attackPoints, tokiImageView));
        Button createButton = new Button("\u002B");
        createButton.setOnAction(event -> addPopup(tokimonCards, currentIndex, tokiName, tokiElement, tokiId, rarity, healthPoints, attackPoints, tokiImageView));
        Button editButton = new Button("EDIT");
        editButton.setOnAction(event -> editPopup(tokimonCards, currentIndex, tokiName, tokiElement, tokiId, rarity, healthPoints, attackPoints, tokiImageView));

        navButtons.getChildren().add(deleteButton);
        navButtons.getChildren().add(prevButton);
        navButtons.getChildren().add(nextButton);
        navButtons.getChildren().add(createButton);
        navButtons.getChildren().add(editButton);

        // Search bar
        HBox searchBar = new HBox(5);
        Label searchLabel = new Label("Search ID#: ");
        TextField searchField = new TextField();
        searchField.getStyleClass().add("searchField");
        Button searchButton = new Button("\uD83D\uDD0D");
        searchButton.setOnAction(event -> handleSearch(searchField.getText(), tokiName, tokiElement, tokiId, searchField, rarity, healthPoints, attackPoints, tokiImageView));
        searchBar.getChildren().add(searchLabel);
        searchBar.getChildren().add(searchField);
        searchBar.getChildren().add(searchButton);

        buttonMenu.getChildren().add(navButtons);
        buttonMenu.getChildren().add(searchBar);

        mainContainer.getChildren().add(tokiInfo);
        mainContainer.getChildren().add(buttonMenu);

        Scene scene = new Scene(mainContainer, 550, 300); // argument 1: width, arg2: height
        scene.getStylesheets().add("file:src/main/resources/styles.css");
        stage.setScene(scene);
        stage.setTitle("Tokidex Viewer");
        stage.show();
    }

    private void handleSearch(String searchText, Label tokiName, Label tokiElement, Label tokiId, TextField searchField, Label rarity, Label healthPoints, Label attackPoints, ImageView tokiImageView) {
        try {
            int id = Integer.parseInt(searchText);
            URI uri = new URI("http://localhost:8080/api/tokimon/" + id);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.getInputStream();

            // the server controller's response method will automatically serialize the TokimonCard object to JSON and return that as the response
            // the following code is used to read the response from the server
            String jsonResponse = readResponse(connection);

            // the following code is used to convert the JSON response to a TokimonCard object
            Gson gson = new Gson();
            TokimonCard tokimonCard = gson.fromJson(jsonResponse.toString(), TokimonCard.class);

            // Update JavaFX labels with the TokimonCard object
//            tokiName.setText(tokimonCard.getName());
//            tokiElement.setText(tokimonCard.getElementType().toString());
//            tokiId.setText(Long.toString(tokimonCard.getTid()));
            //List<TokimonCard> tokimonCards, AtomicInteger currentIndex, Label tokiName, Label tokiElement, Label tokiId, Label rarity, Label healthPoints, Label attackPoints, ImageView imageView)
            List<TokimonCard> tokimonCards = new ArrayList<>();
            tokimonCards.add(tokimonCard);
            AtomicInteger currentIndex = new AtomicInteger(0);
            updateTokimonCard(tokimonCards, currentIndex, tokiName, tokiElement, tokiId, rarity, healthPoints, attackPoints, tokiImageView);


        } catch (Exception e) {
            System.out.println("Invalid Search");
            searchField.clear();
            searchField.setPromptText("Invalid Search!");
        }
    }

    private void handleNext(List<TokimonCard> tokimonCards, AtomicInteger currentIndex, Label tokiName, Label tokiElement, Label tokiId, Label rarity, Label healthPoints, Label attackPoints, ImageView imageView) {
        if (currentIndex.get() < tokimonCards.size() - 1) {
            currentIndex.getAndIncrement();
        } else {
            currentIndex.set(0);
        }
        updateTokimonCard(tokimonCards, currentIndex, tokiName, tokiElement, tokiId, rarity, healthPoints, attackPoints, imageView);
    }

    private void handlePrev(List<TokimonCard> tokimonCards, AtomicInteger currentIndex, Label tokiName, Label tokiElement, Label tokiId, Label rarity, Label healthPoints, Label attackPoints, ImageView imageView) {
        if (currentIndex.get() > 0) {
            currentIndex.getAndDecrement();
        } else {
            currentIndex.set(tokimonCards.size() - 1);
        }
        updateTokimonCard(tokimonCards, currentIndex, tokiName, tokiElement, tokiId, rarity, healthPoints, attackPoints, imageView);
    }

    private void handleDelete(List<TokimonCard> tokimonCards, AtomicInteger currentIndex, Label tokiName, Label tokiElement, Label tokiId, Label rarity, Label healthPoints, Label attackPoints, ImageView imageView) {
        try {
            URI uri = new URI("http://localhost:8080/api/tokimon/" + tokimonCards.get(currentIndex.get()).getTid());
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.getInputStream();

            //fetch the updated list of TokimonCards for the server
            List<TokimonCard> updatedTokimonCards = fetchAllTokimonCards();
            tokimonCards.clear();
            tokimonCards.addAll(updatedTokimonCards);

            if (currentIndex.get() == tokimonCards.size()) {
                currentIndex.set(tokimonCards.size() - 1);
            }

            //update ui with new list of TokimonCards
            if (!tokimonCards.isEmpty()){
                updateTokimonCard(tokimonCards, currentIndex, tokiName, tokiElement, tokiId, rarity, healthPoints, attackPoints, imageView);
            } else {
                //clear the ui
                tokiName.setText("Name: ");
                tokiElement.setText("");
                tokiId.setText("");
            }

        } catch (Exception e) {
            System.out.println("Invalid Delete");
        }
    }

    private void addPopup(List<TokimonCard> tokimonCards, AtomicInteger currentIndex, Label tokiName, Label tokiElement, Label tokiId, Label rarity, Label healthPoints, Label attackPoints, ImageView imageView) {
        showPopup(false, tokimonCards, currentIndex, tokiName, tokiElement, tokiId, rarity, healthPoints, attackPoints, imageView);
    }

    private void editPopup(List<TokimonCard> tokimonCards, AtomicInteger currentIndex, Label tokiName, Label tokiElement, Label tokiId, Label rarity, Label healthPoints, Label attackPoints, ImageView tokiImageView) {
        //Note: this method only edits the client side, the server side is not updated, handleEdit method will handle the server side
        showPopup(true, tokimonCards, currentIndex, tokiName, tokiElement, tokiId, rarity, healthPoints, attackPoints, tokiImageView);
    }

    private void showPopup(boolean isEdit, List<TokimonCard> tokimonCards, AtomicInteger currentIndex, Label tokiName, Label tokiElement, Label tokiId, Label rarity, Label healthPoints, Label attackPoints, ImageView tokiImageView) {
        Stage popupStage = new Stage();
        popupStage.setTitle(isEdit ? "Edit Tokimon" : "Add Tokimon");

        VBox popupVBox = new VBox(10);
        popupVBox.setPadding(new Insets(10, 10, 10, 10)); // top, right, bottom, left
        popupVBox.setPrefSize(150, 200);
        popupVBox.setAlignment(Pos.CENTER);
        popupVBox.getStyleClass().add("popupVBox");
        Image backgroundImage = new Image("file:src/main/resources/pattern.png");

        //Create BackgroundImage obj
        BackgroundImage background = new BackgroundImage(backgroundImage, null, null, null, new BackgroundSize(BackgroundSize.DEFAULT.getWidth(), BackgroundSize.DEFAULT.getHeight(), true, true, false, true));
        popupVBox.setBackground(new Background(background));
        popupVBox.setAlignment(Pos.CENTER);

        TokimonCard currentTokimonCard = isEdit ? tokimonCards.get(currentIndex.get()) :
                new TokimonCard(0, "", TokimonCard.ElementType.FIRE, "unown.png", 0, 0);

        TextField newTokiName = new TextField(isEdit ? currentTokimonCard.getName() : "");
        newTokiName.setPromptText("[Name]");
        newTokiName.getStyleClass().add("popupTextField");
        popupVBox.getChildren().add(newTokiName);

        ComboBox<TokimonCard.ElementType> newElementComboBox = new ComboBox<>();
        newElementComboBox.getItems().addAll(TokimonCard.ElementType.values());
        newElementComboBox.setId("popupComboBox");
        newElementComboBox.setPromptText("Element");
        newElementComboBox.setMaxWidth(150);

        popupVBox.getChildren().add(newElementComboBox);

        TextField newHealth = new TextField(isEdit ? String.valueOf(currentTokimonCard.getHealthPoints()) : "");
        newHealth.setPromptText("[Health Points]");
        newHealth.getStyleClass().add("popupTextField");
        popupVBox.getChildren().add(newHealth);

        TextField newAttack = new TextField(isEdit ? String.valueOf(currentTokimonCard.getAttackPoints()) : "");
        newAttack.setPromptText("[Attack Points]");
        newAttack.getStyleClass().add("popupTextField");
        popupVBox.getChildren().add(newAttack);

        Button uploadBtn = new Button("Upload Photo");
        AtomicReference<String> imageName = new AtomicReference<>(isEdit ? currentTokimonCard.getImageName() : "unown.png");
        uploadBtn.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(popupStage);
            if (file != null) {
                uploadPhoto(file);
                imageName.set(file.getName());
            }
        });
        popupVBox.getChildren().add(uploadBtn);

        Label errorLabel = new Label();

        Button actionButton = new Button(isEdit ? "Edit Tokimon" : "Add Tokimon");
        popupVBox.getChildren().add(actionButton);
        actionButton.setOnAction(event -> {
            try{
                if (isEdit){
                    handleEdit(newTokiName.getText(), String.valueOf(newElementComboBox.getValue()), Integer.parseInt(newHealth.getText()), Integer.parseInt(newAttack.getText()), tokimonCards, currentIndex, tokiName, tokiElement, tokiId, popupStage, tokiImageView, imageName.toString(), rarity, healthPoints, attackPoints);
                } else {
                    handleAdd(newTokiName.getText(), String.valueOf(newElementComboBox.getValue()), Integer.parseInt(newHealth.getText()), Integer.parseInt(newAttack.getText()), tokimonCards, currentIndex, tokiName, tokiElement, tokiId, popupStage, tokiImageView, imageName.toString(), rarity, healthPoints, attackPoints);
                }
            } catch (Exception e){
                System.out.println("Invalid " + (isEdit ? "Edit" : "Add"));
                errorLabel.setText("Invalid " + (isEdit ? "Edit" : "Add. Please ensure you have filled all entries properly!"));
            }
        });
        popupVBox.getChildren().add(errorLabel);

        Scene popupScene = new Scene(popupVBox, 250, 300);
        popupScene.getStylesheets().add("file:src/main/resources/styles.css");
        popupStage.setScene(popupScene);
        popupStage.show();
    }

    // method is used to confirm addition of a new tokimon card within the popup
    private void handleAdd(String name, String element, int health, int attack, List<TokimonCard> tokimonCards, AtomicInteger currentIndex, Label tokiName, Label tokiElement, Label tokiId, Stage popupStage, ImageView tokiImageView, String imageName, Label rarity, Label healthPoints, Label attackPoints) {
        handleChange(false, name, element, health, attack, tokimonCards, currentIndex, tokiName, tokiElement, tokiId, popupStage, tokiImageView, imageName, rarity, healthPoints, attackPoints);
    }

    private void handleEdit(String name, String element, int health, int attack, List<TokimonCard> tokimonCards, AtomicInteger currentIndex, Label tokiName, Label tokiElement, Label tokiId, Stage popupStage, ImageView tokiImageView, String imageName, Label rarity, Label healthPoints, Label attackPoints) {
        handleChange(true, name, element, health, attack, tokimonCards, currentIndex, tokiName, tokiElement, tokiId, popupStage, tokiImageView, imageName, rarity, healthPoints, attackPoints);
    }

    //if isEdit then method sends a PUT request, if false then method sends a POST indicating we are adding a new tokimonCard
    private void handleChange(boolean isEdit ,String name, String element, int health, int attack, List<TokimonCard> tokimonCards, AtomicInteger currentIndex, Label tokiName, Label tokiElement, Label tokiId, Stage popupStage, ImageView tokiImageView, String imageName, Label rarity, Label healthPoints, Label attackPoints){
        try {
            URI uri;
            if (isEdit){
                String idText = tokiId.getText().replace("ID: ", "").trim(); // Remove the "ID: " prefix and trim any whitespace
                long id = Long.parseLong(idText);
                uri = new URI("http://localhost:8080/api/tokimon/edit/" + id);
            } else {
                uri = new URI("http://localhost:8080/api/tokimon/add");
            }

            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(isEdit ? "PUT" : "POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json"); // Set the request content type to JSON

            //for edits, we keep the same ID, for adds we increment tokimonCards.size and set it as the ID
            TokimonCard newTokimonCard = new TokimonCard(isEdit ? Long.parseLong(tokiId.getText().replace("ID: ", "").trim()) :
                    tokimonCards.size() + 1, name, TokimonCard.ElementType.valueOf(element.toUpperCase()), imageName, health, attack);

            Gson gson = new Gson();
            String json = gson.toJson(newTokimonCard); // convert the TokimonCard object to a JSON string using Gson

            OutputStream os = connection.getOutputStream();
            os.write(json.getBytes());
            os.flush();
            os.close();

            connection.getInputStream();

            List<TokimonCard> updatedTokimonCards = fetchAllTokimonCards();
            tokimonCards.clear();
            tokimonCards.addAll(updatedTokimonCards);

            if (currentIndex.get() >= tokimonCards.size()){
                currentIndex.set(0);
            }

            TokimonCard tokimonCard = tokimonCards.get(currentIndex.get());
            tokimonCard.setName(name);
            tokimonCard.setElementType(TokimonCard.ElementType.valueOf(element.toUpperCase()));
            tokimonCard.setHealthPoints(health);
            tokimonCard.setAttackPoints(attack);
            tokimonCard.setImageName(imageName);

            updateTokimonCard(tokimonCards, currentIndex, tokiName, tokiElement, tokiId, rarity, healthPoints, attackPoints, tokiImageView);

        } catch (Exception e) {
            System.out.println("Invalid " + (isEdit ? "Edit" : "Add"));
            e.printStackTrace();
        }

        popupStage.close();
    }

    private void updateTokimonCard(List<TokimonCard> tokimonCards, AtomicInteger currentIndex, Label tokiName, Label tokiElement, Label tokiId, Label rarity, Label healthPoints, Label attackPoints, ImageView imageView) {
        TokimonCard currentTokimonCard = tokimonCards.get(currentIndex.get());
        tokiName.setText("Name: " + currentTokimonCard.getName());
        tokiElement.setText("Element: " + currentTokimonCard.getElementType().toString());
        tokiId.setText("ID: " + currentTokimonCard.getTid());
        rarity.setText("Rarity: " + currentTokimonCard.getRarity());
        healthPoints.setText("HP: " + currentTokimonCard.getHealthPoints());
        attackPoints.setText("AP: " + currentTokimonCard.getAttackPoints());
        imageView.setImage(new Image("http://localhost:8080/images/" + currentTokimonCard.getImageName()));
    }

    private void uploadPhoto(File file){
        try {
            URL url = new URL("http://localhost:8080/api/tokimon/uploadPhoto");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true); // Set the connection to allow output
            connection.setRequestProperty("Content-Type", "multipart/formdata; boundary=---ContentBoundary"); // Set the request content type to JSON

            OutputStream outputStream = connection.getOutputStream(); // Get the output stream from the connection
            FileInputStream fileInputStream = new FileInputStream(file); // Read the file into a FileInputStream

            // Write the file content to the output stream, the strings below are the format of the multipart form data
            String boundary = "---ContentBoundary";
            String newLine = "\r\n"; // \r is carriage return
            String fileName = file.getName();

            outputStream.write(("--" + boundary + newLine).getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + newLine).getBytes()); // Write the file name
            outputStream.write((("Content-Type: ") + Files.probeContentType(file.toPath()) + newLine).getBytes()); // Probe the content type of the file
            outputStream.write(newLine.getBytes());

            // Write the file content to the output stream by reading the file input stream
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.write(newLine.getBytes()); // Write a new line to separate the file content from the boundary
            outputStream.write(("--" + boundary + "--" + newLine).getBytes()); // Write the boundary to indicate the end of the file content

            int responseCode = connection.getResponseCode(); // Send the request
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("File uploaded successfully");
            } else {
                System.out.println("File upload failed");
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private List<TokimonCard> fetchAllTokimonCards(){
        List<TokimonCard> tokimonCards = new ArrayList<>();
        try {
            URI uri = new URI("http://localhost:8080/api/tokimon/all");
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            String jsonResponse = readResponse(connection);

            Gson gson = new Gson();
            TokimonCard[] tokimonCardArray = gson.fromJson(jsonResponse.toString(), TokimonCard[].class);
            tokimonCards = new ArrayList<>(Arrays.asList(tokimonCardArray)); // Convert the array to a modifiable list
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tokimonCards;
    }

    // method is used to read the response from the server, returns the response as a string
    private String readResponse(HttpURLConnection connection) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        return response.toString();
    }

    public static void main(String[] args) {
        launch();
    }
}