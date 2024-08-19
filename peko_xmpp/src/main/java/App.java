

// Java FX
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.application.*;
import javafx.collections.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.util.*;

// Smack Lib
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.*;

import org.jivesoftware.smack.util.dns.minidns.MiniDnsResolver;
import org.jivesoftware.smack.util.DNSUtil;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.roster.*;
import org.jivesoftware.smack.chat2.*;
import org.jivesoftware.smack.tcp.*;
import org.jivesoftware.smack.*;

// XMPP Lib
import org.jxmpp.jid.parts.*;
import org.jxmpp.jid.impl.*;
import org.jxmpp.jid.*;

// Java
import java.util.*;
import java.io.*;

public class App extends Application {
	private AbstractXMPPConnection xmpp_connection;
	private MultiUserChatManager multi_user_chat_manager;
	private MultiUserChat multi_user_chat;
	private ChatManager chat_manager;


	private ObservableList<Pair<String,String>> chatMessages = FXCollections.observableArrayList();
	private ObservableList<Pair<String,String>> roomMessages = FXCollections.observableArrayList();
	private List<IncomingChatMessageListener> chat_listeners = new ArrayList<>();
	private List<MessageListener> room_chat_listeners = new ArrayList<>();

	private String user_domain = "@alumchat.lol";
	private String room_domain = "@conference.alumchat.lol";

	private String domain = "alumchat.lol";
	private String group_id = "grupeko";
	private String username = "mar21430-test";
	private String nickname = "Pekoyo";
	private String password = "Test_123";

	@Override
	public void start(Stage stage) throws IOException {
		stage.setTitle("XMPP Chat");

		StackPane root = new StackPane();

		Scene scene = new Scene(root, 1600, 950);
		scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
		scene.setRoot(root);

		stage.setScene(scene);
		stage.show();

		guiLoginScreen(scene);
	}
/*-----------------------------

GUI

-----------------------------*/
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

		scene.setRoot(root);

		button_signin.setOnAction(event -> {
			username = field_username.getText();
			password = field_password.getText();
			if (signIn(username, password)) {
				setup();
				guiHomeScreen(scene);
			}
			//guiHomeScreen(scene);
		});

		button_signup.setOnAction(event -> {
			username = field_username.getText();
			password = field_password.getText();
			if (signUp(username, password)) {
				setup();
				guiHomeScreen(scene);
			}
		});
	}

	private void guiHomeScreen(Scene scene) {
		Label label = new Label("Welcome " + username + "  |  " + nickname);
		label.setStyle("-fx-max-width: Infinity; -fx-font-size: 20px;");

		Button button_logout = new Button("Log Out");

		HBox layout_header = new HBox(10);
		layout_header.getChildren().addAll(label, button_logout);
		HBox.setHgrow(label, Priority.ALWAYS);

		Button button_chat = new Button("Chat");
		button_chat.setStyle("-fx-max-width: Infinity;");

		Button button_group_chat = new Button("Group Chat");
		button_group_chat.setStyle("-fx-max-width: Infinity;");

		Button button_account = new Button("My Account");
		button_account.setStyle("-fx-max-width: Infinity;");

		VBox layout_main = new VBox(10);
		layout_main.setAlignment(Pos.TOP_CENTER);

		HBox layout_menu = new HBox(10);
		layout_menu.setAlignment(Pos.CENTER);
		layout_menu.getChildren().addAll(button_chat,button_group_chat, button_account);
		HBox.setHgrow(button_chat, Priority.ALWAYS);
		HBox.setHgrow(button_group_chat, Priority.ALWAYS);
		HBox.setHgrow(button_account, Priority.ALWAYS);

		VBox layout_container = new VBox(10);
		layout_container.setPadding(new Insets(10));
		layout_container.setAlignment(Pos.TOP_CENTER);
		layout_container.getChildren().add(layout_header);
		layout_container.getChildren().add(layout_main);
		layout_container.getChildren().add(layout_menu);
		VBox.setVgrow(layout_main, Priority.ALWAYS);

		StackPane root = new StackPane();
		root.getChildren().add(layout_container);

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

		guiChatScreen(scene, layout_main, label);
	}

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
		VBox.setVgrow(scroll_contacts, Priority.ALWAYS);
//
		Label label_messages = new Label("Chat");
		label_messages.setStyle("-fx-max-width: Infinity;");

		TextField field_user_jid = new TextField(username);
		field_user_jid.setPromptText("Chat to user with JID...");
		field_user_jid.setStyle("-fx-max-width: Infinity;");
		field_user_jid.setAlignment(Pos.CENTER_RIGHT);

		Label label_address = new Label(user_domain);
		label_address.setStyle("-fx-max-width: Infinity; -fx-max-height: Infinity;");

		Button button_join_chat = new Button("Load Chat");
		button_join_chat.setStyle("-fx-max-width: Infinity;");

		VBox layout_message_area = new VBox(10);
		layout_message_area.setPadding(new Insets(10));
		ScrollPane scroll_messages = new ScrollPane(layout_message_area);
		scroll_messages.setStyle("-fx-max-height: Infinity; fx-max-width: Infinity;");
		scroll_messages.setFitToWidth(true);
		scroll_messages.setFitToHeight(true);

		TextArea field_message = new TextArea();
		field_message.setStyle("-fx-max-height: Infinity;");
		field_message.setPromptText("Message...");
		field_message.setVisible(false);

		HBox layout_message_header = new HBox(10);
		layout_message_header.getChildren().addAll(field_user_jid, label_address, button_join_chat);
		HBox.setHgrow(field_user_jid, Priority.ALWAYS);

		VBox layout_message = new VBox(10);
		layout_message.getChildren().addAll(label_messages, layout_message_header, scroll_messages, field_message);
		layout_message.setStyle("-fx-pref-width: 800px;");
		VBox.setVgrow(scroll_messages, Priority.ALWAYS);
//
		Label label_users = new Label("Connected Users");
		label_users.setStyle("-fx-max-width: Infinity;");

		VBox layout_user_list_content = new VBox(10);
		layout_user_list_content.setPadding(new Insets(10));
		ScrollPane scroll_users = new ScrollPane(layout_user_list_content);
		scroll_users.setStyle("-fx-max-height: Infinity; -fx-max-width: Infinity; -fx-min-width:200px;");
		scroll_users.setFitToWidth(true);
		scroll_users.setFitToHeight(true);

		VBox layout_users = new VBox(10);
		layout_users.getChildren().addAll(label_users, scroll_users);
		VBox.setVgrow(scroll_users, Priority.ALWAYS);
//
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(layout_contacts, layout_message, layout_users);
		HBox.setHgrow(layout_contacts, Priority.ALWAYS);
		HBox.setHgrow(layout_message, Priority.ALWAYS);
		HBox.setHgrow(layout_users, Priority.ALWAYS);

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

		button_join_chat.setOnAction(event -> {
			layout_message_area.getChildren().clear();
			field_message.setVisible(true);

			for (Pair<String,String> message: chatMessages) {
				if (message.getKey().equals(field_user_jid.getText() + user_domain)  || message.getKey().equals(username)) {
						guiAddIncomingMessage(message.getKey(), message.getValue(), layout_message_area);
				}
				else {
					System.out.println("Ignored Message from: [ " + message.getKey() + " ] != [ " + field_user_jid.getText() + user_domain + " ]  |  " + message.getValue());
				}
			}

			chatMessages.addListener((ListChangeListener<Pair<String, String>>) change -> {
				layout_message_area.getChildren().clear();
				for (Pair<String,String> message: chatMessages) {
					if (message.getKey().equals(field_user_jid.getText() + user_domain) || message.getKey().equals(username)) {
							guiAddIncomingMessage(message.getKey(), message.getValue(), layout_message_area);
					}
					else {
						System.out.println("Ignored Message from: [ " + message.getKey() + " ] != [ " + field_user_jid.getText() + user_domain + " ]  |  " + message.getValue());
					}
				}
			});
		});

		field_message.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER) {
				sendChatMessage(field_user_jid.getText(), field_message.getText());
				Platform.runLater(() -> {
					chatMessages.add(new Pair<String,String>(username, field_message.getText()));
				});
				event.consume();
			}
		});

		getConnectedUsers(layout_user_list_content);
		getContacts(layout_contacts_list_content);
	}

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
				"-fx-max-width: Infinity; "
				+ "-fx-background-color: rgb(250,100,100); "
			)
		);
		button_delete_room.setOnMouseExited(e -> 
			button_delete_room.setStyle(
				"-fx-max-width: Infinity; "
				+ "-fx-background-color: rgb(100,50,50); "
			)
		);
		VBox layout_message_area = new VBox(10);
		layout_message_area.setPadding(new Insets(10));
		ScrollPane scroll_messages = new ScrollPane(layout_message_area);
		scroll_messages.setStyle("-fx-max-height: Infinity; fx-max-width: Infinity;");
		scroll_messages.setFitToWidth(true);
		scroll_messages.setFitToHeight(true);

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
				layout_message_area.getChildren().clear();
			}
		});

		field_message.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER) {
				sendRoomMessage(field_room_jid.getText() + room_domain, field_message.getText());
				event.consume();
			}
		});
	}

	private void guiAccountScreen(Scene scene, VBox container, Label label) {
		container.getChildren().clear();
		label.setText("My Account  |  " + username + "  |  " + nickname);
//
		Label label_presence = new Label("Status Message");
		label_presence.setStyle("-fx-max-width: Infinity;");

		TextField field_presence = new TextField();
		field_presence.setPromptText("Status Message...");
		field_presence.setStyle("-fx-max-width: Infinity;");

		Button button_set_presence = new Button("Update");

		HBox layout_presence_set = new HBox(10);
		layout_presence_set.getChildren().addAll(field_presence, button_set_presence);
		HBox.setHgrow(field_presence, Priority.ALWAYS);

		Label label_nickname = new Label("Nickname");
		label_nickname.setStyle("-fx-max-width: Infinity;");

		TextField field_nickname = new TextField(nickname);
		field_nickname.setPromptText("Nickname...");
		field_nickname.setStyle("-fx-max-width: Infinity;");

		Button button_set_nickname = new Button("Update");

		HBox layout_nickname_set = new HBox(10);
		layout_nickname_set.getChildren().addAll(field_nickname, button_set_nickname);
		HBox.setHgrow(field_nickname, Priority.ALWAYS);

		VBox layout_account_settings = new VBox(10);
		layout_account_settings.getChildren().addAll(label_presence, layout_presence_set, label_nickname, layout_nickname_set);
//
		Button button_close_account = new Button("Delete Account");
		button_close_account.setStyle("-fx-max-width: Infinity; -fx-background-color: rgb(100,50,50);");
		
		button_close_account.setOnMouseEntered(e -> 
		button_close_account.setStyle(
				"-fx-max-width: Infinity; "
				+ "-fx-background-color: rgb(250,100,100); "
			)
		);
		button_close_account.setOnMouseExited(e -> 
		button_close_account.setStyle(
				"-fx-max-width: Infinity; "
				+ "-fx-background-color: rgb(100,50,50); "
			)
		);
//
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(layout_account_settings);
		HBox.setHgrow(layout_account_settings, Priority.ALWAYS);

		container.getChildren().addAll(hbox, button_close_account);
		VBox.setVgrow(hbox, Priority.ALWAYS);

		button_set_presence.setOnAction(event -> {
			definePresence(username, field_presence.getText());
		});

		button_set_nickname.setOnAction(event -> {
			nickname = field_nickname.getText();
			label.setText("My Account  |  " + username + "  |  " + nickname);
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

		Roster roster = Roster.getInstanceFor(xmpp_connection);
		Presence presence_message = roster.getPresence(xmpp_connection.getUser().asBareJid());
		field_presence.setText(presence_message.getStatus());
	}

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

	private void guiUpdateConnectedUsers(VBox contents, Roster roster) {
		contents.getChildren().clear();

		for (RosterEntry entry : roster.getEntries()) {
			Presence presence = roster.getPresence(entry.getJid());
			String user_status = presence.isAvailable() ? "Conectado" : "Desconectado";
			String status_message = presence.getStatus() != null ? presence.getStatus() : "Sin mensaje de status/presencia.";
	
			Label label_jid = new Label(entry.getJid().toString());
			label_jid.setStyle("-fx-max-width: Infinity;");
			
			Label label_status = new Label(user_status);
			if ("Conectado".equals(user_status)) {
				label_status.setStyle("-fx-text-fill: rgb(100,250,100);");
			} else {
				label_status.setStyle("-fx-text-fill: rgb(250,100,100);");
			}
			
			Label label_message = new Label(status_message);
			label_message.setStyle("-fx-max-width: Infinity; -fx-font-size: 12px;");
			label_message.setAlignment(Pos.CENTER_RIGHT);
	
			HBox layout_contact_sub = new HBox(5);
			layout_contact_sub.getChildren().addAll(label_jid, label_status);
			HBox.setHgrow(label_jid, Priority.ALWAYS);
	
			VBox layout_contact = new VBox(5);
			layout_contact.setPadding(new Insets(10));
			layout_contact.getChildren().addAll(layout_contact_sub, label_message);
			layout_contact.setStyle("-fx-background-color: rgb(50,50,50); -fx-background-radius: 5px;");
	
			contents.getChildren().add(layout_contact);
		}
	}

	private void guiUpdateContacts(VBox contents, Roster roster) {
		contents.getChildren().clear();

		for (RosterEntry entry : roster.getEntries()) {
			Presence presence = roster.getPresence(entry.getJid());
			String user_status = presence.isAvailable() ? "Conectado" : "Desconectado";
			String status_message = presence.getStatus() != null ? presence.getStatus() : "Sin mensaje de status/presencia.";
	
			Label label_jid = new Label(entry.getJid().toString());
			label_jid.setStyle("-fx-max-width: Infinity;");
			
			Label label_status = new Label(user_status);
			if ("Conectado".equals(user_status)) {
				label_status.setStyle("-fx-text-fill: rgb(100,250,100);");
			} else {
				label_status.setStyle("-fx-text-fill: rgb(250,100,100);");
			}
			
			Label label_message = new Label(status_message);
			label_message.setStyle("-fx-max-width: Infinity; -fx-font-size: 12px;");
			label_message.setAlignment(Pos.CENTER_RIGHT);
	
			HBox layout_contact_sub = new HBox(5);
			layout_contact_sub.getChildren().addAll(label_jid, label_status);
			HBox.setHgrow(label_jid, Priority.ALWAYS);
	
			VBox layout_contact = new VBox(5);
			layout_contact.setPadding(new Insets(10));
			layout_contact.getChildren().addAll(layout_contact_sub, label_message);
			layout_contact.setStyle("-fx-background-color: rgb(50,50,50); -fx-background-radius: 5px;");
	
			contents.getChildren().add(layout_contact);
		}
	}
/*-----------------------------

XMPP

-----------------------------*/
	public void setup() {
		chat_manager = ChatManager.getInstanceFor(xmpp_connection);
		multi_user_chat_manager = MultiUserChatManager.getInstanceFor(xmpp_connection);
		setupChatMessageListener();
	}

	public boolean signUp(String username, String password) {
		try {
			DNSUtil.setDNSResolver(MiniDnsResolver.getInstance());
			XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
				.setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
				.setXmppDomain(domain)
				.setHost(domain)
				.setPort(5222)
				.build();
			xmpp_connection = new XMPPTCPConnection(config);

			try {
				xmpp_connection.connect();
				AccountManager accountManager = AccountManager.getInstance(xmpp_connection);
				accountManager.sensitiveOperationOverInsecureConnection(true);

				if (accountManager.supportsAccountCreation()) {
					Localpart localpart = Localpart.from(username);
					accountManager.createAccount(localpart, password);
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

	public boolean signIn(String username, String password) {
		try {
			DNSUtil.setDNSResolver(MiniDnsResolver.getInstance());
			XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
				.setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
				.setXmppDomain(domain)
				.setHost(domain)
				.setPort(5222)
				.build();
			xmpp_connection = new XMPPTCPConnection(config);

			try {
				xmpp_connection.connect();
				xmpp_connection.login(username, password);
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

	public boolean definePresence(String username, String message) {
		try {
			Presence presence = new Presence(Presence.Type.available);
			presence.setStatus(message);
			xmpp_connection.sendStanza(presence);
			System.out.println("Status message set [ " + message + " ]");
			return true;
		}
		catch (Exception e) {
			System.out.println("Error defining status message [ " + message + " ] for [ " + username + user_domain + " ]  |  " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public boolean sendChatMessage(String to_user_jid, String message_body) {
		try {
			ChatManager manager = ChatManager.getInstanceFor(xmpp_connection);
			EntityBareJid jid = JidCreate.entityBareFrom(to_user_jid + user_domain);
			Chat chat = manager.chatWith(jid);

			Message message = new Message(jid, Message.Type.chat);
			message.setBody(message_body);
			chat.send(message);

			System.out.println("Menssage [ " + message_body + " ] sent to [ " + to_user_jid + user_domain + " ] by [ " + username + user_domain + " ]");
			return true;
		}
		catch (Exception e) {
			System.out.println("Error sending message to [ " + to_user_jid + user_domain + " ] by [ " + username + user_domain+ " ]  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.WARNING);
			
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error sending message to [ " + to_user_jid + user_domain + "] by [ " + username + user_domain + " ]  |  " + e.getMessage());
			alert.showAndWait();

			return false;
		}
	}

	private void setupChatMessageListener() {
		for (IncomingChatMessageListener listener : chat_listeners) {
			chat_manager.removeIncomingListener(listener);
		}
		chat_listeners.clear();
		IncomingChatMessageListener newListener = (from, message, chat) -> {
			Platform.runLater(() -> {
				chatMessages.add(new Pair<String,String>(from.toString(), message.getBody()));
			});
		};
		chat_manager.addIncomingListener(newListener);
		chat_listeners.add(newListener);
	}

	private boolean joinRoom(String room_jid) {
		try {
			EntityBareJid roomJid = JidCreate.entityBareFrom(room_jid);
			multi_user_chat = multi_user_chat_manager.getMultiUserChat(roomJid);
			multi_user_chat.join(Resourcepart.from(nickname));

			multi_user_chat.addParticipantStatusListener(new ParticipantStatusListener() {
				@Override
				public void joined(EntityFullJid participant) {
					System.out.println("[ " +participant + " ] joined the chat");
				}

				@Override
				public void left(EntityFullJid participant) {
					System.out.println("[ " +participant + " ] left the chat");
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

	private boolean createRoom(String room_jid) {
		try {
			EntityBareJid roomJid = JidCreate.entityBareFrom(room_jid);
			multi_user_chat = multi_user_chat_manager.getMultiUserChat(roomJid);
			multi_user_chat.createOrJoin(Resourcepart.from(nickname));
			multi_user_chat.sendConfigurationForm(multi_user_chat.getConfigurationForm().getFillableForm());

			multi_user_chat.addParticipantStatusListener(new ParticipantStatusListener() {
				@Override
				public void joined(EntityFullJid participant) {
					System.out.println("[ " +participant + " ] joined the chat");
				}

				@Override
				public void left(EntityFullJid participant) {
					System.out.println("[ " +participant + " ] left the chat");
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

	private void setupRoomMessageListener() {
		for (MessageListener listener : room_chat_listeners) {
			multi_user_chat.removeMessageListener(listener);
		}
		room_chat_listeners.clear();
		MessageListener newListener = message -> {
			Platform.runLater(() -> {
				roomMessages.add(new Pair<String,String>(message.getFrom().getResourceOrEmpty().toString(), message.getBody()));
			});
		};
		multi_user_chat.addMessageListener(newListener);
		room_chat_listeners.add(newListener);
	}

	private void getContacts(VBox contents) {
		Roster roster = Roster.getInstanceFor(xmpp_connection);

		roster.addRosterListener(new RosterListener() {
			@Override
			public void entriesAdded(Collection<Jid> addresses) {
				Platform.runLater(() -> guiUpdateContacts(contents, roster));
			}
			@Override
			public void entriesUpdated(Collection<Jid> addresses) {
				Platform.runLater(() -> guiUpdateContacts(contents, roster));
			}
			@Override
			public void entriesDeleted(Collection<Jid> addresses) {
				Platform.runLater(() -> guiUpdateContacts(contents, roster));
			}
			@Override
			public void presenceChanged(Presence presence) {
				Platform.runLater(() -> guiUpdateContacts(contents, roster));
			}
		});
		guiUpdateContacts(contents, roster);
	}

	private void getConnectedUsers(VBox contents) {
		Roster roster = Roster.getInstanceFor(xmpp_connection);

		roster.addRosterListener(new RosterListener() {
			@Override
			public void entriesAdded(Collection<Jid> addresses) {
				Platform.runLater(() -> guiUpdateConnectedUsers(contents, roster));
			}
			@Override
			public void entriesUpdated(Collection<Jid> addresses) {
				Platform.runLater(() -> guiUpdateConnectedUsers(contents, roster));
			}
			@Override
			public void entriesDeleted(Collection<Jid> addresses) {
				Platform.runLater(() -> guiUpdateConnectedUsers(contents, roster));
			}
			@Override
			public void presenceChanged(Presence presence) {
				Platform.runLater(() -> guiUpdateConnectedUsers(contents, roster));
			}
		});
		guiUpdateConnectedUsers(contents, roster);
	}

	public boolean addContact(String user_jid) {
		try {
			EntityBareJid jid = JidCreate.entityBareFrom(user_jid);
			Roster roster = Roster.getInstanceFor(xmpp_connection);
			roster.createEntry(jid, user_jid, null);
			System.out.println("Contact added [ " + user_jid + " ]");
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

	public boolean removeContact(String user_jid) {
		try {
			EntityBareJid jid = JidCreate.entityBareFrom(user_jid);
			Roster roster = Roster.getInstanceFor(xmpp_connection);
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

	public boolean sendFile(File file) {
		return false;
	}

	public File receiveFile() {
		return null;
	}

	public static void main(String[] args) {
		launch(args);
	}
}