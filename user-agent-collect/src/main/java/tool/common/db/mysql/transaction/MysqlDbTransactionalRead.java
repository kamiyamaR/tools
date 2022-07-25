package tool.common.db.mysql.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 読み込みトランザクション用アノテーション.<br>
 * @author kamiyama ryohei
 *
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE, ElementType.METHOD })
@Transactional(transactionManager = "mysqlDbTransactionManager", readOnly = true, propagation = Propagation.REQUIRED, rollbackFor = {
        Throwable.class })
public @interface MysqlDbTransactionalRead {
}
