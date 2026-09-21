# ✈️ fly-free

API para consulta de preços de passagens aéreas da GOL. Monitora rotas cadastradas, sincroniza dados 2x ao dia via GeckoAPI e entrega sempre a menor tarifa disponível.

## Stack

- **Kotlin** + **Spring Boot 3.2**
- **PostgreSQL** (Supabase — free tier)
- **Flyway** para migrations
- **Spring WebFlux WebClient** para chamadas à GeckoAPI
- **Spring @Scheduled** para sincronização automática

## Endpoints

### Rotas

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/v1/routes?from=GRU&to=GIG&date=2026-10-01` | Cadastra rota para monitoramento |
| `GET` | `/api/v1/routes` | Lista todas as rotas |
| `DELETE` | `/api/v1/routes/{id}` | Desativa uma rota |

### Voos

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/api/v1/flights?from=GRU&to=GIG&date=2026-10-01` | Retorna a melhor oferta |

### Sincronização

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/v1/sync/trigger` | Força sincronização manual |
| `GET` | `/api/v1/sync/logs?limit=20` | Histórico de sincronizações |

## Configuração

Copie `.env.example` para `.env` e preencha as variáveis:

```bash
cp .env.example .env
```

```env
DATABASE_URL=jdbc:postgresql://...
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=...
GECKOAPI_API_KEY=sua_chave
```

Obtenha sua chave GeckoAPI (100 créditos grátis, sem cartão): https://dashboard.geckoapi.com.br

## Rodando localmente

```bash
./gradlew bootRun
```

## Deploy gratuito

### Railway
1. Crie um projeto em https://railway.app
2. Adicione um serviço PostgreSQL
3. Conecte o repositório GitHub
4. Configure as variáveis de ambiente
5. Deploy automático a cada push

## Documentação técnica

Ver pasta [`docs/`](./docs/):
- [ADR-001 — Decisões arquiteturais](./docs/ADR-001-arquitetura.md)
- [Fluxograma](./docs/fluxograma.md)
- [Diagrama de Sequência](./docs/diagrama-sequencia.md)
