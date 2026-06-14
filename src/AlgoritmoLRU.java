import java.util.LinkedHashMap;

/**
 * Algoritmo LRU (Least Recently Used).
 *
 * La página víctima es la que fue usada hace más tiempo.
 * Se mantiene un registro del orden de uso — cada vez que
 * una página es referenciada (hit o carga), se mueve al
 * final del registro como la más recientemente usada.
 *
 * (menos recientemente usada).
 */
public class AlgoritmoLRU extends AlgoritmoReemplazo {

    // LinkedHashMap mantiene el orden de inserción/acceso
    // clave: página, valor: índice del marco donde está cargada
    private LinkedHashMap<Integer, Integer> ordenUso;

    public AlgoritmoLRU(int nroMarcos) {
        super(nroMarcos);
        // accessOrder=true: reordena automáticamente al acceder
        this.ordenUso = new LinkedHashMap<>(nroMarcos, 0.75f, true);
    }

    /**
     * Verifica si la página está en memoria.
     * Si es un hit, actualiza su posición en el registro de uso
     * (LinkedHashMap con accessOrder=true lo hace automáticamente al get()).
     */
    @Override
    public boolean hayFallo(int pagina) {
        if (!ordenUso.containsKey(pagina)) return true;

        // Hit: acceder a la clave la mueve al final automáticamente
        ordenUso.get(pagina);
        return false;
    }

    /**
     * Carga la página en un marco libre y la registra como
     * la más recientemente usada.
     */
    @Override
    public void adquirir(int pagina) {
        int indiceLibre = buscarMarcoLibre();
        marcos[indiceLibre] = pagina;
        ordenUso.put(pagina, indiceLibre);
    }

    /**
     * Reemplaza la página menos recientemente usada (la primera
     * del LinkedHashMap) por la nueva página.
     */
    @Override
    protected void reemplazar(int pagina) {
        // La primera entrada es la menos recientemente usada
        int victima = ordenUso.keySet().iterator().next();
        int indiceMarco = ordenUso.remove(victima);

        marcos[indiceMarco] = pagina;
        ordenUso.put(pagina, indiceMarco);
    }

    // --- Helpers ---

    private int buscarMarcoLibre() {
        for (int i = 0; i < nroMarcos; i++) {
            if (marcos[i] == -1) return i;
        }
        return -1; // nunca debería llegar aquí
    }

    @Override
    public String getNombreAlgoritmo() {
        return "LRU (Least Recently Used)";
    }
}