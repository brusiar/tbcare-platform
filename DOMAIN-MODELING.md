# Modelagem do Domínio - TB Care Platform

## Visão Geral

Este documento explica as decisões de modelagem do domínio inicial do TB Care Platform, focado em atendimento psicológico com suporte a recorrência de sessões.

---

## Entidades Principais

### 1. Professional

Representa um profissional de saúde (psicólogo) no sistema.

**Campos:**
- `id` (UUID) - Identificador único
- `tenant_id` (UUID) - Isolamento multi-tenant
- `user_id` (UUID) - Referência ao usuário do sistema
- `meet_link` (String) - Link fixo da sala Google Meet
- `specialty` (String) - Especialidade do profissional
- `active` (Boolean) - Status ativo/inativo
- `created_at`, `updated_at` - Auditoria

**Decisões:**
- Separamos Professional de User para permitir que um usuário tenha múltiplos papéis
- O `meet_link` é armazenado no profissional (não na sessão) para reutilização
- Cada profissional tem sua própria sala fixa, simplificando o fluxo

### 2. Patient

Representa um paciente no sistema.

**Campos:**
- `id` (UUID) - Identificador único
- `tenant_id` (UUID) - Isolamento multi-tenant
- `name` (String) - Nome completo
- `email` (String) - Email de contato
- `phone` (String) - Telefone
- `date_of_birth` (Date) - Data de nascimento
- `notes` (Text) - Observações gerais
- `active` (Boolean) - Status ativo/inativo
- `created_at`, `updated_at` - Auditoria

**Decisões:**
- Campos de contato (email, phone) são opcionais para flexibilidade
- `notes` permite anotações gerais sobre o paciente
- Soft delete via campo `active` para manter histórico

### 3. Appointment

Representa uma sessão/consulta agendada.

**Campos:**
- `id` (UUID) - Identificador único
- `tenant_id` (UUID) - Isolamento multi-tenant
- `patient_id` (UUID) - Referência ao paciente
- `professional_id` (UUID) - Referência ao profissional
- `recurrence_id` (UUID) - Referência à recorrência (opcional)
- `scheduled_at` (DateTime) - Data e hora agendada
- `duration_min` (Integer) - Duração em minutos (padrão: 60)
- `status` (Enum) - Status da sessão
- `meet_link` (String) - Link da sala (herdado do profissional)
- `notes` (Text) - Observações da sessão
- `created_at`, `updated_at` - Auditoria

**Status Possíveis:**
- `SCHEDULED` - Agendada
- `CONFIRMED` - Confirmada pelo paciente
- `COMPLETED` - Realizada
- `CANCELLED` - Cancelada
- `NO_SHOW` - Paciente não compareceu

**Decisões:**
- `professional_id` em vez de `user_id` para clareza semântica
- `meet_link` pode ser sobrescrito por sessão se necessário
- `recurrence_id` conecta sessões recorrentes à sua configuração
- Status `NO_SHOW` diferencia de `CANCELLED` para métricas
- `duration_min` permite flexibilidade (sessões de 30, 60, 90 min)

### 4. AppointmentRecurrence

Representa a configuração de recorrência de sessões.

**Campos:**
- `id` (UUID) - Identificador único
- `tenant_id` (UUID) - Isolamento multi-tenant
- `patient_id` (UUID) - Paciente da recorrência
- `professional_id` (UUID) - Profissional responsável
- `recurrence_type` (Enum) - Tipo de recorrência
- `start_date` (Date) - Data de início
- `end_date` (Date) - Data de término (opcional)
- `duration_min` (Integer) - Duração padrão
- `time_of_day` (String) - Horário (ex: "14:00")
- `day_of_week` (Integer) - Dia da semana (1-7, para semanal/quinzenal)
- `active` (Boolean) - Status ativo/inativo
- `created_at`, `updated_at` - Auditoria

**Tipos de Recorrência:**
- `NONE` - Sem recorrência (sessão única)
- `WEEKLY` - Semanal (mesmo dia da semana)
- `BIWEEKLY` - Quinzenal (a cada 2 semanas)
- `MONTHLY` - Mensal (mesmo dia do mês)

**Decisões:**
- Separamos a configuração de recorrência das sessões individuais
- Permite modificar sessões individuais sem afetar a recorrência
- `end_date` opcional permite recorrências indefinidas
- `day_of_week` usa ISO (1=Segunda, 7=Domingo)
- `time_of_day` como string para simplicidade inicial

---

## Relacionamentos

```
Tenant (1) ──< (N) Professional
Tenant (1) ──< (N) Patient
Tenant (1) ──< (N) Appointment
Tenant (1) ──< (N) AppointmentRecurrence

User (1) ──< (1) Professional

Patient (1) ──< (N) Appointment
Patient (1) ──< (N) AppointmentRecurrence

Professional (1) ──< (N) Appointment
Professional (1) ──< (N) AppointmentRecurrence

AppointmentRecurrence (1) ──< (N) Appointment
```

---

## Fluxos Principais

### 1. Cadastro de Paciente

1. Profissional acessa tela de pacientes
2. Clica em "Novo Paciente"
3. Preenche dados (nome obrigatório, demais opcionais)
4. Sistema cria paciente com `tenant_id` do profissional

### 2. Agendamento de Sessão Única

1. Profissional acessa agenda
2. Clica em "Nova Sessão"
3. Seleciona paciente, data/hora e duração
4. Sistema cria `Appointment` com:
   - `professional_id` do profissional logado
   - `meet_link` copiado do profissional
   - `status` = SCHEDULED
   - `recurrence_id` = null

### 3. Agendamento de Sessão Recorrente (Futuro)

1. Profissional acessa tela de recorrências
2. Configura: paciente, tipo (semanal/quinzenal/mensal), horário
3. Sistema cria `AppointmentRecurrence`
4. Sistema gera `Appointment` para as próximas N semanas
5. Cada `Appointment` referencia a `recurrence_id`

### 4. Cancelamento de Sessão

1. Profissional acessa sessão na agenda
2. Clica em "Cancelar"
3. Sistema atualiza `status` para CANCELLED
4. Se for sessão recorrente, apenas aquela sessão é cancelada

### 5. Reagendamento

1. Profissional acessa sessão
2. Edita data/hora
3. Sistema atualiza `scheduled_at`
4. Se for sessão recorrente, apenas aquela sessão é alterada

---

## Decisões de Design

### Por que separar Professional de User?

- **Flexibilidade**: Um usuário pode ter múltiplos papéis (admin + profissional)
- **Dados específicos**: Professional tem dados específicos (meet_link, specialty)
- **Escalabilidade**: Facilita adicionar outros tipos de profissionais no futuro

### Por que AppointmentRecurrence separado?

- **Flexibilidade**: Permite modificar sessões individuais sem afetar o padrão
- **Histórico**: Mantém registro da configuração original
- **Cancelamento**: Cancelar uma sessão não afeta as demais
- **Reagendamento**: Reagendar uma sessão não quebra a recorrência

### Por que meet_link no Professional?

- **Simplicidade**: Cada profissional tem uma sala fixa
- **UX**: Paciente sempre usa o mesmo link
- **Flexibilidade**: Pode ser sobrescrito por sessão se necessário
- **Sem integração**: Não precisamos integrar com Google Meet API agora

### Por que status NO_SHOW separado de CANCELLED?

- **Métricas**: Importante diferenciar quem cancelou (profissional vs paciente)
- **Cobrança**: NO_SHOW pode ser cobrado, CANCELLED não
- **Análise**: Identificar pacientes com alta taxa de faltas

### Por que duration_min em vez de end_time?

- **Simplicidade**: Mais fácil de trabalhar
- **Padrão**: Sessões geralmente têm duração fixa
- **Cálculo**: end_time = scheduled_at + duration_min

---

## Índices de Performance

```sql
-- Busca de sessões por período (agenda)
CREATE INDEX idx_appointments_scheduled ON appointments(scheduled_at);
CREATE INDEX idx_appointments_tenant ON appointments(tenant_id);

-- Busca de sessões por paciente
CREATE INDEX idx_appointments_patient ON appointments(patient_id);

-- Busca de sessões por profissional
CREATE INDEX idx_appointments_professional ON appointments(professional_id);

-- Busca de recorrências ativas
CREATE INDEX idx_recurrences_patient ON appointment_recurrences(patient_id);
CREATE INDEX idx_recurrences_professional ON appointment_recurrences(professional_id);

-- Busca de profissionais por usuário
CREATE INDEX idx_professionals_user ON professionals(user_id);
```

---

## Limitações Atuais

### O que NÃO foi implementado (propositalmente):

1. **Geração automática de sessões recorrentes**
   - Estrutura está pronta, mas geração é manual por enquanto
   - Futuro: Job que gera sessões baseado em AppointmentRecurrence

2. **Notificações**
   - Estrutura permite, mas não implementado
   - Futuro: Email/SMS de lembrete

3. **Pagamentos**
   - Fora do escopo da ETAPA 3
   - Futuro: Integração com gateway de pagamento

4. **Portal do Paciente**
   - Fora do escopo da ETAPA 3
   - Futuro: Paciente pode ver/confirmar sessões

5. **Integração Google Meet**
   - Link é manual por enquanto
   - Futuro: Criar salas automaticamente via API

6. **Conflitos de horário**
   - Não há validação de conflito
   - Futuro: Validar disponibilidade antes de agendar

---

## Evolução Futura

### Curto Prazo
- Validação de conflitos de horário
- Geração automática de sessões recorrentes
- Edição em lote de recorrências

### Médio Prazo
- Notificações de lembrete
- Confirmação de presença pelo paciente
- Relatórios de frequência

### Longo Prazo
- Portal do paciente
- Integração Google Meet API
- Módulo financeiro
- Prontuário eletrônico

---

## Conclusão

A modelagem foi projetada para:
- **Simplicidade**: Fácil de entender e usar
- **Flexibilidade**: Permite evoluir sem refatoração
- **Escalabilidade**: Preparada para crescimento
- **Multi-tenant**: Isolamento completo por tenant
- **Manutenibilidade**: Código limpo e organizado

A estrutura de recorrência está preparada mas não totalmente implementada, permitindo evolução gradual conforme necessidade.
