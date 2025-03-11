package main.java.gui.components;

import main.java.App;
import main.java.City;
import main.java.grid.GridIndex;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class CityForm extends JPanel {
    private JTextField cityNameField, xCoordField, yCoordField;
    private JButton addCityButton;
    private final GridIndex gridIndex;
    private java.util.List<Runnable> cityListeners = new ArrayList<>();

    public CityForm(GridIndex gridIndex) {
        this.gridIndex = gridIndex;
        initializeComponents();
    }

    public void addCityListener(Runnable listener) {
        cityListeners.add(listener);
    }

    private void notifyCityListeners() {
        for (Runnable listener : cityListeners) {
            listener.run();
        }
    }

    private void initializeComponents() {
        setLayout(new GridLayout(0, 2));

        cityNameField = new JTextField(20);
        xCoordField = new JTextField(20);
        yCoordField = new JTextField(20);

        add(new JLabel("City Name:"));
        add(cityNameField);
        add(new JLabel("X Coordinate:"));
        add(xCoordField);
        add(new JLabel("Y Coordinate:"));
        add(yCoordField);

        addCityButton = new JButton("Add City");
        addCityButton.addActionListener(e -> addCity());
        add(addCityButton);
    }

    private void addCity() {
        String cityName = cityNameField.getText();
        String xCoord = xCoordField.getText();
        String yCoord = yCoordField.getText();

        try {
            int x = Integer.parseInt(xCoord);
            int y = Integer.parseInt(yCoord);

            if (!cityName.isEmpty() && !xCoord.isEmpty() && !yCoord.isEmpty()) {
                City city = new City(cityName, x, y);
                gridIndex.addCity(App.generateKey(),city);
                cityNameField.setText("");
                xCoordField.setText("");
                yCoordField.setText("");
                JOptionPane.showMessageDialog(this, "City added successfully!");
            }
            notifyCityListeners();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid coordinates", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


}