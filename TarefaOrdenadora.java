public class TarefaOrdenadora extends Thread
{
    private byte[] pedaco;
    private String rotulo;

    public TarefaOrdenadora (byte[] pedaco, String rotulo) throws Exception
    {
        if (pedaco==null)
            throw new Exception ("Pedaco ausente");

        if (pedaco.length==0)
            throw new Exception ("Pedaco vazio");

        if (rotulo==null || rotulo.equals(""))
            throw new Exception ("Rotulo ausente");

        this.pedaco = pedaco;
        this.rotulo = rotulo;
    }

    private boolean pronto = false;

    // ordena, de forma tradicional e recursiva, o trecho de vetor
    // que vai da posicao inicio a posicao fim (merge sort sequencial)
    private void ordene (byte[] vetor, int inicio, int fim)
    {
        if (inicio>=fim)
            return;

        int meio = (inicio+fim)/2;

        this.ordene (vetor, inicio, meio);
        this.ordene (vetor, meio+1, fim);
        this.intercale (vetor, inicio, meio, fim);
    }

    private void intercale (byte[] vetor, int inicio, int meio, int fim)
    {
        byte[] esquerda = new byte [meio-inicio+1];
        byte[] direita  = new byte [fim-meio];

        for (int i=0; i<esquerda.length; i++)
            esquerda[i] = vetor[inicio+i];

        for (int i=0; i<direita.length; i++)
            direita[i] = vetor[meio+1+i];

        int i=0, j=0, k=inicio;

        while (i<esquerda.length && j<direita.length)
            if (esquerda[i]<=direita[j])
                vetor[k++] = esquerda[i++];
            else
                vetor[k++] = direita[j++];

        while (i<esquerda.length)
            vetor[k++] = esquerda[i++];

        while (j<direita.length)
            vetor[k++] = direita[j++];
    }

    @Override
    public void run ()
    {
        System.out.println ("[log] "+this.rotulo+": iniciando ordenacao de "+this.pedaco.length+" elementos...");

        this.ordene (this.pedaco, 0, this.pedaco.length-1);
        this.pronto = true;

        System.out.println ("[log] "+this.rotulo+": ordenacao concluida.");
    }

    // so pode ser chamado depois de dar join nesta tarefa
    public byte[] getOrdenado () throws Exception
    {
        if (!this.pronto)
            throw new Exception ("Pedaco ainda nao foi ordenado (de join na tarefa antes)");

        return this.pedaco;
    }
}
