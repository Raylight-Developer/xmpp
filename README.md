Alejandro Martínez - 21430

# Funciones
### Administración de cuentas
1) Registrar una nueva cuenta en el servidor
	```java
	public boolean signUp(String username, String password); // Working
	```
2) Iniciar sesión con una cuenta
	```java
	public boolean signIn(String username, String password); // Working
	```
3) Cerrar sesión con una cuenta
	```java
	public boolean signOut(); // Working
	```
4) Eliminar la cuenta del servidor
	```java
	public boolean deleteAccount(); // Working
	```
### Comunicación
1) Mostrar todos los usuarios/contactos y su estado
	```java
	public void getConnectedUsers(VBox contents); // TODO <Not Working>
	private void getContacts(VBox contents); // Working
	```
2) Agregar un usuario a los contactos
	```java
	public boolean addContact(String user_jid); // Working
	```
3) Mostrar detalles de contacto de un usuario
	```java

	```
4) Comunicación 1 a 1 con cualquier usuario/contacto
	```java
		private void getChatMessages(VBox contents); // Working
		public boolean sendChatMessage(String from_username, String to_user_jid, String message_body); // Working
	```
5) Participar en conversaciones grupales
	```java
		private void joinRoom(String room_jid);
		private void getRoomMessages(VBox contents); // TODO
		public boolean sendRoomMessage(String from_username, String to_room_jid, String message_body); // Working
	```
6) Definir mensaje de presencia
	```java
	public boolean definePresence(String username, String message); // Working
	```
7) Enviar/recibir notificaciones
	```java

	```
8) Enviar/recibir archivos
	```java
	public boolean sendFile(File file); // TODO
	public File receiveFile(); // TODO
	```