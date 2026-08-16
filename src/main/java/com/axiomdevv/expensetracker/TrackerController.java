package com.axiomdevv.expensetracker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Predicate;

public class TrackerController {
    @FXML private Circle doughnutHole;
    @FXML private AreaChart<String,Number> barChart;
    @FXML private TextField descriptionField,amountField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private DatePicker datePicker;
    @FXML private Label totalBalance;
    @FXML private TableView<Expense> table;
    @FXML private TableColumn<Expense,String> descriptionColumn,dateColumn,categoryColumn;
    @FXML private TableColumn<Expense,Double> amountColumn;
    @FXML private PieChart pieChart;
    @FXML private ToggleButton categoryToggle;
    @FXML private ToggleButton incomeExpenseToggle;
    @FXML private ToggleGroup chartToggleGroup;
    @FXML private Label totalExpenses , totalIncome;
    @FXML private ToggleButton dayToggle , monthToggle, yearToggle ,darkModeToggle;

    private static final Map<String, String> PIE_COLORS = Map.ofEntries(
            Map.entry("Housing",       "#0D6B3E"),
            Map.entry("Utilities",     "#67C48A"),
            Map.entry("Food",          "#084D2C"),
            Map.entry("Transport",     "#D46A6A"),
            Map.entry("Insurance",     "#23844F"),
            Map.entry("Debt",          "#8FE0B0"),
            Map.entry("Healthcare",    "#C99A00"),
            Map.entry("Entertainment", "#0B5A33"),
            Map.entry("Clothing",      "#B8C0BC"),
            Map.entry("Income",        "#23844F"),
            Map.entry("Expenses",      "#D46A6A")
    );



    private static final ObjectMapper mapper = new ObjectMapper();

    private static final File DATA_FILE = new File("data.json");

    ObservableList<Expense> data = FXCollections.observableArrayList();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public void initialize(){
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));


        table.setItems(data);

        categoryBox.setItems(FXCollections.observableArrayList(Category.toStringArray()));

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        checkDateFormat();

        addValidation(amountField , val -> val.matches("\\d+"));

        loadData(DATA_FILE);

        chartToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) oldToggle.setSelected(true);
        });

        Platform.runLater(() -> {
            //Here : we can use Node class but it doesn't have width and height as readable properties. Region is a subclass of Node that adds those , it represents any node that has a measurable size.
            Region legend = (Region)pieChart.lookup(".chart-legend");
            //lookup() is like CSS search, same as querySelector()
            if (legend != null) {
                doughnutHole.translateYProperty().bind(legend.heightProperty().divide(-2));
            }
        });
    }

    public void checkDateFormat(){

        datePicker.getEditor().focusedProperty().addListener((obs,wasFocused,isNowFocused) -> {
            if(!isNowFocused){
                String text = datePicker.getEditor().getText();

                if(datePicker.getValue() != null){
                    datePicker.getEditor().setStyle("-fx-border-color: green;");
                    return;
                }
                if(text.isEmpty()){
                    datePicker.getEditor().setStyle("");
                    return;
                }

                try {
                    LocalDate.parse(text,formatter);
                    datePicker.getEditor().setStyle("-fx-border-color: green;");
                } catch (Exception e) {
                    datePicker.getEditor().setStyle("-fx-border-color: red;");
                }
            }
        });

        datePicker.getEditor().setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if(newText.matches("[0-9/]*") && newText.length() <= 10){
                return change;
            }
            return null;
        }));

        datePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            if(newValue != null){
                datePicker.getEditor().setStyle("-fx-border-color: green;");
            }
        });
    }

    public void addValidation(TextField field , Predicate<String> isValid){
        field.textProperty().addListener((obs,oldVal,newVal) -> {
            if(newVal.isEmpty()){
                field.setStyle("");
            }else if(isValid.test(newVal)){
                field.setStyle("-fx-border-color: green;");
            }else{
                field.setStyle("-fx-border-color: red;");
            }

        });
    }



    public void addDataToTable(){
        // check first if fields aren't empty
        if(descriptionField.getText().trim().isEmpty() || datePicker.getValue() == null || categoryBox.getValue() == null || amountField.getText().trim().isEmpty()){
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText());
        } catch(NumberFormatException ex ){
            return;
        }

        amount = (categoryBox.getValue().equals(Category.INCOME.toString())) ? Math.abs(amount) : -Math.abs(amount);

        String date = formatter.format(datePicker.getValue());
        data.add(new Expense(descriptionField.getText(),date,Category.fromString(categoryBox.getValue()),amount));
        clearFields();

    }

    public void storeData(File dataFile) {
        // store data in json file
        List<Expense> snapshot = new ArrayList<>(data);
        new Thread(() -> {
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile , snapshot);
            }catch (IOException ioe) {
                System.out.println("IO exception : " + ioe);
            }
        }).start();

    }

    public void loadData(File dataFile) {
        // load data from the json file
        if (!dataFile.exists()) return ;
        new Thread( () -> {
            try {
                List<Expense> loaded = mapper.readValue(dataFile, new TypeReference<List<Expense>>() {}
                );
                Platform.runLater(() -> {
                            data.setAll(loaded);
                            updateStats();
                            updateChart();
                            updateBarChart();
                        }
                );
            } catch (IOException ioe) {
                System.out.println("IO exception : " + ioe);
            }
        }).start();

    }

    public void addData(){
        // should contain sotreData() and addDataToTable , and linked to add button
        addDataToTable();
        storeData(DATA_FILE);
        updateStats();
        updateChart();
        updateBarChart();

    }

    public void deleteData(){
        // delete one or multiple rows depending on what the user selected using table.getSelectionModel().getSelectedItems() store them in an observableList and then delete , and should be deleted also in json file
        // delete selected using the id
        List<Expense> toDelete = new ArrayList<>(table.getSelectionModel().getSelectedItems());
        data.removeAll(toDelete);
        table.getSelectionModel().clearSelection();
        storeData(DATA_FILE);
        updateStats();
        updateChart();
        updateBarChart();
    }

    public void updateStats(){
        double sum = data.stream().mapToDouble(Expense::getAmount).sum();
        double incomeSum = data.stream().filter( e -> e.getAmount() > 0).mapToDouble(Expense::getAmount).sum();
        double expensesSum = data.stream().filter( e -> e.getAmount() < 0).mapToDouble(Expense::getAmount).sum();


        totalBalance.setText(String.format("%s$%.2f" , sum < 0 ? "-" : "", Math.abs(sum)));
        totalIncome.setText(String.format("$%.2f" , incomeSum));
        totalExpenses.setText(String.format("$%.2f" , Math.abs(expensesSum)));
    }

    public void clearFields(){
        descriptionField.setText("");
        datePicker.setValue(null);
        datePicker.setPromptText("");
        categoryBox.setValue(null);
        amountField.setText("");
        datePicker.getEditor().setStyle("");
    }

    public void updateChart(){
        ObservableList<PieChart.Data> slices = FXCollections.observableArrayList();

        if(categoryToggle.isSelected()){
            Map<String,Double> totals = new LinkedHashMap<>();
            for(Expense e : data){
                if(e.getAmount() < 0){
                    totals.merge(e.getCategory().getDisplayName(),Math.abs(e.getAmount()),Double::sum);
                }
            }

            double tot = totals.values().stream().mapToDouble(Double::doubleValue).sum();

            totals.forEach((category,total)->{
                double percent = total > 0 ? (total/tot) * 100 : 0 ;
                String label = String.format("%s (%.0f%%)",category,percent);
                slices.add(new PieChart.Data(label,total));
            });

        }else if (incomeExpenseToggle.isSelected()){
            double income = 0;
            double expenses = 0;

            for(Expense e : data){
                if(e.getAmount() > 0){
                    income += e.getAmount();
                }else{
                    expenses += Math.abs(e.getAmount());
                }
            }

            double tot = income + expenses;

            if(income > 0) {
                double percent = (income/ tot) * 100;
                slices.add(new PieChart.Data(String.format("Income (%.0f%%)",percent),income));
            }
            if(expenses > 0) {
                double percent = (expenses/ tot) * 100;
                slices.add(new PieChart.Data(String.format("Expenses (%.0f%%)",percent),expenses));

            }
        }

        doughnutHole.setRadius(0);

        for (PieChart.Data d : slices) {
            d.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode == null) return;
                String name = d.getName().contains(" (")
                        ? d.getName().substring(0, d.getName().indexOf(" ("))
                        : d.getName();
                String color = PIE_COLORS.getOrDefault(name, "#B8C0BC");
                Platform.runLater(() -> newNode.setStyle("-fx-pie-color: " + color + ";"));
            });
        }

        pieChart.setData(slices);

        //Adding Zoom out Animation to the circle inside the pieChart so it goes with the slices animation
        Timeline grow = new Timeline(
                new KeyFrame(Duration.millis(500),
                        new KeyValue(doughnutHole.radiusProperty(), 65))
        );
        grow.play();

    }

    public void updateBarChart(){
        XYChart.Series<String,Number> series = new XYChart.Series<>();

        if(dayToggle.isSelected()){

            Map<DayOfWeek,Double> totals = new TreeMap<>();
            for(Expense e : data){
                if(e.getAmount() < 0){
                    LocalDate date = LocalDate.parse(e.getDate(),formatter);
                    totals.merge(date.getDayOfWeek(),Math.abs(e.getAmount()),Double::sum);
                }
            }
            totals.forEach((day,total) ->
                    series.getData().add(new XYChart.Data<>(day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), total))
            );
        }else if (monthToggle.isSelected()){
            Map<YearMonth,Double> totals = new TreeMap<>();
            for(Expense e : data){
                if(e.getAmount() < 0){
                    LocalDate date = LocalDate.parse(e.getDate(),formatter);
                    totals.merge(YearMonth.from(date),Math.abs(e.getAmount()),Double::sum);
                }
            }
            totals.forEach((yearMonth,total) ->
                    series.getData().add(new XYChart.Data<>(yearMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + yearMonth.getYear() , total))
            );
        }else if (yearToggle.isSelected()){
            Map<YearMonth,Double> totals = new TreeMap<>();
            for(Expense e : data){
                if(e.getAmount() < 0){
                    LocalDate date = LocalDate.parse(e.getDate(),formatter);
                    totals.merge(YearMonth.of(date.getYear(),1),Math.abs(e.getAmount()),Double::sum);
                }
            }
            totals.forEach((year,total) ->
                    series.getData().add(new XYChart.Data<>(String.valueOf(year.getYear()) , total))
            );
        }
        barChart.getData().clear();
        barChart.getData().add(series);

    }

    public void toggleDarkMode() {
        String css = getClass().getResource(darkModeToggle.isSelected() ? "DarkModeStyle.css" : "LightModeStyle.css").toExternalForm();
        table.getScene().getStylesheets().setAll(css);
        darkModeToggle.setText(darkModeToggle.isSelected() ? "☀ LightMode" : "🌙  Dark Mode");
        doughnutHole.setFill(darkModeToggle.isSelected() ? Color.web("#1E293B") : Color.web("#FFFFFF"));

    }
}