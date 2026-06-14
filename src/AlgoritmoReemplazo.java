import java.util.ArrayList;
import java.util.List;

/**
 * Clase abstracta que define el contrato y la lógica común
 * para todos los algoritmos de reemplazo de páginas.
 *
 * Cada subclase debe implementar únicamente reemplazar(),
 * que define cómo elegir la página víctima cuando los marcos están llenos.
 */
public abstract class AlgoritmoReemplazo {

    // --- Estado compartido ---

    protected int[]  marcos;         // contenido actual de los marcos
    protected int    nroMarcos;      // cantidad de marcos disponibles
    protected int    fallosPagina;   // contador de fallos acumulados

    // Historial: cada entrada es el estado de los marcos en ese paso
    // null en una posición indica que no hubo cambio (hit)
    private List<int[]> historial;

    // ---

    public AlgoritmoReemplazo(int nroMarcos) {
        this.nroMarcos    = nroMarcos;
        this.marcos       = new int[nroMarcos];
        this.fallosPagina = 0;
        this.historial    = new ArrayList<>();

        inicializarMarcos();
    }

    // Inicializa todos los marcos a -1 (vacío)
    private void inicializarMarcos() {
        for (int i = 0; i < nroMarcos; i++) {
            marcos[i] = -1;
        }
    }

    // --- Métodos del flujo principal ---

    /**
     * Ejecuta la simulación completa sobre la secuencia dada.
     * Retorna el historial de estados para que la GUI lo renderice.
     */
    public List<int[]> simular(int[] secuencia) {
        for (int pagina : secuencia) {
            if (!hayFallo(pagina)) {
                registrarEstado(); // hit: guarda estado sin cambio
                continue;
            }

            fallosPagina++;

            if (hayMarcoLibre()) {
                adquirir(pagina);
            } else {
                reemplazar(pagina);
            }

            registrarEstado();
        }

        return historial;
    }

    /**
     * Verifica si la página está cargada en algún marco.
     * No modifica estado.
     */
    public boolean hayFallo(int pagina) {
        for (int marco : marcos) {
            if (marco == pagina) return false;
        }
        return true;
    }

    /**
     * Carga la página en el primer marco libre disponible.
     * Solo debe llamarse cuando hayMarcoLibre() es true.
     */
    public void adquirir(int pagina) {
        for (int i = 0; i < nroMarcos; i++) {
            if (marcos[i] != -1) continue; // guard clause: marco ocupado
            marcos[i] = pagina;
            return;
        }
    }

    /**
     * Guarda una copia del estado actual de los marcos en el historial.
     */
    private void registrarEstado() {
        historial.add(marcos.clone());
    }

    // --- Helpers ---

    private boolean hayMarcoLibre() {
        for (int marco : marcos) {
            if (marco == -1) return true;
        }
        return false;
    }

    // --- Getters ---

    public int getFallosPagina() {
        return fallosPagina;
    }

    public int[] getEstadoMarcos() {
        return marcos.clone();
    }

    public int getNroMarcos() {
        return nroMarcos;
    }

    // --- Método abstracto ---

    /**
     * Define la lógica de elección de víctima cuando los marcos están llenos.
     * Cada subclase implementa su propia estrategia.
     */
    protected abstract void reemplazar(int pagina);

    /**
     * Retorna el nombre del algoritmo para mostrar en la GUI.
     */
    public abstract String getNombreAlgoritmo();
}
