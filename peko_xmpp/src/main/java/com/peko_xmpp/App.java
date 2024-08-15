package com.peko_xmpp;

// Java FX
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

// XMPP Lib
import org.jxmpp.jid.parts.*;
import org.jxmpp.jid.impl.*;
import org.jxmpp.jid.*;

import java.io.IOException;

public class App extends Application {

	private static Scene scene;

	@Override
	public void start(Stage stage) throws IOException {
		StackPane root = new StackPane();
		
		scene = new Scene(root, 640, 480);
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}

}