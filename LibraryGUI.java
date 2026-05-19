import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class LibraryGUI extends JFrame {

    private final SmartLibrary library;
    private final JTextArea displayArea;
    private final JTable bookTable;
    private final DefaultTableModel tableModel;
    private final String userRole; // Tracks the logged-in role ("Librarian" or "Student")

    // Constructor accepts the user's role from LoginGUI
    public LibraryGUI(String role) {
        this.userRole = role;
        
        // 1. Initialize the backend engine (automatically loads storage data)
        library = new SmartLibrary();

        // 2. Setup Frame / Layout Geometry
        setTitle("Smart Library Management System - (" + userRole + ")");
        setSize(850, 600); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window on user screen
        setLayout(new BorderLayout(10, 10));

        // 3. Header Title Block Layout
        JLabel headerLabel = new JLabel("Smart Library Management Dashboard (" + userRole + ")", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(headerLabel, BorderLayout.NORTH);

        // 4. JTable Setup (Top Component of Split Frame View)
        String[] columnNames = {"ISBN", "Title", "Author"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent users from altering table cells directly
            }
        };
        bookTable = new JTable(tableModel);
        bookTable.setFillsViewportHeight(true);
        bookTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bookTable.setRowHeight(25);
        
        JScrollPane tableScrollPane = new JScrollPane(bookTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Available Library Stock Catalogue"));

        // Load visual records into table straight from the loaded backend BST data
        populateTableFromBST(library.getCatalogue().getRoot());

        // 5. Text Terminal Monitor Setup (Bottom Component of Split Frame View)
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        displayArea.setBackground(new Color(245, 245, 245));

        JScrollPane consoleScrollPane = new JScrollPane(displayArea);
        consoleScrollPane.setBorder(BorderFactory.createTitledBorder("System Operations Log Console"));

        // Split view combination container
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, consoleScrollPane);
        splitPane.setDividerLocation(220); 
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 15, 15));
        add(splitPane, BorderLayout.CENTER);

        // 6. Action Control Sidebar Buttons Setup
        JPanel buttonPanel = new JPanel();
        
        JButton btnAdd = new JButton("Add Book");
        JButton btnSearch = new JButton("Search Book");
        JButton btnBorrow = new JButton("Borrow Book");
        JButton btnHistory = new JButton("View History");
        JButton btnClear = new JButton("Clear Console");
        JButton btnExit = new JButton("Exit System");

        Dimension buttonSize = new Dimension(145, 42);
        
        // Formulate authorized list of buttons dynamically depending on rules
        java.util.List<JButton> authorizedButtons = new java.util.ArrayList<>();
        
        boolean isStudent = "Student".equalsIgnoreCase(userRole);
        
        if (!isStudent) {
            authorizedButtons.add(btnAdd);     // Librarians get Add Book
        }
        
        authorizedButtons.add(btnSearch);      // Both get Search
        authorizedButtons.add(btnBorrow);      // Both get Borrow
        
        if (!isStudent) {
            authorizedButtons.add(btnHistory); // Only Librarians get View History
        }
        
        authorizedButtons.add(btnClear);       // Both get Clear
        authorizedButtons.add(btnExit);        // Both get Exit

        // Dynamically adjust grid rows to fit only authorized buttons tightly
        buttonPanel.setLayout(new GridLayout(authorizedButtons.size(), 1, 5, 12)); 
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 5));

        for (JButton btn : authorizedButtons) {
            btn.setPreferredSize(buttonSize);
            btn.setFont(new Font("Arial", Font.BOLD, 12));
            btn.setFocusPainted(false);

            if (btn == btnExit) {
                btn.setBackground(new Color(178, 34, 34)); // Firebrick Red
                btn.setForeground(Color.WHITE); 
            } else if (btn == btnClear) {
                btn.setBackground(new Color(105, 105, 105)); // Dim Gray
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(new Color(0, 104, 56)); // Forest Green
                btn.setForeground(Color.WHITE); 
            }
            buttonPanel.add(btn);
        }
        add(buttonPanel, BorderLayout.WEST);

        // 7. Interactive Controls Actions Wire-up

        // ADD BOOK BUTTON (Only processed if UI added it)
        btnAdd.addActionListener(e -> {
            JPanel inputFormPanel = new JPanel(new GridLayout(3, 2, 5, 10));
            JTextField isbnField = new JTextField();
            JTextField titleField = new JTextField();
            JTextField authorField = new JTextField();

            inputFormPanel.add(new JLabel("Book ISBN:"));
            inputFormPanel.add(isbnField);
            inputFormPanel.add(new JLabel("Book Title:"));
            inputFormPanel.add(titleField);
            inputFormPanel.add(new JLabel("Book Author:"));
            inputFormPanel.add(authorField);

            int result = JOptionPane.showConfirmDialog(LibraryGUI.this, inputFormPanel, 
                    "Catalog New Book Registry Form", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    String isbnStr = isbnField.getText().trim();
                    String title = titleField.getText().trim();
                    String author = authorField.getText().trim();

                    if (isbnStr.isEmpty() || title.isEmpty() || author.isEmpty()) {
                        JOptionPane.showMessageDialog(LibraryGUI.this, "[Error] All fields are mandatory.", "Entry Warning", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int isbn = Integer.parseInt(isbnStr);
                    runLibraryCommand(() -> library.addBook(isbn, title, author));
                    tableModel.addRow(new Object[]{isbn, title, author});
                    LibraryStorage.saveCatalogue(library.getCatalogue());

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(LibraryGUI.this, "[Error] ISBN must strictly be an integer!", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // SEARCH BOOK BUTTON
        btnSearch.addActionListener(e -> {
            try {
                String isbnStr = JOptionPane.showInputDialog(LibraryGUI.this, "Enter Book ISBN to search catalogue:");
                if (isbnStr == null || isbnStr.trim().isEmpty()) return; 
                
                int isbn = Integer.parseInt(isbnStr.trim());
                runLibraryCommand(() -> library.searchBook(isbn));
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(LibraryGUI.this, "[Error] ISBN must be an integer!", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // BORROW BOOK BUTTON
        btnBorrow.addActionListener(e -> {
            JPanel borrowPanel = new JPanel(new GridLayout(2, 2, 5, 10));
            JTextField isbnField = new JTextField();
            JTextField userField = new JTextField();

            // Auto-fills and locks down username input box if logged-in user is a Student
            if (isStudent) {
                userField.setText(LoginGUI.getLoggedInUsername()); 
                userField.setEditable(false);
            }

            borrowPanel.add(new JLabel("Book ISBN:"));
            borrowPanel.add(isbnField);
            borrowPanel.add(new JLabel("Student Username:"));
            borrowPanel.add(userField);

            int result = JOptionPane.showConfirmDialog(LibraryGUI.this, borrowPanel, 
                "Book Check-Out Process", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    String isbnStr = isbnField.getText().trim();
                    String username = userField.getText().trim();

                    if (isbnStr.isEmpty() || username.isEmpty()) {
                        JOptionPane.showMessageDialog(LibraryGUI.this, "[Error] Both fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int isbn = Integer.parseInt(isbnStr);
                    runLibraryCommand(() -> library.borrowBook(isbn, username));
            
                    LibraryStorage.saveCatalogue(library.getCatalogue());
                    LibraryStorage.saveHistory(library.getHistory());
            
                    // Refresh view grid table
                    tableModel.setRowCount(0);
                    populateTableFromBST(library.getCatalogue().getRoot());
            
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(LibraryGUI.this, "[Error] ISBN must be an integer!", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // HISTORY VIEWER BUTTON
        btnHistory.addActionListener(e -> runLibraryCommand(library::viewLatestHistory));

        // CONSOLE WIPE BUTTON
        btnClear.addActionListener(e -> displayArea.setText(""));

        // TERMINATION BUTTON
        btnExit.addActionListener(e -> System.exit(0));
    }

    private void populateTableFromBST(Book node) {
        if (node != null) {
            populateTableFromBST(node.left);
            tableModel.addRow(new Object[]{node.isbn, node.title, node.author});
            populateTableFromBST(node.right);
        }
    }

    private void runLibraryCommand(Runnable libraryTask) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream customPrintStream = new PrintStream(buffer);
        PrintStream standardConsoleOut = System.out;
        
        System.setOut(customPrintStream);
        try {
            libraryTask.run();
        } finally {
            System.out.flush();
            System.setOut(standardConsoleOut); 
        }
        
        displayArea.append(buffer.toString());
        displayArea.setCaretPosition(displayArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryGUI("Librarian").setVisible(true));
    }
}