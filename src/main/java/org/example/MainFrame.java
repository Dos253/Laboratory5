package org.example;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final GraphicsDisplay display; // Панель отображения графиков

    public MainFrame() {
        // Заголовок окна
        super("Построение графиков функций");

        // Устанавливаем начальный размер окна
        setSize(800, 600);

        // Устанавливаем действие при закрытии окна
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Создаем панель для отображения графика
        display = new GraphicsDisplay();

        // Добавляем панель в центр окна
        getContentPane().add(display, BorderLayout.CENTER);

        // Создаем меню прриложения
        createMenuBar();
    }

    // Метод для создания меню приложения
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar(); // Создаём строку меню
        setJMenuBar(menuBar); // Устанавливаем меню в окно


        JMenu fileMenu = new JMenu("Файл"); // Создаем меню "Файл"
        menuBar.add(fileMenu); // Добавляем его в строку меню

        // Создание пункта меню "Открыть" для загрузки данных из файла
        JMenuItem openMenuItem = new JMenuItem("Открыть");

        // Добавляем обработчик события
        openMenuItem.addActionListener(e -> {
            // Открытие файла данных
            openGraphicsData();
        });

        // Добавляеи пункт "Открыть" в меню "Файл"
        fileMenu.add(openMenuItem);

        // Создание пункта меню "Закрыть" для завершения работы приложения
        JMenuItem exitMenuItem = new JMenuItem("Закрыть");

        // Обработчик завершения программы
        exitMenuItem.addActionListener(e -> System.exit(0));

        // Добавляем пункт "Закрыть" в меню "Файл"
        fileMenu.add(exitMenuItem);

        JMenu graphicsMenu = new JMenu("График"); // Создаем меню "График"
        menuBar.add(graphicsMenu); // Добавляем его строку меню

        // Пункт меню "Показать оси"
        JCheckBoxMenuItem showAxisMenuItem = new JCheckBoxMenuItem("Показать оси");
        showAxisMenuItem.setSelected(true); // По умолчанию "Показать оси" будет включено

        // Обработчик события
        showAxisMenuItem.addActionListener(e -> display.setShowAxis(showAxisMenuItem.isSelected()));

        // Добавляем "Показать оси" в меню "График"
        graphicsMenu.add(showAxisMenuItem);

        // Создание пункта меню "Показать маркеры"
        JCheckBoxMenuItem showMarkersMenuItem = new JCheckBoxMenuItem("Показать маркеры");

        // По умолчанию "Показать маркеры" будет включено
        showMarkersMenuItem.setSelected(true);

        // Обработчик события
        showMarkersMenuItem.addActionListener(e -> display.setShowMarkers(showMarkersMenuItem.isSelected()));

        // Добавляем пункт "Показать маркеры" в меню "График"
        graphicsMenu.add(showMarkersMenuItem);

        // Создаем пункт меню "Показать |f(x)|"
        JCheckBoxMenuItem showAbsFunctionMenuItem = new JCheckBoxMenuItem("Показать |f(x)|");
        showAbsFunctionMenuItem.setSelected(false);
        showAbsFunctionMenuItem.addActionListener(e -> display.setShowAbsFunction(showAbsFunctionMenuItem.isSelected()));
        graphicsMenu.add(showAbsFunctionMenuItem);
    }

    // Метод для открытия файла с графическими данными.
    private void openGraphicsData() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                // Чтение данных из файла
                java.util.List<Double[]> graphicsData = new java.util.ArrayList<>();
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.FileReader(fileChooser.getSelectedFile())
                );
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.trim().split("\\s+");
                    double x = Double.parseDouble(parts[0]);
                    double y = Double.parseDouble(parts[1]);
                    graphicsData.add(new Double[]{x, y});
                }
                reader.close();

                // Передача данных в GraphicsDisplay
                display.showGraphics(graphicsData.toArray(new Double[0][]));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки данных", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        // Создаем и показываем главное окно приложения
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}