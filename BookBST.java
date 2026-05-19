// ============================================================
// DATA STRUCTURE 1 — Binary Search Tree
// ============================================================

/**
 * BookBST stores books in a Binary Search Tree keyed on isbn.
 * Insertion, search, and deletion are O(log n) on a balanced tree.
 */
class BookBST {

    /** The root of the tree; null when the catalogue is empty. */
    private Book root;

    public BookBST() {
        this.root = null; // Catalog starts empty
    }

    // ============================================================
    // PUBLIC API
    // ============================================================

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

    public Book getRoot() {
        return this.root;
    }

    /**
     * Deletes a book from the BST catalogue by its ISBN.
     * Used when a book is borrowed from the inventory.
     */
    public void delete(int isbn) {
        root = deleteRec(root, isbn);
    }

    /**
     * Optional utility helper method to display the catalog in sorted order 
     * using In-Order Traversal (prints from lowest ISBN to highest ISBN).
     */
    public void displayCatalog() {
        if (root == null) {
            System.out.println("The library catalogue is currently empty.");
            return;
        }
        System.out.println("\n--- Available Library Catalogue (Sorted by ISBN) ---");
        inOrderTraversal(root);
        System.out.println("-----------------------------------------------------");
    }

    // ============================================================
    // PRIVATE RECURSIVE HELPERS
    // ============================================================

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

    /**
     * Helper to recursively locate and structurally remove the target node,
     * maintaining the strict binary search structural layout.
     */
    private Book deleteRec(Book node, int isbn) {
        // Base case: item not found or tree empty
        if (node == null) {
            return null;
        }

        // Navigate the tree branches looking for the target ISBN
        if (isbn < node.isbn) {
            node.left = deleteRec(node.left, isbn);
        } else if (isbn > node.isbn) {
            node.right = deleteRec(node.right, isbn);
        } else {
            // Found the matching node to delete!

            // Case 1 & 2: Node has one child or zero children
            if (node.left == null) {
                return node.right;
            } else if (node.right == null) {
                return node.left;
            }

            // Case 3: Node has two children.
            // Find the In-Order Successor (absolute smallest key in the right subtree)
            Book successor = findMin(node.right);

            // Copy the successor's data values into this node position
            node.isbn = successor.isbn;
            node.title = successor.title;
            node.author = successor.author;

            // Recursively delete the old duplicate successor node from the right branch
            node.right = deleteRec(node.right, successor.isbn);
        }

        return node;
    }

    /**
     * Helper method to locate the absolute leftmost node in a given subtree branch.
     * This is required to find the in-order successor during complex deletions.
     */
    private Book findMin(Book node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    /**
     * Performs an in-order depth-first traversal utility stream.
     */
    private void inOrderTraversal(Book node) {
        if (node != null) {
            inOrderTraversal(node.left);
            System.out.printf("  ISBN: %-6d | Title: %-22s | Author: %s%n", node.isbn, node.title, node.author);
            inOrderTraversal(node.right);
        }
    }
}