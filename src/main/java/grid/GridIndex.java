package main.java.grid;

import main.java.City;
import main.java.Location;
import main.java.Road;
import main.java.graph.Graph;

import java.util.Arrays;

public class GridIndex {
    private final int width;
    private final int height;
    private final Graph<String, City, Road> graph;

    private int[] horizontal_cuts;
    private int[] vertical_cuts;

    private int HORIZONTAL_CUT = 0;
    private int VERTICAL_CUT = 1;

    private int lastCut = HORIZONTAL_CUT;

    private City[][] grid_address;

    private class GridAddressIndexes {
        int x;
        int y;

        public GridAddressIndexes(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }


    public GridIndex(int width, int height) {
        this.width = width;
        this.height = height;
        this.graph = new Graph<>();
        this.horizontal_cuts = new int[]{0, width};
        this.vertical_cuts = new int[]{0, height};
        this.grid_address = new City[1][1];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getHorizontal_cuts() {
        return horizontal_cuts;
    }

    public int[] getVertical_cuts() {
        return vertical_cuts;
    }

    public Graph<String, City, Road> getGraph() {
        return graph;
    }

    public void addCity(String key, City city) {
        //TODO přidat kontrolu že na daném city.Location není už jiné city a zároveň že v daném místě není udělaný řez.
        if (isLocationOccupied(city.getLocation())) {
            throw new IllegalArgumentException("Location is already occupied by another city or a cut. City:" + city.toString());
        }
        graph.addVertex(key, city);
        if (shouldPerformCut(city))
            cut(city);
//        addCityToGridAddress(city);
        System.out.println("x: " + Arrays.toString(vertical_cuts));
        System.out.println("y: "+Arrays.toString(horizontal_cuts));

        mapAllCitiesToGridAddress();

        for (int j = 0; j < grid_address[0].length; j++) {
            for (int i = 0; i < grid_address.length; i++) {
                if (grid_address[i][j] != null) {
                    System.out.print(grid_address[i][j].getName() + " ");
                } else {
                    System.out.print("* ");
                }
            }
            System.out.println();
        }
    }

    private boolean isLocationOccupied(Location location) {
        for (City[] row : grid_address) {
            for (City city : row) {
                if (city != null && (Math.abs(city.getLocation().getX() - location.getX()) <= 1 || Math.abs(city.getLocation().getY() - location.getY()) <= 1)) {
                    return true;
                }
            }
        }
        for (int cut : horizontal_cuts) {
            if (Math.abs(cut - location.getY()) <= 1) {
                return true;
            }
        }
        for (int cut : vertical_cuts) {
            if (Math.abs(cut - location.getX()) <= 1) {
                return true;
            }
        }
        return false;
    }

    public void addRoad(String from, String to, Road road) {
        graph.addEdge(from, to, road);
    }

    public void findCityByCoordinates(int x, int y) {
        //TODO
    }

    public void findCityBySegment(int x1, int y1, int x2, int y2) {
        //TODO
    }

    private boolean shouldPerformCut(City city) {
        GridAddressIndexes cityGridAddressIndexes = getCityGridAddressIndexes(city);
        return grid_address[cityGridAddressIndexes.x][cityGridAddressIndexes.y] != null;
    }

    private GridAddressIndexes getCityGridAddressIndexes(City city){
        Location cityLocation = city.getLocation();
        int xIndex = getCityXIndexInGridAddress(cityLocation.getX());
        int yIndex = getCityYIndexInGridAddress(cityLocation.getY());
        return new GridAddressIndexes(xIndex,yIndex);
    }

    private int getCityXIndexInGridAddress(int cityX){
        for(int i = 0; i < vertical_cuts.length; i++) {
            if (cityX < vertical_cuts[i]) {
                 return i-1;
            }
        }
        return -1;
    }

    private int getCityYIndexInGridAddress(int cityY){
        for(int i = 0; i < horizontal_cuts.length; i++) {
            if (cityY < horizontal_cuts[i]) {
                 return i-1;
            }
        }
        return -1;
    }

    private void cut(City city) {
        if (lastCut == HORIZONTAL_CUT) {
            performVerticalCut(city);
            lastCut = VERTICAL_CUT;
        } else {
            performHorizontalCut(city);
            lastCut = HORIZONTAL_CUT;
        }
    }

    private void performHorizontalCut(City city) {
        //TODO
        // takže mám lokaci města
        // nejprve zjistím v jakém je sloupci tedy v horizontal_cuts najdu dvě za sebou jdoucí hodnoty mezi kterými leží
        // Pak najdu město které je nejblíže v tomto sektoru a mezi nima přibliže uprostřed udělám cut.
        // pak upravím hodnoty v horizontal_cuts
        // pak upravím hodnoty v grid_addresory

        GridAddressIndexes cityGridAddressIndexes = getCityGridAddressIndexes(city);
        City cityInGrid = grid_address[cityGridAddressIndexes.x][cityGridAddressIndexes.y];
        int inBetween = Math.round((cityInGrid.getLocation().getY() + city.getLocation().getY()) / 2.0f);

        // Check if inBetween intersects with any existing city
        for (int i = 0; i < grid_address.length; i++) {
            City existingCity = grid_address[i][cityGridAddressIndexes.x];
            if (existingCity != null && existingCity.getLocation().getY() == inBetween) {
                throw new IllegalArgumentException("Cut cannot intersect with an existing city at X: " + inBetween);
            }
        }

        int[] newHorizontalCuts = new int[horizontal_cuts.length + 1];
        int indexForInBetween = -1;
        for (int i = 1; i < horizontal_cuts.length; i++) {
            if (horizontal_cuts[i-1] < inBetween && horizontal_cuts[i] > inBetween) {
                indexForInBetween = i;
                break;
            }
        }
        System.arraycopy(horizontal_cuts, 0, newHorizontalCuts, 0, indexForInBetween);
        newHorizontalCuts[indexForInBetween] = inBetween;
        System.arraycopy(horizontal_cuts, indexForInBetween, newHorizontalCuts, indexForInBetween + 1, horizontal_cuts.length - indexForInBetween);
        horizontal_cuts = newHorizontalCuts;

        // vytvořím si nové o jedna větší pole

        grid_address = new City[grid_address.length][grid_address[0].length + 1];
    }

    private void performVerticalCut(City city) {
        GridAddressIndexes cityGridAddressIndexes = getCityGridAddressIndexes(city);
        City cityInGrid = grid_address[cityGridAddressIndexes.x][cityGridAddressIndexes.y];
        int inBetween = Math.round((cityInGrid.getLocation().getX() + city.getLocation().getX()) / 2.0f);

        // Check if inBetween intersects with any existing city
        for (int i = 0; i < grid_address.length; i++) {
            City existingCity = grid_address[i][cityGridAddressIndexes.y];
            if (existingCity != null && existingCity.getLocation().getX() == inBetween) {
                throw new IllegalArgumentException("Cut cannot intersect with an existing city at X: " + inBetween);
            }
        }

        int[] newWidthCuts = new int[vertical_cuts.length + 1];
        int indexForInBetween = -1;
        for (int i = 1; i < vertical_cuts.length; i++) {
            if (vertical_cuts[i-1] < inBetween && vertical_cuts[i] > inBetween) {
                indexForInBetween = i;
                break;
            }
        }
        System.arraycopy(vertical_cuts, 0, newWidthCuts, 0, indexForInBetween);
        newWidthCuts[indexForInBetween] = inBetween;
        System.arraycopy(vertical_cuts, indexForInBetween, newWidthCuts, indexForInBetween + 1, horizontal_cuts.length - indexForInBetween);
        vertical_cuts = newWidthCuts;

        grid_address = new City[grid_address.length+1][grid_address[0].length];
    }

    private void mapAllCitiesToGridAddress(){
        for (String key : graph.getVertices().keySet()) {
            City city = graph.getVertex(key);
            addCityToGridAddress(city);
        }
    }

    private void addCityToGridAddress(City city) {
        GridAddressIndexes cityGridAddressIndexes = getCityGridAddressIndexes(city);
        if(grid_address[cityGridAddressIndexes.x][cityGridAddressIndexes.y] != null) {
            throw new IllegalArgumentException("Index is already in use!");
        }
        grid_address[cityGridAddressIndexes.x][cityGridAddressIndexes.y] = city;
    }

}
