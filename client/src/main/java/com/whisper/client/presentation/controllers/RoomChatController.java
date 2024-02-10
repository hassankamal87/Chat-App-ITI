package com.whisper.client.presentation.controllers;

import com.whisper.client.HelloApplication;
import com.whisper.client.MyApp;
import com.whisper.client.business.services.ChattingService;
import com.whisper.client.business.services.ClientService;
import com.whisper.client.presentation.services.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Message;
import org.example.entities.RoomChat;
import org.example.entities.Type;
import org.example.entities.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class RoomChatController implements ReceiveMessageInterface {
    @FXML
    private AnchorPane roomChatPane;
    @FXML
    private ImageView personalImage;
    @FXML
    private Text nameText;
    @FXML
    private Text modeText;
    @FXML
    private Button callBtn;
    @FXML
    private BorderPane chatBorderPane;
    @FXML
    private HTMLEditor messageEditor;
    @FXML
    private Button sendBtn;
    @FXML
    private ScrollPane messagesScrollPane;
    @FXML
    private VBox messageList;

    private ChattingService chattingService;
    private int roomChatID;
    private List<User> friendsOnChat;
    @FXML
    private Button attachBtn;
    @FXML
    private Button editBtn;


    public void setData(RoomChat roomChat, List<User> friendsOnChat) {
        this.roomChatID = roomChat.getRoomChatId();
        this.friendsOnChat = friendsOnChat;
        if (roomChat.getType() == Type.individual) {
            nameText.setText(friendsOnChat.get(0).getUserName());
            modeText.setText(friendsOnChat.get(0).getMode().name());
            personalImage.setImage(new Image(new ByteArrayInputStream(friendsOnChat.get(0).getProfilePhoto())));
        } else {
            nameText.setText(roomChat.getGroupName());
            modeText.setText("Group");
            editBtn.setVisible(true);
            if(roomChat.getAdminId() == MyApp.getInstance().getCurrentUser().getUserId()){
                editBtn.setDisable(false);
            }
        }
        makeImageRounded(personalImage);
        try {
            ClientService.getInstance().registerChat(roomChatID, this);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        HashMap<Message, File> messagesAndFiles = chattingService.getMessagesWithFilesForRoomChat(roomChatID);

        Platform.runLater(() -> {
            showOldMessages(messagesAndFiles);
        });

    }

    @FXML
    public void initialize() {
        chattingService = ChattingService.getInstance();
    }

    @FXML
    public void onSendBtnClicked(Event event) {
        String htmlContent = messageEditor.getHtmlText();

        Document doc = Jsoup.parse(htmlContent);
        String textContent = doc.text();

        if (!textContent.isEmpty()) {
            appendMessageInList(htmlContent);
            chattingService.sendMessage(MyApp.getInstance().getCurrentUser().getUserId(), roomChatID, htmlContent);
            messageEditor.setHtmlText("");
        }
    }

    @FXML
    public void onMouseEnteredSendBtn(Event event) {
    }

    private void appendMessageInList(String htmlContent) {
        try {
            Node node = FXMLLoader.load(Objects.requireNonNull(HelloApplication.class.getResource("views/messageItemView.fxml")));

            WebView messageWebView = (WebView) node.lookup("#messageWebView");
            ImageView myImage = (ImageView) node.lookup("#ImageView");
            WebEngine messageEngine = messageWebView.getEngine();
            makeWebViewTransparent(messageEngine);
            myImage.setImage(new Image(new ByteArrayInputStream(MyApp.getInstance().getCurrentUser().getProfilePhoto())));
            makeImageRounded(myImage);
            messageEngine.loadContent(htmlContent);
            messageList.getChildren().add(node);

            messagesScrollPane.setVvalue(1D);
            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.seconds(0.7), // Set the delay duration here (e.g., 0.5 seconds)
                    event -> messagesScrollPane.setVvalue(1D) // Code to execute after the delay
            ));
            timeline.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveMessageFromList(Message message) {
        friendsOnChat = chattingService.getUsersInCommonRoomChat(roomChatID);
        try {
            Node node = FXMLLoader.load(Objects.requireNonNull(HelloApplication.class.getResource("views/messageItemViewReciever.fxml")));

            WebView messageWebView = (WebView) node.lookup("#messageWebView");
            ImageView myImage = (ImageView) node.lookup("#ImageView");
            Label nameLabel = (Label) node.lookup("#nameLabel");

            User friendUser = friendsOnChat.stream().filter(friend -> friend.getUserId() == message.getFromUserId()).findFirst().get();
            WebEngine messageEngine = messageWebView.getEngine();
            makeWebViewTransparent(messageEngine);
            messageEngine.loadContent(message.getBody());
            if (message.getMessageId() != -3) {
                myImage.setImage(new Image(new ByteArrayInputStream(friendUser.getProfilePhoto())));
                nameLabel.setText(friendUser.getUserName());
            }else{
                //if message id = -3 that's means sender is ChatBot
                myImage.setImage(new Image(getClass().getResourceAsStream("/com/whisper/client/images/robot.png")));
                nameLabel.setText("Whisper Bot");
            }
            makeImageRounded(myImage);
            messageList.getChildren().add(node);

            messagesScrollPane.setVvalue(1D);
            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.seconds(0.7), // Set the delay duration here (e.g., 0.5 seconds)
                    event -> messagesScrollPane.setVvalue(1D) // Code to execute after the delay
            ));
            timeline.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendMessageFile(File selectedFile, Date date,String fileN) {
        try {
            Node node = FXMLLoader.load(Objects.requireNonNull(HelloApplication.class.getResource("views/fileItemView1.fxml")));

            ImageView myImage = (ImageView) node.lookup("#ImageView");
            Button downloadBtn = (Button) node.lookup("#downloadBtn");
            Label fileName = (Label) node.lookup("#fileName");
            Label timeLabel = (Label) node.lookup("#timeLabel");

            //timeLabel.setText(formatUtilDate(date));
            timeLabel.setText("06:23 AM");
            fileName.setText(fileN);
            myImage.setImage(new Image(new ByteArrayInputStream(MyApp.getInstance().getCurrentUser().getProfilePhoto())));
            makeImageRounded(myImage);

            downloadBtn.setOnAction(e -> {
                saveFile(selectedFile);
            });
            messageList.getChildren().add(node);

            messagesScrollPane.setVvalue(1D);
            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.seconds(1), // Set the delay duration here (e.g., 0.5 seconds)
                    event -> messagesScrollPane.setVvalue(1D) // Code to execute after the delay
            ));
            timeline.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveFileFromList(Message message, File file) {
        friendsOnChat = chattingService.getUsersInCommonRoomChat(roomChatID);
        try {
            Node node = FXMLLoader.load(Objects.requireNonNull(HelloApplication.class.getResource("views/fileItemViewReciever2.fxml")));

            User friendUser = friendsOnChat.stream().filter(friend -> friend.getUserId() == message.getFromUserId()).findFirst().get();

            Label nameLabel = (Label) node.lookup("#nameLabel");
            ImageView myImage = (ImageView) node.lookup("#ImageView");
            Button downloadBtn = (Button) node.lookup("#downloadBtn");
            Label fileName = (Label) node.lookup("#fileName");
            Label timeLabel = (Label) node.lookup("#timeLabel");


            timeLabel.setText(formatUtilDate(message.getSentDate()));
            nameLabel.setText(friendUser.getUserName());
            fileName.setText(message.getBody());
            myImage.setImage(new Image(new ByteArrayInputStream(friendUser.getProfilePhoto())));
            makeImageRounded(myImage);

            downloadBtn.setOnAction(e -> {
                saveFile(file);
            });
            messageList.getChildren().add(node);

            messagesScrollPane.setVvalue(1D);
            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.seconds(1), // Set the delay duration here (e.g., 0.5 seconds)
                    event -> messagesScrollPane.setVvalue(1D) // Code to execute after the delay
            ));
            timeline.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showOldMessages(HashMap<Message, File> messagesAndFiles) {
        List<Map.Entry<Message, File>> sortedMessages = new ArrayList<>(messagesAndFiles.entrySet());

        // Sort the list based on Message ID
        Collections.sort(sortedMessages, Comparator.comparing(entry -> entry.getKey().getMessageId()));

        // Iterate over the sorted list
        for (Map.Entry<Message, File> entry : sortedMessages) {
            Message message = entry.getKey();
            File file = entry.getValue();

            if (message.getFromUserId() == MyApp.getInstance().getCurrentUser().getUserId()) {
                if (file == null)
                    appendMessageInList(message.getBody());
                else
                    appendMessageFile(file, message.getSentDate(), message.getBody());
            } else {
                if (file == null)
                    receiveMessageFromList(message);
                else
                    receiveFileFromList(message, file);
            }
        }
    }

    private void makeImageRounded(ImageView chatItemImage) {
        Circle clip = new Circle(chatItemImage.getFitWidth() / 2.6, chatItemImage.getFitHeight() / 2, chatItemImage.getFitWidth() / 3);
        chatItemImage.setClip(clip);
        chatItemImage.setPreserveRatio(true);
    }

    @FXML
    public void onAttachBtnClicked(Event event) {
        // Create a FileChooser
        FileChooser fileChooser = new FileChooser();

        // Set the title for the FileChooser dialog
        fileChooser.setTitle("Choose a File");

        // Show the FileChooser dialog
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        // Check if a file was selected
        if (selectedFile != null) {
            // Print the absolute path of the selected file
            //append file message in list view and send it to server
            appendMessageFile(selectedFile, Calendar.getInstance().getTime(),selectedFile.getName());
            sendMessageFile(selectedFile);

        } else {
            System.out.println("No file selected.");
        }
    }

    private void sendMessageFile(File selectedFile) {
        chattingService.sendFile(MyApp.getInstance().getCurrentUser().getUserId(), roomChatID, selectedFile);
    }

    private void saveFile(File sourceFile) {
        FileChooser saveFileChooser = new FileChooser();
        saveFileChooser.setInitialFileName(sourceFile.getName());
        saveFileChooser.setTitle("Whisper");
        File saveFile = saveFileChooser.showSaveDialog(new Stage());
        Path sourcePath = sourceFile.toPath();
        if (saveFile != null) {
            Path destinationPath = saveFile.toPath();
            try {
                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File saved successfully to: " + destinationPath);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error saving the file.");
            }
        }
    }

    public String formatUtilDate(Date utilDate) {
        // Create a SimpleDateFormat with the desired pattern
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

        // Format the utilDate to a string
        return sdf.format(utilDate);
    }

    private void makeWebViewTransparent(WebEngine engine) {
        final com.sun.webkit.WebPage webPage = com.sun.javafx.webkit.Accessor.getPageFor(engine);
        webPage.setBackgroundColor(0);
    }

    @FXML
    public void onEditBtnClickLed(Event event) {
       /* try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("views/profileGroupView.fxml"));
            Parent root = fxmlLoader.load();

            profileGroupController controller = fxmlLoader.getController();
            controller.setData(roomChatID);

            Scene scene = new Scene(root);

            Stage newStage= new Stage();
            newStage.setScene(scene);
            newStage.setResizable(false);
            newStage.setTitle("Edit Group");
            //   Image icon = new Image(getClass().getResourceAsStream("images/wlogo.png")); // Adjust the path accordingly
            //   newStage.getIcons().add(icon);
            newStage.show();
        } catch (IOException e) {
            System.out.println("exception in main Controller class line 93");
        }*/
    }
}