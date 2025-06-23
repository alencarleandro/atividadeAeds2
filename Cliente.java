import java.util.Random;

public class Cliente {
    private static int ultimoId = 1;
    private int id;
    private String nome;
    private Lista<Pedido> pedidos;

    public Cliente(String nome) {
        this.id = ultimoId++;
        this.nome = nome;
        this.pedidos = new Lista<>();
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Lista<Pedido> getPedidos() {
        return pedidos;
    }

    public void adicionarPedido(Pedido pedido) {
        pedidos.inserir(pedido);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cliente ID: ").append(id).append(", Nome: ").append(nome);
        sb.append("\nPedidos:");
        if (pedidos.vazia()) {
            sb.append(" Nenhum pedido cadastrado.");
        } else {
            sb.append(pedidos.toString());
        }
        return sb.toString();
    }
} 