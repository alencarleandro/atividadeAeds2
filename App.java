import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

	/** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;
    
    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Quantidade de produtos cadastrados atualmente na lista */
    static int quantosProdutos = 0;

    static ABB<Integer, Produto> produtosPorId;
    static ABB<String, Produto> produtosPorNome;
    static TabelaHash<Produto, Lista<Pedido>> pedidosPorProduto;
    static Lista<Cliente> clientes;
    static TabelaHash<Integer, Cliente> clientesPorId;

    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        limparTela();
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }
    
    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {
        
    	T valor;
        
    	System.out.println(mensagem);
    	try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
        		| InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }
    
    /** Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * Perceba que poderia haver uma melhor modularização com a criação de uma classe Menu.
     * @return Um inteiro com a opção do usuário.
    */
    static int menu() {
        cabecalho();
        System.out.println("1 - Procurar produtos, por id");
        System.out.println("2 - Recortar produtos, por descrição");
        System.out.println("3 - Pedidos de um produto, em arquivo");
        System.out.println("4 - Relatório de clientes, em arquivo");
        System.out.println("5 - Relatório de pedidos, em arquivo");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        return Integer.parseInt(teclado.nextLine());
    }
    
    /**
     * Lê os dados de um arquivo-texto e retorna uma árvore de produtos. Arquivo-texto no formato
     * N (quantidade de produtos) <br/>
     * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
     * Deve haver uma linha para cada um dos produtos. Retorna uma árvore vazia em caso de problemas com o arquivo.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Uma árvore com os produtos carregados, ou vazia em caso de problemas de leitura.
     */
    static <T> ABB<T,Produto> lerProdutos(String nomeArquivoDados,
                                        Function<Produto,T> extratorDeChave) {
    	
    	Scanner arquivo = null;
    	int numProdutos;
    	String linha;
    	Produto produto;
    	ABB<T,Produto> produtosCadastrados;
    	
    	try {
    		arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));
    		
    		numProdutos = Integer.parseInt(arquivo.nextLine());
    		produtosCadastrados = new AVL<>();
    		
    		for (int i = 0; i < numProdutos; i++) {
    			linha = arquivo.nextLine();
    			produto = Produto.criarDoTexto(linha);
                T chave = extratorDeChave.apply(produto);
    			produtosCadastrados.inserir(chave, produto);
    		}
    		quantosProdutos = produtosCadastrados.tamanho();
    		
    	} catch (IOException excecaoArquivo) {
    		produtosCadastrados = null;
    	} finally {
    		arquivo.close();
    	}
    	
    	return produtosCadastrados;
    }
    
    /** Localiza um produto na árvore de produtos organizados por id, a partir do código de produto informado pelo usuário, e o retorna. 
    *  Em caso de não encontrar o produto, retorna null */
    static Produto localizarProdutoID() {
        cabecalho();
        System.out.println("LOCALIZANDO POR ID");
        int ID = lerOpcao("Digite o ID para busca", Integer.class);
        Produto localizado =  localizarProduto(produtosPorId, ID);
        mostrarProduto(localizado);
        return localizado;
    }
    
    static <K> Produto localizarProduto(ABB<K, Produto> produtosCadastrados, K chave){
        cabecalho();
        Produto localizado =  produtosCadastrados.pesquisar(chave);
        System.out.println("Tempo: " +produtosCadastrados.getTempo());
        System.out.println("Comparações: " +produtosCadastrados.getComparacoes());
        pausa();
        return localizado;
    }


    
    private static void mostrarProduto(Produto produto) {
    	
        cabecalho();
        String mensagem = "Dados inválidos para o produto!";
        
        if (produto != null){
            mensagem = String.format("Dados do produto:\n%s", produto);
        }
        
        System.out.println(mensagem);
    }

    private static void gerarClientes(int quantidade) {
        clientes = new Lista<>();
        clientesPorId = new TabelaHash<>(quantidade * 2);
        Random random = new Random(42);
        for (int i = 0; i < quantidade; i++) {
            String nome = "Cliente" + (i + 1);
            Cliente cliente = new Cliente(nome);
            clientes.inserir(cliente);
            clientesPorId.inserir(cliente.getId(), cliente);
        }
    }

    private static void gerarPedidosClientes(int quantidadePedidos) {
        Random sorteio = new Random(42);
        for (int i = 0; i < quantidadePedidos; i++) {
            Pedido ped = new Pedido();
            int quantProdutos = sorteio.nextInt(8) + 1;
            for (int j = 0; j < quantProdutos; j++) {
                int id = sorteio.nextInt(7750) + 10_000;
                Produto prod = produtosPorId.pesquisar(id);
                ped.incluirProduto(prod);
                inserirNaTabela(prod, ped);
            }
            // Associar pedido a um cliente aleatório
            int idCliente = sorteio.nextInt(clientes.tamanho()) + 1;
            Cliente cliente = clientesPorId.pesquisar(idCliente);
            cliente.adicionarPedido(ped);
        }
    }

    private static void relatorioClientes() {
        String nomeArquivo = "RelatorioClientes.txt";
        try (FileWriter arquivoRelatorio = new FileWriter(nomeArquivo)) {
            arquivoRelatorio.append(clientes.toString()).append("\n");
            System.out.println("Relatório de clientes salvo em " + nomeArquivo);
        } catch (IOException e) {
            System.out.println("Problemas para criar o arquivo " + nomeArquivo + ". Tente novamente");
        }
    }

    private static void relatorioPedidos() {
        String nomeArquivo = "RelatorioPedidos.txt";
        try (FileWriter arquivoRelatorio = new FileWriter(nomeArquivo)) {
            for (int i = 0; i < clientes.tamanho(); i++) {
                Cliente cliente = clientesPorId.pesquisar(i + 1);
                arquivoRelatorio.append("Cliente: ").append(cliente.getNome()).append("\n");
                Lista<Pedido> pedidos = cliente.getPedidos();
                if (pedidos.vazia()) {
                    arquivoRelatorio.append("  Nenhum pedido cadastrado.\n");
                } else {
                    arquivoRelatorio.append(pedidos.toString()).append("\n");
                }
            }
            System.out.println("Relatório de pedidos salvo em " + nomeArquivo);
        } catch (IOException e) {
            System.out.println("Problemas para criar o arquivo " + nomeArquivo + ". Tente novamente");
        }
    }

    private static void inserirNaTabela(Produto produto, Pedido pedido){
        Lista<Pedido> pedidosDoProduto;
        try{
            pedidosDoProduto = pedidosPorProduto.pesquisar(produto);
        }catch(NoSuchElementException nex){
            pedidosDoProduto = new Lista<>();
            pedidosPorProduto.inserir(produto, pedidosDoProduto);
        }
        pedidosDoProduto.inserir(pedido);
    }

    private static void recortarArvore(ABB<String, Produto> arvore) {
    	
        cabecalho();
        System.out.print("Digite ponto de início do filtro: ");
        String descIni =teclado.nextLine();
        System.out.print("Digite ponto de fim do filtro: ");
        String descFim =teclado.nextLine();
        
        System.out.println(arvore.recortar(descIni, descFim));
    }
    
    static void pedidosDoProduto(){
        Produto produto = localizarProdutoID();
        String nomeArquivo = "RelatorioProduto"+produto.hashCode()+".txt";    
        try (FileWriter arquivoRelatorio = new FileWriter(nomeArquivo)){
            Lista<Pedido> listaProd = pedidosPorProduto.pesquisar(produto);
            arquivoRelatorio.append(listaProd+"\n");
            arquivoRelatorio.close();
            System.out.println("Dados salvos em "+nomeArquivo);
        } catch (IOException e) {
            System.out.println("Problemas para criar o arquivo "+nomeArquivo+". Tente novamente");
        }
    }
    public static void main(String[] args) {
		teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        produtosPorId = lerProdutos(nomeArquivoDados, Produto::hashCode);
        produtosPorNome = new AVL<>(produtosPorId, prod -> prod.descricao, String::compareTo);
        pedidosPorProduto = new TabelaHash<>((int)(quantosProdutos*1.25));
        gerarClientes(100); // Gera 100 clientes
        gerarPedidosClientes(25000); // Gera 25000 pedidos ligados a clientes
       
        int opcao = -1;
        
        do{
            opcao = menu();
            switch (opcao) {
                case 1 -> localizarProdutoID();
                case 2 -> recortarArvore(produtosPorNome);
                case 3 -> pedidosDoProduto();
                case 4 -> relatorioClientes();
                case 5 -> relatorioPedidos();
             }
            pausa();
        }while(opcao != 0);       

        teclado.close();    
    }
}
