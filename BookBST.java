// ============================================================
// DATA STRUCTURE 1 — Binary Search Tree
// ============================================================

/**
 * BookBST stores books in a Binary Search Tree keyed on isbn.
 * Insertion and search are O(log n) on a balanced tree.
 */
class BookBST {

    /** The root of the tree; null when the catalogue is empty. */
    private Book root;

    // ── Public API ──────────────────────────────────────────

    /**
     * Inserts a new Book into the BST.
     * Duplicate ISBNs are silently ignored.
     */
    public void insert(Book book) {
        root = insertRec(root, book);
    }

    /**
     * Searches for a book by isbn.
     *
     * @return the matching Book, or null if not found.
     */
    public Book search(int isbn) {
        return searchRec(root, isbn);
    }

    // ── Private recursive helpers ────────────────────────────

    /**
     * Recursively inserts {@code book} into the subtree rooted at
     * {@code node} and returns the (possibly new) root of that subtree.
     */
    private Book insertRec(Book node, Book book) {
        // Base case: empty slot found — place the book here
        if (node == null) {
            return book;
        }

        if (book.isbn < node.isbn) {
            // Go left for smaller ISBNs
            node.left = insertRec(node.left, book);
        } else if (book.isbn > node.isbn) {
            // Go right for larger ISBNs
            node.right = insertRec(node.right, book);
        }
        // Duplicate ISBN — do nothing

        return node;
    }

    /**
     * Recursively searches the subtree rooted at {@code node}
     * for a book whose isbn matches {@code isbn}.
     *
     * @return matching Book or null.
     */
    private Book searchRec(Book node, int isbn) {
        // Base case: subtree exhausted without a match
        if (node == null) {
            return null;
        }

        if (isbn == node.isbn) {
            return node; // Found!
        } else if (isbn < node.isbn) {
            return searchRec(node.left, isbn); // Search left subtree
        } else {
            return searchRec(node.right, isbn); // Search right subtree
        }
    }
}