import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

public class Menu {

    private static final String ARCHIVO_POR_DEFECTO = "jugadores_futbol_nombre_unico.csv";

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

    private final List<Jugador> listaJugadores = new ArrayList<>();
    private final List<Equipo> listaEquipos = new ArrayList<>();
    private final Scanner teclado = new Scanner(System.in, "UTF-8");


    public void iniciar() {
        System.out.println("Bienvenido al programa de jugadores FIFA.");
        cargarSiExiste(Paths.get(ARCHIVO_POR_DEFECTO));

        try {
            int opcion;
            do {
                mostrarMenu();
                opcion = leerEntero("Elige una opción: ");
                System.out.println();

                switch (opcion) {
                    case 1:
                        opcionCargarCsv();
                        break;
                    case 2:
                        mostrarTabla(listaJugadores);
                        break;
                    case 3:
                        opcionBuscarPorNombre();
                        break;
                    case 4:
                        opcionBuscarPorId();
                        break;
                    case 5:
                        opcionFiltrarPorPais();
                        break;
                    case 6:
                        opcionFiltrarPorPosicion();
                        break;
                    case 7:
                        opcionAgregarJugador();
                        break;
                    case 8:
                        menuEquipos();
                        break;
                    case 0:
                        System.out.println("Hasta luego.");
                        break;
                    default:
                        System.out.println("Opción no válida. Elige un número del menú.");
                }
            } while (opcion != 0);
        } catch (NoSuchElementException e) {
            System.out.println("\nSe cerró la entrada de datos. Hasta luego.");
        }
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("==================================");
        System.out.println("     FIFAS - MENÚ DE JUGADORES");
        System.out.println("==================================");
        System.out.println("Jugadores cargados: " + listaJugadores.size() + " | Equipos: " + listaEquipos.size());
        System.out.println(" 1. Cargar archivo CSV");
        System.out.println(" 2. Ver todos los jugadores");
        System.out.println(" 3. Buscar jugador por nombre");
        System.out.println(" 4. Buscar jugador por ID");
        System.out.println(" 5. Filtrar por país");
        System.out.println(" 6. Filtrar por posición");
        System.out.println(" 7. Agregar jugador");
        System.out.println(" 8. Equipos (agrupar, añadir y sacar jugadores)");
        System.out.println(" 0. Salir");
        System.out.println("----------------------------------");
    }


    private void opcionCargarCsv() {
        String ruta = leerLinea("Ruta del CSV (Enter para usar " + ARCHIVO_POR_DEFECTO + "): ");
        if (ruta.length() >= 2 && ruta.startsWith("\"") && ruta.endsWith("\"")) {
            ruta = ruta.substring(1, ruta.length() - 1);
        }
        if (ruta.isEmpty()) {
            ruta = ARCHIVO_POR_DEFECTO;
        }
        try {
            cargarArchivo(Paths.get(ruta));
        } catch (InvalidPathException e) {
            System.out.println("La ruta no es válida: " + ruta);
        }
    }

    private void opcionBuscarPorNombre() {
        if (sinJugadores()) {
            return;
        }
        String texto = leerLinea("Nombre a buscar: ");
        if (texto.isEmpty()) {
            System.out.println("Escribe al menos una letra del nombre.");
            return;
        }

        List<Jugador> resultado = buscarPorNombre(texto);
        if (resultado.isEmpty()) {
            System.out.println("No se encontró ningún jugador con ese nombre.");
        } else if (resultado.size() == 1) {
            mostrarFicha(resultado.get(0));
        } else {
            mostrarTabla(resultado);
            System.out.println("Varios jugadores coinciden. Usa la opción 4 con un ID para ver la ficha completa.");
        }
    }

    private void opcionBuscarPorId() {
        if (sinJugadores()) {
            return;
        }
        String id = leerLinea("ID a buscar: ");
        Jugador j = buscarPorId(id);
        if (j == null) {
            System.out.println("No existe ningún jugador con el ID " + id + ".");
        } else {
            mostrarFicha(j);
        }
    }

    private void opcionFiltrarPorPais() {
        if (sinJugadores()) {
            return;
        }
        String texto = leerLinea("País a buscar: ");
        if (texto.isEmpty()) {
            System.out.println("Escribe el país.");
            return;
        }
        mostrarTabla(buscarPorPais(texto));
    }

    private void opcionFiltrarPorPosicion() {
        if (sinJugadores()) {
            return;
        }
        String texto = leerLinea("Posición a buscar (por ejemplo: delantero): ");
        if (texto.isEmpty()) {
            System.out.println("Escribe la posición.");
            return;
        }
        mostrarTabla(buscarPorPosicion(texto));
    }

    private void opcionAgregarJugador() {
        System.out.println("--- Agregar jugador ---");

        String nombre = leerLinea("Nombre: ");
        while (nombre.isEmpty()) {
            nombre = leerLinea("El nombre no puede estar vacío. Nombre: ");
        }
        String pais = leerLinea("País: ");
        LocalDate nacimiento = leerFecha("Fecha de nacimiento (AAAA-MM-DD): ");

        if (existe(nombre, nacimiento)) {
            System.out.println("Ya existe un jugador con ese nombre y esa fecha de nacimiento. No se agregó.");
            return;
        }

        int dorsal = leerEnteroEnRango("Dorsal (0-99): ", 0, 99);
        Posicion posicion = leerPosicion();
        boolean vivo = leerSiNo("¿Está vivo? (s/n): ");

        Jugador jugador = new Jugador(nombre, nacimiento);
        jugador.setCountry(pais);
        jugador.dorsal = dorsal;
        jugador.posicion = posicion;
        jugador.is_alive = vivo;
        listaJugadores.add(jugador);

        System.out.println("Jugador agregado con ID " + jugador.getId() + ".");
        mostrarFicha(jugador);
    }


    private void menuEquipos() {
        if (sinJugadores()) {
            return;
        }
        int opcion;
        do {
            mostrarMenuEquipos();
            opcion = leerEntero("Elige una opción: ");
            System.out.println();

            switch (opcion) {
                case 1:
                    opcionAgruparAutomatico();
                    break;
                case 2:
                    opcionCrearEquipo();
                    break;
                case 3:
                    opcionVerEquipos();
                    break;
                case 4:
                    opcionVerPlantilla();
                    break;
                case 5:
                    opcionAnadirJugadorAEquipo();
                    break;
                case 6:
                    opcionSacarJugadorDeEquipo();
                    break;
                case 7:
                    opcionJugadoresSinEquipo();
                    break;
                case 8:
                    opcionDisolverEquipo();
                    break;
                case 0:
                    System.out.println("Volviendo al menú principal.");
                    break;
                default:
                    System.out.println("Opción no válida. Elige un número del menú.");
            }
        } while (opcion != 0);
    }

    private void mostrarMenuEquipos() {
        System.out.println();
        System.out.println("==================================");
        System.out.println("             EQUIPOS");
        System.out.println("==================================");
        System.out.println("Equipos: " + listaEquipos.size() + " | Jugadores sin equipo: " + jugadoresSinEquipo().size());
        System.out.println(" 1. Agrupar jugadores sin equipo en equipos de " + Equipo.JUGADORES_POR_EQUIPO);
        System.out.println(" 2. Crear un equipo vacío");
        System.out.println(" 3. Ver equipos");
        System.out.println(" 4. Ver la plantilla de un equipo");
        System.out.println(" 5. Añadir un jugador a un equipo");
        System.out.println(" 6. Sacar un jugador de un equipo");
        System.out.println(" 7. Ver jugadores sin equipo");
        System.out.println(" 8. Disolver un equipo");
        System.out.println(" 0. Volver al menú principal");
        System.out.println("----------------------------------");
    }

    private void opcionAgruparAutomatico() {
        List<Jugador> libres = jugadoresSinEquipo();
        int equiposNuevos = libres.size() / Equipo.JUGADORES_POR_EQUIPO;
        int sobran = libres.size() % Equipo.JUGADORES_POR_EQUIPO;

        if (equiposNuevos == 0) {
            System.out.println("Hacen falta al menos " + Equipo.JUGADORES_POR_EQUIPO
                    + " jugadores sin equipo. Ahora hay " + libres.size() + ".");
            return;
        }

        System.out.println("Hay " + libres.size() + " jugadores sin equipo. Se crearán " + equiposNuevos
                + " equipos de " + Equipo.JUGADORES_POR_EQUIPO + " y quedarán " + sobran + " sin equipo.");
        if (!leerSiNo("¿Continuar? (s/n): ")) {
            System.out.println("No se creó ningún equipo.");
            return;
        }

        int posicion = 0;
        for (int e = 0; e < equiposNuevos; e++) {
            Equipo equipo = new Equipo(siguienteNombreDeEquipo());
            for (int k = 0; k < Equipo.JUGADORES_POR_EQUIPO; k++) {
                equipo.agregarJugador(libres.get(posicion));
                posicion++;
            }
            listaEquipos.add(equipo);
        }
        System.out.println("Se crearon " + equiposNuevos + " equipos. Usa la opción 3 para verlos.");
    }

    private void opcionCrearEquipo() {
        String nombre = leerLinea("Nombre del equipo: ");
        if (nombre.isEmpty()) {
            System.out.println("El nombre no puede estar vacío.");
            return;
        }
        if (buscarEquipoPorNombre(nombre) != null) {
            System.out.println("Ya existe un equipo con ese nombre.");
            return;
        }
        listaEquipos.add(new Equipo(nombre));
        System.out.println("Equipo \"" + nombre + "\" creado (0/" + Equipo.JUGADORES_POR_EQUIPO + ").");
    }

    private void opcionVerEquipos() {
        if (listaEquipos.isEmpty()) {
            System.out.println("Todavía no hay equipos. Usa la opción 1 o la 2.");
            return;
        }
        System.out.printf("%-4s %-24s %-10s %-11s%n", "Nº", "Equipo", "Jugadores", "Estado");
        System.out.println("-".repeat(52));
        for (int i = 0; i < listaEquipos.size(); i++) {
            Equipo e = listaEquipos.get(i);
            System.out.printf("%-4d %-24s %-10s %-11s%n",
                    i + 1,
                    e.getTeamName(),
                    textoPlantilla(e),
                    e.estaCompleto() ? "Completo" : "Incompleto");
        }
        System.out.println("-".repeat(52));
    }

    private void opcionVerPlantilla() {
        Equipo equipo = elegirEquipo();
        if (equipo == null) {
            return;
        }
        System.out.println(equipo.getTeamName() + " (" + textoPlantilla(equipo) + ")");
        mostrarTabla(equipo.getPlantilla());
    }

    private void opcionAnadirJugadorAEquipo() {
        Equipo equipo = elegirEquipo();
        if (equipo == null) {
            return;
        }
        if (equipo.estaCompleto()) {
            System.out.println(equipo.getTeamName() + " ya está completo (" + textoPlantilla(equipo)
                    + "). Saca a un jugador primero.");
            return;
        }

        System.out.println("Consejo: la opción 7 muestra los jugadores que no tienen equipo.");
        String id = leerLinea("ID del jugador a añadir: ");
        Jugador jugador = buscarPorId(id);
        if (jugador == null) {
            System.out.println("No existe ningún jugador con el ID " + id + ".");
            return;
        }

        Equipo actual = equipoDe(jugador);
        if (actual != null) {
            System.out.println(jugador.name + " ya está en " + actual.getTeamName() + ". Sácalo de ese equipo primero.");
            return;
        }

        equipo.agregarJugador(jugador);
        System.out.println(jugador.name + " se añadió a " + equipo.getTeamName() + " (" + textoPlantilla(equipo) + ").");
    }

    private void opcionSacarJugadorDeEquipo() {
        Equipo equipo = elegirEquipo();
        if (equipo == null) {
            return;
        }
        if (equipo.cantidadJugadores() == 0) {
            System.out.println(equipo.getTeamName() + " no tiene jugadores.");
            return;
        }

        mostrarTabla(equipo.getPlantilla());
        String id = leerLinea("ID del jugador a sacar: ");
        Jugador jugador = buscarPorId(id);
        if (jugador == null || !equipo.contiene(jugador)) {
            System.out.println("El ID " + id + " no está en " + equipo.getTeamName() + ".");
            return;
        }

        equipo.sacarJugador(jugador);
        System.out.println(jugador.name + " salió de " + equipo.getTeamName() + " (" + textoPlantilla(equipo)
                + "). Ahora no tiene equipo.");
    }

    private void opcionJugadoresSinEquipo() {
        List<Jugador> libres = jugadoresSinEquipo();
        if (libres.isEmpty()) {
            System.out.println("Todos los jugadores tienen equipo.");
            return;
        }
        System.out.println("Jugadores sin equipo:");
        mostrarTabla(libres);
    }

    private void opcionDisolverEquipo() {
        Equipo equipo = elegirEquipo();
        if (equipo == null) {
            return;
        }
        if (!leerSiNo("¿Disolver " + equipo.getTeamName() + "? Sus " + equipo.cantidadJugadores()
                + " jugador(es) quedarán sin equipo (s/n): ")) {
            System.out.println("No se disolvió el equipo.");
            return;
        }
        listaEquipos.remove(equipo);
        System.out.println(equipo.getTeamName() + " se disolvió.");
    }

    private Equipo elegirEquipo() {
        if (listaEquipos.isEmpty()) {
            System.out.println("Todavía no hay equipos. Usa la opción 1 o la 2.");
            return null;
        }
        for (int i = 0; i < listaEquipos.size(); i++) {
            Equipo e = listaEquipos.get(i);
            System.out.println(" " + (i + 1) + ". " + e.getTeamName() + " (" + textoPlantilla(e) + ")");
        }
        int n = leerEnteroEnRango("Elige el equipo: ", 1, listaEquipos.size());
        return listaEquipos.get(n - 1);
    }

    private String textoPlantilla(Equipo equipo) {
        return equipo.cantidadJugadores() + "/" + Equipo.JUGADORES_POR_EQUIPO;
    }

    private Equipo equipoDe(Jugador jugador) {
        for (Equipo e : listaEquipos) {
            if (e.contiene(jugador)) {
                return e;
            }
        }
        return null;
    }

    private List<Jugador> jugadoresSinEquipo() {
        List<Jugador> libres = new ArrayList<>();
        for (Jugador j : listaJugadores) {
            if (equipoDe(j) == null) {
                libres.add(j);
            }
        }
        return libres;
    }

    private Equipo buscarEquipoPorNombre(String nombre) {
        String consulta = normalizar(nombre);
        for (Equipo e : listaEquipos) {
            if (normalizar(e.getTeamName()).equals(consulta)) {
                return e;
            }
        }
        return null;
    }

    private String siguienteNombreDeEquipo() {
        int n = listaEquipos.size() + 1;
        while (buscarEquipoPorNombre("Equipo " + n) != null) {
            n++;
        }
        return "Equipo " + n;
    }


    private void mostrarTabla(List<Jugador> jugadores) {
        if (jugadores.isEmpty()) {
            System.out.println("No hay jugadores para mostrar.");
            return;
        }
        System.out.printf("%-5s %-24s %-18s %-7s %-15s %-10s %-5s%n",
                "ID", "Nombre", "País", "Dorsal", "Posición", "Estado", "Edad");
        System.out.println("-".repeat(90));
        for (Jugador j : jugadores) {
            System.out.printf("%-5s %-24s %-18s %-7d %-15s %-10s %-5s%n",
                    j.getId(),
                    j.name,
                    textoOGuion(j.getCountry()),
                    j.dorsal,
                    j.posicion == null ? "-" : j.posicion.toString(),
                    j.is_alive ? "Vivo" : "Fallecido",
                    j.is_alive ? String.valueOf(j.getEdad()) : "-"); // no hay fecha de fallecimiento para calcular la edad
        }
        System.out.println("-".repeat(90));
        System.out.println(jugadores.size() + " jugador(es).");
    }

    private void mostrarFicha(Jugador j) {
        System.out.println("----------------------------------");
        System.out.println("ID:          " + j.getId());
        System.out.println("Nombre:      " + j.name);
        System.out.println("País:        " + textoOGuion(j.getCountry()));
        System.out.println("Nacimiento:  " + j.getNacimiento());
        System.out.println("Estado:      " + (j.is_alive ? "Vivo" : "Fallecido"));
        System.out.println("Edad:        " + (j.is_alive ? j.getEdad() + " años" : "No disponible (fallecido)"));
        System.out.println("Dorsal:      " + j.dorsal);
        System.out.println("Posición:    " + (j.posicion == null ? "-" : j.posicion.toString()));
        Equipo equipo = equipoDe(j);
        System.out.println("Equipo:      " + (equipo == null ? "Sin equipo" : equipo.getTeamName()));
        System.out.println("----------------------------------");
    }

    private String textoOGuion(String texto) {
        return (texto == null || texto.isEmpty()) ? "-" : texto;
    }

    private boolean sinJugadores() {
        if (listaJugadores.isEmpty()) {
            System.out.println("Todavía no hay jugadores. Usa la opción 1 para cargar el CSV.");
            return true;
        }
        return false;
    }

    private String leerLinea(String mensaje) {
        System.out.print(mensaje);
        if (!teclado.hasNextLine()) {
            throw new NoSuchElementException("Fin de la entrada");
        }
        return teclado.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número entero.");
            }
        }
    }

    private int leerEnteroEnRango(String mensaje, int minimo, int maximo) {
        while (true) {
            int n = leerEntero(mensaje);
            if (n >= minimo && n <= maximo) {
                return n;
            }
            System.out.println("El número debe estar entre " + minimo + " y " + maximo + ".");
        }
    }

    private LocalDate leerFecha(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje);
            try {
                LocalDate fecha = LocalDate.parse(texto);
                if (fecha.isAfter(LocalDate.now())) {
                    System.out.println("La fecha de nacimiento no puede ser futura.");
                } else {
                    return fecha;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido. Usa AAAA-MM-DD, por ejemplo 1998-05-21.");
            }
        }
    }

    private Posicion leerPosicion() {
        Posicion[] posiciones = Posicion.values();
        System.out.println("Posiciones:");
        for (int i = 0; i < posiciones.length; i++) {
            System.out.println(" " + (i + 1) + ". " + posiciones[i]);
        }
        int n = leerEnteroEnRango("Elige la posición: ", 1, posiciones.length);
        return posiciones[n - 1];
    }

    private boolean leerSiNo(String mensaje) {
        while (true) {
            String texto = normalizar(leerLinea(mensaje));
            if (texto.equals("s") || texto.equals("si") || texto.equals("y")) {
                return true;
            }
            if (texto.equals("n") || texto.equals("no")) {
                return false;
            }
            System.out.println("Responde s o n.");
        }
    }


    private void cargarSiExiste(Path archivo) {
        if (Files.exists(archivo)) {
            cargarArchivo(archivo);
        } else {
            System.out.println("No se encontró " + archivo.getFileName() + " en la carpeta del proyecto.");
            System.out.println("Usa la opción 1 para cargar un archivo.");
        }
    }

    private void cargarArchivo(Path archivo) {
        if (!Files.exists(archivo)) {
            System.out.println("No se encontró el archivo: " + archivo.toAbsolutePath());
            return;
        }
        try {
            ResultadoCarga r = cargarCSV(archivo);

            if (!listaEquipos.isEmpty()) {
                System.out.println("Se disolvieron los " + listaEquipos.size() + " equipos porque se cargó un archivo nuevo.");
                listaEquipos.clear();
            }

            long fallecidos = 0;
            for (Jugador j : listaJugadores) {
                if (!j.is_alive) {
                    fallecidos++;
                }
            }
            System.out.println("Se cargaron " + r.cargados + " jugadores de " + archivo.getFileName()
                    + " (" + (r.cargados - fallecidos) + " vivos, " + fallecidos + " fallecidos).");
            if (r.repetidos > 0) {
                System.out.println(r.repetidos + " fila(s) repetida(s) (mismo nombre y fecha de nacimiento) se ignoraron.");
            }
            if (r.omitidos > 0) {
                System.out.println(r.omitidos + " fila(s) se omitieron por tener datos inválidos.");
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo: " + e.getMessage());
        }
    }

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
                LocalDate nacimiento = parsearFecha(campo(c, iNacimiento));

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


    private List<Jugador> buscarPorNombre(String texto) {
        String consulta = normalizar(texto);
        List<Jugador> resultado = new ArrayList<>();
        for (Jugador j : listaJugadores) {
            if (normalizar(j.name).contains(consulta)) {
                resultado.add(j);
            }
        }
        return resultado;
    }

    private Jugador buscarPorId(String id) {
        for (Jugador j : listaJugadores) {
            if (j.getId().equals(id.trim())) {
                return j;
            }
        }
        return null;
    }

    private List<Jugador> buscarPorPais(String texto) {
        String consulta = normalizar(texto);
        List<Jugador> resultado = new ArrayList<>();
        for (Jugador j : listaJugadores) {
            if (normalizar(j.getCountry()).contains(consulta)) {
                resultado.add(j);
            }
        }
        return resultado;
    }

    private List<Jugador> buscarPorPosicion(String texto) {
        String consulta = normalizar(texto);
        List<Jugador> resultado = new ArrayList<>();
        for (Jugador j : listaJugadores) {
            if (j.posicion != null && normalizar(j.posicion.name()).contains(consulta)) {
                resultado.add(j);
            }
        }
        return resultado;
    }

    private boolean existe(String nombre, LocalDate nacimiento) {
        String nombreNormalizado = normalizar(nombre);
        for (Jugador j : listaJugadores) {
            if (normalizar(j.name).equals(nombreNormalizado) && nacimiento.equals(j.getNacimiento())) {
                return true;
            }
        }
        return false;
    }

    private static String normalizar(String s) {
        if (s == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.trim().toLowerCase();
    }
}
