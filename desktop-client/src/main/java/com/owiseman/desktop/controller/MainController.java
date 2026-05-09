package com.owiseman.desktop.controller;

import com.owiseman.desktop.service.AgentService;
import com.owiseman.document.model.Block;
import com.owiseman.document.model.Document;
import com.owiseman.document.model.Page;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainController {

    private final AgentService agentService = new AgentService();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private TextArea chatInput;
    private ListView<String> chatHistory;
    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private TreeView<String> documentTree;
    private Label statusLabel;
    private ListView<String> documentList;
    private final ObservableList<String> docItems = FXCollections.observableArrayList();

    public BorderPane createMainLayout(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        root.setTop(createToolBar(stage));
        root.setLeft(createDocumentPanel());
        root.setCenter(createChatPanel());
        root.setBottom(createStatusBar());

        setupDragDrop(root, stage);

        return root;
    }

    private ToolBar createToolBar(Stage stage) {
        ToolBar toolBar = new ToolBar();
        toolBar.setStyle("-fx-background-color: #16213e; -fx-padding: 8;");

        Button importBtn = new Button("Import Document");
        importBtn.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-background-radius: 4;");
        importBtn.setOnAction(e -> handleImport(stage));

        Button exportBtn = new Button("Export");
        exportBtn.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-background-radius: 4;");

        Separator sep = new Separator();

        Label title = new Label("  OfficeAgent Desktop");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#e94560"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolBar.getItems().addAll(importBtn, exportBtn, sep, title, spacer);
        return toolBar;
    }

    private VBox createDocumentPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(12));
        panel.setPrefWidth(280);
        panel.setStyle("-fx-background-color: #16213e;");

        Label label = new Label("Documents");
        label.setFont(Font.font("System", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#a8d8ea"));

        documentList = new ListView<>(docItems);
        documentList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setTextFill(Color.web("#e2e8f0"));
                        setStyle("-fx-background-color: transparent;");
                        setText(item);
                    }
                }
            };
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    showDocumentDetail(cell.getItem());
                }
            });
            return cell;
        });
        VBox.setVgrow(documentList, Priority.ALWAYS);

        TreeItem<String> rootItem = new TreeItem<>("Document IR");
        rootItem.setExpanded(true);
        documentTree = new TreeView<>(rootItem);
        documentTree.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: #e2e8f0;");
        documentTree.setShowRoot(true);
        VBox.setVgrow(documentTree, Priority.ALWAYS);

        panel.getChildren().addAll(label, documentList, new Separator(), documentTree);
        return panel;
    }

    private VBox createChatPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(12));
        panel.setStyle("-fx-background-color: #1a1a2e;");

        Label label = new Label("Agent Chat");
        label.setFont(Font.font("System", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#a8d8ea"));

        chatHistory = new ListView<>(messages);
        chatHistory.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setWrapText(true);
                    setPrefWidth(chatHistory.getWidth() - 20);
                    if (item.startsWith("[User]")) {
                        setTextFill(Color.web("#60a5fa"));
                        setStyle("-fx-background-color: #1e3a5f; -fx-background-radius: 8; -fx-padding: 8;");
                    } else if (item.startsWith("[Agent]")) {
                        setTextFill(Color.web("#34d399"));
                        setStyle("-fx-background-color: #1a3a2e; -fx-background-radius: 8; -fx-padding: 8;");
                    } else {
                        setTextFill(Color.web("#94a3b8"));
                        setStyle("-fx-background-color: transparent; -fx-padding: 4;");
                    }
                    setText(item);
                }
            }
        });
        VBox.setVgrow(chatHistory, Priority.ALWAYS);

        HBox inputBox = new HBox(8);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        chatInput = new TextArea();
        chatInput.setPromptText("Enter task, e.g.: Help me create a quarterly report PPT...");
        chatInput.setPrefRowCount(3);
        chatInput.setStyle("-fx-background-color: #16213e; -fx-text-fill: #e2e8f0; -fx-control-inner-background: #0f172a;");
        chatInput.setWrapText(true);
        HBox.setHgrow(chatInput, Priority.ALWAYS);

        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-background-radius: 4; -fx-font-weight: bold;");
        sendBtn.setPrefWidth(80);
        sendBtn.setOnAction(e -> handleSend());

        chatInput.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                handleSend();
            }
        });

        inputBox.getChildren().addAll(chatInput, sendBtn);
        panel.getChildren().addAll(label, chatHistory, inputBox);
        return panel;
    }

    private HBox createStatusBar() {
        HBox bar = new HBox(16);
        bar.setPadding(new Insets(6, 12, 6, 12));
        bar.setStyle("-fx-background-color: #16213e;");
        bar.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("Ready");
        statusLabel.setTextFill(Color.web("#94a3b8"));
        statusLabel.setFont(Font.font(11));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label versionLabel = new Label("OfficeAgent v1.0.0 | Java " + System.getProperty("java.version"));
        versionLabel.setTextFill(Color.web("#475569"));
        versionLabel.setFont(Font.font(10));

        bar.getChildren().addAll(statusLabel, spacer, versionLabel);
        return bar;
    }

    private void setupDragDrop(BorderPane root, Stage stage) {
        root.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });

        root.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    importFile(file.toPath());
                }
            }
            e.setDropCompleted(true);
            e.consume();
        });
    }

    private void handleImport(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Document");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Office Files", "*.pptx", "*.docx", "*.xlsx", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            importFile(file.toPath());
        }
    }

    private void importFile(Path path) {
        statusLabel.setText("Importing: " + path.getFileName());
        executor.submit(() -> {
            try {
                Document doc = agentService.importDocument(path);
                Platform.runLater(() -> {
                    docItems.add(doc.documentId() + " - " + path.getFileName());
                    updateDocumentTree(doc);
                    statusLabel.setText("Imported: " + path.getFileName() + " (" + doc.pages().size() + " pages)");
                    messages.add("[System] Document imported: " + path.getFileName() + " (ID: " + doc.documentId() + ")");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Import failed: " + e.getMessage());
                    messages.add("[System] Import failed: " + e.getMessage());
                });
            }
        });
    }

    private void updateDocumentTree(Document doc) {
        TreeItem<String> rootItem = documentTree.getRoot();
        TreeItem<String> docItem = new TreeItem<>(doc.documentId() + " [" + doc.documentType() + "]");
        docItem.setExpanded(true);

        for (Page page : doc.pages()) {
            TreeItem<String> pageItem = new TreeItem<>("Page " + page.pageIndex() + " (" + page.blocks().size() + " blocks)");
            for (Block block : page.blocks()) {
                String preview = block.content() != null && block.content().length() > 40
                        ? block.content().substring(0, 40) + "..."
                        : (block.content() != null ? block.content() : "empty");
                TreeItem<String> blockItem = new TreeItem<>(
                        block.semanticRole() + ": " + preview
                );
                pageItem.getChildren().add(blockItem);
            }
            docItem.getChildren().add(pageItem);
        }
        rootItem.getChildren().add(docItem);
    }

    private void handleSend() {
        String text = chatInput.getText().trim();
        if (text.isBlank()) return;

        messages.add("[User] " + text);
        chatInput.clear();
        statusLabel.setText("Processing...");

        executor.submit(() -> {
            try {
                var result = agentService.executeTask(text).join();
                Platform.runLater(() -> {
                    messages.add("[Agent] Task completed! Status: " + result.status() + ", Duration: " + result.durationMs() + "ms");
                    statusLabel.setText("Task completed");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    messages.add("[Agent] Error: " + e.getMessage());
                    statusLabel.setText("Task failed");
                });
            }
        });
    }

    private void showDocumentDetail(String item) {
        if (item == null) return;
        String docId = item.split(" - ")[0];
        Document doc = agentService.getDocument(docId);
        if (doc != null) {
            messages.add("[System] Document: " + doc.documentId() + ", Type: " + doc.documentType() + ", Pages: " + doc.pages().size());
        }
    }
}
