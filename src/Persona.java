public class Persona {
    public String name;
    private String country;
    private String id;
    public boolean is_alive = true;
    private static int contadorGeneral = 0;

    public Persona(String name) {
        this.name = name;
        contadorGeneral++;
        this.id = String.valueOf(contadorGeneral);
    }

        public static void reiniciarContador() {
        contadorGeneral = 0;
    }
    public static void fijarContador(int valor) {
        contadorGeneral = valor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String newCountry) {
        this.country = newCountry;
    }

    @Override
    public String toString() {
        return "Persona{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", is_alive=" + is_alive +
                '}';
    }
}
