public class Capacidade extends Comunicado
{
    private int processadores;

    public Capacidade (int processadores) throws Exception
    {
        if (processadores<1)
            throw new Exception ("Quantidade de processadores invalida");

        this.processadores = processadores;
    }

    public int getProcessadores ()
    {
        return this.processadores;
    }

    public String toString ()
    {
        return (this.processadores+" processador(es)");
    }
}
