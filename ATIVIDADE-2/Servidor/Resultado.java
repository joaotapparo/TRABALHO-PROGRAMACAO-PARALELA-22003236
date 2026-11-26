// resultado em si de uma ordenacao/mergeamento: o vetor resultante
public class Resultado extends Comunicado
{
    private byte[] vetorResultante;

    public Resultado (byte[] vetorResultante) throws Exception
    {
        if (vetorResultante==null)
            throw new Exception ("Vetor resultante ausente");

        this.vetorResultante = vetorResultante;
    }

    public byte[] getVetorResultante ()
    {
        return this.vetorResultante;
    }

    public String toString ()
    {
        return ("vetor com "+this.vetorResultante.length+" elementos");
    }
}
