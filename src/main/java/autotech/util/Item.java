package autotech.util;

public class Item {

    private final int id;
    private final String texto;

    public Item(int id, String texto) {
        this.id = id;
        this.texto = texto;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return texto;
    }
}
