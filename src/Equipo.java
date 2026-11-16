import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Equipo {
    public static final int JUGADORES_POR_EQUIPO = 11;

    private String teamName;
    private final List<Jugador> plantilla = new ArrayList<>();
    private Tecnico tecnico_equipo; 
    public Equipo(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Tecnico getTecnico() {
        return tecnico_equipo;
    }

    public void setTecnico(Tecnico tecnico) {
        this.tecnico_equipo = tecnico;
    }

    public boolean tieneTecnico() {
        return tecnico_equipo != null;
    }

        public boolean asignarTecnico(Tecnico tecnico) {
        if (tecnico == null || tieneTecnico()) {
            return false;
        }
        tecnico_equipo = tecnico;
        return true;
    }

    
    public Tecnico sacarTecnico() {
        Tecnico saliente = tecnico_equipo;
        tecnico_equipo = null;
        return saliente;
    }    public List<Jugador> getPlantilla() {
        return Collections.unmodifiableList(plantilla);
    }

    public int cantidadJugadores() {
        return plantilla.size();
    }

    public boolean estaCompleto() {
        return plantilla.size() >= JUGADORES_POR_EQUIPO;
    }

    public boolean contiene(Jugador jugador) {
        return plantilla.contains(jugador);
    }

        public boolean agregarJugador(Jugador jugador) {
        if (jugador == null || estaCompleto() || contiene(jugador)) {
            return false;
        }
        plantilla.add(jugador);
        return true;
    }

        public boolean sacarJugador(Jugador jugador) {
        return plantilla.remove(jugador);
    }

    @Override
    public String toString() {
        return "Equipo{" +
                "teamName='" + teamName + '\'' +
                ", jugadores=" + plantilla.size() + "/" + JUGADORES_POR_EQUIPO +
                '}';
    }
}
