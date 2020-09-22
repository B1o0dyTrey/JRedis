package com.github.bdqfork.core.transaction.backup;

/**
 * BackupStrategy工厂
 *
 * @author bdq
 * @since 2020/09/22
 */
public class BackupStrategyFactory {

    /**
     * 获取备份策略
     *
     * @param type 策略名
     * @return BackupStrategy 备份策略实例
     */
    public BackupStrategy getBackupStrategy(String type) {
        return new AlwaysBackup();
    }
}