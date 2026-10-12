package br.com.fiap.bank.plataforma.repository;

import br.com.fiap.bank.plataforma.model.SolicitacaoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitacaoCreditoRepository extends JpaRepository<SolicitacaoCredito, Long> {
    List<SolicitacaoCredito> findByCpfClienteOrderByDataSolicitacaoDesc(String cpfCliente);
    List<SolicitacaoCredito> findAllByOrderByDataSolicitacaoDesc();
}
