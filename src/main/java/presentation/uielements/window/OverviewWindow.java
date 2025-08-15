package presentation.uielements.window;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Callback;
import logic.bdo.BusinessDomainObject;
import presentation.util.WindowManager;

import java.net.URL;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public abstract class OverviewWindow<BDO extends BusinessDomainObject> extends BDOWindow<BDO> {
    @FXML
    Button addBtn, deleteBtn;
    @FXML
    private HBox filterNodeContainer;
    @FXML
    protected ListView<BDO> listView;
    @FXML
    private TitledPane filterTitledPane;
    
    private Predicate<BDO> externalFilter;
    private final Set<Predicate<BDO>> internalFilters = new HashSet<>();
    
    public OverviewWindow(Predicate<BDO> externalFilter) {
        this.externalFilter = externalFilter;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        
        setHeaderText(getTitle());
        
        initializeExternalFilter();
        
        addBtn.setOnAction(e -> addItem());
        deleteBtn.setOnAction(e -> deleteItem());
        
        initializeListView();
        refresh();
    }
    
    private void initializeExternalFilter() {
        if (externalFilter == null)
            return;
        
        Button removeBtn = new Button("Remove external filter");
        Label label = new Label("Externally set filter");
        label.setFont(new Font(10.0));
        
        VBox vBox = new VBox(removeBtn, label);
        
        filterNodeContainer.getChildren().add(vBox);
        filterTitledPane.setExpanded(true); // Initially expanded to show filter
        
        removeBtn.setOnAction(e -> {
            filterNodeContainer.getChildren().remove(vBox);
            externalFilter = null;
            refresh();
        });
    }
    
    private void initializeListView() {
        // Show the name of the sample query in the list view
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<BDO> call(ListView<BDO> listView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(BDO item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.toString());
                        }
                    }
                };
            }
        });
        
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV)
                        -> deleteBtn.setDisable(newV == null)
                );
        
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                BDO selection = listView.getSelectionModel().getSelectedItem();
                if (selection != null)
                    WindowManager.openDetails(selection);
            }
        });
        
        // Key events
        listView.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DELETE -> deleteItem();
                case ENTER -> {
                    BDO selection = listView.getSelectionModel().getSelectedItem();
                    if (selection != null)
                        WindowManager.openDetails(selection);
                }
            }
        });
        
        // Context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem openDetailsItem = new MenuItem("Show details");
        MenuItem deleteItem = new MenuItem("Delete");
        
        openDetailsItem.setOnAction(e -> {
            BDO selection = listView.getSelectionModel().getSelectedItem();
            if (selection != null)
                WindowManager.openDetails(selection);
        });
        
        deleteItem.setOnAction(e -> deleteItem());
        
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean disable = newVal == null;
            openDetailsItem.setDisable(disable);
            deleteItem.setDisable(disable);
        });
        
        contextMenu.getItems().addAll(openDetailsItem, deleteItem);
        listView.setContextMenu(contextMenu);
    }
    
    public void refresh() {
        BDO prevSelection = listView.getSelectionModel().getSelectedItem();
        listView.getItems().clear();
        listView.getItems().addAll(getService().getAll().stream()
                .filter(externalFilter == null ? bdo -> true : externalFilter) // External filter
                .filter(internalFilters.stream().reduce(Predicate::and).orElse(bdo -> true)) // Internal filters
                .sorted(Comparator.comparing(BusinessDomainObject::toString)).toList());
        if (prevSelection != null && listView.getItems().contains(prevSelection))
            listView.getSelectionModel().select(prevSelection);
    }
    
    private void addFilter(Predicate<BDO> filterLiteral, Control filterControl, String filterName) {
        internalFilters.add(filterLiteral);
        Label label = new Label(filterName);
        label.setFont(new Font(10.0));
        VBox vBox = new VBox(filterControl, label);
        HBox.setHgrow(vBox, Priority.ALWAYS);
        filterControl.prefWidthProperty().bind(vBox.widthProperty());
        filterNodeContainer.getChildren().add(vBox);
    }
    
    protected <T> void addObjectFilter(Function<BDO, T> func, String filterName) {
        ComboBox<Map.Entry<T, Integer>> comboBox = new ComboBox<>();
        comboBox.valueProperty().addListener(obs -> refresh());
        comboBox.getItems().setAll(getAllTsWithCardinality(func).entrySet());
        comboBox.getItems().add(0, null); // No selection is allowed
        Callback<ListView<Map.Entry<T, Integer>>, ListCell<Map.Entry<T, Integer>>> cellFactory =
                lv -> new ListCell<>() {
                    @Override
                    protected void updateItem(Map.Entry<T, Integer> entry, boolean empty) {
                        super.updateItem(entry, empty);
                        setText(empty || entry == null ? "" : entry.getKey() + " (" + entry.getValue() + ")");
                    }
                };
        
        comboBox.setCellFactory(cellFactory);
        comboBox.setButtonCell(cellFactory.call(null));
        
        Predicate<BDO> filter = bdo ->
                comboBox.getValue() == null
                        || comboBox.getValue().getKey() == func.apply(bdo);
        addFilter(filter, comboBox, filterName);
    }
    
    protected void addStringFilter(Function<BDO, String> func, String filterName) {
        TextField textField = new TextField();
        textField.textProperty().addListener(obs -> refresh());
        Predicate<BDO> filter = bdo ->
                textField.getText() == null
                        || textField.getText().isBlank()
                        || func.apply(bdo).toLowerCase().contains(textField.getText().toLowerCase());
        addFilter(filter, textField, filterName);
    }
    
    private <T> Map<T, Integer> getAllTsWithCardinality(Function<BDO, T> func) {
        return getService().getAll().stream().filter(bdo -> func.apply(bdo) != null).collect(Collectors.groupingBy(func, Collectors.summingInt(bdo -> 1)));
    }
    
    @FXML
    private void deleteItem() {
        BDO selection = listView.getSelectionModel().getSelectedItem();
        
        if (selection != null && requestDeletion(selection)) {
            getService().delete(selection);
            refresh();
        }
    }
    
    @FXML
    protected abstract void addItem();
}
