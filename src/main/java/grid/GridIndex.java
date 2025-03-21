package main.java.grid;

import main.java.Location;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class GridIndex<T extends LocationInterface> { // todo generický
    private final int width;
    private final int height;
    private final ArrayList<T> elements = new ArrayList<>();
    private final Class<T> Tclass;
    private int[] horizontal_cuts; // z leva do prava
    private int[] vertical_cuts; // z vrchu dolu

    private static final int HORIZONTAL_CUT = 0;
    private static final int VERTICAL_CUT = 1;

    private int lastCut = HORIZONTAL_CUT;

    private T[][] grid_address;

    private static class GridAddressIndexes {
        int x;
        int y;

        public GridAddressIndexes(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public GridIndex(int width, int height, Class<T> clazz) {
        this.width = width;
        this.height = height;
        this.horizontal_cuts = new int[]{0, height};
        this.vertical_cuts = new int[]{0, width};
        this.Tclass = clazz;
        this.grid_address = (T[][]) Array.newInstance(clazz, 1, 1);

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

    public void add(T element) throws IllegalArgumentException {
        if (element.getLocation().getX() >= width || element.getLocation().getY() >= height) {
            throw new IllegalArgumentException("Element is out of bounds");
        }

        if (isLocationOccupied(element.getLocation())) {
            System.out.println("Location is ocupaied, throwing exception");
            throw new IllegalArgumentException("Location is already occupied by another element or a cut. Element:" + element.toString());
        }

        if (shouldPerformCut(element)) {
            cut(element);
        }

        elements.add(element);
        mapAllCitiesToGridAddress();

        printGrid();
    }

    private void printGrid() {
        System.out.println("x: " + Arrays.toString(vertical_cuts));
        System.out.println("y: " + Arrays.toString(horizontal_cuts));


        for (int j = 0; j < grid_address[0].length; j++) {
            for (int i = 0; i < grid_address.length; i++) {
                if (grid_address[i][j] != null) {
                    System.out.print(grid_address[i][j].toString() + " ");
                } else {
                    System.out.print("* ");
                }
            }
            System.out.println();
        }
    }

    private boolean isLocationOccupied(Location location) {
        for (T[] row : grid_address) {
            for (T element : row) {
                if (element != null && (Math.abs(element.getLocation().getX() - location.getX()) <= 0 && Math.abs(element.getLocation().getY() - location.getY()) <= 0)) {
//                    System.out.println("City: " + element.getName() + " is on the same location as new city: " + location.toString());
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

    public T findElementsByCoordinates(int x, int y) throws IllegalArgumentException {
        T foundElement = null;
        for (T element : elements) {
            if (element.getLocation().getX() == x && element.getLocation().getY() == y) {
                foundElement = element;
                break;
            }
        }
        return foundElement;
    }

    public ArrayList<T> findElementBySegment(int x1, int y1, int x2, int y2) {

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

        ArrayList<T> elements = new ArrayList<>();

        for (int i = xStartIndex; i < xEndIndex; i++) {
            for (int j = yStartIndex; j < yEndIndex; j++) {
                if (grid_address[i][j] != null && isElementInSearchDimensions(grid_address[i][j], x1, y1, x2, y2)) {
                    elements.add(grid_address[i][j]);
                }
            }
        }

        return elements;
    }

    private boolean isElementInSearchDimensions(T element, int x1, int y1, int x2, int y2) {
        return element.getLocation().getX() >= x1 && element.getLocation().getY() >= y1 && element.getLocation().getX() <= x2 && element.getLocation().getY() <= y2;
    }

    private boolean shouldPerformCut(T element) {
        GridAddressIndexes elementGridAddressIndexes = getElementGridAddressIndexes(element);
        return grid_address[elementGridAddressIndexes.x][elementGridAddressIndexes.y] != null;
    }

    private GridAddressIndexes getElementGridAddressIndexes(T element) {
        Location elementLocation = element.getLocation();
        int xIndex = getElementXIndexInGridAddress(elementLocation.getX());
        int yIndex = getElementYIndexInGridAddress(elementLocation.getY());
        return new GridAddressIndexes(xIndex, yIndex);
    }

    private int getElementXIndexInGridAddress(int elementX) {
        for (int i = 0; i < vertical_cuts.length; i++) {
            if (elementX < vertical_cuts[i]) {
                return i - 1;
            }
        }
        return -1;
    }

    private int getElementYIndexInGridAddress(int elementY) {
        for (int i = 0; i < horizontal_cuts.length; i++) {
            if (elementY < horizontal_cuts[i]) {
                return i - 1;
            }
        }
        return -1;
    }

    private void cut(T element) {
        if (lastCut == HORIZONTAL_CUT) {
            // střídám, takže chci řezat druhým směrem
            boolean cutSuccessful = performVerticalCut(element);
            if (!cutSuccessful) { // pokud to nejde tak zkusím horizontálně
                boolean horizontalCutSuccessful = performHorizontalCut(element);
                if (!horizontalCutSuccessful) // pokud nejde ani horizonrálně, tak výjmka
                    throw new IllegalArgumentException("Cannot cut the grid");
            } else {
                lastCut = VERTICAL_CUT;
            }
        } else {
            boolean horizontalCutSuccessful = performHorizontalCut(element);
            if (!horizontalCutSuccessful) {
                boolean verticalCutSuccessful = performVerticalCut(element);
                if (!verticalCutSuccessful)
                    throw new IllegalArgumentException("Cannot cut the grid");
            } else {
                lastCut = HORIZONTAL_CUT;
            }
        }
    }

    private boolean canAddToInBetween(int elementInGridPosition, int newElementPosition, int inBetween) {
        if (Math.abs(elementInGridPosition - newElementPosition) > 1)
            if (inBetween < elementInGridPosition && inBetween > newElementPosition)
                return (Math.abs(elementInGridPosition - inBetween) > 1);
        if ((inBetween > elementInGridPosition && inBetween < newElementPosition))
            return Math.abs(newElementPosition - inBetween) > 1;
        return false;
    }

    private boolean canRetrieveFromInBetween(int elementInGridPosition, int newElementPosition, int inBetween) {
        if (Math.abs(elementInGridPosition - newElementPosition) > 1)
            if (inBetween < elementInGridPosition && inBetween > newElementPosition)
                return (Math.abs(newElementPosition - inBetween) > 1);
        if ((inBetween > elementInGridPosition && inBetween < newElementPosition))
            return Math.abs(elementInGridPosition - inBetween) > 1;
        return false;
    }

    private boolean performHorizontalCut(T newElement) {

        GridAddressIndexes elementGridAddressIndexes = getElementGridAddressIndexes(newElement);
        T elementInGrid = grid_address[elementGridAddressIndexes.x][elementGridAddressIndexes.y];
        int inBetween = Math.round((elementInGrid.getLocation().getY() + newElement.getLocation().getY()) / 2.0f);

        boolean didChangeInBetween = false;

        do {
            didChangeInBetween = false;
            // Check if inBetween intersects with any existing city
            for (T element : elements) {
                if (element != null && element.getLocation().getY() == inBetween) {

                    //  v případě že řez prochází nějakým městem, tak se pokusit posunout řez, jinak výjmka, jelilkož řez nejde provézt
                    if (canAddToInBetween(elementInGrid.getLocation().getY(), newElement.getLocation().getY(), inBetween)) {
                        inBetween++;
                        didChangeInBetween = true;
                        break;
                    } else if (canRetrieveFromInBetween(elementInGrid.getLocation().getY(), newElement.getLocation().getY(), inBetween)) {
                        inBetween--;
                        didChangeInBetween = true;
                        break;
                    } else {
                        return false;
//                        throw new IllegalArgumentException("Cut cannot intersect with an existing city at Y: " + inBetween);
                    }

                }
            }
        } while (didChangeInBetween);

//        System.out.println("Performing new vertical cut at y: " + inBetween + " between " + elementInGrid.getName() + " y: " + elementInGrid.getLocation().getY() + " and " + newElement.getName() + " y: " + newElement.getLocation().getY());

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
        grid_address = (T[][]) Array.newInstance(Tclass, grid_address.length, grid_address[0].length + 1);
        return true;
    }

    private boolean performVerticalCut(T newElement) {
        GridAddressIndexes elementGridAddressIndexes = getElementGridAddressIndexes(newElement);
        T elementInGrid = grid_address[elementGridAddressIndexes.x][elementGridAddressIndexes.y];
        int inBetween = Math.round((elementInGrid.getLocation().getX() + newElement.getLocation().getX()) / 2.0f);

        boolean didChangeInBetween = false;
        do {
            didChangeInBetween = false;
            // Check if inBetween intersects with any existing city
            for (T existingElement : elements) {
                if (existingElement != null && existingElement.getLocation().getX() == inBetween) {

                    //  v případě že řez prochází nějakým městem, tak se pokusit posunout řez, jinak výjmka, jelilkož řez nejde provézt
                    if (canAddToInBetween(elementInGrid.getLocation().getX(), newElement.getLocation().getX(), inBetween)) {
                        inBetween++;
                        didChangeInBetween = true;
                        break;
                    } else if (canRetrieveFromInBetween(elementInGrid.getLocation().getX(), newElement.getLocation().getX(), inBetween)) {
                        inBetween--;
                        didChangeInBetween = true;
                        break;
                    } else {
                        return false;
//                        throw new IllegalArgumentException("Cut cannot intersect with an existing city at X: " + inBetween);
                    }
                }
            }
        } while (didChangeInBetween);


//        System.out.println("Performing new vertical cut at x: " + inBetween + " between " + elementInGrid.getName() + " x: " + elementInGrid.getLocation().getX() + " and " + newElement.getName() + " x: " + newElement.getLocation().getX());

        int[] newVerticalCuts = new int[vertical_cuts.length + 1];
        int indexForInBetween = -1;
        for (int i = 1; i < vertical_cuts.length; i++) {
            if (vertical_cuts[i - 1] < inBetween && vertical_cuts[i] > inBetween) {
                indexForInBetween = i;
                break;
            }
        }
        System.arraycopy(vertical_cuts, 0, newVerticalCuts, 0, indexForInBetween);
        newVerticalCuts[indexForInBetween] = inBetween;
        System.arraycopy(vertical_cuts, indexForInBetween, newVerticalCuts, indexForInBetween + 1, vertical_cuts.length - indexForInBetween);
        vertical_cuts = newVerticalCuts;

        grid_address = (T[][]) Array.newInstance(Tclass, grid_address.length + 1, grid_address[0].length);
        return true;
    }

    private void mapAllCitiesToGridAddress() {
        // grid_address naplnit null
        for (int i = 0; i < grid_address.length; i++) {
            for (int j = 0; j < grid_address[0].length; j++) {
                grid_address[i][j] = null;
            }
        }
        // pro každé město v grafu, vložit na správné místo v grid_address
        for (T element : elements) {
            addElementToGridAddress(element);
        }
    }

    private void addElementToGridAddress(T element) {
        GridAddressIndexes elementGridAddressIndexes = getElementGridAddressIndexes(element);
        if (grid_address[elementGridAddressIndexes.x][elementGridAddressIndexes.y] != null) {
            throw new IllegalArgumentException("Index is already in use!");
        }
        grid_address[elementGridAddressIndexes.x][elementGridAddressIndexes.y] = element;
    }

}
