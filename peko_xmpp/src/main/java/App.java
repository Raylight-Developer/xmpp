

// Java FX
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.util.*;

// Smack Lib
import org.jivesoftware.smackx.iqregister.AccountManager;
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
	private static AbstractXMPPConnection xmpp_connection;
	private static ObservableList<Pair<String, String>> incoming_messages;
	private ObservableList<String> connected_users;

	private static String domain = "alumchat.lol";
	private String username = "mar21430-test";
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
		TextField field_username = new TextField();
		field_username.setPromptText("Usuario");
		field_username.setStyle("-fx-max-width: Infinity;");
		field_username.setText(username);

		PasswordField field_password = new PasswordField();
		field_password.setPromptText("Contraseña");
		field_password.setStyle("-fx-max-width: Infinity;");
		field_password.setText(password);

		Button button_signin = new Button("Iniciar sesión");
		button_signin.setStyle("-fx-pref-width: 200px;");

		Button button_signup = new Button("Registrar nueva cuenta");
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

		button_signin.setOnAction(e -> {
			username = field_username.getText();
			password = field_password.getText();
			if (signIn(username, password)) {
				guiHomeScreen(scene);
			}
			//guiHomeScreen(scene);
		});

		button_signup.setOnAction(e -> {
			username = field_username.getText();
			password = field_password.getText();
			if (signUp(username, password)) {
				guiHomeScreen(scene);
			}
		});
	}

	private void guiHomeScreen(Scene scene) {
		Label label = new Label("Bienvenido " + username);
		label.setStyle("-fx-max-width: Infinity; -fx-font-size: 20px;");

		Button button_logout = new Button("Log Out");

		HBox layout_header = new HBox(10);
		layout_header.getChildren().addAll(label, button_logout);
		HBox.setHgrow(label, Priority.ALWAYS);

		Button button_chat = new Button("Chatear");
		button_chat.setStyle("-fx-max-width: Infinity;");

		Button button_account = new Button("Mi Cuenta");
		button_account.setStyle("-fx-max-width: Infinity;");

		VBox layout_main = new VBox(10);
		layout_main.setAlignment(Pos.TOP_CENTER);

		HBox layout_menu = new HBox(10);
		layout_menu.setAlignment(Pos.CENTER);
		layout_menu.getChildren().addAll(button_chat, button_account);
		HBox.setHgrow(button_chat, Priority.ALWAYS);
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

		guiChatScreen(scene, layout_main, label);

		button_chat.setOnAction(e -> {
			guiChatScreen(scene, layout_main, label);
		});

		button_account.setOnAction(e -> {
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
	}

	private void guiChatScreen(Scene scene, VBox container, Label label) {
		container.getChildren().clear();
		label.setText("Chat");
//
		Label label_contact = new Label("Mis Contactos");
		label_contact.setStyle("-fx-max-width: Infinity;");

		Button button_add_contact = new Button("Agregar Contacto");
		button_add_contact.setStyle("-fx-max-width: Infinity;");

		Button button_remove_contact = new Button("Eliminar Contacto");
		button_remove_contact.setStyle("-fx-max-width: Infinity;");

		HBox layout_contacts_header = new HBox(10);
		layout_contacts_header.getChildren().addAll(button_add_contact, button_remove_contact);
		HBox.setHgrow(button_add_contact, Priority.ALWAYS);
		HBox.setHgrow(button_remove_contact, Priority.ALWAYS);

		VBox layout_contacts_list_content = new VBox(10);
		ScrollPane scroll_contacts = new ScrollPane(layout_contacts_list_content);
		scroll_contacts.setStyle("-fx-max-height: Infinity; -fx-max-width: Infinity; -fx-min-width:200px;");

		VBox layout_contacts = new VBox(10);
		layout_contacts.getChildren().addAll(label_contact, layout_contacts_header, scroll_contacts);
		VBox.setVgrow(scroll_contacts, Priority.ALWAYS);
//
		Label label_messages = new Label("Chattear");
		label_messages.setStyle("-fx-max-width: Infinity;");

		TextField field_to_user = new TextField();
		field_to_user.setPromptText("Eviar a Usuario con JID...");
		field_to_user.setStyle("-fx-max-width: Infinity;");

		Button button_send_mesasage = new Button("Enviar Mensaje");
		button_send_mesasage.setStyle("-fx-max-width: Infinity;");

		TextArea field_message = new TextArea();
		field_message.setStyle("-fx-max-height: Infinity; -fx-min-width:200px;");
		field_message.setPromptText("Mensaje...");

		HBox layout_message_header = new HBox(10);
		layout_message_header.getChildren().addAll(field_to_user, button_send_mesasage);
		HBox.setHgrow(field_to_user, Priority.ALWAYS);

		VBox layout_message = new VBox(10);
		layout_message.getChildren().addAll(label_messages, layout_message_header, field_message);
		VBox.setVgrow(field_message, Priority.ALWAYS);
//
		Label label_users = new Label("Usuarios Conectados");
		label_users.setStyle("-fx-max-width: Infinity;");

		VBox layout_user_list_content = new VBox(10);
		ScrollPane scroll_users = new ScrollPane(layout_user_list_content);
		scroll_users.setStyle("-fx-max-height: Infinity; -fx-max-width: Infinity; -fx-min-width:200px;");

		VBox layout_user = new VBox(10);
		layout_user.getChildren().addAll(label_users, scroll_users);
		VBox.setVgrow(scroll_users, Priority.ALWAYS);
//
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(layout_contacts, layout_message, layout_user);
		HBox.setHgrow(layout_contacts, Priority.ALWAYS);
		HBox.setHgrow(layout_message, Priority.ALWAYS);
		HBox.setHgrow(layout_user, Priority.ALWAYS);

		container.getChildren().add(hbox);
		VBox.setVgrow(hbox, Priority.ALWAYS);

		Roster roster = Roster.getInstanceFor(xmpp_connection);
		for (RosterEntry entry : getConnectedUsers()) {
			Presence presence = roster.getPresence(entry.getJid());
			String user_status = presence.isAvailable() ? "Conectado" : "Desconectado";
			String status_message = presence.getStatus() != null ? presence.getStatus() : "Sin mensaje de status/presencia.";

			Label label_jid = new Label(entry.getJid().toString());
			Label label_status = new Label(user_status);
			Label label_message = new Label(status_message);

			HBox layout_contact_sub = new HBox(5);
			layout_contact_sub.getChildren().addAll(label_jid, label_status);

			VBox layout_contact = new VBox(5);
			layout_contact.getChildren().addAll(layout_contact_sub, label_message);

			layout_user_list_content.getChildren().add(layout_contact);
		}
	}

	private void guiAccountScreen(Scene scene, VBox container, Label label) {
		container.getChildren().clear();
		label.setText("Mi Cuenta  |  " + username);
//
		Label label_presence = new Label("Mensaje de Presencia");
		label_presence.setStyle("-fx-max-width: Infinity;");

		TextField field_presence = new TextField();
		field_presence.setPromptText("Mensaje de Presencia...");
		field_presence.setStyle("-fx-max-width: Infinity;");

		Button button_set_presence = new Button("Actualizar");

		HBox layout_presence_set = new HBox(10);
		layout_presence_set.getChildren().addAll(field_presence, button_set_presence);
		HBox.setHgrow(field_presence, Priority.ALWAYS);

		VBox layout_presence = new VBox(10);
		layout_presence.getChildren().addAll(label_presence, layout_presence_set);
//
		Button button_close_account = new Button("Eliminar Cuenta");
		button_close_account.setStyle("-fx-max-width: Infinity; -fx-background-color: rgb(100,30,30);");
//
		HBox hbox = new HBox(10);
		hbox.getChildren().addAll(layout_presence);
		HBox.setHgrow(layout_presence, Priority.ALWAYS);

		container.getChildren().addAll(hbox, button_close_account);
		VBox.setVgrow(hbox, Priority.ALWAYS);

		button_set_presence.setOnAction(event -> {
			definePresence(username, field_presence.getText());
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
/*-----------------------------

XMPP

-----------------------------*/
	public static boolean signUp(String username, String password) {
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
			catch (IOException e) {
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

	public static boolean signIn(String username, String password) {
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
			catch (IOException e) {
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

	public static boolean signOut() {
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

	public static boolean deleteAccount() {
		try {
			AccountManager accountManager = AccountManager.getInstance(xmpp_connection);
			accountManager.deleteAccount();
			xmpp_connection.disconnect();
			System.out.println("Cuenta eliminada exitosamente.");
			return true;
		}
		catch (Exception e) {
			System.out.println("Error eliminando cuenta  |  " + e.getMessage());
			e.printStackTrace();

			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning");
			alert.setHeaderText("Error");
			alert.setContentText("Error eliminando cuenta  |  " + e.getMessage());
			alert.showAndWait();

			return false;
		}
	}

	public static boolean definePresence(String username, String message) {
		try {
			Presence presence = new Presence(Presence.Type.available);
			presence.setStatus(message);
			xmpp_connection.sendStanza(presence);
			System.out.println("Mensaje de presencia definido [ " + message + " ]");
			return true;
		}
		catch (Exception e) {
			System.out.println("Error al definir el mensaje de presencia [ " + message + " ] para" + username + "  |  " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public static boolean sendMessage(String from_username, String to_user_jid, String message_body) {
		try {
			ChatManager manager = ChatManager.getInstanceFor(xmpp_connection);
			EntityBareJid jid = JidCreate.entityBareFrom(to_user_jid);
			Chat chat = manager.chatWith(jid);

			Message message = new Message(jid, Message.Type.chat);
			message.setBody(message_body);
			chat.send(message);

			System.out.println("Mensaje [ " + message_body + " ] enviado de " + from_username + " a " + to_user_jid);
			return true;
		}
		catch (Exception e) {
			System.out.println("Error al enviar mensaje a " + to_user_jid + "  |  " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	private void getMessages() {
		ChatManager manager = ChatManager.getInstanceFor(xmpp_connection);
		manager.addIncomingListener(new IncomingChatMessageListener() {
			@Override
			public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
				String from_user = from.toString();
				String message_body = message.getBody();

				System.out.println("Mensaje recibido de " + from_user + ": " + message_body);
				incoming_messages.add(new Pair<String, String>(from_user, message_body));

				Platform.runLater(() -> {
					//displayMessage(fromUser, messageBody);
				});
			}
		});
	}

	public static boolean addContact(String user_jid) {
		try {
			EntityBareJid jid = JidCreate.entityBareFrom(user_jid);
			Roster roster = Roster.getInstanceFor(xmpp_connection);
			roster.createEntry(jid, user_jid, null);
			System.out.println("Contacto agregado " + user_jid);
			return true;
		}
		catch (Exception e) {
			System.out.println("Error al agregar contacto " + user_jid + "  |  " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public static Collection<RosterEntry> getContacts() {
		try {
			Roster roster = Roster.getInstanceFor(xmpp_connection);
			Collection<RosterEntry> contacts = roster.getEntries();
			return contacts;
		}
		catch (Exception e) {
			System.out.println("Error obteniendo contactos  |  " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public static Collection<RosterEntry> getConnectedUsers() {
		try {
			Roster roster = Roster.getInstanceFor(xmpp_connection);
			Collection<RosterEntry> contacts = roster.getEntries();
			return contacts;
		}
		catch (Exception e) {
			System.out.println("Error obteniendo usuarios conectados  |  " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

//	private void setupPresenceListener() {
//		Roster roster = Roster.getInstanceFor(xmpp_connection);
//
//		Collection<RosterEntry> entries = roster.getEntries();
//		for (RosterEntry entry : entries) {
//			Presence presence = roster.getPresence(entry.getJid());
//			if (presence.isAvailable()) {
//				addUserToList(entry.getJid().toString());
//			}
//		}
//
//		roster.addRosterListener(new RosterListener() {
//			@Override
//			public void entriesAdded(Collection<Jid> addresses) {
//				for (Jid jid : addresses) {
//					Presence presence = roster.getPresence(jid.asBareJid());
//					if (presence.isAvailable()) {
//						addUserToList(jid.toString());
//					}
//				}
//			}
//
//			@Override
//			public void entriesUpdated(Collection<Jid> addresses) {
//				for (Jid jid : addresses) {
//					Presence presence = roster.getPresence(jid.asBareJid());
//					if (presence.isAvailable()) {
//						addUserToList(jid.toString());
//					} else {
//						removeUserFromList(jid.toString());
//					}
//				}
//			}
//
//			@Override
//			public void entriesDeleted(Collection<Jid> addresses) {
//				for (Jid jid : addresses) {
//					removeUserFromList(jid.toString());
//				}
//			}
//
//			@Override
//			public void presenceChanged(Presence presence) {
//				EntityBareJid jid = presence.getFrom().asEntityBareJidIfPossible();
//				if (presence.isAvailable()) {
//					addUserToList(jid.toString());
//				} else {
//					removeUserFromList(jid.toString());
//				}
//			}
//		});
//	}

	private void addUserToList(String user_jid) {
		Platform.runLater(() -> {
			if (!connected_users.contains(user_jid)) {
				connected_users.add(user_jid);
			}
		});
	}

	private void removeUserFromList(String user_jid) {
		Platform.runLater(() -> connected_users.remove(user_jid));
	}

	public static boolean sendFile(File file) {
		return false;
	}

	public static File receiveFile() {
		return null;
	}

	public static void main(String[] args) {
		launch(args);
	}
}