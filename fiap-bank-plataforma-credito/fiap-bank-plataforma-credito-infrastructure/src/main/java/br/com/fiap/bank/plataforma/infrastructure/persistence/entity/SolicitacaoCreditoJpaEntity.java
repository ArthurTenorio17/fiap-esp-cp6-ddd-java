package br.com.fiap.bank.plataforma.infrastructure.persistence.entity;

import br.com.fiap.bank.plataforma.domain.model.StatusSolicitacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_solicitacao_credito")
public class SolicitacaoCreditoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cpf_cliente", nullable = false, length = 11)
    private String cpfCliente;

    @Column(name = "nome_cliente", nullable = false)
    private String nomeCliente;

    @Column(name = "tipo_credito", nullable = false)
    private String tipoCredito;

    @Column(name = "valor_solicitado", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorSolicitado;

    @Column(name = "quantidade_parcelas", nullable = false)
    private Integer quantidadeParcelas;

    @Column(name = "renda_mensal", nullable = false, precision = 15, scale = 2)
    private BigDecimal rendaMensal;

    @Column(name = "score_credito")
    private Integer scoreCredito;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status;

    @Column(name = "parecer_operador", length = 500)
    private String parecerOperador;

    @Column(name = "scr_restricoes")
    private Boolean scrRestricoes;

    @Column(name = "score_bureau")
    private Integer scoreBureau;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
}
