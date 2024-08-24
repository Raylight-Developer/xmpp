Alejandro Martínez - 21430

```bash
mvn clean install
mvn javadoc::javadoc
mvn exec:java
```

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
	private void getContacts(VBox contents);
	```
2) Agregar un usuario a los contactos
	```java
	public boolean addContact(String user_jid);
	public boolean removeContact(String user_jid);
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
	private boolean joinRoom(String room_jid); // TODO <Only works for the same user> Potential problems: users not allowed to view; admin / room / server permissions
	private boolean createRoom(String room_jid);
	private boolean deleteRoom(String room_jid);
	private void setupRoomMessageListener();
	public boolean sendRoomMessage(String from_username, String to_room_jid, String message_body);
	```
6) Definir mensaje de presencia
	```java
	public boolean setStatus();
	```
7) Enviar/recibir notificaciones
	```java
	private void addNotification(String value);
	```
8) Enviar/recibir archivos
	```java
	public void sendFile(String to_jid, File file); // TODO send file format
	public void receiveFile(String file_content); // TODO figure out file format
	```