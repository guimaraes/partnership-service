package br.com.ecomercial.partnership.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // Contadores para fechamentos
    private Counter closingExecutionCounter;
    private Counter closingSuccessCounter;
    private Counter closingFailureCounter;
    private Counter closingReopenCounter;

    // Timers para performance
    private Timer closingExecutionTimer;

    // Contadores para importações
    private Counter importPartnerCounter;
    private Counter importClientCounter;
    private Counter importSuccessCounter;
    private Counter importFailureCounter;

    // Contadores para auditoria
    private Counter auditLogCounter;

    // Contadores para parceiros e clientes
    private Counter partnerCreatedCounter;
    private Counter partnerUpdatedCounter;
    private Counter partnerDeletedCounter;
    private Counter clientCreatedCounter;
    private Counter clientUpdatedCounter;
    private Counter clientDeletedCounter;

    // Contadores para regras de comissão
    private Counter commissionRuleCreatedCounter;
    private Counter commissionRuleUpdatedCounter;

    /**
     * Incrementa contador de execução de fechamento
     */
    public void incrementClosingExecution(YearMonth month) {
        if (closingExecutionCounter == null) {
            closingExecutionCounter = Counter.builder("closings.execution.count")
                    .description("Number of monthly closing executions")
                    .tag("month", month.toString())
                    .register(meterRegistry);
        }
        closingExecutionCounter.increment();
        log.info("Closing execution metric incremented for month: {}", month);
    }

    /**
     * Incrementa contador de fechamento bem-sucedido
     */
    public void incrementClosingSuccess(YearMonth month) {
        if (closingSuccessCounter == null) {
            closingSuccessCounter = Counter.builder("closings.success.count")
                    .description("Number of successful monthly closings")
                    .tag("month", month.toString())
                    .register(meterRegistry);
        }
        closingSuccessCounter.increment();
        log.info("Closing success metric incremented for month: {}", month);
    }

    /**
     * Incrementa contador de falha de fechamento
     */
    public void incrementClosingFailure(YearMonth month, String reason) {
        if (closingFailureCounter == null) {
            closingFailureCounter = Counter.builder("closings.failure.count")
                    .description("Number of failed monthly closings")
                    .tag("month", month.toString())
                    .tag("reason", reason)
                    .register(meterRegistry);
        }
        closingFailureCounter.increment();
        log.info("Closing failure metric incremented for month: {} with reason: {}", month, reason);
    }

    /**
     * Incrementa contador de reabertura de fechamento
     */
    public void incrementClosingReopen(YearMonth month) {
        if (closingReopenCounter == null) {
            closingReopenCounter = Counter.builder("closings.reopen.count")
                    .description("Number of monthly closing reopens")
                    .tag("month", month.toString())
                    .register(meterRegistry);
        }
        closingReopenCounter.increment();
        log.info("Closing reopen metric incremented for month: {}", month);
    }

    /**
     * Registra tempo de execução de fechamento
     */
    public Timer.Sample startClosingTimer() {
        if (closingExecutionTimer == null) {
            closingExecutionTimer = Timer.builder("closings.execution.duration")
                    .description("Duration of monthly closing execution")
                    .register(meterRegistry);
        }
        return Timer.start(meterRegistry);
    }

    /**
     * Para o timer de execução de fechamento
     */
    public void stopClosingTimer(Timer.Sample sample) {
        if (sample != null && closingExecutionTimer != null) {
            sample.stop(closingExecutionTimer);
            log.info("Closing execution timer stopped");
        }
    }

    /**
     * Incrementa contador de importação de parceiros
     */
    public void incrementImportPartner(int count) {
        if (importPartnerCounter == null) {
            importPartnerCounter = Counter.builder("imports.partners.count")
                    .description("Number of partners imported")
                    .register(meterRegistry);
        }
        importPartnerCounter.increment(count);
        log.info("Import partners metric incremented by: {}", count);
    }

    /**
     * Incrementa contador de importação de clientes
     */
    public void incrementImportClient(int count) {
        if (importClientCounter == null) {
            importClientCounter = Counter.builder("imports.clients.count")
                    .description("Number of clients imported")
                    .register(meterRegistry);
        }
        importClientCounter.increment(count);
        log.info("Import clients metric incremented by: {}", count);
    }

    /**
     * Incrementa contador de importação bem-sucedida
     */
    public void incrementImportSuccess(String type) {
        if (importSuccessCounter == null) {
            importSuccessCounter = Counter.builder("imports.success.count")
                    .description("Number of successful imports")
                    .tag("type", type)
                    .register(meterRegistry);
        }
        importSuccessCounter.increment();
        log.info("Import success metric incremented for type: {}", type);
    }

    /**
     * Incrementa contador de falha de importação
     */
    public void incrementImportFailure(String type, String reason) {
        if (importFailureCounter == null) {
            importFailureCounter = Counter.builder("imports.failure.count")
                    .description("Number of failed imports")
                    .tag("type", type)
                    .tag("reason", reason)
                    .register(meterRegistry);
        }
        importFailureCounter.increment();
        log.info("Import failure metric incremented for type: {} with reason: {}", type, reason);
    }

    /**
     * Incrementa contador de logs de auditoria
     */
    public void incrementAuditLog(String action) {
        if (auditLogCounter == null) {
            auditLogCounter = Counter.builder("audit.logs.count")
                    .description("Number of audit logs created")
                    .tag("action", action)
                    .register(meterRegistry);
        }
        auditLogCounter.increment();
        log.info("Audit log metric incremented for action: {}", action);
    }

    /**
     * Incrementa contador de criação de parceiro
     */
    public void incrementPartnerCreated() {
        if (partnerCreatedCounter == null) {
            partnerCreatedCounter = Counter.builder("partners.created.count")
                    .description("Number of partners created")
                    .register(meterRegistry);
        }
        partnerCreatedCounter.increment();
        log.info("Partner created metric incremented");
    }

    /**
     * Incrementa contador de atualização de parceiro
     */
    public void incrementPartnerUpdated() {
        if (partnerUpdatedCounter == null) {
            partnerUpdatedCounter = Counter.builder("partners.updated.count")
                    .description("Number of partners updated")
                    .register(meterRegistry);
        }
        partnerUpdatedCounter.increment();
        log.info("Partner updated metric incremented");
    }

    /**
     * Incrementa contador de exclusão de parceiro
     */
    public void incrementPartnerDeleted() {
        if (partnerDeletedCounter == null) {
            partnerDeletedCounter = Counter.builder("partners.deleted.count")
                    .description("Number of partners deleted")
                    .register(meterRegistry);
        }
        partnerDeletedCounter.increment();
        log.info("Partner deleted metric incremented");
    }

    /**
     * Incrementa contador de criação de cliente
     */
    public void incrementClientCreated() {
        if (clientCreatedCounter == null) {
            clientCreatedCounter = Counter.builder("clients.created.count")
                    .description("Number of clients created")
                    .register(meterRegistry);
        }
        clientCreatedCounter.increment();
        log.info("Client created metric incremented");
    }

    /**
     * Incrementa contador de atualização de cliente
     */
    public void incrementClientUpdated() {
        if (clientUpdatedCounter == null) {
            clientUpdatedCounter = Counter.builder("clients.updated.count")
                    .description("Number of clients updated")
                    .register(meterRegistry);
        }
        clientUpdatedCounter.increment();
        log.info("Client updated metric incremented");
    }

    /**
     * Incrementa contador de exclusão de cliente
     */
    public void incrementClientDeleted() {
        if (clientDeletedCounter == null) {
            clientDeletedCounter = Counter.builder("clients.deleted.count")
                    .description("Number of clients deleted")
                    .register(meterRegistry);
        }
        clientDeletedCounter.increment();
        log.info("Client deleted metric incremented");
    }

    /**
     * Incrementa contador de criação de regra de comissão
     */
    public void incrementCommissionRuleCreated() {
        if (commissionRuleCreatedCounter == null) {
            commissionRuleCreatedCounter = Counter.builder("commission.rules.created.count")
                    .description("Number of commission rules created")
                    .register(meterRegistry);
        }
        commissionRuleCreatedCounter.increment();
        log.info("Commission rule created metric incremented");
    }

    /**
     * Incrementa contador de atualização de regra de comissão
     */
    public void incrementCommissionRuleUpdated() {
        if (commissionRuleUpdatedCounter == null) {
            commissionRuleUpdatedCounter = Counter.builder("commission.rules.updated.count")
                    .description("Number of commission rules updated")
                    .register(meterRegistry);
        }
        commissionRuleUpdatedCounter.increment();
        log.info("Commission rule updated metric incremented");
    }
}
