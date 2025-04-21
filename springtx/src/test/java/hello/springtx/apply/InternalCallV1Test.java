package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired CallService callService;

    @Test
    void printProxy() {
        log.info("callService: {}", callService.getClass());
        Assertions.assertThat(AopUtils.isAopProxy(callService)).isTrue();
    }

    @Test
    void internalCall() {
        callService.internal();
        callService.internalV2();
    }

    @Test
    void externalCall() {
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig {
        @Bean
        CallService callService() {
            return new CallService();
        }
    }

    static class CallService {

        public void external() {
            log.info("call external");
            printTxInfo();
            internal(); // <-
        }

        @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }

        @Transactional
        private void internalV2() {
            // 접근제한자 트랜잭션 무시 확인
            // 스프링부트 3.0 부터는 'protected','package-visible'(default 접근제한자)에도 트랜잭션이 적용된다
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("txActive: {}", txActive);
        }
    }

}
