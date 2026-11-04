import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ventana principal. Sigue la misma estructura del tutorial de JetBrains:
 * una clase que extiende JFrame, un panel contentPane, campos de texto,
 * un JComboBox, un JCheckBox, etiquetas y botones con ActionListener.
 *
 * Aquí también viven la lista de jugadores, la carga del CSV y la búsqueda.
 * El ID de cada jugador no viene en el CSV: lo asigna la clase Persona al crearlo.
 */
public class Pantalla extends JFrame {

    /** Opciones del selector de búsqueda. */
    public enum Criterio {
        TODOS("Todos los campos"),
        ID("ID"),
        NOMBRE("Nombre"),
        PAIS("País"),
        POSICION("Posición");

        private final String etiqueta;

        Criterio(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    /**
     * Resumen de una carga: filas cargadas, filas con datos inválidos que se saltaron
     * y filas repetidas (mismo nombre y fecha de nacimiento) que se ignoraron.
     */
    private static class ResultadoCarga {
        final int cargados;
        final int omitidos;
        final int repetidos;

        ResultadoCarga(int cargados, int omitidos, int repetidos) {
            this.cargados = cargados;
            this.omitidos = omitidos;
            this.repetidos = repetidos;
        }
    }

    private JPanel contentPane;

    // Formulario "Nuevo jugador"
    private JTextField nombreField;
    private JTextField paisField;
    private JTextField nacimientoField;
    private JSpinner dorsalSpinner;
    private JComboBox<Posicion> posicionComboBox;
    private JCheckBox vivoCheckBox;
    private JButton guardarButton;
    private JButton cancelarButton;

    // Carga y búsqueda
    private JButton cargarCsvButton;
    private JLabel archivoLabel;
    private JComboBox<Criterio> criterioComboBox;
    private JTextField busquedaField;
    private JButton buscarButton;
    private JLabel resultadosLabel;
    private JTable jugadoresTable;
    private DefaultTableModel tableModel;

    // Los jugadores se guardan en memoria
    private final List<Jugador> listaJugadores = new ArrayList<>();

    public Pantalla() {
        super("Base de jugadores");

        construirInterfaz();
        registrarEventos();
        mostrarJugadores(listaJugadores);

        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    // ---------- Construcción de la interfaz ----------

    private void construirInterfaz() {
        contentPane = new JPanel(new BorderLayout(0, 10));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10)); // márgenes de 10, como en el tutorial
        contentPane.setPreferredSize(new Dimension(760, 560));

        contentPane.add(crearPanelFormulario(), BorderLayout.NORTH);
        contentPane.add(crearPanelBusqueda(), BorderLayout.CENTER);
    }

    private JPanel crearPanelFormulario() {
        nombreField = new JTextField(15);
        paisField = new JTextField(15);
        nacimientoField = new JTextField(10);
        dorsalSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 99, 1));
        posicionComboBox = new JComboBox<>(Posicion.values());
        vivoCheckBox = new JCheckBox("Está vivo", true);
        cancelarButton = new JButton("Cancelar");
        guardarButton = new JButton("Guardar");

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nuevo jugador"));

        agregarCampo(panel, "Nombre", nombreField, 0, 0);
        agregarCampo(panel, "País", paisField, 0, 1);
        agregarCampo(panel, "Nacimiento (AAAA-MM-DD)", nacimientoField, 1, 0);
        agregarCampo(panel, "Dorsal", dorsalSpinner, 1, 1);
        agregarCampo(panel, "Posición", posicionComboBox, 2, 0);
        agregarCampo(panel, "Estado", vivoCheckBox, 2, 1);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.add(cancelarButton);
        botones.add(guardarButton);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 4;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 6, 4, 6);
        panel.add(botones, c);

        return panel;
    }

    private void agregarCampo(JPanel panel, String etiqueta, JComponent campo, int fila, int columna) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = fila;

        c.gridx = columna * 2;
        c.weightx = 0;
        panel.add(new JLabel(etiqueta), c);

        c.gridx = columna * 2 + 1;
        c.weightx = 1;
        panel.add(campo, c);
    }

    private JPanel crearPanelBusqueda() {
        cargarCsvButton = new JButton("Cargar CSV...");
        archivoLabel = new JLabel("Sin archivo cargado");
        criterioComboBox = new JComboBox<>(Criterio.values());
        busquedaField = new JTextField();
        buscarButton = new JButton("Buscar");
        resultadosLabel = new JLabel(" ");

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "País", "Dorsal", "Posición", "Estado", "Edad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jugadoresTable = new JTable(tableModel);
        jugadoresTable.setFillsViewportHeight(true);

        JPanel archivoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        archivoPanel.add(cargarCsvButton);
        archivoPanel.add(Box.createHorizontalStrut(10));
        archivoPanel.add(archivoLabel);

        JPanel barra = new JPanel(new BorderLayout(6, 0));
        barra.add(criterioComboBox, BorderLayout.WEST);
        barra.add(busquedaField, BorderLayout.CENTER);
        barra.add(buscarButton, BorderLayout.EAST);

        JPanel superior = new JPanel(new BorderLayout(0, 8));
        superior.add(archivoPanel, BorderLayout.NORTH);
        superior.add(barra, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Buscar jugadores"),
                new EmptyBorder(6, 6, 6, 6)));
        panel.add(superior, BorderLayout.NORTH);
        panel.add(new JScrollPane(jugadoresTable), BorderLayout.CENTER);
        panel.add(resultadosLabel, BorderLayout.SOUTH);
        return panel;
    }

    // ---------- Eventos ----------

    private void registrarEventos() {
        guardarButton.addActionListener(e -> guardarJugador());
        cancelarButton.addActionListener(e -> limpiarFormulario());
        cargarCsvButton.addActionListener(e -> elegirCsv());
        buscarButton.addActionListener(e -> buscar());
        busquedaField.addActionListener(e -> buscar()); // Enter dentro del campo de búsqueda
    }

    private void elegirCsv() {
        JFileChooser selector = new JFileChooser(new File("."));
        selector.setFileFilter(new FileNameExtensionFilter("Archivos CSV", "csv"));
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            cargarArchivo(selector.getSelectedFile().toPath(), true);
        }
    }

    /** Carga un CSV y actualiza la tabla. Si mostrarAviso es true, informa el resultado. */
    public void cargarArchivo(Path archivo, boolean mostrarAviso) {
        try {
            ResultadoCarga r = cargarCSV(archivo);

            String resumen = r.cargados + " jugadores";
            if (r.repetidos > 0) {
                resumen += ", " + r.repetidos + " repetidos ignorados";
            }
            archivoLabel.setText(archivo.getFileName() + " (" + resumen + ")");
            busquedaField.setText("");
            criterioComboBox.setSelectedItem(Criterio.TODOS);
            mostrarJugadores(listaJugadores);

            if (mostrarAviso) {
                String mensaje = r.cargados + " jugadores cargados.";
                if (r.repetidos > 0) {
                    mensaje += "\n" + r.repetidos + " fila(s) repetida(s) (mismo nombre y fecha de nacimiento) se ignoraron.";
                }
                if (r.omitidos > 0) {
                    mensaje += "\n" + r.omitidos + " fila(s) se omitieron por tener datos inválidos.";
                }
                JOptionPane.showMessageDialog(this, mensaje, "CSV cargado", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo leer el archivo:\n" + ex.getMessage(),
                    "Error al cargar", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Al iniciar: carga el archivo si existe; si no, avisa en pantalla cómo cargarlo. */
    public void cargarSiExiste(Path archivo) {
        if (Files.exists(archivo)) {
            cargarArchivo(archivo, false);
        } else {
            archivoLabel.setText("No se encontró " + archivo.getFileName()
                    + " en la carpeta del proyecto. Usa \"Cargar CSV...\"");
        }
    }

    private void guardarJugador() {
        String nombre = nombreField.getText().trim();
        String pais = paisField.getText().trim();
        String fecha = nacimientoField.getText().trim();

        if (nombre.isEmpty()) {
            mostrarError("Escribe el nombre del jugador.");
            nombreField.requestFocus();
            return;
        }

        LocalDate nacimiento;
        try {
            nacimiento = LocalDate.parse(fecha);
        } catch (DateTimeParseException ex) {
            mostrarError("La fecha debe tener el formato AAAA-MM-DD, por ejemplo 1998-05-21.");
            nacimientoField.requestFocus();
            return;
        }
        if (nacimiento.isAfter(LocalDate.now())) {
            mostrarError("La fecha de nacimiento no puede ser futura.");
            nacimientoField.requestFocus();
            return;
        }

        // El ID se asigna solo, dentro del constructor de Persona.
        Jugador jugador = new Jugador(nombre, nacimiento);
        jugador.setCountry(pais);
        jugador.dorsal = ((Number) dorsalSpinner.getValue()).intValue();
        jugador.posicion = (Posicion) posicionComboBox.getSelectedItem();
        jugador.is_alive = vivoCheckBox.isSelected();
        listaJugadores.add(jugador);

        limpiarFormulario();
        busquedaField.setText("");
        criterioComboBox.setSelectedItem(Criterio.TODOS);
        mostrarJugadores(listaJugadores);

        JOptionPane.showMessageDialog(this,
                "Jugador guardado con ID " + jugador.getId(),
                "Guardado", JOptionPane.INFORMATION_MESSAGE);
    }

    // Igual que "Cancel" en el tutorial: limpia el formulario sin guardar.
    private void limpiarFormulario() {
        nombreField.setText("");
        paisField.setText("");
        nacimientoField.setText("");
        dorsalSpinner.setValue(1);
        posicionComboBox.setSelectedIndex(0);
        vivoCheckBox.setSelected(true);
        nombreField.requestFocus();
    }

    private void buscar() {
        Criterio criterio = (Criterio) criterioComboBox.getSelectedItem();
        mostrarJugadores(filtrar(criterio, busquedaField.getText()));
    }

    // ---------- Tabla ----------

    private void mostrarJugadores(List<Jugador> jugadores) {
        tableModel.setRowCount(0);
        for (Jugador j : jugadores) {
            tableModel.addRow(new Object[]{
                    j.getId(),
                    j.name,
                    j.getCountry(),
                    j.dorsal,
                    j.posicion,
                    j.is_alive ? "Vivo" : "Fallecido",
                    j.is_alive ? String.valueOf(j.getEdad()) : "-" // no hay fecha de fallecimiento para calcular la edad
            });
        }
        resultadosLabel.setText(jugadores.size() + " jugador(es) encontrado(s)");
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Datos incompletos", JOptionPane.WARNING_MESSAGE);
    }

    // ---------- Carga desde CSV ----------

    /**
     * Reemplaza los jugadores actuales por los del archivo.
     * Columnas (los títulos no distinguen mayúsculas ni tildes): Nombre, Fecha_de_Nacimiento (AAAA-MM-DD),
     * Pais, Dorsal, Posicion y Vivo (Sí/No). También acepta is_alive y los títulos cortos nombre/nacimiento.
     * Solo el nombre y la fecha de nacimiento son obligatorios. Acepta separador , o ; y UTF-8 o Windows-1252.
     */
    private ResultadoCarga cargarCSV(Path archivo) throws IOException {
        List<String> lineas = leerLineas(archivo);
        if (lineas.isEmpty()) {
            throw new IOException("El archivo está vacío.");
        }

        String encabezado = quitarBOM(lineas.get(0));
        char sep = detectarSeparador(encabezado);

        Map<String, Integer> columnas = new HashMap<>();
        String[] titulos = dividir(encabezado, sep);
        for (int i = 0; i < titulos.length; i++) {
            columnas.put(normalizar(titulos[i]).replace(' ', '_'), i);
        }

        int iNombre = indice(columnas, "nombre", "name");
        int iPais = indice(columnas, "pais", "country");
        int iNacimiento = indice(columnas, "fecha_de_nacimiento", "nacimiento", "fecha_nacimiento", "birth");
        int iDorsal = indice(columnas, "dorsal", "numero");
        int iPosicion = indice(columnas, "posicion", "position");
        int iVivo = indice(columnas, "is_alive", "vivo", "alive");

        if (iNombre < 0 || iNacimiento < 0) {
            throw new IOException("La primera línea debe tener al menos las columnas 'nombre' y 'nacimiento'.");
        }

        listaJugadores.clear();
        Persona.reiniciarContador();

        Set<String> vistos = new HashSet<>();
        int cargados = 0;
        int omitidos = 0;
        int repetidos = 0;
        for (int n = 1; n < lineas.size(); n++) {
            String linea = lineas.get(n);
            if (linea.trim().isEmpty()) {
                continue;
            }
            String[] c = dividir(linea, sep);
            try {
                String nombre = campo(c, iNombre);
                if (nombre.isEmpty()) {
                    throw new IllegalArgumentException("Sin nombre");
                }
                // La fecha se lee antes de crear el jugador, así una fila inválida no gasta un ID.
                LocalDate nacimiento = parsearFecha(campo(c, iNacimiento));

                // Un jugador repetido (mismo nombre y fecha) se ignora para que no tenga dos IDs.
                if (!vistos.add(normalizar(nombre) + "|" + nacimiento)) {
                    repetidos++;
                    continue;
                }

                Jugador j = new Jugador(nombre, nacimiento);
                j.setCountry(campo(c, iPais));
                j.dorsal = parsearDorsal(campo(c, iDorsal));
                j.posicion = parsearPosicion(campo(c, iPosicion));
                j.is_alive = parsearVivo(campo(c, iVivo));
                listaJugadores.add(j);
                cargados++;
            } catch (RuntimeException e) {
                omitidos++;
            }
        }
        return new ResultadoCarga(cargados, omitidos, repetidos);
    }

    private static List<String> leerLineas(Path archivo) throws IOException {
        try {
            return Files.readAllLines(archivo, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            // Archivos guardados desde Excel en Windows suelen venir en Windows-1252.
            return Files.readAllLines(archivo, Charset.forName("windows-1252"));
        }
    }

    private static String quitarBOM(String s) {
        return s.startsWith("\uFEFF") ? s.substring(1) : s;
    }

    private static char detectarSeparador(String encabezado) {
        int puntoYComa = 0;
        int comas = 0;
        for (char ch : encabezado.toCharArray()) {
            if (ch == ';') puntoYComa++;
            if (ch == ',') comas++;
        }
        return puntoYComa > comas ? ';' : ',';
    }

    // Divide una línea respetando campos entre comillas ("Pérez, Juan").
    private static String[] dividir(String linea, char sep) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char ch = linea.charAt(i);
            if (ch == '"') {
                if (entreComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"');
                    i++;
                } else {
                    entreComillas = !entreComillas;
                }
            } else if (ch == sep && !entreComillas) {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(ch);
            }
        }
        campos.add(actual.toString());
        return campos.toArray(new String[0]);
    }

    private static int indice(Map<String, Integer> columnas, String... alias) {
        for (String a : alias) {
            Integer i = columnas.get(a);
            if (i != null) {
                return i;
            }
        }
        return -1;
    }

    private static String campo(String[] c, int i) {
        return (i < 0 || i >= c.length) ? "" : c[i].trim();
    }

    private static LocalDate parsearFecha(String s) {
        try {
            return LocalDate.parse(s); // AAAA-MM-DD
        } catch (DateTimeParseException e) {
            return LocalDate.parse(s, DateTimeFormatter.ofPattern("d/M/yyyy")); // si falla, lanza la excepción
        }
    }

    private static int parsearDorsal(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Posicion parsearPosicion(String s) {
        String clave = normalizar(s).toUpperCase();
        for (Posicion p : Posicion.values()) {
            if (p.name().equals(clave)) {
                return p;
            }
        }
        return null;
    }

    // Vacío o no reconocido cuenta como vivo.
    private static boolean parsearVivo(String s) {
        switch (normalizar(s)) {
            case "false":
            case "falso":
            case "no":
            case "0":
            case "muerto":
            case "fallecido":
                return false;
            default:
                return true;
        }
    }

    // ---------- Búsqueda ----------

    private List<Jugador> filtrar(Criterio criterio, String texto) {
        String consulta = normalizar(texto);
        List<Jugador> resultado = new ArrayList<>();
        for (Jugador j : listaJugadores) {
            if (consulta.isEmpty() || coincide(j, criterio, consulta)) {
                resultado.add(j);
            }
        }
        return resultado;
    }

    private boolean coincide(Jugador j, Criterio criterio, String consulta) {
        switch (criterio) {
            case ID:
                return normalizar(j.getId()).equals(consulta);
            case NOMBRE:
                return normalizar(j.name).contains(consulta);
            case PAIS:
                return normalizar(j.getCountry()).contains(consulta);
            case POSICION:
                return j.posicion != null && normalizar(j.posicion.name()).contains(consulta);
            default:
                return coincide(j, Criterio.ID, consulta)
                        || coincide(j, Criterio.NOMBRE, consulta)
                        || coincide(j, Criterio.PAIS, consulta)
                        || coincide(j, Criterio.POSICION, consulta);
        }
    }

    // Ignora mayúsculas y tildes: "España" coincide con "espana".
    private static String normalizar(String s) {
        if (s == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.trim().toLowerCase();
    }
}
