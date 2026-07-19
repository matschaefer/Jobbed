# Deployment

Jobbed kann als einzelner Docker-Stack auf einem Linux-Server betrieben werden.
Die Produktionskonfiguration enthält PostgreSQL, Backend, Frontend und Caddy als
HTTPS-Reverse-Proxy. Mailpit gehört ausschließlich zur lokalen Entwicklung.

## Voraussetzungen

- Linux-Server mit Docker Engine und Docker Compose
- Domain mit einem A/AAAA-Record auf den Server
- Freigegebene Ports 80 und 443
- SMTP-Zugang mit verifiziertem Absender
- Regelmäßige externe Backups für Datenbank und Uploads

## Erstes Deployment

```bash
git clone <repository-url> jobbed
cd jobbed
cp .env.production.example .env.production
```

In `.env.production` müssen mindestens Domain, Datenbankpasswort, JWT-Secret und
SMTP-Zugangsdaten ersetzt werden. `MAIL_FROM` muss bei deinem Mailanbieter als
Absender verifiziert sein. Sichere Werte lassen sich beispielsweise so
erzeugen:

```bash
openssl rand -base64 48
```

Danach wird der Stack gestartet:

```bash
docker compose --env-file .env.production -f compose.prod.yml up -d --build
docker compose --env-file .env.production -f compose.prod.yml ps
```

Caddy beantragt das TLS-Zertifikat automatisch. Die Anwendung ist anschließend
unter `https://<DOMAIN>` erreichbar. Der Bestätigungslink in Registrierungs-E-Mails
verwendet dieselbe Domain.

## Aktualisierung

```bash
git pull --ff-only
docker compose --env-file .env.production -f compose.prod.yml up -d --build
```

Flyway führt neue Datenbankmigrationen beim Backend-Start aus. Vor jedem Update
sollte trotzdem ein Datenbank-Backup erstellt werden.

## Backups

```bash
docker compose --env-file .env.production -f compose.prod.yml exec -T postgres \
  pg_dump -U jobbed -d jobbed -Fc > jobbed-$(date +%F).dump
```

Zusätzlich muss das Volume `backend-uploads` gesichert werden. Backups sollten
verschlüsselt auf ein externes Ziel übertragen und regelmäßig testweise
wiederhergestellt werden.

## Release-Checkliste

- DNS und HTTPS funktionieren
- Registrierung liefert eine echte Bestätigungs-E-Mail
- SMTP-Absenderdomain ist verifiziert; SPF, DKIM und DMARC sind eingerichtet
- Bestätigungslink und Passwort-Reset verwenden die Produktionsdomain
- `SPRING_PROFILES_ACTIVE=prod` und `DEMO_DATA_ENABLED=false`
- Keine Beispiel-Secrets oder `.env.production` im Repository
- Datenbank- und Upload-Backups eingerichtet
- Datenschutzerklärung und Impressum mit echten Angaben vorhanden
- Monitoring für `/actuator/health` eingerichtet
