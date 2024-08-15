import slixmpp

class EchoBot(slixmpp.ClientXMPP):
	def __init__(self, jid, password):
		super().__init__(jid, password)
		self.add_event_handler("session_start", self.start)
		self.add_event_handler("message", self.message)

	async def start(self, event):
		self.send_presence()
		await self.get_roster()

	async def message(self, msg):
		if msg['type'] in ('chat', 'normal'):
			response = f"Received: {msg['body']}"
			msg.reply(response).send()

# Replace with your XMPP credentials
jid = "alumchat.lol"
password = "password"

xmpp = EchoBot(jid, password)
xmpp.connect()
xmpp.process(forever=False)