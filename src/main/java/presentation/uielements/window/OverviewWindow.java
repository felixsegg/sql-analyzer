package presentation.uielements.window;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import logic.bdo.BusinessDomainObject;
import presentation.util.WindowManager;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

public abstract class OverviewWindow<T extends BusinessDomainObject> extends BDOWindow<T> {
    @FXML
    Button addBtn, deleteBtn;
    @FXML
    protected ListView<T> listView;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        
        setHeaderText(getTitle());
        
        addBtn.setOnAction(e -> addItem());
        deleteBtn.setOnAction(e -> deleteItem());
        
        initializeListView();
        
        refresh();
    }
    
    private void initializeListView() {
        // Show the name of the sample query in the list view
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<T> call(ListView<T> listView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getDisplayedName());
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
                T selection = listView.getSelectionModel().getSelectedItem();
                if (selection != null)
                    WindowManager.openDetailsWindow(selection, this);
            }
        });
        
        // Key events
        listView.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DELETE -> deleteItem();
                case ENTER -> {
                    T selection = listView.getSelectionModel().getSelectedItem();
                    if (selection != null)
                        WindowManager.openDetailsWindow(selection, this);
                }
            }
        });
        
        // Context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem openDetailsItem = new MenuItem("Show details");
        MenuItem deleteItem = new MenuItem("Delete");
        
        openDetailsItem.setOnAction(e -> {
            T selection = listView.getSelectionModel().getSelectedItem();
            if (selection != null)
                WindowManager.openDetailsWindow(selection, this);
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
    
    protected void refresh() {
        listView.getItems().clear();
        listView.getItems().addAll(getService().getAll());
        listView.getItems().sort(Comparator.comparing(BusinessDomainObject::getDisplayedName));
    }
    
    @FXML
    private void deleteItem() {
        T selection = listView.getSelectionModel().getSelectedItem();
        
        if (selection != null && requestDeletion(selection)) {
            getService().delete(selection);
            refresh();
        }
    }
    
    @FXML
    protected abstract void addItem();
}
