Alejandro Martínez - 21430

# Funciones
### Administración de cuentas
1) Registrar una nueva cuenta en el servidor
	```java
	public static boolean signUp(String username, String password); // Working
	```
2) Iniciar sesión con una cuenta
	```java
	public static boolean signIn(String username, String password); // Working
	```
3) Cerrar sesión con una cuenta
	```java
	public static boolean signOut(); // Working
	```
4) Eliminar la cuenta del servidor
	```java
	public static boolean deleteAccount(); // Working
	```
### Comunicación
1) Mostrar todos los usuarios/contactos y su estado
	```java
	public static Collection<RosterEntry> getConnectedUsers(); // TODO <Not Working>
	public static Collection<RosterEntry> getContacts(); // Working ?
	```
2) Agregar un usuario a los contactos
	```java
	public static boolean addContact(String user_jid); // Working ?
	```
3) Mostrar detalles de contacto de un usuario
	```java

	```
4) Comunicación 1 a 1 con cualquier usuario/contacto
	```java

	```
5) Participar en conversaciones grupales
	```java
		MultiUserChatManager 
	```
6) Definir mensaje de presencia
	```java
	public static boolean definePresence(String username, String message); // Working
	```
7) Enviar/recibir notificaciones
	```java

	```
8) Enviar/recibir archivos
	```java
	public static boolean sendFile(File file); // TODO
	public static File receiveFile(); // TODO
	```