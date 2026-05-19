// ============================================================
// ENTITY
// ============================================================

/**
 * Book is the fundamental node used by both the BST catalogue
 * and the borrow-history stack.
 */
class Book {
    int isbn;
    String title, author;

    // BST child pointers (used by BookBST only)
    Book left,right;

    /**
     * Constructs a Book with the given details.
     * left and right are implicitly null (no children yet).
     */
    Book(int isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.left = null;
        this.right = null;
    }
}