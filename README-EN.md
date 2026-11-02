# Shop Builder

> One-click shop mini-program full-stack development spec + multi-agent collaboration workflow. One instruction generates runnable WeChat mini-program, Java backend, and Vue3 admin console projects.

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)
![Author](https://img.shields.io/badge/author-Shenzhen%20Qingjiufeng%20Network%20Tech-orange)

## Overview

**Shop Builder (shop-builder)** is a WorkBuddy skill that distills a complete "one-click shop mini-program" full-stack development handbook into a reusable spec. It makes AI automatically follow a unified architecture, API contracts, coding rules, and a multi-agent workflow when building the project's WeChat mini-program, Java backend, and Vue admin frontend. The point is not "code that compiles" but "an engineering project that actually boots."

## ✨ Features

- **Three-tier one-click generation**: mini-program (Taro/Weapp), Spring Boot backend, and Vue3 admin frontend — same conventions, same contracts.
- **Multi-agent pipeline**: Product Manager Xiaoqing orchestrates and accepts; mini-program Zhang, backend Wang, and admin-frontend Liu each own a tier, advancing serially with consistent cross-tier contracts.
- **Unified API contract**: dual prefixes `/mini` (storefront) and `/private` (admin); unified `Response<DataResult<T>>` / `Response<PageResult<T>>` responses; centralized order numbers via `OrderUtils.getOrderNo`.
- **Scaffold script**: `scripts/scaffold_shop_builder.py` boots the initial skeleton in one command, with per-tier selective generation.
- **AI pay-per-use (402 / Machine Pay)**: wire backend read-only endpoints into Alipay AI Pay, supporting the full `probe → pay → complete → ack` lifecycle so external AI agents can call your API per use.
- **Java action rules**: 50+ global backend constraints (ban `@RequestMapping`, enforce DTO/enums, no loops for batch ops, constructor injection, `jakarta.*`, etc.) to reduce the "obviously AI-generated" smell.

## 🧱 Tech Stack

| Tier | Tech | Port |
| --- | --- | --- |
| WeChat mini-program | Taro / Weapp + TypeScript/JavaScript + Taro UI | `:9001` |
| Backend | Spring Boot 4.1 + JDK 17 + MyBatis-Plus + MySQL 8 + Redis 7 | `:9002` |
| Admin frontend | Vue3 + TypeScript + Vben 5.0 + Axios | — |

## 📁 Directory Structure

```
shop-builder/
├── SKILL.md                      # Skill definition (authoritative entry)
├── assets/
│   ├── a2m/                      # Alipay AI pay-per-use template code
│   │   ├── A2MPaymentController.java
│   │   ├── AlipayA2mConfig.java
│   │   ├── InMemoryOrderRepository.java
│   │   └── README.md
│   └── logo/
│       └── shop-builder-logo.png
├── references/
│   ├── manual.md                 # Full development handbook (authoritative source)
│   ├── java-action-rules.md      # 50+ backend global constraints
│   └── a2m-pay-integration.md    # AI pay-per-use integration spec
└── scripts/
    └── scaffold_shop_builder.py  # One-click scaffold
```

## 🚀 Quick Start

```bash
# Generate the full three-tier skeleton (Taro mini-program)
python scripts/scaffold_shop_builder.py --target <output_dir> --mini-framework taro

# Generate backend + admin only, skip the mini-program
python scripts/scaffold_shop_builder.py --target <output_dir> --mini-framework weapp --no-mini

# Generate with the AI pay-per-use backend included
python scripts/scaffold_shop_builder.py --target <output_dir> --mini-framework taro --with-a2m
```

Outputs: `ai-shop-system` (Spring Boot backend), `shop-mini` (mini-program), `shop-admin` (Vben admin frontend).

## 👥 Multi-Agent Workflow

| Role | Responsibility |
| --- | --- |
| Xiaoqing (Product Manager) | Break down requirements, dispatch tasks, orchestrate; accept and release after all three tiers are delivered |
| Zhang (Mini-program) | Confirm theme color / name / logo / target dir, wrap `requestUtils`, implement pages and data structures |
| Wang (Backend) | Set up the Spring Boot monolith, implement system / product / order / mini-program APIs and scheduled jobs, produce SQL |
| Liu (Admin Frontend) | Build the admin UI on Vben 5.0 from backend APIs and entities |

## 💰 AI Pay-Per-Use (402 / Machine Pay)

Wire backend read-only endpoints into Alipay AI Pay so external AI agents pay per call:

| Phase | Server action |
| --- | --- |
| **probe** | Without `Payment-Proof`, return `HTTP 402` + `Payment-Needed` header (Base64URL invoice) |
| **pay** | Agent pays from the invoice; server does not participate |
| **complete** | Retry with `Payment-Proof` → verify payment → deliver resource (`200` + `Payment-Validation`) |
| **ack** | Call fulfillment confirmation and mark the order complete |

## 📐 Backend Rules (Summary)

Full clauses in `references/java-action-rules.md`. Highlights:

- Ban `@RequestMapping`; use `@PostMapping/@GetMapping` with full paths prefixed by `/private`; no `Map`/`HttpRequest` params; no raw `Entity` returns (convert to VO).
- Params over 2 fields must use a DTO (extends `BaseDTO`); status/type fields must be enums.
- Batch operations must be batched — no `for/while` touching the database; ServiceImpl injects Service interfaces only.
- Inject via `@RequiredArgsConstructor` + `private final`; ban `@Autowired/@Resource`.
- Mutating methods must be `@Transactional`; JDK17 uses `jakarta.*`; time fields get `@JsonFormat`.
- Run `mvn -pl compile` for a real compile check after writing code.

## 📄 License

[MIT](https://opensource.org/licenses/MIT) © Shenzhen Qingjiufeng Network Technology Co., Ltd.
