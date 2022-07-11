package com.ufes.delivery.model.state;

import com.ufes.delivery.desconto.ProcessadoraDesconto;
import com.ufes.delivery.imposto.ProcessadoraImposto;
import com.ufes.delivery.model.EventoPedido;
import com.ufes.delivery.model.ItemPedido;
import com.ufes.delivery.model.Pedido;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author clayton
 */
public class PedidoNovoState extends PedidoState {

    public PedidoNovoState(Pedido pedido) {
        super(pedido, "Novo");
    }

    @Override
    public void incluir(ItemPedido item) {
        existeItem(item.getNomeProduto());
        this.pedido.add(item);
        this.pedido.add(new EventoPedido(LocalDateTime.now(), "Item incluído no pedido: " + item.getNomeProduto()));
    }

    private void existeItem(String nome) {
        List<ItemPedido> itens = this.pedido.getItens();
        for (ItemPedido item : itens) {
            if (item.getNomeProduto().equalsIgnoreCase(nome)) {
                throw new RuntimeException("Produto " + nome + " já existe no pedido!");
            }
        }
    }

    @Override
    public void removeItem(String nome) {
        List<ItemPedido> itens = this.pedido.getItens();
        for (ItemPedido item : itens) {
            if (item.getNomeProduto().equalsIgnoreCase(nome)) {
                itens.remove(item);
                this.pedido.add(new EventoPedido(LocalDateTime.now(), "Item removido do pedido: " + item.getNomeProduto()));
            }
        }
        throw new RuntimeException("Item " + nome + " não existe");
    }

    @Override
    public void concluir() {
        if(!super.pedido.getItens().isEmpty()) {
            ProcessadoraDesconto processadoraDesconto = new ProcessadoraDesconto();
            processadoraDesconto.processar(this.pedido);

            ProcessadoraImposto processadoraImposto = new ProcessadoraImposto();
            processadoraImposto.processar(this.pedido);

            this.pedido.setState(new PedidoAguardandoPagamentoState(this.pedido));
        }else{
            throw new RuntimeException("O pedido não pode ser confirmado sem nenhum item na lista");
        }
    }

    @Override
    public void cancelar() {
        this.pedido.setState(new PedidoCanceladoState(this.pedido, "Novo pedido cancelado pelo cliente"));
    }
}
