import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * Ventana principal de la simulación.
 * Responsabilidades:
 * - Recibir entradas del usuario (nroMarcos, algoritmo)
 * - Generar secuencia aleatoria
 * - Mostrar la tabla animada columna por columna
 * - Mostrar el contador de fallos en tiempo real
 */
public class VentanaPrincipal extends JFrame {

    // --- Componentes de entrada ---
    private JButton         btnGenerar;
    private JButton         btnEjecutar;
    private JSpinner        spinnerMarcos;
    private JRadioButton    radioLRU;
    private JLabel          lblSecuencia;

    // --- Componentes de salida ---
    private JTable          tabla;
    private DefaultTableModel modeloTabla;
    private JLabel          lblFallos;

    // --- Estado interno ---
    private int[]           secuencia;
    private List<int[]>     historial;
    private int             pasoActual;
    private javax.swing.Timer animacion;

    // --- Constantes ---
    private static final int LARGO_SECUENCIA  = 20;
    private static final int RANGO_PAGINAS    = 10; // páginas del 0 al 9
    private static final int VELOCIDAD_MS     = 800; // milisegundos por columna

    // --- Colores ---
    private static final Color COLOR_FONDO        = new Color(245, 245, 250);
    private static final Color COLOR_CELDA_LLENA  = new Color(198, 228, 255);
    private static final Color COLOR_CELDA_VACIA  = Color.WHITE;
    private static final Color COLOR_ENCABEZADO   = new Color(70, 130, 180);

    public VentanaPrincipal() {
        configurarVentana();
        construirUI();
    }

    // --- Configuración inicial ---

    private void configurarVentana() {
        setTitle("Simulador de Reemplazo de Páginas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(COLOR_FONDO);
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));

        add(construirPanelControles(), BorderLayout.NORTH);
        add(construirPanelTabla(),     BorderLayout.CENTER);
        add(construirPanelResultados(),BorderLayout.SOUTH);
    }

    // --- Paneles ---

    private JPanel construirPanelControles() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));

        // Botón generar
        btnGenerar = new JButton("Generar Secuencia");
        btnGenerar.addActionListener(e -> generarSecuencia());

        // Spinner de marcos
        SpinnerNumberModel modeloSpinner = new SpinnerNumberModel(3, 3, 6, 1);
        spinnerMarcos = new JSpinner(modeloSpinner);
        spinnerMarcos.setPreferredSize(new Dimension(55, 28));

        // Radio buttons de algoritmo
        radioLRU = new JRadioButton("LRU", true);
        radioLRU.setBackground(COLOR_FONDO);

        ButtonGroup grupoAlgoritmos = new ButtonGroup();
        grupoAlgoritmos.add(radioLRU);

        // Botón ejecutar
        btnEjecutar = new JButton("Ejecutar");
        btnEjecutar.setEnabled(false);
        btnEjecutar.addActionListener(e -> ejecutarSimulacion());

        panel.add(btnGenerar);
        panel.add(new JLabel("Marcos:"));
        panel.add(spinnerMarcos);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(radioLRU);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(btnEjecutar);

        return panel;
    }

    private JPanel construirPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Etiqueta de secuencia
        lblSecuencia = new JLabel("Secuencia: —");
        lblSecuencia.setFont(new Font("Monospaced", Font.PLAIN, 13));
        lblSecuencia.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));

        // Tabla
        modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(38);
        tabla.setFont(new Font("Monospaced", Font.BOLD, 14));
        tabla.setShowGrid(true);
        tabla.setGridColor(new Color(180, 180, 200));
        tabla.getTableHeader().setBackground(COLOR_ENCABEZADO);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        aplicarRenderizadorCeldas();

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(860, 200));

        panel.add(lblSecuencia, BorderLayout.NORTH);
        panel.add(scroll,       BorderLayout.CENTER);

        return panel;
    }

    private JPanel construirPanelResultados() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));

        lblFallos = new JLabel("Fallos de página: —");
        lblFallos.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblFallos.setForeground(new Color(180, 50, 50));

        panel.add(lblFallos);
        return panel;
    }

    // --- Lógica de simulación ---

    private void generarSecuencia() {
        // Detiene animación previa si existía
        detenerAnimacion();
        limpiarTabla();

        Random random = new Random();
        secuencia = new int[LARGO_SECUENCIA];
        StringBuilder sb = new StringBuilder("Secuencia:  ");

        for (int i = 0; i < LARGO_SECUENCIA; i++) {
            secuencia[i] = random.nextInt(RANGO_PAGINAS);
            sb.append(secuencia[i]);
            if (i < LARGO_SECUENCIA - 1) sb.append(",  ");
        }

        lblSecuencia.setText(sb.toString());
        lblFallos.setText("Fallos de página: —");
        btnEjecutar.setEnabled(true);
    }

    private void ejecutarSimulacion() {
        if (secuencia == null) return;

        detenerAnimacion();
        limpiarTabla();

        int nroMarcos = (int) spinnerMarcos.getValue();
        AlgoritmoReemplazo algoritmo = construirAlgoritmo(nroMarcos);

        historial = algoritmo.simular(secuencia);

        construirColumnasTabla(nroMarcos);
        iniciarAnimacion(algoritmo.getFallosPagina());

        btnEjecutar.setEnabled(false);
        btnGenerar.setEnabled(false);
    }

    private AlgoritmoReemplazo construirAlgoritmo(int nroMarcos) {
        return new AlgoritmoLRU(nroMarcos);
    }

    private void construirColumnasTabla(int nroMarcos) {
        // Columna de etiqueta de marco
        modeloTabla.addColumn("Marco");

        // Una columna por cada paso de la secuencia
        for (int i = 0; i < secuencia.length; i++) {
            modeloTabla.addColumn(String.valueOf(secuencia[i]));
        }

        // Filas: una por marco, inicialmente vacías
        for (int i = 0; i < nroMarcos; i++) {
            Object[] fila = new Object[secuencia.length + 1];
            fila[0] = "Marco " + (i + 1);
            modeloTabla.addRow(fila);
        }
    }

    // --- Animación ---

    private void iniciarAnimacion(int totalFallos) {
        pasoActual = 0;

        animacion = new javax.swing.Timer(VELOCIDAD_MS, e -> {
            if (pasoActual >= historial.size()) {
                detenerAnimacion();
                lblFallos.setText("Fallos de página: " + totalFallos);
                btnGenerar.setEnabled(true);
                return;
            }

            rellenarColumna(pasoActual);
            pasoActual++;
        });

        animacion.start();
    }

    private void rellenarColumna(int paso) {
        int[] estadoMarcos = historial.get(paso);
        int columna = paso + 1; // columna 0 es la etiqueta "Marco N"

        for (int fila = 0; fila < estadoMarcos.length; fila++) {
            int valor = estadoMarcos[fila];
            // -1 significa marco vacío
            modeloTabla.setValueAt(valor == -1 ? "" : String.valueOf(valor), fila, columna);
        }
    }

    private void detenerAnimacion() {
        if (animacion == null) return;
        animacion.stop();
    }

    // --- Utilidades ---

    private void limpiarTabla() {
        modeloTabla.setRowCount(0);
        modeloTabla.setColumnCount(0);
    }

    private void aplicarRenderizadorCeldas() {
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);

                if (column == 0) {
                    // Columna de etiqueta
                    setBackground(new Color(230, 230, 240));
                    setForeground(Color.DARK_GRAY);
                    return this;
                }

                boolean celdaLlena = value != null && !value.toString().isEmpty();
                setBackground(celdaLlena ? COLOR_CELDA_LLENA : COLOR_CELDA_VACIA);
                setForeground(Color.DARK_GRAY);
                return this;
            }
        });
    }
}