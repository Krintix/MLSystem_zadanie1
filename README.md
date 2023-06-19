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

## Kompilacja

Skompilowane w Maven'ie jako fat jar. Biblioteka **[Easy Modbus](EasyModbusJava.jar)** została dodana do lokalnego repozytorium Maven'a.

### Easy Modbus

Komenda to instalacji w lokalnym repozytorium

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
