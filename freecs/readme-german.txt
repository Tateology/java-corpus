Beta-Release der Version 1.2

Folgende Verzeichnisse findet man in diesem Paket:
*src/
*build/
config/
docs/
grouppolicies/
lib/
static/
templatesets/
(Die mit * gekennzeichneten Verzeichnisse sind nur in den SRC-Packages enthalten)

config:
config.cfg dient zur Einstellunge genereller Dinge. Z.B. port, servername, ...
auth.properties dient zur Einstellung der Authentifizierungsvarianten
command.properties dient zur deaktivierung von unerwünschten chat-commandos
membership.properties (beta)
	Dieses File dient zur definierung der einzelnen Memberships. Ein User
	kann über Membership-Definitionen die Userrechte zugewiesen bekommen und
	auch andere Erkennungsmerkmale erhalten.

Nähere Informationen zur Konfiguration findet man in den einzelnen Dateien
(sofern vorhanden).

docs:
Hier findet man die derzeit leider rudimentäre Dokumentation. Mehr hinweise findet
man in den Foren auf http://freecs.sourceforge.net. Fragen und Anregungen werden 
meist recht flott beantwortet. (Es wird jedoch keinerlei Zusicherung gegeben,
dass alle Fragen sofort beantwortet werden)

grouppolicies:
Hier kann man eingenschaften von Räumen beeinflussen. Unter anderm automatisches
SU für definierte User, SU unterbinden, Sperren der Gruppe verhindern, ...

lib:
Unter lib befindet sich das Jar-File, welches die Java-Classen des
Chatservers beinhaltet. Weiters ist dort auch das xmlrpc-1.2-b1.jar für den
XML-RPC-Server. Sollte man die Authentifizierung der User
über eine SQL-Datenbank erledigen wollen, muss man hier noch die
JDBC-Treiber ablegen und den Startaufruf so abändern, dass sich 
dieses Jar-File noch im Classpath von Java befindet.

templatesets:
Im Verzeichnis Templatesets liegen die einzelnen templatesets. Jedes
Verzeichniss representiert ein Templatest, welches wiederum über den
Browser mit dem Parameter 'templateset' ausgewählt wird. (Ob Post-
oder Get-Methode spielt hier keine Rolle).
D.h. Wenn man ein Verzeichnis namens 'test' hat, in dem ein gültiges
Templateset liegt, kann man dieses mit 'http://chaturl/?templateset=test'
ansprechen.

static:
Hier kann man Image-Dateien ablegen, die man auch vom Chatserver ausliefern will.
In den Beispiel-Templates wird hier z.B. das colorbar.gif referenziert.