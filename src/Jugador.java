import java.time.LocalDate;
import java.time.Period;

public class Jugador extends Persona {
    public int dorsal;
    public Posicion posicion;
    private LocalDate nacimiento;

    public Jugador(String nombre, LocalDate nacimiento) {
        super(nombre);
        this.nacimiento = nacimiento;
    }

    public LocalDate getNacimiento() {
        return nacimiento;
    }

    public void setNacimiento(LocalDate nacimiento) {
        this.nacimiento = nacimiento;
    }

    public int getEdad() {
        if (nacimiento == null) {
            return 0;
        }
        return Period.between(nacimiento, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        return "Jugador{" +
                "dorsal=" + dorsal +
                "} " + super.toString();
    }
}
