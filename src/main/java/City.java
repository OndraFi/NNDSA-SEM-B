package main.java;

public class City{

    private final String name;
    private final Location location;

    public City(String name, int x ,int y) {
        this.location = new Location(x,y);
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return this.name;
    }

}
