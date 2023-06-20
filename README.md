# MLSystem Zadanie 1

W folderze **[Finished Services](FinishedServices/)** znajduję się ukończone zadanie wraz z plikami **.service**.

## Usługi

Usługi zostały wykonane dla funkcji **Systemd**</br>
Były one zarejestrowane pod **/etc/systemd/system**</br>
Komendy użyte do testowania:

```bash
sudo systemctl daemon-reload
sudo systemctl start toggler.service
sudo systemctl start guard.service

systemctl status toggler.service
systemctl status guard.service

sudo systemctl stop toggler.service
sudo systemctl stop guard.service
```

## Konfiguracja

Osobno **[Guard](FinishedServices/guard/guard.jar)** i **[Toggler](FinishedServices/toggler/toggler.jar)** przyjmują plik *application.properties*, w którym powinna być zawarta konfiguracja programu. Jeśli pliku nie będzie program użyje domyślnych testowych.

### SNMP Variable Binding

Wpisów może być wiele i mogą zawierać nie tylko informacje o **OID**'zie ale także po symbolu ***~*** tekst, który zostanie przypisany do wiadomości. Powtórne użycie ***~*** w konfiguracji zostanie zamienione na wiadomość przesłaną przez program.

```properties
VariableBinding0:1.2.3.4.5.6
VariableBinding1:1.2.3.4.5.6~TEST
VariableBinding2:1.2.3.4.5.6~Wykryto błąd(~)
```

Pakiety te są wysyłane przez **SNMP** kiedy zostanie wykryty błąd do zgłoszenia.

## Kompilacja

Skompilowane w Maven'ie jako fat jar. Biblioteka **[Easy Modbus](EasyModbusJava.jar)** została dodana do lokalnego repozytorium Maven'a.

### Easy Modbus

Komenda do instalacji w lokalnym repozytorium

```bash
mvn install:install-file -Dfile=<scieżka_do_pliku> -DgroupId=modbus -DartifactId=easymodbus -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
```

### Shared

```bash
mvn clean install
```

### Guard

```bash
mvn clean package
```

### Toggler

Oparty na frameworku Spring-Boot

```bash
mvnw clean package
```
