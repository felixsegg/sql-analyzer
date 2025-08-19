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

/**
 * Abstract base for list/overview windows of {@link logic.bdo.BusinessDomainObject}s.
 * Provides a ready-to-use {@link javafx.scene.control.ListView} with single selection,
 * external and internal filtering, context menu &amp; double-click/ENTER to open details,
 * and wired Add/Delete actions (guarded by the parent’s deletion workflow).
 * Subclasses supply the service via {@code getService()} and implement {@code addItem()}.
 * Intended for FXML controllers; use on the JavaFX Application Thread.
 *
 * @param <BDO> the domain type displayed in the overview
 * @author Felix Seggebäing
 * @since 1.0
 */
public abstract class OverviewWindow<BDO extends BusinessDomainObject> extends BDOWindow<BDO> {
    @FXML
    Button addBtn, deleteBtn;
    @FXML
    private HBox filterNodeContainer;
    @FXML
    protected ListView<BDO> listView;
    @FXML
    private TitledPane filterTitledPane;
    
    /**
     * Optional externally supplied predicate applied in addition to internal filters.
     * When non-null, pre-filters the list contents and can be removed via the UI.
     */
    private Predicate<BDO> externalFilter;
    
    /**
     * Set of dynamic predicates built from UI controls (internal filters).
     * All predicates are AND-composed; an empty set means no filtering.
     */
    private final Set<Predicate<BDO>> internalFilters = new HashSet<>();
    
    /**
     * Creates an overview window with an optional external filter applied
     * to the displayed items.
     *
     * @param externalFilter initial predicate to pre-filter items; may be {@code null}
     */
    public OverviewWindow(Predicate<BDO> externalFilter) {
        this.externalFilter = externalFilter;
    }
    
    /**
     * Initializes the overview window: calls {@code super.initialize}, sets the header
     * from {@link #getTitle()}, installs the external filter UI (if any), wires Add/Delete
     * actions, configures the list view behavior, and performs an initial {@link #refresh()}.
     *
     * @param location  FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Must be invoked by the FXML loader on the JavaFX Application Thread.
     */
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
    
    /**
     * Installs a UI indicator for the active external filter with a “Remove external filter”
     * button. Expands the filter pane, and upon removal clears the predicate and refreshes
     * the list. No-op if no external filter is set.
     *
     * @implNote Must run on the JavaFX Application Thread.
     */
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
    
    /**
     * Configures the overview list: renders items via {@code toString()}, enforces single
     * selection, enables/disables the Delete button with selection, opens details on
     * double-click or ENTER, deletes on DELETE, and attaches a context menu with
     * “Show details” and “Delete” actions (tied to selection).
     *
     * @implNote Invoke on the JavaFX Application Thread.
     */
    private void initializeListView() {
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
    
    /**
     * Reloads the list contents from the backing service, applying the external
     * filter (if present) and the AND-composed internal filters, then sorts by
     * {@code toString()}. Restores the previous selection when still available.
     *
     * @implNote Invoke on the JavaFX Application Thread.
     */
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
    
    /**
     * Adds an internal filter predicate together with its UI control and label
     * to the filter area, sizing the control to fill available width.
     *
     * @param filterLiteral predicate to include in the internal AND-composed filters
     * @param filterControl UI control that configures the predicate
     * @param filterName    label shown beneath the control
     * @implNote Listener wiring (e.g., calling {@link #refresh()}) should be done by the caller.
     */
    private void addFilter(Predicate<BDO> filterLiteral, Control filterControl, String filterName) {
        internalFilters.add(filterLiteral);
        Label label = new Label(filterName);
        label.setFont(new Font(10.0));
        VBox vBox = new VBox(filterControl, label);
        HBox.setHgrow(vBox, Priority.ALWAYS);
        filterControl.prefWidthProperty().bind(vBox.widthProperty());
        filterNodeContainer.getChildren().add(vBox);
    }
    
    /**
     * Adds a drop-down filter for a derived attribute of each item. The combo box lists
     * all distinct values (with total occurrence counts) plus a “no selection” entry; changing
     * the selection triggers {@link #refresh()}. Filtering matches items whose derived
     * value is the selected one (by reference identity).
     *
     * @param <T>        value type produced by {@code func}
     * @param func       mapping from item to the attribute to filter by
     * @param filterName label shown beneath the combo box
     * @implNote Options are populated from current data with cardinalities; equality check uses {@code ==}.
     */
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
    
    /**
     * Adds a case-insensitive substring filter driven by a text field. Typing triggers
     * {@link #refresh()}; empty input disables the filter.
     *
     * @param func       maps an item to the string to search in (expected non-null)
     * @param filterName label shown beneath the text field
     * @implNote The predicate calls {@code func.apply(bdo).toLowerCase()}, so {@code func} should
     * not return {@code null} for eligible items.
     */
    protected void addStringFilter(Function<BDO, String> func, String filterName) {
        TextField textField = new TextField();
        textField.textProperty().addListener(obs -> refresh());
        Predicate<BDO> filter = bdo ->
                textField.getText() == null
                        || textField.getText().isBlank()
                        || func.apply(bdo).toLowerCase().contains(textField.getText().toLowerCase());
        addFilter(filter, textField, filterName);
    }
    
    /**
     * Builds a frequency map of derived values across all items from the service.
     * Values mapped to {@code null} are ignored.
     *
     * @param <T>  the derived value type
     * @param func maps an item to the value to count
     * @return a map from distinct values to their occurrence count
     * @implNote Uses {@code groupingBy} with {@code equals}/hash semantics over the current snapshot.
     */
    private <T> Map<T, Integer> getAllTsWithCardinality(Function<BDO, T> func) {
        return getService().getAll().stream().filter(bdo -> func.apply(bdo) != null).collect(Collectors.groupingBy(func, Collectors.summingInt(bdo -> 1)));
    }
    
    /**
     * Deletes the currently selected item after passing the guarded deletion check,
     * then refreshes the list. No-op if nothing is selected or deletion is cancelled.
     *
     * @implNote Invoked by FXML as an event handler.
     */
    @FXML
    private void deleteItem() {
        BDO selection = listView.getSelectionModel().getSelectedItem();
        
        if (selection != null && requestDeletion(selection)) {
            getService().delete(selection);
            refresh();
        }
    }
    
    /**
     * Opens the details view for a newly created {@code BDO} instance (i.e., “Add” flow).
     * Creation/persistence should occur only if the user confirms/saves in the details view.
     *
     * @implNote Typically delegates to {@code WindowManager.openDetails(...)} with a fresh BDO.
     * Invoked by FXML (e.g., Add button).
     */
    @FXML
    protected abstract void addItem();
}
