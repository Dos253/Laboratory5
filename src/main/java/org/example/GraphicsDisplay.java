package org.example;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public class GraphicsDisplay extends JPanel implements MouseMotionListener, MouseListener {
    private Double[][] graphicsData;
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean showAbsFunction = false;  // Новый флаг для отображения |f(x)| графика

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double scale;

    private final BasicStroke graphicsStroke;
    private final BasicStroke axisStroke;
    private final BasicStroke absFunctionStroke;  // Стиль для графика |f(x)|
    private final BasicStroke selectionStroke;

    private Point2D.Double selectionStart = null;
    private Point2D.Double selectionEnd = null;
    private boolean selecting = false;

    private Point2D.Double hoveredPoint = null;

    public GraphicsDisplay() {
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[]{10, 10}, 0.0f);
        axisStroke = new BasicStroke(2.0f);
        absFunctionStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[]{5, 5}, 0.0f);  // Новый стиль
        selectionStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[]{5, 5}, 0.0f);

        addMouseMotionListener(this);
        addMouseListener(this);
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        this.minX = graphicsData[0][0];
        this.maxX = graphicsData[graphicsData.length - 1][0];
        this.minY = Double.MAX_VALUE;
        this.maxY = Double.MIN_VALUE;
        for (Double[] point : graphicsData) {
            minY = Math.min(minY, point[1]);
            maxY = Math.max(maxY, point[1]);
        }
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    public void setShowAbsFunction(boolean showAbsFunction) {
        this.showAbsFunction = showAbsFunction;  // Обновляем флаг отображения |f(x)|
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphicsData == null || graphicsData.length == 0) return;

        double width = getWidth();
        double height = getHeight();
        double scaleX = width / (maxX - minX);
        double scaleY = height / (maxY - minY);
        scale = Math.min(scaleX, scaleY);

        Graphics2D canvas = (Graphics2D) g;
        canvas.setStroke(axisStroke);
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);

        if (showMarkers) paintMarkers(canvas);

        if (selecting && selectionStart != null && selectionEnd != null) {
            canvas.setStroke(selectionStroke);
            canvas.setColor(Color.BLUE);
            Rectangle2D.Double selectionRect = new Rectangle2D.Double(
                    Math.min(selectionStart.getX(), selectionEnd.getX()),
                    Math.min(selectionStart.getY(), selectionEnd.getY()),
                    Math.abs(selectionEnd.getX() - selectionStart.getX()),
                    Math.abs(selectionEnd.getY() - selectionStart.getY())
            );
            canvas.draw(selectionRect);
        }
    }

    private void paintAxis(Graphics2D canvas) {
        canvas.setColor(Color.BLACK);
        canvas.setStroke(axisStroke);

        // Рисуем оси
        if (minX <= 0 && maxX >= 0) {
            Point2D.Double start = xyToPoint(0, maxY);
            Point2D.Double end = xyToPoint(0, minY);
            canvas.draw(new Line2D.Double(start, end));
        }
        if (minY <= 0 && maxY >= 0) {
            Point2D.Double start = xyToPoint(minX, 0);
            Point2D.Double end = xyToPoint(maxX, 0);
            canvas.draw(new Line2D.Double(start, end));
        }
    }

    private void paintGraphics(Graphics2D canvas) {
        if (graphicsData == null) return;

        canvas.setColor(Color.BLACK);
        canvas.setStroke(graphicsStroke);
        GeneralPath graph = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            if (i == 0) {
                graph.moveTo(point.getX(), point.getY());
            } else {
                graph.lineTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graph);

        // Если нужно, рисуем график |f(x)|
        if (showAbsFunction) {
            canvas.setColor(Color.BLUE);  // Новый цвет для |f(x)|
            canvas.setStroke(absFunctionStroke);
            GeneralPath absGraph = new GeneralPath();
            for (int i = 0; i < graphicsData.length; i++) {
                double yAbs = Math.abs(graphicsData[i][1]);  // Абсолютное значение y
                Point2D.Double point = xyToPoint(graphicsData[i][0], yAbs);
                if (i == 0) {
                    absGraph.moveTo(point.getX(), point.getY());
                } else {
                    absGraph.lineTo(point.getX(), point.getY());
                }
            }
            canvas.draw(absGraph);
        }
    }

    private void paintMarkers(Graphics2D canvas) {
        for (Double[] point : graphicsData) {
            Point2D.Double center = xyToPoint(point[0], point[1]);
            canvas.setColor(Color.RED);
            canvas.fill(new Ellipse2D.Double(center.getX() - 5, center.getY() - 5, 10, 10));

            // Если текущая точка является наведенной, показываем координаты
            if (hoveredPoint != null && hoveredPoint.equals(center)) {
                String coords = String.format("(%.2f, %.2f)", point[0], point[1]);
                canvas.setFont(new Font("Arial", Font.BOLD, 14));
                canvas.setColor(Color.BLACK);
                canvas.drawString(coords, (float) center.getX() + 10, (float) center.getY() - 10);
            }
        }
    }

    private Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scale, deltaY * scale);
    }

    private double[] pointToXY(Point2D.Double point) {
        double x = point.getX() / scale + minX;
        double y = maxY - point.getY() / scale;
        return new double[]{x, y};
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (graphicsData == null) return;

        Point mousePoint = e.getPoint();
        hoveredPoint = null;

        // Проверяем расстояние до каждой точки
        for (Double[] point : graphicsData) {
            Point2D.Double graphPoint = xyToPoint(point[0], point[1]);
            if (graphPoint.distance(mousePoint) < 5) { // Если курсор в радиусе 5 пикселей
                hoveredPoint = graphPoint;
                break;
            }
        }
        repaint(); // Перерисовываем, чтобы отобразить изменения
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (selecting) {
            selectionEnd = new Point2D.Double(e.getX(), e.getY());
            repaint();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            selecting = true;
            selectionStart = new Point2D.Double(e.getX(), e.getY());
            selectionEnd = selectionStart;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            selecting = false;
            double[] start = pointToXY(selectionStart);
            double[] end = pointToXY(selectionEnd);
            minX = Math.min(start[0], end[0]);
            maxX = Math.max(start[0], end[0]);
            minY = Math.min(start[1], end[1]);
            maxY = Math.max(start[1], end[1]);
            repaint();
        } else if (SwingUtilities.isRightMouseButton(e)) {
            showGraphics(graphicsData);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
