package main.java.grid;

import main.java.City;
import main.java.Location;
import main.java.Road;
import main.java.graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;

public class GridIndex {
    private final int width;
    private final int height;
    private final Graph<String, City, Road> graph;

    private int[] horizontal_cuts; // z leva do prava
    private int[] vertical_cuts; // z vrchu dolu

    private static final int HORIZONTAL_CUT = 0;
    private static final int VERTICAL_CUT = 1;

    private int lastCut = HORIZONTAL_CUT;

    private City[][] grid_address;

    private static class GridAddressIndexes {
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
        this.horizontal_cuts = new int[]{0, height};
        this.vertical_cuts = new int[]{0, width};
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

    public void addCity(String key, City city) throws IllegalArgumentException {
        if(city.getLocation().getX() >= width || city.getLocation().getY() >= height) {
            throw new IllegalArgumentException("City is out of bounds");
        }

        if (isLocationOccupied(city.getLocation())) {
            System.out.println("Location is ocupaied, throwing exception");
            throw new IllegalArgumentException("Location is already occupied by another city or a cut. City:" + city.toString());
        }

        if (shouldPerformCut(city)) {
            cut(city);
        }

        graph.addVertex(key, city);

        mapAllCitiesToGridAddress();

        printGrid();
    }

    private void printGrid() {
        System.out.println("x: " + Arrays.toString(vertical_cuts));
        System.out.println("y: " + Arrays.toString(horizontal_cuts));


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
                if (city != null && (Math.abs(city.getLocation().getX() - location.getX()) <= 0 && Math.abs(city.getLocation().getY() - location.getY()) <= 0)) {
                    System.out.println("City: " + city.getName() + " is on the same location as new city: " + location.toString());
                    return true;
                }
            }
        }
        for (int cut : horizontal_cuts) {
            if (Math.abs(cut - location.getY()) <= 0) {
                System.out.println("Horizontal cut is on this location: " + cut);
                return true;
            }
        }
        for (int cut : vertical_cuts) {
            if (Math.abs(cut - location.getX()) <= 0) {
                System.out.println("Vertical cut is on this location: " + cut);
                return true;
            }
        }
        return false;
    }

    public void addRoad(String from, String to, Road road) {
        graph.addEdge(from, to, road);
    }

    public City findCityByCoordinates(int x, int y) throws IllegalArgumentException {
        City foundCity = null;
        for(City city : graph.getVertices().values()){
            if(city.getLocation().getX() == x && city.getLocation().getY() == y){
                foundCity = city;
                break;
            }
        }
        return foundCity;
    }

    public ArrayList<City> findCityBySegment(int x1, int y1, int x2, int y2) {

        if (x1 < 0 || x1 > width || y1 < 0 || y1 > height || x2 < 0 || x2 > width || y2 < 0 || y2 > height) {
            throw new IllegalArgumentException("Coordinates are out of bounds");
        }

        int xStartIndex = -1;
        int xEndIndex = -1;
        for (int i = 0; i < vertical_cuts.length; i++) {
            if (vertical_cuts[i] > x1) {
                xStartIndex = i - 1;
            }
            if (vertical_cuts[i] >= x2) {
                xEndIndex = i;
            }
        }

        int yStartIndex = -1;
        int yEndIndex = -1;

        for (int i = 0; i < horizontal_cuts.length; i++) {
            if (horizontal_cuts[i] > y1) {
                yStartIndex = i - 1;
            }
            if (horizontal_cuts[i] >= y2) {
                yEndIndex = i;
            }
        }

        ArrayList<City> cities = new ArrayList<>();

        for (int i = xStartIndex; i < xEndIndex; i++) {
            for (int j = yStartIndex; j < yEndIndex; j++) {
                if (grid_address[i][j] != null && isCityInSearchDimensions(grid_address[i][j], x1, y1, x2, y2)) {
                    cities.add(grid_address[i][j]);
                }
            }
        }

        return cities;
    }

    private boolean isCityInSearchDimensions(City city, int x1, int y1, int x2, int y2) {
        return city.getLocation().getX() >= x1 && city.getLocation().getY() >= y1 && city.getLocation().getX() <= x2 && city.getLocation().getY() <= y2;
    }

    private boolean shouldPerformCut(City city) {
        GridAddressIndexes cityGridAddressIndexes = getCityGridAddressIndexes(city);
        return grid_address[cityGridAddressIndexes.x][cityGridAddressIndexes.y] != null;
    }

    private GridAddressIndexes getCityGridAddressIndexes(City city) {
        Location cityLocation = city.getLocation();
        int xIndex = getCityXIndexInGridAddress(cityLocation.getX());
        int yIndex = getCityYIndexInGridAddress(cityLocation.getY());
        return new GridAddressIndexes(xIndex, yIndex);
    }

    private int getCityXIndexInGridAddress(int cityX) {
        for (int i = 0; i < vertical_cuts.length; i++) {
            if (cityX < vertical_cuts[i]) {
                return i - 1;
            }
        }
        return -1;
    }

    private int getCityYIndexInGridAddress(int cityY) {
        for (int i = 0; i < horizontal_cuts.length; i++) {
            if (cityY < horizontal_cuts[i]) {
                return i - 1;
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

    private boolean canAddToInBetween(int cityInGrid, int city, int inBetween) {
        if (Math.abs(cityInGrid - city) > 1)
            if (inBetween < cityInGrid && inBetween > city)
                return (Math.abs(cityInGrid - inBetween) > 1);
        if ((inBetween > cityInGrid && inBetween < city))
            return Math.abs(city - inBetween) > 1;
        return false;
    }

    private boolean canRetrieveFromInBetween(int cityInGrid, int city, int inBetween) {
        if (Math.abs(cityInGrid - city) > 1)
            if (inBetween < cityInGrid && inBetween > city)
                return (Math.abs(city - inBetween) > 1);
        if ((inBetween > cityInGrid && inBetween < city))
            return Math.abs(cityInGrid - inBetween) > 1;
        return false;
    }

    private void performHorizontalCut(City city) {

        GridAddressIndexes cityGridAddressIndexes = getCityGridAddressIndexes(city);
        City cityInGrid = grid_address[cityGridAddressIndexes.x][cityGridAddressIndexes.y];
        int inBetween = Math.round((cityInGrid.getLocation().getY() + city.getLocation().getY()) / 2.0f);

        boolean didChangeInBetween = false;

        do {
            didChangeInBetween = false;
            // Check if inBetween intersects with any existing city
            for (City existingCity : graph.getVertices().values()) {
                if (existingCity != null && existingCity.getLocation().getY() == inBetween) {

                    //  v případě že řez prochází nějakým městem, tak se pokusit posunout řez, jinak výjmka, jelilkož řez nejde provézt
                    if (canAddToInBetween(cityInGrid.getLocation().getY(), city.getLocation().getY(), inBetween)) {
                        inBetween++;
                        didChangeInBetween = true;
                        break;
                    } else if (canRetrieveFromInBetween(cityInGrid.getLocation().getY(), city.getLocation().getY(), inBetween)) {
                        inBetween--;
                        didChangeInBetween = true;
                        break;
                    } else {
                        throw new IllegalArgumentException("Cut cannot intersect with an existing city at Y: " + inBetween);
                    }

                }
            }
        } while (didChangeInBetween);

        System.out.println("Performing new vertical cut at y: " + inBetween + " between " + cityInGrid.getName() + " y: " + cityInGrid.getLocation().getY() + " and " + city.getName() + " y: " + city.getLocation().getY());

        int[] newHorizontalCuts = new int[horizontal_cuts.length + 1];
        int indexForInBetween = -1;
        for (int i = 1; i < horizontal_cuts.length; i++) {
            if (horizontal_cuts[i - 1] < inBetween && horizontal_cuts[i] > inBetween) {
                indexForInBetween = i;
                break;
            }
        }

        // nakopíruju pole po index nového řezu
        System.arraycopy(horizontal_cuts, 0, newHorizontalCuts, 0, indexForInBetween);
        newHorizontalCuts[indexForInBetween] = inBetween;
        // nakopíruju pole od nového řezu dál
        System.arraycopy(horizontal_cuts, indexForInBetween, newHorizontalCuts, indexForInBetween + 1, horizontal_cuts.length - indexForInBetween);
        horizontal_cuts = newHorizontalCuts;

        // vytvořím si nové pole s o jedna delšími sloupci
        grid_address = new City[grid_address.length][grid_address[0].length + 1];
    }

    private void performVerticalCut(City city) {
        GridAddressIndexes cityGridAddressIndexes = getCityGridAddressIndexes(city);
        City cityInGrid = grid_address[cityGridAddressIndexes.x][cityGridAddressIndexes.y];
        int inBetween = Math.round((cityInGrid.getLocation().getX() + city.getLocation().getX()) / 2.0f);

        boolean didChangeInBetween = false;
        do {
            didChangeInBetween = false;
            // Check if inBetween intersects with any existing city
            for (City existingCity : graph.getVertices().values()) {
//                City existingCity = grid_address[i][cityGridAddressIndexes.y];
                if (existingCity != null && existingCity.getLocation().getX() == inBetween) {

                    //  v případě že řez prochází nějakým městem, tak se pokusit posunout řez, jinak výjmka, jelilkož řez nejde provézt
                    if (canAddToInBetween(cityInGrid.getLocation().getX(), city.getLocation().getX(), inBetween)) {
                        inBetween++;
                        didChangeInBetween = true;
                        break;
                    } else if (canRetrieveFromInBetween(cityInGrid.getLocation().getX(), city.getLocation().getX(), inBetween)) {
                        inBetween--;
                        didChangeInBetween = true;
                        break;
                    } else {
                        throw new IllegalArgumentException("Cut cannot intersect with an existing city at X: " + inBetween);
                    }
                }
            }
        } while (didChangeInBetween);


        System.out.println("Performing new vertical cut at x: " + inBetween + " between " + cityInGrid.getName() + " x: " + cityInGrid.getLocation().getX() + " and " + city.getName() + " x: " + city.getLocation().getX());

        int[] newWidthCuts = new int[vertical_cuts.length + 1];
        int indexForInBetween = -1;
        for (int i = 1; i < vertical_cuts.length; i++) {
            if (vertical_cuts[i - 1] < inBetween && vertical_cuts[i] > inBetween) {
                indexForInBetween = i;
                break;
            }
        }
        System.arraycopy(vertical_cuts, 0, newWidthCuts, 0, indexForInBetween);
        newWidthCuts[indexForInBetween] = inBetween;
        System.arraycopy(vertical_cuts, indexForInBetween, newWidthCuts, indexForInBetween + 1, horizontal_cuts.length - indexForInBetween);
        vertical_cuts = newWidthCuts;

        grid_address = new City[grid_address.length + 1][grid_address[0].length];
    }

    private void mapAllCitiesToGridAddress() {
        // grid_address naplnit null
        for (int i = 0; i < grid_address.length; i++) {
            for (int j = 0; j < grid_address[0].length; j++) {
                grid_address[i][j] = null;
            }
        }
        for (String key : graph.getVertices().keySet()) {
            City city = graph.getVertex(key);
            addCityToGridAddress(city);
        }
    }

    private void addCityToGridAddress(City city) {
        GridAddressIndexes cityGridAddressIndexes = getCityGridAddressIndexes(city);
        if (grid_address[cityGridAddressIndexes.x][cityGridAddressIndexes.y] != null) {
            throw new IllegalArgumentException("Index is already in use!");
        }
        grid_address[cityGridAddressIndexes.x][cityGridAddressIndexes.y] = city;
    }

}
