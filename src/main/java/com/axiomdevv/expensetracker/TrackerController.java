package com.axiomdevv.expensetracker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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
    @FXML private AreaChart<String,Number> barChart;
    @FXML private TextField descriptionField,amountField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private DatePicker datePicker;
    @FXML private Button addButton,clearButton,deleteButton;
    @FXML private Label totalBalance;
    @FXML private TableView<Expense> table;
    @FXML private TableColumn<Expense,String> descriptionColumn,dateColumn,categoryColumn;
    @FXML private TableColumn<Expense,Double> amountColumn;
    @FXML private PieChart pieChart;
    @FXML private ToggleButton categoryToggle;
    @FXML private ToggleButton incomeExpenseToggle;
    @FXML private ToggleGroup chartToggleGroup;
    @FXML private Label totalExpenses , totalIncome , netSavings;
    @FXML private ToggleButton dayToggle , monthToggle, yearToggle ,darkModeToggle;
    @FXML private ToggleGroup barToggleGroup;




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
            totals.forEach((category,total)->{
                slices.add(new PieChart.Data(category,total));
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
            if(income > 0) slices.add(new PieChart.Data("Income",income));
            if(expenses > 0) slices.add(new PieChart.Data("Expenses",expenses));
        }

        pieChart.setData(slices);

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
    }





}