package io.nuls.api.core.model;

public class AccountInfo {

    private String address;

    private String alias;

    private int type;

    private int txCount;

    private long totalOut;

    private long totalIn;

    private long consensusLock;

    private long timeLock;

    private long balance;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTxCount() {
        return txCount;
    }

    public void setTxCount(int txCount) {
        this.txCount = txCount;
    }

    public long getTotalOut() {
        return totalOut;
    }

    public void setTotalOut(long totalOut) {
        this.totalOut = totalOut;
    }

    public long getTotalIn() {
        return totalIn;
    }

    public void setTotalIn(long totalIn) {
        this.totalIn = totalIn;
    }

    public long getConsensusLock() {
        return consensusLock;
    }

    public void setConsensusLock(long consensusLock) {
        this.consensusLock = consensusLock;
    }

    public long getTimeLock() {
        return timeLock;
    }

    public void setTimeLock(long timeLock) {
        this.timeLock = timeLock;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }
}