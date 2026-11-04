import java.time.LocalDate;

import javax.swing.*;
import java.nio.file.Paths;

public class Main {
    // Se busca en la carpeta raÃ­z del proyecto (junto a src). TambiÃ©n puedes usar el botÃ³n "Cargar CSV...".
    private static final String ARCHIVO_CSV = "jugadores_futbol_nombre_unico.csv";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si falla, Swing usa su aspecto por defecto.
        }

        SwingUtilities.invokeLater(() -> {
            Pantalla pantalla = new Pantalla();
            pantalla.cargarSiExiste(Paths.get(ARCHIVO_CSV));
            pantalla.setVisible(true);
        });
    }
}
