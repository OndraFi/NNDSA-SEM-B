package main.java.gui.components;

import main.java.City;
import main.java.Road;
import main.java.graph.EdgeData;
import main.java.graph.Graph;
import main.java.Location;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GraphPanel extends JPanel {
    private Map<String, Point> positions;
    private Graph<String, City, Road> graph;
    private double scaleX, scaleY;
    private double zoomFactor = 1.0;
    private double zoomMultiplier = 1.1;
    private Point dragStartScreen;
    private Point dragEndScreen;
    private AffineTransform coordTransform = new AffineTransform();
    private ArrayList<String> path;

    public GraphPanel(Graph<String, City, Road> graph, Dimension size) {
        this.positions = new HashMap<>();
        this.graph = graph;
        this.setPreferredSize(size);
        calculateScaleFactors();
        setupMouseWheelZoom();
        setupMousePan();
    }

    public void setPath(ArrayList<String> path) {
        this.path = path;
        repaint();
    }

    private void calculateScaleFactors() {
        double maxX = 0;
        double maxY = 0;
        for (City city : graph.getVertices().values()) {
            Location loc = city.getLocation();
            if (loc.getX() > maxX) maxX = loc.getX();
            if (loc.getY() > maxY) maxY = loc.getY();
        }
        scaleX = this.getPreferredSize().width / maxX;
        scaleY = this.getPreferredSize().height / maxY;
    }

    private void setupMouseWheelZoom() {
        addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                double delta = 0.05f * e.getPreciseWheelRotation();
                double factor = Math.exp(-delta); // Calculate zoom factor
                double x = e.getX();
                double y = e.getY();

                // Update transformation matrix for zoom around cursor
                AffineTransform at = new AffineTransform();
                at.translate(x, y);
                at.scale(factor, factor);
                at.translate(-x, -y);

                coordTransform.preConcatenate(at);

                repaint();
            }
        });
    }

    private void setupMousePan() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragStartScreen = e.getPoint();
                dragEndScreen = null;
            }

            public void mouseReleased(MouseEvent e) {
                moveCamera(e);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                moveCamera(e);
            }
        });
    }

    private void moveCamera(MouseEvent e) {
        try {
            dragEndScreen = e.getPoint();
            Point2D.Float dragStart = transformPoint(dragStartScreen);
            Point2D.Float dragEnd = transformPoint(dragEndScreen);
            double dx = dragEnd.x - dragStart.x;
            double dy = dragEnd.y - dragStart.y;
            coordTransform.translate(dx, dy);
            dragStartScreen = dragEndScreen;
            dragEndScreen = null;
            repaint();
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }

    private Point2D.Float transformPoint(Point p1) throws NoninvertibleTransformException {
        AffineTransform inverse = coordTransform.createInverse();
        Point2D.Float p2 = new Point2D.Float();
        inverse.transform(p1, p2);
        return p2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Apply zoom transformation
        AffineTransform transform = new AffineTransform();
        transform.translate(getWidth() / 2.0, getHeight() / 2.0);
        transform.scale(zoomFactor, zoomFactor);
        transform.translate(-getWidth() / 2.0, -getHeight() / 2.0);
        transform.concatenate(coordTransform);

        g2.setTransform(transform);

        // Set font size based on zoomFactor
        int fontSize = (int) Math.max(10, 10 * zoomFactor);  // Base size is 10, adjusted with zoom
        g2.setFont(new Font("Arial", Font.PLAIN, fontSize));

        // Draw vertices
        for (City city : graph.getVertices().values()) {
            Location loc = city.getLocation();
            int x = (int) loc.getX();
            int y = (int) loc.getY();
            int ovalSize = (int) Math.max(5, 5 * zoomFactor);  // Base size is 5, adjusted with zoom

            // Vertex
            g2.fillOval(x - ovalSize / 2, y - ovalSize / 2, ovalSize, ovalSize);
            // Text
            g2.drawString(city.getName(), x - ovalSize / 2, y - ovalSize / 2 - fontSize / 2);
        }

        // Draw edges
        for (EdgeData<String,Road> edge : graph.getEdges()) {
            City c1 = graph.getVertex(edge.getVertex1Key());
            City c2 = graph.getVertex(edge.getVertex2Key());
            if(edge.getData().isAccessible()) {
                g2.setColor(Color.BLACK);
            } else {
                g2.setColor(Color.RED);
            }
            g2.drawLine((int) c1.getLocation().getX(), (int) c1.getLocation().getY(),
                    (int) c2.getLocation().getX(), (int) c2.getLocation().getY());
            // add edge weight from edge.getData().getWeight()
            int midX = ((int) c1.getLocation().getX() + (int) c2.getLocation().getX()) / 2;
            int midY = ((int) c1.getLocation().getY() + (int) c2.getLocation().getY()) / 2;
            g2.drawString(String.valueOf(edge.getData().getWeight()), midX, midY);
        }

        // Draw path in green
        if (path != null && !path.isEmpty()) {
            g2.setColor(Color.GREEN);
            for (int i = 0; i < path.size() - 1; i++) {
                City c1 = graph.getVertex(path.get(i));
                City c2 = graph.getVertex(path.get(i + 1));
                g2.drawLine((int) c1.getLocation().getX(), (int) c1.getLocation().getY(),
                        (int) c2.getLocation().getX(), (int) c2.getLocation().getY());
            }
        }
    }
}