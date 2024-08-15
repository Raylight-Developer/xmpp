package com.peko_xmpp;

// Java FX
import javafx.application.Application;
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
	private static AbstractXMPPConnection connection;
	private String username;

	@Override
	public void start(Stage stage) throws IOException {
		stage.setTitle("XMPP Chat");

		TextField field_username = new TextField();
		field_username.setPromptText("Usuario");

		PasswordField field_password = new PasswordField();
		field_password.setPromptText("Contraseña");

		Button button_signin = new Button("Iniciar sesión");
		Button button_signup = new Button("Registrar nueva cuenta");

		VBox loginBox = new VBox(10);
		loginBox.setAlignment(Pos.CENTER);
		loginBox.setPadding(new Insets(40));
		loginBox.getChildren().addAll(field_username, field_password, button_signin, button_signup);

		StackPane root = new StackPane();
		root.getChildren().add(loginBox);

		Scene scene = new Scene(root, 640, 480);
		scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

		stage.setScene(scene);
		stage.show();
	}
	public static void main(String[] args) {
		launch(args);
	}
}