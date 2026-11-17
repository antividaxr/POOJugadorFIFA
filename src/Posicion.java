public enum Posicion {
    PORTERO,
    DEFENSA,
    DELANTERO,
    CENTROCAMPISTA,
    LATERAL,
    EXTREMO,
    VOLANTE;

    @Override
    public String toString() {
        String n = name();
        return n.charAt(0) + n.substring(1).toLowerCase();
    }
}
