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
		layout_a.setHgrow(field_username, Priority.ALWAYS);

		HBox layout_b = new HBox(10);
		layout_b.setAlignment(Pos.CENTER);
		layout_b.getChildren().addAll(field_password, button_signup);
		layout_b.setHgrow(field_password, Priority.ALWAYS);

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
	}
	public static void main(String[] args) {
		launch(args);
	}
}