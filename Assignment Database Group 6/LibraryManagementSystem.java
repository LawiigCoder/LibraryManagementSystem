import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class LibraryManagementSystem extends JFrame {
    private static class Theme {
        static final Color BACKGROUND = new Color(0xF0F4F8);
        static final Color PANEL_BACKGROUND = Color.WHITE;
        static final Color PRIMARY_COLOR = new Color(0x3B82F6);
        static final Color TEXT_COLOR = new Color(0x334155);
        static final Color HEADER_TEXT_COLOR = new Color(0x0F172A);
        static final Color TABLE_HEADER_BACKGROUND = new Color(0xE2E8F0);
        static final Color BORDER_COLOR = new Color(0xCBD5E1);
        static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 12);
        static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 12);
        static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    }
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Library_DB";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "123";
    private static final String SELECT_BOOKS_SQL = "SELECT b.book_id, b.title, b.author, b.publisher, b.year_published, b.isbn, c.category_name, b.status FROM books b LEFT JOIN categories c ON b.category_id = c.category_id";
    private static final String SELECT_MEMBERS_SQL = "SELECT * FROM members";
    private static final String SELECT_TRANSACTIONS_SQL = "SELECT t.transaction_id, b.title AS book_title, m.name AS member_name, t.borrow_date, t.due_date, t.return_date, t.status, t.book_id, t.member_id FROM transactions t JOIN books b ON t.book_id = b.book_id JOIN members m ON t.member_id = m.member_id";
    private static final String SELECT_CATEGORIES_SQL = "SELECT * FROM categories";
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private DefaultTableModel bookTableModel, memberTableModel, transactionTableModel, categoryTableModel;
    private JTextField tfUsername, tfBookID, tfTitle, tfAuthor, tfPublisher, tfYear, tfISBN,
            tfMemberID, tfStudentCardID, tfMemberName, tfEmail, tfPhone,
            tfTransactionID, tfDueDate, tfReturnDate, tfTransactionBook, tfTransactionMember,
            tfCategoryID, tfCategoryName, tfBookStatus, tfMemberStatus, tfBorrowDate,
            tfTransactionBookId, tfTransactionMemberId;
    private JPasswordField pfPassword;
    private JComboBox<String> cbBookCategory, cbTransactionStatus;
    private JTable bookTable, memberTable, transactionTable, categoryTable;
    public LibraryManagementSystem() {
        setTitle("Library Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        if (!isDatabaseConnected()) {
            showErrorDialog("Database Connection Failed", "Cannot connect to the database. Please check credentials.");
            System.exit(1);
        }
        initComponents();
    }
    private void initComponents() {
        mainPanel.setBackground(Theme.BACKGROUND);
        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createDashboardPanel(), "Dashboard");
        mainPanel.add(createBookPanel(), "Books");
        mainPanel.add(createMemberPanel(), "Members");
        mainPanel.add(createTransactionPanel(), "Transactions");
        mainPanel.add(createCategoryPanel(), "Categories");
        add(mainPanel);
        cardLayout.show(mainPanel, "Login");
    }
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 2));
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Theme.BACKGROUND);
        wrapper.add(panel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JLabel titleLabel = new JLabel("Library System Login");
        titleLabel.setFont(Theme.TITLE_FONT);
        titleLabel.setForeground(Theme.HEADER_TEXT_COLOR);
        panel.add(titleLabel, gbc);
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        gbc.gridy++;
        panel.add(createStyledLabel("Username:"), gbc);
        gbc.gridy++;
        panel.add(createStyledLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        tfUsername = createStyledTextField(15);
        panel.add(tfUsername, gbc);
        gbc.gridy++;
        pfPassword = new JPasswordField(15);
        styleTextField(pfPassword);
        panel.add(pfPassword, gbc);
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JButton loginButton = createStyledButton("Login");
        loginButton.addActionListener(e -> loginAction());
        panel.add(loginButton, gbc);
        return wrapper;
    }
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        JLabel titleLabel = new JLabel("Library Dashboard", JLabel.CENTER);
        titleLabel.setFont(Theme.TITLE_FONT);
        titleLabel.setForeground(Theme.HEADER_TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(Theme.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0; gbc.gridy = 0;
        buttonPanel.add(createDashboardButton("Manage Books", "📚", e -> showPanel("Books")), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        buttonPanel.add(createDashboardButton("Manage Members", "👥", e -> showPanel("Members")), gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        buttonPanel.add(createDashboardButton("Manage Transactions", "↔️", e -> showPanel("Transactions")), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        buttonPanel.add(createDashboardButton("Manage Categories", "🏷️", e -> showPanel("Categories")), gbc);
        panel.add(buttonPanel, BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(Theme.BACKGROUND);
        southPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JButton logoutButton = createStyledButton("Logout");
        logoutButton.addActionListener(e -> {
            tfUsername.setText("");
            pfPassword.setText("");
            cardLayout.show(mainPanel, "Login");
        });
        southPanel.add(logoutButton);
        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }
    private JPanel createBookPanel() {
        String[] columns = {"ID", "Title", "Author", "Publisher", "Year", "ISBN", "Category", "Status"};
        Map<String, String> searchFields = new LinkedHashMap<>();
    		searchFields.put("ID", "b.book_id");
    		searchFields.put("Title", "b.title");
    		searchFields.put("Author", "b.author");
    		searchFields.put("Publisher", "b.publisher");
    		searchFields.put("Year", "b.year_published");
    		searchFields.put("ISBN", "b.isbn");
    		searchFields.put("Category", "c.category_name");
    		searchFields.put("Status", "b.status");
        bookTableModel = createNonEditableTableModel(columns);
        bookTable = new JTable(bookTableModel);
        styleTable(bookTable);
        tfBookID = createDisabledTextField();
        tfTitle = createStyledTextField(20);
        tfAuthor = createStyledTextField(20);
        tfPublisher = createStyledTextField(20);
        tfYear = createStyledTextField(20);
        tfISBN = createStyledTextField(20);
        cbBookCategory = createStyledComboBox();
        tfBookStatus = createDisabledTextField();
        tfBookStatus.setText("Available");
        JPanel formPanel = createFormPanel(new String[]{"Book ID:", "Title:", "Author:", "Publisher:", "Year:", "ISBN:", "Category:", "Status:"}, new JComponent[]{tfBookID, tfTitle, tfAuthor, tfPublisher, tfYear, tfISBN, cbBookCategory, tfBookStatus});
        JPanel buttonPanel = createCrudButtonPanel(e -> addBook(), e -> updateBook(), e -> deleteBook(), e -> clearBookForm());
        return createManagementPanel("Books Management", bookTable, formPanel, buttonPanel, searchFields, this::searchBooks);
    }
    private JPanel createMemberPanel() {
        String[] columns = {"ID", "Student Card ID", "Name", "Email", "Phone", "Membership Date", "Status"};
        Map<String, String> searchFields = new LinkedHashMap<>();
    		searchFields.put("ID", "member_id");
    		searchFields.put("Student Card ID", "student_card_id");
    		searchFields.put("Name", "name");
    		searchFields.put("Email", "email");
    		searchFields.put("Phone", "phone");
    		searchFields.put("Membership Date", "membership_date");
    		searchFields.put("Status", "status");
        memberTableModel = createNonEditableTableModel(columns);
        memberTable = new JTable(memberTableModel);
        styleTable(memberTable);
        tfMemberID = createDisabledTextField();
        tfStudentCardID = createStyledTextField(20);
        tfMemberName = createStyledTextField(20);
        tfEmail = createStyledTextField(20);
        tfPhone = createStyledTextField(20);
        tfMemberStatus = createStyledTextField(20);
        tfMemberStatus.setText("Active");
        JPanel formPanel = createFormPanel(new String[]{"Member ID:", "Student Card ID:", "Name:", "Email:", "Phone:", "Status:"}, new JComponent[]{tfMemberID, tfStudentCardID, tfMemberName, tfEmail, tfPhone, tfMemberStatus});
        JPanel buttonPanel = createCrudButtonPanel(e -> addMember(), e -> updateMember(), e -> deleteMember(), e -> clearMemberForm());
        return createManagementPanel("Members Management", memberTable, formPanel, buttonPanel, searchFields, this::searchMembers);
    }
    private JPanel createTransactionPanel() {
        String[] columns = {"ID", "Book", "Member", "Borrow Date", "Due Date", "Return Date", "Status", "Book ID", "Member ID"};
        Map<String, String> searchFields = new LinkedHashMap<>();
    		searchFields.put("ID", "t.transaction_id");
    		searchFields.put("Book", "b.title");
    		searchFields.put("Member", "m.name");
    		searchFields.put("Borrow Date", "t.borrow_date");
    		searchFields.put("Due Date", "t.due_date");
    		searchFields.put("Return Date", "t.return_date");
    		searchFields.put("Status", "t.status");
        transactionTableModel = createNonEditableTableModel(columns);
        transactionTable = new JTable(transactionTableModel);
        styleTable(transactionTable);
        transactionTable.removeColumn(transactionTable.getColumnModel().getColumn(8));
        transactionTable.removeColumn(transactionTable.getColumnModel().getColumn(7));
        tfTransactionID = createDisabledTextField();
        tfTransactionBook = createStyledTextField(20);
        tfTransactionBook.setEditable(false);
        tfTransactionBook.setBackground(Theme.TABLE_HEADER_BACKGROUND);
        JButton searchBookButton = createStyledButton("Search");
        JPanel bookPanel = new JPanel(new BorderLayout(5, 0));
        bookPanel.setOpaque(false);
        bookPanel.add(tfTransactionBook, BorderLayout.CENTER);
        bookPanel.add(searchBookButton, BorderLayout.EAST);
        tfTransactionMember = createStyledTextField(20);
        tfTransactionMember.setEditable(false);
        tfTransactionMember.setBackground(Theme.TABLE_HEADER_BACKGROUND);
        JButton searchMemberButton = createStyledButton("Search");
        JPanel memberPanel = new JPanel(new BorderLayout(5, 0));
        memberPanel.setOpaque(false);
        memberPanel.add(tfTransactionMember, BorderLayout.CENTER);
        memberPanel.add(searchMemberButton, BorderLayout.EAST);
        tfTransactionBookId = new JTextField();
        tfTransactionMemberId = new JTextField();
        tfBorrowDate = createDisabledTextField();
        tfBorrowDate.setText(LocalDate.now().toString());
        tfDueDate = createStyledTextField(20);
        tfDueDate.setText(LocalDate.now().plusWeeks(2).toString());
        tfReturnDate = createStyledTextField(20);
        cbTransactionStatus = createStyledComboBox(new String[]{"Borrowed", "Returned", "Overdue"});
        searchBookButton.addActionListener(e -> showBookSearchDialog());
        searchMemberButton.addActionListener(e -> showMemberSearchDialog());
        JPanel formPanel = createFormPanel(
                new String[]{"Transaction ID:", "Book:", "Member:", "Borrow Date:", "Due Date:", "Return Date:", "Status:"},
                new JComponent[]{tfTransactionID, bookPanel, memberPanel, tfBorrowDate, tfDueDate, tfReturnDate, cbTransactionStatus});
        JPanel buttonPanel = createCrudButtonPanel(e -> addTransaction(), e -> updateTransaction(), e -> deleteTransaction(), e -> clearTransactionForm());
        JButton returnButton = createStyledButton("Mark as Returned");
        returnButton.addActionListener(e -> markAsReturned());
        buttonPanel.add(returnButton);
        return createManagementPanel("Transactions Management", transactionTable, formPanel, buttonPanel, searchFields, this::searchTransactions);
    }
    private JPanel createCategoryPanel() {
        String[] columns = {"ID", "Category Name"};
        Map<String, String> searchFields = Map.of("Category ID", "category_id", "Category Name", "category_name");
        categoryTableModel = createNonEditableTableModel(columns);
        categoryTable = new JTable(categoryTableModel);
        styleTable(categoryTable);
        tfCategoryID = createDisabledTextField();
        tfCategoryName = createStyledTextField(20);
        JPanel formPanel = createFormPanel(new String[]{"Category ID:", "Category Name:"}, new JComponent[]{tfCategoryID, tfCategoryName});
        JPanel buttonPanel = createCrudButtonPanel(e -> addCategory(), e -> updateCategory(), e -> deleteCategory(), e -> clearCategoryForm());
        return createManagementPanel("Categories Management", categoryTable, formPanel, buttonPanel, searchFields, this::searchCategories);
    }
    @FunctionalInterface
    private interface SearchFunction { void search(String field, String term); }
    private JPanel createManagementPanel(String title, JTable table, JPanel formPanel, JPanel buttonPanel, Map<String, String> searchFields, SearchFunction searchFunction) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Theme.BACKGROUND);
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(Theme.TITLE_FONT);
        titleLabel.setForeground(Theme.HEADER_TEXT_COLOR);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Theme.PANEL_BACKGROUND);
        JComboBox<String> searchCombo = createStyledComboBox(searchFields.keySet().toArray(new String[0]));
        JTextField searchField = createStyledTextField(20);
        JButton searchButton = createStyledButton("Search");
        JButton resetButton = createStyledButton("Reset");
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText().trim();
            if (searchTerm.isEmpty()) { showErrorDialog("Search Error", "Search term cannot be empty."); return; }
            String selectedField = searchFields.get(searchCombo.getSelectedItem().toString());
            searchFunction.search(selectedField, searchTerm);
        });
        resetButton.addActionListener(e -> {
            searchField.setText("");
            loadDataBasedOnPanel(title);
        });
        searchPanel.add(createStyledLabel("Search By:"));
        searchPanel.add(searchCombo);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(resetButton);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        panel.add(topPanel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        JPanel bottomContainer = new JPanel(new BorderLayout(5, 5));
        bottomContainer.setOpaque(false);
        bottomContainer.add(formPanel, BorderLayout.CENTER);
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        buttonPanel.setOpaque(false);
        actionPanel.add(buttonPanel, BorderLayout.NORTH);
        JButton backButton = createStyledButton("Back to Dashboard");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backButtonPanel.setOpaque(false);
        backButtonPanel.add(backButton);
        actionPanel.add(backButtonPanel, BorderLayout.SOUTH);
        bottomContainer.add(actionPanel, BorderLayout.SOUTH);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, bottomContainer);
        splitPane.setResizeWeight(1.0);
        splitPane.setEnabled(false);
        splitPane.setDividerSize(8);
        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    public void setBorder(Border b) {}
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(Theme.BACKGROUND);
                        g.fillRect(0, 0, getSize().width, getSize().height);
                        super.paint(g);
                    }
                };
            }
        });
        splitPane.setBorder(null);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }
    private void loadDataBasedOnPanel(String title) {
        if (title.contains("Books")) loadBooksFromDB();
        else if (title.contains("Members")) loadMembersFromDB();
        else if (title.contains("Transactions")) loadTransactionsFromDB();
        else if (title.contains("Categories")) loadCategoriesFromDB();
    }
    private void styleTable(JTable table) {
        table.setFont(Theme.MAIN_FONT);
        table.setForeground(Theme.TEXT_COLOR);
        table.setSelectionBackground(Theme.PRIMARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(Theme.BORDER_COLOR);
        table.setRowHeight(25);
        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.TABLE_HEADER_BACKGROUND);
        header.setForeground(Theme.HEADER_TEXT_COLOR);
        header.setFont(Theme.BOLD_FONT);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    populateFormFromSelectedRow(table, selectedRow);
                }
            }
        });
    }
    private void populateFormFromSelectedRow(JTable table, int row) {
        if (table == bookTable) {
            tfBookID.setText(bookTableModel.getValueAt(row, 0).toString());
            tfTitle.setText(bookTableModel.getValueAt(row, 1).toString());
            tfAuthor.setText(bookTableModel.getValueAt(row, 2).toString());
            tfPublisher.setText(bookTableModel.getValueAt(row, 3).toString());
            tfYear.setText(bookTableModel.getValueAt(row, 4).toString());
            tfISBN.setText(bookTableModel.getValueAt(row, 5).toString());
            cbBookCategory.setSelectedItem(bookTableModel.getValueAt(row, 6).toString());
            tfBookStatus.setText(bookTableModel.getValueAt(row, 7).toString());
        } else if (table == memberTable) {
            tfMemberID.setText(memberTableModel.getValueAt(row, 0).toString());
            tfStudentCardID.setText(memberTableModel.getValueAt(row, 1).toString());
            tfMemberName.setText(memberTableModel.getValueAt(row, 2).toString());
            tfEmail.setText(memberTableModel.getValueAt(row, 3).toString());
            tfPhone.setText(memberTableModel.getValueAt(row, 4).toString());
            tfMemberStatus.setText(memberTableModel.getValueAt(row, 6).toString());
        } else if (table == transactionTable) {
            tfTransactionID.setText(transactionTableModel.getValueAt(row, 0).toString());
            tfTransactionBook.setText(transactionTableModel.getValueAt(row, 1).toString());
            tfTransactionMember.setText(transactionTableModel.getValueAt(row, 2).toString());
            tfBorrowDate.setText(transactionTableModel.getValueAt(row, 3).toString());
            tfDueDate.setText(transactionTableModel.getValueAt(row, 4).toString());
            tfReturnDate.setText(getOptionalString(transactionTableModel.getValueAt(row, 5)));
            cbTransactionStatus.setSelectedItem(transactionTableModel.getValueAt(row, 6).toString());
            tfTransactionBookId.setText(transactionTableModel.getValueAt(row, 7).toString());
            tfTransactionMemberId.setText(transactionTableModel.getValueAt(row, 8).toString());
        } else if (table == categoryTable) {
            tfCategoryID.setText(categoryTableModel.getValueAt(row, 0).toString());
            tfCategoryName.setText(categoryTableModel.getValueAt(row, 1).toString());
        }
    }
    private String getOptionalString(Object value) { return value != null ? value.toString() : ""; }
    private JPanel createFormPanel(String[] labels, JComponent[] components) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.PANEL_BACKGROUND);
        Border titledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR), "Details", 2, 2, Theme.BOLD_FONT, Theme.HEADER_TEXT_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); gbc.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; panel.add(createStyledLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; panel.add(components[i], gbc);
        }
        return panel;
    }
    private JPanel createCrudButtonPanel(ActionListener addAction, ActionListener updateAction, ActionListener deleteAction, ActionListener clearAction) {
        JPanel panel = new JPanel();
        panel.add(createStyledButton("Add", addAction));
        panel.add(createStyledButton("Update", updateAction));
        panel.add(createStyledButton("Delete", deleteAction));
        panel.add(createStyledButton("Clear", clearAction));
        return panel;
    }
    private JButton createDashboardButton(String text, String emoji, ActionListener actionListener) {
        JButton button = new JButton("<html><center>" + emoji + "<br>" + text + "</center></html>");
        button.setFont(Theme.BOLD_FONT);
        button.setPreferredSize(new Dimension(220, 80));
        styleButton(button);
        button.addActionListener(actionListener);
        return button;
    }
    private DefaultTableModel createNonEditableTableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }
    private JTextField createDisabledTextField() {
        JTextField textField = new JTextField();
        textField.setEditable(false);
        textField.setBackground(Theme.TABLE_HEADER_BACKGROUND);
        textField.setForeground(Theme.TEXT_COLOR);
        textField.setFont(Theme.MAIN_FONT);
        textField.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        return textField;
    }
    private void styleTextField(JComponent component) {
        component.setFont(Theme.MAIN_FONT);
        component.setForeground(Theme.TEXT_COLOR);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 5, 4, 5)));
    }
    private JTextField createStyledTextField(int columns) {
        JTextField tf = new JTextField(columns);
        styleTextField(tf);
        return tf;
    }
    private JComboBox<String> createStyledComboBox() { return createStyledComboBox(new String[]{}); }
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(Theme.MAIN_FONT);
        cb.setForeground(Theme.TEXT_COLOR);
        cb.setBackground(Color.WHITE);
        cb.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        return cb;
    }
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BOLD_FONT);
        label.setForeground(Theme.TEXT_COLOR);
        return label;
    }
    private JButton createStyledButton(String text, ActionListener listener) {
        JButton button = createStyledButton(text);
        button.addActionListener(listener);
        return button;
    }
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        styleButton(button);
        return button;
    }
    private void styleButton(JButton button) {
        button.setFont(Theme.BOLD_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    private void loginAction() {
        String username = tfUsername.getText();
        String password = new String(pfPassword.getPassword());
        if ("admin".equals(username) && "1234".equals(password)) cardLayout.show(mainPanel, "Dashboard");
        else showErrorDialog("Login Failed", "Invalid username or password.");
    }
    private void showPanel(String panelName) {
        switch (panelName) {
            case "Books": loadBooksFromDB(); loadCategoriesToComboBox(cbBookCategory); clearBookForm(); break;
            case "Members": loadMembersFromDB(); clearMemberForm(); break;
            case "Transactions":
                loadTransactionsFromDB();
                clearTransactionForm();
                break;
            case "Categories": loadCategoriesFromDB(); clearCategoryForm(); break;
        }
        cardLayout.show(mainPanel, panelName);
    }
    private void loadBooksFromDB() { loadDataIntoTable(SELECT_BOOKS_SQL, bookTableModel); }
    private void searchBooks(String field, String term) {
        String sql = SELECT_BOOKS_SQL + " WHERE " + field + " LIKE ?";
        loadDataIntoTable(sql, bookTableModel, "%" + term + "%");
    }
    private void addBook() {
        String sql = "INSERT INTO books (title, author, publisher, year_published, isbn, category_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tfTitle.getText());
            ps.setString(2, tfAuthor.getText());
            ps.setString(3, tfPublisher.getText());
            ps.setString(4, tfYear.getText());
            ps.setString(5, tfISBN.getText());
            ps.setInt(6, getCategoryId(conn, cbBookCategory.getSelectedItem().toString()));
            ps.setString(7, tfBookStatus.getText());
            ps.executeUpdate();
            showMessageDialog("Success", "Book added successfully!");
            loadBooksFromDB();
            clearBookForm();
        } catch (SQLException ex) { showErrorDialog("Database Error", "Failed to add book: " + ex.getMessage()); }
    }
    private void updateBook() {
        if (bookTable.getSelectedRow() == -1) { showErrorDialog("Selection Error", "Please select a book to update."); return; }
        String sql = "UPDATE books SET title=?, author=?, publisher=?, year_published=?, isbn=?, category_id=?, status=? WHERE book_id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tfTitle.getText());
            ps.setString(2, tfAuthor.getText());
            ps.setString(3, tfPublisher.getText());
            ps.setString(4, tfYear.getText());
            ps.setString(5, tfISBN.getText());
            ps.setInt(6, getCategoryId(conn, cbBookCategory.getSelectedItem().toString()));
            ps.setString(7, tfBookStatus.getText());
            ps.setInt(8, Integer.parseInt(tfBookID.getText()));
            ps.executeUpdate();
            showMessageDialog("Success", "Book updated successfully!");
            loadBooksFromDB();
        } catch (SQLException ex) { showErrorDialog("Database Error", "Failed to update book: " + ex.getMessage()); }
    }
    private void deleteBook() {
        if (bookTable.getSelectedRow() == -1) { showErrorDialog("Selection Error", "Please select a book to delete."); return; }
        if (confirmDialog("Are you sure you want to delete this book?")) {
            String sql = "DELETE FROM books WHERE book_id=?";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, Integer.parseInt(tfBookID.getText()));
                ps.executeUpdate();
                showMessageDialog("Success", "Book deleted successfully!");
                loadBooksFromDB();
                clearBookForm();
            } catch (SQLException ex) { showErrorDialog("Database Error", "Failed to delete book. It may have associated transactions."); }
        }
    }
    private void clearBookForm() {
        tfBookID.setText(""); tfTitle.setText(""); tfAuthor.setText(""); tfPublisher.setText("");
        tfYear.setText(""); tfISBN.setText(""); cbBookCategory.setSelectedIndex(-1);
        tfBookStatus.setText("Available"); bookTable.clearSelection();
    }
    private void loadMembersFromDB() { loadDataIntoTable(SELECT_MEMBERS_SQL, memberTableModel); }
    private void searchMembers(String field, String term) {
        String sql = SELECT_MEMBERS_SQL + " WHERE " + field + " LIKE ?";
        loadDataIntoTable(sql, memberTableModel, "%" + term + "%");
    }
    private void addMember() {
        String sql = "INSERT INTO members (student_card_id, name, email, phone, membership_date, status) VALUES (?, ?, ?, ?, CURDATE(), ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tfStudentCardID.getText());
            ps.setString(2, tfMemberName.getText());
            ps.setString(3, tfEmail.getText());
            ps.setString(4, tfPhone.getText());
            ps.setString(5, tfMemberStatus.getText());
            ps.executeUpdate();
            showMessageDialog("Success", "Member added successfully!");
            loadMembersFromDB();
            clearMemberForm();
        } catch (SQLException ex) { showErrorDialog("Database Error", "Failed to add member: " + ex.getMessage()); }
    }
    private void updateMember() {
        if (memberTable.getSelectedRow() == -1) { showErrorDialog("Selection Error", "Please select a member to update."); return; }
        String sql = "UPDATE members SET student_card_id=?, name=?, email=?, phone=?, status=? WHERE member_id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tfStudentCardID.getText());
            ps.setString(2, tfMemberName.getText());
            ps.setString(3, tfEmail.getText());
            ps.setString(4, tfPhone.getText());
            ps.setString(5, tfMemberStatus.getText());
            ps.setInt(6, Integer.parseInt(tfMemberID.getText()));
            ps.executeUpdate();
            showMessageDialog("Success", "Member updated successfully!");
            loadMembersFromDB();
        } catch (SQLException ex) { showErrorDialog("Database Error", "Failed to update member: " + ex.getMessage()); }
    }
    private void deleteMember() {
        if (memberTable.getSelectedRow() == -1) { showErrorDialog("Selection Error", "Please select a member to delete."); return; }
        if (confirmDialog("Are you sure you want to delete this member?")) {
            String sql = "DELETE FROM members WHERE member_id=?";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, Integer.parseInt(tfMemberID.getText()));
                ps.executeUpdate();
                showMessageDialog("Success", "Member deleted successfully!");
                loadMembersFromDB();
                clearMemberForm();
            } catch (SQLException ex) { showErrorDialog("Database Error", "Failed to delete member. They may have transaction history."); }
        }
    }
    private void clearMemberForm() {
        tfMemberID.setText(""); tfStudentCardID.setText(""); tfMemberName.setText("");
        tfEmail.setText(""); tfPhone.setText(""); tfMemberStatus.setText("Active");
        memberTable.clearSelection();
    }
    private void loadTransactionsFromDB() { loadDataIntoTable(SELECT_TRANSACTIONS_SQL, transactionTableModel); }
    private void searchTransactions(String field, String term) {
        String sql = SELECT_TRANSACTIONS_SQL + " WHERE " + field + " LIKE ?";
        loadDataIntoTable(sql, transactionTableModel, "%" + term + "%");
    }
    private void addTransaction() {
        String bookIdStr = tfTransactionBookId.getText();
        String memberIdStr = tfTransactionMemberId.getText();

        if (bookIdStr.isEmpty() || memberIdStr.isEmpty()) {
            showErrorDialog("Input Error", "Please select a book and a member using the search buttons.");
            return;
        }
        String sql = "INSERT INTO transactions (book_id, member_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int bookId = Integer.parseInt(bookIdStr);
                int memberId = Integer.parseInt(memberIdStr);
                ps.setInt(1, bookId);
                ps.setInt(2, memberId);
                ps.setDate(3, Date.valueOf(LocalDate.now()));
                ps.setDate(4, Date.valueOf(tfDueDate.getText()));
                ps.setString(5, "Borrowed");
                ps.executeUpdate();
                updateBookStatus(conn, bookId, "Borrowed");
                conn.commit();
                showMessageDialog("Success", "Transaction recorded successfully!");
                loadTransactionsFromDB();
                clearTransactionForm();
            } catch (SQLException ex) { conn.rollback(); showErrorDialog("Database Error", "Failed to add transaction: " + ex.getMessage()); }
        } catch (SQLException ex) { showErrorDialog("Database Error", "Database connection error: " + ex.getMessage()); }
    }
    private void updateTransaction() {
        if (transactionTable.getSelectedRow() == -1) { showErrorDialog("Selection Error", "Please select a transaction to update."); return; }
        String sql = "UPDATE transactions SET due_date=?, return_date=?, status=? WHERE transaction_id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tfDueDate.getText()));
            ps.setDate(2, tfReturnDate.getText().isEmpty() ? null : Date.valueOf(tfReturnDate.getText()));
            ps.setString(3, cbTransactionStatus.getSelectedItem().toString());
            ps.setInt(4, Integer.parseInt(tfTransactionID.getText()));
            ps.executeUpdate();
            if ("Returned".equals(cbTransactionStatus.getSelectedItem().toString())) {
                int bookId = Integer.parseInt(tfTransactionBookId.getText());
                updateBookStatus(conn, bookId, "Available");
            }
            showMessageDialog("Success", "Transaction updated successfully!");
            loadTransactionsFromDB();
        } catch (SQLException ex) { showErrorDialog("Database Error", "Failed to update transaction: " + ex.getMessage()); }
    }
    private void deleteTransaction() {
    if (transactionTable.getSelectedRow() == -1) { 
        showErrorDialog("Selection Error", "Please select a transaction to delete."); 
        return; 
    }
    String status = transactionTableModel.getValueAt(transactionTable.getSelectedRow(), 6).toString();
    if ("Borrowed".equals(status)) {
        showErrorDialog("Deletion Error", "Cannot delete a transaction that is still borrowed. Please mark it as returned first.");
        return;
    }
    if (confirmDialog("Are you sure you want to delete this transaction?")) {
        String sql = "DELETE FROM transactions WHERE transaction_id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int bookId = getBookIdFromTransaction(conn, Integer.parseInt(tfTransactionID.getText()));
            ps.setInt(1, Integer.parseInt(tfTransactionID.getText()));
            ps.executeUpdate();
            updateBookStatus(conn, bookId, "Available");
            showMessageDialog("Success", "Transaction deleted successfully!");
            loadTransactionsFromDB();
            clearTransactionForm();
        } catch (SQLException ex) { 
            showErrorDialog("Database Error", "Failed to delete transaction: " + ex.getMessage()); 
        }
    }
}
    private void markAsReturned() {
    if (transactionTable.getSelectedRow() == -1) { 
        showErrorDialog("Selection Error", "Please select a transaction to mark as returned."); 
        return; 
    }
    String currentStatus = transactionTableModel.getValueAt(transactionTable.getSelectedRow(), 6).toString();
    
    if ("Returned".equals(currentStatus)) {
        showErrorDialog("Status Error", "This transaction is already marked as returned.");
        return;
    }
    tfReturnDate.setText(LocalDate.now().toString());
    cbTransactionStatus.setSelectedItem("Returned");
    updateTransaction();
}
    private void clearTransactionForm() {
        tfTransactionID.setText("");
        tfTransactionBook.setText("");
        tfTransactionBookId.setText("");
        tfTransactionMember.setText("");
        tfTransactionMemberId.setText("");
        tfBorrowDate.setText(LocalDate.now().toString());
        tfDueDate.setText(LocalDate.now().plusWeeks(2).toString());
        tfReturnDate.setText("");
        cbTransactionStatus.setSelectedItem("Borrowed");
        transactionTable.clearSelection();
    }
    private void loadCategoriesFromDB() {loadDataIntoTable(SELECT_CATEGORIES_SQL + " ORDER BY category_id ASC", categoryTableModel); }
    private void searchCategories(String field, String term) {
    	String sql = SELECT_CATEGORIES_SQL + " WHERE " + field + " LIKE ? ORDER BY category_id ASC";
    	loadDataIntoTable(sql, categoryTableModel, "%" + term + "%");
    }
    private void addCategory() {
        String sql = "INSERT INTO categories (category_name) VALUES (?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tfCategoryName.getText());
            ps.executeUpdate();
            showMessageDialog("Success", "Category added successfully!");
            loadCategoriesFromDB();
            clearCategoryForm();
        } catch (SQLException ex) { showErrorDialog("Database Error", "Failed to add category: " + ex.getMessage()); }
    }
    private void updateCategory() {
        if (categoryTable.getSelectedRow() == -1) { showErrorDialog("Selection Error", "Please select a category to update."); return; }
        String sql = "UPDATE categories SET category_name=? WHERE category_id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tfCategoryName.getText());
            ps.setInt(2, Integer.parseInt(tfCategoryID.getText()));
            ps.executeUpdate();
            showMessageDialog("Success", "Category updated successfully!");
            loadCategoriesFromDB();
        } catch (SQLException ex) { showErrorDialog("Database Error", "Failed to update category: " + ex.getMessage()); }
    }
    private void deleteCategory() {
        if (categoryTable.getSelectedRow() == -1) { showErrorDialog("Selection Error", "Please select a category to delete."); return; }
        if (confirmDialog("Are you sure you want to delete this category?")) {
            String sql = "DELETE FROM categories WHERE category_id=?";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, Integer.parseInt(tfCategoryID.getText()));
                ps.executeUpdate();
                showMessageDialog("Success", "Category deleted successfully!");
                loadCategoriesFromDB();
                clearCategoryForm();
            } catch (SQLException ex) { showErrorDialog("Database Error", "Failed to delete category. It may have books assigned to it."); }
        }
    }
    private void clearCategoryForm() { tfCategoryID.setText(""); tfCategoryName.setText(""); categoryTable.clearSelection(); }
    public Connection getConnection() throws SQLException { return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS); }
    private void loadDataIntoTable(String sql, DefaultTableModel model, Object... params) {
        model.setRowCount(0);
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                int columnCount = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) row[i] = rs.getObject(i + 1);
                    model.addRow(row);
                }
            }
        } catch (SQLException e) { showErrorDialog("Database Error", "Failed to load data: " + e.getMessage()); }
    }
    private void showBookSearchDialog() {
        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("Book ID", "book_id");
	criteria.put("Title", "title");
        criteria.put("Author", "author");
        String[] result = showSearchDialog("Select a Book",
                "SELECT book_id, title, author FROM books WHERE status = 'Available'",
                criteria);
        if (result != null) {
            tfTransactionBookId.setText(result[0]);
            tfTransactionBook.setText(result[1]);
        }
    }
    private void showMemberSearchDialog() {
        Map<String, String> criteria = new LinkedHashMap<>();
	criteria.put("Member ID", "member_id");
        criteria.put("Name", "name");
        criteria.put("Student Card ID", "student_card_id");
        

        String[] result = showSearchDialog("Select a Member",
                "SELECT member_id, name, student_card_id FROM members WHERE status = 'Active'",
                criteria);
        if (result != null) {
            tfTransactionMemberId.setText(result[0]);
            tfTransactionMember.setText(result[1]);
        }
    }
    private String[] showSearchDialog(String dialogTitle, String baseQuery, Map<String, String> searchCriteria) {
        JDialog dialog = new JDialog(this, dialogTitle, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(5, 5));
        final String[][] result = {null};
        DefaultTableModel tableModel = createNonEditableTableModel(new String[]{});
        JTable table = new JTable(tableModel);
        styleTable(table);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JComboBox<String> criteriaCombo = createStyledComboBox(searchCriteria.keySet().toArray(new String[0]));
        JTextField searchField = createStyledTextField(20);
        JButton searchButton = createStyledButton("Search");
        JButton resetButton = createStyledButton("Reset");
        searchPanel.add(createStyledLabel("Search By:"));
        searchPanel.add(criteriaCombo);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(resetButton);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton selectButton = createStyledButton("Select");
        JButton cancelButton = createStyledButton("Cancel");
        bottomPanel.add(selectButton);
        bottomPanel.add(cancelButton);
        loadDataIntoDialogTable(baseQuery, tableModel);
        searchButton.addActionListener(e -> {
            String selectedCriterion = criteriaCombo.getSelectedItem().toString();
            String dbColumn = searchCriteria.get(selectedCriterion);
            String searchTerm = searchField.getText().trim();
            String query;
            if (searchTerm.isEmpty()) {
                showErrorDialog("Search Error", "Please enter a search term.");
                return;
            }
            if (baseQuery.toLowerCase().contains("where")) {
                query = baseQuery + " AND " + dbColumn + " LIKE ?";
            } else {
                query = baseQuery + " WHERE " + dbColumn + " LIKE ?";
            }
            loadDataIntoDialogTable(query, tableModel, "%" + searchTerm + "%");
        });
        resetButton.addActionListener(e -> {
            searchField.setText("");
            criteriaCombo.setSelectedIndex(0);
            loadDataIntoDialogTable(baseQuery, tableModel);
        });
        selectButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String id = tableModel.getValueAt(selectedRow, 0).toString();
                String displayValue = tableModel.getValueAt(selectedRow, 1).toString();
                result[0] = new String[]{id, displayValue};
                dialog.dispose();
            } else {
                showErrorDialog("Selection Error", "Please select an item from the list.");
            }
        });
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.add(searchPanel, BorderLayout.NORTH);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
        return result[0];
    }
    private void loadDataIntoDialogTable(String sql, DefaultTableModel model, Object... params) {
        model.setRowCount(0);
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                int columnCount = rs.getMetaData().getColumnCount();
                String[] columnNames = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    columnNames[i] = rs.getMetaData().getColumnLabel(i + 1).replace("_", " ").toUpperCase();
                }
                model.setColumnIdentifiers(columnNames);

                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) row[i] = rs.getObject(i + 1);
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            showErrorDialog("Database Error", "Failed to load search data: " + e.getMessage());
        }
    }
    private int getCategoryId(Connection conn, String categoryName) throws SQLException {
        String sql = "SELECT category_id FROM categories WHERE category_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("category_id");
                throw new SQLException("Category not found: " + categoryName);
            }
        }
    }
    private int getBookIdFromTransaction(Connection conn, int transactionId) throws SQLException {
        String sql = "SELECT book_id FROM transactions WHERE transaction_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("book_id");
                throw new SQLException("Transaction not found: " + transactionId);
            }
        }
    }
    private void updateBookStatus(Connection conn, int bookId, String status) throws SQLException {
        String sql = "UPDATE books SET status = ? WHERE book_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookId);
            ps.executeUpdate();
        }
    }
    private void loadItemsToComboBox(String sql, JComboBox<String> comboBox) {
        comboBox.removeAllItems();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) comboBox.addItem(rs.getString(1));
        } catch (SQLException e) { showErrorDialog("Database Error", "Failed to load items into combo box: " + e.getMessage()); }
    }
    private void loadCategoriesToComboBox(JComboBox<String> comboBox) { loadItemsToComboBox("SELECT category_name FROM categories", comboBox); }

    private boolean isDatabaseConnected() {
        try (Connection conn = getConnection()) { return conn != null; }
        catch (SQLException e) { return false; }
    }
    private void showErrorDialog(String title, String message) { JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE); }
    private void showMessageDialog(String title, String message) { JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE); }
    private boolean confirmDialog(String message) { return JOptionPane.showConfirmDialog(this, message, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION; }
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { e.printStackTrace(); }
        SwingUtilities.invokeLater(() -> new LibraryManagementSystem().setVisible(true));
    }
}