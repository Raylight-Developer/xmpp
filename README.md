Alejandro Martínez - 21430

# Funciones
### Administración de cuentas
1) Registrar una nueva cuenta en el servidor
	```java
	public boolean signUp();
	```
2) Iniciar sesión con una cuenta
	```java
	public boolean signIn();
	```
3) Cerrar sesión con una cuenta
	```java
	public boolean signOut();
	```
4) Eliminar la cuenta del servidor
	```java
	public boolean deleteAccount();
	```
### Comunicación
1) Mostrar todos los usuarios/contactos y su estado
	```java
	public void getConnectedUsers(VBox contents); // TODO <Server permissions?>
	private void getContacts(VBox contents);
	```
2) Agregar un usuario a los contactos
	```java
	public boolean addContact(String user_jid);
	```
3) Mostrar detalles de contacto de un usuario
	```java
	private void guiUpdateContacts(VBox contents, VBox layout_message_area, TextArea field_message, TextField field_user, Roster roster);
	```
4) Comunicación 1 a 1 con cualquier usuario/contacto
	```java
	private void guiAddIncomingMessage(String sender_username, String message, VBox contents);
	private void setupChatMessageListener();
	public boolean sendChatMessage(String to_user_jid, String message_body);
	```
5) Participar en conversaciones grupales
	```java
	private boolean joinRoom(String room_jid); // TODO <Only works for the same user, server permissions?>
	private boolean createRoom(String room_jid);
	private boolean deleteRoom(String room_jid);
	private void setupRoomMessageListener();
	public boolean sendRoomMessage(String from_username, String to_room_jid, String message_body);
	```
6) Definir mensaje de presencia
	```java
	public boolean definePresence(String username, String message);
	```
7) Enviar/recibir notificaciones
	```java
	// TODO
	```
8) Enviar/recibir archivos
	```java
	public boolean sendFile(File file); // TODO
	public File receiveFile(); // TODO
	```