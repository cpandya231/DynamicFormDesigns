package ai.smartfac.logever.model;

public class Entry {
    private int entryId;
    private int historyEntryId;

    public Entry() {
    }

    public Entry(int entryId, int historyEntryId) {
        this.entryId = entryId;
        this.historyEntryId = historyEntryId;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public int getHistoryEntryId() {
        return historyEntryId;
    }

    public void setHistoryEntryId(int historyEntryId) {
        this.historyEntryId = historyEntryId;
    }
}
