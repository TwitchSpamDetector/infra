# infra

Orquestación local de todo el sistema **TwitchSpamDetector**: levanta Postgres, RabbitMQ y los 3 microservicios (`Ingestor`, `ModerationEngine`, `CoreApi`) con un solo comando.

## Estructura esperada

Este repo asume que está clonado como carpeta **hermana** de los otros 3:

```
TwitchSpamDetector/
├── infra/              <- este repo
├── Ingestor/
├── ModerationEngine/
└── CoreApi/
```

Si tu estructura es distinta, ajusta las rutas `build:` en `docker-compose.yml`.

## Primer arranque

1. Copia el archivo de ejemplo de variables de entorno:
   ```powershell
   copy .env.example .env
   ```
   Verifica que `.env` haya quedado con valores (no vacíos) — sin este archivo, Postgres y RabbitMQ no arrancan.

2. Levanta todo:
   ```powershell
   docker compose up --build
   ```

3. Verifica que los 5 contenedores estén arriba:
   ```powershell
   docker compose ps
   ```
   Deberías ver `postgres`, `rabbitmq`, `core-api`, `moderation-engine` e `ingestor`, todos `Up`.

## Puertos y URLs

| Servicio | URL | Notas |
|---|---|---|
| CoreApi | http://localhost:8080/api/v1 | REST |
| ModerationEngine | http://localhost:8000/api/v1 | REST (`/analyze`, `/health`) |
| Ingestor | http://localhost:3000/api/v1 | REST (`/listeners`, `/health`) |
| RabbitMQ (AMQP) | localhost:5672 | usado por Ingestor y ModerationEngine |
| **RabbitMQ (UI)** | **http://localhost:15672** | usuario/password = `RABBITMQ_USER`/`RABBITMQ_PASSWORD` de tu `.env` (NO `guest/guest`) |
| Postgres | localhost:5432 | usuario/db = `DB_USER`/`DB_NAME` de tu `.env` |

## Usar la UI de RabbitMQ

Entra a **http://localhost:15672** e inicia sesión con las credenciales de tu `.env`.

- **Queues** → deberías ver la cola `chat-messages` (la declara `moderation-engine` al arrancar).
- Clic en la cola → **"Publish message"** al fondo de la página → te sirve para mandar un `ChatMessage` de prueba a mano, sin depender de una conexión real a Twitch. Ejemplo de payload:
  ```json
  {
    "messageId": "11111111-1111-1111-1111-111111111111",
    "channelId": "mi_canal_twitch",
    "userId": "user1",
    "username": "tester",
    "text": "hola",
    "timestamp": "2026-09-14T03:30:00Z"
  }
  ```
- **Connections** / **Channels** → útil para confirmar que `ingestor` y `moderation-engine` sí se conectaron al broker.

## Probar el flujo completo

1. Registra un canal en CoreApi (requerido antes de cualquier veredicto — si no, verás un 404/502 al procesar mensajes):
   ```powershell
   curl -Method POST http://localhost:8080/api/v1/channels -Body '{"channelId":"tu_canal","displayName":"Tu Canal"}' -ContentType "application/json"
   ```

2. Dile al Ingestor que escuche ese canal (debe ser un **canal real de Twitch**, mismo `channelId` que registraste arriba):
   ```powershell
   curl -Method POST http://localhost:3000/api/v1/listeners -Body '{"channelId":"tu_canal"}' -ContentType "application/json"
   ```

3. Escribe en el chat de ese canal en Twitch y observa los logs:
   ```powershell
   docker compose logs -f moderation-engine
   ```

4. Confirma que CoreApi guardó el veredicto:
   ```powershell
   curl http://localhost:8080/api/v1/channels/tu_canal/verdicts -UseBasicParsing
   ```

## Apagar todo

```powershell
docker compose down          # detiene y borra los contenedores, conserva el volumen de Postgres
docker compose down -v       # además borra el volumen (pierdes los datos de Postgres)
```

## Pendiente

- [ ] Carpeta `terraform/` cuando se decida mover algo a AWS (por ahora no hay nada que definir ahí).
