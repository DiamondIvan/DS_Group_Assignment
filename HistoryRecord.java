public class HistoryRecord {
    public int isbn;
    public String title;
    public String author;
    public String borrowedBy; // <-- Added tracking field

    public HistoryRecord(int isbn, String title, String author, String borrowedBy) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.borrowedBy = borrowedBy;
    }

    @Override
    public String toString() {
        return "ISBN: " + isbn + " | \"" + title + "\" by " + author + " [Borrowed by: " + borrowedBy + "]";
    }
}