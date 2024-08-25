package com.peko_xmpp;

// Java FX
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.*;
import javafx.collections.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.util.*;

// Smack Lib
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.xdata.form.*;
import org.jivesoftware.smackx.muc.*;

import org.jivesoftware.smack.packet.Presence.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.roster.*;
import org.jivesoftware.smack.chat2.*;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.tcp.*;
import org.jivesoftware.smack.*;

// XMPP Lib
import org.jxmpp.jid.parts.*;
import org.jxmpp.jid.impl.*;
import org.jxmpp.jid.*;

// Java
import javax.swing.JFileChooser;
import java.nio.file.*;
import java.util.*;
import java.io.*;

public class App extends Application {
	private Stage main_stage;
	private VBox layout_notifications;
	private Button button_notifications;
	private boolean notifications_shown;

	private XMPPTCPConnection xmpp_connection;
	private MultiUserChatManager multi_user_chat_manager;
	private MultiUserChat multi_user_chat;
	private ChatManager chat_manager;

	private ObservableList<Pair<String,String>> chatMessages = FXCollections.observableArrayList();
	private ObservableList<Pair<String,String>> roomMessages = FXCollections.observableArrayList();

	private Map<String, MessageListener> room_chat_listeners = new HashMap<>();

	private String user_domain = "@alumchat.lol";
	private String room_domain = "@conference.alumchat.lol";

	private String domain = "alumchat.lol";
	private String group_id = "grupeko";
	private String username = "mar21430";
	private String nickname = "Pekoyo";
	private String password = "Test_123";

	private Mode status = Mode.available;
	private String status_message = "Pe↑ko↓ pe↑ko↓ pe↑ko↓ pe↑ko↓";

/**
 * Start UI
 *
 * @param stage Stage
 * @throws IOException
 */
	@Override
	public void start(Stage stage) throws IOException {
		main_stage = stage;
		main_stage.setTitle("XMPP Chat");
		main_stage.setOnCloseRequest(event -> {
			if (xmpp_connection != null && xmpp_connection.isConnected()) {
				xmpp_connection.removeAllStanzaAcknowledgedListeners();
				signOut();
			}

		});

		StackPane root = new StackPane();

		Scene scene = new Scene(root, 1600, 950);
		scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
		scene.setRoot(root);

		main_stage.setScene(scene);
		main_stage.show();

		guiLoginScreen(scene);
	}
/*-----------------------------

GUI

-----------------------------*/
/**
 * Load GUI for the Login Screen
 *
 * @param scene Scene
 */
	private void guiLoginScreen(Scene scene) {
		TextField field_username = new TextField(username);
		field_username.setPromptText("Username");
		field_username.setStyle("-fx-max-width: Infinity;");

		PasswordField field_password = new PasswordField();
		field_password.setPromptText("Password");
		field_password.setStyle("-fx-max-width: Infinity;");
		field_password.setText(password);

		Button button_signin = new Button("Sign In");
		button_signin.setStyle("-fx-pref-width: 200px;");

		Button button_signup = new Button("Sign Up");
		button_signup.setStyle("-fx-pref-width: 200px;");

		HBox layout_a = new HBox(10);
		layout_a.setAlignment(Pos.CENTER);
		layout_a.getChildren().addAll(field_username, button_signin);
		HBox.setHgrow(field_username, Priority.ALWAYS);

		HBox layout_b = new HBox(10);
		layout_b.setAlignment(Pos.CENTER);
		layout_b.getChildren().addAll(field_password, button_signup);
		HBox.setHgrow(field_password, Priority.ALWAYS);

		VBox layout = new VBox(10);
		layout.setAlignment(Pos.TOP_CENTER);
		layout.setPadding(new Insets(10));
		layout.getChildren().addAll(layout_a, layout_b);

		StackPane root = new StackPane();
		root.getChildren().add(layout);
		root.setStyle("-fx-background-radius: 0px;");

		scene.setRoot(root);

		button_signin.setOnAction(event -> {
			username = field_username.getText();
			password = field_password.getText();
			if (signIn()) {
				setup();
				guiHomeScreen(scene);
			}
		});

		button_signup.setOnAction(event -> {
			username = field_username.getText();
			password = field_password.getText();
			if (signUp()) {
				setup();
				guiHomeScreen(scene);
			}
		});

		field_username.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER) {
				username = field_username.getText();
				password = field_password.getText();
				if (signIn()) {
					setup();
					guiHomeScreen(scene);
				}
			}
		});

		field_password.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER) {
				username = field_username.getText();
				password = field_password.getText();
				if (signIn()) {
					setup();
					guiHomeScreen(scene);
				}
			}
		});
	}
/**
 * Load GUI for the Home Screen
 *
 * @param scene Scene
 */
	private void guiHomeScreen(Scene scene) {
		Label label = new Label("Welcome " + username + "  |  " + nickname);
		label.setStyle("-fx-max-width: Infinity; -fx-font-size: 20px;");

		Button button_logout = new Button("Log Out");
		
		button_notifications = new Button(" 🔔 ");

		HBox layout_header = new HBox(10);
		layout_header.getChildren().addAll(label, button_logout, button_notifications);
		HBox.setHgrow(label, Priority.ALWAYS);

		Button button_chat = new Button("Chat");
		button_chat.setStyle("-fx-max-width: Infinity;");

		Button button_group_chat = new Button("Group Chat");
		button_group_chat.setStyle("-fx-max-width: Infinity;");

		Button button_account = new Button("My Account");
		button_account.setStyle("-fx-max-width: Infinity;");

		VBox layout_main = new VBox(10);
		layout_main.setAlignment(Pos.TOP_CENTER);
		layout_main.setStyle("-fx-max-width: Infinity;");

		HBox layout_menu = new HBox(10);
		layout_menu.setAlignment(Pos.CENTER);
		layout_menu.getChildren().addAll(button_chat,button_group_chat, button_account);
		HBox.setHgrow(button_chat, Priority.ALWAYS);
		HBox.setHgrow(button_group_chat, Priority.ALWAYS);
		HBox.setHgrow(button_account, Priority.ALWAYS);

		Label label_notifications = new Label("Notifications");
		label_notifications.setStyle("-fx-max-width: Infinity;");

		layout_notifications = new VBox(10);
		layout_notifications.setPadding(new Insets(10));
		ScrollPane scroll_notifications = new ScrollPane(layout_notifications);
		scroll_notifications.setStyle("-fx-max-height: Infinity;");
		scroll_notifications.setPrefWidth(800);
		scroll_notifications.setFitToWidth(true);
		scroll_notifications.setFitToHeight(true);
		notifications_shown = false;
		
		VBox layout_notification_area = new VBox(10);
		layout_notification_area.getChildren().addAll(label_notifications, scroll_notifications);
		VBox.setVgrow(scroll_notifications, Priority.ALWAYS);

		HBox sub_layout = new HBox(10);
		sub_layout.getChildren().addAll(layout_main);
		HBox.setHgrow(layout_main, Priority.ALWAYS);

		VBox layout_container = new VBox(10);
		layout_container.setPadding(new Insets(10));
		layout_container.setAlignment(Pos.TOP_CENTER);
		layout_container.getChildren().add(layout_header);
		layout_container.getChildren().add(sub_layout);
		layout_container.getChildren().add(layout_menu);
		VBox.setVgrow(sub_layout, Priority.ALWAYS);

		StackPane root = new StackPane();
		root.getChildren().add(layout_container);
		root.setStyle("-fx-background-radius: 0px;");

		scene.setRoot(root);

		button_chat.setOnAction(event -> {
			guiChatScreen(scene, layout_main, label);
		});

		button_group_chat.setOnAction(event -> {
			guiRoomScreen(scene, layout_main, label);
		});

		button_account.setOnAction(event -> {
			guiAccountScreen(scene, layout_main, label);
		});
		
		button_logout.setOnAction(event -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			
			alert.setTitle("Confirmation");
			alert.setHeaderText("Confirm your action");
			alert.setContentText("Are you sure you want to sign out?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				signOut();
				guiLoginScreen(scene);
			}
		});

		button_notifications.setOnAction(event -> {
			if (!sub_layout.getChildren().contains(layout_notification_area)) {
				sub_layout.getChildren().add(layout_notification_area);
				button_notifications.setStyle("-fx-background-color: rgb(25,25,25);");
				notifications_shown = true;
				for (Node node : layout_notifications.getChildren()) {
					PauseTransition pause = new PauseTransition(Duration.seconds(3));
					pause.setOnFinished(e -> {
						FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), node);
						fadeOut.setFromValue(1.0);
						fadeOut.setToValue(0.0);
						fadeOut.setOnFinished(ee -> {
							layout_notifications.getChildren().remove(node);
						});
						fadeOut.play();
					});
					pause.play();
				}
			}
			else {
				sub_layout.getChildren().remove(layout_notification_area);
				button_notifications.setStyle("-fx-background-color: rgb(25,25,25);");
				notifications_shown = false;
			}
		});

		guiChatScreen(scene, layout_main, label);
	}
/**
 * Load GUI for the Chat Screen
 *
 * @param scene Scene
 * @param container VBox to insert the Chat Contents
 * @param label Label for the current screen
 */
	private void guiChatScreen(Scene scene, VBox container, Label label) {
		container.getChildren().clear();
		label.setText("Chat");
//
		Label label_contact = new Label("My Contacts");
		label_contact.setStyle("-fx-max-width: Infinity;");

		Button button_add_contact = new Button("Add Contact");
		button_add_contact.setStyle("-fx-max-width: Infinity;");

		Button button_remove_contact = new Button("Delete Contact");
		button_remove_contact.setStyle("-fx-max-width: Infinity;");

		HBox layout_contacts_header = new HBox(10);
		layout_contacts_header.getChildren().addAll(button_add_contact, button_remove_contact);
		HBox.setHgrow(button_add_contact, Priority.ALWAYS);
		HBox.setHgrow(button_remove_contact, Priority.ALWAYS);

		VBox layout_contacts_list_content = new VBox(10);
		layout_contacts_list_content.setPadding(new Insets(10));
		ScrollPane scroll_contacts = new ScrollPane(layout_contacts_list_content);
		scroll_contacts.setStyle("-fx-max-height: Infinity; -fx-max-width: Infinity; -fx-min-width:200px;");
		scroll_contacts.setFitToWidth(true);
		scroll_contacts.setFitToHeight(true);

		VBox layout_contacts = new VBox(10);
		layout_contacts.getChildren().addAll(label_contact, layout_contacts_header, scroll_contacts);
		layout_contacts.setStyle("-fx-min-width: 400px;");
		VBox.setVgrow(scroll_contacts, Priority.ALWAYS);
//
		Label label_messages = new Label("Chat");
		label_messages.setStyle("-fx-max-width: Infinity;");

		Label label_address = new Label("NONE" + user_domain);
		label_address.setStyle("-fx-max-width: Infinity; -fx-max-height: Infinity;");

		VBox layout_message_area = new VBox(10);
		layout_message_area.setPadding(new Insets(10));
		ScrollPane scroll_messages = new ScrollPane(layout_message_area);
		scroll_messages.setStyle("-fx-max-height: Infinity; fx-max-width: Infinity;");
		scroll_messages.setFitToWidth(true);
		scroll_messages.setFitToHeight(true);
		scroll_messages.setVisible(false);

		TextArea field_message = new TextArea();
		field_message.setStyle("-fx-max-height: Infinity;");
		field_message.setPromptText("Message...");
		field_message.setVisible(false);

		Button button_attach_file = new Button("Send File");
		button_attach_file.setStyle("-fx-max-width: Infinity;");
		button_attach_file.setVisible(false);

		VBox layout_message = new VBox(10);
		layout_message.getChildren().addAll(label_messages, label_address, scroll_messages, field_message, button_attach_file);
		layout_message.setStyle("-fx-pref-width: 800px;");
		VBox.setVgrow(scroll_messages, Priority.ALWAYS);
//
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(layout_contacts, layout_message);
		HBox.setHgrow(layout_contacts, Priority.ALWAYS);
		HBox.setHgrow(layout_message, Priority.ALWAYS);

		container.getChildren().add(hbox);
		VBox.setVgrow(hbox, Priority.ALWAYS);

		button_add_contact.setOnAction(event -> {
			TextInputDialog dialog = new TextInputDialog();
			dialog.getDialogPane().getStyleClass().add("alert");
			dialog.setTitle("Add contact");
			dialog.setHeaderText("Add contact");
			dialog.setContentText("Contact JID to add:");

			dialog.showAndWait().ifPresent(jid -> addContact(jid + user_domain));
		});

		button_remove_contact.setOnAction(event -> {
			TextInputDialog dialog = new TextInputDialog();
			dialog.getDialogPane().getStyleClass().add("alert");
			dialog.setTitle("Delete contact");
			dialog.setHeaderText("Delete contact");
			dialog.setContentText("Contact JID to delete:");

			dialog.showAndWait().ifPresent(jid -> removeContact(jid + user_domain));
		});

		field_message.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER) {
				sendChatMessage(label_address.getText() + user_domain, field_message.getText());
				chatMessages.add(new Pair<String,String>(username, field_message.getText()));
				field_message.clear();
				event.consume();
			}
		});

		button_attach_file.setOnAction(event-> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Select a file");
			int userSelection = fileChooser.showOpenDialog(null);
			if (userSelection == JFileChooser.APPROVE_OPTION) {
				sendFile(label_address.getText() + user_domain, fileChooser.getSelectedFile());
				chatMessages.add(new Pair<String,String>(username, fileChooser.getSelectedFile().getAbsolutePath()));
				field_message.clear();
			}
		});

		getContacts(layout_contacts_list_content, layout_message_area, field_message, button_attach_file, scroll_messages, label_address);
	}
/**
 * Load GUI for the Group Chat Screen
 *
 * @param scene Scene
 * @param container VBox to insert the Group Chat Contents
 * @param label Label for the current screen
 */
	private void guiRoomScreen(Scene scene, VBox container, Label label) {
		container.getChildren().clear();
		label.setText("Group Chat");
//
		Label label_messages = new Label("Group Chat");
		label_messages.setStyle("-fx-max-width: Infinity;");

		TextField field_room_jid = new TextField(group_id);
		field_room_jid.setPromptText("Join Group with JID...");
		field_room_jid.setStyle("-fx-max-width: Infinity;");
		field_room_jid.setAlignment(Pos.CENTER_RIGHT);

		Label label_address = new Label(room_domain);
		label_address.setStyle("-fx-max-width: Infinity; -fx-max-height: Infinity;");

		Button button_join_room = new Button("Join Room");
		button_join_room.setStyle("-fx-max-width: Infinity;");
		
		Button button_delete_room = new Button("Delete Room");
		button_delete_room.setStyle("-fx-max-width: Infinity; -fx-background-color: rgb(100,50,50);");
		button_delete_room.setOnMouseEntered(e -> 
			button_delete_room.setStyle(
				"-fx-max-width: Infinity; -fx-background-color: rgb(250,100,100); "
			)
		);
		button_delete_room.setOnMouseExited(e -> 
			button_delete_room.setStyle(
				"-fx-max-width: Infinity; -fx-background-color: rgb(100,50,50); "
			)
		);
		VBox layout_message_area = new VBox(10);
		layout_message_area.setPadding(new Insets(10));
		ScrollPane scroll_messages = new ScrollPane(layout_message_area);
		scroll_messages.setStyle("-fx-max-height: Infinity; fx-max-width: Infinity;");
		scroll_messages.setFitToWidth(true);
		scroll_messages.setFitToHeight(true);
		scroll_messages.setVisible(false);

		TextArea field_message = new TextArea();
		field_message.setStyle("-fx-max-height: Infinity;");
		field_message.setPromptText("Message...");
		field_message.setVisible(false);

		HBox layout_message_header = new HBox(10);
		layout_message_header.getChildren().addAll(field_room_jid, label_address, button_join_room, button_delete_room);
		HBox.setHgrow(field_room_jid, Priority.ALWAYS);

		VBox layout_message = new VBox(10);
		layout_message.getChildren().addAll(label_messages, layout_message_header, scroll_messages, field_message);
		layout_message.setStyle("-fx-pref-width: 800px;");
		VBox.setVgrow(scroll_messages, Priority.ALWAYS);
//
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(layout_message);
		HBox.setHgrow(layout_message, Priority.ALWAYS);

		container.getChildren().add(hbox);
		VBox.setVgrow(hbox, Priority.ALWAYS);

		button_join_room.setOnAction(event -> {
			roomMessages.clear();
			if (joinRoom(field_room_jid.getText() + room_domain)) {
				layout_message_area.getChildren().clear();
				field_message.setVisible(true);
				scroll_messages.setVisible(true);

				setupRoomMessageListener();

				roomMessages.addListener((ListChangeListener<Pair<String, String>>) change -> {
					layout_message_area.getChildren().clear();
					for (Pair<String,String> message: roomMessages) {
						if (message.getValue() != null) {
							guiAddIncomingMessage(message.getKey(), message.getValue(), layout_message_area);
						}
					}
				});
			}
		});

		button_delete_room.setOnAction(event -> {
			if (deleteRoom(field_room_jid.getText() + room_domain)) {
				field_message.setVisible(false);
				scroll_messages.setVisible(false);
				layout_message_area.getChildren().clear();
			}
		});

		field_message.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER) {
				sendRoomMessage(field_room_jid.getText() + room_domain, field_message.getText());
				field_message.clear();
				event.consume();
			}
		});
	}
/**
 * Load GUI for the Account Screen
 *
 * @param scene Scene
 * @param container VBox to insert the Account Settings
 * @param label Label for the current screen
 */
	private void guiAccountScreen(Scene scene, VBox container, Label label) {
		container.getChildren().clear();
		label.setText("My Account  |  " + username + "  |  " + nickname);
//
		Label label_presence = new Label("Status Message");
		label_presence.setStyle("-fx-max-width: Infinity;");

		TextField field_presence = new TextField(status_message);
		field_presence.setPromptText("Status Message...");
		field_presence.setStyle("-fx-max-width: Infinity;");

		Button button_set_presence = new Button("Update");

		HBox layout_presence_set = new HBox(10);
		layout_presence_set.getChildren().addAll(field_presence, button_set_presence);
		HBox.setHgrow(field_presence, Priority.ALWAYS);
	//
		Label label_status_options = new Label("Status");
		label_status_options.setStyle("-fx-max-width: Infinity;");

		ComboBox<Mode> combobox_status_options = new ComboBox<>();
		combobox_status_options.setStyle("-fx-max-width: Infinity;");
		combobox_status_options.getItems().addAll(Mode.available, Mode.away, Mode.chat, Mode.dnd, Mode.xa);
		combobox_status_options.setValue(Mode.available);
	//
		Label label_nickname = new Label("Nickname");
		label_nickname.setStyle("-fx-max-width: Infinity;");

		TextField field_nickname = new TextField(nickname);
		field_nickname.setPromptText("Nickname...");
		field_nickname.setStyle("-fx-max-width: Infinity;");

		Button button_set_nickname = new Button("Update");

		HBox layout_nickname_set = new HBox(10);
		layout_nickname_set.getChildren().addAll(field_nickname, button_set_nickname);
		HBox.setHgrow(field_nickname, Priority.ALWAYS);
//
		VBox layout_account_settings = new VBox(10);
		layout_account_settings.getChildren().addAll(label_presence, layout_presence_set, label_status_options, combobox_status_options, label_nickname, layout_nickname_set);
//
		Button button_close_account = new Button("Delete Account");
		button_close_account.setStyle("-fx-max-width: Infinity; -fx-background-color: rgb(100,50,50);");
		
		button_close_account.setOnMouseEntered(e -> 
		button_close_account.setStyle(
				"-fx-max-width: Infinity; -fx-background-color: rgb(250,100,100); "
			)
		);
		button_close_account.setOnMouseExited(e -> 
		button_close_account.setStyle(
				"-fx-max-width: Infinity; -fx-background-color: rgb(100,50,50); "
			)
		);
//
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(layout_account_settings);
		HBox.setHgrow(layout_account_settings, Priority.ALWAYS);

		container.getChildren().addAll(hbox, button_close_account);
		VBox.setVgrow(hbox, Priority.ALWAYS);

		button_set_presence.setOnAction(event -> {
			status_message = field_presence.getText();
			setStatus();
		});

		button_set_presence.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER) {
				status_message = field_presence.getText();
				setStatus();
			}
		});

		button_set_nickname.setOnAction(event -> {
			nickname = field_nickname.getText();
			label.setText("My Account  |  " + username + "  |  " + nickname);
		});

		button_set_nickname.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER) {
				nickname = field_nickname.getText();
				label.setText("My Account  |  " + username + "  |  " + nickname);
			}
		});

		button_close_account.setOnAction(event -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			
			alert.setTitle("Confirmation");
			alert.setHeaderText("Confirm your action");
			alert.setContentText("Are you sure you want to delete your account?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				deleteAccount();
				guiLoginScreen(scene);
			}
		});

		combobox_status_options.setOnAction(event -> {
			status = combobox_status_options.getValue();
			setStatus();
		});
	}
/**
 * Add GUI Chat Element for the Chat Screen
 *
 * @param sender_username String username of the one who sent the message
 * @param message String containing the message
 * @param contents VBox of where to insert the messages
 */
	private void guiAddIncomingMessage(String sender_username, String message, VBox contents) {
		if (sender_username.equals(username) || sender_username.equals(nickname)) {
			Label label_message = new Label(message);
			label_message.setStyle("-fx-max-width: Infinity; -fx-font-size: 14px; -fx-background-radius: 5px 5px; -fx-background-color: rgb(60,60,60); -fx-text-fill: rgb(175,250,175);");
			label_message.setAlignment(Pos.CENTER_RIGHT);
			label_message.setTextAlignment(TextAlignment.RIGHT);

			VBox layout_message = new VBox(0);
			layout_message.setStyle("-fx-min-width: 500px; -fx-max-width: 500px;");
			layout_message.getChildren().add(label_message);
			layout_message.setAlignment(Pos.CENTER_RIGHT);

			HBox layout_message_right = new HBox();
			layout_message_right.getChildren().add(layout_message);
			layout_message_right.setStyle("-fx-max-width: Infinity; -fx-background-color: transparent; -fx-alignment: center-right;");

			contents.getChildren().add(layout_message_right);
		}
		else {
			Label label_username = new Label(username);
			label_username.setStyle("-fx-max-width: Infinity; -fx-background-radius: 5px 5px 0px 0px; -fx-background-color: rgb(80,80,80);");

			Label label_message= new Label(message);
			label_message.setStyle("-fx-max-width: Infinity; -fx-font-size: 14px; -fx-background-radius: 0px 0px 5px 5px; -fx-background-color: rgb(60,60,60);");

			VBox layout_message = new VBox(0);
			layout_message.setStyle("-fx-min-width: 500px; -fx-max-width: 500px;");
			layout_message.getChildren().addAll(label_username, label_message);
			contents.getChildren().add(layout_message);
		}
	}
/**
 * Add GUI Contact Element for the Contact Screen
 *
 * @param contents VBox of where to insert the contacts
 * @param layout_message_area VBox of the messages to show/hide
 * @param field_message TextArea of the messages to show/hide
 * @param button_attach_file Button to show/hide
 * @param scroll_messages ScrollPane of the messages to show/hide
 * @param label_address Label to change on contact selected
 * @param roster Roster roster to update the contacts with
 */
	private void guiUpdateContacts(VBox contents, VBox layout_message_area, TextArea field_message, Button button_attach_file, ScrollPane scroll_messages, Label label_address, Roster roster) {
		contents.getChildren().clear();

		for (RosterEntry entry : roster.getEntries()) {
			Presence presence = roster.getPresence(entry.getJid());
			String user_type = presence.getType().toString();
			String user_status = presence.getMode().toString();
			String status_message = presence.getStatus() != null ? presence.getStatus() : "Sin mensaje de status/presencia.";
			String user_id = entry.getJid().toString().replace(user_domain, "");
	
			Label label_jid = new Label(user_id);
			label_jid.setStyle("-fx-max-width: Infinity;");

			Label label_message = new Label(status_message);
			label_message.setStyle("-fx-max-width: Infinity; -fx-font-size: 12px;");
			label_message.setAlignment(Pos.CENTER_RIGHT);

			Label label_status = new Label(user_status);
			if ("available".equals(user_type)) {
				if ("available".equals(user_status)) {
					label_status.setStyle("-fx-text-fill: rgb(100,250,100);");
				} else if ("away".equals(user_status)) {
					label_status.setStyle("-fx-text-fill: rgb(250,250,100);");
				} else {
					label_status.setStyle("-fx-text-fill: rgb(100,200,250);");
				}
			}
			else {
				label_status.setText("unavailable");
				label_message.setText("");
				label_status.setStyle("-fx-text-fill: rgb(250,100,100);");
			}

			Button button_message = new Button();
			button_message.setText("✉");
			button_message.setStyle("-fx-max-height: Infinity; -fx-background-color: rgb(50,100,50); -fx-font-size: 28px;");
			button_message.setOnMouseEntered(e -> 
			button_message.setStyle(
					"-fx-max-height: Infinity; -fx-background-color: rgb(75,150,75); -fx-font-size: 28px;"
				)
			);
			button_message.setOnMouseExited(e -> 
			button_message.setStyle(
					"-fx-max-height: Infinity; -fx-background-color: rgb(50,100,50); -fx-font-size: 28px;"
				)
			);
	
			HBox layout_contact_sub_sub = new HBox(5);
			layout_contact_sub_sub.getChildren().addAll(label_jid, label_status);
			HBox.setHgrow(label_jid, Priority.ALWAYS);
	
			VBox layout_contact_sub = new VBox(5);
			layout_contact_sub.getChildren().add(layout_contact_sub_sub);
			if ("available".equals(user_type)) {
				layout_contact_sub.getChildren().addAll(label_message);
			}
			layout_contact_sub.setStyle("-fx-max-width: Infinity;");
			
			HBox layout_contact = new HBox(5);
			layout_contact.setPadding(new Insets(10));
			layout_contact.getChildren().add(layout_contact_sub);
			if ("available".equals(user_status) && "available".equals(user_type)) {
				layout_contact.getChildren().add(button_message);
			}
			HBox.setHgrow(layout_contact_sub, Priority.ALWAYS);
			layout_contact.setStyle("-fx-background-color: rgb(50,50,50); -fx-background-radius: 5px;");
	
			contents.getChildren().add(layout_contact);

			button_message.setOnAction(event -> {
				layout_message_area.getChildren().clear();
				field_message.setVisible(true);
				button_attach_file.setVisible(true);
				scroll_messages.setVisible(true);
				label_address.setText(user_id);

				for (Pair<String,String> message: chatMessages) {
					if (message.getKey().equals(user_id)  || message.getKey().equals(username)) {
							guiAddIncomingMessage(message.getKey(), message.getValue(), layout_message_area);
					}
					else {
						System.out.println("Ignored Message from: [ " + message.getKey() + " ] != [ " + user_id + " ]  |  " + message.getValue());
					}
				}

				chatMessages.addListener((ListChangeListener<Pair<String, String>>) change -> {
					layout_message_area.getChildren().clear();
					for (Pair<String,String> message: chatMessages) {
						if (message.getKey().equals(user_id) || message.getKey().equals(username)) {
								guiAddIncomingMessage(message.getKey(), message.getValue(), layout_message_area);
						}
						else {
							System.out.println("Ignored Message from: [ " + message.getKey() + " ] != [ " + user_id + " ]  |  " + message.getValue());
						}
					}
				});
			});
		}
	}
/**
 * Add GUI Chat Element for Notifications
 *
 * @param value String text to add
 */
	private void addNotification(String value) {
		Label label = new Label(value);
		layout_notifications.getChildren().add(label);
		if (!notifications_shown) {
			button_notifications.setStyle("-fx-background-color: rgb(50,150,150);");
		}
		else {
			PauseTransition pause = new PauseTransition(Duration.seconds(3));
			pause.setOnFinished(e -> {
				FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), label);
				fadeOut.setFromValue(1.0);
				fadeOut.setToValue(0.0);
				fadeOut.setOnFinished(ee -> {
					layout_notifications.getChildren().remove(label);
				});
				fadeOut.play();
			});
			pause.play();
		}
	}
/*-----------------------------

XMPP

-----------------------------*/
/**
 * Setup Listeners and Managers
 *
 */
	public void setup() {
		setContactListener();
		setStatus();
		chat_manager = ChatManager.getInstanceFor(xmpp_connection);
		multi_user_chat_manager = MultiUserChatManager.getInstanceFor(xmpp_connection);
		setupChatMessageListener();
	}
/**
 * SignUp to server
 *
 * @return boolean if succesful
 */
	public boolean signUp() {
		try {
			XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
				.setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
				.setUsernameAndPassword(username, password)
				.setXmppDomain("alumchat.lol")
				.setHost("alumchat.lol")
				.build();

			xmpp_connection = new XMPPTCPConnection(config);

			try {
				xmpp_connection.connect();
				AccountManager accountManager = AccountManager.getInstance(xmpp_connection);
				accountManager.sensitiveOperationOverInsecureConnection(true);

				if (accountManager.supportsAccountCreation()) {
					accountManager.sensitiveOperationOverInsecureConnection(true);
					accountManager.createAccount(Localpart.from(username), password);
					System.out.println("Signed up [ " + username + " ]");
					return true;
				}
				else {
					System.out.println("Server action does not support registration.");
					return false;
				}

			}
			catch (Exception e) {
				System.out.println("Error registering " + username + "  |  " + e.getMessage());
				e.printStackTrace();

				Alert alert = new Alert(AlertType.WARNING);
				
				alert.setTitle("Warning");
				alert.setHeaderText("Error");
				alert.setContentText("Error registering " + username + "  |  " + e.getMessage());
				alert.showAndWait();

				return false;
			}
		}
		catch (Exception e) {
			System.out.println("Error connecting to server " + domain + "  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.WARNING);
			
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error connecting to server " + domain + "  |  " + e.getMessage());
			alert.showAndWait();

			return false;
		}
	}
/**
 * SignIn to server
 *
 * @return boolean if succesful
 */
	public boolean signIn() {
		try {
			XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
				.setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
				.setUsernameAndPassword(username, password)
				.setXmppDomain("alumchat.lol")
				.setHost("alumchat.lol")
				.build();

			xmpp_connection = new XMPPTCPConnection(config);

			try {
				xmpp_connection.connect().login();
				System.out.println(username + " : Logged in");
				return true;
			}
			catch (Exception e) {
				System.out.println("Error signing in " + username + "  |  " + e.getMessage());
				e.printStackTrace();

				Alert alert = new Alert(AlertType.WARNING);
				
				alert.setTitle("Warning");
				alert.setHeaderText("Error");
				alert.setContentText("Error signing in " + username + "  |  " + e.getMessage());
				alert.showAndWait();

				return false;
			}
		}
		catch (Exception e) {
			System.out.println("Error connecting to server " + domain + "  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.WARNING);
			
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error connecting to server " + domain + "  |  " + e.getMessage());
			alert.showAndWait();

			return false;
		}
	}
/**
 * SignOut from server
 *
 * @return boolean if succesful
 */
	public boolean signOut() {
		try {
			xmpp_connection.disconnect();
			return true;
		}
		catch (Exception e) {
			System.out.println("Error signing out  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.WARNING);
			
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error signing out  |  " + e.getMessage());
			alert.showAndWait();

			return false;
		}
	}
/**
 * Delete Account from server
 *
 * @return boolean if succesful
 */
	public boolean deleteAccount() {
		try {
			AccountManager accountManager = AccountManager.getInstance(xmpp_connection);
			accountManager.deleteAccount();
			xmpp_connection.disconnect();
			System.out.println("Account deleted succesfully [ " + username + user_domain + " ]");
			return true;
		}
		catch (Exception e) {
			System.out.println("Error deleting account [ " + username + user_domain + " ]  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.WARNING);
			
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error deleting account [ " + username + user_domain + " ]  |  " + e.getMessage());
			alert.showAndWait();

			return false;
		}
	}
/**
 * Set Status Message for the user to the server
 *
 * @return boolean if succesful
 */
	public boolean setStatus() {
		try {
			Presence presence = PresenceBuilder.buildPresence()
			.ofType(Type.available)
			.setMode(status)
			.setStatus(status_message)
			.build();
			xmpp_connection.sendStanza(presence);
			System.out.println("Status set [ " + status.toString() + " ] | [ " + status_message + " ]");
			return true;
		}
		catch (Exception e) {
			System.out.println("Error defining status message [ " + status_message + " ] for [ " + username + user_domain + " ]  |  " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
/**
 * Send Chat message to a user
 *
 * @param to_user_jid String of target user
 * @param message_body String of the message to send
 * @return boolean if succesful
 * @deprecated org.jivesoftware.smack.packet.Message is deprecated
 */
	public boolean sendChatMessage(String to_user_jid, String message_body) {
		try {
			ChatManager manager = ChatManager.getInstanceFor(xmpp_connection);
			EntityBareJid jid = JidCreate.entityBareFrom(to_user_jid);
			Chat chat = manager.chatWith(jid);

			Message message = new Message(jid, Message.Type.chat);
			message.setBody(message_body);
			chat.send(message);

			System.out.println("Menssage [ " + message_body + " ] sent to [ " + to_user_jid + " ] by [ " + username + user_domain + " ]");
			return true;
		}
		catch (Exception e) {
			System.out.println("Error sending message to [ " + to_user_jid + " ] by [ " + username + user_domain+ " ]  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.WARNING);
			
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error sending message to [ " + to_user_jid + "] by [ " + username + user_domain + " ]  |  " + e.getMessage());
			alert.showAndWait();

			return false;
		}
	}
/**
 * Set and connect Chat Message Listener
 *
 */
	private void setupChatMessageListener() {
		chat_manager.addIncomingListener((from, message, chat) -> {
			Platform.runLater(() -> {
				if (message.getBody().length() > 1024) {
					addNotification("New FILE From [ " + from.toString() + " ]");
					receiveFile(message.getBody());
				}
				else {
					chatMessages.add(new Pair<String,String>(from.toString(), message.getBody()));
					addNotification("New Message From [ " + from.toString() + " ]  |  " + message.getBody());
				}
			});
		});
	}
/**
 * Join a chat room
 *
 * @param room_jid String of room ID
 * @return boolean if succesful
 */
	private boolean joinRoom(String room_jid) {
		try {
			EntityBareJid roomJid = JidCreate.entityBareFrom(room_jid);
			multi_user_chat = multi_user_chat_manager.getMultiUserChat(roomJid);
			multi_user_chat.join(Resourcepart.from(nickname));

			multi_user_chat.addParticipantStatusListener(new ParticipantStatusListener() {
				@Override
				public void joined(EntityFullJid participant) {
					System.out.println("[ " + participant + " ] joined the chat");
					Platform.runLater(() -> {
						addNotification(participant + " joined the group chat [ " + room_jid + " ]");
					});
				}

				@Override
				public void left(EntityFullJid participant) {
					System.out.println("[ " + participant + " ] left the chat");
					Platform.runLater(() -> {
						addNotification(participant + " left the group chat [ " + room_jid + " ]");
					});
				}
			});
			System.out.println("You joined the chatroom [ " + room_jid + " ]");
			return true;
		}
		catch (Exception e) {
			System.out.println("Error joining chatroom [ " + room_jid + " ]  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.CONFIRMATION);
			
			alert.setTitle("Confirmation");
			alert.setHeaderText("Confirm your action");
			alert.setContentText("Chatroom does not exist. Do you wish to create it?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				return createRoom(room_jid);
			}
			return false;
		}
	}
/**
 * Create a chat room
 *
 * @param room_jid String of room ID
 * @return boolean if succesful
 */
	private boolean createRoom(String room_jid) {
		try {
			EntityBareJid roomJid = JidCreate.entityBareFrom(room_jid);
			multi_user_chat = multi_user_chat_manager.getMultiUserChat(roomJid);
			multi_user_chat.createOrJoin(Resourcepart.from(nickname));

			Form form = multi_user_chat.getConfigurationForm();
			FillableForm submitForm = form.getFillableForm();
			submitForm.setAnswer("muc#roomconfig_publicroom", true);
			submitForm.setAnswer("muc#roomconfig_persistentroom", true);
			submitForm.setAnswer("muc#roomconfig_membersonly", false);
			submitForm.setAnswer("muc#roomconfig_moderatedroom", false);
			submitForm.setAnswer("muc#roomconfig_whois", "anyone");
			multi_user_chat.sendConfigurationForm(submitForm);

			multi_user_chat.addParticipantStatusListener(new ParticipantStatusListener() {
				@Override
				public void joined(EntityFullJid participant) {
					System.out.println("[ " + participant + " ] joined the chat");

					Platform.runLater(() -> {
						addNotification(participant + " joined the group chat [ " + room_jid + " ]");
					});
				}

				@Override
				public void left(EntityFullJid participant) {
					System.out.println("[ " + participant + " ] left the group chat [ ");

					Platform.runLater(() -> {
						addNotification(participant + " left the group chat [ " + room_jid + " ]");
					});
				}
			});
			System.out.println("Created and joined chatroom [ " + room_jid + " ]");
			return true;
		}
		catch (Exception e) {
			System.out.println("Error creating chatroom [ " + room_jid + " ]  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.WARNING);
			
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error creating chatroom [ " + room_jid + " ]  |  " + e.getMessage());
			alert.showAndWait();

			return false;
		}
	}
/**
 * Delete a chat room
 *
 * @param room_jid String of room ID
 * @return boolean if succesful
 */
	public boolean deleteRoom(String room_jid) {
		try {
			EntityBareJid roomJid = JidCreate.entityBareFrom(room_jid);
			multi_user_chat = multi_user_chat_manager.getMultiUserChat(roomJid);
			multi_user_chat.destroy("Room deleted by admin ", null);

			System.out.println("Chatroom deleted [ " + room_jid + " ]");
			return true;
		}
		catch (Exception e) {
			System.out.println("Error deleting chatroom [ " + room_jid + " ]  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.WARNING);
			
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error deleteing chatroom " + room_jid + "  |  " + e.getMessage());
			alert.showAndWait();

			return false;
		}
	}
/**
 * Send message to a chat room
 *
 * @param room_jid String of room ID
 * @param message_body String message
 * @return boolean if succesful
 */
	public boolean sendRoomMessage(String to_room_jid, String message_body) {
		try {
			multi_user_chat.sendMessage(message_body);
			System.out.println("Message [ " + message_body + " ] sent to chatroom [ " + to_room_jid + " ] by [ " + username + " ]");
			return true;
		}
		catch (Exception e) {
			System.out.println("Error sending message to chatroom [ " + to_room_jid + " ]  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.WARNING);
			
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error sending message to chatroom [ " + to_room_jid + " ]  |  " + e.getMessage());
			alert.showAndWait();

			return false;
		}
	}
/**
 * Set and connect Chat Room Listener
 *
 */
	private void setupRoomMessageListener() {
		if (room_chat_listeners.containsKey(multi_user_chat.getRoom().toString())) {
			multi_user_chat.removeMessageListener(room_chat_listeners.get(multi_user_chat.getRoom().toString()));
		}
		MessageListener newListener = message -> {
			Platform.runLater(() -> {
				if (message.getBody() != null && message.getFrom().getResourceOrEmpty().toString() != null) {
					if (message.getBody().length() > 1024) {
						addNotification("New FILE From [ " + message.getFrom().getResourceOrEmpty().toString() + " ]");
						receiveFile(message.getBody());
					}
					else {
						roomMessages.add(new Pair<String,String>(message.getFrom().getResourceOrEmpty().toString(), message.getBody()));
						addNotification("New Group Message [ " + multi_user_chat.getRoom().toString() + " ] from [ " + message.getFrom().getResourceOrEmpty().toString() + " ]  |  " +  message.getBody());
					}
				}
			});
		};
		multi_user_chat.addMessageListener(newListener);
		room_chat_listeners.put(multi_user_chat.getRoom().toString(), newListener);
	}
/**
 * Set and connect Contact Listener
 *
 * @param contents VBox of where to insert the contacts
 * @param layout_message_area VBox of the messages to show/hide
 * @param field_message TextArea of the messages to show/hide
 * @param button_attach_file Button to show/hide
 * @param scroll_messages ScrollPane of the messages to show/hide
 * @param label_address Label to change on contact selected
 */
	private void getContacts(VBox contents, VBox layout_message_area, TextArea field_message, Button button_attach_file, ScrollPane scroll_messages, Label label_address) {
		Roster roster = Roster.getInstanceFor(xmpp_connection);
		roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

		roster.addRosterListener(new RosterListener() {
			@Override
			public void entriesAdded(Collection<Jid> addresses) {
				Platform.runLater(() -> guiUpdateContacts(contents, layout_message_area, field_message, button_attach_file,scroll_messages, label_address, roster));
			}
			@Override
			public void entriesUpdated(Collection<Jid> addresses) {
				Platform.runLater(() -> guiUpdateContacts(contents, layout_message_area, field_message, button_attach_file,scroll_messages, label_address, roster));
			}
			@Override
			public void entriesDeleted(Collection<Jid> addresses) {
				Platform.runLater(() -> guiUpdateContacts(contents, layout_message_area, field_message, button_attach_file, scroll_messages, label_address, roster));
			}
			@Override
			public void presenceChanged(Presence presence) {
				Platform.runLater(() -> guiUpdateContacts(contents, layout_message_area, field_message, button_attach_file, scroll_messages, label_address, roster));
			}
		});
		guiUpdateContacts(contents, layout_message_area, field_message, button_attach_file,scroll_messages, label_address, roster);
	}
/**
 * Set and connect incoming Contact Request Listener
 *
 */
	private void setContactListener() {
		xmpp_connection.addAsyncStanzaListener(new StanzaListener() {
			@Override
			public void processStanza(Stanza packet) {
				Presence presence = (Presence) packet;
				if (presence.getType() == Presence.Type.subscribe) {
					Platform.runLater(() -> {
						try {
							Presence subscribedPresence = PresenceBuilder.buildPresence()
								.ofType(Presence.Type.subscribed)
								.to(presence.getFrom())
								.build();
							xmpp_connection.sendStanza(subscribedPresence);
							addContact(presence.getFrom().asBareJid().toString());
							System.out.println("Accepted Contact Request [ " + presence.getFrom().asBareJid().toString() + " ]");

							addNotification("Accepted Contact Request [ " + presence.getFrom().asBareJid().toString() + " ]");
						}
						catch (Exception e) {
							System.out.println("Error accepting contact request from [ " + presence.getFrom().asBareJid().toString() + " ]  |  " + e.getMessage());
							e.printStackTrace();
							
							Alert alert = new Alert(AlertType.WARNING);
							alert.setTitle("Warning");
							alert.setHeaderText("Error");
							alert.setContentText("Error accepting contact request from [ " + presence.getFrom().asBareJid().toString() + " ]  |  " + e.getMessage());
							alert.showAndWait();
						}
					});
				}
			}
		}, new StanzaTypeFilter(Presence.class));
	}
/**
 * Add contact
 *
 * @param user_jid String of user ID
 * @return boolean if succesful
 */
	public boolean addContact(String user_jid) {
		try {
			Roster roster = Roster.getInstanceFor(xmpp_connection);
			EntityBareJid jid = JidCreate.entityBareFrom(user_jid);
			if (!roster.contains(jid)) {
				Presence subscribe = PresenceBuilder.buildPresence()
					.ofType(Presence.Type.subscribe)
					.to(jid)
					.build();

				xmpp_connection.sendStanza(subscribe);
				System.out.println("Added Contact  [ " + user_jid + " ]");
			}
			return true;
		}
		catch (Exception e) {
			System.out.println("Error adding contact [ " + user_jid + " ]  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error adding contact [ " + user_jid + " ]  |  " + e.getMessage());
			alert.showAndWait();

			return false;
		}
	}
/**
 * Remove contact
 *
 * @param user_jid String of user ID
 * @return boolean if succesful
 */
	public boolean removeContact(String user_jid) {
		try {
			EntityBareJid jid = JidCreate.entityBareFrom(user_jid);
			Roster roster = Roster.getInstanceFor(xmpp_connection);
			roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
			RosterEntry entry = roster.getEntry(jid);
			roster.removeEntry(entry);
			System.out.println("Deleted contact [ " + user_jid + " ]");
			return true;
		}
		catch (Exception e) {
			System.out.println("Error deleting contact [ " + user_jid + " ]  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error deleting contact [ " + user_jid + " ]  |  " + e.getMessage());
			alert.showAndWait();

			return false;
		}
	}
/**
 * Send file to user/chat
 *
 * @param to_jid String of target ID
 * @param file File
 */
	public void sendFile(String to_jid, File file) {
		try {
			byte[] fileBytes = Files.readAllBytes(file.toPath());
			String encodedFile = Base64.getEncoder().encodeToString(fileBytes);
			sendChatMessage(to_jid, encodedFile);

			System.out.println("File [ " + file.getAbsolutePath() + " ] sent to [ " + to_jid + " ]");
		}
		catch (Exception e) {
			System.out.println("Error sending file [ " + file.getAbsolutePath() + " ] to [ " + to_jid + " ]  |  " + e.getMessage());
			e.printStackTrace();
			
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error sending file [ " + file.getAbsolutePath() + " ] to [ " + to_jid + " ]  |  " + e.getMessage());
			alert.showAndWait();
		}
	}
/**
 * Receive file
 *
 * @param file_content String of the encoded file
 */
	public void receiveFile(String file_content) {
		try {
			byte[] decodedBytes = Base64.getDecoder().decode(file_content);
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Save file to:");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int userSelection = fileChooser.showSaveDialog(null);

			if (userSelection == JFileChooser.APPROVE_OPTION) {
				File fileToSave = fileChooser.getSelectedFile();
				Path outputPath = fileToSave.toPath();

				Files.createDirectories(outputPath.getParent());
				Files.write(outputPath, decodedBytes);

				System.out.println("File saved to [ " + outputPath.toString() + " ]");
			}
			else {
				System.out.println("File discarded.");
			}

		}
		catch (Exception e) {
			System.out.println("Error al receiving/saving file  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error al receiving/saving file  |  " + e.getMessage());
			alert.showAndWait();
		}
	}
	public static void main(String[] args) {
		launch(args);
	}
}