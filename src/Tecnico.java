public class Tecnico extends Persona {
    public String club;

    public Tecnico(String name, String club) {
        super(name);
        this.club = club;
    }

    @Override
    public String toString() {
        return "Tecnico{" +
                "club='" + club + '\'' +
                "} " + super.toString();
    }
}

