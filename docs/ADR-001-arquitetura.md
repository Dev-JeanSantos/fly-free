# ADR-001: Arquitetura do fly-free

**Status:** Aceito  
**Data:** 2026-09-21  
**Autores:** fly-free team

---

## Contexto

Existe uma necessidade de oferecer aos integradores uma API simples e barata para consultar preços de passagens aéreas da GOL. Consumir a GeckoAPI diretamente a cada request seria custoso (créditos por chamada) e lento. A proposta é criar uma camada intermediária que cache os dados e entregue sempre a menor tarifa disponível.

---

## Decisão

Construir o **fly-free** como um serviço backend em **Spring Boot + Kotlin** que:

1. Expõe uma API REST aberta para cadastro de rotas e consulta de melhores ofertas
2. Utiliza um **scheduler interno** (2x ao dia: 6h e 18h) para buscar dados na GeckoAPI e persistir no banco
3. Armazena todos os resultados em **PostgreSQL** (Supabase — free tier)
4. Serve as consultas dos clientes **direto do banco**, sem chamadas síncronas à GeckoAPI

---

## Alternativas consideradas

| Alternativa | Motivo de rejeição |
|---|---|
| Chamar GeckoAPI a cada request do cliente | Consome créditos por chamada, latência alta, risco de rate limit |
| Cache em memória (Redis) | Custo adicional de infraestrutura, dados somem ao reiniciar |
| Ktor em vez de Spring Boot | Spring foi definido como requisito do projeto |
| Atualização em tempo real (webhook/stream) | GeckoAPI não oferece esse modelo; preços de passagens não mudam a cada minuto |

---

## Consequências

**Positivas:**
- Custo previsível na GeckoAPI (número fixo de chamadas/dia por rota)
- Baixa latência para o cliente (leitura direta do banco)
- Histórico de preços naturalmente armazenado para análises futuras
- Fácil de escalar: adicionar novas rotas não muda a arquitetura

**Negativas / trade-offs:**
- Dados podem ter até 12h de defasagem em relação ao site da GOL
- Rotas precisam ser previamente cadastradas para serem monitoradas
- Se o scheduler falhar, os dados ficam desatualizados silenciosamente (mitigado pelo `sync_logs`)

---

## Componentes da solução

### Entidades

| Tabela | Responsabilidade |
|---|---|
| `routes` | Rotas monitoradas (origin, destination, travel_date) |
| `flight_results` | Todos os voos retornados pela GeckoAPI (dados brutos) |
| `best_offers` | Menor tarifa calculada por rota + data |
| `sync_logs` | Histórico de sincronizações (status, erros, duração) |

### Endpoints

| Método | Path | Descrição |
|---|---|---|
| `POST` | `/api/v1/routes` | Cadastra uma rota para monitoramento |
| `GET` | `/api/v1/routes` | Lista todas as rotas monitoradas |
| `DELETE` | `/api/v1/routes/{id}` | Remove uma rota do monitoramento |
| `GET` | `/api/v1/flights` | Consulta melhor oferta por rota e data |
| `GET` | `/api/v1/sync/logs` | Consulta histórico de sincronizações |
| `POST` | `/api/v1/sync/trigger` | Força uma sincronização manual |

### Parâmetros dos endpoints principais

**POST /api/v1/routes**
```
?from=GRU&to=GIG&date=2026-10-01
```

**GET /api/v1/flights**
```
?from=GRU&to=GIG&date=2026-10-01
```

---

## Stack técnica

| Camada | Tecnologia | Justificativa |
|---|---|---|
| Linguagem | Kotlin 1.9 | Conciso, type-safe, interoperável com Java |
| Framework | Spring Boot 3.2 | Requisito do projeto |
| ORM | Spring Data JPA + Hibernate | Padrão Spring, suporte a PostgreSQL |
| Migrations | Flyway | Versionamento de schema reproducível |
| HTTP Client | Spring WebFlux WebClient | Reativo, não-bloqueante para chamadas externas |
| Banco | PostgreSQL via Supabase | 500MB grátis, sem cartão de crédito |
| Scheduler | Spring `@Scheduled` | Nativo do Spring, sem dependência extra |
| Deploy | Railway | Suporte JVM, free tier, deploy via Git |

---

## Decisões futuras (fora do escopo atual)

- Autenticação via API key para clientes
- Suporte a múltiplas companhias (LATAM, Azul)
- Critérios de melhor oferta além de preço (escalas, bagagem)
- Alertas de variação de preço via webhook
