// ============================================================
//  INTERFACE
// ============================================================

/**
 * LibraryADT defines the contract that every Smart Library
 * implementation must fulfil.
 */
interface LibraryADT {
    /** Adds a new book to the catalogue. */
    void addBook(int isbn, String title, String author);

    /** Borrows a book by ISBN and records it in the history. */
    void borrowBook(int isbn, String username);

    /** Displays the most recently borrowed books first. */
    void viewLatestHistory();

    /** Searches for a book by ISBN and prints the result. */
    void searchBook(int isbn);
}