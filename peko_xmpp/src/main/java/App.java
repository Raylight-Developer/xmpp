

// Java FX
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;

// Smack Lib
import org.jivesoftware.smack.AbstractXMPPConnection;

// XMPP Lib
import org.jxmpp.jid.parts.*;
import org.jxmpp.jid.impl.*;
import org.jxmpp.jid.*;

// Java
import java.io.IOException;

public class App extends Application {
	private static AbstractXMPPConnection xmpp_connection;
	private static ObservableList<String> incoming_messages;
	private String username = "21430";
	private String password = "Test_123";

	@Override
	public void start(Stage stage) throws IOException {
		stage.setTitle("XMPP Chat");
		guiLoginScreen(stage);
	}

	private void guiLoginScreen(Stage stage) {
		TextField field_username = new TextField();
		field_username.setPromptText("Usuario");
		field_username.setStyle("-fx-max-width: Infinity;");

		PasswordField field_password = new PasswordField();
		field_password.setPromptText("Contraseña");
		field_password.setStyle("-fx-max-width: Infinity;");

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
		layout.setSpacing(10);
		layout.setPadding(new Insets(10));
		layout.getChildren().addAll(layout_a, layout_b);

		StackPane root = new StackPane();
		root.getChildren().add(layout);

		Scene scene = new Scene(root, 640, 480);
		scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

		stage.setScene(scene);
		stage.show();

		button_signin.setOnAction(e -> {
			username = field_username.getText();
			password = field_password.getText();
			guiHomeScreen(stage);
		});

		button_signup.setOnAction(e -> {
			username = field_username.getText();
			password = field_password.getText();
			guiHomeScreen(stage);
		});
	}

	private void guiHomeScreen(Stage stage) {
		Label label = new Label("Bienvenido " + username);

		Button button_chat = new Button("Chatear");
		button_chat.setStyle("-fx-max-width: Infinity;");
		Button button_contacts = new Button("Mis Contactos");
		button_contacts.setStyle("-fx-max-width: Infinity;");
		Button button_account = new Button("Mi Cuenta");
		button_account.setStyle("-fx-max-width: Infinity;");

		VBox layout_main = new VBox(10);
		layout_main.setAlignment(Pos.TOP_CENTER);

		HBox layout_menu_a = new HBox(10);
		layout_menu_a.setAlignment(Pos.CENTER);
		layout_menu_a.getChildren().addAll(button_contacts, button_account);
		HBox.setHgrow(button_contacts, Priority.ALWAYS);
		HBox.setHgrow(button_account, Priority.ALWAYS);

		VBox layout_menu = new VBox(10);
		layout_menu.setAlignment(Pos.TOP_CENTER);
		layout_menu.setSpacing(10);
		layout_menu.setPadding(new Insets(10));
		layout_menu.getChildren().addAll(label, button_chat, layout_menu_a);

		VBox layout_container = new VBox(10);
		layout_container.setAlignment(Pos.TOP_CENTER);
		layout_container.getChildren().add(label);
		layout_container.getChildren().add(layout_main);
		layout_container.getChildren().add(layout_menu);
		VBox.setVgrow(layout_main, Priority.ALWAYS);

		StackPane root = new StackPane();
		root.getChildren().add(layout_container);

		Scene scene = new Scene(root, 640, 480);
		scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

		stage.setScene(scene);
		stage.show();
	}

	public static void signUp() {
	}

	public static void signIn() {
	}

	public static void signOut() {
	}

	public static void sendMessage() {
	}

	public static void getMessages() {
	}

	public static void addContact() {
	}

	public static void getContacts() {
	}

	public static void getConnectedUsers() {
	}

	public static void sendFile() {
	}

	public static void receiveFile() {
	}

	public static void main(String[] args) {
		launch(args);
	}
}