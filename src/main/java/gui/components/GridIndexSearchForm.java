package main.java.gui.components;

import main.java.City;
import main.java.grid.GridIndex;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GridIndexSearchForm extends JPanel{

    private final GridIndex gridIndex;
    private final GraphPanel graphPanel;
    private java.util.List<Runnable> listeners = new ArrayList<>();
    JTextField x1;
    JTextField y1;
    JTextField x2;
    JTextField y2;

    JTextField x3; //TODO rename
    JTextField y3;


    public GridIndexSearchForm(GridIndex gridIndex, GraphPanel graphPanel) {
        this.gridIndex = gridIndex;
        this.graphPanel = graphPanel;
        initComponent();
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    private void initComponent() {
        setLayout(new GridLayout(0, 3));
        x1 = new JTextField(20);
        y1 = new JTextField(20);
        x2 = new JTextField(20);
        y2 = new JTextField(20);
        JLabel xLabel = new JLabel("X:");
        JLabel yLabel = new JLabel("Y:");

        add(xLabel);
        add(x1);
        add(x2);
        add(yLabel);
        add(y1);
        add(y2);

        JButton searchButton = new JButton("Hledat");
        searchButton.addActionListener(e -> {search();});
        add(searchButton);

        x3 = new JTextField(20);
        y3 = new JTextField(20);
        JLabel xLabel3 = new JLabel("bodové vyhledávání:");
        JButton serachPoint = new JButton("Hledat");
        JLabel nothing = new JLabel("");
        JLabel nothing2 = new JLabel("");
        add(nothing);
        add(nothing2);
        serachPoint.addActionListener(e -> {searchPoint();});
        add(xLabel3);
        add(x3);
        add(y3);
        add(serachPoint);
    };

    private void search() {
        int x1 = Integer.parseInt(this.x1.getText());
        int y1 = Integer.parseInt(this.y1.getText());
        int x2 = Integer.parseInt(this.x2.getText());
        int y2 = Integer.parseInt(this.y2.getText());

        if(x1 < 0 || x1 >= gridIndex.getWidth() || y1 < 0 || y1 >= gridIndex.getHeight() || x2 < 0 || x2 >= gridIndex.getWidth() || y2 < 0 || y2 >= gridIndex.getHeight()) {
            JOptionPane.showMessageDialog(null, "Neplatné souřadnice");
            return;
        }

        ArrayList<City> cities = gridIndex.findCityBySegment(x1, y1, x2, y2);

        graphPanel.setCitiesSearchedInGraphIndex(cities);
        graphPanel.setSearchDimensions(x1, y1, x2, y2);
        graphPanel.repaint();
        notifyListeners();
    }

    private void searchPoint() {
        int x = Integer.parseInt(this.x3.getText());
        int y = Integer.parseInt(this.y3.getText());

        if(x < 0 || x >= gridIndex.getWidth() || y < 0 || y >= gridIndex.getHeight()) {
            JOptionPane.showMessageDialog(null, "Neplatne souradnice");
        }

        City city = gridIndex.findCityByCoordinates(x,y);
        graphPanel.setFoundCity(city);
        graphPanel.repaint();

        if(city == null)
            JOptionPane.showMessageDialog(null,"Mesto nebylo nalezeno");
    }
}
